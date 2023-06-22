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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfileLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for user manager.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(UserRestOpenApi.PATH)
public interface UserRestOpenApi {
    // <editor-fold desc="user-manager" defaultstate="collapsed">
    public static final String PATH = "/v2.1/user-manager/"; //NOI18N
    
    @Operation(summary = "Retrieves the list of all users.", description = "The list of users.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = UserProfile.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getUsers/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<UserProfile> getUsers(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the list of all groups.", description = "An array of GroupProfile.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = GroupProfile.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getGroups/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<GroupProfile> getGroups(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves a user from the session ring given a session id.", description = "The user associated to a given session.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getUserInSession/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public UserProfile getUserInSession(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets all the users in a group.", description = "The list of users in that group.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getUsersInGroup/{groupId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<UserProfile> getUsersInGroup(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the group.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.GROUP_ID, required = true) long groupId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the list of groups a user belongs to.", description = "The list of groups for this user.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getGroupsForUser/{userId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<GroupProfileLight> getGroupsForUser(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the user.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Set the properties of a given user using the id to search for it.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, 
            value = "setUserProperties/{id}/{userName}/{password}/{firstName}/{lastName}/{enabled}/{type}/{email}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void setUserProperties(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New user's name. Use null to leave it unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New user's password. Use null to leave it unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PASSWORD, required = true) String password,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New user's first name. Use null to leave it unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FIRST_NAME, required = true) String firstName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New user's last name. Use null to leave it unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LAST_NAME, required = true) String lastName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "0 for false, 1 for true, -1 to leave it unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ENABLED, required = true) int enabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User type. See UserProfile.USER_TYPE* for possible values. Use -1 to leave it unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TYPE, required = true) int type,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New user's email. Use null to leave it unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.EMAIL, required = true) String email,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Sets a privilege to a user. If the feature token provided already has been assigned to the user, the access level will be changed, otherwise, a privilege will be created.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "setPrivilegeToUser/{userId}/{featureToken}/{accessLevel}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void setPrivilegeToUser(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The user Id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The feature token. See class Privilege for details. Note that this token must match to the one expected by the client application. That's the only way the correct features will be enabled.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FEATURE_TOKEN, required = true) String featureToken,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The feature token. See class Privilege.ACCESS_LEVEL* for details.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ACCESS_LEVEL, required = true) int accessLevel,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Sets a privilege to a group. If the feature token provided already has been assigned to the group, the access level will be changed, otherwise, a privilege will be created.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "setPrivilegeToGroup/{groupId}/{featureToken}/{accessLevel}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void setPrivilegeToGroup(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The group Id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.GROUP_ID, required = true) long groupId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The feature token. See class Privilege for details. Note that this token must match to the one expected by the client application. That's the only way the correct features will be enabled.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FEATURE_TOKEN, required = true) String featureToken,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The feature token. See class Privilege.ACCESS_LEVEL* for details.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ACCESS_LEVEL, required = true) int accessLevel,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Set the attributes of a group.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "setGroupProperties/{id}/{name}/{description}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void setGroupProperties(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the group.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the group. Use null to leave the old value.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The description of the group. Use null to leave the old value.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Creates a user.", description = "The id of the newly created user.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createUser/{userName}/{password}/{firstName}/{lastName}/{enabled}/{type}/{email}/{groupId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createUser(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New user's name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New user's password.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PASSWORD, required = true) String password,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New user's first name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FIRST_NAME, required = true) String firstName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New user's last name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LAST_NAME, required = true) String lastName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Shall the new user be enabled by default.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ENABLED, required = true) boolean enabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User type. See UserProfileLight.USER_TYPE_* for possible values", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TYPE, required = true) int type,
            @NotNull @Parameter(in = ParameterIn.PATH, description = " New user's email", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.EMAIL, required = true) String email,
            @NotNull @Parameter(description = "New user's privileges.", required = true, schema = @Schema())
            @Valid @RequestBody(required = true) List<Privilege> privileges,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Default group this user will be associated to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.GROUP_ID, required = true) long groupId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Creates a group.", description = "The new group id.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createGroup/{name}/{description}/{users}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createGroup(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The group name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The group description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Users who belong the group.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USERS, required = true) Long[] users,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Adds a user to a group.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "addUserToGroup/{userId}/{groupId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addUserToGroup(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the user to be added to the group.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The of the group which the user will be added to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.GROUP_ID, required = true) long groupId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Removes a user from a group.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "removeUserFromGroup/{userId}/{groupId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removeUserFromGroup(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the user to be removed from the group.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The Id of the group which the user will be removed from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.GROUP_ID, required = true) long groupId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Removes a privilege from a user.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "removePrivilegeFromUser/{userId}/{featureToken}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removePrivilegeFromUser(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the user.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The feature token. See class Privilege for details.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FEATURE_TOKEN, required = true) String featureToken,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Removes a privilege from a group.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "removePrivilegeFromGroup/{groupId}/{featureToken}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removePrivilegeFromGroup(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the group.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.GROUP_ID, required = true) long groupId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The feature token. See class Privilege for details.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FEATURE_TOKEN, required = true) String featureToken,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Removes a list of users.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteUsers/{ids}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteUsers(
            @NotNull @Parameter(description = "The ids of the users to be deleted.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IDS, required = true) Long[] ids,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Removes a list of groups.", tags = {"user-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteGroups/{ids}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteGroups(
            @NotNull @Parameter(description = "The id of the groups to delete.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IDS, required = true) Long[] ids,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    //  </editor-fold>
}