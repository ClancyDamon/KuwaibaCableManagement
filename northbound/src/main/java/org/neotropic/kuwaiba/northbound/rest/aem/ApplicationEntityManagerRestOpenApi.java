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
import java.util.HashMap;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.BusinessRule;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for Application Entity Manager.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(ApplicationEntityManagerRestOpenApi.PATH)
public interface ApplicationEntityManagerRestOpenApi {
    /**
     * Path that includes the Kuwaiba version and core
     */
    public static final String PATH = "/v2.1/core/aem/"; //NOI18N
    
    // <editor-fold desc="pools" defaultstate="collapsed">
    
    @Operation(summary = "Creates a pool without a parent. They're used as general purpose place to put inventory objects, or as root for particular models.", description = "The id of the new pool.", tags = {"pools"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createRootPool/{name}/{description}/{className}/{type}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createRootPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Pool name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Pool description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "What kind of objects can this pool contain?", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TYPE, required = true) int type,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates a pool that will have as parent an inventory object. This special containment structure can be used to provide support for new models.", description = "The id of the new pool.", tags = {"pools"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createPoolInObject/{parentClassName}/{parentId}/{name}/{description}/{className}/{type}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createPoolInObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name of the parent object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the parent object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Pool name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Pool description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "What kind of objects can this pool contain?", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TYPE, required = true) int type,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates a pool that will have as parent another pool. This special containment structure can be used to provide support for new models.", description = "The id of the new pool.", tags = {"pools"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createPoolInPool/{parentId}/{name}/{description}/{className}/{type}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createPoolInPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the parent object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Pool name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Pool description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "What kind of objects can this pool contain?", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Type of pool. Not used so far, but it will be in the future. It will probably be used to help organize the existing pools.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TYPE, required = true) int type,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a set of pools. Note that this method will delete and commit the changes until it finds an error, so if deleting any of the pools fails, don't try to delete those that were already processed.", tags = {"pools"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deletePools/{ids}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deletePools(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The list of ids from the objects to be deleted.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IDS, required = true) String[] ids,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates a pool. The class name field is read only to preserve the integrity of the pool. Same happens to the field type.", description = "The summary of the changes.", tags = {"pools"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ChangeDescriptor.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "setPoolProperties/{poolId}/{name}/{description}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor setPoolProperties(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Pool Id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Pool name. If null, this field will remain unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Pool description. If null, this field will remain unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);   
    // </editor-fold>
    
    // <editor-fold desc="audit-trail" defaultstate="collapsed">
    @Operation(summary = "Gets a business object audit trail.", description = "The list of activity entries.", tags = {"audit-trail"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ActivityLogEntry.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getBusinessObjectAuditTrail/{className}/{objectId}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ActivityLogEntry> getBusinessObjectAuditTrail(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max number of results to be shown.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the number of general activity log entries.", description = "The number of activity log entries.", tags = {"audit-trail"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "getGeneralActivityAuditTrailCount/{page}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getGeneralActivityAuditTrailCount(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Current page.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) int page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Limit of results per page. 0 to retrieve them all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The response may be filtered by user (use key <code>user</code>, value the user name, a String) or event type (use key <code>type</code>,"
                    + "value any from ActivityLogEntry.ACTIVITY_TYPE_XXXX, an integer). If this parameter is null, no filters will be applied. If a key is not present, it won't be used as filter. If both are present, a logical AND will be applied.",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, Object> filters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the list of general activity log entries.", description = "The list of activity log entries. The entries are sorted by creation date in descending order.", tags = {"audit-trail"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ActivityLogEntry.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "getGeneralActivityAuditTrail/{page}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ActivityLogEntry> getGeneralActivityAuditTrail(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Current page.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) int page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Limit of results per page. 0 to retrieve them all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The response may be filtered by user (use key <code>user</code>, value the user name, a String) or event type (use key <code>type</code>,"
                    + "value any from ActivityLogEntry.ACTIVITY_TYPE_XXXX, an integer). If this parameter is null, no filters will be applied. If a key is not present, it won't be used as filter. If both are present, a logical AND will be applied.",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, Object> filters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
   
    // <editor-fold desc="views" defaultstate="collapsed">
    
    @Operation(summary = "Get a view related to an object, such as the default rack or object views.", description = "The associated view (there should be only one of each type). Null if there's none yet.", tags = {"views"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ViewObject.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getObjectRelatedView/{objectId}/{className}/{viewId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ViewObject getObjectRelatedView(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_ID, required = true) long viewId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Get a view related to an object, such as the default, rack or equipment views.", description = "The associated views.", tags = {"views"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ViewObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getObjectRelatedViews/{objectId}/{className}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ViewObjectLight> getObjectRelatedViews(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = " Max number of results.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Allows to retrieve a list of views of a certain type, specifying their class.", description = "The view class name.", tags = {"views"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ViewObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getGeneralViews/{className}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ViewObjectLight> getGeneralViews(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The limit of results. -1 for all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Returns a view of those that are not related to a particular object (i.e.: GIS views).", description = "An object representing the view.", tags = {"views"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ViewObject.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getGeneralView/{viewId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ViewObject getGeneralView(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_ID, required = true) long viewId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates a view for a given object.If there's already a view of the provided view type, it will be overwritten.", description = "The id of the new view.", tags = {"views"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST,
            value = "createObjectRelatedView/{objectId}/{className}/{name}/{description}/{viewClassName}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createObjectRelatedView(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View class name (See class ViewObject for details about the supported types).", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_CLASS_NAME, required = true) String viewClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The structure of the view as string Base64, from an XML document with the view structure (see http://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format).", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.STRUCTURE, required = true) String structure,
            @Parameter(in = ParameterIn.PATH, description = "The background image as string Base64. Used \"null\" for none.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.BACKGROUND, required = true) String background,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates a view not related to a particular object.", description = "The id of the newly created view.", tags = {"views"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST,
            value = "createGeneralView/{className}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createGeneralView(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The structure of the view as string Base64, from an XML document specifying the view structure (nodes, edges, control points).", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.STRUCTURE, required = true) String structure,
            @Parameter(in = ParameterIn.PATH, description = "The background image as string Base64. Used \"null\" for none.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.BACKGROUND, required = true) String background,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates a view for a given object.If there's already a view of the provided view type, it will be overwritten.", description = "The summary of the changes.", tags = {"views"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeDescriptor.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateObjectRelatedView/{objectId}/{className}/{viewId}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateObjectRelatedView(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VIEW_ID, required = true) long viewId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The structure of the view as string Base64, from an XML document with the view structure (see http://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format).", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.STRUCTURE, required = true) String structure,
            @Parameter(in = ParameterIn.PATH, description = "The background image as string Base64. If \"null\", the previous will be removed, if 0-sized array, it will remain unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.BACKGROUND, required = true) String background,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Saves a view not related to a particular object.The view type can not be changed.", description = "The summary of the changes.", tags = {"views"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeDescriptor.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateGeneralView/{id}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateGeneralView(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View name. Null to leave unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "View description. Null to leave unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The structure of the view as string Base64, from an XML document specifying the view structure (nodes, edges, control points). Null to leave unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.STRUCTURE, required = true) String structure,
            @Parameter(in = ParameterIn.PATH, description = "The background image as string Base64. If \"null\", the previous will be removed, if 0-sized array, it will remain unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.BACKGROUND, required = true) String background,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a list of general views.", tags = {"views"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteGeneralViews/{ids}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteGeneralViews(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The ids of the views to be deleted.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IDS, required = true) List<Long> ids,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="files" defaultstate="collapsed">
    
    @Operation(summary = "Relates a file to a list type item.", description = "The id of the resulting file object.", tags = {"files"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "attachFileToListTypeItem/{name}/{tags}/{file}/{listTypeItemClassName}/{listTypeItemId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long attachFileToListTypeItem(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the file.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The tags that describe the contents of the file.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TAGS, required = true) String tags,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The file itself as string Base64.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILE, required = true) String file,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The list type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_CLASS_NAME, required = true) String listTypeItemClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the list type item the file will be attached to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_ID, required = true) String listTypeItemId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Fetches the files associated to an inventory object. Note that this call won't retrieve the actual files, but only references to them.", description = "The list of files.", tags = {"files"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = FileObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getFilesForListTypeItem/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<FileObjectLight> getFilesForListTypeItem(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object whose files will be fetched from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object whose files will be fetched from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves a particular file associated to an inventory list type item.This call returns the actual file.", description = "The list of files.", tags = {"files"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FileObject.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getFile/{id}/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public FileObject getFile(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "TThe id of the file object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object the file is associated to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the list type item the file is associated to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Releases (and deletes) a file associated to a list type item.", tags = {"files"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "detachFileFromListTypeItem/{id}/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void detachFileFromListTypeItem(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "TThe id of the file object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the list type item the file is associated to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the list type item the file is associated to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates the properties of a file list type item (name or tags).", tags = {"files"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateFileProperties/{id}/{className}/{listTypeItemId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateFileProperties(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the file.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The set of properties as a dictionary key-value. Valid keys are name and tags.", required = true, content = @Content(schema = @Schema(implementation = StringPair.class)))
            @RequestBody List<StringPair> properties,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object the file is attached to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the list type item the file is attached to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIST_TYPE_ITEM_ID, required = true) String listTypeItemId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="business-rule" defaultstate="collapsed">
    
    @Operation(summary = "Creates a business rule given a set of constraints.", description = "The id of the newly created business rule.", tags = {"business-rule"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createBusinessRule/{name}/{description}/{type}/{scope}/{appliesTo}/{version}/{constraints}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createBusinessRule(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Rule name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Rule description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Rule type. See BusinesRule.TYPE* for possible values.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TYPE, required = true) int type,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scope of the rule. See BusinesRule.SCOPE* for possible values.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SCOPE, required = true) int scope,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class this rule applies to. Can not be null.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.APPLIES_TO, required = true) String appliesTo,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The version of the rule. Useful to migrate it if necessary in further versions of the platform.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VERSION, required = true) String version,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "An array with the definition of the logic to be matched with the rule. Can not be empty or null.", required = false, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONSTRAINTS, required = true) List<String> constraints,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a business rule.", tags = {"business-rule"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteBusinessRule/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteBusinessRule(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Rule id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the business rules of a particular type.", description = "The list of business rules with the matching type.", tags = {"business-rule"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessRule.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getBusinessRules/{type}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessRule> getBusinessRules(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Rule id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TYPE, required = true) int type,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    @Operation(summary = "Get the data model class hierarchy as an XML document.", description = "The class hierarchy as an XML document.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = byte.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getClassHierachy/{showAll}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public byte[] getClassHierachy(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SHOW_ALL, required = true) boolean showAll,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
}