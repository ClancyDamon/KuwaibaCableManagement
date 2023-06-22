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
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for layout editor.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(LayoutRestOpenApi.PATH)
public interface LayoutRestOpenApi {
    // <editor-fold desc="layout-editor" defaultstate="collapsed">
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/layout-editor/"; //NOI18N
    
    @Operation(summary = "Creates a view for a given list type item. If there's already a view of the provided view type, it will be overwritten.", description = "The id of the new view.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST,
            value = "createListTypeItemRelatedLayout/{listTypeItemId}/{listTypeItemClassName}/{viewClassName}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createListTypeItemRelatedLayout(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_ID, required = true) String listTypeItemId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_CLASS_NAME, required = true) String listTypeItemClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_CLASS_NAME, required = true) String viewClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The structure of the view as string Base64,from an XML document with the view structure", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.STRUCTURE, required = true) String structure,
            @Parameter(in = ParameterIn.PATH, description = "The background image as string Base64. If any, \"null\" otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.BACKGROUND, required = true) String background,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Allows to retrieve a list of all existent layout views.", description = "The list of views.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ViewObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getLayouts/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ViewObjectLight> getLayouts(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The limit of results. -1 for all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Returns a layout view with the given id.", description = "An object representing the view.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ViewObject.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getLayout/{viewId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ViewObject getLayout(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_ID, required = true) long viewId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Returns the list type item related with the given view.", description = "An object representing the list type item.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BusinessObjectLight.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getListTypeItemForLayout/{viewId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getListTypeItemForLayout(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_ID, required = true) long viewId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates a new layout view.Creates a new Layout view with the given data.", description = "The id of the new view.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, 
            value = "createLayout/{viewClassName}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createLayout(
            @Parameter(in = ParameterIn.PATH, description = "View class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_CLASS_NAME, required = true) String viewClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The structure of the view as string Base64,from an XML document with the view structure", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.STRUCTURE, required = true) String structure,
            @Parameter(in = ParameterIn.PATH, description = "The background image as string Base64. If any, \"null\" otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.BACKGROUND, required = true) String background,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Relate a list type item with a view. Creates a relationship between the given list type and layout view.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "setListTypeItemRelatedLayout/{listTypeItemId}/{listTypeItemClassName}/{viewId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void setListTypeItemRelatedLayout(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_ID, required = true) String listTypeItemId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_CLASS_NAME, required = true) String listTypeItemClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The view id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_ID, required = true) long viewId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Release a list type item with a view.Deletes a relationship between the given list type and layout view.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "releaseListTypeItemRelatedLayout/{listTypeItemId}/{listTypeItemClassName}/{viewId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void releaseListTypeItemRelatedLayout(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_ID, required = true) String listTypeItemId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_CLASS_NAME, required = true) String listTypeItemClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The view id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_ID, required = true) long viewId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a view related to a list type item, such as the default, rack or equipment views.", description = "The associated view (there should be only one of each type). Null if there's none yet.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ViewObject.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getListTypeItemRelatedLayout/{listTypeItemId}/{listTypeItemClassName}/{viewId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ViewObject getListTypeItemRelatedLayout(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_ID, required = true) String listTypeItemId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_CLASS_NAME, required = true) String listTypeItemClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The view id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_ID, required = true) long viewId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates a view for a given list type item. If there's already a view of the provided view type, it will be overwritten.", description = "The summary of the changes.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeDescriptor.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, 
            value = "updateListTypeItemRelatedLayout/{listTypeItemId}/{listTypeItemClassName}/{viewId}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateListTypeItemRelatedLayout(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_ID, required = true) String listTypeItemId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_CLASS_NAME, required = true) String listTypeItemClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_ID, required = true) long viewId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The structure of the view as string Base64,from an XML document with the view structure", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.STRUCTURE, required = true) String structure,
            @Parameter(in = ParameterIn.PATH, description = "The background image as string Base64. If \"null\", the previous will be removed, if 0-sized array, it will remain unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.BACKGROUND, required = true) String background,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates a layout view. Updates the given layout view with the parameters provided.", description = "The summary of the changes.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeDescriptor.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, 
            value = "updateLayout/{viewId}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateLayout(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_ID, required = true) long viewId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The structure of the view as string Base64,from an XML document with the view structure", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.STRUCTURE, required = true) String structure,
            @Parameter(in = ParameterIn.PATH, description = "The background image as string Base64. If \"null\", the previous will be removed, if 0-sized array, it will remain unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.BACKGROUND, required = true) String background,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the views related to a list type item, such as the default, rack or equipment views.", description = "The associated views.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ViewObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getListTypeItemRelatedLayouts/{listTypeItemId}/{listTypeItemClassName}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ViewObjectLight> getListTypeItemRelatedLayouts(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_ID, required = true) String listTypeItemId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_CLASS_NAME, required = true) String listTypeItemClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max number of results.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a list type item related view.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteListTypeItemRelatedLayout/{listTypeItemId}/{listTypeItemClassName}/{viewId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteListTypeItemRelatedLayout(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_ID, required = true) String listTypeItemId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_CLASS_NAME, required = true) String listTypeItemClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Related view id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_ID, required = true) long viewId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a layout view.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteLayout/{viewId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteLayout(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Related view id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_ID, required = true) long viewId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the list of template elements with a device layout.", description = "The list of template elements with a device layout.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getDeviceLayouts/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getDeviceLayouts(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the device layout structure.", description = "The structure of the device layout.", tags = {"layout-editor"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = byte.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getDeviceLayoutStructure/{id}/{className}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public byte[] getDeviceLayoutStructure(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
}