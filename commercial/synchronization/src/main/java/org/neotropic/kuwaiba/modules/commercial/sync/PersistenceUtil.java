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

package org.neotropic.kuwaiba.modules.commercial.sync;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.UnsupportedPropertyException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.GenericObjectList;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.ObjectGraphMappingService;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceCommonParameters;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
import org.neotropic.kuwaiba.modules.commercial.sync.model.TemplateDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class PersistenceUtil {

    public static ObjectGraphMappingService ogmService;

    /**
     * Converts a node representing a Node into a SynchronizationGroup object
     *
     * @param syncGroupNode The source node
     * @return A SynchronizationGroup object built from the source node information
     * @throws InvalidArgumentException        if some element of the list of
     *                                         syncDataSourceConfiguration has more paramNames than paramValues
     * @throws MetadataObjectNotFoundException If can not find the class name of
     *                                         the device related with the data source configuration.
     * @throws UnsupportedPropertyException    If the sync group can not be mapped
     *                                         into a Java object.
     */
    public static SynchronizationGroup createSyncGroupFromNode(Node syncGroupNode)
            throws InvalidArgumentException, MetadataObjectNotFoundException, UnsupportedPropertyException {

        if (!(syncGroupNode.hasProperty(Constants.PROPERTY_NAME.toUpperCase())
                || syncGroupNode.hasProperty(Constants.PROPERTY_NAME.toLowerCase())))
            throw new InvalidArgumentException(String.format("The sync group with id %s is malformed. Check its properties", syncGroupNode.getId()));
        String name = (String) (syncGroupNode.hasProperty(Constants.PROPERTY_NAME.toUpperCase()) ?
                syncGroupNode.getProperty(Constants.PROPERTY_NAME.toUpperCase()) :
                syncGroupNode.getProperty(Constants.PROPERTY_NAME.toLowerCase()));

        List<SyncDataSourceConfiguration> syncDataSourceConfiguration = new ArrayList<>();

        for (Relationship rel : syncGroupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_SYNC_GROUP))
            syncDataSourceConfiguration.add(createSyncDataSourceConfigFromNode(rel.getStartNode()));

        String description = null;
        if (syncGroupNode.hasProperty(Constants.PROPERTY_DESCRIPTION.toUpperCase())
                || syncGroupNode.hasProperty(Constants.PROPERTY_DESCRIPTION.toLowerCase()))
            description = (String) (syncGroupNode.hasProperty(Constants.PROPERTY_DESCRIPTION.toUpperCase()) ?
                    syncGroupNode.getProperty(Constants.PROPERTY_DESCRIPTION.toUpperCase()) :
                    syncGroupNode.getProperty(Constants.PROPERTY_DESCRIPTION.toLowerCase()));

        return new SynchronizationGroup(syncGroupNode.getId(), name, description
                , syncDataSourceConfiguration);
    }

    /**
     * Converts a node to a SyncDataSourceConfiguration object
     *
     * @param syncDataSourceConfigNode The source node
     * @return A SyncDataSourceConfiguration object built from the source node information
     * @throws InvalidArgumentException     if some element of the list of
     *                                      syncDataSourceConfiguration has more paramNames than paramValues
     * @throws UnsupportedPropertyException if any property of the sync data source node is malformed or if there is an
     *                                      error with the relationship between the syncNode and it InventoryObjectNode
     */
    public static SyncDataSourceConfiguration createSyncDataSourceConfigFromNode(Node syncDataSourceConfigNode) throws UnsupportedPropertyException, InvalidArgumentException {

        if (!(syncDataSourceConfigNode.hasProperty(Constants.PROPERTY_NAME.toUpperCase())
                || syncDataSourceConfigNode.hasProperty(Constants.PROPERTY_NAME.toLowerCase())))
            throw new UnsupportedPropertyException(String.format("The sync configuration with id %s is malformed. Its name is empty", syncDataSourceConfigNode.getId()));

        if (!syncDataSourceConfigNode.hasRelationship(RelTypes.HAS_SYNC_CONFIGURATION))
            throw new UnsupportedPropertyException(String.format("The sync configuration with id %s is malformed. It is not related to any inventory object", syncDataSourceConfigNode.getId()));

        HashMap<String, String> parameters = new HashMap<>();
        String configName = "";
        String configDescription = "";

        for (String property : syncDataSourceConfigNode.getPropertyKeys()) {
            if (property.equalsIgnoreCase(Constants.PROPERTY_NAME))
                configName = (String) syncDataSourceConfigNode.getProperty(property);
            else if (property.equalsIgnoreCase(Constants.PROPERTY_UUID))
                throw new UnsupportedPropertyException("The sync configuration with id %s is malformed. It seems to be incorrectly related to a network device");
            else if (property.equalsIgnoreCase(Constants.PROPERTY_DESCRIPTION))
                configDescription = (String) syncDataSourceConfigNode.getProperty(property);
            else
                parameters.put(property, (String) syncDataSourceConfigNode.getProperty(property));
        }
        SyncDataSourceConfiguration syncDataSourceConfiguration = new SyncDataSourceConfiguration(syncDataSourceConfigNode.getId()
                , configName, configDescription, parameters);

        Node inventoryObjectNode = syncDataSourceConfigNode.getSingleRelationship(RelTypes.HAS_SYNC_CONFIGURATION, Direction.INCOMING).getStartNode();
        if (inventoryObjectNode != null) {
            BusinessObject businessObject = ogmService.createObjectFromNode(inventoryObjectNode);
            syncDataSourceConfiguration.setBusinessObjectLight(businessObject);
        }

        Node commonPropertiesNode = syncDataSourceConfigNode.getSingleRelationship(RelTypes.HAS_SYNC_COMMON_PROPERTIES, Direction.OUTGOING).getEndNode();
        if (commonPropertiesNode != null) {
            SyncDataSourceCommonParameters entity = PersistenceUtil
                    .createSyncDSCommonPropertiesFromNode(commonPropertiesNode);
            syncDataSourceConfiguration.setCommonParameters(entity);
        }

        Node templateDataSourceNode = syncDataSourceConfigNode.getSingleRelationship(RelTypes.EXTENDS_FROM_TEMPLATE, Direction.OUTGOING).getEndNode();
        if (templateDataSourceNode != null) {
            TemplateDataSource entity = PersistenceUtil
                    .createTemplateDataSourceFromNode(templateDataSourceNode);
            syncDataSourceConfiguration.setTemplateDataSource(entity);
        }

        return syncDataSourceConfiguration;
    }

    /**
     * Creates a generic object list (a list type) from a node
     *
     * @param node the list type node
     * @return a list type The specified list type node is malformed and lacks uuid property.
     * @throws InvalidArgumentException if some element of the list of
     *                                  syncDataSourceConfiguration has more paramNames than paramValues
     */
    public static GenericObjectList createGenericObjectListFromNode(Node node) throws InvalidArgumentException {
        String listTypeNodeUuid = node.hasProperty(Constants.PROPERTY_UUID) ? (String) node.getProperty(Constants.PROPERTY_UUID) : null;

        if (listTypeNodeUuid == null)
            throw new InvalidArgumentException(String.format("The list type item with id %s does not have uuid", node.getId()));
        String name = (String) (node.getProperty(Constants.PROPERTY_NAME.toUpperCase()) != null ?
                node.getProperty(Constants.PROPERTY_NAME.toUpperCase()) :
                node.getProperty(Constants.PROPERTY_NAME.toLowerCase()));


        GenericObjectList listType = new GenericObjectList(listTypeNodeUuid, name);
        return listType;
    }

    /**
     * Converts a node to a SyncDataSourceConfiguration object
     *
     * @param syncDSCommonPropertiesNode The source node
     * @return A SyncDataSourceCommonParameters object built from the source node information
     * @throws InvalidArgumentException     if the size of the list of paramNames and paramValues are not the same
     * @throws UnsupportedPropertyException if any property of the sync data source node is malformed or if there is an
     *                                      error with the relationship between the syncNode and it InventoryObjectNode
     */
    public static SyncDataSourceCommonParameters createSyncDSCommonPropertiesFromNode(Node syncDSCommonPropertiesNode)
            throws UnsupportedPropertyException, InvalidArgumentException {

        HashMap<String, String> parameters = new HashMap<>();
        String datasourceType = null;

        for (String property : syncDSCommonPropertiesNode.getPropertyKeys()) {
            if (property.equalsIgnoreCase(Constants.PROPERTY_DATASOURCE_TYPE))
                datasourceType = (String) syncDSCommonPropertiesNode.getProperty(property);
            else
                parameters.put(property, (String) syncDSCommonPropertiesNode.getProperty(property));
        }

        return new SyncDataSourceCommonParameters(syncDSCommonPropertiesNode.getId(), datasourceType, parameters);
    }

    /**
     * Transform Template Datasource node into entity
     *
     * @param node Template Datasource node
     * @return Template Datasource entity
     * @throws UnsupportedPropertyException if any property of the sync data source node is malformed or if there is an
     *                                      error with the relationship between the syncNode and it InventoryObjectNode
     * @throws InvalidArgumentException     if the size of the list of paramNames and paramValues are not the same
     */
    static TemplateDataSource createTemplateDataSourceFromNode(Node node)
            throws UnsupportedPropertyException, InvalidArgumentException {
        HashMap<String, String> parameters = new HashMap<>();
        String name = null;
        String description = null;

        for (String property : node.getPropertyKeys()) {
            if (property.equalsIgnoreCase(Constants.PROPERTY_NAME))
                name = (String) node.getProperty(property);
            else if (property.equalsIgnoreCase(Constants.PROPERTY_DESCRIPTION))
                description = (String) node.getProperty(property);
            else
                parameters.put(property, (String) node.getProperty(property));
        }

        return new TemplateDataSource(node.getId(), name, description, parameters);
    }

    /**
     * Reference to the service that maps nodes to inventory/application objects
     */
    @Autowired
    public void setObjectGraphMappingService(ObjectGraphMappingService ogmService) {
        PersistenceUtil.ogmService = ogmService;
    }
}
