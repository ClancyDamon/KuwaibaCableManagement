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
import org.apache.commons.codec.binary.Base64;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.BusinessRule;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.neotropic.kuwaiba.northbound.rest.RestUtil;
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
 * Set of resources to manage the application entities.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(ApplicationEntityManagerRestController.PATH)
public class ApplicationEntityManagerRestController implements ApplicationEntityManagerRestOpenApi {
    
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
    public static Logger LOG = LoggerFactory.getLogger(ApplicationEntityManagerRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and core
     */
    public static final String PATH = "/v2.1/core/aem/"; //NOI18N
    
    // <editor-fold desc="pools" defaultstate="collapsed">
    
    /**
     * Creates a pool without a parent. They're used as general purpose place to put inventory objects, or as root for particular models.
     * @param name Pool name.
     * @param description Pool description.
     * @param className What kind of objects can this pool contain?
     * @param type Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX.
     * @param sessionId The session token id.
     * @return The id of the new pool.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createRootPool/{name}/{description}/{className}/{type}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createRootPool(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.TYPE) int type,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createRootPool", "127.0.0.1", sessionId);
            return aem.createRootPool(name, description, className, type);
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
     * Creates a pool that will have as parent an inventory object. This special containment structure can be used to provide support for new models.
     * @param parentClassName Class name of the parent object.
     * @param parentId Id of the parent object.
     * @param name Pool name.
     * @param description Pool description.
     * @param className What kind of objects can this pool contain?
     * @param type Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX.
     * @param sessionId The session token id.
     * @return The id of the new pool.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createPoolInObject/{parentClassName}/{parentId}/{name}/{description}/{className}/{type}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createPoolInObject(
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.TYPE) int type,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createPoolInObject", "127.0.0.1", sessionId);
            return aem.createPoolInObject(parentClassName, parentId, name, description, className, type);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException  ex) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Creates a pool that will have as parent another pool. This special containment structure can be used to provide support for new models.
     * @param parentId Id of the parent pool.
     * @param name Pool name.
     * @param description Pool description.
     * @param className What kind of objects can this pool contain?
     * @param type Type of pool. Not used so far, but it will be in the future. It will probably be used to help organize the existing pools.
     * @param sessionId The session token id.
     * @return The id of the new pool.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createPoolInPool/{parentId}/{name}/{description}/{className}/{type}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createPoolInPool(
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.TYPE) int type,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createPoolInPool", "127.0.0.1", sessionId);
            return aem.createPoolInPool(parentId, name, description, className, type);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException  ex) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Deletes a set of pools. Note that this method will delete and commit the changes until it finds an error, so if deleting any of the pools fails, don't try to delete those that were already processed.
     * @param ids The list of ids from the objects to be deleted.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deletePools/{ids}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deletePools(
            @PathVariable(RestConstants.IDS) String[] ids,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deletePools", "127.0.0.1", sessionId);
            aem.deletePools(ids);
        } catch (OperationNotPermittedException ex) {
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
     * Updates a pool. The class name field is read only to preserve the integrity of the pool. Same happens to the field type.
     * @param poolId Pool Id.
     * @param name Pool name. If null, this field will remain unchanged.
     * @param description Pool description. If null, this field will remain unchanged.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "setPoolProperties/{poolId}/{name}/{description}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor setPoolProperties(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("setPoolProperties", "127.0.0.1", sessionId);
            return aem.setPoolProperties(poolId, name, description);
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
    
    // <editor-fold desc="audit-trail" defaultstate="collapsed">
    /**
     * Gets a business object audit trail.
     * @param className Object class name.
     * @param objectId Object id.
     * @param limit Max number of results to be shown.
     * @param sessionId The session token id.
     * @return The list of activity entries.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getBusinessObjectAuditTrail/{className}/{objectId}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ActivityLogEntry> getBusinessObjectAuditTrail(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getBusinessObjectAuditTrail", "127.0.0.1", sessionId);
            return aem.getBusinessObjectAuditTrail(className, objectId, limit);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Retrieves the number of general activity log entries.
     * @param page Current page.
     * @param limit Limit of results per page. 0 to retrieve them all.
     * @param filters The response may be filtered by user (use key <code>user</code>, value the user name, a String) or event type (use key <code>type</code>, 
     * value any from ActivityLogEntry.ACTIVITY_TYPE_XXXX, an integer). If this parameter is null, no filters will be applied. If a key is not present, it won't 
     * be used as filter. If both are present, a logical AND will be applied.
     * @param sessionId The session token id.
     * @return The number of activity log entries.
     */
    @RequestMapping(method = RequestMethod.POST, value = "getGeneralActivityAuditTrailCount/{page}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getGeneralActivityAuditTrailCount(
            @PathVariable(RestConstants.PAGE) int page,
            @PathVariable(RestConstants.LIMIT) int limit,
            @RequestBody HashMap<String, Object> filters,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getGeneralActivityAuditTrailCount", "127.0.0.1", sessionId);
            return aem.getGeneralActivityAuditTrailCount(page, limit, filters);
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
     * Retrieves the list of general activity log entries.
     * @param page Current page
     * @param limit Limit of results per page. 0 to retrieve them all.
     * @param filters The response may be filtered by user (use key <code>user</code>, value the user name, a String) or event type (use key <code>type</code>, 
     * value any from ActivityLogEntry.ACTIVITY_TYPE_XXXX, an integer). If this parameter is null, no filters will be applied. If a key is not present, it won't 
     * be used as filter. If both are present, a logical AND will be applied.
     * @param sessionId The session token id.
     * @return The list of activity log entries. The entries are sorted by creation date in descending order.
     */
    @RequestMapping(method = RequestMethod.POST, value = "getGeneralActivityAuditTrail/{page}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ActivityLogEntry> getGeneralActivityAuditTrail(
            @PathVariable(RestConstants.PAGE) int page,
            @PathVariable(RestConstants.LIMIT) int limit,
            @RequestBody HashMap<String, Object> filters,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getGeneralActivityAuditTrail", "127.0.0.1", sessionId);
            return aem.getGeneralActivityAuditTrail(page, limit, filters);
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
    
    // <editor-fold desc="views" defaultstate="collapsed">
    /**
     * Get a view related to an object, such as the default rack or object views.
     * @param objectId Object id.
     * @param className Object class name.
     * @param viewId View id.
     * @param sessionId The session token id.
     * @return The associated view (there should be only one of each type). Null if there's none yet.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getObjectRelatedView/{objectId}/{className}/{viewId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ViewObject getObjectRelatedView(
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.VIEW_ID) long viewId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectRelatedView", "127.0.0.1", sessionId);
            return aem.getObjectRelatedView(objectId, className, viewId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Get a view related to an object, such as the default, rack or equipment views.
     * @param objectId Object id.
     * @param className Object class name.
     * @param limit Max number of results.
     * @param sessionId The session token id.
     * @return The associated views.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getObjectRelatedViews/{objectId}/{className}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ViewObjectLight> getObjectRelatedViews(
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectRelatedViews", "127.0.0.1", sessionId);
            return aem.getObjectRelatedViews(objectId, className, limit);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Allows to retrieve a list of views of a certain type, specifying their class.
     * @param className The class name.
     * @param limit The limit of results. -1 for all.
     * @param sessionId The session token id.
     * @return The view class name.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getGeneralViews/{className}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ViewObjectLight> getGeneralViews(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getGeneralViews", "127.0.0.1", sessionId);
            return aem.getGeneralViews(className, limit);
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
     * Returns a view of those that are not related to a particular object (i.e.: GIS views).
     * @param viewId View id.
     * @param sessionId The session token id.
     * @return An object representing the view.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getGeneralView/{viewId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ViewObject getGeneralView(
            @PathVariable(RestConstants.VIEW_ID) long viewId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getGeneralView", "127.0.0.1", sessionId);
            return aem.getGeneralView(viewId);
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
     * Creates a view for a given object.If there's already a view of the provided view type, it will be overwritten.
     * @param objectId Object id.
     * @param className Object class name.
     * @param name View name.
     * @param description View description.
     * @param viewClassName View class name (See class ViewObject for details about the supported types).
     * @param structure The structure of the view as string Base64, from an XML document with the view structure (see http://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format).
     * @param background The background image as string Base64. Used "null" for none.
     * @param sessionId The session token id.
     * @return The id of the new view.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value = "createObjectRelatedView/{objectId}/{className}/{name}/{description}/{viewClassName}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createObjectRelatedView(
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.VIEW_CLASS_NAME) String viewClassName,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.BACKGROUND) String background,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createObjectRelatedView", "127.0.0.1", sessionId);
            
            background = background.equals("null") ? null : background;
            if (RestUtil.isBase64(structure)) {
                if(background != null) {
                    if (!RestUtil.isBase64(background)) {
                        LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "createObjectRelatedView"));
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "createObjectRelatedView"));
                    }
                }
                return aem.createObjectRelatedView(
                        objectId,
                        className,
                        name,
                        description,
                        viewClassName,
                        Base64.decodeBase64(structure),
                        background == null ? null : Base64.decodeBase64(background)
                );
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","createObjectRelatedView"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","createObjectRelatedView"));
            }
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(ex.getStatus(), ex.getMessage());
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Creates a view not related to a particular object.
     * @param className View class name.
     * @param name View name.
     * @param description View description.
     * @param structure The structure of the view as string Base64, from an XML document specifying the view structure (nodes, edges, control points).
     * @param background The background image as string Base64. Used "null" for none.
     * @param sessionId The session token id.
     * @return The id of the newly created view.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value = "createGeneralView/{className}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createGeneralView(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.BACKGROUND) String background,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createGeneralView", "127.0.0.1", sessionId);
            
            background = background.equals("null") ? null : background;
            if (RestUtil.isBase64(structure)) {
                if(background != null) {
                    if (!RestUtil.isBase64(background)) {
                        LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "createGeneralView"));
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "createGeneralView"));
                    }
                }
                return aem.createGeneralView(
                        className,
                        name,
                        description,
                        Base64.decodeBase64(structure),
                        background == null ? null : Base64.decodeBase64(background)
                );
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","createGeneralView"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","createGeneralView"));
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
     * Updates a view for a given object.If there's already a view of the provided view type, it will be overwritten.
     * @param objectId Object id.
     * @param className Object class name.
     * @param viewId View id.
     * @param name View name.
     * @param description View description.
     * @param structure The structure of the view as string Base64, from an XML document with the view structure (see http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format).
     * @param background The background image as string Base64. If "null", the previous will be removed, if 0-sized array, it will remain unchanged.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateObjectRelatedView/{objectId}/{className}/{viewId}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateObjectRelatedView(
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.VIEW_ID) long viewId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.BACKGROUND) String background,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateObjectRelatedView", "127.0.0.1", sessionId);
            
            background = background.equals("null") ? null : background;
            if (RestUtil.isBase64(structure)) {
                if(background != null) {
                    if (!RestUtil.isBase64(background)) {
                        LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "updateObjectRelatedView"));
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "updateObjectRelatedView"));
                    }
                }
                return aem.updateObjectRelatedView(
                        objectId,
                        className,
                        viewId,
                        name,
                        description,
                        Base64.decodeBase64(structure),
                        background == null ? null : Base64.decodeBase64(background)
                );
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","updateObjectRelatedView"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","updateObjectRelatedView"));
            }
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(ex.getStatus(), ex.getMessage());
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
          
    /**
     * Saves a view not related to a particular object.The view type can not be changed.
     * @param id View id.
     * @param name View name. Null to leave unchanged.
     * @param description View description. Null to leave unchanged.
     * @param structure The structure of the view as string Base64, from an XML document specifying the view structure (nodes, edges, control points). Null to leave unchanged.
     * @param background The background image as string Base64. If "null", the previous will be removed, if 0-sized array, it will remain unchanged.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateGeneralView/{id}/{name}/{description}/{structure}/{background}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateGeneralView(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.BACKGROUND) String background,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateGeneralView", "127.0.0.1", sessionId);
            
            background = background.equals("null") ? null : background;
            if (RestUtil.isBase64(structure)) {
                if(background != null) {
                    if (!RestUtil.isBase64(background)) {
                        LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "updateGeneralView"));
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "background", "updateGeneralView"));
                    }
                }
                return aem.updateGeneralView(
                        id,
                        name,
                        description,
                        Base64.decodeBase64(structure),
                        background == null ? null : Base64.decodeBase64(background)
                );
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","updateGeneralView"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure","updateGeneralView"));
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
     * Deletes a list of general views.
     * @param ids The ids of the views to be deleted.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteGeneralViews/{ids}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteGeneralViews(
            @PathVariable(RestConstants.IDS) List<Long> ids,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteGeneralViews", "127.0.0.1", sessionId);
            aem.deleteGeneralViews(ids);
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
    
    // <editor-fold desc="files" defaultstate="collapsed">
    
    /**
     * Relates a file to a list type item.
     * @param name The name of the file.
     * @param tags The tags that describe the contents of the file.
     * @param file The file itself as string Base64.
     * @param listTypeItemClassName The list type item class name.
     * @param listTypeItemId The id of the list type item the file will be attached to.
     * @param sessionId The session token id.
     * @return The id of the resulting file object.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "attachFileToListTypeItem/{name}/{tags}/{file}/{listTypeItemClassName}/{listTypeItemId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long attachFileToListTypeItem(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.TAGS) String tags,
            @PathVariable(RestConstants.FILE) String file,
            @PathVariable(RestConstants.LIST_TYPE_ITEM_CLASS_NAME) String listTypeItemClassName,
            @PathVariable(RestConstants.LIST_TYPE_ITEM_ID) String listTypeItemId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("attachFileToListTypeItem", "127.0.0.1", sessionId);
            if (RestUtil.isBase64(file)) {
                return aem.attachFileToListTypeItem(
                        name,
                        tags,
                        Base64.decodeBase64(file),
                        listTypeItemClassName,
                        listTypeItemId
                );
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "file","attachFileToListTypeItem"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                         String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "file","attachFileToListTypeItem"));
            }
        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(ex.getStatus(), ex.getMessage());
        } catch (InvalidArgumentException | OperationNotPermittedException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Fetches the files associated to an inventory object. Note that this call won't retrieve the actual files, but only references to them.
     * @param className The class of the object whose files will be fetched from.
     * @param objectId The id of the object whose files will be fetched from.
     * @param sessionId The session token id.
     * @return The list of files.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getFilesForListTypeItem/{className}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<FileObjectLight> getFilesForListTypeItem(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("getFilesForListTypeItem", "127.0.0.1", sessionId);
            return aem.getFilesForListTypeItem(className, objectId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Retrieves a particular file associated to an inventory list type item.This call returns the actual file.
     * @param id The id of the file object.
     * @param className The class of the object the file is associated to.
     * @param objectId The id of the list type item the file is associated to.
     * @param sessionId The session token id.
     * @return The file.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getFile/{id}/{className}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public FileObject getFile(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getFile", "127.0.0.1", sessionId);
            return aem.getFile(id, className, objectId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Releases (and deletes) a file associated to a list type item.
     * @param id The id of the file.
     * @param className The class of the list type item the file is associated to.
     * @param objectId The id of the list type item the file is associated to.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "detachFileFromListTypeItem/{id}/{className}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void detachFileFromListTypeItem(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("detachFileFromListTypeItem", "127.0.0.1", sessionId);
            aem.detachFileFromListTypeItem(id, className, objectId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Updates the properties of a file list type item (name or tags).
     * @param id The id of the file.
     * @param properties The set of properties as a dictionary key-value. Valid keys are "name" and "tags".
     * @param className The class of the object the file is attached to.
     * @param listTypeItemId The id of the list type item the file is attached to.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateFileProperties/{id}/{className}/{listTypeItemId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateFileProperties(
            @PathVariable(RestConstants.ID) long id,
            @RequestBody List<StringPair> properties,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.LIST_TYPE_ITEM_ID) String listTypeItemId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateFileProperties", "127.0.0.1", sessionId);
            aem.updateFileProperties(id, properties, className, listTypeItemId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    // </editor-fold>
    
    // <editor-fold desc="business-rule" defaultstate="collapsed">
    
    /**
     * Creates a business rule given a set of constraints.
     * @param name Rule name.
     * @param description Rule description.
     * @param type Rule type. See BusinesRule.TYPE* for possible values.
     * @param scope The scope of the rule. See BusinesRule.SCOPE* for possible values.
     * @param appliesTo The class this rule applies to. Can not be null.
     * @param version The version of the rule. Useful to migrate it if necessary in further versions of the platform.
     * @param constraints An array with the definition of the logic to be matched with the rule. Can not be empty or null.
     * @param sessionId The session token id.
     * @return The id of the newly created business rule.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createBusinessRule/{name}/{description}/{type}/{scope}/{appliesTo}/{version}/{constraints}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createBusinessRule(
                    @PathVariable(RestConstants.NAME) String name,
                    @PathVariable(RestConstants.DESCRIPTION) String description,
                    @PathVariable(RestConstants.TYPE) int type, 
                    @PathVariable(RestConstants.SCOPE) int scope,
                    @PathVariable(RestConstants.APPLIES_TO) String appliesTo,
                    @PathVariable(RestConstants.VERSION) String version,
                    @PathVariable(RestConstants.CONSTRAINTS) List<String> constraints,
                    @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createBusinessRule", "127.0.0.1", sessionId);
            return aem.createBusinessRule(name, description, type, scope, appliesTo, version, constraints);
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
     * Deletes a business rule.
     * @param id Rule id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteBusinessRule/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteBusinessRule(
                    @PathVariable(RestConstants.ID) long id,
                    @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteBusinessRule", "127.0.0.1", sessionId);
            aem.deleteBusinessRule(id);
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
     * Retrieves the business rules of a particular type.
     * @param type Rule type. See BusinesRule.TYPE* for possible values. Use -1 to retrieve all.
     * @param sessionId The session token id.
     * @return The list of business rules with the matching type.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getBusinessRules/{type}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessRule> getBusinessRules(
                    @PathVariable(RestConstants.TYPE) int type,
                    @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getBusinessRules", "127.0.0.1", sessionId);
            return aem.getBusinessRules(type);
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
    
    /**
     * Get the data model class hierarchy as an XML document.
     * @param showAll
     * @param sessionId The session token id.
     * @return The class hierarchy as an XML document.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getClassHierachy/{showAll}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public byte[] getClassHierachy(
            @PathVariable(RestConstants.SHOW_ALL) boolean showAll,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getClassHierachy", "127.0.0.1", sessionId);
            return aem.getClassHierachy(showAll);
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
}