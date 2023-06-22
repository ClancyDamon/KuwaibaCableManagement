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
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for list type manager.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(ListTypeRestOpenApi.PATH)
public interface ListTypeRestOpenApi {
    // <editor-fold desc="lt-manager" defaultstate="collapsed">
    public static final String PATH = "/v2.1/lt-manager/"; //NOI18N
    
    @Operation(summary = "Retrieves all the items related to a given list type.", description = "A list of RemoteBusinessObjectLight instances representing the items.", tags = {"lt-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "/getListTypeItems/{className}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getListTypeItems(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Retrieves list type item given its id.", description = "A RemoteBusinessObjectLight instance representing the item.", tags = {"lt-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObject.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "/getListTypeItem/{className}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObject getListTypeItem(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the list type item.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Retrieves a list type item given its name.", description = "A RemoteBusinessObjectLight instance representing the item.", tags = {"lt-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "/getListTypeItem/{className}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getListTypeItemWithName(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of list type item.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Get the possible list types.", description = "A list of ClassMetadataLight instances representing the possible list types.", tags = {"lt-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getInstanceableListTypes/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getInstanceableListTypes(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Creates a list type item.", description = "New item's id.", tags = {"lt-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createListTypeItem/{className}/{name}/{displayName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createListTypeItem(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New item's name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New item's name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DISPLAY_NAME, required = true) String displayName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Updates a list type item.", description = "The summary of the changes made.", tags = {"lt-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ChangeDescriptor.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateListTypeItem/{className}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateListTypeItem(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the LTI to be updated. It must be a subclass of GenericObjectList.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the list type item to be updated.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.QUERY, description = "The attributes to be changed.", required = true, schema = @Schema())
            @Valid @RequestBody(required = true) HashMap<String, String> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Deletes a list type item.", tags = {"lt-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteListTypeItem/{className}/{id}/{releaseRelationships}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteListTypeItem(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Should the relationships be released.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RELEASE_RELATIONSHIPS, required = true) boolean releaseRelationships,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Release a list type item relationships.", tags = {"lt-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "releaseListTypeItem/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void releaseListTypeItem(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List type item id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Retrieves the objects that make reference to a given list type item.", description = "The list of business objects related to the list type item.", tags = {"lt-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getListTypeItemUses/{className}/{id}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getListTypeItemUses(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The list type class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The list type item id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The limit of results. Use -1 to retrieve all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    //  </editor-fold>
}