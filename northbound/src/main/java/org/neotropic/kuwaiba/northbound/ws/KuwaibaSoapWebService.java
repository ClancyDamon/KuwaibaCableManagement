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

package org.neotropic.kuwaiba.northbound.ws;

import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.northbound.ws.todeserialize.TransientQuery;
import org.neotropic.kuwaiba.northbound.ws.model.application.ApplicationLogEntry;
import org.neotropic.kuwaiba.northbound.ws.model.application.GroupInfo;
import org.neotropic.kuwaiba.northbound.ws.model.application.GroupInfoLight;
import org.neotropic.kuwaiba.northbound.ws.model.application.PrivilegeInfo;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteActivityDefinition;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteArtifact;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteArtifactDefinition;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteBackgroundJob;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteBusinessRule;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteConfigurationVariable;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteFavoritesFolder;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteInventoryProxy;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemotePool;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteProcessDefinition;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteQuery;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteQueryLight;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteReportMetadata;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteReportMetadataLight;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSession;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSynchronizationConfiguration;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSynchronizationGroup;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSynchronizationProvider;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteTask;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteTaskResult;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteValidator;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteValidatorDefinition;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteViewObject;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteViewObjectLight;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteResultRecord;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSyncAction;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSyncFinding;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSyncResult;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteTaskNotificationDescriptor;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteTaskScheduleDescriptor;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteUserInfo;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteUserInfoLight;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteAssetLevelCorrelatedInformation;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteContact;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteFileObject;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteFileObjectLight;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteLogicalConnectionDetails;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteMPLSConnectionDetails;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteObject;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteObjectLight;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteObjectLightList;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteObjectRelatedObjects;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteObjectSpecialRelationships;
import org.neotropic.kuwaiba.northbound.ws.model.business.modules.sdh.RemoteSDHContainerLinkDefinition;
import org.neotropic.kuwaiba.northbound.ws.model.business.modules.sdh.RemoteSDHPosition;
import org.neotropic.kuwaiba.northbound.ws.model.metadata.RemoteAttributeMetadata;
import org.neotropic.kuwaiba.northbound.ws.model.metadata.RemoteClassMetadata;
import org.neotropic.kuwaiba.northbound.ws.model.metadata.RemoteClassMetadataLight;

/**
 * The principal northbound interface. A simple, yet comprehensive, SOAP-based web service.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@WebService
public interface KuwaibaSoapWebService {
    // <editor-fold defaultstate="collapsed" desc="Application methods. Click on the + sign on the left to edit the code.">
    /**
     * Creates a session. Only one session per type is allowed. If a new session is created and there was already one of the same type, 
     * the old one will be discarded. See RemoteSession.TYPE_XXX for possible session types. System users can not create sessions.
     * @param username user login name
     * @param password user password
     * @param sessionType The type of session to be created. This type depends on what kind of client is trying to access (a desktop client, a web client, a web service user, etc. See RemoteSession.TYPE_XXX for possible session types
     * @return A session object, including the session token
     * @throws ServerSideException If the user does not exist
     *                             If the password is incorrect or if the user is not enabled.
     */
    @WebMethod(operationName = "createSession")
    public RemoteSession createSession(@WebParam(name = "username") String username,
            @WebParam(name = "password") String password, @WebParam(name = "sessionType") int sessionType) throws ServerSideException;
    /**
     * Closes a session
     * @param sessionId Session token
     * @throws ServerSideException If the session ID is Invalid or the IP does not match with the one registered for this session
     */
    @WebMethod(operationName = "closeSession")
    public void closeSession(@WebParam(name = "sessionId")String sessionId) throws ServerSideException ;

   /**
     * Retrieves the list of uses
     * @param sessionId session token
     * @return The list of users
     * @throws ServerSideException If the user is not allowed to invoke the method
     */
    @WebMethod(operationName = "getUsers")
    public List<RemoteUserInfo> getUsers(@WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the users in a group
     * @param groupId The id of the group
     * @param sessionId Session token
     * @return The list of users in the requested group
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the group does not exist or something unexpected happened.
     */
    @WebMethod(operationName = "getUsersInGroup")
    public List<RemoteUserInfo> getUsersInGroup(@WebParam(name = "groupId")long groupId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the groups a user belongs to
     * @param userId  The id of the user
     * @param sessionId Session token
     * @return The list of groups the user belongs to
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the group does not exist or something unexpected happened.
     */
    @WebMethod(operationName = "getGroupsForUser")
    public List<GroupInfoLight> getGroupsForUser(@WebParam(name = "userId")long userId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the list of groups
     * @param sessionId Session token
     * @return A group object list
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             or any possible error raised at runtime
     */
    @WebMethod(operationName = "getGroups")
    public List<GroupInfo> getGroups(@WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Creates a user. System users can be created but not deleted or modified. <b>Create system users only if you are a developer</b>, 
     * as they can only be modified or deleted by accessing directly to the database.
     * @param username User name. Can't be null, empty or have non standard characters.
     * @param password A password (in plain text, it'll be encrypted later). Can't be null nor an empty string
     * @param firstName User's first name
     * @param lastName User's last name
     * @param enabled Is this user enable by default?
     * @param type The type of the user. See UserProfileLight.USER_TYPE* for possible values
     * @param email User's email
     * @param privileges A list privileges that will be granted to this user.
     * @param defaultGroupId Default group this user will be associated to. Users <b>always</b> belong to at least one group. Other groups can be added later.
     * @param sessionId Session token
     * @return The new user Id
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the username is null or empty or the username already exists, 
     * if the user type is invalid or if the password is an empty string, or if it is attempted to change 
     * the user name of the admin user name, or if this operation is attempted on a system user. Also, if the new user type is invalid.
     */
    @WebMethod(operationName = "createUser")
    public long createUser(
            @WebParam(name = "username")String username,
            @WebParam(name = "password")String password,
            @WebParam(name = "firstName")String firstName,
            @WebParam(name = "lastName")String lastName,
            @WebParam(name = "enabled")boolean enabled,
            @WebParam(name = "type") int type,
            @WebParam(name = "email") String email,
            @WebParam(name = "privileges")List<PrivilegeInfo> privileges,
            @WebParam(name = "defaultGroupId")long defaultGroupId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Sets the properties of a given user using the id to search for it
     * @param oid User id
     * @param username New user's name. Use null to leave it unchanged.
     * @param password New user's password. Use null to leave it unchanged
     * @param firstName New user's first name. Use null to leave it unchanged
     * @param lastName New user's last name. Use null to leave it unchanged
     * @param enabled 0 for false, 1 for true, -1 to leave it unchanged
     * @param type User type. See UserProfile.USER_TYPE* for possible values. Use -1 to leave it unchanged
     * @param email New user's email
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the username is null or empty or the username already exists, 
     * if the user type is invalid or if the password is an empty string, or if it is attempted to change 
     * the user name of the admin user name, or if this operation is attempted on a system user. Also, if the new user type is invalid.
     */
    @WebMethod(operationName = "setUserProperties")
    public void setUserProperties(
            @WebParam(name = "oid")long oid,
            @WebParam(name = "username")String username,
            @WebParam(name = "firstName")String firstName,
            @WebParam(name = "lastName")String lastName,
            @WebParam(name = "password")String password,
            @WebParam(name = "enabled")int enabled,
            @WebParam(name = "type")int type,
            @WebParam(name = "email") String email,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Adds a user to a group
     * @param userId The id of the user to be added to the group
     * @param groupId Id of the group which the user will be added to
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the user is already related to that group or if the user or group can not be found.
     */
    @WebMethod(operationName = "addUserToGroup")
    public void addUserToGroup(@WebParam(name = "userId") long userId, 
            @WebParam(name = "groupId") long groupId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Removes a user from a group
     * @param userId The id of the user to be added to the group
     * @param groupId Id of the group which the user will be added to
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the user is not related to that group or if the user or the group could not be found
     */
    @WebMethod(operationName = "removeUserFromGroup")
    public void removeUserFromGroup(@WebParam(name = "userId") long userId, 
            @WebParam(name = "groupId") long groupId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Sets a privilege to a user. If the privilege already exists, the access level is updated, otherwise, the new privilege is added to the user.
     * @param userId The user Id
     * @param featureToken The feature token. See class Privilege for details. Note that this token must match to the one expected by the client application. That's the only way the correct features will be enabled.
     * @param accessLevel The feature token. See class Privilege.ACCESS_LEVEL* for details. 
     * @param sessionId Session token
     * @throws ServerSideException If the access level is invalid, if the featureToken has a wrong format or if the user already has that privilege or if the user could not be found.
     */
    @WebMethod(operationName = "setPrivilegeToUser")
    public void setPrivilegeToUser(@WebParam(name = "userId") long userId, 
            @WebParam(name = "featureToken") String featureToken, 
            @WebParam(name = "accessLevel") int accessLevel, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Sets a privilege to a group. If the privilege already exists, the access level is updated, otherwise, the new privilege is added to the group.
     * @param groupId The user Id
     * @param featureToken The feature token. See class Privilege for details. Note that this token must match to the one expected by the client application. That's the only way the correct features will be enabled.
     * @param accessLevel The feature token. See class Privilege.ACCESS_LEVEL* for details. 
     * @param sessionId Session token
     * @throws ServerSideException If the access level is invalid, if the featureToken has a wrong format or if the group already has that privilege or if the group could not be found
     */
    @WebMethod(operationName = "setPrivilegeToGroup")
    public void setPrivilegeToGroup(@WebParam(name = "groupId") long groupId, 
            @WebParam(name = "featureToken") String featureToken, 
            @WebParam(name = "accessLevel") int accessLevel, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Removes a privilege from a user
     * @param userId Id of the user
     * @param featureToken The feature token. See class Privilege for details. 
     * @param sessionId Session token
     * @throws ServerSideException If the feature token is not related to the user or if the user could not be found
     */
    @WebMethod(operationName = "removePrivilegeFromUser")
    public void removePrivilegeFromUser(@WebParam(name = "userId") long userId, 
            @WebParam(name = "featureToken") String featureToken, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Removes a privilege from a user
     * @param groupId Id of the group
     * @param featureToken The feature token. See class Privilege for details. 
     * @param sessionId Session token
     * @throws ServerSideException If the feature token is not related to the group or if the group could not be found
     */
    @WebMethod(operationName = "removePrivilegeFromGroup")
    public void removePrivilegeFromGroup(@WebParam(name = "groupId") long groupId, 
            @WebParam(name = "featureToken") String featureToken, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Creates a group
     * @param groupName Group name
     * @param description Group description
     * @param users List of user ids to be related to this group
     * @param sessionId Session token
     * @return The group id
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the provided users could not be found
     */
    @WebMethod(operationName = "createGroup")
    public long createGroup(
            @WebParam(name = "groupName")String groupName,
            @WebParam(name = "description")String description,
            @WebParam(name = "users")List<Long> users,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Sets the properties for an existing group
     * @param oid Group id
     * @param groupName New group name (null if unchanged)
     * @param description New group description (null if unchanged)
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
                                      If any of the privileges ids is invalid.
                                      If the group could not be found.
     */
    @WebMethod(operationName = "setGroupProperties")
    public void setGroupProperties(@WebParam(name = "oid")long oid,
            @WebParam(name = "groupName")String groupName,
            @WebParam(name = "description")String description,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Deletes a list of users
     * @param oids List of user ids to be deleted
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the users is the default administrator, which can't be deleted
     *                             Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteUsers")
    public void deleteUsers(@WebParam(name = "oids")List<Long> oids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Deletes a list of groups
     * @param oids list of group ids to be deleted
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the group you are trying to delete contains the default administrator
     *                             Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteGroups")
    public void deleteGroups(@WebParam(name = "oids")List<Long> oids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Gets a particular view related to an object
     * @param oid Object id
     * @param objectClass Object class
     * @param viewId The view id
     * @param sessionId Session token
     * @return The View object (which is basically an XML document)
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object or the view can not be found
     *                             If the corresponding class metadata can not be found
     *                             If the provided view type is not supported
     */
    @WebMethod(operationName = "getObjectRelatedView")
    public RemoteViewObject getObjectRelatedView(@WebParam(name = "oid")String oid,
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "viewId")long viewId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Returns list of views associated to an object
     * @param oid Object id
     * @param objectClass Object class
     * @param viewType View type
     * @param limit Max number of results
     * @param sessionId Session token
     * @return List of objects related to the object
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If the corresponding class metadata can not be found
     *                             If the provided view type is not supported
     */
    @WebMethod(operationName = "getObjectRelatedViews")
    public List<RemoteViewObjectLight> getObjectRelatedViews(@WebParam(name = "oid")String oid,
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "viewType")int viewType,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Gets all views that are not related to a particular object
     * @param viewClass View class. Used to filter
     * @param limit Max number if results
     * @param sessionId Session token
     * @return A list of views
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the view class does not exist
     *                             If the user is not allowed to query for general views
     */
    @WebMethod(operationName = "getGeneralViews")
    public RemoteViewObjectLight[] getGeneralViews(@WebParam(name = "viewClass")String viewClass,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Gets the information of a particular view
     * @param viewId View id
     * @param sessionId Session token
     * @return The view
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the requested view
     */
    @WebMethod(operationName = "getGeneralView")
    public RemoteViewObject getGeneralView(@WebParam(name = "viewId")long viewId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a view for a given list type item. If there's already a view of the provided view type, it will be overwritten
     * @param listTypeItemId list type item id
     * @param listTypeItemClassName list type item class name
     * @param viewClassName view class name
     * @param name view name
     * @param description view description
     * @param structure XML document with the view structure
     * @param background background image
     * @param sessionId Session token
     * @return The id of the new view.
     * @throws ServerSideException If the list type item class can not be found
     *                             If the view type is not supported
     */
    @WebMethod(operationName = "createListTypeItemRelatedView")
    public long createListTypeItemRelatedView(
        @WebParam(name = "listTypeItemId") String listTypeItemId, 
        @WebParam(name = "listTypeItemClassName") String listTypeItemClassName, 
        @WebParam(name = "viewClassName") String viewClassName, 
        @WebParam(name = "name") String name, 
        @WebParam(name = "description") String description, 
        @WebParam(name = "structure") byte [] structure, 
        @WebParam(name = "background") byte [] background, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates a view for a given list type item. If there's already a view of the provided view type, it will be overwritten
     * @param listTypeItemId list type item id
     * @param listTypeItemClass list type item class
     * @param viewId viewId
     * @param name view name
     * @param description view description
     * @param structure XML document with the view structure
     * @param background Background image. If null, the previous will be removed, if 0-sized array, it will remain unchanged
     * @param sessionId Session token
     * @throws ServerSideException If the list type item can not be found
     *                             If the list type item class can not be found
     *                             If the view type is not supported
     */
    @WebMethod(operationName = "updateListTypeItemRelatedView")
    public void updateListTypeItemRelatedView(
        @WebParam(name = "listTypeItemId") String listTypeItemId, 
        @WebParam(name = "listTypeItemClass") String listTypeItemClass, 
        @WebParam(name = "viewId") long viewId, 
        @WebParam(name = "name") String name, 
        @WebParam(name = "description") String description, 
        @WebParam(name = "structure") byte[] structure, 
        @WebParam(name = "background") byte[] background, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets a view related to a list type item, given its id. An example of these views is a layout associated to a particular device model
     * @param listTypeItemId list type item id
     * @param listTypeItemClass list type item class
     * @param viewId view id
     * @param sessionId Session token
     * @return The associated view (there should be only one of each type). Null if there's none yet
     * @throws ServerSideException If the list type item or the view can not be found.
     *                             If the corresponding class metadata can not be found.
     *                             If the provided view type is not supported.
     */
    @WebMethod(operationName = "getListTypeItemRelatedView")
    public RemoteViewObject getListTypeItemRelatedView(
        @WebParam(name = "listTypeItemId") String listTypeItemId, 
        @WebParam(name = "listTypeItemClass") String listTypeItemClass, 
        @WebParam(name = "viewId") long viewId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets all the views related to a list type item. An example of these views is a layout associated to a particular device model
     * @param listTypeItemId list type item id
     * @param listTypeItemClass list type class name
     * @param limit max number of results
     * @param sessionId Session token
     * @return The associated views
     * @throws ServerSideException If the corresponding class metadata can not be found
     *                             If the provided view type is not supported
     */
    @WebMethod(operationName = "getListTypeItemRelatedViews")
    public RemoteViewObjectLight[] getListTypeItemRelatedViews(
        @WebParam(name = "listTypeItemId")  String listTypeItemId, 
        @WebParam(name = "listTypeItemClass")  String listTypeItemClass, 
        @WebParam(name = "limit")  int limit, 
        @WebParam(name = "sessionId")  String sessionId) throws ServerSideException;
    
    /**
     * Deletes a view related to a list type item
     * @param listTypeItemId list type item id
     * @param listTypeItemClass list type class name
     * @param viewId related view id
     * @param sessionId Session token
     * @throws ServerSideException If the list type item class can not be found
     *                             If the list type item can no be found using the id
     *                             If the view can not be found
     */
    @WebMethod(operationName = "deleteListTypeItemRelatedView")
    public void deleteListTypeItemRelatedView(
        @WebParam(name = "listTypeItemId") String listTypeItemId, 
        @WebParam(name = "listTypeItemClass") String listTypeItemClass, 
        @WebParam(name = "viewId") long viewId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the list of template elements with a device layout
     * @param sessionId Session id token
     * @return the list of template elements with a device layout
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getDeviceLayouts")
    public List<RemoteObjectLight> getDeviceLayouts(
        @WebParam(name = "sessionId")  String sessionId) throws ServerSideException;

    /**
     * Gets the device layout structure
     * @param oid object id
     * @param className class of object
     * @param sessionId Session id token
     * @return the structure of the device layout
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getDeviceLayoutStructure")
    public byte[] getDeviceLayoutStructure(
        @WebParam(name = "oid") String oid, 
        @WebParam(name = "className") String className, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Creates a view an relates it to an existing object
     * @param objectId Object id
     * @param objectClass Object class
     * @param name View name
     * @param description View description
     * @param viewClassName View class name
     * @param structure Structure (as an XML document)
     * @param background Background
     * @param sessionId Session id
     * @return The id of the newly created view
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If the object class can not be found
     *                             If the view type is not supported
     */
    @WebMethod(operationName = "createObjectRelatedView")
    public long createObjectRelatedView(@WebParam(name = "objectId")String objectId,
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "name")String name,
            @WebParam(name = "description")String description,
            @WebParam(name = "viewClassName")String viewClassName,
            @WebParam(name = "structure")byte[] structure,
            @WebParam(name = "background")byte[] background,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a general view (a view that is not associated to any object)
     * @param viewClass View class
     * @param name View name
     * @param description Description
     * @param structure Structure
     * @param background background
     * @param sessionId Session id
     * @return The id of the newly created view
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             InvalidArgumentException if the view type is invalid
     */
    @WebMethod(operationName = "createGeneralView")
    public long createGeneralView(@WebParam(name = "viewClass")String viewClass,
            @WebParam(name = "name")String name,
            @WebParam(name = "description")String description,
            @WebParam(name = "structure")byte[] structure,
            @WebParam(name = "background")byte[] background,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Updates an object view (a view that is linked to a particular object)
     * @param objectOid Object id
     * @param objectClass Object class
     * @param viewId View id
     * @param viewName View name. Null to leave unchanged
     * @param viewDescription View description. Null to leave unchanged
     * @param structure View structure. Null to leave unchanged
     * @param background Background. Null to leave unchanged
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the view type is not supported
     */
    @WebMethod(operationName = "updateObjectRelatedView")
    public void updateObjectRelatedView(@WebParam(name = "objectOid")String objectOid,
            @WebParam(name = "objectClass")String objectClass, @WebParam(name = "viewId")long viewId,
            @WebParam(name = "viewName")String viewName, @WebParam(name = "viewDescription")String viewDescription,
            @WebParam(name = "structure")byte[] structure,
            @WebParam(name = "background")byte[] background, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Updates a general view (a view that is not linked to any particular object)
     * @param viewId View id
     * @param viewName View name. Null to leave unchanged
     * @param viewDescription View Description. Null to leave unchanged
     * @param structure View structure. Null to leave unchanged
     * @param background Background. Null to leave unchanged
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the view type is invalid or if the background could not be saved.
     *                             If the view couldn't be found
     */
    @WebMethod(operationName = "updateGeneralView")
    public void updateGeneralView(@WebParam(name = "viewId")long viewId,
            @WebParam(name = "viewName")String viewName, @WebParam(name = "viewDescription")String viewDescription,
            @WebParam(name = "structure")byte[] structure, @WebParam(name = "background")byte[] background, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Deletes views
     * @param oids Ids of the views to be deleted
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the view can't be found
     */
    @WebMethod(operationName = "deleteGeneralView")
    public void deleteGeneralView(@WebParam(name = "oids")List<Long> oids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Creates a list type item
     * @param className List type item class name
     * @param name List type item name
     * @param displayName List type item display name
     * @param sessionId Session token
     * @return the id of the new object
     * @throws ServerSideException If the class provided is not a list type
     *                             If the class is abstract or marked as in design.
     */
    @WebMethod(operationName = "createListTypeItem")
    public String createListTypeItem(
            @WebParam(name = "className") String className,
            @WebParam(name = "name") String name,
            @WebParam(name = "displayName") String displayName,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates a list type item. Formerly this functionality was provided by updateObject, but the implementation was split in two methods.
     * @param className The class of the LTI to be updated. it must be a subclass of GenericObjectList
     * @param oid The id of the LTI to be updated.
     * @param attributes The attributes to be changed.
     * @param sessionId The session token.
     * @throws ServerSideException If the LTI does not exist or if the class provided is not a subclass of GenericObjectList.
     */
    @WebMethod(operationName = "updateListTypeItem")
    public void updateListTypeItem(@WebParam(name = "className")String className,
            @WebParam(name = "oid")String oid,
            @WebParam(name = "attributes")List<StringPair> attributes,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Deletes a list type item
     * @param className list type item class name
     * @param oid list type item id
     * @param releaseRelationships should the deletion process release the relationships attached to this object
     * @param sessionId Session token
     * @throws ServerSideException If the class provided is not a list type
     *                             If the user can't delete a list type item
     */
    @WebMethod(operationName = "deleteListTypeItem")
    public void deleteListTypeItem(
            @WebParam(name = "className") String className,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Retrieves list type item given its id
     * @param listTypeClassName The class name of list type item
     * @param listTypeItemId The id of list type item
     * @param sessionId Session token
     * @return A RemoteBusinessObject instance representing the item
     * @throws ServerSideException If className is not an existing class
     *                             If the class provided is not a list type
     */
    @WebMethod(operationName = "getListTypeItem")
    public RemoteObject getListTypeItem(
            @WebParam(name = "listTypeClassName") String listTypeClassName,
            @WebParam(name = "listTypeItemId") String listTypeItemId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves all items for a single list type
     * @param className The list type class
     * @param sessionId Session token
     * @return a list of list type items
     * @throws ServerSideException If className is not an existing class
     *                             If the class provided is not a list type
     */
    @WebMethod(operationName = "getListTypeItems")
    public List<RemoteObjectLight> getListTypeItems(
            @WebParam(name = "className") String className,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Retrieves all possible list types
     * @param sessionId Session token
     * @return A list of list types as ClassInfoLight instances
     * @throws ServerSideException If the GenericObjectList class does not exist
     */
    @WebMethod(operationName = "getInstanceableListTypes")
    public RemoteClassMetadataLight[] getInstanceableListTypes(
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Executes a complex query generated using the Graphical Query Builder.  Please note
     * that the first record is reserved for the column headers, so and empty result set
     * will have at least one record.
     * @param query The TransientQuery object (a code friendly version of the graphical query designed at client side).
     * @param sessionId session id to check permissions
     * @return An array of records (the first raw is used to put the headers)
     * @throws ServerSideException If the class to be search is cannot be found
     *                             If the user is not allowed to invoke the method
     */
    @WebMethod(operationName = "executeQuery")
    public RemoteResultRecord[] executeQuery(@WebParam(name="query")TransientQuery query,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Creates a query using the Graphical Query Builder
     * @param queryName Query name
     * @param ownerOid OwnerOid. Null if public
     * @param queryStructure XML document as a byte array
     * @param description a short descriptions for the query
     * @param sessionId session id to check permissions
     * @return a RemoteObjectLight wrapping the newly created query
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the owner could not be found.
     */
    @WebMethod(operationName = "createQuery")
    public long createQuery(@WebParam(name="queryName")String queryName,
            @WebParam(name="ownerOid")long ownerOid,
            @WebParam(name="queryStructure")byte[] queryStructure,
            @WebParam(name="description")String description,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Save the query made in the graphical Query builder
     * @param queryOid query oid to be updated
     * @param queryName query name (the same if unchanged)
     * @param ownerOid owneroid (if unchanged)
     * @param queryStructure XML document if unchanged. Null otherwise
     * @param description Query description. Null if unchanged
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the query can not be found
     */
    @WebMethod(operationName = "saveQuery")
    public void saveQuery(@WebParam(name="queryOid")long queryOid,
            @WebParam(name = "queryName")String queryName,
            @WebParam(name = "ownerOid")long ownerOid,
            @WebParam(name = "queryStructure")byte[] queryStructure,
            @WebParam(name = "description")String description,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Deletes a query
     * @param queryOid Query id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the query could not be found
     */
    @WebMethod(operationName = "deleteQuery")
    public void deleteQuery(@WebParam(name="queryOid")long queryOid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Retrieves all saved queries
     * @param showPublic should this method return the public queries along with the private to this user?
     * @param sessionId Session token
     * @return A list with the available queries
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getQueries")
    public RemoteQueryLight[] getQueries(@WebParam(name="showPublic")boolean showPublic,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Retrieves a saved query
     * @param queryOid Query id
     * @param sessionId Session token
     * @return The query
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the query could not be found
     */
    @WebMethod(operationName = "getQuery")
    public RemoteQuery getQuery(@WebParam(name="queryOid")long queryOid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Creates an XML document describing the class hierarchy
     * @param showAll should this method return all entity classes or only InventoryObject subclasses
     * @param sessionId session identifier
     * @return A byte array containing the class hierarchy as an XML document. 
     * @throws ServerSideException If one of the core classes could not be found
     */
    @WebMethod(operationName = "getClassHierarchy")
    public byte[] getClassHierarchy(@WebParam(name = "showAll")boolean showAll,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Methods related to manage pools
     */
    /**
     * Creates a pool without a parent. They're used as general purpose place to put inventory objects, or as root for particular models
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX
     * @param sessionId The session token
     * @return The id of the new pool
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the log root node could not be found
     */
    @WebMethod(operationName = "createRootPool")
    public String createRootPool(@WebParam(name = "name")String name, 
                               @WebParam(name = "description")String description, 
                               @WebParam(name = "instancesOfClass")String instancesOfClass, 
                               @WebParam(name = "type")int type, 
                               @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a pool that will have as parent an inventory object. This special containment structure can be used to 
     * provide support for new models
     * @param parentClassname Class name of the parent object
     * @param parentId Id of the parent object
     * @param name Pool name
     * @param description Pool descriptionCreates a pool that will have as parent an inventory object. This special containment structure can be used to 
     * provide support for new models
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX
     * @param sessionId The session token
     * @return The id of the new pool
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If instancesOfClass is not a valid subclass of InventoryObject
     *                             If the parent object can not be found
     */
    @WebMethod(operationName = "createPoolInObject")
    public String createPoolInObject(@WebParam(name = "parentClassname")String parentClassname, 
                                   @WebParam(name = "parentId")String parentId, 
                                   @WebParam(name = "name")String name, 
                                   @WebParam(name = "description")String description, 
                                   @WebParam(name = "instancesOfClass")String instancesOfClass, 
                                   @WebParam(name = "type")int type, 
                               @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a pool that will have as parent another pool. This special containment structure can be used to 
     * provide support for new models
     * @param parentId Id of the parent pool
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. Not used so far, but it will be in the future. It will probably be used to help organize the existing pools
     * @param sessionId The session token
     * @return The id of the new pool
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If instancesOfClass is not a valid subclass of InventoryObject
     *                             If the parent object can not be found
     */
    @WebMethod(operationName = "createPoolInPool")
    public String createPoolInPool(@WebParam(name = "parentId")String parentId, 
                                   @WebParam(name = "name")String name, 
                                   @WebParam(name = "description")String description, 
                                   @WebParam(name = "instancesOfClass")String instancesOfClass, 
                                   @WebParam(name = "type")int type, 
                               @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates an object within a pool
     * @param poolId Id of the pool under which the object will be created
     * @param className Class this object is going to be instance of
     * @param attributeNames Attributes to be set in the new object. Null or empty array for none
     * @param attributeValues Attributes to be set in the new object (values). Null for none. The size of this array must match attributeNames size
     * @param templateId Template to be used. Use -1 to not use any template
     * @param sessionId Session identifier
     * @return The id of the newly created object
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If attributeNames and attributeValues have different sizes
     *                             If the class name could not be found 
     */
    @WebMethod(operationName = "createPoolItem")
    public String createPoolItem(@WebParam(name = "poolId")String poolId,
            @WebParam(name = "className")String className,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "templateId")String templateId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Deletes a set of pools
     * @param ids Pools to be deleted
     * @param sessionId Session identifier
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the pools to be deleted couldn't be found
     *                             If any of the objects in the pool can not be deleted because it's not a business related instance (it's more a security restriction)
     */
    @WebMethod(operationName = "deletePools")
    public void deletePools(@WebParam(name = "ids")String[] ids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the pools that don't have any parent and are normally intended to be managed by the Pool Manager
     * @param className The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @param type The type of pools that should be retrieved. Root pools can be for general purpose, or as roots in models
     * @param includeSubclasses Use <code>true</code> if you want to get only the pools whose <code>className</code> property matches exactly the one provided, and <code>false</code> if you want to also include the subclasses
     * @param sessionId Session token
     * @return A set of pools
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             or in case something goes wrong
     */
    @WebMethod(operationName = "getRootPools")
    public List<RemotePool> getRootPools(@WebParam(name = "className")String className, 
                                         @WebParam(name = "type")int type, 
                                         @WebParam(name = "includeSubclasses")boolean includeSubclasses, 
                                         @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves all the pools that are children of a particular object.
     * @param objectClassName Object class.
     * @param objectId Object id.
     * @param poolClass Type of the pools that are to be retrieved (that is, the class of the objects contained within the pool)
     * @param sessionId Session id.
     * @return A list of children pools.
     * @throws ServerSideException If the user is not allowed to invoke the method or 
     *                             if the parent object can not be found or 
     *                             if the argument <code>poolClass</code> is not a valid class.
     */
    @WebMethod(operationName = "getPoolsInObject")
    public List<RemotePool> getPoolsInObject(@WebParam(name = "objectClassName")String objectClassName, 
                                             @WebParam(name = "objectId")String objectId,
                                             @WebParam(name = "poolClass")String poolClass, 
                                             @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the pools contained inside a pool.
     * @param parentPoolId Parent pool id.
     * @param poolClass Class of the objects contained by the desired pool (not the parent pool).
     * @param sessionId Session token.
     * @return A list of children pools
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the parent object can not be found
     */
    @WebMethod(operationName = "getPoolsInPool")
    public List<RemotePool> getPoolsInPool(@WebParam(name = "parentPoolId")String parentPoolId,
                                             @WebParam(name = "poolClass")String poolClass, 
                                             @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves information about a particular pool
     * @param poolId The id of the pool
     * @param sessionId The session token
     * @return The pool object
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the pool could not be found
     */
    @WebMethod(operationName = "getPool")
    public RemotePool getPool(@WebParam(name = "poolId") String poolId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates pool name and description
     * @param poolId Pool Id
     * @param name Pool name
     * @param description Pool description
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "setPoolProperties")
    public void setPoolProperties(@WebParam(name = "poolId") String poolId, 
            @WebParam(name = "name") String name, 
            @WebParam(name = "description") String description, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the objects contained into a pool
     * @param poolId Parent pool id
     * @param limit limit of results. -1 to return all
     * @param sessionId Session identifier
     * @return The list of items
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the pool id provided is not valid
     */
    @WebMethod(operationName = "getPoolItems")
    public List<RemoteObjectLight> getPoolItems(@WebParam(name = "poolId")String poolId,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    // <editor-fold defaultstate="collapsed" desc="Task Manager">    
    /**
     * Creates and schedule a task. A task is an application entity that allows to run jobs that will be executed depending on certain schedule
     * @param name Task name
     * @param description Task description
     * @param enabled Is the task enabled?
     * @param commitOnExecute Should this task commit the changes (if any) after its execution? <b>Handle with extreme care, you are basically running arbitrary code and affecting the db</b>
     * @param script The script to be executed
     * @param parameters The parameters for the script
     * @param schedule When the task should be executed
     * @param notificationType How the result of the task should be notified to the associated users 
     * @param sessionId The session token
     * @return The id of the newly created task
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task is disabled
     *                             If something goes wrong
     */
    @WebMethod(operationName = "createTask")
    public long createTask(@WebParam(name = "name")String name,
            @WebParam(name = "description")String description,
            @WebParam(name = "enabled")boolean enabled,
            @WebParam(name = "commitOnExecute")boolean commitOnExecute,
            @WebParam(name = "script")String script,
            @WebParam(name = "parameters")List<StringPair> parameters,
            @WebParam(name = "schedule")RemoteTaskScheduleDescriptor schedule,
            @WebParam(name = "notificationType")RemoteTaskNotificationDescriptor notificationType,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Updates any of these properties from a task: name, description, enabled and script
     * @param taskId Task id
     * @param propertyName Property name. Possible values: "name", "description", "enabled" and "script"
     * @param propertyValue The value of the property. For the property "enabled", the allowed values are "true" and "false"
     * @param sessionId The session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task could not be found
     *                             If the property name has an invalid value
     */
    @WebMethod(operationName = "updateTaskProperties")
    public void updateTaskProperties(@WebParam(name = "taskId")long taskId,
            @WebParam(name = "propertyName")String propertyName,
            @WebParam(name = "propertyValue")String propertyValue,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Updates the parameters of a task. If any of the values is null, that parameter will be deleted, if the parameter does not exist, it will be created
     * @param taskId Task id
     * @param parameters The parameters to be modified as pairs paramName/paramValue. A null value means that that parameter should be deleted
     * @param sessionId The session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task could not be found
     */
    @WebMethod(operationName = "updateTaskParameters")
    public void updateTaskParameters(@WebParam(name = "taskId")long taskId,
            @WebParam(name = "parameters")List<StringPair> parameters,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Updates a task schedule
     * @param taskId Task id
     * @param schedule New schedule
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task could not be found
     */
    @WebMethod(operationName = "updateTaskSchedule")
    public void updateTaskSchedule(@WebParam(name = "taskId")long taskId,
            @WebParam(name = "schedule")RemoteTaskScheduleDescriptor schedule,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Updates a task notification type
     * @param taskId Task id
     * @param notificationType New notification type
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task could not be found
     */
    @WebMethod(operationName = "updateTaskNotificationType")
    public void updateTaskNotificationType(@WebParam(name = "taskId")long taskId,
            @WebParam(name = "notificationType")RemoteTaskNotificationDescriptor notificationType,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the information about a particular task
     * @param taskId Id of the task
     * @param sessionId Session token
     * @return A remote task object representing the task
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task could not be found
     */
    @WebMethod(operationName = "getTask")
    public RemoteTask getTask(@WebParam(name = "taskId")long taskId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets all the registered tasks
     * @param sessionId Session token
     * @return A list of task objects
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             in case something goes wrong
     */
    @WebMethod(operationName = "getTasks")
    public List<RemoteTask> getTasks(@WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets all the tasks related to a particular user
     * @param userId User id
     * @param sessionId Session token
     * @return A list of task objects
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the user can not be found
     */
    @WebMethod(operationName = "getTasksForUser")
    public List<RemoteTask> getTasksForUser(@WebParam(name = "userId")long userId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the users subscribed to a particular task.
     * @param taskId Task id.
     * @param sessionId Session token.
     * @return The list of subscribed users.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             Id the task could not be found
     */
    public List<RemoteUserInfoLight> getSubscribersForTask(@WebParam(name = "taskId") long taskId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a task and unsubscribes all users from it
     * @param taskId Task id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task could not be found
     */
    @WebMethod(operationName = "deleteTask")
    public void deleteTask(@WebParam(name = "taskId")long taskId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Subscribes a user to a task, so it will be notified of the result of its execution
     * @param taskId Id of the task
     * @param userId Id of the user
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the user is already subscribed to the task
     */
    @WebMethod(operationName = "subscribeUserToTask")
    public void subscribeUserToTask(@WebParam(name = "userId")long userId,
            @WebParam(name = "taskId")long taskId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Unsubscribes a user from a task, so it will no longer be notified about the result of its execution
     * @param taskId Id of the task
     * @param userId Id of the user
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task or the user could not be found
     */
    @WebMethod(operationName = "unsubscribeUserFromTask")
    public void unsubscribeUserFromTask(@WebParam(name = "userId")long userId,
            @WebParam(name = "taskId")long taskId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Executes a task on demand.
     * @param taskId The task id
     * @param sessionId The session token
     * @return A RemoteTaskResult object wrapping the task execution messages and details.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task doesn't have a script
     */
    @WebMethod(operationName = "executeTask")
    public RemoteTaskResult executeTask(@WebParam(name = "taskId") long taskId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    // </editor-fold>    
    
    //<editor-fold defaultstate="collapsed" desc="Contact Manager">
    /**
     * Creates a contact
     * @param contactClass The class of the new contact. It must be a subclass of GenericContact
     * @param properties A dictionary (key-value list) with the set of string-type attributes to be set. No string-type attributes are not currently supported. Attribute <b>name</b> is mandatory.
     * @param customerClassName The class of the customer this contact will be associated to
     * @param customerId The id of the customer this contact will be associated to
     * @param sessionId The session token
     * @return The id of the newly created contact
     * @throws ServerSideException If the contact class provided is not a valid GenericCustomer, or if the customer does not exist or if any of the properties does not exist or its type is invalid (not a string)
     */
    public String createContact(
        @WebParam(name = "contactClass")String contactClass, 
        @WebParam(name = "properties")List<StringPair> properties, 
        @WebParam(name = "customerClassName")String customerClassName, 
        @WebParam(name = "customerId")String customerId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Updates a set of properties of a contact
     * @param contactClass The class of the contact to be updated
     * @param contactId The id of the contact to be updated
     * @param properties A set of pairs key-value with the properties to be updated
     * @param sessionId The session token
     * @throws ServerSideException If the contact could not be found or if any of the attributes to be set could not be found or has an invalid value
     */
    public void updateContact(
        @WebParam(name = "contactClass")String contactClass, 
        @WebParam(name = "contactId")String contactId, 
        @WebParam(name = "properties")List<StringPair> properties, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Deletes a contact
     * @param contactClass The class of the contact to be deleted
     * @param contactId The id of the contact to be deleted
     * @param sessionId The session token
     * @throws ServerSideException If the contact was not found
     */
    public void deleteContact(
        @WebParam(name = "contactClass")String contactClass, 
        @WebParam(name = "contactId")String contactId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the whole information about a contact
     * @param contactClass The class of the contact
     * @param contactId The id of the contact
     * @param sessionId The session token
     * @return The contact object
     * @throws ServerSideException If the contact could not be found
     */
    public RemoteContact getContact(
        @WebParam(name = "contactClass")String contactClass, 
        @WebParam(name = "contactId")String contactId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Searches for contacts given a search string, This string will be searched in the attribute values of all contacts
     * @param searchString The string to be searched. Use null or an empty string to retrieve all the contacts
     * @param maxResults Maximum number of results. Use -1 to retrieve all results at once
     * @param sessionId The session token
     * @return The list of contacts for whom at least one of their attributes matches  
     * @throws ServerSideException If the contact could not be found
     */
    public List<RemoteContact> searchForContacts(
            @WebParam(name = "searchString") String searchString, 
            @WebParam(name = "maxResults") int maxResults,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the contacts associated to a given customer
     * @param customerClass The class of the customer to get the contacts from
     * @param customerId The id of the customer to get the contacts from
     * @param sessionId The session token
     * @return The list of contacts associated to the customer
     * @throws ServerSideException If the customer could not be found
     */
    public List<RemoteContact> getContactsForCustomer(
        @WebParam(name = "customerClass")String customerClass, 
        @WebParam(name = "customerId")String customerId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
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
     * @param sessionId The session token
     * @return The list of contacts that matches the search criteria
     * @throws ServerSideException If an error occurs while building the contact objects
     */
    @WebMethod(operationName = "getContacts")
    public List<RemoteContact> getContacts(
            @WebParam(name = "page") int page,
            @WebParam(name = "limit") int limit,
            @WebParam(name = "filters") List<StringPair> filters,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Relates an inventory object to a contact.
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param contactClass The class of the contact.
     * @param contactId The id of the contact.
     * @param sessionId Session token.
     * @throws ServerSideException If the inventory object could not be found or
     *                             if the contact could not be found or
     *                             if the two entities are already related.
     */
    /*
     * This method will be enabled in a later version
    @WebMethod(operationName = "relateObjectToContact")
    public void relateObjectToContact(
            @WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "objectId") String objectId,
            @WebParam(name = "contactClass") String contactClass,
            @WebParam(name = "contactId") String contactId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;*/
    
    /**
     * Releases an inventory object from a contact.
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param contactClass The class of the contact.
     * @param contactId The id of the contact.
     * @param sessionId Session token.
     * @throws ServerSideException If the inventory object could not be found or
     *                             if the contact could not be found or
     *                             if the two entities are already related.
     */
    /*
     * This method will be enabled in a later version
    @WebMethod(operationName = "releaseObjectFromContact")
    public void releaseObjectFromContact(
            @WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "objectId") String objectId,
            @WebParam(name = "contactClass") String contactClass,
            @WebParam(name = "contactId") String contactId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;*/
    
    /**
     * Retrieves the list of resources (inventory objects) related to a contact. 
     * @param contactClass The class of the contact.
     * @param contactId The id of the contact.
     * @param sessionId Session token.
     * @return List of related resources.
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If the contact is not subclass of GenericContact.
     */
    /*
     * This method will be enabled in a later version
    @WebMethod(operationName = "getContactResources")
    public List<RemoteObjectLight> getContactResources(
            @WebParam(name = "contactClass") String contactClass,
            @WebParam(name = "contactId") String contactId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;*/
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Configuration Variables">
    /**
     * Creates a configuration variable inside a pool. A configuration variable is a place where a value will be stored so it can retrieved by whomever need it. 
     * These variables are typically used to store values that help other modules to work, such as URLs, user names, dimensions, etc
     * @param configVariablesPoolId The id of the pool where the config variable will be put
     * @param name The name of the pool. This value can not be null or empty. Duplicate variable names are not allowed
     * @param description The description of the what the variable does
     * @param type The type of the variable. Use 1 for number, 2 for strings, 3 for booleans, 4 for unidimensional arrays and 5 for matrixes. 
     * @param masked If the value should be masked when rendered (for security reasons, for example)
     * @param valueDefinition In most cases (primitive types like numbers, strings or booleans) will be the actual value of the variable as a string (for example "5" or "admin" or "true"). For arrays and matrixes use the following notation: <br> 
     * Arrays: (value1,value2,value3,valueN), matrix: [(row1col1, row1col2,... row1colN), (row2col1, row2col2,... row2colN), (rowNcol1, rowNcol2,... rowNcolN)]. The values will be interpreted as strings 
     * @param sessionId The session token
     * @return The id of the newly created variable
     * @throws ServerSideException If the parent pool could not be found or if the name is empty, the type is invalid, the value definition is empty
     */
    @WebMethod(operationName = "createConfigurationVariable")
    public long createConfigurationVariable(
            @WebParam(name = "configVariablesPoolId") String configVariablesPoolId, 
            @WebParam(name = "name") String name, 
            @WebParam(name = "description") String description,
            @WebParam(name = "type") int type,
            @WebParam(name = "masked") boolean masked,
            @WebParam(name = "valueDefinition") String valueDefinition, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates the value of a configuration variable. See #{@link #createConfigurationVariable(long, java.lang.String, java.lang.String, int, boolean, java.lang.String, java.lang.String) } for value definition syntax
     * @param name The current name of the variable that will be modified
     * @param propertyToUpdate The name of the property to be updated. Possible values are: "name", "description", "type", "masked" and "value"
     * @param newValue The new value as string
     * @param sessionId The session token
     * @throws ServerSideException If the property to be updated can not be recognized or if the configuration variable can not be found
     */
    @WebMethod(operationName = "updateConfigurationVariable")
    public void updateConfigurationVariable(
            @WebParam(name = "name") String name,
            @WebParam(name = "propertyToUpdate") String propertyToUpdate, 
            @WebParam(name = "newValue")String newValue,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Deletes a configuration variable
     * @param name The name of the variable to be deleted
     * @param sessionId The session token
     * @throws ServerSideException If the configuration variable could not be found
     */
    @WebMethod(operationName = "deleteConfigurationVariable")
    public void deleteConfigurationVariable(
            @WebParam(name = "name") String name,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves a configuration variable
     * @param name The name of the variable to be retrieved
     * @param sessionId The session token
     * @return The configuration variable
     * @throws ServerSideException If the variable could not be found
     */
    @WebMethod(operationName = "getConfigurationVariable")
    public RemoteConfigurationVariable getConfigurationVariable(
            @WebParam(name = "name") String name, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves only the value of a configuration variable. Masked values are returned as null.
     * @param name The name of the variable. Masked values are returned as null.
     * @param sessionId The session token
     * @return  The value of the variable as a java object/data type. The numbers are returned as floats.
     * The arrays and matrixes are returned as <code>ArrayList{@literal <String>}</code> and <code>ArrayList<ArrayList{@literal <String>}</code> instances respectively
     * @throws ServerSideException If the variable could not be found 
     */
    @WebMethod(operationName = "getConfigurationVariableValue")
    public RemoteObject getConfigurationVariableValue(
            @WebParam(name = "name") String name,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the configuration variables in a configuration variable pool
     * @param poolId The id pool to retrieve the variables from
     * @param sessionId The session token
     * @return The list of configuration variables in the given pool
     * @throws ServerSideException If the pool could not be found
     */
    @WebMethod(operationName = "getConfigurationVariablesInPool")
    public List<RemoteConfigurationVariable> getConfigurationVariablesInPool(
            @WebParam(name = "poolId") String poolId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the configuration variables with a given prefix
     * @param prefix The prefix of the variables name
     * @param sessionId The session token
     * @return The list of configuration variables with the given prefix
     * @throws ServerSideException If the prefix could not be found
     */
    @WebMethod(operationName = "getConfigurationVariablesWithPrefix")
    public List<RemoteConfigurationVariable> getConfigurationVariablesWithPrefix(
            @WebParam(name = "prefix") String prefix, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets all the configuration variables in the database, no matter what pool they belong to
     * @param sessionId The session token
     * @return The list of existing configuration variables
     * @throws ServerSideException In case of an unexpected server side error
     */
    @WebMethod(operationName = "getAllConfigurationVariables")
    public List<RemoteConfigurationVariable> getAllConfigurationVariables(
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the list of pools of configuration variables.
     * @param sessionId The session token.
     * @return The available pools of configuration variables.
     * @throws ServerSideException If an unexpected error occurred.
     */
    @WebMethod(operationName = "getConfigurationVariablesPools")
    public List<RemotePool> getConfigurationVariablesPools(@WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a pool of configuration variables.
     * @param name The name of the pool. Empty or null values are not allowed.
     * @param description The description of the pool.
     * @param sessionId The session token.
     * @return The id of the newly created pool.
     * @throws ServerSideException If the name provided is null or empty.
     */
    @WebMethod(operationName = "createConfigurationVariablesPool")
    public String createConfigurationVariablesPool(
            @WebParam(name = "name") String name,
            @WebParam(name = "description") String description, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates an attribute of a given configuration variables pool.
     * @param poolId The id of the pool to update.
     * @param propertyToUpdate The property to update. The valid values are "name" and "description".
     * @param value The value of the property to be updated.
     * @param sessionId The session token.
     * @throws ServerSideException If the pool could not be found or If the property provided is not valid.
     */
    @WebMethod(operationName = "updateConfigurationVariablesPool")
    public void updateConfigurationVariablesPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "propertyToUpdate") String propertyToUpdate, 
            @WebParam(name = "value") String value,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Deletes a configuration variables pool. Deleting a pool also deletes the configuration variables contained within.
     * @param poolId The id of the pool to be deleted.
     * @param sessionId The session token.
     * @throws ServerSideException If the pool could not be found.
     */
    @WebMethod(operationName = "deleteConfigurationVariablesPool")
    public void deleteConfigurationVariablesPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Proxy Management">
    /**
     * Creates an inventory proxy. Inventory proxies are used to integrate third party-applications with Kuwaiba. Sometimes these applications must refer to 
     * assets managed by Kuwaiba from another perspective (financial, for example). In these applications, multiple Kuwaiba inventory assets might be represented by
     * a single entity (e.g. a router with slots, boards and ports might just be something like "standard network device"). Proxies are used to map multiple inventory 
     * elements into a single entity. It's a sort of "impedance matching" between systems that refer to the same real world object from different perspectives.
     * @param proxyPoolId The parent pool id.
     * @param proxyClass The proxy class. Must be subclass of GenericProxy.
     * @param attributes The set of initial attributes. If no attribute <code>name</code> is specified, an empty string will be used.
     * @param sessionId Session token.
     * @return The id of the newly created proxy.
     * @throws ServerSideException If the parent pool could not be found or if any of the initial attributes could not be mapped or 
     * if the proxy class could not be found.
     */
    @WebMethod(operationName = "createProxy")
    public String createProxy(@WebParam(name = "proxyPoolId")String proxyPoolId, @WebParam(name = "proxyClass")String proxyClass, 
            @WebParam(name = "attributes")List<StringPair> attributes, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Deletes a proxy and delete its association with the related inventory objects. These objects will remain untouched.
     * @param proxyClass The class of the proxy.
     * @param proxyId The id of the proxy
     * @param sessionId Session token.
     * @throws ServerSideException If the proxy could not be found or if the proxy class could not be found.
     */
    @WebMethod(operationName = "deleteProxy")
    public void deleteProxy(@WebParam(name = "proxyClass")String proxyClass, @WebParam(name = "proxyId")String proxyId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Updates one or many proxy attributes.
     * @param proxyId The parent pool id,
     * @param proxyClass The class of the proxy.
     * @param attributes The set of initial attributes. If no attribute <code>name</code> is specified, an empty string will be used.
     * @param sessionId Session token.
     * @throws ServerSideException If the parent pool could not be found or if any of the initial attributes could not be mapped or 
     * if the proxy class could not be found.
     */
    @WebMethod(operationName = "updateProxy")
    public void updateProxy(@WebParam(name = "proxyClass")String proxyClass, @WebParam(name = "proxyId")String proxyId, 
            @WebParam(name = "attributes")List<StringPair> attributes, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a proxy pool.
     * @param name The name of the pool.
     * @param description The description of the pool.
     * @param sessionId Session token.
     * @throws ServerSideException In case something unexpected happened.
     * @return The id of the newly created proxy.
     */
    @WebMethod(operationName = "createProxyPool")
    public String createProxyPool(@WebParam(name = "name")String name, 
            @WebParam(name = "description")String description, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Updates an attribute of a proxy pool.
     * @param proxyPoolId The id of the pool to be updated.
     * @param attributeName The name of the pool attribute to be updated. Valid values are "name" and "description"
     * @param attributeValue The value of the attribute. Null values will be ignored.
     * @param sessionId Session token.
     * @throws ServerSideException If the pool could not be found or if an unknown attribute name is provided.
     */
    @WebMethod(operationName = "updateProxyPool")
    public void updateProxyPool(@WebParam(name = "proxyPoolId")String proxyPoolId, @WebParam(name = "attributeName")String attributeName, 
            @WebParam(name = "attributeValue")String attributeValue, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Deletes a proxy pool.
     * @param proxyPoolId The id of the pool.
     * @param sessionId Session token.
     * @throws ServerSideException If the pool could not be found.
     */
    @WebMethod(operationName = "deleteProxyPool")
    public void deleteProxyPool(@WebParam(name = "proxyPoolId")String proxyPoolId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the list of pools of proxies.
     * @return The available pools of inventory proxies.
     * @param sessionId Session token.
     * @throws ServerSideException If case something unexpected happened.
     */
    @WebMethod(operationName = "getProxyPools")
    public List<RemotePool> getProxyPools(@WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    /**
     * Gets the list of inventory proxies in a given pool.
     * @param proxyPoolId The id of the parent pool.
     * @param sessionId Session token.
     * @return The proxies
     * @throws ServerSideException If the parent pool could not be found or if the object in the database can not be mapped into an InventoryProxy instance.
     */
    @WebMethod(operationName = "getProxiesInPool")
    public List<RemoteInventoryProxy> getProxiesInPool(@WebParam(name = "proxyPoolId")String proxyPoolId, 
            @WebParam(name = "sessionId")String sessionId)throws ServerSideException;
    
    /**
     * Gets all the inventory proxies in the database.
     * @param sessionId Session token.
     * @return The list of inventory proxy objects.
     * @throws ServerSideException If any proxy node could not be mapped into a Java object.
     */
    @WebMethod(operationName = "getAllProxies")
    public List<RemoteInventoryProxy> getAllProxies(@WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Associates an inventory object to an inventory proxy.
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param proxyClass The class of the proxy.
     * @param proxyId The id of the proxy.
     * @param sessionId Session token.
     * @throws ServerSideException If the inventory object could not be found or
     *                             if the proxy could not be found or
     *                             if the two entities are already related.
     */
    @WebMethod(operationName = "associateObjectToProxy")
    public void associateObjectToProxy(@WebParam(name = "objectClass")String objectClass, @WebParam(name = "objectId")String objectId, 
            @WebParam(name = "proxyClass")String proxyClass, @WebParam(name = "proxyId")String proxyId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Releases an inventory previously related to an inventory proxy.
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param proxyClass The class of the proxy.
     * @param proxyId The id of the proxy.
     * @param sessionId Session token.
     * @throws ServerSideException If the inventory object could not be found or 
     *                             if the proxy could not be found.
     */
    @WebMethod(operationName = "releaseObjectFromProxy")
    public void releaseObjectFromProxy(@WebParam(name = "objectClass")String objectClass, @WebParam(name = "objectId")String objectId, 
            @WebParam(name = "proxyClass")String proxyClass, @WebParam(name = "proxyId")String proxyId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Moves a proxy from a pool to another pool.
     * @param poolId The id of the parent pool.
     * @param proxyClass The class of the proxy.
     * @param proxyId The id of the proxy.
     * @param sessionId Session token.
     * @throws ServerSideException If the pool node can not be found.
     *                             If the proxy can not be move to the selected pool.
     *                             If the proxy can not be found.
     *                             If the proxy class name can no be found.
     */
    /*
     * This method will be enabled in a later version
    @WebMethod(operationName = "moveProxyToPool")
    public void moveProxyToPool(
            @WebParam(name = "poolId")String poolId, 
            @WebParam(name = "proxyClass")String proxyClass, 
            @WebParam(name = "proxyId")String proxyId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;*/
    
    /**
     * Creates a copy of a proxy.
     * @param poolId The id of the parent pool.
     * @param proxyClass The class of the proxy.
     * @param proxyId The id of the proxy.
     * @param sessionId Session token.
     * @return The newly created proxy id.
     * @throws ServerSideException If the pool node can not be found.
     *                             If the proxy can not be copy to the selected pool.
     *                             If the proxy can not be found.
     *                             If the proxy class name can no be found.
     */
    /*
     * This method will be enabled in a later version
    @WebMethod(operationName = "copyProxyToPool")
    public String copyProxyToPool(
            @WebParam(name = "poolId")String poolId, 
            @WebParam(name = "proxyClass")String proxyClass, 
            @WebParam(name = "proxyId")String proxyId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;*/
    //</editor-fold>
    
    //<editor-fold desc="Validators" defaultstate="collapsed">
    /**
     * Creates a validator definition. 
     * @param name The name of the validator. It's recommended to use camel case notation (for example thisIsAName). This field is mandatory
     * @param description The optional description of the validator
     * @param classToBeApplied The class or super class of the classes whose instances will be checked against this validator
     * @param script The groovy script containing the logic of the validator , that is, the 
     * @param enabled If this validador should be applied or not
     * @param sessionId The session token
     * @return The id of the newly created validator definition
     * @throws ServerSideException If the name is null or empty or if the classToBeApplied argument could not be found
     */
    @WebMethod(operationName = "createValidatorDefinition")
    public long createValidatorDefinition(
            @WebParam(name = "name") String name,
            @WebParam(name = "description") String description, 
            @WebParam(name = "classToBeApplied") String classToBeApplied,
            @WebParam(name = "script") String script, 
            @WebParam(name = "enabled") boolean enabled,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Updates the properties of a validator. The null values will be ignored
     * @param validatorDefinitionId The id of teh validator definition to be updated
     * @param name The new name, if any, null otherwise
     * @param description The new description, if any, null otherwise
     * @param classToBeApplied The new class to be associated to this validator, if any, null otherwise
     * @param script The new script, if any, null otherwise
     * @param enabled If the validator should be enabled or not, if any, null otherwise
     * @param sessionId The session token
     * @throws ServerSideException If the validator definition could not be found or if the classToBeApplied parameter is not valid or if the name is not null, but it is empty
     */
    @WebMethod(operationName = "updateValidatorDefinition")
    public void updateValidatorDefinition(
            @WebParam(name = "validatorDefinitionId") long validatorDefinitionId,
            @WebParam(name = "name") String name, 
            @WebParam(name = "description") String description,
            @WebParam(name = "classToBeApplied") String classToBeApplied, 
            @WebParam(name = "script") String script,
            @WebParam(name = "enabled") Boolean enabled, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves all the validator definitions inside a class
     * @param className The class to retrieve the validator definitions from.
     * @param sessionId The session token
     * @return The list of validator definitions
     * @throws ServerSideException In case of an unexpected server side error
     */
    @WebMethod(operationName = "getValidatorDefinitionsForClass")
    public List<RemoteValidatorDefinition> getValidatorDefinitionsForClass(
            @WebParam(name = "className") String className, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves all the validator definitions in the system
     * @param sessionId The session token
     * @return The list of all validator definitions
     * @throws ServerSideException In case of an unexpected server side error
     */
    @WebMethod(operationName = "getAllValidatorDefinitions")
    public List<RemoteValidatorDefinition> getAllValidatorDefinitions( 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Runs the existing validations for the class associated to the given object. Validators set to enabled = false will be ignored
     * @param objectClass The class of the object
     * @param objectId The id of the object
     * @param sessionId The session token
     * @return The list of validators associated to the object and its class
     * @throws ServerSideException If the object can not be found
     */
    @WebMethod(operationName = "runValidationsForObject")
    public List<RemoteValidator> runValidationsForObject(
            @WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "objectId") long objectId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a validator definition
     * @param validatorDefinitionId the id of the validator to be deleted
     * @param sessionId The session token
     * @throws ServerSideException If the validator definition could not be found
     */
    @WebMethod(operationName = "deleteValidatorDefinition")
    public void deleteValidatorDefinition(
            @WebParam(name = "validatorDefinitionId") long validatorDefinitionId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Business Methods. Click on the + sign on the left to edit the code.">
    /**
     * Gets the children of a given object given his class id and object id
     * @param objectClassId object's class id
     * @param oid object's id
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array of all the direct children of the provided object according with the current container hierarchy
     * @throws ServerSideException If the class could not be found
     */
    @WebMethod(operationName = "getObjectChildrenForClassWithId")
    public List<RemoteObjectLight> getObjectChildrenForClassWithId(@WebParam(name = "oid") String oid,
            @WebParam(name = "objectClassId") long objectClassId,
            @WebParam(name = "maxResults") int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

     /**
     * Gets the children of a given object given his class name and object id
     * @param oid Object's oid
     * @param objectClassName object's class name
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array of all the direct children of the provided object according with the current container hierarchy
     * @throws ServerSideException If the class could not be found.
     */
    @WebMethod(operationName = "getObjectChildren")
    public List<RemoteObjectLight> getObjectChildren(@WebParam(name = "objectClassName") String objectClassName,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "maxResults") int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Returns the siblings of an object in the containment hierarchy
     * @param objectClassName Object class
     * @param oid Object oid
     * @param maxResults Max number of results to be returned
     * @param sessionId Session token
     * @return List of siblings
     * @throws ServerSideException If the class does not exist
     *                             If the object does not exist
     */
    @WebMethod(operationName = "getSiblings")
    public List<RemoteObjectLight> getSiblings(@WebParam(name = "objectClassName") String objectClassName,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "maxResults") int  maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Gets all children of an object of a given class
     * @param parentOid Parent whose children are requested
     * @param parentClass Class name of the element we want the children from
     * @param childrenClass The type of children we want to retrieve
     * @param page the number of elements to skip in the query
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array with the children objects
     * @throws ServerSideException If any of the classes can not be found
     *                             If parent object can not be found
     *                             If the database objects can not be correctly mapped into serializable Java objects.
     */
    @WebMethod(operationName="getChildrenOfClass")
    public List<RemoteObject> getChildrenOfClass(@WebParam(name="parentOid")String parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="childrenClass")String childrenClass,
            @WebParam(name="page")int page,
            @WebParam(name="maxResults")int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets recursively all children of an object of a given class
     * @param parentOid Parent whose children are requested
     * @param parentClass Class name of the element we want the children from
     * @param childrenClass The type of children we want to retrieve
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array with the children objects
     * @throws ServerSideException If any of the classes can not be found
     *                             If parent object can not be found
     *                             If the database objects can not be correctly mapped into serializable Java objects.
     */
    @WebMethod(operationName="getChildrenOfClassLightRecursive")
    public List<RemoteObjectLight> getChildrenOfClassLightRecursive(
        @WebParam(name="parentOid") String parentOid,
        @WebParam(name="parentClass") String parentClass,
        @WebParam(name="childrenClass") String childrenClass,
        @WebParam(name="maxResults") int maxResults,
        @WebParam(name="sessionId") String sessionId) throws ServerSideException;

     /**
     * Gets all children of an object of a given class
     * @param parentOid Object oid whose children will be returned
     * @param parentClass
     * @param childrenClass
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array with children
     * @throws ServerSideException If any of the classes can not be found
     *                             If parent object can not be found
     */
    @WebMethod(operationName="getChildrenOfClassLight")
    public List<RemoteObjectLight> getChildrenOfClassLight(@WebParam(name="parentOid")String parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="childrenClass")String childrenClass,
            @WebParam(name="maxResults")int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Returns the special children of a given object as RemoteObjectLight instances. This method is not recursive.
     * @param parentOid The id of the parent object.
     * @param parentClass The class name of the parent object.
     * @param classToFilter The superclass/class to be used to filter the results. You can also use abstract superclasses.
     * @param maxResults The max number of results to fetch. Use -1 to retrieve all.
     * @param sessionId The session token.
     * @return The list of special children of the given object, filtered using classToFilter.
     * @throws ServerSideException If the parent class name provided could not be found or if the parent object could not be found.
     */
    @WebMethod(operationName="getSpecialChildrenOfClassLight")
    public List<RemoteObjectLight> getSpecialChildrenOfClassLight(@WebParam(name="parentOid")String parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="classToFilter")String classToFilter,
            @WebParam(name="maxResults")int maxResults,
            @WebParam(name ="sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Returns the special children of a given object as RemoteObjectLight instances in a recursive fashion.
     * @param parentOid The id of the parent object.
     * @param parentClass The class name of the parent object.
     * @param classToFilter The superclass/class to be used to filter the results. You can also use abstract superclasses.
     * @param maxResults The max number of results to fetch. Use -1 to retrieve all.
     * @param sessionId The session token.
     * @return The recursive list of special children of the given object, filtered using classToFilter.
     * @throws ServerSideException If the parent class name provided could not be found or if the parent object could not be found.
     */
    public List<RemoteObjectLight> getSpecialChildrenOfClassLightRecursive(@WebParam(name="parentOid")String parentOid, 
            @WebParam(name="parentClass")String parentClass, 
            @WebParam(name="classToFilter")String classToFilter, 
            @WebParam(name="maxResults")int maxResults,
            @WebParam(name ="sessionId")String sessionId) throws ServerSideException;

    /**
      * Gets the complete information about a given object (all its attributes)
      * @param objectClass Object class
      * @param oid Object id
      * @param sessionId Session token
      * @return a representation of the entity as a RemoteObject. The list of attribute values is a hashmap of strings (should be mapped to 
      * actual types by the consumer of the service). Single list types are represented by the id of the list type item (a numeric value), 
      * while multiple list types are strings wit the ids of the related list type items separated by semicolons (e.g. 123;786576;92332)
      * @throws ServerSideException If the className class could not be found
      *                             If the requested object could not be found
      *                             If the object id could not be found
      */
    @WebMethod(operationName = "getObject")
    public RemoteObject getObject(@WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Gets the basic information about a given object (oid, classname, name)
     * @param objectClass Object class name
     * @param oid Object oid
     * @param sessionId Session token
     * @return a representation of the entity as a RemoteObjectLight
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectLight")
    public RemoteObjectLight getObjectLight(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves a list of light instances of a given class given a simple filter. This method will search for all objects with a string-based attribute (filterName) whose value matches a value provided (filterValue)
     * @param className The class of the objects to be searched. This method support abstract superclasses as well
     * @param filterName The attribute to be used as filter. This will work <b>only</b> with String-type attributes. Example: serialNumber. To list type attributes the filter must be applied to the name. Example: filterName: model, filterValue: XYZ
     * @param filterValue The value to be use to match the instances. Example "Serial-12345"
     * @param sessionId The session token
     * @return The list of instances that matches the filterName/filterValue criteria
     * @throws ServerSideException if the class provided could not be found
     */    
    @WebMethod(operationName = "getObjectsWithFilterLight")
    public List<RemoteObjectLight> getObjectsWithFilterLight(@WebParam(name = "className") String className,
            @WebParam(name = "filterName") String filterName,
            @WebParam(name = "filterValue") String filterValue,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    
    /**
     * Same as {@link #getObjectsWithFilterLight(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}, but returns RemoteObjects instead of RemoteObjectLights
     * @param className The class of the objects to be searched. This method support abstract superclasses as well
     * @param filterName The attribute to be used as filter. This will work <b>only</b> with String-type attributes. Example: serialNumber. To list type attributes the filter must be applied to the name. Example: filterName: model, filterValue: XYZ
     * @param filterValue The value to be use to match the instances. Example "Serial-12345"
     * @param sessionId The session token
     * @return The list of instances that matches the filterName/filterValue criteria
     * @throws ServerSideException if the class provided could not be found
     */   
    @WebMethod(operationName = "getObjectsWithFilter")
    public List<RemoteObject> getObjectsWithFilter(@WebParam(name = "className") String className,
            @WebParam(name = "filterName") String filterName,
            @WebParam(name = "filterValue") String filterValue,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets all objects of a given class
     * @param className Class name
     * @param attributesToFilter attributes to filter for
     * @param page number of results to skip. Use -1 to retrieve all
     * @param maxResults Max number of results. Use -1 to retrieve all
     * @param sessionId Session token
     * @return A list of instances of @className
     * @throws ServerSideException If the class can not be found
     *                             If the class is not subclass of InventoryObject
     */
    @WebMethod(operationName = "getObjectsOfClassLight")
    public List<RemoteObjectLight> getObjectsOfClassLight(@WebParam(name = "className") String className,
            @WebParam(name = "attributesToFilter") List<StringPair> attributesToFilter,
            @WebParam(name = "page")int page,
            @WebParam(name = "maxResults")int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the common parent between an a object and b object
     * @param aObjectClass Object a class name
     * @param aOid Object a id
     * @param bObjectClass Object b class name
     * @param bOid Object a id
     * @param sessionId Session Id token
     * @return The common parent
     * @throws ServerSideException If the requested object can't be found
     *                             If any of the class nodes involved is malformed
     *                             If the database object could not be properly mapped into a serializable java object.
     */
    @WebMethod(operationName = "getCommonParent")
    public RemoteObjectLight getCommonParent(
        @WebParam(name = "aObjectClass") String aObjectClass, 
        @WebParam(name = "aOid") String aOid, 
        @WebParam(name = "bObjectClass") String bObjectClass, 
        @WebParam(name = "bOid") String bOid, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the parent of a given object in the standard and special containment hierarchy
     * @param objectClass Object class of child
     * @param oid Object id for the child
     * @param sessionId Session id
     * @return The parent object
     * @throws ServerSideException If the requested object can't be found
     *                             If any of the class nodes involved is malformed
     *                             If the database object could not be properly 
     *                            mapped into a serializable java object.
     */
    @WebMethod(operationName = "getParent")
    public RemoteObjectLight getParent(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves all the ancestors of an object in the containment hierarchy. If the provided object is in a pool, the ancestor pools will be returned.
     * @param objectClass Object class
     * @param oid Object id.
     * @param sessionId Session token.
     * @return The list of ancestors.
     * @throws ServerSideException In case something goes wrong.
     */
    @WebMethod(operationName = "getParents")
    public List<RemoteObjectLight> getParents(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Returns all the special relationships of a given object as a hashmap whose keys are
     * the names of the relationships and the values the list of related objects
     * @param objectClass Object class
     * @param oid Object id
     * @param sessionId Session token
     * @return An object comprising the list of special relationships of the given object and the other end of such relationship
     * @throws ServerSideException If case something goes wrong
     */
    @WebMethod(operationName = "getSpecialAttributes")
    public RemoteObjectSpecialRelationships getSpecialAttributes(@WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the list of parents (according to the special and standard containment hierarchy) until it finds an instance of class 
     * objectToMatchClassName (for example "give me the parents of this port until you find the nearest rack")
     * @param objectClass Class of the object to get the parents from
     * @param oid Id of the object to get the parents from
     * @param objectToMatchClassName Class of the object that will limit the search. It can be a superclass, if you want to match many classes at once
     * @param sessionId Session token
     * @return The list of parents until an instance of objectToMatchClassName is found. If no instance of that class is found, all parents until the Dummy Root will be returned
     * @throws ServerSideException If the object to evaluate can not be found or if any of the classes provided could not be found.
     */
    @WebMethod(operationName = "getParentsUntilFirstOfClass")
    public List<RemoteObjectLight> getParentsUntilFirstOfClass(
        @WebParam(name = "objectClass") String objectClass, 
        @WebParam(name = "oid") String oid, 
        @WebParam(name = "objectToMatchClassName") String objectToMatchClassName, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Gets the first occurrence of a parent with a given class (according to the special and standard containment hierarchy)
     * (for example "give me the parent of this port until you find the nearest rack")
     * @param objectClass Class of the object to get the parent from
     * @param oid Id of the object to get the parent from
     * @param objectToMatchClassName Class of the object that will limit the search. It can be a superclass, if you want to match many classes at once
     * @param sessionId The session id token
     * @return The the first occurrence of a parent with a given class. If no instance of that class is found, the child of Dummy Root related in this hierarchy will be returned
     * @throws ServerSideException If the object to evaluate can not be found
                                   If any of the classes provided could not be found
                                   If the object provided is not in the standard containment hierarchy
     */
    @WebMethod(operationName = "getFirstParentOfClass")
    public RemoteObjectLight getFirstParentOfClass(
            @WebParam(name = "objectClass") String objectClass, 
            @WebParam(name = "oid") String oid, 
            @WebParam(name = "objectToMatchClassName") String objectToMatchClassName, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the first parent of an object which matches the given class in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object oid
     * @param parentClass Class to be matched
     * @param sessionId sssion Id
     * @return The direct parent of the provided object.
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getParentOfClass")
    public RemoteObject getParentOfClass(@WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "parentClass") String parentClass,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Gets the value of a special attribute, this is, those related to a model, such as cables connected to ports
     * @param objectClass Object's class
     * @param oid object oid
     * @param attributeName attribute's name
     * @param sessionId Session token
     * @return A list of the values related to the given object through attributeName.
     * Note that this is a <strong>string</strong> array on purpose, so the values used not necessarily are not longs
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getSpecialAttribute")
    public List<RemoteObjectLight> getSpecialAttribute(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "attributename") String attributeName,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the special children of a given object. This relationship depends on the model. The relationship between a container and the links in the physical layer model is an example of this kind of relationships.
     * @param objectClass The class of the object to be searched.
     * @param objectId The id of the object to be searched.
     * @param sessionId Session token.
     * @return A list of special children.
     * @throws ServerSideException If the class could not be found
     *                             If the object could not be found
     */
    @WebMethod(operationName = "getObjectSpecialChildren")
    public List<RemoteObjectLight> getObjectSpecialChildren (@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "objectId") String objectId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Updates attributes of a given object
     * @param className object's class name
     * @param oid Object id
     * @param attributes A dictionary with pairs key-value, being <b>key</b>, the attribute name, and <b>value</b>, 
     * the serialized version of the attribute value. Single list types are represented by the id of the list type item (a numeric value), 
     * while multiple list types are strings wit the ids of the related list type items separated by semicolons (e.g. 123;786576;92332)
     * @param sessionId Session token
     * @throws ServerSideException If the object class can't be found
     *                             If the object can't be found
     *                             If the update can't be performed due a business rule or because the object is blocked
     *                             If any of the names provided does not exist or can't be set using this method or of the value of any of the attributes can not be mapped correctly.
     */
    @WebMethod(operationName = "updateObject")
    public void updateObject(@WebParam(name = "className")String className,
            @WebParam(name = "oid")String oid,
            @WebParam(name = "attributes")List<StringPair> attributes,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Creates a business object
     * @param className New object class name
     * @param parentObjectClassName New object parent's class name
     * @param parentOid New object parent's id
     * @param attributeNames Names of the attributes to be set at creation time
     * @param attributeValues Values for those attributes
     * @param templateId Template id. Use -1 to not use any template
     * @param sessionId Session token
     * @return the id of the new object
     * @throws ServerSideException If the object's class can't be found
     *                             If the parent id is not found
     *                             If there's a business constraint that doesn't allow to create the object.
     *                             If any of the attribute values has an invalid value or format.
     *                             If the specified template could not be found
     */
    @WebMethod(operationName = "createObject")
    public String createObject(@WebParam(name = "className")String className,
            @WebParam(name = "parentObjectClassName")String parentObjectClassName,
            @WebParam(name = "parentOid")String parentOid,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "templateId")String templateId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a special business object. It's a generic method to create objects proper to
     * special models. Parent object won't be linked to the new object through a conventional 
     * containment relationship
     * @param className New object class name
     * @param parentObjectClassName New object parent's class name
     * @param parentOid New object parent's id
     * @param attributeNames Names of the attributes to be set at creation time
     * @param attributeValues Values for those attributes
     * @param templateId Template id. Use -1 to not use any template
     * @param sessionId Session token
     * @return the id of the new object
     * @throws ServerSideException If the object's class can't be found
     *                             If the parent id is not found
     *                             If the update can't be performed due to a format issue
     *                             If any of the attribute values has an invalid value or format.
     *                             If the specified template could not be found.
     */
    @WebMethod(operationName = "createSpecialObject")
    public String createSpecialObject(@WebParam(name = "className")String className,
            @WebParam(name = "parentObjectClassName")String parentObjectClassName,
            @WebParam(name = "parentOid")String parentOid,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "templateId")String templateId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Deletes a set of objects. Note that this method must be used only for business objects (not metadata or application ones)
     * @param className Objects class names
     * @param oid object id from the objects to be deleted
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws ServerSideException If the object couldn't be found
     *                             If the class could not be found
     *                             If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     */
    @WebMethod(operationName = "deleteObject")
    public void deleteObject(@WebParam(name = "className")String className,
            @WebParam(name = "oid")String oid,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Deletes a set of objects. Note that this method must be used only for business objects (not metadata or application ones)
     * @param classNames Objects class names
     * @param oids object id from the objects to be deleted
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws ServerSideException If the requested object can't be found
     *                             If the requested object class can't be found
     *                             If the update can't be performed due a business rule or because the object is blocked or it has relationships and releaseRelationships is false
     *                             If it was not possible to release the possible unique attributes
     */
    @WebMethod(operationName = "deleteObjects")
    public void deleteObjects(@WebParam(name = "classNames")String[] classNames,
            @WebParam(name = "oid")String[] oids,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
     /**
     * Moves objects from their current parent to a pool target object.
     * @param  targetClass New parent object id
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @param sessionId Session token
     * @throws ServerSideException If the object's or new parent's class can't be found
     *                             If the object or its new parent can't be found
     *                             If the update can't be performed due to a business rule
     */
    @WebMethod(operationName = "moveObjectsToPool")
    public void moveObjectsToPool(@WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")String targetOid,
            @WebParam(name = "objectsClasses")String[] objectClasses,
            @WebParam(name = "objectsOids")String[] objectOids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Moves objects from their current parent to a target object.
     * @param  targetClass New parent object id
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @param sessionId Session token
     * @throws ServerSideException If the object's or new parent's class can't be found
     *                             If the object or its new parent can't be found
     *                             If the update can't be performed due to a business rule
     */
    @WebMethod(operationName = "moveObjects")
    public void moveObjects(@WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")String targetOid,
            @WebParam(name = "objectsClasses")String[] objectClasses,
            @WebParam(name = "objectsOids")String[] objectOids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
     /**
     * Moves special objects from their current parent to a target object.
     * @param  targetClass New parent object id
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @param sessionId Session token
     * @throws ServerSideException If the object's or new parent's class can't be found
     *                             If the object or its new parent can't be found
     *                             If the update can't be performed due to a business rule
     */
    @WebMethod(operationName = "moveSpecialObjects")
    public void moveSpecialObjects(@WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")String targetOid,
            @WebParam(name = "objectsClasses")String[] objectClasses,
            @WebParam(name = "objectsOids")String[] objectOids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Move a pool item from a pool to another pool
     * @param poolId The id of the pool node
     * @param poolItemClassName The class name for the pool item
     * @param poolItemId The id for the pool item
     * @param sessionId Session token
     * @throws ServerSideException If the pool node can not be found
     *                             If the pool item can not be move to the selected pool
     *                             If the pool item can not be found
     *                             If the pool item class name can no be found
     */
    @WebMethod(operationName = "movePoolItemToPool")
    public void movePoolItemToPool(
        @WebParam(name = "poolId") String poolId, 
        @WebParam(name = "poolItemClassName") String poolItemClassName, 
        @WebParam(name = "poolItemId") String poolItemId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

     /**
     * Copy objects from its current parent to a target. This is <b>not</b> a deep copy. Only the selected object will be copied, not the children
     * @param targetClass  The new parent class name
     * @param targetOid The new parent oid
     * @param objectClasses Class names of the objects to be copied
     * @param objectOids Oids of the objects to be copied
     * @param recursive should the objects be copied recursively? (themselves plus their children)
     * @param sessionId Session token
     * @return An array with the ids of the new objects
     * @throws ServerSideException If any of the provided classes couldn't be found
     *                             If any of the template objects couldn't be found
     *                             If the target parent can't contain any of the new instances
     */
    @WebMethod(operationName = "copyObjects")
    public String[] copyObjects(
            @WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")String targetOid,
            @WebParam(name = "templateClases")String[] objectClasses,
            @WebParam(name = "templateOids")String[] objectOids,
            @WebParam(name = "recursive")boolean recursive,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
     /**
     * Copy special objects from its current parent to a target. 
     * This is <b>not</b> a deep copy. Only the selected object will be copied, not the children
     * @param targetClass  The new parent class name
     * @param targetOid The new parent oid
     * @param objectClasses Class names of the objects to be copied
     * @param objectOids Oids of the objects to be copied
     * @param recursive should the objects be copied recursively? (themselves plus their children)
     * @param sessionId Session token
     * @return An array with the ids of the new objects
     * @throws ServerSideException If any of the provided classes couldn't be found
     *                             If any of the template objects couldn't be found
     *                             If the target parent can't contain any of the new instances
     */
    @WebMethod(operationName = "copySpecialObjects")
    public String[] copySpecialObjects(
            @WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")String targetOid,
            @WebParam(name = "templateClases")String[] objectClasses,
            @WebParam(name = "templateOids")String[] objectOids,
            @WebParam(name = "recursive")boolean recursive,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Copy a pool item from a pool to another pool
     * @param poolId The id of the pool node
     * @param poolItemClassName The class name for the pool item
     * @param poolItemId The id for the pool item
     * @param recursive If this operation should also copy the children objects recursively
     * @param sessionId Session token
     * @throws ServerSideException If the pool node can not be found
     *                             If the pool item can not be move to the selected pool
     *                             If the pool item can not be found
     *                             If the pool item class name can no be found
     */
    @WebMethod(operationName = "copyPoolItemToPool")
    public void copyPoolItemToPool(
        @WebParam(name = "poolId") String poolId, 
        @WebParam(name = "poolItemClassName") String poolItemClassName, 
        @WebParam(name = "poolItemId") String poolItemId, 
        @WebParam(name = "recursive") boolean recursive,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the mandatory attributes for a given class
     * @param className The class name
     * @param sessionId Session token
     * @return The list of mandatory attributes in the given class
     * @throws ServerSideException If the class doesn't exist
     */
    @WebMethod(operationName = "getMandatoryAttributesInClass")
    public List<RemoteAttributeMetadata> getMandatoryAttributesInClass(
            @WebParam(name = "className") String className,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates multiple objects using a given name pattern
     * @param className The class name for the new objects
     * @param parentClassName The parent class name for the new objects
     * @param parentOid The object id of the parent
     * @param namePattern A pattern to create the names for the new objects
     * @param templateId the id template to create the object, it can be null if not a template is going to be used
     * @param sessionId Session id token
     * @return A list of ids for the new objects
     * @throws ServerSideException If the className or the parentClassName can not be found.
     *                             If the className is not a possible children of parentClassName.
     *                             If the className is not in design or are abstract.
     *                             If the className is not an InventoryObject.
     *                             If the parent node can not be found.
     *                             If the given name pattern not match with the regular expression to build the new object name.
     */
    @WebMethod(operationName = "createBulkObjects")
    public String[] createBulkObjects(
        @WebParam(name = "className") String className, 
        @WebParam(name = "parentClassName") String parentClassName, 
        @WebParam(name = "parentOid") String parentOid, 
        @WebParam(name = "namePattern") String namePattern, 
        @WebParam(name = "templateId") String templateId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates multiple special objects using a given naming pattern
     * @param className The class name for the new special objects
     * @param parentClassName The parent class name for the new special objects
     * @param parentId The object id of the parent
     * @param namePattern A pattern to create the names for the new special objects
     * @param templateId The template to be used for the new set of objects.
     * @param sessionId Session id token
     * @return A list of ids for the new special objects
     * @throws ServerSideException If the className or the parentClassName can not be found.
     *                             If the parent node can not be found.
     *                             If the given name pattern not match with the regular expression to build the new object name.
     *                             If the className is not a possible special children of parentClassName.
     *                             If the className is not in design or are abstract.
     *                             If the className is not an InventoryObject.
     *                             If the template could not be found.
     */
    @WebMethod(operationName = "createBulkSpecialObjects")
    public String[] createBulkSpecialObjects(
        @WebParam(name = "className") String className, 
        @WebParam(name = "parentClassName") String parentClassName, 
        @WebParam(name = "parentId") String parentId,
        @WebParam(name = "namePattern") String namePattern, 
        @WebParam(name = "templateId") String templateId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    /**
     * Models
     */

    //Physical and Logical connections
    /**
     * Connect two ports using a mirror relationship
     * @param aObjectClass Port A class
     * @param aObjectId Port A id
     * @param bObjectClass Port B class
     * @param bObjectId Port B id
     * @param sessionId Session token
     * @throws ServerSideException If any of the objects can't be found
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "connectMirrorPort")
    public void connectMirrorPort(
            @WebParam(name = "aObjectClass")String[] aObjectClass,
            @WebParam(name = "aObjectId")String[] aObjectId,
            @WebParam(name = "bObjectClass")String[] bObjectClass,
            @WebParam(name = "bObjectId")String[] bObjectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    /**
     * Connect two ports using a mirrorMultiple relationship
     * @param aObjectClass Port a class
     * @param aObjectId Port a id
     * @param bObjectClasses Port b classes
     * @param bObjectIds Port b ids
     * @param sessionId Session token
     * @throws ServerSideException
     */
    @WebMethod(operationName = "connectMirrorMultiplePort")
    public void connectMirrorMultiplePort(
        @WebParam(name="aObjectClass") String aObjectClass, 
        @WebParam(name="aObjectId") String aObjectId, 
        @WebParam(name="bObjectClasses") List<String> bObjectClasses, 
        @WebParam(name="bObjectIds") List<String> bObjectIds,
        @WebParam(name="sessionId") String sessionId) throws ServerSideException;
    /**
     * Releases a port mirroring relationship between two ports, receiving one of the ports as parameter
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session token
     * @throws ServerSideException If the object can not be found
     *                             If the class can not be found
     */
    @WebMethod(operationName = "releaseMirrorPort")
    public void releaseMirrorPort(
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    /**
     * Releases a port mirroring multiple relationship between two ports, receiving one of the ports as parameter
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session token
     * @throws ServerSideException If the object can not be found
     *                             If the class can not be found
     */
    @WebMethod(operationName = "releaseMirrorMultiplePort")
    public void releaseMirrorMultiplePort(
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a physical connection (a container or a link). The validations are made at server side (this is,
     * if the connection can be established between the two endpoints, if they're not already connected, etc)
     * @param aObjectClass "a" endpoint object class
     * @param aObjectId "a" endpoint object id
     * @param bObjectClass "b" endpoint object class
     * @param bObjectId "b" endpoint object id
     * @param name COnnection name. Leave empty if you want to use the one in the template
     * @param connectionClass Class used to create the connection. See Constants class for supported values
     * @param templateId Id of the template for class connectionClass. Use -1 if you want to create a connection without template
     * @param sessionId Session token
     * @return The new connection id
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object's class can't be found
     *                             If the parent id is not found
     *                             If the update can't be performed due to a format issue
     *                             If any of the attribute values has an invalid value or format.
     *                             If the specified template could not be found.
     *                             If any of the objects can't be found
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "createPhysicalConnection")
    public String createPhysicalConnection(
            @WebParam(name = "aObjectClass")String aObjectClass,
            @WebParam(name = "aObjectId")String aObjectId,
            @WebParam(name = "bObjectClass")String bObjectClass,
            @WebParam(name = "bObjectId")String bObjectId,
            @WebParam(name = "name")String name,
            @WebParam(name = "connectionClass") String connectionClass,
            @WebParam(name = "templateId") String templateId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Creates a physical connection (a container or a link). The validations are made at server side (this is,
     * if the connection can be established between the two endpoints, if they're not already connected, etc)
     * @param aObjectClasses "a" endpoints object class
     * @param aObjectIds "a" endpoints object id
     * @param bObjectClasses "b" endpoints object class
     * @param bObjectIds "b" endpoints object id
     * @param name COnnection name. Leave empty if you want to use the one in the template
     * @param connectionClass Class used to create the connection. See Constants class for supported values
     * @param templateId Id of the template for class connectionClass. Use -1 if you want to create a connection without template
     * @param sessionId Session token
     * @return The new connection id
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object's class can't be found
     *                             If the parent id is not found
     *                             If the update can't be performed due to a format issue
     *                             If any of the attribute values has an invalid value or format.
     *                             If the specified template could not be found.
     *                             If any of the objects can't be found
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "createPhysicalConnections")
    public String[] createPhysicalConnections(
            @WebParam(name = "aObjectClasses")String[] aObjectClasses,
            @WebParam(name = "aObjectIds")String[] aObjectIds,
            @WebParam(name = "bObjectClasses")String[] bObjectClasses,
            @WebParam(name = "bObjectIds")String[] bObjectIds,
            @WebParam(name = "name")String name,
            @WebParam(name = "connectionClass") String connectionClass,
            @WebParam(name = "templateId") String templateId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Returns the endpoints of a physical connection
     * @param connectionClass Connection class
     * @param connectionId Connection id
     * @param sessionId Session token
     * @return An array of two positions: the first is the A endpoint and the second is the B endpoint
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If either the object class or the attribute can not be found
     */
    @WebMethod(operationName = "getPhysicalConnectionEndpoints")
    public RemoteObjectLight[] getPhysicalConnectionEndpoints(@WebParam(name = "connectionClass")String connectionClass, 
            @WebParam(name = "connectionId")String connectionId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Returns the structure of a logical connection. The current implementation is quite simple and the return object 
     * simply provides the endpoints and the next ports connected to such endpoints using a physical connection
     * @param linkClass The class of the connection to be evaluated
     * @param linkId The id of the connection to be evaluated
     * @param sessionId Session token
     * @return An object with the details of the connection and the physical resources associated to it
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the provided connection could not be found
     */
    @Deprecated
    @WebMethod(operationName = "getLogicalLinkDetails")
    public RemoteLogicalConnectionDetails getLogicalLinkDetails(@WebParam(name = "linkClass")String linkClass, 
                                        @WebParam(name = "linkId")String linkId,
                                        @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
   
    /**
     * Validates a saved structure of a end to end view. The current implementation
     * provides the endpoints and the next ports connected to such endpoints using a physical connection
     * also adds continuity if a VLAN or a BridgeDomain is found
     * @param linkClasses The class of the connection to be evaluated
     * @param linkIds The id of the connection to be evaluated
     * @param savedView a given saved view to validate
     * @param sessionId Session token
     * @return An object with the details of the connection and the physical resources associated to it
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the provided connection could not be found
     */
    @WebMethod(operationName = "validateSavedE2EView")
    public RemoteViewObject validateSavedE2EView( 
            @WebParam(name = "linkClasses")List<String> linkClasses,
            @WebParam(name = "linkIds")List<String> linkIds,
            @WebParam(name = "savedView") RemoteViewObject savedView,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Returns the structure of a logical connection. The current implementation is quite simple and the return object 
     * simply provides the endpoints and the next ports connected to such endpoints using a physical connection
     * @param linkClasses The class of the connection to be evaluated
     * @param linkIds The id of the connection to be evaluated
     * @param includeVLANs true to include the bridge domains continuity
     * @param includeBDIs true to include the bridge domains continuity
     * @param sessionId Session token
     * @return An object with the details of the connection and the physical resources associated to it
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the provided connection could not be found
     */
    @WebMethod(operationName = "getE2EView")
    public RemoteViewObject getE2View( 
            @WebParam(name = "linkClasses")List<String> linkClasses,
            @WebParam(name = "linkIds")List<String> linkIds,
            @WebParam(name = "includeVLANs")boolean includeVLANs,
            @WebParam(name = "includeBDIs")boolean includeBDIs,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
   
    /**
     * Retrieves the existing containers between two given nodes. 
     * @param objectAClass The class of the object A.
     * @param objectAId The id of the object A.
     * @param objectBClass The class of the object B. (end point B class)
     * @param objectBId The id of the object B (end point B id).
     * @param containerClass The class of the containers to be return.
     * @param sessionId Session token
     * @return A list with the common wire containers between the two objects
     * @throws ServerSideException if an objects doesn't exist or if a given class doesn't exist

     */
    @WebMethod(operationName = "getContainersBetweenObjects")
    public List<RemoteObjectLight> getContainersBetweenObjects(@WebParam(name = "objectAClass")String objectAClass, 
            @WebParam(name = "objectAId")String objectAId, 
            @WebParam(name = "objectBClass")String objectBClass, 
            @WebParam(name = "objectBId")String objectBId, 
            @WebParam(name = "containerClass")String containerClass, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Loops through all instances of GenericCommunicationsPort at any level inside the given object and gets the physical path. 
     * Only the ports with connections {@literal (physicalPath.size > 1)} are returned
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param sessionId Session token
     * @return A list of physical paths from all the ports with connections inside the given object. See <code>getPhysicalPath</code> for details about the structure of each entry
     * @throws ServerSideException In the same cases as <code>getChildrenOfClassLightRecursive</code> and <code>getPhysicalPath</code>

     */
    @WebMethod(operationName = "getPhysicalConnectionsInObject")
    public List<RemoteObjectLightList> getPhysicalConnectionsInObject(@WebParam(name = "objectClass")String objectClass, 
            @WebParam(name = "objectId")String objectId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Convenience method that returns the link connected to a port (if any). It serves to avoid calling {@link getSpecialAttribute} two times.
     * @param portClassName The class of the port
     * @param portId The id of the port
     * @param sessionId The session token
     * @return The link connected to the port or null if there isn't any
     * @throws ServerSideException If the port could not be found or if the class provided does not exist or if The class provided is not a subclass of GenericPort
     */
    @WebMethod(operationName = "getLinkConnectedToPort")
    public RemoteObject getLinkConnectedToPort(
        @WebParam(name = "portClassName") String portClassName, 
        @WebParam(name = "portId") String portId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the physical trace of connections and ports from a given port.
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session token
     * @return An array containing the sorted elements in the physical path of the given port. The first element is the port from which the trace is generated, 
     * while the last is the destination port until which there is physical continuity. If the source port does not have any connection, the physical path will have only one element (that port).
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             
     */
    @WebMethod(operationName = "getPhysicalPath")
    public List<RemoteObjectLight> getPhysicalPath (@WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the tree representation of all physical paths.
     * @param objectClass Port object class
     * @param objectId Port object id
     * @param sessionId Session token
     * @return A tree representation of all physical paths.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *  If any of the objects involved in the path cannot be found
     *  If any of the object classes involved in the path cannot be found
     *  If any of the objects involved in the path has a malformed list type attribute
     *  If any of the objects involved in the path has an invalid objectId or className
     */
    @WebMethod(operationName = "getPhysicalTree")
    public RemoteObjectRelatedObjects getPhysicalTree(
        @WebParam(name = "objectClass") String objectClass, 
        @WebParam(name = "objectId") String objectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
   
    /**
     * Connects pairs of ports (if they are not connected already) using physical link
     * @param sideAClassNames The list of classes of one of the sides of the connection
     * @param sideAIds The list of ids the objects on one side of the connection
     * @param linksClassNames the classes of the links that will connect the two sides
     * @param linksIds The ids of these links
     * @param sideBClassNames The list of classes of the other side of the connection
     * @param sideBIds The list of ids the objects on the other side of the connection
     * @param sessionId Session token
     * @throws ServerSideException If the object can not be found
     *                             If either the object class or the attribute can not be found
     *                             If any of the objects can't be found
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     *                             If the object activity log could no be found
     */
    @WebMethod(operationName = "connectPhysicalLinks")
    public void connectPhysicalLinks (@WebParam(name = "sideAClassNames")String[] sideAClassNames, @WebParam(name = "sideAIds")String[] sideAIds,
                                      @WebParam(name = "linksClassNames")String[] linksClassNames, @WebParam(name = "linksIds")String[] linksIds,
                                      @WebParam(name = "sideBClassNames")String[] sideBClassNames, @WebParam(name = "sideBIds")String[] sideBIds,
                                      @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Connects pairs of elements (of any class except subclasses of GenericPort) using containers (subclasses of GenericPhysicalContainer) 
     * @param sideAClassNames The list of classes of one of the sides of the connection
     * @param sideAIds The list of ids the objects on one side of the connection
     * @param containersClassNames the classes of the containers that will connect the two sides
     * @param containersIds The ids of these containers
     * @param sideBClassNames The list of classes of the other side of the connection
     * @param sideBIds The list of ids the objects on the other side of the connection
     * @param sessionId Session token
     * @throws ServerSideException If any of the provided objects can not be found, if the endpoints are already connected, or if one of the endpoints is a port
     */
    @WebMethod(operationName = "connectPhysicalContainers")
    public void connectPhysicalContainers (@WebParam(name = "sideAClassNames")String[] sideAClassNames, @WebParam(name = "sideAIds")String[] sideAIds,
                                      @WebParam(name = "containersClassNames")String[] containersClassNames, @WebParam(name = "containersIds")String[] containersIds,
                                      @WebParam(name = "sideBClassNames")String[] sideBClassNames, @WebParam(name = "sideBIds")String[] sideBIds,
                                      @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
        /**
     * Disconnects a side or both sides of a physical connection (a link or a container)
     * @param connectionClass Class of the connection to be edited
     * @param connectionId Id of the connection to be edited
     * @param sideToDisconnect Side to disconnect. Use 1 to disconnect only the side a, 2 to disconnect only side b and 3 to disconnect both sides at once
     * @param sessionId Session token
     * @throws ServerSideException If the object can not be found
     *                             If either the object class or the attribute can not be found
     *                             If the class provided does not exist
     *                             If any of the relationships is now allowed according to the defined data model
     *                             If the object activity log could no be found
     */
    @WebMethod(operationName = "disconnectPhysicalConnection")
    public void disconnectPhysicalConnection(@WebParam(name = "connectionClass")String connectionClass,
                                      @WebParam(name = "connectionId")String connectionId, 
                                      @WebParam(name = "sideToDisconnect")int sideToDisconnect,
                                      @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
        
    /**
     * Changes one or both sides (endpoints) of a physical connection (link or container). Use this method carefully in containers, as it does not check 
     * if the endpoints of the links inside the container that was reconnected are consistent with its new endpoints. Also note that 
     * when used in physical links, the link will NOT be moved (as in the special containment hierarchy) to the nearest common parent of both endpoints. 
     * This method can not be used to <i>disconnect</i> connections, to do that use {@link #disconnectPhysicalConnection(java.lang.String, long, int, java.lang.String) }.
     * @param connectionClass The class of the connection to be modified
     * @param connectionId The id of the connection to be modified
     * @param newASideClass The class of the new side A of the connection. Use null if this side is not to be changed.
     * @param newASideId The id of the new side A of the connection. Use -1 if this side is not to be changed.
     * @param newBSideClass The class of the new side B of the connection. Use null if this side is not to be changed.
     * @param newBSideId The id of the new side B of the connection. Use -1 if this side is not to be changed.
     * @param sessionId The session token
     * @throws ServerSideException If any of the objects provided could not be found or if the new endpoint is not a port (if reconnecting a link) or if it is a port 
     * (if reconnecting a container)
     */
    @WebMethod(operationName = "reconnectPhysicalConnection")
    public void reconnectPhysicalConnection(@WebParam(name = "connectionClass")String connectionClass,
                                      @WebParam(name = "connectionId")String connectionId, 
                                      @WebParam(name = "newASideClass")String newASideClass,
                                      @WebParam(name = "newASideId")String newASideId,
                                      @WebParam(name = "newBSideClass")String newBSideClass,
                                      @WebParam(name = "newBSideId")String newBSideId,
                                      @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Deletes a physical connection
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object couldn't be found
     *                             If the class could not be found
     *                             If the object could not be deleted because there's some 
     *                            business rules that avoids it or it has incoming relationships.
     *                             If the log root node could not be found
     */
    @WebMethod(operationName = "deletePhysicalConnection")
    public void deletePhysicalConnection(
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    //Audit Trail
    /**
     * Retrieves the log entries for a given [business] object
     * @param objectClass Object class
     * @param objectId Object id
     * @param limit Max number of results (0 to retrieve all)
     * @param sessionId Session token
     * @return The object's audit trail
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the class provided is not subclass of  InventoryObject
     */
    @WebMethod(operationName = "getBusinessObjectAuditTrail")
    public ApplicationLogEntry[] getBusinessObjectAuditTrail (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the list of activity log entries
     * @param page current page
     * @param limit limit of results per page. 0 to retrieve them all
     * @param sessionId The session id
     * @return The list of activity log entries
     * @throws ServerSideException If the user is not allowed to invoke the method, If anything goes wrong
     */
    @WebMethod(operationName = "getGeneralActivityAuditTrail")
    public ApplicationLogEntry[] getGeneralActivityAuditTrail (
            @WebParam(name = "page")int page,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Attaches a file to an inventory object
     * @param name The name of the file. It's more like its title, instead of the file name
     * @param tags A semicolon (";") separated string with the tags associated to this document. These tags can be used to help find documents in a search
     * @param file The actual file
     * @param className The class name of the inventory object the file will be attached to
     * @param objectId The id of the inventory object the file will be attached to
     * @param sessionId Session token
     * @return The id of the file object that was created
     * @throws ServerSideException If the file can not be saved or if there's already a file with that name related to the object or if the file exceeds the max size configured
     */
    @WebMethod(operationName = "attachFileToObject")
    public long attachFileToObject(@WebParam(name = "name")String name, 
            @WebParam(name = "tags")String tags, @WebParam(name = "file")byte[] file, 
            @WebParam(name = "className")String className, @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Detaches a file from an inventory object. Note that the file will also be deleted. 
     * @param fileObjectId The id of the file object
     * @param className The class of the object the file will be detached from
     * @param objectId The id of the object the file will be detached from
     * @param sessionId The session token
     * @throws ServerSideException If the object or its class could not be found, or if the file object could not be found or if there was a problem physically deleting the file from disk
     */
    @WebMethod(operationName = "detachFileFromObject")
    public void detachFileFromObject(@WebParam(name = "fileObjectId")long fileObjectId,
            @WebParam(name = "className")String className, @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the files associated to a given inventory object
     * @param className The class of the object o retrieve the files from
     * @param objectId The id of the object o retrieve the files from
     * @param sessionId The session token
     * @return A list of light file objects
     * @throws ServerSideException If the object or its class could not be found
     */
    @WebMethod(operationName = "getFilesForObject")
    public List<RemoteFileObjectLight> getFilesForObject(@WebParam(name = "className")String className, @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves a particular file from those attached to an inventory object. The returned object contains the contents of the file
     * @param fileObjectId The id of the file object
     * @param className The class of the object the file will be detached from
     * @param objectId The id of the object the file will be detached from
     * @param sessionId The session token
     * @return The object file encapsulating the contents of the file.
     * @throws ServerSideException If the object or its class could not be found, or if the file object could not be found or if there was a problem physically deleting the file from disk
     */
    @WebMethod(operationName = "getFile")
    public RemoteFileObject getFile(@WebParam(name = "fileObjectId")long fileObjectId, 
            @WebParam(name = "className")String className, @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Updates the properties of a file object (name or tags)
     * @param fileObjectId The id of the file object
     * @param properties The set of properties as a dictionary key-value. Valid keys are "name" and "tags"
     * @param className The class of the object the file is attached to
     * @param objectId The id of the object the file is attached to
     * @param sessionId The session token
     * @throws ServerSideException If the object file is attached to could not be found or if the file object could not be found or if any of the properties has an invalid name or if the file name is empty or if the class of the object file is attached to could not be found
     */
    @WebMethod(operationName = "updateFileProperties")
    public void updateFileProperties(@WebParam(name = "fileObjectId") long fileObjectId, 
            @WebParam(name = "properties")List<StringPair> properties, @WebParam(name = "className")String className, 
            @WebParam(name = "objectId")String objectId, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Metadata Methods. Click on the + sign on the left to edit the code.">

    /**
     * Creates a class metadata object. This method is still under testing and might be buggy
     * @param className Class name
     * @param displayName Class display name
     * @param description Class description
     * @param isAbstract is this class abstract?
     * @param isCustom Is this class part of the core of the application (can not be deleted) or if it's an extension to the default data model. In most cases, this should be "true".
     * @param parentClassName Parent class name
     * @param isCountable NOt used so far. It's intended to be used to mark the classes that are created to make consistent the model, but that are not actual inventory elements, such as Slots
     * @param icon Icon for views. The size is limited by the value in Constants.MAX_ICON_SIZE and it's typically 32x32 pixels
     * @param isInDesign Says if a class can be instantiated or not. This is useful if you are creating many classes and want to avoid the users to create objects from those classes until you have finished the data model roll-out.
     * @param smallIcon Icon for trees. The size is limited by the value in Constants.MAX_ICON_SIZE
     * @param sessionId Session token
     * @param color The color to be used to display the instances of this class (depends on the client used)
     * @return the id of the new class metadata object
     * @throws ServerSideException If the specified parent class doesn't exist
     *                             If the reference node doesn't exist
     *                             If any of the fields of the class definition has an invalid value
     */
    @WebMethod(operationName = "createClass")
    public long createClass(@WebParam(name = "className")
        String className, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "description")
        String description, @WebParam(name = "isAbstract")
        boolean isAbstract, @WebParam(name = "isCustom")
        boolean isCustom, @WebParam(name = "isCountable")
        boolean isCountable, @WebParam(name = "isInDesign")
        boolean isInDesign, @WebParam(name = "parentClassName")
        String parentClassName, @WebParam(name = "icon")
        byte[] icon, @WebParam(name = "smallIcon")
        byte[] smallIcon, @WebParam(name = "color")
        int color, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException;

     /**
     * Updates a class metadata properties. Use null values for those properties that shouldn't be touched
     * @param classId
     * @param className metadata name. Null if unchanged
     * @param displayName New class metadata display name. Null if unchanged
     * @param description New class metadata description. Null if unchanged
     * @param isAbstract is this class abstract?
     * @param icon New icon for views. Null if unchanged. The size is limited by the value in Constants.MAX_ICON_SIZE
     * @param color The color of the instances of this class.
     * @param smallIcon New icon for trees. Null if unchanged. The size is limited by the value in Constants.MAX_ICON_SIZE
     * @param isInDesign If the class is in design stage (see createClass).
     * @param isCustom If the class is custom (see createClass).
     * @param isCountable If the class is countable (see createClass). 
     * @param sessionId Session token
     * @throws ServerSideException If there is any problem retrieving an object, 
     *                             while checking if every created object of the 
     *                             class with an attributes marked as mandatory has value.
     */
    @WebMethod(operationName = "setClassProperties")
    public void setClassProperties(@WebParam(name = "classId")
        long classId, @WebParam(name = "className")
        String className, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "description")
        String description, @WebParam(name = "smallIcon")
        byte[] smallIcon,  @WebParam(name = "icon")
        byte[] icon, @WebParam(name = "color")
        int color,@WebParam(name = "isAbstract")
        Boolean isAbstract, @WebParam(name = "isInDesign")
        Boolean isInDesign, @WebParam(name = "isCustom")
        Boolean isCustom, @WebParam(name = "isCountable")
        Boolean isCountable, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException;
    
    /**
     * Checks if a class has a attribute with a given name
     * @param className Class name
     * @param attributeName Attribute name
     * @param sessionId Session token
     * @return True if the given class has the attribute
     * @throws ServerSideException If there is no a class with such className
     */
    @WebMethod(operationName = "hasAttribute")
    public boolean hasAttribute(
        @WebParam(name = "className") String className, 
        @WebParam(name = "attributeName") String attributeName, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets a class attribute, using the class name as key to find it
     * @param className the class name
     * @param attributeName
     * @param sessionId Session token
     * @return the class attribute
     * @throws ServerSideException If there is no a class with such className
     *                             If the attributeName does not exist
     */
    @WebMethod(operationName = "getAttribute")
    public RemoteAttributeMetadata getAttribute(@WebParam(name = "className")
    String className, @WebParam(name = "attributeName")
    String attributeName, @WebParam(name = "sesionId")
    String sessionId) throws ServerSideException;
    
    /**
     * Gets a class attribute, using the class id as key to find it
     * @param classId Class id
     * @param attributeName Attribute name
     * @param sessionId  Session token
     * @return The attribute definition
     * @throws ServerSideException If there is no a class with such classId
     *                             If the attributeName does not exist
     */
    @WebMethod(operationName = "getAttributeForClassWithId")
    public RemoteAttributeMetadata getAttributeForClassWithId(@WebParam(name = "classId")
        String classId, @WebParam(name = "attributeName")
        String attributeName, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException;
   
    /**
     * Adds an attribute to a class using its name as key to find it. If value of a given attribute is null, a default value will be set (except for the name, which is mandatory)
     * @param className Class name where the attribute will be attached
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param administrative is the attribute administrative?
     * @param visible is the attribute visible?
     * @param noCopy Marks an attribute as not to be copied during a copy operation.
     * @param isReadOnly is the attribute read only?
     * @param unique should this attribute be unique?
     * @param mandatory is the attribute mandatory when an object is created
     * @param multiple Indicates if the attribute is a multiple selection list type. This flag has no effect in primitive types, such as strings or numbers
     * @param order Tells the system how to sort the attributes. A call to any method that returns the attributes of a class will return them sorted by order.
     * This is useful to show the attributes in property sheets in order of importance, for example. The default value is 1000
     * @param sessionId session token
     * @throws ServerSideException If there is no a class with such className
     *                             If any of the parameters to create the attribute has a wrong value
     */
    @WebMethod(operationName = "createAttribute")
    public void createAttribute(@WebParam(name = "className")
        String className,  @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        String description, @WebParam(name = "administrative")
        boolean administrative, @WebParam(name = "visible")
        boolean visible, @WebParam(name = "isReadOnly")
        boolean isReadOnly, @WebParam(name = "noCopy")
        boolean noCopy, @WebParam(name = "unique")
        boolean unique, @WebParam(name = "mandatory")
        boolean mandatory, @WebParam(name = "multiple")
        boolean multiple, @WebParam(name = "order")
        int order, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException;

    /**
     * Adds an attribute to a class using its id as key to find it. If value of a given attribute is null, a default value will be put in place (except for the name, which is mandatory)
     * @param ClassId Class id where the attribute will be attached
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param administrative is the attribute administrative?
     * @param visible is the attribute visible?
     * @param readOnly is the attribute read only?
     * @param noCopy Marks an attribute as not to be copied during a copy operation.
     * @param unique should this attribute be unique?
     * @param mandatory is the attribute mandatory when an object is created
     * @param multiple Indicates if the attribute is a multiple selection list type. This flag has no effect in primitive types, such as strings or numbers
     * @param order Tells the system how to sort the attributes. A call to any method that returns the attributes of a class will return them sorted by order.
     * This is useful to show the attributes in property sheets in order of importance, for example. The default value is 1000
     * @param sessionId session token
     * @throws ServerSideException If any of the parameters to create the attribute has a wrong value
     */
    @WebMethod(operationName = "createAttributeForClassWithId")
    public void createAttributeForClassWithId(@WebParam(name = "classId")
        long ClassId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        String description, @WebParam(name = "administrative")
        boolean administrative, @WebParam(name = "visible")
        boolean visible, @WebParam(name = "readOnly")
        boolean readOnly, @WebParam(name = "noCopy")
        boolean noCopy, @WebParam(name = "unique")
        boolean unique, @WebParam(name = "mandatory")
        boolean mandatory, @WebParam(name = "multiple")
        boolean multiple, @WebParam(name = "order")
        int order, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException;
    
    /**
     * Updates a class attribute taking its name as key to find it. If value of a given attribute is null, the old value will remain unchanged.
     * @param className Class the attribute belongs to
     * @param attributeId attribute id
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param administrative is the attribute administrative?
     * @param visible is the attribute visible?
     * @param readOnly is the attribute read only?
     * @param unique should this attribute be unique?
     * @param mandatory is the attribute mandatory when an object is created
     * @param multiple Indicates if the attribute is a multiple selection list type. This flag has no effect in primitive types, such as strings or numbers
     * @param noCopy can this attribute be copy in copy/paste operation?
     * @param order Tells the system how to sort the attributes. A call to any method that returns the attributes of a class will return them sorted by order.
     * This is useful to show the attributes in property sheets in order of importance, for example. The default value is 1000
     * @param sessionId session token
     * @throws ServerSideException If an object can't be find, while it is checking 
     *                             if every object of the class (or subclasses) has 
     *                             a value in an attribute marked as mandatory
     */
    @WebMethod(operationName = "setAttributeProperties")
    public void setAttributeProperties(@WebParam(name = "className")
        String className, @WebParam(name = "attributeId")
        long attributeId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "description")
        String description, @WebParam(name = "type")
        String type, @WebParam(name = "administrative")
        Boolean administrative, @WebParam(name = "mandatory")
        Boolean mandatory, @WebParam(name = "multiple")
        Boolean multiple, @WebParam(name = "noCopy")
        Boolean noCopy, @WebParam(name = "readOnly")
        Boolean readOnly, @WebParam(name = "unique")
        Boolean unique, @WebParam(name = "visible")
        Boolean visible, @WebParam(name = "order")
        Integer order, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException;

    /**
     * Updates a class attribute taking its id as key to find it. If value of a given attribute is null, the old value will remain unchanged.
     * @param classId Class the attribute belongs to
     * @param attributeId attribute id
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param administrative is the attribute administrative?
     * @param visible is the attribute visible?
     * @param readOnly is the attribute read only?
     * @param unique should this attribute be unique?
     * @param mandatory is the attribute mandatory when an object is created
     * @param multiple Indicates if the attribute is a multiple selection list type. This flag has no effect in primitive types, such as strings or numbers
     * @param noCopy can this attribute be copy in copy/paste operation?
     * @param order Tells the system how to sort the attributes. A call to any method that returns the attributes of a class will return them sorted by order.
     * This is useful to show the attributes in property sheets in order of importance, for example. The default value is 1000
     * @param sessionId session token
     * @throws ServerSideException If an object can't be find, while it is checking 
     *                             if every object of the class (or subclasses) has 
     *                             a value in an attribute marked as mandatory
     */
    @WebMethod(operationName = "setAttributePropertiesForClassWithId")
    public void setAttributePropertiesForClassWithId(@WebParam(name = "classId")
        long classId, @WebParam(name = "attributeId")
        long attributeId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "description")
        String description, @WebParam(name = "type")
        String type, @WebParam(name = "administrative")
        Boolean administrative, @WebParam(name = "mandatory")
        Boolean mandatory, @WebParam(name = "multiple")
        Boolean multiple, @WebParam(name = "noCopy")
        Boolean noCopy, @WebParam(name = "readOnly")
        Boolean readOnly, @WebParam(name = "unique")
        Boolean unique, @WebParam(name = "visible")
        Boolean visible, @WebParam(name = "order")
        Integer order, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException;
    
    /**
     * Deletes an attribute from a class using the class name as key to find it
     * @param className Class name
     * @param attributeName Attribute name
     * @param sessionId Session token
     * @throws ServerSideException If the class could not be found.
     *                             If the attributes name or creationDate are to be deleted.
     */    
    @WebMethod(operationName = "deleteAttribute")
    public void deleteAttribute(@WebParam(name = "className") 
            String className, @WebParam(name = "attributeName")
            String attributeName, @WebParam(name = "sessionId")
            String sessionId) throws ServerSideException;

    /**
     * Deletes an attribute from a class using the class id as key to find it
     * @param classId Class id
     * @param attributeName Attribute name
     * @param sessionId Session token
     * @throws ServerSideException If the class could not be found.
     *                             If the attributes name or creationDate are to be deleted.
     */
    @WebMethod(operationName = "deleteAttributeForClassWithId")
    public void deleteAttributeForClassWithId(@WebParam(name = "classId") 
            long classId, @WebParam(name = "attributeName")
            String attributeName, @WebParam(name = "sessionId")
            String sessionId) throws ServerSideException;

    /**
     * Gets the metadata of a given class using its name as key to find it
     * @param className Class name
     * @param sessionId Session token
     * @return The metadata as a ClassInfo instance
     * @throws ServerSideException If there is no class with such className
     */
    @WebMethod(operationName = "getClass")
    public RemoteClassMetadata getClass(@WebParam(name = "className")
    String className, @WebParam(name = "sessionId")
    String sessionId) throws ServerSideException;

    /**
     * Gets the metadata of a given class using its id as key to find it
     * @param classId Class metadata object id
     * @param sessionId session token
     * @return The metadata as a ClassInfo instance
     * @throws ServerSideException If there is no class with such classId
     */
    @WebMethod(operationName = "getClassWithId")
    public RemoteClassMetadata getClassWithId(@WebParam(name = "classId")
    long classId, @WebParam(name = "sessionId")
    String sessionId) throws ServerSideException;
    
    /**
     * Gets the subclasses of a given class
     * @param className Class name
     * @param includeAbstractClasses should the result include the abstract classes?
     * @param includeSelf Should the list include the subclasses and the parent class?
     * @param sessionId Session token
     * @return The list of subclasses
     * @throws ServerSideException If the provided class is not a subclass of InventoryObject
     */
    @WebMethod(operationName = "getSubClassesLight")
    public List<RemoteClassMetadataLight> getSubClassesLight(
            @WebParam(name = "className")String className,
            @WebParam(name = "includeAbstractClasses")boolean includeAbstractClasses,
            @WebParam(name = "includeSelf")boolean includeSelf,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the subclasses of a given class
     * @param className Class name
     * @param includeAbstractClasses should the result include the abstract classes?
     * @param includeSelf Should the list include the subclasses and the parent class?
     * @param sessionId Session token
     * @return The list of subclasses
     * @throws ServerSideException Exception If the class could not be found.
     */
    @WebMethod(operationName = "getSubClassesLightNoRecursive")
    public List<RemoteClassMetadataLight> getSubClassesLightNoRecursive(
            @WebParam(name = "className")String className,
            @WebParam(name = "includeAbstractClasses")boolean includeAbstractClasses,
            @WebParam(name = "includeSelf")boolean includeSelf,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Retrieves the metadata for the entire class hierarchy as ClassInfo instances
     * @param sessionId Session token
     * @param includeListTypes boolean to indicate if the list should include the subclasses of
     * GenericObjectList
     * @return An array with the metadata for the entire class hierarchy as ClassInfo instances
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getAllClasses")
    public List<RemoteClassMetadata> getAllClasses(
            @WebParam(name = "includeListTypes")boolean includeListTypes,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the metadata for the entire class hierarchy as ClassInfoLight instances
     * @param sessionId Session token
     * @param includeListTypes boolean to indicate if the list should include the subclasses of
     * GenericObjectList
     * @return An array with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws ServerSideException If GenericListType class does not exist.
     */
    @WebMethod(operationName = "getAllClassesLight")
    public List<RemoteClassMetadataLight> getAllClassesLight(
            @WebParam(name = "includeListTypes")boolean includeListTypes,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Deletes a class from the data model using its name as key to find it
     * @param className Class name
     * @param sessionId Session token
     * @throws ServerSideException If there is not a class with de ClassName
     *                             If the class is a core class, has instances, has 
     *                            incoming relationships or is a list type that is 
     *                            used by another class.
     */
    @WebMethod(operationName = "deleteClass")
    public void deleteClass(@WebParam(name = "className")
    String className, @WebParam(name = "sessionId")
    String sessionId) throws ServerSideException;

    /**
     * Deletes a class from the data model using its id as key to find it
     * @param classId Class id
     * @param sessionId Session token
     * @throws ServerSideException If the class is a core class, has instances, has 
     *                             incoming relationships or is a list type that is
     *                             used by another class.
     */
    @WebMethod(operationName = "deleteClassWithId")
    public void deleteClassWithId(@WebParam(name = "classId")long classId, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;

    /**
     * Gets the possible children of a class according to the containment hierarchy. This method is recursive, and if a possible child is an abstract class, it gets its non-abstract subclasses
     * @param parentClassName Class to retrieve its possible children
     * @param sessionId Session token
     * @return A list of possible children as ClassInfoLight instances
     * An array with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws ServerSideException If the class can not be found
     */
    @WebMethod(operationName = "getPossibleChildren")
    public List<RemoteClassMetadataLight> getPossibleChildren(@WebParam(name = "parentClassName")
                    String parentClassName, @WebParam(name = "sessionId")
                    String sessionId) throws ServerSideException;
    
    /**
     * Gets the possible special children of a class according to the containment hierarchy. 
     * This method is recursive, and if a possible special child is an abstract class, 
     * it gets its non-abstract subclasses
     * @param parentClassName Class to retrieve its possible special children
     * @param sessionId Session token
     * @return A list of possible special children as ClassInfoLight instances
     * @throws ServerSideException If the class can not be found
     */
    @WebMethod(operationName = "getPossibleSpecialChildren")
    public List<RemoteClassMetadataLight> getPossibleSpecialChildren(
        @WebParam(name = "parentClassName") String parentClassName, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Gets the possible children of a class according to the containment hierarchy.
     * This method is not recursive, and only returns the direct possible children,
     * even if they're abstract
     * @param parentClassName Class to retrieve its possible children
     * @param sessionId Session token
     * @return An array with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws ServerSideException If the class could not be found
     */
    @WebMethod(operationName = "getPossibleChildrenNoRecursive")
    public List<RemoteClassMetadataLight> getPossibleChildrenNoRecursive(@WebParam(name = "parentClassName")
    String parentClassName, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the possible special children of a class according to the containment hierarchy.
     * This method is not recursive, and only returns the direct possible special children,
     * even if they're abstract
     * @param parentClassName Class to retrieve its possible special children
     * @param sessionId Session token
     * @return A List with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws ServerSideException If the class could not be found
     */
    @WebMethod(operationName = "getPossibleSpecialChildrenNoRecursive")
    public List<RemoteClassMetadataLight> getPossibleSpecialChildrenNoRecursive(
        @WebParam(name = "parentClassName") String parentClassName, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
        
    /**
     * Adds possible children to a given class using its id as argument. If any of the arguments provided are already added,
     * it will abort the operation and rise an exception
     * @param parentClassId Class to attach the new possible children
     * @param newPossibleChildren List of new possible children. Abstract classes are de-aggregated
     * @param sessionId Session token
     * @throws ServerSideException If any of the possible children or the parent doesn't exist
     *                             If any of the possible children classes already are possible children.
     */
    @WebMethod(operationName = "addPossibleChildrenForClassWithId")
    public void addPossibleChildrenForClassWithId(@WebParam(name = "parentClassId")
            long parentClassId, @WebParam(name = "childrenToBeAdded")
            long[] newPossibleChildren, @WebParam(name = "sessionId")
            String sessionId) throws ServerSideException;
    
    /**
     * Adds possible special children to a given class using its id as argument. If any of the arguments provided are already added,
     * it will abort the operation and rise an exception
     * @param parentClassId Class to attach the new possible special children
     * @param possibleSpecialChildren List of new possible children. Abstract classes are de-aggregated
     * @param sessionId Session token
     * @throws ServerSideException If any of the possible children or the parent doesn't exist
     *                             If any of the possible children classes already are possible special children.
     */
    @WebMethod(operationName = "addPossibleSpecialChildrenWithId")
    public void addPossibleSpecialChildrenWithId(
        @WebParam(name = "parentClassId") long parentClassId, 
        @WebParam(name = "possibleSpecialChildren") long[] possibleSpecialChildren, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

     /**
     * Adds possible children to a given class using its name as argument.
     * If any of the arguments provided are already added,
     * it will abort the operation and rise an exception
     * @param parentClassName Class to attach the new possible children
     * @param childrenToBeAdded List of new possible children. Abstract classes are de-aggregated
     * @param sessionId Session token
     * @throws ServerSideException If the parent class or any of the possible children can not be found
     *                             If any of the given possible children can not be a possible children of parentClassName
     */
    @WebMethod(operationName = "addPossibleChildren")
    public void addPossibleChildren(@WebParam(name = "parentClassName")
            String parentClassName, @WebParam(name = "childrenToBeAdded")
            String[] childrenToBeAdded, @WebParam(name = "sessionId")
            String sessionId) throws ServerSideException;
    
    /**
     * Adds special possible children to a given class using its name.
     * If any of the arguments provided are already added,
     * it will abort the operation and rise an exception
     * @param parentClassName Class to attach the new possible special children
     * @param possibleSpecialChildren List of new possible special children. Abstract classes are de-aggregated
     * @param sessionId Session token
     * @throws ServerSideException  If the parent class or any of the possible children can not be found
     *                              If any of the given possible children can not be a possible children of parentClassName
     */
    @WebMethod(operationName = "addPossibleSpecialChildren")
    public void addPossibleSpecialChildren(
        @WebParam(name = "parentClassName") String parentClassName, 
        @WebParam(name = "possibleSpecialChildren") String[] possibleSpecialChildren, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Removes a set of possible children for a given class
     * @param parentClassId Class the possible children are going to be removed from
     * @param childrenToBeRemoved List of ids of classes to be removed as possible children
     * @param sessionId Session token
     * @throws ServerSideException If any of the ids provided can't be found
     */
    @WebMethod(operationName = "removePossibleChildrenForClassWithId")
    public void removePossibleChildrenForClassWithId(@WebParam(name = "parentClassId")
    long parentClassId, @WebParam(name = "childrenToBeRemoved")
    long[] childrenToBeRemoved, @WebParam(name = "sessionId")
    String sessionId) throws ServerSideException; 
    
    
    /**
     * Removes a set of possible special children for a given class.
     * @param parentClassId Parent Class of the possible special children are going to be removed from
     * @param specialChildrenToBeRemoved List of ids of classes to be remove as possible special children
     * @param sessionId Session token
     * @throws ServerSideException If any of the ids provided can't be found
     */
    @WebMethod(operationName = "removePossibleSpecialChildren")
    public void removePossibleSpecialChildren(
        @WebParam(name = "parentClassId") long parentClassId, 
        @WebParam(name = "specialChildrenToBeRemoved") long[] specialChildrenToBeRemoved, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Gets the containment hierarchy of a given class, but upwards (i.e. for Building, it could return 
     * City, Country, Continent)
     * @param className Class to be evaluated
     * @param recursive do it recursively or not
     * @param sessionId
     * @return List of classes in the upstream containment hierarchy
     * @throws ServerSideException If className does not correspond to any existing class
     */
    @WebMethod(operationName = "getUpstreamContainmentHierarchy")
    public List<RemoteClassMetadataLight> getUpstreamContainmentHierarchy(@WebParam(name = "className")
            String className, @WebParam(name = "recursive")
            boolean recursive, @WebParam(name = "sessionId")
            String sessionId) throws ServerSideException;
    
    /**
     * Gets the special containment hierarchy of a given class, but upwards (i.e. for Building, it could return 
     * City, Country, Continent)
     * @param className Class to be evaluated
     * @param recursive Do it recursively or not
     * @param sessionId Session id token
     * @return List of classes in upstream special containment hierarchy
     * @throws ServerSideException If className does not correspond to any existing class
     * 
     */
    @WebMethod(operationName = "getUpstreamSpecialContainmentHierarchy")
    public List<RemoteClassMetadataLight> getUpstreamSpecialContainmentHierarchy(
        @WebParam(name="className") String className, 
        @WebParam(name="recursive") boolean recursive, 
        @WebParam(name="sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the parent classes of a given class up to RootObject.
     * @param className The class to get the superclasses from
     * @param includeSelf If the result should also include the class in className
     * @param sessionId The session token
     * @return The list of super classes until the root of the hierarchy
     * @throws ServerSideException If the class provided could not be found
     */
    @WebMethod(operationName = "getUpstreamClassHierarchy")
    public List<RemoteClassMetadataLight> getUpstreamClassHierarchy(
        @WebParam(name="className") String className, 
        @WebParam(name="includeSelf") boolean includeSelf, 
        @WebParam(name="sessionId") String sessionId) throws ServerSideException;
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Utility methods. Click on the + sign on the left to edit the code.">/**
    /**
     * Tests if a class is subclass of another.
     * @param className Class to be tested.
     * @param allegedParentClass Class to be tested against.
     * @param sessionId Session token.
     * @return If the tested class is subclass of allegedParentClass or not.
     * @throws ServerSideException In case something goes wrong.
     */
    @WebMethod(operationName =  "isSubclassOf")
    public boolean isSubClassOf(@WebParam(name = "className") String className, 
                                @WebParam(name = "allegedParentClass") String allegedParentClass,
                                @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Sync/ bulk load methods. Click on the + sign on the left to edit the code.">/**
    /**
     * Creates many objects at once given a well formatted file. See user manual for details on how to format the file
     * @param file The file with size no greater 
     * @param commitSize The records are not committed one by one, but in batch. This number tells Kuwaiba how many records (lines) to commit at once.
     * @param dataType What kind of data contains the file, listTypes, inventory objects, etc
     * @param sessionId Session token.
     * @return The result of the operation.
     * @throws ServerSideException If something goes wrong.
     */
    @WebMethod(operationName = "bulkUpload")
    public String bulkUpload(@WebParam(name = "file")
        byte[] file, @WebParam(name = "commitSize")
        int commitSize, @WebParam(name = "dataType")
        int dataType, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the log file product of a bulk load operation.
     * @param fileName The name of the file  (provided by the method that performs the bulk creation)
     * @param sessionId Session token
     * @return The contents of the file.
     * @throws ServerSideException  If something goes wrong.
     */
    @WebMethod(operationName = "downloadBulkLoadLog")
    public byte[] downloadBulkLoadLog(@WebParam(name = "fileName")
        String fileName, @WebParam(name = "sessionId")
            String sessionId) throws ServerSideException;
    // </editor-fold>
    
    //<editor-fold desc="Templates" defaultstate="collapsed">
    /**
     * Creates a template.
     * @param templateClass The class you want to create a template for.
     * @param templateName The name of the template.
     * @param sessionId Session token.
     * @return The id of the newly created template.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the provided class does not exist
     *                             If the template class is abstract.
     */
    @WebMethod(operationName = "createTemplate")
    public String createTemplate(@WebParam(name = "templateClass")String templateClass, 
            @WebParam(name = "templateName")String templateName, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates an object inside a template.
     * @param templateElementClass Class of the object you want to create.
     * @param templateElementParentClassName Class of the parent to the object you want to create.
     * @param templateElementParentId Id of the parent to the object you want to create.
     * @param templateElementName Name of the element.
     * @param sessionId Session token.
     * @return The id of the new object.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object (or its parent) class could not be found
     *                             If the parent object could not be found
     *                             If the class provided to create the new element from is abstract.
     */
    @WebMethod(operationName = "createTemplateElement")
    public String createTemplateElement(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementParentClassName")String templateElementParentClassName,
            @WebParam(name = "templateElementParentId")String templateElementParentId,
            @WebParam(name = "templateElementName")String templateElementName,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates an special object inside a template.
     * @param tsElementClass Class of the special object you want to create.
     * @param tsElementParentClassName Class of the parent to the special object you want to create.
     * @param tsElementParentId Id of the parent to the special object you want to create.
     * @param tsElementName Name of the element.
     * @param sessionId Session token.
     * @return The id of the new object.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the element class are not a possible special child of the element parent class
     *                             If the element class given are abstract
     *                             If the element class or element parent class can not be found
     *                             If the element parent can no be found
     */
    @WebMethod(operationName = "createTemplateSpecialElement")
    public String createTemplateSpecialElement(
            @WebParam(name = "templateElementClass")String tsElementClass, 
            @WebParam(name = "tsElementParentClassName")String tsElementParentClassName,
            @WebParam(name = "tsElementParentId")String tsElementParentId,
            @WebParam(name = "tsElementName")String tsElementName,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates multiple template elements using a given name pattern
     * @param templateElementClassName The class name of the new set of template elements
     * @param templateElementParentClassName The parent class name of the new set of template elements
     * @param templateElementParentId The parent id of the new set of template elements     
     * @param templateElementNamePattern Name pattern of the new set of template elements
     * @param sessionId Session id token
     * @return An array of ids for the new template elements
     * @throws ServerSideException If the parent class name or the template element class name cannot be found
     *                             If the given template element class cannot be a child of the given parent
     *                             If the parent class name cannot be found
     *                             If the given pattern to generate the name has less possibilities that the number of template elements to be created
     */
    @WebMethod(operationName = "createBulkTemplateElement")
    public String[] createBulkTemplateElement(
        @WebParam(name = "templateElementClass") String templateElementClassName, 
        @WebParam(name = "templateElementParentClassName") String templateElementParentClassName, 
        @WebParam(name = "templateElementParentId") String templateElementParentId,         
        @WebParam(name = "templateElementNamePattern") String templateElementNamePattern, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates multiple special template elements using a given name pattern
     * @param stElementClass The class name of the new set of special template elements
     * @param stElementParentClassName The parent class name of the new set of special template elements
     * @param stElementParentId The parent id of the new set of special template elements     
     * @param stElementNamePattern Name pattern of the new set of special template elements
     * @param sessionId Session id token
     * @return An array if ids for the new special template elements
     * @throws ServerSideException If the parent class name or the special template element class name cannot be found
     *                             If the given special template element class cannot be a child of the given parent
     *                             If the parent class name cannot be found
     *                             If the given pattern to generate the name has less possibilities that the number of special template elements to be created
     */
    @WebMethod(operationName = "createBulkSpecialTemplateElement")
    public String[] createBulkSpecialTemplateElement(
        @WebParam(name = "stElementClass") String stElementClass, 
        @WebParam(name = "stElementParentClassName") String stElementParentClassName, 
        @WebParam(name = "stElementParentId") String stElementParentId,         
        @WebParam(name = "stElementNamePattern") String stElementNamePattern, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates the value of an attribute of a template element.
     * @param templateElementClass Class of the element you want to update.
     * @param templateElementId Id of the element you want to update.
     * @param attributeNames Names of the attributes that you want to be updated as an array of strings.
     * @param attributeValues The values of the attributes you want to update. For list types, it's the id of the related type
     * @param sessionId Session token.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the classes provided as arguments do not exist
     *                             If the template element could not be found
     *                             If the arrays attributeNames and attributeValues have different sizes
     */
    @WebMethod(operationName = "updateTemplateElement")
    public void updateTemplateElement(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementId")String templateElementId,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Deletes an element within a template or a template itself.
     * @param templateElementClass The template element class.
     * @param templateElementId The template element id.
     * @param sessionId Session token.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the element's class could not be found.
     *                             If the element could not be found.
     */
    @WebMethod(operationName = "deleteTemplateElement")
    public void deleteTemplateElement(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementId")String templateElementId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the templates available for a given class
     * @param className Class whose templates we need
     * @param sessionId Session token
     * @return A list of templates (actually, the top element) as a list of RemoteOObjects
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the class provided could not be found.
     */
    @WebMethod(operationName = "getTemplatesForClass")
    public List<RemoteObjectLight> getTemplatesForClass(
            @WebParam(name = "className")String className, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Copy template elements within templates. Should not be used to copy entire templates.
     * @param sourceObjectsClassNames Array with the class names of the elements to be copied.
     * @param sourceObjectsIds  Array with the ids of the elements to be copied.
     * @param newParentClassName Class of the parent of the copied objects.
     * @param newParentId Id of the parent of the copied objects.
     * @param sessionId Session token.
     * @return An array with the ids of the newly created elements in the same order they were provided.
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If any of the classes could not be found.
     *                             If any of the source template elements could not be found.
     *                             If the arrays provided as arguments have different sizes.
     */
    @WebMethod(operationName = "copyTemplateElements")
    public String[] copyTemplateElements(@WebParam(name = "sourceObjectsClassNames")String[] sourceObjectsClassNames, 
                                       @WebParam(name = "sourceObjectsIds")String[] sourceObjectsIds, 
                                       @WebParam(name = "newParentClassName")String newParentClassName,
                                       @WebParam(name = "newParentId")String newParentId, 
                                       @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    
    /**
     * Copy template special elements within templates. Should not be used to copy entire templates.
     * @param sourceObjectsClassNames Array with the class names of the special elements to be copied.
     * @param sourceObjectsIds  Array with the ids of the special elements to be copied.
     * @param newParentClassName Class of the parent of the copied objects.
     * @param newParentId Id of the parent of the copied objects.
     * @param sessionId Session token.
     * @return An array with the ids of the newly created special elements in the same order they were provided.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the arrays provided as arguments have different sizes.
     *                             If any of the classes could not be found.
     *                             If any of the source template elements could not be found.
     */
    @WebMethod(operationName = "copyTemplateSpecialElements")
    public String[] copyTemplateSpecialElements(
        @WebParam(name = "sourceObjectsClassNames")String[] sourceObjectsClassNames, 
        @WebParam(name = "sourceObjectsIds")String[] sourceObjectsIds, 
        @WebParam(name = "newParentClassName")String newParentClassName,
        @WebParam(name = "newParentId")String newParentId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the children of a given template element.
     * @param templateElementClass Template element class.
     * @param templateElementId Template element id.
     * @param sessionId 
     * @return The template element's children as a list of RemoteBusinessObjectLight instances.
     * @throws ServerSideException If the user is not allowed to invoke the method
     */
    @WebMethod(operationName = "getTemplateElementChildren")
    public List<RemoteObjectLight> getTemplateElementChildren(
        @WebParam(name = "templateElementClass")String templateElementClass, 
        @WebParam(name = "templateElementId")String templateElementId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the children of a given template special element.
     * @param tsElementClass Template special element class.
     * @param tsElementId Template special element id.
     * @param sessionId 
     * @return The template element's children as a list of RemoteBusinessObjectLight instances.
     * @throws ServerSideException If the user is not allowed to invoke the method
     */
    @WebMethod(operationName = "getTemplateSpecialElementChildren")
    public List<RemoteObjectLight> getTemplateSpecialElementChildren(
        @WebParam(name = "tsElementClass")String tsElementClass, 
        @WebParam(name = "tsElementId")String tsElementId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrives all the information of a given template element.
     * @param templateElementClass Template element class.
     * @param templateElementId Template element id.
     * @param sessionId session token
     * @return The template element information
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the template class does not exist
     *                             If the template element could not be found
     *                             If an attribute value can't be mapped into value
     */
    @WebMethod(operationName = "getTemplateElement")
    public RemoteObject getTemplateElement(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementId")String templateElementId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Reporting API methods">
    /**
     * Creates a class level report (a report that will be available for all instances of a given class -and its subclasses-)
     * @param className Class this report is going to be related to. It can be ab abstract class and the report will be available for all its subclasses
     * @param reportName Name of the report.
     * @param reportDescription Report description.
     * @param script Script text.
     * @param outputType What will be the default output of this report? See ClassLevelReportDescriptor for possible values
     * @param enabled If enabled, a report can be executed.
     * @param sessionId Session token
     * @return The id of the newly created report.
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If the class provided could not be found.
     */
    @WebMethod(operationName = "createClassLevelReport")
    public long createClassLevelReport(@WebParam(name = "className")String className, 
            @WebParam(name = "reportName")String reportName, @WebParam(name = "reportDescription")String reportDescription, 
            @WebParam(name = "script")String script, @WebParam(name = "outputType")int outputType, 
            @WebParam(name = "enabled")boolean enabled, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates an inventory level report (a report that is not tied to a particlar instance or class. In most cases, they also receive parameters)
     * @param reportName Name of the report.
     * @param reportDescription Report description.
     * @param script Script text.
     * @param outputType What will be the default output of this report? See InventoryLevelReportDescriptor for possible values
     * @param enabled If enabled, a report can be executed.
     * @param parameters Optional (it might be either null or an empty array). The list of the names parameters that this report will support. They will always be captured as strings, so it's up to the author of the report the sanitization and conversion of the inputs
     * @param sessionId Session token
     * @return The id of the newly created report.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the dummy root could not be found, which is actually a severe problem.
     */
    @WebMethod(operationName = "createInventoryLevelReport")
    public long createInventoryLevelReport(@WebParam(name = "reportName")String reportName, 
            @WebParam(name = "reportDescription")String reportDescription, @WebParam(name = "script")String script, 
            @WebParam(name = "outputType")int outputType, @WebParam(name = "enabled")boolean enabled, 
            @WebParam(name = "parameters")List<StringPair> parameters, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Deletes a report
     * @param reportId The id of the report.
     * @param sessionId Session token.
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If the report could not be found.
     */
    @WebMethod(operationName = "deleteReport")
    public void deleteReport(@WebParam(name = "reportId")long reportId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Updates the properties of an existing class level report.
     * @param reportId Id of the report.
     * @param reportName The name of the report. Null to leave it unchanged.
     * @param reportDescription The description of the report. Null to leave it unchanged.
     * @param enabled Is the report enabled? . Null to leave it unchanged.
     * @param type Type of the output of the report. See LocalReportLight for possible values
     * @param script Text of the script. 
     * @param sessionId Session token.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the report properties has a wrong or unexpected format
     *                             If the report could not be found.
     */
    @WebMethod(operationName = "updateReport")
    public void updateReport(@WebParam(name = "reportId")long reportId, @WebParam(name = "reportName")String reportName, 
            @WebParam(name = "reportDescription")String reportDescription, @WebParam(name = "enabled")Boolean enabled,
            @WebParam(name = "type")Integer type, @WebParam(name = "script")String script, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Updates the value of any of the parameters of a given report.
     * @param reportId Report id.
     * @param parameters List of pairs attribute-value of the report. Valid values are name, description, script and enabled.
     * @param sessionId Session token.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the any of the parameters has an invalid name
     */
    @WebMethod(operationName = "updateReportParameters")
    public void updateReportParameters(@WebParam(name = "reportId")long reportId, 
            @WebParam(name = "parameters")List<StringPair> parameters, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the class level reports associated to the given class (or its superclasses)
     * @param className The class to extract the reports from.
     * @param recursive False to get only the directly associated reports. True top get also the reports associate top its superclasses
     * @param includeDisabled True to also include the reports marked as disabled. False to return only the enabled ones.
     * @param sessionId Session token.
     * @return The list of reports.
     * @throws ServerSideException If the class could not be found
     *                             If the user is not allowed to invoke the method
     */
    @WebMethod(operationName = "getClassLevelReports")
    public List<RemoteReportMetadataLight> getClassLevelReports(@WebParam(name = "className")String className, 
            @WebParam(name = "recursive") boolean recursive, 
            @WebParam(name = "includeDisabled") boolean includeDisabled, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the inventory class reports.
     * @param includeDisabled True to also include the reports marked as disabled. False to return only the enabled ones.
     * @param sessionId Session token.
     * @return The list of reports.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the dummy root could not be found, which is actually a severe problem.
     */
    @WebMethod(operationName = "getInventoryLevelReports")
    public List<RemoteReportMetadataLight> getInventoryLevelReports(@WebParam(name = "includeDisabled")boolean includeDisabled, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the information related to a class level report.
     * @param reportId The id of the report.
     * @param sessionId Session token.
     * @return  The report.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the report could not be found.
     */
    @WebMethod(operationName = "getReport")
    public RemoteReportMetadata getReport(@WebParam(name = "reportId")long reportId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Executes a class level report and returns the result.
     * @param objectClassName The class of the instance that will be used as input for the report.
     * @param objectId The id of the instance that will be used as input for the report.
     * @param reportId The id of the report.
     * @param sessionId Session token.
     * @return The result of the report execution.
     * @throws ServerSideException If the class could not be found or 
     *                             if the report could not be found or 
     *                             if the inventory object could not be found or 
     *                             if there's an error during the execution of the report. 
     */
    @WebMethod(operationName = "executeClassLevelReport")
    public byte[] executeClassLevelReport(@WebParam(name = "objectClassName")String objectClassName, 
            @WebParam(name = "objectId")String objectId, @WebParam(name = "reportId")long reportId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
   
    /**
     * Executes an inventory level report and returns the result.
     * @param reportId The id of the report.
     * @param parameters List of pairs param name - param value
     * @param sessionId Session token.
     * @return The result of the report execution.
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             If the report could not be found or 
     *                             if the associated script exits with error.
     */
    @WebMethod(operationName = "executeInventoryLevelReport")
    public byte[] executeInventoryLevelReport(@WebParam(name = "reportId")long reportId, 
            @WebParam(name = "parameters")List<StringPair> parameters, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    // </editor-fold>

    //<editor-fold desc="Favorites" defaultstate="collapsed">
    /**
     * Creates a favorites folder for User.
     * @param favoritesFolderName Bookmark folder name
     * @param userId User id
     * @param sessionId The session token
     * @return The id of the new Bookmark folder
     * @throws ServerSideException If the user is not allowed to invoke the method or 
     *                             If the user can not be found or
     *                             If the name is null or empty
     */
    @WebMethod(operationName = "createFavoritesFolderForUser")
    public long createFavoritesFolderForUser(
        @WebParam(name = "favoritesFolderName") String favoritesFolderName, 
        @WebParam(name = "userId") long userId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a set of favorites folders
     * @param favoritesFolderId Bookmark folder id
     * @param userId The User id
     * @param sessionId The session token
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             If any favorites folder in the array can not be found
     */
    @WebMethod(operationName = "deleteFavoritesFolders")
    public void deleteFavoritesFolders (
        @WebParam(name = "favoritesFolderId") long[] favoritesFolderId, 
        @WebParam(name = "userId") long userId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
        
    /**
     * Gets the list of favorites folders of a given User.
     * @param userId User id
     * @param sessionId The session token
     * @return The list of Bookmarks for user
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             If the user can not be found
     */
    @WebMethod(operationName = "getFavoritesFoldersForUser")
    public List<RemoteFavoritesFolder> getFavoritesFoldersForUser(
        @WebParam(name = "userId") long userId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Associates a list of objects to an existing favorites folder
     * @param objectClass Object class name
     * @param objectId Object id
     * @param favoritesFolderId Bookmark folder id
     * @param userId The User id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the favorites folder can not be found
     *                             If the object can not be found
     *                             If the object have a relationship with the favorite folder
     */
    @WebMethod(operationName = "addObjectsToFavoritesFolder")    
    public void addObjectsToFavoritesFolder(
        @WebParam(name = "objectClass") String[] objectClass, 
        @WebParam(name = "objectId") String[] objectId, 
        @WebParam(name = "favoritesFolderId") long favoritesFolderId, 
        @WebParam(name = "userId") long userId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Removes a list of objects from a given favorites folder
     * @param objectClass Object class name
     * @param objectId Object id
     * @param favoritesFolderId Bookmark folder id
     * @param userId User id the favorites folder belongs to
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the favorites folder can not be found
     *                             If the object can not be found
     *                             If the object can not be found
     */
    @WebMethod(operationName = "removeObjectsFromFavoritesFolder")
    public void removeObjectsFromFavoritesFolder(
        @WebParam(name = "objectClass") String[] objectClass, 
        @WebParam(name = "objectId") String[] objectId, 
        @WebParam(name = "favoritesFolderId") long favoritesFolderId, 
        @WebParam(name = "userId") long userId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the objects in a to favorites folder
     * @param favoritesFolderId Bookmark folder id
     * @param userId User Id
     * @param limit Max number of results. Use -1 to retrieve all.
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the favorites folder can not be found
     * @return The list of objects
     */
    @WebMethod(operationName = "getObjectsInFavoritesFolder")
    public List<RemoteObjectLight> getObjectsInFavoritesFolder(
        @WebParam(name = "favoritesFolderId") long favoritesFolderId, 
        @WebParam(name = "userId") long userId,
        @WebParam(name = "limit") int limit,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the favorites folders an object is included into.
     * @param userId User Id
     * @param objectClass Object Class name
     * @param objectId Object id
     * @param sessionId Session token
     * @return The list of bookmarks where an object is associated
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If the object is associated to a bookmark folder but 
     *                            The favorites folder is not associated to the current user
     */
    @WebMethod(operationName = "getFavoritesFoldersForObject")
    public List<RemoteFavoritesFolder> getFavoritesFoldersForObject(
        @WebParam(name = "userId") long userId,
        @WebParam(name = "objectClass") String objectClass, 
        @WebParam(name = "objectId") String objectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
        
    /**
     * Gets a favorites folder
     * @param favoritesFolderId Bookmark folder id
     * @param userId User id
     * @param sessionId Session token
     * @return The Bookmark folder
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the favorites folder can not be found
     */
    @WebMethod(operationName = "getFavoritesFolder")
    public RemoteFavoritesFolder getFavoritesFolder(
        @WebParam(name = "favoritesFolderId") long favoritesFolderId, 
        @WebParam(name = "userId") long userId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates a favorites folder
     * @param favoritesFolderId Favorites folder id
     * @param favoritesFolderName Favorites folder name
     * @param userId User id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the favorites folder can not be found
     *                             If the name of the favorites folder is null or empty
     */
    @WebMethod(operationName = "updateFavoritesFolder")
    public void updateFavoritesFolder(
        @WebParam(name = "favoritesFolderId") long favoritesFolderId, 
        @WebParam(name = "favoritesFolderName") String favoritesFolderName, 
        @WebParam(name = "userId") long userId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    //</editor-fold>
    
    //<editor-fold desc="Business Rule Engine" defaultstate="collapsed">
    /**
     * Creates a business rule given a set of constraints
     * @param ruleName Rule name
     * @param ruleDescription Rule description
     * @param ruleType Rule type. See BusinesRule.TYPE* for possible values.
     * @param ruleScope The scope of the rule. See BusinesRule.SCOPE* for possible values.
     * @param appliesTo The class this rule applies to. Can not be null.
     * @param ruleVersion The version of the rule. Useful to migrate it if necessary in further versions of the platform
     * @param constraints An array with the definition of the logic to be matched with the rule. Can not be empty or null
     * @param sessionId Session token
     * @return The id of the newly created business rule
     * @throws ServerSideException If any of the parameters is null (strings) or leer than 1 or if the constraints array is null or empty
     */
    @WebMethod(operationName = "createBusinessRule")
    public long createBusinessRule(@WebParam(name = "ruleName")String ruleName, @WebParam(name = "ruleDescription")String ruleDescription, 
            @WebParam(name = "ruleType")int ruleType, @WebParam(name = "ruleScope")int ruleScope, @WebParam(name = "appliesTo")String appliesTo, 
            @WebParam(name = "ruleVersion")String ruleVersion, @WebParam(name = "constraints")List<String> constraints, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Deletes a business rule
     * @param businessRuleId Rule id
     * @param sessionId Session token
     * @throws ServerSideException If the given rule does not exist
     */
    @WebMethod(operationName = "deleteBusinessRule")
    public void deleteBusinessRule(@WebParam(name = "businessRuleId")long businessRuleId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the business rules of a particular type.
     * @param type Rule type. See BusinesRule.TYPE* for possible values. Use -1 to retrieve all
     * @param sessionId Session token
     * @return The list of business rules with the matching type.
     * @throws ServerSideException If something unexpected happens
     */
    @WebMethod(operationName = "getBusinessRules")
    public List<RemoteBusinessRule> getBusinessRules(int type, String sessionId) throws ServerSideException;
    //</editor-fold>
    
    //<editor-fold desc="Synchronization Framework methods" defaultstate="collapsed">
    /**
     * Executes a supervised synchronization job, which consist on connecting to the sync data source 
     * using the configuration attached to the given sync group and finding the differences 
     * between the information currently in the inventory platform and what's in the sync data source. 
     * A supervised sync job needs a human to review the differences and decide what to do,
     * while an automated sync job automatically decides what to do based on built-in business rules.
     * Please note that the execution might take some time, so it is expected that the client to implement an asynchronous call
     * @param syncGroupId The sync group id
     * @param sessionId The session token
     * @return A list of differences that require the authorization of a user to be resolved
     * @throws ServerSideException If the sync group could not be found or if
     */
    @WebMethod(operationName = "launchSupervisedSynchronizationTask")
    public List<RemoteSyncFinding> launchSupervisedSynchronizationTask(@WebParam(name = "syncGroupId") long syncGroupId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
  
    /**
     * Executes an automated synchronization job, which consist on connecting to the sync data source 
     * using the configuration attached to the given sync group and finding the differences 
     * between the information currently in the inventory platform and what's in the sync data source. 
     * An automated sync job does not need human intervention it automatically decides what to do based 
     * on built-in business rules
     * @param syncGroupId The sync group id
     * @param providersName
     * @param sessionId The session token
     * @return The set of results 
     * @throws ServerSideException If the sync group could not be found
     */
    @WebMethod(operationName = "launchAutomatedSynchronizationTask")
    public List<RemoteSyncResult> launchAutomatedSynchronizationTask(
            @WebParam(name = "syncGroupId") long syncGroupId, 
            @WebParam(name = "providersName")  String providersName, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Executes an automated synchronization job, which consist on connecting to the sync data source 
     * using the configuration attached to the given sync group and finding the differences 
     * between the information currently in the inventory platform and what's in the sync data source. 
     * An automated sync job does not need human intervention it automatically decides what to do based 
     * on built-in business rules
     * @param synDsConfigIds The sync data source configurations ids
     * @param providersName
     * @param sessionId The session token
     * @return The set of results 
     * @throws ServerSideException If the sync group could not be found
     */
    @WebMethod(operationName = "launchAdHocAutomatedSynchronizationTask")
    public List<RemoteSyncResult> launchAdHocAutomatedSynchronizationTask(
            @WebParam(name = "synDsConfigIds") List<Long> synDsConfigIds, 
            @WebParam(name = "providersName")  String providersName, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Executes the synchronization actions that the user selected after check the  list of findings
     * @param syncGroupId the sync groupId
     * @param actions the list findings to be processed
     * @param sessionId the session token
     * @return the list of results after the actions were executed
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "executeSyncActions")
    public List<RemoteSyncResult> executeSyncActions(
            @WebParam(name = "syncGroupId") long syncGroupId,
            @WebParam(name = "actions") List<RemoteSyncAction> actions,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the current jobs which are executing
     * @param sessionId the session id token
     * @throws ServerSideException
     * @return The list of the current jobs which are executing
     */
    @WebMethod(operationName = "getCurrentJobs")
    public List<RemoteBackgroundJob> getCurrentJobs(
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Kills a job given its id
     * @param jobId id of job to kill
     * @param sessionId the session id token
     * @throws ServerSideException If the job cannot be found
     */
    @WebMethod(operationName = "killJob")
    public void killJob(
        @WebParam(name = "jobId") long jobId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="SDH Networks Module">
    /**
     * Creates an SDH transport link (STMX)
     * @param classNameEndpointA The class name of the endpoint A (some kind of port)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointB  The class name of the endpoint Z (some kind of port)
     * @param idEndpointB Id of endpoint Z
     * @param linkType Type of link (STM1, STM4, STM16, STM256, etc)
     * @param defaultName The default name of the transport link.
     * @param sessionId Session token
     * @return The id of the newly created transport link
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             If any of the requested objects can't be found or 
     *                             If any of the classes provided can not be found or
     *                             If any of the objects involved can't be connected.
     */
    @WebMethod(operationName = "createSDHTransportLink")
    public String createSDHTransportLink(@WebParam(name = "classNameEndpointA") String classNameEndpointA, 
            @WebParam(name = "idEndpointA") String idEndpointA, 
            @WebParam(name = "classNameEndpointB") String classNameEndpointB, 
            @WebParam(name = "idEndpointB") String idEndpointB, 
            @WebParam(name = "linkType") String linkType, 
            @WebParam(name = "defaultName") String defaultName, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates an SDH container link (VCX). In practical terms, it's always a high order container, such a VC4XXX
     * @param classNameEndpointA The class name of the endpoint A (a GenericCommunicationsEquipment)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointB  The class name of the endpoint B (GenericCommunicationsEquipment)
     * @param idEndpointB Id of endpoint B
     * @param linkType Type of link (VC4, VC3, V12, etc. A VC12 alone doesn't make much sense, though)
     * @param positions This param specifies the transport links and positions used by the container. For more details on how this works, please read the "SDH Model: Technical Design and Tools" document. Please note that is greatly advisable to provide them already sorted
     * @param defaultName the name to be assigned to the new element. If null, an empty string will be used
     * @param sessionId Sesion token
     * @return The id of the newly created container link
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             If any of the requested objects can't be found or 
     *                             If any of the classes provided can not be found or
     *                             If any of the objects involved can't be connected.
     */
    @WebMethod(operationName = "createSDHContainerLink")
    public String createSDHContainerLink(@WebParam(name = "classNameEndpointA") String classNameEndpointA, 
            @WebParam(name = "idEndpointA") String idEndpointA, 
            @WebParam(name = "classNameEndpointB") String classNameEndpointB,
            @WebParam(name = "idEndpointB") String idEndpointB,
            @WebParam(name = "linkType") String linkType, 
            @WebParam(name = "positions") List<RemoteSDHPosition> positions, 
            @WebParam(name = "defaultName") String defaultName,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates an SDH tributary link (VCXTributaryLink)
     * @param classNameEndpointA The class name of the endpoint A (some kind of tributary port)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointB  The class name of the endpoint B (some kind of tributary port)
     * @param idEndpointB Id of endpoint B
     * @param linkType Type of link (VC4TributaryLink, VC3TributaryLink, V12TributaryLink, etc)
     * @param positions This param specifies the transport links and positions used by the container. For more details on how this works, please read the SDH Model: Technical Design and Tools document. Please note that is greatly advisable to provide them already sorted. Please note that creating a tributary link automatically creates a container link to deliver it
     * @param defaultName the name to be assigned to the new element
     * @param sessionId Session token
     * @return The id of the newly created tributary link
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             If any of the requested objects can't be found or 
     *                             If any of the classes provided can not be found or
     *                             If any of the objects involved can't be connected.
     */
    @WebMethod(operationName = "createSDHTributaryLink")
    public String createSDHTributaryLink(@WebParam(name = "classNameEndpointA") String classNameEndpointA, 
            @WebParam(name = "idEndpointA") String idEndpointA, 
            @WebParam(name = "classNameEndpointB") String classNameEndpointB, 
            @WebParam(name = "idEndpointB") String idEndpointB, 
            @WebParam(name = "linkType") String linkType, 
            @WebParam(name = "positions") List<RemoteSDHPosition> positions, 
            @WebParam(name = "defaultName") String defaultName, 
            @WebParam(name = "sessionId")  String sessionId) throws ServerSideException;
    
    /**
     * Deletes a transport link
     * @param transportLinkClass Transport Link class
     * @param transportLinkId Transport link id
     * @param forceDelete Delete recursively all SDH elements transported by the transport link
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the transport link could not be found
     */
    @WebMethod(operationName = "deleteSDHTransportLink")
    public void deleteSDHTransportLink(@WebParam(name = "transportLinkClass") String transportLinkClass, 
            @WebParam(name = "transportLinkId") String transportLinkId, 
            @WebParam(name = "forceDelete") boolean forceDelete, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a container link
     * @param containerLinkClass Container link class
     * @param containerLinkId Container class id
     * @param forceDelete Delete recursively all SDH elements contained by the container link
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the container link could not be found
     */
    @WebMethod(operationName = "deleteSDHContainerLink")
    public void deleteSDHContainerLink(@WebParam(name = "containerLinkClass") String containerLinkClass, 
            @WebParam(name = "containerLinkId") String containerLinkId, 
            @WebParam(name = "forceDelete") boolean forceDelete, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a tributary link and its corresponding container link. This method will delete all the object relationships.
     * @param tributaryLinkClass The class of the tributary link
     * @param tributaryLinkId the id of the tributary link
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the tributary link could not be found
     */
    @WebMethod(operationName = "deleteSDHTributaryLink")
    public void deleteSDHTributaryLink(@WebParam(name = "tributaryLinkClass") String tributaryLinkClass, 
            @WebParam(name = "tributaryLinkId") String tributaryLinkId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Finds a route between two GenericcommunicationsEquipment based on the TransportLinks network map (for more details on how this works, please read the SDH Model: Technical Design and Tools document)
     * @param communicationsEquipmentClassA The class of one of the route endpoints
     * @param communicationsEquipmentIdA The id of one of the route endpoints
     * @param communicationsEquipmentClassB The class of the other route endpoint
     * @param communicationsEquipmentIB The id of the other route endpoint
     * @param sessionId Session token
     * @return A sorted list of RemoteObjectLights containing the route. This list includes the transport links and the nodes in between, including the very endpoints
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the given communication equipment is no subclass of GenericCommunicationsEquipment
     */
    @WebMethod(operationName = "findSDHRoutesUsingTransportLinks")
    public List<RemoteObjectLightList> findSDHRoutesUsingTransportLinks(@WebParam(name = "communicationsEquipmentClassA") String communicationsEquipmentClassA, 
                                            @WebParam(name = "communicationsEquipmentIdA") String  communicationsEquipmentIdA, 
                                            @WebParam(name = "communicationsEquipmentClassB") String communicationsEquipmentClassB, 
                                            @WebParam(name = "communicationsEquipmentIB") String  communicationsEquipmentIB, 
                                            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Finds a route between two GenericcommunicationsEquipment based on the ContainerLinks network map (for more details on how this works, please read the SDH Model: Technical Design and Tools document)
     * @param communicationsEquipmentClassA The class of one of the route endpoints
     * @param communicationsEquipmentIdA The id of one of the route endpoints
     * @param communicationsEquipmentClassB The class of the other route endpoint
     * @param communicationsEquipmentIB The id of the other route endpoint
     * @param sessionId Session token
     * @return A sorted list of RemoteObjectLights containing the route. This list includes the transport links and the nodes in between, including the very endpoints
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the given communication equipment is no subclass of GenericCommunicationsEquipment
     */
    @WebMethod(operationName = "findSDHRoutesUsingContainerLinks")
    public List<RemoteObjectLightList> findSDHRoutesUsingContainerLinks(@WebParam(name = "communicationsEquipmentClassA") String communicationsEquipmentClassA, 
                                            @WebParam(name = "communicationsEquipmentIdA") String  communicationsEquipmentIdA, 
                                            @WebParam(name = "communicationsEquipmentClassB") String communicationsEquipmentClassB, 
                                            @WebParam(name = "communicationsEquipmentIB") String  communicationsEquipmentIB, 
                                            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the container links within a transport link (e.g. the VC4XX in and STMX)
     * @param transportLinkClass Transportlink's class
     * @param transportLinkId Transportlink's id
     * @param sessionId Session token
     * @return The list of the containers that go through that transport link
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the given transport link is no subclass of GenericSDHTransportLink
     */
    @WebMethod(operationName = "getSDHTransportLinkStructure")
    public List<RemoteSDHContainerLinkDefinition> getSDHTransportLinkStructure(@WebParam(name = "transportLinkClass")String transportLinkClass, 
            @WebParam(name = "transportLinkId")String transportLinkId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the internal structure of a container link. This is useful to provide information about the occupation of a link. This is only applicable to VC4XX
     * @param containerLinkClass Container class
     * @param containerLinkId Container Id
     * @param sessionId Session token
     * @return The list of containers contained in the container
     * @throws ServerSideException If the user is not authorized to know the structure of a container link, 
     *                             if the container supplied is not subclass of GenericSDHHighOrderContainerLink, 
     *                             if the container could not be found or if the class could not be found
     */
    @WebMethod(operationName = "getSDHContainerLinkStructure")
    public List<RemoteSDHContainerLinkDefinition> getSDHContainerLinkStructure(@WebParam(name = "containerLinkClass")String containerLinkClass, 
            @WebParam(name = "containerLinkId")String containerLinkId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
        // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc="IPAM Module"> 
    /**
     * Retrieves all the pools of subnets
     * @param parentId parent id parent id of the pool, -1 to retrieve the pools from the root nodes
     * @param className IPv4 or IPv6 subnet
     * @param sessionId the session token
     * @return a set of subnet pools
     * @throws ServerSideException if there are not IPAM root nodes or if can't get the pools of a subnet pool
     */
    @WebMethod(operationName = "getSubnetPools")
    public List<RemotePool> getSubnetPools(@WebParam(name = "parentId") String parentId,
            @WebParam(name = "className") String className,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the subnets of a given pool id
     * @param poolId subnet pool id
     * @param limit limit of returned subnets, -1 to no limit
     * @param sessionId the session token
     * @return a set of subnets
     * @throws ServerSideException if the given subnet pool id is not valid
     */
    @WebMethod(operationName = "getSubnets")
    public List<RemoteObjectLight> getSubnets(@WebParam(name = "poolId")String poolId,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a pool of subnets if the parentId is -1 the pool will be created 
     * in the default root for pools of subnets
     * @param parentId subnet parent Id, -1 to if want to create the pool in the root node
     * @param subnetPoolName subnet pool name
     * @param subnetPoolDescription subnet pool description
     * @param className if is a IPv4 or an IPv6 subnet
     * @param sessionId session token
     * @return id of the created subnet pool 
     * @throws ServerSideException if the IPAM root nodes doesn't exists, or if the IPv4 or IPv6 classes doesn't exists
     */
    @WebMethod(operationName = "createSubnetPool")
    public String createSubnetPool(
            @WebParam(name = "parentId")String parentId, 
            @WebParam(name = "subnetPoolName")String subnetPoolName, 
            @WebParam(name = "subnetPoolDescription")String subnetPoolDescription, 
            @WebParam(name = "className")String className, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a subnet
     * @param poolId The id of the pool that will contain the subnet
     * @param className The class name of the subnet (e.g. SubnetIPv4, SubnetIPv6)
     * @param attributes The attributes that will be set on the newly created element as a string-based key-value dictionary
     * @param sessionId Session token.
     * @return The id of the new subnet.
     * @throws ServerSideException If something goes wrong, can't find the 
     * parent id, IPv4 or IPv6 classes doesn't exists, or some problem with 
     * attributes, different size between attribute names and attribute values.
     */
    @WebMethod(operationName = "createSubnet")
    public String createSubnet(@WebParam(name = "poolId")String poolId,
            @WebParam(name = "className")String className,
            @WebParam(name = "attributes")List<StringPair> attributes,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Deletes a set of subnet pools
     * @param ids ids of the pools to be deleted
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteSubnetPools")
    public void deleteSubnetPools(@WebParam(name = "ids")String[] ids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
     /**
     * Deletes a subnet. All subnets must be instances of the same class
     * @param oids The ids of the subnets to be deleted
     * @param className The subnet class
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteSubnets")
    public void deleteSubnets(@WebParam(name = "className") String className,
            @WebParam(name = "oids")List<String> oids,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
      * Gets the complete information about a given subnet (all its attributes)
      * @param id Subnet id
      * @param className Subnet class IPv4 o IPv6
      * @param sessionId Session token
      * @return a representation of the subnet as a RemoteObject
      * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
      */
    @WebMethod(operationName = "getSubnet")
    public RemoteObject getSubnet(
            @WebParam(name = "id") String id,
            @WebParam(name = "className") String className,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
      * Gets the complete information about a given subnet pool (all its attributes)
      * @param subnetPoolId Subnet pool id
      * @param sessionId Session token
      * @return a representation of the entity as a RemoteObject
      * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
      */
    @WebMethod(operationName = "getSubnetPool")
    public RemotePool getSubnetPool(
            @WebParam(name = "subnetPoolId") String subnetPoolId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Adds an IP address to a Subnet
     * @param id ipAddres id
     * @param parentClassName the parent class name
     * @param attributes IP address attributes as a String based key-value dictionary
     * @param sessionId The session token
     * @return the id of the new IP Address
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "addIPAddress")
    public String addIPAddress(@WebParam(name = "id")String id,
            @WebParam(name = "parentClassName")String parentClassName,
            @WebParam(name = "attributes")List<StringPair> attributes,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Removes a set of IP Addresses from a subnet. Note that this method must be used only for Subnet objects
     * @param oids ids of the IPs to be deleted
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "removeIP")
    public void removeIP(
            @WebParam(name = "oid")String[] oids,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the IP addresses of a subnet
     * @param id subnet id
     * @param limit limit of returned subnets
     * @param className the class name
     * @param sessionId the session id
     * @return a set of IPs
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getSubnetUsedIps")
    public List<RemoteObjectLight> getSubnetUsedIps(@WebParam(name = "id")String id,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "className")String className,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the sub-subnets of a subnet
     * @param id subnet id
     * @param limit limit of returned subnets
     * @param className the class name if is IPv6 or IPv4
     * @param sessionId The session token
     * @return a set of subnets
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getSubnetsInSubnet")
    public List<RemoteObjectLight> getSubnetsInSubnet(@WebParam(name = "id")String id,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "className")String className,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a relation between a Subnet and a VLAN, this method is also using to 
     * associate VFRs, and BDIs to a VLAN  
     * TODO: check the model, there are redundant relationships
     * @param id Subnet id
     * @param className if the subnet has IPv4 or IPv6 IP addresses
     * @param vlanId VLAN id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "relateSubnetToVlan")
    public void relateSubnetToVlan (
            @WebParam(name = "id")String id,
            @WebParam(name = "className")String className,
            @WebParam(name = "vlanId")String vlanId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Releases the relation between a subnet and a VLAN, this method is also using 
     * to release VFRs, and BDIs from a VLAN  
     * TODO: check the model there are redundant relationships 
     * @param subnetId Subnet id
     * @param vlanId the VLAN id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime    
     */
    @WebMethod(operationName = "releaseSubnetFromVlan")
    public void releaseSubnetFromVlan (
            @WebParam(name = "subnetId")String subnetId,
            @WebParam(name = "vlanId")String vlanId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Releases the relation between a subnet and a VRF
     * @param subnetId Subnet id
     * @param vrfId the VRF id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime    
     */
    @WebMethod(operationName = "releaseSubnetFromVRF")
    public void releaseSubnetFromVRF (
            @WebParam(name = "subnetId")String subnetId,
            @WebParam(name = "vrfId")String vrfId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a relation between a subnet and a VRF
     * @param id Subnet id
     * @param className if the subnet is IPv4 or IPv6
     * @param vrfId VRF id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "relateSubnetToVrf")
    public void relateSubnetToVrf (
            @WebParam(name = "id")String id,
            @WebParam(name = "className")String className,
            @WebParam(name = "vrfId")String vrfId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Creates a relation between an IP address and a port
     * @param id IP address id
     * @param portClassName port class
     * @param portId port id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "relateIPtoPort")
    public void relateIPtoPort (
            @WebParam(name = "id")String id,
            @WebParam(name = "portClassName")String portClassName,
            @WebParam(name = "portId")String portId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Checks if a new subnet overlaps with an existing one
     * @param networkIp the network ip for the subnet
     * @param broadcastIp the broadcast ip for the subnet
     * @param sessionId Session token
     * @return true if overlaps
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "itOverlaps")
    public boolean itOverlaps (
            @WebParam(name = "networkIp")String networkIp,
            @WebParam(name = "broadcastIp")String broadcastIp,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Releases the relation between an IP address and a port
     * @param deviceClassName port class name
     * @param deviceId port id
     * @param id Subnet id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime    
     */
    @WebMethod(operationName = "releasePortFromIP")
    public void releasePortFromIP (
            @WebParam(name = "deviceClassName")String deviceClassName,
            @WebParam(name = "deviceId")String deviceId,
            @WebParam(name = "id")String id,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
        //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Contract Manager">
    /**
     * Creates a contract pool.
     * @param poolName Contract pool name. Must be subclass of GenericContract.
     * @param poolDescription Contract pool description.
     * @param poolClass Contract pool class.
     * @param sessionId Session id token.
     * @return The id of the newly created contract pool.
     * @throws ServerSideException If poolClass is not a valid subclass of GenericContract. 
     */
    @WebMethod(operationName = "createContractPool")
    public String createContractPool(
        @WebParam(name = "poolName") String poolName, 
        @WebParam(name = "poolDescription") String poolDescription, 
        @WebParam(name = "poolClass") String poolClass, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the contract pools properties.
     * @param poolId Contract pool id.
     * @param poolClass Contract pool class. Must be subclass of GenericContract.
     * @param sessionId Session id token.
     * @return The contract pool properties.
     * @throws ServerSideException If poolClass is not a valid subclass of GenericContract. 
     */
    @WebMethod(operationName = "getContractPool")
    public RemotePool getContractPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates the attributes of a contract pool.
     * @param poolId The id of the contract pool to be updated.
     * @param poolClass Contract pool class.
     * @param poolName Attribute value for pool name.
     * @param poolDescription Attribute value for pool description.
     * @param sessionId Session id token.
     * @throws ServerSideException If poolClass is not a valid subclass of GenericContract. 
     */
    @WebMethod(operationName = "updateContractPool")
    public void updateContractPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "poolName") String poolName,
            @WebParam(name = "poolDescription") String poolDescription,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a contract pool.
     * @param poolId The id of the contract pool to be deleted.
     * @param poolClass Contract pool class.
     * @param sessionId Session id token.
     * @throws ServerSideException If poolClass is not a valid subclass of GenericContract.
     */
    @WebMethod(operationName = "deleteContractPool")
    public void deleteContractPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the contract pool list.
     * @param sessionId Session id token.
     * @return The available contract pools.
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "getContractPools")
    public List<RemotePool> getContractPools(@WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates a contract.
     * @param poolId Contract pool id.
     * @param contractClass Contract class name.
     * @param attributeNames Attributes names.
     * @param attributeValues Attributes values.
     * @param sessionId Session id token.
     * @return The contract id.
     * @throws ServerSideException If any of the attributes or its type is invalid.
     *                             If attributeNames and attributeValues have different sizes.
     *                             If the class name could not be found.
     */
    @WebMethod(operationName = "createContract")
    public String createContract(
        @WebParam(name = "poolId") String poolId, 
        @WebParam(name = "contractClass") String contractClass, 
        @WebParam(name = "attributeNames") String[] attributeNames, 
        @WebParam(name = "attributeValues") String[] attributeValues, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the detailed information about a contract.
     * @param contractClass Contract class. Must be subclass of GenericContract.
     * @param contractId Contract id.
     * @param sessionId Session id token.
     * @return The contract properties.
     * @throws ServerSideException If the object couldn't be found. 
     *                             If the class couldn't be found.
     */
    @WebMethod(operationName = "getContract")
    public RemoteObject getContract(
            @WebParam(name = "contractClass") String contractClass,
            @WebParam(name = "contractId") String contractId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates one or many contract attributes.
     * @param contractClass Contract class. Must be subclass of GenericContract.
     * @param contractId contract id.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @param sessionId Session id token.
     * @throws ServerSideException If the object couldn't be found. 
     *                             If the class couldn't be found.
     */
    @WebMethod(operationName = "updateContract")
    public void updateContract(
            @WebParam(name = "contractClass") String contractClass,
            @WebParam(name = "contractId") String contractId,
            @WebParam(name = "attributes") List<StringPair> attributes,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a contract and delete its association with the related inventory objects. These objects will remain untouched.
     * @param contractClass The contract class.
     * @param contractId The contract id.
     * @param sessionId Session id token.
     * @throws ServerSideException If the object couldn't be found.
     *                             If the class could not be found.
     *                             If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     */
    @WebMethod(operationName = "deleteContract")
    public void deleteContract(
        @WebParam(name = "contractClass") String contractClass, 
        @WebParam(name = "contractId") String contractId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the entire list of contracts registered in the Contract Manager module.
     * @param page Page number of results to skip. Use -1 to retrieve all
     * @param limit Max number of results per page. Use -1 to retrieve all
     * @param sessionId The session token.
     * @return The entire list of contracts created in the Contract Manager module.
     * @throws ServerSideException If something unexpected happens.
     */
    @WebMethod(operationName = "getAllContracts")
    public List<RemoteObjectLight> getAllContracts(
            @WebParam(name = "page") int page,
            @WebParam(name = "limit") int limit,
            @WebParam(name = "sessionId") String sessionId)  throws ServerSideException;
    
    /**
     * Gets the contracts inside a contract pool.
     * @param poolId Contract pool id.
     * @param limit  the results limit. per page 0 to avoid the limit.
     * @param sessionId Session id token.
     * @return An array of contracts in a contract pool
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If the pool can't be found.
     */
    @WebMethod(operationName = "getContractsInPool")
    public List<RemoteObjectLight> getContractsInPool(
        @WebParam(name = "poolId") String poolId,
        @WebParam(name = "limit") int limit, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Relates a list of objects (instances of a subclass of InventoryObject) to an existing contract (most probably a support contract).
     * @param objectClass Object class.
     * @param objectId Object id.
     * @param contractClass contract class.
     * @param contractId contract id.
     * @param sessionId Session id token.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the objects can't be found
     *                             If any of the objects involved can't be associated
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "relateObjectsToContract")
    public void relateObjectsToContract (
            @WebParam(name = "objectClass")String[] objectClass,
            @WebParam(name = "objectId")String[] objectId,
            @WebParam(name = "contractClass")String contractClass,
            @WebParam(name = "contractId")String contractId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Relates an object to a contract.
     * @param contractClass Contract class.
     * @param contractId Contract id.
     * @param objectClass Object class.
     * @param objectId Object id.
     * @param sessionId Session id token.
     * @throws ServerSideException If the contract is not subclass of GenericContract.
     */
    @WebMethod(operationName = "relateObjectToContract")
    public void relateObjectToContract(
        @WebParam(name = "contractClass") String contractClass, 
        @WebParam(name = "contractId") String contractId, 
        @WebParam(name = "objectClass") String objectClass, 
        @WebParam(name = "objectId") String objectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
        
    /**
     * Releases an inventory object from a contract it was related to
     * @param objectClass Object class.
     * @param objectId Object id.
     * @param contractClass Contract class.
     * @param contractId Contract id.
     * @param sessionId Session id token.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If the class can not be found
     */
    @WebMethod(operationName = "releaseObjectFromContract")
    public void releaseObjectFromContract (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "contractClass")String contractClass,
            @WebParam(name = "contractId")String contractId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the resources (objects) related to a contract.
     * @param contractClass Contract class.
     * @param contractId Contract id.
     * @param sessionId Session id.
     * @return The contract resources list.
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If the contract is not subclass of GenericContract.
     */
    @WebMethod(operationName = "getContractResources")
    public List<RemoteObjectLight> getContractResources(
        @WebParam(name = "contractClass") String contractClass, 
        @WebParam(name = "contractId") String contractId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Associates a list of objects (instances of a subclass of InventoryObject) to an existing contract (most probably a support contract)
     * @param objectClass Object class
     * @param objectId Object id
     * @param contractClass contract class
     * @param contractId contract id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the objects can't be found
     *                             If any of the objects involved can't be associated
     *                             If any of the classes provided can not be found
     */
    @Deprecated
    @WebMethod(operationName = "associateObjectsToContract")
    public void associateObjectsToContract (
            @WebParam(name = "objectClass")String[] objectClass,
            @WebParam(name = "objectId")String[] objectId,
            @WebParam(name = "contractClass")String contractClass,
            @WebParam(name = "contractId")String contractId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="MPLS Module">
    /**
     * Creates an MPLS link
     * @param classNameEndpointA The class name of the endpoint A (some kind of port)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointB  The class name of the endpoint Z (some kind of port)
     * @param idEndpointB Id of endpoint Z
     * @param attributesToBeSet Attributes to be set, e.g. mplsLink's name
     * @param sessionId Session token
     * @return The id of the newly created transport link
     * @throws ServerSideException If the given linkType is no subclass of GenericLogicalConnection
     *                             If any of the requested objects can't be found
     *                             If any of the classes provided can not be found
     *                             If any of the objects involved can't be connected
     */
    @WebMethod(operationName = "createMPLSLink")
    public String createMPLSLink(@WebParam(name = "classNameEndpointA") String classNameEndpointA, 
            @WebParam(name = "idEndpointA") String idEndpointA, 
            @WebParam(name = "classNameEndpointB") String classNameEndpointB, 
            @WebParam(name = "idEndpointB") String idEndpointB, 
            @WebParam(name="parameters") List<StringPair> attributesToBeSet,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
                
    /**
     * The details of a given mpls link
     * @param connectionId the mpls link id
     * @param sessionId Session token
     * @return An array of two positions: the first is the A endpoint and the second is the B endpoint
     * @throws ServerSideException f the given id class name is not MPLS Link
     *                             If any of the requested objects can't be found
     *                             If any of the classes provided can not be found
     *                             If any of the objects involved can't be connected  
     */
    @WebMethod(operationName = "getMPLSLinkEndpoints")
    public RemoteMPLSConnectionDetails getMPLSLinkEndpoints(@WebParam(name = "connectionId") String connectionId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
        
    /**
     * Connect a given mpls links with a given ports for every side
     * @param sideAClassNames end point side A class names
     * @param sideAIds end point side A ids
     * @param linksIds mpls links ids
     * @param sideBClassNames end point side B class names
     * @param sideBIds end point side B ids
     * @param sessionId session token
     * @throws ServerSideException If the given ports are not subclass of GenericPort
     *                             If the given link ids are not of the class MPLSLink
     *                             If any of the requested objects can't be found
     *                             If any of the classes provided can not be found
     *                             If any of the objects involved can't be connected  
     */
    @WebMethod(operationName = "connectMplsLink")
    public void connectMplsLink(@WebParam(name = "sideAClassNames") String[] sideAClassNames,
            @WebParam(name = "sideAIds") String[] sideAIds,
            @WebParam(name = "linksIds") String[] linksIds,
            @WebParam(name = "sideBClassNames") String[] sideBClassNames,
            @WebParam(name = "sideBIds") String[] sideBIds,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * disconnect mpls link
     * @param connectionId mpls link id
     * @param sideToDisconnect which side will be disconnect 1 side A, 2 side B, 3 both sides 
     * @param sessionId session token
     * @throws ServerSideException If the given link id is not of the class MPLSLink
     *                             If any of the requested objects can't be found
     *                             If any of the classes provided can not be found
     *                             If any of the objects involved can't be connected 
     */
    @WebMethod(operationName = "disconnectMPLSLink")
    public void disconnectMPLSLink(@WebParam(name = "connectionId") String connectionId,
            @WebParam(name = "sideToDisconnect") int sideToDisconnect,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a MPLS link
     * @param linkId the id of the mpls link
     * @param forceDelete Ignore the existing relationships
     * @param sessionId Session token
     * @throws ServerSideException If the object can not be found
     *                             If either the object class or the attribute can not be found
     *                             If the class could not be found
     *                             If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     */
    @WebMethod(operationName = "deleteMPLSLink")
    public void deleteMPLSLink(@WebParam(name = "linkId") String linkId, 
            @WebParam(name = "forceDelete") boolean forceDelete, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Associates any GenericCommunicationsPort to existing BridgeDomainInterface
     * @param portId MPLSTunnel or BridgeDomain or FrameRelay or VRF id
     * @param portClassName if is a MPLSTunnel or BridgeDomain or FrameRelay or VRF
     * @param interfaceClassName network element class name
     * @param interfaceId network element id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "relatePortToInterface")
    public void relatePortToInterface (
            @WebParam(name = "portId")String portId,
            @WebParam(name = "portClassName")String portClassName,
            @WebParam(name = "interfaceClassName")String interfaceClassName,
            @WebParam(name = "interfaceId")String interfaceId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Release the association between a network element and a MPLSTunnel or BridgeDomain or 
     * FrameRelay or VRF
     * @param portId MPLSTunnel or BridgeDomain or FrameRelay or VRF id
     * @param interfaceClassName network element class name
     * @param interfaceId network element id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    
    //String networkElementClass, long networkElementId ,long id
    @WebMethod(operationName = "releasePortFromInterface")
    public void releasePortFromInterface (
            @WebParam(name = "interfaceClassName")String interfaceClassName,
            @WebParam(name = "interfaceId")String interfaceId,
            @WebParam(name = "portId")String portId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
        // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Projects Module">
    /**
     * Retrieves the projects pool list.
     * @param sessionId Session id token.
     * @return The available project pools.
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "getProjectPools")
    public List<RemotePool> getProjectPools(@WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates a Project.
     * @param projectPoolId Project pool id.
     * @param projectClass Project class name.
     * @param attributeNames Attributes names.
     * @param attributeValues Attributes values.
     * @param sessionId Session id token.
     * @return The project id.
     * @throws ServerSideException If any of the attributes or its type is invalid.
     *                             If attributeNames and attributeValues have different sizes.
     *                             If the class name could not be found.
     */
    @WebMethod(operationName = "createProject")
    public String createProject(
        @WebParam(name = "projectPoolId") String projectPoolId, 
        @WebParam(name = "projectClass") String projectClass, 
        @WebParam(name = "attributeNames") String[] attributeNames, 
        @WebParam(name = "attributeValues") String[] attributeValues, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the detailed information about a project
     * @param projectClass Project class. Must be subclass of GenericProject.
     * @param projectId Project id.
     * @param sessionId Session id token.
     * @return The project properties.
     * @throws ServerSideException If the object couldn't be found. 
     *                             If the class couldn't be found.
     */
    @WebMethod(operationName = "getProject")
    public RemoteObject getProject(
            @WebParam(name = "projectClass") String projectClass,
            @WebParam(name = "projectId") String projectId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates one or many project attributes.
     * @param projectClass Project class. Must be subclass of GenericProject.
     * @param projectId Project id.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @param sessionId Session id token.
     * @throws ServerSideException If the object couldn't be found. 
     *                             If the class couldn't be found.
     */
    @WebMethod(operationName = "updateProject")
    public void updateProject(
            @WebParam(name = "projectClass") String projectClass,
            @WebParam(name = "projectId") String projectId,
            @WebParam(name = "attributes") List<StringPair> attributes,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a project and delete its association with the related inventory objects.These objects will remain untouched.
     * @param projectClass The project class.
     * @param projectId The project id.
     * @param sessionId Session id token.
     * @throws ServerSideException If the object couldn't be found.
     *                             If the class could not be found.
     *                             If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     */
    @WebMethod(operationName = "deleteProject")
    public void deleteProject(
        @WebParam(name = "projectClass") String projectClass, 
        @WebParam(name = "projectId") String projectId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates an Activity inside a project.
     * @param projectId The project id.
     * @param projectClass The project class.
     * @param activityClass The activity class. Must be subclass of GenericActivity.
     * @param attributeNames Attributes names.
     * @param attributeValues Attributes values.
     * @param sessionId Session id token.
     * @return The Activity id.
     * @throws ServerSideException If the object's class can't be found.
     *                             If the parent id is not found.
     *                             If any of the attribute values has an invalid value or format.
     *                             If the update can't be performed due to a format issue.
     *                             If attributeNames and attributeValues have different sizes.
     */
    @WebMethod(operationName = "createActivity")
    public String createActivity(
        @WebParam(name ="projectId") String projectId, 
        @WebParam(name ="projectClass") String projectClass, 
        @WebParam(name ="activityClass") String activityClass, 
        @WebParam(name ="attributeNames") String[] attributeNames, 
        @WebParam(name ="attributeValues") String[] attributeValues, 
        @WebParam(name ="sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the detailed information about an activity.
     * @param activityClass Activity class. Must be subclass of GenericActivity.
     * @param activityId Activity id.
     * @param sessionId Session id token.
     * @return The activity properties.
     * @throws ServerSideException If the object couldn't be found. 
     *                             If the class couldn't be found.
     */
    @WebMethod(operationName = "getActivity")
    public RemoteObject getActivity(
            @WebParam(name = "activityClass") String activityClass,
            @WebParam(name = "activityId") String activityId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates one or many activity attributes.
     * @param activityClass Activity class. Must be subclass of GenericActivity.
     * @param activityId Activity id.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @param sessionId Session id token.
     * @throws ServerSideException If the object couldn't be found. 
     *                             If the class couldn't be found. 
     */
    @WebMethod(operationName = "updateActivity")
    public void updateActivity(
            @WebParam(name = "activityClass") String activityClass,
            @WebParam(name = "activityId") String activityId,
            @WebParam(name = "attributes") List<StringPair> attributes,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes an activity and delete its association with the related inventory objects. These objects will remain untouched.
     * @param activityClass The activity class. Must be subclass of GenericActivity.
     * @param activityId The activity id.
     * @param releaseRelationships Release of existing relationships.
     * @param sessionId Session id token.
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If the object couldn't be found.
     *                             If the class could not be found.
     *                             If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     */
    @WebMethod(operationName = "deleteActivity")
    public void deleteActivity(
        @WebParam(name ="activityClass") String activityClass, 
        @WebParam(name ="activityId") String activityId, 
        @WebParam(name ="releaseReltationships") boolean releaseRelationships, 
        @WebParam(name ="sessionId") String sessionId) throws ServerSideException;

    /**
     * Gets the projects inside a project pool.
     * @param poolId Project pool id.
     * @param limit  the results limit. per page 0 to avoid the limit.
     * @param sessionId Session id token
     * @return An array of projects in a project pool
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If the pool can't be found.
     */
    @WebMethod(operationName = "getProjectsInPool")
    public List<RemoteObjectLight> getProjectsInPool(
        @WebParam(name = "poolId") String poolId,
        @WebParam(name = "limit") int limit, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the entire list of projects registered in the Project Manager module.
     * @param page Page number of results to skip. Use -1 to retrieve all
     * @param limit Max number of results per page. Use -1 to retrieve all
     * @param sessionId The session token.
     * @return The entire list of projects created in the Project Manager module.
     * @throws ServerSideException If something unexpected happens.
     */
    @WebMethod(operationName = "getAllProjects")
    public List<RemoteObjectLight> getAllProjects(
            @WebParam(name = "page") int page,
            @WebParam(name = "limit") int limit,
            @WebParam(name = "sessionId") String sessionId)  throws ServerSideException;
        
    /**
     * Retrieves the entire list of projects registered in the Project Manager module.
     * @param filters Attributes to filter for
     * @param page Page number of results to skip. Use -1 to retrieve all
     * @param limit Max number of results per page. Use -1 to retrieve all
     * @param sessionId The session token.
     * @return The entire list of projects created in the Project Manager module.
     * @throws ServerSideException If something unexpected happens.
     */
    @WebMethod(operationName = "getProjectsWithFilter")
    public List<RemoteObjectLight> getProjectsWithFilter(
            @WebParam(name = "filters") List<StringPair> filters,
            @WebParam(name = "page") int page,
            @WebParam(name = "limit") int limit,
            @WebParam(name = "sessionId") String sessionId)  throws ServerSideException;
    
    /**
     * Gets the resources (objects) related to a project.
     * @param projectClass Project class.
     * @param projectId Project id.
     * @param sessionId Session id.
     * @return The project resources list.
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If the project is not subclass of GenericProject.
     */
    @WebMethod(operationName = "getProjectResources")
    public List<RemoteObjectLight> getProjectResources(
        @WebParam(name = "projectClass") String projectClass, 
        @WebParam(name = "projectId") String projectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the activities inside a project.
     * @param projectClass Project class.
     * @param projectId Project Id.
     * @param sessionId Session Id.
     * @return The activities list.
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If the project is not subclass of GenericProject.
     *                             If the project class is not found.
     *                             If the project can't be found.
     */
    @WebMethod(operationName = "getProjectActivities")
    public List<RemoteObjectLight> getProjectActivities(
        @WebParam(name = "projectClass") String projectClass, 
        @WebParam(name = "projectId") String projectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
        
    /**
     * Relates a set of objects to a project
     * @param projectClass Project class.
     * @param projectId Project id.
     * @param objectClass Object class.
     * @param objectId Object id.
     * @param sessionId Session id token.
     * @throws ServerSideException Generic If the project is not subclass of GenericProject.
     *                                     If array sizes of objectClass and objectId are not the same.
     */
    @WebMethod(operationName = "relateObjectsToProject")
    public void relateObjectsToProject(
        @WebParam(name = "projectClass") String projectClass, 
        @WebParam(name = "projectId") String projectId, 
        @WebParam(name = "objectClass") String[] objectClass, 
        @WebParam(name = "objectId") String[] objectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Relates an object to a project.
     * @param projectClass Project class.
     * @param projectId Project id.
     * @param objectClass Object class.
     * @param objectId Object id.
     * @param sessionId Session id token.
     * @throws ServerSideException If the project is not subclass of GenericProject.
     */
    @WebMethod(operationName = "relateObjectToProject")
    public void relateObjectToProject(
        @WebParam(name = "projectClass") String projectClass, 
        @WebParam(name = "projectId") String projectId, 
        @WebParam(name = "objectClass") String objectClass, 
        @WebParam(name = "objectId") String objectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
        
    /**
     * Releases an object from project.
     * @param objectClass Object class.
     * @param objectId Object id.
     * @param projectClass Project class.
     * @param projectId Project id.
     * @param sessionId Session id token.
     * @throws ServerSideException If the project is not subclass of GenericProject.
     */    
    @WebMethod(operationName = "releaseObjectFromProject")
    public void releaseObjectFromProject(
        @WebParam(name = "objectClass") String objectClass, 
        @WebParam(name = "objectId") String objectId, 
        @WebParam(name = "projectClass") String projectClass, 
        @WebParam(name = "projectId") String projectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the projects related to an object.
     * @param objectClass Object class.
     * @param objectId Object id.
     * @param sessionId The session id token.
     * @return The list projects related to an object.
     * @throws ServerSideException If the object can't be found.
     *                             If the object class provided can't be found.
     */
    @WebMethod(operationName = "getProjectsRelatedToObject")
    public List<RemoteObjectLight> getProjectsRelatedToObject(
        @WebParam(name = "objectClass") String objectClass, 
        @WebParam(name = "ObjectId") String objectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates a project pool.
     * @param poolName Project pool name. Must be subclass of GenericProject.
     * @param poolDescription Project pool description.
     * @param poolClass Project pool class.
     * @param sessionId Session id token.
     * @return The id of the newly created project pool.
     * @throws ServerSideException If poolClass is not a valid subclass of GenericProject. 
     */
    @WebMethod(operationName = "createProjectPool")
    public String createProjectPool(
        @WebParam(name = "poolName") String poolName, 
        @WebParam(name = "poolDescription") String poolDescription, 
        @WebParam(name = "poolClass") String poolClass, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the project pools properties.
     * @param poolId Project pool id.
     * @param poolClass Project pool class. Must be subclass of GenericProject.
     * @param sessionId Session id token.
     * @return The project pool properties.
     * @throws ServerSideException If poolClass is not a valid subclass of GenericProject. 
     */
    @WebMethod(operationName = "getProjectPool")
    public RemotePool getProjectPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates the attributes of a project pool.
     * @param poolId The id of the project pool to be updated.
     * @param poolClass Project pool class.
     * @param poolName Attribute value for pool name.
     * @param poolDescription Attribute value for pool description.
     * @param sessionId Session id token.
     * @throws ServerSideException If poolClass is not a valid subclass of GenericProject. 
     */
    @WebMethod(operationName = "updateProjectPool")
    public void updateProjectPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "poolName") String poolName,
            @WebParam(name = "poolDescription") String poolDescription,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a project pool.
     * @param poolId The id of the project pool to be deleted.
     * @param poolClass Project pool class.
     * @param sessionId Session id token.
     * @throws ServerSideException If poolClass is not a valid subclass of GenericProject.
     */
    @WebMethod(operationName = "deleteProjectPool")
    public void deleteProjectPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates a copy of a project.
     * @param poolId The id of the parent pool.
     * @param projectClass The class of the project.
     * @param projectId The id of the project.
     * @param sessionId Session id token.
     * @return The newly created project id.
     * @throws ServerSideException If the pool node can not be found.
     *                             If the project can not be copy to the selected pool.
     *                             If the project can not be found.
     *                             If the project class name can no be found.
     */
    /*
     * This method will be enabled in a later version
    @WebMethod(operationName = "copyProjectToPool")
    public String copyProjectToPool(
            @WebParam(name = "poolId")String poolId, 
            @WebParam(name = "projectClass")String projectClass, 
            @WebParam(name = "projectId")String projectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;*/
    
    /**
     * Moves a project from a pool to another pool.
     * @param poolId The id of the parent pool.
     * @param projectClass The class of the project.
     * @param projectId The id of the project.
     * @param sessionId Session id token.
     * @throws ServerSideException If the pool node can not be found.
     *                             If the project can not be copy to the selected pool.
     *                             If the project can not be found.
     *                             If the project class name can no be found.
     */
    /*
     * This method will be enabled in a later version
    @WebMethod(operationName = "moveProjectToPool")
    public void moveProjectToPool(
            @WebParam(name = "poolId")String poolId, 
            @WebParam(name = "projectClass")String projectClass, 
            @WebParam(name = "projectId")String projectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;*/
    // </editor-fold>
    
        //<editor-fold desc="Fault Management Integration" defaultstate="collapsed">
    /**
     * Retrieves the services affected when a given network resource is alarmed (or down). 
     * The services associated directly to the given resource are returned first, but depending on the resource type, the analysis might be more extensive. 
     * If the resource is a link (logical or physical), the services associated to the endpoint ports are also returned. 
     * If the the resource is a container (physical or logical), the services associated to the contained links are also returned. 
     * If the resource is a network equipment, the services associated directly to the ports contained and their connections are also returned.
     * @param resourceType Use 1 for hardware and 2 for logical links
     * @param resourceDefinition A semi-colon (;) separated string. The first segment (mandatory) is the name of the affected element, 
     * the second is the number of the slot (optional) and the third is the port (optional). Note that to address a logical connection, the 
     * resource definition will contain only the name of such connection.
     * @param sessionId Session token
     * @return A compact summary with the full information about the device/interface provided, the related services and their customers
     * @throws ServerSideException If the resource could not be found or if the resource definition/resource type is not valid
     */
    @WebMethod(operationName = "getAffectedServices")
    public RemoteAssetLevelCorrelatedInformation getAffectedServices(@WebParam(name = "resourceType")int resourceType,
            @WebParam(name = "resourceDefinition")String resourceDefinition, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
        //</editor-fold>
    
        //<editor-fold desc="Inventory Synchronization" defaultstate="collapsed">
        /**
         * Get the set of sync providers defined in the configuration variables pool called -Sync Providers-
         * @param sessionId Session token
         * @return The set of sync providers defined in the configuration variables pool called -Sync Providers-
         * @throws ServerSideException If the pool could not be found
         * If the value of the variable could not be successfully translated into a java type variable
         * If no configuration variable with that name could be found
         */
        @WebMethod(operationName = "getSynchronizationProviders")
        public List<RemoteSynchronizationProvider> getSynchronizationProviders(
            @WebParam(name="sessionId") String sessionId)throws ServerSideException;
    
        /**
         * Creates a Synchronization Data Source Configuration. A Sync data source configuration is a set of parameters 
         * used to connect to a sync data source (usually IPs, paths, etc)
         * @param objectId Id of the object that the configuration is attached to         
         * @param syncGroupId Id of the sync group this configuration is related to         
         * @param name The name of the new sync data source configuration
         * @param parameters The list of parameters to be stored as pairs name/value. 
         * Note that the Sync provider provides metadata definition to check if the number 
         * and format of the parameters correct, so it can be checked at server side
         * @param sessionId Session token
         * @return The id of the newly created sync configuration.
         * @throws ServerSideException If the sync group could not be found or if 
         * the any of the parameters does not comply with the expected format
         */
        @WebMethod(operationName = "createSynchronizationDataSourceConfig")
        public long createSynchronizationDataSourceConfig(
                @WebParam(name="objectId")String objectId, 
                @WebParam(name="syncGroupId")long syncGroupId,
                @WebParam(name="name")String name, 
                @WebParam(name="parameters")List<StringPair> parameters, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
    
        /**
         * Creates a Synchronization Group. A Sync Group is a set of Synchronization Configurations that will be processed by the same
         * Synchronization Provider. Take into account that the schedule for the SG to be executed is not configured here, but in Task Manager's task
         * @param name The name of the new sync group
         * @param sessionId Session token
         * @return The id of the newly created sync group
         * @throws ServerSideException If the name or the sync provider are invalid 
         */
        @WebMethod(operationName = "createSynchronizationGroup")
        public long createSynchronizationGroup(@WebParam(name="name")String name, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
         /**
         * Creates a Synchronization Group. A Sync Group is a set of Synchronization Configurations that will be processed by the same
         * Synchronization Provider. Take into account that the schedule for the SG to be executed is not configured here, but in Task Manager's task
         * @param syncGroupId The name of the new sync group
         * @param syncGroupProperties The synchronization group properties
         * @param sessionId Session token
         * @throws ServerSideException If the name or the sync provider are invalid 
         */
        @WebMethod(operationName = "updateSynchronizationGroup")
        public void updateSynchronizationGroup(@WebParam(name="syncGroupId") long syncGroupId,
                @WebParam(name="syncGroupProperties")List<StringPair> syncGroupProperties, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Gets a given sync group
         * @param syncGroupId The sync group id
         * @param sessionId Session token
         * @return The requested sync group
         * @throws ServerSideException If the sync group could not be found
         */
        @WebMethod(operationName = "getSynchronizationGroup")
        public RemoteSynchronizationGroup getSynchronizationGroup(
                @WebParam(name="syncGroupId")long syncGroupId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Gets the available sync groups
         * @param sessionId Session token
         * @return The list of available sync groups
         * @throws ServerSideException If something unexpected goes wrong
         */
        @WebMethod(operationName = "getSynchronizationGroups")
        public List<RemoteSynchronizationGroup> getSynchronizationGroups(
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Gets the synchronization data source configurations for an object
         * @param objectId the objectId
         * @param sessionId Session token
         * @return The list of available sync groups
         * @throws ServerSideException If something unexpected goes wrong
         */
        @WebMethod(operationName = "getSyncDataSourceConfiguration")
        public RemoteSynchronizationConfiguration getSyncDataSourceConfiguration(
                @WebParam(name="objectId")String objectId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Gets the synchronization data source configurations for a sync group
         * @param syncGroupId the syncGroupId
         * @param sessionId Session token
         * @return The list of available sync groups
         * @throws ServerSideException If something unexpected goes wrong
         */
        @WebMethod(operationName = "getSyncDataSourceConfigurations")
        public List<RemoteSynchronizationConfiguration> getSyncDataSourceConfigurations(
                @WebParam(name="syncGroupId")long syncGroupId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Updates a sync data source configuration
         * @param syncDataSourceConfigId the sync source configuration Id
         * @param parameters the updated parameters
         * @param sessionId session token 
         * @throws ServerSideException If the sync data source could not be found or if 
         * the any of the parameters does not comply with the expected format
         */
        @WebMethod(operationName = "updateSyncDataSourceConfiguration")
        public void updateSyncDataSourceConfiguration(
                @WebParam(name="syncDataSourceConfigId")long syncDataSourceConfigId, 
                @WebParam(name="parameters")List<StringPair> parameters, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Deletes a synchronization group and all the sync configurations associated to it
         * @param syncGroupId The id of the group
         * @param sessionId Session token
         * @throws ServerSideException If the group could not be found
         */
        @WebMethod(operationName = "deleteSynchronizationGroup")
        public void deleteSynchronizationGroup(@WebParam(name="syncGroupId")long syncGroupId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Deletes a sync data source configuration.
         * @param syncDataSourceConfigId The id of the configuration
         * @param sessionId Session token
         * @throws ServerSideException If the config could not be found
         */
        @WebMethod(operationName = "deleteSynchronizationDataSourceConfig")
        public void deleteSynchronizationDataSourceConfig(@WebParam(name="syncDataSourceConfigId")long syncDataSourceConfigId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Creates "copy" a relation between a set of sync data source configurations and a given sync group
         * @param syncGroupId The Sync Group Id target
         * @param syncDataSourceConfigurationIds Set of sync data source configuration ids
         * @param sessionId Session Token
         * @throws ServerSideException If the sync group cannot be found, or some sync data source configuration cannot be found
         *                             If the sync group is malformed, or some sync data source configuration is malformed
         */
        @WebMethod(operationName = "copySyncDataSourceConfiguration")
        public void copySyncDataSourceConfiguration(
            @WebParam(name="syncGroupId") long syncGroupId, 
            @WebParam(name="syncDataSourceConfigurationId") long[] syncDataSourceConfigurationIds, 
            @WebParam(name="sessionId") String sessionId) throws ServerSideException;
        
        /**
         * Release a set of sync data source configuration from a given sync group
         * @param syncGroupId The Sync Group Id target
         * @param syncDataSourceConfigurationIds Set of sync data source configuration ids
         * @param sessionId Session Token
         * @throws ServerSideException If the sync group cannot be found, or some sync data source configuration cannot be found
         *                             If the sync group is malformed, or some sync data source configuration is malformed
         */
        @WebMethod(operationName = "releaseSyncDataSourceConfigFromSyncGroup")
        public void releaseSyncDataSourceConfigFromSyncGroup(
            @WebParam(name="syncGroupId") long syncGroupId, 
            @WebParam(name="syncDataSourceConfigurationId") long[] syncDataSourceConfigurationIds, 
            @WebParam(name="sessionId") String sessionId) throws ServerSideException;

        /**
         * Moves a sync data source configuration from a sync group to another sync group
         * @param newSyncGroupId The Sync Group Id target to release
         * @param syncDataSourceConfigurationIds Set of sync data source configuration ids
         * @param sessionId Session token
         * @throws ServerSideException If the sync group cannot be found, or some sync data source configuration cannot be found
         *                             If the sync group is malformed, or some sync data source configuration is malformed
         */
        @WebMethod(operationName = "moveSyncDataSourceConfiguration")
        public void moveSyncDataSourceConfiguration(
            @WebParam(name="newSyncGroupId") long newSyncGroupId, 
            @WebParam(name="syncDataSourceConfiguration") long[] syncDataSourceConfigurationIds, 
            @WebParam(name="sessionId") String sessionId) throws ServerSideException;
        //</editor-fold>
        
        //<editor-fold desc="Process Manager" defaultstate="collapsed">
        /**
         * Creates a process definition. A process definition is the metadata that defines the steps and constraints 
         * of a given project
         * @param name The name of the new process definition
         * @param description The description of the new process definition
         * @param version The version of the new process definition. This is a three numbers, dot separated string (e.g. 2.4.1)
         * @param enabled If the project is enabled to create instances from it
         * @param structure The structure of the process definition. It's an XML document that represents a BPMN process definition
         * @param sessionId The session token
         * @return The id of the newly created process definition
         * @throws ServerSideException If the process structure defines a malformed process or if the version is invalid
         */
        @WebMethod(operationName = "createProcessDefinition")
        public String createProcessDefinition(@WebParam(name = "name")String name, 
                @WebParam(name="description")String description, @WebParam(name="version")String version, 
                @WebParam(name="enabled")boolean enabled, @WebParam(name="structure")byte[] structure, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Updates a process definition, either its standard properties or its structure
         * @param processDefinitionId The process definition id
         * @param properties A key value dictionary with the standard properties to be updated. These properties are: name, description, version and enabled (use 'true' or 'false' for the latter)
         * @param structure A byte array withe XML process definition body
         * @param sessionId The session token
         * @throws ServerSideException If the structure is invalid, or the process definition could not be found or one of the properties is malformed or have an unexpected name
         */
        @WebMethod(operationName = "updateProcessDefinition")
        public void updateProcessDefinition(@WebParam(name="processDefinitionId")String processDefinitionId, 
                @WebParam(name="properties")List<StringPair> properties, @WebParam(name="structure")byte[] structure, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Deletes a process definition
         * @param processDefinitionId The process definition to be deleted
         * @param sessionId The session token
         * @throws ServerSideException If the process definition could not be found or if there are process instances related to the process definition
         */
        @WebMethod(operationName = "deleteProcessDefinition")
        public void deleteProcessDefinition(@WebParam(name="processDefinitionId")String processDefinitionId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Retrieves a process definition
         * @param processDefinitionId The id of the process
         * @param sessionId The session token
         * @return The process definition. It contains an XML document to be parsed by the consumer
         * @throws ServerSideException If the process could not be found or if it's malformed
         */
        @WebMethod(operationName = "getProcessDefinition")
        public RemoteProcessDefinition getProcessDefinition(@WebParam(name="processDefinitionId")String processDefinitionId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Creates an instance of a process, that is, starts one
         * @param processDefinitionId The id of the process to be started
         * @param processInstanceName The name of the new process
         * @param processInstanceDescription The description of the new process
         * @param sessionId The session token
         * @return The id of the newly created process instance
         * @throws ServerSideException If the process definition could not be found or if it's disabled
         */
        @WebMethod(operationName = "createProcessInstance")
        public String createProcessInstance(@WebParam(name="processDefinitionId")String processDefinitionId, 
                @WebParam(name="processInstancename")String processInstanceName, 
                @WebParam(name="processInstanceDescription")String processInstanceDescription, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Requests for the next activity to be executed in a process instance.
         * @param processInstanceId The running process to get the next activity from
         * @param sessionId The session id
         * @return The activity definition
         * @throws ServerSideException If the process instance could not be found, or if the process already ended
         */
        @WebMethod(operationName = "getNextActivityForProcessInstance")
        public RemoteActivityDefinition getNextActivityForProcessInstance(@WebParam(name="processInstanceId")String processInstanceId,
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Saves the artifact generated once an activity has been completed (for example, the user filled in a form). 
         * @param processInstanceId The process instance the activity belongs to
         * @param activityDefinitionId The activity id
         * @param artifact The artifact to be saved
         * @param sessionId The session token
         * @throws ServerSideException If the process could not be found, or if the activity had been already executed, 
         * or if the activity definition could not be found, or of there's a mismatch in the artifact versions or if the user is not an authorized actor to carry on with the activity
         */
        @WebMethod(operationName = "commitActivity")
        public void commitActivity(@WebParam(name="processInstanceId")String processInstanceId, 
                @WebParam(name="activityDefinitionId")String activityDefinitionId, 
                @WebParam(name="artifact")RemoteArtifact artifact, @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Given an activity definition, returns the artifact definition associated to it
         * @param processDefinitionId The id of the process the activity is related to
         * @param activityDefinitionId The id of the activity
         * @param sessionId The session token
         * @return An object containing the artifact definition
         * @throws ServerSideException If the process or the activity could not be found
         */
        @WebMethod(operationName = "getArtifactDefinitionForActivity")
        public RemoteArtifactDefinition getArtifactDefinitionForActivity(@WebParam(name="processDefinitionId")String processDefinitionId, 
                @WebParam(name="activityDefinitionId")String activityDefinitionId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Gets the artifact associated to an activity (for example, a form that was already filled in by a user in a previous, already committed activity)
         * @param processInstanceId The id of the process instance. This process may have been ended already.
         * @param activityId The id of the activity the artifact belongs to
         * @param sessionId The session token
         * @return The artifact corresponding to the given activity
         * @throws ServerSideException If the process instance or activity couldn't be found.
         */
        @WebMethod(operationName = "getArtifactForActivity")
        public RemoteArtifact getArtifactForActivity(@WebParam(name="processinstanceId")String processInstanceId, 
                @WebParam(name="activityId")String activityId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        //</editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc="Warehouse Module">
    /**
     * Gets the warehouse module root pools.
     * @param sessionId Session token.
     * @return the warehouse module root pools.
     * @throws ServerSideException If the class GenericWarehouse does not exist.
     */
    @WebMethod(operationName = "getWarehouseRootPools")
    public List<RemotePool> getWarehouseRootPools(
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Get the warehouses list inside the pool
     * @param poolId Root pool id
     * @param limit Result limit. -1 To return all
     * @param sessionId Session token.
     * @return warehouses list
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             If the pool can't be found
     */
    @WebMethod(operationName = "getWarehousesInPool")
    public List<RemoteObjectLight> getWarehousesInPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "limit") int limit,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Get the spare pool list inside the warehouse
     * @param objectClassName Object class name
     * @param objectId Object id
     * @param sessionId Session token
     * @return Spare pool list
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             If the warehouse can't be found
     *                             If any of the classes provided cannot be found
     */
    @WebMethod(operationName = "getPoolsInWarehouse")
    public List<RemotePool> getPoolsInWarehouse(
            @WebParam(name = "objectClassName") String objectClassName,
            @WebParam(name = "objectId") String objectId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Get the objects list inside the spare pool
     * @param poolId Spare pool id
     * @param limit Result limit. -1 To return all
     * @param sessionId Session token
     * @return Object list
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             If the spare pool can't be found
     */
    @WebMethod(operationName = "getObjectsInSparePool")
    public List<RemoteObjectLight> getObjectsInSparePool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "limit") int limit,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Creates a warehouse
     * @param poolId Warehouse pool id
     * @param poolClass This warehouse is going to be instance of
     * @param attributeNames Attributes to be set in the new service. Null or
     * empty array for none
     * @param attributeValues Attributes to be set in the new service (values).
     * Null for none. The size of this array must match attributeNames size
     * @param templateId Template to be used. Use -1 to not use any template
     * @param sessionId Session id token
     * @return The id of the newly created warehouse
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             If the warehouse pool cannot be found
     *                             If the provided class cannot be found
     */
    @WebMethod(operationName = "createWarehouse")
    public String createWarehouse(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "attributeNames") String[] attributeNames,
            @WebParam(name = "attributeValues") String[] attributeValues,
            @WebParam(name = "templateId") String templateId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a warehouse
     * @param warehouseClass Warehouse class name
     * @param warehouseId Warehouse id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             If the warehouse cannot be found
     */
    @WebMethod(operationName = "deleteWarehouse")
    public void deleteWarehouse(
            @WebParam(name = "warehouseClass") String warehouseClass,
            @WebParam(name = "warehouseId") String warehouseId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates a spare pool
     * @param warehouseClass The parent(warehouse) class name
     * @param warehouseId The parent(warehouse) id
     * @param poolName Service Pool name
     * @param poolDescription Service Pool description
     * @param instancesOfClass What kind of objects can this pool contain?
     * @param sessionId Session id token
     * @return The id of the new spare pool
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             If the warehouse cannot be found
     *                             If the provided class cannot be found
     */
    @WebMethod(operationName = "createPoolInWarehouse")
    public String createPoolInWarehouse(
            @WebParam(name = "warehouseClass") String warehouseClass,
            @WebParam(name = "warehouseId") String warehouseId,
            @WebParam(name = "poolName") String poolName,
            @WebParam(name = "poolDescription") String poolDescription,
            @WebParam(name = "instancesOfClass") String instancesOfClass,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a spare pool
     * @param poolId Spare pool id
     * @param poolClass Spare pool class
     * @param sessionId Session id token
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             If the provided class cannot be found
     *                             If the spare pool cannot be found
     */
    @WebMethod(operationName = "deleteSparePool")
    public void deleteSparePool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates a spare part
     * @param poolId Spare pool id
     * @param poolClass This spare part is going to be instance of
     * @param attributeNames Attributes to be set in the new service. Null or
     * empty array for none
     * @param attributeValues Attributes to be set in the new service (values).
     * Null for none. The size of this array must match attributeNames size
     * @param templateId Template to be used. Use -1 to not use any template
     * @param sessionId Session id token
     * @return The id of the newly created spare part
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             If the spare pool cannot be found
     *                             If the provided class cannot be found
     */
    @WebMethod(operationName = "createSparePart")
    public String createSparePart(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "attributeNames") String[] attributeNames,
            @WebParam(name = "attributeValues") String[] attributeValues,
            @WebParam(name = "templateId") String templateId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Associates an object (a resource) to an existing warehouse or virtual warehouse
     * @param objectClass Object class
     * @param objectId Object id
     * @param warehouseClass Warehouse class
     * @param warehouseId Warehouse id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             If any of the objects can't be found 
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "associatePhysicalNodeToWarehouse")
    public void associatePhysicalNodeToWarehouse(
            @WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "objectId") String objectId,
            @WebParam(name = "warehouseClass") String warehouseClass,
            @WebParam(name = "warehouseId") String warehouseId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Associates a list of objects (resources) to an existing warehouse or virtual warehouse
     * @param objectClass Object class
     * @param objectId Object id
     * @param warehouseClass Warehouse class
     * @param warehouseId Warehouse id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             If any of the objects can't be found 
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "associatesPhysicalNodeToWarehouse")
    public void associatesPhysicalNodeToWarehouse(
            @WebParam(name = "objectClass") String[] objectClass,
            @WebParam(name = "objectId") String[] objectId,
            @WebParam(name = "warehouseClass") String warehouseClass,
            @WebParam(name = "warehouseId") String warehouseId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Releases an object from a warehouse or virtual warehouse that is using it
     * @param warehouseClass Warehouse class
     * @param warehouseId Warehouse id
     * @param targetId target object id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             If the object can not be found
     *                             If the class can not be found 
     *                             If the object activity log could no be found
     */
    @WebMethod(operationName = "releasePhysicalNodeFromWarehouse")
    public void releasePhysicalNodeFromWarehouse(
            @WebParam(name = "warehouseClass") String warehouseClass,
            @WebParam(name = "warehouseId") String warehouseId,
            @WebParam(name = "targetId") String targetId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Moves objects from their current parent to a warehouse pool target object.
     * @param targetClass New parent object id
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @param sessionId Session token
     * @throws ServerSideException If the object's or new parent's class can't be found 
     *                             If the object or its new parent can't be found 
     *                             If the update can't be performed due to a business rule
     */
    @WebMethod(operationName = "moveObjectsToWarehousePool")
    public void moveObjectsToWarehousePool(@WebParam(name = "targetClass") String targetClass,
            @WebParam(name = "targetOid") String targetOid,
            @WebParam(name = "objectsClasses") String[] objectClasses,
            @WebParam(name = "objectsOids") String[] objectOids,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Moves objects from their current parent to a target object.
     * @param targetClass New parent object id
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @param sessionId Session token
     * @throws ServerSideException If the object's or new parent's class can't be found 
     *                             If the object or its new parent can't be found 
     *                             If the update can't be performed due to a business rule
     */
    @WebMethod(operationName = "moveObjectsToWarehouse")
    public void moveObjectsToWarehouse(@WebParam(name = "targetClass") String targetClass,
            @WebParam(name = "targetOid") String targetOid,
            @WebParam(name = "objectsClasses") String[] objectClasses,
            @WebParam(name = "objectsOids") String[] objectOids,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    // </editor-fold>
        
        //<editor-fold desc="Routing Explorer Module" defaultstate="collapsed">
        /**
         * Creates/updates a map of the BGPLinks
         * @param mappedBgpLinksIds BGPLinks that are already mapped, it can be 
         * an empty array if nothing is mapped of if the whole map should be generated again.
         * @param sessionId The session token
         * @return A list of BGPlinks with their endPoints(ports) and the CommunicationsElements parents of the ports
         * @throws ServerSideException If the structure is invalid, or the process definition could not be found or one of the properties is malformed or have an unexpected name
         */
        @WebMethod(operationName = "getBGPMap")
        public List<RemoteLogicalConnectionDetails> getBGPMap(@WebParam(name="mappedBgpLinksIds")List<String> mappedBgpLinksIds, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException;
        // </editor-fold>
        
        //<editor-fold desc="Outside plant">
        /**
         * Creates an Outside Plant View
         * @param name The name of the new view
         * @param description The description of the new view
         * @param content The XML document with the contents of the view. The format of the XML document is consistent with the other views
         * @param sessionId Session token
         * @throws ServerSideException If the name is empty. 
         * @return The id of the newly created view
         */
        @WebMethod(operationName = "createOSPView")
        public long createOSPView(@WebParam(name = "name")String name, @WebParam(name = "description")String description, 
                @WebParam(name = "content")byte[] content, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Retrieves the specific information about an existing OSP view
         * @param viewId The id of the view
         * @param sessionId The session token
         * @return An object containing the view details and structure
         * @throws ServerSideException If the view could not be found
         */
        @WebMethod(operationName = "getOSPView")
        public RemoteViewObject getOSPView(@WebParam(name = "viewId")long viewId, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Retrieves the existing OSP views
         * @param sessionId The session token
         * @return The list of existing OSP views
         * @throws ServerSideException If an unexpected error appeared
         */
        @WebMethod(operationName = "getOSPViews")
        public List<RemoteViewObjectLight> getOSPViews(@WebParam(name = "sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Updates an existing OSP view
         * @param viewId The id of the view
         * @param name The new name of the view. Null if to remain unchanged
         * @param description The new description of the view. Null if to remain unchanged
         * @param content  The new content of the view. Null if to remain unchanged
         * @param sessionId The session token
         * @throws ServerSideException If the view could not be found or if the new name (if applicable) is empty
         */
        @WebMethod(operationName = "updateOSPView")
        public void updateOSPView(@WebParam(name = "viewId")long viewId, @WebParam(name = "name")String name, @WebParam(name = "description")String description, 
                @WebParam(name = "content")byte[] content, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
        
        /**
         * Deletes an existing OSP view
         * @param viewId The id of the view to be deleted
         * @param sessionId The session token
         * @throws ServerSideException If the view could not be found
         */
        @WebMethod(operationName = "deleteOSPView")
        public void deleteOSPView(@WebParam(name = "viewId")long viewId, @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
        
        //</editor-fold>
    // </editor-fold>
 
    // <editor-fold defaultstate="collapsed" desc="Helpers. Click on the + sign on the left to edit the code.">/**
    /**
     * Gets the IP address from the client issuing the request
     * @return the IP address as string
     */
    public String getIPAddress();
    
    // <editor-fold defaultstate="collapsed" desc="Service Manager">
    /**
     * Creates a Customer Pool
     * @param poolName Customer Pool name
     * @param poolDescription Customer Pool description
     * @param sessionId Session id token
     * @return The id of the new Customer Pool
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "createCustomerPool")
    public String createCustomerPool(
            @WebParam(name = "poolName") String poolName,
            @WebParam(name = "poolDescription") String poolDescription,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the project pools
     * @param sessionId Session id token
     * @return The list of customer pools
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "getCustomerPools")
    public List<RemotePool> getCustomerPools(@WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets a customer pool
     * @param poolId The customer pool id
     * @param poolClass The customer pool class
     * @param sessionId Session id token
     * @return A detailed representation of the requested customer pool
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "getCustomerPool")
    public RemotePool getCustomerPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates a customer pool
     * @param poolId The customer pool id
     * @param poolClass The customer pool class
     * @param poolName The customer pool name
     * @param poolDescription The customer pool description
     * @param sessionId Session id token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "updateCustomerPool")
    public void updateCustomerPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "poolName") String poolName,
            @WebParam(name = "poolDescription") String poolDescription,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;

    /**
     * Deletes a customer pool
     * @param poolId The customer pool id
     * @param poolClass The customer pool class
     * @param sessionId Session id token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "deleteCustomerPool")
    public void deleteCustomerPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates a customer
     * @param poolId Parent pool id
     * @param customerClass This customer is going to be instance of
     * @param attributeNames Attributes to be set in the new customer. Null or empty array for none
     * @param attributeValues Attributes to be set in the new customer (values). Null for none. The size of this array must match attributeNames size
     * @param sessionId Session id token
     * @return The id of the newly created customer
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "createCustomer")
    public String createCustomer(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "customerClass") String customerClass,
            @WebParam(name = "attributeNames") String[] attributeNames,
            @WebParam(name = "attributeValues") String[] attributeValues,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets all the customers
     * @param filters Attributes to filter for
     * @param page Page number of results to skip. Use -1 to retrieve all
     * @param limit Max number of results. Use -1 to retrieve all
     * @param sessionId Session token
     * @return All customers
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "getAllCustomers")
    public List<RemoteObjectLight> getAllCustomers(
            @WebParam(name = "filters") List<StringPair> filters,
            @WebParam(name = "page") int page,
            @WebParam(name = "limit") int limit,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the list of customers from a pool
     * @param poolId Parent pool id
     * @param className A given className to retrieve a set of customers of that className form the pool
     * @param page Page number of results to skip. Use -1 to retrieve all
     * @param limit Max number of results. Use -1 to retrieve all
     * @param sessionId Session token
     * @return The list of customers inside the pool
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "getCustomersInPool")
    public List<RemoteObjectLight> getCustomersInPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "className") String className,
            @WebParam(name = "page") int page,
            @WebParam(name = "limit") int limit,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the detailed information about a customer
     * @param customerClass Customer class name
     * @param customerId Customer id
     * @param sessionId Session token
     * @return A detailed representation of the requested customer
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "getCustomer")
    public RemoteObject getCustomer(
            @WebParam(name = "customerClass") String customerClass,
            @WebParam(name = "customerId") String customerId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates a customer
     * @param customerClass Customer class name
     * @param customerId Customer id
     * @param attributes The attributes to be updated (the key is the attribute name, 
     * the value is and array with the value -or values in case of MANY TO MANY list type attributes-).
     * @param sessionId Session token
     * @throws ServerSideException ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "updateCustomer")
    public void updateCustomer(
            @WebParam(name = "customerClass") String customerClass,
            @WebParam(name = "customerId") String customerId,
            @WebParam(name = "attributes") List<StringPair> attributes,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a customer
     * @param customerClass Customer class name
     * @param customerId Customer id
     * @param sessionId Session token
     * @throws ServerSideException ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "deleteCostumer")
    public void deleteCostumer(
            @WebParam(name = "customerClass")String customerClass,
            @WebParam(name = "customerId")String customerId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
        
    /**
     * Creates a Service Pool
     * @param customerClass The parent class name
     * @param customerId The parent id
     * @param poolName Service Pool name
     * @param poolDescription Service Pool description
     * @param sessionId Session id token
     * @return The id of the new Service Pool
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "createServicePool")
    public String createServicePool(
            @WebParam(name = "customerClass") String customerClass,
            @WebParam(name = "customerId") String customerId,
            @WebParam(name = "poolName") String poolName,
            @WebParam(name = "poolDescription") String poolDescription,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    
    /**
     * Retrieves the pools associated to a particular customer
     * @param customerClass The parent customer class name
     * @param customerId The parent customer id
     * @param servicePoolClass The class name used to filter the results. This class should be GenericService
     * @param sessionId Session id token
     * @return The service pools list
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "getServicePoolsInCostumer")
    public List<RemotePool> getServicePoolsInCostumer(
            @WebParam(name = "customerClass") String customerClass,
            @WebParam(name = "customerId") String customerId,
            @WebParam(name = "servicePoolClass") String servicePoolClass,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the detailed information about a service pool
     * @param poolId The service pool id
     * @param poolClass The service pool class name
     * @param sessionId Session id token
     * @return A detailed representation of the requested service pool
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getServicePool")
    public RemotePool getServicePool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates a service pool
     * @param poolId The service pool id
     * @param poolClass The service pool class
     * @param poolName The service pool name
     * @param poolDescription The service pool description
     * @param sessionId Session id token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "updateServicePool")
    public void updateServicePool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "poolName") String poolName,
            @WebParam(name = "poolDescription") String poolDescription,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a service pool
     * @param poolId The service pool id
     * @param poolClass The service pool class
     * @param sessionId Session id token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "deleteServicePool")
    public void deleteServicePool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "poolClass") String poolClass,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Creates a service
     * @param poolId Parent pool id
     * @param serviceClass This service is going to be instance of
     * @param attributeNames Attributes to be set in the new service. Null or empty array for none
     * @param attributeValues Attributes to be set in the new service (values). Null for none. The size of this array must match attributeNames size
     * @param sessionId Session id token
     * @return The id of the newly created service
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "createService")
    public String createService(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "serviceClass") String serviceClass,
            @WebParam(name = "attributeNames") String[] attributeNames,
            @WebParam(name = "attributeValues") String[] attributeValues,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets all the services
     * @param filters Attributes to filter for
     * @param page Page number of results to skip. Use -1 to retrieve all
     * @param limit Max number of results. Use -1 to retrieve all
     * @param sessionId Session token
     * @return All services
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "getAllServices")
    public List<RemoteObjectLight> getAllServices(
            @WebParam(name = "filters") List<StringPair> filters,
            @WebParam(name = "page") int page,
            @WebParam(name = "limit") int limit,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Retrieves the list of services from a pool
     * @param poolId Parent pool id
     * @param className A given className to retrieve a set of services of that className form the pool
     * @param page Page number of results to skip. Use -1 to retrieve all
     * @param limit Max number of results. Use -1 to retrieve all
     * @param sessionId Session token
     * @return The list of services inside the pool
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "getServicesInPool")
    public List<RemoteObjectLight> getServicesInPool(
            @WebParam(name = "poolId") String poolId,
            @WebParam(name = "className") String className,
            @WebParam(name = "page") int page,
            @WebParam(name = "limit") int limit,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Gets the detailed information about a service
     * @param serviceClass Service class name
     * @param serviceId Service id
     * @param sessionId Session token
     * @return A detailed representation of the requested service
     * @throws ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "getService")
    public RemoteObject getService(
            @WebParam(name = "serviceClass") String serviceClass,
            @WebParam(name = "serviceId") String serviceId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Updates a service
     * @param serviceClass Service class name
     * @param serviceId Service id
     * @param attributes The attributes to be updated (the key is the attribute name, 
     * the value is and array with the value -or values in case of MANY TO MANY list type attributes-).
     * @param sessionId Session token
     * @throws ServerSideException ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "updateService")
    public void updateService(
            @WebParam(name = "serviceClass") String serviceClass,
            @WebParam(name = "serviceId") String serviceId,
            @WebParam(name = "attributes") List<StringPair> attributes,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Deletes a service
     * @param serviceClass Service class name
     * @param serviceId Service id
     * @param sessionId Session token
     * @throws ServerSideException ServerSideException Generic exception encapsulating any possible error raised at run time
     */
    @WebMethod(operationName = "deleteService")
    public void deleteService(
            @WebParam(name = "serviceClass") String serviceClass,
            @WebParam(name = "serviceId") String serviceId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException;
    
    /**
     * Associates an object (a resource) to an existing service
     * @param objectClass Object class
     * @param objectId Object id
     * @param serviceClass service class
     * @param serviceId service id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the objects can't be found
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "relateObjectToService")
    public void relateObjectToService (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")String serviceId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Associates a list of objects (resources) to an existing service
     * @param objectClass Object class
     * @param objectId Object id
     * @param serviceClass service class
     * @param serviceId service id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the objects can't be found
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "relateObjectsToService")
    public void relateObjectsToService (
            @WebParam(name = "objectClass")String[] objectClass,
            @WebParam(name = "objectId")String[] objectId,
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")String serviceId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Releases an object from a service that is using it
     * @param serviceClass Service class
     * @param serviceId Service id
     * @param objectId Target object id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If the class can not be found
     *                             If the object activity log could no be found
     */
    @WebMethod(operationName = "releaseObjectFromService")
    public void releaseObjectFromService (
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")String serviceId,
            @WebParam(name = "objectId")String objectId,           
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    
    /**
     * Gets the objects (resources) associated to a service 
     * @param serviceClass Service class
     * @param serviceId Service id
     * @param sessionId Session token
     * @return A list of services
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If either the object class or the attribute can not be found
     */
    @WebMethod(operationName = "getObjectsRelatedToService")
    public List<RemoteObjectLight> getObjectsRelatedToService (
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")String serviceId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException;
    // </editor-fold>    
}