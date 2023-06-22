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
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ArtifactDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.neotropic.kuwaiba.northbound.rest.todeserialize.TransientArtifact;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for process manager.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(ProcessRestOpenApi.PATH)
public interface ProcessRestOpenApi {
    // <editor-fold desc="process-manager" defaultstate="collapsed">
    public static final String PATH = "/v2.1/process-manager/"; //NOI18N
    
    @Operation(summary = "Gets the artifact associated to an activity (for example, a form that was already filled in by a user in a previous, already committed activity).",
            description = "The artifact corresponding to the given activity.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Artifact.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getArtifactForActivity/{processInstanceId}/{activityId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Artifact getArtifactForActivity(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the process instance. This process may have been ended already.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_INSTANCE_ID, required = true) String processInstanceId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the activity the artifact belongs to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ACTIVITY_ID, required = true) String activityId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Given an activity definition, returns the artifact definition associated to it.", description = "An object containing the artifact definition.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ArtifactDefinition.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getArtifactDefinitionForActivity/{processDefinitionId}/{activityDefinitionId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ArtifactDefinition getArtifactDefinitionForActivity(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the process the activity is related to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_DEFINITION_ID, required = true) String processDefinitionId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the activity.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ACTIVITY_DEFINITION_ID, required = true) String activityDefinitionId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Saves the artifact generated once an activity has been completed (for example, the user filled in a form).", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "commitActivity/{processInstanceId}/{activityDefinitionId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void commitActivity(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The process instance the activity belongs to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_INSTANCE_ID, required = true) String processInstanceId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The activity id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ACTIVITY_DEFINITION_ID, required = true) String activityDefinitionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The artifact to be saved.", required = true, content = @Content(schema = @Schema(implementation = TransientArtifact.class)))
            @Valid @RequestBody TransientArtifact artifact,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates the artifact generated once an activity has been completed (for example, the user filled in a form).", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateActivity/{processInstanceId}/{activityDefinitionId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateActivity(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The process instance the activity belongs to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_INSTANCE_ID, required = true) String processInstanceId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The activity id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ACTIVITY_DEFINITION_ID, required = true) String activityDefinitionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The artifact to be saved.", required = true, content = @Content(schema = @Schema(implementation = TransientArtifact.class)))
            @Valid @RequestBody TransientArtifact artifact,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets Process Instance Activities Path.", description = "The activity definition.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ActivityDefinition.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getProcessInstanceActivitiesPath/{processInstanceId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ActivityDefinition> getProcessInstanceActivitiesPath(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Process Instance Id to get path.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_INSTANCE_ID, required = true) String processInstanceId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Requests for the next activity to be executed in a process instance.", description = "The activity definition.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActivityDefinition.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getNextActivityForProcessInstance/{processInstanceId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ActivityDefinition getNextActivityForProcessInstance(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The running process to get the next activity from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_INSTANCE_ID, required = true) String processInstanceId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves a process definition.", description = "The process definition. It contains an XML document to be parsed by the consumer.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProcessDefinition.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getProcessDefinition/{processDefinitionId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ProcessDefinition getProcessDefinition(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the process.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_DEFINITION_ID, required = true) String processDefinitionId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves an activity definition.", description = "The activity definition.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActivityDefinition.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getActivityDefinition/{processDefinitionId}/{activityDefinitionId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ActivityDefinition getActivityDefinition(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the process definition.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_DEFINITION_ID, required = true) String processDefinitionId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the activity definition.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ACTIVITY_DEFINITION_ID, required = true) String activityDefinitionId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a process definition.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteProcessDefinition/{processDefinitionId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteProcessDefinition(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The process definition to be deleted.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_DEFINITION_ID, required = true) String processDefinitionId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates a process definition, either its standard properties or its structure.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateProcessDefinition/{processDefinitionId}/{structure}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateProcessDefinition(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The process definition id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_DEFINITION_ID, required = true) String processDefinitionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The artifact to be saved.", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = StringPair.class))))
            @Valid @RequestBody List<StringPair> properties,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The structure of the process definition as string Base64, from an XML document that represents a BPMN process definition.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.STRUCTURE, required = true) String structure,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates a process definition.A process definition is the metadata that defines the steps and constraints of a given project.",
            description = "The id of the newly created process definition.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createProcessDefinition/{name}/{description}/{version}/{enabled}/{structure}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createProcessDefinition(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the new process definition.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The description of the new process definition.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The version of the new process definition. This is a three numbers, dot separated string (e.g. 2.4.1).", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VERSION, required = true) String version,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If the project is enabled to create instances from it.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ENABLED, required = true) boolean enabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The structure of the process definition as string Base64, from an XML document that represents a BPMN process definition.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.STRUCTURE, required = true) String structure,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a process instances of a process definition.", description = "The process instances.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ProcessInstance.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getProcessInstances/{processDefinitionId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ProcessInstance> getProcessInstances(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The process definition id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_DEFINITION_ID, required = true) String processDefinitionId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a process definition instances.", description = "The process instances.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ProcessDefinition.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getProcessDefinitions/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ProcessDefinition> getProcessDefinitions(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a process instance.", description = "A Process Instance for the given id.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProcessInstance.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getProcessInstance/{processInstanceId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ProcessInstance getProcessInstance(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The process definition id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_INSTANCE_ID, required = true) String processInstanceId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates a process definition.A process definition is the metadata that defines the steps and constraints of a given project.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "reloadProcessDefinitions/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void reloadProcessDefinitions(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates an instance of a process, that is, starts one.", description = "The id of the newly created process instance.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createProcessInstance/{processDefinitionId}/{name}/{description}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createProcessInstance(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the process to be started.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_DEFINITION_ID, required = true) String processDefinitionId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the new process.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The description of the new process.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates the process instance name and description.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateProcessInstance/{processInstanceId}/{name}/{description}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateProcessInstance(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The process instance id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_INSTANCE_ID, required = true) String processInstanceId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The process instance name to update.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The process instance description to update.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.DESCRIPTION, required = true) String description,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a process instance.", tags = {"process-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteProcessInstance/{processInstanceId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteProcessInstance(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Process Instance Id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROCESS_INSTANCE_ID, required = true) String processInstanceId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
}
