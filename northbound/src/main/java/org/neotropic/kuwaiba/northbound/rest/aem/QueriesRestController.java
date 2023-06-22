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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.CompactQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ExtendedQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ResultRecord;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueriesPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryParameter;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryResult;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ExecutionException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.neotropic.kuwaiba.northbound.rest.RestUtil;
import org.neotropic.kuwaiba.northbound.rest.todeserialize.TransientScriptedQueryParameter;
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
 * Queries Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(QueriesRestController.PATH)
public class QueriesRestController implements QueriesRestOpenApi {
    
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
    public static Logger LOG = LoggerFactory.getLogger(QueriesRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/scripted-queries/"; //NOI18N
    
    // <editor-fold desc="scripted-queries" defaultstate="collapsed">
    
    /**
     * Creates a scripted queries pool.
     * @param name Scripted queries pool name.
     * @param description Scripted queries pool description.
     * @param sessionId The session token id.
     * @return The scripted queries pool id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createScriptedQueriesPool/{name}/{description}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createScriptedQueriesPool(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createScriptedQueriesPool", "127.0.0.1", sessionId);
            return aem.createScriptedQueriesPool(name, description);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ExecutionException ex) {
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
     * Updates a scripted queries pool.
     * @param id Scripted queries pool id.
     * @param name Scripted queries pool name.
     * @param description Scripted queries pool description.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateScriptedQueriesPool/{id}/{name}/{description}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateScriptedQueriesPool(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateScriptedQueriesPool", "127.0.0.1", sessionId);
            aem.updateScriptedQueriesPool(id, name, description);
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
     * Deletes a scripted queries pool.
     * @param id Scripted queries pool id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteScriptedQueriesPool/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteScriptedQueriesPool(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteScriptedQueriesPool", "127.0.0.1", sessionId);
            aem.deleteScriptedQueriesPool(id);
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
     * Gets a scripted queries pool given the name.
     * @param name Scripted queries pool name.
     * @param sessionId The session token id.
     * @return A scripted queries pool.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueriesPoolByName/{name}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ScriptedQueriesPool getScriptedQueriesPoolByName(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQueriesPoolByName", "127.0.0.1", sessionId);
            return aem.getScriptedQueriesPoolByName(name);
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
     * Gets a scripted queries pool.
     * @param id Scripted queries pool id.
     * @param sessionId The session token id.
     * @return A scripted queries pool.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueriesPool/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ScriptedQueriesPool getScriptedQueriesPool(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQueriesPool", "127.0.0.1", sessionId);
            return aem.getScriptedQueriesPool(id);
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
     * Counts the scripted queries pools.
     * @param filterName Scripted queries pool name to filter.
     * @param sessionId The session token id.
     * @return The size of scripted queries pools.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueriesPoolCount/{filterName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public int getScriptedQueriesPoolCount(
            @PathVariable(RestConstants.FILTER_NAME) String filterName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQueriesPoolCount", "127.0.0.1", sessionId);
            return aem.getScriptedQueriesPoolCount(filterName);
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
     * Gets a set of scripted queries pools.
     * @param filterName Filter by scripted queries pool name.
     * @param skip Result skip.
     * @param limit Result limit.
     * @param sessionId The session token id.
     * @return A set of scripted queries pools.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueriesPools/{filterName}/{skip}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ScriptedQueriesPool> getScriptedQueriesPools(
            @PathVariable(RestConstants.FILTER_NAME) String filterName,
            @PathVariable(RestConstants.SKIP) int skip,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQueriesPools", "127.0.0.1", sessionId);
            return aem.getScriptedQueriesPools(filterName, skip, limit);
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
     * Counts the scripted queries filter by name in a scripted queries pool.
     * @param id Scripted queries pool id.
     * @param filterName Filter by scripted query name.
     * @param ignoreDisabled True to ignore disabled scripted queries.
     * @param sessionId The session token id.
     * @return The size of scripted queries.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueryCountByPoolId/{id}/{filterName}/{ignoreDisabled}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public int getScriptedQueryCountByPoolId(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.FILTER_NAME) String filterName,
            @PathVariable(RestConstants.IGNORE_DISABLED) boolean ignoreDisabled,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQueryCountByPoolId", "127.0.0.1", sessionId);
            return aem.getScriptedQueryCountByPoolId(id, filterName, ignoreDisabled);
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
     * Gets a set of scripted queries in a scripted queries pool the its id.
     * @param id Scripted queries pool id.
     * @param filterName Scripted query name filter.
     * @param ignoreDisabled True to return all scripted queries. False to return the enabled only.
     * @param skip Result skip.
     * @param limit Result limit.
     * @param sessionId The session token id.
     * @return A set of scripted queries in a scripted queries pool.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueriesByPoolId/{id}/{filterName}/{ignoreDisabled}/{skip}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ScriptedQuery> getScriptedQueriesByPoolId(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.FILTER_NAME) String filterName,
            @PathVariable(RestConstants.IGNORE_DISABLED) boolean ignoreDisabled,
            @PathVariable(RestConstants.SKIP) int skip,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQueriesByPoolId", "127.0.0.1", sessionId);
            return aem.getScriptedQueriesByPoolId(id, filterName, ignoreDisabled, skip, limit);
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
     * Counts the scripted queries filter by name in a scripted queries pool.
     * @param name Scripted queries pool name.
     * @param filterName Scripted query name to filter.
     * @param sessionId The session token id.
     * @return The size of scripted queries.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueryCountByPoolName/{name}/{filterName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public int getScriptedQueryCountByPoolName(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.FILTER_NAME) String filterName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQueryCountByPoolName", "127.0.0.1", sessionId);
            return aem.getScriptedQueryCountByPoolName(name, filterName);
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
     * Gets a set of scripted queries in a scripted queries pool the its name.
     * @param name Scripted queries pool name.
     * @param filterName Scripted query name filter.
     * @param ignoreDisabled True to return all scripted queries. False to return the enabled only.
     * @param skip Result skip.
     * @param limit Result limit.
     * @param sessionId The session token id.
     * @return A set of scripted queries in a scripted queries pool.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueriesByPoolName/{name}/{filterName}/{ignoreDisabled}/{skip}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ScriptedQuery> getScriptedQueriesByPoolName(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.FILTER_NAME) String filterName,
            @PathVariable(RestConstants.IGNORE_DISABLED) boolean ignoreDisabled,
            @PathVariable(RestConstants.SKIP) int skip,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQueriesByPoolName", "127.0.0.1", sessionId);
            return aem.getScriptedQueriesByPoolName(name, filterName, ignoreDisabled, skip, limit);
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
     * Creates a scripted query.
     * @param id Scripted queries pool id.
     * @param name Scripted query name.
     * @param description Scripted query description.
     * @param script Scripted query script.
     * @param enabled True to enable the Scripted query.
     * @param sessionId The session token id.
     * @return The scripted query id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createScriptedQuery/{id}/{name}/{description}/{script}/{enabled}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createScriptedQuery(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.SCRIPT) String script,
            @PathVariable(RestConstants.ENABLED) boolean enabled,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createScriptedQuery", "127.0.0.1", sessionId);
            return aem.createScriptedQuery(id, name, description, script, enabled);
        } catch (InvalidArgumentException | ExecutionException ex) {
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
     * Updates a scripted query given its id.
     * @param id The scripted query id.
     * @param name New scripting query name.
     * @param description New scripting query description.
     * @param script New query script.
     * @param enabled New value, true to enable the scripting query.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateScriptedQuery/{id}/{name}/{description}/{script}/{enabled}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateScriptedQuery(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.SCRIPT) String script,
            @PathVariable(RestConstants.ENABLED) boolean enabled,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateScriptedQuery", "127.0.0.1", sessionId);
            aem.updateScriptedQuery(id, name, description, script, enabled);
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
     * Deletes an scripted query given the id.
     * @param id The scripting query id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteScriptedQuery/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteScriptedQuery(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteScriptedQuery", "127.0.0.1", sessionId);
            aem.deleteScriptedQuery(id);
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
     * Gets a scripted query given its id.
     * @param id The scripted query id.
     * @param sessionId The session token id.
     * @return A scripted query.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQuery/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ScriptedQuery getScriptedQuery(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQuery", "127.0.0.1", sessionId);
            return aem.getScriptedQuery(id);
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
     * Counts the scripted queries.
     * @param filterName Scripted query name to filter.
     * @param sessionId The session token id.
     * @return The size of scripted queries.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueryCount/{filterName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public int getScriptedQueryCount(
            @PathVariable(RestConstants.FILTER_NAME) String filterName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQueryCount", "127.0.0.1", sessionId);
            return aem.getScriptedQueryCount(filterName);
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
     * Gets a set of scripted queries.
     * @param filterName Scripted query name to filter.
     * @param ignoreDisabled True to return all scripted queries. False to return the enabled only.
     * @param skip Result skip.
     * @param limit Result limit.
     * @param sessionId The session token id.
     * @return A set of scripted queries.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueries/{filterName}/{ignoreDisabled}/{skip}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ScriptedQuery> getScriptedQueries(
            @PathVariable(RestConstants.FILTER_NAME) String filterName,
            @PathVariable(RestConstants.IGNORE_DISABLED) boolean ignoreDisabled,
            @PathVariable(RestConstants.SKIP) int skip,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQueries", "127.0.0.1", sessionId);
            return aem.getScriptedQueries(filterName, ignoreDisabled, skip, limit);
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
     * Executes the scripted query.
     * @param id The scripted query class id.
     * @param parameters The scripted query parameters.
     * @param sessionId The session token id.
     * @return The result of execute the scripted query.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "executeScriptedQuery/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ScriptedQueryResult executeScriptedQuery(
            @PathVariable(RestConstants.ID) String id,
            @RequestBody List<TransientScriptedQueryParameter> parameters,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("executeScriptedQuery", "127.0.0.1", sessionId);
            List<ScriptedQueryParameter> listParameters = new ArrayList<>();
            parameters.forEach(param -> {
                ScriptedQueryParameter scriptedQueryParameter = RestUtil.transientScriptedQueryParameterToScriptedQueryParameter(param);
                if (scriptedQueryParameter != null)
                    listParameters.add(scriptedQueryParameter);
            });
            return aem.executeScriptedQuery(id, listParameters.toArray(new ScriptedQueryParameter[0]));
        } catch (InvalidArgumentException | ExecutionException ex) {
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
     * Creates a scripted query parameter.
     * @param id The scripted query id.
     * @param name The scripted query parameter name.
     * @param description The scripted query parameter description.
     * @param type The scripted query parameter type.
     * @param mandatory True scripted query parameter is mandatory.
     * @param defaultValue The scripted query parameter default value.
     * @param sessionId The session token id.
     * @return The scripted query parameter id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createScriptedQueryParameter/{id}/{name}/{description}/{type}/{mandatory}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createScriptedQueryParameter(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.TYPE) String type,
            @PathVariable(RestConstants.MANDATORY) boolean mandatory,
            @RequestBody Object defaultValue,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createScriptedQueryParameter", "127.0.0.1", sessionId);
            return aem.createScriptedQueryParameter(id, name, description, type, mandatory, defaultValue);
        } catch (InvalidArgumentException | ExecutionException ex) {
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
     * Updates a scripted query parameter.
     * @param id The scripted query parameter id.
     * @param name The scripted query parameter name.
     * @param description The scripted query parameter description.
     * @param type The scripted query parameter type.
     * @param mandatory True if the scripted query parameter is mandatory.
     * @param defaultValue The scripted query parameter default value.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateScriptedQueryParameter/{id}/{name}/{description}/{type}/{mandatory}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateScriptedQueryParameter(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.TYPE) String type,
            @PathVariable(RestConstants.MANDATORY) boolean mandatory,
            @RequestBody Object defaultValue,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateScriptedQueryParameter", "127.0.0.1", sessionId);
            aem.updateScriptedQueryParameter(id, name, description, type, mandatory, defaultValue);
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
     * Deletes a scripted query parameter.
     * @param id The id of the scripted query parameter to delete.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteScriptedQueryParameter/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteScriptedQueryParameter(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteScriptedQueryParameter", "127.0.0.1", sessionId);
            aem.deleteScriptedQueryParameter(id);
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
     * Gets an scripted query parameter.
     * @param id The scripted query parameter id.
     * @param sessionId The session token id.
     * @return The scripted query parameter.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueryParameter/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ScriptedQueryParameter getScriptedQueryParameter(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQueryParameter", "127.0.0.1", sessionId);
            return aem.getScriptedQueryParameter(id);
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
     * Gets the scripted query parameters.
     * @param id The scripted query id.
     * @param sessionId The session token id.
     * @return The scripted query parameters.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getScriptedQueryParameters/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ScriptedQueryParameter> getScriptedQueryParameters(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getScriptedQueryParameters", "127.0.0.1", sessionId);
            return aem.getScriptedQueryParameters(id);
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
     * Creates a Query.
     * @param name The name of the query.
     * @param ownerId The if of the user that will own the query. Use -1 to make it public.
     * @param structure The structure of the query as string Base64, from the XML document:
     * <pre>
     * {@code
     *  <query version="" logicalconnector="" limit="">
     *      <class name="">
     *          <visibleattributes>
     *              <attribute name=""> </attribute>
     *          </visibleattributes>
     *          <filters>
     *              <filter attribute="" condition=""> </filter>
     *          </filters>
     *      </class>
     *  </query>
     * }
     * </pre>
     * @param description The description of the query.
     * @param sessionId The session token id.
     * @return The id of the newly created query.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createQuery/{name}/{ownerId}/{structure}/{description}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createQuery(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.OWNER_ID) long ownerId,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createQuery", "127.0.0.1", sessionId);
            if (RestUtil.isBase64(structure)) {
                byte [] queryStructure =  Base64.decodeBase64(structure);
                return aem.createQuery(name, ownerId, queryStructure, description);
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","createQuery"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","createQuery"));
            }
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(ex.getStatus(), ex.getMessage());
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
     * Updates an existing query.
     * @param id The id of the query.
     * @param name The name of the query. Leave null to keep the old value.
     * @param ownerId The id of the user that owns this query. Use -1 to keep the old value.
     * @param structure The structure of the query as string Base64, from an XML document
     * <pre>
     * {@code
     *  <query version="" logicalconnector="" limit="">
     *      <class name="">
     *          <visibleattributes>
     *              <attribute name=""> </attribute>
     *          </visibleattributes>
     *          <filters>
     *              <filter attribute="" condition=""> </filter>
     *          </filters>
     *      </class>
     *  </query>
     * }
     * </pre>
     * @param description The description of the query. Leave null to keep the old value.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "saveQuery/{id}/{name}/{ownerId}/{structure}/{description}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor saveQuery(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.OWNER_ID) long ownerId,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("saveQuery", "127.0.0.1", sessionId);
            if (RestUtil.isBase64(structure)) {
                byte [] queryStructure =  Base64.decodeBase64(structure);
                return aem.saveQuery(id, name, ownerId, queryStructure, description);
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","saveQuery"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","saveQuery"));
            }
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(ex.getStatus(), ex.getMessage());
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
     * Deletes a Query.
     * @param id The id of the query.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteQuery/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteQuery(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteQuery", "127.0.0.1", sessionId);
            aem.deleteQuery(id);
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
     * Gets all queries.
     * @param showPublic Include public queries or show only the private ones.
     * @param sessionId The session token id.
     * @return The list of queries.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getQueries/{showPublic}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<CompactQuery> getQueries(
            @PathVariable(RestConstants.SHOW_PUBLIC) boolean showPublic,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getQueries", "127.0.0.1", sessionId);
            return aem.getQueries(showPublic);
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
     * Gets a single query.
     * @param id The id of the query.
     * @param sessionId The session token id.
     * @return The query as an object.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getQuery/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public CompactQuery getQuery(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getQuery", "127.0.0.1", sessionId);
            return aem.getQuery(id);
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
     * Used to perform complex queries.
     * Please note that the first record is reserved for the column headers, so and empty result set will have at least one record.
     * @param query The code-friendly representation of the query made using the graphical query builder.
     * @param sessionId The session token id.
     * @return A set of objects matching the specified criteria as ResultRecord array.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "executeQuery/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ResultRecord> executeQuery(
            @RequestBody ExtendedQuery query,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("executeQuery", "127.0.0.1", sessionId);
            return aem.executeQuery(query);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException ex) {
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