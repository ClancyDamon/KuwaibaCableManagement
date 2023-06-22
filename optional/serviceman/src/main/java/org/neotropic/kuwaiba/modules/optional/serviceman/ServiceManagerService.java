/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.modules.optional.serviceman;

import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
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
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to manage services.
 * @author Mauricio Ruiz Beltran {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Service
public class ServiceManagerService {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Relationship Name
     */
    private static final String RELATIONSHIP_NAME = "uses";

    /**
     * Creates a customer pool.
     * @param poolName The pool name.
     * @param poolDescription The pool description.
     * @param userName The user name of the session.
     * @return The id newly created customer pool.
     * @throws MetadataObjectNotFoundException If instancesOfClass is not a valid subclass of InventoryObject
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found
     * @throws BusinessObjectNotFoundException
     */
    public String createCustomerPool(String poolName, String poolDescription, String userName) 
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, BusinessObjectNotFoundException {
        String poolId = aem.createRootPool(poolName, poolDescription, Constants.CLASS_GENERICCUSTOMER, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format(ts.getTranslatedString("module.serviceman.actions.new-customer-pool.ui.customer-pool-created-log"), poolName));
        
        return poolId;
    }
    
    /**
     * Retrieves the list of customer pools.
     * @return A set of customer pools.
     * @throws InvalidArgumentException If a customer pool does not have the uuid.
     */
    public List<InventoryObjectPool> getCustomerPools() throws InvalidArgumentException {
        return bem.getRootPools(Constants.CLASS_GENERICCUSTOMER, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
    }
    
    /**
     * Gets a customer pool
     * @param poolId The pool id.
     * @param poolClass The pool class.
     * @return The pool as a Pool object
     * @throws ApplicationObjectNotFoundException If the pool could not be found
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid
     */
    public InventoryObjectPool getCustomerPool(String poolId, String poolClass)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        if (!poolClass.equals(Constants.CLASS_GENERICCUSTOMER))
            throw new ApplicationObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.customer-pool.class-error"), poolClass));
        
        return bem.getPool(poolId);  
    }
    
    /**
     * Updates a customer pool.
     * @param poolId The pool id.
     * @param poolClass The pool class.
     * @param poolName The pool name.
     * @param poolDescription The pool description.
     * @param userName The user name of the session.
     * @throws ApplicationObjectNotFoundException If any of the pools to be updated couldn't be found.
     */
    public void updateCustomerPool(String poolId, String poolClass, String poolName, String poolDescription, String userName)
            throws ApplicationObjectNotFoundException {
        if (!poolClass.equals(Constants.CLASS_GENERICCUSTOMER))
            throw new ApplicationObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.customer-pool.class-error"), poolClass));
        
        ChangeDescriptor changeDescriptor = aem.setPoolProperties(poolId, poolName, poolDescription);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, changeDescriptor);
    }
    
    /**
     * Deletes a customer pool.
     * @param poolId The pool id.
     * @param poolClass The pool class.
     * @param userName The user name of the session.
     * @throws ApplicationObjectNotFoundException If any of the pools to be deleted couldn't be found.
     * @throws OperationNotPermittedException  If any of the objects in the pool can not be deleted because
     * it's not a business related instance (it's more a security restriction).
     */
    public void deleteCustomerPool(String poolId, String poolClass, String userName)
            throws ApplicationObjectNotFoundException, OperationNotPermittedException {
        if (!poolClass.equals(Constants.CLASS_GENERICCUSTOMER))
             throw new ApplicationObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.customer-pool.class-error"), poolClass));
            
        String[] customerPoolId = {poolId};
        aem.deletePools(customerPoolId);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT,
                String.format(ts.getTranslatedString("module.serviceman.actions.delete-customer-pool.ui.customer-pool-deleted-log"), poolId));
    }
    
    /**
     * Creates a customer.
     * @param poolId Parent pool id.
     * @param customerClass This customer is going to be instance of
     * @param attributes The list of attributes to be set initially. The values are serialized objects.
     * @param userName The user name of the session.
     * @return The id newly created customer.
     * @throws ApplicationObjectNotFoundException If the parent pool can't be found.
     * @throws InvalidArgumentException If any of the attributes or its type is invalid.
     * @throws MetadataObjectNotFoundException If the class name could not be found.
     */
    public String createCustomer(String poolId, String customerClass, HashMap<String, String> attributes, String userName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCUSTOMER, customerClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.customer.class-error"), customerClass));

        String customerId = bem.createPoolItem(poolId, customerClass, attributes, null);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT,
                String.format(ts.getTranslatedString("module.serviceman.actions.new-customer.ui.customer-created-log"), customerId));

        return customerId;
    }
    
    /**
     * Gets all the customer.
     * @param filters map of filters key: attribute name, value: attribute value
     * @param page page or number of elements to skip
     * @param limit max count of child per page
     * @return The customers list.
     * @throws InvalidArgumentException If the class name is null
     * @throws MetadataObjectNotFoundException If the provided class name doesn't exists
     */
    public List<BusinessObjectLight> getAllCustomers(HashMap <String, String> filters, long page, long limit)
            throws InvalidArgumentException, MetadataObjectNotFoundException  {
        return bem.getObjectsOfClassLight(Constants.CLASS_GENERICCUSTOMER, filters, page, limit);    
    }
    
    /**
     * Retrieves the list of customers from a pool
     * @param poolId Parent pool id
     * @param className  a given className to retrieve a set of objects of that className form the pool
     * used when the pool is a Generic class and could have objects of different class
     * @param page the number of values of the result to skip or the page 0 to avoid
     * @param limit the results limit. per page 0 to avoid the limit
     * @return The list of customers inside the pool
     * @throws ApplicationObjectNotFoundException If the pool id provided is not valid
     * @throws InvalidArgumentException If the pool item is an inventory object and it does not have the uuid
     */
    public List<BusinessObjectLight> getCustomersInPool(String poolId, String className, int page, int limit)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        return bem.getPoolItemsByClassName(poolId, className, page, limit);
    }
    
    /**
     * Gets the detailed information about a customer.
     * @param customerClass Customer class name.
     * @param customerId Customer id.
     * @return A detailed representation of the requested customer.
     * @throws MetadataObjectNotFoundException If the className class can't be found.
     * @throws BusinessObjectNotFoundException If the requested object can't be found.
     * @throws InvalidArgumentException If the object id can not be found.
     */
    public BusinessObject getCustomer(String customerClass, String customerId) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCUSTOMER, customerClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.customer.class-error"), customerClass));
        
        return bem.getObject(customerClass, customerId);
    }
    
    /**
     * Updates a customer.
     * @param customerClass Customer class name.
     * @param customerId Customer id.
     * @param attributes The attributes to be updated (the key is the attribute name, 
     * the value is and array with the value -or values in case of MANY TO MANY list type attributes-).
     * @param userName The user name of the session.
     * @throws MetadataObjectNotFoundException If the object class can't be found.
     * @throws BusinessObjectNotFoundException If the object can't be found.
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked.
     * @throws InvalidArgumentException If any of the names provided does not exist or can't be set using this method or of the value.
     * @throws ApplicationObjectNotFoundException If the log root node could not be found.
     */
    public void updateCustomer(String customerClass, String customerId, HashMap<String, String> attributes, String userName) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException,
            InvalidArgumentException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCUSTOMER, customerClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.customer.class-error"), customerClass));
        
        ChangeDescriptor changeDescriptor = bem.updateObject(customerClass, customerId, attributes);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, changeDescriptor);
    }
    
    /**
     * Deletes a customer
     * @param customerClass Customer class name.
     * @param customerId Customer id.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If the object couldn't be found.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     * @throws OperationNotPermittedException If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     * @throws InvalidArgumentException If the id is null.
     * @throws ApplicationObjectNotFoundException If the log root node could not be found.
     */
    public void deleteCostumer(String customerClass, String customerId, String userName)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException,
            OperationNotPermittedException, InvalidArgumentException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCUSTOMER, customerClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.customer.class-error"), customerClass));
        
        bem.deleteObject(customerClass, customerId, false);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                    String.format(ts.getTranslatedString("module.serviceman.actions.delete-customer.ui.customer-deleted-log"),
                            customerId, customerClass));
    }
    
    /**
     * Creates a service pool.
     * @param customerClass Customer class name.
     * @param customerId Customer id.
     * @param poolName The service pool name.
     * @param poolDescription The service pool description.
     * @param userName The user name of the session.
     * @return The id newly created service pool.
     * @throws MetadataObjectNotFoundException If instancesOfClass is not a valid subclass of InventoryObject.
     * @throws BusinessObjectNotFoundException If the parent object can not be found.
     * @throws ApplicationObjectNotFoundException If the log root node could not be found.
     */
    public String createServicePool(String customerClass, String customerId, String poolName, String poolDescription, String userName)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException {
        String servicePoolId = aem.createPoolInObject(customerClass, customerId, poolName, poolDescription, 
                        Constants.CLASS_GENERICSERVICE, ApplicationEntityManager.POOL_TYPE_MODULE_COMPONENT);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format(ts.getTranslatedString("module.serviceman.actions.new-service-pool.ui.pool-created-log"), 
                        poolName, customerClass, customerId));  
        
        return servicePoolId;
    }
        
    /**
     * Retrieves the pools associated to a particular customer
     * @param customerClass The parent customer class name
     * @param customerId The parent customer id
     * @param servicePoolClass The class name used to filter the results. 
     * @return A set of service pools
     * @throws BusinessObjectNotFoundException If the parent customer can not be found
     * @throws InvalidArgumentException If a pool does not have uuid
     * @throws MetadataObjectNotFoundException If the argument servicePoolClass is not a valid class.
     * @throws ApplicationObjectNotFoundException  If the pool could not be found.
     */
    public List<InventoryObjectPool> getServicePoolsInCostumer(String customerClass, String customerId, String servicePoolClass)
            throws BusinessObjectNotFoundException, InvalidArgumentException,
            MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        if (!servicePoolClass.equals(Constants.CLASS_GENERICSERVICE))
            throw new ApplicationObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.service-pool.class-error"), servicePoolClass));
        
        return bem.getPoolsInObject(customerClass, customerId, servicePoolClass);
    }
    
    /**
     * Gets a service pool.
     * @param poolId The pool id.
     * @param poolClass The pool class.
     * @return The pool as a Pool object.
     * @throws ApplicationObjectNotFoundException If the pool could not be found.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid.
     */
    public InventoryObjectPool getServicePool(String poolId, String poolClass)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        if (!poolClass.equals(Constants.CLASS_GENERICSERVICE))
            throw new ApplicationObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.service-pool.class-error"), poolClass));
        
        return bem.getPool(poolId);  
    }
    
    /**
     * Updates a service pool.
     * @param poolId The pool id.
     * @param poolClass The pool class.
     * @param poolName The pool name.
     * @param poolDescription The pool description.
     * @param userName The user name of the session.
     * @throws ApplicationObjectNotFoundException If any of the pools to be updated couldn't be found.
     */
    public void updateServicePool(String poolId, String poolClass, String poolName, String poolDescription, String userName)
            throws ApplicationObjectNotFoundException {
        if (!poolClass.equals(Constants.CLASS_GENERICSERVICE))
            throw new ApplicationObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.service-pool.class-error"), poolClass));
        
        ChangeDescriptor changeDescriptor = aem.setPoolProperties(poolId, poolName, poolDescription);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, changeDescriptor);
    }
    
    /**
     * Deletes a service pool.
     * @param poolId The pool id.
     * @param poolClass The pool class.
     * @param userName The user name of the session.
     * @throws ApplicationObjectNotFoundException If any of the pools to be deleted couldn't be found.
     * @throws OperationNotPermittedException  If any of the objects in the pool can not be deleted because
     * it's not a business related instance (it's more a security restriction).
     */
    public void deleteServicePool(String poolId, String poolClass, String userName)
            throws ApplicationObjectNotFoundException, OperationNotPermittedException {
        if (!poolClass.equals(Constants.CLASS_GENERICSERVICE))
             throw new ApplicationObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.service-pool.class-error"), poolClass));
            
        String[] customerPoolId = {poolId};
        aem.deletePools(customerPoolId);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT,
                String.format(ts.getTranslatedString("module.serviceman.actions.delete-service-pool.ui.service-pool-deleted-log"), poolId));
    }
    
    /**
     * Creates a service.
     * @param poolId Parent pool id.
     * @param serviceClass This service is going to be instance of
     * @param attributes The list of attributes to be set initially. The values are serialized objects.
     * @param userName The user name of the session.
     * @return The id newly created service.
     * @throws ApplicationObjectNotFoundException If the parent pool can't be found.
     * @throws InvalidArgumentException If any of the attributes or its type is invalid.
     * @throws MetadataObjectNotFoundException If the class name could not be found.
     */
    public String createService(String poolId, String serviceClass, HashMap<String, String> attributes, String userName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICSERVICE, serviceClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.service.class-error"), serviceClass));

        String serviceId = bem.createPoolItem(poolId, serviceClass, attributes, null);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT,
                String.format(ts.getTranslatedString("module.serviceman.actions.new-service.ui.service-created-log"), serviceId, poolId));

        return serviceId;
    }
    
    /**
     * Gets all the services.
     * @param filters map of filters key: attribute name, value: attribute value
     * @param page page or number of elements to skip
     * @param limit max count of child per page
     * @return The services list.
     * @throws InvalidArgumentException If the class name is null
     * @throws MetadataObjectNotFoundException If the provided class name doesn't exists
     */
    public List<BusinessObjectLight> getAllServices(HashMap <String, String> filters, long page, long limit)
            throws InvalidArgumentException, MetadataObjectNotFoundException  {
        return bem.getObjectsOfClassLight(Constants.CLASS_GENERICSERVICE, filters, page, limit);    
    }
    
    /**
     * Retrieves the list of services from a pool
     * @param poolId Parent pool id
     * @param className  a given className to retrieve a set of objects of that className form the pool
     * used when the pool is a Generic class and could have objects of different class
     * @param page the number of values of the result to skip or the page 0 to avoid
     * @param limit the results limit. per page 0 to avoid the limit
     * @return The list of services inside the pool
     * @throws ApplicationObjectNotFoundException If the pool id provided is not valid
     * @throws InvalidArgumentException If the pool item is an inventory object and it does not have the uuid
     */
    public List<BusinessObjectLight> getServicesInPool(String poolId, String className, int page, int limit)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        return bem.getPoolItemsByClassName(poolId, className, page, limit);
    }
    
    /**
     * Gets the detailed information about a service.
     * @param serviceClass Service class name.
     * @param serviceId Service id.
     * @return A detailed representation of the requested customer.
     * @throws MetadataObjectNotFoundException If the className class can't be found.
     * @throws BusinessObjectNotFoundException If the requested object can't be found.
     * @throws InvalidArgumentException If the object id can not be found.
     */
    public BusinessObject getService(String serviceClass, String serviceId) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICSERVICE, serviceClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.service.class-error"), serviceClass));
        
        return bem.getObject(serviceClass, serviceId);
    }
    
    /**
     * Updates a service.
     * @param serviceClass Service class name.
     * @param serviceId Service id.
     * @param attributes The attributes to be updated (the key is the attribute name, 
     * the value is and array with the value -or values in case of MANY TO MANY list type attributes-).
     * @param userName The user name of the session.
     * @throws MetadataObjectNotFoundException If the object class can't be found.
     * @throws BusinessObjectNotFoundException If the object can't be found.
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked.
     * @throws InvalidArgumentException If any of the names provided does not exist or can't be set using this method or of the value.
     * @throws ApplicationObjectNotFoundException If the log root node could not be found.
     */
    public void updateService(String serviceClass, String serviceId, HashMap<String, String> attributes, String userName) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException,
            InvalidArgumentException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICSERVICE, serviceClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.service.class-error"), serviceClass));
        
        ChangeDescriptor changeDescriptor = bem.updateObject(serviceClass, serviceId, attributes);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, changeDescriptor);
    }
    
    /**
     * Deletes a service.
     * @param serviceClass Service class name.
     * @param serviceId Service id.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If the object couldn't be found.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     * @throws OperationNotPermittedException If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     * @throws InvalidArgumentException If the id is null.
     * @throws ApplicationObjectNotFoundException If the log root node could not be found.
     */
    public void deleteService(String serviceClass, String serviceId, String userName)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException,
            OperationNotPermittedException, InvalidArgumentException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICSERVICE, serviceClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.customer.class-error"), serviceClass));
        
        bem.deleteObject(serviceClass, serviceId, true);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                    String.format(ts.getTranslatedString("module.serviceman.actions.delete-service.ui.service-deleted-log"),
                            serviceId, serviceClass));
    }
    
    /**
     * Relates an inventory object (resource) to a service.
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param serviceClass The class of the service.
     * @param serviceId The id of the service.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException if any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException if any of the classes provided can not be found
     * @throws InvalidArgumentException If the a/b Object Id are null
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found.
     */
    public void relateObjectToService(String objectClass, String objectId, String serviceClass, String serviceId, String userName)
            throws BusinessObjectNotFoundException, OperationNotPermittedException,
            MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        if(!mem.isSubclassOf(Constants.CLASS_GENERICSERVICE, serviceClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.service.class-error"), serviceClass));
        
        bem.createSpecialRelationship(serviceClass, serviceId, objectClass, objectId, RELATIONSHIP_NAME, true);

        aem.createObjectActivityLogEntry(userName, objectClass, objectId, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME, "",
                RELATIONSHIP_NAME, String.format(ts.getTranslatedString("module.serviceman.actions.relate-object-to-service.ui.relationship-log"), objectId, serviceId));
    }
    
    /**
     * Relates inventory objects (resources) to a service.
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param serviceClass The class of the service.
     * @param serviceId The id of the service.
     * @param userName The user name of the session.
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws InvalidArgumentException If the a/b Object Id are null
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found.
     */
    public void relateObjectsToService(String[] objectClass, String[] objectId, String serviceClass, String serviceId, String userName)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException,
            OperationNotPermittedException, InvalidArgumentException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICSERVICE, serviceClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.service.class-error"), serviceClass));

        for (int i = 0; i < objectId.length; i++) {
            bem.createSpecialRelationship(serviceClass, serviceId, objectClass[i], objectId[i], RELATIONSHIP_NAME, true);
            aem.createObjectActivityLogEntry(userName, objectClass[i], objectId[i], ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME, "",
                    RELATIONSHIP_NAME, String.format(ts.getTranslatedString("module.serviceman.actions.relate-object-to-service.ui.relationship-log"), objectId[i], serviceId));
        }
    }
    
    /**
     * Releases an inventory object from a service.
     * @param serviceClass The class of the service.
     * @param serviceId The id of the service.
     * @param objectId The id of the object.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If the object can not be found.
     * @throws MetadataObjectNotFoundException If the class can not be found.
     * @throws InvalidArgumentException If serviceId or objectId are null.
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found.
     */
     public void releaseObjectFromService(String serviceClass, String serviceId, String objectId, String userName)
             throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
         if(!mem.isSubclassOf(Constants.CLASS_GENERICSERVICE, serviceClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.service.class-error"), serviceClass));
            
         bem.releaseSpecialRelationship(serviceClass, serviceId, objectId, RELATIONSHIP_NAME);

         aem.createObjectActivityLogEntry(userName, serviceClass, serviceId, ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME, RELATIONSHIP_NAME,
                 "", String.format(ts.getTranslatedString("module.serviceman.actions.release-object-from-service.ui.relationship-log"), objectId, serviceId));
    }
    
    /**
     * Retrieves the objects related to service
     * @param serviceClass The service class name
     * @param serviceId The service id
     * @return A list of objects related to service
     * @throws BusinessObjectNotFoundException If the service can not be found
     * @throws MetadataObjectNotFoundException If either the service class or the attribute can not be found
     * @throws InvalidArgumentException If the service id is null
     */ 
    public List<BusinessObjectLight> getObjectsRelatedToService(String serviceClass, String serviceId)
             throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICSERVICE, serviceClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.serviceman.actions.service.class-error"), serviceClass));
        
        return bem.getSpecialAttribute(serviceClass, serviceId, RELATIONSHIP_NAME);
    }
}