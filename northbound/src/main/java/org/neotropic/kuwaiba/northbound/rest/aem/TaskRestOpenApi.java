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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskNotificationDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskScheduleDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfileLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for task manager.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(TaskRestOpenApi.PATH)
public interface TaskRestOpenApi {
    // <editor-fold desc="task-manager" defaultstate="collapsed">
    public static final String PATH = "/v2.1/task-manager/"; //NOI18N
    
    @Operation(summary = "Creates and schedule a task. A task is an application entity that allows to run jobs that will be executed depending on certain schedule.",
            description = "The id of the newly created task.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createTask/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The task that contains, name, description, is the task enabled?,"
                            + "commitOnExecute Should this task commit the changes made (if any) after executing it?,"
                            + "the script to be executed, the parameters for the script,"
                            + "schedule When the task should be executed,"
                            + "notificationType How the result of the task should be notified to the associated users.",
                    content = @Content(schema = @Schema(implementation = Task.class))
            )
            @Valid @RequestBody Task task,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates any of these properties from a task: name, description, enabled and script.", description = "The summary of the changes.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ChangeDescriptor.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateTaskProperties/{id}/{property}/{value}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateTaskProperties(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Task id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Property name. Possible values: \"name\", \"description\", \"enabled\" and \"script\".", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PROPERTY, required = true) String property,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The value of the property. For the property \"enabled\", the allowed values are \"true\" and \"false\".", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.VALUE, required = true) String value,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates the parameters of a task. If any of the values is null, that parameter will be deleted, if the parameter does not exist, it will be created.", description = "The summary of the changes.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ChangeDescriptor.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateTaskParameters/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateTaskParameters(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Task id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parameters to be modified as pairs paramName/paramValue.", required = true, schema = @Schema())
            @Valid @RequestBody List<StringPair> parameters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates a task schedule.", description = "The summary of the changes.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ChangeDescriptor.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateTaskSchedule/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateTaskSchedule(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Task id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New schedule.", required = true, schema = @Schema())
            @Valid @RequestBody TaskScheduleDescriptor schedule,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates a task notification type.", description = "The summary of the changes.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ChangeDescriptor.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateTaskNotificationType/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateTaskNotificationType(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Task id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "New notification type.", required = true, schema = @Schema())
            @Valid @RequestBody TaskNotificationDescriptor notificationType,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a task and unsubscribes all users from it.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteTask/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteTask(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Task id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Subscribes a user to a task, so it will be notified of the result of its execution.", description = "The summary of the changes.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ChangeDescriptor.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "subscribeUserToTask/{userId}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor subscribeUserToTask(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the user.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the task.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Unsubscribes a user from a task, so it will no longer be notified about the result of its execution.", description = "The summary of the changes.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ChangeDescriptor.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "unsubscribeUserFromTask/{userId}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor unsubscribeUserFromTask(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the user.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the task.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the information about a particular task.", description = "A list with the task objects.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Task.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getTask/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Task getTask(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the task.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the subscribers of a particular task.", description = "The list of users subscribers to the task identified with the id taskId.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = UserProfileLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getSubscribersForTask/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<UserProfileLight> getSubscribersForTask(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Task id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets all registered tasks.", description = "A list with the task objects.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Task.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getTasks/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Task> getTasks(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the tasks associated to a particular user.", description = "A list with the task objects.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Task.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getTasksForUser/{userId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Task> getTasksForUser(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the user.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_ID, required = true) long userId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Executes a task on demand. The task may have user-defined parameters that must be set using {@link #updateTaskParameters(long, java.util.List) } before running the task.", description = "An {@link TaskResult} instance representing the task result.", tags = {"task-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = TaskResult.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "executeTask/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public TaskResult executeTask(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the task. Could be known by calling {@link #getTasks() } first.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
}