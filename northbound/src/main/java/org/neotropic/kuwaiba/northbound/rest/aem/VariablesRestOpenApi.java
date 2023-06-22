/*
 * Copyright 2023 Neotropic SAS.
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
import java.util.Properties;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.neotropic.kuwaiba.core.apis.persistence.application.ConfigurationVariable;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for variables.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(VariablesRestOpenApi.PATH)
public interface VariablesRestOpenApi {
    // <editor-fold desc="configuration-variables" defaultstate="collapsed">
    public static final String PATH = "/v2.1/configuration-variables/"; //NOI18N
    
    @Operation(summary = "Creates a configuration variable inside a pool. A configuration variable is a place where a value will be stored so it can retrieved by whomever need it. These variables are typically used to store values that help other modules to work, such as URLs, user names, dimensions, etc.",
            description = "The id of the newly created variable.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createConfigurationVariable/{poolId}/{name}/{description}/{type}/{masked}/{value}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createConfigurationVariable(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the pool where the configuration variable will be put.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the pool. This value can not be null or empty. Duplicate variable names are not allowed.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The description of the what the variable does.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The type of the variable. Use 1 for number, 2 for strings, 3 for booleans, 4 for unidimensional arrays and 5 for matrixes.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TYPE, required = true) int type,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If the value should be masked when rendered (for security reasons, for example).", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.MASKED, required = true) boolean masked,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "In most cases (primitive types like numbers, strings or booleans) will be the actual value of the variable as a string (for example \"5\" or \"admin\" or \"true\")."
                    + "Arrays: (value1,value2,value3,valueN), matrixes: [(row1col1, row1col2,... row1colN), (row2col1, row2col2,... row2colN), (rowNcol1, rowNcol2,... rowNcolN)]. The values will be interpreted as strings.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VALUE, required = true) String value,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates a pool of configuration variables.", description = "The id of the newly created pool.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createConfigurationVariablesPool/{name}/{description}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createConfigurationVariablesPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the pool. Empty or null values are not allowed.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The description of the pool.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates the value of a configuration variable.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateConfigurationVariable/{name}/{property}/{value}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateConfigurationVariable(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The current name of the variable that will be modified.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the property to be updated. Possible values are: \"name\", \"description\", \"type\", \"masked\" and \"value\".", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROPERTY, required = true) String property,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The new value as string.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VALUE, required = true) String value,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the configuration variables in a configuration variable pool.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateConfigurationVariablesPool/{poolId}/{property}/{value}/{userName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateConfigurationVariablesPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the pool to update.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The property to update. The valid values are \"name\" and \"description\".", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROPERTY, required = true) String property,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The value of the property to be updated.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VALUE, required = true) String value,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token user name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves a configuration variable.", description = "The variable.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ConfigurationVariable.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getConfigurationVariable/{name}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ConfigurationVariable getConfigurationVariable(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the variable to be retrieved.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves only the value of a configuration variable. Masked values are returned as null.", 
            description = "The value of the variable as a java object/data type. The numbers are returned as floats."
                    + "The arrays and matrixes are returned as <code>ArrayList{@literal <String>}</code> and <code>ArrayList<ArrayList{@literal <String>}</code> instances respectively.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Object.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getConfigurationVariableValue/{name}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Object getConfigurationVariableValue(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the variable. Masked values are returned as null.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the configuration variables in a configuration variable pool.", description = "The list of configuration variables in the given pool.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ConfigurationVariable.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getConfigurationVariablesInPool/{poolId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ConfigurationVariable> getConfigurationVariablesInPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id pool to retrieve the variables from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the configuration variables with a given prefix.", description = "The list of configuration variables with the given prefix.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ConfigurationVariable.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getConfigurationVariablesWithPrefix/{prefix}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ConfigurationVariable> getConfigurationVariablesWithPrefix(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The prefix of the variables name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PREFIX, required = true) String prefix,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets all the configuration variables in the database, no matter what pool they belong to.", description = "The list of existing configuration variables.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ConfigurationVariable.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getAllConfigurationVariables/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ConfigurationVariable> getAllConfigurationVariables(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the list of pools of configuration variables.", description = "The available pools of configuration variables.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = InventoryObjectPool.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getConfigurationVariablesPools/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<InventoryObjectPool> getConfigurationVariablesPools(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the configuration variables of this manager.", description = "A Properties object with the configuration variables.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Properties.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getConfiguration/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Properties getConfiguration(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a configuration variable.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteConfigurationVariable/{name}/{userName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteConfigurationVariable(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the variable to be deleted.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token user name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a configuration variables pool.Deleting a pool also deletes the configuration variables contained within.", tags = {"configuration-variables"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteConfigurationVariablesPool/{poolId}/{userName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteConfigurationVariablesPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the pool to be deleted.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token user name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    //  </editor-fold>
}