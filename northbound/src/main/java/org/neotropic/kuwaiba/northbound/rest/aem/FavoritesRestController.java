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
import org.neotropic.kuwaiba.core.apis.persistence.application.FavoritesFolder;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
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
 * The definition of the Favorites Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(FavoritesRestController.PATH)
public class FavoritesRestController implements FavoritesRestOpenApi {
    
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
    public static org.slf4j.Logger LOG = LoggerFactory.getLogger(FavoritesRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/favorites/"; //NOI18N
    
    // <editor-fold desc="favorites" defaultstate="collapsed">
    
    /**
     * Adds an object to the favorites folder.
     * @param className Object class name.
     * @param objectId Object id.
     * @param folderId Favorites folder id.
     * @param userId User id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "addObjectTofavoritesFolder/{className}/{objectId}/{folderId}/{userId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addObjectTofavoritesFolder(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.FOLDER_ID) long folderId,
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("addObjectTofavoritesFolder", "127.0.0.1", sessionId);
            aem.addObjectTofavoritesFolder(className, objectId, folderId, userId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
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
     * Removes an object associated to a favorites folder.
     * @param className Object class name.
     * @param objectId Object id.
     * @param folderId favorites folder id.
     * @param userId User id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "removeObjectFromfavoritesFolder/{className}/{objectId}/{folderId}/{userId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removeObjectFromfavoritesFolder(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.FOLDER_ID) long folderId,
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("removeObjectFromfavoritesFolder", "127.0.0.1", sessionId);
            aem.removeObjectFromfavoritesFolder(className, objectId, folderId, userId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Create a relationship between an user and a new favorites folder.
     * @param name Favorites folder name.
     * @param userId User id.
     * @param sessionId The session token id.
     * @return The new favorites folder id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createFavoritesFolderForUser/{name}/{userId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createFavoritesFolderForUser(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createFavoritesFolderForUser", "127.0.0.1", sessionId);
            return aem.createFavoritesFolderForUser(name, userId);
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
     * Delete a Bookmark Folder of an User.
     * @param ids Favorites folders id.
     * @param userId User id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteFavoritesFolders/{ids}/{userId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteFavoritesFolders(
            @PathVariable(RestConstants.IDS) long[] ids,
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteFavoritesFolders", "127.0.0.1", sessionId);
            aem.deleteFavoritesFolders(ids, userId);
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
     * Get the favorites folders create by an user.
     * @param userId user id.
     * @param sessionId The session token id.
     * @return List of Bookmarks folders for an User.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getFavoritesFoldersForUser/{userId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<FavoritesFolder> getFavoritesFoldersForUser(
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getFavoritesFoldersForUser", "127.0.0.1", sessionId);
            return aem.getFavoritesFoldersForUser(userId);
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
     * Get the object assigned to the bookmark.
     * @param folderId Favorites folder id.
     * @param userId User id.
     * @param limit Max number of results.
     * @param sessionId The session token id.
     * @return List of objects related to bookmark.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getObjectsInFavoritesFolder/{folderId}/{userId}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectsInFavoritesFolder(
            @PathVariable(RestConstants.FOLDER_ID) long folderId,
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectsInFavoritesFolder", "127.0.0.1", sessionId);
            return aem.getObjectsInFavoritesFolder(folderId, userId, limit);
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
     * Get the bookmarks where an object is associated.
     * @param userId User id.
     * @param className Object class name.
     * @param objectId Object id.
     * @param sessionId The session token id.
     * @return List of favorites folders where an object are an item.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getFavoritesFoldersForObject/{userId}/{className}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<FavoritesFolder> getFavoritesFoldersForObject(
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getFavoritesFoldersForObject", "127.0.0.1", sessionId);
            return aem.getFavoritesFoldersForObject(userId, className, objectId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Gets a favorites folder.
     * @param folderId Favorites folder id.
     * @param userId User id.
     * @param sessionId The session token id.
     * @return The favorite folder with id.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getFavoritesFolder/{folderId}/{userId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public FavoritesFolder getFavoritesFolder(
            @PathVariable(RestConstants.FOLDER_ID) long folderId,
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getFavoritesFolder", "127.0.0.1", sessionId);
            return aem.getFavoritesFolder(folderId, userId);
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
     * Updates a favorites folder.
     * @param folderId Favorites folder id
     * @param userId User id.
     * @param name Favorites folder name.
     * @param sessionId The favorite folder with id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateFavoritesFolder/{folderId}/{userId}/{name}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateFavoritesFolder(
            @PathVariable(RestConstants.FOLDER_ID) long folderId,
            @PathVariable(RestConstants.USER_ID) long userId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateFavoritesFolder", "127.0.0.1", sessionId);
            aem.updateFavoritesFolder(folderId, userId, name);
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