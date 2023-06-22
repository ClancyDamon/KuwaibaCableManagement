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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expregss or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.core.persistence.reference.neo4j;

import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.BusinessRule;
import org.neotropic.kuwaiba.core.apis.persistence.application.CompactQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ConfigurationVariable;
import org.neotropic.kuwaiba.core.apis.persistence.application.ExtendedQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.FavoritesFolder;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfileLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.ResultRecord;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskNotificationDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskScheduleDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfileLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ArtifactDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.application.queries.WarehouseManagerSearchQuery;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectList;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ArraySizeMismatchException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessRuleException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.GenericObjectList;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.QueryStatistics;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterators;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.Filter;
import org.neotropic.kuwaiba.core.apis.persistence.application.FilterDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryParameter;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueriesPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryResult;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ExecutionException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ScriptNotCompiledException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.extras.caching.CacheManager;
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.ProcessManagerService;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.ObjectGraphMappingService;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.Util;
import org.neotropic.kuwaiba.core.persistence.reference.naming.util.DynamicNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Application Entity Manager reference implementation
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class ApplicationEntityManagerImpl implements ApplicationEntityManager {
    /**
     * Configuration variables
     */
    private Properties configuration;
    /**
     * Default background path
     */
    private static final String DEFAULT_BACKGROUNDS_PATH = "/data/img/backgrounds"; //NOI18N  
       /**
     * Default attachment location.
     */
    private static final String DEFAULT_ATTACHMENTS_PATH = "/data/files/attachments";
    private static final String DEFAULT_MAX_ATTACHMENT_SIZE = "10";
    /**
     * Key prefix to error messages in the Application Entity Manager Service
     */
    private final String KEY_PREFIX = "api.aem.error";
    /**
     * Users index
     */
    private final Label userLabel;
    /**
     * Object label
     */
    private final Label inventoryObjectLabel;
    /**
     * Class label
     */
    private final Label classLabel;
    /**
     * Groups label
     */
    private final Label groupLabel;
    /**
     * Label for list type items (of all classes)
     */
    private final Label listTypeItemLabel;
    /**
     * Pools label 
     */
    private final Label poolLabel;
    /**
     * Device layouts label 
     */
    private final Label layoutLabel;
    /**
     * Templates label 
     */
    private final Label templateLabel;
    /**
     * Template elements label 
     */
    private final Label templateElementLabel;
    /**
     * Label for special nodes(like group root node)
     */
    private final Label specialNodeLabel;
    /**
     * Queries label 
     */
    private final Label queryLabel;
    /**
     * Task label
     */
    private final Label taskLabel;
    /**
     * Label for business rules
     */
    private final Label businessRulesLabel;
    /**
     * Label for general views (those not related to a particular object)
     */
    private final Label generalViewsLabel;
    /**
     * The label that contains the configuration variables pools
     */
    private final Label configurationVariablesPools;
    /**
     * The label that contains the configuration variables
     */
    private final Label configurationVariables;
    /**
     * Label to tag the nodes corresponding to validators. Validators are pieces of logic that validate is a given object matches certain condition, 
     * for example, if the state of an object matches particular value, or if an object has a relationship with another. Validators are attached to 
     * BusinessObjectLight instances and are used mostly to render the objects in a certain way depending on the value of the validator. For example,
     * a client would like to show a green icon besides the name of an object whose state is "operational"
     */
    private final Label validatorDefinitions;
    /**
     * Label to tag the node filter
     */
    private final Label filterDefinitionsLabel;
    /**
     * Label for scripted queries pools.
     */
    private final Label scriptedQueriesPoolsLabel;
    /**
     * Label for scripted queries.
     */
    private final Label scriptedQueriesLabel;
    /**
     * Label for scripted queries parameters.
     */
    private final Label scriptedQueriesParametersLabel;
    /**
     * Label for process instances.
     */
    private final Label processInstanceLabel;
    /**
     * Label for privilege.
     */
    private final Label privilegeLabel;
    /**
     * Label for object related view.
     */
    private final Label objectRelatedViewLabel;
    /**
     * A class loader to place all the filter definition classes created on-the-fly.
     */
    private final GroovyClassLoader filterDefinitionsClassLoader;
    /**
     * The default password encoder to authenticate users. It is also used to set new passwords. It uses BCrypt algorithm and a strength of 60.
     */
    private final PasswordEncoder passwordEnconder;
    /**
     * Reference to the singleton instance of CacheManager
     */
    private CacheManager cm;
    /**
     * Reference to the metadata entity manager
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the metadata entity manager
     */
    @Autowired
    private BusinessEntityManagerImpl bem;
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
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the process manager service.
     */
    @Autowired
    private ProcessManagerService processManagerService;
    
    /**
     * Map with the current sessions. The key is the session token, the value is the list of session object. Note that a single user might have multiple
     * session opened as long as they are of different type.
     */
    private final ConcurrentHashMap<String, Session> sessions;
    
    public ApplicationEntityManagerImpl() {
        this.configuration = new Properties();
        this.sessions = new ConcurrentHashMap<>();
        this.passwordEnconder = new BCryptPasswordEncoder();
        
        // Initilize labels
        this.userLabel = Label.label(Constants.LABEL_USER);
        this.inventoryObjectLabel = Label.label(Constants.LABEL_INVENTORY_OBJECTS);
        this.classLabel = Label.label(Constants.LABEL_CLASS);
        this.groupLabel = Label.label(Constants.LABEL_GROUP);
        this.listTypeItemLabel = Label.label(Constants.LABEL_LIST_TYPE_ITEMS);
        this.poolLabel = Label.label(Constants.LABEL_POOLS);
        this.layoutLabel = Label.label(Constants.LABEL_LAYOUTS);
        this.templateLabel = Label.label(Constants.LABEL_TEMPLATES);
        this.templateElementLabel = Label.label(Constants.LABEL_TEMPLATE_ELEMENTS);
        this.specialNodeLabel = Label.label(Constants.LABEL_SPECIAL_NODE);
        this.queryLabel = Label.label(Constants.LABEL_QUERIES);
        this.taskLabel = Label.label(Constants.LABEL_TASKS);
        this.businessRulesLabel = Label.label(Constants.LABEL_BUSINESS_RULES);
        this.generalViewsLabel = Label.label(Constants.LABEL_GENERAL_VIEWS);
        this.configurationVariablesPools = Label.label(Constants.LABEL_CONFIG_VARIABLES_POOLS);
        this.configurationVariables = Label.label(Constants.LABEL_CONFIG_VARIABLES);
        this.validatorDefinitions = Label.label(Constants.LABEL_VALIDATOR_DEFINITIONS);
        this.filterDefinitionsLabel = Label.label(Constants.LABEL_FILTER_DEFINITIONS);
        this.scriptedQueriesPoolsLabel = Label.label(Constants.LABEL_SCRIPTED_QUERIES_POOLS);
        this.scriptedQueriesLabel = Label.label(Constants.LABEL_SCRIPTED_QUERIES);
        this.scriptedQueriesParametersLabel = Label.label(Constants.LABEL_SCRIPTED_QUERIES_PARAMETERS);
        this.processInstanceLabel = Label.label(Constants.LABEL_PROCESS_INSTANCE);
        this.privilegeLabel = Label.label(Constants.LABEL_PRIVILEGES);
        this.objectRelatedViewLabel = Label.label(Constants.LABEL_OBJECT_RELATED_VIEWS);
        
        this.filterDefinitionsClassLoader = new GroovyClassLoader();
    }

    @Override
    public void initCache() {
        this.cm = CacheManager.getInstance();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            ResourceIterator<Node> listTypeItems = connectionManager.getConnectionHandler().findNodes(listTypeItemLabel);
            while (listTypeItems.hasNext()) {
                Node listTypeNode = listTypeItems.next();
                GenericObjectList aListType = Util.createGenericObjectListFromNode(listTypeNode);
                cm.putListType(aListType);
            }
        } catch(Exception ex) {
            Logger.getLogger(ApplicationEntityManagerImpl.class.getName()).log(Level.SEVERE, String.format("[KUWAIBA] [%s] An error was found while creating the AEM instance: %s", 
                    Calendar.getInstance().getTime(), ex.getMessage()));
        }
    }
    
    @Override
    public ConcurrentHashMap<String, Session> getSessions() {
        return sessions;
    }
    
    @Override
    public boolean isSessionValid(String username, String token) {
        return this.sessions.values().stream().filter(aSession -> aSession.getToken().equals(token) && aSession.getUser().getUserName().equals(username)).
                findAny().isPresent();
    }

    @Override
    public void validateCall(String methodName, String username) {
        // Do nothing for now
    }

    @Override
    public long createUser(String userName, String password, String firstName,
            String lastName, boolean enabled, int type, String email, List<Privilege> privileges, long defaultGroupId)
            throws InvalidArgumentException {
        if (userName == null)
            throw new InvalidArgumentException("User name can not be null");
        
        if (userName.trim().isEmpty())
            throw new InvalidArgumentException("User name can not be an empty string");
        
        if (!userName.matches("^[a-zA-Z0-9_.]*$"))
            throw new InvalidArgumentException(String.format("User name %s contains invalid characters", userName));
        
        if (password == null)
            throw new InvalidArgumentException("Password can not be null");
       
        if (password.trim().isEmpty())
            throw new InvalidArgumentException("Password can not be an empty string");
        
        if (type != UserProfile.USER_TYPE_GUI && type != UserProfile.USER_TYPE_WEB_SERVICE && 
                type != UserProfile.USER_TYPE_SOUTHBOUND && type != UserProfile.USER_TYPE_SYSTEM)
            throw new InvalidArgumentException("Invalid user type");
            
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node storedUser = connectionManager.getConnectionHandler().findNode(userLabel, Constants.PROPERTY_NAME, userName);

            if (storedUser != null)
                throw new InvalidArgumentException(String.format("User name %s already exists", userName));
            
            Node newUserNode = connectionManager.getConnectionHandler().createNode(userLabel);

            newUserNode.setProperty(UserProfile.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            newUserNode.setProperty(UserProfile.PROPERTY_NAME, userName);
            newUserNode.setProperty(UserProfile.PROPERTY_PASSWORD, passwordEnconder.encode(password));
            newUserNode.setProperty(UserProfile.PROPERTY_FIRST_NAME, firstName == null ? "" : firstName);
            newUserNode.setProperty(UserProfile.PROPERTY_LAST_NAME, lastName == null ? "" : lastName);
            newUserNode.setProperty(UserProfile.PROPERTY_TYPE, type);
            newUserNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
            newUserNode.setProperty(UserProfile.PROPERTY_EMAIL, email);
            
            Node defaultGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), groupLabel, defaultGroupId);
            
            if (defaultGroupNode != null)
                newUserNode.createRelationshipTo(defaultGroupNode, RelTypes.BELONGS_TO_GROUP);
            else {
                tx.failure();
                throw new InvalidArgumentException(String.format("Group with id %s can not be found", defaultGroupId));
            }

            if (privileges != null) {
                privileges.stream().map(privilege -> {
                    Node privilegeNode = connectionManager.getConnectionHandler().createNode();
                    privilegeNode.setProperty(Privilege.PROPERTY_FEATURE_TOKEN, privilege.getFeatureToken());
                    privilegeNode.setProperty(Privilege.PROPERTY_ACCESS_LEVEL, privilege.getAccessLevel());
                    return privilegeNode;
                }).forEachOrdered(privilegeNode -> {
                    newUserNode.createRelationshipTo(privilegeNode, RelTypes.HAS_PRIVILEGE);
                });
            }
            tx.success();
            
            cm.putUser(Util.createUserProfileWithGroupPrivilegesFromNode(newUserNode));
            return newUserNode.getId();
        }
    }

    @Override
    public void setUserProperties(long oid, String userName, String password, String firstName,
            String lastName, int enabled, int type, String email)
            throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, oid);

            if(userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a user with id %s", oid));
            
            // Note that once a system user is created, it can only be deleted or modified by accessing directly to the database 
            if (userNode.hasProperty(UserProfile.PROPERTY_TYPE) && (int)userNode.getProperty(UserProfile.PROPERTY_TYPE) == UserProfile.USER_TYPE_SYSTEM)
                throw new InvalidArgumentException("System users can not be deleted or modified");

            if(password != null) {
                if (password.trim().isEmpty())
                    throw new InvalidArgumentException("Password can't be an empty string");
                
                userNode.setProperty(Constants.PROPERTY_PASSWORD, passwordEnconder.encode(password));
            }
            
            if (firstName != null)
                userNode.setProperty(Constants.PROPERTY_FIRST_NAME, firstName);
            
            if (lastName != null)
                userNode.setProperty(Constants.PROPERTY_LAST_NAME, lastName);
            
            if (type != -1 && type != UserProfile.USER_TYPE_GUI && type != UserProfile.USER_TYPE_WEB_SERVICE && 
                    type != UserProfile.USER_TYPE_SOUTHBOUND && type != UserProfile.USER_TYPE_SYSTEM)
                throw new InvalidArgumentException("Invalid user type");
            
            if (type != -1)
                userNode.setProperty(Constants.PROPERTY_TYPE, type);
            
            if (enabled != -1 && enabled != 0 && enabled != 1)
                throw new InvalidArgumentException("User enabled state is not valid");
            
            if (enabled != -1)
                userNode.setProperty(Constants.PROPERTY_ENABLED, enabled == 1);
            
            if (email != null)
                userNode.setProperty(UserProfile.PROPERTY_EMAIL, email);
            
            if(userName != null) {
                if (userName.trim().isEmpty())
                    throw new InvalidArgumentException("User name can not be an empty string");

                if (!userName.matches("^[a-zA-Z0-9_.]*$"))
                    throw new InvalidArgumentException(String.format("The user name %s contains invalid characters", userName));

                if (UserProfile.DEFAULT_ADMIN.equals(userNode.getProperty(UserProfile.PROPERTY_NAME)))
                    throw new InvalidArgumentException("The default administrator user name can not be changed");
                
                Node aUser = connectionManager.getConnectionHandler().findNode(userLabel, Constants.PROPERTY_NAME, userName);
                if (aUser != null)
                    throw new InvalidArgumentException(String.format("User name %s already exists", userName));
                
                //Refresh the user index and update the user name
                userNode.setProperty(Constants.PROPERTY_NAME, userName);
                cm.removeUser(userName);
            }
            UserProfile userProfile = Util.createUserProfileWithGroupPrivilegesFromNode(userNode);
            sessions.values().stream().filter(session -> (session.getUser().getId() == userProfile.getId())).forEachOrdered(session -> {
                session.setUser(userProfile);
            });
            tx.success();
            cm.putUser(userProfile);
        }
    }

    @Override
    public void setUserProperties(String formerUsername, String newUserName, String password, String firstName,
            String lastName, int enabled, int type, String email)
            throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) { 
            Node userNode = connectionManager.getConnectionHandler().findNode(userLabel, Constants.PROPERTY_NAME, formerUsername);

            if(userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a user with name %s", formerUsername));

            // Note that once a system user is created, it can only be deleted or modified by accessing directly to the database 
            if (userNode.hasProperty(UserProfile.PROPERTY_TYPE) && (int)userNode.getProperty(UserProfile.PROPERTY_TYPE) == UserProfile.USER_TYPE_SYSTEM)
                throw new InvalidArgumentException("System users can not be deleted or modified");
            
            if(newUserName != null) {
                if (newUserName.trim().isEmpty())
                    throw new InvalidArgumentException("User name can not be an empty string");

                if (UserProfile.DEFAULT_ADMIN.equals(formerUsername))
                    throw new InvalidArgumentException("The default administrator user name can not be changed");
                
                Node storedUser =  connectionManager.getConnectionHandler().findNode(userLabel, Constants.PROPERTY_NAME, newUserName);
                if (storedUser != null)
                    throw new InvalidArgumentException(String.format("User name %s already exists", newUserName));

                if (!newUserName.matches("^[a-zA-Z0-9_.]*$"))
                    throw new InvalidArgumentException(String.format("The user name %s contains invalid characters", newUserName));
            }
            if(password != null) {
                if (password.trim().isEmpty())
                    throw new InvalidArgumentException("Password can't be an empty string");
            }
        
            if (newUserName != null) {
                //refresh the userindex
                userNode.setProperty(Constants.PROPERTY_NAME, newUserName);
                
                cm.removeUser(newUserName);
            }
            if (password != null)
                userNode.setProperty(Constants.PROPERTY_PASSWORD, passwordEnconder.encode(password));
            if(firstName != null)
                userNode.setProperty(Constants.PROPERTY_FIRST_NAME, firstName);
            if(lastName != null)
                userNode.setProperty(Constants.PROPERTY_LAST_NAME, lastName);
            if (type != -1 && type != UserProfile.USER_TYPE_GUI && type != UserProfile.USER_TYPE_WEB_SERVICE && 
                    type != UserProfile.USER_TYPE_SOUTHBOUND && type != UserProfile.USER_TYPE_SYSTEM)
                throw new InvalidArgumentException("Invalid user type");
            if (type != -1)
                userNode.setProperty(Constants.PROPERTY_TYPE, type );
            if (enabled != -1 && enabled != 0 && enabled != 1)
                throw new InvalidArgumentException("User enabled state is not valid");
            if (enabled != -1)
                userNode.setProperty(Constants.PROPERTY_ENABLED, enabled == 1 );
            if (email != null)
                userNode.setProperty(UserProfile.PROPERTY_EMAIL, email);
            
            UserProfile userProfile = Util.createUserProfileWithGroupPrivilegesFromNode(userNode);
            sessions.values().stream().filter(session -> (session.getUser().getId() == userProfile.getId())).forEachOrdered(session -> {
                session.setUser(userProfile);
            });
            tx.success();
            cm.putUser(userProfile);
        }
    }

    @Override
    public void addUserToGroup(long userId, long groupId) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node groupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), groupLabel, groupId);
            if (groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Group with id %s could not be found", groupId));
            
            for (Relationship belongsToGroupRelationship : groupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP)) {
                if (belongsToGroupRelationship.getStartNode().getId() == userId)
                    throw new InvalidArgumentException(String.format("The user with id %s already belongs to group with id %s", userId, groupId));
            }            
            Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, userId);
            
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("User with id %s could not be found", userId));
            
            userNode.createRelationshipTo(groupNode, RelTypes.BELONGS_TO_GROUP);
            
            tx.success();
        }
    }

    @Override
    public void removeUserFromGroup(long userId, long groupId) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node groupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), groupLabel, groupId);
            
            if (groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Group with id %s could not be found", groupId));
            
            for (Relationship belongsToGroupRelationship : groupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP)) {
                Node userNode = belongsToGroupRelationship.getStartNode();
                if (userNode.getId() == userId) {
                    belongsToGroupRelationship.delete();
                    if (!userNode.hasRelationship(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP))
                        throw new InvalidArgumentException("No orphan users are allowed. Put it in another group before removing it from this one");
                    
                    tx.success();
                    return;
                }
            }
            throw new ApplicationObjectNotFoundException(String.format("User with id %s is not related to group with id %s", userId, groupId));
        }
    }

    @Override
    public void setPrivilegeToUser(long userId, String featureToken, int accessLevel) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        //TODO: This method should check the new privilege against a list of predefined feature tokens to avoid setting/adding bogus items
        if (accessLevel != Privilege.ACCESS_LEVEL_READ && accessLevel != Privilege.ACCESS_LEVEL_READ_WRITE)
            throw new InvalidArgumentException(String.format("The access level privided is not valid: %s", accessLevel));
            
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, userId);
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("User with id %s could not be found", userId));

            Node privilegeNode = null;
            for (Relationship hasPrivilegeRelationship : userNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE)) {
                if(featureToken.equals(hasPrivilegeRelationship.getEndNode().getProperty(Privilege.PROPERTY_FEATURE_TOKEN)))
                    privilegeNode = hasPrivilegeRelationship.getEndNode();
            }
        
            if (privilegeNode == null) {
                privilegeNode = connectionManager.getConnectionHandler().createNode(privilegeLabel);
                userNode.createRelationshipTo(privilegeNode, RelTypes.HAS_PRIVILEGE);
                privilegeNode.setProperty(Privilege.PROPERTY_FEATURE_TOKEN, featureToken);
            }
            
            privilegeNode.setProperty(Privilege.PROPERTY_ACCESS_LEVEL, accessLevel);
            tx.success();
        }
    }

    @Override
    public void setPrivilegeToGroup(long groupId, String featureToken, int accessLevel) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        //TODO: This method should check the new privilege against a list of predefined feature tokens to avoid adding bogus items
        if (accessLevel != Privilege.ACCESS_LEVEL_READ && accessLevel != Privilege.ACCESS_LEVEL_READ_WRITE)
            throw new InvalidArgumentException(String.format("The provided access level is not valid: %s", accessLevel));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node groupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), groupLabel, groupId);
            if (groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Group with id %s could not be found", groupId));

            Node privilegeNode = null;
            for (Relationship hasPrivilegeRelationship : groupNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE)) {
                if(featureToken.equals(hasPrivilegeRelationship.getEndNode().getProperty(Privilege.PROPERTY_FEATURE_TOKEN)))
                    privilegeNode = hasPrivilegeRelationship.getEndNode();
            }
        
            if (privilegeNode == null) {
                privilegeNode = connectionManager.getConnectionHandler().createNode(privilegeLabel);
                groupNode.createRelationshipTo(privilegeNode, RelTypes.HAS_PRIVILEGE);
                privilegeNode.setProperty(Privilege.PROPERTY_FEATURE_TOKEN, featureToken);
            }
            
            privilegeNode.setProperty(Privilege.PROPERTY_ACCESS_LEVEL, accessLevel);
            tx.success();
        }
    }

    @Override
    public void removePrivilegeFromUser(long userId, String featureToken) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, userId);
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("User with id %s could not be found", userId));
        
            for (Relationship hasPrivilegeRelationship : userNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE)) {
                if (featureToken.equals(hasPrivilegeRelationship.getEndNode().getProperty(Privilege.PROPERTY_FEATURE_TOKEN))) {
                    hasPrivilegeRelationship.delete();
                    hasPrivilegeRelationship.getEndNode().delete();
                    tx.success();
                    return; 
                }
            }
            tx.failure();
            throw new InvalidArgumentException(String.format("The user with id %s already does not have the privilege %s", userId, featureToken));
        }
    }

    @Override
    public void removePrivilegeFromGroup(long groupId, String featureToken) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node groupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), groupLabel, groupId);
            if (groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Group with id %s could not be found", groupId));
        
            for (Relationship hasPrivilegeRelationship : groupNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE)) {
                if (featureToken.equals(hasPrivilegeRelationship.getEndNode().getProperty(Privilege.PROPERTY_FEATURE_TOKEN))) {
                    hasPrivilegeRelationship.delete();
                    hasPrivilegeRelationship.getEndNode().delete();
                    tx.success();
                    return; 
                }
            }
            tx.failure();
            throw new InvalidArgumentException(String.format("The group with id %s already does not have the privilege %s", groupId, featureToken));
        }
    }
    
    @Override
    public long createGroup(String groupName, String description, List<Long> users) throws InvalidArgumentException, 
                    ApplicationObjectNotFoundException {
        if (groupName == null)
            throw new InvalidArgumentException("Group name can not be null");
        if (groupName.trim().isEmpty())
            throw new InvalidArgumentException("Group name can not be an empty string");
        if (!groupName.matches("^[a-zA-Z0-9_. ]*$"))
            throw new InvalidArgumentException(String.format("Group \"%s\" contains invalid characters", groupName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node storedGroup = connectionManager.getConnectionHandler().findNode(groupLabel, Constants.PROPERTY_NAME, groupName);
            if (storedGroup != null)
                throw new InvalidArgumentException(String.format("Group \"%s\" already exists", groupName));

            Node newGroupNode = connectionManager.getConnectionHandler().createNode(groupLabel);

            newGroupNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            newGroupNode.setProperty(Constants.PROPERTY_NAME, groupName);
            newGroupNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);

            if (users != null) {
                for (long userId : users) {
                    Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, userId);
                    if(userNode != null)
                        userNode.createRelationshipTo(newGroupNode, RelTypes.BELONGS_TO_GROUP);
                    else {
                        tx.failure();
                        throw new ApplicationObjectNotFoundException(String.format("User with id %s can not be found. Group creation aborted.", userId));
                    }
                }
            }
            tx.success();
            cm.putGroup(Util.createGroupProfileFromNode(newGroupNode));
            
            return newGroupNode.getId();
        }
    }

    @Override
    public List<UserProfile> getUsers() {
        List<UserProfile> usersProfile = new ArrayList<>();
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            try (ResourceIterator<Node> users = connectionManager.getConnectionHandler().findNodes(userLabel)) {
                while (users.hasNext())
                    usersProfile.add(Util.createUserProfileWithGroupPrivilegesFromNode(users.next()));
                return usersProfile;
            }            
        }
    }

    @Override
    public List<GroupProfile> getGroups() {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            ResourceIterator<Node> groupNodes = connectionManager.getConnectionHandler().findNodes(groupLabel);
            List<GroupProfile> groups =  new ArrayList<>();

            while (groupNodes.hasNext())
                groups.add((Util.createGroupProfileFromNode(groupNodes.next())));
            return groups;
        }
    }

    @Override
    public void setGroupProperties(long id, String groupName, String description)
            throws InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
                Node groupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), groupLabel, id);
            if(groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find the group with id %s",id));
            
            if(groupName != null) {
                if (groupName.trim().isEmpty())
                    throw new InvalidArgumentException("Group name can not be an empty string");
                if (!groupName.matches("^[a-zA-Z0-9_. ]*$"))
                    throw new InvalidArgumentException(String.format("Group %s contains invalid characters", groupName));

                Node storedGroup = connectionManager.getConnectionHandler().findNode(groupLabel, Constants.PROPERTY_NAME, groupName);
                    if (storedGroup != null)
                        throw new InvalidArgumentException(String.format("The group name %s is already in use", groupName));
                cm.removeGroup((String)groupNode.getProperty(Constants.PROPERTY_NAME));
                groupNode.setProperty(Constants.PROPERTY_NAME, groupName);
            }
            if(description != null)
                groupNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            
            cm.putGroup(Util.createGroupProfileFromNode(groupNode));
            tx.success();
        }
    }

    @Override
    public void deleteUsers(List<Long> oids) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            //TODO watch if there are relationships you can/should not delete
            if(oids != null) {
                for (long id : oids) {
                    Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, id);
                    Util.deleteUserNode(userNode);
                }
            }
            
            tx.success();
        }
    }

    @Override
    public void deleteGroups(List<Long> oids) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if(oids != null) {
                
                for (long id : oids) {
                    Node groupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), groupLabel, id);
                    if(groupNode == null)
                        throw new ApplicationObjectNotFoundException(String.format("Can not find a group with id %s", id));
                    
                    
                    for (Relationship relationship : groupNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE)) {
                        Node privilegeNode = relationship.getEndNode();
                        relationship.delete();
                        privilegeNode.delete();
                    }
                    
                    for (Relationship relationship : groupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP)) {
                        Node userNode = relationship.getStartNode();
                                                
                        relationship.delete();
                        
                        // This will delete all users associated *only* to this group. The users associated to other groups will be kept and the relationship 
                        // with this group will be released. The user "admin" can not be deleted
                        if (!userNode.hasRelationship(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP)) 
                            Util.deleteUserNode(userNode);
                    }
                    
                    // Now we release the rest of the relationships, if any
                    for (Relationship otherRelationship : groupNode.getRelationships())
                        otherRelationship.delete();
                    
                    cm.removeGroup((String)groupNode.getProperty(GroupProfile.PROPERTY_NAME));
                    groupNode.delete();
                }
            }
            tx.success();
        }
    }
    
   //List type related methods
    @Override
   public String createListTypeItem(String className, String name, String displayName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException {               
        if (name == null || className == null) {
            InvalidArgumentException ex = new InvalidArgumentException(ts.getTranslatedString(KEY_PREFIX + ".1"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(1);
            throw ex;
        }
       
        ClassMetadata myClass= cm.getClass(className);
       try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode ==  null) {
                MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".2"), className));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(2);
                ex.setMessageArgs(className);
                throw ex;
            }

            if (myClass == null) {
                 myClass = Util.createClassMetadataFromNode(classNode);
                 cm.putClass(myClass);
             }      

            if (!mem.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, className)) {
                InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".3"), className));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(3);
                ex.setMessageArgs(className);
                 throw ex;
            }
            if (myClass.isInDesign()) {
                OperationNotPermittedException ex = new OperationNotPermittedException(ts.getTranslatedString(KEY_PREFIX + ".4"));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(4);
                 throw ex;
            }
            if (myClass.isAbstract()) {
                OperationNotPermittedException ex = new OperationNotPermittedException(ts.getTranslatedString(KEY_PREFIX + ".5"));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(5);
                 throw ex;
            }
       
           Node newItem = connectionManager.getConnectionHandler().createNode(listTypeItemLabel);
           String uuid = UUID.randomUUID().toString();
           newItem.setProperty(Constants.PROPERTY_UUID, uuid);
                      
           newItem.setProperty(Constants.PROPERTY_NAME, name);
           if (displayName != null)
               newItem.setProperty(Constants.PROPERTY_DISPLAY_NAME, displayName);
           newItem.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);

           tx.success();
           GenericObjectList newListType = new GenericObjectList(uuid, name);
           cm.putListType(newListType);
           return uuid;
        }
    }
   
    @Override
    public ChangeDescriptor updateListTypeItem(String className, String oid, HashMap<String, String> attributes)
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        ClassMetadata classMetadata = mem.getClass(className);
        if (classMetadata == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, className))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.messages.wrong-subclass"), 
                    className, Constants.CLASS_GENERICOBJECTLIST));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            ChangeDescriptor changes = ogmService.updateObject(oid, classMetadata, attributes);
            tx.success();
            
            return changes;
        }
    }

    @Override
    public void deleteListTypeItem(String className, String oid, boolean realeaseRelationships) 
            throws MetadataObjectNotFoundException, OperationNotPermittedException, BusinessObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx())
        {
            ClassMetadata classMetadata = Util.createClassMetadataFromNode(connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className));
            
            if (!mem.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, className)) {
                InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".6"), className));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(6);
                ex.setMessageArgs(className);
                throw ex;
            }

            Node listTypeItemNode = getInstanceOfClass(className, oid);
            
            //Updates the unique attributes cache
            try {
                BusinessObject remoteObject = ogmService.createObjectFromNode(listTypeItemNode);
                for(AttributeMetadata attribute : classMetadata.getAttributes()) {
                    if(attribute.isUnique()) { 
                        Object attributeValue = remoteObject.getAttributes().get(attribute.getName());
                        if(attributeValue != null)
                            CacheManager.getInstance().removeUniqueAttributeValue(className, attribute.getName(), attributeValue);
                    }
                }
            } catch (InvalidArgumentException ex) {
                //Should not happen
            }
            
            Iterator<Relationship> relLayoutIterator = listTypeItemNode.getRelationships(RelTypes.HAS_LAYOUT).iterator();        
            if (relLayoutIterator.hasNext()) {
                for (Relationship aHasLayoutRelationship : listTypeItemNode.getRelationships(RelTypes.HAS_LAYOUT)) 
                     aHasLayoutRelationship.delete();
            }
            Iterator<Relationship> relationShipsIterator = listTypeItemNode.getRelationships(RelTypes.RELATED_TO).iterator();        
            if (relationShipsIterator.hasNext()) {
                if (realeaseRelationships) {
                    for (Relationship aRelatedToRelationship : listTypeItemNode.getRelationships(RelTypes.RELATED_TO)) 
                        aRelatedToRelationship.delete();
                }
                else {
                    OperationNotPermittedException ex = new OperationNotPermittedException(String.format(ts.getTranslatedString(KEY_PREFIX + ".7"), className, oid));
                    ex.setPrefix(KEY_PREFIX);
                    ex.setCode(7);
                    ex.setMessageArgs(className, oid);
                    throw ex;
                }
            }
            if (listTypeItemNode.hasRelationship(Direction.OUTGOING, RelTypes.INSTANCE_OF))
                listTypeItemNode.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).delete();
            
            listTypeItemNode.delete();
            tx.success();
            cm.removeListType(className);
        }
    }
    
    @Override
    public void releaseListTypeItem(String listTypeItemId) 
            throws MetadataObjectNotFoundException, OperationNotPermittedException, BusinessObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
          try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String cypherQuery = "MATCH (listType: " + listTypeItemLabel + " {_uuid: '" + listTypeItemId + "'}) - [r:HAS_ATTRIBUTE] - () DELETE r;";                      
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            tx.success();
        }
    }
    
    @Override
    public List<BusinessObjectLight> getListTypeItems(String className)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        List<BusinessObjectLight> children = new ArrayList<>();
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode ==  null) {
                MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".8"), className));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(8);
                ex.setMessageArgs(className);
                throw ex;
            }

            if (!Util.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, classNode)) {
                InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".9"), className));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(9);
                ex.setMessageArgs(className);
                throw ex;
            }

            Iterable<Relationship> childrenAsRelationships = classNode.getRelationships(RelTypes.INSTANCE_OF);
            Iterator<Relationship> relationships = childrenAsRelationships.iterator();

            while(relationships.hasNext()) {
                Node child = relationships.next().getStartNode();
                String childUuid = child.hasProperty(Constants.PROPERTY_UUID) ? (String) child.getProperty(Constants.PROPERTY_UUID) : null;
                if (childUuid == null) {
                    InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".10"), child.getId()));
                    ex.setPrefix(KEY_PREFIX);
                    ex.setCode(10);
                    ex.setMessageArgs(child.getId());
                    throw ex;
                }
                children.add(new BusinessObjectLight(className, childUuid, (String)child.getProperty(Constants.PROPERTY_NAME), 
                        (String)classNode.getProperty(Constants.PROPERTY_DISPLAY_NAME, null)));
            }
            tx.success();
        }
        
        Collections.sort(children);
        
        return children;
    }
    
    @Override
    public BusinessObject getListTypeItem(String listTypeClassName, String listTypeItemId) throws 
        MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node listTypeItemNode = connectionManager.getConnectionHandler().findNode(listTypeItemLabel, Constants.PROPERTY_UUID, listTypeItemId);
            
            if (listTypeItemNode == null) {
                ApplicationObjectNotFoundException ex = new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".11"), listTypeItemId));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(11);
                ex.setMessageArgs(listTypeItemId);
                throw ex;
            }
            
            if (listTypeItemNode.hasRelationship(RelTypes.INSTANCE_OF) && 
                    !listTypeItemNode.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME).equals(listTypeClassName)) {
                MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".12"), listTypeClassName));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(12);
                ex.setMessageArgs(listTypeClassName);
                throw ex;
            }

            tx.success();
            return createBusinessObjectFromNode(listTypeItemNode);
        }
    }
    
    @Override
    public BusinessObjectLight getListTypeItemWithName(String listTypeClassName, String listTypeItemName) throws 
        MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, listTypeClassName);
            
            if (classNode == null) {
                MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".13"), listTypeClassName));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(13);
                ex.setMessageArgs(listTypeClassName);
                throw ex;
            }
            
            if (!Util.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, classNode)) {
                InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".14"), listTypeClassName));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(14);
                ex.setMessageArgs(listTypeClassName);
                throw ex;
            }
            
            for (Relationship childRel : classNode.getRelationships(RelTypes.INSTANCE_OF)) {
                Node child = childRel.getStartNode();
                if (child.hasProperty(Constants.PROPERTY_NAME) && child.getProperty(Constants.PROPERTY_NAME).equals(listTypeItemName)) {
                    tx.success();
                    String childUuid = child.hasProperty(Constants.PROPERTY_UUID) ? (String) child.getProperty(Constants.PROPERTY_UUID) : null;
                    if (childUuid == null) {
                        InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".15"), child.getId()));
                        ex.setPrefix(KEY_PREFIX);
                        ex.setCode(15);
                        ex.setMessageArgs(child.getId());
                        throw ex;
                    }
                    return new BusinessObjectLight(listTypeClassName, childUuid, (String) child.getProperty(Constants.PROPERTY_NAME),
                                (String)classNode.getProperty(Constants.PROPERTY_DISPLAY_NAME, null));
                }
                
            }
            ApplicationObjectNotFoundException ex = new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".16"), listTypeClassName, listTypeItemName));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(16);
            ex.setMessageArgs(listTypeClassName, listTypeItemName);
            throw ex;
        }
    }
    
    @Override
    public List<ClassMetadataLight> getInstanceableListTypes()
            throws ApplicationObjectNotFoundException {
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node genericObjectListNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, Constants.CLASS_GENERICOBJECTLIST);

            if (genericObjectListNode == null)
                throw new ApplicationObjectNotFoundException("ClassGenericObjectList not found");
            
            String cypherQuery = "MATCH (classmetadata:classes) <-[:".concat(RelTypes.EXTENDS.toString()).concat("*]-(listType) ").concat(
                                 "WHERE classmetadata.name IN ['").concat(Constants.CLASS_GENERICOBJECTLIST).concat("']").concat(
                                 "RETURN listType ").concat(
                                 "ORDER BY listType.name ASC");    
                        
            List<ClassMetadataLight> res = new ArrayList<>();
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
        
            Iterator<Node> n_column = result.columnAs("listType");
            
            for (Node node : Iterators.asIterable(n_column)) {
                if (!(Boolean)node.getProperty(Constants.PROPERTY_ABSTRACT))
                    res.add(Util.createClassMetadataLightFromNode(node));
            }
            return res;
        }
        
    }
    
    private Node getListTypeItemNode(String listTypeItemId, String listTypeItemClassName) 
        throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, listTypeItemClassName);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found. Contact your administrator.", listTypeItemClassName));
            
            if (!Util.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, classNode))
                throw new InvalidArgumentException(String.format("Class %s is not a list type", listTypeItemClassName));
            
            Node listTypeItemNode = null;
            
            for (Relationship childRel : classNode.getRelationships(RelTypes.INSTANCE_OF)) {
                Node child = childRel.getStartNode();
                
                String childUuid = child.hasProperty(Constants.PROPERTY_UUID) ? (String) child.getProperty(Constants.PROPERTY_UUID) : null;
                if (childUuid == null)
                    throw new InvalidArgumentException(String.format("The list type item with id %s does not have uuid", child.getId()));
                
                if (childUuid.equals(listTypeItemId)) {
                    listTypeItemNode = child;
                    break;
                }
            }
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), listTypeItemId));
            
            tx.success();
            return listTypeItemNode;
        }
    }
      
    @Override
    public long createListTypeItemRelatedLayout(String listTypeItemId, String listTypeItemClassName, 
            String viewClassName, String name, String description, byte [] structure, byte [] background) throws MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClassName);
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), listTypeItemId));
            
            Node viewNode = connectionManager.getConnectionHandler().createNode(layoutLabel); // This is temporary. In the future, not all list type item related views will be device layouts
            viewNode.setProperty(Constants.PROPERTY_CLASSNAME, viewClassName);
            listTypeItemNode.createRelationshipTo(viewNode, RelTypes.HAS_LAYOUT);
            
            if (name != null && !name.isEmpty())
                viewNode.setProperty(Constants.PROPERTY_NAME, name);
            else
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.name-cant-be-empty-or-null"));
            
            if (description != null)
                viewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            
            if (structure != null)
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            else
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, new byte[0]);
            
            if (background != null) {
                try {
                    String fileName = "view-" + listTypeItemId + "-" + viewNode.getId() + "-" + viewClassName;
                    Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                    viewNode.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                } catch(Exception ex) {
                    throw new InvalidArgumentException(String.format("Background image for view %s could not be saved: %s",
                            listTypeItemId, ex.getMessage()));
                }
            }
            tx.success();
            return viewNode.getId();
        }
    }
    
    @Override
    public List<ViewObjectLight> getLayouts(int limit) 
            throws InvalidArgumentException, NotAuthorizedException 
    {
        String cypherQuery = String.format("MATCH (view:%s)" +
                                   " WHERE NOT (view) <- [:%s] - (:listTypeItems) - [:%s] -> (:classes {name : \"CustomShape\"}) " +
                                   " RETURN view", layoutLabel, RelTypes.HAS_LAYOUT, RelTypes.INSTANCE_OF);

        if (limit != -1)
            cypherQuery += " LIMIT " + limit;
           
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<Node> lytViews = result.columnAs("view");
            List<ViewObjectLight> myRes = new ArrayList<>();
            while (lytViews.hasNext()) {
                Node lytView = lytViews.next();
                ViewObjectLight aView = new ViewObjectLight(lytView.getId(),
                        lytView.hasProperty(Constants.PROPERTY_NAME) ? (String) lytView.getProperty(Constants.PROPERTY_NAME) : null,
                        lytView.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String) lytView.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                        (String) lytView.getProperty(Constants.PROPERTY_CLASSNAME));
                myRes.add(aView);
            }
            return myRes;
        }
    }
    @Override    
    public ViewObject getLayout(long layoutViewId) throws ApplicationObjectNotFoundException {
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node gView = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), layoutLabel, layoutViewId);

            if (gView == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.layout-not-found"), layoutViewId));

            ViewObject aView = new ViewObject(gView.getId(),
                    gView.hasProperty(Constants.PROPERTY_NAME) ? (String)gView.getProperty(Constants.PROPERTY_NAME) : null,
                    gView.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)gView.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                    (String)gView.getProperty(Constants.PROPERTY_CLASSNAME));
            if (gView.hasProperty(Constants.PROPERTY_STRUCTURE))
                aView.setStructure((byte[])gView.getProperty(Constants.PROPERTY_STRUCTURE));
            if (gView.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)) {
                String fileName = (String)gView.getProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                byte[] background = null;
                try {
                    background = Util.readBytesFromFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName);
                } catch(Exception e) {
                    System.out.println("[KUWAIBA] " + e.getMessage());
                }
                aView.setBackground(background);
            }
            return aView;
        }
    }
    @Override    
    public BusinessObjectLight getListTypeItemForLayout(long layoutViewId) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node gView = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), layoutLabel, layoutViewId);

            if (gView == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.layout-not-found"), layoutViewId));

            for (Relationship rel : gView.getRelationships(RelTypes.HAS_LAYOUT, Direction.INCOMING)) {
                Node viewNode = rel.getStartNode();
                return createBusinessObjectFromNode(viewNode);
            }
            return null;
        }
    }
    
    @Override
    public long createLayout(String viewClassName, String name, String description, byte [] structure, byte [] background) throws InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
           
            Node viewNode = connectionManager.getConnectionHandler().createNode(layoutLabel); // This is temporary. In the future, not all list type item related views will be device layouts
            viewNode.setProperty(Constants.PROPERTY_CLASSNAME, viewClassName);           
            
            if (name != null && !name.isEmpty())
                viewNode.setProperty(Constants.PROPERTY_NAME, name);
            else
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.name-cant-be-empty-or-null"));
            
            if (description != null)
                viewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            
            if (structure != null)
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            else
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, new byte[0]);
            
            if (background != null) {
                try {
                    String fileName = "view-" + viewNode.getId() + "-" + viewClassName;
                    Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                    viewNode.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                } catch(IOException ex) {
                    throw new InvalidArgumentException(String.format("Background image for view %s could not be saved: %s",
                            name, ex.getMessage()));
                }
            }
            tx.success();
            return viewNode.getId();
        }
    }
    
    
    @Override
    public void setListTypeItemRelatedLayout(String listTypeItemId, String listTypeItemClass, long viewId) throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClass);
            if (listTypeItemNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), listTypeItemId));
              
            for (Relationship rel : listTypeItemNode.getRelationships(RelTypes.HAS_LAYOUT, Direction.OUTGOING)) {
                 rel.delete();
            }
            
            Node viewNode = connectionManager.getConnectionHandler().getNodeById(viewId);    
            if (viewNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), viewId)); 
             
            for (Relationship rel : viewNode.getRelationships(RelTypes.HAS_LAYOUT, Direction.INCOMING)) {
                 rel.delete();
            }
            listTypeItemNode.createRelationshipTo(viewNode, RelTypes.HAS_LAYOUT);
            tx.success();
        } 
    }
    
          
    @Override
    public void releaseListTypeItemRelatedLayout(String listTypeItemId, String listTypeItemClass, long viewId) 
        throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClass);            
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.cannot-find-list-type-item"), listTypeItemId));
            
            for (Relationship rel : listTypeItemNode.getRelationships(RelTypes.HAS_LAYOUT, Direction.OUTGOING)) {
                Node viewNode = rel.getEndNode();
                if (viewNode.getId() == viewId) {
                    rel.delete();
                    tx.success();
                    return;
                }
            }
            tx.success();
        }
        throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), viewId)); 
    }
    
    @Override
    public ChangeDescriptor updateListTypeItemRelatedLayout(String listTypeItemId, String listTypeItemClass, long viewId, 
        String name, String description, byte[] structure, byte[] background) 
        throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClass);
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), listTypeItemId));
            
            Node viewNode = null;
            for (Relationship rel : listTypeItemNode.getRelationships(RelTypes.HAS_LAYOUT, Direction.OUTGOING)) {
                if (rel.getEndNode().getId() == viewId) {
                    viewNode = rel.getEndNode();
                    break;
                }
            }
            if (viewNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), viewId)); 
                        
            String affectedProperties = "", oldValues = "", newValues = "";
            
            if (name != null) {
                oldValues +=  " " + viewNode.getProperty(Constants.PROPERTY_NAME);
                newValues += " " + name;
                affectedProperties += " " + Constants.PROPERTY_NAME;
                viewNode.setProperty(Constants.PROPERTY_NAME, name);
            }

            if (structure != null) {
                affectedProperties += " " + Constants.PROPERTY_STRUCTURE;
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            }

            if (description != null) {
                oldValues += " " + viewNode.getProperty(Constants.PROPERTY_DESCRIPTION);
                newValues += " " + description;
                affectedProperties += " " + Constants.PROPERTY_DESCRIPTION;
                viewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            }

            String fileName = "view-" + listTypeItemId + "-" + viewId + "-" + viewNode.getProperty(Constants.PROPERTY_CLASSNAME);
            if (background != null) {
                try{
                    affectedProperties += " " + Constants.PROPERTY_BACKGROUND;
                    Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                    viewNode.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                }catch(Exception ex) {
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.background-image-couldnt-be-saved"),
                            listTypeItemId, ex.getMessage()));
                }
            }
            else {
                if (viewNode.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)) {
                    try{
                        new File(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName).delete();
                    }catch(Exception ex) {
                        throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-background-couldnt-be-deleted"), 
                                configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName, ex.getMessage()));
                    }
                    viewNode.removeProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                    affectedProperties += " " + Constants.PROPERTY_BACKGROUND;
                }
            }
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, null);
        }
    }
    
    @Override
    public ChangeDescriptor updateLayout(long viewId, String name, String description, byte[] structure, byte[] background) 
        throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
             Node viewNode = connectionManager.getConnectionHandler().getNodeById(viewId);
            
            if (viewNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), viewId)); 
                        
            String affectedProperties = "", oldValues = "", newValues = "";
            
            if (name != null) {
                oldValues +=  " " + (viewNode.hasProperty(Constants.PROPERTY_NAME) ? viewNode.getProperty(Constants.PROPERTY_NAME) : "");
                newValues += " " + name;
                affectedProperties += " " + Constants.PROPERTY_NAME;
                viewNode.setProperty(Constants.PROPERTY_NAME, name);
            }

            if (structure != null) {
                affectedProperties += " " + Constants.PROPERTY_STRUCTURE;
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            }

            if (description != null) {
                oldValues += " " + (viewNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? viewNode.getProperty(Constants.PROPERTY_DESCRIPTION) : "");
                newValues += " " + description;
                affectedProperties += " " + Constants.PROPERTY_DESCRIPTION;
                viewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            }

            String fileName = "view-" + "-" + viewId + "-" + viewNode.getProperty(Constants.PROPERTY_CLASSNAME);
            if (background != null) {
                try {
                    affectedProperties += " " + Constants.PROPERTY_BACKGROUND;
                    Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                    viewNode.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                } catch (IOException ex) {
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.background-image-couldnt-be-saved"),
                            viewId, ex.getMessage()));
                }
            }
            else {
                if (viewNode.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)) {
                    try{
                        new File(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName).delete();
                    }catch(Exception ex) {
                        throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-background-couldnt-be-deleted"), 
                                configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName, ex.getMessage()));
                    }
                    viewNode.removeProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                    affectedProperties += " " + Constants.PROPERTY_BACKGROUND;
                }
            }
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, null);
        }
    }
    

    
    @Override
    public ViewObject getListTypeItemRelatedLayout(String listTypeItemId, String listTypeItemClass, long viewId) 
        throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClass);
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), listTypeItemId));
            
            for (Relationship rel : listTypeItemNode.getRelationships(RelTypes.HAS_LAYOUT, Direction.OUTGOING)) {
                Node viewNode = rel.getEndNode();
                if (viewNode.getId() == viewId) {
                    ViewObject res = new ViewObject(viewId,
                            viewNode.hasProperty(Constants.PROPERTY_NAME) ? (String)viewNode.getProperty(Constants.PROPERTY_NAME) : null,
                            viewNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)viewNode.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                            (String)viewNode.getProperty(Constants.PROPERTY_CLASSNAME));
                    if (viewNode.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)) {
                        String fileName = (String)viewNode.getProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                        byte[] background = null;
                        try {
                            background = Util.readBytesFromFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName);
                        }catch(Exception e) {
                            System.out.println(e.getMessage());
                        }
                        res.setBackground(background);
                    }
                    if (viewNode.hasProperty(Constants.PROPERTY_STRUCTURE))
                        res.setStructure((byte[])viewNode.getProperty(Constants.PROPERTY_STRUCTURE));
                    return res;
                }
            }
        }
        throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), viewId));                
    }
    
    @Override        
    public List<ViewObjectLight> getListTypeItemRelatedLayout(String listTypeItemId, String listTypeItemClass, int limit) 
        throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClass);
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), listTypeItemId));
            
            List<ViewObjectLight> res = new ArrayList();
            int i = 0;
            for (Relationship rel : listTypeItemNode.getRelationships(RelTypes.HAS_LAYOUT, Direction.OUTGOING)) {
                if (limit != -1) {
                    if (i < limit)
                        i++;
                    else
                        break;
                }
                Node viewNode = rel.getEndNode();
                res.add(new ViewObjectLight(viewNode.getId(),
                    viewNode.hasProperty(Constants.PROPERTY_NAME) ? (String)viewNode.getProperty(Constants.PROPERTY_NAME) : null,
                    viewNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)viewNode.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                    (String) viewNode.getProperty(Constants.PROPERTY_CLASSNAME)));
            }
            return res;
        }
    }
    
    @Override        
    public void deleteListTypeItemRelatedLayout(String listTypeItemId, String listTypeItemClass, long viewId) 
        throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClass);            
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), listTypeItemId));
            
            for (Relationship rel : listTypeItemNode.getRelationships(RelTypes.HAS_LAYOUT, Direction.OUTGOING)) {
                Node viewNode = rel.getEndNode();
                if (viewNode.getId() == viewId) {
                    rel.delete();
                    viewNode.delete();
                    tx.success();
                    return;
                }
            }
            tx.success();
        }
        throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), viewId)); 
    }
            
    @Override
    public void deleteLayout(long viewId) 
        throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node viewNode = connectionManager.getConnectionHandler().getNodeById(viewId);
            
            if (viewNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), viewId)); 
                        
            for (Relationship rel : viewNode.getRelationships())              
                    rel.delete();           
            
            viewNode.delete();
            tx.success();
        }
    }
    
    @Override
    public List<BusinessObjectLight> getListTypeItemUses(String listTypeItemClass, String listTypeItemId, int limit) 
        throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            String cypherQuery = String.format("MATCH (ltItem:%s)<-[:%s]-(ltUser) WHERE ltItem._uuid = '%s' RETURN ltUser ORDER BY ltUser.name ASC %s", 
                    listTypeItemLabel, RelTypes.RELATED_TO, listTypeItemId, limit < 1 ? "" : "LIMIT " + limit);
            
            List<BusinessObjectLight> res = new ArrayList<>();
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
        
            Iterator<Node> objectsThatUseListType = result.columnAs("ltUser");
            
            while (objectsThatUseListType.hasNext()) {
                Node node = objectsThatUseListType.next();
                if (node.hasRelationship(RelTypes.INSTANCE_OF))
                    res.add(createRemoteObjectLightFromNode(node));
                else if (node.hasRelationship(RelTypes.INSTANCE_OF_SPECIAL)) {
                    Node templateObjectClassNode = node.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                    
                    String nodeUuid = node.hasProperty(Constants.PROPERTY_UUID) ? (String) node.getProperty(Constants.PROPERTY_UUID) : null;
                    if (nodeUuid == null)
                        throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", node.getId()));
                    
                    res.add(new BusinessObjectLight((String)templateObjectClassNode.getProperty(Constants.PROPERTY_NAME), 
                            nodeUuid, (String)node.getProperty(Constants.PROPERTY_NAME),
                            (String)templateObjectClassNode.getProperty(Constants.PROPERTY_DISPLAY_NAME, null)));
                }
            }
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getDeviceLayouts() throws InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String columnName = "elements"; //NOI18N
            String cypherQuery = String.format(
                "MATCH (classNode)<-[r1:%s]-(templateElement)-[r2:%s]->(list)-[:%s]->(view) "
              + "WHERE r1.name=\"template\" AND r2.name=\"model\" "
              + "RETURN templateElement AS %s "
              + "ORDER BY templateElement.name ASC ", 
                 RelTypes.INSTANCE_OF_SPECIAL, RelTypes.RELATED_TO, RelTypes.HAS_LAYOUT, columnName);
            
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<Node> column = result.columnAs(columnName);
            
            List<BusinessObjectLight> templateElements = new ArrayList();
            
            for (Node templateElementNode : Iterators.asIterable(column)) {
                Node templateObjectClassNode = templateElementNode.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                
                String templateElementNodeUuid = templateElementNode.hasProperty(Constants.PROPERTY_UUID) ? (String) templateElementNode.getProperty(Constants.PROPERTY_UUID) : null;
                if (templateElementNodeUuid == null)
                    throw new InvalidArgumentException(String.format("The template with id %s does not have uuid", templateElementNode.getId()));                    
                                
                templateElements.add(new BusinessObjectLight((String)templateObjectClassNode.getProperty(Constants.PROPERTY_NAME), 
                    templateElementNodeUuid, (String)templateElementNode.getProperty(Constants.PROPERTY_NAME),
                     (String)templateObjectClassNode.getProperty(Constants.PROPERTY_DISPLAY_NAME, null)));
            }
                        
            return templateElements;
        }
    }
          
    @Override
    public byte[] getDeviceLayoutStructure(String oid, String className) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        final String columnName = "name"; //NOI18N
        final String columnId = "id"; //NOI18N
        final String columnClassName = "className"; //NOI18N
        final String columnModelId = "modelId"; //NOI18N
        final String columnModelName = "modelName"; //NOI18N
        final String columnModelClassName = "modelClassName"; //NOI18N
        final String columnViewId = "viewId"; //NOI18N
        final String columnViewClassName = "viewClassName"; //NOI18N
        final String columnViewStructure = "viewStructure"; //NOI18N
        final String columnParentId = "parentId"; //NOI18N
        
        HashMap<String, HashMap<String, Object>> devicesWithLayout = new HashMap();
                
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("MATCH (deviceClass:classes)<-[:INSTANCE_OF]-(device:inventoryObjects)<-[:CHILD_OF*]-(deviceChild:inventoryObjects)").append(" ");
            stringBuilder.append("WHERE deviceClass.name = {className} AND device._uuid = {id}").append(" ");
            stringBuilder.append("WITH [deviceChild, device] AS deviceChild1").append(" ");
            stringBuilder.append("UNWIND deviceChild1 AS deviceChild2").append(" ");
            stringBuilder.append("MATCH (deviceChildClass:classes)<-[:INSTANCE_OF]-(deviceChild2:inventoryObjects)-[:RELATED_TO{name:'model'}]->(model:listTypeItems)-[:HAS_LAYOUT]->(layout:layouts), (deviceChild2)-[:CHILD_OF]->(deviceParent), (model)-[:INSTANCE_OF]->(modelClass:classes)").append(" ");
            stringBuilder.append("RETURN DISTINCT deviceChild2.name AS name, deviceChild2._uuid AS id, deviceChildClass.name AS className, model._uuid as modelId, model.name as modelName, modelClass.name as modelClassName, id(layout) as viewId, layout.className as viewClassName, layout.structure as viewStructure, deviceParent._uuid as parentId;");

            String cypherQuery = stringBuilder.toString();

            HashMap<String, Object> queryParameters = new HashMap<>();
            queryParameters.put("id", oid); //NOI18N
            queryParameters.put("className", className); //NOI18N

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery, queryParameters);
            
            while (result.hasNext()) {
                Map<String, Object> next = result.next();
                
                HashMap<String, Object> properties = new HashMap();
                properties.put(columnName, next.get(columnName));
                properties.put(columnId, next.get(columnId));
                properties.put(columnClassName, next.get(columnClassName));
                properties.put(columnModelId, next.get(columnModelId));
                properties.put(columnModelName, next.get(columnModelName));
                properties.put(columnModelClassName, next.get(columnModelClassName));
                properties.put(columnViewId, next.get(columnViewId));
                properties.put(columnViewClassName, next.get(columnViewClassName));
                properties.put(columnViewStructure, next.get(columnViewStructure));
                properties.put(columnParentId, next.get(columnParentId));                
                
                devicesWithLayout.put((String) next.get(columnId), properties);
            }
            tx.success();
        }
        HashMap<String, HashMap<String, Object>> devices = new HashMap();
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder stringBuilder = new StringBuilder();        
            stringBuilder.append("MATCH (deviceClass:classes)<-[:INSTANCE_OF]-(device:inventoryObjects)<-[:CHILD_OF*]-(deviceChild:inventoryObjects)").append(" ");
            stringBuilder.append("WHERE deviceClass.name = {className} AND device._uuid = {id}").append(" ");
            stringBuilder.append("WITH deviceChild AS deviceChild1").append(" ");
            stringBuilder.append("MATCH (deviceChild1)-[:INSTANCE_OF]->(deviceChildClass), (deviceChild1)-[:CHILD_OF]->(deviceChildParent)").append(" ");
            stringBuilder.append("RETURN deviceChild1.name AS name, deviceChild1._uuid AS id, deviceChildClass.name AS className, deviceChildParent._uuid AS parentId;");
            
            String cypherQuery = stringBuilder.toString();
            
            HashMap<String, Object> queryParameters = new HashMap<>();
            queryParameters.put("id", oid); //NOI18N
            queryParameters.put("className", className); //NOI18N

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery, queryParameters);
            
            while(result.hasNext()) {
                Map<String, Object> next = result.next();
                                
                HashMap<String, Object> properties = new HashMap();
                properties.put(columnName, next.get(columnName));
                properties.put(columnId, next.get(columnId));
                properties.put(columnClassName, next.get(columnClassName));
                properties.put(columnParentId, next.get(columnParentId));
                
                devices.put((String) next.get(columnId), properties);
            }
            tx.success();
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName tagDeviceLayoutStructure = new QName("deviceLayoutStructure"); //NOI18N
            QName tagDevice = new QName("device"); //NOI18N
            QName tagModel = new QName("model"); //NOI18N
            QName tagView = new QName("view"); //NOI18N
            QName tagStructure = new QName("structure"); //NOI18N
            QName attrId = new QName("id"); //NOI18N
            QName attrName = new QName("name"); //NOI18N
            QName attrClassName = new QName("className"); //NOI18N
            QName attrParentId = new QName("parentId"); //NOI18N
            
            xmlew.add(xmlef.createStartElement(tagDeviceLayoutStructure, null, null));
            for (String deviceId : devices.keySet()) {
                xmlew.add(xmlef.createStartElement(tagDevice, null, null));
                xmlew.add(xmlef.createAttribute(attrId, String.valueOf(devices.get(deviceId).get("id")))); //NOI18N
                xmlew.add(xmlef.createAttribute(attrName, String.valueOf(devices.get(deviceId).get("name")))); //NOI18N
                xmlew.add(xmlef.createAttribute(attrClassName, String.valueOf(devices.get(deviceId).get("className")))); //NOI18N
                if (!oid.equals(deviceId))
                    xmlew.add(xmlef.createAttribute(attrParentId, String.valueOf(devices.get(deviceId).get("parentId")))); //NOI18N
                if (devicesWithLayout.containsKey(deviceId)) {
                    xmlew.add(xmlef.createStartElement(tagModel, null, null));
                    xmlew.add(xmlef.createAttribute(attrId, String.valueOf(devicesWithLayout.get(deviceId).get("modelId")))); //NOI18N
                    xmlew.add(xmlef.createAttribute(attrClassName, String.valueOf(devicesWithLayout.get(deviceId).get("modelClassName")))); //NOI18N
                    xmlew.add(xmlef.createAttribute(attrName, String.valueOf(devicesWithLayout.get(deviceId).get("modelName")))); //NOI18N
                    
                    xmlew.add(xmlef.createStartElement(tagView, null, null));
                    xmlew.add(xmlef.createAttribute(attrId, String.valueOf(devicesWithLayout.get(deviceId).get("viewId")))); //NOI18N
                    xmlew.add(xmlef.createAttribute(attrClassName, String.valueOf(devicesWithLayout.get(deviceId).get("viewClassName")))); //NOI18N
                                        
                    xmlew.add(xmlef.createStartElement(tagStructure, null, null));
                    xmlew.add(xmlef.createCharacters(DatatypeConverter.printBase64Binary((byte[]) devicesWithLayout.get(deviceId).get("viewStructure")))); //NOI18N
                    
                    xmlew.add(xmlef.createEndElement(tagStructure, null));
                    xmlew.add(xmlef.createEndElement(tagView, null));
                    xmlew.add(xmlef.createEndElement(tagModel, null));
                }
                xmlew.add(xmlef.createEndElement(tagDevice, null));
            }
            xmlew.add(xmlef.createEndElement(tagDeviceLayoutStructure, null));
            xmlew.close();
            
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistance.aem.messages.layout-corrupted"));
        }                
    }
            
    @Override
    public long createObjectRelatedView(String oid, String objectClass, String name, String description, String viewClassName, 
        byte[] structure, byte[] background) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (objectClass == null)
            throw new InvalidArgumentException("The root object can not be related to any view");
        
        long id;
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node instance = getInstanceOfClass(objectClass, oid);
            Node viewNode = connectionManager.getConnectionHandler().createNode(objectRelatedViewLabel);
            viewNode.setProperty(Constants.PROPERTY_CLASSNAME, viewClassName);
            instance.createRelationshipTo(viewNode, RelTypes.HAS_VIEW);

            if (name != null)
                viewNode.setProperty(Constants.PROPERTY_NAME, name);

            if (structure != null)
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);

            if (background != null) {
                try{
                    String fileName = "view-" + oid + "-" + viewNode.getId() + "-" + viewClassName;
                    Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                    viewNode.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                }catch(Exception ex) {
                    throw new InvalidArgumentException(String.format("Background image for view %s could not be saved: %s",
                            oid, ex.getMessage()));
                }
            }

            if (description != null)
                viewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);

            tx.success();
            id = viewNode.getId();
        }
        return id;
    }

    @Override
    public long createGeneralView(String viewClass, String name, String description, byte[] structure, byte[] background)
            throws InvalidArgumentException {
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node newView = connectionManager.getConnectionHandler().createNode(generalViewsLabel);

            newView.setProperty(Constants.PROPERTY_CLASSNAME, viewClass);
            if (name != null)
                newView.setProperty(Constants.PROPERTY_NAME, name);
            if (description != null)
                newView.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            if (structure != null)
                newView.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            if (background != null) {
                try{
                    String fileName = "view-" + newView.getId() + "-" + viewClass;
                    Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                    newView.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                }catch(Exception ex) {
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.background-image-couldnt-be-saved"), 
                            newView.getId(), ex.getMessage()));
                }
            }
            tx.success();
            return newView.getId();
        }
    }

    @Override
    public ChangeDescriptor updateObjectRelatedView(String oid, String objectClass, long viewId, 
    String name, String description, byte[] structure, byte[] background)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (objectClass == null)
            throw new InvalidArgumentException("The root object does not have views");
        
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node instance = getInstanceOfClass(objectClass, oid);
            String affectedProperties = "", oldValues = "", newValues = "";
            Node viewNode = null;
            for (Relationship rel : instance.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)) {
                if (rel.getEndNode().getId() == viewId) {
                    viewNode = rel.getEndNode();
                    break;
                }
            }

            if (viewNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-not-found"), viewId)); //NOI18N

            if (name != null) {
                oldValues +=  " " + viewNode.getProperty(Constants.PROPERTY_NAME);
                newValues += " " + name;
                affectedProperties += " " + Constants.PROPERTY_NAME;
                viewNode.setProperty(Constants.PROPERTY_NAME, name);
            }

            if (structure != null) {
                affectedProperties += " " + Constants.PROPERTY_STRUCTURE;
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            }

            if (description != null) {
                oldValues += " " + viewNode.getProperty(Constants.PROPERTY_DESCRIPTION);
                newValues += " " + description;
                affectedProperties += " " + Constants.PROPERTY_DESCRIPTION;
                viewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            }

            String fileName = "view-" + oid + "-" + viewId + "-" + viewNode.getProperty(Constants.PROPERTY_CLASSNAME);
            if (background != null) {
                try{
                    affectedProperties += " " + Constants.PROPERTY_BACKGROUND;
                    Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                    viewNode.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                } catch(Exception ex) {
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.background-image-couldnt-be-saved"),
                            oid, ex.getMessage()));
                }
            }
            else {
                if (viewNode.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)) {
                    try{
                        new File(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName).delete();
                    }catch(Exception ex) {
                        throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.view-background-couldnt-be-deleted"), 
                                configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName, ex.getMessage()));
                    }
                    viewNode.removeProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                    affectedProperties += " " + Constants.PROPERTY_BACKGROUND;
                }
            }
            tx.success();
            
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, null);
            
        }
    }

    @Override
    public ChangeDescriptor updateGeneralView(long oid, String name, String description, byte[] structure, byte[] background)
            throws InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String affectedProperty = "", oldValue = "", newValue = ""; //NOI18N
            
            Node gView = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), generalViewsLabel, oid);
            if (gView == null)
                throw new ApplicationObjectNotFoundException(String.format("View with id %s could not be found", oid));
            if (name != null) {
                affectedProperty += Constants.PROPERTY_NAME;
                oldValue += String.valueOf(gView.getProperty(Constants.PROPERTY_NAME));
                gView.setProperty(Constants.PROPERTY_NAME, name);
                newValue += name;
            }
            if (description != null) {
                affectedProperty += " " + Constants.PROPERTY_DESCRIPTION;
                oldValue += " " + String.valueOf(gView.getProperty(Constants.PROPERTY_DESCRIPTION));
                gView.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            }
            if (structure != null) {
                gView.setProperty(Constants.PROPERTY_STRUCTURE, structure);
                affectedProperty += " " + Constants.PROPERTY_STRUCTURE;
            }
            if (background != null) {
                if (background.length != 0) {
                    try{
                        String fileName = "view-" + oid + "-" + gView.getProperty(Constants.PROPERTY_CLASSNAME);
                        Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                        gView.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                        affectedProperty += " " + Constants.PROPERTY_BACKGROUND;
                    }catch(Exception ex) {
                        throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.background-image-couldnt-be-saved"),
                                oid, ex.getMessage()));
                    }
                }
            }else {
                gView.removeProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                affectedProperty += " " + Constants.PROPERTY_BACKGROUND;
            }   
            tx.success();
            return new ChangeDescriptor(affectedProperty, oldValue, newValue ,null);
        }
    }

    @Override
    public void deleteGeneralViews(List<Long> ids) throws ApplicationObjectNotFoundException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            for (long id : ids) {
                Node gView = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), generalViewsLabel, id);
                
                if (gView == null)
                    throw new ApplicationObjectNotFoundException(String.format("View with id %s could not be found", id));
                
                gView.delete();
            }
            tx.success();
        }
    }

    @Override
    public ViewObject getObjectRelatedView(String oid, String objectClass, long viewId)
            throws ApplicationObjectNotFoundException, BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node instance = getInstanceOfClass(objectClass, oid);

            for (Relationship rel : instance.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)) {
                Node viewNode = rel.getEndNode();
                if (viewNode.getId() == viewId) {
                    ViewObject res = new ViewObject(viewId,
                            viewNode.hasProperty(Constants.PROPERTY_NAME) ? (String)viewNode.getProperty(Constants.PROPERTY_NAME) : null,
                            viewNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)viewNode.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                            (String)viewNode.getProperty(Constants.PROPERTY_CLASSNAME));
                    if (viewNode.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)) {
                        String fileName = (String)viewNode.getProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                        byte[] background = null;
                        try {
                            background = Util.readBytesFromFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName);
                        }catch(Exception e) {
                            System.out.println(e.getMessage());
                        }
                        res.setBackground(background);
                    }
                    if (viewNode.hasProperty(Constants.PROPERTY_STRUCTURE))
                        res.setStructure((byte[])viewNode.getProperty(Constants.PROPERTY_STRUCTURE));
                    return res;
                }
            }
        }
        throw new ApplicationObjectNotFoundException(String.format("View Object with id %s could not be found. It might have been deleted already", viewId));
    }

    @Override
    public List<ViewObjectLight> getObjectRelatedViews(String oid, String objectClass, int limit)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node instance = getInstanceOfClass(objectClass, oid);
            List<ViewObjectLight> res = new ArrayList<>();
            int i = 0;
            for (Relationship rel : instance.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)) {
                if (limit != -1) {
                    if (i < limit)
                        i++;
                    else break;
                }
                Node viewNode = rel.getEndNode();
                res.add(new ViewObjectLight(viewNode.getId(), 
                        viewNode.hasProperty(Constants.PROPERTY_NAME) ? (String)viewNode.getProperty(Constants.PROPERTY_NAME) : null,
                        viewNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)viewNode.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                        (String)viewNode.getProperty(Constants.PROPERTY_CLASSNAME)));
            }
            return res;
        }
    }

    @Override
    public List<ViewObjectLight> getGeneralViews(String viewClass, int limit) 
            throws InvalidArgumentException, NotAuthorizedException 
    {
        String cypherQuery = "MATCH (gView:" + Constants.LABEL_GENERAL_VIEWS + ") ";
        cypherQuery += " WHERE gView." + Constants.PROPERTY_CLASSNAME + "='" + viewClass + "'";

        cypherQuery += " RETURN gView";

        if (limit != -1)
            cypherQuery += " LIMIT " + limit;
    
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<Node> gViews = result.columnAs("gView");
            List<ViewObjectLight> myRes = new ArrayList<>();
            while (gViews.hasNext()) {
                Node gView = gViews.next();
                ViewObjectLight aView = new ViewObjectLight(gView.getId(), (String)gView.getProperty(Constants.PROPERTY_NAME),
                        (String)gView.getProperty(Constants.PROPERTY_DESCRIPTION), (String)gView.getProperty(Constants.PROPERTY_CLASSNAME));
                if (gView.hasProperty(Constants.PROPERTY_NAME));
                    aView.setName((String)gView.getProperty(Constants.PROPERTY_NAME));
                if (gView.hasProperty(Constants.PROPERTY_DESCRIPTION));
                    aView.setDescription((String)gView.getProperty(Constants.PROPERTY_DESCRIPTION));

                myRes.add(aView);
            }
            return myRes;
        }
    }

    @Override
    public ViewObject getGeneralView(long viewId) throws ApplicationObjectNotFoundException {
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node gView = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), generalViewsLabel, viewId);

            if (gView == null)
                throw new ApplicationObjectNotFoundException(String.format("View Object with id %s could not be found. It might have been deleted already", viewId));

            ViewObject aView = new ViewObject(gView.getId(),
                    gView.hasProperty(Constants.PROPERTY_NAME) ? (String)gView.getProperty(Constants.PROPERTY_NAME) : null,
                    gView.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)gView.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                    (String)gView.getProperty(Constants.PROPERTY_CLASSNAME));
            if (gView.hasProperty(Constants.PROPERTY_STRUCTURE))
                aView.setStructure((byte[])gView.getProperty(Constants.PROPERTY_STRUCTURE));
            if (gView.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)) {
                String fileName = (String)gView.getProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                byte[] background = null;
                try {
                    background = Util.readBytesFromFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName);
                }catch(Exception e) {
                    System.out.println("[KUWAIBA] " + e.getMessage());
                }
                aView.setBackground(background);
            }
            return aView;
        }
    }

    //Queries
    @Override
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure,
            String description) throws ApplicationObjectNotFoundException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node queryNode =  connectionManager.getConnectionHandler().createNode(queryLabel);
            queryNode.setProperty(CompactQuery.PROPERTY_QUERYNAME, queryName);
            if(description == null)
                description = "";
            queryNode.setProperty(CompactQuery.PROPERTY_DESCRIPTION, description);
            queryNode.setProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE, queryStructure);
            queryNode.setProperty(CompactQuery.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            
            if(ownerOid != -1) {
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, false);
                Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, ownerOid);

                if(userNode != null)
                    userNode.createRelationshipTo(queryNode, RelTypes.OWNS_QUERY);
                else
                    throw new ApplicationObjectNotFoundException(String.format("User with id %s could not be found", ownerOid));
            }
            else
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, true);

            tx.success();
            return queryNode.getId();

        }
    }

    @Override
    public ChangeDescriptor saveQuery(long queryOid, String queryName, long ownerOid,
            byte[] queryStructure, String description) throws ApplicationObjectNotFoundException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node queryNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), queryLabel, queryOid);
            
            if(queryNode == null)
                throw new ApplicationObjectNotFoundException(String.format(
                        "Can not find the query with id %s", queryOid));
            String affectedProperties = "", oldValues = "", newValues = "", notes = "";

            queryNode.setProperty(CompactQuery.PROPERTY_QUERYNAME, queryName);
            affectedProperties += CompactQuery.PROPERTY_QUERYNAME;
            newValues += queryName;
            
            if(description != null) {
                queryNode.setProperty(CompactQuery.PROPERTY_DESCRIPTION, description);
                affectedProperties += " " + CompactQuery.PROPERTY_DESCRIPTION;
                newValues += " " + description;
            }
            
            queryNode.setProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE, queryStructure);
            affectedProperties += " " + CompactQuery.PROPERTY_QUERYSTRUCTURE;
            
            if(ownerOid != -1) {
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, false);
                
                affectedProperties += " " + CompactQuery.PROPERTY_IS_PUBLIC;
                newValues += " " + "false";
                
                Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, ownerOid);
                if(userNode == null)
                    throw new ApplicationObjectNotFoundException(String.format(
                                "Can not find the query with id %s", queryOid));

                Relationship singleRelationship = queryNode.getSingleRelationship(RelTypes.OWNS_QUERY, Direction.INCOMING);

                if(singleRelationship == null)
                    userNode.createRelationshipTo(queryNode, RelTypes.OWNS_QUERY);
            }
            else {
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, true);
                
                affectedProperties += " " + CompactQuery.PROPERTY_IS_PUBLIC;
                newValues += " " + "true";
            }
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, notes);
        }
    }

    @Override
    public void deleteQuery(long queryOid) throws ApplicationObjectNotFoundException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx())
        {
            Node queryNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), queryLabel, queryOid);
            if(queryNode == null)
                throw new ApplicationObjectNotFoundException(String.format(
                        "Can not find the query with id %1s", queryOid));

            Iterable<Relationship> relationships = queryNode.getRelationships(RelTypes.OWNS_QUERY, Direction.INCOMING);
            for (Relationship relationship : relationships) {
                relationship.delete();
            }
            queryNode.delete();
            tx.success();
        }
    }

    @Override
    public List<CompactQuery> getQueries(boolean showPublic) {
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx())
        {
            List<CompactQuery> queryList = new ArrayList<>();
            ResourceIterator<Node> queries = connectionManager.getConnectionHandler().findNodes(queryLabel);
            while (queries.hasNext()) {
                Node queryNode = queries.next();
                
                CompactQuery cq =  new CompactQuery();
                cq.setName((String)queryNode.getProperty(CompactQuery.PROPERTY_QUERYNAME));
                cq.setDescription((String)queryNode.getProperty(CompactQuery.PROPERTY_DESCRIPTION));
                cq.setContent((byte[])queryNode.getProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE));
                cq.setIsPublic((Boolean)queryNode.getProperty(CompactQuery.PROPERTY_IS_PUBLIC));
                cq.setId(queryNode.getId());

                Relationship ownRelationship = queryNode.getSingleRelationship(RelTypes.OWNS_QUERY, Direction.INCOMING);

                if(ownRelationship != null) {
                    Node ownerNode =  ownRelationship.getStartNode();
                    cq.setOwnerId(ownerNode.getId());
                }
                queryList.add(cq);
            }//end for
            return queryList;
        }
    }

    @Override
    public CompactQuery getQuery(long queryOid)
            throws ApplicationObjectNotFoundException {
        
        CompactQuery cq =  new CompactQuery();

        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node queryNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), queryLabel, queryOid);
            if (queryNode == null)
                 throw new ApplicationObjectNotFoundException(String.format(
                            "Can not find the query with id %s", queryOid));

            cq.setName((String)queryNode.getProperty(CompactQuery.PROPERTY_QUERYNAME));
            if(queryNode.hasProperty(CompactQuery.PROPERTY_DESCRIPTION))
                cq.setDescription((String)queryNode.getProperty(CompactQuery.PROPERTY_DESCRIPTION));
            
            cq.setContent((byte[])queryNode.getProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE));
            cq.setIsPublic((Boolean)queryNode.getProperty(CompactQuery.PROPERTY_IS_PUBLIC));
            cq.setId(queryNode.getId());

            Relationship ownRelationship = queryNode.getSingleRelationship(RelTypes.OWNS_QUERY, Direction.INCOMING);

            if(ownRelationship != null) {
                Node ownerNode =  ownRelationship.getStartNode();
                cq.setOwnerId(ownerNode.getId());
            }
            return cq;
        }
    }

    @Override
    public List<ResultRecord> executeQuery(ExtendedQuery query) throws MetadataObjectNotFoundException, InvalidArgumentException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            CypherQueryBuilder cqb = new CypherQueryBuilder();
            cqb.setClassNodes(getNodesFromQuery(query));
            cqb.createQuery(query);
            return cqb.getResultList();
        }
    }
    
    @Override
    public byte[] getClassHierachy(boolean showAll) 
            throws MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameHierarchy = new QName("hierarchy");
            xmlew.add(xmlef.createStartElement(qnameHierarchy, null, null));
            xmlew.add(xmlef.createAttribute(new QName("documentVersion"), Constants.CLASS_HIERARCHY_NEXT_DOCUMENT_VERSION));
            xmlew.add(xmlef.createAttribute(new QName("serverVersion"), Constants.PERSISTENCE_SERVICE_VERSION));
            xmlew.add(xmlef.createAttribute(new QName("date"), Long.toString(Calendar.getInstance().getTimeInMillis())));
            
            QName qnameInventory = new QName("inventory");
            xmlew.add(xmlef.createStartElement(qnameInventory, null, null));
            
            QName qnameClasses = new QName("classes");
            xmlew.add(xmlef.createStartElement(qnameClasses, null, null));
            
            Node rootObjectNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, Constants.CLASS_ROOTOBJECT);
            if (rootObjectNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", Constants.CLASS_ROOTOBJECT));
            getXMLNodeForClass(rootObjectNode, xmlew, xmlef);
            
            xmlew.add(xmlef.createEndElement(qnameClasses, null));
            
            xmlew.add(xmlef.createEndElement(qnameInventory, null));
            
            xmlew.add(xmlef.createEndElement(qnameHierarchy, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            Logger.getLogger(ApplicationEntityManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    //Pools
    @Override
    public String createRootPool(String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node poolNode = connectionManager.getConnectionHandler().createNode(poolLabel);
            String uuid = UUID.randomUUID().toString();
            poolNode.setProperty(Constants.PROPERTY_UUID, uuid);

            if (name != null)
                poolNode.setProperty(Constants.PROPERTY_NAME, name);
            if (description != null)
                poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);

            poolNode.setProperty(Constants.PROPERTY_TYPE, type);
            
            ClassMetadata classMetadata = cm.getClass(instancesOfClass);
            
            if (classMetadata == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", instancesOfClass));
            
            poolNode.setProperty(Constants.PROPERTY_CLASSNAME, instancesOfClass);
                                                
            tx.success();
            return uuid;
        }
    }
    
    @Override
    public String createPoolInObject(String parentClassname, String parentId, String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node poolNode =  connectionManager.getConnectionHandler().createNode(poolLabel);
            String uuid = UUID.randomUUID().toString();
            poolNode.setProperty(Constants.PROPERTY_UUID, uuid);

            if (name != null)
                poolNode.setProperty(Constants.PROPERTY_NAME, name);
            if (description != null)
                poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            if (type != 0)
                    poolNode.setProperty(Constants.PROPERTY_TYPE, type);
            
            ClassMetadata classMetadata = cm.getClass(instancesOfClass);
            
            if (classMetadata == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", instancesOfClass));
            
            poolNode.setProperty(Constants.PROPERTY_CLASSNAME, instancesOfClass);
            
            Node parentNode = connectionManager.getConnectionHandler().findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, parentId);
            if (parentNode == null)
                throw new BusinessObjectNotFoundException(parentClassname, parentId);
            
            poolNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);          
            
            tx.success();
            return uuid;
        }
    }
    
    @Override
    public String createPoolInPool(String parentId, String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node poolNode =  connectionManager.getConnectionHandler().createNode(poolLabel);
            String uuid = UUID.randomUUID().toString();
            poolNode.setProperty(Constants.PROPERTY_UUID, uuid);

            if (name != null)
                poolNode.setProperty(Constants.PROPERTY_NAME, name);
            if (description != null)
                poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            if (type != 0)
                    poolNode.setProperty(Constants.PROPERTY_TYPE, type);
            
            ClassMetadata classMetadata = cm.getClass(instancesOfClass);
            
            if (classMetadata == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", instancesOfClass));
            
            poolNode.setProperty(Constants.PROPERTY_CLASSNAME, instancesOfClass);
            
            Node parentNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, parentId);
            
            if (parentNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A pool with id %s could not be found", parentId));
            
            poolNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);          
                        
            tx.success();
            return uuid;
        }
    }
        
    private void deletePool(String id) throws ApplicationObjectNotFoundException, OperationNotPermittedException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node poolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, id);
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A pool with id %s does not exist", id));

            deletePool(poolNode);
            
            tx.success();
        }
    }
    
    @Override
    public void deletePools(String[] ids) throws ApplicationObjectNotFoundException, OperationNotPermittedException {
        for (String id : ids)
            deletePool(id);
    }
    
    @Override
    public ChangeDescriptor setPoolProperties(String poolId, String name, String description) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node poolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, poolId);
            String affectedProperties = "", oldValues = "", newValues = "";
            
            if(name != null) {
                oldValues += " " + (poolNode.hasProperty(Constants.PROPERTY_NAME) ? poolNode.getProperty(Constants.PROPERTY_NAME) : " ");
                poolNode.setProperty(Constants.PROPERTY_NAME, name);
                affectedProperties += " " + Constants.PROPERTY_NAME;                
                newValues += " " + name;   
            }
            if(description != null) {
                oldValues += " " + (poolNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? poolNode.getProperty(Constants.PROPERTY_DESCRIPTION) : " ");
                poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
                affectedProperties += " " + Constants.PROPERTY_DESCRIPTION;                
                newValues += " " + description;                
            }
            
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, String.format("Set %s pool properties", name));
        }
    }
    
    @Override
    public List<ActivityLogEntry> getBusinessObjectAuditTrail(String objectClass, String objectId, int limit) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, objectClass))
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass"),
                        objectClass, Constants.CLASS_INVENTORYOBJECT));
            Node instanceNode = getInstanceOfClass(objectClass, objectId);
            List<ActivityLogEntry> log = new ArrayList<>();
            int i = 0;
            for (Relationship rel : instanceNode.getRelationships(RelTypes.HAS_HISTORY_ENTRY)) {
                if (limit > 0) {
                    if (i < limit)
                        i++;
                    else
                        break;
                }
                Node logEntry = rel.getEndNode();
                log.add(new ActivityLogEntry(logEntry.getId(), instanceNode.getId(), (Integer)logEntry.getProperty(Constants.PROPERTY_TYPE), 
                        (String)logEntry.getSingleRelationship(RelTypes.PERFORMED_BY, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME), 
                        (Long)logEntry.getProperty(Constants.PROPERTY_CREATION_DATE), 
                        logEntry.hasProperty(Constants.PROPERTY_AFFECTED_PROPERTY) ? (String)logEntry.getProperty(Constants.PROPERTY_AFFECTED_PROPERTY) : null, 
                        logEntry.hasProperty(Constants.PROPERTY_OLD_VALUE) ? (String)logEntry.getProperty(Constants.PROPERTY_OLD_VALUE) :  null, 
                        logEntry.hasProperty(Constants.PROPERTY_NEW_VALUE) ? (String)logEntry.getProperty(Constants.PROPERTY_NEW_VALUE) : null, 
                        logEntry.hasProperty(Constants.PROPERTY_NOTES) ? (String)logEntry.getProperty(Constants.PROPERTY_NOTES) : null));
            }
            return log;
        }
    }
    
    @Override
    public long getGeneralActivityAuditTrailCount(int page, int limit, HashMap<String, Object> filters) {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> parameters = new HashMap<>();
            
            String userFilter;
            if (filters != null && filters.containsKey("user")) {
                userFilter = "user.name = {userName}";
                parameters.put("userName", filters.get("user"));
            } else
                userFilter = "true";
            
            String typeFilter;
            if (filters != null && filters.containsKey("type")) {
                typeFilter = "auditTrailEntry.type = {type}";
                parameters.put("type", filters.get("type"));
            } else
                typeFilter = "true";
            
            String query = "MATCH (auditTrailEntry:generalActivityLogs)-[:PERFORMED_BY]->(user) "
                    + ((filters == null || filters.isEmpty()) ? "" : "WHERE " + userFilter + " AND " + typeFilter)
                    + " RETURN count(auditTrailEntry) AS count";
            
            Result result = connectionManager.getConnectionHandler().execute(query, parameters);
                        
            while (result.hasNext()) {
                tx.success();
                return (long) result.next().get("count");
            }
            return 0;
        }
    }
    
    @Override
    public List<ActivityLogEntry> getGeneralActivityAuditTrail(int page, int limit, HashMap<String, Object> filters) {        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> parameters = new HashMap<>();
            
            String userFilter;
            if (filters != null && filters.containsKey("user")) {
                userFilter = "user.name = {userName}";
                parameters.put("userName", filters.get("user"));
            } else
                userFilter = "true";
            
            String typeFilter;
            if (filters != null && filters.containsKey("type")) {
                typeFilter = "auditTrailEntry.type = {type}";
                parameters.put("type", filters.get("type"));
            } else
                typeFilter = "true";
            
            String query = "MATCH (auditTrailEntry:generalActivityLogs)-[:PERFORMED_BY]->(user) "
                    + ((filters == null || filters.isEmpty()) ? "" : "WHERE " + userFilter + " AND " + typeFilter)
                    + " RETURN auditTrailEntry, user"
                    + " ORDER BY auditTrailEntry.creationDate DESC"
                    + (page >= 0 && limit >= 0 ? " SKIP " + page + " LIMIT " + limit : "");
            
            Result result = connectionManager.getConnectionHandler().execute(query, parameters);
            
            List<ActivityLogEntry> log = new ArrayList<>();
            while (result.hasNext()) {
                Map<String, Object> resultEntry = result.next();
                Node logEntry = (Node)resultEntry.get("auditTrailEntry");
                Node user = (Node)resultEntry.get("user");
                
                log.add(new ActivityLogEntry(logEntry.getId(), 0, (Integer)logEntry.getProperty(Constants.PROPERTY_TYPE), 
                        (String)user.getProperty(Constants.PROPERTY_NAME), 
                        (Long)logEntry.getProperty(Constants.PROPERTY_CREATION_DATE), 
                        logEntry.hasProperty(Constants.PROPERTY_AFFECTED_PROPERTY) ? (String)logEntry.getProperty(Constants.PROPERTY_AFFECTED_PROPERTY) : null, 
                        logEntry.hasProperty(Constants.PROPERTY_OLD_VALUE) ? (String)logEntry.getProperty(Constants.PROPERTY_OLD_VALUE) :  null, 
                        logEntry.hasProperty(Constants.PROPERTY_NEW_VALUE) ? (String)logEntry.getProperty(Constants.PROPERTY_NEW_VALUE) : null, 
                        logEntry.hasProperty(Constants.PROPERTY_NOTES) ? (String)logEntry.getProperty(Constants.PROPERTY_NOTES) : null));
            }
            
            tx.success();
            return log;
        }
    }
    
    @Override
    public void validateCall(String methodName, String ipAddress, String sessionId)
            throws NotAuthorizedException {
        Session aSession = sessions.get(sessionId);
        
        if(aSession == null) {
            NotAuthorizedException ex = new NotAuthorizedException(ts.getTranslatedString(KEY_PREFIX + ".17"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(17);
            throw ex;
        }

//        We won't be using this for now, since the desktop client still uses the web service. This will work as of version 2.0        
//        if (aSession.getUser().getType() != UserProfile.USER_TYPE_WEB_SERVICE)
//            throw new NotAuthorizedException(String.format("The user %s is not authorized to call web service methods", aSession.getUser().getUserName()));
        
//        for (Privilege privilege : aSession.getUser().getPrivileges()) { //The featureToken for web service users is the method name itself
//            if (methodName.equals(privilege.getFeatureToken()))
//                return;
//        }
//                
//        throw new NotAuthorizedException(String.format("The user %s is not authorized to call web service method %s", aSession.getUser().getUserName(), methodName));
            
    }
    
    @Override
    public Session createSession(String userName, String password, int sessionType) throws ApplicationObjectNotFoundException, NotAuthorizedException {
        if (userName == null || password == null) {
            ApplicationObjectNotFoundException ex = new ApplicationObjectNotFoundException(ts.getTranslatedString("api.aem.error.200001"));
            ex.setPrefix("api.aem.error");
            ex.setCode(200001);
            throw ex;
        }
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node userNode = connectionManager.getConnectionHandler().findNode(userLabel, Constants.PROPERTY_NAME, userName);

            if (userNode == null) {
                ApplicationObjectNotFoundException ex = new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("api.aem.error.200002"), userName));
                ex.setPrefix("api.aem.error");
                ex.setCode(200002);
                ex.setMessageArgs(userName);
                throw ex;
            }

            if (userNode.hasProperty(UserProfile.PROPERTY_TYPE) && (int)userNode.getProperty(UserProfile.PROPERTY_TYPE) == UserProfile.USER_TYPE_SYSTEM) {
                NotAuthorizedException ex = new NotAuthorizedException(ts.getTranslatedString("api.aem.error.200003"));
                ex.setPrefix("api.aem.error");
                ex.setCode(200003);
                throw ex;
            }
            
            if (!(Boolean)userNode.getProperty(Constants.PROPERTY_ENABLED)) {
                NotAuthorizedException ex = new NotAuthorizedException(String.format(ts.getTranslatedString("api.aem.error.200004"), userName));
                ex.setPrefix("api.aem.error");
                ex.setCode(200004);
                ex.setMessageArgs(userName);
                throw ex;
            }

            if (passwordEnconder.matches(password, (String)userNode.getProperty(Constants.PROPERTY_PASSWORD))) {
                UserProfile user = Util.createUserProfileWithGroupPrivilegesFromNode(userNode);

                for (Session aSession : sessions.values()) {
                    if (aSession.getUser().getUserName().equals(userName) 
                            && aSession.getSessionType() == sessionType) { //Multiple sessions withe the same user are allowed as long as they have a different type (e.g. one mobile session and the other web session)
                        sessions.remove(aSession.getToken());
                        break;
                    }
                }
                
                Session newSession = new Session(user, sessionType);
                sessions.put(newSession.getToken(), newSession);
                cm.putUser(user);
                
                createGeneralActivityLogEntry(user.getUserName(), ActivityLogEntry.ACTIVITY_TYPE_OPEN_SESSION, 
                        "Session created");
                
                return newSession;
            } else {
                NotAuthorizedException ex = new NotAuthorizedException(ts.getTranslatedString("api.aem.error.200005"));
                ex.setPrefix("api.aem.error");
                ex.setCode(200005);
                throw ex;
            }
        }
    }
    
    @Override
    public UserProfile getUserInSession(String sessionId) {
        return sessions.get(sessionId).getUser();
    }
    
    @Override
    public List<UserProfile> getUsersInGroup(long groupId) throws ApplicationObjectNotFoundException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node groupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), groupLabel, groupId);
            if (groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Group with id %s could not be found", groupId));

            List<UserProfile> usersInGroup = new ArrayList<>();
            for (Relationship userInGroupRelationship : groupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP))
                usersInGroup.add(Util.createUserProfileWithoutGroupPrivilegesFromNode(userInGroupRelationship.getStartNode()));

            return usersInGroup;
        }
    }
    
    @Override
    public List<GroupProfileLight> getGroupsForUser(long userId) throws ApplicationObjectNotFoundException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, userId);
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("User with id %s could not be found", userId));

            List<GroupProfileLight> groupsForUser = new ArrayList<>();
            for (Relationship groupForUserRelationship : userNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP))
                groupsForUser.add(Util.createGroupProfileLightFromNode(groupForUserRelationship.getEndNode()));

            return groupsForUser;
        }
    }

    @Override
    public void closeSession(String sessionId) throws NotAuthorizedException {
        Session aSession = sessions.get(sessionId);
        if (aSession == null) {
            NotAuthorizedException ex = new NotAuthorizedException(ts.getTranslatedString("api.aem.error.200007"));
            ex.setPrefix("api.aem.error");
            ex.setCode(200007);
            throw ex;
        }
        sessions.remove(sessionId);
    }
            
    @Override
    public void setConfiguration (Properties properties) {
        this.configuration = properties;
    }

    @Override
    public Properties getConfiguration () {
        return this.configuration;
    }

    @Override
    public void createGeneralActivityLogEntry(String userName, int type, String notes) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Util.createActivityLogEntry(null, userName, type,
                    Calendar.getInstance().getTimeInMillis(), null, null, null, notes, connectionManager.getConnectionHandler(), ts);
            tx.success();        
        }
    }
    
    @Override
    public void createGeneralActivityLogEntry(String userName, int type, ChangeDescriptor changeDescriptor) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Util.createActivityLogEntry(null, userName, type,
                    Calendar.getInstance().getTimeInMillis(), changeDescriptor.getAffectedProperties(), 
                    changeDescriptor.getOldValues(), changeDescriptor.getNewValues(), changeDescriptor.getNotes(), connectionManager.getConnectionHandler(), ts);

            tx.success();  
        }
    }
    
    @Override
    public long createObjectActivityLogEntry(String userName, String className, String oid, int type, 
        String affectedProperties, String oldValues, String newValues, String notes) throws ApplicationObjectNotFoundException, BusinessObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = connectionManager.getConnectionHandler().findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, oid);
            
            if (objectNode == null)
                throw new BusinessObjectNotFoundException(className, oid);
            
            Node activityLogEntryNode = Util.createActivityLogEntry(objectNode, userName, type, Calendar.getInstance().getTimeInMillis(), 
                    affectedProperties, oldValues, newValues, notes, connectionManager.getConnectionHandler(), ts);

            tx.success();
            return activityLogEntryNode.getId();
        }
    }
    
    @Override
    public void createObjectActivityLogEntry(String userName, String className, String oid,  
            int type, ChangeDescriptor changeDescriptor) throws ApplicationObjectNotFoundException, BusinessObjectNotFoundException {
        createObjectActivityLogEntry(userName, className, oid, type, changeDescriptor.getAffectedProperties(), changeDescriptor.getOldValues(), changeDescriptor.getNewValues(), changeDescriptor.getNotes());
    }
    
    // <editor-fold defaultstate="collapsed" desc="Task Manager">
    @Override
    public long createTask(String name, String description, boolean enabled, boolean commitOnExecute, String script, 
            List<StringPair> parameters, TaskScheduleDescriptor schedule, TaskNotificationDescriptor notificationType) {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node taskNode = connectionManager.getConnectionHandler().createNode(taskLabel);
            taskNode.setProperty(Constants.PROPERTY_NAME, name == null ? "" : name);
            taskNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);
            taskNode.setProperty(Constants.PROPERTY_COMMIT_ON_EXECUTE, commitOnExecute);
            taskNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
            
            if (script != null)
                taskNode.setProperty(Constants.PROPERTY_SCRIPT, script);
            
            for (StringPair parameter : parameters) //param names have the prefic PARAM_ to identify them from the other properties
                taskNode.setProperty("PARAM_" + parameter.getKey(), parameter.getValue());
            
            if (schedule != null) {
                taskNode.setProperty(Constants.PROPERTY_EXECUTION_TYPE, schedule.getExecutionType());
                taskNode.setProperty(Constants.PROPERTY_EVERY_X_MINUTES, schedule.getEveryXMinutes());
                taskNode.setProperty(Constants.PROPERTY_START_TIME, schedule.getStartTime());
            }
            
            if (notificationType != null) {
                taskNode.setProperty(Constants.PROPERTY_NOTIFICATION_TYPE, notificationType.getNotificationType());
                taskNode.setProperty(Constants.PROPERTY_EMAIL, notificationType.getEmail() ==  null ? "" : notificationType.getEmail());
            }
                        
            tx.success();
            return taskNode.getId();
        }
    }

    @Override
    public ChangeDescriptor updateTaskProperties(long taskId, String propertyName, String propertyValue) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-not-found"), taskId));
            String affectedProperty, oldValue, newValue;
            affectedProperty = propertyName;
            oldValue = String.valueOf(taskNode.hasProperty(propertyName) ? taskNode.getProperty(propertyName) : " ");

            switch (propertyName) {
                case Constants.PROPERTY_NAME:
                case Constants.PROPERTY_DESCRIPTION:
                case Constants.PROPERTY_SCRIPT:
                    taskNode.setProperty(propertyName, propertyValue);
                    break;
                case Constants.PROPERTY_ENABLED:
                case Constants.PROPERTY_COMMIT_ON_EXECUTE:
                    taskNode.setProperty(propertyName, Boolean.valueOf(propertyValue));
                    break;
                default:
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-property-not-valid"), propertyName));
            }
            String taskName = taskNode.hasProperty(Constants.PROPERTY_NAME) ? (String) taskNode.getProperty(Constants.PROPERTY_NAME) : " ";
            newValue = propertyValue;
            tx.success();
            return new ChangeDescriptor(affectedProperty, oldValue, newValue, 
                String.format(ts.getTranslatedString("module.taskman.task.actions.update-task-properties-success"), taskName, taskId));
        }
    }

    @Override
    public ChangeDescriptor updateTaskParameters(long taskId, List<StringPair> parameters) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-not-found"), taskId));
            String affectedProperties = "", oldValues = "", newValues = "";

            for (StringPair parameter : parameters) {
                String actualParameterName = "PARAM_" + parameter.getKey();
                //The parameters are stored with a prefix PARAM_
                //params set to null, must be deleted
                if (taskNode.hasProperty(actualParameterName) && parameter.getValue() == null)
                    taskNode.removeProperty(actualParameterName);
                else {                    
                    oldValues += " " + (taskNode.hasProperty(actualParameterName) ? taskNode.getProperty(actualParameterName) : " ");
                    
                    taskNode.setProperty(actualParameterName, parameter.getValue());
                    
                    affectedProperties += " " + parameter.getKey();
                    newValues += " " + parameter.getValue();
                }
            }
            String taskName = taskNode.hasProperty(Constants.PROPERTY_NAME) ? (String) taskNode.getProperty(Constants.PROPERTY_NAME) : " ";
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, 
                String.format(ts.getTranslatedString("module.taskman.task.actions.update-task-parameter-success"), taskName, taskId));
        }
    }

    @Override
    public ChangeDescriptor updateTaskSchedule(long taskId, TaskScheduleDescriptor schedule) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-not-found"), taskId));
            String affectedProperties = "", oldValues = "", newValues = "";
            
            affectedProperties += " " + Constants.PROPERTY_EXECUTION_TYPE;
            oldValues += " " + (taskNode.hasProperty(Constants.PROPERTY_EXECUTION_TYPE) ? taskNode.getProperty(Constants.PROPERTY_EXECUTION_TYPE) : " ");
            taskNode.setProperty(Constants.PROPERTY_EXECUTION_TYPE, schedule.getExecutionType());
            newValues += " " + schedule.getExecutionType();
            
            affectedProperties += " " + Constants.PROPERTY_EVERY_X_MINUTES;
            oldValues += " " + (taskNode.hasProperty(Constants.PROPERTY_EVERY_X_MINUTES) ? taskNode.getProperty(Constants.PROPERTY_EVERY_X_MINUTES) : " ");
            taskNode.setProperty(Constants.PROPERTY_EVERY_X_MINUTES, schedule.getEveryXMinutes());
            newValues += " " + schedule.getEveryXMinutes();
            
            affectedProperties += " " + Constants.PROPERTY_START_TIME;
            oldValues += " " + (taskNode.hasProperty(Constants.PROPERTY_START_TIME) ? taskNode.getProperty(Constants.PROPERTY_START_TIME) : " ");
            taskNode.setProperty(Constants.PROPERTY_START_TIME, schedule.getStartTime());
            newValues += " " + schedule.getStartTime();
            
            String taskName = taskNode.hasProperty(Constants.PROPERTY_NAME) ? (String) taskNode.getProperty(Constants.PROPERTY_NAME) : " ";
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, 
                String.format(ts.getTranslatedString("module.taskman.task.actions.update-task-schedule-success"), taskName, taskId));
        }
    }

    @Override
    public ChangeDescriptor updateTaskNotificationType(long taskId, TaskNotificationDescriptor notificationType) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-not-found"), taskId));
            String affectedProperties = "", oldValues = "", newValues = "";
            
            affectedProperties += " " + Constants.PROPERTY_NOTIFICATION_TYPE;
            oldValues += " " + (taskNode.hasProperty(Constants.PROPERTY_NOTIFICATION_TYPE) ? taskNode.getProperty(Constants.PROPERTY_NOTIFICATION_TYPE) : " ");
            taskNode.setProperty(Constants.PROPERTY_NOTIFICATION_TYPE, notificationType.getNotificationType());
            newValues += " " + notificationType.getNotificationType();
            
            affectedProperties += " " + Constants.PROPERTY_EMAIL;
            oldValues += " " + (taskNode.hasProperty(Constants.PROPERTY_EMAIL) ? taskNode.getProperty(Constants.PROPERTY_EMAIL) : " ");
            taskNode.setProperty(Constants.PROPERTY_EMAIL, notificationType.getEmail() == null ? "" : notificationType.getEmail());
            newValues += " " + notificationType.getEmail() == null ? "" : notificationType.getEmail();
            
            String taskName = taskNode.hasProperty(Constants.PROPERTY_NAME) ? (String) taskNode.getProperty(Constants.PROPERTY_NAME) : " ";
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, 
                String.format(ts.getTranslatedString("module.taskman.task.actions.update-task-notification-success"), taskName, taskId));
        }
    }

    @Override
    public void deleteTask(long taskId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-not-found"), taskId));
                        
            for (Relationship rel : taskNode.getRelationships()) //Unsubscribe users
                rel.delete();
            
            taskNode.delete();
            tx.success();
        }
    }

    @Override
    public ChangeDescriptor subscribeUserToTask(long userId, long taskId) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-not-found"), taskId));
            
            boolean found = false;
            
            for (Relationship rel : taskNode.getRelationships(Direction.INCOMING, RelTypes.SUBSCRIBED_TO)) {
                if (rel.getStartNode().getId() == userId) {
                    found = true;
                    break;
                }
            }
            
            if (found)
                throw new InvalidArgumentException(ts.getTranslatedString("module.taskman.task.actions.new-task-user-error"));
                        
            Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, userId);
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-user-not-found"), userId));
            
            Relationship rel = userNode.createRelationshipTo(taskNode, RelTypes.SUBSCRIBED_TO);
            rel.setProperty(Constants.PROPERTY_NAME, "task"); //NOI18N
            
            String taskName = taskNode.hasProperty(Constants.PROPERTY_NAME) ? (String) taskNode.getProperty(Constants.PROPERTY_NAME) : "";
            String userName = userNode.hasProperty(Constants.PROPERTY_NAME) ? (String) userNode.getProperty(Constants.PROPERTY_NAME) : "";
            tx.success();
            
            return new ChangeDescriptor("", "", "", String.format(ts.getTranslatedString("module.taskman.task.actions.new-task-user-log"),
                     userName, taskName));
        }
    }

    @Override
    public ChangeDescriptor unsubscribeUserFromTask(long userId, long taskId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), taskLabel, taskId);
            
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-not-found"), taskId));
            
            boolean found = false;
            String userName = null;
            
            for (Relationship rel : taskNode.getRelationships(Direction.INCOMING, RelTypes.SUBSCRIBED_TO)) {
                if (rel.getStartNode().getId() == userId) {
                    userName = rel.getStartNode().hasProperty(Constants.PROPERTY_NAME) ? (String) rel.getStartNode().getProperty(Constants.PROPERTY_NAME) : "";
                    rel.delete();
                    found = true;
                    break;
                }
            }
            
            if (!found)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-user-not-found"),
                        taskId));
            
            String taskName = taskNode.hasProperty(Constants.PROPERTY_NAME) ? (String) taskNode.getProperty(Constants.PROPERTY_NAME) : "";            
            tx.success();
            
            return new ChangeDescriptor("", "", "",
                    String.format(ts.getTranslatedString("module.taskman.task.actions.delete-task-user-log"), userName, taskName));
        }
    }

    @Override
    public Task getTask(long taskId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node taskNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-not-found"), taskId));
                        
            return Util.createTaskFromNode(taskNode);
        }
    }
    
    @Override
    public List<UserProfileLight> getSubscribersForTask(long taskId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), taskLabel, taskId);
            
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-not-found"), taskId));
                
            List<UserProfileLight> subscribers = new ArrayList<>();
            for (Relationship rel : taskNode.getRelationships(Direction.INCOMING, RelTypes.SUBSCRIBED_TO)) {
                Node userNode = rel.getStartNode();
                subscribers.add(new UserProfileLight(userNode.getId(), (String)userNode.getProperty(Constants.PROPERTY_NAME)));
            }
            
            return subscribers;
        }
    }

    @Override
    public List<Task> getTasks() {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            ResourceIterator<Node> taskNodes = connectionManager.getConnectionHandler().findNodes(taskLabel);
            List<Task> allTasks = new ArrayList<>();
            
            while (taskNodes.hasNext()) {
                Node taskNode = taskNodes.next();
                allTasks.add(Util.createTaskFromNode(taskNode));
            }
            return allTasks;
        }
    }

    @Override
    public List<Task> getTasksForUser(long userId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, userId);
            
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-user-not-found"), userId));
            
            List<Task> allTasks = new ArrayList<>();
            
            for (Relationship rel : userNode.getRelationships(Direction.OUTGOING, RelTypes.SUBSCRIBED_TO)) {
                Node taskNode = rel.getEndNode();

                allTasks.add(Util.createTaskFromNode(taskNode));
            }
            
            return allTasks;
        }
    }
    
    @Override
    public TaskResult executeTask(long taskId) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        Binding environmentParameters = new Binding();
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), taskLabel, taskId);
            
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-not-found"), taskId));
            if (!taskNode.hasProperty(Constants.PROPERTY_SCRIPT))
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.taskman.task.actions.task-script-not-found"), taskId));
            
            if (!(boolean)taskNode.getProperty(Constants.PROPERTY_ENABLED))
                throw new InvalidArgumentException(ts.getTranslatedString("module.taskman.task.actions.task-disabled"));
            
            String script = (String)taskNode.getProperty(Constants.PROPERTY_SCRIPT);
            
            Iterable<String> allProperties = taskNode.getPropertyKeys();
            HashMap<String, String> scriptParameters = new HashMap<>();
            for (String property : allProperties) {
                if (property.startsWith("PARAM_"))
                    scriptParameters.put(property.replace("PARAM_", ""), (String)taskNode.getProperty(property));
            }
            environmentParameters.setVariable("aem", this); //NOI18N
            environmentParameters.setVariable("bem", bem); //NOI18N
            environmentParameters.setVariable("mem", mem); //NOI18N
            environmentParameters.setVariable("connectionHandler", connectionManager.getConnectionHandler()); //NOI18N
            environmentParameters.setVariable("scriptParameters", scriptParameters); //NOI18N
         
            GroovyShell shell = new GroovyShell(ApplicationEntityManager.class.getClassLoader(), environmentParameters);
            Object theResult = shell.evaluate(script);

            if (theResult == null)
                throw new InvalidArgumentException(ts.getTranslatedString("module.taskman.task.actions.task-script-result-null-object"));
            else if (!TaskResult.class.isInstance(theResult))
                throw new InvalidArgumentException(ts.getTranslatedString("module.taskman.task.actions.task-script-result-error"));
            //Commit only if it's configured to do so 
            if (taskNode.hasProperty(Constants.PROPERTY_COMMIT_ON_EXECUTE) && (boolean)taskNode.getProperty(Constants.PROPERTY_COMMIT_ON_EXECUTE))
                tx.success();

            return (TaskResult)theResult;

        } catch(Exception ex) {
            return TaskResult.createErrorResult(ex.getMessage());
        }
    }
    // </editor-fold>
    
    //Templates
    @Override
    public String createTemplate(String templateClass, String templateName) throws MetadataObjectNotFoundException, OperationNotPermittedException {  
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, templateClass);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", templateClass));
            
            if (classNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
                throw new OperationNotPermittedException(String.format("Abstract class %s can not have templates", templateClass));
            
            String uuid = UUID.randomUUID().toString();
            Node templateNode = connectionManager.getConnectionHandler().createNode(templateLabel, templateElementLabel); // The template root object is also a template element
            templateNode.setProperty(Constants.PROPERTY_NAME, templateName == null ? "" : templateName);
            templateNode.setProperty(Constants.PROPERTY_UUID, uuid);
            templateNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis()); //The default value is right now
                        
            classNode.createRelationshipTo(templateNode, RelTypes.HAS_TEMPLATE);
            Relationship specialInstanceRelationship = templateNode.createRelationshipTo(classNode, RelTypes.INSTANCE_OF_SPECIAL);
            specialInstanceRelationship.setProperty(Constants.PROPERTY_NAME, "template"); //NOI18N
            
            tx.success();
            return uuid;
        }
    }

    @Override
    public String createTemplateElement(String templateElementClass, String templateElementParentClassName, String templateElementParentId, String templateElementName) throws 
        MetadataObjectNotFoundException, ApplicationObjectNotFoundException, OperationNotPermittedException {
        
        boolean isPossibleChildren = false;
        for (ClassMetadataLight possibleChildren : mem.getPossibleChildren(templateElementParentClassName, true)) {
            if (possibleChildren.getName().equals(templateElementClass)) {
                isPossibleChildren = true;
                break;
            }
        }
        if (!isPossibleChildren) 
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", templateElementClass, templateElementParentClassName == null ? Constants.NODE_DUMMYROOT : templateElementParentClassName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, templateElementClass);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", templateElementClass));
            
            Node parentClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, templateElementParentClassName);
            if (parentClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Parent class %s can not be found", templateElementParentClassName));
            
            if (classNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", templateElementClass));
            
            Node parentNode = null;
            
            for(Relationship instanceOfSpecialRelationship : parentClassNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF_SPECIAL)) {
                if (templateElementParentId.equals(instanceOfSpecialRelationship.getStartNode().getProperty(Constants.PROPERTY_UUID))) {
                    parentNode = instanceOfSpecialRelationship.getStartNode();
                    break;
                }
            }
            
            if (parentNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Parent object %s of class %s not found", templateElementParentId, templateElementParentClassName));
            
            String uuid = UUID.randomUUID().toString();
            Node templateObjectNode = connectionManager.getConnectionHandler().createNode(templateElementLabel);
            templateObjectNode.setProperty(Constants.PROPERTY_NAME, templateElementName == null ? "" : templateElementName);
            templateObjectNode.setProperty(Constants.PROPERTY_UUID, uuid);
            templateObjectNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis()); //The default value is right now
            
            templateObjectNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF);
            
            Relationship specialInstanceRelationship = templateObjectNode.createRelationshipTo(classNode, RelTypes.INSTANCE_OF_SPECIAL);
            specialInstanceRelationship.setProperty(Constants.PROPERTY_NAME, "template"); //NOI18N 
            
            tx.success();
            return uuid;
        }
    }
    
    @Override    
    public String createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, String tsElementParentId, String tsElementName) 
        throws OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        
        boolean isPossibleSpecialChildren = false;
        for (ClassMetadataLight  possibleSpecialChildren : mem.getPossibleSpecialChildren(tsElementParentClassName)) {
            if (possibleSpecialChildren.getName().equals(tsElementClass)) {
                isPossibleSpecialChildren = true;
                break;
            }
        }
        if (!isPossibleSpecialChildren)
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as special child of %s", tsElementClass, tsElementParentClassName == null ? Constants.NODE_DUMMYROOT : tsElementParentClassName));
            
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, tsElementClass);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", tsElementClass));
                        
            Node parentClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, tsElementParentClassName);
            if (parentClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Parent class %s can not be found", tsElementParentClassName));
            
            if (classNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", tsElementClass));
            
            Node parentNode = null;
            
            for(Relationship instanceOfSpecialRelationship : parentClassNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF_SPECIAL)) {
                if (tsElementParentId.equals(instanceOfSpecialRelationship.getStartNode().getProperty(Constants.PROPERTY_UUID))) {
                    parentNode = instanceOfSpecialRelationship.getStartNode();
                    break;
                }
            }
            
            if (parentNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Parent object %s of class %s not found", tsElementParentId, tsElementParentClassName));
            
            String uuid = UUID.randomUUID().toString();
            Node templateObjectNode = connectionManager.getConnectionHandler().createNode(templateElementLabel);
            templateObjectNode.setProperty(Constants.PROPERTY_NAME, tsElementName == null ? "" : tsElementName);
            templateObjectNode.setProperty(Constants.PROPERTY_UUID, uuid);
            templateObjectNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis()); //The default value is right now
            
            templateObjectNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
            
            Relationship specialInstanceRelationship = templateObjectNode.createRelationshipTo(classNode, RelTypes.INSTANCE_OF_SPECIAL);
            specialInstanceRelationship.setProperty(Constants.PROPERTY_NAME, "template"); //NOI18N 
            
            tx.success();
            return uuid;
        }
    }
    
    @Override
    public String[] createBulkTemplateElement(String templateElementClassName, String templateElementParentClassName, 
            String templateElementParentId, String templateElementNamePattern) 
        throws MetadataObjectNotFoundException, OperationNotPermittedException, ApplicationObjectNotFoundException, InvalidArgumentException {
        
        boolean isPossibleChildren = false;
        for (ClassMetadataLight possibleChildren : mem.getPossibleChildren(templateElementParentClassName, true)) {
            if (possibleChildren.getName().equals(templateElementClassName)) {
                isPossibleChildren = true;
                break;
            }
        }
        if (!isPossibleChildren) 
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", templateElementClassName, templateElementParentClassName == null ? Constants.NODE_DUMMYROOT : templateElementParentClassName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, templateElementClassName);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", templateElementClassName));
            
            Node parentClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, templateElementParentClassName);
            if (parentClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Parent class %s can not be found", templateElementParentClassName));
            
            if (classNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", templateElementClassName));
            
            Node parentNode = null;
            
            for(Relationship instanceOfSpecialRelationship : parentClassNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF_SPECIAL)) {
                if (templateElementParentId.equals(instanceOfSpecialRelationship.getStartNode().getProperty(Constants.PROPERTY_UUID))) {
                    parentNode = instanceOfSpecialRelationship.getStartNode();
                    break;
                }
            }
            if (parentNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Parent object %s of class %s not found", templateElementParentId, templateElementParentClassName));
            
            DynamicNameGenerator dynamicName = new DynamicNameGenerator(templateElementNamePattern);            
            String res[] = new String[dynamicName.getNumberOfDynamicNames()];
            
            for (int i = 0; i < dynamicName.getNumberOfDynamicNames(); i++) {
                String templateElementName = dynamicName.getDynamicNames().get(i);
                
                String uuid = UUID.randomUUID().toString();
                Node templateObjectNode = connectionManager.getConnectionHandler().createNode(templateElementLabel);
                templateObjectNode.setProperty(Constants.PROPERTY_NAME, templateElementName == null ? "" : templateElementName);
                templateObjectNode.setProperty(Constants.PROPERTY_UUID, uuid);
                templateObjectNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis()); //The default value is right now

                templateObjectNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF);

                Relationship specialInstanceRelationship = templateObjectNode.createRelationshipTo(classNode, RelTypes.INSTANCE_OF_SPECIAL);
                specialInstanceRelationship.setProperty(Constants.PROPERTY_NAME, "template"); //NOI18N 
                
                res[i] = uuid;
            }            
            tx.success();            
            return res;
        }
    }
        
    @Override
    public String[] createBulkSpecialTemplateElement(String stElementClass, String stElementParentClassName, String stElementParentId, String stElementNamePattern) 
        throws OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        
        boolean isPossibleSpecialChildren = false;
        for (ClassMetadataLight  possibleSpecialChildren : mem.getPossibleSpecialChildren(stElementParentClassName)) {
            if (possibleSpecialChildren.getName().equals(stElementClass)) {
                isPossibleSpecialChildren = true;
                break;
            }
        }
        if (!isPossibleSpecialChildren)
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as special child of %s", stElementClass, stElementParentClassName == null ? Constants.NODE_DUMMYROOT : stElementParentClassName));
            
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, stElementClass);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", stElementClass));
                        
            Node parentClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, stElementParentClassName);
            if (parentClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Parent class %s can not be found", stElementParentClassName));
            
            if (classNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", stElementClass));
            
            Node parentNode = null;
            
            for(Relationship instanceOfSpecialRelationship : parentClassNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF_SPECIAL)) {
                if (stElementParentId.equals(instanceOfSpecialRelationship.getStartNode().getProperty(Constants.PROPERTY_UUID))) {
                    parentNode = instanceOfSpecialRelationship.getStartNode();
                    break;
                }
            }
            
            if (parentNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Parent object %s of class %s not found", stElementParentId, stElementParentClassName));
            
            DynamicNameGenerator dynamicName = new DynamicNameGenerator(stElementNamePattern);
            
            String res[] = new String[dynamicName.getNumberOfDynamicNames()];
            
            for (int i = 0; i < dynamicName.getNumberOfDynamicNames(); i++) {
                String stElementName = dynamicName.getDynamicNames().get(i);

                String uuid = UUID.randomUUID().toString();
                Node templateObjectNode = connectionManager.getConnectionHandler().createNode(templateElementLabel);
                templateObjectNode.setProperty(Constants.PROPERTY_NAME, stElementName == null ? "" : stElementName);
                templateObjectNode.setProperty(Constants.PROPERTY_UUID, uuid);
                templateObjectNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis()); //The default value is right now

                templateObjectNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);

                Relationship specialInstanceRelationship = templateObjectNode.createRelationshipTo(classNode, RelTypes.INSTANCE_OF_SPECIAL);
                specialInstanceRelationship.setProperty(Constants.PROPERTY_NAME, "template"); //NOI18N 
                
                res[i] = uuid;
            }
            tx.success();
            return res;
        }
    }
        
    @Override
    public ChangeDescriptor updateTemplateElement(String templateElementClass, String templateElementId, String[] attributeNames, 
            String[] attributeValues) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        if (attributeNames.length != attributeValues.length)
            throw new InvalidArgumentException("Attribute names and values must have the same length");
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            try {
                Node objectNode = connectionManager.getConnectionHandler().findNode(templateElementLabel, Constants.PROPERTY_UUID, templateElementId);
                
                if (!objectNode.hasRelationship(Direction.OUTGOING, RelTypes.INSTANCE_OF_SPECIAL) ||
                        !objectNode.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME).equals(templateElementClass))
                    throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", templateElementClass));
                
                String affectedProperties = "", oldValues = "", newValues = "";
                ClassMetadata classMetadata = cm.getClass(templateElementClass);

                for (int i = 0; i < attributeNames.length; i++) {
                    if (!classMetadata.hasAttribute(attributeNames[i]))
                        throw new MetadataObjectNotFoundException(String.format("Class %s does not have any attribute named %s", templateElementClass, attributeNames[i]));

                    affectedProperties += " " + attributeNames[i];

                    String attributeType = classMetadata.getType(attributeNames[i]);
                    if (AttributeMetadata.isPrimitive(attributeType)) {
                        oldValues += " " + (objectNode.hasProperty(attributeNames[i]) ? objectNode.getProperty(attributeNames[i]) : "null");

                        if (attributeValues[i] == null) {
                            if (objectNode.hasProperty(attributeNames[i]))
                                objectNode.removeProperty(attributeNames[i]);
                        } else {                        
                            objectNode.setProperty(attributeNames[i], Util.getRealValue(attributeValues[i], attributeType, ts));
                            newValues += " " + objectNode.getProperty(attributeNames[i]);
                        }
                    } else { //It's a list type
                        for (Relationship relatedToRelationship : objectNode.getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO)) {
                            if (relatedToRelationship.hasProperty(Constants.PROPERTY_NAME) && relatedToRelationship.getProperty(Constants.PROPERTY_NAME).equals(attributeNames[i])) {
                                oldValues += " " + (relatedToRelationship.getEndNode().hasProperty(Constants.PROPERTY_NAME) ? relatedToRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME) : "null");
                                relatedToRelationship.delete();
                                break;
                            }
                        }

                        if (attributeValues[i] != null && !attributeValues[i].equals("0") ) { //NOI18N 
                            Node listTypeItemNode = connectionManager.getConnectionHandler().findNode(listTypeItemLabel, Constants.PROPERTY_UUID, attributeValues[i]);

                            if (listTypeItemNode == null)
                                throw new ApplicationObjectNotFoundException(String.format("A list type %s with id %s could not be found", attributeType, attributeValues[i]));

                            Relationship relatedToRelationship = objectNode.createRelationshipTo(listTypeItemNode, RelTypes.RELATED_TO);
                            relatedToRelationship.setProperty(Constants.PROPERTY_NAME, attributeNames[i]);

                            newValues += " " + (listTypeItemNode.hasProperty(Constants.PROPERTY_NAME) ? listTypeItemNode.getProperty(Constants.PROPERTY_NAME) : "null");
                        } 
                    }
                }
                String templateElementName = objectNode.hasProperty(Constants.PROPERTY_NAME) ? (String) objectNode.getProperty(Constants.PROPERTY_NAME) : "null";
                tx.success();
                return new ChangeDescriptor(affectedProperties, oldValues, newValues, String.format("Updated template element %s [%s]", templateElementName, templateElementClass));
            } catch (NotFoundException ex) {
                throw new ApplicationObjectNotFoundException(String.format("Template object %s of class %s could not be found", templateElementId, templateElementClass));
            }
        }
    }

    @Override
    public ChangeDescriptor deleteTemplateElement(String templateElementClass, String templateElementId) 
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node templateObjectNode = connectionManager.getConnectionHandler().findNode(templateElementLabel, Constants.PROPERTY_UUID, templateElementId);
                
            if (!templateObjectNode.hasRelationship(Direction.OUTGOING, RelTypes.INSTANCE_OF_SPECIAL) ||
                    !templateObjectNode.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME).equals(templateElementClass))
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", templateElementClass));
             
            String templateObjectName = templateObjectNode.hasProperty(Constants.PROPERTY_NAME) ? (String) templateObjectNode.getProperty(Constants.PROPERTY_NAME) : "null";
            //Delete the template element recursively
            Util.deleteTemplateObject(templateObjectNode);
            
            tx.success();
            return new ChangeDescriptor("", "", "", String.format("Deleted template element %s [%s]", templateObjectName, templateElementClass));
        } catch (NotFoundException ex) {
            throw new ApplicationObjectNotFoundException(String.format("Template object %s of class %s could not be found", templateElementId, templateElementClass));
        }
    }

    @Override
    public List<TemplateObjectLight> getTemplatesForClass(String className) throws MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<TemplateObjectLight> templates = new ArrayList<>();
                        
            String query = "MATCH (classNode)-[:" + RelTypes.HAS_TEMPLATE + "]->(templateObject) WHERE classNode.name={className} RETURN templateObject ORDER BY templateObject.name ASC"; //NOI18N
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("className", className); //NOI18N
            ResourceIterator<Node> queryResult = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("templateObject");
            
            while (queryResult.hasNext()) {
                Node templateNode = queryResult.next();
                templates.add(new TemplateObjectLight(className, (String)templateNode.getProperty(Constants.PROPERTY_UUID), 
                        (String)templateNode.getProperty(Constants.PROPERTY_NAME)));
            }
            tx.success();
            return templates;
        }
    }
    
    @Override
    public List<TemplateObjectLight> getTemplateElementChildren(String templateElementClass, String templateElementId)  {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            String query = "MATCH (classNode)<-[:" + RelTypes.INSTANCE_OF_SPECIAL + 
                    "]-(templateElement)<-[:" + RelTypes.CHILD_OF + "]-(templateElementChild) "
                    + "WHERE classNode.name={templateElementClass} AND templateElement._uuid = {templateElementId} "
                    + "RETURN templateElementChild ORDER BY templateElementChild.name ASC"; //NOI18N
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("templateElementClass", templateElementClass); //NOI18N
            parameters.put("templateElementId", templateElementId); //NOI18N
            ResourceIterator<Node> queryResult = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("templateElementChild");
            
            List<TemplateObjectLight> templateElementChildren = new ArrayList<>();
            while (queryResult.hasNext()) {
                Node templateChildNode = queryResult.next();
                templateElementChildren.add(new TemplateObjectLight((String)templateChildNode.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME), 
                        (String)templateChildNode.getProperty(Constants.PROPERTY_UUID), (String)templateChildNode.getProperty(Constants.PROPERTY_NAME)));
            }
            
            return templateElementChildren; 
        }
    }
    
    @Override
    public List<TemplateObjectLight> getTemplateSpecialElementChildren(String tsElementClass, String tsElementId) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = "MATCH (classNode)<-[:" + RelTypes.INSTANCE_OF_SPECIAL + 
                    "]-(templateElement)<-[:" + RelTypes.CHILD_OF_SPECIAL + "]-(templateElementChild) "
                    + "WHERE classNode.name={templateElementClass} AND templateElement._uuid = {templateElementId} "
                    + "RETURN templateElementChild ORDER BY templateElementChild.name ASC"; //NOI18N
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("templateElementClass", tsElementClass); //NOI18N
            parameters.put("templateElementId", tsElementId); //NOI18N
            ResourceIterator<Node> queryResult = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("templateElementChild");
            
            List<TemplateObjectLight> templateElementChildren = new ArrayList<>();
            while (queryResult.hasNext()) {
                Node templateChildNode = queryResult.next();
                templateElementChildren.add(new TemplateObjectLight((String)templateChildNode.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME), 
                        (String)templateChildNode.getProperty(Constants.PROPERTY_UUID), (String)templateChildNode.getProperty(Constants.PROPERTY_NAME)));
            }
            
            return templateElementChildren; 
        }
    }
    
    @Override
    public TemplateObject getTemplateElement(String templateElementClass, String templateElementId)
        throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            try {
                //First we find the object, then we check if the class is the same as specified
                Node templateObjectNode = connectionManager.getConnectionHandler().findNode(templateElementLabel, Constants.PROPERTY_UUID, templateElementId);
                if (!templateObjectNode.hasRelationship(RelTypes.INSTANCE_OF_SPECIAL) ||
                        !templateObjectNode.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode().
                                getProperty(Constants.PROPERTY_NAME).equals(templateElementClass)) {
                    
                    MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".18"), templateElementClass));
                    ex.setPrefix(KEY_PREFIX);
                    ex.setCode(18);
                    ex.setMessageArgs(templateElementClass);
                    throw ex;
                }
                return createTemplateElementFromNode(templateObjectNode, templateElementClass);
            } catch (NotFoundException ex) {
                ApplicationObjectNotFoundException nestedEx = new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".19"), templateElementId, templateElementClass));
                nestedEx.setPrefix(KEY_PREFIX);
                nestedEx.setCode(19);
                nestedEx.setMessageArgs(templateElementId, templateElementClass);
                throw nestedEx;
            }
        }
    }

    @Override
    public String[] copyTemplateElements(String[] sourceObjectsClassNames, String[] sourceObjectsIds, String newParentClassName, 
            String newParentId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, ArraySizeMismatchException {
        
        if (sourceObjectsClassNames.length != sourceObjectsIds.length)
            throw new ArraySizeMismatchException("The sourceObjectsClassNames and sourceObjectsIds arrays have different sizes");
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String[] newTemplateElements = new String[sourceObjectsClassNames.length];
            
            Node newParentNode = getTemplateElementInstance(newParentClassName, newParentId);
            
            for (int i = 0; i < sourceObjectsClassNames.length; i++) {
                Node templateObjectNode = getTemplateElementInstance(sourceObjectsClassNames[i], sourceObjectsIds[i]);
                Node newTemplateElementInstance = copyTemplateElement(templateObjectNode, true);
                newTemplateElementInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF);
                newTemplateElements[i] = (String)newTemplateElementInstance.getProperty(Constants.PROPERTY_UUID);
            }
            tx.success();
            return newTemplateElements;
        }
    }
    
    @Override
    public String[] copyTemplateSpecialElement(String[] sourceObjectsClassNames, String[] sourceObjectsIds, String newParentClassName, String newParentId) 
     throws ArraySizeMismatchException, ApplicationObjectNotFoundException, MetadataObjectNotFoundException {
        if(sourceObjectsClassNames.length != sourceObjectsIds.length)
            throw new ArraySizeMismatchException("The sourceObjectsClassNames and sourceObjectsIds arrays have different sizes");
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String[] newTemplateSpecialElements = new String[sourceObjectsClassNames.length];
            
            Node newParentNode = getTemplateElementInstance(newParentClassName, newParentId);
            
            for (int i = 0; i < sourceObjectsClassNames.length; i++) {
                Node templateObjectNode = getTemplateElementInstance(sourceObjectsClassNames[i], sourceObjectsIds[i]);
                Node newTemplateSpecialElementInstance = copyTemplateElement(templateObjectNode, true);
                newTemplateSpecialElementInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL);
                newTemplateSpecialElements[i] = (String)newTemplateSpecialElementInstance.getProperty(Constants.PROPERTY_UUID);
            }
            tx.success();
            return newTemplateSpecialElements;
        }
    }
    
    @Override
    public HashMap<String, BusinessObjectList> executeCustomDbCode(String dbCode, boolean needReturn) throws NotAuthorizedException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Result theResult = connectionManager.getConnectionHandler().execute(dbCode);
            if(needReturn) {
                HashMap<String, BusinessObjectList> thePaths = new HashMap<>();
            
                for (String column : theResult.columns())
                    thePaths.put(column, new BusinessObjectList());

                try {
                    while (theResult.hasNext()) {
                        Map<String, Object> row = theResult.next();
                        for (String column : row.keySet()) 
                            thePaths.get(column).add(createBusinessObjectFromNode((Node)row.get(column)));
                    }
                } catch (InvalidArgumentException ex) {} //this should not happen

                return thePaths;
            }
            else {
                tx.success();
                return null;
            }
        }
    }
    
    @Override
    public List<Properties> executeCustomScriptedQuery(String queryName, Properties parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        switch (queryName) {
            case "whSuggestions":
                return new WarehouseManagerSearchQuery().execute(parameters);
            default:
                throw new ApplicationObjectNotFoundException(String.format("Scripted query %s could not be found", queryName));
        }
    }
    
    // Helpers
    /**
     * recursive method used to generate a single "class" node.
     * @param classNode Node representing the class to be added
     * @param paretTag Parent to attach the new class node
     */
    private void getXMLNodeForClass(Node classNode, XMLEventWriter xmlew, XMLEventFactory xmlef) throws XMLStreamException {
        int applicationModifiers = 0;
        int javaModifiers = 0;
        QName qnameClass = new QName("class"); // NOI18N
        xmlew.add(xmlef.createStartElement(qnameClass, null, null));
        
        xmlew.add(xmlef.createAttribute(new QName("id"), Long.toString(classNode.getId())));
        xmlew.add(xmlef.createAttribute(new QName("name"), classNode.getProperty(Constants.PROPERTY_NAME).toString()));
        xmlew.add(xmlef.createAttribute(new QName("classPackage"), ""));
        
        //Application modifiers
        if ((Boolean)classNode.getProperty(Constants.PROPERTY_COUNTABLE))
            applicationModifiers |= Constants.CLASS_MODIFIER_COUNTABLE;

        if ((Boolean)classNode.getProperty(Constants.PROPERTY_CUSTOM))
            applicationModifiers |= Constants.CLASS_MODIFIER_CUSTOM;
        xmlew.add(xmlef.createAttribute(new QName("applicationModifiers"), Integer.toString(applicationModifiers)));
        
        //Language modifiers
        if ((Boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
            javaModifiers |= Modifier.ABSTRACT;
        xmlew.add(xmlef.createAttribute(new QName("javaModifiers"), Integer.toString(javaModifiers)));
        
        //Class type
        if (classNode.getProperty(Constants.PROPERTY_NAME).equals(Constants.CLASS_ROOTOBJECT)) {
            xmlew.add(xmlef.createAttribute(new QName("classType"), Integer.toString(Constants.CLASS_TYPE_ROOT)));
        }else{
            if (Util.isSubclassOf("InventoryObject", classNode))
                xmlew.add(xmlef.createAttribute(new QName("classType"), Integer.toString(Constants.CLASS_TYPE_INVENTORY)));
            else{
                if (Util.isSubclassOf("ApplicationObject", classNode))
                    xmlew.add(xmlef.createAttribute(new QName("classType"), Integer.toString(Constants.CLASS_TYPE_APPLICATION)));
                else
                    xmlew.add(xmlef.createAttribute(new QName("classType"), Integer.toString(Constants.CLASS_TYPE_OTHER)));
            }
        }
        
        QName qnameAttributes = new QName("attributes");
        xmlew.add(xmlef.createStartElement(qnameAttributes, null, null));
        for (Relationship relWithAttributes : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE, Direction.OUTGOING)) {
            QName qnameAttribute = new QName("attribute");
            xmlew.add(xmlef.createStartElement(qnameAttribute, null, null));
            
            Node attributeNode = relWithAttributes.getEndNode();
            int attributeApplicationModifiers = 0;
            
            xmlew.add(xmlef.createAttribute(new QName("name"), attributeNode.getProperty(Constants.PROPERTY_NAME).toString()));
            xmlew.add(xmlef.createAttribute(new QName("type"), attributeNode.getProperty(Constants.PROPERTY_TYPE).toString()));
            xmlew.add(xmlef.createAttribute(new QName("javaModifiers"), "0")); // Not used
            //Application modifiers
            if ((Boolean)attributeNode.getProperty(Constants.PROPERTY_NO_COPY))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_NOCOPY;
            if ((Boolean)attributeNode.getProperty(Constants.PROPERTY_VISIBLE))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_VISIBLE;
            if ((Boolean)attributeNode.getProperty(Constants.PROPERTY_ADMINISTRATIVE))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_ADMINISTRATIVE;
            if ((Boolean)attributeNode.getProperty(Constants.PROPERTY_READ_ONLY))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_READONLY;
            xmlew.add(xmlef.createAttribute(new QName("applicationModifiers"), Integer.toString(attributeApplicationModifiers)));
            xmlew.add(xmlef.createEndElement(qnameAttribute, null));
        }
        xmlew.add(xmlef.createEndElement(qnameAttributes, null));
        
        QName qnameSubclasses = new QName("subclasses");
        xmlew.add(xmlef.createStartElement(qnameSubclasses, null, null));
        for (Relationship relWithSubclasses : classNode.getRelationships(RelTypes.EXTENDS, Direction.INCOMING))
            getXMLNodeForClass(relWithSubclasses.getStartNode(), xmlew, xmlef);
        xmlew.add(xmlef.createEndElement(qnameSubclasses, null));
        
        xmlew.add(xmlef.createEndElement(qnameClass, null));
    }
    /**
     * Reads a ExtendedQuery looking for the classes involved in the query and returns all class nodes
     * @param query
     * @return class metadata nodes
     */
    private Map<String, Node> getNodesFromQuery(ExtendedQuery query)  throws MetadataObjectNotFoundException{

        Map<String, Node> classNodes = new HashMap<>();
        List<String> ListClassNames = new ArrayList();
        readJoins(ListClassNames, query);
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            for(String className : ListClassNames)
                classNodes.put(className, connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className));            
        }
        return classNodes;
    }
    
    private String readJoins(List<String> l, ExtendedQuery query) {
        
        String className;

        if(query == null)
            return null;
        else
            className = query.getClassName();

        if(query.getJoins() != null) {
            for(ExtendedQuery join : query.getJoins()) {
                    readJoins(l,join);
            }
        }
        if(className != null || className.isEmpty())
            l.add(className);
        return className;
    }
    
    private Node getInstanceOfClass(String className, String oid) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException
    {
        //Note that for this method, the caller should handle the transaction
        //if any of the parameters is null, return the dummy root
        if (className == null)
            return connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
        
        Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME,className);
        if (classNode == null) {
            MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".20"), className));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(20);
            ex.setMessageArgs(className);
            throw ex;
        }

        Iterable<Relationship> iteratorInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iteratorInstances.iterator();

        while (instances.hasNext()) {
            Node otherSide = instances.next().getStartNode();
            String otherSideUuid = otherSide.hasProperty(Constants.PROPERTY_UUID) ? (String) otherSide.getProperty(Constants.PROPERTY_UUID) : null;
            if (otherSideUuid == null) {
                InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".21"), otherSide.getId()));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(21);
                ex.setMessageArgs(otherSide.getId());
                throw ex;
            }
                        
            if (otherSideUuid.equals(oid))
                return otherSide;
        }
        throw new BusinessObjectNotFoundException(className, oid);
    }    
    
    public void deletePool(Node poolNode) throws OperationNotPermittedException {
        
        for (Relationship containmentRelationship : poolNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                //A pool may have inventory objects as children or other pools
                Node child = containmentRelationship.getStartNode();
                if (child.hasRelationship(RelTypes.INSTANCE_OF)) //It's an inventory object
                    deleteObject(child, false);
                else
                    deletePool(child); //Although making deletePool to receive a node as argument would be more efficient,
                                               //the impact is not that much since the number of pools is expected to be low
            }
            
            //Removes any remaining relationships
            for (Relationship remainingRelationship : poolNode.getRelationships())
                remainingRelationship.delete();
            
            poolNode.delete();
    }
    
    private Node getTemplateElementInstance(String templateElementClassName, String templateElementId) throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException {
        Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, templateElementClassName);
            
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", templateElementClassName));

        Node templateElementNode = null;

        for (Relationship instanceOfSpecialRelationship : classNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF_SPECIAL)) {
            Node startNode = instanceOfSpecialRelationship.getStartNode();
            if (templateElementId.equals(startNode.getProperty(Constants.PROPERTY_UUID))) {
                templateElementNode = startNode;
                break;
            }
        }

        if (templateElementNode == null)
            throw new ApplicationObjectNotFoundException(String.format("Template object %s of class %s could not be found", templateElementId, templateElementClassName));

        return templateElementNode;
    }
    
    private Node copyTemplateElement(Node templateObject, boolean recursive) {
        
        Node newTemplateElementInstance = connectionManager.getConnectionHandler().createNode(templateElementLabel);
        newTemplateElementInstance.setProperty(Constants.PROPERTY_UUID, UUID.randomUUID().toString());
        
        for (String property : templateObject.getPropertyKeys()) {
            if (Constants.PROPERTY_UUID.equals(property))
                continue;
            newTemplateElementInstance.setProperty(property, templateObject.getProperty(property));
        }
        for (Relationship rel : templateObject.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING))
            newTemplateElementInstance.createRelationshipTo(rel.getEndNode(), RelTypes.RELATED_TO).setProperty(Constants.PROPERTY_NAME, rel.getProperty(Constants.PROPERTY_NAME));
        
        newTemplateElementInstance.createRelationshipTo(templateObject.getRelationships(RelTypes.INSTANCE_OF_SPECIAL).iterator().next().getEndNode(), RelTypes.INSTANCE_OF_SPECIAL);

        if (recursive) {
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING)) {
                Node newChild = copyTemplateElement(rel.getStartNode(), true);
                newChild.createRelationshipTo(newTemplateElementInstance, RelTypes.CHILD_OF);
            }
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING)) {
                Node newChild = copyTemplateElement(rel.getStartNode(), true);
                newChild.createRelationshipTo(newTemplateElementInstance, RelTypes.CHILD_OF_SPECIAL);
            }
        }
        return newTemplateElementInstance;
    }
    
    //End of Helpers  
    
    // Bookmarks
    // <editor-fold defaultstate="collapsed" desc="Favorites">
    @Override
    public void addObjectTofavoritesFolder(String objectClass, String objectId, long favoritesFolderId, long userId)
            throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException{
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node favoritesFolderNode = getFavoritesFolderForUser(favoritesFolderId, userId);
            if (favoritesFolderNode == null)
                throw new ApplicationObjectNotFoundException(
                        String.format(ts.getTranslatedString("module.favorites.favorite-folder-not-found"), favoritesFolderId));
            
            Node objectNode = getInstanceOfClass(objectClass, objectId);
            
            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.IS_BOOKMARK_ITEM_IN)) {
                for (Relationship relationship : objectNode.getRelationships(Direction.OUTGOING, RelTypes.IS_BOOKMARK_ITEM_IN)) {
                    if (favoritesFolderNode.getId() == relationship.getEndNode().getId())
                        throw new OperationNotPermittedException(ts.getTranslatedString("module.favorites.actions.favorite-add-object-to-favorite.error"));
                }
            }
            objectNode.createRelationshipTo(favoritesFolderNode, RelTypes.IS_BOOKMARK_ITEM_IN);
            
            tx.success();
        }
    }
    
    @Override
    public void removeObjectFromfavoritesFolder(String objectClass, String objectId, long favoritesFolderId, long userId) 
        throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
                
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node objectNode = getInstanceOfClass(objectClass, objectId);
            
            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.IS_BOOKMARK_ITEM_IN)) {
                
                Node favoritesFolderNode = getFavoritesFolderForUser(favoritesFolderId, userId);
                if (favoritesFolderNode == null)
                    throw new ApplicationObjectNotFoundException(
                            String.format(ts.getTranslatedString("module.favorites.favorite-folder-not-found"), favoritesFolderId));
                
                Relationship relationshipToDelete = null;
                
                for (Relationship relationship : objectNode.getRelationships(Direction.OUTGOING, RelTypes.IS_BOOKMARK_ITEM_IN)) {
                    
                    if (favoritesFolderNode.getId() == relationship.getEndNode().getId())
                        relationshipToDelete = relationship;
                }
                if (relationshipToDelete != null) {
                    relationshipToDelete.delete();
                    tx.success();
                }
            }
        }
    }
    
    @Override
    public long createFavoritesFolderForUser(String name, long userId) 
        throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        if (name == null || name.trim().isEmpty())
                throw new InvalidArgumentException(ts.getTranslatedString("module.favorites.actions.favorite-edit-favorite-name-empty"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, userId);
            
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(
                        String.format(ts.getTranslatedString("module.general.messages.user-id-not-found"), userId));
            
            Node favoritesFolderNode = connectionManager.getConnectionHandler().createNode();
            favoritesFolderNode.setProperty(Constants.PROPERTY_NAME, name);            
            
            userNode.createRelationshipTo(favoritesFolderNode, RelTypes.HAS_BOOKMARK);
            
            tx.success();
            return favoritesFolderNode.getId();
        }
    }
    
    @Override
    public void deleteFavoritesFolders (long[] favoritesFolderId, long userId)
        throws ApplicationObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (favoritesFolderId != null) {
                for (long id : favoritesFolderId) {
                    Node favoritesFolderNode = getFavoritesFolderForUser(id, userId);
                    
                    if (favoritesFolderNode == null)
                        throw new ApplicationObjectNotFoundException(
                                String.format(ts.getTranslatedString("module.favorites.favorite-folder-not-found"), id));
                    
                    for (Relationship relationship : favoritesFolderNode.getRelationships(Direction.INCOMING, RelTypes.HAS_BOOKMARK))
                        relationship.delete();
                    
                    for (Relationship relationship : favoritesFolderNode.getRelationships(Direction.INCOMING, RelTypes.IS_BOOKMARK_ITEM_IN))
                        relationship.delete();
                    
                    favoritesFolderNode.delete();
                    tx.success();
                }
            }
        }
    }
    
    @Override
    public List<FavoritesFolder> getFavoritesFoldersForUser(long userId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, userId);
            
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.user-id-not-found"), userId));
            
            List<FavoritesFolder> favoritesFolders = new ArrayList(); 
            
            if (userNode.hasRelationship(Direction.OUTGOING, RelTypes.HAS_BOOKMARK)) {
                
                for (Relationship relationship : userNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_BOOKMARK)) {
                    Node favoritesFolderNode = relationship.getEndNode();
                    String name = favoritesFolderNode.hasProperty(Constants.PROPERTY_NAME) ? (String) favoritesFolderNode.getProperty(Constants.PROPERTY_NAME) : null;
                    
                    favoritesFolders.add(new FavoritesFolder(favoritesFolderNode.getId(), name));
                }
            }
            return favoritesFolders;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectsInFavoritesFolder(long favoritesFolderId, long userId, int limit) 
        throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node favoritesFolderNode = getFavoritesFolderForUser(favoritesFolderId, userId);
            
            if (favoritesFolderNode == null)
                throw new ApplicationObjectNotFoundException(String.format(
                        ts.getTranslatedString("module.favorites.favorite-folder-not-found"), favoritesFolderId));
            
            List<BusinessObjectLight> bookmarkItems = new ArrayList<>();
            
            int i = 0;
            for (Relationship relationship : favoritesFolderNode.getRelationships(Direction.INCOMING, RelTypes.IS_BOOKMARK_ITEM_IN)) {
                if (limit != -1) {
                    if (i >= limit)
                        break;
                    i++;
                }
                Node bookmarkItem = relationship.getStartNode();
                Node classNode = bookmarkItem.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode();
                        
                String bookmarkItemUuid = bookmarkItem.hasProperty(Constants.PROPERTY_UUID) ? (String) bookmarkItem.getProperty(Constants.PROPERTY_UUID) : null;
                if (bookmarkItemUuid == null)
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.object-not-have-uuid"), bookmarkItem.getId()));
                                                                
                BusinessObjectLight rbol = new BusinessObjectLight((String)classNode.getProperty(Constants.PROPERTY_NAME),
                    bookmarkItemUuid, 
                    bookmarkItem.hasProperty(Constants.PROPERTY_NAME) ? (String) bookmarkItem.getProperty(Constants.PROPERTY_NAME) : null, 
                    (String)classNode.getProperty(Constants.PROPERTY_DISPLAY_NAME, null));
                
                bookmarkItems.add(rbol);
            }
            return bookmarkItems;
        }
    }
    
    @Override
    public List<FavoritesFolder> getFavoritesFoldersForObject(long userId, String objectClass, String objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getInstanceOfClass(objectClass, objectId);
                        
            List<FavoritesFolder> favoritesFolders = new ArrayList(); 
                
            for (Relationship relationship : objectNode.getRelationships(Direction.OUTGOING, RelTypes.IS_BOOKMARK_ITEM_IN)) {
                Node favoritesFolderNode = relationship.getEndNode();

                if (getFavoritesFolderForUser(favoritesFolderNode.getId(), userId) != null) //If null, the object is in a favorites folder, but that folder does not belong to the current user, so it's safe to omiit it
                    favoritesFolders.add(new FavoritesFolder(favoritesFolderNode.getId(), (String) favoritesFolderNode.getProperty(Constants.PROPERTY_NAME)));
            }
            return favoritesFolders;
        }
    }
    
    @Override
    public FavoritesFolder getFavoritesFolder(long favoritesFolderId, long userId) 
        throws ApplicationObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node favoritesFolderNode = getFavoritesFolderForUser(favoritesFolderId, userId);
            
            if (favoritesFolderNode == null)
                throw new ApplicationObjectNotFoundException(
                        String.format(ts.getTranslatedString("module.favorites.favorite-folder-not-found"), favoritesFolderId));
            
            String name = favoritesFolderNode.hasProperty(Constants.PROPERTY_NAME) ? (String) favoritesFolderNode.getProperty(Constants.PROPERTY_NAME) : null;
                    
            return new FavoritesFolder(favoritesFolderNode.getId(), name);
        }
    }
    
    @Override
    public void updateFavoritesFolder(long favoritesFolderId, long userId, String favoritesFolderName) 
        throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node favoritesFolderNode = getFavoritesFolderForUser(favoritesFolderId, userId);
            
            if (favoritesFolderNode == null)
                throw new ApplicationObjectNotFoundException(String.format(
                        ts.getTranslatedString("module.favorites.favorite-folder-not-found"), favoritesFolderId));
            
            if (favoritesFolderName != null && !favoritesFolderName.trim().isEmpty()) {
                favoritesFolderNode.setProperty(Constants.PROPERTY_NAME, favoritesFolderName);
                tx.success();
            } else 
                throw new InvalidArgumentException(ts.getTranslatedString("module.favorites.actions.favorite-edit-favorite-name-empty"));
        }
    }
    // </editor-fold>
    
    @Override
    public long attachFileToListTypeItem(String name, String tags, byte[] file, String listTypeItemClass, String listTypeItemId) 
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        float maxAttachmentSize = Float.valueOf(configuration.getProperty("maxAttachmentSize", DEFAULT_MAX_ATTACHMENT_SIZE)) * 1048576; // Default maxSize value is 10MB
        
        String attachmentsPath = configuration.getProperty("attachmentsPath", DEFAULT_ATTACHMENTS_PATH);
        
        if (!Files.exists(Paths.get(attachmentsPath)))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.invalid-path"), attachmentsPath));
            
        if (file.length > maxAttachmentSize)
            throw new InvalidArgumentException(String.format("The file size exceeds the maximum size allowed (%s MB)", maxAttachmentSize));
        
        if (name == null || name.trim().isEmpty())
            throw new InvalidArgumentException("The file name can not be an empty string");
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClass);  
            
            Node fileObjectNode = connectionManager.getConnectionHandler().createNode(Label.label(Constants.LABEL_ATTACHMENTS));
            fileObjectNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            fileObjectNode.setProperty(Constants.PROPERTY_NAME, name);
            fileObjectNode.setProperty(Constants.PROPERTY_TAGS, tags == null ? "" : tags);
            
            Relationship hasAttachmentRelationship = listTypeItemNode.createRelationshipTo(fileObjectNode, RelTypes.HAS_ATTACHMENT);
            hasAttachmentRelationship.setProperty(Constants.PROPERTY_NAME, "attachments");
            
            String fileName = listTypeItemNode.getProperty(Constants.PROPERTY_UUID) + "_" + fileObjectNode.getId();
            Util.saveFile(attachmentsPath, fileName, file);
            
            tx.success();
            return fileObjectNode.getId();
        } catch(IOException ex) {
            throw new OperationNotPermittedException(ex.getMessage());
        }
    }

    @Override
    public List<FileObjectLight> getFilesForListTypeItem(String className, String objectId) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getListTypeItemNode(objectId, className);
            List<FileObjectLight> res = new ArrayList<>();
            
            for (Relationship fileObjectRelationship : objectNode.getRelationships(RelTypes.HAS_ATTACHMENT, Direction.OUTGOING)) {
                Node fileObjectNode = fileObjectRelationship.getEndNode();
                res.add(new FileObjectLight(fileObjectNode.getId(), (String)fileObjectNode.getProperty(Constants.PROPERTY_NAME), 
                        (String)fileObjectNode.getProperty(Constants.PROPERTY_TAGS), (long)fileObjectNode.getProperty(Constants.PROPERTY_CREATION_DATE)));
            }
            return res;
        }
    }

    @Override
    public FileObject getFile(long fileObjectId, String className, String objectId) throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getListTypeItemNode(objectId, className);

            for (Relationship fileObjectRelationship : objectNode.getRelationships(RelTypes.HAS_ATTACHMENT, Direction.OUTGOING)) {
                if (fileObjectRelationship.getEndNode().getId() == fileObjectId) {
                    String fileName = objectNode.getProperty(Constants.PROPERTY_UUID) + "_" + fileObjectId;
                    try {
                        byte[] background = Util.readBytesFromFile(configuration.getProperty("attachmentsPath", DEFAULT_ATTACHMENTS_PATH) + "/" + fileName);
                        return new FileObject(fileObjectId, (String)fileObjectRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME), 
                                                    (String)fileObjectRelationship.getEndNode().getProperty(Constants.PROPERTY_TAGS), 
                                                    (long)fileObjectRelationship.getEndNode().getProperty(Constants.PROPERTY_CREATION_DATE), 
                                                    background);
                    }catch(IOException ex) {
                        throw new InvalidArgumentException(String.format("File with id %s could not be retrieved: %s", fileObjectId, ex.getMessage()));
                    }
                }
            }
            throw new InvalidArgumentException(String.format("The file with id %s could not be found", fileObjectId));
        }
    }

    @Override
    public void detachFileFromListTypeItem(long fileObjectId, String className, String objectId) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getListTypeItemNode(objectId, className);

            for (Relationship fileObjectRelationship : objectNode.getRelationships(RelTypes.HAS_ATTACHMENT, Direction.OUTGOING)) {
                if (fileObjectRelationship.getEndNode().getId() == fileObjectId) {
                    fileObjectRelationship.delete();
                    fileObjectRelationship.getEndNode().delete();
                    
                    try {
                        String fileName = objectNode.getProperty(Constants.PROPERTY_UUID) + "_" + fileObjectId;
                        new File(configuration.getProperty("attachmentsPath", DEFAULT_ATTACHMENTS_PATH) + File.separator + fileName).delete();
                    } catch(Exception ex) {
                        throw new InvalidArgumentException(String.format("File with id %s could not be retrieved: %s", fileObjectId, ex.getMessage()));
                    }
                    tx.success();
                    return;
                }
            }
            
            throw new InvalidArgumentException(String.format("The file with id %s could not be found", fileObjectId));
        }
    }
    
    @Override
    public void updateFileProperties(long fileObjectId, List<StringPair> properties, String className, String objectId) throws BusinessObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getListTypeItemNode(objectId, className);

            for (Relationship fileObjectRelationship : objectNode.getRelationships(RelTypes.HAS_ATTACHMENT, Direction.OUTGOING)) {
                if (fileObjectRelationship.getEndNode().getId() == fileObjectId) {
                    for (StringPair property : properties) {
                        switch (property.getKey()) {
                            case Constants.PROPERTY_NAME:
                                if (property.getValue().trim().isEmpty())
                                    throw new InvalidArgumentException("The file name can not be an empty string");
                                
                                fileObjectRelationship.getEndNode().setProperty(Constants.PROPERTY_NAME, property.getValue());
                                break;
                            case Constants.PROPERTY_TAGS:
                                fileObjectRelationship.getEndNode().setProperty(Constants.PROPERTY_TAGS, property.getValue());
                                break;
                            default:
                                throw new InvalidArgumentException(String.format("The property %s is not valid", property.getKey()));
                        }
                    }
                    tx.success();
                    return;
                }
            }
            
            throw new InvalidArgumentException(String.format("The file with id %s could not be found", fileObjectId));
        }
    }
    

    
    //<editor-fold desc="Business Rules" defaultstate="collapsed">
    @Override
    public long createBusinessRule(String ruleName, String ruleDescription, int ruleType, 
            int ruleScope, String appliesTo, String ruleVersion, List<String> constraints) throws InvalidArgumentException {
        
        if (ruleName == null || ruleDescription == null || ruleVersion == null || appliesTo == null || ruleType < 1 || ruleScope < 1)
            throw new InvalidArgumentException("Invalid parameter. Make sure all parameters are different from null and greater than 1");
        
        if (constraints == null || constraints.isEmpty())
            throw new InvalidArgumentException("The rule must have at least one constraint");
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node businessRuleNode = connectionManager.getConnectionHandler().createNode(businessRulesLabel);
            
            businessRuleNode.setProperty(Constants.PROPERTY_NAME, ruleName);
            businessRuleNode.setProperty(Constants.PROPERTY_DESCRIPTION, ruleDescription);
            businessRuleNode.setProperty(Constants.PROPERTY_TYPE, ruleType);
            businessRuleNode.setProperty(Constants.PROPERTY_SCOPE, ruleScope);
            businessRuleNode.setProperty(Constants.PROPERTY_APPLIES_TO, appliesTo);
            businessRuleNode.setProperty(Constants.PROPERTY_VERSION, ruleVersion);
            
            for (int i = 0; i < constraints.size(); i++)
                businessRuleNode.setProperty("constraint" + (i + 1), constraints.get(i)); //NOI18N
            
            tx.success();
            return businessRuleNode.getId();
        }
    }
    
    @Override
    public void deleteBusinessRule(long businessRuleId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node businessRuleNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), businessRulesLabel, businessRuleId);
            
            if (businessRuleNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Business rule with id %s not found", businessRuleId));
            
            businessRuleNode.delete();
            
            tx.success();
        }
    }
    
    @Override
    public List<BusinessRule> getBusinessRules(int type) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<BusinessRule> res = new ArrayList<>();
            
            ResourceIterator<Node> businessRuleNodes = connectionManager.getConnectionHandler().findNodes(businessRulesLabel);

            while (businessRuleNodes.hasNext()) {
                Node businessRuleNode = businessRuleNodes.next();
                if (type == -1 || type == (int)businessRuleNode.getProperty(Constants.PROPERTY_TYPE))
                    res.add(new BusinessRule(businessRuleNode.getId(), businessRuleNode.getAllProperties()));
            }
            return res;
        }
    }
    
    @Override
    public void checkRelationshipByAttributeValueBusinessRules(String sourceObjectClassName, String sourceObjectId ,
            String targetObjectClassName, String targetObjectId) throws BusinessRuleException, InvalidArgumentException  {
        
        if (!Boolean.valueOf(getConfiguration().getProperty("enforceBusinessRules", "false")))
            return;
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {    
            ResourceIterator<Node> businessRuleNodes = connectionManager.getConnectionHandler().findNodes(businessRulesLabel);

            while (businessRuleNodes.hasNext()) {
                Node businessRuleNode = businessRuleNodes.next();
                                
                if (sourceObjectClassName.equals(businessRuleNode.getProperty(Constants.PROPERTY_APPLIES_TO))) {
                    /**
                     * In this type of business rules:
                     * constraint1 is the class name of the target object
                     * constraint2 is the name of the attribute name of the source object
                     * constraint3 is the name of the attribute name of the target object
                     * constraint4 is the name of the attribute value of the source object
                     * constraint5 is the name of the attribute value of the target object
                     */
                    if (!businessRuleNode.hasProperty("constraint1") || !businessRuleNode.hasProperty("constraint2") || !businessRuleNode.hasProperty("constraint3") 
                            || !businessRuleNode.hasProperty("constraint4") || !businessRuleNode.hasProperty("constraint5"))
                        throw new InvalidArgumentException("Malformed busines rule. One of the 5 required constraints is not present");
                    
                    if (businessRuleNode.getProperty("constraint1").equals(targetObjectClassName)) {
                        
                        String sourceObjectAttributeConstraint = (String)businessRuleNode.getProperty("constraint4");
                        String targetObjectAttributeConstraint = (String)businessRuleNode.getProperty("constraint5");
                        if (sourceObjectAttributeConstraint.isEmpty() || targetObjectAttributeConstraint.isEmpty()) //This link can be connected to any object
                            return;
                        
                        Node sourceInstance = connectionManager.getConnectionHandler().findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, sourceObjectId);
                        String sourceInstanceAttributeValue = Util.getAttributeFromNode(sourceInstance, (String)businessRuleNode.getProperty("constraint2"));
                        
                        if (sourceObjectAttributeConstraint.equals(sourceInstanceAttributeValue)) {
                            Node targetInstance = connectionManager.getConnectionHandler().findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, targetObjectId);
                            String targetInstanceAttributeValue = Util.getAttributeFromNode(targetInstance, (String)businessRuleNode.getProperty("constraint3"));
                            
                            if (!targetObjectAttributeConstraint.equals(targetInstanceAttributeValue))
                                throw new BusinessRuleException(String.format("Value of %s in %s does not match %s in %s", 
                                                                        businessRuleNode.getProperty("constraint3"),
                                                                        targetObjectClassName, businessRuleNode.getProperty("constraint2"), sourceObjectClassName));
                            else
                                return; //After finding the first matching rule, return. This behavior might change in further releases
                        }
                    } else 
                        throw new BusinessRuleException(String.format("Objects of class %s can not be connected to objects of class %s", sourceObjectClassName, targetObjectClassName));
                }
            }
            throw new BusinessRuleException(String.format("No matching rule was found for %s and %s", sourceObjectClassName, targetObjectClassName));
        }
    }
    
    //</editor-fold>
   
    //<editor-fold desc="Process API" defaultstate="collapsed">
    @Override
    public Artifact getArtifactForActivity(String processInstanceId, String activityId) throws ApplicationObjectNotFoundException {
        return processManagerService.getArtifactForActivity(processInstanceId, activityId);
    }

    @Override
    public ArtifactDefinition getArtifactDefinitionForActivity(String processDefinitionId, String activityDefinitionId) throws ApplicationObjectNotFoundException {
        try {
            return processManagerService.getArtifactDefinitionForActivity(processDefinitionId, activityDefinitionId);
        } catch (InventoryException ex) {
            throw new ApplicationObjectNotFoundException(ex.getMessage());
        }
    }

    @Override
    public void commitActivity(String processInstanceId, String activityDefinitionId, Artifact artifact) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node processInstanceNode = Util.findNodeByLabelAndUuid(connectionManager.getConnectionHandler(), processInstanceLabel, processInstanceId);
            if (processInstanceNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The Process Instance with id %s could not be found", processInstanceId));
            
            try {
                processManagerService.commitActivity(processInstanceId, activityDefinitionId, artifact);
                
                ProcessInstance processInstace = processManagerService.getProcessInstance(processInstanceId);
                                
                processInstanceNode.setProperty(Constants.PROPERTY_CURRENT_ACTIVITY_ID, processInstace.getCurrentActivityId());
                                
                if (processInstace.getArtifactsContent() != null)
                    processInstanceNode.setProperty(Constants.PROPERTY_ARTIFACTS_CONTENT, processInstace.getArtifactsContent());
                
            } catch (InventoryException ex) {
                throw new InvalidArgumentException(String.format("The Process Instance could not be commited", processInstanceId));
            }
            
            tx.success();
        }
    }

    @Override
    public void updateActivity(String processInstanceId, String activityDefinitionId, Artifact artifact) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node processInstanceNode = Util.findNodeByLabelAndUuid(connectionManager.getConnectionHandler(), processInstanceLabel, processInstanceId);
            if (processInstanceNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The Process Instance with id %s could not be found", processInstanceId));
            
            try {
                Artifact anArtifact = processManagerService.getArtifact(processInstanceId, activityDefinitionId);
                if (anArtifact != null) {
                    anArtifact.setId(artifact.getId());
                    anArtifact.setName(artifact.getName());
                    anArtifact.setContentType(artifact.getContentType());
                    anArtifact.setContent(artifact.getContent());
                    anArtifact.setSharedInformation(artifact.getSharedInformation());
                    processManagerService.updateActivity(processInstanceId, activityDefinitionId, anArtifact);
                } else
                    processManagerService.updateActivity(processInstanceId, activityDefinitionId, artifact);
                                    
                ProcessInstance processInstace = processManagerService.getProcessInstance(processInstanceId);
                if (processInstace.getArtifactsContent() != null)
                    processInstanceNode.setProperty(Constants.PROPERTY_ARTIFACTS_CONTENT, processInstace.getArtifactsContent());
                
            } catch (InventoryException ex) {
                throw new InvalidArgumentException(String.format("The Process Instance could not be updated", processInstanceId));
            }
            tx.success();
        }
    }

    @Override
    public List<ActivityDefinition> getProcessInstanceActivitiesPath(String processInstanceId) throws InventoryException {
        return processManagerService.getProcessInstanceActivitiesPath(processInstanceId);
    }

    @Override
    public ActivityDefinition getNextActivityForProcessInstance(String processInstanceId) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try {
            return processManagerService.getNextActivityForProcessInstance(processInstanceId);
        } catch (InventoryException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public ProcessDefinition getProcessDefinition(String processDefinitionId) throws ApplicationObjectNotFoundException {
        return processManagerService.getProcessDefinition(processDefinitionId);
    }

    @Override
    public ActivityDefinition getActivityDefinition(String processDefinitionId, String activityDefinitionId) {
        try {
            return processManagerService.getActivityDefinition(processDefinitionId, activityDefinitionId);
        } catch (InventoryException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteProcessDefinition(String processDefinitionId) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateProcessDefinition(String processDefinitionId, List<StringPair> properties, byte[] structure) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createProcessDefinition(String name, String description, String version, boolean enabled, byte[] structure) throws InvalidArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ProcessInstance> getProcessInstances(String processDefinitionId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            ResourceIterator<Node> processInstanceNodes = connectionManager.getConnectionHandler().findNodes(processInstanceLabel);
            if (processInstanceNodes != null) {
                List<ProcessInstance> processInstances = new ArrayList();
                while (processInstanceNodes.hasNext()) {
                    Node processInstanceNode = processInstanceNodes.next();
                    ProcessInstance processInstance = Util.createProcessInstanceFromNode(processInstanceNode);
                    
                    if (processInstance.getProcessDefinitionId() != null && processInstance.getProcessDefinitionId().equals(processDefinitionId)) {
                        
                        processInstances.add(processInstance);
                        
                        try {
                            processManagerService.setProcessInstance(processInstance);
                        } catch (InventoryException ex) {
                            throw new ApplicationObjectNotFoundException(ex.getMessage());
                        }
                    }
                }
                try {
                    processManagerService.setProcessInstances(processDefinitionId, processInstances);
                } catch (InventoryException ex) {
                    throw new ApplicationObjectNotFoundException(ex.getMessage());
                }
            }
            tx.success();
        }
        try {
            return processManagerService.getProcessInstances(processDefinitionId);
        } catch (InventoryException ex) {
            throw new ApplicationObjectNotFoundException(ex.getMessage());
        }
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitions() {
        return processManagerService.getProcessDefinitions();
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node processInstanceNode = Util.findNodeByLabelAndUuid(connectionManager.getConnectionHandler(), processInstanceLabel, processInstanceId);
            if (processInstanceNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The Process Instance with id %s could not be found", processInstanceId));
            
            ProcessInstance processInstance = Util.createProcessInstanceFromNode(processInstanceNode);
            
            try {
                processManagerService.setProcessInstance(processInstance);
                return processManagerService.getProcessInstance(processInstance.getId());
            } catch (InventoryException ex) {
                throw new ApplicationObjectNotFoundException(String.format("The Process Instance with id %s could not be found", processInstanceId));
            }
        }
    }

    @Override
    public void reloadProcessDefinitions() throws InvalidArgumentException {
        processManagerService.updateProcessDefinitions();
    }

    @Override
    public String createProcessInstance(String processDefinitionId, String processInstanceName, String processInstanceDescription) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String newProcessInstanceId = UUID.randomUUID().toString();
            
            Node processInstanceNode = connectionManager.getConnectionHandler().createNode(processInstanceLabel);
            processInstanceNode.setProperty(Constants.PROPERTY_UUID, newProcessInstanceId);
            processInstanceNode.setProperty(Constants.PROPERTY_PROCESS_DEFINITION_ID, processDefinitionId);
            processInstanceNode.setProperty(Constants.PROPERTY_NAME, processInstanceName != null ? processInstanceName : "");
            processInstanceNode.setProperty(Constants.PROPERTY_DESCRIPTION, processInstanceDescription != null ? processInstanceDescription : "");
            
            try {
                String processInstanceId = processManagerService.createProcessInstance(newProcessInstanceId, processDefinitionId, processInstanceName, processInstanceDescription);
                
                ProcessInstance processInstance = processManagerService.getProcessInstance(processInstanceId);
                
                processInstanceNode.setProperty(Constants.PROPERTY_CURRENT_ACTIVITY_ID, processInstance.getCurrentActivityId());
                tx.success();
                return processInstanceId;
                
            } catch (InventoryException ex) {
                throw new ApplicationObjectNotFoundException(ex.getMessage());
            }
        }
    }
    
    @Override
    public void updateProcessInstance(String processInstanceId, String name, String description) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("processInstanceId", processInstanceId); //NOI18N
            parameters.put("name", name); //NOI18N
            parameters.put("description", description); //NOI18N
            
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (p:processInstance {_uuid: $processInstanceId})").append("\n"); //NOI18N
            queryBuilder.append("SET p.name = $name").append("\n"); //NOI18N
            queryBuilder.append("SET p.description = $description").append("\n"); //NOI18N
            
            connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            tx.success();
        }
    }

    @Override
    public void deleteProcessInstance(String processInstanceId) throws OperationNotPermittedException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node processInstanceNode = Util.findNodeByLabelAndUuid(connectionManager.getConnectionHandler(), processInstanceLabel, processInstanceId);
                        
            for (Relationship rel : processInstanceNode.getRelationships(RelTypes.HAS_PROCESS_INSTANCE, Direction.INCOMING)) {
                
                Node startNode = rel.getStartNode();
                rel.delete();
                deleteObject(startNode, false);
            }
            processInstanceNode.delete();
            
            tx.success();
        } catch(Exception ex) {
             throw new OperationNotPermittedException("Cannot delete process instance, because it still has relationships");
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Configuration Variables">
    @Override
    public long createConfigurationVariable(String configVariablesPoolId, String name, String description, int type, 
            boolean masked, String valueDefinition) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        if (type > 5 || type < 0)
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.invalid-type"), type));

        if (name == null || name.trim().isEmpty())
            throw  new InvalidArgumentException(ts.getTranslatedString("module.configman.error.configuration-variable-name-empty"));

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (connectionManager.getConnectionHandler().findNode(configurationVariables, Constants.PROPERTY_NAME, name) != null)
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.configman.error.configuration-variable-already-exists"), name));
            
            Node parentPoolNode = connectionManager.getConnectionHandler().findNode(configurationVariablesPools, Constants.PROPERTY_UUID, configVariablesPoolId);
            
            if (parentPoolNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.pool-id-not-found"), configVariablesPoolId));
            
            Node newConfigVariableNode = connectionManager.getConnectionHandler().createNode(configurationVariables);
            newConfigVariableNode.createRelationshipTo(parentPoolNode, RelTypes.CHILD_OF_SPECIAL);
            newConfigVariableNode.setProperty(Constants.PROPERTY_NAME, name);
            newConfigVariableNode.setProperty(Constants.PROPERTY_DESCRIPTION, description != null ? description : "");
            newConfigVariableNode.setProperty(Constants.PROPERTY_TYPE, type);
            newConfigVariableNode.setProperty(Constants.PROPERTY_MASKED, masked);
            newConfigVariableNode.setProperty(Constants.PROPERTY_VALUE, valueDefinition != null ? valueDefinition : "");
            
            tx.success();
            return newConfigVariableNode.getId();
        }
    }

    @Override
    public void updateConfigurationVariable(String name, String propertyToUpdate, String newValue) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node configVariableNode = connectionManager.getConnectionHandler().findNode(configurationVariables, Constants.PROPERTY_NAME, name);
            
            if (configVariableNode == null)
                throw new ApplicationObjectNotFoundException(String.format(
                        ts.getTranslatedString("module.configman.error.configuration-variable-not-found"), name));
            
            switch (propertyToUpdate) {
                case Constants.PROPERTY_NAME:
                    if (newValue == null || newValue.trim().isEmpty())
                        throw  new InvalidArgumentException(ts.getTranslatedString("module.configman.error.configuration-variable-name-empty"));
                    configVariableNode.setProperty(Constants.PROPERTY_NAME, newValue);
                    break;
                case Constants.PROPERTY_DESCRIPTION:
                    configVariableNode.setProperty(Constants.PROPERTY_DESCRIPTION, newValue);
                    break;
                case Constants.PROPERTY_TYPE:
                    try {
                        int type = Integer.valueOf(newValue);
                        
                        if (type > 5 || type < 0)
                            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.invalid-type"), type));
                        
                        configVariableNode.setProperty(Constants.PROPERTY_TYPE, type);
                    } catch (NumberFormatException ex) {
                        throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.invalid-type-number"), newValue));
                    }
                    break;
                case Constants.PROPERTY_MASKED:
                    configVariableNode.setProperty(Constants.PROPERTY_MASKED, Boolean.valueOf(newValue));
                    break;
                case Constants.PROPERTY_VALUE:
                    configVariableNode.setProperty(Constants.PROPERTY_VALUE, newValue);
                    break;
                default:
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.invalid-property"), propertyToUpdate));
            }
            
            tx.success();
            cm.removeConfigurationVariableValue(name); //Removes the variable from the cache, it will be cached the next time it is requested 
        }
    }

    @Override
    public void deleteConfigurationVariable(String name, String userName) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node configVariableNode = connectionManager.getConnectionHandler().findNode(configurationVariables, Constants.PROPERTY_NAME, name);
            
            if (configVariableNode == null)
                throw new ApplicationObjectNotFoundException(
                        String.format(ts.getTranslatedString("module.configman.error.configuration-variable-not-found"), name));
            
            configVariableNode.getRelationships().forEach((relationship) -> {
                relationship.delete();
            });
            
            configVariableNode.delete();
            tx.success();
            cm.removeConfigurationVariableValue(name);
            
            createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                    String.format(ts.getTranslatedString("module.configman.actions.delete-configuration-variable.ui.deleted-log"), name));
        }
    }

    @Override
    public ConfigurationVariable getConfigurationVariable(String name) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node configVariableNode = connectionManager.getConnectionHandler().findNode(configurationVariables, Constants.PROPERTY_NAME, name);
            
            tx.success();
            if (configVariableNode == null)
                throw new ApplicationObjectNotFoundException(
                        String.format(ts.getTranslatedString("module.configman.error.configuration-variable-not-found"), name));
            
            return new ConfigurationVariable(configVariableNode.getId(), (String)configVariableNode.getProperty(Constants.PROPERTY_NAME), 
                    (String)configVariableNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                    (String)configVariableNode.getProperty(Constants.PROPERTY_VALUE), 
                    (boolean)configVariableNode.getProperty(Constants.PROPERTY_MASKED), 
                     (int)configVariableNode.getProperty(Constants.PROPERTY_TYPE));
        }
    }

    @Override
    public Object getConfigurationVariableValue(String name) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        if (cm.getConfigurationVariableValue(name) != null)
            return cm.getConfigurationVariableValue(name);
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node configVariableNode = connectionManager.getConnectionHandler().findNode(configurationVariables, Constants.PROPERTY_NAME, name);
            
            if (configVariableNode == null) 
                throw new ApplicationObjectNotFoundException(
                        String.format(ts.getTranslatedString("module.configman.error.configuration-variable-not-found"), name));
            
            String rawConfigVariableValue = (String)configVariableNode.getProperty(Constants.PROPERTY_VALUE); //The values are always stored as string, serialized versions of an object.
            Object realConfigVariableValue = null;
            switch ((int)configVariableNode.getProperty(Constants.PROPERTY_TYPE)) {
                case ConfigurationVariable.TYPE_STRING:
                    realConfigVariableValue = rawConfigVariableValue;
                    break;
                case ConfigurationVariable.TYPE_INTEGER:
                    try {
                        realConfigVariableValue = Integer.valueOf(rawConfigVariableValue);
                    } catch (NumberFormatException nfex) {
                        throw new InvalidArgumentException(
                                String.format(ts.getTranslatedString("module.configman.error.configuration-variable-converted-to-integer"), name, rawConfigVariableValue));
                    }
                    break;
                case ConfigurationVariable.TYPE_FLOAT:
                    try {
                        //In Java, Floats are 32-bits numbers, while Doubles are 64. For the sake of simplicity, while taking a bit more memory, all floats will be taken as doubles
                        realConfigVariableValue =  Double.valueOf(rawConfigVariableValue);
                    } catch (NumberFormatException nfex) {
                        throw new InvalidArgumentException(
                                String.format(ts.getTranslatedString("module.configman.error.configuration-variable-converted-to-float"), name, rawConfigVariableValue));
                    }
                    break;
                case ConfigurationVariable.TYPE_BOOLEAN:
                    realConfigVariableValue = Boolean.valueOf(rawConfigVariableValue);
                    break;
                case ConfigurationVariable.TYPE_ARRAY: //Not implemented yet
                    return rawConfigVariableValue.split(",");
                case ConfigurationVariable.TYPE_MATRIX:
                    return new ArrayList<>();
                default:
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.invalid-property"), name));
            }
            
            cm.addConfigurationValue(name, realConfigVariableValue);
            tx.success();
            return realConfigVariableValue;
        }
    }

    @Override
    public List<ConfigurationVariable> getConfigurationVariablesInPool(String poolId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentPoolNode = connectionManager.getConnectionHandler().findNode(configurationVariablesPools, Constants.PROPERTY_UUID, poolId);
            
            if (parentPoolNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.pool-id-not-found"), poolId));
            
            List<ConfigurationVariable> result = new ArrayList<>();
            
            parentPoolNode.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING).forEach((childOfRelationship) -> {
                Node configVariableNode = childOfRelationship.getStartNode();
                
                result.add(new ConfigurationVariable(configVariableNode.getId(), (String)configVariableNode.getProperty(Constants.PROPERTY_NAME), 
                    (String)configVariableNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                    (String)configVariableNode.getProperty(Constants.PROPERTY_VALUE), 
                    (boolean)configVariableNode.getProperty(Constants.PROPERTY_MASKED), 
                     (int)configVariableNode.getProperty(Constants.PROPERTY_TYPE)));
            });
            
            tx.success();
            
            result.sort((o1, o2) -> {
                return o1.getName().compareTo(o2.getName());
            });
            
            return result;
        }
    }
    
    @Override
    public List<ConfigurationVariable> getConfigurationVariablesWithPrefix(String prefix) {
        if (prefix == null)
            return new ArrayList();
        if (prefix.isEmpty())
            return new ArrayList();
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            ResourceIterator<Node> configVariableNodes = connectionManager.getConnectionHandler().findNodes(configurationVariables);
            
            List<ConfigurationVariable> result = new ArrayList<>();
            
            while (configVariableNodes.hasNext()) {
                Node configVariableNode = configVariableNodes.next();
                
                String name = (String) configVariableNode.getProperty(Constants.PROPERTY_NAME);
                
                if (name != null && name.startsWith(prefix)) {
                    ConfigurationVariable configurationVariable = new ConfigurationVariable(configVariableNode.getId(), (String)configVariableNode.getProperty(Constants.PROPERTY_NAME), 
                                                (String)configVariableNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                                                (String)configVariableNode.getProperty(Constants.PROPERTY_VALUE), 
                                                (boolean)configVariableNode.getProperty(Constants.PROPERTY_MASKED), 
                                                 (int)configVariableNode.getProperty(Constants.PROPERTY_TYPE));
                    
                    result.add(configurationVariable);
                }
            }
            tx.success();
            return result;            
        }
    }
    
    @Override
    public List<ConfigurationVariable> getAllConfigurationVariables() {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = "MATCH (variable:" + configurationVariables +") RETURN variable ORDER BY variable.name ASC";
            List<ConfigurationVariable> result = new ArrayList<>();
            Result queryResult = connectionManager.getConnectionHandler().execute(query);
            queryResult.columnAs("variable").stream().forEach( aQueryResult -> {
                Node configVariableNode = (Node)aQueryResult;
                result.add(new ConfigurationVariable(configVariableNode.getId(), 
                        (String)configVariableNode.getProperty(Constants.PROPERTY_NAME), 
                        (String)configVariableNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                        (String)configVariableNode.getProperty(Constants.PROPERTY_VALUE), 
                        (boolean)configVariableNode.getProperty(Constants.PROPERTY_MASKED), 
                        (int)configVariableNode.getProperty(Constants.PROPERTY_TYPE)));
            });
            tx.success();
            return result;            
        }
    }

    @Override
    public List<InventoryObjectPool> getConfigurationVariablesPools() {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<InventoryObjectPool> res = new ArrayList<>();
            connectionManager.getConnectionHandler().findNodes(configurationVariablesPools).stream().forEach((poolNode) -> {
                if (!poolNode.hasProperty(Constants.PROPERTY_UUID))
                    poolNode.setProperty(Constants.PROPERTY_UUID, UUID.randomUUID().toString());
                res.add(new InventoryObjectPool((String)poolNode.getProperty(Constants.PROPERTY_UUID), (String)poolNode.getProperty(Constants.PROPERTY_NAME), (String)poolNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                        ts.getTranslatedString("apis.persistence.aem.labels.pool-config-variables"), POOL_TYPE_MODULE_ROOT));
            });
            
            tx.success();
            
            res.sort((o1, o2) -> {
                return o1.getName().compareTo(o2.getName());
            });
            
            return res;
        }
    }

    @Override
    public String createConfigurationVariablesPool(String name, String description) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (name == null || name.trim().isEmpty())
            throw  new InvalidArgumentException(ts.getTranslatedString("module.configman.error.configuration-variable-pool-name-empty"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newPoolNode = connectionManager.getConnectionHandler().createNode(configurationVariablesPools);
            String poolId = UUID.randomUUID().toString();
            newPoolNode.setProperty(Constants.PROPERTY_UUID, poolId);
            newPoolNode.setProperty(Constants.PROPERTY_NAME, name);
            newPoolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);
            
            tx.success();
            return poolId;
        }
    }

    @Override
    public void updateConfigurationVariablesPool(String poolId, String propertyToUpdate, String value, String userName) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node poolNode = connectionManager.getConnectionHandler().findNode(configurationVariablesPools, Constants.PROPERTY_UUID, poolId);
            
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(
                        String.format(ts.getTranslatedString("module.general.messages.pool-id-not-found"), poolId));
            
            String oldValue;
            switch(propertyToUpdate) {
                case Constants.PROPERTY_NAME:
                    if (value == null || value.trim().isEmpty())
                        throw  new InvalidArgumentException(ts.getTranslatedString("module.general.messages.pool-name-not-empty"));
                    oldValue = poolNode.getProperty(Constants.PROPERTY_NAME).toString();
                    poolNode.setProperty(Constants.PROPERTY_NAME, value);
                    break;
                case Constants.PROPERTY_DESCRIPTION:
                    oldValue = poolNode.getProperty(Constants.PROPERTY_DESCRIPTION).toString();
                    poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, value == null ? "" : value);
                    break;
                default:
                    throw new InvalidArgumentException(
                            String.format(ts.getTranslatedString("module.general.messages.invalid-property"), propertyToUpdate));
            }
            
            tx.success();
            
            ChangeDescriptor changeDescriptor = new ChangeDescriptor(propertyToUpdate, oldValue, value,
                        String.format(ts.getTranslatedString("module.configman.actions.update-configuration-variable-pool.ui.pool-updated-log"), poolId));
            createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, changeDescriptor);
        }
    }

    @Override
    public void deleteConfigurationVariablesPool(String poolId, String userName) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node poolNode = connectionManager.getConnectionHandler().findNode(configurationVariablesPools, Constants.PROPERTY_UUID, poolId);
            
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(
                        String.format(ts.getTranslatedString("module.general.messages.pool-id-not-found"), poolId));
            
            poolNode.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING).forEach((childOfRelationship) -> {
                Node configVariableNode = childOfRelationship.getStartNode();
                childOfRelationship.delete();
                configVariableNode.delete();
            });
            
            poolNode.delete();
            tx.success();
            
            createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT,
                    String.format(ts.getTranslatedString("module.configman.actions.delete-configuration-variable-pool.ui.pool-deleted-log"), poolId));
        }
    }
    // </editor-fold>
    
    @Override
    public long createOSPView(String name, String description, byte[] structure) throws InvalidArgumentException {
        
        if (name == null || name.trim().isEmpty())
            throw new InvalidArgumentException("The name of the view can not be empty");
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node newViewNode = connectionManager.getConnectionHandler().createNode(Label.label("ospViews")); //NOI18N
            newViewNode.setProperty(Constants.PROPERTY_NAME, name);
            newViewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);
            newViewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure == null ? new byte[0] : structure);
            
            tx.success();
            return newViewNode.getId();
        }
    }

    @Override
    public ViewObject getOSPView(long viewId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node ospViewNode = connectionManager.getConnectionHandler().findNodes(Label.label("ospViews")).stream().filter((viewNode) -> { //NOI18N
                return viewNode.getId() == viewId; 
            }).findFirst().get();
            
            if (ospViewNode == null) 
                throw new ApplicationObjectNotFoundException(String.format("OSP view with id %s could not be found", viewId));
            
            ViewObject res = new ViewObject(viewId, (String)ospViewNode.getProperty(Constants.PROPERTY_NAME), 
                    (String)ospViewNode.getProperty(Constants.PROPERTY_DESCRIPTION), "OSPView"); //NOI18N
            
            res.setStructure((byte[])ospViewNode.getProperty(Constants.PROPERTY_STRUCTURE));
            tx.success();
            
            return res;
        }
    }

    @Override
    public List<ViewObjectLight> getOSPViews() throws InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<ViewObjectLight> res = new ArrayList<>();
            connectionManager.getConnectionHandler().findNodes(Label.label("ospViews")).stream().forEach((viewNode) -> {
                res.add(new ViewObjectLight(viewNode.getId(), (String)viewNode.getProperty(Constants.PROPERTY_NAME), 
                        (String)viewNode.getProperty(Constants.PROPERTY_DESCRIPTION), "OSPView")); //NOI18N
            });
            
            tx.success();
            return res;
        }
    }

    @Override
    public void updateOSPView(long viewId, String name, String description, byte[] structure) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node ospViewNode = connectionManager.getConnectionHandler().findNodes(Label.label("ospViews")).stream().filter((viewNode) -> { //NOI18N
                return viewNode.getId() == viewId; 
            }).findFirst().get();
            
            if (ospViewNode == null) 
                throw new ApplicationObjectNotFoundException(String.format("OSP view with id %s could not be found", viewId));
            
            if (name != null) {
                if (name.trim().isEmpty())
                    throw new InvalidArgumentException("The name of the view can not be empty");
                else
                    ospViewNode.setProperty(Constants.PROPERTY_NAME, name);
            }
            
            if (description != null)
                ospViewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            
            if (structure != null)
                ospViewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            
            tx.success();
        }
    }
    //</editor-fold>
    
    //<editor-fold desc="Validators" defaultstate="collapsed">
    @Override
    public long createValidatorDefinition(String name, String description, String classToBeApplied, String script, boolean enabled, String userName) 
            throws InvalidArgumentException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        
        if (name == null || name.trim().isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("module.configman.validators.validator-name-null"));
        
        if (classToBeApplied == null || classToBeApplied.trim().isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("module.configman.validators.validator-class-not-valid"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            mem.getClass(classToBeApplied); //Checks that the class provided does exist
            
            Node newValidatorNode = connectionManager.getConnectionHandler().createNode(validatorDefinitions);
            
            newValidatorNode.setProperty(Constants.PROPERTY_NAME, name);
            newValidatorNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);
            newValidatorNode.setProperty(Constants.PROPERTY_SCRIPT, script == null ? "" : script.trim());
            newValidatorNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
            newValidatorNode.setProperty(Constants.PROPERTY_CLASSNAME, classToBeApplied);
            
            tx.success();
            
            //While not entirely efficient, this will clear all cached validator definitions to prevent that a validator definition 
            //associated to a super class is missed by the caching system
            cm.clearValidatorDefinitionsCache();
            
            createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT,
                     String.format(ts.getTranslatedString("module.configman.validators.actions.new-validator.ui.created-log"), name));
            
            return newValidatorNode.getId();
        } 
    }
    
    @Override
    public void updateValidatorDefinition(long validatorDefinitionId, String name, String description
            , String classToBeApplied, String script, Boolean enabled, String userName) 
            throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node validatorDefinitionNode = connectionManager.getConnectionHandler().findNodes(validatorDefinitions).stream().filter((aValidatorDefinitionNode) -> {
                return aValidatorDefinitionNode.getId() == validatorDefinitionId; 
            }).findFirst().get();
            
            if (validatorDefinitionNode == null)
                throw new ApplicationObjectNotFoundException(
                        String.format(ts.getTranslatedString("module.configman.validators.validator-id-not-found"), validatorDefinitionId));
            
            if (name != null) {
                if (name.trim().isEmpty())
                    throw new InvalidArgumentException(ts.getTranslatedString("module.configman.validators.validator-name-null"));
                
                validatorDefinitionNode.setProperty(Constants.PROPERTY_NAME, name);
            }
            
            if (description != null)
                validatorDefinitionNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            
            if (classToBeApplied != null) {
                mem.getClass(classToBeApplied); //Check if the class does exist
                validatorDefinitionNode.setProperty(Constants.PROPERTY_CLASSNAME, classToBeApplied);
            }
            
            if (script != null)
                validatorDefinitionNode.setProperty(Constants.PROPERTY_SCRIPT, script);
            
            if (enabled != null)
                validatorDefinitionNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
            
            //While not entirely efficient, this will clear all cached validator definitions to prevent that a validator definition 
            //associated to a super class is missed by the caching system
            cm.clearValidatorDefinitionsCache();
            tx.success();
            
            createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT,
                    String.format(ts.getTranslatedString("module.configman.validators.actions.update-validator.ui.updated-log"), validatorDefinitionId));
        }
    }
    
    @Override
    public List<ValidatorDefinition> getValidatorDefinitionsForClass(String className) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<ValidatorDefinition> res = new ArrayList<>();
            connectionManager.getConnectionHandler().findNodes(validatorDefinitions).stream().filter((aValidatorNode) -> {
                    return aValidatorNode.getProperty(Constants.PROPERTY_CLASSNAME).equals(className);
                }).forEach((aValidatorDefinitionNode) -> {
                    res.add(new ValidatorDefinition(aValidatorDefinitionNode.getId(),
                            (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_NAME), 
                            (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                            (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_CLASSNAME), 
                            (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_SCRIPT), 
                            (boolean)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_ENABLED)));
                });
            tx.success();
            Collections.sort(res);
            return res;
        }
    }
    
    @Override
    public List<ValidatorDefinition> getAllValidatorDefinitions() {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<ValidatorDefinition> res = new ArrayList<>();
            String query = "MATCH (validator:" + validatorDefinitions + ") RETURN validator ORDER BY validator.name ASC";
            Result result = connectionManager.getConnectionHandler().execute(query);
            while (result.hasNext()) {
                Node node = (Node) result.next().get("validator");
                res.add(new ValidatorDefinition(node.getId(),
                            (String)node.getProperty(Constants.PROPERTY_NAME), 
                            (String)node.getProperty(Constants.PROPERTY_DESCRIPTION), 
                            (String)node.getProperty(Constants.PROPERTY_CLASSNAME), 
                            (String)node.getProperty(Constants.PROPERTY_SCRIPT), 
                            (boolean)node.getProperty(Constants.PROPERTY_ENABLED)));
            }
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<Validator> runValidationsForObject(String objectClass, long objectId) throws BusinessObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<Validator> res = new ArrayList<>();
            connectionManager.getConnectionHandler().findNodes(validatorDefinitions).forEachRemaining((aValidatorDefinitionNode) -> { // Is it worth to cache this?
                String script = (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_SCRIPT);
                
                if (!script.trim().isEmpty()) { //Skip uninitialized scripts
                    Binding environmentParameters = new Binding();
                    environmentParameters.setVariable(Constants.PROPERTY_CLASSNAME, objectClass);
                    environmentParameters.setVariable(Constants.PROPERTY_ID, objectId);
                    try {
                        if ((boolean)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_ENABLED) && 
                                mem.isSubclassOf((String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_CLASSNAME), objectClass)) {
                            GroovyShell shell = new GroovyShell(ApplicationEntityManager.class.getClassLoader(), environmentParameters);
                            Object theResult = shell.evaluate(script);

                            if (theResult instanceof Validator) //The script must return a validator, otherwise, the result will be ignored
                                res.add((Validator)theResult);
                        }
                    } catch (MetadataObjectNotFoundException ex) { } // The validators referring to inexistent classes are ignored
                }
            });
            tx.failure(); //Rollback any non-nested transaction just in case
            return res;
        }
    }
    
    @Override
    public void deleteValidatorDefinition(long validatorDefinitionId, String userName) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node validatorDefinitionNode = connectionManager.getConnectionHandler().findNodes(validatorDefinitions).stream().filter((aValidatorDefinitionNode) -> {
                return aValidatorDefinitionNode.getId() == validatorDefinitionId; 
            }).findFirst().get();
            
            if (validatorDefinitionNode == null)
                throw new ApplicationObjectNotFoundException(
                        String.format(ts.getTranslatedString("module.configman.validators.validator-id-not-found"), validatorDefinitionId));
            
            validatorDefinitionNode.delete();
            
            //While not entirely efficient, this will clear all cached validator definitions to prevent that a validator definition 
            //associated to a super class is missed by the caching system
            cm.clearValidatorDefinitionsCache();
            tx.success();
            
            createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT,
                     String.format(ts.getTranslatedString("module.configman.validators.actions.delete-validator.ui.deleted-log"), validatorDefinitionId));
        }
    }
    //</editor-fold>
    
    //<editor-fold desc="Outside Plant" defaultstate="collapsed">
    @Override    
    public void deleteOSPView(long viewId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node ospViewNode = connectionManager.getConnectionHandler().findNodes(Label.label("ospViews")).stream().filter((viewNode) -> { //NOI18N
                return viewNode.getId() == viewId; 
            }).findFirst().get();
            
            if (ospViewNode == null) 
                throw new ApplicationObjectNotFoundException(String.format("OSP view with id %s could not be found", viewId));
            
            ospViewNode.delete();
            tx.success();
        }
    }

    //</editor-fold>
    //</editor-fold>
//Helpers
    private Node getFavoritesFolderForUser(long favoritesFolderId, long userId) {
        Node userNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), userLabel, userId);

        if (userNode == null)
            return null; // user not found
        
        
        if (userNode.hasRelationship(Direction.OUTGOING, RelTypes.HAS_BOOKMARK)) {
            for (Relationship relationship : userNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_BOOKMARK)) {

                Node favoritesFolderNode = relationship.getEndNode();

                if (favoritesFolderNode.getId() == favoritesFolderId)
                    return favoritesFolderNode;

            }
        }
        return null; //The user doesn't seem to have a favorites folder with that id
    }
      
    public TemplateObject createTemplateElementFromNode (Node templateInstanceNode, String className) throws InvalidArgumentException, 
            MetadataObjectNotFoundException {
        ClassMetadata classMetadata = mem.getClass(className);
        HashMap<String, String> attributes = new HashMap<>();
        String name = "";
        
        for (AttributeMetadata attribute : classMetadata.getAttributes()) {
            //Only set the attributes existing in the current node. Please note that properties can't be null in
            //Neo4J, so a null value is actually a non-existing relationship/value
            if (templateInstanceNode.hasProperty(attribute.getName())) {
               String value = String.valueOf(templateInstanceNode.getProperty(attribute.getName()));
                        
                if (Constants.PROPERTY_NAME.equals(attribute.getName()))
                    name = value;

                attributes.put(attribute.getName(),value);
            }
        }
        
        for(Relationship aRelatedToRelationship : templateInstanceNode.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING)) {
            if (!aRelatedToRelationship.hasProperty(Constants.PROPERTY_NAME)) {
                InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".22"), templateInstanceNode.getId()));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(22);
                ex.setMessageArgs(templateInstanceNode.getId());
                throw ex;
            }
            
            String relationshipName = (String)aRelatedToRelationship.getProperty(Constants.PROPERTY_NAME);              
            
            if (classMetadata.hasAttribute(relationshipName)) { 
                if (attributes.containsKey(relationshipName))
                    attributes.put(relationshipName, attributes.containsKey(relationshipName) ? "" : attributes.get(relationshipName) + ";" + aRelatedToRelationship.getEndNode().getId());
                else
                    attributes.put(relationshipName, (String)aRelatedToRelationship.getEndNode().getProperty(Constants.PROPERTY_UUID));
            } else {//This verification will help us find potential inconsistencies with list types
                    //What this does is to verify if is there is a RELATED_TO relationship that shouldn't exist because its name is not an attribute of the class
                InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".23"), 
                            templateInstanceNode.getProperty(Constants.PROPERTY_NAME), templateInstanceNode.getId(), aRelatedToRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME), 
                            aRelatedToRelationship.getEndNode().getId()));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(23);
                ex.setMessageArgs(templateInstanceNode.getProperty(Constants.PROPERTY_NAME), templateInstanceNode.getId(), aRelatedToRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME), 
                    aRelatedToRelationship.getEndNode().getId());
                throw ex;
            }
        } 

        return new TemplateObject(classMetadata.getName(), (String)templateInstanceNode.getProperty(Constants.PROPERTY_UUID), name, attributes);
    }
   
    /**
     * TODO: The following two methods are duplicated in BEM, this should be re-designed
     */
    private BusinessObjectLight createRemoteObjectLightFromNode (Node instance) throws InvalidArgumentException {
        Node classNode = instance.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode();
        
        String instanceUuid = instance.hasProperty(Constants.PROPERTY_UUID) ? (String) instance.getProperty(Constants.PROPERTY_UUID) : null;
        if (instanceUuid == null) {
            InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".24"), instance.getId()));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(24);
            ex.setMessageArgs(instance.getId());
            throw ex;
        }
        
        BusinessObjectLight res = new BusinessObjectLight((String)classNode.getProperty(Constants.PROPERTY_NAME), instanceUuid, 
            (String)instance.getProperty(Constants.PROPERTY_NAME),
            (String)classNode.getProperty(Constants.PROPERTY_DISPLAY_NAME, null));
        
        return res;
    }
    
    private BusinessObject createBusinessObjectFromNode(Node instance) throws InvalidArgumentException {
        String className = (String)instance.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME);
        try {
            return createBusinessObjectFromNode(instance, mem.getClass(className));
        } catch (MetadataObjectNotFoundException mex) {
            throw new InvalidArgumentException(mex.getLocalizedMessage());
        }
    }
    
    /**
     * Builds a RemoteBusinessObject instance from a node representing a business object
     * @param instance The object as a Node instance.
     * @param classMetadata The class metadata to map the node's properties into a RemoteBussinessObject.
     * @return The business object.
     * @throws InvalidArgumentException If an attribute value can't be mapped into value.
     */
    private BusinessObject createBusinessObjectFromNode(Node instance, ClassMetadata classMetadata) throws InvalidArgumentException {
        
        HashMap<String, Object> attributes = new HashMap<>();
        String name = "";
        
        for (AttributeMetadata myAtt : classMetadata.getAttributes()) {
            //Only set the attributes existing in the current node. Please note that properties can't be null in
            //Neo4J, so a null value is actually a non-existing relationship/value
            if (instance.hasProperty(myAtt.getName())) {
               if (AttributeMetadata.isPrimitive(myAtt.getType())) {
                    String value = String.valueOf(instance.getProperty(myAtt.getName()));

                    if (Constants.PROPERTY_NAME.equals(myAtt.getName()))
                        name = value;

                    attributes.put(myAtt.getName(),value);
                }
            }
        }

        //Iterates through relationships and transform the into "plain" attributes
        Iterable<Relationship> iterableRelationships = instance.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING);
        Iterator<Relationship> relationships = iterableRelationships.iterator();

        while(relationships.hasNext()) {
            Relationship relationship = relationships.next();
            if (!relationship.hasProperty(Constants.PROPERTY_NAME))
                throw new InvalidArgumentException(String.format("The object with id %s is malformed", instance.getId()));

            String relationshipName = (String)relationship.getProperty(Constants.PROPERTY_NAME);              
            
            boolean hasRelationship = false;
            for (AttributeMetadata myAtt : classMetadata.getAttributes()) {
                if (myAtt.getName().equals(relationshipName)) {
                    if (attributes.containsKey(relationshipName))
                        attributes.put(relationshipName, attributes.get(relationshipName) + ";" + (String)relationship.getEndNode().getProperty(Constants.PROPERTY_UUID)); //A multiple selection list type
                    else    
                        attributes.put(relationshipName, (String)relationship.getEndNode().getProperty(Constants.PROPERTY_UUID));
                    hasRelationship = true;
                    break;
                }                  
            }
            
            if (!hasRelationship) //This verification will help us find potential inconsistencies with list types
                                  //What this does is to verify if is there is a RELATED_TO relationship that shouldn't exist because its name is not an attribute of the class
                throw new InvalidArgumentException(String.format("The object with %s (%s) is related to list type %s (%s), but that is not consistent with the data model", 
                            instance.getProperty(Constants.PROPERTY_NAME), instance.getId(), relationship.getEndNode().getProperty(Constants.PROPERTY_NAME), relationship.getEndNode().getId()));
        }
        String instanceUuid = instance.hasProperty(Constants.PROPERTY_UUID) ? (String) instance.getProperty(Constants.PROPERTY_UUID) : null;
        if (instanceUuid == null)
            throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", instance.getId()));
        
        BusinessObject res = new BusinessObject(classMetadata.getName(), instanceUuid, name, attributes);
        
        return res;
    }
    
    //The following methods are used 
    
    /**
     * Deletes recursively and object and all its children. Note that the transaction should be handled by the caller
     * @param instance The object to be deleted
     * @param unsafeDeletion True if you want the object to be deleted no matter if it has RELATED_TO, HAS_PROCESS_INSTANCE or RELATED_TO_SPECIAL relationships
     * @throws org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException If the object already has relationships
     */
    private void deleteObject(Node instance, boolean unsafeDeletion) throws OperationNotPermittedException {
        if(!unsafeDeletion && instance.hasRelationship(RelTypes.RELATED_TO_SPECIAL, RelTypes.HAS_PROCESS_INSTANCE)) 
            throw new OperationNotPermittedException(String.format("The object with %s (%s) can not be deleted since it has relationships", 
                    instance.getProperty(Constants.PROPERTY_NAME), instance.getProperty(Constants.PROPERTY_UUID)));
        
        for (Relationship rel : instance.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF, RelTypes.CHILD_OF_SPECIAL))
            deleteObject(rel.getStartNode(), unsafeDeletion);
        
        // Searches the related views to delete the nodes in the data base       
        for (Relationship aHasViewRelationship : instance.getRelationships(RelTypes.HAS_VIEW)) {
            Node viewNode = aHasViewRelationship.getEndNode();
            aHasViewRelationship.delete();
            viewNode.delete();
        }
        
        //Now we delete the audit trail entries
        for (Relationship aHasHistoryEntryRelationship : instance.getRelationships(RelTypes.HAS_HISTORY_ENTRY)) {
            Node historyEntryNode = aHasHistoryEntryRelationship.getEndNode();
            aHasHistoryEntryRelationship.delete();
            historyEntryNode.delete();
        }
        
        //Now we dispose of the attachments
        for (Relationship aHasAttachmentRelationship : instance.getRelationships(RelTypes.HAS_ATTACHMENT)) {
            Node attachmentNode = aHasAttachmentRelationship.getEndNode();
            aHasAttachmentRelationship.delete();
            
            String fileName = instance.getProperty(Constants.PROPERTY_UUID) + "_" + attachmentNode.getId(); //NOI18N
            try {
                Files.deleteIfExists(Paths.get(fileName));
                File fileToBeDeleted = new File(fileName);
                fileToBeDeleted.delete();
            } catch (IOException ex) {
                System.out.println(String.format("[KUWAIBA] [%s] An error occurred while deleting attachment %s for object %s (%s)", 
                        Calendar.getInstance().getTime(), fileName, instance.getProperty(Constants.PROPERTY_NAME), instance.getId()));
            }
            attachmentNode.delete();
        }
        
        for (Relationship rel : instance.getRelationships())
            rel.delete();

        instance.delete();
    }
    
    //<editor-fold desc="Filters" defaultstate="collapsed">
    @Override
    public long createFilterDefinition(String name, String description, String className, String script, boolean enabled) 
            throws InvalidArgumentException, MetadataObjectNotFoundException {
        if (name == null || name.trim().isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.filert-name-not-null"));
        
        if (className == null || className.trim().isEmpty())
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.mem.messages.class-not-found"), className));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            mem.getClass(className); //Checks that the class provided does exist
            
            Node newFilterNode = connectionManager.getConnectionHandler().createNode(filterDefinitionsLabel);
            
            newFilterNode.setProperty(Constants.PROPERTY_NAME, name);
            newFilterNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);
            newFilterNode.setProperty(Constants.PROPERTY_SCRIPT, script == null ? "" : script.trim());
            newFilterNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
            newFilterNode.setProperty(Constants.PROPERTY_CLASSNAME, className);
            
            tx.success();
            //we don't cached a filter in the creation because a filter it not useful until it has a copiled filter
            //and a filter is compiled after the update of the script of the filter definition.
            return newFilterNode.getId();
        }
    }
    
    @Override
    public void updateFilterDefinition(long filterId, String name, String description, String className, String script, Boolean enabled) 
            throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ScriptNotCompiledException {
        
        FilterDefinition filterDefinition;
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node filterNode = connectionManager.getConnectionHandler().findNodes(filterDefinitionsLabel).stream().filter((aFilterNode) -> {
                return aFilterNode.getId() == filterId; 
            }).findFirst().get();
             
            if (filterNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.filert-not-found"), filterId));
            
            filterDefinition = new FilterDefinition(filterNode.getId(),
                        (String)filterNode.getProperty(Constants.PROPERTY_NAME),
                        (String)filterNode.getProperty(Constants.PROPERTY_DESCRIPTION),
                        (String)filterNode.getProperty(Constants.PROPERTY_CLASSNAME),
                        (String)filterNode.getProperty(Constants.PROPERTY_SCRIPT),
                        (boolean)filterNode.getProperty(Constants.PROPERTY_ENABLED));

            if (name != null) {
                if (name.trim().isEmpty())
                    throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.filert-name-not-null"));
                
                filterNode.setProperty(Constants.PROPERTY_NAME, name);
                filterDefinition.setName(name);
            }
            
            if (description != null){
                filterNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
                filterDefinition.setDescription(description);
            }
            
            if (script != null){
                filterNode.setProperty(Constants.PROPERTY_SCRIPT, script);
                //every time the script is modify we try to compile an create the filter
                if(!script.isEmpty()){
                    filterDefinition.setScript(script);
                    filterDefinition.setFilter(compileFilterDefinition(filterId, script));
                }
            }
            if (enabled != null){
                filterNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
                filterDefinition.setEnabled(enabled);
                String savedScript = (String)filterNode.getProperty(Constants.PROPERTY_SCRIPT);
                //We also try to compile the filter in case the filter is enable
                if(enabled && !savedScript.isEmpty() && filterDefinition.getFilter() == null)
                    filterDefinition.setFilter(compileFilterDefinition(filterId, savedScript));
            }
            tx.success();
            
            cm.updateFilterInFilterDefinition((String)filterNode.getProperty(Constants.PROPERTY_CLASSNAME), filterDefinition);
        }
    }

    @Override
    public List<FilterDefinition> getFilterDefinitionsForClass(String className, 
            boolean includeParentClassesFilters,
            boolean ignoreCache, HashMap<String, Object> attributesToFilter, 
            int page, int limit) 
            throws InvalidArgumentException
    {
        if (className == null)
                throw new InvalidArgumentException(String.format(
                        ts.getTranslatedString("module.general.messages.parameter-not-found"), "className"));
        
        List<FilterDefinition> res = null;
        List<ClassMetadataLight> upstreamClassHierarchy = new ArrayList<>();
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if(includeParentClassesFilters) //if we need the parent's filters
                upstreamClassHierarchy = mem.getUpstreamClassHierarchy(className, false);

            if(!ignoreCache) {
                List<FilterDefinition> cacheRes = new ArrayList<>();
                List<FilterDefinition> cache = cm.getFilterDefinitionsForClass(className);
                if(cache != null)
                    cacheRes.addAll(cache);

                if(includeParentClassesFilters){
                    for (ClassMetadataLight c : upstreamClassHierarchy){
                        List<FilterDefinition> parentfilters = cm.getFilterDefinitionsForClass(c.getName());
                        if(parentfilters != null)
                            cacheRes.addAll(parentfilters);
                    }
                }
                if(attributesToFilter.containsKey(Constants.PROPERTY_ENABLED) 
                        && (Boolean)attributesToFilter.get(Constants.PROPERTY_ENABLED)){
                    Collections.sort(cacheRes);
                    res = !(Boolean)attributesToFilter.get(Constants.PROPERTY_ENABLED) ? cacheRes : cacheRes.stream().filter(aFilterDefinition -> 
                            aFilterDefinition.isEnabled()).collect(Collectors.toList());
                }
            }

            if(ignoreCache || cm.getFilterDefinitionsForClass(className) == null){
                res = new ArrayList<>();
                //Params for parent classses 
                StringBuilder classesNamesBuilder = new StringBuilder();
                if(includeParentClassesFilters){
                    for (ClassMetadataLight c : upstreamClassHierarchy)
                        classesNamesBuilder.append("'").append(c.getName()).append("', ");
                }
                classesNamesBuilder.append("'").append(className).append("'"); 
                
                //filter definition params to filter in the query
                HashMap<String, Object> parameters = new HashMap();
                StringBuilder queryFilterBuilder = new StringBuilder();
                
                if(attributesToFilter != null && !attributesToFilter.isEmpty()){
                    attributesToFilter.entrySet().forEach(entry -> {
                        String key = (String)entry.getKey();
                        Object val = entry.getValue();
                        if (val != null) {
                            parameters.put(key, val);
                            if(val instanceof String)
                                queryFilterBuilder.append(String.format("AND TOLOWER(f.%s) CONTAINS TOLOWER($%s) ", key, key)); //NOI18N
                            else
                                queryFilterBuilder.append(String.format("AND f.%s = $%s ", key, key)); //NOI18N
                        }
                    });
                }
                //we also should get the filter for the parent classes of the class 
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("MATCH (f:").append(filterDefinitionsLabel).append(") "); //NOI18N
                queryBuilder.append("WHERE (f.className) IN [").append(classesNamesBuilder.toString()).append("] "); //NOI18N
                queryBuilder.append(queryFilterBuilder); //NOI18N
                queryBuilder.append("RETURN f ORDER BY f.name "); 
            
                if(page > 0){
                    queryBuilder.append("SKIP $page "); //NOI18N                
                    parameters.put("page", page); //NOI18N
                }
                if(limit > 0){
                    queryBuilder.append("LIMIT $limit "); //NOI18N                
                    parameters.put("limit", limit); //NOI18N
                }
                
                Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
                
                while (result.hasNext()){
                    Node aFilterDefinitionNode = (Node)result.next().get("f");
                    FilterDefinition filterDefinition = new FilterDefinition(aFilterDefinitionNode.getId(),
                                                            (String)aFilterDefinitionNode.getProperty(Constants.PROPERTY_NAME),
                                                            (String)aFilterDefinitionNode.getProperty(Constants.PROPERTY_DESCRIPTION),
                                                            (String)aFilterDefinitionNode.getProperty(Constants.PROPERTY_CLASSNAME),
                                                            (String)aFilterDefinitionNode.getProperty(Constants.PROPERTY_SCRIPT),
                                                            (boolean)aFilterDefinitionNode.getProperty(Constants.PROPERTY_ENABLED));
                    try {
                        if(!filterDefinition.getScript().isEmpty() 
                                && cm.getFilterDefinitionByClassNameAndFilterId(filterDefinition.getClassToBeApplied(), filterDefinition.getId()) == null)

                            filterDefinition.setFilter(compileFilterDefinition(filterDefinition.getId(), filterDefinition.getScript()));
                        
                        else if(cm.getFilterDefinitionByClassNameAndFilterId(filterDefinition.getClassToBeApplied(), filterDefinition.getId()) != null){
                            FilterDefinition cachedFilDefinition = cm.getFilterDefinitionByClassNameAndFilterId(filterDefinition.getClassToBeApplied(), filterDefinition.getId());
                            if(cachedFilDefinition.getFilter() != null)
                                filterDefinition.setFilter(cachedFilDefinition.getFilter());
                        }
                    } catch (Exception ex) {
                        filterDefinition.setFilter(null);
                    }
                    res.add(filterDefinition);
                }
                //we must cached the fitlers fo the parent classes
                for(FilterDefinition f : res)
                    cm.addFilterDefinition(f.getClassToBeApplied(), f);
                
                tx.success();
            } 
        }catch (MetadataObjectNotFoundException ex){}//Should not happen
        return res;
    }
    
    @Override
    public long getFilterDefinitionsForClassCount(String className, 
            boolean includeParentClassesFilters, boolean ignoreCache, 
            HashMap<String, Object> attributesToFilter, int page, int limit)
            throws InvalidArgumentException
    {
        if (className == null)
                throw new InvalidArgumentException(String.format(
                        ts.getTranslatedString("module.general.messages.parameter-not-found"), "className"));
        
        List<ClassMetadataLight> upstreamClassHierarchy = new ArrayList<>();
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if(includeParentClassesFilters) //if we need the parent's filters
                upstreamClassHierarchy = mem.getUpstreamClassHierarchy(className, false);
            
            if (ignoreCache) {
                //filter definition params to filter in the query
                HashMap<String, Object> parameters = new HashMap();
                StringBuilder queryFilterBuilder = new StringBuilder();
                if(attributesToFilter != null && !attributesToFilter.isEmpty()){
                    attributesToFilter.entrySet().forEach(entry -> {
                        String key = (String)entry.getKey();
                        Object val = entry.getValue();
                        if (val != null) {
                            parameters.put(key, val);
                            if(val instanceof String)
                                queryFilterBuilder.append(String.format("AND TOLOWER(f.%s) CONTAINS TOLOWER($%s) ", key, key)); //NOI18N
                            else
                                queryFilterBuilder.append(String.format("AND f.%s = $%s ", key, key)); //NOI18N
                        }
                    });
                }
                //Params for parent classses 
                StringBuilder classesNamesBuilder = new StringBuilder();
                if(includeParentClassesFilters){
                    for (ClassMetadataLight c : upstreamClassHierarchy)
                        classesNamesBuilder.append("'").append(c.getName()).append("', ");
                }
                classesNamesBuilder.append("'").append(className).append("'");
                
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("MATCH (f:").append(filterDefinitionsLabel).append(") "); //NOI18N
                queryBuilder.append("WHERE (f.className) IN [");
                queryBuilder.append(classesNamesBuilder.toString());
                queryBuilder.append("] "); //NOI18N
                queryBuilder.append(queryFilterBuilder);
                queryBuilder.append("RETURN COUNT(f) AS count "); 
                if(page > 0){
                   queryBuilder.append("SKIP $page "); //NOI18N                
                   parameters.put("page", page); //NOI18N
                }
                if(limit > 0){
                    queryBuilder.append("LIMIT $limit "); //NOI18N                
                    parameters.put("limit", limit); //NOI18N
                }
                Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
                
                while (result.hasNext()){
                    tx.success();
                    return (long)result.next().get("count");
                }
            } 
            tx.success();
        }catch (MetadataObjectNotFoundException ex){}//Should not happen
        return 0;
    }
   
    @Override
    public List<FilterDefinition> getAllFilterDefinitions(HashMap<String, Object> attributesToFilter, int page, int limit){
        List<FilterDefinition> res = new ArrayList<>();
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            
            queryBuilder.append("MATCH (f:").append(filterDefinitionsLabel).append(") "); //NOI18N
            //Attributes to filter
            if(attributesToFilter != null && !attributesToFilter.isEmpty()){
                StringBuilder queryFilterBuilder = new StringBuilder();
                
                attributesToFilter.entrySet().forEach(entry -> {
                    String key = (String)entry.getKey();
                    String val = (String)entry.getValue();
                    if(val != null && queryFilterBuilder.length() == 0)
                        queryFilterBuilder.append("WHERE ");
                    if (val != null) {
                        parameters.put(key, val);
                        queryFilterBuilder.append(String.format("TOLOWER(f.%s) CONTAINS TOLOWER($%s) AND", key, key)); //NOI18N
                    }
                });
                if(queryFilterBuilder.length() > 3)
                    queryBuilder.append(queryFilterBuilder.substring(0, queryFilterBuilder.length() - 3));
            }
            queryBuilder.append("RETURN f ORDER BY f.name ");

            if(page > 0){
                queryBuilder.append("SKIP $page "); //NOI18N                
                parameters.put("page", page); //NOI18N
            }
            if(limit > 0){
                queryBuilder.append("LIMIT $limit "); //NOI18N                
                parameters.put("limit", limit); //NOI18N
            }

            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (result.hasNext()){
                Node aFilterDefinitionNode = (Node)result.next().get("f");
                FilterDefinition filterDefinition = new FilterDefinition(aFilterDefinitionNode.getId(),
                                                        (String)aFilterDefinitionNode.getProperty(Constants.PROPERTY_NAME),
                                                        (String)aFilterDefinitionNode.getProperty(Constants.PROPERTY_DESCRIPTION),
                                                        (String)aFilterDefinitionNode.getProperty(Constants.PROPERTY_CLASSNAME),
                                                        (String)aFilterDefinitionNode.getProperty(Constants.PROPERTY_SCRIPT),
                                                        (boolean)aFilterDefinitionNode.getProperty(Constants.PROPERTY_ENABLED));
                try {
                    if(!filterDefinition.getScript().isEmpty())
                        // Now we try to compile the script and create a filter instance to cache
                        filterDefinition.setFilter(compileFilterDefinition(filterDefinition.getId(), filterDefinition.getScript()));
                } catch (Exception ex) {
                    filterDefinition.setFilter(null);
                }
                res.add(filterDefinition);
            }
            
            for(FilterDefinition f : res)
                cm.addFilterDefinition(f.getClassToBeApplied(), f);
            
            tx.success();
        }
        return res;
    }
    
    @Override
    public long getAllFilterDefinitionsCount(HashMap<String, Object> attributesToFilter){
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            
            queryBuilder.append("MATCH (f:").append(filterDefinitionsLabel).append(") "); //NOI18N
            //Attributes to filter
            if(attributesToFilter != null && !attributesToFilter.isEmpty()){
                StringBuilder queryFilterBuilder = new StringBuilder();
                attributesToFilter.entrySet().forEach(entry -> {
                    String key = (String)entry.getKey();
                    String val = (String)entry.getValue();
                    if(val != null && queryFilterBuilder.length() == 0)
                        queryFilterBuilder.append("WHERE ");
                    if (val != null) {
                        parameters.put(key, val);
                        queryFilterBuilder.append(String.format("TOLOWER(f.%s) CONTAINS TOLOWER($%s) AND", key, key)); //NOI18N
                    }
                });
                if(queryFilterBuilder.length() > 3 )
                    queryBuilder.append(queryFilterBuilder.substring(0, queryFilterBuilder.length() - 3));
            }
            
            queryBuilder.append("RETURN COUNT (f) as count"); 

            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (result.hasNext()){
                tx.success();
                return (long) result.next().get("count");
            }
            tx.success();
            return 0;
        }
    }
    
    @Override
    public void deleteFilterDefinition(long filterId, String className) throws InvalidArgumentException {
        if (className == null)
            throw new InvalidArgumentException(String.format(
                    ts.getTranslatedString("module.general.messages.parameter-not-found"), "className"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (f:").append(filterDefinitionsLabel).append(") "); //NOI18N
            queryBuilder.append("WHERE id(f)=").append(filterId).append(" ");
            queryBuilder.append("DELETE f"); 

            connectionManager.getConnectionHandler().execute(queryBuilder.toString());

            cm.removeFilterDefinitionsForClass(className, filterId);
            tx.success();
        }
    }
    
    /**
     * Creates an Filter instances with a given script fo the filter definition
     * @param filterId the filter id
     * @param script the filter definition script
     * @return a Filter instance
     * @throws ScriptNotCompiledException if something goes worn with the compilation
     */
    private Filter compileFilterDefinition(long filterId, String script) throws ScriptNotCompiledException{
        Filter filterInstance = null;
        try{    
            String filterClassName = "Filter" + filterId;
            Class filterDefinitionClass = filterDefinitionsClassLoader.parseClass(
                String.format(script, filterClassName, filterClassName));
            
            // Now we try to compile the script and create a filter instance to cache
            filterInstance = (Filter)filterDefinitionClass.
                getConstructor(MetadataEntityManager.class, ApplicationEntityManager.class, BusinessEntityManager.class, ConnectionManager.class).
                newInstance(mem, this, bem, connectionManager);
        } catch (Exception ex) {
            throw new ScriptNotCompiledException(ex.getLocalizedMessage());
        }
        return filterInstance;
    }
    //</editor-fold>
    //<editor-fold desc="Scripted Queries" defaultstate="collapsed">
    @Override
    public String createScriptedQueriesPool(String name, String description) 
        throws InvalidArgumentException, ExecutionException {
        if (name == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-name-cannot-be-null"));
        else if (name.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-name-cannot-be-empty"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "CREATE (n:%s {_uuid: randomUUID(), name: $name, description: $description}) RETURN n._uuid AS id", 
                scriptedQueriesPoolsLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("name", name);
            parameters.put("description", description);
            Result result = connectionManager.getConnectionHandler().execute(query, parameters);
            if (result.hasNext()) {
                tx.success();
                return (String) result.next().get("id");
            }
            throw new ExecutionException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-was-not-created"), name));
        }
    }
    @Override
    public void updateScriptedQueriesPool(String scriptedQueriesPoolId, String name, String description) 
        throws InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (scriptedQueriesPoolId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-id-cannot-be-null"));
        else if (scriptedQueriesPoolId.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-id-cannot-be-empty"));
        
        if (name == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-name-cannot-be-null"));
        else if (name.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-name-cannot-be-empty"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s {_uuid: $_uuid}) SET n.name = $name SET n.description = $description RETURN n",
                scriptedQueriesPoolsLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueriesPoolId);
            parameters.put("name", name);
            parameters.put("description", description);
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("n");
            if (result.hasNext()) {
                tx.success();
                return;
            }
            throw new ApplicationObjectNotFoundException(String.format(
                ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-id-not-found"), 
                scriptedQueriesPoolId
            ));
        }
    }
    @Override
    public void deleteScriptedQueriesPool(String scriptedQueriesPoolId) 
        throws InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (scriptedQueriesPoolId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-id-cannot-be-null"));
        else if (scriptedQueriesPoolId.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-id-cannot-be-empty"));
            
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s {_uuid: $_uuid})<-[:%s*0..]-(m)-[:%s*0..]->(p) DETACH DELETE p DETACH DELETE m DELETE n RETURN n", 
                scriptedQueriesPoolsLabel.name(),
                RelTypes.CHILD_OF_SPECIAL,
                RelTypes.HAS_PARAMETER);

            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueriesPoolId);
            QueryStatistics queryStatistics = connectionManager.getConnectionHandler().execute(query, parameters).getQueryStatistics();
            if (queryStatistics.containsUpdates()) {
                tx.success();
                return;
            }
            throw new ApplicationObjectNotFoundException(String.format(
                ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-id-not-found"), 
                scriptedQueriesPoolId
            ));
        }
    }
    @Override
    public ScriptedQueriesPool getScriptedQueriesPoolByName(String scriptedQueriesPoolName) 
        throws InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (scriptedQueriesPoolName == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-name-cannot-be-null"));
        else if (scriptedQueriesPoolName.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-name-cannot-be-empty"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s {name: $name}) RETURN n",
                scriptedQueriesPoolsLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("name", scriptedQueriesPoolName);
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("n");
            tx.success();
            if (result.hasNext()) {
                Node node = result.next();
                return new ScriptedQueriesPool(
                    (String) node.getProperty("_uuid"),
                    (String) node.getProperty("name"),
                    (String) node.getProperty("description", null)
                );
            }
            throw new ApplicationObjectNotFoundException(String.format(
                ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-name-not-found"), 
                scriptedQueriesPoolName
            ));
        }
    }
    @Override
    public ScriptedQueriesPool getScriptedQueriesPool(String scriptedQueriesPoolId) 
        throws InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (scriptedQueriesPoolId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-id-cannot-be-null"));
        else if (scriptedQueriesPoolId.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-id-cannot-be-empty"));
            
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s {_uuid: $_uuid}) RETURN n",
                scriptedQueriesPoolsLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueriesPoolId);
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("n");
            if (result.hasNext()) {
                Node node = result.next();
                tx.success();
                return new ScriptedQueriesPool(
                    (String) node.getProperty("_uuid"),
                    (String) node.getProperty("name"),
                    (String) node.getProperty("description", null)
                );
            }
            throw new ApplicationObjectNotFoundException(
                String.format(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-id-not-found"), 
                scriptedQueriesPoolId
            ));
        }
    }
    @Override
    public int getScriptedQueriesPoolCount(String filterName) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s) WHERE n.name CONTAINS $name RETURN count(n) AS size",
                scriptedQueriesPoolsLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("name", filterName);
            Result result = connectionManager.getConnectionHandler().execute(query, parameters);
            if (result.hasNext()) {
                tx.success();
                return ((Long) result.next().get("size")).intValue();
            }
            return 0;
        }
    }
    @Override
    public List<ScriptedQueriesPool> getScriptedQueriesPools(String filterName, int skip, int limit) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String limitQry = limit >= 0 ? "LIMIT $limit" : "";
            String query = String.format(
                "MATCH (n:%s) WHERE n.name CONTAINS $name RETURN n SKIP $skip " + limitQry,
                scriptedQueriesPoolsLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("name", filterName);
            parameters.put("skip", skip);
            parameters.put("limit", limit);
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("n");
            List<ScriptedQueriesPool> scriptedQueriesPools = new ArrayList();
            while (result.hasNext()) {
                Node node = result.next();
                scriptedQueriesPools.add(new ScriptedQueriesPool(
                    (String) node.getProperty("_uuid"), 
                    (String) node.getProperty("name"), 
                    (String) node.getProperty("description", null)
                ));
            }
            tx.success();
            return scriptedQueriesPools;
        }
    }
    @Override
    public int getScriptedQueryCountByPoolId(String scriptedQueriesPoolId, String filterName, boolean ignoreDisabled) {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format("MATCH (n:%s {_uuid: $_uuid})<-[:%s]-(m:%s) WHERE m.name CONTAINS $name RETURN count(m) AS size", 
                scriptedQueriesPoolsLabel.name(), 
                RelTypes.CHILD_OF_SPECIAL, 
                scriptedQueriesLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueriesPoolId);
            parameters.put("name", filterName);
            Result result = connectionManager.getConnectionHandler().execute(query, parameters);
            if (result.hasNext()) {
                tx.success();
                return ((Long) result.next().get("size")).intValue();
            }
            return 0;
        }
    }
    @Override
    public List<ScriptedQuery> getScriptedQueriesByPoolId(String scriptedQueriesPoolId, String filterName, boolean ignoreDisabled, int skip, int limit) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String limitQry = limit >= 0 ? "LIMIT $limit" : "";
            String query = String.format("MATCH (n:%s {_uuid: $_uuid})<-[:%s]-(m:%s) WHERE m.name CONTAINS $name RETURN m SKIP $skip " + limitQry, 
                scriptedQueriesPoolsLabel.name(), 
                RelTypes.CHILD_OF_SPECIAL, 
                scriptedQueriesLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueriesPoolId);
            parameters.put("name", filterName);
            parameters.put("skip", skip);
            parameters.put("limit", limit);
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("m");
            List<ScriptedQuery> scriptedQueries = new ArrayList();
            while (result.hasNext()) {
                Node node = result.next();
                scriptedQueries.add(new ScriptedQuery(
                    (String) node.getProperty("_uuid"),
                    (String) node.getProperty("name"),
                    (String) node.getProperty("description", null),
                    (String) node.getProperty("script", null),
                    (boolean) node.getProperty("enabled")
                ));
            }
            tx.success();
            return scriptedQueries;
        }
    }
    @Override
    public int getScriptedQueryCountByPoolName(String scriptedQueriesPoolName, String filterName) 
        throws InvalidArgumentException, ExecutionException {
        if (scriptedQueriesPoolName == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-name-cannot-be-null"));
        else if (scriptedQueriesPoolName.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-name-cannot-be-empty"));
        
        if (filterName == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.filter.scripted-query-name.error"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format("MATCH (n:%s {name: $nname})<-[:%s]-(m:%s) WHERE m.name CONTAINS $mname RETURN count(m) AS size", 
                scriptedQueriesPoolsLabel.name(), 
                RelTypes.CHILD_OF_SPECIAL, 
                scriptedQueriesLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("nname", scriptedQueriesPoolName);
            parameters.put("mname", filterName);
            Result result = connectionManager.getConnectionHandler().execute(query, parameters);
            if (result.hasNext()) {
                tx.success();
                return ((Long) result.next().get("size")).intValue();
            }
            throw new ExecutionException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-count-by-pool-name-error"));
        }
    }
    @Override
    public List<ScriptedQuery> getScriptedQueriesByPoolName(String scriptedQueriesPoolName, String filterName, boolean ignoreDisabled, int skip, int limit) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format("MATCH (n:%s {name: $name})<-[:%s]-(m:%s) WHERE m.name CONTAINS $mname RETURN m SKIP $skip LIMIT $limit", 
                scriptedQueriesPoolsLabel.name(), 
                RelTypes.CHILD_OF_SPECIAL, 
                scriptedQueriesLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("name", scriptedQueriesPoolName);
            parameters.put("mname", filterName);
            parameters.put("skip", skip);
            parameters.put("limit", limit);
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("m");
            List<ScriptedQuery> scriptedQueries = new ArrayList();
            while (result.hasNext()) {
                Node node = result.next();
                scriptedQueries.add(new ScriptedQuery(
                    (String) node.getProperty("_uuid"),
                    (String) node.getProperty("name"),
                    (String) node.getProperty("description", null),
                    (String) node.getProperty("script", null),
                    (boolean) node.getProperty("enabled")
                ));
            }
            tx.success();
            return scriptedQueries;
        }
    }
    @Override
    public String createScriptedQuery(String scriptedQueriesPoolId, String name, String description, String script, boolean enabled) 
        throws InvalidArgumentException, ExecutionException {
        
        if (scriptedQueriesPoolId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-id-cannot-be-null"));
        else if (scriptedQueriesPoolId.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-queries-pool-id-cannot-be-empty"));
        
        if (name == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-name-cannot-be-null"));
        else if (name.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-name-cannot-be-empty"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format("MATCH (n:%s {_uuid: $_uuid}) CREATE (n)<-[:%s]-(m:%s {_uuid: randomUUID(), name: $name, description: $description, script: $script, enabled: $enabled}) RETURN m._uuid AS id", 
                scriptedQueriesPoolsLabel.name(), 
                RelTypes.CHILD_OF_SPECIAL,
                scriptedQueriesLabel.name());
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueriesPoolId);
            parameters.put("name", name);
            parameters.put("description", description);
            parameters.put("script", script);
            parameters.put("enabled", enabled);
            Result result = connectionManager.getConnectionHandler().execute(query, parameters);
            if (result.hasNext()) {
                tx.success();
                return (String) result.next().get("id");
            }
            throw new ExecutionException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-was-not-created"));
        }
    }
    @Override
    public void updateScriptedQuery(String scriptedQueryId, String name, String description, String script, boolean enabled) 
        throws InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (scriptedQueryId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-id-cannot-be-null"));
        else if (scriptedQueryId.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-id-cannot-be-empty"));
        
        if (name == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-name-cannot-be-null"));
        else if (name.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-name-cannot-be-empty"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s {_uuid: $_uuid}) SET n.name = $name SET n.description = $description SET n.script = $script SET n.enabled = $enabled RETURN n", 
                scriptedQueriesLabel.name() 
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueryId);
            parameters.put("name", name);
            parameters.put("description", description);
            parameters.put("script", script);
            parameters.put("enabled", enabled);
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("n");
            if (result.hasNext()) {
                tx.success();
                return;
            }
            throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-id-not-found"), scriptedQueryId));
        }
    }
    @Override
    public void deleteScriptedQuery(String scriptedQueryId) 
        throws InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (scriptedQueryId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-id-cannot-be-null"));
        else if (scriptedQueryId.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-id-cannot-be-empty"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s {_uuid: $_uuid})-[:%s*0..]->(m) DETACH DELETE n DELETE m RETURN n",
                scriptedQueriesLabel.name(),
                RelTypes.HAS_PARAMETER
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueryId);
            QueryStatistics queryStatistics = connectionManager.getConnectionHandler().execute(query, parameters).getQueryStatistics();
            if (queryStatistics.containsUpdates()) {
                tx.success();
                return;
            }
            throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-id-not-found"), scriptedQueryId));
        }
    }
    @Override
    public ScriptedQuery getScriptedQuery(String scriptedQueryId) 
        throws InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (scriptedQueryId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-id-cannot-be-null"));
        else if (scriptedQueryId.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-id-cannot-be-empty"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s {_uuid: $_uuid}) RETURN n", 
                scriptedQueriesLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueryId);
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("n");
            if (result.hasNext()) {
                Node node = result.next();
                tx.success();
                return new ScriptedQuery(
                    (String) node.getProperty("_uuid"),
                    (String) node.getProperty("name"),
                    (String) node.getProperty("description", null),
                    (String) node.getProperty("script", null),
                    (boolean) node.getProperty("enabled")
                );
            }
            throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-id-not-found"), scriptedQueryId));
        }
    }
    @Override
    public int getScriptedQueryCount(String filterName) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s) WHERE n.name CONTAINS $name RETURN count(n) AS size", 
                scriptedQueriesLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("name", filterName);
            Result result = connectionManager.getConnectionHandler().execute(query, parameters);
            if (result.hasNext()) {
                tx.success();
                return ((Long) result.next().get("size")).intValue();
            }
            return 0;
        }
    }
    @Override
    public List<ScriptedQuery> getScriptedQueries(String filterName, boolean ignoreDisabled, int skip, int limit) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s) WHERE n.name CONTAINS $name RETURN n SKIP $skip LIMIT $limit", 
                scriptedQueriesLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("name", filterName);
            parameters.put("skip", skip);
            parameters.put("limit", limit);
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("n");
            List<ScriptedQuery> scriptedQueries = new ArrayList();
            while (result.hasNext()) {
                Node node = result.next();
                scriptedQueries.add(new ScriptedQuery(
                    (String) node.getProperty("_uuid"),
                    (String) node.getProperty("name"),
                    (String) node.getProperty("description", null),
                    (String) node.getProperty("script", null),
                    (boolean) node.getProperty("enabled")
                ));
            }
            tx.success();
            return scriptedQueries;
        }
    }
    @Override
    public ScriptedQueryResult executeScriptedQuery(String scriptedQueryId, ScriptedQueryParameter... parameters) 
        throws InvalidArgumentException, ApplicationObjectNotFoundException, ExecutionException {
        
        ScriptedQuery scriptedQuery = getScriptedQuery(scriptedQueryId);
        if (scriptedQuery.getScript() == null)
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-script-is-null"), scriptedQuery.getName(), scriptedQuery.getId()));
        if (!scriptedQuery.isEnabled())
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-disabled"), scriptedQuery.getName(), scriptedQuery.getId()));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> scriptParameters = new HashMap();
            for (ScriptedQueryParameter parameter : parameters)
                scriptParameters.put(parameter.getName(), parameter.getValue());
            
            Binding sharedData = new Binding();            
            sharedData.setVariable("aem", this); //NOI18N
            sharedData.setVariable("bem", bem); //NOI18N
            sharedData.setVariable("mem", mem); //NOI18N
            sharedData.setVariable("connectionHandler", connectionManager.getConnectionHandler()); //NOI18N
            sharedData.setVariable("scriptParameters", scriptParameters); //NOI18N
            
            GroovyShell shell = new GroovyShell(ApplicationEntityManager.class.getClassLoader(), sharedData);
            Object result = shell.evaluate(scriptedQuery.getScript());
            
            if (result == null)
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.null-scripted-query-result"));
            else if (!(result instanceof ScriptedQueryResult))
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.not-scripted-query-result"));
            return (ScriptedQueryResult) result;
        } catch (Exception ex) {
            throw new ExecutionException(ex.getLocalizedMessage());
        }
    }
    @Override
    public String createScriptedQueryParameter(String scriptedQueryId, String name, String description, String type, boolean mandatory, Object defaultValue) 
        throws InvalidArgumentException, ExecutionException {
        if (scriptedQueryId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-id-cannot-be-null"));
        if (name == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-parameter-name-cannot-be-null"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format("MATCH (n:%s {_uuid: $_uuid}) CREATE (n)-[:%s]->(m:%s {_uuid: randomUUID(), name: $name, description: $description, type: $type, defaultValue: $defaultValue}) RETURN m._uuid AS id",
                scriptedQueriesLabel.name(),
                RelTypes.HAS_PARAMETER,
                scriptedQueriesParametersLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueryId); //NOI18N
            parameters.put("name", name); //NOI18N
            parameters.put("description", description); //NOI18N
            parameters.put("type", type); //NOI18N
            parameters.put("defaultValue", defaultValue); //NOI18N
            parameters.put("mandatory", mandatory); //NOI18N
            Result result = connectionManager.getConnectionHandler().execute(query, parameters);
            if (result.hasNext()) {
                tx.success();
                return (String) result.next().get("id"); //NOI18N
            }
            throw new ExecutionException(String.format(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-parameter-was-not-created"), name));
        }
    }
    @Override
    public void updateScriptedQueryParameter(String scriptedQueryParameterId, String name, String description, String type, boolean mandatory, Object defaultValue) 
        throws InvalidArgumentException, ApplicationObjectNotFoundException {
        if (scriptedQueryParameterId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-parameter-id-cannot-be-null"));
        if (name == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-parameter-name-cannot-be-null"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s {_uuid: $_uuid}) SET n.name = $name SET n.description = $description SET n.type = $type SET n.defaultValue = $defaultValue RETURN n", 
                scriptedQueriesParametersLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueryParameterId); //NOI18N
            parameters.put("name", name); //NOI18N
            parameters.put("description", description); //NOI18N
            parameters.put("type", type); //NOI18N
            parameters.put("defaultValue", defaultValue); //NOI18N
            parameters.put("mandatory", mandatory); //NOI18N
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("n");
            if (result.hasNext()) {
                tx.success();
                return;
            }
            throw new ApplicationObjectNotFoundException(String.format(
                ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-parameter-id-not-found"), 
                scriptedQueryParameterId
            ));
        }
    }
    @Override
    public void deleteScriptedQueryParameter(String scriptedQueryParameterId) 
        throws InvalidArgumentException, ApplicationObjectNotFoundException {
        if (scriptedQueryParameterId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-parameter-id-cannot-be-null"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s {_uuid: $_uuid}) DETACH DELETE n RETURN n", 
                scriptedQueriesParametersLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueryParameterId); //NOI18N
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("n");
            if (result.hasNext()) {
                tx.success();
                return;
            }
            throw new ApplicationObjectNotFoundException(String.format(
                ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-parameter-id-not-found"), 
                scriptedQueryParameterId
            ));
        }
    }
    @Override
    public ScriptedQueryParameter getScriptedQueryParameter(String scriptedQueryParameterId) 
        throws InvalidArgumentException, ApplicationObjectNotFoundException {
        if (scriptedQueryParameterId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-parameter-id-cannot-be-null"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s {_uuid: $_uuid}) RETURN n",
                scriptedQueriesParametersLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueryParameterId); //NOI18N
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("n");
            if (result.hasNext()) {
                Node node = result.next();
                tx.success();
                return new ScriptedQueryParameter(
                    (String) node.getProperty("_uuid"), //NOI18N
                    (String) node.getProperty("name"), //NOI18N
                    (String) node.getProperty("description", null), //NOI18N
                    (String) node.getProperty("type", null), //NOI18N
                    (Boolean) node.getProperty("mandatory", false), //NOI18N
                    node.getProperty("defaultValue", null) //NOI18N
                );
            }
        }
        throw new ApplicationObjectNotFoundException(String.format(
            ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-parameter-id-not-found"), 
            scriptedQueryParameterId
        ));
    }
    @Override
    public List<ScriptedQueryParameter> getScriptedQueryParameters(String scriptedQueryId) 
        throws InvalidArgumentException {
        if (scriptedQueryId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-id-cannot-be-null"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String query = String.format(
                "MATCH (n:%s {_uuid: $_uuid})-[:%s]->(m:%s) RETURN m", 
                scriptedQueriesLabel.name(), 
                RelTypes.HAS_PARAMETER, 
                scriptedQueriesParametersLabel.name()
            );
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("_uuid", scriptedQueryId); //NOI18N
            ResourceIterator<Node> result = connectionManager.getConnectionHandler().execute(query, parameters).columnAs("m");
            List<ScriptedQueryParameter> scriptedQueryParameters = new ArrayList();
            while (result.hasNext()) {
                Node node = result.next();
                scriptedQueryParameters.add(new ScriptedQueryParameter(
                    (String) node.getProperty("_uuid"), //NOI18N
                    (String) node.getProperty("name"), //NOI18N
                    (String) node.getProperty("description", null), //NOI18N
                    (String) node.getProperty("type", null), //NOI18N
                    (Boolean) node.getProperty("mandatory", false), //NOI18N
                    node.getProperty("defaultValue", null) //NOI18N
                ));
            }
            tx.success();
            return scriptedQueryParameters;
        }
    }
    //</editor-fold>
}
