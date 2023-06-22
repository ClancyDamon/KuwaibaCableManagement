/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.core.persistence.reference.neo4j;

import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.DatabaseException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.kernel.impl.traversal.MonoDirectionalTraversalDescription;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.extras.caching.CacheManager;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * MetadataEntityManager implementation for Neo4j.
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Service
public class MetadataEntityManagerImpl implements MetadataEntityManager {
    /**
     * Class label
     */
    private Label classLabel;
    /**
     * Reference to the CacheManager
     */
    private CacheManager cm;
    /**
     * This hash contains the display name of the special relationship used in the different models
     */
    private HashMap<String, String> relationshipDisplayNames;
    /**
     * Configuration variables set from the persistence service before using this entity manager.
     */
    private Properties configuration;
    /**
     * Reference to the connection manager.
     */
    @Autowired
    private ConnectionManager<GraphDatabaseService> connectionManager;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    
    public MetadataEntityManagerImpl() {
        this.relationshipDisplayNames = new HashMap<>();
        this.classLabel = Label.label(Constants.LABEL_CLASS);
        
    }
    
    @Override
    public void initCache() {
        this.cm = CacheManager.getInstance();
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            buildClassCache();
        } catch(Exception ex) {
            Logger.getLogger(MetadataEntityManagerImpl.class.getName()).log(Level.SEVERE, 
                    String.format("[KUWAIBA] [%s] An error was found while creating the MEM instance: %s", 
                    Calendar.getInstance().getTime(), ex.getMessage()));
        }
    }

    @Override
    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public long createClass(ClassMetadata classDefinition) throws MetadataObjectNotFoundException, DatabaseException, InvalidArgumentException {
        if (classDefinition.getName() == null)
            throw new InvalidArgumentException("Class name can not be null");
            
        if (!classDefinition.getName().matches("^[a-zA-Z0-9_-]*$"))
            throw new InvalidArgumentException(String.format("Class %s contains invalid characters", classDefinition.getName()));
        
        if(classDefinition.getName().isEmpty())
                    throw new InvalidArgumentException("Class name can not be an empty string");
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            if (connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, classDefinition.getName()) != null)
                throw new InvalidArgumentException(String.format("Class %s already exists", classDefinition.getName()));
            
            Node classNode = connectionManager.getConnectionHandler().createNode(classLabel);

            classNode.setProperty(Constants.PROPERTY_NAME, classDefinition.getName());
            classNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, classDefinition.getDisplayName() == null ? "" : classDefinition.getDisplayName());
            classNode.setProperty(Constants.PROPERTY_CUSTOM, classDefinition.isCustom());
            classNode.setProperty(Constants.PROPERTY_COUNTABLE, classDefinition.isCountable());
            classNode.setProperty(Constants.PROPERTY_COLOR, classDefinition.getColor());
            classNode.setProperty(Constants.PROPERTY_DESCRIPTION, classDefinition.getDescription() == null ? "" : classDefinition.getDescription());
            classNode.setProperty(Constants.PROPERTY_ABSTRACT, classDefinition.isAbstract());
            classNode.setProperty(Constants.PROPERTY_ICON, classDefinition.getIcon() == null ? new byte[0] : classDefinition.getIcon());
            classNode.setProperty(Constants.PROPERTY_SMALL_ICON, classDefinition.getSmallIcon() ==  null ? new byte[0] : classDefinition.getSmallIcon());
            classNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            classNode.setProperty(Constants.PROPERTY_IN_DESIGN, classDefinition.isInDesign());
          
            //Here we add the attributes
            if (classDefinition.getAttributes() != null) {
                for (AttributeMetadata attributeMetadata : classDefinition.getAttributes())
                    //This no longer checks for duplicates since the attributes are now a set
                    Util.createAttribute(classNode, attributeMetadata, true);
            }
            
            //Now we make our class to inherit the attributes from its parent class (except for the root class, RootObject)
            if (classDefinition.getParentClassName() == null) { //Is this class the root of all class hierarchy
                
                if (classDefinition.getName().equals(Constants.CLASS_ROOTOBJECT))
                    classNode.addLabel(Label.label(Constants.LABEL_ROOT));
                else
                    throw new MetadataObjectNotFoundException(String.format("Only %s can be the root superclass", Constants.CLASS_ROOTOBJECT));
            }
            else { 
                Node parentNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, classDefinition.getParentClassName());
                
                if (parentNode != null) {
                    classNode.createRelationshipTo(parentNode, RelTypes.EXTENDS);
                    Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
                    //Set extendended attributes from parent
                    for (Relationship rel : relationships) {
                        Node parentAttrNode = rel.getEndNode();
                        
                        //We ignore the attributes already existing in the class definition
                        //TODO: This block of code only exists because the class hierachy 
                        //file used to reset the data model is redundant. Once its fixed, please remove it
                        String attributeName = String.valueOf(parentAttrNode.getProperty(Constants.PROPERTY_NAME));
                        boolean skipThis = false;
                        for (AttributeMetadata anAttribute : classDefinition.getAttributes()) {
                            if (anAttribute.getName().equals(attributeName)) {
                                skipThis = true;
                                break;
                            }
                        }
                        
                        if (skipThis)
                            continue;
                        
                        Label label = Label.label(Constants.LABEL_ATTRIBUTE);
                        Node newAttrNode = connectionManager.getConnectionHandler().createNode(label);
                        //Locks are not inherited
                        newAttrNode.setProperty(Constants.PROPERTY_NAME, attributeName);
                        newAttrNode.setProperty(Constants.PROPERTY_DESCRIPTION, parentAttrNode.getProperty(Constants.PROPERTY_DESCRIPTION));
                        newAttrNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, parentAttrNode.getProperty(Constants.PROPERTY_DISPLAY_NAME));
                        newAttrNode.setProperty(Constants.PROPERTY_TYPE, parentAttrNode.getProperty(Constants.PROPERTY_TYPE));
                        newAttrNode.setProperty(Constants.PROPERTY_READ_ONLY, parentAttrNode.getProperty(Constants.PROPERTY_READ_ONLY));
                        newAttrNode.setProperty(Constants.PROPERTY_VISIBLE, parentAttrNode.getProperty(Constants.PROPERTY_VISIBLE));
                        newAttrNode.setProperty(Constants.PROPERTY_ADMINISTRATIVE, parentAttrNode.getProperty(Constants.PROPERTY_ADMINISTRATIVE));
                        newAttrNode.setProperty(Constants.PROPERTY_NO_COPY, parentAttrNode.getProperty(Constants.PROPERTY_NO_COPY));
                        newAttrNode.setProperty(Constants.PROPERTY_UNIQUE, parentAttrNode.getProperty(Constants.PROPERTY_UNIQUE));
                        newAttrNode.setProperty(Constants.PROPERTY_ORDER, parentAttrNode.hasProperty(Constants.PROPERTY_ORDER) ?  
                                parentAttrNode.getProperty(Constants.PROPERTY_ORDER) : 1000);
                        newAttrNode.setProperty(Constants.PROPERTY_MULTIPLE, parentAttrNode.hasProperty(Constants.PROPERTY_MULTIPLE) ?  
                                parentAttrNode.getProperty(Constants.PROPERTY_MULTIPLE) : false);
                        newAttrNode.setProperty(Constants.PROPERTY_MANDATORY, parentNode.hasProperty(Constants.PROPERTY_MANDATORY) ?
                                parentAttrNode.getProperty(Constants.PROPERTY_MANDATORY) : false);
                        
                        
                        classNode.createRelationshipTo(newAttrNode, RelTypes.HAS_ATTRIBUTE);
                    }
                }//end if there is a Parent
                else
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find parent class with name %s", classDefinition.getParentClassName()));
            }//end else not rootNode
            
            buildClassCache();
            getSubClassesLight(classDefinition.getName(), true, false);
            getSubClassesLightNoRecursive(classDefinition.getName(), true, false);
            
            tx.success();
            return classNode.getId();
        }
    }

    @Override
    public ChangeDescriptor setClassProperties (long classId, HashMap<String, Object> newProperties) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException {
        String affectedProperties = "", oldValues = "", newValues = "";
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classMetadataNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, classId);
            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "The class with id %s could not be found", classId));

            String className = (String)classMetadataNode.getProperty(Constants.PROPERTY_NAME);
            
            for (String aProperty : newProperties.keySet()) {
                switch (aProperty) {
                    case Constants.PROPERTY_NAME:
                        String newName = (String)newProperties.get(aProperty);
                        String formerName = className;
                        if (newName != null) {
                            if (newName.trim().isEmpty())
                                throw new InvalidArgumentException("Class name can not be an empty string");

                            if (!newName.matches("^[a-zA-Z0-9_-]*$"))
                                throw new InvalidArgumentException(String.format("Class name %s contains invalid characters", newName));

                            if (connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, newName) != null)
                               throw new InvalidArgumentException(String.format("Class %s already exists", newName));

                            classMetadataNode.setProperty(Constants.PROPERTY_NAME, newName);

                            affectedProperties += Constants.PROPERTY_NAME + " ";
                            oldValues += formerName + " ";
                            newValues += newName + " ";
                            
                            className = newName;
                            
                            buildClassCache();
                        }
                        break;
                    case Constants.PROPERTY_DESCRIPTION:
                        String newDescription = (String)newProperties.get(aProperty);
                        affectedProperties += Constants.PROPERTY_DESCRIPTION + " ";
                        oldValues += classMetadataNode.getProperty(Constants.PROPERTY_DESCRIPTION) + " ";
                        newValues += newDescription + " ";
                        classMetadataNode.setProperty(Constants.PROPERTY_DESCRIPTION, newDescription == null ? "" : newDescription);
                        break;
                    case Constants.PROPERTY_DISPLAY_NAME:
                        String newDisplayName = (String)newProperties.get(aProperty);
                        affectedProperties += Constants.PROPERTY_DISPLAY_NAME + " ";
                        oldValues += classMetadataNode.getProperty(Constants.PROPERTY_DISPLAY_NAME) + " ";
                        newValues += newDisplayName + " ";
                        classMetadataNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, newDisplayName);
                        break;
                    case Constants.PROPERTY_ICON:
                        affectedProperties += Constants.PROPERTY_ICON + " ";
                        oldValues += " ";
                        newValues += " ";
                        classMetadataNode.setProperty(Constants.PROPERTY_ICON, newProperties.get(aProperty) == null ?
                                new byte[0] : newProperties.get(aProperty));
                        break;
                    case Constants.PROPERTY_SMALL_ICON:
                        affectedProperties += Constants.PROPERTY_SMALL_ICON + " ";
                        oldValues += " ";
                        newValues += " ";
                        classMetadataNode.setProperty(Constants.PROPERTY_SMALL_ICON, newProperties.get(aProperty) == null ? 
                                new byte[0] : newProperties.get(aProperty));
                        break;
                    case Constants.PROPERTY_COLOR:
                        if (!(newProperties.get(aProperty) instanceof Integer))
                            throw new InvalidArgumentException(String.format(ts.
                                    getTranslatedString("apis.persistence.mem.messages.wrong-type-property"), 
                                            Constants.PROPERTY_COLOR, ts.getTranslatedString("apis.persistence.mem.labels.integer"),
                                            ts.getTranslatedString("apis.persistence.mem.labels.string"))
                            );
                        int newColor = (int)newProperties.get(aProperty);
                        affectedProperties += Constants.PROPERTY_COLOR + " ";
                        oldValues += classMetadataNode.getProperty(Constants.PROPERTY_COLOR) + " ";
                        newValues += newColor + " ";
                        classMetadataNode.setProperty(Constants.PROPERTY_COLOR, newColor);
                        break;
                    case Constants.PROPERTY_COUNTABLE:
                        boolean newCountable = (boolean)newProperties.get(aProperty);
                        affectedProperties = Constants.PROPERTY_COUNTABLE + " ";
                        oldValues = classMetadataNode.getProperty(Constants.PROPERTY_COUNTABLE) + " ";
                        newValues = newCountable + " ";
                        classMetadataNode.setProperty(Constants.PROPERTY_COUNTABLE, newCountable);
                        break;
                    case Constants.PROPERTY_ABSTRACT:
                        boolean newAbstract = (boolean)newProperties.get(aProperty);
                        affectedProperties = Constants.PROPERTY_ABSTRACT + " ";
                        oldValues = classMetadataNode.getProperty(Constants.PROPERTY_ABSTRACT) + " ";
                        newValues = newAbstract + " ";
                        classMetadataNode.setProperty(Constants.PROPERTY_ABSTRACT, newAbstract);
                        break;
                    case Constants.PROPERTY_IN_DESIGN:
                        boolean newInDesign = (boolean)newProperties.get(aProperty);
                        affectedProperties = Constants.PROPERTY_IN_DESIGN + " ";
                        oldValues = classMetadataNode.getProperty(Constants.PROPERTY_IN_DESIGN) + " ";
                        newValues = newInDesign + " ";
                        classMetadataNode.setProperty(Constants.PROPERTY_IN_DESIGN, newInDesign);
                        break;
                    case Constants.PROPERTY_CUSTOM:
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Class property %s is unknown", aProperty));
                }
            }
            
            tx.success();
            cm.clearClassCache();
            cm.putClass(Util.createClassMetadataFromNode(classMetadataNode));

            return new ChangeDescriptor(affectedProperties.trim(), oldValues.trim(), 
                    newValues.trim(), "");
        }
    }
  
    @Override
    public void deleteClass(long classId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node node = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, classId);

            if (node == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "The class with id %s could not be found. Contact your administrator.", classId));
                      
            if (!(Boolean)node.getProperty(Constants.PROPERTY_CUSTOM))
                throw new InvalidArgumentException(String.format(
                        "Core classes can not be deleted"));
            
            if (node.hasRelationship(RelTypes.INSTANCE_OF))
                throw new InvalidArgumentException(String.format(
                        "Class %s has instances and can not be deleted", node.getProperty(Constants.PROPERTY_NAME)));
            
            if (node.hasRelationship(Direction.INCOMING, RelTypes.EXTENDS))
                throw new InvalidArgumentException(String.format(
                        "Class %s has subclasses and can not be deleted", node.getProperty(Constants.PROPERTY_NAME)));

            String className = (String)node.getProperty(Constants.PROPERTY_NAME);
            if (isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, className)) {
                //If the class is a list type, let's check if it's used by another class
                ResourceIterator<Node> classes = connectionManager.getConnectionHandler().findNodes(classLabel);
                                
                while (classes.hasNext()) {
                    Node classNode = classes.next();
                                        
                    for (Relationship hasAttributeRelationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_ATTRIBUTE)) {
                        Node attributeNode = hasAttributeRelationship.getEndNode();
                        if (attributeNode.getProperty(Constants.PROPERTY_TYPE).equals(className))
                            throw new InvalidArgumentException(
                                    String.format("%s is a list type and at least one attribute (%s) from another class (%s) is using it",
                                                    className, attributeNode.getProperty(Constants.PROPERTY_NAME), classNode.getProperty(Constants.PROPERTY_NAME)));
                    }
               }
            }
            
            //Delete the related elements that don't require any complex Remove operation
            Iterable<Relationship> outgoingSimpleRelationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE, RelTypes.HAS_REPORT);
            for (Relationship rel : outgoingSimpleRelationships){
                Node relatedNode = rel.getEndNode();
                rel.delete();
                relatedNode.delete();
            }
            
            //Delete the existing templates
            Iterable<Relationship> templateRelationshipsRelationships = node.getRelationships(RelTypes.HAS_TEMPLATE);
            for (Relationship rel : templateRelationshipsRelationships)
                Util.deleteTemplateObject(rel.getEndNode());
            
            
            //Release the rest of relationships
            for (Relationship rel : node.getRelationships())
                rel.delete();
                        
            node.delete();
            buildClassCache();
            tx.success();
        }
    }
    
    @Override
    public void deleteClass(String className) 
            throws MetadataObjectNotFoundException, InvalidArgumentException   {
        try (Transaction tx  = connectionManager.getConnectionHandler().beginTx()) {
            
            Node node = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);

            if (node == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Class %s could not be found", className));
            
            if (!(Boolean)node.getProperty(Constants.PROPERTY_CUSTOM))
                throw new InvalidArgumentException(String.format(
                        "Core classes can not be deleted"));
                       
            if (node.hasRelationship(RelTypes.INSTANCE_OF))
                throw new InvalidArgumentException(String.format(
                        "The class with name %s has instances and can not be deleted", className));
            
            if (node.hasRelationship(Direction.INCOMING, RelTypes.EXTENDS))
                throw new InvalidArgumentException(String.format(
                        "The class with name %s has subclasses and can not be deleted", className));
            
            if (isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, className)) {
                //If the class is a list type, let's check if it's used by another class
                ResourceIterator<Node> classes = connectionManager.getConnectionHandler().findNodes(classLabel);
                                
                while (classes.hasNext()) {
                    Node classNode = classes.next();
                    
                    for (Relationship hasAttributeRelationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_ATTRIBUTE)) {
                        Node attributeNode = hasAttributeRelationship.getEndNode();
                        if (attributeNode.getProperty(Constants.PROPERTY_TYPE).equals(className))
                            throw new InvalidArgumentException(
                                    String.format("%s is a list type and at least one attribute (%s) from another class (%s) is using it",
                                                    className, attributeNode.getProperty(Constants.PROPERTY_NAME), classNode.getProperty(Constants.PROPERTY_NAME)));
                    }
               }
            }
            
            //Delete the related elements that don't require any complex Remove operation
            Iterable<Relationship> outgoingSimpleRelationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE, RelTypes.HAS_REPORT);
            for (Relationship rel : outgoingSimpleRelationships){
                Node relatedNode = rel.getEndNode();
                rel.delete();
                relatedNode.delete();
            }
            
            //Delete the existing templates
            Iterable<Relationship> templateRelationshipsRelationships = node.getRelationships(RelTypes.HAS_TEMPLATE);
            for (Relationship rel : templateRelationshipsRelationships)
                Util.deleteTemplateObject(rel.getEndNode());
            
            //Release the rest of relationships
            for (Relationship rel : node.getRelationships())
                rel.delete();
            
            node.delete();
            buildClassCache();
            tx.success();
        }
    }
   
    @Override
    public List<ClassMetadataLight> getAllClassesLight(boolean includeListTypes, 
            boolean includeIndesign) 
            throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> cml = new ArrayList<>();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node myClassInventoryObjectNode =  connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, Constants.CLASS_INVENTORYOBJECT);
            Node myClassGenericObjectListNode = null;
            
            if (includeListTypes)
                myClassGenericObjectListNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, Constants.CLASS_GENERICOBJECTLIST);
                        
            if (myClassInventoryObjectNode == null) {
                throw new MetadataObjectNotFoundException(String.format(
                         ts.getTranslatedString("module.general.messages.class-not-found"), Constants.CLASS_INVENTORYOBJECT));
            }
            else
                cml.add(Util.createClassMetadataLightFromNode(myClassInventoryObjectNode));
                            
            if (includeListTypes && myClassGenericObjectListNode == null) {
                throw new MetadataObjectNotFoundException(String.format(
                         ts.getTranslatedString("module.general.messages.class-not-found"), Constants.CLASS_GENERICOBJECTLIST));
            }
            
            if (includeListTypes && myClassGenericObjectListNode != null)
                cml.add(Util.createClassMetadataLightFromNode(myClassGenericObjectListNode));
            
            String rootClasses;

            if(includeListTypes)
                rootClasses = "'" + Constants.CLASS_INVENTORYOBJECT + "', '" + Constants.CLASS_GENERICOBJECTLIST + "'";
            else
                rootClasses = "'" + Constants.CLASS_INVENTORYOBJECT + "'";
            
            String cypherQuery = "MATCH (inventory:classes)<-[:EXTENDS*]-(classmetadata) "
                + "WHERE inventory.name IN [" + rootClasses + "] "
                + "RETURN classmetadata, inventory "
                + "ORDER BY classmetadata.name ASC;";
            
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<Node> n_column = result.columnAs("classmetadata"); 
            
            for (Node node : Iterators.asIterable(n_column))
                 cml.add(Util.createClassMetadataLightFromNode(node));
        }
        return cml;
    }

    @Override
    public List<ClassMetadataLight> getSubClassesLight(String className, boolean includeAbstractClasses, 
            boolean includeSelf) throws MetadataObjectNotFoundException {
        
        ClassMetadata aClass = getClass(className);
        
        List<ClassMetadataLight> subclasses = cm.getSubclasses(className);
        
        List<ClassMetadataLight> classManagerResultList = new ArrayList<>();
        
        if(subclasses != null) {
            for (ClassMetadataLight subclass : subclasses) {                
                if (!includeAbstractClasses && subclass.isAbstract())
                    continue;
                classManagerResultList.add(subclass);
            }
            
            if (includeSelf && (includeAbstractClasses ? true : !aClass.isAbstract()))
                classManagerResultList.add(aClass);
            
            return classManagerResultList;
        }
        // Retrieving all subclasses to update the cache
        String cypherQuery = "MATCH (inventory:classes)<-[:EXTENDS*]-(classmetadata) "
                           + "WHERE inventory.name IN ['" + className + "'] "
                           + "RETURN classmetadata "
                           + "ORDER BY classmetadata.name ASC;";
                
        subclasses = new ArrayList();
            
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<Node> n_column = result.columnAs("classmetadata"); //NOI18N
            if (includeSelf && (includeAbstractClasses ? true : !aClass.isAbstract()))
                classManagerResultList.add(aClass);
            
            for (Node node : Iterators.asIterable(n_column)) {
                ClassMetadataLight classMetadata = Util.createClassMetadataLightFromNode(node);
                subclasses.add(classMetadata);
                
                if (!includeAbstractClasses && classMetadata.isAbstract())
                    continue;
                    
                classManagerResultList.add(classMetadata);
            }
            tx.success();
        }
        cm.putSubclasses(className, subclasses);
        
        return classManagerResultList;
    }


    @Override
    public List<ClassMetadataLight> getSubClassesLightNoRecursive(String className, 
            boolean includeAbstractClasses, boolean includeSelf) throws MetadataObjectNotFoundException {
        ClassMetadata aClass = cm.getClass(className);
        if (aClass == null)
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.class-not-found"), className));
        
        List<ClassMetadataLight> subclasses = cm.getSubclassesNorecursive(className);        
        List<ClassMetadataLight> classManagerResultList = new ArrayList<>();
        if(subclasses != null) {
            for (ClassMetadataLight subclass : subclasses) {
                if (!includeAbstractClasses && subclass.isAbstract())
                    continue;
                classManagerResultList.add(subclass);
            }
            
            if (includeSelf && (includeAbstractClasses ? true : !aClass.isAbstract()))
                classManagerResultList.add(aClass);
            
            return classManagerResultList;
        }
        // Retrieving all subclasses to update the cache
        classManagerResultList = new ArrayList<>();
        
        String cypherQuery = ""
            + "MATCH (inventory:classes)<-[:EXTENDS]-(classmetadata) "
            + "WHERE inventory.name IN ['" + className + "'] "
            + "RETURN classmetadata "
            + "ORDER BY classmetadata.name ASC;";
        
        subclasses = new ArrayList();

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<Node> n_column = result.columnAs("classmetadata"); //NOI18N
            if (includeSelf && (includeAbstractClasses ? true : !aClass.isAbstract()))
                classManagerResultList.add(aClass);
            for (Node node : Iterators.asIterable(n_column)) {
                ClassMetadataLight classMetadata = Util.createClassMetadataLightFromNode(node);
                subclasses.add(classMetadata);
                
                if (!includeAbstractClasses && classMetadata.isAbstract())
                    continue;
                    
                classManagerResultList.add(classMetadata);
            }
            
            tx.success();
        }
        cm.putSubclassesNorecursive(className, subclasses);
        return classManagerResultList;
    }
    
    @Override
    public List<ClassMetadataLight> getSuperClassesLight(String className, boolean includeSelf) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> res = new ArrayList<>();
        String cypherQuery = "MATCH path = (aClass:classes)-[:EXTENDS*]->(topClass{name:\"" + Constants.CLASS_INVENTORYOBJECT + "\"}) "
                + "WHERE aClass.name = {className} RETURN nodes(path) AS superClasses";
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("className", className);
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery, parameters);
            if (!result.hasNext())
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found. Contact your administrator.", className));
            else {
                ResourceIterator<List<Node>> superClasses = result.columnAs("superClasses");
                superClasses.next().stream().forEach((aSuperClassNode) -> {
                    res.add(Util.createClassMetadataLightFromNode(aSuperClassNode));
                });
                tx.success();
                return res;
            }
        }
    }
    
    @Override
    public long getSubClassesCount(String className) 
            throws MetadataObjectNotFoundException {
        ClassMetadata aClass = cm.getClass(className);
        if (aClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found. Contact your administrator.", className));
        
        List<ClassMetadataLight> subclasses = cm.getSubclassesNorecursive(className);        
        if(subclasses != null) {
            return subclasses.size();
        }
        // Retrieving all subclasses to update the cache
        
        String cypherQuery = ""
            + "MATCH (inventory:classes)<-[:EXTENDS]-(classmetadata) "
            + "WHERE inventory.name IN ['" + className + "'] "
            + "RETURN COUNT(classmetadata) as Count";     

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            
            while (result.hasNext()) {
                tx.success();
                return (long) result.next().get("Count");
            }
            
            tx.success();
        }
        return -1;
    }
    
    @Override
    public List<ClassMetadata> getAllClasses(boolean includeListTypes, boolean includeIndesign) {
        List<ClassMetadata> classMetadataResultList = new ArrayList<>();
        
        String rootClasses;

        if(includeListTypes)
            rootClasses = String.format("\'%s\', \'%s\'", Constants.CLASS_INVENTORYOBJECT, Constants.CLASS_GENERICOBJECTLIST);
        else
            rootClasses = String.format("\'%s\'", Constants.CLASS_INVENTORYOBJECT);
        
        String cypherQuery = "MATCH (inventory:classes) <-[:" + RelTypes.EXTENDS + "*]-(classmetadata) " +
                             "WHERE inventory.name IN [" + rootClasses + "] " +
                             "RETURN classmetadata, inventory " +
                             "ORDER BY classmetadata.name ASC";
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
           Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            ResourceIterator<Node> n_column = result.columnAs("classmetadata");
           //First, we inject the InventoryObject class (for some reason, the start node can't be retrieved as part of the path, so it can be sorted)
            ResourceIterator<Node> roots = result.columnAs("inventory");
           classMetadataResultList.add(Util.createClassMetadataFromNode(roots.next()));

           for (Node node : Iterators.asIterable(n_column))
                classMetadataResultList.add(Util.createClassMetadataFromNode(node));
           
           tx.success();
        }
        return classMetadataResultList;
    }
   
    @Override
    public ClassMetadata getClass(long classId)  throws MetadataObjectNotFoundException {
        ClassMetadata clmt = null;
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) 
        {
            Node node = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, classId);
            
            if (node == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "The class with id %s could not be found. Contact your administrator.", classId));
            
            clmt = Util.createClassMetadataFromNode(node);
            tx.success();
        } 
        return clmt;
    }
    
    @Override
    public List<ClassMetadata> getClasses(String classNameTofilter, int page, int limit){
        List<ClassMetadata> classes = new ArrayList<>();
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) 
        {
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            final String _CLASS = "_class"; //NOI18N
            
            queryBuilder.append(String.format("MATCH (class:%s) ", classLabel)); //NOI18N
            if (classNameTofilter != null){ //if no filter is set we return all classes
                parameters.put("searchString", classNameTofilter);//NOI18N
                queryBuilder.append("WHERE TOLOWER(class.name) CONTAINS TOLOWER($searchString) "); //NOI18N
            }
            queryBuilder.append("RETURN class AS _class "); //NOI18N
            queryBuilder.append("ORDER BY class.name ASC "); 
            
            if(page >= 0 && limit >= 0){
                queryBuilder.append("SKIP $skip LIMIT $limit ");
                parameters.put("skip", page); //NOI18N
                parameters.put("limit", limit); //NOI18N
            }
            
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            
            while (result.hasNext())
                classes.add(Util.createClassMetadataFromNode((Node) result.next().get(_CLASS)));
            
            tx.success();
        } 
        return classes;
    }
    
    @Override
    public ClassMetadata getClass(String className) throws MetadataObjectNotFoundException {
        ClassMetadata clmt = cm.getClass(className);
        if (clmt == null) {
            try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
                Node node = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);

                if (node == null) {
                    tx.success();
                    MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(
                        ts.getTranslatedString("api.mem.error.2"), className));
                    ex.setPrefix("api.mem.error");
                    ex.setCode(2);
                    ex.setMessageArgs(className);
                    throw ex;
                }
                clmt = Util.createClassMetadataFromNode(node);
                cm.putClass(clmt);
                tx.success();
            }
        }
        
        return clmt;
    }

    @Override
    public void createAttribute(String className, AttributeMetadata attributeDefinition, boolean recursive) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        if (attributeDefinition.getName() == null || attributeDefinition.getName().isEmpty())
            throw new InvalidArgumentException("Attribute name can not be null or an empty string");
        
        if (!attributeDefinition.getName().matches("^[a-zA-Z0-9_]*$"))
            throw new InvalidArgumentException(String.format("Attribute %s contains invalid characters", attributeDefinition.getName()));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx())
        {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found. Contact your administrator.", className));
            
            Util.createAttribute(classNode, attributeDefinition, recursive);
            //Refresh cache for the affected classes
            refreshCacheOn(classNode);
            tx.success();
        } 
    }
    
    @Override
    public void createAttribute(long classId, AttributeMetadata attributeDefinition) 
            throws MetadataObjectNotFoundException, InvalidArgumentException 
    {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx())
        {        
            Node classNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, classId);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("The class with id %s could not be found. Contact your administrator.", classId));
        
            Util.createAttribute(classNode, attributeDefinition, true);
            //Refresh cache for the affected classes
            refreshCacheOn(classNode);
            tx.success();
            
        } 
    }
    
    @Override
    public boolean hasAttribute(String className, String attributeName) throws MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
                        
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format(
                    "Class %s could not be found. Contact your administrator.", className));
            
            for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                Node attrNode = relationship.getEndNode();
                
                if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName))
                    return true;
            }
            return false;
        }
    }

    @Override
    public AttributeMetadata getAttribute(String className, String attributeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        AttributeMetadata attribute = null;
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx())
        {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Class %s could not be found. Contact your administrator.", className));
            
            Iterable<Relationship> relationships = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node attrNode = relationship.getEndNode();
                if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName)){ 
                    attribute = Util.createAttributeMetadataFromNode(attrNode);
                    break;
                }
            }
        }
        
        if (attribute == null)
            throw new InvalidArgumentException(String.format("Attribute %s does not exist in class %s", attributeName, className));
        
        return attribute;
    }
    
    @Override
    public AttributeMetadata getAttribute(long classId, long attributeId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException
    {
        AttributeMetadata attribute = null;
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx())
        {
            Node classNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, classId);
            if (classNode == null) 
                throw new MetadataObjectNotFoundException(String.format(
                        "The class with id %s could not be found. Contact your administrator.", classId));
            
            Iterable<Relationship> relationships = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node attrNode = relationship.getEndNode();
                if (attrNode.getId() == attributeId) {
                    attribute = Util.createAttributeMetadataFromNode(attrNode);
                    break;
                }
            }
        }
        
        if (attribute == null)
            throw new InvalidArgumentException(String.format("Attribute with id %s does not exist in class with id %s", attributeId, classId));
        
        return attribute;
    }
    
    @Override
    public ChangeDescriptor setAttributeProperties(long classId, long attributeId, HashMap<String, Object> newProperties) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, classId);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("The class with id %s could not be found. Contact your administrator.", classId));

            ChangeDescriptor changeDescriptor = setAttributeProperties(classNode, attributeId, newProperties);
            tx.success();
            return changeDescriptor;
        } 
    }
    
    @Override
    public ChangeDescriptor setAttributeProperties (String className, long attributeId, HashMap<String, Object> newProperties) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException {
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            ChangeDescriptor changeDescriptor = setAttributeProperties(classNode, attributeId, newProperties);
            tx.success();
            return changeDescriptor;
        }
    }
    
    @Override
    public void deleteAttribute(String className, String attributeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        if (attributeName.equals(Constants.PROPERTY_NAME) || attributeName.equals(Constants.PROPERTY_CREATION_DATE))
            throw new InvalidArgumentException(String.format("Attribute \"%s\" can not be deleted", attributeName));
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found. Contact your administrator.", className));

            for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                Node attrNode = relationship.getEndNode();
                if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName)){
                
                    if (AttributeMetadata.isPrimitive((String)attrNode.getProperty(Constants.PROPERTY_TYPE)))
                        Util.deleteAttributeIfPrimitive(classNode, attributeName);
                    else
                        Util.deleteAttributeIfListType(classNode, attributeName);
                    
                    refreshCacheOn(classNode);
                    tx.success();
                    return;
                }//end for
            }//end for
        } 
        throw new MetadataObjectNotFoundException(String.format("Can not find an attribute with the name %s", attributeName));
    }

    @Override
    public void deleteAttribute(long classId, String attributeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        if (attributeName.equals(Constants.PROPERTY_CREATION_DATE) || attributeName.equals(Constants.PROPERTY_NAME))
            throw new InvalidArgumentException(String.format("Attribute \"%s\" can not be deleted", attributeName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, classId);

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("The class with id %s could not be found. Contact your administrator.", classId));

            for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                Node attrNode = relationship.getEndNode();
                if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName)) {
                    if (AttributeMetadata.isPrimitive((String)attrNode.getProperty(Constants.PROPERTY_TYPE)))
                        Util.deleteAttributeIfPrimitive(classNode, attributeName);
                    else
                        Util.deleteAttributeIfListType(classNode, attributeName);
                    
                    refreshCacheOn(classNode);
                    tx.success();
                    return;
                }
            }//end for
        } 
        throw new MetadataObjectNotFoundException(String.format(
                "Can not find an attribute with name %s", attributeName));
    }
    
    @Override
    public List<ClassMetadataLight> getPossibleChildren(String parentClassName, boolean ignoreAbstract) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> classMetadataResultList = new ArrayList<>();
        
        List<ClassMetadata> cachedPossibleChildren = cm.getPossibleChildren(parentClassName);
                
        if (cachedPossibleChildren != null) {
            for (ClassMetadata cachedPossibleChild : cachedPossibleChildren) {
                if (!(ignoreAbstract && cachedPossibleChild.isAbstract()))
                    classMetadataResultList.add(cachedPossibleChild);
            }
            return classMetadataResultList;
        }
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode;
            
            if (parentClassName == null || parentClassName.equals(Constants.NODE_DUMMYROOT))
                parentNode = connectionManager.getConnectionHandler().findNode(Label.label(Constants.LABEL_SPECIAL_NODE), Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            else
                parentNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, parentClassName);
            
            tx.success();
            
            if(!ignoreAbstract)
                return refreshPossibleChildren(parentNode);
            else{
                List<ClassMetadataLight> refreshedPossibleChildren = refreshPossibleChildren(parentNode);
                for (ClassMetadataLight possibleChild : refreshedPossibleChildren) {
                    if (!(ignoreAbstract && possibleChild.isAbstract()))
                        classMetadataResultList.add(possibleChild);
                }
                return classMetadataResultList;
            }
        }
    }
    
    @Override
    public List<ClassMetadataLight> getPossibleSpecialChildren(String parentClassName) 
            throws MetadataObjectNotFoundException   {
        
        List<ClassMetadataLight> classMetadataResultList = new ArrayList<>();
        
        List<String> cachedPossibleSpecialChildren = cm.getPossibleSpecialChildren(parentClassName);
                
        if (cachedPossibleSpecialChildren != null) {
            for (String cachedPossibleChild : cachedPossibleSpecialChildren)
                classMetadataResultList.add(cm.getClass(cachedPossibleChild));
            return classMetadataResultList;
        }
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode; 
            
            if (parentClassName == null || parentClassName.equals(Constants.NODE_DUMMYROOT))
                parentNode = connectionManager.getConnectionHandler().findNode(Label.label(Constants.LABEL_SPECIAL_NODE), Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            else
                parentNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, parentClassName);
                        
            tx.success();
            return refreshPossibleSpecialChildren(parentNode);
        }
    }

    @Override
    public List<ClassMetadataLight> getPossibleChildrenNoRecursive(String parentClassName) 
            throws MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode;
            
            if (parentClassName == null || parentClassName.equals(Constants.NODE_DUMMYROOT))
                parentNode = connectionManager.getConnectionHandler().findNode(Label.label(Constants.LABEL_SPECIAL_NODE), Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            else
                parentNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, parentClassName);
            
            Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.POSSIBLE_CHILD, Direction.OUTGOING);
            
            List<ClassMetadataLight> classMetadataListResult = new ArrayList<>();
            
            for (Relationship rel: relationships) {
                    Node child = rel.getEndNode();
                    classMetadataListResult.add(Util.createClassMetadataFromNode(child));
            }
            
            return classMetadataListResult;
        }
    }
    
    @Override
    public List<ClassMetadataLight> getPossibleSpecialChildrenNoRecursive(String parentClassName) 
            throws MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode;
            
            if (parentClassName == null || parentClassName.equals(Constants.NODE_DUMMYROOT))
                parentNode = connectionManager.getConnectionHandler().findNode(Label.label(Constants.LABEL_SPECIAL_NODE), Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            else
                parentNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, parentClassName);
            
            Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.POSSIBLE_SPECIAL_CHILD, Direction.OUTGOING);
            
            List<ClassMetadataLight> classMetadataListResult = new ArrayList<>();
            
            for (Relationship rel: relationships) {
                Node child = rel.getEndNode();
                classMetadataListResult.add(Util.createClassMetadataFromNode(child));
            }
            
            return classMetadataListResult;
        }
    }
    
    
    @Override
    public boolean canBeChild(String allegedParent, String childToBeEvaluated) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> possibleChildren = getPossibleChildren(allegedParent, true);

        for (ClassMetadataLight possibleChild : possibleChildren) {
            if (possibleChild.getName().equals(childToBeEvaluated))
                return true;
        }
        return false;
    }
    
    @Override
    public boolean canBeSpecialChild(String allegedParent, String childToBeEvaluated) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> possibleSpecialChildren = getPossibleSpecialChildren(allegedParent);

        for (ClassMetadataLight possibleSpecialChild : possibleSpecialChildren) {
            if (possibleSpecialChild.getName().equals(childToBeEvaluated))
                return true;
        }
        return false;
    }

    @Override
    public void addPossibleChildren(long parentClassId, long[] possibleChildren)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        Node parentNode;
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if(parentClassId != -1) {
                parentNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, parentClassId);

                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            ts.getTranslatedString("module.general.messages.class-id-not-found"), parentClassId));
                
                if (parentNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)parentNode.getProperty(Constants.PROPERTY_ABSTRACT))
                    throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.mem.messages.abstract-no-containment-hierarchy"));
                
                if (!isSubclassOf(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format(ts.getTranslatedString("module.containmentman.class.not-business-class")
                                    , (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
            } else // The navigation tree root
                parentNode = connectionManager.getConnectionHandler().findNode(Label.label(Constants.LABEL_SPECIAL_NODE), Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            List<ClassMetadataLight> currentPossibleChildren = refreshPossibleChildren(parentNode);
            
            for (long id : possibleChildren) {
                Node childNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, id);
                if (childNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            ts.getTranslatedString("module.general.messages.parent-class-id-not-found"), parentClassId));
                
                String newPossibleChildrenClassName = (String)childNode.getProperty(Constants.PROPERTY_NAME);
                String parentClassName = (String)parentNode.getProperty(Constants.PROPERTY_NAME);
                
                if (!isSubclassOf(Constants.CLASS_INVENTORYOBJECT, newPossibleChildrenClassName))
                    throw new InvalidArgumentException(
                            String.format(ts.getTranslatedString("module.containmentman.class.not-business-class")
                                    , newPossibleChildrenClassName));
                
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleChildren){
                            if (possibleChild.getId() == subclassNode.getId())
                                throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.containmentman.subclass.already-possible-child"), 
                                        newPossibleChildrenClassName, parentClassName));
                       }
                   }
                }
                else {
                    for (ClassMetadataLight possibleChild : currentPossibleChildren) {
                        if (possibleChild.getId() == childNode.getId())
                            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.containmentman.class.already-possible-child"), 
                                    newPossibleChildrenClassName, parentClassName));
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_CHILD);               
                
                //Refresh cache
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)) {
                    for (Node subClassNode : Util.getAllSubclasses(childNode)) 
                        cm.putPossibleChild(parentClassName, Util.createClassMetadataFromNode(subClassNode));
                } else
                    cm.putPossibleChild(parentClassName, Util.createClassMetadataFromNode(childNode));
            }
            tx.success();
        }
    }
    
    @Override
    public void addPossibleSpecialChildren(long parentClassId, long[] possibleSpecialChildren)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode;
            if(parentClassId != -1) {          
                parentNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, parentClassId);

                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            ts.getTranslatedString("module.general.messages.parent-class-id-not-found"), parentClassId));
                
                if (parentNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)parentNode.getProperty(Constants.PROPERTY_ABSTRACT))
                    throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.mem.messages.abstract-no-containment-hierarchy"));
                
                if (!isSubclassOf(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format(ts.getTranslatedString("module.containmentman.class.not-business-class")
                                    , (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
            } else 
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.mem.messages.nav-tree-root-cant-special-children"));

            List<ClassMetadataLight> currentPossibleSpecialChildren = refreshPossibleSpecialChildren(parentNode);
            
            for (long id : possibleSpecialChildren) {
                Node childNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, id);

                if (childNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            ts.getTranslatedString("module.general.messages.parent-class-id-not-found"), parentClassId));
                
                if (!isSubclassOf(Constants.CLASS_INVENTORYOBJECT, (String)childNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format(ts.getTranslatedString("module.containmentman.class.not-business-class")
                                    , (String)childNode.getProperty(Constants.PROPERTY_NAME)));
                
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleSpecialChild : currentPossibleSpecialChildren){
                            if (possibleSpecialChild.getId() == subclassNode.getId())
                                throw new InvalidArgumentException(String.format(
                                        ts.getTranslatedString("module.containmentman.subclass.already-possible-special-child")
                                        , (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                       }
                   }
                }
                else {
                    for (ClassMetadataLight possibleSpecialChild : currentPossibleSpecialChildren){
                        if (possibleSpecialChild.getId() == childNode.getId())
                            throw new InvalidArgumentException(String.format(
                                    ts.getTranslatedString("module.containmentman.class.already-possible-special-child")
                                    , (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_SPECIAL_CHILD);
                                
                //Refresh cache
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                    for (Node subClassNode : Util.getAllSubclasses(childNode)) {
                        if (!(boolean)subClassNode.getProperty(Constants.PROPERTY_ABSTRACT))
                            cm.putPossibleSpecialChild((String) parentNode.getProperty(Constants.PROPERTY_NAME), (String) subClassNode.getProperty(Constants.PROPERTY_NAME));
                    }
                } else
                    cm.putPossibleSpecialChild((String)parentNode.getProperty(Constants.PROPERTY_NAME), (String)childNode.getProperty(Constants.PROPERTY_NAME));
                
                tx.success();
            }
        }
    }

    @Override
    public void addPossibleChildren(String parentClassName, String[] possibleChildren) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        Node parentNode;

        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if(parentClassName != null) {
                parentNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, parentClassName);

                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(
                            String.format(ts.getTranslatedString("module.general.messages.parent-class-not-found"), parentClassName));
                
                if (parentNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)parentNode.getProperty(Constants.PROPERTY_ABSTRACT))
                    throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.mem.messages.abstract-no-containment-hierarchy"));
                
                if (!isSubclassOf(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format(ts.getTranslatedString("module.containmentman.class.not-business-class")
                                    , (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
            } else { // The navigation tree root
                parentNode = connectionManager.getConnectionHandler().findNode(Label.label(Constants.LABEL_SPECIAL_NODE), Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);

                if(!(Constants.NODE_DUMMYROOT).equals((String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                        throw new MetadataObjectNotFoundException(ts.getTranslatedString("module.general.messages.error-dummy-root"));
            }
            List<ClassMetadataLight> currentPossibleChildren = refreshPossibleChildren(parentNode);
        
            for (String possibleChildName : possibleChildren) {
                
                Node childNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, possibleChildName);
                
                if (childNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            ts.getTranslatedString("module.general.messages.child-class-not-found"), possibleChildName));
                
                if (!isSubclassOf(Constants.CLASS_INVENTORYOBJECT, (String)childNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format(ts.getTranslatedString("module.containmentman.class.not-business-class")
                                    , (String)childNode.getProperty(Constants.PROPERTY_NAME)));
                
                if ((boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)) {
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleChildren) {
                            if (possibleChild.getId() == subclassNode.getId())
                                throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.containmentman.subclass.already-possible-child")
                                        , (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                       }
                   }
                }
                else {
                    for (ClassMetadataLight possibleChild : currentPossibleChildren) {
                        if (possibleChild.getId() == childNode.getId())
                            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.containmentman.class.already-possible-child")
                                    , (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_CHILD);
                
                //Refresh cache
                if ((boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)) {
                    for(ClassMetadata subclass : getSubClasses(childNode, false))
                        cm.putPossibleChild(parentClassName, subclass);
                }
                else
                    cm.putPossibleChild(parentClassName, Util.createClassMetadataFromNode(childNode));
                tx.success();
            }
        }
    }
    
    @Override
    public void addPossibleSpecialChildren(String parentClassName, String[] possibleSpecialChildren) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        Node parentNode;

        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if(parentClassName != null) {
                parentNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, parentClassName);

                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.parent-class-not-found"), parentClassName));
                
                if (parentNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)parentNode.getProperty(Constants.PROPERTY_ABSTRACT))
                    throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.mem.messages.abstract-no-containment-hierarchy"));
                
                if (!isSubclassOf(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format(ts.getTranslatedString("module.containmentman.class.not-business-class")
                                    , (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
            } else 
                throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.mem.messages.nav-tree-root-cant-special-children"));
            
            List<ClassMetadataLight> currentPossibleSpecialChildren = refreshPossibleSpecialChildren(parentNode);
        
            for (String possibleSpecialChildName : possibleSpecialChildren) {
                Node childNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, possibleSpecialChildName);
                
                if (childNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            ts.getTranslatedString("module.general.messages.child-class-not-found"), possibleSpecialChildName));
                
                if (!isSubclassOf(Constants.CLASS_INVENTORYOBJECT, (String)childNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format(ts.getTranslatedString("module.containmentman.class.not-business-class")
                                    , (String)childNode.getProperty(Constants.PROPERTY_NAME)));
                
                if ((boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)) {
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleSpecialChildren) {
                            if (possibleChild.getId() == subclassNode.getId())
                                throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.containmentman.subclass.already-possible-special-child")
                                        , (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                       }
                   }
                }
                else {
                    for (ClassMetadataLight possibleSpecialChild : currentPossibleSpecialChildren) {
                        if (possibleSpecialChild.getId() == childNode.getId())
                            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.containmentman.class.already-possible-special-child")
                                    , (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_SPECIAL_CHILD);
                
                //Refresh cache
                if ((boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)) {
                    for(ClassMetadataLight subclass : getSubClassesLight((String)childNode.getProperty(Constants.PROPERTY_NAME), false, false))
                        cm.putPossibleSpecialChild(parentClassName,subclass.getName());
                }
                else
                    cm.putPossibleSpecialChild(parentClassName, (String)childNode.getProperty(Constants.PROPERTY_NAME));
                
                tx.success();
            }
        }
    }
    
    @Override
    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved) 
            throws MetadataObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode;
            if (parentClassId == -1){
                parentNode = connectionManager.getConnectionHandler().findNode(Label.label(Constants.LABEL_SPECIAL_NODE), Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
                
                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(ts.getTranslatedString("module.general.messages.error-dummy-root"));
            }
            else {
                parentNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, parentClassId);
                
                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            ts.getTranslatedString("module.general.messages.class-id-not-found"), parentClassId));
            }
            for (long id : childrenToBeRemoved) {
                Node childNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, id);
                
                Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.POSSIBLE_CHILD, Direction.OUTGOING);

                for (Relationship rel: relationships) {
                    Node possiblechild = rel.getEndNode();
                    if (childNode.getId() == possiblechild.getId())
                        rel.delete();
                }//end for
            }//end for
            refreshPossibleChildren(parentNode);
            tx.success();
        }
    }
    
    @Override
    public void removePossibleSpecialChildren(long parentClassId, long[] specialChildrenToBeRemoved) 
            throws MetadataObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode;
            if (parentClassId == -1){
                parentNode = connectionManager.getConnectionHandler().findNode(Label.label(Constants.LABEL_SPECIAL_NODE), Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
                
                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(ts.getTranslatedString("module.general.messages.error-dummy-root"));
            }
            else {
                parentNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, parentClassId);
                
                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            ts.getTranslatedString("module.general.messages.class-id-not-found"), parentClassId));
            }
            for (long id : specialChildrenToBeRemoved) {
                Node childNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, id);
                
                Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.POSSIBLE_SPECIAL_CHILD, Direction.OUTGOING);

                for (Relationship rel: relationships) {
                    Node possiblechild = rel.getEndNode();
                    if (childNode.getId() == possiblechild.getId())
                        rel.delete();
                }//end for
            }//end for
            refreshPossibleSpecialChildren(parentNode);
            tx.success();
        }
        
    }

    @Override
    public List<ClassMetadataLight> getUpstreamContainmentHierarchy(String className, 
            boolean recursive) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> res = new ArrayList<>();
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null)
               throw new MetadataObjectNotFoundException(String.format(
                            ts.getTranslatedString("module.general.messages.class-not-found"), className));
            
            String cypherQuery = "MATCH (possibleParentClassNode:classes)-[:POSSIBLE_CHILD" + (recursive ? "*" : "") + "]->(classNode:classes) "+
                                 "WHERE classNode.name = \"" + className + "\" "+
                                 "AND possibleParentClassNode.name <> \"" + Constants.NODE_DUMMYROOT + "\" "+
                                 "RETURN DISTINCT possibleParentClassNode " +
                                 "ORDER BY possibleParentClassNode.name ASC";

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);

            Iterator<Node> directPossibleChildren = result.columnAs("possibleParentClassNode"); //NOI18N
            for (Node node : Iterators.asIterable(directPossibleChildren))
                res.add(Util.createClassMetadataLightFromNode(node));
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<ClassMetadataLight> getUpstreamSpecialContainmentHierarchy(String className, 
            boolean recursive) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> res = new ArrayList<>();
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null)
               throw new MetadataObjectNotFoundException(String.format(
                            ts.getTranslatedString("module.general.messages.class-not-found"), className));
            
            String cypherQuery = "MATCH (possibleParentClassNode:classes)-[:POSSIBLE_SPECIAL_CHILD"+ (recursive ? "*" : "") + "]->(classNode:classes) "+
                                 "WHERE classNode.name = \""+ className + "\" " +
                                 "AND possibleParentClassNode.name <> \""+ Constants.NODE_DUMMYROOT +"\" " +
                                 "RETURN distinct possibleParentClassNode "+
                                 "ORDER BY possibleParentClassNode.name ASC";

            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);

            Iterator<Node> directPossibleChildren = result.columnAs("possibleParentClassNode"); //NOI18N
            for (Node node : Iterators.asIterable(directPossibleChildren))
                res.add(Util.createClassMetadataLightFromNode(node));
        }
        return res;
    }

    @Override
    public List<ClassMetadataLight> getUpstreamClassHierarchy(String className, boolean includeSelf) throws MetadataObjectNotFoundException {
        getClass(className); //Checks if the class exists
        
        //Let's check the cache first
        List<ClassMetadataLight> res = cm.getUpstreamClassHierarchy(className);
        
        if (res != null)
            return res;
            
        res = new ArrayList<>();
        try(Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            //Without the ORDER BY/LIMIT clauses, this query (oddly) the main path and also its parts separately, so we only take the longest path
            String cypherQuery = "MATCH paths = (sourceClass:classes)-[:EXTENDS*]->(rootClass) WHERE sourceClass.name = " +  //NOI18N
                    "{className} AND (rootClass.name = 'InventoryObject' or rootClass.name = 'GenericObjectList') WITH nodes(paths) AS classHierarchy " + //NOI18N
                    "RETURN classHierarchy ORDER BY length(classHierarchy) DESC LIMIT 1"; //NOI18N
            
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("className", className); //NOI18N
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery, parameters);
            
            ResourceIterator<ArrayList<Node>> classHierarchyPath = result.columnAs("classHierarchy");
            
            if (classHierarchyPath.hasNext()) {
                for (Node aClassNode : classHierarchyPath.next()){
                    if(includeSelf && aClassNode.getProperty(Constants.PROPERTY_NAME).equals(className))
                        res.add(Util.createClassMetadataLightFromNode((Node)aClassNode));
                    else if(!aClassNode.getProperty(Constants.PROPERTY_NAME).equals(className))
                        res.add(Util.createClassMetadataLightFromNode((Node)aClassNode));
                }
            }
                        
            //Cache the result and return
            cm.addUpstreamClassHierarchy(className, res);
            tx.success();
        }
        return res;
    }
    
    @Override
    public void setSpecialRelationshipDisplayName(String relationshipName, String relationshipDisplayName) {
        relationshipDisplayNames.put(relationshipName, relationshipDisplayName);
    }
    
    @Override
    public String getSpecialRelationshipDisplayName(String relationshipName) {
        String displayName = relationshipDisplayNames.get(relationshipName);
        return displayName == null ? relationshipName : displayName;
    }
     
    @Override
    public boolean isSubclassOf(String allegedParent, String classToBeEvaluated) throws MetadataObjectNotFoundException {
        if (classToBeEvaluated == null)
            return false;

        if (allegedParent.equals(classToBeEvaluated))
            return true;

        ClassMetadata currentClass = getClass(classToBeEvaluated);
        
        if (currentClass.getParentClassName() == null)
            return false;

        if (currentClass.getParentClassName().equals(allegedParent))
            return true;
        else //Search recursively
            return isSubclassOf(allegedParent, currentClass.getParentClassName());
    }
    
    @Override
    public List<AttributeMetadata> getMandatoryAttributesInClass(String className) 
            throws MetadataObjectNotFoundException
    {
        List<AttributeMetadata> classAttributes = new ArrayList<>(getClass(className).getAttributes());
        List<AttributeMetadata> mandatoryAttributes = new ArrayList<>();
        classAttributes.forEach(attribute -> { 
            if(attribute.isMandatory())
                mandatoryAttributes.add(attribute); 
        });
        return mandatoryAttributes;
    }
    
    /**
     * HELPERS
     */
    /**
     * 
     * @param classNode
     * @param attributeId
     * @param newProperties
     * @return
     * @throws InvalidArgumentException
     * @throws MetadataObjectNotFoundException 
     */
    public ChangeDescriptor setAttributeProperties(Node classNode, long attributeId, HashMap<String, Object> newProperties) 
            throws InvalidArgumentException, MetadataObjectNotFoundException {
        String affectedProperties = "", oldValues = "", newValues = "";

        for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
            Node attrNode = relationship.getEndNode();
            if (attrNode.getId() == attributeId) {
                String currentAttributeName = (String)attrNode.getProperty(Constants.PROPERTY_NAME);

                for (String aProperty : newProperties.keySet()) {
                    switch (aProperty) {
                        case Constants.PROPERTY_NAME:
                            if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                                throw new InvalidArgumentException("Attribute \"name\" can not be renamed");
                            if (!((String)newProperties.get(aProperty)).matches("^[a-zA-Z0-9_]*$"))
                                throw new InvalidArgumentException(String.format("Attribute name contains invalid characters"));

                            Util.changeAttributeName(classNode, currentAttributeName, (String)newProperties.get(aProperty));

                            affectedProperties += Constants.PROPERTY_NAME + " ";
                            oldValues += currentAttributeName + " ";
                            newValues += newProperties.get(aProperty) + " ";
                            break;
                        case Constants.PROPERTY_DESCRIPTION:
                            Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DESCRIPTION, 
                                    (String)newProperties.get(aProperty));
                            affectedProperties = Constants.PROPERTY_DESCRIPTION + " ";
                            oldValues += currentAttributeName + " ";
                            newValues += newProperties.get(aProperty) + " ";
                            break;
                        case Constants.PROPERTY_DISPLAY_NAME:
                            oldValues += attrNode.getProperty(Constants.PROPERTY_DISPLAY_NAME) + " ";
                            Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DISPLAY_NAME, (String)newProperties.get(aProperty));
                            affectedProperties = Constants.PROPERTY_DISPLAY_NAME + " ";
                            newValues += newProperties.get(aProperty) + " ";
                            break;
                        case Constants.PROPERTY_TYPE:
                            if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                                throw new InvalidArgumentException("Attribute \"name\" can only be a String");
                            oldValues += attrNode.getProperty(Constants.PROPERTY_TYPE) + " ";

                            if (AttributeMetadata.isPrimitive((String)attrNode.getProperty(Constants.PROPERTY_TYPE)))
                                Util.changeAttributeTypeIfPrimitive(classNode, currentAttributeName, (String)newProperties.get(aProperty));
                            else
                                Util.changeAttributeTypeIfListType(classNode, currentAttributeName, (String)newProperties.get(aProperty));

                            affectedProperties = Constants.PROPERTY_TYPE + " ";
                            newValues += newProperties.get(aProperty) + " ";
                            break;
                        case Constants.PROPERTY_READ_ONLY:
                            oldValues += " " + (attrNode.hasProperty(Constants.PROPERTY_READ_ONLY) ? attrNode.getProperty(Constants.PROPERTY_READ_ONLY) : "");
                            Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_READ_ONLY, (boolean)newProperties.get(aProperty));
                            affectedProperties = Constants.PROPERTY_READ_ONLY + " ";
                            newValues += newProperties.get(aProperty) + " ";
                            break;
                        case Constants.PROPERTY_VISIBLE:
                            oldValues += " " + (attrNode.hasProperty(Constants.PROPERTY_VISIBLE) ? attrNode.getProperty(Constants.PROPERTY_VISIBLE) : "");
                            Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_VISIBLE, (boolean)newProperties.get(aProperty));
                            affectedProperties = Constants.PROPERTY_VISIBLE + " ";
                            newValues += newProperties.get(aProperty) + " ";
                            break;
                        case Constants.PROPERTY_ADMINISTRATIVE:
                            oldValues += " " + (attrNode.hasProperty(Constants.PROPERTY_ADMINISTRATIVE) ? attrNode.getProperty(Constants.PROPERTY_ADMINISTRATIVE) : "");
                            Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_ADMINISTRATIVE, (boolean)newProperties.get(aProperty));
                            affectedProperties = Constants.PROPERTY_ADMINISTRATIVE + " ";
                            newValues += newProperties.get(aProperty) + " ";
                            break;
                        case Constants.PROPERTY_NO_COPY:
                            oldValues += " " + (attrNode.hasProperty(Constants.PROPERTY_NO_COPY) ? attrNode.getProperty(Constants.PROPERTY_NO_COPY) : "");
                            Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_NO_COPY, (boolean)newProperties.get(aProperty));
                            affectedProperties = Constants.PROPERTY_NO_COPY + " ";
                            newValues += (boolean)newProperties.get(aProperty) + " ";
                            break;
                        case Constants.PROPERTY_UNIQUE:
                            oldValues += " " + (attrNode.hasProperty(Constants.PROPERTY_UNIQUE) ? attrNode.getProperty(Constants.PROPERTY_UNIQUE) : "");
                            if((boolean)newProperties.get(aProperty)) { // Do additional validations only if unique changed from false to true
                                String attributeType = (String) attrNode.getProperty(Constants.PROPERTY_TYPE);
                                if (attributeType.equals("Boolean") || !AttributeMetadata.isPrimitive(attributeType))
                                    throw new InvalidArgumentException("Boolean and list type attributes can not be set as unique");

                                if(canAttributeBeUnique(classNode, currentAttributeName))
                                    Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_UNIQUE, newProperties.get(aProperty));
                                else
                                    throw new InvalidArgumentException(String.format("There are duplicated values of attribute \"%s\" among the existing instances of class %s or its subclasses", 
                                            currentAttributeName, classNode.getProperty(Constants.PROPERTY_NAME)));
                            }
                            else
                                Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_UNIQUE, newProperties.get(aProperty));

                            affectedProperties = Constants.PROPERTY_UNIQUE + " ";
                            newValues += newProperties.get(aProperty) + " ";
                            break;
                        case Constants.PROPERTY_MANDATORY:
                            oldValues += " " + (attrNode.hasProperty(Constants.PROPERTY_MANDATORY) ? attrNode.getProperty(Constants.PROPERTY_MANDATORY) : "");
                            if((boolean)newProperties.get(aProperty)) { // Do additional validations only if mandatory changed from false to true
                                // This checks if every object of the class and subclasses has a value in this attribute marked as mandatory
                                if(objectsOfClassHasValueInMandatoryAttribute((String)classNode.getProperty(Constants.PROPERTY_NAME), currentAttributeName))
                                    Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_MANDATORY, 
                                            newProperties.get(aProperty));
                                else
                                    throw new InvalidArgumentException(String.format("Before setting this attribute as mandatory, all existing instances of the class must have valid values for attribute %s", 
                                            currentAttributeName));
                            }
                            else
                                Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_MANDATORY, newProperties.get(aProperty));

                            affectedProperties = Constants.PROPERTY_MANDATORY + " ";
                            newValues += newProperties.get(aProperty) + " ";
                            break;
                        case Constants.PROPERTY_ORDER:
                            if (!(newProperties.get(aProperty) instanceof Integer))
                                throw new InvalidArgumentException("Property order must be an integer");
                            oldValues += " " + (attrNode.hasProperty(Constants.PROPERTY_ORDER) ? attrNode.getProperty(Constants.PROPERTY_ORDER) : "N/A");
                            Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_ORDER, newProperties.get(aProperty));
                            affectedProperties = Constants.PROPERTY_ORDER + " ";
                            newValues += newProperties.get(aProperty) + " ";
                            break;
                        case Constants.PROPERTY_MULTIPLE:
                            if (AttributeMetadata.isPrimitive((String) attrNode.getProperty(Constants.PROPERTY_TYPE)))
                                throw new InvalidArgumentException("Primitive types can not be set as multiple");
                            oldValues += " " + (attrNode.hasProperty(Constants.PROPERTY_MULTIPLE) ? attrNode.getProperty(Constants.PROPERTY_MULTIPLE) : false);
                            Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_MULTIPLE, (boolean)newProperties.get(aProperty));
                            affectedProperties = Constants.PROPERTY_MULTIPLE + " ";
                            newValues += newProperties.get(aProperty) + " ";
                            break;
                        default:
                            throw new IllegalArgumentException(String.format("Property %s is unknown or can not be changed", aProperty));
                    }
                }

                //Refresh cache for the affected classes
                refreshCacheOn(classNode);
                return new ChangeDescriptor(affectedProperties.trim(), oldValues.trim(), newValues.trim(), String.format("Update attribute properties of class %s", classNode.getProperty(Constants.PROPERTY_NAME)));
            }
        }
        throw new MetadataObjectNotFoundException(String.format(
                    "Attribute with id %s in class %s could not be found", attributeId, classNode.getProperty(Constants.PROPERTY_NAME)));
    }
    
    
    //Callers must handle associated transactions
   private void refreshCacheOn(Node rootClassNode){
        TraversalDescription UPDATE_TRAVERSAL = connectionManager.getConnectionHandler().traversalDescription().
                    breadthFirst().relationships(RelTypes.EXTENDS, Direction.INCOMING);

        for(Path p : UPDATE_TRAVERSAL.traverse(rootClassNode)) {
            cm.removeClass((String)p.endNode().getProperty(Constants.PROPERTY_NAME));
            ClassMetadata rootClassMetadata = Util.createClassMetadataFromNode(p.endNode());
            cm.putClass(rootClassMetadata);            
                        
            refreshPossibleChildren(p.endNode());
            refreshPossibleSpecialChildren(p.endNode());
        }
    }
    @Override
    //Callers must handle associated transactions
    public void buildClassCache() throws InvalidArgumentException {
        cm.clearClassCache();
        ResourceIterator<Node> classes = connectionManager.getConnectionHandler().findNodes(classLabel);
        while (classes.hasNext()) {
            Node classNode = classes.next();
            
            ClassMetadata aClass = Util.createClassMetadataFromNode(classNode);
            cm.putClass(aClass);

            refreshPossibleChildren(classNode);
            refreshPossibleSpecialChildren(classNode);
        }
        loadUniqueAttributesCache();
        //Only the DummyRoot is not cached. It will be cached on demand later
    }
   
    private List<ClassMetadataLight> refreshPossibleChildren(Node classNode) {
        String className = (String) classNode.getProperty(Constants.PROPERTY_NAME);

        if (cm.getPossibleChildren(className) != null)
            cm.getPossibleChildren(className).clear();
        else
            cm.putPossibleChildren(className, new ArrayList());

        List<ClassMetadataLight> possibleChildren = new ArrayList();

        for (Relationship relationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.POSSIBLE_CHILD)) {
            if ((Boolean) relationship.getEndNode().getProperty(Constants.PROPERTY_ABSTRACT)) {
                Iterable<Node> allSubclasses = Util.getAllSubclasses(relationship.getEndNode());
                for (Node childNode : allSubclasses) {
                    ClassMetadata aSubclass = Util.createClassMetadataFromNode(childNode);
                    cm.putPossibleChild(className, aSubclass);
                    possibleChildren.add(aSubclass);
               }
            } else {
                ClassMetadata aSubclass = Util.createClassMetadataFromNode(relationship.getEndNode());
                cm.putPossibleChild(className, aSubclass);
                possibleChildren.add(aSubclass);
           }
       }
       return possibleChildren;
    }
    
    private List<ClassMetadataLight> refreshPossibleSpecialChildren(Node classNode) {
        String className = (String) classNode.getProperty(Constants.PROPERTY_NAME);
        
        if (cm.getPossibleSpecialChildren(className) != null)
            cm.getPossibleSpecialChildren(className).clear();
        else
            cm.putPossibleSpecialChildren(className, new ArrayList());
        
        List<ClassMetadataLight> possibleSpecialChildren = new ArrayList();
        
        for (Relationship relationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.POSSIBLE_SPECIAL_CHILD)) {
            
            if ((Boolean) relationship.getEndNode().getProperty(Constants.PROPERTY_ABSTRACT)) {
                Iterable<Node> allSubclasses = Util.getAllSubclasses(relationship.getEndNode());
                for (Node childNode : allSubclasses) {
                    if (!(Boolean) childNode.getProperty(Constants.PROPERTY_ABSTRACT)) {
                        cm.putPossibleSpecialChild(className, (String) childNode.getProperty(Constants.PROPERTY_NAME));
                        possibleSpecialChildren.add(Util.createClassMetadataLightFromNode(childNode));
                    }
                }
            } else {
                cm.putPossibleSpecialChild(className, (String) relationship.getEndNode().getProperty(Constants.PROPERTY_NAME));
                possibleSpecialChildren.add(Util.createClassMetadataLightFromNode(relationship.getEndNode()));
            }
       }
       return possibleSpecialChildren;
   }
   
   
   /**
    * Checks if all the objects of a given class have a value in a given attribute marked as mandatory.
    * This method also checks all the instances of the subclasses of the given class. This check is made before setting an attribute as mandatory (no empty values are allowed in existing objects, as this would violate the mandatory constraint)
    * @param className The object's class 
    * @param attributeName The object's attribute marked as mandatory
    * @param recursive false: if the method should evaluate all the objects of 
    * the class, true if also should evaluate all the objects of the subclasses
    * @return true if every object has a value in the attribute, false if at 
    * least one object has no value in the attribute marked as mandatory
    */
    private boolean objectsOfClassHasValueInMandatoryAttribute(String className, 
            String attributeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException
    {
        Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
        
        String attributeType = Util.createClassMetadataFromNode(classNode).getAttribute(attributeName).getType();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found. Contact your administrator.", className));
        
        if (!objectsHasAttribute(classNode, attributeName, attributeType))// check every object for the given class
            return false;
        //then check every object of the subclasses of the given class
        String cypherQuery = "MATCH (inventory:classes) <-[:".concat(RelTypes.EXTENDS.toString()).concat("*]-(classmetadata) ").concat(
                             "WHERE inventory.name = \""+ className + "\" ").concat(
                             "RETURN classmetadata ").concat(
                             "ORDER BY classmetadata.name ASC");

        Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
        Iterator<Node> n_column = result.columnAs("classmetadata");
        for (Node nodeClass : Iterators.asIterable(n_column))
            return objectsHasAttribute(nodeClass, attributeName, attributeType);
        
        return true;
    }
    
    /**
     * Checks if all the instances of a class has a value in a given attribute
     * @param classNode the class node
     * @param attributeName name of the attribute
     * @param attributeType type of the attribute
     * @return true if the given attribute has a value
     * @throws InvalidArgumentException 
     */
    private boolean objectsHasAttribute(Node classNode, String attributeName, String attributeType) throws InvalidArgumentException{
        
        boolean everyObjectHasValue = true;
        Iterable<Relationship> iterableInstances = classNode.getRelationships(RelTypes.INSTANCE_OF, Direction.INCOMING);
        Iterator<Relationship> instances = iterableInstances.iterator();
        //check if attribute is a pirmitive type, a property in the object node
        while (instances.hasNext()){
            Node objectNode = instances.next().getStartNode();
            if(AttributeMetadata.isPrimitive(attributeType)){
                if(!objectNode.hasProperty(attributeName)){
                    everyObjectHasValue = false;
                    break;    
                }
            }
            else{//checks if the attribute is a list type property
                //Iterates through relationships and transform the into "plain" attributes
                everyObjectHasValue = false;
                Iterable<Relationship> iterableRelationships = objectNode.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING);
                Iterator<Relationship> relationships = iterableRelationships.iterator();
                if(!relationships.hasNext())//if has no attribute
                    return false;
                else{
                    while(relationships.hasNext()){
                        Relationship relationship = relationships.next();
                        if (!relationship.hasProperty(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException(String.format("Mandatory Attribute Missing: The object with id %s is malformed", objectNode.getId()));

                        if (attributeName.equals((String)relationship.getProperty(Constants.PROPERTY_NAME)))
                            everyObjectHasValue = true;
                    }
                }
            }
        }
        return everyObjectHasValue;
    }
     
    
    /**
     * Checks if the attribute can be unique 
     * @param classNode node of the class to which the unique attribute will be set
     * @param attributeName name of the attribute that will be set as unique
     * @return false if there are not unique values in the attribute of the objects created
     * true if the attribute i s empty or are unique
     */
    private boolean canAttributeBeUnique(Node classNode, String attributeName) {
        String className = (String)classNode.getProperty(Constants.PROPERTY_NAME);
        
        if (classNode.hasRelationship(Direction.INCOMING, RelTypes.EXTENDS)) {
            //First we check the instances of the subclasses 
            String cypherQuery = String.format("MATCH (instance)-[:INSTANCE_OF]->(subclass)-[:EXTENDS*]->(class:classes) WHERE class.name='%s' AND EXISTS(instance.%s) WITH instance.%s as attributeValue, collect(instance) as matchingNodes WHERE SIZE(matchingNodes) > 1 RETURN matchingNodes", className, attributeName, attributeName);
            if (connectionManager.getConnectionHandler().execute(cypherQuery).hasNext())
                return false;
            
            //Then the class itself
            cypherQuery = String.format("MATCH (instance)-[:INSTANCE_OF]->(class:classes) WHERE class.name='%s' AND EXISTS(instance.%s) WITH instance.%s as attributeValue, collect(instance) as matchingNodes WHERE SIZE(matchingNodes) > 1 RETURN matchingNodes", className, attributeName, attributeName);
            return !connectionManager.getConnectionHandler().execute(cypherQuery).hasNext();
        } else {
            String cypherQuery = String.format("MATCH (instance)-[:INSTANCE_OF]->(class:classes) WHERE class.name='%s' AND EXISTS(instance.%s) WITH instance.%s as attributeValue, collect(instance) as matchingNodes WHERE SIZE(matchingNodes) > 1 RETURN matchingNodes", className, attributeName, attributeName);
            return !connectionManager.getConnectionHandler().execute(cypherQuery).hasNext();
        }        
        
    }
    
    /**
     * Loads into cache the unique attributes of every class and if there are 
     * instances of the classes also saves the values of the unique attributes
     * @throws InvalidArgumentException if the attribute name doesn't exists
     */
    private void loadUniqueAttributesCache() throws InvalidArgumentException{
        
        Node inventoryObject = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, Constants.CLASS_INVENTORYOBJECT);
        
        if (inventoryObject == null)
            return;
        
        TraversalDescription td = new MonoDirectionalTraversalDescription(); //TODO Check this!
        td = td.breadthFirst();
        td = td.relationships(RelTypes.EXTENDS, Direction.INCOMING);
        org.neo4j.graphdb.traversal.Traverser traverse = td.traverse(inventoryObject);
        
        for (Node subClassNode : traverse.nodes()) {
            ClassMetadata subClassMetadata = Util.createClassMetadataFromNode(subClassNode);
            for(AttributeMetadata attribute :subClassMetadata.getAttributes()){
                if(attribute.isUnique()){
                    if(subClassNode.hasRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.INCOMING) ||
                        subClassNode.hasRelationship(RelTypes.INSTANCE_OF, Direction.INCOMING)){
                        Iterable<Relationship> iterableRelationships = subClassNode.getRelationships(RelTypes.INSTANCE_OF, Direction.INCOMING);
                        Iterator<Relationship> relationships = iterableRelationships.iterator();
                        while(relationships.hasNext()) {
                            Node objectNode = relationships.next().getStartNode();
                            
                            if(objectNode.hasProperty(attribute.getName()))
                                cm.putUniqueAttributeValueIndex(subClassMetadata.getName(), 
                                        attribute.getName(), 
                                        String.valueOf(objectNode.getProperty(attribute.getName())));
                        }
                    }else//there are no created objects, but this attribute is unique
                        cm.putUniqueAttributeValueIndex(subClassMetadata.getName(), 
                                        attribute.getName(), null);
                }
            }
        }//end for 
    }
    
    /**
     * Fetches recursively all the subclasses in a light flavor of a given class without using a transaction.
     * @param classNode The class node to start the search
     * @param includeAbstractClasses should abstract classes be included in the result?
     * @return The list of the recursive subclasses, given the filters <code>includeAbstractClasses</code>
     */
    private List<ClassMetadataLight> getSubClassesLight(Node classNode, boolean includeAbstractClasses) {
        List<ClassMetadataLight> res = new ArrayList<>();
        for (Relationship inheritanceRelationship : classNode.getRelationships(Direction.INCOMING, RelTypes.EXTENDS)) {
            Node subClassNode = inheritanceRelationship.getStartNode();
            if (subClassNode.hasProperty(Constants.PROPERTY_ABSTRACT) && includeAbstractClasses) {
                res.add(Util.createClassMetadataLightFromNode(subClassNode));
                res.addAll(getSubClassesLight(subClassNode, includeAbstractClasses));
            }
        }
        return res;
    }
    
    /**
     * Fetches recursively all the subclasses of a given class without using a transaction and re
     * @param classNode The class node to start the search
     * @param includeAbstractClasses should abstract classes be included in the result?
     * @return The list of the recursive subclasses, given the filters <code>includeAbstractClasses</code>
     */
    private List<ClassMetadata> getSubClasses(Node classNode, boolean includeAbstractClasses) {
        List<ClassMetadata> res = new ArrayList<>();
        for (Relationship inheritanceRelationship : classNode.getRelationships(Direction.INCOMING, RelTypes.EXTENDS)) {
            Node subClassNode = inheritanceRelationship.getStartNode();
            
            if (subClassNode.hasProperty(Constants.PROPERTY_ABSTRACT) 
                    && !((Boolean) subClassNode.getProperty(Constants.PROPERTY_ABSTRACT) && includeAbstractClasses))
                res.add(Util.createClassMetadataFromNode(subClassNode));
                
            res.addAll(getSubClasses(subClassNode, includeAbstractClasses));
        }
        return res;
    }
}    
