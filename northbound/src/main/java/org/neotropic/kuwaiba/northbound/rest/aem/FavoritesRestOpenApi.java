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
import org.neotropic.kuwaiba.core.apis.persistence.application.FavoritesFolder;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for favorites.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(FavoritesRestOpenApi.PATH)
public interface FavoritesRestOpenApi {
    // <editor-fold desc="favorites" defaultstate="collapsed">
    public static final String PATH = "/v2.1/favorites/"; //NOI18N
    
    @Operation(summary = "Adds an object to the favorites folder.", tags = {"favorites"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "addObjectTofavoritesFolder/{className}/{objectId}/{folderId}/{userId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addObjectTofavoritesFolder(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Favorites folder id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FOLDER_ID, required = true) long folderId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Removes an object associated to a favorites folder.", tags = {"favorites"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "removeObjectFromfavoritesFolder/{className}/{objectId}/{folderId}/{userId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removeObjectFromfavoritesFolder(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Favorites folder id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FOLDER_ID, required = true) long folderId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Create a relationship between an user and a new favorites folder.", description = "The new favorites folder id.", tags = {"favorites"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createFavoritesFolderForUser/{name}/{userId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createFavoritesFolderForUser(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Favorites folder name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Delete a Bookmark Folder of an User.", tags = {"favorites"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteFavoritesFolders/{ids}/{userId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteFavoritesFolders(
            @NotNull @Parameter(description = "Favorites folders id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IDS, required = true) long[] ids,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Get the favorites folders create by an user.", description = "List of Bookmarks folders for an User.", tags = {"favorites"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = FavoritesFolder.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getFavoritesFoldersForUser/{userId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<FavoritesFolder> getFavoritesFoldersForUser(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Get the object assigned to the bookmark.", description = "List of objects related to bookmark.", tags = {"favorites"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getObjectsInFavoritesFolder/{folderId}/{userId}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectsInFavoritesFolder(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Favorites folder id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FOLDER_ID, required = true) long folderId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max number of results.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Get the bookmarks where an object is associated.", description = "List of favorites folders where an object are an item.", tags = {"favorites"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = FavoritesFolder.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getFavoritesFoldersForObject/{userId}/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<FavoritesFolder> getFavoritesFoldersForObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a favorites folder.", description = "The favorite folder with id.", tags = {"favorites"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = FavoritesFolder.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getFavoritesFolder/{folderId}/{userId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public FavoritesFolder getFavoritesFolder(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Favorites folder id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FOLDER_ID, required = true) long folderId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates a favorites folder.", tags = {"favorites"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateFavoritesFolder/{folderId}/{userId}/{name}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateFavoritesFolder(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Favorites folder id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FOLDER_ID, required = true) long folderId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Favorites folder name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
}