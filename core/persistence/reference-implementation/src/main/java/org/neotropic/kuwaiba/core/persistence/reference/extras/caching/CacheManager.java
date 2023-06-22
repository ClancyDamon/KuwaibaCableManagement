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

package org.neotropic.kuwaiba.core.persistence.reference.extras.caching;

import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.GenericObjectList;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.persistence.application.FilterDefinition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.Filter;

/**
 * Manages the caching strategy
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class CacheManager {
    /**
     * Singleton
     */
    private static CacheManager cm;
    /**
     * Cache slots to save complex objects
     */
    private HashMap<String, CacheSlot> cacheSlots;
    /**
     * Class cache
     */
    private HashMap<String, ClassMetadata> classIndex;
    /**
     * List type cache, the key is the list type 
     */
    private HashMap<String, GenericObjectList> listTypeIndex;
    /**
     * Possible children index. The key is the class, the value its possible children. Note that a blank key ("") represents the navigation tree root
     */
    private HashMap<String, List<ClassMetadata>> possibleChildrenIndex;
    /**
     * Possible special children index. The key is the class, the value its possible special children. Note that a blank key ("") represents the navigation tree root. The only difference with the possibleChildrenIndex, is that the relationship used to link the parent object with its children is CHILD_OF_SPECIAL
     */
    private HashMap<String, List<String>> possibleSpecialChildrenIndex;
    /**
     * List of subclasses of a class, the key is the name of the class, the value is the subclasses
     */
    private HashMap<String, List<ClassMetadataLight>> subClassesIndex;
    /**
     * List of subclasses of a class, the key is the name of the class, the value is the subclasses
     */
    private HashMap<String, List<ClassMetadataLight>> subClassesNoRecursiveIndex;
    /**
     * Users index. It is used to ease the username uniqueness validation
     */
    private HashMap<String, UserProfile> userIndex;
    /**
     * Groups index. It is used to ease the username uniqueness validation
     */
    private HashMap<String, GroupProfile> groupIndex;
    /**
     * List of the classes with unique attributes and its values index
     */
    private HashMap<String, HashMap<String, List<Object>>> uniqueClassAttributesIndex;
    /**
     * A structure that caches the superclasses of a given class (the key of the hashmap). This structure does contain redundant information, 
     * but that is the trade off to simplify the access to the upstream class hierarchy
     */
    private HashMap<String, List<ClassMetadataLight>> superClassIndex;
    /**
     * A structure that caches the validators associated to a given class (the key of the hash).
     */
    private HashMap<String, List<ValidatorDefinition>> validatorDefinitionIndex;
    /**
     * A structure that caches the filters associated to a given class (the key of the hash).
     */
    private HashMap<String, List<FilterDefinition>> filterDefinitionIndex;
    /**
     * Caches the existing configuration variables values (not definitions).
     */
    private HashMap<String, Object> configurationVariablesIndex;
    
    private CacheManager() {
        cacheSlots = new HashMap<>();
        classIndex = new HashMap<>();
        userIndex = new HashMap<>();
        groupIndex = new HashMap<>();
        possibleChildrenIndex = new HashMap<>();
        possibleSpecialChildrenIndex = new HashMap<>();
        subClassesIndex =  new HashMap<>();
        subClassesNoRecursiveIndex = new HashMap<>();
        uniqueClassAttributesIndex = new HashMap<>();
        listTypeIndex = new HashMap<>();
        superClassIndex = new HashMap<>();
        validatorDefinitionIndex = new HashMap<>();
        filterDefinitionIndex = new HashMap<>();
        configurationVariablesIndex = new HashMap<>();
    }

    public static CacheManager getInstance(){
        if (cm == null)
            cm = new CacheManager();
        return cm;
    }

    /**
     * Tries to retrieve a cached class
     * @param className the class to be retrieved from the cache
     * @return the cached version of the class. Null if it's  not cached
     */
    public ClassMetadata getClass(String className){
        return classIndex.get(className);
    }

    /**
     * Put/replaces an entry into the class cache
     * @param newClass
     */
    public void putClass(ClassMetadata newClass){
        classIndex.put(newClass.getName(), newClass);
    }
    
    /**
     * Remove a class from cache
     * @param className The class name
     */
    public void removeClass(String className){
        classIndex.remove(className);
    }

    /**
     * Adds an entry to the possible children index
     * @param parent
     * @param children
     */
    public void putPossibleChildren(String parent, List<ClassMetadata>children){
        possibleChildrenIndex.put(parent, children);
    }
    
    /**
     * Adds an entry to the possible special children index
     * @param parent The parent class
     * @param children the list of possible special children classes
     */
    public void putPossibleSpecialChildren(String parent, List<String>children){
        possibleSpecialChildrenIndex.put(parent, children);
    }

    public void putSubClassNoRecursive(String parent, ClassMetadataLight newSubClass){
        List<ClassMetadataLight> children = subClassesNoRecursiveIndex.get(parent);
        children.add(newSubClass);
        subClassesNoRecursiveIndex.put(parent, children);
    }
    /**
     * Adds an entry to the subclasses index
     * @param className the given class
     * @param subClasses  the subclasses of the given class
     */
    public void putSubclasses(String className, List<ClassMetadataLight> subClasses){
        subClassesIndex.put(className, subClasses);
    }
    
    /**
     * Adds an entry to the subclasses index
     * @param className the given class
     * @param subClasses  the subclasses of the given class
     */
    public void putSubclassesNorecursive(String className, List<ClassMetadataLight> subClasses){
        subClassesNoRecursiveIndex.put(className, subClasses);
    }
    
    /**
     * adds an entry for every unique attribute value of every class that has unique attributes
     * @param className class name
     * @param attributeName attribute name 
     * @param value new value of an unique attribute
     */
    public void putUniqueAttributeValueIndex(String className, String attributeName, Object value) {
        HashMap<String, List<Object>> uniqueClassAttributes = uniqueClassAttributesIndex.get(className);
        if(uniqueClassAttributes == null) {
            uniqueClassAttributes = new HashMap<>();
            List<Object> values = new ArrayList<>();
            if(value != null) //maybe still there is no object of this class with this unique attribute
                values.add(value);
            uniqueClassAttributes.put(attributeName, values);
            uniqueClassAttributesIndex.put(className, uniqueClassAttributes);
        }
        else {
            List<Object> values = uniqueClassAttributes.get(attributeName);
            if(values == null) {
                values = new ArrayList<>();
                values.add(value);
                uniqueClassAttributes.put(attributeName, values);
                uniqueClassAttributesIndex.put(className, uniqueClassAttributes);
            }
            else {
                values.add(value);
                uniqueClassAttributes.put(attributeName, values);
                uniqueClassAttributesIndex.put(className, uniqueClassAttributes);
            }
        }
    }
    
    public void putUniqueAttributeValuesIndex(String className, String attributeName, List<Object> values){
        HashMap<String, List<Object>> uniqueClassAttributes = uniqueClassAttributesIndex.get(className);
        if(uniqueClassAttributes == null){
            uniqueClassAttributes = new HashMap<>();
            if(values != null)
                uniqueClassAttributes.put(attributeName, values);
            uniqueClassAttributesIndex.put(className, uniqueClassAttributes);
        }
        else if(values != null){
            uniqueClassAttributes.put(attributeName, values);
            uniqueClassAttributesIndex.put(className, uniqueClassAttributes);
        }
    }
    
    /**
     * Adds an entry to the possible children index
     * @param parent
     * @param child
     */
    public void putPossibleChild(String parent, ClassMetadata child){
        List<ClassMetadata> myList = possibleChildrenIndex.get(parent);
        if (myList != null) {
            myList.add(child);
            myList.sort((classNameA, classNameB) -> { //Sorts the list everytime a new entry is added
                return classNameA.getName().compareTo(classNameB.getName());
            });
        }
    }
    
    /**
     * Adds a single entry to the possible special children index
     * @param parent
     * @param child
     */
    public void putPossibleSpecialChild(String parent, String child){
        List<String> myList = possibleSpecialChildrenIndex.get(parent);
        if (myList != null) {
            myList.add(child);
            myList.sort((classNameA, classNameB) -> { //Sorts the list everytime a new entry is added
                return classNameA.compareTo(classNameB);
            });
        }
    }

    public List<ClassMetadata> getPossibleChildren(String parent){
        if (parent == null)
            return possibleChildrenIndex.get(Constants.NODE_DUMMYROOT);
        return possibleChildrenIndex.get(parent);
    }
    
    public List<String> getPossibleSpecialChildren(String parent){
        if (parent == null)
            return possibleSpecialChildrenIndex.get(Constants.NODE_DUMMYROOT);
        return possibleSpecialChildrenIndex.get(parent);
    }
    
    public List<ClassMetadataLight> getSubclasses(String className){
        return subClassesIndex.get(className);
    }
    
    public List<ClassMetadataLight> getSubclassesNorecursive(String className){
        return subClassesNoRecursiveIndex.get(className);
    }
    
    public HashMap<String, List<Object>> getUniqueClassAttributes(String className){
        return uniqueClassAttributesIndex.get(className);
    }
    
    public List<Object> getUniqueAttributeValues(String className, String attributeName){
        if (uniqueClassAttributesIndex.get(className) != null)
            return uniqueClassAttributesIndex.get(className).get(attributeName);
        else
            return null;
    }

    /**
     * Tries to retrieve a cached user
     * @param userName the class to be retrieved from the cache
     * @return the cached version of the class. Null if it's  not cached
     */
    public UserProfile getUser(String userName){
        return userIndex.get(userName);
    }

    /**
     * Put/replaces an entry into the users cache
     * @param newUser user to be added
     */
    public void putUser(UserProfile newUser){
        userIndex.put(newUser.getUserName(), newUser);
    }

    /**
     * Tries to retrieve a cached user
     * @param groupName the class to be retrieved from the cache
     * @return the cached version of the group. Null if it's  not cached
     */
    public GroupProfile getGroup(String groupName){
        return groupIndex.get(groupName);
    }

    /**
     * Put/replaces an entry into the group cache
     * @param newGroup user to be added
     */
    public void putGroup(GroupProfile newGroup){
        groupIndex.put(newGroup.getName(), newGroup);
    }

    /**
     * Removes an entry into the users cache
     * @param userName
     */
    public void removeUser(String userName){
        userIndex.remove(userName);
    }

    /**
     * Removes an entry into the group cache
     * @param groupName
     */
    public void removeGroup(String groupName){
        userIndex.remove(groupName);
    }
    
    public void removeUniqueAttribute(String className, String attributeName){
        if (uniqueClassAttributesIndex.containsKey(className))
            uniqueClassAttributesIndex.get(className).remove(attributeName);
    }    
    
    public void removeUniqueAttributeValue(String className, String attributeName, Object attributeValue) {
        HashMap<String, List<Object>> uniqueClassAttributes = uniqueClassAttributesIndex.get(className);
        if (uniqueClassAttributes == null)
            uniqueClassAttributes = new HashMap<>();
        List<Object> uniqueValues = uniqueClassAttributes.get(attributeName);
        if(uniqueValues != null)
            uniqueValues.remove(attributeValue);
        uniqueClassAttributes.put(attributeName, uniqueValues);
        uniqueClassAttributesIndex.put(className, uniqueClassAttributes);
    }
    /**
     * Tries to retrieve a cached list type
     * @param listTypeName the list type to be retrieved from the cache
     * @return the cached version of the group. Null if it's  not cached
     */
    public GenericObjectList getListType(String listTypeName){
        return listTypeIndex.get(listTypeName);
    }
    
    /**
     * Put/replaces an entry into the list type cache
     * @param newListType list type to be added
     */
    public void putListType(GenericObjectList newListType){
        listTypeIndex.put(newListType.getClassName(), newListType);
    }
    
    /**
     * Removes an entry into the list type
     * @param listTypeName list type to be deleted
     */
    public void removeListType(String listTypeName){
        listTypeIndex.remove(listTypeName);
    }
    
    /**
     * Adds or replaces an entry in the upstream class hierarchy index.
     * @param className The name of the class the provided class hierarchy belongs to.
     * @param superClasses The super classes of the given class (including itself)
     */
    public void addUpstreamClassHierarchy(String className, List<ClassMetadataLight> superClasses) {
        superClassIndex.put(className, superClasses);
    }
    
    /**
     * Retrieves the cached upstream class hierarchy of a given class.
     * @param className The name of the class.
     * @return The list of cached super classes up to RootObject (including itself). Null of the class provided is not cached
     */
    public List<ClassMetadataLight> getUpstreamClassHierarchy(String className) {
        return superClassIndex.get(className);
    }
    
    /**
     * Adds or replaces an entry in the validator definitions index.
     * @param className The name of the class the provided class hierarchy belongs to.
     * @param validatorDefinitions The super classes of the given class (including itself)
     */
    public void addValidatorDefinitions(String className, List<ValidatorDefinition> validatorDefinitions) {
        validatorDefinitionIndex.put(className, validatorDefinitions);
    }
    
    /**
     * Retrieves the cached validator definitions associated to the given class.
     * @param className The name of the class.
     * @return The list of cached validator definitions. Null of the class provided is not cached
     */
    public List<ValidatorDefinition> getValidatorDefinitions(String className) {
        return validatorDefinitionIndex.get(className);
    }
    
    /**
     * Clears the cached validator definitions
     */
    public void clearValidatorDefinitionsCache() {
        validatorDefinitionIndex.clear();
    }
    
    /**
     * Adds or replaces an entry of a single filter in all the filter definitions index for a class.
     * @param className The name of the class 
     * @param filterDefinition a filter definition for the class to be included in the cache
     */
    public void addFilterDefinition(String className, FilterDefinition filterDefinition) {
        if(filterDefinitionIndex.get(className) == null)
            filterDefinitionIndex.put(className, new ArrayList<>());
        
        if(!filterDefinitionIndex.get(className).contains(filterDefinition))
            filterDefinitionIndex.get(className).add(filterDefinition);
    }
    
    /**
     * Updates or replaces the Filter of an entry in the filter definitions index.
     * this happen when the script has changes an ins compiled again
     * @param className The name of the class the provided class hierarchy belongs to.
     * @param filterDefinition the filter definition to update
     */
    public void updateFilterInFilterDefinition(String className, FilterDefinition filterDefinition) {
        if(filterDefinitionIndex.get(className) == null)
            filterDefinitionIndex.put(className, new ArrayList<>());

        if(!filterDefinitionIndex.get(className).contains(filterDefinition))
            filterDefinitionIndex.get(className).add(filterDefinition);
        else
            filterDefinitionIndex.get(className).set(filterDefinitionIndex.get(className).indexOf(filterDefinition), filterDefinition);
    }
    
    /**
     * Retrieves the cached filter definitions associated to the given class.
     * @param className The name of the class.
     * @return The list of cached filter definitions. Null of the class provided is not cached
     */
    public List<FilterDefinition> getFilterDefinitionsForClass(String className) {
        return filterDefinitionIndex.get(className);
    }
    
    /**
     * Retrieves the cached filter definitions associated to the given class.
     * @param className The name of the class.
     * @param filterId the filter definition id.
     * @return The list of cached filter definitions. Null of the class provided is not cached
     */
    public FilterDefinition getFilterDefinitionByClassNameAndFilterId(String className, long filterId) {
        List<FilterDefinition> filters = filterDefinitionIndex.get(className);
        if(filters != null)
            for (FilterDefinition filter : filters) {
                if(filter.getId() == filterId)
                    return filter;
            }
        return null;
    }
    
    /**
     * Removes a cached filter definition
     * @param className The name of the class.
     * @param filterId the filter definition id.
     */
    public void removeFilterDefinitionsForClass(String className, long filterId) {
        if(filterDefinitionIndex.get(className) != null){
            FilterDefinition filterToRemove = null;
            for(FilterDefinition f : filterDefinitionIndex.get(className))
                if(f.getId() == filterId)
                    filterToRemove = f;
            
            if(filterToRemove != null)
                filterDefinitionIndex.get(className).remove(filterToRemove);
        }
    }
    
    /**
     * Clears the cached filter definitions
     */
    public void clearFiltersCache() {
        filterDefinitionIndex.clear();
    }
    
    /**
     * Adds (or replaces) the value of a configuration variable.
     * @param configVariableName The name of the configuration variable.
     * @param configVariableValue  The value of the configuration value.
     */
    public void addConfigurationValue(String configVariableName, Object configVariableValue) {
        configurationVariablesIndex.put(configVariableName, configVariableValue);
    }
    
    /**
     * Gets the cached value of a configuration variable.
     * @param configVariableName The name of the configuration variable.
     * @return The value of the config variable, or null if it is not cached.
     */
    public Object getConfigurationVariableValue(String configVariableName) {
        return configurationVariablesIndex.get(configVariableName);
    }
    
    /**
     * Removes a cached value of a configuration variable (if present).
     * @param configVariableName The name of the config variable.
     */
    public void removeConfigurationVariableValue(String configVariableName) {
        configurationVariablesIndex.remove(configVariableName);
    }
    
    /**
     * Clears all cached information
     */
    public void clearAll() {
        classIndex.clear();
        userIndex.clear();
        groupIndex.clear();
        listTypeIndex.clear();
        configurationVariablesIndex.clear();
        validatorDefinitionIndex.clear();
        filterDefinitionIndex.clear();
        clearClassCache();
    }
    
    /**
     * Clears only the cached elements associated to the data model. Call it after 
     * performing any change in the class hierarchy of the property of the classes. 
     * Note that this method doesn't actually clears the main classIndex, only de dependencies, such as 
     * the possibleChildrenIndex, possibleSpecialChildrenIndex, subClassesIndex, 
     * subClassesNoRecursiveIndex and uniqueClassAttributesIndex
     */
    public void clearClassCache() {
        possibleChildrenIndex.clear();
        possibleSpecialChildrenIndex.clear();
        subClassesIndex.clear();
        subClassesNoRecursiveIndex.clear();
        uniqueClassAttributesIndex.clear();
        validatorDefinitionIndex.clear();
        superClassIndex.clear();
    }

    public HashMap<String, CacheSlot> getCacheSlots() {
        return cacheSlots;
    }
    
    public CacheSlot getCacheSlot(String cacheSlotName){
        CacheSlot slot = cacheSlots.get(cacheSlotName);
        if(slot == null)
            return null;
        else if(slot.isExpired())
            return null;
        else 
            return slot;
    }
    
    public void putCacheSlot(String cacheSlotName, Object content, int hoursOfValidity){
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.MINUTE, hoursOfValidity);
        cacheSlots.put(cacheSlotName, new CacheSlot(content, currentDate.getTime(), cal.getTimeInMillis()));
    }

    public void setCacheSlots(HashMap<String, CacheSlot> cacheSlots) {
        this.cacheSlots = cacheSlots;
    }
}
