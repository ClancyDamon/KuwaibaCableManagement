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
package org.neotropic.kuwaiba.core.configuration.proxies;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterators;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryProxy;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.ObjectGraphMappingService;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to manage proxies.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Service
public class ProxyManagerService {
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
     * Reference to the Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Database connection manager instance.
     */
    @Autowired
    private ConnectionManager<GraphDatabaseService> connectionManager;
    /**
     * Reference to the service that maps Java objects/attributes into graph nodes/properties.
     */
    @Autowired
    private ObjectGraphMappingService ogmService;
    /**
     * Class label
     */
    private final Label classLabel;
    /**
     * Pool label
     */
    private final Label poolLabel;
    /**
     * Proxy label
     */
    private final Label proxiesLabel;
    /**
     * Inventory Object label
     */
    private final Label inventoryObjectLabel;
    /**
     * Proxy pools label
     */
    private final Label proxyPoolsLabel;
    /**
     * List type items label
     */
    private final Label listTypeItemLabel;        
    /**
     * Relationship Name
     */
    private final String RELATIONSHIP_NAME = "hasProxy";
    
    public ProxyManagerService() {
        // Initilize labels
        this.classLabel = Label.label(Constants.LABEL_CLASS);
        this.poolLabel = Label.label(Constants.LABEL_POOLS);
        this.proxiesLabel = Label.label(Constants.LABEL_PROXIES);
        this.inventoryObjectLabel = Label.label(Constants.LABEL_INVENTORY_OBJECTS);
        this.proxyPoolsLabel = Label.label(Constants.LABEL_PROXY_POOLS);
        this.listTypeItemLabel = Label.label(Constants.LABEL_LIST_TYPE_ITEMS);
    }
    
    /**
     * Creates an inventory proxy. Inventory proxies are used to integrate third party-applications with Kuwaiba. Sometimes these applications must refer to 
     * assets managed by Kuwaiba from another perspective (financial, for example). In these applications, multiple Kuwaiba inventory assets might be represented by
     * a single entity (e.g. a router with slots, boards and ports might just be something like "standard network device"). Proxies are used to map multiple inventory 
     * elements into a single entity. It's a sort of "impedance matching" between systems that refer to the same real world object from different perspectives.
     * @param proxyPoolId The parent pool id.
     * @param proxyClass The proxy class. Must be subclass of GenericProxy.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @return The id of the newly created proxy.
     * @throws ApplicationObjectNotFoundException If the parent pool could not be found.
     * @throws InvalidArgumentException If any of the initial attributes could not be mapped.
     * @throws MetadataObjectNotFoundException If the proxy class could not be found.
     */
    public String createProxy(String proxyPoolId, String proxyClass, HashMap<String,String> attributes) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPROXY, proxyClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICPROXY, proxyClass));        
            
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, proxyClass);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.class-not-found"), proxyClass));
            
            ClassMetadata proxyMetadata = mem.getClass(proxyClass);
            
            Node parentPoolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, proxyPoolId);
            if (parentPoolNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.pool-id-not-found"), proxyPoolId));
            
            Node proxyNode = connectionManager.getConnectionHandler().createNode(proxiesLabel, inventoryObjectLabel);
            proxyNode.createRelationshipTo(parentPoolNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
            proxyNode.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);
            
            proxyNode.setProperty(Constants.PROPERTY_NAME, ""); // By default the proxy name is an empty string
            for (String attributeName : attributes.keySet()) {
                // TODO: Handle list type attributes
                if (!proxyMetadata.hasAttribute(attributeName))
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.attribute-not-found")
                            , attributeName, proxyClass));
                AttributeMetadata attributeMetadata = proxyMetadata.getAttribute(attributeName);
                proxyNode.setProperty(attributeName, Util.getRealValue(attributes.get(attributeName), attributeMetadata.getType(), ts));
            }
            proxyNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            
            String uuid = UUID.randomUUID().toString();
            proxyNode.setProperty(Constants.PROPERTY_UUID, uuid);
            
            tx.success();
            return uuid;
        }
    }
           
    /**
     * Deletes a proxy and delete its association with the related inventory objects.These objects will remain untouched.
     * @param proxyClass The class of the proxy.
     * @param proxyId The id of the proxy
     * @throws ApplicationObjectNotFoundException If the proxy could not be found.
     * @throws MetadataObjectNotFoundException If the proxy class could not be found.
     */
    public void deleteProxy(String proxyClass, String proxyId)
            throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPROXY, proxyClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICPROXY, proxyClass));      
            
            Node proxyNode = connectionManager.getConnectionHandler().findNode(proxiesLabel, Constants.PROPERTY_UUID, proxyId);
            if (proxyNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.configman.proxies.actions.proxy.not-be-found"), proxyId));
            
            proxyNode.getRelationships().forEach( aRelationship -> aRelationship.delete() );
            proxyNode.delete();
            
            tx.success();
        }
    }
    
    /**
     * Updates one or many proxy attributes.
     * @param proxyId The parent pool id,
     * @param proxyClass The class of the proxy.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @throws ApplicationObjectNotFoundException If the parent pool could not be found.
     * @throws InvalidArgumentException If any of the initial attributes could not be mapped.
     * @throws MetadataObjectNotFoundException If the proxy class could not be found.
     */
    public void updateProxy(String proxyClass, String proxyId, HashMap<String,String> attributes) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPROXY, proxyClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICPROXY, proxyClass));
            
            Node proxyNode = connectionManager.getConnectionHandler().findNode(proxiesLabel, Constants.PROPERTY_UUID, proxyId);
            if (proxyNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.configman.proxies.actions.proxy.not-be-found"), proxyId));
            
            ClassMetadata proxyMetadata = mem.getClass(proxyClass);
            for (String attributeName : attributes.keySet()) {
                if (!proxyMetadata.hasAttribute(attributeName))
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.attribute-not-found")
                            , attributeName, proxyClass));
                
                if (AttributeMetadata.isPrimitive(proxyMetadata.getAttribute(attributeName).getType())) {
                    if (attributes.get(attributeName) == null) {
                        if (attributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException(ts.getTranslatedString("module.general.messages.error-null-value"));
                        else {
                            if (proxyNode.hasProperty(attributeName))
                                proxyNode.removeProperty(attributeName);
                        }
                    } else {
                        AttributeMetadata attributeMetadata = proxyMetadata.getAttribute(attributeName);
                        proxyNode.setProperty(attributeName, Util.getRealValue(attributes.get(attributeName), attributeMetadata.getType(), ts));
                    }
                } else { // It's a list type
                    proxyNode.getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO).forEach( aListTypeItemRel -> {
                        if (aListTypeItemRel.hasProperty(Constants.PROPERTY_NAME) && 
                                aListTypeItemRel.getProperty(Constants.PROPERTY_NAME).equals(attributeName))
                            aListTypeItemRel.delete();
                    });
                    
                    if (attributes.get(attributeName) != null) {
                        String[] listTypeItemIds = attributes.get(attributeName).split(";");
                        for (String listTypeItemId : listTypeItemIds) {
                            Node listTypeItemNode = connectionManager.getConnectionHandler().findNode(listTypeItemLabel, Constants.PROPERTY_UUID, listTypeItemId);
                            if (listTypeItemNode == null)
                                throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.list-type-item-id-not-found"), 
                                        proxyMetadata.getAttribute(attributeName).getType(), listTypeItemId));
                            proxyNode.createRelationshipTo(listTypeItemNode, RelTypes.RELATED_TO).setProperty(Constants.PROPERTY_NAME, attributeName);
                        }
                        
                    }
                }
            }
            
            tx.success();
        }
    }

    /**
     * Creates a proxy pool.
     * @param name The name of the pool.
     * @param description The description of the pool
     * @return The id of the newly created proxy.
     */
    public String createProxyPool(String name, String description) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newProxyPoolNode = connectionManager.getConnectionHandler().createNode(poolLabel, proxyPoolsLabel);
            newProxyPoolNode.setProperty(Constants.PROPERTY_NAME, name == null ? "" : name);
            newProxyPoolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);
            newProxyPoolNode.setProperty(Constants.PROPERTY_CLASSNAME, Constants.CLASS_GENERICPROXY);
            newProxyPoolNode.setProperty(Constants.PROPERTY_TYPE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
            
            String uuid = UUID.randomUUID().toString();
            newProxyPoolNode.setProperty(Constants.PROPERTY_UUID, uuid);
            tx.success();
            return uuid;
        }
    }
        
    /**
     * Updates an attribute of a proxy pool.
     * @param proxyPoolId The id of the pool to be updated.
     * @param attributeName The name of the pool attribute to be updated. Valid values are "name" and "description"
     * @param attributeValue The value of the attribute. Null values will be ignored.
     * @throws ApplicationObjectNotFoundException If the pool could not be found.
     * @throws InvalidArgumentException If an unknown attribute name is provided.
     */
    public void updateProxyPool(String proxyPoolId, String attributeName, String attributeValue) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node proxyPoolNode = connectionManager.getConnectionHandler().findNode(proxyPoolsLabel, Constants.PROPERTY_UUID, proxyPoolId);
            if (proxyPoolNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.pool-id-not-found"), proxyPoolId));
            
            switch (attributeName) {
                case Constants.PROPERTY_NAME:
                case Constants.PROPERTY_DESCRIPTION:
                    proxyPoolNode.setProperty(attributeName, attributeValue);
                    break;
                default:
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.invalid-property"), attributeName));
            }
            tx.success();
        }
    }

    /**
     * Deletes a proxy pool.
     * @param proxyPoolId The id of the pool.
     * @throws ApplicationObjectNotFoundException If the pool could not be found.
     */
    public void deleteProxyPool(String proxyPoolId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node proxyPoolNode = connectionManager.getConnectionHandler().findNode(proxyPoolsLabel, Constants.PROPERTY_UUID, proxyPoolId);
            if (proxyPoolNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.pool-id-not-found"), proxyPoolId));
            
            proxyPoolNode.getRelationships(RelTypes.CHILD_OF_SPECIAL).forEach( aChildOfSpecialRelationship -> {
                Node aProxyNode = aChildOfSpecialRelationship.getStartNode();
                // On purpose, we only release the RELATED_TO_SPECIAL and CHILD_OF relationships. If anything is related to the proxy via other relationship types, this method 
                // will fail (technically, someone could try to manipulate the containment hierarchy to create children under a proxy, for example).
                aProxyNode.getRelationships(RelTypes.RELATED_TO_SPECIAL).forEach( aRelationship -> aRelationship.delete() );
                aProxyNode.getSingleRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).delete();
                aProxyNode.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).delete();
                aProxyNode.delete();
            });
            
            proxyPoolNode.delete();
            tx.success();
        }
    }
        
    /**
     * Retrieves the list of pools of proxies.
     * @return The available pools of inventory proxies.
     */
    public List<InventoryObjectPool> getProxyPools() {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<InventoryObjectPool> proxyPools = new ArrayList<>();
            connectionManager.getConnectionHandler().findNodes(proxyPoolsLabel).stream().forEach((poolNode) -> {
                if (!poolNode.hasProperty(Constants.PROPERTY_UUID))
                    poolNode.setProperty(Constants.PROPERTY_UUID, UUID.randomUUID().toString());
                
                proxyPools.add(new InventoryObjectPool((String) poolNode.getProperty(Constants.PROPERTY_UUID), 
                        (String) poolNode.getProperty(Constants.PROPERTY_NAME), 
                        (String) poolNode.getProperty(Constants.PROPERTY_DESCRIPTION),
                        Constants.CLASS_GENERICPROXY, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT)); 
            });
            tx.success();
            proxyPools.sort((o1, o2) -> {
                return o1.getName().compareTo(o2.getName());
            });
            return proxyPools;
        }
    }
    
    /**
     * Gets the list of inventory proxies in a given pool.
     * @param proxyPoolId The id of the parent pool.
     * @return The proxies
     * @throws ApplicationObjectNotFoundException If the parent pool could not be found. 
     * @throws InvalidArgumentException If the object in the database can not be mapped into an InvetoryProxy instance.
     */
    public List<InventoryProxy> getProxiesInPool(String proxyPoolId) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node proxyPoolNode = connectionManager.getConnectionHandler().findNode(proxyPoolsLabel, Constants.PROPERTY_UUID, proxyPoolId);
            if (proxyPoolNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.pool-id-not-found"), proxyPoolId));
            
            List<InventoryProxy> res = new ArrayList();
            for (Relationship rel : proxyPoolNode.getRelationships(RelTypes.CHILD_OF_SPECIAL))
                res.add(new InventoryProxy(ogmService.createObjectFromNode(rel.getStartNode())));

            Collections.sort(res);
            tx.success();
            return res;
        }
    }
    
    /**
     * Gets all the inventory proxies in the database.
     * @return The list of inventory proxy objects.
     * @throws InvalidArgumentException If any proxy node could not be mapped into a Java object.
     */
    public List<InventoryProxy> getAllProxies() throws InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List res = new ArrayList();
            
            ResourceIterator<Node> proxyNodes = connectionManager.getConnectionHandler().findNodes(proxiesLabel);
            
            while(proxyNodes.hasNext())
                res.add(new InventoryProxy(ogmService.createObjectFromNode(proxyNodes.next())));
            
            Collections.sort(res);
            tx.success();
            return res;
        }
    }
        
    /**
     * Associates an project to an inventory proxy.
     * @param projectClass The class of the project.
     * @param projectId The id of the project.
     * @param proxyClass The class of the proxy.
     * @param proxyId The id of the proxy.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If any of the objects can't be found.
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object).
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found.
     * @throws InvalidArgumentException If the a/bObjectId are null.
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found.
     */
    public void associateProjectToProxy(String projectClass, String projectId, String proxyClass, String proxyId, String userName)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException
            , InvalidArgumentException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROXY, proxyClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICPROXY, proxyClass));    
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICPROJECT, projectClass));  
        
        bem.createSpecialRelationship(proxyClass, proxyId, projectClass, projectId, RELATIONSHIP_NAME, true);
        
        aem.createObjectActivityLogEntry(userName, projectClass, projectId, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME, "",
                RELATIONSHIP_NAME, String.format(ts.getTranslatedString("module.configman.proxies.actions.associate-project.success-activity"), projectId, proxyId));
    }
    
    /**
     * Associates an inventory object to an inventory proxy.
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param proxyClass The class of the proxy.
     * @param proxyId The id of the proxy.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If any of the objects can't be found.
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object).
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found.
     * @throws InvalidArgumentException If the a/bObjectId are null.
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found.
     */
    public void associateObjectToProxy(String objectClass, String objectId, String proxyClass, String proxyId, String userName)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException
            , InvalidArgumentException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROXY, proxyClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICPROXY, proxyClass));    
        
        bem.createSpecialRelationship(proxyClass, proxyId, objectClass, objectId, RELATIONSHIP_NAME, true);
        
        aem.createObjectActivityLogEntry(userName, objectClass, objectId, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME, "", 
                RELATIONSHIP_NAME, String.format(ts.getTranslatedString("module.configman.proxies.actions.associate-object-to-proxy.success-activity"), objectId, proxyId));
    }
    
    /**
     * Releases a project from a proxy.
     * @param proxyClass The class of the proxy.
     * @param proxyId The id of the proxy.
     * @param projectClass The class of the project.
     * @param projectId The id of the project.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If the object can not be found.
     * @throws MetadataObjectNotFoundException If the class can not be found.
     * @throws InvalidArgumentException If serviceId or objectId are null.
     * @throws ApplicationObjectNotFoundException If the object activity log
     * could no be found.
     */
    public void releaseProjectFromProxy(String projectClass, String projectId, String proxyClass, String proxyId, String userName)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROXY, proxyClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICPROXY, proxyClass));    

        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICPROJECT, projectClass));  

        bem.releaseSpecialRelationship(proxyClass, proxyId, projectId, RELATIONSHIP_NAME);

        aem.createObjectActivityLogEntry(userName, projectClass, projectId, ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME,
                RELATIONSHIP_NAME, "", String.format(ts.getTranslatedString("module.configman.proxies.actions.release-project.success-activity"), projectId, proxyId));
    }
      
    /**
     * Releases an inventory object from a proxy.
     * @param proxyClass The class of the proxy.
     * @param proxyId The id of the proxy.
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If the object can not be found.
     * @throws MetadataObjectNotFoundException If the class can not be found.
     * @throws InvalidArgumentException If serviceId or objectId are null.
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found.
     */
    public void releaseObjectFromProxy(String objectClass, String objectId, String proxyClass, String proxyId, String userName)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROXY, proxyClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICPROXY, proxyClass));    

        bem.releaseSpecialRelationship(proxyClass, proxyId, objectId, RELATIONSHIP_NAME);

        aem.createObjectActivityLogEntry(userName, objectClass, objectId, ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME,
                RELATIONSHIP_NAME, "", String.format(ts.getTranslatedString("module.configman.proxies.actions.release-object.success-activity"), objectId, proxyId));
    }
    
    /**
     * Moves a proxy from a pool to another pool.
     * @param poolId The id of the parent pool.
     * @param proxyClass  The class of the proxy.
     * @param proxyId The id of the proxy.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If the proxy can not be found.
     * @throws MetadataObjectNotFoundException If the proxy class name can no be found.
     * @throws ApplicationObjectNotFoundException If the pool node can not be found.
     * @throws InvalidArgumentException If the proxy can not be move to the selected pool.
     */
    public void moveProxyToPool(String poolId, String proxyClass, String proxyId, String userName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, BusinessObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROXY, proxyClass))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICPROXY, proxyClass));
        
        bem.movePoolItem(poolId, proxyClass, proxyId);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT
                , String.format(ts.getTranslatedString("module.configman.proxies.actions.move-proxy-to-pool.moved-log"), proxyId, poolId));
    }
        
    /**
     * Creates a copy of a proxy.And optionally its children objects.
     * @param poolId The id of the proxy pool.
     * @param proxyClass The class of the proxy.
     * @param proxyId The id of the proxy.
     * @param userName The user name of the session.
     * @return The newly created proxy id.
     * @throws ApplicationObjectNotFoundException If the pool node can not be found.
     * @throws InvalidArgumentException If the proxy can not be copy to the selected pool.
     * @throws BusinessObjectNotFoundException If the proxy can not be found.
     * @throws MetadataObjectNotFoundException  If the proxy class name can no be found.
     */
    public String copyProxyToPool(String poolId, String proxyClass, String proxyId, String userName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {    
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node poolNode = connectionManager.getConnectionHandler().findNode(proxyPoolsLabel, Constants.PROPERTY_UUID, poolId);
            
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.pool-id-not-found"), poolId));
            
            Node proxyClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, proxyClass);
            
            if (proxyClassNode == null)
                throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.class-not-found"), proxyClass));
            
            if (!mem.isSubclassOf((String) poolNode.getProperty(Constants.PROPERTY_CLASSNAME), proxyClass))
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                        , proxyClass, (String) poolNode.getProperty(Constants.PROPERTY_CLASSNAME)));
                                    
            Node newInstance = null;
            
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (class:classes{name:'%s'})<-[:INSTANCE_OF]-(proxy:proxies{_uuid:'%s'})").append("\n");
            queryBuilder.append("Return proxy");
            
            String query  = String.format(queryBuilder.toString(), proxyClass, proxyId);
            Result queryResult = connectionManager.getConnectionHandler().execute(query);
            Iterator<Node> column = queryResult.columnAs("proxy");
            
            for (Node node: Iterators.asIterable(column))
                newInstance = ogmService.copyObject(node, false, proxiesLabel, inventoryObjectLabel);
            
            if (newInstance == null)
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.object-not-have-uuid"), proxyId));
            
            String newInstanceUuid = newInstance.hasProperty(Constants.PROPERTY_UUID) ? (String) newInstance.getProperty(Constants.PROPERTY_UUID) : null;
            if (newInstanceUuid == null)
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.object-not-have-uuid"), newInstance.getId()));
            
            newInstance.createRelationshipTo(poolNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);            
            tx.success();
            
            aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.configman.proxies.actions.copy-proxy-to-pool.copied-log"), newInstanceUuid, poolId));
            
            return newInstanceUuid;
        }
    }
}