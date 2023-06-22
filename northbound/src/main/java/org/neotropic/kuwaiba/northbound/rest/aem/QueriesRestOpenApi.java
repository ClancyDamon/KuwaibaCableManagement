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
import org.neotropic.kuwaiba.core.apis.persistence.application.CompactQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ExtendedQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ResultRecord;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueriesPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryParameter;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryResult;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.neotropic.kuwaiba.northbound.rest.todeserialize.TransientScriptedQueryParameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for queries.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(QueriesRestOpenApi.PATH)
public interface QueriesRestOpenApi {
    // <editor-fold desc="scripted-queries" defaultstate="collapsed">
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/scripted-queries/"; //NOI18N
    
    @Operation(summary = "Creates a scripted queries pool.", description = "The scripted queries pool id.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createScriptedQueriesPool/{name}/{description}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createScriptedQueriesPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String property,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates a scripted queries pool.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateScriptedQueriesPool/{id}/{name}/{description}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateScriptedQueriesPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String property,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a scripted queries pool.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteScriptedQueriesPool/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteScriptedQueriesPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a scripted queries pool given the name.", description = "A scripted queries pool.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScriptedQueriesPool.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueriesPoolByName/{name}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ScriptedQueriesPool getScriptedQueriesPoolByName(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a scripted queries pool.", description = "A scripted queries pool.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScriptedQueriesPool.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueriesPool/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ScriptedQueriesPool getScriptedQueriesPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Counts the scripted queries pools.", description = "The size of scripted queries pools.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = int.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueriesPoolCount/{filterName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public int getScriptedQueriesPoolCount(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool name to filter.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_NAME, required = true) String filterName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a set of scripted queries pools.", description = "A set of scripted queries pools.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ScriptedQueriesPool.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueriesPools/{filterName}/{skip}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ScriptedQueriesPool> getScriptedQueriesPools(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Filter by scripted queries pool name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_NAME, required = true) String filterName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Result skip.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SKIP, required = true) int skip,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Result limit.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Counts the scripted queries filter by name in a scripted queries pool.", description = "The size of scripted queries.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = int.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueryCountByPoolId/{id}/{filterName}/{ignoreDisabled}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public int getScriptedQueryCountByPoolId(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Filter by scripted query name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_NAME, required = true) String filterName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "True to ignore disabled scripted queries.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IGNORE_DISABLED, required = true) boolean ignoreDisabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a set of scripted queries in a scripted queries pool the its id.", description = "A set of scripted queries in a scripted queries pool.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ScriptedQuery.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueriesByPoolId/{id}/{filterName}/{ignoreDisabled}/{skip}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ScriptedQuery> getScriptedQueriesByPoolId(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted query name filter.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_NAME, required = true) String filterName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "True to return all scripted queries. False to return the enabled only.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IGNORE_DISABLED, required = true) boolean ignoreDisabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Result skip.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SKIP, required = true) int skip,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Result limit.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Counts the scripted queries filter by name in a scripted queries pool.", description = "The size of scripted queries.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = int.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueryCountByPoolName/{name}/{filterName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public int getScriptedQueryCountByPoolName(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted query name to filter.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_NAME, required = true) String filterName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a set of scripted queries in a scripted queries pool the its name.", description = "A set of scripted queries in a scripted queries pool.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ScriptedQuery.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueriesByPoolName/{name}/{filterName}/{ignoreDisabled}/{skip}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ScriptedQuery> getScriptedQueriesByPoolName(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted query name filter.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_NAME, required = true) String filterName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "True to return all scripted queries. False to return the enabled only.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IGNORE_DISABLED, required = true) boolean ignoreDisabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Result skip.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SKIP, required = true) int skip,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Result limit.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates a scripted query.", description = "The scripted query id.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createScriptedQuery/{id}/{name}/{description}/{script}/{enabled}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createScriptedQuery(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted queries pool id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted query name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted query description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted query script.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SCRIPT, required = true) String script,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "True to enable the Scripted query.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ENABLED, required = true) boolean enabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates a scripted query given its id.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateScriptedQuery/{id}/{name}/{description}/{script}/{enabled}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateScriptedQuery(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New scripting query name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New scripting query description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New query script.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SCRIPT, required = true) String script,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New value, true to enable the scripting query.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ENABLED, required = true) boolean enabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes an scripted query given the id.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteScriptedQuery/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteScriptedQuery(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripting query id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a scripted query given its id.", description = "A scripted query.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScriptedQuery.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQuery/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ScriptedQuery getScriptedQuery(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Counts the scripted queries.", description = "The size of scripted queries.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = int.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueryCount/{filterName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public int getScriptedQueryCount(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted query name to filter.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_NAME, required = true) String filterName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a set of scripted queries.", description = "A set of scripted queries.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ScriptedQuery.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueries/{filterName}/{ignoreDisabled}/{skip}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ScriptedQuery> getScriptedQueries(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Scripted query name to filter.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_NAME, required = true) String filterName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "True to return all scripted queries. False to return the enabled only.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IGNORE_DISABLED, required = true) boolean ignoreDisabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Result skip.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SKIP, required = true) int skip,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Result limit.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Executes the scripted query.", description = "The result of execute the scripted query.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScriptedQueryResult.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "executeScriptedQuery/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ScriptedQueryResult executeScriptedQuery(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query class id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The scripted query parameters.", required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransientScriptedQueryParameter.class))))
            @Valid @RequestBody List<TransientScriptedQueryParameter> parameters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates a scripted query parameter.", description = "The scripted query parameter id.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScriptedQuery.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createScriptedQueryParameter/{id}/{name}/{description}/{type}/{mandatory}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createScriptedQueryParameter(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query parameter name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query parameter description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query parameter type.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TYPE, required = true) String type,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "True scripted query parameter is mandatory.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.MANDATORY, required = true) boolean mandatory,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The scripted query parameter default value.", required = true, content = @Content(schema = @Schema(implementation = Object.class)))
            @Valid @RequestBody Object defaultValue,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates a scripted query parameter.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateScriptedQueryParameter/{id}/{name}/{description}/{type}/{mandatory}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateScriptedQueryParameter(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query parameter id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query parameter name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query parameter description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query parameter type.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TYPE, required = true) String type,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "True if the scripted query parameter is mandatory.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.MANDATORY, required = true) boolean mandatory,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The scripted query parameter default value.", required = true, content = @Content(schema = @Schema(implementation = Object.class)))
            @Valid @RequestBody Object defaultValue,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a scripted query parameter.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteScriptedQueryParameter/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteScriptedQueryParameter(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the scripted query parameter to delete.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets an scripted query parameter.", description = "The scripted query parameter.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScriptedQueryParameter.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueryParameter/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ScriptedQueryParameter getScriptedQueryParameter(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query parameter id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets an scripted query parameter.", description = "The scripted query parameter.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ScriptedQueryParameter.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueryParameters/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ScriptedQueryParameter> getScriptedQueryParameters(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The scripted query id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);  
    
    @Operation(summary = "Creates a Query.", description = "The id of the newly created query.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createQuery/{name}/{ownerId}/{structure}/{description}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createQuery(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the query.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The if of the user that will own the query. Use -1 to make it public.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OWNER_ID, required = true) long ownerId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The structure of the query as string Base64, from the XML document.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.STRUCTURE, required = true) String structure,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The description of the query.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates an existing query.", description = "The summary of the changes.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeDescriptor.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "saveQuery/{id}/{name}/{ownerId}/{structure}/{description}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor saveQuery(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the query.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the query. Leave null to keep the old value.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the user that owns this query. Use -1 to keep the old value.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OWNER_ID, required = true) long ownerId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The structure of the query as string Base64, from the XML document.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.STRUCTURE, required = true) String structure,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The description of the query. Leave null to keep the old value.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a Query.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteQuery/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteQuery(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the query.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets all queries.", description = "The list of queries.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CompactQuery.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getQueries/{showPublic}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<CompactQuery> getQueries(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Include public queries or show only the private ones.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SHOW_PUBLIC, required = true) boolean showPublic,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a single query.", description = "The query as an object.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CompactQuery.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getQuery/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public CompactQuery getQuery(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the query.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Used to perform complex queries.", description = "A set of objects matching the specified criteria as ResultRecord array.", tags = {"scripted-queries"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ResultRecord.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "executeQuery/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ResultRecord> executeQuery(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The code-friendly representation of the query made using the graphical query builder.",
                    required = true, content = @Content(schema = @Schema(implementation = ExtendedQuery.class)))
            @Valid @RequestBody ExtendedQuery query,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
}