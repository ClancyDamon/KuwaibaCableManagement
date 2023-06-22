/*
 * Copyright 2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.northbound.rest.aem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfileLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * User Manager Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(UserRestController.PATH)
public class UserRestController implements UserRestOpenApi {
    
    /**
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;
    
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    
    /**
     * Reference to the default logger.
     */
    public static Logger LOG = LoggerFactory.getLogger(UserRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/user-manager/"; //NOI18N
    
    // <editor-fold desc="user-manager" defaultstate="collapsed">
    /**
     * Retrieves the list of all users.
     * @param sessionId The session token id.
     * @return The list of users.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getUsers/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<UserProfile> getUsers(@PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getUsers", "127.0.0.1", sessionId);
            return aem.getUsers();
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Retrieves the list of all groups.
     * @param sessionId The session token id.
     * @return An array of GroupProfile.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getGroups/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<GroupProfile> getGroups(@PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getGroups", "127.0.0.1", sessionId);
            return aem.getGroups();
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Retrieves a user from the session ring given a session id.
     * @param sessionId The session token id.
     * @return The user associated to a given session.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getUserInSession/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public UserProfile getUserInSession(@PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getUserInSession", "127.0.0.1", sessionId);
            return aem.getUserInSession(sessionId);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Gets all the users in a group.
     * @param groupId The id of the group.
     * @param sessionId The session token id.
     * @return The list of users in that group.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getUsersInGroup/{groupId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<UserProfile> getUsersInGroup(
                    @PathVariable(RestConstants.GROUP_ID) long groupId,
                    @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getUsersInGroup", "127.0.0.1", sessionId);
            return aem.getUsersInGroup(groupId);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Retrieves the list of groups a user belongs to.
     * @param userId The id of the user.
     * @param sessionId The session token id.
     * @return The list of groups for this user.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getGroupsForUser/{userId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<GroupProfileLight> getGroupsForUser(
                    @PathVariable(RestConstants.USER_ID) long userId,
                    @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getGroupsForUser", "127.0.0.1", sessionId);
            return aem.getGroupsForUser(userId);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Set the properties of a given user using the id to search for it.
     * @param id User id.
     * @param userName New user's name. Use null to leave it unchanged.
     * @param password New user's password. Use null to leave it unchanged.
     * @param firstName New user's first name. Use null to leave it unchanged.
     * @param lastName New user's last name. Use null to leave it unchanged.
     * @param enabled 0 for false, 1 for true, -1 to leave it unchanged.
     * @param type User type. See UserProfile.USER_TYPE* for possible values. Use -1 to leave it unchanged.
     * @param email New user's email. Use null to leave it unchanged.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, 
            value = "setUserProperties/{id}/{userName}/{password}/{firstName}/{lastName}/{enabled}/{type}/{email}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void setUserProperties(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.PASSWORD) String password,
            @PathVariable(RestConstants.FIRST_NAME) String firstName,
            @PathVariable(RestConstants.LAST_NAME) String lastName,
            @PathVariable(RestConstants.ENABLED) int enabled,
            @PathVariable(RestConstants.TYPE) int type,
            @PathVariable(RestConstants.EMAIL) String email,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("setUserProperties", "127.0.0.1", sessionId);
            aem.setUserProperties(id, userName, password, firstName, lastName, enabled, type, email);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Sets a privilege to a user. If the feature token provided already has been assigned to the user, the access level will be changed, otherwise, a privilege will be created.
     * @param userId The user Id.
     * @param featureToken The feature token. See class Privilege for details. Note that this token must match to the one expected by the client application. That's the only way the correct features will be enabled.
     * @param accessLevel The feature token. See class Privilege.ACCESS_LEVEL* for details.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "setPrivilegeToUser/{userId}/{featureToken}/{accessLevel}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void setPrivilegeToUser(
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.FEATURE_TOKEN) String featureToken,
            @PathVariable(RestConstants.ACCESS_LEVEL) int accessLevel,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("setPrivilegeToUser", "127.0.0.1", sessionId);
            aem.setPrivilegeToUser(userId, featureToken, accessLevel);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Sets a privilege to a group. If the feature token provided already has been assigned to the group, the access level will be changed, otherwise, a privilege will be created.
     * @param groupId The group Id.
     * @param featureToken The feature token. See class Privilege for details. Note that this token must match to the one expected by the client application. That's the only way the correct features will be enabled.
     * @param accessLevel The feature token. See class Privilege.ACCESS_LEVEL* for details.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "setPrivilegeToGroup/{groupId}/{featureToken}/{accessLevel}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void setPrivilegeToGroup(
            @PathVariable(RestConstants.GROUP_ID) long groupId,
            @PathVariable(RestConstants.FEATURE_TOKEN) String featureToken,
            @PathVariable(RestConstants.ACCESS_LEVEL) int accessLevel,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("setPrivilegeToGroup", "127.0.0.1", sessionId);
            aem.setPrivilegeToGroup(groupId, featureToken, accessLevel);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Set the attributes of a group.
     * @param id The id of the group.
     * @param name The name of the group. Use null to leave the old value.
     * @param description The description of the group. Use null to leave the old value.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "setGroupProperties/{id}/{name}/{description}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void setGroupProperties(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("setGroupProperties", "127.0.0.1", sessionId);
            aem.setGroupProperties(id, name, description);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Creates a user. System users ("hard-coded" kind of users used for internal tasks that can not be deleted nor modified byu the end users) 
     * can only be manipulated (that is, anything but created) by accessing directly to the database.
     * @param userName New user's name. Mandatory.
     * @param password New user's password.
     * @param firstName New user's first name.
     * @param lastName New user's last name.
     * @param enabled Shall the new user be enabled by default.
     * @param type User type. See UserProfileLight.USER_TYPE_* for possible values.
     * @param email New user's email.
     * @param privileges New user's privileges.
     * @param groupId Default group this user will be associated to.
     * @param sessionId The session token id.
     * @return The id of the newly created user.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value = "createUser/{userName}/{password}/{firstName}/{lastName}/{enabled}/{type}/{email}/{groupId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createUser(
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.PASSWORD) String password,
            @PathVariable(RestConstants.FIRST_NAME) String firstName,
            @PathVariable(RestConstants.LAST_NAME) String lastName,
            @PathVariable(RestConstants.ENABLED) boolean enabled,
            @PathVariable(RestConstants.TYPE) int type,
            @PathVariable(RestConstants.EMAIL) String email,
            @RequestBody List<Privilege> privileges,
            @PathVariable(RestConstants.GROUP_ID) long groupId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createUser", "127.0.0.1", sessionId);
            
            List<Privilege> listPrivileges = new ArrayList<>();
            privileges.forEach(p -> listPrivileges.add(new Privilege(p.getFeatureToken(), p.getAccessLevel())));
            return aem.createUser(userName, password, firstName, lastName, enabled, type, email, listPrivileges, groupId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Creates a group.
     * @param name The group name.
     * @param description The group description.
     * @param users Users who belong the group.
     * @param sessionId The session token id.
     * @return The new group id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createGroup/{name}/{description}/{users}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createGroup(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.USERS) Long[] users,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createGroup", "127.0.0.1", sessionId);
            return aem.createGroup(name, description, Arrays.asList(users));
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Adds a user to a group.
     * @param userId The id of the user to be added to the group.
     * @param groupId Id of the group which the user will be added to.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "addUserToGroup/{userId}/{groupId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addUserToGroup(
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.GROUP_ID) long groupId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("addUserToGroup", "127.0.0.1", sessionId);
            aem.addUserToGroup(userId, groupId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Removes a user from a group.
     * @param userId The id of the user to be removed from the group.
     * @param groupId Id of the group which the user will be removed from.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "removeUserFromGroup/{userId}/{groupId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removeUserFromGroup(
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.GROUP_ID) long groupId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("removeUserFromGroup", "127.0.0.1", sessionId);
            aem.removeUserFromGroup(userId, groupId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Removes a privilege from a user.
     * @param userId Id of the user.
     * @param featureToken The feature token. See class Privilege for details.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "removePrivilegeFromUser/{userId}/{featureToken}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removePrivilegeFromUser(
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.FEATURE_TOKEN) String featureToken,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("removePrivilegeFromUser", "127.0.0.1", sessionId);
            aem.removePrivilegeFromUser(userId, featureToken);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Removes a privilege from a group.
     * @param groupId Id of the group.
     * @param featureToken The feature token. See class Privilege for details.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "removePrivilegeFromGroup/{groupId}/{featureToken}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removePrivilegeFromGroup(
            @PathVariable(RestConstants.GROUP_ID) long groupId,
            @PathVariable(RestConstants.FEATURE_TOKEN) String featureToken,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("removePrivilegeFromGroup", "127.0.0.1", sessionId);
            aem.removePrivilegeFromGroup(groupId, featureToken);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Removes a list of users.
     * @param ids The ids of the users to be deleted.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteUsers/{ids}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteUsers(
            @PathVariable(RestConstants.IDS) Long[] ids,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteUsers", "127.0.0.1", sessionId);
            aem.deleteUsers(Arrays.asList(ids));
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Removes a list of groups.
     * @param ids The id of the groups to delete.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteGroups/{ids}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteGroups(
            @PathVariable(RestConstants.IDS) Long[] ids,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteGroups", "127.0.0.1", sessionId);
            aem.deleteGroups(Arrays.asList(ids));
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    //  </editor-fold>
}