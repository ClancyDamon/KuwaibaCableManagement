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

import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskNotificationDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskScheduleDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfileLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Task Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(TaskRestController.PATH)
public class TaskRestController implements TaskRestOpenApi {
    
    /**
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;
    
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    
    /**
     * Reference to the default logger.
     */
    public static Logger LOG = LoggerFactory.getLogger(TaskRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/task-manager/"; //NOI18N
    
    // <editor-fold desc="task-manager" defaultstate="collapsed">
    
    /**
     * Creates and schedule a task. A task is an application entity that allows to run jobs that will be executed depending on certain schedule.
     * @param task The task that contains, name, description, is the task enabled?,
     * commitOnExecute Should this task commit the changes made (if any) after executing it?,
     * the script to be executed, the parameters for the script,
     * schedule When the task should be executed,
     * notificationType How the result of the task should be notified to the associated users.
     * With the following structure:
     * <pre>
     * {@code
     *  {
     *      "name" : "",
     *      "description" : "",
     *      "enabled" : boolean,
     *      "commitOnExecute" : boolean,
     *      "script" : "",
     *      "parameters" : [
     *          {
     *              "key": "",
     *              "value": ""
     *          },
     *          {
     *              "key": "",
     *              "value": ""
     *          }
     *      ],
     *      "schedule" : {
     *              "startTime": long,
     *              "everyXMinutes": int,
     *              "executionType": int
     *      },
     *      "notificationType": {
     *          "email": "",
     *          "notificationType": int
     *      }
     *  }
     * }
     * </pre>
     * @param sessionId The session token id.
     * @return The id of the newly created task.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createTask/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createTask(
            @RequestBody Task task,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createTask", "127.0.0.1", sessionId);
            return aem.createTask(
                    task.getName() == null ? "" : task.getName(),
                    task.getDescription()== null ? "" : task.getDescription(),
                    task.isEnabled(),
                    task.commitOnExecute(),
                    task.getScript() == null ? "" : task.getScript(),
                    task.getParameters() == null ? new ArrayList<StringPair>() : task.getParameters(),
                    task.getSchedule() == null ? new TaskScheduleDescriptor() : task.getSchedule(),
                    task.getNotificationType() == null ? new TaskNotificationDescriptor() : task.getNotificationType()
            );
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Updates any of these properties from a task: name, description, enabled and script.
     * @param id Task id.
     * @param property Property name. Possible values: "name", "description", "enabled" and "script".
     * @param value The value of the property. For the property "enabled", the allowed values are "true" and "false".
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateTaskProperties/{id}/{property}/{value}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateTaskProperties(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.PROPERTY) String property,
            @PathVariable(RestConstants.VALUE) String value,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateTaskProperties", "127.0.0.1", sessionId);
            return aem.updateTaskProperties(id, property, value);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Updates the parameters of a task. If any of the values is null, that parameter will be deleted, if the parameter does not exist, it will be created.
     * @param id Task id.
     * @param parameters The parameters to be modified as pairs paramName/paramValue.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateTaskParameters/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateTaskParameters(
            @PathVariable(RestConstants.ID) long id,
            @RequestBody List<StringPair> parameters,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateTaskParameters", "127.0.0.1", sessionId);
            return aem.updateTaskParameters(id, parameters);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Updates a task schedule.
     * @param id Task id.
     * @param schedule New schedule.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateTaskSchedule/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateTaskSchedule(
            @PathVariable(RestConstants.ID) long id,
            @RequestBody TaskScheduleDescriptor schedule,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateTaskSchedule", "127.0.0.1", sessionId);
            return aem.updateTaskSchedule(id, schedule);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Updates a task notification type.
     * @param id Task id.
     * @param notificationType New notification type.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateTaskNotificationType/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateTaskNotificationType(
            @PathVariable(RestConstants.ID) long id,
            @RequestBody TaskNotificationDescriptor notificationType,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateTaskNotificationType", "127.0.0.1", sessionId);
            return aem.updateTaskNotificationType(id, notificationType);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Deletes a task and unsubscribes all users from it.
     * @param id Task id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteTask/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteTask(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteTask", "127.0.0.1", sessionId);
            aem.deleteTask(id);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Subscribes a user to a task, so it will be notified of the result of its execution.
     * @param userId Id of the user.
     * @param id Id of the task.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "subscribeUserToTask/{userId}/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor subscribeUserToTask(
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("subscribeUserToTask", "127.0.0.1", sessionId);
            return aem.subscribeUserToTask(userId, id);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Unsubscribes a user from a task, so it will no longer be notified about the result of its execution.
     * @param userId Id of the user.
     * @param id Id of the task.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "unsubscribeUserFromTask/{userId}/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor unsubscribeUserFromTask(
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("unsubscribeUserFromTask", "127.0.0.1", sessionId);
            return aem.unsubscribeUserFromTask(userId, id);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Retrieves the information about a particular task.
     * @param id Id of the task.
     * @param sessionId The session token id.
     * @return A list with the task objects.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getTask/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Task getTask(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getTask", "127.0.0.1", sessionId);
            return aem.getTask(id);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Gets the subscribers of a particular task.
     * @param id Task id.
     * @param sessionId The session token id.
     * @return The list of users subscribers to the task identified with the id taskId.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getSubscribersForTask/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<UserProfileLight> getSubscribersForTask(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSubscribersForTask", "127.0.0.1", sessionId);
            return aem.getSubscribersForTask(id);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Gets all registered tasks.
     * @param sessionId The session token id.
     * @return A list with the task objects.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getTasks/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Task> getTasks(@PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getTasks", "127.0.0.1", sessionId);
            return aem.getTasks();
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Gets the tasks associated to a particular user.
     * @param userId Id of the user.
     * @param sessionId The session token id.
     * @return A list with the task objects.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getTasksForUser/{userId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Task> getTasksForUser(
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getTasksForUser", "127.0.0.1", sessionId);
            return aem.getTasksForUser(userId);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Executes a task on demand. The task may have user-defined parameters that must be set using {@link #updateTaskParameters(long, java.util.List) } before running the task.
     * @param id Id of the task. Could be known by calling {@link #getTasks() } first.
     * @param sessionId The session token id.
     * @return An {@link TaskResult} instance representing the task result.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "executeTask/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public TaskResult executeTask(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("executeTask", "127.0.0.1", sessionId);
            return aem.executeTask(id);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    // </editor-fold>
}