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
package org.neotropic.kuwaiba.core.persistence.reference.neo4j.util;

import groovy.lang.GroovyClassLoader;
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
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.extras.caching.CacheManager;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides methods to map nodes into Java objects, mostly BusinessObject and BusinessObjectLight instances.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Service
public class ObjectGraphMappingService {
    /**
     * A class loader to place all the validator definition classes created on-the-fly.
     */
    private final GroovyClassLoader validatorDefinitionsClassLoader = new GroovyClassLoader();
    /**
     * Reference to the Connection Manager
     */
    @Autowired
    private ConnectionManager<GraphDatabaseService> connectionManager;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    
    private final String PREFIX = "api.service.object-graph-mapping";
    
    public BusinessObjectLight createObjectLightFromNode (Node instance) {
        Node classNode = instance.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode();
        String className = (String)classNode.getProperty(Constants.PROPERTY_NAME);
        
        //First, we create the naked business object, without validators
        BusinessObjectLight res = new BusinessObjectLight(className, (String)instance.getProperty(Constants.PROPERTY_UUID), 
                (String)instance.getProperty(Constants.PROPERTY_NAME), (String)classNode.getProperty(Constants.PROPERTY_DISPLAY_NAME, null));
        
        //Then, we check the cache for validator definitions
        List<ValidatorDefinition> validatorDefinitions = CacheManager.getInstance().getValidatorDefinitions(className);
        if (validatorDefinitions == null) { //Since the validator definitions are not cached, we retrieve them for the object class and its super classes
            validatorDefinitions = new ArrayList<>();
            try {
                List<ClassMetadataLight> classHierarchy = mem.getUpstreamClassHierarchy(className, true);
                //The query returns the hierarchy from the subclass to the super class, and we reverse it so the lower level validator definitions 
                //have a higher priority (that is, are processed the last)
                Collections.reverse(classHierarchy); 
                for (ClassMetadataLight aClass : classHierarchy) {
                    ResourceIterator<Node> validatorDefinitionNodes = connectionManager.getConnectionHandler().findNodes(Label.label(Constants.LABEL_VALIDATOR_DEFINITIONS), 
                            Constants.PROPERTY_CLASSNAME, 
                            aClass.getName());
                    
                    while (validatorDefinitionNodes.hasNext()) {
                        Node aValidatorDefinitionNode = validatorDefinitionNodes.next();
                        String script = (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_SCRIPT);
                        
                        if (!script.trim().isEmpty()) { //Empty scripts are ignored
                            try {
                                //We will load on-the-fly a ValidatorDefinition subclass and instantiate an object from it. The the signature class defined in the 
                                //script file should be something like "public class %s extends ValidatorDefinition" and implement the "run" mathod. The name of the class
                                //will be built dynamically based on the id of the validator definition and a fixed prefix. This is done so the user doesn't use accidentally a
                                //class name already in use by another validator definition.
                                String validatorDefinitionClassName = "ValidatorDefinition" + aValidatorDefinitionNode.getId();
                                Class validatorDefinitionClass = validatorDefinitionsClassLoader.parseClass(
                                        String.format(script, validatorDefinitionClassName, validatorDefinitionClassName));
                                
                                ValidatorDefinition validatorDefinitionInstance =  (ValidatorDefinition)validatorDefinitionClass.
                                        getConstructor(long.class, String.class, String.class, String.class, String.class, boolean.class).
                                        newInstance(aValidatorDefinitionNode.getId(), 
                                                (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_NAME), 
                                                (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                                                aClass.getName(), 
                                                (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_SCRIPT), 
                                                (boolean)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_ENABLED));

                                validatorDefinitions.add(validatorDefinitionInstance);
                            } catch (Exception ex) { //If there's an error parsing the script or instantiating the class, this validator definition will be ignored and the error logged
                                System.out.println(String.format("[KUWAIBA] %s", ex.getLocalizedMessage()));
                                //ex.printStackTrace();
                            }
                        }
                    }
                }
                
                //Now we cache the results
                CacheManager.getInstance().addValidatorDefinitions(className, validatorDefinitions);
            } catch (MetadataObjectNotFoundException ex) {
                //Should not happen
            }    
        }
        
        List<Validator> validators = new ArrayList<>();
        
        //Now we run the applicable validator definitions
        validatorDefinitions.forEach((aValidatorDefinition) -> {
            try {
                if (aValidatorDefinition.isEnabled()) {
                    Validator validator = aValidatorDefinition.run(className, (String)instance.getProperty(Constants.PROPERTY_UUID), 
                            connectionManager, mem, bem, aem);
                    if (validator != null) //It's possible that after evaluating the condition nothing should be done, so the method "run" could actually return null
                        validators.add(validator);
                }
            } catch (Exception ex) { //Errors will be logged and the validator definition skipped
                System.out.println(String.format("[KUWAIBA] An unexpected error occurred while evaluating validator %s in object %s(%s): %s", 
                        aValidatorDefinition.getName(), instance.getProperty(Constants.PROPERTY_NAME), 
                        instance.getId(), ex.getLocalizedMessage()));
            }
        });
        
        res.setValidators(validators);
        return res;
    }
    
    public ChangeDescriptor updateObject(String id, ClassMetadata classMetadata, HashMap<String, String> attributes) 
            throws InvalidArgumentException, MetadataObjectNotFoundException {
        String oldValues = "", newValues = "", affectedProperties = "";
        
        String query = "MATCH (objectNode)-[:INSTANCE_OF]->(c) WHERE objectNode._uuid = {uuid} AND (objectNode:" + Constants.LABEL_INVENTORY_OBJECTS + 
                " OR objectNode:" + Constants.LABEL_LIST_TYPE_ITEMS +") AND c.name = {className} RETURN objectNode LIMIT 1";
        
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", id);
        parameters.put("className", classMetadata.getName());
        Result queryResult = connectionManager.getConnectionHandler().execute(query, parameters);
        
        if (!queryResult.hasNext()) {
            InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(PREFIX + ".4"), classMetadata.getName(), id));
            ex.setPrefix(PREFIX);
            ex.setCode(4);
            ex.setMessageArgs(classMetadata.getName(), id);
            throw ex;
        }
        
        Node instanceNode = (Node)queryResult.next().get("objectNode");
        
        if (!classMetadata.getName().equals(instanceNode.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME))) {
            MetadataObjectNotFoundException ex = new MetadataObjectNotFoundException(String.format(ts.getTranslatedString(PREFIX + ".4"), classMetadata.getName(), id));
            ex.setPrefix(PREFIX);
            ex.setCode(4);
            ex.setMessageArgs(classMetadata.getName(), id);
            throw ex;
        }
            
        for (String attributeName : attributes.keySet()) {
            if (classMetadata.hasAttribute(attributeName)) {
                affectedProperties = attributeName + " ";
                if (AttributeMetadata.isPrimitive(classMetadata.getType(attributeName))) { // We are changing a primitive type, such as String, or int
                    oldValues += (instanceNode.hasProperty(attributeName) ? String.valueOf(instanceNode.getProperty(attributeName)) : null) + " ";
                    
                    if (attributes.get(attributeName) == null) {
                        if (classMetadata.getAttribute(attributeName).isMandatory()) {//if attribute is mandatory can be set empty or null
                            InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(PREFIX + ".5"), attributeName));
                            ex.setPrefix(PREFIX);
                            ex.setCode(5);
                            ex.setMessageArgs(attributeName);
                            throw ex;
                        } 
                        else
                            instanceNode.removeProperty(attributeName);
                    } else {
                        newValues += attributes.get(attributeName) + " ";
                        //if attribute is mandatory string attributes can't be empty or null
                        if (classMetadata.getAttribute(attributeName).isMandatory()) {
                            if (attributes.get(attributeName).isEmpty()) {
                                InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(PREFIX + ".5"), attributeName));
                                ex.setPrefix(PREFIX);
                                ex.setCode(5);
                                ex.setMessageArgs(attributeName);
                                throw ex;
                            }
                        }
                        if (classMetadata.getAttribute(attributeName).isUnique()) {
                            //
                            BusinessObject businessObject = createObjectFromNode(instanceNode);
                            boolean updateUniqueAttrCache = false;
                            if (businessObject.getAttributes().containsKey(attributeName) && 
                                businessObject.getAttributes().get(attributeName) != null) {
                                updateUniqueAttrCache = true;
                            }
                            if (updateUniqueAttrCache && 
                                !businessObject.getAttributes().get(attributeName).equals(attributes.get(attributeName))) {
                                updateUniqueAttrCache = true;
                            }
                            else
                                updateUniqueAttrCache = false;
                            if (businessObject.getAttributes().get(attributeName) == null)
                                updateUniqueAttrCache = true;
                            //
                            if (updateUniqueAttrCache) {
                                if (isObjectAttributeUnique(classMetadata.getName(), attributeName, attributes.get(attributeName))) {
                                    instanceNode.setProperty(attributeName,Util.getRealValue(attributes.get(attributeName), classMetadata.getType(attributeName), ts));
                                    CacheManager.getInstance().removeUniqueAttributeValue(businessObject.getClassName(), attributeName, businessObject.getAttributes().get(attributeName));
                                } else {
                                    InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(PREFIX + ".3"), classMetadata.getAttribute(attributeName).getDisplayName() != null && !classMetadata.getAttribute(attributeName).getDisplayName().isEmpty() ? classMetadata.getAttribute(attributeName).getDisplayName() : attributeName));
                                    ex.setPrefix(PREFIX);
                                    ex.setCode(3);
                                    ex.setMessageArgs(classMetadata.getAttribute(attributeName).getDisplayName() != null && !classMetadata.getAttribute(attributeName).getDisplayName().isEmpty() ? classMetadata.getAttribute(attributeName).getDisplayName() : attributeName);
                                    throw ex;
                                }
                            }
                        }
                        else
                            instanceNode.setProperty(attributeName,Util.getRealValue(attributes.get(attributeName), classMetadata.getType(attributeName), ts));
                    }
                } else { //If the attribute is not a primitive type, then it's a list type
                    if (!mem.getClass(classMetadata.getType(attributeName)).isListType()) {
                        InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(PREFIX + ".6"), classMetadata.getType(attributeName)));
                        ex.setPrefix(PREFIX);
                        ex.setCode(6);
                        ex.setMessageArgs(classMetadata.getType(attributeName));
                        throw ex;
                    }

                    //Release the previous relationship
                    oldValues += " "; //Two empty, separation spaces
                    for (Relationship rel : instanceNode.getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO)) {
                        if (rel.getProperty(Constants.PROPERTY_NAME).equals(attributeName)) {
                            oldValues += rel.getEndNode().getProperty(Constants.PROPERTY_NAME) + " ";
                            rel.delete();
                        }
                    }
                    
                    if (attributes.get(attributeName) == null || attributes.get(attributeName).trim().isEmpty()) {
                        if (classMetadata.getAttribute(attributeName).isMandatory()) {
                            InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(PREFIX + ".5"), attributeName));
                            ex.setPrefix(PREFIX);
                            ex.setCode(5);
                            ex.setMessageArgs(attributeName);
                            throw ex;
                        }
                    } else {
                        try { //If the new value is different than null, then create the new relationships
                            List<String> listTypeItemIds = new ArrayList<>();
                            for (String listTypeItemIdAsString : attributes.get(attributeName).split(";")) //If the attribute is multiple, the ids will be separated by ";", otherwise, it will be a single long value
                                listTypeItemIds.add(listTypeItemIdAsString);
                            
                            Node listTypeNodeClass = connectionManager.getConnectionHandler().findNode(Label.label(Constants.LABEL_CLASS), Constants.PROPERTY_NAME, classMetadata.getType(attributeName));
                            List<Node> listTypeItemNodes = Util.getListTypeItemNodes(listTypeNodeClass, listTypeItemIds);
                            
                            if (!listTypeItemNodes.isEmpty()) {
                                //Create the new relationships
                                for (Node listTypeItemNode : listTypeItemNodes) {
                                    newValues += listTypeItemNode.getProperty(Constants.PROPERTY_NAME) + " ";
                                    Relationship newRelationship = instanceNode.createRelationshipTo(listTypeItemNode, RelTypes.RELATED_TO);
                                    newRelationship.setProperty(Constants.PROPERTY_NAME, attributeName);
                                }
                                
                            } else if (classMetadata.getAttribute(attributeName).isMandatory()) {
                                InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(PREFIX + ".5"), attributeName));
                                ex.setPrefix(PREFIX);
                                ex.setCode(5);
                                ex.setMessageArgs(attributeName);
                                throw ex;
                            }

                        } catch(NumberFormatException ex) {
                            InvalidArgumentException nestedEx = new InvalidArgumentException(String.format(ts.getTranslatedString(PREFIX + ".7"), attributes.get(attributeName)));
                            nestedEx.setPrefix(PREFIX);
                            nestedEx.setCode(7);
                            nestedEx.setMessageArgs(attributes.get(attributeName));
                            throw ex;
                        }
                    } 
                }
            } else {
                InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(PREFIX + ".8"), attributeName, classMetadata.getName()));
                ex.setPrefix(PREFIX);
                ex.setCode(8);
                ex.setMessageArgs(attributeName, classMetadata.getName());
                throw ex;
            }
        }
        return new ChangeDescriptor(affectedProperties.trim(), oldValues.trim(), newValues.trim(), id);
    }
    
    public BusinessObject createObjectFromNode(Node instance) throws InvalidArgumentException {
        String className = (String)instance.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME);
        try {
            return createObjectFromNode(instance, mem.getClass(className));
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
    public BusinessObject createObjectFromNode(Node instance, ClassMetadata classMetadata) throws InvalidArgumentException {
        
        HashMap<String, Object> attributes = new HashMap<>();
        String name = "";
        
        for (AttributeMetadata myAtt : classMetadata.getAttributes()) {
            //Only set the attributes existing in the current node. Please note that properties can't be null in
            //Neo4J, so a null value is actually a non-existing relationship/value
            if (instance.hasProperty(myAtt.getName())) {
               if (AttributeMetadata.isPrimitive(myAtt.getType())) {
                    Object value = instance.getProperty(myAtt.getName());

                    if (Constants.PROPERTY_NAME.equals(myAtt.getName()))
                        name = (String)value;

                    attributes.put(myAtt.getName(),value);
                }
            }
        }

        //Iterates through relationships and transform the into "plain" attributes
        Iterable<Relationship> iterableRelationships = instance.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING);
        Iterator<Relationship> relationships = iterableRelationships.iterator();

        while(relationships.hasNext()) {
            Relationship relationship = relationships.next();
            if (!relationship.hasProperty(Constants.PROPERTY_NAME)) {
                InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(PREFIX + ".1"), instance.getId()));
                ex.setPrefix(PREFIX);
                ex.setCode(1);
                ex.setMessageArgs(instance.getId());
                throw ex;
            }

            String relationshipName = (String)relationship.getProperty(Constants.PROPERTY_NAME);              
            
            boolean hasRelationship = false;
            for (AttributeMetadata myAtt : classMetadata.getAttributes()) {
                if (myAtt.getName().equals(relationshipName)) {
                    if (attributes.containsKey(relationshipName))
                        attributes.put(relationshipName, attributes.get(relationshipName) + ";" + relationship.getEndNode().getProperty(Constants.PROPERTY_UUID)); //A multiple selection list type
                    else    
                        attributes.put(relationshipName, (String)relationship.getEndNode().getProperty(Constants.PROPERTY_UUID));
                    hasRelationship = true;
                    break;
                }                  
            }
            
            if (!hasRelationship) {//This verification will help us find potential inconsistencies with list types
                                  //What this does is to verify if is there is a RELATED_TO relationship that shouldn't exist because its name is not an attribute of the class
                InvalidArgumentException ex = new InvalidArgumentException(String.format(ts.getTranslatedString(PREFIX + ".2"), 
                    instance.getProperty(Constants.PROPERTY_NAME), instance.getId(), relationship.getEndNode().getProperty(Constants.PROPERTY_NAME), relationship.getEndNode().getId()));
                ex.setPrefix(PREFIX);
                ex.setCode(2);
                ex.setMessageArgs(instance.getProperty(Constants.PROPERTY_NAME), instance.getId(), relationship.getEndNode().getProperty(Constants.PROPERTY_NAME), relationship.getEndNode().getId());
                throw ex;
            }
        }
        
        return new BusinessObject(classMetadata.getName(), (String)instance.getProperty(Constants.PROPERTY_UUID), name, attributes);
    }
    
    /**
     * Check if the value of the given attribute name is unique across other 
     * objects in the class and its subclasses
     * @param className the class name
     * @param attributeName attribute name
     * @param attributeValue attribute value
     * @return true if the attribute value is unique
     */
    public boolean isObjectAttributeUnique(String className, String attributeName, Object attributeValue) {
        List<Object> uniqueAttributeValues = CacheManager.getInstance().getUniqueAttributeValues(className, attributeName);
        if (uniqueAttributeValues != null) {
            for (Object uniqueAttributeValue : uniqueAttributeValues) {
                if (uniqueAttributeValue.equals(attributeValue))
                    return false;
            }
        }
        CacheManager.getInstance().putUniqueAttributeValueIndex(className, attributeName, attributeValue);
        return true;
    }
    
    /**
     * Copies and object and optionally its children objects.This method does not manage transactions
     * @param templateObject The object to be cloned
     * @param objectLabels The labels to new Object
     * @param recursive should the children be copied recursively?
     * @return The cloned node
     */
    public Node copyObject(Node templateObject, boolean recursive, Label... objectLabels) {
        Node newInstance = connectionManager.getConnectionHandler().createNode(objectLabels);
         // Make sure the object has a name, even if it's marked as no copy. Remember that all inventory object nodes 
         //must at least have the properties name, creationDate and _uuid. The latter are also set below too.
        newInstance.setProperty(Constants.PROPERTY_NAME, "");
        
        // Let's find out what attributes should not be copied because they're either marked as unique or noCopy
        ClassMetadata classMetadata = CacheManager.getInstance().
                getClass((String)templateObject.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING)
                        .getEndNode()
                        .getProperty(Constants.PROPERTY_NAME));
        
        // First copy normal attributes
        for (String property : templateObject.getPropertyKeys()) {
            AttributeMetadata currentAttribute = classMetadata.getAttribute(property);
            if (currentAttribute != null && !currentAttribute.isUnique() && !currentAttribute.isNoCopy())
                newInstance.setProperty(property, templateObject.getProperty(property));
        }
        
        // Then list types
        for (Relationship rel : templateObject.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING)) {
            AttributeMetadata currentAttribute = classMetadata.getAttribute((String)rel.getProperty(Constants.PROPERTY_NAME));
            if (currentAttribute != null && !currentAttribute.isNoCopy()) // We don't check for uniqueness, because list types can not be set as unique
                newInstance.createRelationshipTo(rel.getEndNode(), RelTypes.RELATED_TO).setProperty(Constants.PROPERTY_NAME, rel.getProperty(Constants.PROPERTY_NAME));
        }
        
        newInstance.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
        newInstance.setProperty(Constants.PROPERTY_UUID, UUID.randomUUID().toString());
        
        newInstance.createRelationshipTo(templateObject.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode(), RelTypes.INSTANCE_OF);

        if (recursive) {
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING)) {
                Node newChild = copyObject(rel.getStartNode(), true, objectLabels);
                newChild.createRelationshipTo(newInstance, RelTypes.CHILD_OF);
            }
        }
        return newInstance;
    }
}