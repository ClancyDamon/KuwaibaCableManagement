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

import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FilterDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ScriptNotCompiledException;
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
 * Filters Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(FiltersRestController.PATH)
public class FiltersRestController implements FiltersRestOpenApi {

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
    public static Logger LOG = LoggerFactory.getLogger(FiltersRestController.class); 

    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/configuration-filters/"; //NOI18N  
    
    // <editor-fold desc="configuration-filters" defaultstate="collapsed">
    /**
     * Creates a filter. 
     * @param name The name of the filter. It's recommended to use camel case notation (for example thisIsAName). This field is mandatory.
     * @param description The optional description of the filter.
     * @param className The class or super class of the classes whose instances will be checked against this filter.
     * @param script The groovy script containing the logic of the filter , that is, the.
     * @param enabled If this filter should be applied or not.
     * @param sessionId The session token id.
     * @return The id of the newly created filter definition.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "createFilterDefinition/{name}/{description}/{className}/{script}/{enabled}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createFilterDefinition(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SCRIPT) String script,
            @PathVariable(RestConstants.ENABLED) boolean enabled,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createFilterDefinition", "127.0.0.1", sessionId);
            return aem.createFilterDefinition(name, description, className, script, enabled);
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
    
    /**
     * Updates the properties of a filter, null values will be ignored; when the 
     * script of the filter definition its been updated it will tries to compile
     * the script to create a Filter instance, if the compilation fails the 
     * filter attribute in the filter definition will remains null.
     * @param id The id of the filter definition to be updated.
     * @param name The new name, not null.
     * @param description The new description, if any, "null" otherwise.
     * @param className The new class to be associated to this filer, if any, "null" otherwise.
     * @param script The new script, if any, "null" otherwise.
     * @param enabled If the filer should be enabled or not, false by default.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateFilterDefinition/{id}/{name}/{description}/{className}/{script}/{enabled}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateFilterDefinition(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SCRIPT) String script,
            @PathVariable(RestConstants.ENABLED) boolean enabled,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateFilterDefinition", "127.0.0.1", sessionId);
            aem.updateFilterDefinition(
                    id,
                    name,
                    description.equals("null") ? null : description,
                    className.equals("null") ? null : className,
                    script.equals("null") ? null : script,
                    enabled
            );
        } catch (InvalidArgumentException | ScriptNotCompiledException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * For a given class retrieves all its filters definitions (also the filters of its parent classes could be included).
     * @param className The class to retrieve the filter definitions from.
     * @param includeParentClassesFilters If the return must include the filters of the parent class.
     * @param ignoreCache To avoid filters from the cache useful in filters module edition, or to get cached filters useful when retrieve filters to execute.
     * @param attributes Attributes to filter the FiltersDefinition null to not filter.
     * @param page Page the number of page of number of elements to skip, -1 to not skip any result.
     * @param limit Limit of results per page, -1 to retrieve all elements.
     * @param sessionId The session token id.
     * @return The list of filter definitions.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "getFilterDefinitionsForClass/{className}/{includeParentClassesFilters}/{ignoreCache}/{page}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<FilterDefinition> getFilterDefinitionsForClass(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.INCLUDE_PARENT_CLASSES_FILTERS) boolean includeParentClassesFilters,
            @PathVariable(RestConstants.IGNORE_CACHE) boolean ignoreCache,
            @RequestBody(required = false) HashMap<String, Object> attributes,
            @PathVariable(RestConstants.PAGE) int page,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getFilterDefinitionsForClass", "127.0.0.1", sessionId);
            return aem.getFilterDefinitionsForClass(className, includeParentClassesFilters, ignoreCache, attributes, page, limit);
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
     * For a given class name returns the count of the filters definitions.
     * @param className The class to retrieve the filter definitions from.
     * @param includeParentClassesFilters If the return must include the filters of the parent class.
     * @param ignoreCache To avoid filters from the cache useful in filters module edition, or to get cached filters useful when retrieve filters to execute.
     * @param attributes Attributes to filter the FiltersDefinition null to not filter, attributes to filter: onlyEnabled, filter's name.
     * onlyEnabled return only the enabled filters definitions, e.g for the router class, include filters created in class GenericNetworkElement.
     * @param page Page the number of page of number of elements to skip, -1 to not skip any result.
     * @param limit Limit of results per page, -1 to retrieve all elements.
     * @param sessionId The session token id.
     * @return The count of filters definitions.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "getFilterDefinitionsForClassCount/{className}/{includeParentClassesFilters}/{ignoreCache}/{page}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getFilterDefinitionsForClassCount(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.INCLUDE_PARENT_CLASSES_FILTERS) boolean includeParentClassesFilters,
            @PathVariable(RestConstants.IGNORE_CACHE) boolean ignoreCache,
            @RequestBody(required = false) HashMap<String, Object> attributes,
            @PathVariable(RestConstants.PAGE) int page,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getFilterDefinitionsForClassCount", "127.0.0.1", sessionId);
            return aem.getFilterDefinitionsForClassCount(className, includeParentClassesFilters, ignoreCache, attributes, page, limit);
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
     * Retrieves all the filters created in the inventory.
     * @param attributes Attributes to filter the FiltersDefinition null to not filter.
     * @param page Page the number of page of number of elements to skip, -1 to not skip any result.
     * @param limit Limit of results per page, -1 to retrieve all elements.
     * @param sessionId The session token id.
     * @return The list of filter definitions.
     */
    @RequestMapping(method = RequestMethod.POST, value = "getAllFilterDefinitions/{page}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<FilterDefinition> getAllFilterDefinitions(
            @RequestBody(required = false) HashMap<String, Object> attributes,
            @PathVariable(RestConstants.PAGE) int page,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getAllFilterDefinitions", "127.0.0.1", sessionId);
            return aem.getAllFilterDefinitions(attributes, page, limit);
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
     * Counts all the Filters definition created.
     * @param attributes Attributes to filter the count of FiltersDefinition, null to not filter.
     * @param sessionId The session token id.
     * @return The count of FiltersDefinitions.
     */
    @RequestMapping(method = RequestMethod.POST, value = "getAllFilterDefinitionsCount/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getAllFilterDefinitionsCount(
            @RequestBody(required = false) HashMap<String, Object> attributes,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getAllFilterDefinitionsCount", "127.0.0.1", sessionId);
            return aem.getAllFilterDefinitionsCount(attributes);
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
     * Deletes a filter definition.
     * @param id The id of the filter to be deleted.
     * @param className The class name of the filter to be deleted, used to remove the filter from the cache.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteFilterDefinition/{id}/{className}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteFilterDefinition(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteFilterDefinition", "127.0.0.1", sessionId);
            aem.deleteFilterDefinition(id, className);
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
    // </editor-fold>
}