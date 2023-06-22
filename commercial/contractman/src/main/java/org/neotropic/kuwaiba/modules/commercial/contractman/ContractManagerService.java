/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.contractman;

import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ArraySizeMismatchException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The service that provides the actual functionality exposed by this module.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Service
public class ContractManagerService {
    /**
     * Reference to the translation service
     */
    @Autowired
    private TranslationService ts;
    /**
     * The MetadataEntityManager instance
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * The BusinessEntityManager instance
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * The ApplicationEntityManager instance
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Relationship contract to object 
     */
    public static String RELATIONSHIP_CONTRACTHAS = "contractHas";
    
    /**
     * Creates a contract pool.
     * @param poolName The pool name. 
     * @param poolDescription The pool description.
     * @param poolClass The pool class. What kind of objects can this pool contain? Must be subclass of GenericContract.
     * @param userName The user name of the session.
     * @return The id of the newly created contract pool.
     * @throws MetadataObjectNotFoundException If poolClass is not a valid subclass of GenericContract. 
     * @throws ApplicationObjectNotFoundException If the pool activity log can't be found.
     */
    public String createContractPool(String poolName, String poolDescription, String poolClass, String userName)
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, poolClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), poolClass, Constants.CLASS_GENERICCONTRACT));
         String poolId = aem.createRootPool(poolName, poolDescription, poolClass, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT
                , String.format(ts.getTranslatedString("module.contractman.actions.pool.new-pool-created-log")
                        , poolName, poolId, poolClass));
        
        return poolId;
    }
    
    /**
     * Gets the contract pools properties.
     * @param poolId The pool id.
     * @param poolClass The pool class.
     * @return The pool properties.
     * @throws ApplicationObjectNotFoundException If the pool can't be found.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid.
     * @throws MetadataObjectNotFoundException If poolClass is not a valid subclass of GenericContract.
     */
    public InventoryObjectPool getContractPool(String poolId, String poolClass)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, poolClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), poolClass, Constants.CLASS_GENERICCONTRACT));

        return bem.getPool(poolId);
    }
        
    /**
     * Updates the attributes of a contract pool.
     * @param poolId The id of the pool to be updated.
     * @param poolClass The pool class.
     * @param poolName The attribute value for pool name.
     * @param poolDescription The attribute value for pool description.
     * @param userName The user name of the session.
     * @throws ApplicationObjectNotFoundException If the pool can't be found.
     * @throws InvalidArgumentException If an unknown attribute name is provided.
     * @throws MetadataObjectNotFoundException If poolClass is not a valid subclass of GenericContract.
     */
    public void updateContractPool(String poolId, String poolClass, String poolName, String poolDescription, String userName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, poolClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), poolClass, Constants.CLASS_GENERICCONTRACT));
        
        aem.setPoolProperties(poolId, poolName, poolDescription);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT
                , String.format(ts.getTranslatedString("module.contractman.actions.pool.update-pool-updated-log"), poolId, poolClass));
    }
    
    /**
     * Deletes a contract pool.
     * @param poolId The pool id.
     * @param poolClass The pool class.
     * @param userName The user name of the session.
     * @throws ApplicationObjectNotFoundException If the pool can't be found.
     * @throws OperationNotPermittedException If any of the objects in the pool can't be deleted because it's not a business related instance (it's more a security restriction).
     * @throws MetadataObjectNotFoundException If poolClass is not a valid subclass of GenericContract.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid.
     */
    public void deleteContractPool(String poolId, String poolClass, String userName)
            throws ApplicationObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, poolClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), poolClass, Constants.CLASS_GENERICCONTRACT));
            
        InventoryObjectPool pool = getContractPool(poolId, poolClass);
        
        String[] contractPoolId = {poolId};
        aem.deletePools(contractPoolId);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT
                , String.format(ts.getTranslatedString("module.contractman.actions.pool.delete-pool-deleted-log")
                        , pool.getName(), pool.getId(), pool.getClassName()));
    }
    
    /**
     * Retrieves the contract pool list.
     * @return The available contract pools.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid.
     */
    public List<InventoryObjectPool> getContractPools() throws InvalidArgumentException {
        return bem.getRootPools(Constants.CLASS_GENERICCONTRACT, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, true);
    }
    
    /**
     * Creates a contract
     * @param poolId The contract pool id.
     * @param contractClass The contract class. Must be subclass of GenericContract.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @param userName The user name of the session.
     * @return The id of the newly created contract.
     * @throws MetadataObjectNotFoundException If the contract class can't be found.
     * @throws ApplicationObjectNotFoundException If the parent pool can't be found.
     * @throws InvalidArgumentException If any of the attributes or its type is invalid.
     * @throws BusinessObjectNotFoundException If the requested contract can't be found.
     */
    public String createContract(String poolId, String contractClass, HashMap<String, String> attributes, String userName) 
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, contractClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass"), contractClass, Constants.CLASS_GENERICCONTRACT));

        String contractId = bem.createPoolItem(poolId, contractClass, attributes, "");

        String contractName = getContract(contractClass, contractId).getName();
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.contractman.actions.contract.new-contract-created-log"), contractName, contractId, contractClass));

        return contractId;
    }
    
    /**
     * Get contract properties
     * @param contractClass The contract class. Must be subclass of GenericContract.
     * @param contractId The contract id.
     * @return The contract properties.
     * @throws MetadataObjectNotFoundException If the contract class can't be found.
     * @throws InvalidArgumentException If the contract id can't be found.
     * @throws BusinessObjectNotFoundException If the requested contract can't be found.
     */
    public BusinessObject getContract(String contractClass, String contractId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, contractClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), contractClass, Constants.CLASS_GENERICCONTRACT));

        BusinessObject contract = (BusinessObject) bem.getObject(contractClass, contractId);
        if (contract == null)
                throw new BusinessObjectNotFoundException(String.format(ts.getTranslatedString("module.contractman.contract-id-not-found"), contractId));
              
        return contract;
    }
    
    /**
     * Updates one or many contract attributes.
     * @param contractClass The contract class.
     * @param contractId The contract id.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If the requested contract can't be found.
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the contract is blocked.
     * @throws InvalidArgumentException If any of the initial attributes can't be mapped.
     * @throws MetadataObjectNotFoundException If the contract class can't be found.
     * @throws ApplicationObjectNotFoundException If the contract activity log can't be found.
     */
    public void updateContract(String contractClass, String contractId, HashMap<String,String> attributes, String userName) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException
            , OperationNotPermittedException, InvalidArgumentException, ApplicationObjectNotFoundException {     
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, contractClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), contractClass, Constants.CLASS_GENERICCONTRACT));

        BusinessObject contract = (BusinessObject) getContract(contractClass, contractId);
        if (contract == null)
            throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.contractman.contract-id-not-found"), contractId));
        
        bem.updateObject(contractClass, contractId, attributes);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.contractman.actions.contract.update-contract-updated-log")
                        , contract.getId(), contract.getClassName()));
    }
    
    /**
     * Deletes a contract and delete its association with the related inventory objects. These objects will remain untouched.
     * @param contractClass The contract class.
     * @param contractId The contract id.
     * @param userName The user name of the session.
     * @throws MetadataObjectNotFoundException If the contract class can't be found.
     * @throws InvalidArgumentException If the contract id can't be found.
     * @throws BusinessObjectNotFoundException If the requested contract can't be found.
     * @throws OperationNotPermittedException If the contract can't be deleted because there's some business rules that avoids it or it has incoming relationships.
     * @throws ApplicationObjectNotFoundException If the contract activity log can't be found.
     */
    public void deleteContract(String contractClass, String contractId, String userName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException,
             ApplicationObjectNotFoundException, OperationNotPermittedException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, contractClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), contractClass, Constants.CLASS_GENERICCONTRACT));

        BusinessObject contract = (BusinessObject) getContract(contractClass, contractId);
        if (contract == null)
            throw new BusinessObjectNotFoundException(String.format(ts.getTranslatedString("module.contractman.contract-id-not-found"), contractId));
        
        bem.deleteObject(contractClass, contractId, true);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.contractman.actions.contract.delete-contract-deleted-log")
                         , contract.getName(), contract.getId(), contract.getClassName()));
    }
    
    /**
     * Get all contracts, without filters.
     * @param page Page number of results to skip. Use -1 to retrieve all.
     * @param limit Max number of results per page. Use -1 to retrieve all.
     * @return The contracts list. 
     * @throws InvalidArgumentException If any contract node could not be mapped into a Java object.
     * @throws MetadataObjectNotFoundException If the provided class name doesn't exists.
     */
    public List<BusinessObjectLight> getAllContracts(long page, long limit)
            throws InvalidArgumentException, MetadataObjectNotFoundException {
        return bem.getObjectsOfClassLight(Constants.CLASS_GENERICCONTRACT, page, limit);
    }
    
    /** 
     * Gets the contracts inside a contract pool.
     * @param poolId The pool id.
     * @param limit The results limit per page. Use -1 to retrieve all.
     * @return The contracts list.
     * @throws ApplicationObjectNotFoundException If the pool can't be found.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid.
     */
    public List<BusinessObjectLight> getContractsInPool(String poolId, int limit)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        return bem.getPoolItems(poolId, limit);
    }
    
    /**
     * Relates a set of objects to a contract.
     * @param contractClass The contract class.
     * @param contractId The contract id.
     * @param objectClass The objects class names.
     * @param objectId The objects ids.
     * @param userName The user name of the session.
     * @throws InvalidArgumentException If the contract is not subclass of GenericContract.
     * @throws ArraySizeMismatchException If array sizes of objectClass and objectId are not the same.
     * @throws BusinessObjectNotFoundException If any of the objects can't be found.
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object).
     * @throws MetadataObjectNotFoundException If any of the classes provided can't be found.
     * @throws ApplicationObjectNotFoundException If the object activity log can't be found.
     */
    public void relateObjectsToContract(String contractClass, String contractId, String[] objectClass, String[] objectId, String userName)
            throws InvalidArgumentException, ArraySizeMismatchException, BusinessObjectNotFoundException
            , OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, contractClass))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass"), contractClass, Constants.CLASS_GENERICCONTRACT));
        
        if (objectClass.length != objectId.length)
            throw new ArraySizeMismatchException(ts.getTranslatedString("module.general.messages.error-array-lengths"));
        
        boolean allEquipmentANetworkElement = true;
        
        for (int i = 0; i < objectId.length; i++) {
            if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, objectClass[i]))
                allEquipmentANetworkElement = false;
            else 
                bem.createSpecialRelationship(objectClass[i], objectId[i], contractClass, contractId, RELATIONSHIP_CONTRACTHAS, true);
        } 

        String contractName = bem.getObjectLight(contractClass, contractId).getName();
        aem.createObjectActivityLogEntry(userName, contractClass, contractId,
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, RELATIONSHIP_CONTRACTHAS, "", "",
                String.format(ts.getTranslatedString("module.contractman.actions.relate-objects-to-contract.relationship-log")
                        , contractName, contractId, contractClass));
        
        if (!allEquipmentANetworkElement)
            throw new InvalidArgumentException("module.general.messages.error-non-inventory-elements");
    }
    
    /**
     * Relates an object to a contract.
     * @param contractClass The contract class.
     * @param contractId The contract id.
     * @param objectClass The object class.
     * @param objectId The object id.
     * @param userName The user name of the session.
     * @throws MetadataObjectNotFoundException If the object class provided can't be found.
     * @throws InvalidArgumentException If the contract is not subclass of GenericContract.
     * @throws BusinessObjectNotFoundException If any of the objects can't be found.
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object).
     * @throws ApplicationObjectNotFoundException If the object activity log can't be found.
     */
    public void relateObjectToContract(String contractClass, String contractId, String objectClass, String objectId, String userName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException
            , OperationNotPermittedException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, contractClass))
             throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass"), contractClass, Constants.CLASS_GENERICCONTRACT));
        
        bem.createSpecialRelationship(contractClass, contractId, objectClass, objectId, RELATIONSHIP_CONTRACTHAS, true);
        
        aem.createObjectActivityLogEntry(userName, objectClass, objectId, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME, "",
                RELATIONSHIP_CONTRACTHAS, String.format(ts.getTranslatedString("module.contractman.actions.relate-object-to-contract.relationship-log"),
                        objectId, objectClass, contractId, contractClass));
    }
    
    /**
     * Releases an object from contract.
     * @param objectClass The object class.
     * @param objectId The object id.
     * @param contractClass The contract class.
     * @param contractId The contract id.
     * @param userName The user name of the session.
     * @throws MetadataObjectNotFoundException If the object class provided can't be found.
     * @throws InvalidArgumentException If the contract is not subclass of GenericContract.
     * @throws BusinessObjectNotFoundException If any of the objects can't be found.
     * @throws ApplicationObjectNotFoundException 
     */
    public void releaseObjectFromContract(String objectClass, String objectId, String contractClass, String contractId, String userName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, contractClass))
             throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass"), contractClass, Constants.CLASS_GENERICCONTRACT));
        
        bem.releaseSpecialRelationship(objectClass, objectId, contractId, RELATIONSHIP_CONTRACTHAS);
        
        aem.createObjectActivityLogEntry(userName, objectClass, objectId, ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME, RELATIONSHIP_CONTRACTHAS,
                "", String.format(ts.getTranslatedString("module.contractman.actions.release-object-from-contract.relationship-log"), objectId, objectClass, contractId, contractClass));
    }
    
    /**
     * Gets the resources related to a contract.
     * @param contractClass The contract class.
     * @param contractId The contract id.
     * @return The contract resources list.
     * @throws InvalidArgumentException If the contract is not subclass of GenericContract.
     * @throws BusinessObjectNotFoundException If the contract can't be found.
     * @throws MetadataObjectNotFoundException If the contract class provided can't be found.
     */
    public List<BusinessObjectLight> getContractResources(String contractClass, String contractId)
            throws InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, contractClass))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , contractClass, Constants.CLASS_GENERICCONTRACT));
        
        return bem.getSpecialAttribute(contractClass, contractId, RELATIONSHIP_CONTRACTHAS);
    }
    
    /**
     * Creates a copy of a contract.
     * @param poolId The pool id.
     * @param contractClass The contract class.
     * @param contractId The contract id.
     * @param userName The user name of the session.
     * @return The newly created contract id.
     * @throws ApplicationObjectNotFoundException If the pool node can not be found.
     * @throws InvalidArgumentException If the contract can not be copy to the selected pool.
     * @throws BusinessObjectNotFoundException If the contract can not be found.
     * @throws MetadataObjectNotFoundException If the contract class name can no be found.
     */
    public String copyContractToPool(String poolId, String contractClass, String contractId, String userName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, contractClass))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICCONTRACT, contractClass));
        String contractOid = bem.copyPoolItem(poolId, contractClass, contractId, true);
                
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.contractman.actions.copy-contract-to-pool.copied-log"), contractOid, poolId));
        
        return contractOid;
    }
    
    /**
     * Moves a contract from a pool to another pool.
     * @param poolId The pool id.
     * @param contractClass The contract class.
     * @param contractId The contract id.
     * @param userName The user name of the session.
     * @throws MetadataObjectNotFoundException If the contract class name can no be found.
     * @throws InvalidArgumentException If the contract can not be move to the selected pool.
     * @throws ApplicationObjectNotFoundException If the pool node can not be found.
     * @throws BusinessObjectNotFoundException If the contract can not be found.
     */
    public void moveContractToPool(String poolId, String contractClass, String contractId, String userName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, BusinessObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, contractClass))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICCONTRACT, contractClass));
        
        bem.movePoolItem(poolId, contractClass, contractId);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT
                , String.format(ts.getTranslatedString("module.contractman.actions.move-contract-to-pool.moved-log"), contractId, poolId));
    }
}