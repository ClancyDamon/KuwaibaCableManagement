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
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
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
 * List Type Manager Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(ListTypeRestController.PATH)
public class ListTypeRestController implements ListTypeRestOpenApi {
    
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
    public static Logger LOG = LoggerFactory.getLogger(ListTypeRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/lt-manager/"; //NOI18N
    
    // <editor-fold desc="lt-manager" defaultstate="collapsed">
    /**
     * Retrieves all the items related to a given list type.
     * @param className List type item class name.
     * @param sessionId The session token.
     * @return A list of RemoteBusinessObjectLight instances representing the items.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getListTypeItems/{className}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getListTypeItems(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getListTypeItems", "127.0.0.1", sessionId);
            return aem.getListTypeItems(className);
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
     * Retrieves list type item given its id.
     * @param className List type item class name.
     * @param id The id of the list type item.
     * @param sessionId The session token.
     * @return A RemoteBusinessObjectLight instance representing the item.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getListTypeItem/{className}/{id}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObject getListTypeItem(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getListTypeItem", "127.0.0.1", sessionId);
            return aem.getListTypeItem(className, id);
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
     * Retrieves a list type item given its name.
     * @param className List type item class name.
     * @param name The name of list type item.
     * @param sessionId The session token.
     * @return A RemoteBusinessObjectLight instance representing the item.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getListTypeItemWithName/{className}/{name}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getListTypeItemWithName(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getListTypeItemWithName", "127.0.0.1", sessionId);
            return aem.getListTypeItemWithName(className, name);
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
     * Get the possible list types.
     * @param sessionId The session token.
     * @return A list of ClassMetadataLight instances representing the possible list types.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getInstanceableListTypes/{sessionId}",
             produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getInstanceableListTypes(@PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
             aem.validateCall("getInstanceableListTypes", "127.0.0.1", sessionId);
            return aem.getInstanceableListTypes();
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
     * Creates a list type item.
     * @param className List type item class name.
     * @param name New item's name.
     * @param displayName New item's display name.
     * @param sessionId The session token.
     * @return New item's id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createListTypeItem/{className}/{name}/{displayName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createListTypeItem(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DISPLAY_NAME) String displayName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createListTypeItem", "127.0.0.1", sessionId);
            return aem.createListTypeItem(className, name, displayName);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (OperationNotPermittedException ex) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Updates a list type item. Formerly this functionality was provided by {@link BusinessEntityManager} <code>updateObject</code>, but the implementation was split in two methods.
     * @param className The class of the LTI to be updated. It must be a subclass of GenericObjectList.
     * @param id The id of the list type item to be updated.
     * @param attributes The attributes to be changed.
     * @param sessionId The session token.
     * @return The summary of the changes made.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateListTypeItem/{className}/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateListTypeItem(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.ID) String id,
            @RequestBody HashMap<String, String> attributes,
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("updateListTypeItem", "127.0.0.1", sessionId);
            return aem.updateListTypeItem(className, id, attributes);
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
     * Deletes a list type item.
     * @param className List type item class name.
     * @param id List type item id.
     * @param releaseRelationships Should the relationships be released.
     * @param sessionId The session token.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteListTypeItem/{className}/{id}/{releaseRelationships}/{sessionId}",
             produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteListTypeItem(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.RELEASE_RELATIONSHIPS) boolean releaseRelationships,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteListTypeItem", "127.0.0.1", sessionId);
            aem.deleteListTypeItem(className, id, releaseRelationships);
        } catch (InvalidArgumentException | OperationNotPermittedException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Release a list type item relationships.
     * @param id List type item id.
     * @param sessionId The session token.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "releaseListTypeItem/{id}/{sessionId}",
             produces = {MediaType.APPLICATION_JSON_VALUE})
    public void releaseListTypeItem(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("releaseListTypeItem", "127.0.0.1", sessionId);
            aem.releaseListTypeItem(id);
        } catch (InvalidArgumentException | OperationNotPermittedException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Retrieves the objects that make reference to a given list type item.
     * @param className The list type class.
     * @param id The list type item id.
     * @param limit The limit of results. Use -1 to retrieve all.
     * @param sessionId The session token.
     * @return The list of business objects related to the list type item.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getListTypeItemUses/{className}/{id}/{limit}/{sessionId}",
             produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getListTypeItemUses(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getListTypeItemUses", "127.0.0.1", sessionId);
            return aem.getListTypeItemUses(className, id, limit);
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
    //  </editor-fold>
}