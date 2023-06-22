/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.softman;

import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * The class providing the business logic for this module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class SoftwareManagerService {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * The MetadataEntityManager instance.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * The BusinessEntityManager instance.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * The ApplicationEntityManager instance.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Relationship between the object and the license.
     */
    public static String RELATIONSHIP_LICENSEHAS = "licenseHas";
    
    /**
     * Relates an object or a set of objects to an existing software or hardware license.
     * @param licenseClass The class of the license to be created (temporarily).
     * @param licenseId The id of the license
     * @param objectClass The class of the object
     * @param objectId The id of the object
     * @return The id of the newly created license
     * @throws org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException if <code>licenseClass</code> is not a 
     * subclass of GenericSoftwareAsset.
     * @throws org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException if any of the classes provided do not exist.
     * @throws org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException If the business object provided does not exist.
     */
    public void relateObjectToLicense(String licenseClass, String licenseId, String objectClass, String objectId) 
            throws InvalidArgumentException, MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        try {
            bem.createSpecialRelationship(objectClass, objectId, licenseClass, licenseId, RELATIONSHIP_LICENSEHAS, false);
        } catch (OperationNotPermittedException ex) { /* Should not happen because there are not cardinality constraints */ }
    }
    
    /**
     * Gets all available license type, which are basically the non-abstract subclasses of <code>GenericSoftwareAsset</code>.
     * @return The list of available license types.
     * @throws MetadataObjectNotFoundException If the class <code>GenericSoftwareAsset</code> has not been defined
     */
    public List<ClassMetadataLight> getAllLicenseTypes() throws MetadataObjectNotFoundException {
        return mem.getSubClassesLight("GenericSoftwareAsset", false, false);
    }
    
    public List<BusinessObjectLight> getAllProducts() throws MetadataObjectNotFoundException {
        try {
            return aem.getListTypeItems("SoftwareType");
        } catch (InvalidArgumentException ex) { /* Should not happen because this class is a list type item */
            return Collections.EMPTY_LIST;
        }
    }
    
    public List<BusinessObjectLight> getAllLicenses() throws MetadataObjectNotFoundException {
        try {
            return bem.getObjectsOfClassLight("GenericSoftwareAsset", -1, -1);
        } catch (InvalidArgumentException ex) { /* Should not happen because this class is an inventory object */
            return Collections.EMPTY_LIST;
        }
    }
    
    /** 
     * Gets the licenses inside a license pool.
     * @param poolId The pool id.
     * @param limit The results limit per page. Use -1 to retrieve all.
     * @return The licenses list.
     * @throws ApplicationObjectNotFoundException If the pool can't be found.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid.
     */
    public List<BusinessObjectLight> getLicensesInPool(String poolId, int limit)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        return bem.getPoolItems(poolId, limit);
    }
    
    public String createLicense(String poolId, String licenseClass, String licenseName, String licenseProduct)
            throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException {
        String nowAsString = String.valueOf(Calendar.getInstance().getTimeInMillis());
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("type", licenseProduct);
        attributes.put("name", licenseName);
        attributes.put("active", "true");
        attributes.put("expirationDate", nowAsString);
        attributes.put("purchaseDate", nowAsString);
        return bem.createPoolItem(poolId, licenseClass, attributes, null);
    }
    
    public void deleteLicense(String licenseClass, String licenseId) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {
         bem.deleteObject(licenseClass, licenseId, true);
    }
    
    public void releaseLicense(String licenseClass, String licenseId) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        bem.releaseRelationships(licenseClass, licenseId, Arrays.asList(RELATIONSHIP_LICENSEHAS));
    }
    
    /**
     * Releases an object from another object whose relationship is "licenseHas".
     * @param sourceObjectClass The source object class.
     * @param sourceObjectId The source object id.
     * @param targetObjectId The target object id. The object we want to be released from.
     * @throws MetadataObjectNotFoundException If the object class provided can't be found.
     * @throws InvalidArgumentException If sourceObjectId or targetObjectId are null.
     * @throws BusinessObjectNotFoundException If any of the objects can't be found. 
     */
    public void releaseRelationship(String sourceObjectClass, String sourceObjectId, String targetObjectId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        bem.releaseSpecialRelationship(sourceObjectClass, sourceObjectId, targetObjectId, RELATIONSHIP_LICENSEHAS);
    }
    
    /**
     * Get license properties.
     * @param licenseClass The license class. Must be subclass of GenericSoftwareAsset.
     * @param licenseId The license id.
     * @return The license properties.
     * @throws MetadataObjectNotFoundException If the license class can't be found.
     * @throws InvalidArgumentException If the license id can't be found.
     * @throws BusinessObjectNotFoundException If the requested license can't be found.
     */
    public BusinessObject getLicense(String licenseClass, String licenseId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICSOFTWAREASSET, licenseClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), licenseClass, Constants.CLASS_GENERICSOFTWAREASSET));

        BusinessObject license = (BusinessObject) bem.getObject(licenseClass, licenseId);
        if (license == null)
                throw new BusinessObjectNotFoundException(String.format(ts.getTranslatedString("module.softman.license-id-not-found"), licenseId));
              
        return license;
    }
    
    /**
     * Creates a license pool.
     * @param poolName The pool name. Must be subclass of GenericSoftwareAsset.
     * @param poolDescription The pool description.
     * @param poolClass The pool class. What kind of objects can this pool contain?
     * @param userName The user name of the session.
     * @return The id of the newly created project pool.
     * @throws MetadataObjectNotFoundException If poolClass is not a valid subclass of GenericSoftwareAsset. 
     * @throws ApplicationObjectNotFoundException If the pool activity log can't be found.
     */
    public String createLicensePool(String poolName, String poolDescription, String poolClass, String userName)
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICSOFTWAREASSET, poolClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , poolClass, Constants.CLASS_GENERICSOFTWAREASSET));
        
        String poolId = aem.createRootPool(poolName, poolDescription, poolClass, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT
                , String.format(ts.getTranslatedString("module.softman.actions.new-pool.created-log")
                        , poolName, poolId, poolClass));
        
        return poolId;
    }
    
    /**
     * Deletes a license pool.
     * @param poolId The pool id.
     * @param poolClass The pool class.
     * @param userName The user name of the session.
     * @throws ApplicationObjectNotFoundException If the pool can't be found.
     * @throws OperationNotPermittedException If any of the objects in the pool can't be deleted because it's not a business related instance (it's more a security restriction).
     * @throws MetadataObjectNotFoundException If poolClass is not a valid subclass of GenericSoftwareAsset.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid.
     */
    public void deleteLicensePool(String poolId, String poolClass, String userName)
            throws ApplicationObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICSOFTWAREASSET, poolClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), poolClass, Constants.CLASS_GENERICSOFTWAREASSET));
            
        InventoryObjectPool pool = getLicensePool(poolId, poolClass);
        
        String[] licensePoolId = {poolId};
        aem.deletePools(licensePoolId);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT
                , String.format(ts.getTranslatedString("module.softman.actions.delete-pool.deleted-log")
                        , pool.getName(), pool.getId(), pool.getClassName()));
    }
    
    /**
     * Gets the license pool properties.
     * @param poolId The pool id.
     * @param poolClass The pool class.
     * @return The pool properties.
     * @throws ApplicationObjectNotFoundException If the pool can't be found.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid.
     * @throws MetadataObjectNotFoundException If poolClass is not a valid subclass of GenericSoftwareAsset.
     */
    public InventoryObjectPool getLicensePool(String poolId, String poolClass)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICSOFTWAREASSET, poolClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), poolClass, Constants.CLASS_GENERICSOFTWAREASSET));

        return bem.getPool(poolId);
    }
    
    /**
     * Retrieves the license pools list.
     * @return The available license pools.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid.
     */
    public List<InventoryObjectPool> getLicensePools() throws InvalidArgumentException {
        return bem.getRootPools(Constants.CLASS_GENERICSOFTWAREASSET, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, true);
    }
}