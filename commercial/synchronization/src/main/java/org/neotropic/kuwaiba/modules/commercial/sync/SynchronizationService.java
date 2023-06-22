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
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.UnsupportedPropertyException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.ObjectGraphMappingService;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.Util;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceCommonParameters;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
import org.neotropic.kuwaiba.modules.commercial.sync.model.TemplateDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The service corresponding to the Synchronization module.
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class SynchronizationService {

    /**
     * SyncGroup label
     */
    private final Label syncGroupsLabel = Label.label(Constants.LABEL_SYNCGROUPS);
    /**
     * Template data source label
     */
    private final Label templateDataSourceLabel = Label.label(Constants.LABEL_TEMPLATE_DATASOURCE);
    /**
     * SyncDataSourceConfig label
     */
    private final Label syncDatasourceConfigLabel = Label.label(Constants.LABEL_SYNCDSCONFIG);
    /**
     * SyncDataSourceConfig label
     */
    private final Label syncDsCommonPropertiesLabel = Label.label(Constants.LABEL_SYNCDSCOMMON);
    /**
     * Object label
     */
    private final Label inventoryObjectsLabel = Label.label(Constants.LABEL_INVENTORY_OBJECTS);

    @Autowired
    private TranslationService ts;
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
    /**
     * The Connection Manager instance.
     */
    @Autowired
    private ConnectionManager<GraphDatabaseService> connectionManager;
    /**
     * Reference to the service that maps nodes to inventory/application objects
     */
    @Autowired
    private ObjectGraphMappingService ogmService;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private ProviderRegistry providerRegistry;


    /**
     * Fetches a synchronization group. From the conceptual point of view, a
     * sync group is a set of Synchronization Data Sources.
     *
     * @param syncGroupId The id of the sync group.
     * @return The sync group.
     * @throws ApplicationObjectNotFoundException If the sync group could not be
     *                                            found.
     * @throws InvalidArgumentException           If the sync data group information is
     *                                            somehow malformed in the database.
     * @throws MetadataObjectNotFoundException    If can not find the class name of
     *                                            the device related with the data source configuration.
     * @throws UnsupportedPropertyException       If the sync group can not be mapped
     *                                            into a Java object.
     */
    public SynchronizationGroup getSyncGroup(long syncGroupId) throws InvalidArgumentException, ApplicationObjectNotFoundException,
            MetadataObjectNotFoundException, UnsupportedPropertyException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);
            if (syncGroupNode == null) {
                throw new ApplicationObjectNotFoundException(
                        String.format(ts.getTranslatedString("module.sync.actions.get-sync-group.messages.sync-group-not-found"), syncGroupId));
            }
            return PersistenceUtil.createSyncGroupFromNode(syncGroupNode);
        }
    }

    /**
     * Gets the list of available sync groups
     *
     * @return The list of available sync groups
     * @throws InvalidArgumentException        If any of the sync groups is malformed
     *                                         in the database
     * @throws MetadataObjectNotFoundException if the parent class is not
     *                                         found when the parent's bread crumbs is been created for the selected item in the navigation tree
     * @throws UnsupportedPropertyException    if any property of the sync
     *                                         data source node is malformed or if there is an error with the relationship between the syncNode and it InventoryObjectNode
     */
    public List<SynchronizationGroup> getSyncGroups() throws InvalidArgumentException, MetadataObjectNotFoundException, UnsupportedPropertyException {
        List<SynchronizationGroup> synchronizationGroups = new ArrayList<>();

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            ResourceIterator<Node> syncGroupsNodes = connectionManager.getConnectionHandler().findNodes(syncGroupsLabel);

            while (syncGroupsNodes.hasNext()) {
                Node syncGroup = syncGroupsNodes.next();
                synchronizationGroups.add(PersistenceUtil.createSyncGroupFromNode(syncGroup));
            }
            tx.success();
            return synchronizationGroups;
        }
    }

    /**
     * Gets the list of available sync groups
     *
     * @param filters : filter over SyncDataSourceConfiguration properties
     * @param offset  : skiped values over full response, -1 for full response
     * @param limit   : maximun values display per response, -1 for full response
     * @return The list of available sync groups
     * @throws UnsupportedPropertyException if any property of the sync
     *                                      data source node is malformed or if there is an error with the relationship between the syncNode and it InventoryObjectNode
     */
    public List<SynchronizationGroup> getSyncGroups(HashMap<String, String> filters, int offset, int limit)
            throws UnsupportedPropertyException {
        List<SynchronizationGroup> synchronizationGroups = new ArrayList<>();

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            //GenericCommunicationElement -> SyncDatasourceConfiguration
            //cardinality (1..*)
            cypherQuery.append("MATCH (node:").append(syncGroupsLabel).append(")");
            cypherQuery.append(" WHERE TRUE ");
            if (filters != null)
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND UPPER(node.").append(key).append(") CONTAINS UPPER('").append(filters.get(key)).append("')")
                );

            cypherQuery.append(" RETURN node");
            if (offset >= 0 && limit >= 0) {
                params.put("offset", offset);
                params.put("limit", limit);
                cypherQuery.append(" SKIP $offset LIMIT $limit");
            }

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);
            ResourceIterator<Node> entityNodes = result.columnAs("node");

            while (entityNodes.hasNext()) {
                Node node = entityNodes.next();
                synchronizationGroups.add(PersistenceUtil.createSyncGroupFromNode(node));
            }
            tx.success();
            return synchronizationGroups;
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: getting groups %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: getting groups [%s] %s", SynchronizationService.class, ex.getMessage()));
        }
    }

    /**
     * Gets the list of available sync groups count
     *
     * @param filters : filter over SyncDataSourceConfiguration properties
     * @return The list of available sync groups
     * @throws UnsupportedPropertyException if any property of the sync
     *                                      data source node is malformed or if there is an error with the relationship between the syncNode and it InventoryObjectNode
     */
    public int getSyncGroups(HashMap<String, String> filters) throws UnsupportedPropertyException {
        Long count = 0L;

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            //GenericCommunicationElement -> SyncDatasourceConfiguration
            //cardinality (1..*)
            cypherQuery.append("MATCH (node:").append(syncGroupsLabel).append(")");
            cypherQuery.append(" WHERE TRUE ");
            if (filters != null)
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND UPPER(node.").append(key).append(") CONTAINS UPPER('").append(filters.get(key)).append("')")
                );
            cypherQuery.append(" RETURN COUNT(node) AS count");
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);

            while (result.hasNext())
                count = (Long) result.next().get("count");
            tx.success();
            return count.intValue();
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: getting groups %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: getting groups [%s] %s", SynchronizationService.class, ex.getMessage()));
        }
    }

    /**
     * Gets the list of available sync groups
     *
     * @param dataSrcId the object id (a GenericCommunicationElement) or the
     *                  SyncDataSourceConfig id
     * @param filters   : filter over SyncDataSourceConfiguration properties
     * @param offset    : skiped values over full response, -1 for full response
     * @param limit     : maximun values display per response, -1 for full response
     * @return The list of available sync groups
     * @throws InvalidArgumentException        If any of the sync groups is malformed
     *                                         in the database
     * @throws MetadataObjectNotFoundException if the parent class is not
     *                                         found when the parent's bread crumbs is been created for the selected item in the navigation tree
     * @throws UnsupportedPropertyException    if any property of the sync
     *                                         data source node is malformed or if there is an error with the relationship between the syncNode and it InventoryObjectNode
     */
    public List<SynchronizationGroup> getSyncGroupsFromDataSource(long dataSrcId, HashMap<String, String> filters, int offset, int limit)
            throws InvalidArgumentException, MetadataObjectNotFoundException, UnsupportedPropertyException {
        List<SynchronizationGroup> synchronizationGroups = new ArrayList<>();

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            params.put(Constants.PROPERTY_ID, dataSrcId);
            //GenericCommunicationElement -> SyncDatasourceConfiguration
            //cardinality (1..*)
            cypherQuery.append("MATCH (node:").append(syncGroupsLabel).append(")");
            cypherQuery.append("<-[:").append(RelTypes.BELONGS_TO_SYNC_GROUP).append("]");
            cypherQuery.append("-(dataSrc:").append(syncDatasourceConfigLabel).append(")");
            cypherQuery.append(" WHERE ID(dataSrc) = $id ");
            if (filters != null)
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND UPPER(node.").append(key).append(") CONTAINS UPPER('").append(filters.get(key)).append("')")
                );

            cypherQuery.append(" RETURN node");
            if (offset >= 0 && limit >= 0) {
                params.put("offset", offset);
                params.put("limit", limit);
                cypherQuery.append(" SKIP $offset LIMIT $limit");
            }

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);
            ResourceIterator<Node> entityNodes = result.columnAs("node");

            while (entityNodes.hasNext()) {
                Node node = entityNodes.next();
                synchronizationGroups.add(PersistenceUtil.createSyncGroupFromNode(node));
            }
            tx.success();
            return synchronizationGroups;
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: getting groups from datasource %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: getting groups from datasource [%s] %s", SynchronizationService.class, ex.getMessage()));
        }
    }

    /**
     * Gets the list of available sync groups
     *
     * @param dataSrcId the object id (a GenericCommunicationElement) or the
     *                  SyncDataSourceConfig id
     * @param filters   filters over data source
     * @return The list of available sync groups
     * @throws InvalidArgumentException        If any of the sync groups is malformed
     *                                         in the database
     * @throws MetadataObjectNotFoundException if the parent class is not
     *                                         found when the parent's bread crumbs is been created for the selected item in the navigation tree
     * @throws UnsupportedPropertyException    if any property of the sync
     *                                         data source node is malformed or if there is an error with the relationship between the syncNode and it InventoryObjectNode
     */
    public int getSyncGroupsFromDataSourceCount(long dataSrcId, HashMap<String, String> filters)
            throws InvalidArgumentException, MetadataObjectNotFoundException, UnsupportedPropertyException {
        Long count = 0L;

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            params.put(Constants.PROPERTY_ID, dataSrcId);
            //GenericCommunicationElement -> SyncDatasourceConfiguration
            //cardinality (1..*)
            cypherQuery.append("MATCH (node:").append(syncGroupsLabel).append(")");
            cypherQuery.append("-[:").append(RelTypes.BELONGS_TO_SYNC_GROUP).append("]");
            cypherQuery.append("->(dataSrc:").append(syncDatasourceConfigLabel).append(")");
            cypherQuery.append(" WHERE ID(dataSrc) = $id ");
            if (filters != null)
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND UPPER(node.").append(key).append(") CONTAINS UPPER('").append(filters.get(key)).append("')")
                );
            cypherQuery.append(" RETURN COUNT(node) AS count");
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);

            while (result.hasNext())
                count = (Long) result.next().get("count");
            tx.success();
            return count.intValue();
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: getting groups from datasource %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: getting groups from datasource [%s] %s", SynchronizationService.class, ex.getMessage()));
        }
    }

    /**
     * Gets a data source configuration of the object (there is only one data
     * source configuration per object)
     *
     * @param objectId the object id (a GenericCommunicationElement) or the
     *                 SyncDataSourceConfig id
     * @return a SyncDataSourceConfiguration
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database
     * @throws ApplicationObjectNotFoundException If the sync data source
     *                                            configuration could not be found
     * @throws UnsupportedPropertyException       if any property of the sync data
     *                                            source node is malformed or if there is an error with the relationship
     *                                            between the syncNode and it InventoryObjectNode
     */
    public SyncDataSourceConfiguration getSyncDataSourceConfiguration(String objectId)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node inventoryObjectNode = connectionManager.getConnectionHandler().findNode(inventoryObjectsLabel, Constants.PROPERTY_UUID, objectId);

            Node syncDatasourceConfiguration = null;
            if (inventoryObjectNode != null) {
                if (!inventoryObjectNode.hasRelationship(RelTypes.HAS_SYNC_CONFIGURATION)) {
                    throw new UnsupportedPropertyException(String.format(ts.getTranslatedString("module.sync.actions.get-sync-data-source-configuration.messages.no-ds-config"),
                            inventoryObjectNode.getProperty(Constants.PROPERTY_NAME), objectId));
                }

                syncDatasourceConfiguration = inventoryObjectNode.getSingleRelationship(RelTypes.HAS_SYNC_CONFIGURATION, Direction.OUTGOING).getEndNode();
                if (syncDatasourceConfiguration == null) {
                    throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.sync.actions.get-sync-data-source-configuration.messages.malformed-ds-config"), objectId));
                }
            }

            tx.success();
            return PersistenceUtil.createSyncDataSourceConfigFromNode(syncDatasourceConfiguration);
        }
    }

    /**
     * Gets a data source configuration of the object
     *
     * @param objectId : the object id (a GenericCommunicationElement)
     * @param filters  : filter over SyncDataSourceConfiguration properties
     * @param offset   : skiped values over full response, -1 for full response
     * @param limit    : maximun values display per response, -1 for full response
     * @return A list of SyncDataSourceConfiguration
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database
     * @throws ApplicationObjectNotFoundException If the sync data source
     *                                            configuration could not be found
     * @throws UnsupportedPropertyException       if any property of the sync data
     *                                            source node is malformed or if there is an error with the relationship
     *                                            between the syncNode and it InventoryObjectNode
     */
    public List<SyncDataSourceConfiguration> getSyncDataSrcByBussinesObject(String objectId
            , HashMap<String, String> filters, int offset, int limit)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        List<SyncDataSourceConfiguration> elementsFound = new ArrayList<>();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            params.put(Constants.PROPERTY_ID, objectId);
            //GenericCommunicationElement -> SyncDatasourceConfiguration
            //cardinality (1..*)
            cypherQuery.append("MATCH (node:").append(inventoryObjectsLabel).append(")");
            cypherQuery.append("-[:").append(RelTypes.HAS_SYNC_CONFIGURATION).append("]");
            cypherQuery.append("->(dataSource:").append(syncDatasourceConfigLabel).append(")");
            cypherQuery.append(" WHERE node._uuid = $id ");
            if (filters != null)
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND UPPER(dataSource.").append(key).append(") CONTAINS UPPER('").append(filters.get(key)).append("')")
                );

            cypherQuery.append(" RETURN dataSource");
            if (offset >= 0 && limit >= 0) {
                params.put("offset", offset);
                params.put("limit", limit);
                cypherQuery.append(" SKIP $offset LIMIT $limit");
            }

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);
            ResourceIterator<Node> entityNodes = result.columnAs("dataSource");

            while (entityNodes.hasNext()) {
                Node node = entityNodes.next();
                SyncDataSourceConfiguration createSyncDataSourceConfigFromNode = PersistenceUtil.createSyncDataSourceConfigFromNode(node);
                if (createSyncDataSourceConfigFromNode != null)
                    elementsFound.add(createSyncDataSourceConfigFromNode);
            }
            tx.success();
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: save %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: in save [%s] %s", SynchronizationService.class, ex.getMessage()));
        }
        return elementsFound;
    }

    /**
     * Gets a data source configuration of the object
     *
     * @param objectId : the object id (a GenericCommunicationElement)
     * @param filters  : filter over SyncDataSourceConfiguration properties
     * @return A list of SyncDataSourceConfiguration
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database
     * @throws ApplicationObjectNotFoundException If the sync data source
     *                                            configuration could not be found
     * @throws UnsupportedPropertyException       if any property of the sync data
     *                                            source node is malformed or if there is an error with the relationship
     *                                            between the syncNode and it InventoryObjectNode
     */
    public int getSyncDataSrcByBussinesObjectCount(String objectId, HashMap<String, String> filters)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        Long count = 0L;
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            params.put(Constants.PROPERTY_ID, objectId);
            //GenericCommunicationElement -> SyncDatasourceConfiguration
            //cardinality (1..*)
            cypherQuery.append("MATCH (node:").append(inventoryObjectsLabel).append(")");
            cypherQuery.append("-[:").append(RelTypes.HAS_SYNC_CONFIGURATION).append("]");
            cypherQuery.append("->(dataSource:").append(syncDatasourceConfigLabel).append(")");
            cypherQuery.append(" WHERE node._uuid = $id ");
            if (filters != null)
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND UPPER(dataSource.").append(key).append(") CONTAINS UPPER('").append(filters.get(key)).append("')")
                );

            cypherQuery.append(" RETURN COUNT(dataSource) AS count");

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);
            while (result.hasNext())
                count = (Long) result.next().get("count");
            tx.success();
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: save %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: in save [%s] %s", SynchronizationService.class, ex.getMessage()));
        }
        return count.intValue();
    }

    /**
     * Gets a data source configuration of the object
     *
     * @param filters : filter over SyncDataSourceConfiguration properties
     * @param offset  : skiped values over full response, -1 for full response
     * @param limit   : maximun values display per response, -1 for full response
     * @return A list of SyncDataSourceConfiguration
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database
     * @throws ApplicationObjectNotFoundException If the sync data source
     *                                            configuration could not be found
     * @throws UnsupportedPropertyException       if any property of the sync data
     *                                            source node is malformed or if there is an error with the relationship
     *                                            between the syncNode and it InventoryObjectNode
     */
    public List<SyncDataSourceConfiguration> getSyncDataSrc(
            HashMap<String, String> filters, int offset, int limit)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        List<SyncDataSourceConfiguration> elementsFound = new ArrayList<>();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            //GenericCommunicationElement -> SyncDatasourceConfiguration
            //cardinality (1..*)
            cypherQuery.append("MATCH (node:").append(syncDatasourceConfigLabel).append(")");
            cypherQuery.append("-[:EXTENDS_FROM_TEMPLATE]->(:templateDataSource) ");
            cypherQuery.append(" WHERE TRUE ");
            if (filters != null)
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND UPPER(node.").append(key).append(") CONTAINS UPPER('").append(filters.get(key)).append("')")
                );

            cypherQuery.append(" RETURN node");
            if (offset >= 0 && limit >= 0) {
                params.put("offset", offset);
                params.put("limit", limit);
                cypherQuery.append(" SKIP $offset LIMIT $limit");
            }

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);
            ResourceIterator<Node> entityNodes = result.columnAs("node");

            while (entityNodes.hasNext()) {
                Node node = entityNodes.next();
                SyncDataSourceConfiguration createSyncDataSourceConfigFromNode = PersistenceUtil.createSyncDataSourceConfigFromNode(node);

                if (createSyncDataSourceConfigFromNode != null)
                    elementsFound.add(createSyncDataSourceConfigFromNode);
            }
            tx.success();
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: save %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: in save [%s] %s", SynchronizationService.class, ex.getMessage()));
        }
        return elementsFound;
    }

    /**
     * Gets a data source configuration of the object
     *
     * @param filters : filter over SyncDataSourceConfiguration properties
     * @return A list of SyncDataSourceConfiguration
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database
     * @throws ApplicationObjectNotFoundException If the sync data source
     *                                            configuration could not be found
     * @throws UnsupportedPropertyException       if any property of the sync data
     *                                            source node is malformed or if there is an error with the relationship
     *                                            between the syncNode an it InventoryObjectNode
     */
    public int getSyncDataSrcCount(HashMap<String, String> filters)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        Long count = 0L;
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            //GenericCommunicationElement -> SyncDatasourceConfiguration
            //cardinality (1..*)
            cypherQuery.append("MATCH (node:").append(syncDatasourceConfigLabel).append(")");
            cypherQuery.append("-[:EXTENDS_FROM_TEMPLATE]->(:templateDataSource) ");
            cypherQuery.append(" WHERE TRUE ");
            if (filters != null)
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND UPPER(node.").append(key).append(") CONTAINS UPPER('").append(filters.get(key)).append("')")
                );

            cypherQuery.append(" RETURN COUNT(node) AS count");

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);
            while (result.hasNext())
                count = (Long) result.next().get("count");
            tx.success();
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: save %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: in save [%s] %s", SynchronizationService.class, ex.getMessage()));
        }
        return count.intValue();
    }

    /**
     * Gets a data source configuration of the object
     *
     * @param filters : filter over SyncDataSourceConfiguration properties
     * @param offset  : skiped values over full response, -1 for full response
     * @param limit   : maximun values display per response, -1 for full response
     * @return A list of SyncDataSourceConfiguration
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database
     * @throws ApplicationObjectNotFoundException If the sync data source
     *                                            configuration could not be found
     * @throws UnsupportedPropertyException       if any property of the sync data
     *                                            source node is malformed or if there is an error with the relationship
     *                                            between the syncNode and it InventoryObjectNode
     */
    public List<SyncDataSourceCommonParameters> getSyncDataSourceCommonProperties(HashMap<String, String> filters, int offset, int limit)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        List<SyncDataSourceCommonParameters> elementsFound = new ArrayList<>();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            //GenericCommunicationElement -> SyncDatasourceConfiguration
            //cardinality (1..*)
            cypherQuery.append("MATCH (node:").append(syncDsCommonPropertiesLabel).append(")");
            cypherQuery.append(" WHERE TRUE");
            if (filters != null)
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND node.").append(key).append(" CONTAINS '").append(filters.get(key)).append("'")
                );

            cypherQuery.append(" RETURN node ");
            if (offset >= 0 && limit >= 0) {
                params.put("offset", offset);
                params.put("limit", limit);
                cypherQuery.append(" SKIP $offset LIMIT $limit");
            }

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);
            ResourceIterator<Node> entityNodes = result.columnAs("node");

            while (entityNodes.hasNext()) {
                Node node = entityNodes.next();
                SyncDataSourceCommonParameters createCommonParametersFromNode = PersistenceUtil.createSyncDSCommonPropertiesFromNode(node);
                if (createCommonParametersFromNode != null)
                    elementsFound.add(createCommonParametersFromNode);
            }
            tx.success();
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: save %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: in save [%s] %s", SynchronizationService.class, ex.getMessage()));
        }
        return elementsFound;
    }

    /**
     * Gets a data source configuration of the object
     *
     * @param filters : filter over SyncDataSourceConfiguration properties
     * @return A list of SyncDataSourceConfiguration
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database
     * @throws ApplicationObjectNotFoundException If the sync data source
     *                                            configuration could not be found
     * @throws UnsupportedPropertyException       if any property of the sync data
     *                                            source node is malformed or if there is an error with the relationship
     *                                            between the syncNode an it InventoryObjectNode
     */
    public int getSyncDataSourceCommonPropertiesCount(HashMap<String, String> filters)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        Long count = 0L;
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            //GenericCommunicationElement -> SyncDatasourceConfiguration
            //cardinality (1..*)
            cypherQuery.append("MATCH (node:").append(syncDsCommonPropertiesLabel).append(")");
            cypherQuery.append(" WHERE TRUE");
            if (filters != null)
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND node.").append(key).append(" CONTAINS '").append(filters.get(key)).append("'")
                );

            cypherQuery.append(" RETURN COUNT(node) AS count");

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);
            while (result.hasNext())
                count = (Long) result.next().get("count");
            tx.success();
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: save %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: in save [%s] %s", SynchronizationService.class, ex.getMessage()));
        }
        return count.intValue();
    }

    /**
     * Gets a synchronization data source configuration receiving its id as
     * search criteria.
     *
     * @param syncDatasourceId The sync data source configuration id.
     * @return A SyncDatasourceConfiguration instance.
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database
     * @throws ApplicationObjectNotFoundException if the syncDatasource could
     *                                            not be found
     * @throws UnsupportedPropertyException       if any property of the sync data
     *                                            source node is malformed or if there is an error with the relationship
     *                                            between the syncNode and it InventoryObjectNode
     */
    public SyncDataSourceConfiguration getSyncDataSourceConfigurationById(long syncDatasourceId)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {

            Node syncDatasourceConfiguration = connectionManager.getConnectionHandler().getNodeById(syncDatasourceId);
            if (syncDatasourceConfiguration == null) {
                throw new ApplicationObjectNotFoundException(String.format("The sync data source configuration with id: %s is not related with anything", syncDatasourceId));
            }

            tx.success();
            return PersistenceUtil.createSyncDataSourceConfigFromNode(syncDatasourceConfiguration);
        }
    }

    /**
     * Gets the data source configurations associated to a sync group. A data
     * source configuration is a set of parameters to access a sync data source
     *
     * @param syncGroupId The sync group the requested configurations belong to.
     * @param filters     : filter over SyncDataSourceConfiguration properties
     * @param offset      : skiped values over full response, -1 for full response
     * @param limit       : maximun values display per response, -1 for full response
     * @return A list of data source configurations.
     * @throws ApplicationObjectNotFoundException If the sync group could not be
     *                                            found.
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database.
     * @throws UnsupportedPropertyException       If the sync data source can not be
     *                                            mapped into a Java object.
     */
    public List<SyncDataSourceConfiguration> getSyncDataSrcBySyncGroupId(long syncGroupId,
                                                                         HashMap<String, String> filters, int offset, int limit)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        List<SyncDataSourceConfiguration> elementsFound = new ArrayList<>();

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            params.put(Constants.PROPERTY_ID, syncGroupId);
            //GenericCommunicationElement -> SyncDatasourceConfiguration
            //cardinality (1..*)
            cypherQuery.append("MATCH (group:").append(syncGroupsLabel).append(")");
            cypherQuery.append("<-[:").append(RelTypes.BELONGS_TO_SYNC_GROUP).append("]");
            cypherQuery.append("-(node:").append(syncDatasourceConfigLabel).append(")");
            cypherQuery.append("<-[:").append(RelTypes.HAS_SYNC_CONFIGURATION).append("]");
            cypherQuery.append("-(object:").append(inventoryObjectsLabel).append(") \n");
            cypherQuery.append(" WHERE ID(group) = $id");
            if (filters != null) {
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND UPPER(node.").append(key).append(") CONTAINS UPPER('").append(filters.get(key)).append("')")
                );
            }
            cypherQuery.append(" RETURN {datasrc:node, inventoryObject:object} AS result ");
            if (offset >= 0 && limit >= 0) {
                params.put("offset", offset);
                params.put("limit", limit);
                cypherQuery.append(" SKIP $offset LIMIT $limit");
            }

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);

            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                for (Entry<String, Object> entry_ : row.entrySet()) {
                    HashMap values = (HashMap) entry_.getValue();
                    Node inventoryObjectNode = (Node) values.get("inventoryObject");
                    Node datasrcNode = (Node) values.get("datasrc");
                    SyncDataSourceConfiguration dataSourceConfiguration;
                    dataSourceConfiguration = PersistenceUtil.createSyncDataSourceConfigFromNode(datasrcNode);
                    if (dataSourceConfiguration != null) {
                        BusinessObject businessObject = ogmService.createObjectFromNode(inventoryObjectNode);
                        dataSourceConfiguration.setBusinessObjectLight(businessObject);
                        elementsFound.add(dataSourceConfiguration);
                    }
                }
            }
            tx.success();
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: getting datasource from group %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: getting groups from datasource [%s] %s", SynchronizationService.class, ex.getMessage()));
        }

        return elementsFound;
    }

    /**
     * Gets the data source configurations associated to a sync group. A data
     * source configuration is a set of parameters to access a sync data source
     *
     * @param syncGroupId The sync group the requested configurations belong to.
     * @param filters     : filter over SyncDataSourceConfiguration properties
     * @return A list of data source configurations.
     * @throws ApplicationObjectNotFoundException If the sync group could not be
     *                                            found.
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database.
     * @throws UnsupportedPropertyException       If the sync data source can not be
     *                                            mapped into a Java object.
     */
    public int getSyncDataSrcBySyncGroupIdCount(long syncGroupId, HashMap<String, String> filters)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        Long count = 0L;

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            params.put(Constants.PROPERTY_ID, syncGroupId);
            //GenericCommunicationElement -> SyncDatasourceConfiguration
            //cardinality (1..*)
            cypherQuery.append("MATCH (group:").append(syncGroupsLabel).append(")");
            cypherQuery.append("<-[:").append(RelTypes.BELONGS_TO_SYNC_GROUP).append("]");
            cypherQuery.append("-(node:").append(syncDatasourceConfigLabel).append(")");
            cypherQuery.append("<-[:").append(RelTypes.HAS_SYNC_CONFIGURATION).append("]");
            cypherQuery.append("-(object:").append(inventoryObjectsLabel).append(") \n");
            cypherQuery.append(" WHERE ID(group) = $id");
            if (filters != null) {
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND UPPER(node.").append(key).append(") CONTAINS UPPER('").append(filters.get(key)).append("')")
                );
            }

            cypherQuery.append(" RETURN COUNT(node) AS count");

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);
            while (result.hasNext()) {
                count = (Long) result.next().get("count");
            }
            tx.success();
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: getting groups from datasource %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: getting groups from datasource [%s] %s", SynchronizationService.class, ex.getMessage()));
        }

        return count.intValue();
    }


    /**
     * Creates a synchronization group (LEGACY DEPRECATED)
     *
     * @param name The name of the new group
     * @return The id of the newly created group
     * @throws InvalidArgumentException           If any of the parameters is invalid
     * @throws ApplicationObjectNotFoundException If the sync provider could not
     *                                            be found
     */
    public long createSyncGroup(String name) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidArgumentException("The name of the sync group can not be empty");
        }

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncGroupNode = connectionManager.getConnectionHandler().createNode(syncGroupsLabel);
            syncGroupNode.setProperty(Constants.PROPERTY_NAME, name);

            tx.success();

            return syncGroupNode.getId();
        }
    }

    /**
     * Gets the data source configurations associated to a sync group. A data
     * source configuration is a set of parameters to access a sync data source
     * (LEGACY DEPRECATED)
     *
     * @param syncGroupId The sync group the requested configurations belong to.
     * @return A list of data source configurations.
     * @throws ApplicationObjectNotFoundException If the sync group could not be
     *                                            found.
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database.
     * @throws UnsupportedPropertyException       If the sync data source can not be
     *                                            mapped into a Java object.
     */
    public List<SyncDataSourceConfiguration> getSyncDataSourceConfigurations(long syncGroupId)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        List<SyncDataSourceConfiguration> syncDataSourcesConfigurations = new ArrayList<>();

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);

            if (syncGroupNode == null) {
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.sync.actions.get-sync-group.messages.sync-group-not-found"), syncGroupId));
            }

            for (Relationship rel : syncGroupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_SYNC_GROUP)) {
                syncDataSourcesConfigurations.add(PersistenceUtil.createSyncDataSourceConfigFromNode(rel.getStartNode()));
            }

            tx.success();
        }

        return syncDataSourcesConfigurations;
    }

    /**
     * Creates a synchronization group
     *
     * @param synchronizationGroup The name of the new group
     * @throws InvalidArgumentException           If any of the parameters is invalid
     * @throws ApplicationObjectNotFoundException If the sync provider could not
     *                                            be found
     */
    public void createSyncGroup(SynchronizationGroup synchronizationGroup)
            throws InvalidArgumentException, ApplicationObjectNotFoundException {

        if (synchronizationGroup.getName() == null || synchronizationGroup.getName().trim().isEmpty()) {
            throw new InvalidArgumentException("The name of the sync group can not be empty");
        }

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {

            Node syncGroupNode;
            if (synchronizationGroup.getId() <= 0) {
                syncGroupNode = connectionManager.getConnectionHandler().createNode(syncGroupsLabel);
                synchronizationGroup.setId(syncGroupNode.getId());
            } else {
                syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, synchronizationGroup.getId());
                if (syncGroupNode == null) {
                    throw new ApplicationObjectNotFoundException(
                            String.format("The sync group with id %s could not be find", synchronizationGroup.getId()));
                }
            }
            entityToNodeSyncGroup(synchronizationGroup, syncGroupNode);

            if (synchronizationGroup.getSyncDataSourceConfigurations() != null
                    && !synchronizationGroup.getSyncDataSourceConfigurations().isEmpty()) {
                for (Relationship relationship : syncGroupNode.getRelationships(RelTypes.BELONGS_TO_SYNC_GROUP, Direction.INCOMING)) {
                    relationship.delete();
                }

                for (SyncDataSourceConfiguration dataSrcEntity : synchronizationGroup.getSyncDataSourceConfigurations()) {
                    Node dataSrcNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncDatasourceConfigLabel, dataSrcEntity.getId());
                    if (dataSrcNode == null) {
                        throw new ApplicationObjectNotFoundException(
                                String.format("The sync data source with id %s could not be find", dataSrcEntity.getId()));
                    }
                    dataSrcNode.createRelationshipTo(syncGroupNode, RelTypes.BELONGS_TO_SYNC_GROUP);
                }
            }

            tx.success();
        }
    }

    /**
     * Updates the data source configurations associated to a given sync group
     *
     * @param syncGroupId         The Id of the sync group to be updated
     * @param syncGroupProperties The list of synchronization group properties
     * @throws ApplicationObjectNotFoundException If the sync group could not be
     *                                            found
     * @throws InvalidArgumentException           If any of the provided data source
     *                                            configurations is invalid
     */
    public void updateSyncGroup(long syncGroupId, List<StringPair> syncGroupProperties) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        if (syncGroupProperties == null) {
            throw new InvalidArgumentException(String.format("The parameters of the sync group with id %s can not be null", syncGroupId));
        }

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);

            if (syncGroupNode == null) {
                throw new ApplicationObjectNotFoundException(String.format("Synchronization Group with id %s could not be found", syncGroupId));
            }

            syncGroupProperties.forEach(syncGroupProperty -> syncGroupNode.setProperty(syncGroupProperty.getKey(), syncGroupProperty.getValue()));

            tx.success();
        }
    }

    /**
     * Deletes a sync group
     *
     * @param syncGroupId The id of the sync group
     * @throws ApplicationObjectNotFoundException If the sync group can no be
     *                                            found
     */
    public void deleteSynchronizationGroup(long syncGroupId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);

            if (syncGroupNode == null) {
                throw new ApplicationObjectNotFoundException(String.format("Can not find the Synchronization group with id %s", syncGroupId));
            }

            for (Relationship relationship : syncGroupNode.getRelationships()) {
                relationship.delete();
            }

            syncGroupNode.delete();
            tx.success();
        }
    }

    /**
     * Deletes a data source template
     *
     * @param templateDataSourceId The id of the template data source
     * @throws ApplicationObjectNotFoundException If the sync group can no be
     *                                            found
     */
    public void deleteTemplateDataSource(long templateDataSourceId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node templateDataSrcNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), templateDataSourceLabel, templateDataSourceId);

            if (templateDataSrcNode == null) {
                throw new ApplicationObjectNotFoundException(String.format("Can not find the Data Source Template with id %s", templateDataSourceId));
            }

            templateDataSrcNode.delete();
            tx.success();
        }
    }

    /**
     * Creates a data source configuration and associates it to a sync group
     *
     * @param objectId    the id of the object(GenericCommunicationsElement) the
     *                    data source configuration will belong to
     * @param syncGroupId The id of the sync group the data source configuration
     *                    will be related to
     * @param name        The name of the configuration
     * @param parameters  The list of parameters that will be part of the new
     *                    configuration. A sync data source configuration is a set of parameters
     *                    that allow the synchronization provider to access a sync data source
     * @return The id of the newly created data source
     * @throws ApplicationObjectNotFoundException If the object has no sync data
     *                                            source configuration group could not be found
     * @throws InvalidArgumentException           If any of the parameters is not valid
     */
    public long createSyncDataSourceConfig(String objectId, long syncGroupId, String name, List<StringPair> parameters)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidArgumentException("The sync configuration name can not be empty");
        }

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);
            if (syncGroupNode == null) {
                throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be found", syncGroupId));
            }

            Node objectNode = connectionManager.getConnectionHandler().findNode(inventoryObjectsLabel, Constants.PROPERTY_UUID, objectId);
            if (objectNode == null) {
                throw new ApplicationObjectNotFoundException(String.format("The object with id %s could not be found", objectId));
            }

            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.HAS_SYNC_CONFIGURATION)) {
                throw new InvalidArgumentException(String.format("The object id %s already has a sync datasource configuration", objectId));
            }

            Node syncDataSourceConfigNode = connectionManager.getConnectionHandler().createNode(syncDatasourceConfigLabel);
            syncDataSourceConfigNode.setProperty(Constants.PROPERTY_NAME, name);

            for (StringPair parameter : parameters) {
                if (!syncDataSourceConfigNode.hasProperty(parameter.getKey())) {
                    syncDataSourceConfigNode.setProperty(parameter.getKey(), parameter.getValue() == null ? "" : parameter.getValue());
                } else {
                    throw new InvalidArgumentException(String.format("Parameter %s in configuration %s is duplicated", name, parameter.getKey()));
                }
            }

            objectNode.createRelationshipTo(syncDataSourceConfigNode, RelTypes.HAS_SYNC_CONFIGURATION);
            syncDataSourceConfigNode.createRelationshipTo(syncGroupNode, RelTypes.BELONGS_TO_SYNC_GROUP);

            tx.success();
            return syncDataSourceConfigNode.getId();
        }
    }


    /**
     * Updates a synchronization data source
     *
     * @param syncDataSourceConfigId The id of an synchronization data source
     * @param parameters             the list of parameters to update
     * @throws ApplicationObjectNotFoundException If the sync data source cannot
     *                                            be found
     */
    public void updateSyncDataSourceConfig(long syncDataSourceConfigId, List<StringPair> parameters)
            throws ApplicationObjectNotFoundException {

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncDataSourceConfig = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncDatasourceConfigLabel, syncDataSourceConfigId);
            if (syncDataSourceConfig == null) {
                throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSourceConfigId));
            }

            parameters.forEach(parameter -> syncDataSourceConfig.setProperty(parameter.getKey(), parameter.getValue()));

            tx.success();
        }
    }

    /**
     * Deletes a synchronization data source
     *
     * @param syncDataSourceConfigId The id of an synchronization data source
     * @throws ApplicationObjectNotFoundException If the sync data source cannot
     *                                            be found
     */
    public void deleteSynchronizationDataSourceConfig(long syncDataSourceConfigId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncDataSourceConfigNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncDatasourceConfigLabel, syncDataSourceConfigId);
            if (syncDataSourceConfigNode == null) {
                throw new ApplicationObjectNotFoundException(String.format("Can not find the Synchronization Data Source Configuration with id %s", syncDataSourceConfigId));
            }


            for (Relationship relationship : syncDataSourceConfigNode.getRelationships(Direction.INCOMING, RelTypes.HAS_SYNC_CONFIGURATION)) {
                relationship.delete();
            }

            for (Relationship relationship : syncDataSourceConfigNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_SYNC_COMMON_PROPERTIES)) {
                relationship.delete();
            }

            for (Relationship relationship : syncDataSourceConfigNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_SYNC_GROUP)) {
                relationship.delete();
            }


            syncDataSourceConfigNode.delete();
            tx.success();
        }
    }

    /**
     * Creates "copy" a relation between a set of sync data source
     * configurations and a given sync group
     *
     * @param syncGroupId                    The Sync Group Id target
     * @param syncDataSourceConfigurationIds Set of sync data source
     *                                       configuration ids
     * @throws ApplicationObjectNotFoundException If the sync group cannot be
     *                                            found, or some sync data source configuration cannot be found
     * @throws InvalidArgumentException           If the sync group cannot be found, or
     *                                            some sync data source configuration cannot be found
     */
    public void copySyncDataSourceConfiguration(long syncGroupId, long[] syncDataSourceConfigurationIds) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {

            Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);
            if (syncGroupNode == null) {
                throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be find", syncGroupId));
            }

            for (long syncDataSrcId : syncDataSourceConfigurationIds) {
                Node syncDataSrcNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncDatasourceConfigLabel, syncDataSrcId);
                if (syncDataSrcNode == null) {
                    throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSrcId));
                }

                syncDataSrcNode.createRelationshipTo(syncGroupNode, RelTypes.BELONGS_TO_SYNC_GROUP);
            }
            tx.success();
        }
    }

    /**
     * Given a data source configuration associates it to a sync group
     *
     * @param syncGroupIds  Set of sync group ids that will be related to sync data source
     * @param syncDataSrcId The id of sync data source configuration.
     *                      A sync data source configuration is a set of parameterss
     *                      that allow the synchronization provider to access a sync data source
     * @throws ApplicationObjectNotFoundException If the object has no sync data
     *                                            source configuration group could not be found
     * @throws InvalidArgumentException           If any of the parameters is not valid
     */
    public void associateSyncDataSourceToGroup(long syncDataSrcId, Long[] syncGroupIds)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {

            Node syncDataSrcNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncDatasourceConfigLabel, syncDataSrcId);
            if (syncDataSrcNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSrcId));

            //delete old relations between syncgroup  and sync data source
            for (Relationship relationships : syncDataSrcNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_SYNC_GROUP))
                relationships.delete();

            //create new relations between syncgroup  and sync data source
            for (long syncGroupId : syncGroupIds) {
                Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);
                if (syncGroupNode == null)
                    throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be found", syncGroupId));
                syncDataSrcNode.createRelationshipTo(syncGroupNode, RelTypes.BELONGS_TO_SYNC_GROUP);
            }

            tx.success();
        }
    }

    /**
     * Release a set of sync data source configuration from a given sync group
     *
     * @param syncGroupId                    The Sync Group Id target
     * @param syncDataSourceConfigurationIds Set of sync data source
     *                                       configuration ids
     * @throws ApplicationObjectNotFoundException If the sync group cannot be
     *                                            found, or some sync data source configuration cannot be found
     * @throws InvalidArgumentException           If the sync group cannot be found, or
     *                                            some sync data source configuration cannot be found
     */
    public void releaseSyncDataSourceConfigFromSyncGroup(long syncGroupId, long[] syncDataSourceConfigurationIds)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            for (long syncDataSrcId : syncDataSourceConfigurationIds) {
                Node syncDataSrcNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncDatasourceConfigLabel, syncDataSrcId);
                if (syncDataSrcNode == null) {
                    throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSrcId));
                }

                List<Relationship> relsToDelete = new ArrayList<>();
                Iterable<Relationship> relationships = syncDataSrcNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_SYNC_GROUP);

                int i = 0;
                for (Relationship relationship : relationships) {
                    i++;
                    if (relationship.getEndNodeId() == syncGroupId) {
                        relsToDelete.add(relationship);
                    }
                }

                if (i == 1) {
                    throw new ApplicationObjectNotFoundException(String.format("datasource Config, id: %s can not be release, must belong at least to one SyncGroup", syncDataSrcId));
                }
                relsToDelete.forEach(rel -> rel.delete());
            }
            tx.success();
        }
    }


    /**
     * Release a set of sync data source configuration from a given sync group
     *
     * @param syncGroupIds                  The Sync Group Id target
     * @param syncDataSourceConfigurationId Set of sync data source
     *                                      configuration ids
     * @throws ApplicationObjectNotFoundException If the sync group cannot be
     *                                            found, or some sync data source configuration cannot be found
     * @throws InvalidArgumentException           If the sync group cannot be found, or
     *                                            some sync data source configuration cannot be found
     */
    public void releaseSyncDataSourceConfigFromSyncGroups(Long[] syncGroupIds, long syncDataSourceConfigurationId)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            for (long syncGroupId : syncGroupIds) {
                Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);
                if (syncGroupNode == null) {
                    throw new ApplicationObjectNotFoundException(String.format("Synchronization group with id %s could not be found", syncGroupId));
                }

                for (Relationship relationship : syncGroupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_SYNC_GROUP)) {
                    if (relationship.getStartNodeId() == syncDataSourceConfigurationId)
                        relationship.delete();
                }
            }
            tx.success();
        }
    }

    /**
     * Moves a set of sync data source configurations from a sync group to
     * another sync group
     *
     * @param newSyncGroupId                 The target sync group.
     * @param syncDataSourceConfigurationIds Set of sync data source
     *                                       configuration ids
     * @throws ApplicationObjectNotFoundException If the sync group cannot be
     *                                            found, or some sync data source configuration cannot be found
     * @throws InvalidArgumentException           If the sync group is malformed, or some
     *                                            sync data source configuration is malformed
     */
    public void moveSyncDataSourceConfiguration(long newSyncGroupId, long[] syncDataSourceConfigurationIds)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newSyncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, newSyncGroupId);
            if (newSyncGroupNode == null) {
                throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be find", newSyncGroupId));
            }

            for (long syncDataSrcId : syncDataSourceConfigurationIds) {
                Node syncDataSrcNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncDatasourceConfigLabel, syncDataSrcId);
                if (syncDataSrcNode == null) {
                    throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSrcId));
                }

                Iterable<Relationship> relationships = syncDataSrcNode.getRelationships(RelTypes.BELONGS_TO_SYNC_GROUP, Direction.OUTGOING);

                for (Relationship relationship : relationships) {
                    relationship.delete();
                }

                syncDataSrcNode.createRelationshipTo(newSyncGroupNode, RelTypes.BELONGS_TO_SYNC_GROUP);
            }
            tx.success();
        }
    }

    /**
     * @param syncDataSourceConfiguration data source to be saved
     * @throws ApplicationObjectNotFoundException If the sync group cannot be
     *                                            found, or some sync data source configuration cannot be found
     * @throws InvalidArgumentException           If the sync group is malformed, or some
     *                                            sync data source configuration is malformed
     */
    public void saveDataSource(SyncDataSourceConfiguration syncDataSourceConfiguration)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {

        if (syncDataSourceConfiguration.getBusinessObjectLight() == null)
            throw new InvalidArgumentException(
                    String.format("The sync data source with id %s not related to any business object",
                            syncDataSourceConfiguration.getId()));

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {

            Node businesObjectsNode = Util.findNodeByLabelAndUuid(connectionManager.getConnectionHandler(),
                    inventoryObjectsLabel, syncDataSourceConfiguration.getBusinessObjectLight().getId());
            if (businesObjectsNode == null)
                throw new ApplicationObjectNotFoundException(
                        String.format("The sync data source with id %s not related to any business object",
                                syncDataSourceConfiguration.getId()));

            Node dataSourceCommonNode = null;
            if (syncDataSourceConfiguration.getCommonParameters() != null
                    && syncDataSourceConfiguration.getCommonParameters().getId() <= 0) {
                dataSourceCommonNode = connectionManager.getConnectionHandler().createNode(syncDsCommonPropertiesLabel);
                syncDataSourceConfiguration.getCommonParameters().setId(dataSourceCommonNode.getId());
                entityToNodeCommon(syncDataSourceConfiguration.getCommonParameters(), dataSourceCommonNode);
            } else if (syncDataSourceConfiguration.getCommonParameters() != null) {
                dataSourceCommonNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(),
                        syncDsCommonPropertiesLabel, syncDataSourceConfiguration.getCommonParameters().getId());
                if (dataSourceCommonNode == null)
                    throw new ApplicationObjectNotFoundException(
                            String.format("The sync data source with id %s could not be find",
                                    syncDataSourceConfiguration.getCommonParameters().getId()));
                else
                    entityToNodeCommon(syncDataSourceConfiguration.getCommonParameters(), dataSourceCommonNode);
            }

            Node dataSrcConfigurationNode;
            if (syncDataSourceConfiguration.getId() <= 0) {
                dataSrcConfigurationNode = connectionManager.getConnectionHandler().createNode(syncDatasourceConfigLabel);
                syncDataSourceConfiguration.setId(dataSrcConfigurationNode.getId());
                entityToNodeSpecific(syncDataSourceConfiguration, dataSrcConfigurationNode);
            } else {
                dataSrcConfigurationNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(),
                        syncDatasourceConfigLabel, syncDataSourceConfiguration.getId());
                if (dataSrcConfigurationNode == null)
                    throw new ApplicationObjectNotFoundException(
                            String.format("The sync data source with id %s could not be find", syncDataSourceConfiguration.getId()));
                else
                    entityToNodeSpecific(syncDataSourceConfiguration, dataSrcConfigurationNode);
            }

            //create relation between data source and common properties
            for (Relationship relationship : dataSrcConfigurationNode.getRelationships(RelTypes.HAS_SYNC_COMMON_PROPERTIES, Direction.OUTGOING)) {
                relationship.delete();
            }
            if (dataSourceCommonNode != null)
                dataSrcConfigurationNode.createRelationshipTo(dataSourceCommonNode, RelTypes.HAS_SYNC_COMMON_PROPERTIES);

            //create relation between data source and inventory object
            for (Relationship relationship : dataSrcConfigurationNode.getRelationships(RelTypes.HAS_SYNC_CONFIGURATION, Direction.INCOMING)) {
                relationship.delete();
            }
            businesObjectsNode.createRelationshipTo(dataSrcConfigurationNode, RelTypes.HAS_SYNC_CONFIGURATION);

            //create relation between data source and template
            Node templateDataSrcNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(),
                    templateDataSourceLabel, syncDataSourceConfiguration.getTemplateDataSource().getId());
            if (templateDataSrcNode == null)
                throw new ApplicationObjectNotFoundException(
                        String.format("The sync data source with id %s not related to any template object",
                                syncDataSourceConfiguration.getId()));
            Relationship extendsFromTemplateRel = dataSrcConfigurationNode.getSingleRelationship(RelTypes.EXTENDS_FROM_TEMPLATE,
                    Direction.OUTGOING);
            if (extendsFromTemplateRel != null)
                extendsFromTemplateRel.delete();
            dataSrcConfigurationNode.createRelationshipTo(templateDataSrcNode, RelTypes.EXTENDS_FROM_TEMPLATE);

            tx.success();
        }
    }

    /**
     * @param templateDataSource template data source to be saved
     * @throws ApplicationObjectNotFoundException If the sync group cannot be
     *                                            found, or some sync data source configuration cannot be found
     * @throws InvalidArgumentException           If the sync group is malformed, or some
     *                                            sync data source configuration is malformed
     */
    public void saveTemplateDataSource(TemplateDataSource templateDataSource)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {

        if (templateDataSource.getName() == null)
            throw new InvalidArgumentException(
                    String.format("The template data source with id %s not have a name", templateDataSource.getId()));

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {

            Node templateDataSrcNode;
            if (templateDataSource.getId() <= 0) {
                templateDataSrcNode = connectionManager.getConnectionHandler().createNode(templateDataSourceLabel);
                templateDataSource.setId(templateDataSrcNode.getId());
                entityToNodeTemplateDatSrc(templateDataSource, templateDataSrcNode);
            } else {
                templateDataSrcNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), templateDataSourceLabel, templateDataSource.getId());
                if (templateDataSrcNode == null)
                    throw new ApplicationObjectNotFoundException(
                            String.format("The sync data source with id %s could not be find", templateDataSource.getId()));
                else
                    entityToNodeTemplateDatSrc(templateDataSource, templateDataSrcNode);
            }

            tx.success();
        }
    }

    /**
     * get all provider register in service
     *
     * @param nameFilter: provider name
     * @return list of sync providers
     * @throws ApplicationObjectNotFoundException If the sync data source configuration could not be found
     * @throws InvalidArgumentException           If any of the configurations is malformed in the database
     */
    public List<AbstractSyncProvider> getAllProviders(String nameFilter) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        List<AbstractSyncProvider> allProviders;
        allProviders = providerRegistry.getProviders().values().stream().filter(provider -> {
            if (nameFilter != null && !nameFilter.isEmpty()) {
                if (provider.getDisplayName() != null && !provider.getDisplayName().isEmpty())
                    return provider.getDisplayName().equalsIgnoreCase(nameFilter);
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toList());
        return allProviders;
    }

    /**
     * Gets a data source configuration of the object
     *
     * @param filters : filter over SyncDataSourceConfiguration properties
     * @param offset  : skiped values over full response, -1 for full response
     * @param limit   : maximun values display per response, -1 for full response
     * @return A list of SyncDataSourceConfiguration
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database
     * @throws ApplicationObjectNotFoundException If the sync data source
     *                                            configuration could not be found
     * @throws UnsupportedPropertyException       if any property of the sync data
     *                                            source node is malformed or if there is an error with the relationship
     *                                            between the syncNode and it InventoryObjectNode
     */
    public List<TemplateDataSource> getTemplateDataSrc(HashMap<String, String> filters
            , int offset, int limit)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        List<TemplateDataSource> elementsFound = new ArrayList<>();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            cypherQuery.append("MATCH (node:").append(templateDataSourceLabel).append(")");
            cypherQuery.append(" WHERE TRUE ");
            if (filters != null)
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND UPPER(dataSource.").append(key).append(") CONTAINS UPPER('").append(filters.get(key)).append("')")
                );

            cypherQuery.append(" RETURN node");
            if (offset >= 0 && limit >= 0) {
                params.put("offset", offset);
                params.put("limit", limit);
                cypherQuery.append(" SKIP $offset LIMIT $limit");
            }

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);
            ResourceIterator<Node> entityNodes = result.columnAs("node");

            while (entityNodes.hasNext()) {
                Node node = entityNodes.next();
                TemplateDataSource templateDataSrcNode = PersistenceUtil.createTemplateDataSourceFromNode(node);

                if (templateDataSrcNode != null)
                    elementsFound.add(templateDataSrcNode);
            }
            tx.success();
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: getTemplateDataSrc %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: in getTemplateDataSrc [%s] %s", SynchronizationService.class, ex.getMessage()));
        }
        return elementsFound;
    }

    /**
     * Gets a data source configuration of the object
     *
     * @param filters : filter over SyncDataSourceConfiguration properties
     * @return A list of SyncDataSourceConfiguration
     * @throws InvalidArgumentException           If any of the configurations is
     *                                            malformed in the database
     * @throws ApplicationObjectNotFoundException If the sync data source
     *                                            configuration could not be found
     * @throws UnsupportedPropertyException       if any property of the sync data
     *                                            source node is malformed or if there is an error with the relationship
     *                                            between the syncNode and it InventoryObjectNode
     */
    public int getTemplateDataSrcCount(HashMap<String, String> filters)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        Long count = 0L;
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder cypherQuery = new StringBuilder();
            Map<String, Object> params = new HashMap<>();

            cypherQuery.append("MATCH (node:").append(templateDataSourceLabel).append(")");
            cypherQuery.append(" WHERE TRUE ");
            if (filters != null)
                filters.keySet().forEach(key ->
                    cypherQuery.append(" AND UPPER(dataSource.").append(key).append(") CONTAINS UPPER('").append(filters.get(key)).append("')")
                );

            cypherQuery.append(" RETURN COUNT(node) AS count");

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery.toString(), params);
            while (result.hasNext())
                count = (Long) result.next().get("count");
            tx.success();
        } catch (Exception ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE,
                    String.format("ERROR: getTemplateDataSrcCount %s", ex.getMessage()));
            throw new UnsupportedPropertyException(String.format("ERROR: in getTemplateDataSrcCount [%s] %s", SynchronizationService.class, ex.getMessage()));
        }
        return count.intValue();
    }

    /**
     * Transform Sync Datasource Common Parameters entity into node
     *
     * @param entity Sync Datasource Common Parameters entity
     * @param node   Sync Datasource Common Parameters node
     */
    private void entityToNodeCommon(SyncDataSourceCommonParameters entity, Node node) {
        node.setProperty(Constants.PROPERTY_DATASOURCE_TYPE, entity.getDataSourcetype());
        //remove old parameters        
        if (entity.getParameters() != null)
            entity.getParameters().entrySet().stream()
                    .forEachOrdered(entry -> node.removeProperty(entry.getKey()));

        //add new parameters
        entity.getListOfParameters().forEach(item -> {
            if (item.getPropertyName() != null && !item.getPropertyName().trim().isEmpty()
                    && item.getPropertyValue() != null && !item.getPropertyValue().trim().isEmpty()) {
                node.setProperty(item.getPropertyName(), item.getPropertyValue());
            }

        });
    }

    /**
     * Transform Template Datasource entity into node
     *
     * @param entity Template Datasource entity
     * @param node   Template Datasource node
     */
    private void entityToNodeTemplateDatSrc(TemplateDataSource entity, Node node) {
        if (entity.getName() != null && !entity.getName().trim().isEmpty())
            node.setProperty(Constants.PROPERTY_NAME, entity.getName().toUpperCase());

        if (entity.getDescription() != null && !entity.getDescription().trim().isEmpty())
            node.setProperty(Constants.PROPERTY_DESCRIPTION, entity.getDescription());

        //remove old parameters        
        if (entity.getParameters() != null)
            entity.getParameters().entrySet().stream()
                    .forEachOrdered(entry -> node.removeProperty(entry.getKey()));

        //add new parameters
        entity.getListOfParameters().forEach(item -> {
            if (item.getPropertyName() != null && !item.getPropertyName().trim().isEmpty()) {
                node.setProperty(item.getPropertyName(), "");
            }
        });
    }

    /**
     * Transform Sync DataSource Configuration node into entity
     *
     * @param entity Sync DataSource Configuration entity
     * @param node   Sync DataSource Configuration node
     */
    private void entityToNodeSpecific(SyncDataSourceConfiguration entity, Node node) {
        if (entity.getName() != null && !entity.getName().trim().isEmpty())
            node.setProperty(Constants.PROPERTY_NAME, entity.getName());

        if (entity.getDescription() != null && !entity.getDescription().trim().isEmpty())
            node.setProperty(Constants.PROPERTY_DESCRIPTION, entity.getDescription());

        //remove old parameters        
        if (entity.getParameters() != null)
            entity.getParameters().entrySet().stream()
                    .forEachOrdered(entry -> node.removeProperty(entry.getKey()));

        //add new parameters
        entity.getListOfParameters().forEach(item -> {
            if (item.getPropertyName() != null && !item.getPropertyName().trim().isEmpty()
                    && item.getPropertyValue() != null && !item.getPropertyValue().trim().isEmpty()) {
                node.setProperty(item.getPropertyName(), item.getPropertyValue());
            }
        });
    }

    /**
     * Transform Synchronization Group entity into node
     *
     * @param entity Synchronization Group entity
     * @param node   Synchronization Group node
     */
    private void entityToNodeSyncGroup(SynchronizationGroup entity, Node node) {
        if (entity.getName() != null && !entity.getName().trim().isEmpty())
            node.setProperty(Constants.PROPERTY_NAME, entity.getName());

        if (entity.getDescription() != null && !entity.getDescription().trim().isEmpty())
            node.setProperty(Constants.PROPERTY_DESCRIPTION, entity.getDescription());
    }

}
