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

import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ArtifactDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.neotropic.kuwaiba.northbound.rest.RestUtil;
import org.neotropic.kuwaiba.northbound.rest.todeserialize.TransientArtifact;
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
 * Process Manager Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(ProcessRestController.PATH)
public class ProcessRestController implements ProcessRestOpenApi {
    
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
    public static Logger LOG = LoggerFactory.getLogger(ProcessRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/process-manager/"; //NOI18N
    
    // <editor-fold desc="process-manager" defaultstate="collapsed">
    
    /**
     * Gets the artifact associated to an activity (for example, a form that was already filled in by a user in a previous, already committed activity).
     * @param processInstanceId The id of the process instance. This process may have been ended already.
     * @param activityId The id of the activity the artifact belongs to.
     * @param sessionId The session token id.
     * @return The artifact corresponding to the given activity.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getArtifactForActivity/{processInstanceId}/{activityId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Artifact getArtifactForActivity(
            @PathVariable(RestConstants.PROCESS_INSTANCE_ID) String processInstanceId,
            @PathVariable(RestConstants.ACTIVITY_ID) String activityId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getArtifactForActivity", "127.0.0.1", sessionId);
            return aem.getArtifactForActivity(processInstanceId, activityId);
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
     * Given an activity definition, returns the artifact definition associated to it.
     * @param processDefinitionId The id of the process the activity is related to.
     * @param activityDefinitionId The id of the activity.
     * @param sessionId The session token id.
     * @return An object containing the artifact definition.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getArtifactDefinitionForActivity/{processDefinitionId}/{activityDefinitionId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ArtifactDefinition getArtifactDefinitionForActivity(
            @PathVariable(RestConstants.PROCESS_DEFINITION_ID) String processDefinitionId,
            @PathVariable(RestConstants.ACTIVITY_DEFINITION_ID) String activityDefinitionId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getArtifactDefinitionForActivity", "127.0.0.1", sessionId);
            return aem.getArtifactDefinitionForActivity(processDefinitionId, activityDefinitionId);
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
     * Saves the artifact generated once an activity has been completed (for example, the user filled in a form). 
     * @param processInstanceId The process instance the activity belongs to.
     * @param activityDefinitionId The activity id.
     * @param artifact The artifact to be saved.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "commitActivity/{processInstanceId}/{activityDefinitionId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void commitActivity(
            @PathVariable(RestConstants.PROCESS_INSTANCE_ID) String processInstanceId,
            @PathVariable(RestConstants.ACTIVITY_DEFINITION_ID) String activityDefinitionId,
            @RequestBody TransientArtifact artifact,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("commitActivity", "127.0.0.1", sessionId);
            aem.commitActivity(
                    processInstanceId,
                    activityDefinitionId,
                    RestUtil.transientArtifactToArtifact(artifact)
            );
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
     * Updates the artifact generated once an activity has been completed (for example, the user filled in a form). 
     * @param processInstanceId The process instance the activity belongs to.
     * @param activityDefinitionId The activity id.
     * @param artifact The artifact to be saved.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateActivity/{processInstanceId}/{activityDefinitionId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateActivity(
            @PathVariable(RestConstants.PROCESS_INSTANCE_ID) String processInstanceId,
            @PathVariable(RestConstants.ACTIVITY_DEFINITION_ID) String activityDefinitionId,
            @RequestBody TransientArtifact artifact,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateActivity", "127.0.0.1", sessionId);
            aem.updateActivity(
                    processInstanceId,
                    activityDefinitionId,
                    RestUtil.transientArtifactToArtifact(artifact)
            );
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
     * Gets Process Instance Activities Path.
     * @param processInstanceId Process Instance Id to get path.
     * @param sessionId The session token id.
     * @return The activity definition.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getProcessInstanceActivitiesPath/{processInstanceId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ActivityDefinition> getProcessInstanceActivitiesPath(
            @PathVariable(RestConstants.PROCESS_INSTANCE_ID) String processInstanceId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getProcessInstanceActivitiesPath", "127.0.0.1", sessionId);
            return aem.getProcessInstanceActivitiesPath(processInstanceId);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (InventoryException ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Requests for the next activity to be executed in a process instance.
     * @param processInstanceId The running process to get the next activity from.
     * @param sessionId The session token id.
     * @return The activity definition.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getNextActivityForProcessInstance/{processInstanceId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ActivityDefinition getNextActivityForProcessInstance(
            @PathVariable(RestConstants.PROCESS_INSTANCE_ID) String processInstanceId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getNextActivityForProcessInstance", "127.0.0.1", sessionId);
            return aem.getNextActivityForProcessInstance(processInstanceId);
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
     * Retrieves a process definition.
     * @param processDefinitionId The id of the process.
     * @param sessionId The session token id.
     * @return The process definition. It contains an XML document to be parsed by the consumer.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getProcessDefinition/{processDefinitionId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ProcessDefinition getProcessDefinition(
            @PathVariable(RestConstants.PROCESS_DEFINITION_ID) String processDefinitionId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getProcessDefinition", "127.0.0.1", sessionId);
            return aem.getProcessDefinition(processDefinitionId);
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
     * Retrieves an activity definition.
     * @param processDefinitionId The id of the process definition.
     * @param activityDefinitionId The id of the activity definition.
     * @param sessionId The session token id.
     * @return The activity definition.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getActivityDefinition/{processDefinitionId}/{activityDefinitionId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ActivityDefinition getActivityDefinition(
            @PathVariable(RestConstants.PROCESS_DEFINITION_ID) String processDefinitionId,
            @PathVariable(RestConstants.ACTIVITY_DEFINITION_ID) String activityDefinitionId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getActivityDefinition", "127.0.0.1", sessionId);
            return aem.getActivityDefinition(processDefinitionId, activityDefinitionId);
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
     * Deletes a process definition.
     * @param processDefinitionId The process definition to be deleted.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteProcessDefinition/{processDefinitionId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteProcessDefinition(
            @PathVariable(RestConstants.PROCESS_DEFINITION_ID) String processDefinitionId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteProcessDefinition", "127.0.0.1", sessionId);
            aem.deleteProcessDefinition(processDefinitionId);
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
     * Updates a process definition, either its standard properties or its structure.
     * @param processDefinitionId The process definition id.
     * @param properties A key value dictionary with the standard properties to be updated. These properties are: name, description, version and enabled (use 'true' or 'false' for the latter).
     * @param structure The structure of the process definition as string Base64, from an XML document that represents a BPMN process definition
     * <pre>
     * {@code
     *  <processDefinition version="" id="" name="" description="" creationDate="" startActivityId="" enabled="">
     *      <actors>
     *          <actor id="" name="" type=""/>
     *      </actors>
     *      <activityDefinitions>
     *         <activityDefinition id="" name="" description="" actorId="" type="">
     *              <paths>
     *                  <path>value</path>
     *              </paths>
     *              <artifactDefinition id="" type="">
     *                  <parameters>
     *                      <parameter name="">value</parameter>
     *                  </parameters>
     *              </artifactDefinition>
     *         </activityDefinition>
     *      </activityDefinitions>
     *      <bpmnDiagram>
     *          <bpmnSwimlane bpmnElement="" width="" height="" x="" y=""/>
     *          <bpmnShape id="" actorId="" width="" height="" x="" y="" isLabel="" type="">value</bpmnShape>
     *          <bpmnShape id="" actorId="" width="" height="" x="" y="" isLabel="" type="">value</bpmnShape>
     *          <bpmnEdge source="" target="" name=""/>
     *      </bpmnDiagram>
     *  </processDefinition>
     * }
     * </pre>
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateProcessDefinition/{processDefinitionId}/{structure}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateProcessDefinition(
            @PathVariable(RestConstants.PROCESS_DEFINITION_ID) String processDefinitionId,
            @RequestBody List<StringPair> properties,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateProcessDefinition", "127.0.0.1", sessionId);
            if (RestUtil.isBase64(structure)) {
                byte [] processStructure =  Base64.decodeBase64(structure);
                aem.updateProcessDefinition(processDefinitionId, properties, processStructure);
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","updateProcessDefinition"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","updateProcessDefinition"));
            }
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(ex.getStatus(), ex.getMessage());
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
     * Creates a process definition.A process definition is the metadata that defines the steps and constraints of a given project.
     * @param name The name of the new process definition.
     * @param description The description of the new process definition.
     * @param version The version of the new process definition. This is a three numbers, dot separated string (e.g. 2.4.1).
     * @param enabled If the project is enabled to create instances from it.
     * @param structure The structure of the process definition as string Base64, from an XML document that represents a BPMN process definition
     * <pre>
     * {@code
     *  <processDefinition version="" id="" name="" description="" creationDate="" startActivityId="" enabled="">
     *      <actors>
     *          <actor id="" name="" type=""/>
     *      </actors>
     *      <activityDefinitions>
     *         <activityDefinition id="" name="" description="" actorId="" type="">
     *              <paths>
     *                  <path>value</path>
     *              </paths>
     *              <artifactDefinition id="" type="">
     *                  <parameters>
     *                      <parameter name="">value</parameter>
     *                  </parameters>
     *              </artifactDefinition>
     *         </activityDefinition>
     *      </activityDefinitions>
     *      <bpmnDiagram>
     *          <bpmnSwimlane bpmnElement="" width="" height="" x="" y=""/>
     *          <bpmnShape id="" actorId="" width="" height="" x="" y="" isLabel="" type="">value</bpmnShape>
     *          <bpmnShape id="" actorId="" width="" height="" x="" y="" isLabel="" type="">value</bpmnShape>
     *          <bpmnEdge source="" target="" name=""/>
     *      </bpmnDiagram>
     *  </processDefinition>
     * }
     * </pre>
     * @param sessionId The session token id.
     * @return The id of the newly created process definition.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createProcessDefinition/{name}/{description}/{version}/{enabled}/{structure}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createProcessDefinition(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.VERSION) String version,
            @PathVariable(RestConstants.ENABLED) boolean enabled,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createProcessDefinition", "127.0.0.1", sessionId);
            if (RestUtil.isBase64(structure)) {
                byte [] processStructure =  Base64.decodeBase64(structure);
                return aem.createProcessDefinition(name, description, version, enabled, processStructure);
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","updateProcessDefinition"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","updateProcessDefinition"));
            }
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(ex.getStatus(), ex.getMessage());
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
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
     * Gets a process instances of a process definition.
     * @param processDefinitionId The process definition id.
     * @param sessionId The session token id.
     * @return The process instances.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getProcessInstances/{processDefinitionId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ProcessInstance> getProcessInstances(
            @PathVariable(RestConstants.PROCESS_DEFINITION_ID) String processDefinitionId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getProcessInstances", "127.0.0.1", sessionId);
            return aem.getProcessInstances(processDefinitionId);
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
     * Gets a process definition instances.
     * @param sessionId The session token id.
     * @return The process instances.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getProcessDefinitions/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ProcessDefinition> getProcessDefinitions(
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getProcessDefinitions", "127.0.0.1", sessionId);
            return aem.getProcessDefinitions();
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
     * Gets a process instance.
     * @param processInstanceId Process Instance Id.
     * @param sessionId The session token id.
     * @return A Process Instance for the given id.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getProcessInstance/{processInstanceId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ProcessInstance getProcessInstance(
            @PathVariable(RestConstants.PROCESS_INSTANCE_ID) String processInstanceId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getProcessInstance", "127.0.0.1", sessionId);
            return aem.getProcessInstance(processInstanceId);
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
     * Updates the process definitions.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "reloadProcessDefinitions/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void reloadProcessDefinitions(
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("reloadProcessDefinitions", "127.0.0.1", sessionId);
            aem.reloadProcessDefinitions();
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
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
     * Creates an instance of a process, that is, starts one.
     * @param processDefinitionId The id of the process to be started.
     * @param name The name of the new process.
     * @param description The description of the new process.
     * @param sessionId The session token id.
     * @return The id of the newly created process instance.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createProcessInstance/{processDefinitionId}/{name}/{description}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createProcessInstance(
            @PathVariable(RestConstants.PROCESS_DEFINITION_ID) String processDefinitionId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createProcessInstance", "127.0.0.1", sessionId);
            return aem.createProcessInstance(processDefinitionId, name, description);
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
     * Updates the process instance name and description.
     * @param processInstanceId The process instance id.
     * @param name The process instance name to update.
     * @param description The process instance description to update.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateProcessInstance/{processInstanceId}/{name}/{description}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateProcessInstance(
            @PathVariable(RestConstants.PROCESS_INSTANCE_ID) String processInstanceId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateProcessInstance", "127.0.0.1", sessionId);
            aem.updateProcessInstance(processInstanceId, name, description);
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
     * Deletes a process instance.
     * @param processInstanceId Process Instance Id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteProcessInstance/{processInstanceId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteProcessInstance(
            @PathVariable(RestConstants.PROCESS_INSTANCE_ID) String processInstanceId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteProcessInstance", "127.0.0.1", sessionId);
            aem.deleteProcessInstance(processInstanceId);
        } catch (OperationNotPermittedException ex) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    // </editor-fold>
}