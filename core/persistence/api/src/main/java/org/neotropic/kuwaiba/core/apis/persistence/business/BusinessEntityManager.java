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

package org.neotropic.kuwaiba.core.apis.persistence.business;

import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neotropic.kuwaiba.core.apis.persistence.AbstractEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObject;

/**
 * This is the entity in charge of manipulating business objects
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public interface BusinessEntityManager extends AbstractEntityManager {
    /**
     * Creates a new inventory object
     * @param className Name of the class which this object will be instantiated from
     * @param parentClassName Parent object class name. If null, the parent will be the DummyRoot node
     * @param parentOid Parent's oid. If -1, the parent will be the DummyRoot node
     * @param attributes Attributes to be set by default in the new object. It's a HashMap where the keys are the attribute names and the values, the values for such attributes.
     * Note that binary type attributes can't be set here.
     * @param templateId Template id to be used to create the current object. Template values can be
     * overridden if "attributeValues" is not empty. Use an empty string or null to not use a Template.
     * @return The object's id
     * @throws MetadataObjectNotFoundException Thrown if the object's class can't be found
     * @throws BusinessObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException If there's a business constraint that doesn't allow to create the object.
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format.
     * @throws ApplicationObjectNotFoundException If the specified template could not be found
     */
    public String createObject(String className, String parentClassName, String parentOid,
            HashMap<String, String> attributes, String templateId)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, 
                OperationNotPermittedException, ApplicationObjectNotFoundException;
    
    /**
     * Creates an object
     * @param className Class the object will be instance of
     * @param parentClassName Class of the parent the object will be instance of. Use <b>root</b> for the navigation tree
     * @param criteria Criteria to search for the parent. This is a string with two parts: One is the name of the attribute and the other its value, both separated by a fixed colon <b>:</b>. Example: name:Colombia
     * @param attributes Dictionary with the names and the values of the attributes to be set.
     * @param templateId The id of the template to be used to create this object. This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before. Use a null or empty string to not use a template.
     * @return The id of the new object.
     * @throws MetadataObjectNotFoundException Thrown if the object's class can't be found
     * @throws BusinessObjectNotFoundException Thrown if the parent id is not found
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format.
     * @throws OperationNotPermittedException If there's a business constraint that doesn't allow to create the object.
     * @throws ApplicationObjectNotFoundException If the specified template could not be found.
     */
    public String createObject(String className, String parentClassName, HashMap<String,String> attributes, String templateId, String criteria)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException;
    /**
     * Creates a new inventory object for a domain specific model (where the standard containment rules don't apply)
     * @param className Name of the class which this object will be instantiated from
     * @param parentClassName Parent object class name
     * @param parentOid Parent's oid
     * @param attributes Attributes to be set by default in the new object. It's a HashMap where the keys are the attribute names and the values, the values for such attributes.
     * Note that binary type attributes can't be set here.
     * @param templateId The id of the template to be used to create this object. This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before. Use a null or empty string to not use a template.
     * @return The id of the new object.
     * @throws MetadataObjectNotFoundException Thrown if the object's class can't be found
     * @throws BusinessObjectNotFoundException Thrown if the parent id is not found
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format.
     * @throws ApplicationObjectNotFoundException If the specified template could not be found.
     */
    public String createSpecialObject(String className, String parentClassName, String parentOid,
            HashMap<String,String> attributes, String templateId)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException;
    /**
     * Equal to {@link BusinessEntityManager#createSpecialObject(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap, java.lang.String)} but the return is a map of ids.
     * Creates a new inventory object for a domain specific model (where the standard containment rules don't apply)
     * @param className Name of the class which this object will be instantiated from
     * @param parentClassName Parent object class name
     * @param parentOid Parent's oid
     * @param attributes Attributes to be set by default in the new object. It's a HashMap where the keys are the attribute names and the values, the values for such attributes.
     * Note that binary type attributes can't be set here.
     * @param templateId The id of the template to be used to create this object. This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before. Use a null or empty string to not use a template.
     * @return The id of the new object.
     * @throws MetadataObjectNotFoundException Thrown if the object's class can't be found
     * @throws BusinessObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format.
     * @throws ApplicationObjectNotFoundException If the specified template could not be found.
     */
    public HashMap<String, String> createSpecialObjectUsingTemplate(String className, String parentClassName, String parentOid, HashMap<String, String> attributes, String templateId)
        throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException;
    /**
     * Breaks the special hierarchy to enable special children to have more than one parent.
     * @param specialObjectClass Special object class name
     * @param specialObjectId Special Object Id
     * @param parentClass Parent Object class
     * @param parentId Parent Object id
     * @throws BusinessObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public void addParentToSpecialObject(String specialObjectClass, String specialObjectId, String parentClass, String parentId) 
        throws BusinessObjectNotFoundException,OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Creates an object without parent. This might be particularly useful for complex models. Use it carefully to avoid leaving orphan objects. 
     * Always provide custom methods to delete 
     * @param className The class name of the object to be created.
     * @param attributes The initial set of attributes (as pairs attribute name - value) to be set. These values will override those in the template used (if any).
     * @param templateId The id of the template to be used to create the object. Use null or an empty string to not use any template.
     * @return The id of the newly created object.
     * @throws MetadataObjectNotFoundException If the class provided does not exist.
     * @throws InvalidArgumentException If the format of any of the default attributes provided is incorrect.
     * @throws OperationNotPermittedException If the class provided is marked as <i>in design</i> or it is abstract.
     * @throws ApplicationObjectNotFoundException If the template provided does not exist.
     */
    public String createHeadlessObject(String className, HashMap<String,String> attributes, String templateId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException;
    
    /**
     * Creates an object inside a pool
     * @param poolId Parent pool id
     * @param className Class this object is going to be instance of
     * @param attributes The list of attributes to be set initially. The values are serialized objects.
     * @param templateId The id of the template to be used to create this object. This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before. Use a null or empty string to not use a template.
     * @throws ApplicationObjectNotFoundException If the parent pool can't be found
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @return the id of the newly created object
     * @throws MetadataObjectNotFoundException If the class name could not be found 
     */
    public String createPoolItem(String poolId, String className, HashMap<String, String> attributes, String templateId) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException;
    /**
     * Creates multiple objects using a given name pattern
     * @param className The class name for the new objects
     * @param parentClassName The parent class name for the new objects
     * @param parentOid The object id of the parent
     * @param namePattern A pattern to create the names for the new objects
     * @param templateId a template id for the objects creation, it could be null if no template is required
     * @return An arrays of ids for the new objects
     * @throws MetadataObjectNotFoundException If the className or the parentClassName can not be found.
     * @throws BusinessObjectNotFoundException If the parent node can not be found.
     * @throws InvalidArgumentException If the given name pattern not match with the regular expression to build the new object name.
     * @throws OperationNotPermittedException If the className is not a possible children of parentClassName.
     *                                        If the className is not in design or are abstract.
     *                                        If the className is not an InventoryObject.
     * @throws ApplicationObjectNotFoundException if the given template id does not exist.
     */
    public String[] createBulkObjects(String className, String parentClassName, String parentOid, String namePattern, String templateId) 
        throws MetadataObjectNotFoundException, OperationNotPermittedException, BusinessObjectNotFoundException, 
            InvalidArgumentException, ApplicationObjectNotFoundException;
    /**
     * Creates multiple special objects using a given name pattern
     * @param className The class name for the new special objects
     * @param parentClassName The parent class name for the new special objects
     * @param parentId The object id of the parent
     * @param namePattern A pattern to create the names for the new special objects
     * @param templateId The id of the template to be used for the set of objects to be created. Used null for none.
     * @return An array of ids for the new special objects
     * @throws MetadataObjectNotFoundException If the className or the parentClassName can not be found.
     * @throws BusinessObjectNotFoundException If the parent node can not be found.
     * @throws InvalidArgumentException If the given name pattern not match with the regular expression to build the new object name.
     * @throws OperationNotPermittedException If the className is not a possible special children of parentClassName.
     *                                        If the className is not in design or are abstract.
     *                                        If the className is not an InventoryObject.
     * @throws org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException If the provided template could not be found.
     */
    public String[] createBulkSpecialObjects(String className, String parentClassName, String parentId, String namePattern, String templateId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException, ApplicationObjectNotFoundException;
    
    /**
     * Gets the detailed information about an object
     * @param className Object class name
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws MetadataObjectNotFoundException If the className class can't be found
     * @throws BusinessObjectNotFoundException If the requested object can't be found
     * @throws InvalidArgumentException If the object id can not be found.
     */
    public BusinessObject getObject(String className, String oid)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;

    /**
     * Gets the special children of a given object
     * @param objectClass Object class
     * @param objectId object id
     * @return The list of special children.
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws BusinessObjectNotFoundException If the object could not be found.
     * @throws InvalidArgumentException If the object id is null
     */
    public List<BusinessObjectLight> getObjectSpecialChildren(String objectClass, String objectId)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
     /**
     * Gets the special children of a given object
     * @param objectClass Object class
     * @param objectId object id
     * @param filters attributes values to filter the return
     * @param page the number of results to skip or the page
     * @param the limit of results per page
     * @return The list of special children.
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws BusinessObjectNotFoundException If the object could not be found.
     * @throws InvalidArgumentException If the object id is null
     */
    public List<BusinessObjectLight> getObjectSpecialChildrenWithFilters(String objectClass, 
            String objectId, List<String> childrenClassNamesToFilter, int page, int limit)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    /**
     * Gets the special children of a given object
     * @param objectClass Object class
     * @param objectId object id
     * @return The list of special children.
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws BusinessObjectNotFoundException If the object could not be found.
     * @throws InvalidArgumentException If the object id is null
     */
    public long getObjectSpecialChildrenCount(String objectClassName, String objectId, String... childrenClassNamesToFilter)
            throws BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Gets the simplified information about an object
     * @param className Object class name
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws MetadataObjectNotFoundException If the className class can't be found
     * @throws BusinessObjectNotFoundException If the requested object can't be found
     * @throws InvalidArgumentException If the oid is null
     */
    public BusinessObjectLight getObjectLight(String className, String oid)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Retrieves a list of light instances of a given class given a simple filter. This method will search for all objects with a string-based attribute (filterName) whose value matches a value provided (filterValue)
     * @param className The class of the objects to be searched. This method support abstract superclasses as well
     * @param filterName The attribute to be used as filter. This will work <b>only</b> with String-type attributes. Example: serialNumber. To list type attributes the filter must be applied to the name. Example: filterName: model, filterValue: XYZ
     * @param filterValue The value to be use to match the instances. Example "Serial-12345"
     * @return The list of instances that matches the filterName/filterValue criteria
     * @throws InvalidArgumentException If the uuid attribute could not be found in the result nodes
     */
    public List<BusinessObjectLight> getObjectsWithFilterLight (String className, 
            String filterName, String filterValue) throws InvalidArgumentException;
    
    /**
     * Same as {@link #getObjectsWithFilterLight(java.lang.String, java.lang.String, java.lang.String) }, but returns the full information about the objects involved
     * @param className The class of the objects to be searched. This method support abstract superclasses as well
     * @param filterName The attribute to be used as filter. This will work <b>only</b> with String-type attributes. Example: serialNumber. To list type attributes the filter must be applied to the name. Example: filterName: model, filterValue: XYZ
     * @param filterValue The value to be use to match the instances. Example "Serial-12345"
     * @return The list of instances that matches the filterName/filterValue criteria
     * @throws InvalidArgumentException If it's not possible to construct the RemoteBusinessObjects from the information in the database
     */
    public List<BusinessObject> getObjectsWithFilter (String className, 
            String filterName, String filterValue) throws InvalidArgumentException;
    
    /**
     * Suggests a number of objects based on a search string. The search string is matched against the name of the object, 
     * its class name or its class display name.
     * @param filter The string to use as search filter.
     * @param limit The limit of results. Use -1 to retrieve all.
     * @return A list of up to #{@code limit} suggested objects matching the criteria, alphabetically sorted.
     */
    public List<BusinessObjectLight> getSuggestedObjectsWithFilter(String filter, int limit);
    
    /**
     * Same as {@link #getSuggestedObjectsWithFilter(java.lang.String, int) }, but the results will be instances of the super class provided or one of its subclasses. 
     * In reality, this method could do the same as {@link #getSuggestedObjectsWithFilter(java.lang.String, int) } with {@code superClass} set to <code>InventoryObject</code>,
     * but the implementation of both methods may differ significantly in terms of performance and complexity.
     * @param filter The search string
     * @param superClass The results will be instances of this class or one of its subclasses
     * @param limit The limit of results. Use -1 to retrieve all
     * @deprecated In favor of {@link #getSuggestedObjectsWithFilter(java.lang.String, int, int, java.lang.String...) }
     * @return A list of up to #{@code limit} suggested objects matching the criteria, alphabetically sorted
     */
    @Deprecated
    public List<BusinessObjectLight> getSuggestedObjectsWithFilter(String filter, String superClass, int limit);
    /**
     * Gets a set of suggested objects with filter.
     * @param filter a possible part of the name of an object(s) or class(es).
     * @param skip The number of leading suggested objects to skip.
     * @param limit The number of suggested objects the result should be limited to.
     * @param classesToFilter The suggested objects will be instance of this classes or subclasses.
     * @return List of suggested objects.
     */
    public List<BusinessObjectLight> getSuggestedObjectsWithFilter(String filter, int skip, int limit, String... classesToFilter);
    /**
     * Gets a set of objects by its exact names and class names, used to know 
     * if an object with the same its already created in the inventory 
     * e.g. an IP address or a subnet in the ipam module can not be repeated
     * @param names the exact names of the objects
     * @param skip the page
     * @param limit the limit per page
     * @param clasessToFilter class names of the objects 
     * @return a list of objects
     * @throws InvalidArgumentException 
     */
    public List<BusinessObjectLight> getObjectsByNameAndClassName(List<String> names, int skip, int limit, String... clasessToFilter) throws InvalidArgumentException;
    /**
     * Gets a set of suggested children with filter (no recursive).
     * @param parentClass The parent class name.
     * @param parentId The parent id.
     * @param filter Filter children (no recursive) by name or class.
     * @param ignoreSpecialChildren True to ignore special children in the suggested children (no recursive).
     * @param skip The number of leading children to skip.
     * @param limit The number of children the result should be limited to.
     * @param clasessToFilter The suggested children will be instance of this classes or subclasses.
     * @return List of suggested children (no recursive).
     */
    public List<BusinessObjectLight> getSuggestedChildrenWithFilter(String parentClass, String parentId, String filter, boolean ignoreSpecialChildren, int skip, int limit, String... clasessToFilter);
    /**
     * Utility method that returns the value of an attribute of a given object as a string. In date-type attributes, it will return 
     * the formatted dated, while in list types, it will return the name of the linked element
     * @param objectClass The class of the object
     * @param objectId The id of the object
     * @param attributeName The attribute whose value will be retrieved
     * @return The value of the requested attribute. Null values are possible
     * @throws MetadataObjectNotFoundException If the class of the object could not be found
     * @throws BusinessObjectNotFoundException If the object itself could not be found
     * @throws InvalidArgumentException Check with the data model integrity, because this would mean that a the type of the attribute should be a list type, but it's not
     * @throws ApplicationObjectNotFoundException Check with the data model integrity, because this would mean that a list type item related to the object is not an instance of the right list type class
     */
    public String getAttributeValueAsString (String objectClass, String objectId, String attributeName) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException;
    
    /**
     * Fetches the attributes of an inventory object (and their values) and returns them as strings. This is useful mainly to display property sheets and reports, 
     * so it's not necessary to always check if an attribute is a list type and retrieve its string representation
     * @param objectClass The class of the object
     * @param objectId The id of the object
     * @return A dictionary with the name of the attributes and their values represented as strings
     * @throws MetadataObjectNotFoundException If the class of the object could not be found
     * @throws BusinessObjectNotFoundException If the object itself could not be found
     * @throws InvalidArgumentException Check with the data model integrity, because this would mean that a the type of the attribute should be a list type, but it's not
     * @throws ApplicationObjectNotFoundException Check with the data model integrity, because this would mean that a list type item related to the object is not an instance of the right list type class
     */
    public HashMap<String, String> getAttributeValuesAsString (String objectClass, String objectId) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException;
    
    /**
     * Finds the common parent between two objects.
     * @param aObjectClass Object A class name.
     * @param aOid Object A id.
     * @param bObjectClass Object B class name.
     * @param bOid Object B id.
     * @return The common parent or null if none.
     * @throws BusinessObjectNotFoundException If any of the objects could not be found.
     * @throws MetadataObjectNotFoundException If any of the classes provided could not be found.
     * @throws InvalidArgumentException If the database object could not be properly mapped into a serializable Java object.
     */
    public BusinessObjectLight getCommonParent(String aObjectClass, String aOid, String bObjectClass, String bOid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Gets the parent of a given object in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object id
     * @return The immediate parent. Null if the parent is null. A dummy object with id -1 if the parent is DummyRoot
     * @throws BusinessObjectNotFoundException If the requested object can't be found
     * @throws MetadataObjectNotFoundException If any of the class nodes involved is malformed
     * @throws InvalidArgumentException If the database object could not be properly mapped into a serializable java object.
     */
    public BusinessObjectLight getParent(String objectClass, String oid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Retrieves recursively the list of parents of an object in the containment hierarchy
     * @param oid Object id 
     * @param objectClassName Object class
     * @return The list of parents
     * @throws BusinessObjectNotFoundException If the object does not exist
     * @throws MetadataObjectNotFoundException if the class can not be found
     * @throws InvalidArgumentException If the oid is null
     */
    public List<BusinessObjectLight> getParents(String objectClassName, String oid)
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Gets the list of parents (according to the special and standard containment hierarchy) until it finds an instance of class 
     * objectToMatchClassName (for example "give me the parents of this port until you find the nearest rack")
     * @param objectClassName Class of the object to get the parents from
     * @param oid Id of the object to get the parents from
     * @param objectToMatchClassNames Classes of the objects that will limit the search. It can be a superclass, if you want to match many classes at once
     * @return The list of parents until an instance of objectToMatchClassName is found. If no instance of that class is found, all parents until the Dummy Root will be returned
     * @throws BusinessObjectNotFoundException If the object to evaluate can not be found
     * @throws MetadataObjectNotFoundException If any of the classes provided could not be found
     * @throws ApplicationObjectNotFoundException If the object provided is not in the standard containment hierarchy
     * @throws InvalidArgumentException If the oid is null
     */
    public List<BusinessObjectLight> getParentsUntilFirstOfClass(String objectClassName, String oid, String... objectToMatchClassNames)
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Gets the first occurrence of a parent with a given class (according to the special and standard containment hierarchy)
     * (for example "give me the parent of this port until you find the nearest rack")
     * @param objectClassName Class of the object to get the parent from
     * @param oid Id of the object to get the parent from
     * @param objectToMatchClassName Class of the object that will limit the search. It can be a superclass, if you want to match many classes at once
     * @return The the first occurrence of a parent with a given class. If no instance of that class is found, the child of Dummy Root related in this hierarchy will be returned
     * @throws BusinessObjectNotFoundException If the object to evaluate can not be found
     * @throws MetadataObjectNotFoundException If any of the classes provided could not be found
     * @throws ApplicationObjectNotFoundException If the object provided is not in the standard containment hierarchy
     * @throws InvalidArgumentException If the oid is null
     */
    public BusinessObject getFirstParentOfClass(String objectClassName, String oid, String objectToMatchClassName)
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException;
    /**
     * Gets the parents of an object that breaks the containment rule of having only one parent.
     * For example the links and containers in the Outside Plant Module.
     * @param objectClassName Object Class Name.
     * @param objectId Object Id.
     * @throws InvalidArgumentException If the object class is null. If the object id is null.
     * @return The set of parents.
     */
    public List<BusinessObjectLight> getMultipleParents(String objectClassName, String objectId) throws InvalidArgumentException;
    /**
     * Checks if a given object is parent to another, according to the standard or special containment hierarchy.
     * @param parentClass Alleged parent Class Name.
     * @param parentId Alleged parent id.
     * @param childClass Child Class Name.
     * @param childId Child Id.
     * @return True if the given parent has the given child (according to the special and standard containment hierarchy)
     * @throws InvalidArgumentException
     */
    public boolean isParent(String parentClass, String parentId, String childClass, String childId) throws InvalidArgumentException;
    /**
     * Checks recursively if it's safe to delete a single object
     * @param className Object's class name
     * @param oid Objects oid
     * @return True if the object does not have relationships that keep it from being deleted. False otherwise.
     * @throws BusinessObjectNotFoundException If the object couldn't be found
     * @throws MetadataObjectNotFoundException If the requested object class can't be found
     * @throws OperationNotPermittedException If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     * @throws InvalidArgumentException 
     */
    public boolean canDeleteObject(String className, String oid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException;
    
    /**
     * Deletes a set of objects
     * @param releaseRelationships If all the relationships should be release upon deleting the objects. If false, an OperationNotPermittedException  will be raised if the object has incoming relationships.
     * @param  objects a hashmap where the class name is the key and the value is a list of long containing the ids of the objects to be deleted that are instance of the key class
     * @throws BusinessObjectNotFoundException If the requested object can't be found
     * @throws MetadataObjectNotFoundException If the requested object class can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked or it has relationships and releaseRelationships is false
     * @throws InvalidArgumentException If the uuid can not be found
     */
    public void deleteObjects(HashMap<String, List<String>> objects, boolean releaseRelationships)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException;

    /**
     * Deletes a single object
     * @param className Object's class name
     * @param oid Objects oid
     * @param releaseRelationships Release relationships automatically. If set to false, it will fail if the object already has incoming relationships
     * @throws BusinessObjectNotFoundException If the object couldn't be found
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws OperationNotPermittedException If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     * @throws InvalidArgumentException If the oid is null
     */
    public void deleteObject(String className, String oid, boolean releaseRelationships)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException;
    
    /**
     * Updates an object attributes. Note that you can't set binary attributes through this
     * method. Use setBinaryAttributes instead.
     * @param className Object class name
     * @param oid Object's oid
     * @param attributes The attributes to be updated (the key is the attribute name, 
     * the value is and array with the value -or values in case of MANY TO MANY list type attributes-)
     * @return The summary of the changes that were made
     * @throws MetadataObjectNotFoundException If the object class can't be found
     * @throws BusinessObjectNotFoundException If the object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws InvalidArgumentException If any of the names provided does not exist or can't be set using this method or of the value 
     * of any of the attributes can not be mapped correctly or if the class provided is not a subclass of <code>InventoryObject</code>.
     */
    public ChangeDescriptor updateObject(String className, String oid, HashMap<String, String> attributes)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException;
    
    /**
     * Move a list of objects to a new parent: this methods ignores those who can't be moved and raises
     * an OperationNotPermittedException, however, it will move those which can be moved
     * @param objects Map using the object class name as keys and the respective objects oids as values
     * @param targetClassName Parent's class name
     * @param targetOid Parent's oid
     * @throws MetadataObjectNotFoundException If the object's or new parent's class can't be found
     * @throws BusinessObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     * @throws InvalidArgumentException If inventory object/pool does no have uuid
     */
    public void moveObjectsToPool(String targetClassName, String targetOid, HashMap<String, String[]> objects)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Move a list of objects to a new parent: this methods ignores those who can't be moved and raises
     * an OperationNotPermittedException, however, it will move those which can be moved
     * @param objects Map using the object class name as keys and the respective objects oids as values
     * @param targetClassName Parent's class name
     * @param targetOid Parent's oid
     * @throws MetadataObjectNotFoundException If the object's or new parent's class can't be found
     * @throws BusinessObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     * @throws InvalidArgumentException If inventory object/pool does not have uuid
     */
    public void moveObjects(String targetClassName, String targetOid, HashMap<String,String[]> objects)
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException;

     /**
     * Move a list of objects to a new parent(taking into account the special
     * hierarchy containment): this methods ignores those who can't be moved and raises an 
     * OperationNotPermittedException, however, it will move those which can be moved
     * @param objects Map using the object class name as keys and the respective objects oids as values
     * @param targetClassName Parent's class name
     * @param targetOid Parent's oid
     * @throws MetadataObjectNotFoundException If the object's or new parent's class can't be found
     * @throws BusinessObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     * @throws InvalidArgumentException If an inventory object/pool does not have uuid
     */
    public void moveSpecialObjects(String targetClassName, String targetOid, HashMap<String,String[]> objects)
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException;
    /**
     * Move a pool item from a pool to another pool
     * @param poolId The id of the pool node
     * @param poolItemClassName The class name for the pool item
     * @param poolItemId The id for the pool item
     * @throws ApplicationObjectNotFoundException If the pool node can not be found
     * @throws InvalidArgumentException If the pool item can not be move to the selected pool
     * @throws BusinessObjectNotFoundException If the pool item can not be found
     * @throws MetadataObjectNotFoundException If the pool item class name can no be found
     */
    public void movePoolItem(String poolId, String poolItemClassName, String poolItemId) throws 
        ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, 
        MetadataObjectNotFoundException;
    /**
     * Copy a set of objects
     * @param objects Hashmap with the objects class names as keys and their oids as values
     * @param targetClassName Target parent's class name
     * @param targetOid Target parent's oid
     * @param recursive If this operation should also copy the children objects recursively
     * @return A list containing the newly created object ids
     * @throws MetadataObjectNotFoundException If any of the provided classes couldn't be found
     * @throws BusinessObjectNotFoundException If any of the template objects couldn't be found
     * @throws OperationNotPermittedException If the target parent can't contain any of the new instances
     * @throws InvalidArgumentException If an inventory object id is null
     */
    public String[] copyObjects(String targetClassName, String targetOid, HashMap<String, List<String>> objects, boolean recursive)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException;

    /**
     * Copy a set of special objects (this is used to copy objects when they are containment are set in the special containment hierarchy)
     * use case: to move physical links into a wire Container
     * @param objects Hashmap with the objects class names as keys and their oids as values
     * @param targetClassName Target parent's class name
     * @param targetOid Target parent's oid
     * @param recursive If this operation should also copy the children objects recursively
     * @return A list containing the newly created object ids
     * @throws MetadataObjectNotFoundException If any of the provided classes couldn't be found
     * @throws BusinessObjectNotFoundException If any of the template objects couldn't be found
     * @throws InvalidArgumentException If the objects can not be copied to the specified parent (most likely due to containment restrictions).
     */
    public String[] copySpecialObjects(String targetClassName, String targetOid, HashMap<String, List<String>> objects, boolean recursive)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    /**
     * Copy a pool item from a pool to another pool
     * @param poolId The id of the pool node
     * @param poolItemClassName The class name for the pool item
     * @param poolItemId The id for the pool item
     * @param recursive If this operation should also copy the children objects recursively
     * @return The newly created object id
     * @throws ApplicationObjectNotFoundException If the pool node can not be found
     * @throws InvalidArgumentException If the pool item can not be move to the selected pool
     * @throws BusinessObjectNotFoundException If the pool item can not be found
     * @throws MetadataObjectNotFoundException If the pool item class name can no be found
     */
    public String copyPoolItem(String poolId, String poolItemClassName, String poolItemId, boolean recursive) throws 
        ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, 
        MetadataObjectNotFoundException;
    /**
     * Gets the children of a given object
     * @param className Object's class name
     * @param oid Object's oid
     * @param maxResults max number of children to be returned, -1 to return all
     * @return The list of children
     * @throws MetadataObjectNotFoundException If the object's can't be found
     * @throws BusinessObjectNotFoundException If the object or its new parent can't be found
     * @throws InvalidArgumentException If oid is null
     */
    public List<BusinessObjectLight> getObjectChildren(String className, String oid, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Gets the children of a given object, providing the class and object id.
     * @param classId The id of the class the object is instance of
     * @param oid The oid of the object
     * @param maxResults The max number of results to be retrieved. Use 0 to retrieve all
     * @return The list of children.
     * @throws BusinessObjectNotFoundException If the object could not be found.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     * @throws InvalidArgumentException If the oid is null
     */
    public List<BusinessObjectLight> getObjectChildren(long classId, String oid, int maxResults)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Gets the direct children of a given object of a given class.
     * @param parentOid parent id
     * @param parentClass Parent class
     * @param classToFilter Class to be match against
     * @param page the number of page of the number of elements to skip
     * @param maxResults max number of results. 0 to get all
     * @return A list of children of parentid/parentClass instance that are instances of classToFilter.
     * @throws MetadataObjectNotFoundException If any of the classes can not be found
     * @throws BusinessObjectNotFoundException If parent object can not be found
     * @throws InvalidArgumentException If the database objects can not be correctly mapped into serializable Java objects.
     */
    public List<BusinessObject> getChildrenOfClass(String parentOid, String parentClass, String classToFilter, int page, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Returns the special children of a given object as RemoteBusinessObjectLight instances.This method is not recursive.
     * @param parentOid The id of the parent object
     * @param parentClass The class name of the parent object
     * @param classToFilter The superclass/class to be used to filter the results. You can also use abstract superclasses.
     * @param maxResults The max number of results to fetch. Use -1 to retrieve all
     * @return The list of special children of the given object, filtered using classToFilter
     * @throws MetadataObjectNotFoundException If the parent class name provided could not be found
     * @throws BusinessObjectNotFoundException If the parent object could not be found
     * @throws org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException If the filter class is invalid.
     */
    public List<BusinessObjectLight> getSpecialChildrenOfClassLight(String parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    /**
     * Gets all class and abstract class children of a given class to filter in 
     * a hierarchy with root in the given parent.
     * i.e.: all the ports in Router, all the Routers in a City
     * @param parentOid Object id of the root parent of the hierarchy
     * @param parentClass Class name of the root parent of the hierarchy
     * @param classToFilter Class name of the expected children
     * @param attributesToFilters if filtering by the attributes of the retrieved objects
     * @param page The page or the number of elements to skip, no pagination -1
     * @param limit Maximum number of results, -1 no limit
     * @return The list of object instance of the given class to filter
     * @throws MetadataObjectNotFoundException If the parent class is not found
     * @throws BusinessObjectNotFoundException If the parent is not found
     * @throws InvalidArgumentException If the parent Id is null
     */
    public List<BusinessObjectLight> getChildrenOfClassLightRecursive(String parentOid, String parentClass, String classToFilter, HashMap <String, String> attributesToFilters, int page, int limit) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    /**
     * Gets all class and abstract class special children of a given class to filter 
     * in a hierarchy with root in the given parent.
     * Use case: used in some class level and inventory level reports script 
     * @param parentOid Object id of the root parent of the hierarchy
     * @param parentClass Class name of the root parent of the hierarchy
     * @param classToFilter Class name of the expected children
     * @param maxResults Maximum number of results, -1 no limit
     * @return The list of object instance of the given class to filter
     * @throws MetadataObjectNotFoundException If the parent class is not found
     * @throws BusinessObjectNotFoundException If the parent is not found
     * @throws InvalidArgumentException If the parent Id is null
     */
    public List<BusinessObjectLight> getSpecialChildrenOfClassLightRecursive(String parentOid, String parentClass, String classToFilter, int maxResults) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
            
    /**
     * Same as getChildrenOfClass, but returns only the light version of the objects
     * @param parentOid parent id
     * @param parentClass Parent class
     * @param classToFilter Class to be match against
     * @param maxResults max number of results. 0 to get all
     * @return A list of children of parentid/parentClass instance, instances of classToFilter
     * @throws MetadataObjectNotFoundException If any of the classes can not be found
     * @throws BusinessObjectNotFoundException If parent object can not be found
     * @throws InvalidArgumentException If the parent object id is null
     */
    public List<BusinessObjectLight> getChildrenOfClassLight(String parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;

    /**
     * Gets the siblings of a given object in the containment hierarchy
     * @param className Object class
     * @param oid Object oid
     * @param maxResults Max number of results to be returned
     * @return List of siblings
     * @throws MetadataObjectNotFoundException If the class does not exist
     * @throws BusinessObjectNotFoundException If the object does not exist
     * @throws InvalidArgumentException If the oid is null
     */
    public List<BusinessObjectLight> getSiblings(String className, String oid, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Recursively gets all the light instances of given class
     * @param className Class name. It must be a subclass of InventoryObject
     * @param filters map of filters key: attribute name, value: attribute value
     * @param page page or number of elements to skip
     * @param limit max count of child per page
     * @return a set of instances of the class
     * @throws InvalidArgumentException If the class name is null
     * @throws MetadataObjectNotFoundException If the provided class name doesn't exists
     */
    public List<BusinessObjectLight> getObjectsOfClassLight(String className, HashMap <String, String> filters, long page, long limit) throws InvalidArgumentException, MetadataObjectNotFoundException;
    
    /**
     * Recursively gets all the light instances of given class, without filters.
     * @param className Class name. It must be a subclass of InventoryObject
     * @param page page or number of elements to skip
     * @param limit max count of child per page
     * @return a set of instances of the class
     * @throws InvalidArgumentException If the class name is null
     * @throws MetadataObjectNotFoundException If the provided class name doesn't exists
     */
    public List<BusinessObjectLight> getObjectsOfClassLight(String className, long page, long limit) throws InvalidArgumentException, MetadataObjectNotFoundException;
    
    /**
     * Recursively gets all the instances of given class
     * @param className Class name. It mist be a subclass of InventoryObject
     * @param maxResults Max number of results. 0 to get all
     * @return a list of instances 
     * @throws MetadataObjectNotFoundException if the class can not be found
     * @throws InvalidArgumentException If the class is not subclass of InventoryObject
     */
    public List<BusinessObject> getObjectsOfClass(String className, int maxResults)
            throws MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Creates a relationship between two elements and labels it. Use with extreme care, since it can create arbitrary relationships
     * @param aObjectClass a side object class
     * @param aObjectId a side object id
     * @param bObjectClass b side object class
     * @param bObjectId b side object id
     * @param name Name to label the new relationship
     * @param unique If there could be only one relationship between both elements with that name
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException if any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException if any of the classes provided can not be found
     * @throws InvalidArgumentException If the a/bObjectId are null
     */
    public void createSpecialRelationship(String aObjectClass, String aObjectId, String bObjectClass, String bObjectId, String name, boolean unique)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * This method creates a special relationship  with a set of property values
     * @param aObjectClass The class of the first object to related
     * @param aObjectId The id of the first object to related
     * @param bObjectClass The class of the second object to related
     * @param bObjectId The id of the first object to related
     * @param name The name of the relationship
     * @param unique If there could be only one relationship between both elements with that name
     * @param properties A hash with the set of properties and their respective values
     * @throws BusinessObjectNotFoundException If any of the objects can not be found
     * @throws OperationNotPermittedException If, due to a business rule, the objects can not be related
     * @throws MetadataObjectNotFoundException If any of the classes specified does not exist
     * @throws InvalidArgumentException If the a/bObjectId are null
     */
    public void createSpecialRelationship(String aObjectClass, String aObjectId, String bObjectClass, 
        String bObjectId, String name, boolean unique, HashMap<String, Object> properties) 
        throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Release all special relationships with a given name
     * @param objectClass Object class
     * @param objectId Object id
     * @param otherObjectId The object we want to be released from. -1 To all objects related with relationships with that name
     * @param relationshipName Relationship name
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  If the class can not be found
     * @throws InvalidArgumentException If objectId or otherObjectId are null
     */
    public void releaseSpecialRelationship(String objectClass, String objectId, String otherObjectId, String relationshipName)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Release all special relationships with a given name whose target object id matches with the one provided
     * @param objectClass Object class
     * @param objectId Object id
     * @param relationshipName Relationship name
     * @param targetId Id of the object at the end of the relationship
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  If the class can not be found
     * @throws InvalidArgumentException If the object/targetId are null
     */
    public void releaseSpecialRelationshipInTargetObject(String objectClass, String objectId, String relationshipName, String targetId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Gets the value of a special attribute. A special attribute is one belonging to a business domain specific attribute
     * (usually a model. Domain specific attribute information is not filed under the standard metadata but a special one. Implementations may vary)
     * @param objectClass object's class
     * @param objectId object's id
     * @param specialAttributeName Special attribute name
     * @return A list of objects related to the object through a special relationship. An empty array if the object provided is not related to others using that relationship.
     * @throws BusinessObjectNotFoundException if the object can not be found
     * @throws MetadataObjectNotFoundException if either the object class or the attribute can not be found
     * @throws InvalidArgumentException If the object id is null
     */    
    public List<BusinessObjectLight> getSpecialAttribute(String objectClass, String objectId, String specialAttributeName) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * This method will extract the object at the other side of the special relationship and all the properties of the relationship itself
     * @param objectClass The class of the object whose special attribute will be retrieved from
     * @param objectId The object's id
     * @param specialAttributeName The name of the special attribute
     * @return The list of elements related with such relationship plus the properties of theirs relationships
     * @throws BusinessObjectNotFoundException If the object could not be found
     * @throws MetadataObjectNotFoundException If the object class could not be found
     * @throws InvalidArgumentException If the object id is null
     */
    public List<AnnotatedBusinessObjectLight> getAnnotatedSpecialAttribute(String objectClass, String objectId, String specialAttributeName) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Returns the specified special relationships of a given object as a hashmap whose keys are
     * the names of the relationships and the values the list of related objects. If no filter (attributeNames) is provided, all special attributes 
     * (relationships) will be returned
     * @param className Object class
     * @param objectId Object Id
     * @param attributeNames The list of special attributes (relationships) to be fetched. if none provided, the method will return all of them.
     * @return The hash map with the existing special relationships and the associated objects
     * @throws MetadataObjectNotFoundException If the class provided does not exist
     * @throws BusinessObjectNotFoundException if the object does not exist
     * @throws InvalidArgumentException If the object id is null
     */
    public HashMap<String,List<BusinessObjectLight>> getSpecialAttributes (String className, String objectId, String... attributeNames) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Checks if an object has a given number of standard relationships with another object
     * @param objectClass Object class
     * @param objectId Object id
     * @param relationshipName Relationship name
     * @param numberOfRelationships Number of relationships
     * @return True if the object has numberOfRelationships relationships with another object
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  If objectClass does not exist
     * @throws InvalidArgumentException If the object id is null
     */
    public boolean hasRelationship(String objectClass, String objectId, String relationshipName, int numberOfRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Check if an object has a given special attribute.
     * @param objectClass Object class
     * @param objectId Object id
     * @param attributeName Attribute name
     * @return True if the object has special attributes
     */
    public boolean hasSpecialAttribute(String objectClass, String objectId, String attributeName) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    /**
     * Counts if an object has children
     * @param objectClass Object class
     * @param objectId Object id
     * @return Number of children
     */
    public long countChildren(String objectClass, String objectId)
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    /**
     * Counts if an object has special children
     * @param objectClass
     * @param objectId
     * @return Number of special children
     */
    public long countSpecialChildren(String objectClass, String objectId)
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Releases all the relationships with the given names associated to the object provided. If no relationships with such names exist, the method 
     * will do nothing. <b>Use this method with extreme care, you can seriously affect the relational integrity of the system</b>
     * @param objectClass The class of the target object
     * @param objectId The id of the target object
     * @param relationshipsToRelease An array with the relationships to be released
     * @throws MetadataObjectNotFoundException If the class provided does not exist
     * @throws BusinessObjectNotFoundException If the object could not be found
     * @throws InvalidArgumentException If any of the relationships is now allowed according to the defined data model
     */
    public void releaseRelationships(String objectClass, String objectId, List<String> relationshipsToRelease) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Checks if an object has a given number of special relationships with another object
     * @param objectClass Object class
     * @param objectId Object id
     * @param relationshipName Relationship name
     * @param numberOfRelationships Number of relationships
     * @return True if the object has numberOfRelationships relationships with another object
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  if objectClass does not exist
     * @throws InvalidArgumentException If the object id is null
     */
    public boolean hasSpecialRelationship(String objectClass, String objectId, String relationshipName, int numberOfRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Finds all possible routes between two given inventory objects
     * @param objectAClassName Inventory object A class name
     * @param objectAId  Inventory object A id
     * @param objectBClassName  Inventory object B class name
     * @param objectBId  Inventory object B id
     * @param relationshipName The name of the relationship used to navigate through nodes and find the route
     * @return A list of the routes, including only the nodes as RemoteBusinessObjectLights
     * @throws InvalidArgumentException If any of the inventory objects does not have uuid
     */
    public List<BusinessObjectLightList> findRoutesThroughSpecialRelationships (String objectAClassName, String objectAId, String objectBClassName, String objectBId, String relationshipName) throws InvalidArgumentException;
    
    //Attachments management
    /**
     * Relates a file to an inventory object
     * @param name The name of the file
     * @param tags The tags that describe the contents of the file
     * @param file The file itself
     * @param className The class of the object the file will be attached to
     * @param objectId The id of the object the file will be attached to
     * @return The id of the resulting file object
     * @throws BusinessObjectNotFoundException If the inventory object could not be found
     * @throws OperationNotPermittedException If there's some sort of system restriction that prevented the file to be created
     * @throws MetadataObjectNotFoundException If the class provided does not exist
     * @throws InvalidArgumentException If the file size exceeds the max permitted (default value is 10MB)
     */
    public long attachFileToObject(String name, String tags, byte[] file, String className, 
            String objectId) throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Fetches the files associated to an inventory object. Note that this call won't retrieve the actual files, but only references to them
     * @param className The class of the object whose files will be fetched from
     * @param objectId The id of the object whose files will be fetched from
     * @return The list of files
     * @throws BusinessObjectNotFoundException If the object could not be found
     * @throws MetadataObjectNotFoundException If the class provided does not exist
     * @throws InvalidArgumentException If the object id is null
     */
    public List<FileObjectLight> getFilesForObject(String className, String objectId) 
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Retrieves a particular file associated to an inventory object. This call returns the actual file
     * @param fileObjectId The id of the file object
     * @param className The class of the object the file is associated to
     * @param objectId The id of the object the file is associated to
     * @return The file
     * @throws BusinessObjectNotFoundException If the object could not be found
     * @throws InvalidArgumentException If for some low level reason, the file could not be read from its original location
     * @throws MetadataObjectNotFoundException If the class provided does not exist
     */
    public FileObject getFile(long fileObjectId, String className, String objectId) throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException;
    /**
     * Releases (and deletes) a file associated to an inventory object
     * @param fileObjectId The id of the file object
     * @param className The class of the object the file is associated to
     * @param objectId The id of the object the file is associated to
     * @throws BusinessObjectNotFoundException If the object could not be found
     * @throws InvalidArgumentException If for some low level reason, the file could not be deleted from disk
     * @throws MetadataObjectNotFoundException If the class provided does not exist
     */
    public void detachFileFromObject(long fileObjectId, String className, String objectId) throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException;
    /**
     * Updates the properties of a file object (name or tags)
     * @param fileObjectId The id of the file object
     * @param properties The set of properties as a dictionary key-value. Valid keys are "name" and "tags"
     * @param className The class of the object the file is attached to
     * @param objectId The id of the object the file is attached to
     * @throws BusinessObjectNotFoundException If the object file is attached to could not be found
     * @throws ApplicationObjectNotFoundException If the file object could not be found
     * @throws InvalidArgumentException if any of the properties has an invalid name or if the file name is empty
     * @throws MetadataObjectNotFoundException If the class of the object file is attached to could not be found
     */
    public void updateFileProperties(long fileObjectId, List<StringPair> properties, String className, String objectId) throws BusinessObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException;
        
    /**
     * Retrieves a map with the files related to the list type item attributes of the given object
     * @param ObjectUuid the object id
     * @return The map with the files. The key is the list type item and the value a list with the related files
     */
    public Map<BusinessObjectLight, List<FileObjectLight>> getFilesFromRelatedListTypeItems(String ObjectUuid);
    
    //<editor-fold defaultstate="collapsed" desc="Contact Manager">
    /**
     * Creates a contact.Contacts are always associated to a customer
     * @param contactClass Class of the contact. This class should always be a subclass of GenericContact
     * @param customerClassName The class of the customer this contact will be associated to
     * @param customerId The id of the customer this contact will be associated to
     * @param userName The user name of the session
     * @return The id of the newly created contact
     * @throws BusinessObjectNotFoundException If the customer could not be found
     * @throws InvalidArgumentException If any of the properties or its value is invalid
     * @throws MetadataObjectNotFoundException If the customer class could not be found
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found
     */
    public String createContact(String contactClass, String customerClassName, String customerId, String userName) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException;

    /**
     * Updates a contact's information
     * @param contactClass The class of the contact
     * @param contactId The id of the contact
     * @param properties The attributes to be updated. The list types require only the id of the linked list type as a string
     * @param userName The user name of the session
     * @throws BusinessObjectNotFoundException If the contact could not be found
     * @throws InvalidArgumentException If any of the properties or its value is invalid
     * @throws MetadataObjectNotFoundException If the contact class could not be found
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found
     */
    public void updateContact(String contactClass, String contactId, List<StringPair> properties, String userName) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException;
    /**
     * Deletes a contact
     * @param contactClass The class of the contact
     * @param contactId The id of the contact
     * @param userName The user name of the session
     * @throws BusinessObjectNotFoundException If the contact could not be found
     * @throws InvalidArgumentException If the name is empty or there's an attempt to change the creationDate
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found
     */
    public void deleteContact(String contactClass, String contactId, String userName) throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException;
    /**
     * Gets the entire information of a given contact
     * @param contactClass The class of the contact
     * @param contactId The id of the contact
     * @return the contact
     * @throws BusinessObjectNotFoundException If the contact could not be found
     * @throws MetadataObjectNotFoundException If the contact class could not be found
     * @throws InvalidArgumentException If the contact is malformed and the customer it should be related to does not exist
     */
    public Contact getContact(String contactClass, String contactId) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Retrieves the list of contacts associated to a customer
     * @param customerClass The class of the customer the contacts belong to
     * @param customerId The id of the customer the contacts belong to
     * @return The list of contacts
     * @throws BusinessObjectNotFoundException If the customer could not be found
     * @throws MetadataObjectNotFoundException If the customer class could not be found
     * @throws InvalidArgumentException If an error occurs while building the contact objects
     */
    public List<Contact> getContactsForCustomer(String customerClass, String customerId) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Searches in all the properties of a contact for a given string
     * @param searchString The string to be matched
     * @param maxResults The max number of results. Use -1 to retrieve al results
     * @return The list of contacts that matches the search criteria
     * @throws InvalidArgumentException If an error occurs while building the contact objects
     */
    public List<Contact> searchForContacts(String searchString, int maxResults) throws InvalidArgumentException;
    
    /**
     * Retrieves the list of contacts that matches the search criteria
     * @param page current page
     * @param limit limit of results per page. -1 to retrieve them all
     * @param filters The response may be filtered 
     * by customer (use key <code>customer</code>, value the customer name, a String)
     * by type (use key <code>type</code>, value the type name, a String)
     * by contact name (use key <code>contact_name</code>, value the contact name, a String)
     * by contact email1 (use key <code>contact_email1</code>, value the contact email1, a String)
     * by contact email2 (use key <code>contact_email2</code>, value the contact email2, a String)
     * @return The list of contacts that matches the search criteria
     * @throws InvalidArgumentException If an error occurs while building the contact objects
     */
    public List<Contact> getContacts(int page, int limit, HashMap<String, Object> filters) throws InvalidArgumentException;
    
    /**
     * Relates an inventory object to a contact.
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param contactClass The class of the contact.
     * @param contactId The id of the contact.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If any of the objects can't be found.
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object).
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found.
     * @throws InvalidArgumentException If the a/b ObjectId are null.
     * @throws ApplicationObjectNotFoundException If the object activity log could not be found.
     */
    public void relateObjectToContact(String objectClass, String objectId, String contactClass, String contactId, String userName)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException;
    
    /**
     * Releases an inventory object from a contact.
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param contactClass The class of the contact.
     * @param contactId The id of the contact.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If the object can not be found.
     * @throws MetadataObjectNotFoundException If the class can not be found.
     * @throws InvalidArgumentException If contactId or objectId are null.
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found.
     */
    public void releaseObjectFromContact(String objectClass, String objectId, String contactClass, String contactId, String userName)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException;
    
    /**
     * Retrieves the list of resources (inventory objects) related to a contact. 
     * @param contactClass The class of the contact.
     * @param contactId The id of the contact.
     * @return List of related resources.
     * @throws BusinessObjectNotFoundException If the contact can't be found.
     * @throws MetadataObjectNotFoundException If the contract class provided can't be found.
     * @throws InvalidArgumentException If the contact is not subclass of GenericContact.
     */
    public List<BusinessObjectLight> getContactResources(String contactClass, String contactId)
             throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
//</editor-fold>
    
    //<editor-fold desc="Pools" defaultstate="collapsed">
    /**
     * Retrieves the pools that don't have any parent and are normally intended to be managed by the Pool Manager
     * @param className The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @param type The type of pools that should be retrieved. Root pools can be for general purpose, or as roots in models
     * @param includeSubclasses Use <code>true</code> if you want to get only the pools whose <code>className</code> property matches exactly the one provided, and <code>false</code> if you want to also include the subclasses
     * @return A set of pools
     * @throws InvalidArgumentException If a root pool does not have the uuid
     */
    public List<InventoryObjectPool> getRootPools(String className, int type, boolean includeSubclasses) throws InvalidArgumentException;
    /**
     * Retrieves the pools associated to a particular object
     * @param objectClassName The parent object class name
     * @param objectId The parent object id
     * @param poolClass The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @return A set of pools
     * @throws BusinessObjectNotFoundException If the parent object can not be found
     * @throws InvalidArgumentException If a pool does not have uuid
     * @throws MetadataObjectNotFoundException If the argument poolClass is not a valid class.
     */
    public List<InventoryObjectPool> getPoolsInObject(String objectClassName, String objectId, String poolClass) 
        throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException;
    
    /**
     * Retrieves the pools associated to a particular pool
     * @param parentPoolId The parent pool id
     * @param poolClassName The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @param page the page of the result to retrieve
     * @param limit the limit of elements per page
     * @return A set of pools
     * @throws ApplicationObjectNotFoundException If the parent object can not be found
     * @throws InvalidArgumentException If any pool does no have uuid
     */
    public List<InventoryObjectPool> getPoolsInPool(String parentPoolId, String poolClassName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException;
         
    /**
     * Retrieves the total count of pools associated to a particular pool
     * @param parentPoolId The parent pool id
     * @param poolClassName The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @return the total count of the pools
     * @throws ApplicationObjectNotFoundException If the parent object can not be found
     * @throws InvalidArgumentException If any pool does no have uuid
     */
    public long getPoolsInPoolCount(String parentPoolId, String poolClassName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException;
    /**
     * Gets a pool by its id 
     * @param poolId The pool's id
     * @return the pool as a Pool object
     * @throws ApplicationObjectNotFoundException If the pool could not be found
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid
     */
    public InventoryObjectPool getPool(String poolId) throws ApplicationObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Retrieves the list of objects from a pool
     * @param poolId Parent pool id
     * @param limit the results limit. per page 0 to avoid the limit
     * @return The list of items inside the pool
     * @throws ApplicationObjectNotFoundException If the pool id provided is not valid
     * @throws InvalidArgumentException If the pool item is an inventory object and it does not have the uuid
     */
    public List<BusinessObjectLight> getPoolItems(String poolId, int limit)
            throws ApplicationObjectNotFoundException, InvalidArgumentException;
    
     /**
     * Retrieves the list of objects from a pool
     * @param poolId Parent pool id
     * @param className a given className to retrieve a set of objects of that className form the pool
     * used when the pool is a Generic class and could have objects of different class
     * @param page the number of values of the result to skip or the page 0 to avoid
     * @param limit the results limit. per page 0 to avoid the limit
     * @return The list of items inside the pool
     * @throws ApplicationObjectNotFoundException If the pool id provided is not valid
     * @throws InvalidArgumentException If the pool item is an inventory object and it does not have the uuid
     */
    public List<BusinessObjectLight> getPoolItemsByClassName(String poolId, String className, int page, int limit)
            throws ApplicationObjectNotFoundException, InvalidArgumentException;
    /**
     * Count the total of objects in a pool
     * @param poolId Parent pool id
     * @param className a given className to retrieve a set of objects of that className form the pool
     * used when the pool is a Generic class and could have objects of different class
     * @return The count of items inside the pool
     * @throws ApplicationObjectNotFoundException If the pool id provided is not valid
     * @throws InvalidArgumentException If the pool item is an inventory object and it does not have the uuid
     */
    public long getPoolItemsCount(String poolId, String className)
            throws ApplicationObjectNotFoundException, InvalidArgumentException;
    //</editor-fold>
    
    /**
     * Reporting API. Reports are actually Application Objects, however, the BEM has many utility methods that can be used in the scripts to query for inventory objects
     */
    //<editor-fold desc="Reporting API" defaultstate="collapsed">
    /**
     * Creates a class level report (a report that will be available for all instances of a given class -and its subclasses-)
     * @param className Class this report is going to be related to. It can be ab abstract class and the report will be available for all its subclasses
     * @param reportName Name of the report.
     * @param reportDescription ReportMetadata description.
     * @param script Script text.
     * @param outputType What will be the default output of this report? See RemoteReportLight for possible values
     * @param enabled If enabled, a report can be executed.
     * @return The id of the newly created report.
     * @throws MetadataObjectNotFoundException If the class provided could not be found.
     */
    public long createClassLevelReport(String className, String reportName, String reportDescription, String script, 
            int outputType, boolean enabled) throws MetadataObjectNotFoundException;
    
    /**
     * Creates an inventory level report (a report that is not tied to a particlar instance or class. In most cases, they also receive parameters)
     * @param reportName Name of the report.
     * @param reportDescription ReportMetadata description.
     * @param script Script text.
     * @param outputType What will be the default output of this report? See InventoryLevelReportDescriptor for possible values
     * @param enabled If enabled, a report can be executed.
     * @param parameters Optional (it might be either null or an empty list). The list of the parameters that this report will support and optional default values. They will always be captured as strings, so it's up to the author of the report the sanitization and conversion of the inputs
     * @return The id of the newly created report.
     * @throws ApplicationObjectNotFoundException If the dummy root could not be found, which is actually a severe problem.
     * @throws InvalidArgumentException If any of the parameter names is null or empty
     */
    public long createInventoryLevelReport(String reportName, String reportDescription, String script, int outputType, 
            boolean enabled, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Deletes a report
     * @param reportId The id of the report.
     * @return The summary of the changes
     * @throws ApplicationObjectNotFoundException If the report could not be found.
     */
    public ChangeDescriptor deleteReport(long reportId) throws ApplicationObjectNotFoundException;
    
    /**
     * Updates the properties of an existing class level report.
     * @param reportId Id of the report.
     * @param reportName The name of the report. Null to leave it unchanged.
     * @param reportDescription The description of the report. Null to leave it unchanged.
     * @param enabled Is the report enabled? . Null to leave it unchanged.
     * @param type Type of the output of the report. See LocalReportLight for possible values
     * @param script Text of the script. 
     * @return The summary of the changes
     * @throws ApplicationObjectNotFoundException If the report could not be found.
     * @throws InvalidArgumentException If any of the report properties has a wrong or unexpected format.
     */
    public ChangeDescriptor updateReport(long reportId, String reportName, String reportDescription, Boolean enabled,
            Integer type, String script) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Updates the parameters of a report
     * @param reportId The id of the report
     * @param parameters The list of parameters and optional default values. Those with null values will be deleted and the ones that didn't exist previously will be created.
     * @return The summary of the changes
     * @throws ApplicationObjectNotFoundException If the report was not found.
     * @throws InvalidArgumentException If the any of the parameters has an invalid name.
     */
    public ChangeDescriptor updateReportParameters(long reportId, List<StringPair> parameters) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Gets the class level reports associated to the given class (or its superclasses)
     * @param className The class to extract the reports from.
     * @param recursive False to get only the directly associated reports. True top get also the reports associate top its superclasses
     * @param includeDisabled True to also include the reports marked as disabled. False to return only the enabled ones.
     * @return The list of reports.
     * @throws MetadataObjectNotFoundException If the class could not be found
     */
    public List<ReportMetadataLight> getClassLevelReports(String className, boolean recursive, boolean includeDisabled) throws MetadataObjectNotFoundException;
    /**
     * Gets the inventory class reports.
     * @param includeDisabled True to also include the reports marked as disabled. False to return only the enabled ones.
     * @return The list of reports.
     * @throws ApplicationObjectNotFoundException f the dummy root could not be found, which is actually a severe problem.
     */
    public List<ReportMetadataLight> getInventoryLevelReports(boolean includeDisabled) throws ApplicationObjectNotFoundException;
    
    /**
     * Gets the information related to a class level report.
     * @param reportId The id of the report.
     * @return  The report.
     * @throws ApplicationObjectNotFoundException If the report could not be found.
     */
    public ReportMetadata getReport(long reportId) throws ApplicationObjectNotFoundException;
    
    /**
     * Executes a class level report and returns the result.
     * @param objectClassName The class of the instance that will be used as input for the report.
     * @param objectId The id of the instance that will be used as input for the report.
     * @param reportId The id of the report.
     * @return The result of the report execution.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     * @throws ApplicationObjectNotFoundException If the report could not be found.
     * @throws BusinessObjectNotFoundException If the inventory object could not be found.
     * @throws InvalidArgumentException If there's an error during the execution of the report.
     */
    public byte[] executeClassLevelReport(String objectClassName, String objectId, long reportId) 
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Executes an inventory level report and returns the result.
     * @param reportId The id of the report.
     * @param parameters List of pairs param name - param value.
     * @return The result of the report execution.
     * @throws ApplicationObjectNotFoundException If the report could not be found.
     * @throws InvalidArgumentException If the associated script exits with error.
     */
    public byte[] executeInventoryLevelReport(long reportId, List<StringPair> parameters)
            throws ApplicationObjectNotFoundException, InvalidArgumentException;
    //</editor-fold>
    
    //<editor-fold desc="Warehouse" defaultstate="collapsed">
    /**
     * Gets the warehouses in a object
     * @param objectClassName The class of the object
     * @param objectId The id of the object
     * @return Gets the warehouses in a object
     * @throws MetadataObjectNotFoundException If the specified class could not be found.
     * @throws InvalidArgumentException 
     */
    public List<BusinessObjectLight> getWarehousesInObject(String objectClassName, String objectId) 
        throws MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Gets the physical node of a warehouse item
     * @param objectClassName The class of the object
     * @param objectId The id of the object
     * @return Gets the physical node of a warehouse item
     * @throws MetadataObjectNotFoundException If the specified class could not be found.
     * @throws BusinessObjectNotFoundException If the object provided could not be found.
     * @throws InvalidArgumentException 
     */
    public BusinessObjectLight getPhysicalNodeToObjectInWarehouse(String objectClassName, String objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    /**
     * Gets warehouse related to object
     * @param objectClassName The class of the object
     * @param objectId The id of the object
     * @return Gets warehouse related to object.
     * @throws MetadataObjectNotFoundException If the specified class could not be found.
     * @throws BusinessObjectNotFoundException If the object provided could not be found.
     * @throws InvalidArgumentException
     */
    public BusinessObjectLight getWarehouseToObject(String objectClassName, String objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    //</editor-fold>
    
    //<editor-fold desc="Kuwaiba 2.1" defaultstate="collapsed">
    /**
     * Get the child count given the parent class name and id.
     * @param className Parent class name
     * @param oid Parent id
     * @param filters map of filters key: attribute name, value: attribute value
     * @return The count of child
     * @throws InvalidArgumentException If the class name is null
     */
    public long getObjectChildrenCount(String className, String oid, HashMap <String, String> filters) throws InvalidArgumentException;
    
    /**
     * Get a set of children to the given the parent class name and id.
     * @param className Parent class name
     * @param oid Parent id
     * @param filters null for no filter, map of filters key: attribute name, value: attribute value
     * @param skip Skip index
     * @param limit Max count of child
     * @return Set of children
     * @throws InvalidArgumentException If the class name is null
     */
    public List<BusinessObjectLight> getObjectChildren(String className, String oid, HashMap <String, String> filters, long skip, long limit) throws InvalidArgumentException;

    /**
     * Suggests a number of objects based on a search string.
     * This search string will be case-insensitive-matched against the name of 
     * the objects and classes in the inventory attributes to filter.
     * @param List<ClassMetadataLight> classesToFilter a list of classes to limit the search
     * @param filter value to filter in the attribute name of every business object name or class name o class display name
     * @param skip Skip index
     * @param limit Max count of child
     * @return Set of children
     * @throws InvalidArgumentException If the class name, skip or limit are null
     */
    public HashMap<String, List<BusinessObjectLight>> getSuggestedObjectsWithFilterGroupedByClassName(
            List<String> classesNamesToFilter, String filter, long classesSkip
            , long classesLimit, long objectSkip, long objectLimit) throws InvalidArgumentException;
    
    /**
     * Suggests a number of pools based on a search string (the pool name).
     * This search string will be case-insensitive-matched against the name of 
     * the objects and classes in the inventory attributes to filter.
     * @param List<ClassMetadataLight> classesToFilter a list of classes to limit the search
     * @param filter value to filter in the attribute name of every business object name or class name o class display name
     * @param skip Skip index
     * @param limit Max count of child
     * @return Set of pools
     * @throws InvalidArgumentException If the class name, skip or limit are null
     */
    public HashMap<String, List<InventoryObjectPool>> getSuggestedPoolsByName(
            List<String> classesNamesToFilter, String nameTofilter, long poolSkip
            , long poolLimit, long objectSkip, long objectLimit) throws InvalidArgumentException;
    //</editor-fold>
}
