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
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for validator definition.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(ValidatorsRestOpenApi.PATH)
public interface ValidatorsRestOpenApi {
    // <editor-fold desc="configuration-validators" defaultstate="collapsed">
    public static final String PATH = "/v2.1/configuration-validators/"; //NOI18N
    
    @Operation(summary = "Creates a validator definition.", description = "The id of the newly created validator definition.", tags = {"configuration-validators"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createValidatorDefinition/{name}/{description}/{className}/{script}/{enabled}/{userName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createValidatorDefinition(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the validator. It's recommended to use camel case notation (for example thisIsAName).", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The optional description of the validator.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class or super class of the classes whose instances will be checked against this validator.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The groovy script containing the logic of the validator , that is, the.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SCRIPT, required = true) String script,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If this validador should be applied or not.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ENABLED, required = true) boolean enabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token user name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates the properties of a validator. The null values will be ignored.", tags = {"configuration-validators"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateValidatorDefinition/{id}/{name}/{description}/{className}/{script}/{enabled}/{userName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateValidatorDefinition(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of teh validator definition to be updated.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The new name, if any, null otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The new description, if any, null otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The new class to be associated to this validator, if any, null otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The new script, if any, null otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SCRIPT, required = true) String script,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If the validator should be enabled or not, if any, null otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ENABLED, required = true) boolean enabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token user name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves all the validator definitions in the system.", description = "The list of validator definitions.", tags = {"configuration-validators"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ValidatorDefinition.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getValidatorDefinitionsForClass/{className}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ValidatorDefinition> getValidatorDefinitionsForClass(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class to retrieve the validator definitions from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    
    @Operation(summary = "Gets all validator definitions, no matter what class they are related to.", description = "The list of validators.", tags = {"configuration-validators"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ValidatorDefinition.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getAllValidatorDefinitions/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ValidatorDefinition> getAllValidatorDefinitions(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Runs the existing validations for the class associated to the given object. Validators set to enabled = false will be ignored.", description = "The list of validators associated to the object and its class.", tags = {"configuration-validators"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Validator.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "runValidationsForObject/{className}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Validator> runValidationsForObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a validator definition.", tags = {"configuration-validators"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteValidatorDefinition/{id}/{userName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteValidatorDefinition(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the validator to be deleted.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token user name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    //  </editor-fold>
}