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
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ConfigurationVariable;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
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
 * Variables Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(VariablesRestController.PATH)
public class VariablesRestController implements VariablesRestOpenApi {
    
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
    public static Logger LOG = LoggerFactory.getLogger(VariablesRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/configuration-variables/"; //NOI18N
    
    // <editor-fold desc="configuration-variables" defaultstate="collapsed">
    
    /**
     * Creates a configuration variable inside a pool. A configuration variable is a place where a value will be stored so it can retrieved by whomever need it. These variables are typically used to store values that help other modules to work, such as URLs, user names, dimensions, etc.
     * @param poolId The id of the pool where the configuration variable will be put.
     * @param name The name of the pool. This value can not be null or empty. Duplicate variable names are not allowed.
     * @param description The description of the what the variable does.
     * @param type The type of the variable. Use 1 for number, 2 for strings, 3 for booleans, 4 for unidimensional arrays and 5 for matrixes.
     * @param masked If the value should be masked when rendered (for security reasons, for example).
     * @param value In most cases (primitive types like numbers, strings or booleans) will be the actual value of the variable as a string (for example "5" or "admin" or "true"). For arrays and matrixes use the following notation: <br> 
     * Arrays: (value1,value2,value3,valueN), matrixes: [(row1col1, row1col2,... row1colN), (row2col1, row2col2,... row2colN), (rowNcol1, rowNcol2,... rowNcolN)]. The values will be interpreted as strings
     * @param sessionId The session token id.
     * @return The id of the newly created variable.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createConfigurationVariable/{poolId}/{name}/{description}/{type}/{masked}/{value}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createConfigurationVariable(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.TYPE) int type,
            @PathVariable(RestConstants.MASKED) boolean masked,
            @PathVariable(RestConstants.VALUE) String value,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createConfigurationVariable", "127.0.0.1", sessionId);
            return aem.createConfigurationVariable(poolId, name, description, type, masked, value);
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
     * Creates a pool of configuration variables.
     * @param name The name of the pool. Empty or null values are not allowed.
     * @param description The description of the pool.
     * @param sessionId The session token id.
     * @return The id of the newly created pool.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createConfigurationVariablesPool/{name}/{description}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createConfigurationVariablesPool(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createConfigurationVariablesPool", "127.0.0.1", sessionId);
            return aem.createConfigurationVariablesPool(name, description);
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
     * Updates the value of a configuration variable.See #{@link #createConfigurationVariable(long, java.lang.String, java.lang.String, int, boolean, java.lang.String)} for value definition syntax.
     * @param name The current name of the variable that will be modified.
     * @param property The name of the property to be updated. Possible values are: "name", "description", "type", "masked" and "value".
     * @param value The new value as string.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateConfigurationVariable/{name}/{property}/{value}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateConfigurationVariable(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.PROPERTY) String property,
            @PathVariable(RestConstants.VALUE) String value,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateConfigurationVariable", "127.0.0.1", sessionId);
            aem.updateConfigurationVariable(name, property, value);
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
     * Updates an attribute of a given configuration variables pool.
     * @param poolId The id of the pool to update.
     * @param property The property to update. The valid values are "name" and "description".
     * @param value The value of the property to be updated.
     * @param userName The session token user name.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateConfigurationVariablesPool/{poolId}/{property}/{value}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateConfigurationVariablesPool(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.PROPERTY) String property,
            @PathVariable(RestConstants.VALUE) String value,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateConfigurationVariablesPool", "127.0.0.1", sessionId);
            aem.updateConfigurationVariablesPool(poolId, property, value, userName);
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
     * Retrieves a configuration variable.
     * @param name The name of the variable to be retrieved.
     * @param sessionId The session token id.
     * @return The variable.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getConfigurationVariable/{name}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ConfigurationVariable getConfigurationVariable(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getConfigurationVariable", "127.0.0.1", sessionId);
            return aem.getConfigurationVariable(name);
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
     * Retrieves only the value of a configuration variable. Masked values are returned as null.
     * @param name The name of the variable. Masked values are returned as null.
     * @param sessionId The session token id.
     * @return The value of the variable as a java object/data type. The numbers are returned as floats.
     * The arrays and matrixes are returned as <code>ArrayList{@literal <String>}</code> and <code>ArrayList<ArrayList{@literal <String>}</code> instances respectively.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getConfigurationVariableValue/{name}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Object getConfigurationVariableValue(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getConfigurationVariableValue", "127.0.0.1", sessionId);
            return aem.getConfigurationVariableValue(name);
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
     * Gets the configuration variables in a configuration variable pool.
     * @param poolId The id pool to retrieve the variables from.
     * @param sessionId The session token id.
     * @return The list of configuration variables in the given pool.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getConfigurationVariablesInPool/{poolId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ConfigurationVariable> getConfigurationVariablesInPool(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getConfigurationVariablesInPool", "127.0.0.1", sessionId);
            return aem.getConfigurationVariablesInPool(poolId);
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
     * Gets the configuration variables with a given prefix.
     * @param prefix The prefix of the variables name.
     * @param sessionId The session token id.
     * @return The list of configuration variables with the given prefix.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getConfigurationVariablesWithPrefix/{prefix}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ConfigurationVariable> getConfigurationVariablesWithPrefix(
            @PathVariable(RestConstants.PREFIX) String prefix,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getConfigurationVariablesWithPrefix", "127.0.0.1", sessionId);
            return aem.getConfigurationVariablesWithPrefix(prefix);
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
     * Gets all the configuration variables in the database, no matter what pool they belong to.
     * @param sessionId The session token id.
     * @return The list of existing configuration variables.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getAllConfigurationVariables/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ConfigurationVariable> getAllConfigurationVariables(@PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getAllConfigurationVariables", "127.0.0.1", sessionId);
            return aem.getAllConfigurationVariables();
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
     * Retrieves the list of pools of configuration variables.
     * @param sessionId The session token id.
     * @return The available pools of configuration variables.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getConfigurationVariablesPools/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<InventoryObjectPool> getConfigurationVariablesPools(@PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getConfigurationVariablesPools", "127.0.0.1", sessionId);
            return aem.getConfigurationVariablesPools();
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
     * Gets the configuration variables of this manager.
     * @param sessionId The session token id.
     * @return A Properties object with the configuration variables.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getConfiguration/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Properties getConfiguration(@PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getConfiguration", "127.0.0.1", sessionId);
            return aem.getConfiguration();
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
     * Deletes a configuration variable.
     * @param name The name of the variable to be deleted.
     * @param userName The session token user name.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteConfigurationVariable/{name}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteConfigurationVariable(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteConfigurationVariable", "127.0.0.1", sessionId);
            aem.deleteConfigurationVariable(name, userName);
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
     * Deletes a configuration variables pool.Deleting a pool also deletes the configuration variables contained within.
     * @param poolId The id of the pool to be deleted.
     * @param userName The session token user name.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteConfigurationVariablesPool/{poolId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteConfigurationVariablesPool(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteConfigurationVariablesPool", "127.0.0.1", sessionId);
            aem.deleteConfigurationVariablesPool(poolId, userName);
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