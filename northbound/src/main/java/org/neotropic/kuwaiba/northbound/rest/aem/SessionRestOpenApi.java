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
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.validation.constraints.NotNull;
import javax.validation.Valid;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for session manager.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(SessionRestOpenApi.PATH)
public interface SessionRestOpenApi {
    
    // <editor-fold desc="session-manager" defaultstate="collapsed">
    public static final String PATH = "/v2.1/session-manager/"; //NOI18N
    
    @Operation(summary = "Creates a session. System users can not create sessions.", description = "A session object with information about the session itself plus information about the user.", tags={"session-manager"})
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Session.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "createSession/{user}/{password}/{sessionType}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Session createSession(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "User name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER, required = true) String user,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Password.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PASSWORD, required = true) String password,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The type of session to be created. This type depends on what kind of client is trying to access (a desktop client, a web client, a web service user, etc. See Session.TYPE_XXX for possible session types).", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_TYPE, required = true) int sessionType
    );
    
    @Operation(summary = "Closes a session.", tags={"session-manager"})
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "closeSession/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void closeSession(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session id (token).", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId
    );
    //  </editor-fold>
}