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
package org.neotropic.kuwaiba.modules.optional.physcon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterators;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.PersistenceService;
import org.neotropic.kuwaiba.core.persistence.PersistenceService.EXECUTION_STATE;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.ObjectGraphMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to manage the physical connections
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Service
public class PhysicalConnectionsService {
    /**
     * Object Label.
     */
    private final Label inventoryObjectLabel = Label.label(Constants.LABEL_INVENTORY_OBJECTS);
    /**
     * The Translation Service instance.
     */
    private TranslationService ts;
    /**
     * The Persistence Service instance.
     */
    @Autowired
    private PersistenceService persistenceService;
    /**
     * The Connection Manager instance.
     */
    @Autowired
    private ConnectionManager<GraphDatabaseService> connectionManager;
    /**
     * The Application Entity Manager instance.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * The Business Entity Manager instance.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * The Metadata Entity Manager instance.
     */
    @Autowired
    private MetadataEntityManager mem;
    
    @Autowired
    private ObjectGraphMappingService ogmService;
    /**
     * A side in a physical connection.
     */
    public static String RELATIONSHIP_ENDPOINTA = "endpointA"; //NOI18N
    /**
     * B side in a physical connection.
     */
    public static String RELATIONSHIP_ENDPOINTB = "endpointB"; //NOI18N
    
    public String createPhysicalConnection(String aObjectClass, String aObjectId, 
        String bObjectClass, String bObjectId, String name, String connectionClass,
        String templateId, String userName) throws IllegalStateException, OperationNotPermittedException {
        if (persistenceService.getState() == EXECUTION_STATE.STOPPED)
            throw new IllegalStateException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        String newConnectionId = null;
        try {
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new OperationNotPermittedException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass"), connectionClass, Constants.CLASS_GENERICPHYSICALCONNECTION)); //NOI18N
            
            //The connection (either link or container, will be created in the closest common parent between the endpoints)
            BusinessObjectLight commonParent = bem.getCommonParent(aObjectClass, aObjectId, bObjectClass, bObjectId);
            
            if (commonParent == null || commonParent.getName().equals(Constants.DUMMY_ROOT))
                throw new OperationNotPermittedException(ts.getTranslatedString("module.physcon.messages.no-common-parent"));
            
            boolean isLink = false;
            
            //Check if the endpoints are already connected, but only if the connection is a link (the endpoints are ports)
            if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, connectionClass)) { //NOI18N
                
                if (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, aObjectClass) || !mem.isSubclassOf(Constants.CLASS_GENERICPORT, bObjectClass)) //NOI18N
                    throw new OperationNotPermittedException(ts.getTranslatedString("module.physcon.messages.is-not-a-port"));
                
                if (!bem.getSpecialAttribute(aObjectClass, aObjectId, RELATIONSHIP_ENDPOINTA).isEmpty()) //NOI18N
                    
                    throw new OperationNotPermittedException(String.format(ts.getTranslatedString("module.physcon.messages.endpoint-connected"), bem.getObjectLight(aObjectClass, aObjectId)));

                if (!bem.getSpecialAttribute(bObjectClass, bObjectId, RELATIONSHIP_ENDPOINTB).isEmpty()) //NOI18N
                    throw new OperationNotPermittedException(String.format(ts.getTranslatedString("module.physcon.messages.endpoint-connected"), bem.getObjectLight(bObjectClass, bObjectId)));
                
                isLink = true;
            }

            
            HashMap<String, String> attributes = new HashMap<>();
            if (name == null || name.isEmpty())
                throw new OperationNotPermittedException(ts.getTranslatedString("module.physcon.messages.name-empty"));
            
            attributes.put(Constants.PROPERTY_NAME, name);
            
            newConnectionId = bem.createSpecialObject(connectionClass, commonParent.getClassName(), commonParent.getId(), attributes, templateId);
            
            if (isLink) { //Check connector mappings only if it's a link
                aem.checkRelationshipByAttributeValueBusinessRules(connectionClass, newConnectionId, aObjectClass, aObjectId);
                aem.checkRelationshipByAttributeValueBusinessRules(connectionClass, newConnectionId, bObjectClass, bObjectId);
            }
            
            bem.createSpecialRelationship(connectionClass, newConnectionId, aObjectClass, aObjectId, RELATIONSHIP_ENDPOINTA, true);
            bem.createSpecialRelationship(connectionClass, newConnectionId, bObjectClass, bObjectId, RELATIONSHIP_ENDPOINTB, true);
            
            aem.createGeneralActivityLogEntry(userName, 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [%s] (%s)", name, connectionClass, newConnectionId));
            
            return newConnectionId;
        } catch (InventoryException e) {
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != null) {
                try {
                    bem.deleteObject(connectionClass, newConnectionId, true);
                } catch (InventoryException ex) {
                }
            }
            throw new OperationNotPermittedException(e.getMessage());
        }
    }
    
    public void deletePhysicalConnection(String objectClassName, String objectId, String userName) throws IllegalStateException, InventoryException {
        if (persistenceService.getState() == EXECUTION_STATE.STOPPED)
            throw new IllegalStateException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, objectClassName))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.physcon.messages.is-not-a-phys-conn"), objectClassName));
        bem.deleteObject(objectClassName, objectId, true);
        aem.createGeneralActivityLogEntry(userName, 
            ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
            String.format("Deleted %s instance with id %s", objectClassName, objectId));
    }
    /**
     * Finds the physical path from one port to another
     * @param objectClass The source port class.
     * @param objectId The source port id.
     * @return A list of objects that make part of the physical trace.
     * @throws MetadataObjectNotFoundException can't f
     * @throws BusinessObjectNotFoundException 
     * @throws ApplicationObjectNotFoundException
     * @throws InvalidArgumentException If one of the 
     */
    public List<BusinessObjectLight> getPhysicalPath(String objectClass, String objectId) throws 
        IllegalStateException, MetadataObjectNotFoundException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        
        if (persistenceService.getState() == EXECUTION_STATE.STOPPED)
            throw new IllegalStateException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        List<BusinessObjectLight> path = new ArrayList<>();
        //If the port is a logical port (virtual port, Pseudowire or service instance, we look for the first physical parent port)
        String logicalPortId = null;
        if(mem.isSubclassOf(Constants.CLASS_GENERICLOGICALPORT, objectClass)){
            logicalPortId = objectId;
            if(objectClass.equals("Pseudowire"))
                objectId = bem.getFirstParentOfClass(objectClass, objectId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT).getId();
            else{    
                BusinessObjectLight firstPhysicalParentPort = bem.getFirstParentOfClass(objectClass, objectId, Constants.CLASS_GENERICPHYSICALPORT);
                objectId = firstPhysicalParentPort.getId();
            }
        }
        //The first part of the query will return many paths, the longest is the one we need. The others are
        //subsets of the longest
        String cypherQuery = "MATCH paths = (o)-[r:" + RelTypes.RELATED_TO_SPECIAL + "*]-(c) "+
                             "WHERE o._uuid = '" + objectId + "' AND all(rel in r where rel.name IN ['mirror','mirrorMultiple'] or rel.name = 'endpointA' or rel.name = 'endpointB') "+
                             "WITH nodes(paths) as path " +
                             "RETURN path ORDER BY length(path) DESC LIMIT 1";
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()){
            if(logicalPortId != null)
                path.add(ogmService.createObjectLightFromNode(connectionManager.getConnectionHandler().findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, logicalPortId)));
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<List<Node>> column = result.columnAs("path");
            
            for (List<Node> listOfNodes : Iterators.asIterable(column)) {
                for(Node node : listOfNodes)
                    path.add(ogmService.createObjectLightFromNode(node));
            }
        }
        return path;
    }
    /**
     * Gets A tree representation of all physical paths as a hash map.
     * @param objectClass The source port class
     * @param objectId The source port id
     * @return A tree representation of all physical paths as a hash map
     * @throws BusinessObjectNotFoundException If any of the objects involved in the path cannot be found
     * @throws MetadataObjectNotFoundException If any of the object classes involved in the path cannot be found
     * @throws ApplicationObjectNotFoundException If any of the objects involved in the path has a malformed list type attribute
     * @throws InvalidArgumentException If any of the objects involved in the path has an invalid objectId or className
     */    
    public HashMap<BusinessObjectLight, List<BusinessObjectLight>> getPhysicalTree(String objectClass, String objectId) throws 
        IllegalStateException, BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        
        if (persistenceService.getState() == EXECUTION_STATE.STOPPED)
            throw new IllegalStateException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        HashMap<BusinessObjectLight, List<BusinessObjectLight>> tree = new LinkedHashMap();
        // If the port is a logical port (virtual port, Pseudowire or service instance, we look for the first physical parent port)
        String logicalPortId = null;
        if (mem.isSubclassOf(Constants.CLASS_GENERICLOGICALPORT, objectClass)) {
            logicalPortId = objectId;
            if (objectClass.equals("Pseudowire")) {
                BusinessObjectLight object = bem.getFirstParentOfClass(objectClass, objectId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                objectId = object.getId();
            } else {
                BusinessObjectLight firstPhysicalParentPort = bem.getFirstParentOfClass(objectClass, objectId, Constants.CLASS_GENERICPHYSICALPORT);
                objectId = firstPhysicalParentPort.getId();
            }
        }
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            //The first part of the query will return many paths, that we build as a tree
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(String.format("MATCH paths = (o)-[r:%s*]-(c) ", RelTypes.RELATED_TO_SPECIAL));
            queryBuilder.append(String.format("WHERE o._uuid = '%s' AND all(rel in r where rel.name IN ['mirror','mirrorMultiple'] or rel.name = 'endpointA' or rel.name = 'endpointB') ", objectId));
            queryBuilder.append("WITH nodes(paths) as path ");
            queryBuilder.append("RETURN path ORDER BY length(path) DESC");

            BusinessObjectLight logicalPort = null;
            if (logicalPortId != null) {
                logicalPort = ogmService.createObjectLightFromNode(connectionManager.getConnectionHandler().findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, logicalPortId));
                tree.put(logicalPort, new ArrayList());
            }
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString());
            Iterator<List<Node>> column = result.columnAs("path"); //NOI18N

            for (List<Node> listOfNodes : Iterators.asIterable(column)) {
                for (int i = 0; i < listOfNodes.size(); i++) {
                    BusinessObjectLight object = ogmService.createObjectLightFromNode(listOfNodes.get(i));

                    if (!tree.containsKey(object))
                        tree.put(object, new ArrayList());

                    if (logicalPort != null && i == 0)
                        tree.get(logicalPort).add(object);

                    if (i < listOfNodes.size() - 1) {
                        BusinessObjectLight nextObject = ogmService.createObjectLightFromNode(listOfNodes.get(i + 1));

                        if (!tree.get(object).contains(nextObject))
                            tree.get(object).add(nextObject);
                    }
                }
            }
            tx.success();
        }
        return tree;
    }
}
