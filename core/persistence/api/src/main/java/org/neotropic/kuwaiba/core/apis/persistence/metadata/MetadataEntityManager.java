/**
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

package org.neotropic.kuwaiba.core.apis.persistence.metadata;

import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.DatabaseException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.AbstractEntityManager;

/**
 * Manages the metadata entities of the data model, such as classes and attributes.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public interface MetadataEntityManager extends AbstractEntityManager {
    /**
     * Creates a class metadata with its attributes (some new and others inherited from the parent class).
     * @param classDefinition the class definition, name, display name, etc
     * @return the Id of the newClassMetadata
     * @throws MetadataObjectNotFoundException if the specified parent class doesn't exist
     * @throws DatabaseException if the reference node doesn't exist
     * @throws InvalidArgumentException if any of the fields of the class definition has an invalid value
     */
    public long createClass(ClassMetadata classDefinition) throws DatabaseException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Clears the class cache and re-built it.
     * @throws InvalidArgumentException If the unique attributes cache cannot be load.
     */
    public void buildClassCache() throws InvalidArgumentException;
    /**
     * Changes a class metadata definition.
     * @param classId The id of the class. A class metadata definition can not be updated using the name as the key, because the name itself could change.
     * @param newProperties A hashmap with the properties to be updated. The possible key values are: name, color, displayName, description, icon, 
     * smallIcon, countable, abstract, inDesign and custom. See user manual for a more complete explanation on what each one of them are for.
     * @return The summary of the changes that were made.
     * @throws ApplicationObjectNotFoundException
     * @throws MetadataObjectNotFoundException If the class could no be found.
     * @throws InvalidArgumentException If any of the property names provided is unknown or if the name has invalid characters, the 
     * new name is empty or if that name already exists.
     * @throws BusinessObjectNotFoundException If there is any problem retrieving an 
     * object while checking if every created object of the class with an attributes marked as mandatory has value.
     */
    public ChangeDescriptor setClassProperties(long classId, HashMap<String, Object> newProperties) 
            throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException;

    /**
     * Deletes a class metadata, its attributes and category relationships
     * @param className the class name
     * @throws MetadataObjectNotFoundException if there is not a class with de ClassName
     * @throws InvalidArgumentException If the class is a core class, has instances, has incoming relationships or is a list type that is used by another class.
     */
    public void deleteClass(String className) 
            throws MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Deletes a class metadata, its attributes and category relationships
     * @param classId the class id
     * @throws MetadataObjectNotFoundException if there is not a class with de ClassName
     * @throws InvalidArgumentException If the class is a core class, has instances, has incoming relationships or is a list type that is used by another class.
     */
    public void deleteClass(long classId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Retrieves the simplified list of classes, This list won't include either
     * those classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include 
     * the subclasses of GenericObjectList
     * @param includeIndesign Include all the data model classes or only the classes in production
     * @return The list of type classes
     * @throws MetadataObjectNotFoundException If GenericListType class does not exist.
     */
    public List<ClassMetadataLight> getAllClassesLight (boolean includeListTypes, 
            boolean includeIndesign) throws MetadataObjectNotFoundException;
    
    /**
     * Gets the subclasses of a given class recursively.
     * @param className Class name
     * @param includeAbstractClasses Should the list include the abstract subclasses
     * @param includeSelf Should the list include the subclasses and the parent class?
     * @return The list of subclasses
     * @throws MetadataObjectNotFoundException If the class can not be found
     */
    public List<ClassMetadataLight> getSubClassesLight(String className, boolean includeAbstractClasses, 
            boolean includeSelf) throws MetadataObjectNotFoundException;

    /**
     * Gets the direct subclasses of a given class
     * @param className The class name
     * @param includeAbstractClasses If abstract classes should be included.
     * @param includeSelf Also return the metadata of class <code>className</code>
     * @return The list of subclasses
     * @throws MetadataObjectNotFoundException If the class could not be found.
     */
    public List<ClassMetadataLight> getSubClassesLightNoRecursive(String className, boolean includeAbstractClasses, boolean includeSelf) 
            throws MetadataObjectNotFoundException;
    
    /**
     * Retrieves the superclasses of a given class up to InventoryObject.
     * @param className The class to be evaluated.
     * @param includeSelf If the result should include the the very class that was provided in <code>className</code> parameter
     * @return The list of super classes.
     * @throws MetadataObjectNotFoundException If the class provided could not be found
     */
    public List<ClassMetadataLight> getSuperClassesLight(String className, boolean includeSelf) 
            throws MetadataObjectNotFoundException;
    
    /**
     * Get the subclasses count given a parent class name.
     * @param className parent class Name.
     * @return Number of direct subclasses.
     * @throws MetadataObjectNotFoundException If the class provided could not be found.
     */
    public long getSubClassesCount(String className) throws MetadataObjectNotFoundException;
    /**
     * Retrieves all the class metadata except for classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include the subclasses of GenericObjectList
     * @param includeIndesign Include classes marked as "in design"
     * @return An array with the metadata of the classes
     */
    public List<ClassMetadata> getAllClasses(boolean includeListTypes, boolean includeIndesign);

    /**
     * Gets a class metadata, its attributes and Category
     * @param className The class name
     * @return A ClassMetadata with the className
     * @throws MetadataObjectNotFoundException If there is no class with such className
     */
    public ClassMetadata getClass(String className) throws MetadataObjectNotFoundException;
    
    /**
     * Gets a list of classes, its attributes and Category with a given class name to filter
     * @param classNameTofilter The class name to filter for
     * @param page the number of results to skip or the page
     * @param limit The number of results per page
     * @return A list of Classes that contains the filter in the classMetada name
     */
    public List<ClassMetadata> getClasses(String classNameTofilter, int page, int limit);

    /**
     * Gets a class metadata, its attributes and Category
     * @param classId The class id
     * @return A ClassMetadata with the classId
     * @throws MetadataObjectNotFoundException If there is no class with such classId
     */
    public ClassMetadata getClass(long classId) throws MetadataObjectNotFoundException;
    
    /**
     * Adds an attribute to a class.
     * @param className The class the attribute will be added to.
     * @param attributeDefinition An object with the definition of the attribute.
     * @param recursive Defines if the subclasses that has an attribute with name 
     *                  equal to the name of the new attribute can conserve (false) it 
     *                  or must be removed (true), in the case when the attribute must 
     *                  be removed throws an exception
     * @throws MetadataObjectNotFoundException if there is no a class with such className
     * @throws InvalidArgumentException if any of the parameters to create the attribute has a wrong value
     */
    public void createAttribute(String className, AttributeMetadata attributeDefinition, boolean recursive) throws MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Adds an attribute to a class
     * @param classId The id of the class the attribute will be added to.
     * @param attributeDefinition An object with the definition of the attribute.
     * @throws MetadataObjectNotFoundException if there is no a class with such classId
     * @throws InvalidArgumentException if any of the parameters to create the attribute has a wrong value
     */
    public void createAttribute(long classId, AttributeMetadata attributeDefinition) throws MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Checks if a class has a attribute with a given name
     * @param className Class name
     * @param attributeName Attribute name
     * @return True if the given class has the attribute
     * @throws MetadataObjectNotFoundException If there is no a class with such className
     */
    public boolean hasAttribute(String className, String attributeName) throws MetadataObjectNotFoundException;

    /**
     * Gets an attribute belonging to a class
     * @param className Class name.
     * @param attributeName Attribute name.
     * @return AttributeMetata of the requested attribute.
     * @throws MetadataObjectNotFoundException if there is no a class with such className
     * @throws InvalidArgumentException if the attributeName does not exist
     */
    public AttributeMetadata getAttribute(String className, String attributeName) throws MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Gets an attribute belonging to a class
     * @param classId Class id.
     * @param attributeId Attribute id.
     * @return AttributeMetata of the requested attribute.
     * @throws MetadataObjectNotFoundException if there is no a class with such classId
     * @throws InvalidArgumentException if the attributeName does not exist
     */
    public AttributeMetadata getAttribute(long classId, long attributeId) throws InvalidArgumentException, MetadataObjectNotFoundException;

    /**
     * Changes an attribute definition belonging to a class metadata using the class id as key
     * @param classId Id of the class the attribute belongs to.
     * @param attributeId
     * @param newProperties A hashmap with the properties to be updated. The possible key values are: name, color, displayName, description, icon, 
     * smallIcon, countable, abstract, inDesign and custom. See user manual for a more complete explanation on what each one of them are for.
     * @return The summary of the changes that were made.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     * @throws InvalidArgumentException If any of the new attribute parameters has a wrong value.
     * @throws BusinessObjectNotFoundException If an object can't be find, while it is checking if every object of the class (or subclasses) has a value in an attribute marked as mandatory
     */
    public ChangeDescriptor setAttributeProperties(long classId,  long attributeId, HashMap<String, Object> newProperties) throws 
            MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException;
    
    /**
     * Changes an attribute definition belonging to a class metadata use the class name as id
     * @param className Class the attribute belongs to.
     * @param attributeId Id of the attribute to be updated.
     * @param newProperties A hashmap with the properties to be updated. The possible key values are: name, color, displayName, description, icon, 
     * smallIcon, countable, abstract, inDesign and custom. See user manual for a more complete explanation on what each one of them are for.
     * @return The summary of the changes that were made.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     * @throws InvalidArgumentException If any of the new attribute parameters has a wrong value.
     * @throws BusinessObjectNotFoundException  If an object can't be find, while it is checking if every object of the class (or subclasses) has a value in an attribute marked as mandatory
     */
    public ChangeDescriptor setAttributeProperties(String className, long attributeId, HashMap<String, Object> newProperties) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException;

    /**
     * Deletes an attribute from a class.
     * @param className Class name.
     * @param attributeName Attribute name.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     * @throws InvalidArgumentException If the attributes name or creationDate are to be deleted.
     */
    public void  deleteAttribute(String className, String attributeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Deletes an attribute from a class.
     * @param classId Class id.
     * @param attributeName Attribute name.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     * @throws InvalidArgumentException If the attributes name or creationDate are to be deleted.
     */
    public void deleteAttribute(long classId,String attributeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Gets all classes whose instances can be contained into the given parent class.This method
 is recursive, so the result include the possible children in children classes
     * @param parentClassName The name of the class.
     * @param ignoreAbstract true to ignore abstract classes.
     * @return An array with the list of direct possible children classes in the containment hierarchy.
     * @throws MetadataObjectNotFoundException If the class can not be found.
     */
    public List<ClassMetadataLight> getPossibleChildren(String parentClassName, boolean ignoreAbstract) throws MetadataObjectNotFoundException;
    
    /**
     * Gets all classes whose instances can be contained into the given parent class, but using a CHILD_OF_SPECIAL relationship instead of a CHILD_OF one. This is mostly used in complex models, such as the physical layer model. This method
     * is recursive, so the result include the possible children in children classes
     * @param parentClassName The name of the class.
     * @return An array with the list of direct possible children classes in the containment hierarchy.
     * @throws MetadataObjectNotFoundException If the class can not be found.
     */
    public List<ClassMetadataLight> getPossibleSpecialChildren(String parentClassName) throws MetadataObjectNotFoundException;

    /**
     * Same as getPossibleChildren but this one only gets the direct possible children for the given class,
     * this is, subclasses are not included
     * @param parentClassName The name of the class.
     * @return An array with the list of possible children classes in the containment hierarchy, including the subclasses of the abstract classes.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     */
    public List<ClassMetadataLight> getPossibleChildrenNoRecursive(String parentClassName) throws MetadataObjectNotFoundException;
    
    /**
     * Same as getPossibleSpecialChildren but this one only gets the direct special possible children for the given class,
     * this is, subclasses are not included
     * @param parentClassName The name of the class.
     * @return An array with the list of possible children classes in the containment hierarchy, including the subclasses of the abstract classes.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     */
    public List<ClassMetadataLight> getPossibleSpecialChildrenNoRecursive(String parentClassName) throws MetadataObjectNotFoundException;

    /**
     * Finds out if an instance of a given class can be child of an instance of allegedParent. This is a sort of reverse getPossibleChildren
     * @param allegedParent Possible parent
     * @param childToBeEvaluated Class to be evaluated
     * @return True an instance of class childToBeEvaluated be a contained into an instance of allegedParent. False otherwise.
     * @throws MetadataObjectNotFoundException If any of the classes involved doesn't exist
     */
    public boolean canBeChild(String allegedParent, String childToBeEvaluated) throws MetadataObjectNotFoundException;
    /**
     * Same as <code>canBeChild</code>, but using the special containment hierarchy
     * @param allegedParent Possible parent
     * @param childToBeEvaluated Class to be evaluated
     * @return True an instance of class childToBeEvaluated be a contained into an instance of allegedParent (as in the special containment hierarchy). False otherwise.
     * @throws MetadataObjectNotFoundException 
     */
    public boolean canBeSpecialChild(String allegedParent, String childToBeEvaluated) throws MetadataObjectNotFoundException;
    
    /**
     * Adds to a given class a list of possible children classes whose instances can be contained using the class id to find the parent class
     *
     * @param parentClassId Id of the class whose instances can contain the instances of the classes in possibleChildren. Use -1 to refer to the DummyRoot
     * @param possibleChildren ids of the candidates to be contained
     * @throws MetadataObjectNotFoundException if any of the possible children or the parent doesn't exist
     * @throws InvalidArgumentException If any of the possible children classes already are possible children or if one of the possible children is not an instance 
     * of a subclass of InventoryObject or if the parent class is abstract.
     */
    public void addPossibleChildren(long parentClassId, long[] possibleChildren) throws MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Adds to a given class a list of possible special children classes whose instances can be contained using the class id to find the parent class
     *
     * @param parentClassId Id of the class whose instances can contain the instances of the classes in possibleChildren.
     * @param possibleSpecialChildren ids of the candidates to be contained
     * @throws MetadataObjectNotFoundException If any of the possible children or the parent doesn't exist
     * @throws InvalidArgumentException If any of the possible children classes already are possible special children or if one of the possible children is not an instance 
     * of a subclass of InventoryObject or if the parent class is abstract or if it's attempted to use the navigation tree root as parent class ({@link #addPossibleChildren(java.lang.String, java.lang.String[]) } supports -1 as parent class id)
     */
    public void addPossibleSpecialChildren(long parentClassId, long[] possibleSpecialChildren) throws MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Adds to a given class a list of possible children classes whose instances can be contained using the class name to find the parent class
     * @param parentClassName parent class name. Use DummyRoot for the Navigation Tree root
     * @param possibleChildren list of possible children
     * @throws MetadataObjectNotFoundException if the parent class or any of the possible children can not be found
     * @throws InvalidArgumentException if any of the given possible children can not be a possible children of parentClassName or if one of the possible children is not an instance 
     * of a subclass of InventoryObject or if the parent class is abstract.
     */
    public void addPossibleChildren(String parentClassName, String[] possibleChildren) throws MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Adds to a given class a list of possible special children classes whose instances can be contained, using the class name to find the parent class
     * @param parentClassName parent class name.
     * @param possibleSpecialChildren list of possible children
     * @throws MetadataObjectNotFoundException if the parent class or any of the possible children can not be found
     * @throws InvalidArgumentException if any of the given possible children can not be a possible children of parentClassName or if one of the possible children is not an instance 
     * of a subclass of InventoryObject or if the parent class is abstract.
     */
    public void addPossibleSpecialChildren(String parentClassName, String[] possibleSpecialChildren) throws MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * The opposite of addPossibleChildren. It removes the given possible children
     * TODO: Make this method safe. This is, check if there's already intances of the given
     * "children to be deleted" with parentClass as their parent
     * @param parentClassId Id of the class whos instances can contain the instances of the next param
     * @param childrenToBeRemoved ids of the candidates to be deleted
     * @throws MetadataObjectNotFoundException If any of the ids provided can't be found
     */
    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved) throws MetadataObjectNotFoundException;
    /**
     * The opposite of addPossibleSpecialChildren. It removes the given possible special children
     * @param parentClassId Id of the class whos instances can contain the instances of the next param
     * @param childrenToBeRemoved ids of the candidates to be deleted
     * @throws MetadataObjectNotFoundException If any of the ids provided can't be found
     */
    public void removePossibleSpecialChildren(long parentClassId, long[] childrenToBeRemoved) throws MetadataObjectNotFoundException;
    /**
     * Sets the display name of a special relationship used in a model
     * @param relationshipName The name of the relationship the display name is going to be set
     * @param relationshipDisplayName The display name
     */
    public void setSpecialRelationshipDisplayName(String relationshipName, String relationshipDisplayName);
    /**
     * Returns the display name of a special relationship. The display name is useful to improve the way the relationship is displayed on trees and other modules
     * @param relationshipName The name of the relationship
     * @return The display name for the relationship name provided. If it can not be found, the relationship name is returned instead
     */
    public String getSpecialRelationshipDisplayName(String relationshipName);
    /**
     * Assess if a given class is subclass of another
     * @param allegedParent Alleged super class
     * @param classToBeEvaluated class to be evaluated
     * @return True if classToBeEvaluated is subclass of allegedParent. False otherwise. This method also returns true if allegedParent == classToBeEvaluated
     * @throws MetadataObjectNotFoundException If any of the classes provided doesn't exist
     */
    public boolean isSubclassOf(String allegedParent, String classToBeEvaluated) throws MetadataObjectNotFoundException;
    /**
     * Get the upstream containment hierarchy for a given class, unlike getPossibleChildren (which will give you the 
     * downstream hierarchy).
     * @param className Class name
     * @param recursive Get only the direct possible parents, or go up into the <strong>containment</strong> hierarchy. Beware: don't mistake the class hierarchy for the containment one
     * @return An sorted list with the upstream containment hierarchy. Repeated elements are omitted
     * @throws MetadataObjectNotFoundException if className does not correspond to any existing class
     */
    public List<ClassMetadataLight> getUpstreamContainmentHierarchy(String className, boolean recursive) throws MetadataObjectNotFoundException;
    /**
     * Gets the upstream special containment hierarchy for a given class, unlike getPossibleChildren (which will give you the 
     * downstream hierarchy).
     * @param className Class name
     * @param recursive Get only the direct possible parents, or go up into the <strong>containment</strong> hierarchy. Beware: don't mistake the class hierarchy for the containment one
     * @return An sorted list with the special upstream containment hierarchy. Repeated elements are omitted
     * @throws MetadataObjectNotFoundException if className does not correspond to any existing class
     */
    public List<ClassMetadataLight> getUpstreamSpecialContainmentHierarchy(String className, boolean recursive) throws MetadataObjectNotFoundException;
    /**
     * Gets the parent classes of a given class up to <code>InventoryObject</code>. Please note that <code>RootObject</code> is being deliberately omitted.
     * @param className The class to get the superclasses from
     * @param includeSelf If the result should also include the class in className
     * @return The list of super classes until the root of the hierarchy
     * @throws MetadataObjectNotFoundException If the class provided could not be found
     */
    public List<ClassMetadataLight> getUpstreamClassHierarchy(String className, boolean includeSelf) throws MetadataObjectNotFoundException;
    /**
     * Retrieves the list of the attributes marked as mandatory
     * @param className the class name
     * @return a list of AttributeMetadata
     * @throws MetadataObjectNotFoundException if the class doesn't exist
     */
    public List<AttributeMetadata> getMandatoryAttributesInClass(String className) throws 
            MetadataObjectNotFoundException;
}
