/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>
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

package org.neotropic.kuwaiba.core.persistence.reference.neo4j;

import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.InventoryReport;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.AnnotatedBusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLightList;
import org.neotropic.kuwaiba.core.apis.persistence.business.Contact;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterators;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.extras.caching.CacheManager;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.ObjectGraphMappingService;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.Util;
import org.neotropic.kuwaiba.core.persistence.reference.naming.util.DynamicNameGenerator;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.PortUtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Business entity manager reference implementation (using Neo4J as backend).
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class BusinessEntityManagerImpl implements BusinessEntityManager {
    /**
     * Default attachment location.
     */
    private static final String DEFAULT_ATTACHMENTS_PATH = "/data/files/attachments";
    private static final String DEFAULT_MAX_ATTACHMENT_SIZE = "10";
    /**
     * Key prefix to error messages in the Business Entity Manager Service
     */
    private final String KEY_PREFIX = "api.bem.error";
    /**
     * Object Label.
     */
    private final Label inventoryObjectLabel;
    /**
     * Class Label.
     */
    private final Label classLabel;
    /**
     * Pools Label.
     */
    private final Label poolLabel;
    /**
     * Templates label 
     */
    private final Label templateLabel;
    /**
     * Special nodes Label.
     */
    private final Label specialNodeLabel;
    /**
     * Label for reports.
     */
    private final Label reportsLabel;
    /**
     * Label used to tag the nodes that store contact information.
     */
    private final Label contactsLabel;
    /**
     * Global configuration variables.
     */
    private Properties configuration;

    @Autowired
    private ConnectionManager<GraphDatabaseService> connectionManager;
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
     * Reference to the service that maps nodes to inventory/application objects
     */
    @Autowired
    private ObjectGraphMappingService ogmService;
    /**
     * Reference to ports utility service
     */
    @Autowired
    private  PortUtilityService portUtilityService;
    /**
     * Reference to internationalization service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Main constructor. It receives references to the other entity managers
     */
    @Autowired
    public BusinessEntityManagerImpl() {
        this.configuration = new Properties();
        this.inventoryObjectLabel = Label.label(Constants.LABEL_INVENTORY_OBJECTS);
        this.classLabel = Label.label(Constants.LABEL_CLASS);
        this.poolLabel = Label.label(Constants.LABEL_POOLS);
        this.templateLabel = Label.label(Constants.LABEL_TEMPLATES);
        this.specialNodeLabel = Label.label(Constants.LABEL_SPECIAL_NODE);
        this.reportsLabel = Label.label(Constants.LABEL_REPORTS);
        this.contactsLabel = Label.label(Constants.LABEL_CONTACTS);
    }

    @Override
    public void initCache() {
        // Nothing for now.
    }
    
    @Override
    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }

    @Override
    public String createObject(String className, String parentClassName, String parentOid, HashMap<String, String> attributes, String templateId)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (parentOid == null)
            throw new BusinessObjectNotFoundException(parentClassName, parentOid);
                
        ClassMetadata myClass= mem.getClass(className);
        
        if (!mem.canBeChild(parentClassName, className)) {
            OperationNotPermittedException ex = new OperationNotPermittedException(String.format(ts.getTranslatedString(KEY_PREFIX + ".2"), className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(2);
            ex.setMessageArgs(className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName);
            throw ex;
        }
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {        
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null) {
                MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".3"), className));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(3);
                ex.setMessageArgs(className);
                throw ex;
            }

            if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, className)) {
                OperationNotPermittedException ex = new OperationNotPermittedException(ts.getTranslatedString(KEY_PREFIX + ".4"));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(4);
                throw ex;
            }

            //The object should be created under an instance other than the dummy root
            if (parentClassName != null && !parentOid.equals("-1")) {
                ClassMetadata myParentObjectClass= mem.getClass(parentClassName);
                if (myParentObjectClass == null) {
                    MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".5"), parentClassName));
                    ex.setPrefix(KEY_PREFIX);
                    ex.setCode(5);
                    ex.setMessageArgs(parentClassName);
                    throw ex;
                }
            }

            Node parentNode;
            if (!parentOid.equals("-1")) {
                 parentNode = getInstanceOfClass(parentClassName, parentOid);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentOid);
            }
            else
                parentNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            Node newObject;
            if (templateId == null || templateId.isEmpty())
                newObject = createObject(classNode, myClass, attributes);
            else {
                try {
                    Node templateNode = connectionManager.getConnectionHandler().findNode(templateLabel, Constants.PROPERTY_UUID, templateId);
                    if (templateNode.hasRelationship(Direction.INCOMING, RelTypes.HAS_TEMPLATE)) {
                        if (className.equals(templateNode.getSingleRelationship(RelTypes.HAS_TEMPLATE, Direction.INCOMING).
                                getStartNode().getProperty(Constants.PROPERTY_NAME)))
                            newObject = copyTemplateElement(templateNode, myClass, true);
                        else {
                            InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".6"), templateId, className));
                            ex.setPrefix(KEY_PREFIX);
                            ex.setCode(6);
                            ex.setMessageArgs(templateId, className);
                            throw ex;
                        }
                    } else {
                        InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".7"), templateId));
                        ex.setPrefix(KEY_PREFIX);
                        ex.setCode(7);
                        ex.setMessageArgs(templateId);
                        throw ex;
                    }
                    
                } catch (NotFoundException ex) {
                    ApplicationObjectNotFoundException nestedEx = new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".8"), templateId, className));
                    nestedEx.setPrefix(KEY_PREFIX);
                    nestedEx.setCode(8);
                    nestedEx.setMessageArgs(templateId, className);
                    throw nestedEx;
                }
            }
            newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);            
            tx.success();
            return (String)newObject.getProperty(Constants.PROPERTY_UUID);
        }
    }
    
    //TODO: Rewrite this!
    @Override
    public String createObject(String className, String parentClassName, HashMap<String, String> attributes, String templateId, String criteria)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException {
        
        ClassMetadata objectClass = mem.getClass(className);
        if (objectClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
        
        if (objectClass.isInDesign())
            throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");

        if (objectClass.isAbstract())
            throw new OperationNotPermittedException("Can not create instances of abstract classes");

        if (!mem.isSubclassOf("InventoryObject", className))
            throw new OperationNotPermittedException("Can not create non-inventory objects");
        
        if (!mem.canBeChild(parentClassName, className))
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));
        
        String[] splitCriteria = criteria.split(":");
        if (splitCriteria.length != 2)
            throw new InvalidArgumentException("The criteria is not valid, two components expected (attributeName:attributeValue)");

        if (splitCriteria[0].equals(Constants.PROPERTY_OID)) //The user is providing the id of te parent node explicitely
            return createObject(className, parentClassName, splitCriteria[1], attributes, templateId);

        ClassMetadata parentClass = mem.getClass(parentClassName);
        if (parentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", parentClassName));

        AttributeMetadata filterAttribute = parentClass.getAttribute(splitCriteria[0]);

        if (filterAttribute == null)
            throw new MetadataObjectNotFoundException(String.format("Attribute %s could not be found in class %s", splitCriteria[0], parentClassName));

        if (!AttributeMetadata.isPrimitive(filterAttribute.getType()))
            throw new InvalidArgumentException(String.format(
                    "The filter provided (%s) is not a primitive type. Non-primitive types are not supported as they typically don't uniquely identify an object", 
                    splitCriteria[0]));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
           
            Node parentClassNode, parentNode = null;
            
            if (Constants.NODE_DUMMYROOT.equals(parentClassName))
                parentNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            else {
                parentClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, parentClassName);
                
                Iterator<Relationship> instances = parentClassNode.getRelationships(RelTypes.INSTANCE_OF).iterator();

                while (instances.hasNext()) {
                    Node possibleParentNode = instances.next().getStartNode();                   
                    if (possibleParentNode.getProperty(splitCriteria[0]).toString().equals(splitCriteria[1])) {
                        parentNode = possibleParentNode;
                        break;
                    }
                }
            }
            
            if (parentNode == null)
                throw new InvalidArgumentException(String.format("A parent object of class %s and %s = %s could not be found", parentClassName, splitCriteria[0], splitCriteria[1]));
            
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

            Node newObject;
            if (templateId == null || templateId.isEmpty())
                newObject = createObject(classNode, objectClass, attributes);
            else {
                try {
                    Node templateNode = connectionManager.getConnectionHandler().findNode(templateLabel, Constants.PROPERTY_UUID, templateId);
                    if (templateNode.hasRelationship(Direction.INCOMING, RelTypes.HAS_TEMPLATE)) {
                        if (objectClass.equals(templateNode.getSingleRelationship(RelTypes.HAS_TEMPLATE, Direction.INCOMING).
                                getStartNode().getProperty(Constants.PROPERTY_NAME)))
                            newObject = copyTemplateElement(templateNode, objectClass, true);
                        else
                            throw new InvalidArgumentException(String.format("The template with id %s is not applicable to instances of class %s", templateId, objectClass));
                    } else 
                        throw new InvalidArgumentException(String.format("The template with id %s is malformed", templateId));
                    
                } catch (NotFoundException ex) {
                    throw new ApplicationObjectNotFoundException(String.format("No template with id %s was found for class %s", templateId, className));
                }
            }
            
            newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);
                      
            tx.success();
            return newObject.hasProperty(Constants.PROPERTY_UUID) ? newObject.getProperty(Constants.PROPERTY_UUID).toString() : null;
        }
    }
    
    @Override
    public String createSpecialObject(String className, String parentClassName, String parentOid, HashMap<String,String> attributes, String templateId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (parentOid == null)
            throw new BusinessObjectNotFoundException(parentClassName, parentOid);
        
        ClassMetadata classMetadata= mem.getClass(className);
        if (classMetadata == null) {
            MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".9"), className));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(9);
            ex.setMessageArgs(className);
            throw ex;
        }

        if (classMetadata.isInDesign()) {
            InvalidArgumentException ex = new InvalidArgumentException(ts.getTranslatedString(KEY_PREFIX + ".10"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(10);
            throw ex;
        }
        
        if (classMetadata.isAbstract()) {
            InvalidArgumentException ex = new InvalidArgumentException(ts.getTranslatedString(KEY_PREFIX + ".11"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(11);
            throw ex;
        }
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null) {
                MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".12"), className));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(12);
                ex.setMessageArgs(className);
                throw ex;
            }

            //The object should be created under an instance other than the dummy root
            if (parentClassName != null) {
                ClassMetadata myParentObjectClass= mem.getClass(parentClassName);
                if (myParentObjectClass == null) {
                    MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".12"), className));
                    ex.setPrefix(KEY_PREFIX);
                    ex.setCode(12);
                    ex.setMessageArgs(className);
                    throw ex;
                }
            }

            Node parentNode = null;
            if (!parentOid.equals("-1")) {
                parentNode = getInstanceOfClass(parentClassName, parentOid);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentOid);
            }
        
            Node newObject;
            
            if (templateId == null || templateId.isEmpty()) 
                newObject = createObject(classNode, classMetadata, attributes);
                
            else {
                try {
                    Node templateNode = connectionManager.getConnectionHandler().findNode(templateLabel, Constants.PROPERTY_UUID, templateId);
                    if (templateNode.hasRelationship(Direction.INCOMING, RelTypes.HAS_TEMPLATE)) {
                        if (className.equals(templateNode.getSingleRelationship(RelTypes.HAS_TEMPLATE, Direction.INCOMING).
                                getStartNode().getProperty(Constants.PROPERTY_NAME))) {
                            newObject = copyTemplateElement(templateNode, classMetadata, true);
                            ogmService.updateObject((String)newObject.getProperty(Constants.PROPERTY_UUID), 
                                    classMetadata, attributes); //Override the template values with those provided, if any
                        }
                        else {
                            InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".13"), templateId, className));
                            ex.setPrefix(KEY_PREFIX); 
                            ex.setCode(13);
                            ex.setMessageArgs(templateId, className);
                            throw ex;
                        }
                    } else {
                        InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".14"), templateId));
                        ex.setPrefix(KEY_PREFIX);
                        ex.setCode(14);
                        ex.setMessageArgs(templateId);
                        throw ex;
                    }
                    
                } catch (NotFoundException ex) {
                    ApplicationObjectNotFoundException nestedEx = new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".15"), templateId, className));
                    nestedEx.setPrefix(KEY_PREFIX);
                    nestedEx.setCode(15);
                    nestedEx.setMessageArgs(templateId, className);
                    throw nestedEx;
                }
            }
            if (parentNode !=null)
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
            
            tx.success();
            return newObject.hasProperty(Constants.PROPERTY_UUID) ? newObject.getProperty(Constants.PROPERTY_UUID).toString() : null;
        }
    }
    
    @Override
    public HashMap<String, String> createSpecialObjectUsingTemplate(String className, String parentClassName, String parentOid, HashMap<String, String> attributes, String templateId)
        throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (templateId == null || templateId.isEmpty())
            throw new InvalidArgumentException("Template id cannot be null or empty");
        
        if (parentOid == null)
            throw new BusinessObjectNotFoundException(parentClassName, parentOid);
        
        ClassMetadata classMetadata= mem.getClass(className);
        if (classMetadata == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

        if (classMetadata.isInDesign())
            throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");
        
        if (classMetadata.isAbstract())
            throw new OperationNotPermittedException("Can not create objects of abstract classes");
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

            //The object should be created under an instance other than the dummy root
            if (parentClassName != null) {
                ClassMetadata myParentObjectClass= mem.getClass(parentClassName);
                if (myParentObjectClass == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            }

            Node parentNode = null;
            if (!parentOid.equals("-1")) {
                 parentNode = getInstanceOfClass(parentClassName, parentOid);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentOid);
            }
            HashMap<String, String> templateIds = new HashMap();
            
            Node newObject = createObject(classNode, classMetadata, attributes);
            
            try {
                Node templateNode = connectionManager.getConnectionHandler().findNode(templateLabel, Constants.PROPERTY_UUID, templateId);
                if (templateNode.hasRelationship(Direction.INCOMING, RelTypes.HAS_TEMPLATE)) {
                    if (className.equals(templateNode.getSingleRelationship(RelTypes.HAS_TEMPLATE, Direction.INCOMING).
                            getStartNode().getProperty(Constants.PROPERTY_NAME))) {
                        newObject = copyTemplateElement(templateNode, classMetadata, true, templateIds);
                        ogmService.updateObject((String)newObject.getProperty(Constants.PROPERTY_UUID), 
                                classMetadata, attributes); //Override the template values with those provided, if any
                    }
                    else
                        throw new InvalidArgumentException(String.format("The template with id %s is not applicable to instances of class %s", templateId, className));
                } else 
                    throw new InvalidArgumentException(String.format("The template with id %s is malformed", templateId));

            } catch (NotFoundException ex) {
                throw new ApplicationObjectNotFoundException(String.format("No template with id %s was found for class %s", templateId, className));
            }
            
            if (parentNode !=null)
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
            
            tx.success();
            return templateIds;
        }
    }
    
    @Override
    public void addParentToSpecialObject(String specialObjectClass, String specialObjectId, String parentClass, String parentId) 
        throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        if (specialObjectId == null)
            throw new BusinessObjectNotFoundException(specialObjectClass, specialObjectId);
        if (parentId == null)
            throw new BusinessObjectNotFoundException(parentClass, parentId);
        if (specialObjectId.equals(parentId))
            throw new OperationNotPermittedException("An object can not be related to itself");
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node specialObjectNode = getInstanceOfClass(specialObjectClass, specialObjectId);
            for (Relationship rel : specialObjectNode.getRelationships(RelTypes.CHILD_OF_SPECIAL)) {
                String otherNodeUuid = rel.getOtherNode(specialObjectNode).hasProperty(Constants.PROPERTY_UUID) ? (String) rel.getOtherNode(specialObjectNode).getProperty(Constants.PROPERTY_UUID) : null;
                
                if (otherNodeUuid != null && otherNodeUuid.equals(parentId))
                    throw new OperationNotPermittedException("These elements are already related");
            }
            Node objectNode = getInstanceOfClass(parentClass, parentId);
            specialObjectNode.createRelationshipTo(objectNode, RelTypes.CHILD_OF_SPECIAL);
            tx.success();
        }            
    }
    
    @Override
    public String createHeadlessObject(String className, HashMap<String, String> attributes, String templateId)
            throws OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        ClassMetadata myClass= mem.getClass(className);
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {        
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

            if (myClass.isInDesign())
                throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");

            if (myClass.isAbstract())
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", className));

            Node newObject;
            if (templateId == null || templateId.isEmpty())
                newObject = createObject(classNode, myClass, attributes);
            else {
                try {
                    Node templateNode = connectionManager.getConnectionHandler().findNode(templateLabel, Constants.PROPERTY_UUID, templateId);
                    if (templateNode.hasRelationship(Direction.INCOMING, RelTypes.HAS_TEMPLATE)) {
                        if (className.equals(templateNode.getSingleRelationship(RelTypes.HAS_TEMPLATE, Direction.INCOMING).
                                getStartNode().getProperty(Constants.PROPERTY_NAME)))
                            newObject = copyTemplateElement(templateNode, myClass, true);
                        else
                            throw new InvalidArgumentException(String.format("The template with id %s is not applicable to instances of class %s", templateId, className));
                    } else 
                        throw new InvalidArgumentException(String.format("The template with id %s is malformed", templateId));
                    
                } catch (NotFoundException ex) {
                    throw new ApplicationObjectNotFoundException(String.format("No template with id %s was found for class %s", templateId, className));
                }
            }
            tx.success();
            return (String)newObject.getProperty(Constants.PROPERTY_UUID);
        }
    }
    
    @Override
    public String createPoolItem(String poolId, String className, HashMap<String, String> attributes, String templateId) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        
        try (Transaction tx =connectionManager.getConnectionHandler().beginTx()) {
            Node pool = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, poolId);
            
            if (pool == null)
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s could not be found", poolId));
            
            if (!pool.hasProperty(Constants.PROPERTY_CLASSNAME))
                throw new InvalidArgumentException("This pool has not set his class name attribute");
            if (className == null)
                throw new InvalidArgumentException("The class name  can not be null");
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            ClassMetadata classMetadata = mem.getClass(className);
            
            if (!mem.isSubclassOf((String)pool.getProperty(Constants.PROPERTY_CLASSNAME), className))
                throw new InvalidArgumentException(String.format("Class %s is not subclass of %s", className, (String)pool.getProperty(Constants.PROPERTY_CLASSNAME)));
            
            Node newObject = createObject(classNode, classMetadata, attributes);
            newObject.createRelationshipTo(pool, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
            
            tx.success();
            return newObject.hasProperty(Constants.PROPERTY_UUID) ? newObject.getProperty(Constants.PROPERTY_UUID).toString() : null;
        }
    }
    
    @Override
    public String[] createBulkObjects(String className, String parentClassName, String parentOid, String namePattern, String templateId) 
        throws MetadataObjectNotFoundException, OperationNotPermittedException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        if (parentOid == null)
            throw new InvalidArgumentException("The parent id cannot be null");
                
        ClassMetadata myClass = mem.getClass(className);
        
        if (!mem.canBeChild(parentClassName, className))
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
                       
            if (myClass.isInDesign())
                throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");
            
            if (myClass.isAbstract())
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", className));
            
            if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, className))
                throw new OperationNotPermittedException("Can not create non-inventory objects");
            //The object should be created under an instance other than the dummy root
            if (parentClassName != null && !parentOid.equals("-1")) {
                ClassMetadata myParentObjectClass = mem.getClass(parentClassName);
                if (myParentObjectClass == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", parentClassName));
            }
            Node parentNode;
            if (!parentOid.equals("-1")) { // Id -1 means the root of the containment hierarchy
                parentNode = getInstanceOfClass(parentClassName, parentOid);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentOid);
                
            }
            else
                parentNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            if (parentNode == null)
                throw new BusinessObjectNotFoundException(parentClassName, parentOid);
            
            DynamicNameGenerator dynamicName = new DynamicNameGenerator(namePattern);                                   
            String res[] = new String[dynamicName.getNumberOfDynamicNames()];
                        
            for (int i = 0; i < dynamicName.getNumberOfDynamicNames(); i++) {
                Node newObject;
                if (templateId == null || templateId.isEmpty())
                    newObject = createObject(classNode, myClass, null);
                else {
                    try {
                        Node templateNode = connectionManager.getConnectionHandler().findNode(templateLabel, Constants.PROPERTY_UUID, templateId);
                        if (templateNode.hasRelationship(Direction.INCOMING, RelTypes.HAS_TEMPLATE)) {
                            if (className.equals(templateNode.getSingleRelationship(RelTypes.HAS_TEMPLATE, Direction.INCOMING).
                                    getStartNode().getProperty(Constants.PROPERTY_NAME)))
                                newObject = copyTemplateElement(templateNode, myClass, true);
                            else
                                throw new InvalidArgumentException(String.format("The template with id %s is not applicable to instances of class %s", templateId, className));
                        } else 
                            throw new InvalidArgumentException(String.format("The template with id %s is malformed", templateId));

                    } catch (NotFoundException ex) {
                        throw new ApplicationObjectNotFoundException(String.format("No template with id %s was found for class %s", templateId, className));
                    }
                }

                res[i] = newObject.hasProperty(Constants.PROPERTY_UUID) ? newObject.getProperty(Constants.PROPERTY_UUID).toString() : null;
                newObject.setProperty(Constants.PROPERTY_NAME, dynamicName.getDynamicNames().get(i));
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF); 
            }
                        
            tx.success();
            return res;
        }
    }
    
    @Override
    public String[] createBulkSpecialObjects(String className, String parentClassName, String parentId, String namePattern, String templateId) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (parentId == null)
            throw new InvalidArgumentException("The parent id cannot be null");
                
        ClassMetadata myClass = mem.getClass(className);
        
        if (!mem.canBeChild(parentClassName, className))
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
                       
            if (myClass.isInDesign())
                throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");
            
            if (myClass.isAbstract())
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", className));
            
            if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, className))
                throw new OperationNotPermittedException("Can not create non-inventory objects");
            //The object should be created under an instance other than the dummy root
            if (parentClassName != null && !parentId.equals("-1")) {
                ClassMetadata myParentObjectClass = mem.getClass(parentClassName);
                if (myParentObjectClass == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", parentClassName));
            }
            Node parentNode;
            if (!parentId.equals("-1")) { // Id -1 means the root of the containment hierarchy
                parentNode = getInstanceOfClass(parentClassName, parentId);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentId);
                
            }
            else
                parentNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            if (parentNode == null)
                throw new BusinessObjectNotFoundException(parentClassName, parentId);
            
            DynamicNameGenerator dynamicName = new DynamicNameGenerator(namePattern);                                   
            String res[] = new String[dynamicName.getNumberOfDynamicNames()];
                        
            for (int i = 0; i < dynamicName.getNumberOfDynamicNames(); i++) {
                Node newObject;
                if (templateId == null || templateId.isEmpty())
                    newObject = createObject(classNode, myClass, null);
                else {
                    try {
                        Node templateNode = connectionManager.getConnectionHandler().findNode(templateLabel, Constants.PROPERTY_UUID, templateId);
                        if (templateNode.hasRelationship(Direction.INCOMING, RelTypes.HAS_TEMPLATE)) {
                            if (className.equals(templateNode.getSingleRelationship(RelTypes.HAS_TEMPLATE, Direction.INCOMING).
                                    getStartNode().getProperty(Constants.PROPERTY_NAME)))
                                newObject = copyTemplateElement(templateNode, myClass, true);
                            else
                                throw new InvalidArgumentException(String.format("The template with id %s is not applicable to instances of class %s", templateId, className));
                        } else 
                            throw new InvalidArgumentException(String.format("The template with id %s is malformed", templateId));

                    } catch (NotFoundException ex) {
                        throw new ApplicationObjectNotFoundException(String.format("No template with id %s was found for class %s", templateId, className));
                    }
                }

                res[i] = newObject.hasProperty(Constants.PROPERTY_UUID) ? newObject.getProperty(Constants.PROPERTY_UUID).toString() : null;
                newObject.setProperty(Constants.PROPERTY_NAME, dynamicName.getDynamicNames().get(i));
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL); 
            }
                        
            tx.success();
            return res;
        }
    }
    
    @Override
    public BusinessObject getObject(String className, String oid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            ClassMetadata myClass = mem.getClass(className);
            Node instance = getInstanceOfClass(className, oid);
            BusinessObject res = ogmService.createObjectFromNode(instance, myClass);
            tx.success();
            return res;
        }
    }

    @Override
    public BusinessObjectLight getObjectLight(String className, String oid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        if (oid == null) {
            InvalidArgumentException ex = new InvalidArgumentException(ts.getTranslatedString(KEY_PREFIX + ".16"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(16);
            throw ex;
        }
        //TODO: Re-write this method and check if a simple Cypher query is faster than the programatic solution!
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);

            if (classNode == null) {
                MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".17"), className));
                ex.setPrefix(KEY_PREFIX);
                ex.setCode(17);
                ex.setMessageArgs(className);
                throw ex;
            }
            Iterable<Relationship> iterableInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
            Iterator<Relationship> instances = iterableInstances.iterator();
            while (instances.hasNext()) {
                Node instance = instances.next().getStartNode();
                if (instance.getProperty(Constants.PROPERTY_UUID).equals(oid)) {
                    tx.success();
                    return ogmService.createObjectLightFromNode(instance);
                }
            }
            throw new BusinessObjectNotFoundException(className, oid);
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectsWithFilterLight (String className, 
            String filterName, String filterValue) throws InvalidArgumentException {
        if (className == null) {
            InvalidArgumentException ex = new InvalidArgumentException(ts.getTranslatedString(KEY_PREFIX + ".18"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(18);
            throw ex;
        }
        if (filterName == null) {
            InvalidArgumentException ex = new InvalidArgumentException(ts.getTranslatedString(KEY_PREFIX + ".19"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(19);
            throw ex;
        }
        if (filterValue == null) {
            InvalidArgumentException ex = new InvalidArgumentException(ts.getTranslatedString(KEY_PREFIX + ".20"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(20);
            throw ex;
        }
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (class:classes)<-[:EXTENDS*0..]-(subclass:classes{name:'%s'})<-[:EXTENDS*0..]-(:classes)<-[:INSTANCE_OF]-(object:inventoryObjects)").append("\n");
            queryBuilder.append("WHERE object.%s = '%s'").append("\n");
            queryBuilder.append("OR (object)-[:RELATED_TO{name:'%s'}]->(:listTypeItems{name:'%s'})").append("\n");
            queryBuilder.append("RETURN DISTINCT object");
            
            String query = String.format(queryBuilder.toString(), className, filterName, filterValue, filterName, filterValue);
            
            Result queryResult = connectionManager.getConnectionHandler().execute(query);
            ResourceIterator<Node> column = queryResult.columnAs("object");
            
            List<BusinessObjectLight> result = new ArrayList();
            while (column.hasNext())
                result.add(ogmService.createObjectLightFromNode(column.next()));
            tx.success();
            return result;
        }
    }
    
    @Override
    public List<BusinessObject> getObjectsWithFilter (String className, 
            String filterName, String filterValue) throws InvalidArgumentException {
        if (className == null) { 
            InvalidArgumentException ex = new InvalidArgumentException(ts.getTranslatedString(KEY_PREFIX + ".18"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(18);
            throw ex;
        }
        if (filterName == null) {
            InvalidArgumentException ex = new InvalidArgumentException(ts.getTranslatedString(KEY_PREFIX + ".19"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(19);
            throw ex;
        }
        if (filterValue == null) {
            InvalidArgumentException ex = new InvalidArgumentException(ts.getTranslatedString(KEY_PREFIX + ".20"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(20);
            throw ex;
        }
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (class:classes)<-[:EXTENDS*0..]-(subclass:classes{name:'%s'})<-[:EXTENDS*0..]-(:classes)<-[:INSTANCE_OF]-(object:inventoryObjects)").append("\n");
            queryBuilder.append("WHERE object.%s = '%s'").append("\n");
            queryBuilder.append("OR (object)-[:RELATED_TO{name:'%s'}]->(:listTypeItems{name:'%s'})").append("\n");
            queryBuilder.append("RETURN DISTINCT object");
            
            String query = String.format(queryBuilder.toString(), className, filterName, filterValue, filterName, filterValue);
            
            Result queryResult = connectionManager.getConnectionHandler().execute(query);
            ResourceIterator<Node> column = queryResult.columnAs("object");
            
            List<BusinessObject> result = new ArrayList();
            while (column.hasNext())
                result.add(ogmService.createObjectFromNode(column.next()));
            tx.success();
            return result;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSuggestedObjectsWithFilter(String filter, int limit) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String cypherQuery = "MATCH (object:" + inventoryObjectLabel + ")-[:INSTANCE_OF]->(class)" + 
                    " WHERE TOLOWER(object.name) CONTAINS TOLOWER({searchString}) OR TOLOWER(class.name) "
                    + "CONTAINS TOLOWER({searchString}) OR TOLOWER(class.displayName) CONTAINS TOLOWER({searchString}) " 
                    + "RETURN object.name as oname, object._uuid as oid, class.name as cname, class.displayName as cdisplay ORDER BY object.name ASC" + (limit > 0 ? " LIMIT " + limit : ""); //NOI18N
            
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("searchString", filter);
            Result queryResult = connectionManager.getConnectionHandler().execute(cypherQuery, parameters);
            
            List<BusinessObjectLight> res  = new ArrayList<>();
            
            while (queryResult.hasNext()) {
                Map<String, Object> row = queryResult.next();
                res.add(new BusinessObjectLight((String)row.get("cname"), (String)row.get("oid"), 
                        (String)row.get("oname"), (String)row.get("cdisplay")));
            }
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSuggestedObjectsWithFilter(String filter, String superClass, int limit) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String cypherQuery = "MATCH (object:" + inventoryObjectLabel + ")-[:INSTANCE_OF]->(class)" +
                    "-[:EXTENDS*0..]->(superclass) WHERE (TOLOWER(object.name) CONTAINS TOLOWER({searchString})" + 
                    " OR TOLOWER(class.name) CONTAINS TOLOWER({searchString}) OR TOLOWER(class.displayName) CONTAINS TOLOWER({searchString}))" + 
                    " AND superclass.name = {superclass}" + 
                    " RETURN object.name as oname, object._uuid as oid, class.name as cname, class.displayName as cdisplay ORDER BY object.name ASC" + (limit > 0 ? " LIMIT " + limit : "");

            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("searchString", filter);
            parameters.put("superclass", superClass);
            Result queryResult = connectionManager.getConnectionHandler().execute(cypherQuery, parameters);
            
            List<BusinessObjectLight> res  = new ArrayList<>();
            
            while (queryResult.hasNext()) {
                Map<String, Object> row = queryResult.next();
                res.add(new BusinessObjectLight((String)row.get("cname"), (String)row.get("oid"), 
                        (String)row.get("oname"), (String)row.get("cdisplay")));
            }
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSuggestedObjectsWithFilter(String filter, int skip, int limit, String... clasessToFilter) {
        List<BusinessObjectLight> objects = new ArrayList();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder queryBuilder = new StringBuilder();
            HashMap<String, Object> parameters = new HashMap();
            
            queryBuilder.append("MATCH (child:inventoryObjects)-[:INSTANCE_OF]->(childClass:classes)-[:EXTENDS*0..]->(childParentClass:classes)").append("\n"); //NOI18N
            if (filter == null)
                filter = "";
            parameters.put("filter", filter); //NOI18N
            queryBuilder.append("WHERE (TOLOWER(child.name) CONTAINS TOLOWER($filter) OR TOLOWER(childClass.name) CONTAINS TOLOWER($filter) OR TOLOWER(childClass.displayName) CONTAINS TOLOWER($filter))").append("\n"); //NOI18N
            
            if (clasessToFilter != null && clasessToFilter.length > 0) {
                queryBuilder.append("AND ("); //NOI18N
                for (int i = 0; i < clasessToFilter.length; i++) {
                    if (i < clasessToFilter.length - 1)
                        queryBuilder.append(String.format("childParentClass.name = '%s' OR ", clasessToFilter[i])); //NOI18N
                    else
                        queryBuilder.append(String.format("childParentClass.name = '%s'", clasessToFilter[i])); //NOI18N
                }
                queryBuilder.append(")").append("\n");
            }
            queryBuilder.append("RETURN DISTINCT child").append("\n"); //NOI18N
            queryBuilder.append("ORDER BY child.name").append("\n"); //NOI18N
            if (skip >= 0) {
                parameters.put("skip", skip); //NOI18N
                queryBuilder.append("SKIP $skip").append("\n"); //NOI18N
            }
            if (limit >= 0) {
                parameters.put("limit", limit); //NOI18N
                queryBuilder.append("LIMIT $limit").append("\n"); //NOI18N
            }
            Result queryResult = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (queryResult.hasNext()) {
                Map<String, Object> row = queryResult.next();
                objects.add(ogmService.createObjectLightFromNode((Node) row.get("child"))); //NOI18N
            }
            tx.success();
        }
        return objects;
    }
    
    @Override
    public List<BusinessObjectLight> getObjectsByNameAndClassName(List<String> names
            , int skip, int limit, String... clasessToFilter) 
            throws InvalidArgumentException 
    {
        if (names == null || names.isEmpty())
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.names-to-filter-non-null"));
        
        List<BusinessObjectLight> objects = new ArrayList();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder queryBuilder = new StringBuilder();
            HashMap<String, Object> parameters = new HashMap();
            
            queryBuilder.append("MATCH (child:inventoryObjects)-[:INSTANCE_OF]->(childClass:classes)-[:EXTENDS*0..]->(childParentClass:classes)").append("\n"); //NOI18N
            
            queryBuilder.append("WHERE (child.name) IN ['").append(String.join("', '",  names)).append("'] "); //NOI18N
            
            if (clasessToFilter != null && clasessToFilter.length > 0)
                queryBuilder.append("AND childParentClass.name IN ['")
                        .append(String.join("', '", clasessToFilter)).append("'] "); //NOI18N
            
            queryBuilder.append("RETURN DISTINCT child").append("\n"); //NOI18N
            queryBuilder.append("ORDER BY child.name").append("\n"); //NOI18N
            if (skip >= 0) {
                parameters.put("skip", skip); //NOI18N
                queryBuilder.append("SKIP $skip").append("\n"); //NOI18N
            }
            if (limit >= 0) {
                parameters.put("limit", limit); //NOI18N
                queryBuilder.append("LIMIT $limit").append("\n"); //NOI18N
            }
            Result queryResult = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (queryResult.hasNext()) {
                Map<String, Object> row = queryResult.next();
                objects.add(ogmService.createObjectLightFromNode((Node) row.get("child"))); //NOI18N
            }
            tx.success();
        }
         return objects;
    }
    
    @Override
    public List<BusinessObjectLight> getSuggestedChildrenWithFilter(String parentClass, String parentId, String filter, boolean ignoreSpecialChildren, int skip, int limit, String... clasessToFilter) {
        List<BusinessObjectLight> children = new ArrayList();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            StringBuilder queryBuilder = new StringBuilder();
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("parentClass", parentClass); //NOI18N
            parameters.put("parentId", parentId); //NOI18N
            if (ignoreSpecialChildren)
                queryBuilder.append("MATCH (parentClass:classes {name: $parentClass})<-[:INSTANCE_OF]-(parent:inventoryObjects {_uuid: $parentId})<-[:CHILD_OF]-(child:inventoryObjects)-[:INSTANCE_OF]->(childClass:classes)-[:EXTENDS*0..]->(childParentClass:classes)").append("\n"); //NOI18N
            else
                queryBuilder.append("MATCH (parentClass:classes {name: $parentClass})<-[:INSTANCE_OF]-(parent:inventoryObjects {_uuid: $parentId})<-[:CHILD_OF|:CHILD_OF_SPECIAL]-(child:inventoryObjects)-[:INSTANCE_OF]->(childClass:classes)-[:EXTENDS*0..]->(childParentClass:classes)").append("\n"); //NOI18N
            
            if (filter == null)
                filter = "";
            parameters.put("filter", filter); //NOI18N
            queryBuilder.append("WHERE (toLower(child.name) CONTAINS toLower($filter) OR toLower(childClass.name) CONTAINS toLower($filter))").append("\n"); //NOI18N
            
            if (clasessToFilter != null && clasessToFilter.length > 0) {
                queryBuilder.append("AND ("); //NOI18N
                for (int i = 0; i < clasessToFilter.length; i++) {
                    if (i < clasessToFilter.length - 1)
                        queryBuilder.append(String.format("childParentClass.name = '%s' OR ", clasessToFilter[i])); //NOI18N
                    else
                        queryBuilder.append(String.format("childParentClass.name = '%s'", clasessToFilter[i])); //NOI18N
                }
                queryBuilder.append(")").append("\n");
            }
            queryBuilder.append("RETURN DISTINCT child").append("\n"); //NOI18N
            queryBuilder.append("ORDER BY child.name").append("\n"); //NOI18N
            if (skip >= 0) {
                parameters.put("skip", skip); //NOI18N
                queryBuilder.append("SKIP $skip").append("\n"); //NOI18N
            }
            if (limit >= 0) {
                parameters.put("limit", limit); //NOI18N
                queryBuilder.append("LIMIT $limit").append("\n"); //NOI18N
            }
            Result queryResult = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (queryResult.hasNext()) {
                Map<String, Object> row = queryResult.next();
                children.add(ogmService.createObjectLightFromNode((Node) row.get("child")));
            }
            tx.success();
        }
        return children;
    }
    
    @Override
    public String getAttributeValueAsString (String objectClass, String objectId, String attributeName) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        ClassMetadata theClass = mem.getClass(objectClass);
        AttributeMetadata theAttribute = theClass.getAttribute(attributeName);
        
        BusinessObject theObject = getObject(objectClass, objectId);
        if (theObject.getAttributes().get(attributeName) == null)
            return null;
        else {

            switch (theAttribute.getType()) {
                case "String": //NOI18N
                case "Boolean": //NOI18N
                case "Integer": //NOI18N
                case "Float": //NOI18N
                case "Long": //NOI18N
                    return theObject.getAttributes().get(attributeName).toString();
                case "Date": //NOI18N
                case "Time": //NOI18N
                case "Timestamp": //NOI18N
                    return new Date((Long)theObject.getAttributes().get(attributeName)).toString();
                default: //It's (or at least should be) a list type
                    if (theAttribute.isMultiple()) {
                        List<BusinessObjectLight> attributeValues = (List<BusinessObjectLight>)theObject.getAttributes().get(attributeName);
                        return attributeValues.stream().map(aListTypeItem -> aListTypeItem.getName())
                                .collect(Collectors.joining(";"));
                    } else {
                        BusinessObjectLight ltItem = aem.getListTypeItem(theAttribute.getType(), (String) theObject.getAttributes().get(attributeName));
                        return ltItem.getName();
                    }
            }
        }
    }
    
    @Override
    public HashMap<String, String> getAttributeValuesAsString (String objectClass, String objectId) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
    
        BusinessObject theObject = getObject(objectClass, objectId);
        HashMap<String, String> res = new HashMap<>();
        
        for (String attributeName : theObject.getAttributes().keySet()) {
            AttributeMetadata theAttribute = mem.getAttribute(objectClass, attributeName);
            if (theObject.getAttributes().get(attributeName) == null)
                res.put(attributeName, null);
            else { 
                switch (theAttribute.getType()) {
                    case "String": //NOI18N
                    case "Boolean": //NOI18N
                    case "Integer": //NOI18N
                    case "Float": //NOI18N
                    case "Long": //NOI18N
                        res.put(attributeName, theObject.getAttributes().get(attributeName).toString());
                        break;
                    case "Date": //NOI18N
                    case "Time": //NOI18N
                    case "Timestamp": //NOI18N
                        res.put(attributeName, new Date((Long)theObject.getAttributes().get(attributeName)).toString());
                        break;
                    default: //It's (or at least should be) a list type
                        if (theAttribute.isMultiple()) {
                        List<BusinessObjectLight> attributeValues = (List<BusinessObjectLight>)theObject.getAttributes().get(attributeName);
                        res.put(attributeName, attributeValues.stream().map(aListTypeItem -> aListTypeItem.getName())
                                .collect(Collectors.joining(";")));
                    } else {
                            BusinessObjectLight ltItem = aem.getListTypeItem(theAttribute.getType(), (String) theObject.getAttributes().get(attributeName));                      
                            res.put(attributeName, (ltItem).getName());
                        }
                    }
            }
        }
        
        return res;
    }
    
    
    @Override
    public BusinessObjectLight getCommonParent(String aObjectClass, String aOid, String bObjectClass, String bOid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        String cypherQuery = "MATCH (objectA:inventoryObjects)-[:CHILD_OF|CHILD_OF_SPECIAL*]->(parentNode)<-[:CHILD_OF|CHILD_OF_SPECIAL*]-(objectB:inventoryObjects) "
                + "WHERE objectA._uuid = {objectAId} AND objectB._uuid = {objectBId} RETURN parentNode";
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("objectAId", aOid);
            parameters.put("objectBId", bOid);
            Result queryResult = connectionManager.getConnectionHandler().execute(cypherQuery, parameters);
            
            if (!queryResult.hasNext()) //There is no common parent
                return null;
            
            Node commonParentNode = (Node)queryResult.next().get("parentNode");
            Node commonParentClassNode = commonParentNode.getSingleRelationship(RelTypes.INSTANCE_OF, 
                        Direction.OUTGOING).getEndNode();
            tx.success();
            if (Constants.DUMMY_ROOT.equals(commonParentNode.getProperty(Constants.PROPERTY_NAME)))
                return new BusinessObjectLight(Constants.DUMMY_ROOT, "", Constants.DUMMY_ROOT, "Navigation Tree Root");
            else
                return new BusinessObjectLight((String)commonParentClassNode.getProperty(Constants.PROPERTY_NAME), (String)commonParentNode.getProperty(Constants.PROPERTY_UUID), 
                        (String)commonParentNode.getProperty(Constants.PROPERTY_NAME), (String)commonParentClassNode.getProperty(Constants.PROPERTY_DISPLAY_NAME, null));
        }
    }
    
    @Override
    public BusinessObjectLight getParent(String objectClass, String oid) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getInstanceOfClass(objectClass, oid);
            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF)) {
                Node parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();

                //If the direct parent is DummyRoot, return a dummy RemoteBusinessObject with oid = -1
                if (parentNode.hasProperty(Constants.PROPERTY_NAME) && Constants.NODE_DUMMYROOT.equals(parentNode.getProperty(Constants.PROPERTY_NAME)) ) {
                    tx.success();
                    return new BusinessObject(Constants.NODE_DUMMYROOT, "-1", Constants.NODE_DUMMYROOT);
                }
                else {
                     tx.success();
                     return ogmService.createObjectLightFromNode(parentNode);
                }   
            }
            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF_SPECIAL)) {
                Node parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                if (parentNode.hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING))
                    return ogmService.createObjectLightFromNode(parentNode);
                else
                    // Use the dummy root like parent to services, contracts, projects poolNode...
                    return new BusinessObject(Constants.NODE_DUMMYROOT, "-1", Constants.NODE_DUMMYROOT);
                
            }
            
            throw new InvalidArgumentException(String.format("The parent of %s (%s) could not be found", objectNode.getProperty(Constants.PROPERTY_NAME), oid));
        }
    }
    
    @Override
    public List<BusinessObjectLight> getParents (String objectClassName, String oid)
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        List<BusinessObjectLight> parents =  new ArrayList<>();
        String cypherQuery = "MATCH (n)-[:" + RelTypes.CHILD_OF + "|" + RelTypes.CHILD_OF_SPECIAL + "*]->(m) " +
                             "WHERE n._uuid = '" + oid + "' " +
                             "RETURN m as parents";
      
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<Node> column = result.columnAs("parents");
            for (Node node : Iterators.asIterable(column)) {  
                if (node.hasProperty(Constants.PROPERTY_NAME)) {
                    if (node.getProperty(Constants.PROPERTY_NAME).equals(Constants.NODE_DUMMYROOT)) {
                        parents.add(new BusinessObjectLight(Constants.NODE_DUMMYROOT, "-1", Constants.NODE_DUMMYROOT, "Navigation Tree Root"));
                        continue;
                    }
                }
                if (node.hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING))
                    parents.add(ogmService.createObjectLightFromNode(node));
                else //the node has a poolNode as a parent
                    parents.add(Util.createRemoteObjectLightFromPoolNode(node));
            }
        }
        return parents;
    }

    
    @Override
    public List<BusinessObjectLight> getParentsUntilFirstOfClass(String objectClass, 
            String oid, String... objectToMatchClassNames) 
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        List<BusinessObjectLight> parents =  new ArrayList<>();
        String cypherQuery = "MATCH (n)-[:" + RelTypes.CHILD_OF + "|" + RelTypes.CHILD_OF_SPECIAL + "*]->(m) " +
                             "WHERE n._uuid = '" + oid + "' " +
                             "RETURN m as parents";
      
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<Node> column = result.columnAs("parents");
            for (Node node : Iterators.asIterable(column)) {
                
                Label label = Label.label(Constants.LABEL_ROOT); //If the parent node is the dummy root, just return null
                if (node.hasLabel(label))
                    break;
                
                if (node.hasProperty(Constants.PROPERTY_NAME)) {
                    if (node.getProperty(Constants.PROPERTY_NAME).equals(Constants.NODE_DUMMYROOT))
                        break;
                }
                
                if (node.hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING)) {
                    parents.add(ogmService.createObjectLightFromNode(node));
                    
                    String parentNodeClass = Util.getClassName(node);
                    boolean isSubclassOf = false;
                    for (String objectToMatchClassName : objectToMatchClassNames) {
                        if (mem.isSubclassOf(objectToMatchClassName, parentNodeClass)) {
                            isSubclassOf = true;
                            break;
                        }
                    }
                    if (isSubclassOf)
                        break;
                }
                else //the node has a poolNode as a parent
                    parents.add(Util.createRemoteObjectLightFromPoolNode(node));
            }
            tx.success();
            return parents;
        }
    }
    
    @Override
    public BusinessObject getFirstParentOfClass(String objectClassName, String oid, String objectToMatchClassName)
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getInstanceOfClass(objectClassName, oid);
            while (true) {
                Node parentNode = null;
                if (objectNode.hasRelationship(RelTypes.CHILD_OF, Direction.OUTGOING))
                    parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();
                
                if (objectNode.hasRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING))
                    parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                
                if (parentNode == null) //If the object is orphan
                    return null;
                
                try {
                    String parentNodeClass = Util.getClassName(parentNode);

                    if (mem.isSubclassOf(objectToMatchClassName, parentNodeClass))
                        return ogmService.createObjectFromNode(parentNode);

                    objectNode = parentNode;
                } catch (MetadataObjectNotFoundException ex) { //If the parent object is either the Dummy Root or a pool
                    return null;
                }
            }
        }
    }
    
    @Override
    public List<BusinessObjectLight> getMultipleParents(String objectClassName, String objectId) throws InvalidArgumentException {
        if (objectClassName == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.get-first-parent.param.object-class-name.non-null"));
        if (objectId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.get-first-parent.param.object-id.non-null"));
        
        List<BusinessObjectLight> parents = new ArrayList();
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            final String paramObjectClassName = "objectClassName"; //NOI18N
            final String paramObjectId = "objectId"; //NOI18N
            final String columnParent = "parent"; //NOI18N
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(String.format(
                "MATCH (objectClass:classes{name: $%s})<-[:INSTANCE_OF]-(object{_uuid: $%s})-[:CHILD_OF|:CHILD_OF_SPECIAL]->(parent)", //NOI18N
                paramObjectClassName, paramObjectId
            ));
            queryBuilder.append("RETURN parent"); //NOI18N
            
            HashMap<String, Object> params = new HashMap();
            params.put(paramObjectClassName, objectClassName);
            params.put(paramObjectId, objectId);
            
            Result queryResult = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), params);
            while (queryResult.hasNext()) {
                Node nodeParent = (Node) queryResult.next().get(columnParent);
                parents.add(ogmService.createObjectLightFromNode(nodeParent));
            }
            tx.success();
            return parents;
        }
    }
    
    @Override
    public boolean isParent(String parentClass, String parentId, String childClass, String childId) throws InvalidArgumentException {
        if (parentClass == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent.parent-class"));
        if (parentId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent.parent-id"));
        if (childClass == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent.child-class"));
        if (childId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent.child-id"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            final String paramParentClass = "parentClass"; //NOI18N
            final String paramParentId = "parentId"; //NOI18N
            final String paramChildClass = "childId"; //NOI18N
            final String paramChildId = "childClass"; //NOI18N
            final String columnIsParent = "isParent"; //NOI18N
            
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH").append("\n"); //NOI18N
            queryBuilder.append(String.format("(parentClass:classes {name: $%s})", paramParentClass)); //NOI18N
            queryBuilder.append("<-[:INSTANCE_OF]-"); //NOI18N
            queryBuilder.append(String.format("(parent:inventoryObjects {_uuid: $%s})", paramParentId)); //NOI18N
            queryBuilder.append("<-[:CHILD_OF|:CHILD_OF_SPECIAL*]-"); //NOI18N
            queryBuilder.append(String.format("(child:inventoryObjects {_uuid: $%s})", paramChildId)); //NOI18N
            queryBuilder.append("-[:INSTANCE_OF]->"); //NOI18N
            queryBuilder.append(String.format("(childClass:classes {name: $%s})", paramChildClass)).append("\n"); //NOI18N
            queryBuilder.append("RETURN").append("\n"); //NOI18N
            queryBuilder.append(String .format("count(DISTINCT parent) = 1 AS %s", columnIsParent)); //NOI18N
            
            HashMap<String, Object> parameters = new HashMap();
            parameters.put(paramParentClass, parentClass);
            parameters.put(paramParentId, parentId);
            parameters.put(paramChildClass, childClass);
            parameters.put(paramChildId, childId);
            
            Result queryResult = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            
            return queryResult.hasNext() ? (boolean) queryResult.next().get(columnIsParent) : false;
        }
    }
    
    @Override
    public void deleteObjects(HashMap<String, List<String>> objects, boolean releaseRelationships)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            //TODO: Optimize so it can find all objects of a single class in one query
            for (String className : objects.keySet()) {
                for (String oid : objects.get(className)) {
                    ClassMetadata classMetadata = Util.createClassMetadataFromNode(connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className));
                    
                    if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, className))
                        throw new OperationNotPermittedException(String.format("Class %s is not a business-related class", className));

                    Node instance = getInstanceOfClass(className, oid);
                    
                    //Updates the unique attributes cache
                    try {
                        BusinessObject remoteObject = ogmService.createObjectFromNode(instance);
                        for(AttributeMetadata attribute : classMetadata.getAttributes()) {
                            if (attribute.isUnique()) { 
                                Object attributeValue = remoteObject.getAttributes().get(attribute.getName());
                                if (attributeValue != null)
                                    CacheManager.getInstance().removeUniqueAttributeValue(className, attribute.getName(), attributeValue);
                            }
                        }
                    } catch (InvalidArgumentException ex) {
                        //Should not happen
                    }
                    deleteObject(instance, releaseRelationships);
                }
            }
            tx.success();
        }
    }

    @Override
    public void deleteObject(String className, String oid, boolean releaseRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {
        HashMap<String, List<String>> objectsToDelete = new HashMap<>();
        objectsToDelete.put(className, Arrays.asList(oid));
        deleteObjects(objectsToDelete, releaseRelationships);
    }

    @Override
    public ChangeDescriptor updateObject(String className, String oid, HashMap<String, String> attributes)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {
        ClassMetadata classMetadata = mem.getClass(className);
        if (classMetadata == null) {
            MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".21"), className));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(21);
            ex.setMessageArgs(className);
            throw ex;
        }
        
        if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, className)) {
            InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".22"), className, Constants.CLASS_INVENTORYOBJECT));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(22);
            ex.setMessageArgs(className, Constants.CLASS_INVENTORYOBJECT);
            throw ex;
        }
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node instance = getInstanceOfClass(className, oid);

            ChangeDescriptor changes = ogmService.updateObject(oid, classMetadata, attributes);
            tx.success();
            
            return changes;
        }
    }
    
    @Override
    public void createSpecialRelationship(String aObjectClass, String aObjectId, String bObjectClass, String bObjectId, String name, boolean unique)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        createSpecialRelationship(aObjectClass, aObjectId, bObjectClass, bObjectId, name, unique, new HashMap<>());
    }
    
    @Override
    public void createSpecialRelationship(String aObjectClass, String aObjectId, String bObjectClass, 
        String bObjectId, String name, boolean unique, HashMap<String, Object> properties) 
        throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (aObjectId != null && bObjectId != null && aObjectId.equals(bObjectId)) {
            OperationNotPermittedException ex = new OperationNotPermittedException(ts.getTranslatedString(KEY_PREFIX + ".23"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(23);
            throw ex;
        }
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node nodeA = getInstanceOfClass(aObjectClass, aObjectId);
            for (Relationship rel : nodeA.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                String otherNodeUuid = rel.getOtherNode(nodeA).hasProperty(Constants.PROPERTY_UUID) ? (String) rel.getOtherNode(nodeA).getProperty(Constants.PROPERTY_UUID) : null;
                
                if (otherNodeUuid != null && otherNodeUuid.equals(bObjectId) 
                    && rel.getProperty(Constants.PROPERTY_NAME).equals(name) && unique) {
                    OperationNotPermittedException ex = new OperationNotPermittedException(ts.getTranslatedString(KEY_PREFIX + ".24"));
                    ex.setPrefix(KEY_PREFIX);
                    ex.setCode(24);
                    throw ex;
                }
            }
            
            Node nodeB = getInstanceOfClass(bObjectClass, bObjectId);
            Relationship rel = nodeA.createRelationshipTo(nodeB, RelTypes.RELATED_TO_SPECIAL);
            rel.setProperty(Constants.PROPERTY_NAME, name);
            
            //Custom properties
            properties.keySet().forEach((property) -> {
                rel.setProperty(property, properties.get(property));
            });
            
            tx.success();
        }
    }
    
    @Override
    public void releaseSpecialRelationship(String objectClass, String objectId, String otherObjectId, String name)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (otherObjectId == null) {
            InvalidArgumentException ex = new InvalidArgumentException(ts.getTranslatedString(KEY_PREFIX + ".25"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(25);
            throw ex;
        }
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node node = getInstanceOfClass(objectClass, objectId);
            for (Relationship rel : node.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                String otherNodeUuid = rel.getOtherNode(node).hasProperty(Constants.PROPERTY_UUID) ? rel.getOtherNode(node).getProperty(Constants.PROPERTY_UUID).toString() : null;
                
                if ((rel.getProperty(Constants.PROPERTY_NAME).equals(name) && 
                        (otherNodeUuid != null && otherNodeUuid.equals(otherObjectId)) || otherObjectId.equals("-1")))
                    rel.delete();
            }
            tx.success();
        }
    }
    
    @Override
    public void releaseSpecialRelationshipInTargetObject(String objectClass, String objectId, String relationshipName, String targetId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node node = getInstanceOfClass(objectClass, objectId);
            for (Relationship rel : node.getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO_SPECIAL)) {
                
                String endNodeUuid = rel.getEndNode().hasProperty(Constants.PROPERTY_UUID) ? rel.getEndNode().getProperty(Constants.PROPERTY_UUID).toString() : null;
                
                if (rel.getProperty(Constants.PROPERTY_NAME).equals(relationshipName) &&
                            endNodeUuid != null && endNodeUuid.equals(targetId))
                    rel.delete();
            }
            tx.success();
        }
    }
    
    @Override
    public void moveObjectsToPool(String targetClassName, String targetOid, HashMap<String, String[]> objects)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);
        
        boolean isPool = true;
        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newParentNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, targetOid);
            
            if (newParentNode == null) {
                isPool = false;
                
                newParentNode = connectionManager.getConnectionHandler().findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, targetOid);
                
                if (newParentNode == null)
                    throw new BusinessObjectNotFoundException(targetClassName, targetOid);
            }
            
            for (String myClass : objects.keySet()) {
                Node instanceClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (String oid : objects.get(myClass)) {
                    Node instance = getInstanceOfClass(instanceClassNode, oid);
                    //If the object was specialChild of a poolNode
                    if (instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().hasNext()) {
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    if (isPool)
                        instance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
                    else
                        instance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL);
                }
            }
            tx.success();
        }
    }

    @Override
    public void moveObjects(String targetClassName, String targetOid, HashMap<String, String[]> objects)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);
        
        if (newParentClass == null) {
            MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".26"), targetClassName));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(26);
            ex.setMessageArgs(targetClassName);
            throw ex;
        }
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            for (String myClass : objects.keySet()) {
                if (!mem.canBeChild(targetClassName, myClass)) {
                    OperationNotPermittedException ex = new OperationNotPermittedException(String.format(ts.getTranslatedString(KEY_PREFIX + ".27"), myClass, targetClassName));
                    ex.setPrefix(KEY_PREFIX);
                    ex.setCode(27);
                    ex.setMessageArgs(myClass, targetClassName);
                    throw ex;
                }

                Node instanceClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null) {
                    MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(KEY_PREFIX + ".28"), myClass));
                    ex.setPrefix(KEY_PREFIX); 
                    ex.setCode(28);
                    ex.setMessageArgs(myClass);
                    throw ex;
                }
                for (String oid : objects.get(myClass)) {
                    Node instance = getInstanceOfClass(instanceClassNode, oid);
                    if (instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().hasNext()) {
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    //If the object was specialChild of a poolNode
                    if (instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().hasNext()) {
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    
                    instance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF);
                }
            }
            tx.success();
        }
    }
    
    @Override
    public void moveSpecialObjects(String targetClassName, String targetOid, HashMap<String, String[]> objects)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);
        
        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            for (String myClass : objects.keySet()) {
                //check if can be special child only if is not a physical connection, 
                //this is to allow moving physical links in and out of the wire containers, without modifying the hierarchy containment
                if (!mem.isSubclassOf(Constants.CLASS_PHYSICALCONNECTION, myClass)) {
                    if (!mem.canBeSpecialChild(targetClassName, myClass))
                    throw new OperationNotPermittedException(String.format("An instance of class %s can not be special child of an instance of class %s", myClass,targetClassName));
                }
                
                Node instanceClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (String oid : objects.get(myClass)) {
                    Node instance = getInstanceOfClass(instanceClassNode, oid);
                    
                    if (instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().hasNext()) {
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    
                    if (instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().hasNext()) {
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    instance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL);
                }
            }
            tx.success();
        }
    }
    
    @Override
    public void movePoolItem(String poolId, String poolItemClassName, String poolItemId) throws 
        ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, 
        MetadataObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node poolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, poolId);
            
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s could not be found", poolId));
            
            if (!poolNode.hasProperty(Constants.PROPERTY_CLASSNAME))
                throw new InvalidArgumentException("This pool has not set his class name attribute");
            
            Node poolItemClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, poolItemClassName);
            
            if (poolItemClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", poolItemClassName));
            
            if (!mem.isSubclassOf((String) poolNode.getProperty(Constants.PROPERTY_CLASSNAME), poolItemClassName))
                throw new InvalidArgumentException(String.format("Class %s is not subclass of %s", poolItemClassName, (String) poolNode.getProperty(Constants.PROPERTY_CLASSNAME)));
                                    
            Node instance = getInstanceOfClass(poolItemClassNode, poolItemId);
            
            for (Relationship relationship : instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING)) {
                if (relationship.hasProperty(Constants.PROPERTY_NAME)) {
                    if (Constants.REL_PROPERTY_POOL.equals((String) relationship.getProperty(Constants.PROPERTY_NAME)))
                        relationship.delete();
                }
            }
            instance.createRelationshipTo(poolNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
            tx.success();
        }
    }
    
    @Override
    public String[] copyObjects(String targetClassName, String targetOid, HashMap<String, List<String>> objects, boolean recursive)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);

        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            String[] res = new String[objects.size()];
            int i = 0;
            for (String myClass : objects.keySet()) {
                if (!mem.canBeChild(targetClassName, myClass))
                    throw new OperationNotPermittedException(String.format("An instance of class %s can not be child of an instance of class %s", myClass,targetClassName));

                Node instanceClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (String oid : objects.get(myClass)) {
                    Node templateObject = getInstanceOfClass(instanceClassNode, oid);
                    Node newInstance = ogmService.copyObject(templateObject, recursive, inventoryObjectLabel);
                    newInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF);
                    String newInstanceUuid = newInstance.hasProperty(Constants.PROPERTY_UUID) ? (String) newInstance.getProperty(Constants.PROPERTY_UUID) : null;
                    if (newInstanceUuid == null)
                        throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", newInstance.getId()));                        
                    res[i] = newInstanceUuid;
                    i++;            
                }
            }
            tx.success();
            return res;
        }        
    }
    
    @Override
    public String[] copySpecialObjects(String targetClassName, String targetOid, HashMap<String, List<String>> objects, boolean recursive)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);

        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            String[] res = new String[objects.size()];
            int i = 0;
            for (String myClass : objects.keySet()) {
                Node instanceClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (String oid : objects.get(myClass)) {
                    Node templateObject = getInstanceOfClass(instanceClassNode, oid);
                    Node newInstance = ogmService.copyObject(templateObject, recursive, inventoryObjectLabel);
                    newInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL);
                    String newInstanceUuid = newInstance.hasProperty(Constants.PROPERTY_UUID) ? (String) newInstance.getProperty(Constants.PROPERTY_UUID) : null;
                    if (newInstanceUuid == null)
                        throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", newInstance.getId()));
                    res[i] = newInstanceUuid;
                    i++;            
                }
            }
            tx.success();
            return res;
        }        
    }
    
    @Override
    public String copyPoolItem(String poolId, String poolItemClassName, String poolItemId, boolean recursive) throws 
        ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, 
        MetadataObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node poolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, poolId);
            
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s could not be found", poolId));
            
            if (!poolNode.hasProperty(Constants.PROPERTY_CLASSNAME))
                throw new InvalidArgumentException("This pool has not set his class name attribute");
            
            Node poolItemClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, poolItemClassName);
            
            if (poolItemClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", poolItemClassName));
            
            if (!mem.isSubclassOf((String) poolNode.getProperty(Constants.PROPERTY_CLASSNAME), poolItemClassName))
                throw new InvalidArgumentException(String.format("Class %s is not subclass of %s", poolItemClassName, (String) poolNode.getProperty(Constants.PROPERTY_CLASSNAME)));
                                    
            Node instance = getInstanceOfClass(poolItemClassNode, poolItemId);
            
            Node newInstance = ogmService.copyObject(instance, recursive, inventoryObjectLabel);
            String newInstanceUuid = newInstance.hasProperty(Constants.PROPERTY_UUID) ? (String) newInstance.getProperty(Constants.PROPERTY_UUID) : null;
            if (newInstanceUuid == null)
                throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", newInstance.getId()));
            newInstance.createRelationshipTo(poolNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);            
            tx.success();
            return newInstanceUuid;
        }
    }

    @Override
    public List<BusinessObjectLight> getObjectChildren(String className, String oid, int maxResults)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException  {
        try (Transaction tx =  connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode;
            if (oid != null && oid.equals("-1"))
                parentNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            else
                parentNode = getInstanceOfClass(className, oid);
            
            Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            Iterator<Relationship> instances = children.iterator();
            List<BusinessObjectLight> res = new ArrayList<>();

            if (maxResults > 0) {
                int counter = 0;
                while(children.iterator().hasNext() && (counter < maxResults)) {
                    counter++;
                    Node child = children.iterator().next().getStartNode();
                    res.add(ogmService.createObjectLightFromNode(child));
                }
            } else {
                while(instances.hasNext()) {
                    Node child = instances.next().getStartNode();
                    res.add(ogmService.createObjectLightFromNode(child));
                }
            }
            
            Collections.sort(res);
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectChildren(long classId, String oid, int maxResults)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode;
            if (oid != null && oid.equals("-1"))
                parentNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            else
                parentNode = getInstanceOfClass(classId, oid);
            
            Iterable<Relationship> iterableChildren = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            Iterator<Relationship> children = iterableChildren.iterator();
            List<BusinessObjectLight> res = new ArrayList<>();
            if (maxResults > 0) {
                int counter = 0;
                while(children.hasNext() && (counter < maxResults)) {
                    counter++;
                    Node child = children.next().getStartNode();
                    res.add(ogmService.createObjectLightFromNode(child));
                }
            } else {
                while(children.hasNext()) {
                    Node child = children.next().getStartNode();
                    res.add(ogmService.createObjectLightFromNode(child));
                }
            }
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSiblings(String className, String oid, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node node = getInstanceOfClass(className, oid);
            List<BusinessObjectLight> res = new ArrayList<>();
            
            if (!node.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF))
                return res;
            
            Node parentNode = node.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();
            
            int resultCounter = 0;
            for (Relationship rel : parentNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF)) {
                if (maxResults > 0) {
                    if (resultCounter < maxResults)
                        resultCounter ++;
                    else
                        break;
                }
                
                Node child = rel.getStartNode();
                
                String childUuid = child.hasProperty(Constants.PROPERTY_UUID) ? (String) child.getProperty(Constants.PROPERTY_UUID) : null;
                if (childUuid == null)
                    throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", child.getId()));
                
                if (childUuid.equals(oid))
                    continue;
                
                res.add(ogmService.createObjectLightFromNode(child));
            }
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectsOfClassLight(String className, HashMap <String, String> filters, long skip, long limit) throws InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (className == null)
                throw new InvalidArgumentException("The className cannot be null");
            
            Node classMetadataNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(className);

            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            StringBuilder queryFilterBuilder = new StringBuilder();
            
            if(filters != null){
                filters.entrySet().forEach(entry -> {
                    String key = (String)entry.getKey();
                    String val = (String)entry.getValue();
                    if (val != null) {
                        parameters.put(key, val);
                        queryFilterBuilder.append(String.format("AND TOLOWER(instance.%s) CONTAINS TOLOWER($%s) ", key, key)); //NOI18N
                    }
                });
            }
                        
            if ((Boolean) classMetadataNode.getProperty(Constants.PROPERTY_ABSTRACT))
                queryBuilder.append("MATCH (class:classes)<-[:EXTENDS*]-(subclass:classes)<-[:INSTANCE_OF]-"); //NOI18N
            else 
                queryBuilder.append("MATCH (class:classes)<-[:INSTANCE_OF]-"); //NOI18N
            
            queryBuilder.append("(instance:" + Constants.LABEL_INVENTORY_OBJECTS + ") "); //NOI18N
            queryBuilder.append("WHERE class.name = $className "); //NOI18N
            queryBuilder.append(queryFilterBuilder.toString());
            queryBuilder.append("RETURN instance AS instance "); //NOI18N
            queryBuilder.append("ORDER BY instance.name ASC "); //NOI18N
            
            if(skip >= 0 && limit >= 0){
                queryBuilder.append("SKIP $skip "); //NOI18N
                parameters.put("skip", skip); //NOI18N
            }
            
            if(limit >= 0){
                queryBuilder.append("LIMIT $limit "); //NOI18N
                parameters.put("limit", limit); //NOI18N
            }
            parameters.put("className", className); //NOI18N
            
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            List<BusinessObjectLight> objectChildren = new ArrayList();
            while (result.hasNext())
                objectChildren.add(ogmService.createObjectLightFromNode((Node) result.next().get("instance")));
            tx.success();
            return objectChildren;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectsOfClassLight(String className, long skip, long limit) throws InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (className == null)
                throw new InvalidArgumentException("The className cannot be null");

            Node classMetadataNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);

            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(className);

            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            StringBuilder queryFilterBuilder = new StringBuilder();


            if ((Boolean) classMetadataNode.getProperty(Constants.PROPERTY_ABSTRACT))
                queryBuilder.append("MATCH (class:classes)<-[:EXTENDS*]-(subclass:classes)<-[:INSTANCE_OF]-"); //NOI18N
            else
                queryBuilder.append("MATCH (class:classes)<-[:INSTANCE_OF]-"); //NOI18N
     
            queryBuilder.append("(instance:" + Constants.LABEL_INVENTORY_OBJECTS + ") "); //NOI18N
            queryBuilder.append("WHERE class.name = $className "); //NOI18N
            queryBuilder.append(queryFilterBuilder.toString());
            queryBuilder.append("RETURN instance AS instance "); //NOI18N
            queryBuilder.append("ORDER BY instance.name ASC "); //NOI18N

            if (skip >= 0 && limit >= 0) {
                queryBuilder.append("SKIP $skip "); //NOI18N
                parameters.put("skip", skip); //NOI18N
            }

            if (limit >= 0) {
                queryBuilder.append("LIMIT $limit "); //NOI18N
                parameters.put("limit", limit); //NOI18N
            }
            parameters.put("className", className); //NOI18N

            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            List<BusinessObjectLight> objectChildren = new ArrayList();
            while (result.hasNext())
                objectChildren.add(ogmService.createObjectLightFromNode((Node) result.next().get("instance")));
            tx.success();
            return objectChildren;
        }
    }
    
    @Override
    public List<BusinessObject> getObjectsOfClass(String className, int maxResults)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classMetadataNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(className);
                                                
            List<BusinessObject> instances = new ArrayList<>();
            int counter = 0;
            
            boolean isAbstract = (Boolean) classMetadataNode.getProperty(Constants.PROPERTY_ABSTRACT);
            
            String cypherQuery;
            
            if (isAbstract) {
                cypherQuery = "MATCH (class:classes)<-[:EXTENDS*]-(subclass:classes)<-[:INSTANCE_OF]-(instance:" + Constants.LABEL_INVENTORY_OBJECTS + ") "
                            + "WHERE class.name=\"" + className + "\" "
                            + "RETURN instance ORDER BY instance.name ASC";                
            } else {
                cypherQuery = "MATCH (class:classes)<-[:INSTANCE_OF]-(instance:" + Constants.LABEL_INVENTORY_OBJECTS + ") "
                            + "WHERE class.name=\"" + className + "\" "
                            + "RETURN instance ORDER BY instance.name ASC";
            }
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            ResourceIterator<Node> instanceColumn = result.columnAs("instance");
            List<Node> lstInstanceColumn = Iterators.asList(instanceColumn);
            
            for (Node instance : lstInstanceColumn) {
                if (maxResults > 0) {
                    if (counter < maxResults)
                        counter ++;
                    else break;
                }
                instances.add(ogmService.createObjectFromNode(instance));                                                                                
            }
            
            tx.success();
            return instances;
        }
    }

    @Override
    public List<BusinessObject> getChildrenOfClass(String parentOid, String parentClass, String classToFilter, int page, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<BusinessObject> res = new ArrayList<>();
            
            Node classMetadataNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, parentClass);
            
            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(parentClass);

            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            parameters.put("className", parentClass); //NOI18N
            parameters.put("parentOid", parentOid); //NOI18N
            parameters.put("classToFilter", classToFilter); //NOI18N
            
            queryBuilder.append("MATCH (pc:classes {name:$className})<-[:INSTANCE_OF]-"); //NOI18N
            queryBuilder.append("(p:inventoryObjects {_uuid:$parentOid})<-[:CHILD_OF|CHILD_OF_SPECIAL]-(o:inventoryObjects)"); //NOI18N
            queryBuilder.append("-[:INSTANCE_OF]->(childClass:classes {name:$classToFilter})"); //NOI18N
            queryBuilder.append(" RETURN o as child"); //NOI18N
            queryBuilder.append(" ORDER BY o.name "); //NOI18N
            
            if(maxResults > 0) {
                parameters.put("limit", maxResults); //NOI18N
                queryBuilder.append("LIMIT $limit "); //NOI18N
            }
            if(page > 0){
                parameters.put("skip", page); //NOI18N
                queryBuilder.append("SKIP $skip "); //NOI18N
            }
            
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (result.hasNext()){
                ClassMetadata classMetadata = mem.getClass(classToFilter);
                res.add(ogmService.createObjectFromNode((Node) result.next().get("child"), classMetadata));
            }
           
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSpecialChildrenOfClassLight(String parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode = getInstanceOfClass(parentClass, parentOid);
            
            List<BusinessObjectLight> res = new ArrayList<>();
            int counter = 0;
            
            for (Relationship specialChildRelationships : parentNode.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING)) {
                BusinessObjectLight specialChild = ogmService.createObjectLightFromNode(specialChildRelationships.getStartNode());
                
                if (mem.isSubclassOf(classToFilter, specialChild.getClassName())) {
                    res.add(specialChild);
                    if (maxResults > 0 && ++counter == maxResults)
                        break;
                }
            }
            tx.success();
            return res;
        }
    }
    
    
    @Override
    public List<BusinessObjectLight> getChildrenOfClassLightRecursive(String parentOid, String parentClass, 
            String classToFilter, HashMap <String, String> attributesToFilter, int page, int limit) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException,
            InvalidArgumentException {
        List<BusinessObjectLight> res = new ArrayList<>();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classMetadataNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, classToFilter);
            final String CHILD = "child"; //NOI18N
            
            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(classToFilter);

            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            StringBuilder queryFilterBuilder = new StringBuilder();
            
            //Attributes to filter
            if(attributesToFilter != null){
                attributesToFilter.entrySet().forEach(entry -> {
                    String key = (String)entry.getKey();
                    String val = (String)entry.getValue();
                    if (val != null) {
                        parameters.put(key, val);
                        queryFilterBuilder.append(String.format("AND TOLOWER(o.%s) CONTAINS TOLOWER($%s) ", key, key)); //NOI18N
                    }
                });
            }
            
            boolean isAbstract = (Boolean) classMetadataNode.getProperty(Constants.PROPERTY_ABSTRACT);
            
            queryBuilder.append("MATCH (parentClass:classes {name:$className})<-[:INSTANCE_OF]-"); //NOI18N
            queryBuilder.append("(parent:inventoryObjects {_uuid:$parentOid})<-[:CHILD_OF*]-"); //NOI18N
            
            if(isAbstract)
                queryBuilder.append("(o:inventoryObjects)-[:INSTANCE_OF]->(x:classes)-[:EXTENDS*]->(c:classes {name:$classToFilter})"); //NOI18N
            else
                queryBuilder.append("(o:inventoryObjects)-[:INSTANCE_OF]->(childClass:classes {name:$classToFilter}) "); //NOI18N
            
            queryBuilder.append("WHERE parentClass.name=$className "); //NOI18N            
            queryBuilder.append(queryFilterBuilder);
            queryBuilder.append(String.format("RETURN o AS %s ", CHILD)); //NOI18N                
            if(page >= 0){
                queryBuilder.append("SKIP $page "); //NOI18N                
                parameters.put("page", page); //NOI18N
            }
            if(limit >= 0){
                queryBuilder.append("LIMIT $limit "); //NOI18N                
                parameters.put("limit", limit); //NOI18N
            }
            queryBuilder.append(";"); //NOI18N
            
            parameters.put("className", parentClass); //NOI18N
            parameters.put("parentOid", parentOid); //NOI18N
            parameters.put("classToFilter", classToFilter); //NOI18N
        
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (result.hasNext())
                res.add(ogmService.createObjectLightFromNode((Node) result.next().get(CHILD)));
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSpecialChildrenOfClassLightRecursive(String parentOid, String parentClass, String classToFilter, int maxResults) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        List<BusinessObjectLight> res = new ArrayList<>();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (parentOid == null)
                throw new InvalidArgumentException("The className cannot be null");
            if (parentClass == null)
                throw new InvalidArgumentException("The parentClass cannot be less than 0");
            if (classToFilter == null)
                throw new InvalidArgumentException("The classToFilter cannot be null");
            
            Node classMetadataNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, classToFilter);
            final String CHILD = "child"; //NOI18N
            
            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(classToFilter);

            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            
            boolean isAbstract = (Boolean) classMetadataNode.getProperty(Constants.PROPERTY_ABSTRACT);
            parameters.put("className", parentClass); //NOI18N
            parameters.put("parentOid", parentOid); //NOI18N
            parameters.put("classToFilter", classToFilter); //NOI18N
            
        
            queryBuilder.append("MATCH (parentClass:classes {name:$className})<-[:INSTANCE_OF]-"); //NOI18N
            queryBuilder.append("(parent:inventoryObjects {_uuid:$parentOid})<-[:CHILD_OF_SPECIAL*]-"); //NOI18N
            if(isAbstract)
                queryBuilder.append("(o:inventoryObjects)-[:INSTANCE_OF]->(x:classes)-[:EXTENDS*]->(c:classes {name:$classToFilter})"); //NOI18N
            else
                queryBuilder.append("(o:inventoryObjects)-[:INSTANCE_OF]->(childClass:classes {name:$classToFilter}) "); //NOI18N
            
            queryBuilder.append(String.format("RETURN o AS %s ", CHILD)); //NOI18N     
            if(maxResults > 0){
                queryBuilder.append("LIMIT $limit"); //NOI18N                
                parameters.put("limit", maxResults); //NOI18N
            }
        
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (result.hasNext())
                res.add(ogmService.createObjectLightFromNode((Node) result.next().get(CHILD)));
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getChildrenOfClassLight(String parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException  {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode = getInstanceOfClass(parentClass, parentOid);
            List<BusinessObjectLight> res = new ArrayList<>();

            int counter = 0;

            for (Relationship childOfRelationship : parentNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF, RelTypes.CHILD_OF_SPECIAL)) {
                Node child = childOfRelationship.getStartNode();

                if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                    throw new MetadataObjectNotFoundException(String.format("Class for %s (%s) could not be found", 
                            child.getProperty(Constants.PROPERTY_NAME), child.getId()));

                Node classNode = child.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode();
                String className = (String)classNode.getProperty(Constants.PROPERTY_NAME);
                if (mem.isSubclassOf(classToFilter, className)) {
                    String childUuid = child.hasProperty(Constants.PROPERTY_UUID) ? (String) child.getProperty(Constants.PROPERTY_UUID) : null;
                    if (childUuid == null)                                        
                        throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", child.getId()));
                    res.add(ogmService.createObjectLightFromNode(child));
                    if (maxResults > 0) {
                        if (++counter == maxResults)
                            break;
                    }
                }
            }
            
            tx.success();
            return res;
        }
    }

    @Override
    public List<BusinessObjectLight> getSpecialAttribute(String objectClass, String objectId, String specialAttributeName) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node instance = getInstanceOfClass(objectClass, objectId);
            List<BusinessObjectLight> res = new ArrayList<>();
            for (Relationship rel : instance.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                if (rel.hasProperty(Constants.PROPERTY_NAME)) {
                    if (rel.getProperty(Constants.PROPERTY_NAME).equals(specialAttributeName)) {
                        //String uuid = instance.hasProperty(Constants.PROPERTY_UUID) ? (String) instance.getProperty(Constants.PROPERTY_UUID) : null;
                        String endNodeUuid = rel.getEndNode().hasProperty(Constants.PROPERTY_UUID) ? (String) rel.getEndNode().getProperty(Constants.PROPERTY_UUID) : null;
                        if (endNodeUuid == null) {
                            InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".29"), rel.getEndNode()));
                            ex.setPrefix(KEY_PREFIX);
                            ex.setCode(29);
                            ex.setMessageArgs(rel.getEndNode());
                            throw ex;
                        }
                        res.add(endNodeUuid.equals(objectId) ? 
                            ogmService.createObjectLightFromNode(rel.getStartNode()) : ogmService.createObjectLightFromNode(rel.getEndNode()));
                    }
                }
            }
            tx.success();
            return res;
        }
    }

    @Override
    public List<AnnotatedBusinessObjectLight> getAnnotatedSpecialAttribute(String objectClass, String objectId, String specialAttributeName) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node instance = getInstanceOfClass(objectClass, objectId);
            List<AnnotatedBusinessObjectLight> res = new ArrayList<>();
            for (Relationship rel : instance.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                if (rel.hasProperty(Constants.PROPERTY_NAME)) {
                    if (rel.getProperty(Constants.PROPERTY_NAME).equals(specialAttributeName)) {
                        String endNodeUuid = rel.getEndNode().hasProperty(Constants.PROPERTY_UUID) ? (String) rel.getEndNode().getProperty(Constants.PROPERTY_UUID) : null;
                        if (endNodeUuid == null)
                            throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", rel.getEndNode().getId()));                                
                        BusinessObjectLight theObject = endNodeUuid.equals(objectId) ? 
                            ogmService.createObjectLightFromNode(rel.getStartNode()) : ogmService.createObjectLightFromNode(rel.getEndNode());
                        res.add(new AnnotatedBusinessObjectLight(theObject, rel.getAllProperties()));
                    }
                }
            }
            return res;
        }
    }

    @Override
    public HashMap<String, List<BusinessObjectLight>> getSpecialAttributes(String className, String objectId, String... attributeNames) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        HashMap<String,List<BusinessObjectLight>> res = new HashMap<>();
        List<String> attributeNamesAsList = Arrays.asList(attributeNames);
        boolean returnAll = attributeNames.length == 0;
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);
            for (Relationship rel : objectNode.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                String relName = (String)rel.getProperty(Constants.PROPERTY_NAME);
                if (attributeNamesAsList.contains(relName) || returnAll) {
                    List<BusinessObjectLight> currentObjects = res.get(relName);
                    if (currentObjects == null) {
                        currentObjects = new ArrayList<>();
                        res.put(relName, currentObjects);
                    }
                    currentObjects.add(ogmService.createObjectLightFromNode(rel.getOtherNode(objectNode)));
                }
            }
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectSpecialChildren(String objectClass, String objectId)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException  
    {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (objectId == null)
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent-pool.id.non-null"));
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            final String CHILD_NODE = "specialInstance"; //NOI18N
                        
            if (objectClass == null || objectClass.equals(Constants.NODE_DUMMYROOT)){
                queryBuilder.append("MATCH (specialNode:specialNodes {name:'DummyRoot'})"); //NOI18N
                queryBuilder.append("<-[:").append(RelTypes.CHILD_OF).append("]-"); //NOI18N
                queryBuilder.append("(object:inventoryObjects {_uuid:$objectId})"); //NOI18N
                queryBuilder.append("<-[r:").append(RelTypes.CHILD_OF_SPECIAL).append("]-(").append(CHILD_NODE).append(") "); //NOI18N
                queryBuilder.append("WHERE NOT EXISTS(r.name) OR (EXISTS (r.name) AND (r.name='").append(Constants.REL_PROPERTY_POOL).append("'))"); //NOI18N
                queryBuilder.append(" RETURN ").append(CHILD_NODE).append(" "); //NOI18N
            }
                          
            queryBuilder.append("MATCH (class:classes {name:$objectClass})"); //NOI18N
            queryBuilder.append("<-[:").append(RelTypes.INSTANCE_OF).append("]-"); //NOI18N
            queryBuilder.append("(object:inventoryObjects {_uuid:$objectId})"); //NOI18N
            queryBuilder.append("<-[r:").append(RelTypes.CHILD_OF_SPECIAL).append("]-(").append(CHILD_NODE).append(") "); //NOI18N
            queryBuilder.append("WHERE NOT EXISTS(r.name) OR (EXISTS (r.name) AND (r.name='").append(Constants.REL_PROPERTY_POOL).append("'))"); //NOI18N
            queryBuilder.append(" RETURN ").append(CHILD_NODE).append(" "); //NOI18N
           
            parameters.put("objectId", objectId); //NOI18N
            parameters.put("objectClass", objectClass); //NOI18N
            
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            List<BusinessObjectLight> instanceInPoolChildren = new ArrayList();
            while (result.hasNext())
                instanceInPoolChildren.add(ogmService.createObjectLightFromNode((Node) result.next().get(CHILD_NODE)));
            tx.success();
            return instanceInPoolChildren;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectSpecialChildrenWithFilters(String objectClass, 
            String objectId, List<String> childrenClassNamesToFilter, int page, int limit)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException  
    {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (objectId == null)
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent-pool.id.non-null"));
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            final String CHILD_NODE = "specialInstance"; //NOI18N
            
             //We create the class names to filter
            for (int i = 0; i < childrenClassNamesToFilter.size(); i++) 
                childrenClassNamesToFilter.set(i, "'".concat(childrenClassNamesToFilter.get(i)).concat("'"));
        
            String classes = String.join(" ,", childrenClassNamesToFilter);
                        
            if (objectClass == null || objectClass.equals(Constants.NODE_DUMMYROOT)){
                queryBuilder.append("MATCH (specialNode:specialNodes {name:'DummyRoot'})"); //NOI18N
                queryBuilder.append("<-[:").append(RelTypes.CHILD_OF).append("]-"); //NOI18N
            } else{
                queryBuilder.append("MATCH (class:classes {name:$objectClass})"); //NOI18N
                queryBuilder.append("<-[:").append(RelTypes.INSTANCE_OF).append("]-"); //NOI18N
            }
            queryBuilder.append("(object:inventoryObjects {_uuid:$objectId})"); //NOI18N
            queryBuilder.append("<-[r:").append(RelTypes.CHILD_OF_SPECIAL).append("]-(specialInstance)"); //NOI18N
            queryBuilder.append("-[:").append(RelTypes.INSTANCE_OF).append("]->(child_class:classes) "); //NOI18N
            queryBuilder.append("WHERE child_class.name IN [").append(classes).append("] "); //NOI18N
            queryBuilder.append("AND (NOT EXISTS(r.name) OR (EXISTS (r.name) AND (r.name='").append(Constants.REL_PROPERTY_POOL).append("')))"); //NOI18N
            queryBuilder.append(" RETURN ").append(CHILD_NODE).append(" ORDER BY specialInstance.name ASC"); //NOI18N
            
            //pagination
            if(page >= 0){
                parameters.put("skip", page); //NOI18N
                queryBuilder.append(" SKIP $skip "); //NOI18N
            }
            if(limit >= 0){
                parameters.put("limit", limit); //NOI18N
                queryBuilder.append(" LIMIT $limit"); //NOI18N
            }

            parameters.put("objectId", objectId); //NOI18N
            parameters.put("objectClass", objectClass); //NOI18N
            
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            List<BusinessObjectLight> instanceInPoolChildren = new ArrayList();
            while (result.hasNext())
                instanceInPoolChildren.add(ogmService.createObjectLightFromNode((Node) result.next().get(CHILD_NODE)));
            tx.success();
            return instanceInPoolChildren;
        }
    }
    
    @Override
    public long getObjectSpecialChildrenCount(String objectClass, String objectId, String... childrenClassNamesToFilter)
            throws BusinessObjectNotFoundException, InvalidArgumentException  {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (objectId == null)
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent-pool.id.non-null"));
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            final String COUNT = "count"; //NOI18N
            
            //We create the class names to filter
            for (int i = 0; i < childrenClassNamesToFilter.length; i++) 
                childrenClassNamesToFilter[i] = "'".concat(childrenClassNamesToFilter[i]).concat("'");
        
            String classes = String.join(" ,", childrenClassNamesToFilter);
            
                        
            if (objectClass == null || objectClass.equals(Constants.NODE_DUMMYROOT)){
                queryBuilder.append("MATCH (specialNode:specialNodes {name: 'DummyRoot'})"); //NOI18N
                queryBuilder.append("<-[:").append(RelTypes.CHILD_OF).append("]-"); //NOI18N
            }                          
            else{
                queryBuilder.append("MATCH (class:classes {name:$objectClass})"); //NOI18N
                queryBuilder.append("<-[:").append(RelTypes.INSTANCE_OF).append("]-"); //NOI18N
            }
            
            queryBuilder.append("(object:inventoryObjects {_uuid:$objectId})"); //NOI18N
            queryBuilder.append("<-[r:").append(RelTypes.CHILD_OF_SPECIAL).append("]-(specialInstance)"); //NOI18N
            queryBuilder.append("-[:").append(RelTypes.INSTANCE_OF).append("]->(child_class:classes) "); //NOI18N
            queryBuilder.append("WHERE child_class.name IN [").append(classes).append("] "); //NOI18N
            queryBuilder.append("AND (NOT EXISTS (r.name) OR EXISTS (r.name) AND (r.name='").append(Constants.REL_PROPERTY_POOL).append("'))"); //NOI18N
            queryBuilder.append(" RETURN COUNT(specialInstance) AS ").append(COUNT); //NOI18N
                
            parameters.put("objectId", objectId); //NOI18N
            parameters.put("objectClass", objectClass); //NOI18N
            
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (result.hasNext()){
                tx.success();
                return (long) result.next().get(COUNT);
            }
            tx.success();
            return 0;
        }
    }

    @Override
    public boolean hasRelationship(String objectClass, String objectId, String relationshipName, int numberOfRelationships)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node object = getInstanceOfClass(objectClass, objectId);
            int relationshipsCounter = 0;
            for (Relationship rel : object.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                if (rel.getProperty(Constants.PROPERTY_NAME).equals(relationshipName))
                    relationshipsCounter++;
                if (relationshipsCounter == numberOfRelationships)
                    return true;
            }
            return false;
        }
    }
    
    @Override
    public boolean hasSpecialAttribute(String objectClass, String objectId, String attributeName) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        if (objectId == null)
            throw new InvalidArgumentException("The object id cannot be null");
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("className", objectClass); //NOI18N
            params.put("objectId", objectId); //NOI18N
            params.put("attributeName", attributeName); //NOI18N
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (class:classes {name: $className})<-[:INSTANCE_OF]-(object:inventoryObjects {_uuid: $objectId})-[:RELATED_TO_SPECIAL {name: $attributeName}]-() "); //NOI18N
            queryBuilder.append("RETURN count(*) > 0 AS hasSpecialAtribute"); //NOI18N
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), params);
            while (result.hasNext()) {
                tx.success();
                return (Boolean) result.next().get("hasSpecialAtribute"); //NOI18N
            }
            tx.success();
            throw new BusinessObjectNotFoundException(objectClass, objectId);
        }
    }
    
    @Override
    public long countChildren(String objectClass, String objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        if (objectId == null)
            throw new InvalidArgumentException("The object id cannot be null");
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("className", objectClass); //NOI18N
            params.put("objectId", objectId); //NOI18N
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (class:classes {name: $className})<-[:INSTANCE_OF]-(object:inventoryObjects {_uuid: $objectId})<-[:CHILD_OF]-() "); //NOI18N
            queryBuilder.append("RETURN count(*) AS children"); //NOI18N
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), params);
            while (result.hasNext()) {
                tx.success();
                return (long) result.next().get("children"); //NOI18N
            }
            tx.success();
            throw new BusinessObjectNotFoundException(objectClass, objectId);
        }
    }
    
    @Override
    public long countSpecialChildren(String objectClass, String objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        if (objectId == null)
            throw new InvalidArgumentException("The object id cannot be null");
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("className", objectClass); //NOI18N
            params.put("objectId", objectId); //NOI18N
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (class:classes {name:$className})<-[:INSTANCE_OF]-(object:inventoryObjects {_uuid:$objectId})<-[:CHILD_OF_SPECIAL]-() "); //NOI18N
            queryBuilder.append("RETURN count(*) AS specialChildren"); //NOI18N
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), params);
            while (result.hasNext()) {
                tx.success();
                return (long) result.next().get("specialChildren"); //NOI18N
            }
            tx.success();
            throw new BusinessObjectNotFoundException(objectClass, objectId);
        }
    }
    
    @Override
    public void releaseRelationships(String objectClass, String objectId, List<String> relationshipsToRelease) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node object = getInstanceOfClass(objectClass, objectId);
            
            for (Relationship rel : object.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                if (relationshipsToRelease.contains((String)rel.getProperty(Constants.PROPERTY_NAME)))
                    rel.delete();
            }
            tx.success();
        }
    }
    
    @Override
    public boolean hasSpecialRelationship(String objectClass, String objectId, String relationshipName, int numberOfRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException  {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node object = getInstanceOfClass(objectClass, objectId);
            int relationshipsCounter = 0;
            for (Relationship rel : object.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                if (rel.getProperty(Constants.PROPERTY_NAME).equals(relationshipName))
                    relationshipsCounter++;
                if (relationshipsCounter == numberOfRelationships)
                    return true;
            }
        }
        return false;
            
    }
    
    //<editor-fold defaultstate="collapsed" desc="Contact Manager">
    @Override
    public String createContact(String contactClass, String customerClassName, String customerId, String userName) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCUSTOMER, customerClassName))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.messages.wrong-subclass")
                    , customerClassName, Constants.CLASS_GENERICCUSTOMER));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node contactClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, contactClass);
            if (contactClassNode == null)
                throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.class-not-found"), contactClass));
            
            Node customerNode = getInstanceOfClass(customerClassName, customerId);
            Node newContactNode = connectionManager.getConnectionHandler().createNode(contactsLabel, inventoryObjectLabel);
            newContactNode.setProperty(Constants.PROPERTY_UUID, UUID.randomUUID().toString());          
            
            newContactNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            
            Relationship newContactRelationship = customerNode.createRelationshipTo(newContactNode, RelTypes.RELATED_TO_SPECIAL);
            newContactRelationship.setProperty(Constants.PROPERTY_NAME, Constants.LABEL_CONTACTS);
            
            newContactNode.createRelationshipTo(contactClassNode, RelTypes.INSTANCE_OF);
                        
            String newContactNodeUuid = newContactNode.hasProperty(Constants.PROPERTY_UUID) ? (String) newContactNode.getProperty(Constants.PROPERTY_UUID) : null;
            if (newContactNodeUuid == null)
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.object-not-have-uuid"), newContactNode.getId()));                
            
            tx.success();
            
            aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT,
                String.format(ts.getTranslatedString("module.contactman.actions.new-contact.success-activity"), newContactNodeUuid));
            
            return newContactNodeUuid;
        }
    }

    @Override
    public void updateContact(String contactClass, String contactId, List<StringPair> properties, String userName) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node contactNode = getInstanceOfClass(contactClass, contactId);
            
            for (StringPair property : properties) {
                if (property.getKey().equals(Constants.PROPERTY_CREATION_DATE))
                    throw new InvalidArgumentException(ts.getTranslatedString("module.general.messages.attribute-is-read-only"));
                
                contactNode.setProperty(property.getKey(), property.getValue());
            }
            
            tx.success();  
            
            aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT,
                String.format(ts.getTranslatedString("module.contactman.actions.update-contact.success-activity"), contactId));
        }
    }

    @Override
    public void deleteContact(String contactClass, String contactId, String userName) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node contactNode = getInstanceOfClass(contactClass, contactId);
            
            for (Relationship contactRelationship : contactNode.getRelationships())
                contactRelationship.delete();
            
            contactNode.delete();
            
            tx.success();

            aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT,
                String.format(ts.getTranslatedString("module.contactman.actions.delete-contact.success-activity"), contactId));
        }
    }

    @Override
    public Contact getContact(String contactClass, String contactId) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTACT, contactClass))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("apis.persistence.messages.wrong-subclass")
                    , contactClass, Constants.CLASS_GENERICCONTACT));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node contactNode = getInstanceOfClass(contactClass, contactId);
            
            for (Relationship customerRelationship : contactNode.getRelationships(RelTypes.RELATED_TO_SPECIAL, Direction.INCOMING)) {
                if (customerRelationship.hasProperty(Constants.PROPERTY_NAME) && customerRelationship.getProperty(Constants.PROPERTY_NAME).equals(Constants.LABEL_CONTACTS))
                    return new Contact(ogmService.createObjectFromNode(contactNode), ogmService.createObjectLightFromNode(customerRelationship.getStartNode()));
            }
            
            throw new InvalidArgumentException(ts.getTranslatedString("module.contactman.actions.contact-relationships.error-not-have-customer"));
        }
    }

    @Override
    public List<Contact> getContactsForCustomer(String customerClass, String customerId) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node customerNode = getInstanceOfClass(customerClass, customerId);
            List<Contact> contacts = new ArrayList<>();
            for (Relationship contactRelationship : customerNode.getRelationships(RelTypes.RELATED_TO_SPECIAL, Direction.OUTGOING)) {
                if (contactRelationship.hasProperty(Constants.PROPERTY_NAME) && contactRelationship.getProperty(Constants.PROPERTY_NAME).equals(Constants.LABEL_CONTACTS)) {
                    Node contactNode = contactRelationship.getEndNode();
                    contacts.add(new Contact(ogmService.createObjectFromNode(contactNode), ogmService.createObjectLightFromNode(customerNode)));
                }
            }
            return contacts;
        }
    }

    //TODO: Optimize and improve validations!
    @Override
    public List<Contact> searchForContacts(String searchString, int maxResults) throws InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String cypherQuery;
            if (searchString == null || searchString.trim().isEmpty()) //Return all contacts
                cypherQuery = "MATCH(n:contacts)  RETURN n AS contact ORDER BY n.name ASC" +
                        (maxResults > 0 ? (" LIMIT " + maxResults) : ""); //NOI18N
            else //Search the string in the contact and customer name
                cypherQuery = "MATCH (n:contacts)<-[r:" + RelTypes.RELATED_TO_SPECIAL + "]-(c) WHERE " +
                        "TOLOWER(n.name) contains TOLOWER({searchString}) OR TOLOWER(c.name) contains TOLOWER({searchString}) RETURN n AS contact ORDER BY c.name, n.name ASC" +
                        (maxResults > 0 ? (" LIMIT " + maxResults) : ""); //NOI18N
            
            List<Contact> res = new ArrayList<>();
            
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("searchString", searchString);  //NOI18N
            Result rawQueryResult = connectionManager.getConnectionHandler().execute(cypherQuery, parameters);
            ResourceIterator<Node> contactNodes = rawQueryResult.columnAs("contact");
            
            while (contactNodes.hasNext()) {
                Node contactNode = contactNodes.next();
                if (contactNode.hasRelationship(RelTypes.RELATED_TO_SPECIAL, Direction.INCOMING)) {
                    Node customerNode = contactNode.getRelationships(RelTypes.RELATED_TO_SPECIAL, Direction.INCOMING).iterator().next().getStartNode();
                    res.add(new Contact(ogmService.createObjectFromNode(contactNode), ogmService.createObjectLightFromNode(customerNode)));
                }
            }
            return res;
        }
    }
    
    @Override
    public List<Contact> getContacts(int page, int limit, HashMap<String, Object> filters) 
            throws InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> parameters = new HashMap<>();

            String customer;
            if (filters != null && filters.containsKey("customer")) {
                customer = "customer.name = ($customerName)";
                parameters.put("customerName", filters.get("customer"));
            } else
                customer = "true";
            
            String type;
            if (filters != null && filters.containsKey("type")) {
                type = "type.name = ($typeName)";     
                parameters.put("typeName", filters.get("type"));
            } else
                type = "true";
            
            String contactName;
            if (filters != null && filters.containsKey("contact_name")) {
                contactName = "toLower(contact.name) CONTAINS toLower($contactName)";
                parameters.put("contactName", filters.get("contact_name"));
            } else
                contactName = "true";
                
            String contactEmail1;
            if (filters != null && filters.containsKey("contact_email1")) {
                contactEmail1 = "toLower(contact.email1) CONTAINS toLower($contactEmail1)"; 
                parameters.put("contactEmail1", filters.get("contact_email1"));
            } else
                contactEmail1 = "true";
            
            String contactEmail2;
            if (filters != null && filters.containsKey("contact_email2")) {
                contactEmail2 = "toLower(contact.email2) CONTAINS toLower($contactEmail2)"; 
                parameters.put("contactEmail2", filters.get("contact_email2"));
            } else
                contactEmail2 = "true";
            
            String query = "MATCH (type)<-[io:" + RelTypes.INSTANCE_OF + "]-(contact:contacts)<-[rts:" + RelTypes.RELATED_TO_SPECIAL + "]-(customer)"
                    + ((filters == null || filters.isEmpty()) ? "" : 
                    " WHERE " + contactName + " AND " + contactEmail1 + " AND " + contactEmail2 + " AND " + customer + " AND " + type)
                    + " RETURN contact, customer, type"
                    + " ORDER BY contact.creationDate DESC"
                    + (page < 1 || limit < 1 ? "" : " SKIP " + page + " LIMIT " + limit);
            
            List<Contact> contacts = new ArrayList<>();
            Result result = connectionManager.getConnectionHandler().execute(query, parameters);
            ResourceIterator<Node> contactNodes = result.columnAs("contact");
            
            while (contactNodes.hasNext()) {
                Node contactNode = contactNodes.next();
                if (contactNode.hasRelationship(RelTypes.RELATED_TO_SPECIAL, Direction.INCOMING)) {
                    Node customerNode = contactNode.getRelationships(RelTypes.RELATED_TO_SPECIAL, Direction.INCOMING).iterator().next().getStartNode();
                    contacts.add(new Contact(ogmService.createObjectFromNode(contactNode), ogmService.createObjectLightFromNode(customerNode)));
                }
            }
            return contacts;
        }
    }
    
    @Override
    public void relateObjectToContact(String objectClass, String objectId, String contactClass, String contactId, String userName)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        if(!mem.isSubclassOf(Constants.CLASS_GENERICCONTACT, contactClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICCONTACT, contactClass)); 
             
        createSpecialRelationship(contactClass, contactId, objectClass, objectId, "hasContact", true);
        
        aem.createObjectActivityLogEntry(userName, objectClass, objectId, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME , "", "hasContact",
                String.format(ts.getTranslatedString("module.contactman.actions.relate.object-to-contact.related-log"), objectId, objectClass, contactId, contactClass));
    }
    
    @Override
    public void releaseObjectFromContact(String objectClass, String objectId, String contactClass, String contactId, String userName)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        if(!mem.isSubclassOf(Constants.CLASS_GENERICCONTACT, contactClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICCONTACT, contactClass)); 
             
        releaseSpecialRelationship(contactClass, contactId, objectId, "hasContact");
        
        aem.createObjectActivityLogEntry(userName, objectClass, objectId, ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME, "hasContact",
                "", String.format(ts.getTranslatedString("module.contactman.actions.relase.object-from-contact.released-log"), objectId, objectClass, contactId, contactClass));
    }
    
    @Override
    public List<BusinessObjectLight> getContactResources(String contactClass, String contactId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTACT, contactClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), contactClass));
        
        return getSpecialAttribute(contactClass, contactId, "hasContact");
    }
    // </editor-fold>
    
    @Override
    public long attachFileToObject(String name, String tags, byte[] file, String className, String objectId) 
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
            Node objectNode = getInstanceOfClass(className, objectId);
            
            Node fileObjectNode = connectionManager.getConnectionHandler().createNode(Label.label(Constants.LABEL_ATTACHMENTS));
            fileObjectNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            fileObjectNode.setProperty(Constants.PROPERTY_NAME, name);
            fileObjectNode.setProperty(Constants.PROPERTY_TAGS, tags == null ? "" : tags);
            
            Relationship hasAttachmentRelationship = objectNode.createRelationshipTo(fileObjectNode, RelTypes.HAS_ATTACHMENT);
            hasAttachmentRelationship.setProperty(Constants.PROPERTY_NAME, "attachments");
            
            String fileName = objectNode.getProperty(Constants.PROPERTY_UUID) + "_" + fileObjectNode.getId();
            Util.saveFile((String)configuration.get("attachmentsPath"), fileName, file);
            
            tx.success();
            return fileObjectNode.getId();
        } catch(IOException ex) {
            throw new OperationNotPermittedException(ex.getMessage());
        }
    }

    @Override
    public List<FileObjectLight> getFilesForObject(String className, String objectId) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);
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
            Node objectNode = getInstanceOfClass(className, objectId);

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
                        InvalidArgumentException nestedEx = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".30"), fileObjectId, ex.getMessage()));
                        nestedEx.setPrefix(KEY_PREFIX);
                        nestedEx.setCode(30);
                        nestedEx.setMessageArgs(fileObjectId, ex.getMessage());
                        throw nestedEx;
                    }
                }
            }
            InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".31"), fileObjectId));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(31);
            ex.setMessageArgs(fileObjectId);
            throw ex;
        }
    }

    @Override
    public void detachFileFromObject(long fileObjectId, String className, String objectId) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);

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
            Node objectNode = getInstanceOfClass(className, objectId);

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
    @Override   
    public Map<BusinessObjectLight, List<FileObjectLight>> getFilesFromRelatedListTypeItems(String uuid) {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String cypherQuery = "MATCH (object:" + inventoryObjectLabel + " {" + Constants.PROPERTY_UUID + ":'" + uuid + "'}) -[:" + (RelTypes.RELATED_TO.toString() + "]-> (listTypeItem) - " +
                                 " [:"+ RelTypes.HAS_ATTACHMENT + "] -> (file), (listTypeItem) - [:INSTANCE_OF]->(modelClass:classes)" +
                                 " RETURN DISTINCT listTypeItem._uuid as ltiId, listTypeItem.name as ltiName, modelClass.name as modelClassName, modelClass.displayName as modelClassDisplayName, file");    
                        
            Map<BusinessObjectLight, List<FileObjectLight>> attributeFiles = new HashMap<>();
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
                    
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node fileNode = (Node) row.get("file");
                String ltiId = (String) row.get("ltiId");
                String ltiName = (String) row.get("ltiName");
                String ltiClass = (String) row.get("modelClassName");
                String ltiClassDysplayName = (String) row.get("modelClassDisplayName");
                               
                BusinessObjectLight ltItemObject = new BusinessObjectLight(ltiClass, ltiId, ltiName, ltiClassDysplayName);
                if (!attributeFiles.containsKey(ltItemObject)) 
                    attributeFiles.put(ltItemObject, new ArrayList<>());
                attributeFiles.get(ltItemObject).add(new FileObjectLight(fileNode.getId(), (String)fileNode.getProperty(Constants.PROPERTY_NAME), 
                        (String)fileNode.getProperty(Constants.PROPERTY_TAGS), (long)fileNode.getProperty(Constants.PROPERTY_CREATION_DATE)));                     
            }
            tx.success();
            return attributeFiles;
        }     
    }
    
    @Override
    public List<BusinessObjectLightList> findRoutesThroughSpecialRelationships(String objectAClassName, 
            String objectAId, String objectBClassName, String objectBId, String relationshipName) throws InvalidArgumentException {
        List<BusinessObjectLightList> paths = new ArrayList<>();

        String cypherQuery = String.format("MATCH path = (a)-[:%s*1..30{name:\"%s\"}]-(b) " +
                            "WHERE a._uuid = '%s' AND b._uuid = '%s' " +
                            "RETURN nodes(path) as path LIMIT %s", RelTypes.RELATED_TO_SPECIAL, relationshipName, objectAId, objectBId, 
                                                                    aem.getConfiguration().get("maxRoutes")); //NOI18N
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
           
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<List<Node>> column = result.columnAs("path");
            
            //Filtering the routes with repeated nodes didn't work using a cypher query, so we do it here
            //at least while we figure out a working cypher query
            for (List<Node> list : Iterators.asIterable(column)) {
                BusinessObjectLightList aPath = new BusinessObjectLightList();
                boolean discardPath = false;
                for (Node aNode : list) {
                    BusinessObjectLight aHop = ogmService.createObjectLightFromNode(aNode);
                    if (aPath.getList().contains(aHop)) {
                        discardPath = true;
                        break;
                    } else
                        aPath.add(aHop);
                }
                if (!discardPath)
                    paths.add(aPath);
            }
        }
        
        //We implement the path length sorting here since the cypher query seems to be too expensive (?) if the sort is done there
        paths.sort((o1, o2) -> {
            return Integer.compare(o1.getList().size(), o2.getList().size());
        });
        
        return paths;
        
    }
    
    //<editor-fold desc="Warehouse" defaultstate="collapsed">
    @Override
    public List<BusinessObjectLight> getWarehousesInObject(String objectClassName, String objectId) 
        throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, objectClassName);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(objectClassName);
            
            List<BusinessObjectLight> warehouses = new ArrayList();
                                    
            String cypherQuery = ""
                + "MATCH (warehouse:inventoryObjects)-[:RELATED_TO_SPECIAL{ name: 'warehouseHas' }]-(child:inventoryObjects)-[:CHILD_OF*]->(parent:inventoryObjects)-[:INSTANCE_OF]->(class:classes{name: '" + objectClassName + "'}) "
                + "WHERE parent._uuid = '" + objectId + "' RETURN warehouse;";
            
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            ResourceIterator<Node> warehouseColumn = result.columnAs("warehouse");
            List<Node> lstWarehouseColumn = Iterators.asList(warehouseColumn);
            
            for (Node warehouse : lstWarehouseColumn)
                warehouses.add(ogmService.createObjectLightFromNode(warehouse));
            
            Collections.sort(warehouses);
            
            tx.success();
            return warehouses;
        }
    }
    @Override
    public BusinessObjectLight getWarehouseToObject(String objectClassName, String objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, objectClassName);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(objectClassName);
            
            getObjectLight(objectClassName, objectId);
            
            List<BusinessObjectLight> warehouses = new ArrayList();
                                                
            String cypherQuery = ""
                + "MATCH (class{name:'" + objectClassName + "'})<-[:INSTANCE_OF]-(inventoryObject)-[:CHILD_OF_SPECIAL{name: 'pool'}]->(pool)-[:CHILD_OF_SPECIAL{name: 'pool'}]->(warehouse) "
                + "WHERE inventoryObject._uuid = '" + objectId + "' "
                + "RETURN warehouse;";
            
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            ResourceIterator<Node> warehouseColumn = result.columnAs("warehouse");
            List<Node> lstWarehouseColumn = Iterators.asList(warehouseColumn);
            
            for (Node warehouse : lstWarehouseColumn)
                warehouses.add(ogmService.createObjectLightFromNode(warehouse));
            
            Collections.sort(warehouses);
            
            tx.success();
            if (warehouses.size() > 0)
                return warehouses.get(0);
            return null;
        }
    }
   
    @Override
    public BusinessObjectLight getPhysicalNodeToObjectInWarehouse(String objectClassName, String objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, objectClassName);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(objectClassName);
            
            getObjectLight(objectClassName, objectId);
            
            List<BusinessObjectLight> physicalNodes = new ArrayList();
                                    
            String cypherQuery = ""
                + "MATCH (class{name:'" + objectClassName + "'})<-[:INSTANCE_OF]-(inventoryObject)-[:CHILD_OF_SPECIAL{name: 'pool'}]->(pool)-[:CHILD_OF_SPECIAL{name: 'pool'}]->(warehouse)-[:RELATED_TO_SPECIAL{name: 'warehouseHas'}]->(physicalNode) "
                + "WHERE inventoryObject._uuid = '" + objectId + "' "
                + "RETURN physicalNode;";
            
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            ResourceIterator<Node> physicalNodeColumn = result.columnAs("physicalNode");
            List<Node> lstphysicalNodeColumn = Iterators.asList(physicalNodeColumn);
            
            for (Node physicalNode : lstphysicalNodeColumn)
                physicalNodes.add(ogmService.createObjectLightFromNode(physicalNode));
            
            Collections.sort(physicalNodes);
            
            tx.success();
            if (physicalNodes.size() > 0)
                return physicalNodes.get(0);
            return null;
        }
    }
    //</editor-fold>
        
    //<editor-fold desc="Reporting API implementation" defaultstate="collapsed">
        @Override
    public long createClassLevelReport(String className, String reportName, String reportDescription, 
            String script, int outputType, boolean enabled) throws MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            Node newReport = connectionManager.getConnectionHandler().createNode(reportsLabel);
            newReport.setProperty(Constants.PROPERTY_NAME, reportName == null ? "" : reportName);
            newReport.setProperty(Constants.PROPERTY_DESCRIPTION, reportDescription == null ? "" : reportDescription);
            newReport.setProperty(Constants.PROPERTY_SCRIPT, script == null ? "" : script);
            newReport.setProperty(Constants.PROPERTY_TYPE, outputType > 4 ? ReportMetadataLight.TYPE_HTML : outputType);
            newReport.setProperty(Constants.PROPERTY_ENABLED, enabled);
            
            classNode.createRelationshipTo(newReport, RelTypes.HAS_REPORT);
                        
            tx.success();
            return newReport.getId();
        }
    }

    @Override
    public long createInventoryLevelReport(String reportName, String reportDescription, String script, 
            int outputType, boolean enabled, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node dummyRootNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            if (dummyRootNode == null)
                throw new ApplicationObjectNotFoundException("Dummy Root could not be found");
            
            Node newReport = connectionManager.getConnectionHandler().createNode(reportsLabel);
            newReport.setProperty(Constants.PROPERTY_NAME, reportName == null ? "" : reportName);
            newReport.setProperty(Constants.PROPERTY_DESCRIPTION, reportDescription == null ? "" : reportDescription);
            newReport.setProperty(Constants.PROPERTY_SCRIPT, script == null ? "" : script);
            newReport.setProperty(Constants.PROPERTY_TYPE, Math.abs(outputType) > 4 ? ReportMetadataLight.TYPE_HTML : outputType);
            newReport.setProperty(Constants.PROPERTY_ENABLED, enabled);
            
            if (parameters != null) {
                for (StringPair parameter : parameters) {
                    if (parameter.getKey() == null || parameter.getKey().trim().isEmpty())
                        throw new InvalidArgumentException("Parameter names can not be empty strings");
                    
                    newReport.setProperty("PARAM_" + parameter.getKey(), 
                            parameter.getValue() == null ? "" : parameter.getValue());
                }
            }
            
            
            dummyRootNode.createRelationshipTo(newReport, RelTypes.HAS_REPORT);
                        
            tx.success();
            return newReport.getId();
        }
    }

    @Override
    public ChangeDescriptor deleteReport(long reportId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node reportNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId));
            
            for (Relationship rel : reportNode.getRelationships())
                rel.delete();
            
            String reportName = reportNode.hasProperty(Constants.PROPERTY_NAME) ? (String) reportNode.getProperty(Constants.PROPERTY_NAME) : "";
            
            reportNode.delete();
            
            tx.success();
            return new ChangeDescriptor("","","", String.format("Deleted report %s", reportName));
        }
    }

    @Override
    public ChangeDescriptor updateReport(long reportId, String reportName, String reportDescription, Boolean enabled,
            Integer type, String script) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId));
            String affectedProperties = "", oldValues = "", newValues = "";
            
            if (reportName != null) {
                affectedProperties += " " + Constants.PROPERTY_NAME;
                oldValues += " " + (reportNode.hasProperty(Constants.PROPERTY_NAME) ? reportNode.getProperty(Constants.PROPERTY_NAME) : "null");                
                newValues += " " + reportName;
                
                reportNode.setProperty(Constants.PROPERTY_NAME, reportName);
            }
            else
                reportName = reportNode.hasProperty(Constants.PROPERTY_NAME) ? (String) reportNode.getProperty(Constants.PROPERTY_NAME) : "";
            
            if (reportDescription != null) {
                affectedProperties += " " + Constants.PROPERTY_DESCRIPTION;
                oldValues += " " + (reportNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? reportNode.getProperty(Constants.PROPERTY_DESCRIPTION) : "null");
                newValues += " " + reportDescription;
                
                reportNode.setProperty(Constants.PROPERTY_DESCRIPTION, reportDescription);
            }
            if (enabled != null) {
                affectedProperties += " " + Constants.PROPERTY_ENABLED;
                oldValues += " " + (reportNode.hasProperty(Constants.PROPERTY_ENABLED) ? reportNode.getProperty(Constants.PROPERTY_ENABLED) : "null");
                newValues += " " + enabled;
                
                reportNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
            }
            if (type != null) {
                affectedProperties += " " + Constants.PROPERTY_TYPE;
                oldValues += " " + (reportNode.hasProperty(Constants.PROPERTY_TYPE) ? reportNode.getProperty(Constants.PROPERTY_TYPE) : "null");
                newValues += " " + type;
                
                reportNode.setProperty(Constants.PROPERTY_TYPE, type);
            }
            if (script != null) {
                affectedProperties += " " + Constants.PROPERTY_SCRIPT;
                oldValues += " " + (reportNode.hasProperty(Constants.PROPERTY_SCRIPT) ? reportNode.getProperty(Constants.PROPERTY_SCRIPT) : "null");
                newValues += " " + script;
                
                reportNode.setProperty(Constants.PROPERTY_SCRIPT, script);
            }

            tx.success();
            return new ChangeDescriptor(affectedProperties.trim(), oldValues.trim(), 
                newValues.trim(), String.format("Updated Report %s", reportName));
        }
    }
    
    @Override
    public ChangeDescriptor updateReportParameters(long reportId, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A report with id %s could not be found", reportId));
            String affectedProperties = "", oldValues = "", newValues = "";
            
            for (StringPair parameter : parameters) {
                
                if (parameter.getKey() == null || parameter.getKey().trim().isEmpty())
                        throw new InvalidArgumentException("Parameter names can not be empty strings");
                
                String actualParameterName = "PARAM_" + parameter.getKey();
                //The parameters are stored with a prefix PARAM_
                //params set to null, must be deleted
                if (reportNode.hasProperty(actualParameterName) && parameter.getValue() == null) {
                    affectedProperties += " " + parameter.getKey();
                    reportNode.removeProperty(actualParameterName);
                }
                else {
                    affectedProperties += " " + parameter.getKey();
                    oldValues += " " + (reportNode.hasProperty(actualParameterName) ? reportNode.getProperty(actualParameterName) : "null");
                    newValues += " " + parameter.getValue();
                    
                    reportNode.setProperty(actualParameterName, parameter.getValue());
                }
            }
            String reportName = reportNode.hasProperty(Constants.PROPERTY_NAME) ? (String) reportNode.getProperty(Constants.PROPERTY_NAME) : "";
            tx.success();
            return new ChangeDescriptor(affectedProperties.trim(), oldValues.trim(), 
                newValues.trim(), String.format("Updated %s report parameters", reportName));
        }
    }

    @Override
    public List<ReportMetadataLight> getClassLevelReports(String className, boolean recursive, boolean includeDisabled) throws MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node mainClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            List<ReportMetadataLight> remoteReports = new ArrayList<>();        
            
            if (mainClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            String cypherQuery;
            
            if (recursive)
                cypherQuery = "MATCH (theClass:classes)-[:EXTENDS*]->(aSuperClass) WHERE (aSuperClass.name = \"" + className + "\" OR theClass.name = \"" + className + "\")" +
                        " WITH Collect(theClass)+Collect(aSuperClass) as res " +
                        " UNWIND res as nodes" +
                        " MATCH (theReport:reports)<-[:HAS_REPORT]-(aClass) WHERE "
                        + "(aClass.name = nodes.name) " + (includeDisabled ? "" : " AND theReport.enabled = true") + 
                        " RETURN DISTINCT theReport ORDER BY theReport.name ASC";
            else
                cypherQuery = "MATCH(theReport:reports)<-[:HAS_REPORT]-(aClass) "
                    + "WHERE aClass.name=\"" + className + "\"" + (includeDisabled ? "" : " AND theReport.enabled = true") + " RETURN theReport ORDER BY theReport.name ASC ";
                
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            ResourceIterator<Node> reports = result.columnAs("theReport");
            List<Node> lstReports = Iterators.asList(reports);

            for (Node reportNode: lstReports) 
                    remoteReports.add(new ReportMetadataLight(reportNode.getId(), 
                                                        (String)reportNode.getProperty(Constants.PROPERTY_NAME), 
                                                        (String)reportNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                                                        (boolean)reportNode.getProperty(Constants.PROPERTY_ENABLED),
                                                        (int)reportNode.getProperty(Constants.PROPERTY_TYPE))); 
            
            return remoteReports;
        }
    }

    @Override
    public List<ReportMetadataLight> getInventoryLevelReports(boolean includeDisabled) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node dummyRootNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            if (dummyRootNode == null)
                throw new ApplicationObjectNotFoundException("Dummy Root could not be found");
            
            List<ReportMetadataLight> remoteReports = new ArrayList<>();
            
            for (Relationship hasReportRelationship : dummyRootNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_REPORT)) {
                if (includeDisabled || (boolean)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_ENABLED))
                    remoteReports.add(new ReportMetadataLight(hasReportRelationship.getEndNode().getId(), 
                                                        (String)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME), 
                                                        (String)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_DESCRIPTION), 
                                                        (boolean)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_ENABLED),
                                                        (int)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_TYPE)));
            }
            
            Collections.sort(remoteReports);
            
            return remoteReports;
        }
    }

    @Override
    public ReportMetadata getReport(long reportId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId)); 
            
            List<StringPair> parameters = new ArrayList<>();
            for (String property : reportNode.getPropertyKeys()) {
                if (property.startsWith("PARAM_"))
                    parameters.add(new StringPair(property.replace("PARAM_", ""), (String)reportNode.getProperty(property)));
            }
                
            return new ReportMetadata(reportNode.getId(), (String)reportNode.getProperty(Constants.PROPERTY_NAME), 
                                    (String)reportNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                                    (boolean)reportNode.getProperty(Constants.PROPERTY_ENABLED),
                                    (int)reportNode.getProperty(Constants.PROPERTY_TYPE),
                                    (String)reportNode.getProperty(Constants.PROPERTY_SCRIPT), 
                                    parameters);
        }
    }

    @Override
    public byte[] executeClassLevelReport(String objectClassName, String objectId, long reportId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId)); 
            
            Node instanceNode = getInstanceOfClass(objectClassName, objectId);
            
            String script = (String)reportNode.getProperty(Constants.PROPERTY_SCRIPT);
            
            Binding environmentParameters = new Binding();
            environmentParameters.setVariable("instanceNode", instanceNode); //NOI18N
            environmentParameters.setVariable("connectionHandler", connectionManager.getConnectionHandler()); //NOI18N
            environmentParameters.setVariable("mem", mem); //NOI18N
            environmentParameters.setVariable("aem", aem); //NOI18N
            environmentParameters.setVariable("bem", this); //NOI18N
            
            //To keep backwards compatibility
            environmentParameters.setVariable("objectClassName", objectClassName); //NOI18N
            environmentParameters.setVariable("objectId", objectId); //NOI18N
            
            try {
                GroovyShell shell = new GroovyShell(BusinessEntityManager.class.getClassLoader(), environmentParameters);
                Object theResult = shell.evaluate(script);
                
                if (theResult == null)
                    throw new InvalidArgumentException("The script returned a null object. Please check the syntax.");
                else {
                    if (theResult instanceof InventoryReport)
                        return ((InventoryReport)theResult).asByteArray();
                    else
                        throw new InvalidArgumentException("The script does not return an InventoryReport object. Please check the return value.");
                }
            } catch(Exception ex) {
                return ("<html><head><title>Error</title></head><body><center>" + ex.getMessage() + "</center></body></html>").getBytes(StandardCharsets.UTF_8);
            }
        }
    }

    @Override
    public byte[] executeInventoryLevelReport(long reportId, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Report with id %s could not be found", reportId)); 
                     
            String script = (String)reportNode.getProperty(Constants.PROPERTY_SCRIPT);
            
            HashMap<String, String> scriptParameters = new HashMap<>();
            for(StringPair parameter : parameters)
                scriptParameters.put(parameter.getKey(), parameter.getValue());
            
            Binding environmentParameters = new Binding();
            environmentParameters.setVariable("parameters", scriptParameters); //NOI18N
            environmentParameters.setVariable("connectionHandler", connectionManager.getConnectionHandler()); //NOI18N
            environmentParameters.setVariable("inventoryObjectLabel", inventoryObjectLabel); //NOI18N
            environmentParameters.setVariable("classLabel", classLabel); //NOI18N
            environmentParameters.setVariable("mem", mem); //NOI18N
            environmentParameters.setVariable("aem", aem); //NOI18N
            environmentParameters.setVariable("bem", this); //NOI18N
            
            try {
                GroovyShell shell = new GroovyShell(BusinessEntityManager.class.getClassLoader(), environmentParameters);
                Object theResult = shell.evaluate(script);
                
                if (theResult == null)
                    throw new InvalidArgumentException("The script returned a null object. Please check the syntax.");
                else {
                    if (theResult instanceof InventoryReport)
                        return ((InventoryReport)theResult).asByteArray();
                    else
                        throw new InvalidArgumentException("The script does not return an InventoryReport instance. Please check the return value.");
                }
            } catch(Exception ex) {
                return ("<html><head><title>Error</title></head><body><center>" + ex.getMessage() + "</center></body></html>").getBytes(StandardCharsets.UTF_8);
            }
        }
    }
    //</editor-fold>
    
    //<editor-fold desc="Pools" defaultstate="collapsed">
    @Override
    public List<InventoryObjectPool> getRootPools(String className, int type, boolean includeSubclasses) throws InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<InventoryObjectPool> pools  = new ArrayList<>();
            ResourceIterator<Node> poolNodes = connectionManager.getConnectionHandler().findNodes(poolLabel);
            while (poolNodes.hasNext()) {
                Node poolNode = poolNodes.next();
                if (!poolNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF_SPECIAL)) { //Root pools don't have parents
                    if (Integer.valueOf(String.valueOf(poolNode.getProperty(Constants.PROPERTY_TYPE))) == type) {
                        // The following conditions could probably normalized, but I think this way,
                        // the code is a bit more readable
                        if (className != null) { //We will return only those matching with the specified class name or its subclasses, depending on the value of includeSubclasses
                            String poolClass = (String)poolNode.getProperty(Constants.PROPERTY_CLASSNAME);
                            if (includeSubclasses) {
                                try {
                                    if (mem.isSubclassOf(className, poolClass))
                                        pools.add(Util.createPoolFromNode(poolNode, ts));
                                } catch (MetadataObjectNotFoundException ex) { } //Should not happen
                            } else {
                                if (className.equals(poolClass))
                                    pools.add(Util.createPoolFromNode(poolNode, ts));
                            }
                        } else //All pools with no parent are returned
                            pools.add(Util.createPoolFromNode(poolNode, ts));
                    }
                }
            }
            //tx.success();
            return pools;
        }
    }
    
    @Override
    public List<InventoryObjectPool> getPoolsInObject(String objectClassName, String objectId, String poolClass) throws 
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
                    if (poolClass != null) { // We will return only those matching with the specified class name or any of its subclasses
                        if (mem.isSubclassOf(poolClass, (String)poolNode.getProperty(Constants.PROPERTY_CLASSNAME)))
                            pools.add(Util.createPoolFromNode(poolNode, ts));
                    } else
                        pools.add(Util.createPoolFromNode(poolNode, ts));
                }
            }
            tx.success();
            return pools;
        }
    }
    
    @Override
    public List<InventoryObjectPool> getPoolsInPool(String parentPoolId, String poolClassName) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException 
    {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (poolClassName == null)
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent-pool.classname.non-null"));
           
            if (parentPoolId == null)
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent-pool.id.non-null"));
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            final String CHILD_NODE = "childNode"; //NOI18N
            
            queryBuilder.append("MATCH (pool:pools {_uuid:$uuid})"); //NOI18N
            queryBuilder.append("<-[").append(RelTypes.CHILD_OF_SPECIAL).append("]-(childPool)\n");
            queryBuilder.append("WHERE (EXISTS (childPool.className) AND (childPool.className=$classN))\n"); //NOI18N
            queryBuilder.append("RETURN childPool AS ").append(CHILD_NODE).append(" ORDER BY childPool.name ASC "); //NOI18N
           
            parameters.put("uuid", parentPoolId); //NOI18N
            parameters.put("className", poolClassName); //NOI18N
            parameters.put("classN", poolClassName); //NOI18N
            
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            List<InventoryObjectPool> pools = new ArrayList();
            while (result.hasNext())
                pools.add(Util.createPoolFromNode((Node) result.next().get(CHILD_NODE), ts));
            tx.success();
            return pools;
        }
    }
    
    @Override
    public long getPoolsInPoolCount(String parentPoolId, String poolClassName) 
        throws ApplicationObjectNotFoundException, InvalidArgumentException 
    {
        long f = 0;
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (poolClassName == null)
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent-pool.classname.non-null"));
           
            if (parentPoolId == null)
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent-pool.id.non-null"));
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            final String COUNT = "x"; //NOI18N
            
            queryBuilder.append("MATCH (pool:pools {_uuid:$uuid})"); //NOI18N
            queryBuilder.append("<-[").append(RelTypes.CHILD_OF_SPECIAL).append("]-(childPool)\n");
            queryBuilder.append("WHERE (EXISTS (childPool.className) AND (childPool.className=$classN))\n"); //NOI18N
            queryBuilder.append("RETURN COUNT(childPool) AS ").append(COUNT); //NOI18N

            parameters.put("uuid", parentPoolId); //NOI18N
            parameters.put("className", poolClassName); //NOI18N
            parameters.put("classN", poolClassName); //NOI18N
            
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            
            while (result.hasNext()){
                f = (long) result.next().get(COUNT);
            }
            tx.success();
        }
        return f;
    }
           
    @Override
    public InventoryObjectPool getPool(String poolId) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node poolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, poolId);
            
            if (poolNode != null) {                
                String name = poolNode.hasProperty(Constants.PROPERTY_NAME) ? 
                                    (String)poolNode.getProperty(Constants.PROPERTY_NAME) : null;
                
                String description = poolNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? 
                                    (String)poolNode.getProperty(Constants.PROPERTY_DESCRIPTION) : null;
                
                String className = poolNode.hasProperty(Constants.PROPERTY_CLASSNAME) ? 
                                    (String)poolNode.getProperty(Constants.PROPERTY_CLASSNAME) : null;
                
                int type = poolNode.hasProperty(Constants.PROPERTY_TYPE) ? 
                                    (int)poolNode.getProperty(Constants.PROPERTY_TYPE) : 0;
                
                return new InventoryObjectPool(poolId, name, description, className, type);
            }
            else
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s could not be found", poolId));
        }
    }
    
    @Override
    public List<BusinessObjectLight> getPoolItems(String poolId, int limit)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()){
            if (poolId == null)
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent-pool.id.non-null"));
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            final String CHILD_NODE = "childNode"; //NOI18N
            
            queryBuilder.append("MATCH (pool:pools {_uuid:$_uuid})"); //NOI18N
            queryBuilder.append("<-[r:").append(RelTypes.CHILD_OF_SPECIAL).append("]-"); //NOI18N
            queryBuilder.append("(childNode)\n"); //NOI18N
            queryBuilder.append("WHERE EXISTS(r.name) AND r.name='").append(Constants.REL_PROPERTY_POOL).append("' "); //NOI18N
            queryBuilder.append("AND NOT childNode:").append(poolLabel).append(" "); //NOI18N
            
            queryBuilder.append(String.format("RETURN childNode AS %s ORDER BY childNode.name ASC \n", CHILD_NODE)); //NOI18N
            if(limit > 0){
                parameters.put("limit", limit); //NOI18N
                queryBuilder.append(" LIMIT $limit"); //NOI18N
            }
            parameters.put("_uuid", poolId); //NOI18N
            
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            List<BusinessObjectLight> objectChildren = new ArrayList();
            while (result.hasNext())
                objectChildren.add(ogmService.createObjectLightFromNode((Node) result.next().get(CHILD_NODE)));
            tx.success();
            return objectChildren;
        }
       
    }
    
    @Override
    public List<BusinessObjectLight> getPoolItemsByClassName(String poolId, String className, int page, int limit)
            throws ApplicationObjectNotFoundException, InvalidArgumentException 
    {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()){
            if (poolId == null)
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent-pool.id.non-null"));
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            final String CHILD_NODE = "childNode"; //NOI18N
            
            queryBuilder.append("MATCH (pool:pools {_uuid:$_uuid})"); //NOI18N
            queryBuilder.append("<-[r:").append(RelTypes.CHILD_OF_SPECIAL).append("]-"); //NOI18N
            queryBuilder.append("(childNode)\n"); //NOI18N
            if(className != null){
                queryBuilder.append("-[:INSTANCE_OF]->(c:").append(classLabel).append(" {name:$className}) "); //NOI18N
                parameters.put("className", className);
            }
            queryBuilder.append("WHERE EXISTS(r.name) AND r.name='").append(Constants.REL_PROPERTY_POOL).append("' "); //NOI18N
            queryBuilder.append("AND NOT childNode:").append(poolLabel).append(" "); //NOI18N
            
            queryBuilder.append(String.format("RETURN childNode AS %s ORDER BY childNode.name ASC \n", CHILD_NODE)); //NOI18N
            //pagination
            if(page >= 0){
                parameters.put("skip", page); //NOI18N
                queryBuilder.append(" SKIP $skip "); //NOI18N
            }
            if(limit > 0){
                parameters.put("limit", limit); //NOI18N
                queryBuilder.append(" LIMIT $limit"); //NOI18N
            }
            parameters.put("_uuid", poolId); //NOI18N
            
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            List<BusinessObjectLight> objectChildren = new ArrayList();
            while (result.hasNext())
                objectChildren.add(ogmService.createObjectLightFromNode((Node) result.next().get(CHILD_NODE)));
            tx.success();
            return objectChildren;
        }
    }
    
    @Override
    public long getPoolItemsCount(String poolId, String className)
            throws ApplicationObjectNotFoundException, InvalidArgumentException 
    {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()){
            if (poolId == null)
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent-pool.id.non-null"));
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            final String COUNT = "count"; //NOI18N
            
            queryBuilder.append("MATCH (pool:pools {_uuid:$_uuid})"); //NOI18N
            queryBuilder.append("<-[r:").append(RelTypes.CHILD_OF_SPECIAL).append("]-"); //NOI18N
            queryBuilder.append("(childNode)\n"); //NOI18N
            if(className != null){
                queryBuilder.append("-[:INSTANCE_OF]->(c:").append(classLabel).append(" {name:$className}) "); //NOI18N
                parameters.put("className", className);
            }
            queryBuilder.append("WHERE EXISTS(r.name) AND r.name='").append(Constants.REL_PROPERTY_POOL).append("' "); //NOI18N
            queryBuilder.append("AND NOT childNode:").append(poolLabel).append(" "); //NOI18N
            queryBuilder.append("RETURN COUNT(childNode) AS ").append(COUNT); //NOI18N
            
            parameters.put("_uuid", poolId); //NOI18N
            
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (result.hasNext())
                 return (long) result.next().get(COUNT);
            tx.success();
            return 0;
        }
    }
    //</editor-fold>
    
    //<editor-fold desc="Helpers" defaultstate="collapsed">
    /**
     * Boiler-plate code. Gets a particular instance given the class name and the oid. Callers must handle associated transactions
     * @param className object class name
     * @param oid object id
     * @return a Node representing the entity
     * @throws MetadataObjectNotFoundException id the class could not be found
     * @throws BusinessObjectNotFoundException if the object could not be found
     * @throws InvalidArgumentException If the object id is null
     */
    public Node getInstanceOfClass(String className, String oid) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        if (oid == null) {
            InvalidArgumentException ex = new InvalidArgumentException(ts.getTranslatedString(KEY_PREFIX + ".32"));
            ex.setPrefix(KEY_PREFIX);
            ex.setCode(32);
            throw ex;
        }
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("className", className); //NOI18N
            params.put("objectId", oid); //NOI18N
            StringBuilder queryBuilder = new StringBuilder();
            if (className == null || className.equals(Constants.NODE_DUMMYROOT))
                queryBuilder.append("MATCH (specialNode:specialNodes {name: 'DummyRoot'})<-[:CHILD_OF]-(object:inventoryObjects {_uuid: $objectId}) RETURN object"); //NOI18N
            queryBuilder.append("MATCH (class:classes {name: $className})<-[:INSTANCE_OF]-(object:inventoryObjects{_uuid: $objectId}) RETURN object"); //NOI18N
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), params);
            while (result.hasNext()) {
                tx.success();
                return (Node) result.next().get("object"); //NOI18N
            }
            tx.success();
            throw new BusinessObjectNotFoundException(className, oid);
        }
    }
    /**
     * Boiler-plate code. Gets a particular instance given the class name and the oid
     * @param classId object class id
     * @param oid object id
     * @return a Node representing the entity
     * @throws MetadataObjectNotFoundException if the class could not be found
     * @throws BusinessObjectNotFoundException
     */
    public Node getInstanceOfClass(long classId, String oid) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException{
                
        //if any of the parameters is null, return the dummy root
        if (classId == -1)
            return connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
        
        Node classNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, classId);
        
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class with id %s could not be found", classId));

        Iterable<Relationship> iteratorInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iteratorInstances.iterator();
        while (instances.hasNext()) {
            Node otherSide = instances.next().getStartNode();
            
            String otherSideUuid = otherSide.hasProperty(Constants.PROPERTY_UUID) ? (String) otherSide.getProperty(Constants.PROPERTY_UUID) : null;
            if (otherSideUuid != null && otherSideUuid.equals(oid))
                return otherSide;
        }
        throw new BusinessObjectNotFoundException((String)classNode.getProperty(Constants.PROPERTY_NAME), oid);
    }

    public Node getInstanceOfClass(Node classNode, String oid) throws BusinessObjectNotFoundException{
        Iterable<Relationship> iterableInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iterableInstances.iterator();
        
        while (instances.hasNext()) {
            Node otherSide = instances.next().getStartNode();
            
            String otherSideUuid = otherSide.hasProperty(Constants.PROPERTY_UUID) ? (String) otherSide.getProperty(Constants.PROPERTY_UUID) : null;
            if (otherSideUuid != null && otherSideUuid.equals(oid))
                return otherSide;
        }
        throw new BusinessObjectNotFoundException((String)classNode.getProperty(Constants.PROPERTY_NAME), oid);
    }
    
    public Node createObject(Node classNode, ClassMetadata classToMap, HashMap<String,String> attributes) 
            throws InvalidArgumentException, MetadataObjectNotFoundException {
 
        if (classToMap.isAbstract())
            throw new InvalidArgumentException(String.format("Can not create objects from abstract classes (%s)", classToMap.getName()));
        
        if (classToMap.isInDesign())
            throw new InvalidArgumentException(String.format("Can not create objects from classes marked as inDesign (%s)", classToMap.getName()));
        
        Node newObject = connectionManager.getConnectionHandler().createNode(inventoryObjectLabel);
                
        String uuid = UUID.randomUUID().toString();
        newObject.setProperty(Constants.PROPERTY_UUID, uuid);
        
        newObject.setProperty(Constants.PROPERTY_NAME, ""); //The default value is an empty string 

        newObject.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis()); //The default value is right now
        
        if (attributes == null)
            attributes = new HashMap<>();
        
        for (AttributeMetadata attributeMetadata : classToMap.getAttributes()) {
            if (attributeMetadata.isMandatory() && attributes.get(attributeMetadata.getName()) == null)
                throw new InvalidArgumentException(String.format("The attribute %s is mandatory, it can not be empty", attributes.get(attributeMetadata.getName())));
            
            if (attributes.get(attributeMetadata.getName()) == null) //If the attribute is not included in the initial set of attributes to be set, we skip any further action
                continue;
            
            String attributeName = attributeMetadata.getName();
            String attributeType = classToMap.getType(attributeName);

            if (AttributeMetadata.isPrimitive(attributeType)) {
                if (classToMap.isUnique(attributeName)) {
                    //if an attribute is unique and mandatory it should be checked before the object creation, here
                    if (classToMap.getType(attributeName).equals("String") || //NOI18N
                        classToMap.getType(attributeName).equals("Integer") ||  //NOI18N
                        classToMap.getType(attributeName).equals("Float") ||  //NOI18N
                        classToMap.getType(attributeName).equals("Long")) { //NOI18N
                        if (ogmService.isObjectAttributeUnique(classToMap.getName(), attributeName, attributes.get(attributeName)))
                            newObject.setProperty(attributeName, Util.getRealValue(attributes.get(attributeName), classToMap.getType(attributeName), ts));
                        else
                            throw new InvalidArgumentException(String.format("The attribute %s is unique, but the value provided is already in use", attributeName));
                    }
                }
                else
                    newObject.setProperty(attributeName, Util.getRealValue(attributes.get(attributeName), classToMap.getType(attributeName), ts));
            }
            else {
                //If it's not a primitive type, maybe it must be a a list type
                try {
                    
                    if (!mem.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, attributeType))
                        throw new InvalidArgumentException(String.format("Type %s is not a primitive nor a list type", attributeName));

                    List<String> listTypeItemIds = new ArrayList<>();
                    for (String listTypeItemIdAsString : attributes.get(attributeName).split(";")) //If the attribute is multiple, the ids will be separated by ";", otherwise, it will be a single long value
                        listTypeItemIds.add(listTypeItemIdAsString);

                    Node listTypeClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, attributeType);

                    if (listTypeClassNode == null)
                        throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", attributeType));
                    
                    List<Node> listTypeItemNodes = Util.getListTypeItemNodes(listTypeClassNode, listTypeItemIds);

                    if (!listTypeItemNodes.isEmpty()) {
                        //Create the new relationships
                        for (Node listTypeItemNode : listTypeItemNodes) {
                            Relationship newRelationship = newObject.createRelationshipTo(listTypeItemNode, RelTypes.RELATED_TO);
                            newRelationship.setProperty(Constants.PROPERTY_NAME, attributeName);
                        }

                    } else if (attributeMetadata.isMandatory())
                        throw new InvalidArgumentException(String.format("The attribute %s is mandatory, can not be set to null", attributeName));

                } catch (NumberFormatException ex) {
                    throw new InvalidArgumentException(String.format("The value %s is not a valid list type item id", attributes.get(attributeName)));
                }
            }
        }
        
        newObject.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);
        
        return newObject;       
    }
        
    /**
     * Spawns [recursively] an inventory object from a template element
     * @param templateObject The template object used to create the inventory object
     * @param recursive Should the spawn operation be recursive?
     * @return The root copied object
     */
    private Node copyTemplateElement(Node templateObject, ClassMetadata classToMap, boolean recursive) throws InvalidArgumentException {
        
        Node newInstance = connectionManager.getConnectionHandler().createNode(inventoryObjectLabel);
       
        for (String property : templateObject.getPropertyKeys()) {
            if (!property.equals(Constants.PROPERTY_UUID)) {
                if (classToMap.isMandatory(property) && ((String)templateObject.getProperty(property)).isEmpty()) {
                    InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(KEY_PREFIX + ".33"), property));
                    ex.setPrefix(KEY_PREFIX);
                    ex.setCode(33);
                    ex.setMessageArgs(property);
                    throw ex;
                }

                newInstance.setProperty(property, templateObject.getProperty(property));
            }
        }
        
        for (Relationship rel : templateObject.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING))
            newInstance.createRelationshipTo(rel.getEndNode(), RelTypes.RELATED_TO).setProperty(Constants.PROPERTY_NAME, rel.getProperty(Constants.PROPERTY_NAME));
        
        newInstance.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
        newInstance.createRelationshipTo(templateObject.getRelationships(RelTypes.INSTANCE_OF_SPECIAL).iterator().next().getEndNode(), 
                RelTypes.INSTANCE_OF);

        if (recursive) {
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING)) {
                Node classNode = rel.getStartNode().getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                Node newChild = copyTemplateElement(rel.getStartNode(), Util.createClassMetadataFromNode(classNode), true);
                newChild.createRelationshipTo(newInstance, RelTypes.CHILD_OF);
            }
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING)) {
                Node classNode = rel.getStartNode().getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                Node newChild = copyTemplateElement(rel.getStartNode(), Util.createClassMetadataFromNode(classNode), true);
                newChild.createRelationshipTo(newInstance, RelTypes.CHILD_OF_SPECIAL);
            }
        }

        newInstance.setProperty(Constants.PROPERTY_UUID, UUID.randomUUID().toString());
        return newInstance;
    }
    
    /**
     * Equal to {@link BusinessEntityManagerImpl#copyTemplateElement(org.neo4j.graphdb.Node, org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata, boolean) }
     * but add the in/out parameter templateIds
     * Spawns [recursively] an inventory object from a template element
     * @param templateObject The template object used to create the inventory object
     * @param recursive Should the spawn operation be recursive?
     * @param templateIds Map of the template id and the new object or special object
     * @return The root copied object
     */
    private Node copyTemplateElement(Node templateObject, ClassMetadata classToMap, boolean recursive, HashMap<String, String> templateIds) throws InvalidArgumentException {
        
        Node newInstance = connectionManager.getConnectionHandler().createNode(inventoryObjectLabel);
       
        for (String property : templateObject.getPropertyKeys()) {
            if (!property.equals(Constants.PROPERTY_UUID)) {
                if (classToMap.isMandatory(property) && ((String)templateObject.getProperty(property)).isEmpty())
                    throw new InvalidArgumentException(String.format("The attribute %s is mandatory, can not be set null or empty", property));

                newInstance.setProperty(property, templateObject.getProperty(property));
            }
        }
        
        for (Relationship rel : templateObject.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING))
            newInstance.createRelationshipTo(rel.getEndNode(), RelTypes.RELATED_TO).setProperty(Constants.PROPERTY_NAME, rel.getProperty(Constants.PROPERTY_NAME));
        
        newInstance.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
        newInstance.createRelationshipTo(templateObject.getRelationships(RelTypes.INSTANCE_OF_SPECIAL).iterator().next().getEndNode(), 
                RelTypes.INSTANCE_OF);

        if (recursive) {
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING)) {
                Node classNode = rel.getStartNode().getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                Node newChild = copyTemplateElement(rel.getStartNode(), Util.createClassMetadataFromNode(classNode), true, templateIds);
                newChild.createRelationshipTo(newInstance, RelTypes.CHILD_OF);
            }
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING)) {
                Node classNode = rel.getStartNode().getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                Node newChild = copyTemplateElement(rel.getStartNode(), Util.createClassMetadataFromNode(classNode), true, templateIds);
                newChild.createRelationshipTo(newInstance, RelTypes.CHILD_OF_SPECIAL);
            }
        }

        newInstance.setProperty(Constants.PROPERTY_UUID, UUID.randomUUID().toString());
        if (templateIds != null) {
            templateIds.put(
                templateObject.getProperty(Constants.PROPERTY_UUID).toString(),
                newInstance.getProperty(Constants.PROPERTY_UUID).toString()
            );
        }
        return newInstance;
    }
    
    @Override
    public boolean canDeleteObject(String className, String oid) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {
        if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, className))
            throw new OperationNotPermittedException(String.format("Class %s is not a business-related class", className));
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {   
            Node instance = getInstanceOfClass(className, oid);
            boolean isSafeToDelete = canDeleteObject(instance);
            if (!isSafeToDelete)
                return false;
            else {
                for (Relationship rel : instance.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF, RelTypes.CHILD_OF_SPECIAL))
                    isSafeToDelete = canDeleteObject(rel.getStartNode());
            }
            
            tx.success();
            return isSafeToDelete;
        }
    }

    /**
     * This class wraps a set of attribute definitions necessary to create objects with default values
     */
    public class AttributeDefinitionSet implements Serializable {
        /**
         * The key is the attribute name, the value, the attribute definition, typically one value, a string or a number
         */
        private HashMap<String, String[]> attributes;

        public HashMap<String, String[]> getAttributes() {
            return attributes;
        }

        public void setAttributes(HashMap<String, String[]> attributes) {
            this.attributes = attributes;
        }
    }
    
    //</editor-fold>
    /**
     * Deletes recursively and object and all its children. Note that the transaction should be handled by the caller
     * @param instance The object to be deleted
     * @param unsafeDeletion True if you want the object to be deleted no matter if it has RELATED_TO, HAS_PROCESS_INSTANCE or RELATED_TO_SPECIAL relationships
     * @throws org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException If the object already has relationships
     */
    private void deleteObject(Node instance, boolean unsafeDeletion) throws OperationNotPermittedException {
        if (!unsafeDeletion && !canDeleteObject(instance)) 
            throw new OperationNotPermittedException(String.format("The object %s (%s) can not be deleted since it has relationships", 
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
            if (historyEntryNode.hasRelationship(RelTypes.PERFORMED_BY))
                historyEntryNode.getSingleRelationship(RelTypes.PERFORMED_BY, Direction.OUTGOING).delete();
            if (historyEntryNode.hasRelationship(RelTypes.CHILD_OF_SPECIAL))
                historyEntryNode.getSingleRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).delete();
            aHasHistoryEntryRelationship.delete();
            historyEntryNode.delete();
        }
        
        //Now we dispose of the attachments
        for (Relationship aHasAttachmentRelationship : instance.getRelationships(RelTypes.HAS_ATTACHMENT)) {
            Node attachmentNode = aHasAttachmentRelationship.getEndNode();
            aHasAttachmentRelationship.delete();
            
            String fileName = configuration.getProperty("attachmentsPath", DEFAULT_ATTACHMENTS_PATH) +  //NOI18N
                    File.separator + instance.getId() + "_" + attachmentNode.getId(); //NOI18N
            try {
                new File(fileName).delete();
            } catch (Exception ex) {
                System.out.println(String.format("[KUWAIBA] An error occurred while deleting attachment %s for object %s (%s)", 
                        fileName, instance.getProperty(Constants.PROPERTY_NAME), instance.getId()));
            }
            attachmentNode.delete();
        }
        
        //Now the SyncsDataSourceConfiguration Related to the object
        for (Relationship rel : instance.getRelationships(RelTypes.HAS_SYNC_CONFIGURATION)) {
            Node endNode = rel.getEndNode();
            for (Relationship relds : endNode.getRelationships())
                relds.delete();
            endNode.delete();
        }
        
        //Remove the remaining relationships without deleting the other end of the relationship, 
        //because we don't know what's there (a list type, another element of the model that should not be delete along, etc)
        for (Relationship rel : instance.getRelationships())
            rel.delete();

        instance.delete();
    }
    
    /**
     * Checks if it's safe to delete (not recursively) an object and all its children. Note that the transaction should be handled by the caller.
     * @param instance The object to be deleted.
     * @return true if the object is safe to be deleted, false otherwise.
     */
    private boolean canDeleteObject(Node instance) {
        return !instance.hasRelationship(RelTypes.RELATED_TO_SPECIAL, RelTypes.HAS_PROCESS_INSTANCE);        
    }
    
    //<editor-fold desc="Kuwaiba 2.1 most used in navigation module for now" defaultstate="collapsed">
    @Override
    public long getObjectChildrenCount(String className, String oid, 
            HashMap <String, String> filters) 
            throws InvalidArgumentException 
    {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (className == null)
                throw new InvalidArgumentException("The className cannot be null");
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            StringBuilder queryFilterBuilder = new StringBuilder();
            final String COUNT = "count"; //NOI18N

            if(filters != null){
                filters.entrySet().forEach(entry -> {
                    String key = (String)entry.getKey();
                    String val = (String)entry.getValue();
                    if (val != null) {
                        parameters.put(key, val);
                        queryFilterBuilder.append(String.format("AND TOLOWER(instance.%s) CONTAINS TOLOWER($%s) ", key, key)); //NOI18N
                    }
                });
            }
                                    
            if (oid == null) {
                queryBuilder.append("MATCH (dummyRoot:root:specialNodes)"); //NOI18N
                queryBuilder.append("<-[:CHILD_OF]-"); //NOI18N
                queryBuilder.append("(childNode:inventoryObjects)\n"); //NOI18N
                queryBuilder.append("WHERE dummyRoot.name = $name\n"); //NOI18N
                queryBuilder.append("RETURN count(childNode) AS ").append(COUNT); //NOI18N
                
                parameters.put("name", Constants.DUMMY_ROOT); //NOI18N
            } else {
                queryBuilder.append("MATCH (classNode:classes)"); //NOI18N
                queryBuilder.append("<-[:INSTANCE_OF]-"); //NOI18N
                queryBuilder.append("(parentNode:inventoryObjects)"); //NOI18N
                queryBuilder.append("<-[:CHILD_OF]-"); //NOI18N
                queryBuilder.append("(childNode:inventoryObjects)\n"); //NOI18N
                queryBuilder.append("WHERE classNode.name = $className\n"); //NOI18N
                queryBuilder.append("AND parentNode._uuid = $oid\n"); //NOI18N
                queryBuilder.append("RETURN count(childNode) AS ").append(COUNT); //NOI18N
                
                parameters.put("className", className); //NOI18N
                parameters.put("oid", oid); //NOI18N
            }
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (result.hasNext()) {
                tx.success();
                return (long) result.next().get(COUNT);
            }
            tx.success();
            return 0;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectChildren(String className, 
            String oid, HashMap <String, String> filters, long skip, long limit) 
            throws InvalidArgumentException 
    {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (className == null)
                throw new InvalidArgumentException("The className cannot be null");
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            StringBuilder queryFilterBuilder = new StringBuilder();
            final String CHILD_NODE = "childNode"; //NOI18N
            
            
            if(filters != null){
                filters.entrySet().forEach(entry -> {
                    String key = (String)entry.getKey();
                    String val = (String)entry.getValue();
                    if (val != null) {
                        parameters.put(key, val);
                        queryFilterBuilder.append(String.format("AND TOLOWER(instance.%s) CONTAINS TOLOWER($%s) ", key, key)); //NOI18N
                    }
                });
            }
                                    
            if (oid == null) {
                queryBuilder.append("MATCH (dummyRoot:root:specialNodes)"); //NOI18N
                queryBuilder.append("<-[:CHILD_OF]-"); //NOI18N
                queryBuilder.append("(childNode:inventoryObjects)\n"); //NOI18N
                queryBuilder.append("WHERE dummyRoot.name = $name\n"); //NOI18N
                queryBuilder.append("RETURN childNode ORDER BY toLower(childNode.name) ASC\n"); //NOI18N
                queryBuilder.append((skip >= 0 && limit >= 0)  ? "SKIP $skip LIMIT $limit;" : ""); //NOI18N
                
                parameters.put("name", Constants.DUMMY_ROOT); //NOI18N
            } else {
                queryBuilder.append("MATCH (classNode:classes)"); //NOI18N
                queryBuilder.append("<-[:INSTANCE_OF]-"); //NOI18N
                queryBuilder.append("(parentNode:inventoryObjects)"); //NOI18N
                queryBuilder.append("<-[:CHILD_OF]-"); //NOI18N
                queryBuilder.append("(childNode:inventoryObjects)\n"); //NOI18N
                queryBuilder.append("WHERE classNode.name = $className\n"); //NOI18N
                queryBuilder.append("AND parentNode._uuid = $oid\n"); //NOI18N
                queryBuilder.append("RETURN childNode ORDER BY toLower(childNode.name) ASC\n"); //NOI18N
                queryBuilder.append((skip >= 0 && limit >= 0)  ? "SKIP $skip LIMIT $limit;" : ""); //NOI18N
                
                parameters.put("className", className); //NOI18N
                parameters.put("oid", oid); //NOI18N
            }
            if(skip >= 0 && limit >= 0){
                parameters.put("skip", skip); //NOI18N
                parameters.put("limit", limit); //NOI18N
            }
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            List<BusinessObjectLight> objectChildren = new ArrayList();
            while (result.hasNext())
                objectChildren.add(ogmService.createObjectLightFromNode((Node) result.next().get(CHILD_NODE)));
            tx.success();
            return objectChildren;
        }
    }
    
    @Override
    public HashMap<String, List<BusinessObjectLight>> getSuggestedObjectsWithFilterGroupedByClassName(
            List<String> classesNamesToFilter, String filter
            , long classesSkip, long classesLimit
            , long objectSkip, long objectLimit) throws InvalidArgumentException 
    {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (filter == null)
                throw new InvalidArgumentException("The className cannot be null");
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            parameters.put("searchString", filter); //NOI18N
            
            if(classesNamesToFilter == null){
                queryBuilder.append("MATCH (object:").append(inventoryObjectLabel).append(")-[:INSTANCE_OF]->(class:").append(classLabel).append(") "); //NOI18N
                queryBuilder.append("WHERE TOLOWER(class.name) CONTAINS TOLOWER($searchString) OR TOLOWER(class.displayName) CONTAINS TOLOWER($searchString) OR TOLOWER(object.name) CONTAINS TOLOWER($searchString)"); //NOI18N
                queryBuilder.append("RETURN class {.name, .displayName, objects: collect(distinct object {.name, ._uuid})");
                queryBuilder.append("[").append(objectSkip).append("..").append(objectLimit).append("]} "); //NOI18N
                
                queryBuilder.append("ORDER BY class.name ASC "); 
                if(classesSkip >= 0 && classesLimit >= 0){
                    queryBuilder.append("SKIP $skip LIMIT $limit ");
                    parameters.put("skip", classesSkip); //NOI18N
                    parameters.put("limit", classesLimit); //NOI18N
                }
            }else if(!classesNamesToFilter.isEmpty()){
                queryBuilder.append("MATCH (class:").append(classLabel).append(")"); //NOI18N
                queryBuilder.append("-[:EXTENDS*]->"); //NOI18N
                queryBuilder.append("(pclass:").append(classLabel).append(") "); //NOI18N
                queryBuilder.append("WHERE "); //NOI18N
                
                String instanceClasses = classesNamesToFilter.stream()
                    .map(s -> String.format("class.name= '%s'", s)) // add double quotes around each string
                    .collect(Collectors.joining(" OR "));
                
                queryBuilder.append(instanceClasses).append(" OR "); //NOI18N
                //We also compare with possible parent classes in case a GenericClassName was provided
                String possibleParentClasses = classesNamesToFilter.stream()
                    .map(s -> String.format("pclass.name = '%s'", s)) // add double quotes around each string
                    .collect(Collectors.joining(" OR "));
                queryBuilder.append(possibleParentClasses);
                queryBuilder.append(" WITH class "); //NOI18N
                
                queryBuilder.append("MATCH (object:").append(inventoryObjectLabel).append(")"); //NOI18N
                queryBuilder.append("-[:INSTANCE_OF]->(class:").append(classLabel).append(") "); //NOI18N
                queryBuilder.append("WHERE TOLOWER(object.name) CONTAINS TOLOWER($searchString) "); //NOI18N
                queryBuilder.append("WITH class, COLLECT(distinct object)[").append(objectSkip).append("..").append(objectLimit).append("] as objs "); //NOI18N
                queryBuilder.append("UNWIND objs as obj "); //NOI18N
            
                queryBuilder.append("RETURN class {.name, .displayName, objects: collect(obj {.name, ._uuid})} "); //NOI18N
                queryBuilder.append("ORDER BY class.name ASC "); 
                if(classesSkip >= 0 && classesLimit >= 0){
                    queryBuilder.append("SKIP $skip LIMIT $limit ");
                    parameters.put("skip", classesSkip); //NOI18N
                    parameters.put("limit", classesLimit); //NOI18N
                }
            }
            
            HashMap<String, List<BusinessObjectLight>> res  = new HashMap<>();
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            //format {name: className1, Objects[{}, {}]} {name: className2, Objects[{}, {}]} 
            while (result.hasNext()){
                Map<String, Object> row = result.next();
                
                row.entrySet().forEach(entry_ -> {
                    //format of the json result {name: className1, Objects[{}, {}]} {name: className2, Objects[{}, {}]} 
                    HashMap val = (HashMap)entry_.getValue();
                    String className = (String)val.get("name"); //name: className
                    String classDisplayName = (String)val.get("displayName");
                    //objects: list the objects attributes of the className in a map: [{name, _uuid:}, {name:, uuid:}, {name:, _uuid:}]
                    ArrayList objects = (ArrayList)val.get("objects");

                    if(res.get(className) == null)
                        res.put(className, new ArrayList<>());

                    objects.forEach(inventoryObjectData -> {
                        res.get(className).add(new BusinessObjectLight(className,
                                (String)((HashMap)inventoryObjectData).get("_uuid"), 
                                (String)((HashMap)inventoryObjectData).get("name"), 
                                classDisplayName));
                    });
                });
            }
            tx.success();
            return res;
        }
    }
    
    @Override
    public HashMap<String, List<InventoryObjectPool>> getSuggestedPoolsByName(
            List<String> classesNamesToFilter, String nameTofilter, long poolSkip
            , long poolLimit, long objectSkip, long objectLimit) throws InvalidArgumentException{
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (nameTofilter == null)
                throw new InvalidArgumentException("The className cannot be null");
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            
            queryBuilder.append(String.format("MATCH (pool:%s) ", poolLabel)); //NOI18N
            if(classesNamesToFilter == null)
                queryBuilder.append("WHERE TOLOWER(pool.className) CONTAINS TOLOWER($searchString) OR "); //NOI18N
            
            else if(!classesNamesToFilter.isEmpty()){
                  String classesNames = classesNamesToFilter.stream()
                        .map(s -> String.format("'%s'", s)) // add double quotes around each string
                        .collect(Collectors.joining(","));
                  
                queryBuilder.append("WHERE (pool.className) IN [").append(classesNames).append("] AND "); //NOI18N
            }
            queryBuilder.append("TOLOWER(pool.name) CONTAINS TOLOWER($searchString) "); //NOI18N
            queryBuilder.append("RETURN pool {.className, pools: collect(distinct pool {.name, ._uuid, .description, .type})"); //NOI18N
            queryBuilder.append("[").append(objectSkip).append("..").append(objectLimit).append("]").append("} "); //NOI18N
            queryBuilder.append("ORDER BY pool.name ASC ");
            if(poolLimit >= 0 && poolSkip >= 0){
                queryBuilder.append("SKIP $skip LIMIT $limit ");
                parameters.put("skip", poolSkip); //NOI18N
                parameters.put("limit", poolLimit); //NOI18N
            }
            
            parameters.put("searchString", nameTofilter);//NOI18N
            
            HashMap<String, List<InventoryObjectPool>> res  = new HashMap<>();
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (result.hasNext()){
                Map<String, Object> row = result.next();
                
                row.entrySet().forEach(entry_ -> {
                    //format of the json result {name: className1, Objects[{}, {}]} {name: className2, Objects[{}, {}]} 
                    HashMap val = (HashMap)entry_.getValue();
                    String className = (String)val.get("className"); //name: className
                   //objects: list the objects attributes of the className in a map: [{name, _uuid:}, {name:, uuid:}, {name:, _uuid:}]
                    ArrayList pools = (ArrayList)val.get("pools");

                    if(res.get(className) == null)
                        res.put(className, new ArrayList<>());
                            
                    pools.forEach(inventoryObjectData -> {
                        res.get(className).add(new InventoryObjectPool(
                            (String)((HashMap)inventoryObjectData).get("_uuid"),
                            (String)((HashMap)inventoryObjectData).get("name"), 
                            (String)((HashMap)inventoryObjectData).get("description"),
                            className,
                            (int)((HashMap)inventoryObjectData).get("type")));
                    });
                });
            }
            tx.success();
            return res;
        }
    }
    //</editor-fold>
}
