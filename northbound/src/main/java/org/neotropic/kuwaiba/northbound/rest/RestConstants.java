/*
 *  Copyright 2023 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.northbound.rest;

/**
 * Set of constants to use in the REST Controllers.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class RestConstants {
    public static final String HEADER_USER = "user";
    public static final String HEADER_PASSWORD = "password";
    public static final String HEADER_TOKEN = "token";
    public static final String HEADER_ERROR = "error";
    /**
     * General
     */
    public static final String NAME = "name";
    public static final String NAMES = "names";
    public static final String DESCRIPTION = "description";
    public static final String ENABLED = "enabled";
    public static final String ID = "id";
    public static final String IDS = "ids";
    public static final String POOL_ID = "poolId";
    public static final String SCRIPT = "script";
    public static final String VALUE = "value";
    public static final String PROPERTY = "property";
    public static final String PARENT_CLASS_NAME = "parentClassName";
    public static final String PARENT_CLASS_ID = "parentClassId";
    public static final String PARENT_ID = "parentId";
    public static final String PARENT_POOL_ID = "parentPoolId";
    public static final String OBJECT_ID = "objectId";
    public static final String OBJECT_CLASS_NAME = "objectClassName";
    public static final String OBJECTS_CLASS_NAMES = "objectsClassNames";
    public static final String OBJECTS_IDS = "objectsIds";
    public static final String FOLDER_ID = "folderId";
    public static final String LIMIT = "limit";
    public static final String CLASS_ID = "classId";
    public static final String CLASS_NAME = "className";
    public static final String CLASS_NAMES = "classNames";
    public static final String SUPER_CLASS_NAME = "superClassName";
    public static final String CLASS_NAME_TO_FILTER = "classNameToFilter";
    public static final String DISPLAY_NAME = "displayName";
    public static final String PAGE = "page";
    public static final String SKIP = "skip";
    public static final String OBJECT_SKIP = "objectSkip";
    public static final String OBJECT_LIMIT = "objectLimit";
    public static final String CLASSES_SKIP = "classesSkip";
    public static final String CLASSES_LIMIT = "classesLimit";
    public static final String IGNORE_DISABLED = "ignoreDisabled";
    public static final String FILTER_NAME = "filterName";
    public static final String FILTER_VALUE = "filterValue";
    public static final String MANDATORY = "mandatory";
    public static final String OWNER_ID = "ownerId";
    public static final String SHOW_PUBLIC = "showPublic";
    public static final String STRUCTURE = "structure";
    public static final String VERSION = "version";
    public static final String VIEW_CLASS_NAME = "viewClassName";
    public static final String BACKGROUND = "background";
    public static final String VIEW_ID = "viewId";
    public static final String TAGS = "tags";
    public static final String FILE = "file";
    public static final String SHOW_ALL = "showAll";
    public static final String TYPE = "type";
    public static final String SCOPE = "scope";
    public static final String APPLIES_TO = "appliesTo";
    public static final String CONSTRAINTS = "constraints";
    public static final String SOURCE_OBJECT_CLASS_NAME = "sourceObjectClassName";
    public static final String SOURCE_OBJECT_ID = "sourceObjectId";
    public static final String TARGET_OBJECT_CLASS_NAME = "targetObjectClassName";
    public static final String TARGET_OBJECT_ID = "targetObjectId";
    public static final String ICON = "icon";
    public static final String SMALL_ICON = "smallIcon";
    public static final String INCLUDE_LIST_TYPES = "includeListTypes";
    public static final String INCLUDE_INDESIGN = "includeIndesign";
    public static final String INCLUDE_ABSTRACT_CLASSES = "includeAbstractClasses";
    public static final String INCLUDE_SELF = "includeSelf";
    public static final String ALLEGED_PARENT  = "allegedParent";
    public static final String RECURSIVE  = "recursive";
    public static final String IGNORE_ABSTRACT  = "ignoreAbstract";
    public static final String CHILD_TO_BE_EVALUATED = "childToBeEvaluated";
    public static final String POSSIBLE_CHILDREN = "possibleChildren";
    public static final String POSSIBLE_SPECIAL_CHILDREN = "possibleSpecialChildren";
    public static final String CHILDREN_TO_BE_REMOVED = "childrenToBeRemoved";
    public static final String RELATIONSHIP_NAME = "relationshipName";
    public static final String RELATIONSHIP_DISPLAY_NAME = "relationshipDisplayName";
    public static final String TEMPLATE_ID = "templateId";
    public static final String SPECIAL_OBJECT_CLASS = "specialObjectClass";
    public static final String SPECIAL_OBJECT_ID = "specialObjectId";
    public static final String MAX_RESULTS = "maxResults";
    public static final String CHILDREN_CLASS_NAMES_TO_FILTER = "childrenClassNamesToFilter";
    public static final String IGNORE_SPECIAL_CHILDREN = "ignoreSpecialChildren";
    public static final String POOL_ITEM_CLASS_NAME = "poolItemClassName";
    public static final String POOL_ITEM_ID = "poolItemId";
    public static final String ATTRIBUTE_NAME = "attributeName";
    public static final String SPECIAL_ATTRIBUTE_NAME = "specialAttributeName";
    public static final String A_OBJECT_CLASS_NAME = "aObjectClassName";
    public static final String B_OBJECT_CLASS_NAME = "bObjectClassName";
    public static final String A_OBJECT_ID = "aObjectId";
    public static final String B_OBJECT_ID = "bObjectId";
    public static final String OBJECT_TO_MATCH_CLASS_NAMES = "objectToMatchClassNames";
    public static final String OBJECT_TO_MATCH_CLASS_NAME = "objectToMatchClassName";
    public static final String CHILD_CLASS_NAME = "childClassName";
    public static final String CHILD_ID = "childId";
    public static final String INCLUDE_SUBCLASSES = "includeSubclasses";
    public static final String RELEASE_RELATIONSHIPS = "releaseRelationships";
    public static final String POOL_CLASS_NAME = "poolClassName";
    public static final String POOL_SKIP = "poolSkip";
    public static final String POOL_LIMIT = "poolLimit";
    public static final String CRITERIA = "criteria";
    public static final String POOL_NAME = "poolName";
    public static final String POOL_DESCRIPTION = "poolDescription";
    /**
     * Session Manager
     */
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String SESSION_TYPE = "sessionType";
    public static final String SESSION_ID = "sessionId";
    public static final String TOKEN = "token";
    public static final String USER_NAME = "userName";
    public static final String METHOD_NAME = "methodName";
    /**
     * List Type Manager
     */
    public static final String LIST_TYPE_ITEM_ID = "listTypeItemId";
    public static final String LIST_TYPE_ITEM_CLASS_NAME = "listTypeItemClassName";
    /**
     * User Manager 
     */
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String EMAIL = "email";
    public static final String PRIVILEGES = "privileges";
    public static final String GROUP_ID = "groupId";
    public static final String USERS = "users";
    public static final String USER_ID = "userId";
    public static final String FEATURE_TOKEN = "featureToken";
    public static final String ACCESS_LEVEL = "accessLevel";
    /**
     * Configuration Variables
     */
    public static final String MASKED = "masked";
    public static final String PREFIX = "prefix";
    /**
     * Task Manager
     */
    public static final String COMMIT_ON_EXECUTE = "commitOnExecute";
    /**
     * Configuration Filters
     */
    public static final String INCLUDE_PARENT_CLASSES_FILTERS = "includeParentClassesFilters";
    public static final String IGNORE_CACHE = "ignoreCache";
    /**
     * Template Manager
     */
    public static final String NAME_PATTERN = "namePattern";
    public static final String ATTRIBUTE_NAMES = "attributeNames";
    public static final String ATTRIBUTE_VALUES = "attributeValues";
    public static final String SOURCE_OBJECTS_NAMES = "sourceObjectsClassNames";
    public static final String SOURCE_OBJECTS_IDS = "sourceObjectsIds";
    public static final String NEW_PARENT_CLASS_NAME = "newParentClassName";
    public static final String NEW_PARENT_ID = "newParentId";
    /**
     * Process Manager
     */
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";
    public static final String ACTIVITY_ID = "activityId";
    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";
    public static final String ACTIVITY_DEFINITION_ID = "activityDefinitionId";
    /**
     * Contact
     */
    public static final String CONTACT_ID = "contactId";
    public static final String CONTACT_CLASS_NAME = "contactClassName";
    public static final String CUSTOMER_CLASS_NAME = "customerClassName";
    public static final String CUSTOMER_ID = "customerId";
    public static final String SEARCH_STRING = "searchString";
    /**
     * Report
     */
    public static final String REPORT_ID = "reportId";
    public static final String REPORT_NAME = "reportName";
    public static final String REPORT_DESCRIPTION = "reportDescription";
    public static final String OUTPUT_TYPE = "outputType";
    public static final String INCLUDE_DISABLED = "includeDisabled";
    /**
     * Attachment
     */
    public static final String FILE_OBJECT_ID = "fileObjectId";
    /**
     * Contract
     */
    public static final String CONTRACT_CLASS_NAME = "contractClassName";
    public static final String CONTRACT_ID = "contractId";
}