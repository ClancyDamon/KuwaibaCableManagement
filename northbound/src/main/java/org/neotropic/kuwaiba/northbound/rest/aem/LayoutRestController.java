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
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.neotropic.kuwaiba.northbound.rest.RestUtil;
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
 * Layout Editor Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(LayoutRestController.PATH)
public class LayoutRestController implements LayoutRestOpenApi {
    
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
    public static Logger LOG = LoggerFactory.getLogger(LayoutRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/layout-editor/"; //NOI18N
    
    // <editor-fold desc="layout-editor" defaultstate="collapsed">
    
    /**
     * Creates a view for a given list type item. If there's already a view of the provided view type, it will be overwritten.
     * @param listTypeItemId List type item id.
     * @param listTypeItemClassName List type item class name.
     * @param viewClassName View class name.
     * @param name View name.
     * @param description View description.
     * @param structure The structure of the view as string Base64,from an XML document with the view structure
     * <pre>
     * {@code
     *  <view version="">
     *      <layout name="" x="" y="" width="" height="" type="">
     *          <shape type="" x="" y="" height="" width="" opaque=""
     *                 isEquipment="" name="" id="" className="" color="" borderColor="">
     *          </shape> 
     *          <shape ... >
     *          </shape>
     *          <shape ... >
     *          </shape>
     *          .
     *          .
     *          .
     *      </layout>
     *  </view>
     * }
     * </pre>
     * @param background The background image as string Base64. If any, "null" otherwise.
     * @param sessionId The session token id.
     * @return The id of the new view.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "createListTypeItemRelatedLayout/{listTypeItemId}/{listTypeItemClassName}/{viewClassName}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createListTypeItemRelatedLayout(
            @PathVariable(RestConstants.LIST_TYPE_ITEM_ID) String listTypeItemId,
            @PathVariable(RestConstants.LIST_TYPE_ITEM_CLASS_NAME) String listTypeItemClassName,
            @PathVariable(RestConstants.VIEW_CLASS_NAME) String viewClassName,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.BACKGROUND) String background,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createListTypeItemRelatedLayout", "127.0.0.1", sessionId);
            
            background = background.equals("null") ? null : background;
            if (RestUtil.isBase64(structure)) {
                if(background != null) {
                    if (!RestUtil.isBase64(background)) {
                        LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "createListTypeItemRelatedLayout"));
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "createListTypeItemRelatedLayout"));
                    }
                }
                return aem.createListTypeItemRelatedLayout(
                        listTypeItemId,
                        listTypeItemClassName,
                        viewClassName,
                        name,
                        description,
                        Base64.decodeBase64(structure),
                        background == null ? null : Base64.decodeBase64(background)
                );
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","createListTypeItemRelatedLayout"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","createListTypeItemRelatedLayout"));
            }
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(ex.getStatus(), ex.getMessage());
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
     * Allows to retrieve a list of all existent layout views.
     * @param limit The limit of results. -1 for all.
     * @param sessionId The session token id.
     * @return The list of views.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getLayouts/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ViewObjectLight> getLayouts(
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getLayouts", "127.0.0.1", sessionId);
            return aem.getLayouts(limit);
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
     * Returns a layout view with the given id.
     * @param viewId View id.
     * @param sessionId The session token id.
     * @return An object representing the view.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getLayout/{viewId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ViewObject getLayout(
            @PathVariable(RestConstants.VIEW_ID) long viewId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getLayout", "127.0.0.1", sessionId);
            return aem.getLayout(viewId);
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
     * Returns the list type item related with the given view.
     * @param viewId View id.
     * @param sessionId The session token id.
     * @return An object representing the list type item.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getListTypeItemForLayout/{viewId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getListTypeItemForLayout(
            @PathVariable(RestConstants.VIEW_ID) long viewId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getListTypeItemForLayout", "127.0.0.1", sessionId);
            return aem.getListTypeItemForLayout(viewId);
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
     * Creates a new layout view.Creates a new Layout view with the given data.
     * @param viewClassName View class name. If any, "null" otherwise.
     * @param name View name.
     * @param description View description.
     * @param structure The structure of the view as string Base64,from an XML document with the view structure
     * <pre>
     * {@code
     *  <view version="">
     *      <layout name="" x="" y="" width="" height="" type="">
     *          <shape type="" x="" y="" height="" width="" opaque=""
     *                 isEquipment="" name="" id="" className="" color="" borderColor="">
     *          </shape> 
     *          <shape ... >
     *          </shape>
     *          <shape ... >
     *          </shape>
     *          .
     *          .
     *          .
     *      </layout>
     *  </view>
     * }
     * </pre>
     * @param background The background image as string Base64. If any, "null" otherwise.
     * @param sessionId The session token id.
     * @return The id of the new view.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value = "createLayout/{viewClassName}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createLayout(
            @PathVariable(RestConstants.VIEW_CLASS_NAME) String viewClassName,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.BACKGROUND) String background,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createLayout", "127.0.0.1", sessionId);
            
            background = background.equals("null") ? null : background;
            if (RestUtil.isBase64(structure)) {
                if(background != null) {
                    if (!RestUtil.isBase64(background)) {
                        LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "createLayout"));
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "createLayout"));
                    }
                }
                
                return aem.createLayout(
                        viewClassName.equals("null") ? "" : viewClassName,
                        name,
                        description,
                        Base64.decodeBase64(structure),
                        background == null ? null : Base64.decodeBase64(background)
                );
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","createLayout"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","createLayout"));
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
     * Relate a list type item with a view. Creates a relationship between the given list type and layout view.
     * @param listTypeItemId List type item id.
     * @param listTypeItemClassName List type item class name.
     * @param viewId The view id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "setListTypeItemRelatedLayout/{listTypeItemId}/{listTypeItemClassName}/{viewId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void setListTypeItemRelatedLayout(
            @PathVariable(RestConstants.LIST_TYPE_ITEM_ID) String listTypeItemId,
            @PathVariable(RestConstants.LIST_TYPE_ITEM_CLASS_NAME) String listTypeItemClassName,
            @PathVariable(RestConstants.VIEW_ID) long viewId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("setListTypeItemRelatedLayout", "127.0.0.1", sessionId);
            aem.setListTypeItemRelatedLayout(listTypeItemId, listTypeItemClassName, viewId);
        } catch (InvalidArgumentException ex) {
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
     * Release a list type item with a view.Deletes a relationship between the given list type and layout view.
     * @param listTypeItemId List type item id.
     * @param listTypeItemClassName List type item class name.
     * @param viewId The view id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "releaseListTypeItemRelatedLayout/{listTypeItemId}/{listTypeItemClassName}/{viewId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void releaseListTypeItemRelatedLayout(
            @PathVariable(RestConstants.LIST_TYPE_ITEM_ID) String listTypeItemId,
            @PathVariable(RestConstants.LIST_TYPE_ITEM_CLASS_NAME) String listTypeItemClassName,
            @PathVariable(RestConstants.VIEW_ID) long viewId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("releaseListTypeItemRelatedLayout", "127.0.0.1", sessionId);
            aem.releaseListTypeItemRelatedLayout(listTypeItemId, listTypeItemClassName, viewId);
        } catch (InvalidArgumentException ex) {
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
     * Gets a view related to a list type item, such as the default, rack or equipment views.
     * @param listTypeItemId List type item id.
     * @param listTypeItemClassName List type item class.
     * @param viewId The view id.
     * @param sessionId The session token id.
     * @return The associated view (there should be only one of each type). Null if there's none yet.
     */ 
    @RequestMapping(method = RequestMethod.GET, value = "getListTypeItemRelatedLayout/{listTypeItemId}/{listTypeItemClassName}/{viewId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ViewObject getListTypeItemRelatedLayout(
            @PathVariable(RestConstants.LIST_TYPE_ITEM_ID) String listTypeItemId,
            @PathVariable(RestConstants.LIST_TYPE_ITEM_CLASS_NAME) String listTypeItemClassName,
            @PathVariable(RestConstants.VIEW_ID) long viewId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("getListTypeItemRelatedLayout", "127.0.0.1", sessionId);
            return aem.getListTypeItemRelatedLayout(listTypeItemId, listTypeItemClassName, viewId);
        } catch (InvalidArgumentException ex) {
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
     * Updates a view for a given list type item. If there's already a view of the provided view type, it will be overwritten.
     * @param listTypeItemId List type item id.
     * @param listTypeItemClassName List type item class name.
     * @param viewId View Id.
     * @param name View name.
     * @param description View description.
     * @param structure The structure of the view as string Base64,from an XML document with the view structure
     * <pre>
     * {@code
     *  <view version="">
     *      <layout name="" x="" y="" width="" height="" type="">
     *          <shape type="" x="" y="" height="" width="" opaque=""
     *                 isEquipment="" name="" id="" className="" color="" borderColor="">
     *          </shape> 
     *          <shape ... >
     *          </shape>
     *          <shape ... >
     *          </shape>
     *          .
     *          .
     *          .
     *      </layout>
     *  </view>
     * }
     * </pre>
     * @param background The background image as string Base64. If "null", the previous will be removed, if 0-sized array, it will remain unchanged.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT, 
            value = "updateListTypeItemRelatedLayout/{listTypeItemId}/{listTypeItemClassName}/{viewId}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateListTypeItemRelatedLayout(
            @PathVariable(RestConstants.LIST_TYPE_ITEM_ID) String listTypeItemId,
            @PathVariable(RestConstants.LIST_TYPE_ITEM_CLASS_NAME) String listTypeItemClassName,
            @PathVariable(RestConstants.VIEW_ID) long viewId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.BACKGROUND) String background,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateListTypeItemRelatedLayout", "127.0.0.1", sessionId);
            
            background = background.equals("null") ? null : background;
            if (RestUtil.isBase64(structure)) {
                if(background != null) {
                    if (!RestUtil.isBase64(background)) {
                        LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "updateListTypeItemRelatedLayout"));
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "updateListTypeItemRelatedLayout"));
                    }
                }
                
                return aem.updateListTypeItemRelatedLayout(
                        listTypeItemId,
                        listTypeItemClassName,
                        viewId,
                        name,
                        description,
                        Base64.decodeBase64(structure),
                        background == null ? null : Base64.decodeBase64(background)
                );
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","updateListTypeItemRelatedLayout"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","updateListTypeItemRelatedLayout"));
            }
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(ex.getStatus(), ex.getMessage());
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
     * Updates a layout view. Updates the given layout view with the parameters provided.
     * @param viewId View Id.
     * @param name View name.
     * @param description View description.
     * @param structure The structure of the view as string Base64,from an XML document with the view structure
     * <pre>
     * {@code
     *  <view version="">
     *      <layout name="" x="" y="" width="" height="" type="">
     *          <shape type="" x="" y="" height="" width="" opaque=""
     *                 isEquipment="" name="" id="" className="" color="" borderColor="">
     *          </shape> 
     *          <shape ... >
     *          </shape>
     *          <shape ... >
     *          </shape>
     *          .
     *          .
     *          .
     *      </layout>
     *  </view>
     * }
     * </pre>
     * @param background The background image as string Base64. If "null", the previous will be removed, if 0-sized array, it will remain unchanged.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT, 
            value = "updateLayout/{viewId}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateLayout(
            @PathVariable(RestConstants.VIEW_ID) long viewId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.BACKGROUND) String background,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateLayout", "127.0.0.1", sessionId);
            
            background = background.equals("null") ? null : background;
            if (RestUtil.isBase64(structure)) {
                if(background != null) {
                    if (!RestUtil.isBase64(background)) {
                        LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "updateLayout"));
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "updateLayout"));
                    }
                }
                
                return aem.updateLayout(
                        viewId,
                        name,
                        description,
                        Base64.decodeBase64(structure),
                        background == null ? null : Base64.decodeBase64(background)
                );
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","updateLayout"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","updateLayout"));
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
     * Gets the views related to a list type item, such as the default, rack or equipment views.
     * @param listTypeItemId List type item id.
     * @param listTypeItemClassName List type item class name.
     * @param limit Max number of results.
     * @param sessionId The session token id.
     * @return The associated views.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getListTypeItemRelatedLayouts/{listTypeItemId}/{listTypeItemClassName}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ViewObjectLight> getListTypeItemRelatedLayouts(
            @PathVariable(RestConstants.LIST_TYPE_ITEM_ID) String listTypeItemId,
            @PathVariable(RestConstants.LIST_TYPE_ITEM_CLASS_NAME) String listTypeItemClassName,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("getListTypeItemRelatedLayout", "127.0.0.1", sessionId);
            return aem.getListTypeItemRelatedLayout(listTypeItemId, listTypeItemClassName, limit);
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
     * Deletes a list type item related view.
     * @param listTypeItemId List type item id.
     * @param listTypeItemClassName List type class name.
     * @param viewId Related view id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteListTypeItemRelatedLayout/{listTypeItemId}/{listTypeItemClassName}/{viewId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteListTypeItemRelatedLayout(
            @PathVariable(RestConstants.LIST_TYPE_ITEM_ID) String listTypeItemId,
            @PathVariable(RestConstants.LIST_TYPE_ITEM_CLASS_NAME) String listTypeItemClassName,
            @PathVariable(RestConstants.VIEW_ID) long viewId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("deleteListTypeItemRelatedLayout", "127.0.0.1", sessionId);
            aem.deleteListTypeItemRelatedLayout(listTypeItemId, listTypeItemClassName, viewId);
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
     * Deletes a layout view.
     * @param viewId Related view id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteLayout/{viewId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteLayout(
            @PathVariable(RestConstants.VIEW_ID) long viewId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("deleteLayout", "127.0.0.1", sessionId);
            aem.deleteLayout(viewId);
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
     * Gets the list of template elements with a device layout.
     * @param sessionId The session token id.
     * @return The list of template elements with a device layout.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getDeviceLayouts/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getDeviceLayouts(
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getDeviceLayouts", "127.0.0.1", sessionId);
            return aem.getDeviceLayouts();
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
     * Gets the device layout structure.
     * <pre>
     * {@code
     *  <deviceLayoutStructure>
     *      <device id="" className="" name=""/>
     *      ...
     *      .
     *      .
     *      <device id="" className="" name="" parentId="">
     *          <model id="" className="" name="">
     *              <view id="" className="">
     *                  <structure>
     *                      Base64                      
     *                  </structure>
     *              </view>
     *          </model>
     *      </device>
     *      ...
     *      .
     *      .
     *      <device id="" className="" name="" parentId=""/>
     *      ...
     *      .
     *      .
     *      <device id="" className="" name="" parentId="">
     *          <model id="" className="" name="">
     *              <view id="" className="">
     *                  <structure>
     *                      Base64                      
     *                  </structure>
     *              </view>
     *          </model>
     *      </device>
     *  </deviceLayoutStructure>
     * }
     * </pre>
     * @param id Object id.
     * @param className Object class.
     * @param sessionId The session token id.
     * @return The structure of the device layout.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getDeviceLayoutStructure/{id}/{className}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public byte[] getDeviceLayoutStructure(
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getDeviceLayoutStructure", "127.0.0.1", sessionId);
            return aem.getDeviceLayoutStructure(id, className);
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