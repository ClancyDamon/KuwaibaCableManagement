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
import org.neotropic.kuwaiba.core.apis.persistence.application.FilterDefinition;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for filters.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(FiltersRestOpenApi.PATH)
public interface FiltersRestOpenApi {
    // <editor-fold desc="configuration-filters" defaultstate="collapsed">
    public static final String PATH = "/v2.1/configuration-filters/"; //NOI18N 
    
    @Operation(summary = "Creates a filter.", description = "The id of the newly created filter definition.", tags = {"configuration-filters"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createFilterDefinition/{name}/{description}/{className}/{script}/{enabled}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createFilterDefinition(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the filter. It's recommended to use camel case notation (for example thisIsAName). This field is mandatory.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The optional description of the filter.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class or super class of the classes whose instances will be checked against this filter.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The groovy script containing the logic of the filter , that is, the.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SCRIPT, required = true) String script,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If this filter should be applied or not.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ENABLED, required = true) boolean enabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Updates the properties of a filter.", tags = {"configuration-filters"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateFilterDefinition/{id}/{name}/{description}/{className}/{script}/{enabled}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateFilterDefinition(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the filter definition to be updated.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The new name, not null.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The new description, if any, \"null\" otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The new class to be associated to this filer, if any, \"null\" otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The new script, if any, \"null\" otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SCRIPT, required = true) String script,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If the filer should be enabled or not, false by default.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ENABLED, required = true) boolean enabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "For a given class retrieves all its filters definitions (also the filters of its parent classes could be included).", description = "The list of filter definitions.", tags = {"configuration-filters"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = FilterDefinition.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "getFilterDefinitionsForClass/{className}/{includeParentClassesFilters}/{ignoreCache}/{page}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<FilterDefinition> getFilterDefinitionsForClass(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class to retrieve the filter definitions from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If the return must include the filters of the parent class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_PARENT_CLASSES_FILTERS, required = true) boolean includeParentClassesFilters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "To avoid filters from the cache useful in filters module edition, or to get cached filters useful when retrieve filters to execute.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IGNORE_CACHE, required = true) boolean ignoreCache,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Attributes to filter the FiltersDefinition null to not filter.", required = false, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, Object> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Page the number of page of number of elements to skip, -1 to not skip any result.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) int page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Limit of results per page, -1 to retrieve all elements.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "For a given class name returns the count of the filters definitions.", description = "The count of filters definitions.", tags = {"configuration-filters"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "getFilterDefinitionsForClassCount/{className}/{includeParentClassesFilters}/{ignoreCache}/{page}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getFilterDefinitionsForClassCount(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class to retrieve the filter definitions from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If the return must include the filters of the parent class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_PARENT_CLASSES_FILTERS, required = true) boolean includeParentClassesFilters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "To avoid filters from the cache useful in filters module edition, or to get cached filters useful when retrieve filters to execute.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IGNORE_CACHE, required = true) boolean ignoreCache,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Attributes to filter the FiltersDefinition null to not filter.", required = false, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, Object> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Page the number of page of number of elements to skip, -1 to not skip any result.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) int page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Limit of results per page, -1 to retrieve all elements.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Retrieves all the filters created in the inventory.", description = "The list of filter definitions.", tags = {"configuration-filters"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = FilterDefinition.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "getAllFilterDefinitions/{page}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<FilterDefinition> getAllFilterDefinitions(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Attributes to filter the FiltersDefinition null to not filter.", required = false, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, Object> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Page the number of page of number of elements to skip, -1 to not skip any result.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) int page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Limit of results per page, -1 to retrieve all elements.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Counts all the Filters definition created.", description = "The count of FiltersDefinitions.", tags = {"configuration-filters"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "getAllFilterDefinitionsCount/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getAllFilterDefinitionsCount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Attributes to filter the FiltersDefinition null to not filter.", required = false, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, Object> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    
    @Operation(summary = "Deletes a filter definition.", tags = {"configuration-filters"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteFilterDefinition/{id}/{className}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteFilterDefinition(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the filter to be deleted.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name of the filter to be deleted, used to remove the filter from the cache.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    // </editor-fold>
}