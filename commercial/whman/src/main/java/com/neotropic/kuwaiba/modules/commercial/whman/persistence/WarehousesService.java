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
package com.neotropic.kuwaiba.modules.commercial.whman.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.ObjectGraphMappingService;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to manage warehouses
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @author Mauricio Ruiz Beltran {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Service
public class WarehousesService {
    /**
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the service that maps Java objects/attributes into graph nodes/properties.
     */
    @Autowired
    private ObjectGraphMappingService ogmService;
    /**
     * Database connection manager instance.
     */
    @Autowired
    private ConnectionManager<GraphDatabaseService> connectionManager;
    /**
     * Pool label
     */
    private final Label poolLabel;
    /**
     * Inventory Object label
     */
    private final Label inventoryObjectLabel;
    /**
     * Relationship used to assign a Warehouse or VirtualWarehouse to a GenericLocation
     */
    public static final String RELATIONSHIP_WAREHOUSE_HAS = "warehouseHas"; //NOI18N
    
    @PostConstruct
    public void init() {
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_WAREHOUSE_HAS, 
            ts.getTranslatedString("module.whman.special-relationship.warehouse-has.display-name"));
    }
    
    public WarehousesService() {
        // Initilize labels
        this.poolLabel = Label.label(Constants.LABEL_POOLS);
        this.inventoryObjectLabel = Label.label(Constants.LABEL_INVENTORY_OBJECTS);
    }
        
    /**
     * Gets the Warehouse Module Root Pools
     * @return A list of root pools
     * @throws MetadataObjectNotFoundException If the classes Warehouse or VirtualWarehouse could not be found.
     * @throws InvalidArgumentException If any pool does not have uuid
     */
    public List<InventoryObjectPool> getWarehouseRootPools() 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        List<InventoryObjectPool> warehousePools = bem.getRootPools(Constants.CLASS_WAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        List<InventoryObjectPool> virtualWarehousePools = bem.getRootPools(Constants.CLASS_VIRTUALWAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        // If the Warehouse root pool does not exist then it is created
        if (warehousePools.isEmpty()) {
            aem.createRootPool(Constants.NODE_WAREHOUSE, Constants.NODE_WAREHOUSE, Constants.CLASS_WAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
            warehousePools = bem.getRootPools(Constants.CLASS_WAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        }
        // If the VirtualWarehouse root pool does not exist then it is created
        if (virtualWarehousePools.isEmpty()) {
            aem.createRootPool(Constants.NODE_VIRTUALWAREHOUSE, Constants.NODE_VIRTUALWAREHOUSE, Constants.CLASS_VIRTUALWAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
            virtualWarehousePools = bem.getRootPools(Constants.CLASS_VIRTUALWAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        }
        List<InventoryObjectPool> warehouseRootPools = new ArrayList();
        warehouseRootPools.addAll(warehousePools);
        warehouseRootPools.addAll(virtualWarehousePools);
        
        return warehouseRootPools;
    }
          
    /**
     * Get the warehouses of a pool
     * @param poolId Root pool id
     * @param limit Result limit. -1 To return all
     * @return List of warehouses
     * @throws ApplicationObjectNotFoundException If the pool id provided is not valid
     * @throws InvalidArgumentException If any warehouse does not have uuid
     */
    public List<BusinessObjectLight> getWarehousesInPool(String poolId, int limit)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node poolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, poolId);

            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.pool-id-not-found"), poolId));

            List<BusinessObjectLight> warehouses  = new ArrayList<>();

            int i = 0;
            for (Relationship rel : poolNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                if (limit != -1) {
                    if (i >= limit)
                         break;
                    i++;
                }
                if (rel.hasProperty(Constants.PROPERTY_NAME)) {
                    if (rel.getProperty(Constants.PROPERTY_NAME).equals(Constants.REL_PROPERTY_POOL)) {
                        Node item = rel.getStartNode();
                        
                        String itemUuid = item.hasProperty(Constants.PROPERTY_UUID) ? (String) item.getProperty(Constants.PROPERTY_UUID) : null;
                        if (itemUuid == null)
                            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.pool-not-have-uuid"), item.getId()));
                        
                        Node temp = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, itemUuid);
                        if (temp == null)  //If it's not a pool, but a normal business object
                            warehouses.add(ogmService.createObjectLightFromNode(item));
                    }
                }
            }
            tx.success();
            return warehouses;
        }
    }
    
    /**
     * Get the pools of a warehouses
     * @param objectClassName Warehouse class name
     * @param objectId Warehouse id
     * @return List of spare pools
     * @throws BusinessObjectNotFoundException If the parent object can not be found
     * @throws InvalidArgumentException If pool warehouse does not have uui
     * @throws MetadataObjectNotFoundException If the classes could not be found.
     */
    public List<InventoryObjectPool> getPoolsInWarehouse(String objectClassName, String objectId) throws 
            BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<InventoryObjectPool> pools  = new ArrayList<>();
            Node objectNode = connectionManager.getConnectionHandler().findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, objectId);
            
            if (objectNode == null)
                throw new BusinessObjectNotFoundException(objectClassName, objectId);
            
            for (Relationship containmentRelationship : objectNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                if (containmentRelationship.hasProperty(Constants.PROPERTY_NAME) && 
                        Constants.REL_PROPERTY_POOL.equals(containmentRelationship.getProperty(Constants.PROPERTY_NAME))) {
                    Node poolNode = containmentRelationship.getStartNode();
                    
                    if (mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, (String)poolNode.getProperty(Constants.PROPERTY_CLASSNAME)))
                            pools.add(Util.createPoolFromNode(poolNode, ts));
                }
            }
            tx.success();
            return pools;
        }
    }
        
    /**
     * Get the objects of a pool
     * @param poolId Root pool id
     * @param limit Result limit. -1 To return all
     * @return List of objects
     * @throws ApplicationObjectNotFoundException If the pool id provided is not valid
     * @throws InvalidArgumentException If any object does not have uuid
     */
    public List<BusinessObjectLight> getObjectsInSparePool(String poolId, int limit)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node poolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, poolId);

            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.pool-id-not-found"), poolId));

            List<BusinessObjectLight> objects = new ArrayList<>();

            int i = 0;
            for (Relationship rel : poolNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                if (limit != -1) {
                    if (i >= limit)
                         break;
                    i++;
                }
                if (rel.hasProperty(Constants.PROPERTY_NAME)) {
                    if (rel.getProperty(Constants.PROPERTY_NAME).equals(Constants.REL_PROPERTY_POOL)) {
                        Node item = rel.getStartNode();
                        
                        String itemUuid = item.hasProperty(Constants.PROPERTY_UUID) ? (String) item.getProperty(Constants.PROPERTY_UUID) : null;
                        if (itemUuid == null)
                            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.object-not-have-uuid"), item.getId()));
                        
                        Node temp = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, itemUuid);
                        if (temp == null)  //If it's not a pool, but a normal business object
                            objects.add(ogmService.createObjectLightFromNode(item));
                    }
                }
            }
            tx.success();
            return objects;
        }
    }
    
    /**
     * Creates a warehouse inside a pool
     * @param poolId Parent pool id
     * @param poolClass Class this warehouse is going to be instance of
     * @param attributes The list of attributes to be set initially. The values are serialized objects.
     * @param templateId  The id of the template to be used to create this object. This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before. Use a null or empty string to not use a template.
     * @param userName The user name of the session.
     * @return the id of the newly created warehouse
     * @throws ApplicationObjectNotFoundException If the parent pool can't be found
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @throws MetadataObjectNotFoundException If the class name could not be found 
     */
    public String createWarehouse(String poolId, String poolClass, HashMap<String, String> attributes, String templateId, String userName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERIC_WAREHOUSE, poolClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.whman.class-warehouse.error"), poolClass));

        String warehouseId = bem.createPoolItem(poolId, poolClass, attributes, templateId);

        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT,
                String.format(ts.getTranslatedString("module.whman.actions.warehouses.new-warehouse.created-log"), warehouseId));

        return warehouseId;
    }
    
    /**
     * Deletes a warehouse and delete its association with the related inventory objects. These objects will remain untouched
     * @param warehouseClass The class of the warehouse
     * @param warehouseId The id of the warehouse
     * @param userName The user name of the session
     * @throws ApplicationObjectNotFoundException If the log root node could not be found
     * @throws MetadataObjectNotFoundException If the warehouse class could not be found
     * @throws BusinessObjectNotFoundException If the warehouse could not be found
     * @throws OperationNotPermittedException If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships
     * @throws InvalidArgumentException If the id is null
     */
    public void deleteWarehouse(String warehouseClass, String warehouseId, String userName)
            throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException
            , BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERIC_WAREHOUSE, warehouseClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.whman.class-warehouse.error"), warehouseClass));

        bem.deleteObject(warehouseClass, warehouseId, true);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT,
                String.format(ts.getTranslatedString("module.whman.actions.warehouses.delete-warehouse.deleted-log"),
                        warehouseId, warehouseClass));
    }
   
    /**
     * Creates a pool inside a warehouse
     * @param warehouseClass Class name of the parent warehouse
     * @param warehouseId Id of the parent object
     * @param poolName Pool name
     * @param poolDescription Pool description
     * @param instancesOfClass What kind of objects can this pool contain?
     * @param userName The user name of the session
     * @return The id of the new pool
     * @throws MetadataObjectNotFoundException If instancesOfClass is not a valid subclass of InventoryObject
     * @throws BusinessObjectNotFoundException If the parent object can not be found
     * @throws ApplicationObjectNotFoundException If the log root node could not be found
     */
    public String createPoolInWarehouse(String warehouseClass, String warehouseId
            , String poolName, String poolDescription, String instancesOfClass, String userName)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERIC_WAREHOUSE, warehouseClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.whman.class-warehouse.error"), warehouseClass));
        
        String sparePoolId = aem.createPoolInObject(warehouseClass, warehouseId, poolName, poolDescription
                , instancesOfClass, ApplicationEntityManager.POOL_TYPE_MODULE_COMPONENT);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format(ts.getTranslatedString("module.whman.actions.spare.new-spare.created-log"), 
                        poolName, warehouseClass, warehouseId));  
        
        return sparePoolId;
    }
    
    /**
     * Deletes a spare pool.
     * @param poolId The spare pool id.
     * @param poolClass The spare pool class.
     * @param userName The user name of the session.
     * @throws ApplicationObjectNotFoundException If the pools to be deleted couldn't be found.
     * @throws OperationNotPermittedException  If any of the objects in the pool can not be deleted because
     * it's not a business related instance (it's more a security restriction).
     * @throws MetadataObjectNotFoundException If spare pool class name is null
     */
    public void deleteSparePool(String poolId, String poolClass, String userName)
            throws ApplicationObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
            
        if (poolClass == null)
            throw new MetadataObjectNotFoundException(ts.getTranslatedString("module.general.messages.class-name-not-null"));
        
        String[] sparePoolId = {poolId};
        aem.deletePools(sparePoolId);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT,
                String.format(ts.getTranslatedString("module.whman.actions.spare.delete-spare.deleted-log"), poolId));
    }
    
    /**
     * Creates a spare part inside a pool
     * @param poolId Spare pool id
     * @param className Class this spare part is going to be instance of
     * @param attributes The list of attributes to be set initially. The values are serialized objects.
     * @param templateId  The id of the template to be used to create this object.
     * This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before. 
     * Use a null or empty string to not use a template.
     * @param userName The user name of the session.
     * @return the id of the newly created spare part
     * @throws ApplicationObjectNotFoundException If the parent pool can't be found
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @throws MetadataObjectNotFoundException If the class name could not be found 
     */
    public String createSparePart(String poolId, String className, HashMap<String, String> attributes, String templateId, String userName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {

        String sparePartId = bem.createPoolItem(poolId, className, attributes, templateId);

        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT,
                String.format(ts.getTranslatedString("module.whman.actions.spare.new-spare-part.created-log"), sparePartId));

        return sparePartId;
    }
    
    /**
     * Creates a copy of an object in a warehouse. And optionally its children objects.
     * @param poolId The spare pool id.
     * @param objectClass The object class name.
     * @param ObjectId The object id.
     * @param recursive If this operation should also copy the children objects recursively.
     * @param userName The user name of the session.
     * @throws ApplicationObjectNotFoundException If the spare pool can not be found.
     * @throws InvalidArgumentException If the object can not be copy to the selected pool.
     * @throws BusinessObjectNotFoundException If the object can not be found.
     * @throws MetadataObjectNotFoundException If the object class name can no be found.
     */
    public void copyObjectToWarehouse(String poolId, String objectClass, String ObjectId, boolean recursive, String userName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        String objectId = bem.copyPoolItem(poolId, objectClass, ObjectId, recursive);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.whman.actions.copy-object-to-warehouse.copied-log"), objectId, objectClass, poolId));
    }
    
    /**
     * Moves an object from a warehouse to another warehouse.
     * @param poolId The spare pool id.
     * @param objectClass The object class name.
     * @param ObjectId The object id.
     * @param userName The user name of the session.
     * @throws ApplicationObjectNotFoundException If the spare pool can not be found.
     * @throws InvalidArgumentException If the object can not be move to the selected pool.
     * @throws BusinessObjectNotFoundException If the object can not be found.
     * @throws MetadataObjectNotFoundException If the object class name can no be found.
     */
    public void moveObjectToWarehouse(String poolId, String objectClass, String ObjectId, String userName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        bem.movePoolItem(poolId, objectClass, ObjectId);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT
                , String.format(ts.getTranslatedString("module.whman.actions.move-object-to-warehouse.moved-log"), ObjectId, objectClass, poolId));
    } 
}