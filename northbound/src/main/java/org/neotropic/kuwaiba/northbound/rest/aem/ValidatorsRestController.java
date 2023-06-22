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
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The definition of the Validator Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(ValidatorsRestController.PATH)
public class ValidatorsRestController implements ValidatorsRestOpenApi {
    
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
    public static Logger LOG = LoggerFactory.getLogger(ValidatorsRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/configuration-validators/"; //NOI18N
    
    // <editor-fold desc="configuration-validators" defaultstate="collapsed">
    
    /**
     * Creates a validator definition. 
     * @param name The name of the validator. It's recommended to use camel case notation (for example thisIsAName). This field is mandatory
     * @param description The optional description of the validator.
     * @param className The class or super class of the classes whose instances will be checked against this validator.
     * @param script The groovy script containing the logic of the validator , that is, the.
     * @param enabled If this validador should be applied or not.
     * @param userName The session token user name.
     * @param sessionId The session token id.
     * @return The id of the newly created validator definition.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "createValidatorDefinition/{name}/{description}/{className}/{script}/{enabled}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createValidatorDefinition(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SCRIPT) String script,
            @PathVariable(RestConstants.ENABLED) boolean enabled,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createValidatorDefinition", "127.0.0.1", sessionId);
            return aem.createValidatorDefinition(name, description, className, script, enabled, userName);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Updates the properties of a validator. The null values will be ignored.
     * @param id The id of teh validator definition to be updated.
     * @param name The new name, if any, null otherwise.
     * @param description The new description, if any, null otherwise.
     * @param className The new class to be associated to this validator, if any, null otherwise.
     * @param script The new script, if any, null otherwise.
     * @param enabled If the validator should be enabled or not, if any, null otherwise.
     * @param userName The session token user name.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateValidatorDefinition/{id}/{name}/{description}/{className}/{script}/{enabled}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateValidatorDefinition(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SCRIPT) String script,
            @PathVariable(RestConstants.ENABLED) boolean enabled,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateValidatorDefinition", "127.0.0.1", sessionId);
            aem.updateValidatorDefinition(id, name,description, className, script, enabled, userName);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Retrieves all the validator definitions in the system.
     * @param className The class to retrieve the validator definitions from.
     * @param sessionId The session token id.
     * @return The list of validator definitions.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getValidatorDefinitionsForClass/{className}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ValidatorDefinition> getValidatorDefinitionsForClass(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getValidatorDefinitionsForClass", "127.0.0.1", sessionId);
            return aem.getValidatorDefinitionsForClass(className);
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
     * Gets all validator definitions, no matter what class they are related to.
     * @param sessionId The session token id.
     * @return The list of validators.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getAllValidatorDefinitions/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ValidatorDefinition> getAllValidatorDefinitions(@PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getAllValidatorDefinitions", "127.0.0.1", sessionId);
            return aem.getAllValidatorDefinitions();
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
     * Runs the existing validations for the class associated to the given object. Validators set to enabled = false will be ignored.
     * @param className The class of the object.
     * @param id The id of the object.
     * @param sessionId The session token id.
     * @return The list of validators associated to the object and its class.
     */
    @RequestMapping(method = RequestMethod.POST, value = "runValidationsForObject/{className}/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Validator> runValidationsForObject(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("runValidationsForObject", "127.0.0.1", sessionId);
            return aem.runValidationsForObject(className, id);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (BusinessObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Deletes a validator definition.
     * @param id The id of the validator to be deleted.
     * @param userName The session token user name.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteValidatorDefinition/{id}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteValidatorDefinition(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteValidatorDefinition", "127.0.0.1", sessionId);
            aem.deleteValidatorDefinition(id, userName);
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
    //  </editor-fold>
}