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
package org.neotropic.kuwaiba.northbound.rest.bem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.AnnotatedBusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
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
 * This is the entity in charge of manipulating business objects.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(BusinessEntityManagerRestController.PATH)
public class BusinessEntityManagerRestController implements BusinessEntityManagerRestOpenApi {
    
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    
    /**
     * Reference to the Application Entity Manager.
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
    public static Logger LOG = LoggerFactory.getLogger(BusinessEntityManagerRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and core
     */
    public static final String PATH = "/v2.1/core/bem/"; //NOI18N
    
    // <editor-fold desc="objects" defaultstate="collapsed">
    
    /**
     * Creates a new inventory object.
     * @param className Name of the class which this object will be instantiated from.
     * @param parentClassName Parent object class name. If "null", the parent will be the DummyRoot node.
     * @param parentId Parent's oid. If "-1", the parent will be the DummyRoot node.
     * @param attributes Attributes to be set by default in the new object. It's a HashMap<String, String> where the keys are the attribute names and the values, the values for such attributes.
     * Note that binary type attributes can't be set here.
     * @param templateId Template id to be used to create the current object. Template values can be
     * overridden if "attributeValues" is not empty. Use an empty string or "null" to not use a Template.
     * @param sessionId The session token id.
     * @return The object's id.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value = "createObject/{className}/{parentClassName}/{parentId}/{templateId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createObject(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @RequestBody HashMap<String, String> attributes,
            @PathVariable(RestConstants.TEMPLATE_ID) String templateId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createObject", "127.0.0.1", sessionId);
            return bem.createObject(
                    className,
                    parentClassName.equals("null") ? null : parentClassName,
                    parentId,
                    attributes,
                    templateId.equals("null") ? null : templateId
            );
        } catch (InvalidArgumentException | OperationNotPermittedException ex) {
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
     * Creates an object.
     * @param className Class the object will be instance of.
     * @param parentClassName Class of the parent the object will be instance of. Use <b>root</b> for the navigation tree.
     * @param criteria Criteria to search for the parent. This is a string with two parts: 
     * One is the name of the attribute and the other its value, both separated by a fixed colon <b>:</b>. Example: name:Colombia.
     * @param attributes Dictionary with the names and the values of the attributes to be set.
     * @param templateId The id of the template to be used to create this object. 
     * This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before. 
     * Use a "null" or empty string to not use a template.
     * @param sessionId The session token id.
     * @return The id of the new object.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value = "createObjectWithCriteria/{className}/{parentClassName}/{templateId}/{criteria}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createObjectWithCriteria(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @RequestBody HashMap<String, String> attributes,
            @PathVariable(RestConstants.TEMPLATE_ID) String templateId,
            @PathVariable(RestConstants.CRITERIA) String criteria,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createObjectWithCriteria", "127.0.0.1", sessionId);
            return bem.createObject(
                    className,
                    parentClassName,
                    attributes,
                    templateId.equals("null") ? null : templateId,
                    criteria
            );
        } catch (InvalidArgumentException | OperationNotPermittedException ex) {
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
     * Creates a new inventory object for a domain specific model (where the standard containment rules don't apply).
     * @param className Name of the class which this object will be instantiated from.
     * @param parentClassName Parent object class name.
     * @param parentId Parent's oid.
     * @param attributes Attributes to be set by default in the new object. It's a HashMap<String, String> where the keys are the attribute names and the values, the values for such attributes.
     * Note that binary type attributes can't be set here.
     * @param templateId The id of the template to be used to create this object. This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before. Use a "null" or empty string to not use a template.
     * @param sessionId The session token id.
     * @return The id of the new object.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value = "createSpecialObject/{className}/{parentClassName}/{parentId}/{templateId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createSpecialObject(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @RequestBody HashMap<String, String> attributes,
            @PathVariable(RestConstants.TEMPLATE_ID) String templateId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createSpecialObject", "127.0.0.1", sessionId);
            return bem.createSpecialObject(
                    className,
                    parentClassName,
                    parentId,
                    attributes,
                    templateId.equals("null") ? null : templateId
            );
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
     * Equal to {@link BusinessEntityManager#createSpecialObject(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap, java.lang.String)} but the return is a map of ids.Creates a new inventory object for a domain specific model (where the standard containment rules don't apply).
     * @param className Name of the class which this object will be instantiated from.
     * @param parentClassName Parent object class name.
     * @param parentId Parent's oid.
     * @param attributes Attributes to be set by default in the new object. It's a HashMap<String, String> where the keys are the attribute names and the values, the values for such attributes.
     * Note that binary type attributes can't be set here.
     * @param templateId The id of the template to be used to create this object. This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before. Use a null or empty string to not use a template.
     * @param sessionId The session token id.
     * @return The id of the new object.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createSpecialObjectUsingTemplate/{className}/{parentClassName}/{parentId}/{templateId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HashMap<String, String> createSpecialObjectUsingTemplate(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @RequestBody HashMap<String, String> attributes,
            @PathVariable(RestConstants.TEMPLATE_ID) String templateId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createSpecialObjectUsingTemplate", "127.0.0.1", sessionId);
            return bem.createSpecialObjectUsingTemplate(className, parentClassName, parentId, attributes, templateId);
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
     * Breaks the special hierarchy to enable special children to have more than one parent.
     * @param specialObjectClass Special object class name
     * @param specialObjectId Special Object Id.
     * @param parentClassName Parent Object class name.
     * @param parentId Parent Object id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value = "addParentToSpecialObject/{specialObjectClass}/{specialObjectId}/{parentClassName}/{parentId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addParentToSpecialObject(
            @PathVariable(RestConstants.SPECIAL_OBJECT_CLASS) String specialObjectClass,
            @PathVariable(RestConstants.SPECIAL_OBJECT_ID) String specialObjectId,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("addParentToSpecialObject", "127.0.0.1", sessionId);
            bem.addParentToSpecialObject(specialObjectClass, specialObjectId, parentClassName, parentId);
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
     * Creates an object without parent.This might be particularly useful for complex models. Use it carefully to avoid leaving orphan objects.
     * Always provide custom methods to delete.
     * @param className The class name of the object to be created.
     * @param attributes The initial set of attributes (as pairs attribute name - value <String, String>) to be set. These values will override those in the template used (if any).
     * @param templateId The id of the template to be used to create the object. Use "null" or an empty string to not use any template.
     * @param sessionId The session token id.
     * @return The id of the newly created object.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value = "createHeadlessObject/{className}/{templateId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createHeadlessObject(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @RequestBody HashMap<String, String> attributes,
            @PathVariable(RestConstants.TEMPLATE_ID) String templateId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createHeadlessObject", "127.0.0.1", sessionId);
            return bem.createHeadlessObject(
                    className,
                    attributes,
                    templateId.equals("null") ? null : templateId
            );
        } catch (InvalidArgumentException | OperationNotPermittedException ex) {
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
     * Creates multiple objects using a given name pattern.
     * @param className The class name for the new objects.
     * @param parentClassName The parent class name for the new objects.
     * @param parentId The object id of the parent.
     * @param namePattern A pattern to create the names for the new objects.
     * @param templateId A template id for the objects creation, it could be "null" if no template is required.
     * @param sessionId The session token id.
     * @return An arrays of ids for the new objects.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "createBulkObjects/{className}/{parentClassName}/{parentId}/{templateId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] createBulkObjects(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId, 
            @RequestBody String namePattern,
            @PathVariable(RestConstants.TEMPLATE_ID) String templateId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createBulkObjects", "127.0.0.1", sessionId);
            return bem.createBulkObjects(
                    className,
                    parentClassName,
                    parentId,
                    namePattern,
                    templateId.equals("null") ? null : templateId
            );
        } catch (InvalidArgumentException | OperationNotPermittedException ex) {
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
     * Creates multiple special objects using a given name pattern.
     * @param className The class name for the new special objects.
     * @param parentClassName The parent class name for the new special objects.
     * @param parentId The object id of the parent.
     * @param namePattern A pattern to create the names for the new special objects.
     * @param templateId The id of the template to be used for the set of objects to be created. Used "null" for none.
     * @param sessionId The session token id.
     * @return An array of ids for the new special objects.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value = "createBulkSpecialObjects/{className}/{parentClassName}/{parentId}/{templateId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] createBulkSpecialObjects(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId, 
            @RequestBody String namePattern,
            @PathVariable(RestConstants.TEMPLATE_ID) String templateId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createBulkSpecialObjects", "127.0.0.1", sessionId);
            return bem.createBulkSpecialObjects(
                    className,
                    parentClassName,
                    parentId,
                    namePattern,
                    templateId.equals("null") ? null : templateId
            );
        } catch (InvalidArgumentException | OperationNotPermittedException ex) {
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
     * Gets the detailed information about an object.
     * @param className Object class name.
     * @param objectId Object's oid.
     * @param sessionId The session token id.
     * @return A detailed representation of the requested object.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getObject/{className}/{objectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObject getObject(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObject", "127.0.0.1", sessionId);
            return bem.getObject(className, objectId);
        } catch (InvalidArgumentException ex) {
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
     * Gets the simplified information about an object.
     * @param className Object class name.
     * @param objectId Object's oid.
     * @param sessionId The session token id.
     * @return A detailed representation of the requested object.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getObjectLight/{className}/{objectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getObjectLight(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectLight", "127.0.0.1", sessionId);
            return bem.getObjectLight(className, objectId);
        } catch (InvalidArgumentException ex) {
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
     * Gets the children of a given object.
     * @param className Object's class name.
     * @param objectId Object's oid.
     * @param maxResults Max number of children to be returned, -1 to return all.
     * @param sessionId The session token id.
     * @return The list of children.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getObjectChildren/{className}/{objectId}/{maxResults}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectChildren(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.MAX_RESULTS) int maxResults,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectChildren", "127.0.0.1", sessionId);
            return bem.getObjectChildren(className, objectId, maxResults);
        } catch (InvalidArgumentException ex) {
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
     * Gets the children of a given object, providing the class and object id.
     * @param classId The id of the class the object is instance of.
     * @param objectId The oid of the object.
     * @param maxResults The max number of results to be retrieved. Use 0 to retrieve all.
     * @param sessionId The session token id.
     * @return The list of children.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getObjectChildrenForClassWithId/{classId}/{objectId}/{maxResults}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectChildrenForClassWithId(
            @PathVariable(RestConstants.CLASS_ID) long classId,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.MAX_RESULTS) int maxResults,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectChildrenForClassWithId", "127.0.0.1", sessionId);
            return bem.getObjectChildren(classId, objectId, maxResults);
        } catch (InvalidArgumentException ex) {
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
     * Gets the special children of a given object.
     * @param className Object class name.
     * @param objectId Object id.
     * @param sessionId The session token id.
     * @return The list of special children.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getObjectSpecialChildren/{className}/{objectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectSpecialChildren(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectSpecialChildren", "127.0.0.1", sessionId);
            return bem.getObjectSpecialChildren(className, objectId);
        } catch (InvalidArgumentException ex) {
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
     * Gets the special children of a given object.
     * @param className Object class name.
     * @param objectId Object id.
     * @param childrenClassNamesToFilter Class names values to filter the return.
     * @param page The number of results to skip or the page.
     * @param limit The limit of results per page.
     * @param sessionId The session token id.
     * @return The list of special children.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getObjectSpecialChildrenWithFilters/{className}/{objectId}/{childrenClassNamesToFilter}/{page}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectSpecialChildrenWithFilters(
            @PathVariable(RestConstants.CLASS_NAME) String className, 
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.CHILDREN_CLASS_NAMES_TO_FILTER) List<String> childrenClassNamesToFilter,
            @PathVariable(RestConstants.PAGE) int page,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectSpecialChildrenWithFilters", "127.0.0.1", sessionId);
            return bem.getObjectSpecialChildrenWithFilters(className, objectId, childrenClassNamesToFilter, page, limit);
        } catch (InvalidArgumentException ex) {
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
     * Gets the special children count of a given object.
     * @param className Object class name.
     * @param objectId Object id.
     * @param childrenClassNamesToFilter Class names values to filter the return.
     * @param sessionId The session token id.
     * @return The count of special children.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getObjectSpecialChildrenCount/{className}/{objectId}/{childrenClassNamesToFilter}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getObjectSpecialChildrenCount(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.CHILDREN_CLASS_NAMES_TO_FILTER) String[] childrenClassNamesToFilter,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectSpecialChildrenCount", "127.0.0.1", sessionId);
            return bem.getObjectSpecialChildrenCount(className, objectId, childrenClassNamesToFilter);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
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
     * Retrieves a list of light instances of a given class given a simple filter.This method will search for all objects with a string-based attribute (filterName) whose value matches a value provided (filterValue).
     * @param className The class of the objects to be searched. This method support abstract superclasses as well.
     * @param filterName The attribute to be used as filter. This will work <b>only</b> with String-type attributes. Example: serialNumber. To list type attributes the filter must be applied to the name. Example: filterName: model, filterValue: XYZ.
     * @param filterValue The value to be use to match the instances. Example "Serial-12345".
     * @param sessionId The session token id.
     * @return The list of instances that matches the filterName/filterValue criteria.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getObjectsWithFilterLight/{className}/{filterName}/{filterValue}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectsWithFilterLight(
            @PathVariable(RestConstants.CLASS_NAME) String className, 
            @PathVariable(RestConstants.FILTER_NAME) String filterName,
            @PathVariable(RestConstants.FILTER_VALUE) String filterValue,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectsWithFilterLight", "127.0.0.1", sessionId);
            return bem.getObjectsWithFilterLight(className, filterName, filterValue);
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
     * Same as {@link #getObjectsWithFilterLight(java.lang.String, java.lang.String, java.lang.String) }, but returns the full information about the objects involved.
     * @param className The class of the objects to be searched. This method support abstract superclasses as well.
     * @param filterName The attribute to be used as filter. This will work <b>only</b> with String-type attributes. Example: serialNumber. To list type attributes the filter must be applied to the name. Example: filterName: model, filterValue: XYZ.
     * @param filterValue The value to be use to match the instances. Example "Serial-12345".
     * @param sessionId The session token id.
     * @return The list of instances that matches the filterName/filterValue criteria.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getObjectsWithFilter/{className}/{filterName}/{filterValue}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObject> getObjectsWithFilter(
            @PathVariable(RestConstants.CLASS_NAME) String className, 
            @PathVariable(RestConstants.FILTER_NAME) String filterName,
            @PathVariable(RestConstants.FILTER_VALUE) String filterValue,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectsWithFilter", "127.0.0.1", sessionId);
            return bem.getObjectsWithFilter(className, filterName, filterValue);
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
     * Suggests a number of objects based on a search string.The search string is matched against the name of the object, its class name or its class display name.
     * @param filterValue The string to use as search filter.
     * @param limit The limit of results. Use -1 to retrieve all.
     * @param sessionId The session token id.
     * @return A list of up to #{@code limit} suggested objects matching the criteria, alphabetically sorted.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getSuggestedObjectsWithFilter/{filterValue}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSuggestedObjectsWithFilter(
            @PathVariable(RestConstants.FILTER_VALUE) String filterValue,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("getSuggestedObjectsWithFilter", "127.0.0.1", sessionId);
            return bem.getSuggestedObjectsWithFilter(filterValue, limit);
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
     * Same as {@link #getSuggestedObjectsWithFilter(java.lang.String, int) }, but the results will be instances of the super class provided or one of its subclasses.
     * In reality, this method could do the same as {@link #getSuggestedObjectsWithFilter(java.lang.String, int)} with {@code superClass} set to <code>InventoryObject</code>,
     * but the implementation of both methods may differ significantly in terms of performance and complexity.
     * @param filterValue The search string.
     * @param superClassName The results will be instances of this class or one of its subclasses.
     * @param limit The limit of results. Use -1 to retrieve all.
     * @param sessionId The session token id.
     * @deprecated In favor of {@link #getSuggestedObjectsWithFilter(java.lang.String, int, int, java.lang.String...) }
     * @return A list of up to #{@code limit} suggested objects matching the criteria, alphabetically sorted.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getSuggestedObjectsWithFilterAndSuperClass/{filterValue}/{superClassName}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @Deprecated
    public List<BusinessObjectLight> getSuggestedObjectsWithFilterAndSuperClass(
            @PathVariable(RestConstants.FILTER_VALUE) String filterValue,
            @PathVariable(RestConstants.SUPER_CLASS_NAME) String superClassName,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("getSuggestedObjectsWithFilterAndSuperClass", "127.0.0.1", sessionId);
            return bem.getSuggestedObjectsWithFilter(filterValue, superClassName, limit);
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
     * Gets a set of suggested objects with filter.
     * @param filterValue A possible part of the name of an object(s) or class(es).
     * @param skip The number of leading suggested objects to skip.
     * @param limit The number of suggested objects the result should be limited to.
     * @param classNames The suggested objects will be instance of this classes or subclasses. Used "null" for none.
     * @param sessionId The session token id.
     * @return List of suggested objects.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value = "getSuggestedObjectsWithFilterAndClasses/{filterValue}/{skip}/{limit}/{classNames}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSuggestedObjectsWithFilterAndClasses(
            @PathVariable(RestConstants.FILTER_VALUE) String filterValue,
            @PathVariable(RestConstants.SKIP) int skip,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(value = RestConstants.CLASS_NAMES) String[] classNames,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSuggestedObjectsWithFilterAndClasses", "127.0.0.1", sessionId);
            return bem.getSuggestedObjectsWithFilter(
                    filterValue,
                    skip,
                    limit,
                    classNames[0].equals("null") ? null : classNames
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
     * Gets a set of objects by its exact names and class names, used to know 
     * if an object with the same its already created in the inventory 
     * e.g.an IP address or a subnet in the ipam module can not be repeated.
     * @param names The exact names of the objects.
     * @param skip The page.
     * @param limit The limit per page.
     * @param classNames Class names of the objects. 
     * @param sessionId The session token id.
     * @return A list of objects. 
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getObjectsByNameAndClassName/{names}/{skip}/{limit}/{classNames}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectsByNameAndClassName(
            @PathVariable(RestConstants.NAMES) List<String> names,
            @PathVariable(RestConstants.SKIP) int skip,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.CLASS_NAMES) String[] classNames,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectsByNameAndClassName", "127.0.0.1", sessionId);
            return bem.getObjectsByNameAndClassName(names, skip, limit, classNames);
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
     * Gets a set of suggested children with filter (no recursive).
     * @param parentClassName The parent class name.
     * @param parentId The parent id.
     * @param filterValue Filter children (no recursive) by name or class.
     * @param ignoreSpecialChildren True to ignore special children in the suggested children (no recursive).
     * @param skip The number of leading children to skip.
     * @param limit The number of children the result should be limited to.
     * @param classNames The suggested children will be instance of this classes or subclasses. If any, "null" otherwise.
     * @param sessionId The session token id.
     * @return List of suggested children (no recursive).
     */
    @RequestMapping(method = RequestMethod.GET, 
            value = "getSuggestedChildrenWithFilter/{parentClassName}/{parentId}/{filterValue}/{ignoreSpecialChildren}/{skip}/{limit}/{classNames}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSuggestedChildrenWithFilter(
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @PathVariable(RestConstants.FILTER_VALUE) String filterValue,
            @PathVariable(RestConstants.IGNORE_SPECIAL_CHILDREN) boolean ignoreSpecialChildren,
            @PathVariable(RestConstants.SKIP) int skip,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.CLASS_NAMES) String[] classNames,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSuggestedChildrenWithFilter", "127.0.0.1", sessionId);
            return bem.getSuggestedChildrenWithFilter(
                    parentClassName,
                    parentId,
                    filterValue,
                    ignoreSpecialChildren,
                    skip,
                    limit,
                    classNames[0].equals("null") ? null : classNames
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
     * Recursively gets all the light instances of given class, without filters.
     * @param className Class name. It must be a subclass of InventoryObject.
     * @param page Page or number of elements to skip.
     * @param limit Max count of child per page.
     * @param sessionId The session token id.
     * @return A set of instances of the class.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getObjectsOfClassLight/{className}/{page}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectsOfClassLight(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PAGE) long page,
            @PathVariable(RestConstants.LIMIT) long limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectsOfClassLight", "127.0.0.1", sessionId);
            return bem.getObjectsOfClassLight(className, page, limit);
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
     * Recursively gets all the light instances of given class.
     * @param className Class name. It must be a subclass of InventoryObject.
     * @param filters Map of filters key: attribute name, value: attribute value. <String,String>
     * @param page Page or number of elements to skip.
     * @param limit Max count of child per page.
     * @param sessionId The session token id.
     * @return A set of instances of the class.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value ="getObjectsOfClassLightWithFilter/{className}/{page}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectsOfClassLightWithFilter(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @RequestBody (required = false) HashMap<String, String> filters,
            @PathVariable(RestConstants.PAGE) long page,
            @PathVariable(RestConstants.LIMIT) long limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectsOfClassLightWithFilter", "127.0.0.1", sessionId);         
            return bem.getObjectsOfClassLight(className, filters, page, limit);
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
     * Recursively gets all the instances of given class.
     * @param className Class name. It mist be a subclass of InventoryObject.
     * @param maxResults Max number of results. 0 to get all.
     * @param sessionId The session token id.
     * @return A list of instances.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value ="getObjectsOfClass/{className}/{maxResults}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObject> getObjectsOfClass(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.MAX_RESULTS) int maxResults,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectsOfClass", "127.0.0.1", sessionId);
            return bem.getObjectsOfClass(className, maxResults);
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
     * Get the child count given the parent class name and id.
     * @param parentClassName Parent class name.
     * @param parentId Parent id.
     * @param filters Map of filters key: attribute name, value: attribute value. <String,String>
     * @param sessionId The session token id.
     * @return The count of child.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value ="getObjectChildrenCount/{parentClassName}/{parentId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getObjectChildrenCount(
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @RequestBody(required = false) HashMap<String, String> filters,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectChildrenCount", "127.0.0.1", sessionId);
            return bem.getObjectChildrenCount(parentClassName, parentId, filters);
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
     * Get a set of children to the given the parent class name and id.
     * @param parentClassName Parent class name.
     * @param parentId Parent id.
     * @param filters Null for no filter, map of filters key: attribute name, value: attribute value. <String,String>
     * @param skip Skip index.
     * @param limit Max count of child.
     * @param sessionId The session token id.
     * @return Set of children.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value ="getObjectChildrenWithFilter/{parentClassName}/{parentId}/{skip}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectChildrenWithFilter(
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @RequestBody(required = false) HashMap<String, String> filters,
            @PathVariable(RestConstants.SKIP) long skip,
            @PathVariable(RestConstants.LIMIT) long limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getObjectChildrenWithFilter", "127.0.0.1", sessionId);
            return bem.getObjectChildren(parentClassName, parentId, filters, skip, limit);
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
     * Suggests a number of objects based on a search string.
     * This search string will be case-insensitive-matched against
     * the name of the objects and classes in the inventory attributes to filter.
     * @param classNames List<ClassMetadataLight> classesToFilter a list of classes to limit the search. Used "null" for none.
     * @param filterValue Value to filter in the attribute name of every business object name or class name o class display name.
     * @param classesSkip Class skip index.
     * @param classesLimit Max count of child.
     * @param objectSkip Object skip index.
     * @param objectLimit Max count of objects.
     * @param sessionId The session token id.
     * @return Set of children.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getSuggestedObjectsWithFilterGroupedByClassName/{classNames}/{filterValue}/{classesSkip}/{classesLimit}/{objectSkip}/{objectLimit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HashMap<String, List<BusinessObjectLight>> getSuggestedObjectsWithFilterGroupedByClassName(
            @PathVariable(RestConstants.CLASS_NAMES) List<String> classNames,
            @PathVariable(RestConstants.FILTER_VALUE) String filterValue,
            @PathVariable(RestConstants.CLASSES_SKIP) long classesSkip,
            @PathVariable(RestConstants.CLASSES_LIMIT) long classesLimit,
            @PathVariable(RestConstants.OBJECT_SKIP) long objectSkip,
            @PathVariable(RestConstants.OBJECT_LIMIT) long objectLimit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSuggestedObjectsWithFilterGroupedByClassName", "127.0.0.1", sessionId);
            return bem.getSuggestedObjectsWithFilterGroupedByClassName(
                    classNames.get(0).equals("null") ? null : classNames,
                    filterValue,
                    classesSkip,
                    classesLimit,
                    objectSkip,
                    objectLimit
            );
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
     * Copy a set of objects.
     * @param targetObjectClassName Target parent's class name.
     * @param targetObjectId Target parent's oid.
     * @param objects Hashmap<String, List<String>> with the objects class names as keys and their oids as values.
     * @param recursive If this operation should also copy the children objects recursively.
     * @param sessionId The session token id.
     * @return A list containing the newly created object ids.
     */
    @RequestMapping(method = RequestMethod.PUT, 
            value ="copyObjects/{targetObjectClassName}/{targetObjectId}/{recursive}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] copyObjects(
            @PathVariable(RestConstants.TARGET_OBJECT_CLASS_NAME) String targetObjectClassName,
            @PathVariable(RestConstants.TARGET_OBJECT_ID) String targetObjectId,
            @RequestBody HashMap<String, List<String>> objects,
            @PathVariable(RestConstants.RECURSIVE) boolean recursive,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("copyObjects", "127.0.0.1", sessionId);
            return bem.copyObjects(
                    targetObjectClassName,
                    targetObjectId,
                    objects != null ? objects : new HashMap<String, List<String>>(),
                    recursive
            );
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
     * Copy a set of special objects (this is used to copy objects when they are containment are set in the special containment hierarchy)
     * use case: to move physical links into a wire Container.
     * @param targetObjectClassName Target parent's class name.
     * @param targetObjectId Target parent's oid.
     * @param objects Hashmap<String, List<String>> with the objects class names as keys and their oids as values. 
     * @param recursive If this operation should also copy the children objects recursively.
     * @param sessionId The session token id.
     * @return A list containing the newly created object ids.
     */
    @RequestMapping(method = RequestMethod.PUT, 
            value ="copySpecialObjects/{targetObjectClassName}/{targetObjectId}/{recursive}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] copySpecialObjects(
            @PathVariable(RestConstants.TARGET_OBJECT_CLASS_NAME) String targetObjectClassName,
            @PathVariable(RestConstants.TARGET_OBJECT_ID) String targetObjectId,
            @RequestBody HashMap<String, List<String>> objects,
            @PathVariable(RestConstants.RECURSIVE) boolean recursive,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("copySpecialObjects", "127.0.0.1", sessionId);
            return bem.copySpecialObjects(
                    targetObjectClassName,
                    targetObjectId,
                    objects != null ? objects : new HashMap<String, List<String>>(),
                    recursive
            );
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
     * Move a list of objects to a new parent: this methods ignores those who can't be moved and raises
     * an OperationNotPermittedException, however, it will move those which can be moved.
     * @param targetObjectClassName Parent's class name.
     * @param targetObjectId Parent's oid.
     * @param objects Hashmap<String, String[]> using the object class name as keys and the respective objects oids as values.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, 
            value ="moveObjects/{targetObjectClassName}/{targetObjectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void moveObjects(
            @PathVariable(RestConstants.TARGET_OBJECT_CLASS_NAME) String targetObjectClassName,
            @PathVariable(RestConstants.TARGET_OBJECT_ID) String targetObjectId,
            @RequestBody HashMap<String,String[]> objects,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("moveObjects", "127.0.0.1", sessionId);
            bem.moveObjects(
                    targetObjectClassName,
                    targetObjectId,
                    objects != null ? objects : new HashMap<String, String[]>()
            );
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
     * Move a list of objects to a new parent(taking into account the special
     * hierarchy containment): this methods ignores those who can't be moved and raises an 
     * OperationNotPermittedException, however, it will move those which can be moved.
     * @param targetObjectClassName Parent's class name.
     * @param targetObjectId Parent's oid.
     * @param objects Hashmap<String, String[]> using the object class name as keys and the respective objects oids as values.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, 
            value ="moveSpecialObjects/{targetObjectClassName}/{targetObjectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void moveSpecialObjects(
            @PathVariable(RestConstants.TARGET_OBJECT_CLASS_NAME) String targetObjectClassName,
            @PathVariable(RestConstants.TARGET_OBJECT_ID) String targetObjectId,
            @RequestBody HashMap<String, String[]> objects,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("moveSpecialObjects", "127.0.0.1", sessionId);
            bem.moveSpecialObjects(
                    targetObjectClassName,
                    targetObjectId,
                    objects != null ? objects : new HashMap<String, String[]>()
            );
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
     * Checks recursively if it's safe to delete a single object.
     * @param className Object's class name.
     * @param objectId Object's id.
     * @param sessionId The session token id.
     * @return True if the object does not have relationships that keep it from being deleted. False otherwise.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="canDeleteObject/{className}/{objectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean canDeleteObject(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("canDeleteObject", "127.0.0.1", sessionId);
            return bem.canDeleteObject(className, objectId);
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
     * Deletes a set of objects.
     * @param objects A HashMap where the class name is the key and the value is a list of long containing the ids of the objects to be deleted that are instance of the key class.
     * @param releaseRelationships If all the relationships should be release upon deleting the objects. If false, an OperationNotPermittedException  will be raised if the object has incoming relationships.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE,
            value ="deleteObjects/{releaseRelationships}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteObjects(
            @RequestBody HashMap<String, List<String>> objects,
            @PathVariable(RestConstants.RELEASE_RELATIONSHIPS) boolean releaseRelationships,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteObjects", "127.0.0.1", sessionId);
            bem.deleteObjects(
                    objects != null ? objects : new HashMap<String, List<String>>(),
                    releaseRelationships
            );
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
     * Deletes a single object.
     * @param className Object's class name.
     * @param objectId Object's id.
     * @param releaseRelationships Release relationships automatically. If set to false, it will fail if the object already has incoming relationships.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE,
            value ="deleteObject/{className}/{objectId}/{releaseRelationships}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteObject(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.RELEASE_RELATIONSHIPS) boolean releaseRelationships,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteObject", "127.0.0.1", sessionId);
            bem.deleteObject(className, objectId, releaseRelationships);
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
     * Updates an object attributes. Note that you can't set binary attributes through this method. Use setBinaryAttributes instead.
     * @param className Object's class name.
     * @param objectId Object's id.
     * @param attributes The attributes to be updated (the key is the attribute name, 
     * the value is and array with the value -or values in case of MANY TO MANY list type attributes).
     * @param sessionId The session token id.
     * @return The summary of the changes that were made.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value ="updateObject/{className}/{objectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateObject(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @RequestBody HashMap<String, String> attributes,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateObject", "127.0.0.1", sessionId);
            return bem.updateObject(className, objectId, attributes);
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
     * Move a list of objects to a new parent: this methods ignores those who can't be moved and raises
     * an OperationNotPermittedException, however, it will move those which can be moved.
     * @param targetObjectClassName Parent's class name.
     * @param targetObjectId Parent's id.
     * @param objects Map using the object class name as keys and the respective objects ids as values.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value ="moveObjectsToPool/{targetObjectClassName}/{targetObjectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void moveObjectsToPool(
            @PathVariable(RestConstants.TARGET_OBJECT_CLASS_NAME) String targetObjectClassName,
            @PathVariable(RestConstants.TARGET_OBJECT_ID) String targetObjectId,
            @RequestBody HashMap<String, String[]> objects,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("moveObjectsToPool", "127.0.0.1", sessionId);
            bem.moveObjectsToPool(targetObjectClassName, targetObjectId, objects);
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
    // </editor-fold>
    
    // <editor-fold desc="pool-item" defaultstate="collapsed">
    
    /**
     * Creates an object inside a pool.
     * @param poolId Parent pool id.
     * @param className Class this object is going to be instance of.
     * @param attributes The list of attributes to be set initially. The values are serialized objects.
     * @param templateId The id of the template to be used to create this object.
     * This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before.
     * Use a "null" or empty string to not use a template.
     * @param sessionId The session token id.
     * @return The id of the newly created object.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "createPoolItem/{poolId}/{className}/{templateId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createPoolItem(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @RequestBody HashMap<String, String> attributes,
            @PathVariable(RestConstants.TEMPLATE_ID) String templateId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createPoolItem", "127.0.0.1", sessionId);
            return bem.createPoolItem(
                    poolId,
                    className,
                    attributes,
                    templateId.equals("null") ? null : templateId
            );
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
     * Copy a pool item from a pool to another pool.
     * @param poolId The id of the pool node.
     * @param poolItemClassName The class name for the pool item.
     * @param poolItemId The id for the pool item.
     * @param recursive If this operation should also copy the children objects recursively.
     * @param sessionId The session token id.
     * @return The newly created object id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "copyPoolItem/{poolId}/{poolItemClassName}/{poolItemId}/{recursive}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String copyPoolItem(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.POOL_ITEM_CLASS_NAME) String poolItemClassName,
            @PathVariable(RestConstants.POOL_ITEM_ID) String poolItemId,
            @PathVariable(RestConstants.RECURSIVE) boolean recursive,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("copyPoolItem", "127.0.0.1", sessionId);
            return bem.copyPoolItem(poolId, poolItemClassName, poolItemId, recursive);
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
     * Move a pool item from a pool to another pool.
     * @param poolId The id of the pool node.
     * @param poolItemClassName The class name for the pool item.
     * @param poolItemId The id for the pool item.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value ="movePoolItem/{poolId}/{poolItemClassName}/{poolItemId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void movePoolItem(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.POOL_ITEM_CLASS_NAME) String poolItemClassName,
            @PathVariable(RestConstants.POOL_ITEM_ID) String poolItemId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("movePoolItem", "127.0.0.1", sessionId);
            bem.movePoolItem(poolId, poolItemClassName, poolItemId);
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
     * Retrieves the list of objects from a pool.
     * @param poolId Parent pool id.
     * @param limit The results limit. Per page 0 to avoid the limit.
     * @param sessionId The session token id.
     * @return The list of items inside the pool.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getPoolItems/{poolId}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getPoolItems(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getPoolItems", "127.0.0.1", sessionId);
            return bem.getPoolItems(poolId, limit);
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
     * Retrieves the list of objects from a pool.
     * @param poolId Parent pool id.
     * @param className A given className to retrieve a set of objects of that className form the pool
     * used when the pool is a Generic class and could have objects of different class.
     * @param page The number of values of the result to skip or the page 0 to avoid.
     * @param limit The results limit. per page 0 to avoid the limit.
     * @param sessionId The session token id.
     * @return The list of items inside the pool.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getPoolItemsByClassName/{poolId}/{className}/{page}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getPoolItemsByClassName(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PAGE) int page,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getPoolItemsByClassName", "127.0.0.1", sessionId);
            return bem.getPoolItemsByClassName(poolId, className, page, limit);
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
     * Count the total of objects in a pool.
     * @param poolId Parent pool id.
     * @param className A given className to retrieve a set of objects of that className form the pool
     * used when the pool is a Generic class and could have objects of different class.
     * @param sessionId The session token id.
     * @return The count of items inside the pool.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getPoolItemsCount/{poolId}/{className}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getPoolItemsCount(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getPoolItemsCount", "127.0.0.1", sessionId);
            return bem.getPoolItemsCount(poolId, className);
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
    
    // <editor-fold desc="attributes" defaultstate="collapsed">
    
    /**
     * Utility method that returns the value of an attribute of a given object as a string. In date-type attributes, it will return 
     * the formatted dated, while in list types, it will return the name of the linked element.
     * @param objectClassName The class of the object.
     * @param objectId The id of the object.
     * @param attributeName The attribute whose value will be retrieved.
     * @param sessionId The session token id.
     * @return The value of the requested attribute. Null values are possible.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getAttributeValueAsString/{objectClassName}/{objectId}/{attributeName}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getAttributeValueAsString(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.ATTRIBUTE_NAME) String attributeName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getAttributeValueAsString", "127.0.0.1", sessionId);
            return bem.getAttributeValueAsString(objectClassName, objectId, attributeName);
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
     * Fetches the attributes of an inventory object (and their values) and returns them as strings. This is useful mainly to display property sheets and reports, 
     * so it's not necessary to always check if an attribute is a list type and retrieve its string representation.
     * @param objectClassName The class of the object.
     * @param objectId The id of the object.
     * @param sessionId The session token id.
     * @return A dictionary with the name of the attributes and their values represented as strings.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getAttributeValuesAsString/{objectClassName}/{objectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HashMap<String, String> getAttributeValuesAsString(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getAttributeValuesAsString", "127.0.0.1", sessionId);
            return bem.getAttributeValuesAsString(objectClassName, objectId);
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
     * Gets the value of a special attribute.A special attribute is one belonging to a business domain specific attribute
     * (usually a model. Domain specific attribute information is not filed under the standard metadata but a special one. Implementations may vary).
     * @param objectClassName Object's class name.
     * @param objectId Object's id.
     * @param specialAttributeName Special attribute name.
     * @param sessionId The session token id.
     * @return A list of objects related to the object through a special relationship. An empty array if the object provided is not related to others using that relationship.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getSpecialAttribute/{objectClassName}/{objectId}/{specialAttributeName}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSpecialAttribute(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SPECIAL_ATTRIBUTE_NAME) String specialAttributeName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSpecialAttribute", "127.0.0.1", sessionId);
            return bem.getSpecialAttribute(objectClassName, objectId, specialAttributeName);
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
     * Returns the specified special relationships of a given object as a hashmap whose keys are the names of the relationships and the values the list of related objects.
     * If no filter (attributeNames) is provided, all special attributes (relationships) will be returned.
     * @param objectClassName Object class name.
     * @param objectId Object Id.
     * @param attributeNames The list of special attributes (relationships) to be fetched. if none provided, the method will return all of them.
     * @param sessionId The session token id.
     * @return The hash map with the existing special relationships and the associated objects.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getSpecialAttributes/{objectClassName}/{objectId}/{attributeNames}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HashMap<String,List<BusinessObjectLight>> getSpecialAttributes(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.ATTRIBUTE_NAMES) String[] attributeNames,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSpecialAttributes", "127.0.0.1", sessionId);
            return bem.getSpecialAttributes(objectClassName, objectId, attributeNames);
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
     * This method will extract the object at the other side of the special relationship and all the properties of the relationship itself.
     * @param objectClassName The class of the object whose special attribute will be retrieved from.
     * @param objectId The object's id.
     * @param specialAttributeName The name of the special attribute.
     * @param sessionId The session token id.
     * @return The list of elements related with such relationship plus the properties of theirs relationships.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getAnnotatedSpecialAttribute/{objectClassName}/{objectId}/{specialAttributeName}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<AnnotatedBusinessObjectLight> getAnnotatedSpecialAttribute(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SPECIAL_ATTRIBUTE_NAME) String specialAttributeName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getAnnotatedSpecialAttribute", "127.0.0.1", sessionId);
            return bem.getAnnotatedSpecialAttribute(objectClassName, objectId, specialAttributeName);
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
     * Check if an object has a given special attribute.
     * @param objectClassName Object class name.
     * @param objectId Object id.
     * @param attributeName Attribute name.
     * @param sessionId The session token id.
     * @return True if the object has special attributes.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="hasSpecialAttribute/{objectClassName}/{objectId}/{attributeName}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean hasSpecialAttribute(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.ATTRIBUTE_NAME) String attributeName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("hasSpecialAttribute", "127.0.0.1", sessionId);
            return bem.hasSpecialAttribute(objectClassName, objectId, attributeName);
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
    // </editor-fold>
    
    // <editor-fold desc="parents" defaultstate="collapsed">
    /**
     * Finds the common parent between two objects.
     * @param aObjectClassName Object A class name.
     * @param aObjectId Object A id.
     * @param bObjectClassName Object B class name.
     * @param bObjectId Object B id.
     * @param sessionId The session token id.
     * @return The common parent or null if none.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getCommonParent/{aObjectClassName}/{aObjectId}/{bObjectClassName}/{bObjectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getCommonParent(
            @PathVariable(RestConstants.A_OBJECT_CLASS_NAME) String aObjectClassName,
            @PathVariable(RestConstants.A_OBJECT_ID) String aObjectId,
            @PathVariable(RestConstants.B_OBJECT_CLASS_NAME) String bObjectClassName,
            @PathVariable(RestConstants.B_OBJECT_ID) String bObjectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getCommonParent", "127.0.0.1", sessionId);
            return bem.getCommonParent(aObjectClassName, aObjectId, bObjectClassName, bObjectId);
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
     * Gets the parent of a given object in the containment hierarchy.
     * @param objectClassName Object class name.
     * @param objectId Object id.
     * @param sessionId The session token id.
     * @return The immediate parent. Null if the parent is null. A dummy object with id -1 if the parent is DummyRoot.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getParent/{objectClassName}/{objectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getParent(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getParent", "127.0.0.1", sessionId);
            return bem.getParent(objectClassName, objectId);
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
     * Retrieves recursively the list of parents of an object in the containment hierarchy.
     * @param objectClassName Object class name.
     * @param objectId Object id.
     * @param sessionId The session token id.
     * @return The list of parents.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getParents/{objectClassName}/{objectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getParents(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getParents", "127.0.0.1", sessionId);
            return bem.getParents(objectClassName, objectId);
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
     * Gets the list of parents (according to the special and standard containment hierarchy) until it finds an instance of class 
     * objectToMatchClassName (for example "give me the parents of this port until you find the nearest rack").
     * @param objectClassName Class of the object to get the parents from.
     * @param objectId Id of the object to get the parents from.
     * @param objectToMatchClassNames Classes of the objects that will limit the search. It can be a superclass, if you want to match many classes at once.
     * @param sessionId The session token id.
     * @return The list of parents until an instance of objectToMatchClassName is found. If no instance of that class is found, all parents until the Dummy Root will be returned.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getParentsUntilFirstOfClass/{objectClassName}/{objectId}/{objectToMatchClassNames}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getParentsUntilFirstOfClass(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.OBJECT_TO_MATCH_CLASS_NAMES) String[] objectToMatchClassNames,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getParentsUntilFirstOfClass", "127.0.0.1", sessionId);
            return bem.getParentsUntilFirstOfClass(objectClassName, objectId, objectToMatchClassNames);
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
     * Gets the first occurrence of a parent with a given class (according to the special and standard containment hierarchy)
     * (for example "give me the parent of this port until you find the nearest rack").
     * @param objectClassName Class of the object to get the parent from.
     * @param objectId Id of the object to get the parent from.
     * @param objectToMatchClassName Class of the object that will limit the search. It can be a superclass, if you want to match many classes at once.
     * @param sessionId The session token id.
     * @return The the first occurrence of a parent with a given class. If no instance of that class is found, the child of Dummy Root related in this hierarchy will be returned.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getFirstParentOfClass/{objectClassName}/{objectId}/{objectToMatchClassName}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObject getFirstParentOfClass(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.OBJECT_TO_MATCH_CLASS_NAME) String objectToMatchClassName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getFirstParentOfClass", "127.0.0.1", sessionId);
            return bem.getFirstParentOfClass(objectClassName, objectId, objectToMatchClassName);
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
     * Gets the parents of an object that breaks the containment rule of having only one parent. For example the links and containers in the Outside Plant Module.
     * @param objectClassName Object Class Name.
     * @param objectId Object Id.
     * @param sessionId The session token id.
     * @return The set of parents.
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="getMultipleParents/{objectClassName}/{objectId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getMultipleParents(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getMultipleParents", "127.0.0.1", sessionId);
            return bem.getMultipleParents(objectClassName, objectId);
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
     * Checks if a given object is parent to another, according to the standard or special containment hierarchy.
     * @param parentClassName Alleged parent Class Name.
     * @param parentId Alleged parent id.
     * @param childClassName Child Class Name.
     * @param childId Child Id.
     * @param sessionId The session token id.
     * @return True if the given parent has the given child (according to the special and standard containment hierarchy).
     */
    @RequestMapping(method = RequestMethod.GET,
            value ="isParent/{parentClassName}/{parentId}/{childClassName}/{childId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean isParent(
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @PathVariable(RestConstants.CHILD_CLASS_NAME) String childClassName,
            @PathVariable(RestConstants.CHILD_ID)  String childId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("isParent", "127.0.0.1", sessionId);
            return bem.isParent(parentClassName, parentId, childClassName, childId);
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
    
    // <editor-fold desc="pools" defaultstate="collapsed">
    /**
     * Retrieves the pools that don't have any parent and are normally intended to be managed by the Pool Manager.
     * @param className The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use "null" if you want to get all.
     * @param type The type of pools that should be retrieved. Root pools can be for general purpose, or as roots in models.
     * @param includeSubclasses Use <code>true</code> if you want to get only the pools whose <code>className</code> property matches exactly the one provided, and <code>false</code> if you want to also include the subclasses.
     * @param sessionId The session token id.
     * @return A set of pools.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getRootPools/{className}/{type}/{includeSubclasses}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<InventoryObjectPool> getRootPools(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.TYPE) int type,
            @PathVariable(RestConstants.INCLUDE_SUBCLASSES) boolean includeSubclasses,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getRootPools", "127.0.0.1", sessionId);
            return bem.getRootPools(
                    className.equals("null") ? null : className,
                    type,
                    includeSubclasses
            );
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
     * Retrieves the pools associated to a particular object.
     * @param objectClassName The parent object class name.
     * @param objectId The parent object id.
     * @param poolClassName The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. 
     * Use "null" if you want to get all.
     * @param sessionId The session token id.
     * @return A set of pools.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getPoolsInObject/{objectClassName}/{objectId}/{poolClassName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<InventoryObjectPool> getPoolsInObject(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.POOL_CLASS_NAME) String poolClassName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getPoolsInObject", "127.0.0.1", sessionId);
            return bem.getPoolsInObject(
                    objectClassName,
                    objectId,
                    poolClassName.equals("null") ? null : poolClassName
            );
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
     * Retrieves the pools associated to a particular pool.
     * @param parentPoolId The parent pool id.
     * @param poolClassName The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned.
     * @param sessionId The session token id.
     * @return A set of pools.
     */
    @RequestMapping(method = RequestMethod.GET, value ="getPoolsInPool/{parentPoolId}/{poolClassName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<InventoryObjectPool> getPoolsInPool(
            @PathVariable(RestConstants.PARENT_POOL_ID) String parentPoolId,
            @PathVariable(RestConstants.POOL_CLASS_NAME) String poolClassName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getPoolsInPool", "127.0.0.1", sessionId);
            return bem.getPoolsInPool(parentPoolId, poolClassName);
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
     * Suggests a number of pools based on a search string (the pool name).This search string will be case-insensitive-matched against the name of 
     * the objects and classes in the inventory attributes to filter.
     * @param classNames List <ClassMetadataLight> classesToFilter a list of classes to limit the search. Used "null" for none.
     * @param filterValue Value to filter in the attribute name of every business object name or class name o class display name.
     * @param poolSkip Skip index.
     * @param poolLimit Max count of child.
     * @param objectSkip Object skip index.
     * @param objectLimit Max count of objects.
     * @param sessionId The session token id.
     * @return Set of pools.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getSuggestedPoolsByName/{classNames}/{filterValue}/{poolSkip}/{poolLimit}/{objectSkip}/{objectLimit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HashMap<String, List<InventoryObjectPool>> getSuggestedPoolsByName(
            @PathVariable(RestConstants.CLASS_NAMES) List<String> classNames,
            @PathVariable(RestConstants.FILTER_VALUE) String filterValue,
            @PathVariable(RestConstants.POOL_SKIP) long poolSkip,
            @PathVariable(RestConstants.POOL_LIMIT) long poolLimit,
            @PathVariable(RestConstants.OBJECT_SKIP) long objectSkip,
            @PathVariable(RestConstants.OBJECT_LIMIT) long objectLimit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSuggestedPoolsByName", "127.0.0.1", sessionId);
            return bem.getSuggestedPoolsByName(
                    classNames.get(0).equals("null") ? null : classNames,
                    filterValue,
                    poolSkip,
                    poolLimit,
                    objectSkip,
                    objectLimit
            );
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
     * Retrieves the total count of pools associated to a particular pool.
     * @param parentPoolId The parent pool id.
     * @param poolClassName The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned.
     * @param sessionId The session token id.
     * @return The total count of the pools.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getPoolsInPoolCount/{parentPoolId}/{poolClassName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getPoolsInPoolCount(
            @PathVariable(RestConstants.PARENT_POOL_ID) String parentPoolId,
            @PathVariable(RestConstants.POOL_CLASS_NAME) String poolClassName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getPoolsInPoolCount", "127.0.0.1", sessionId);
            return bem.getPoolsInPoolCount(parentPoolId, poolClassName);
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
     * Gets a pool by its id.
     * @param poolId The pool's id.
     * @param sessionId The session token id.
     * @return The pool as a Pool object.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getPool/{poolId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public InventoryObjectPool getPool(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getPool", "127.0.0.1", sessionId);
            return bem.getPool(poolId);
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
    
    // <editor-fold desc="warehouses" defaultstate="collapsed">
    /**
     * Gets the warehouses in a object.
     * @param objectClassName The class of the object.
     * @param objectId The id of the object.
     * @param sessionId The session token id.
     * @return Gets the warehouses in a object.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getWarehousesInObject/{objectClassName}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getWarehousesInObject(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getWarehousesInObject", "127.0.0.1", sessionId);
            return bem.getWarehousesInObject(objectClassName, objectId);
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
     * Gets the physical node of a warehouse item.
     * @param objectClassName The class of the object.
     * @param objectId The id of the object.
     * @param sessionId The session token id.
     * @return Gets the physical node of a warehouse item.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getPhysicalNodeToObjectInWarehouse/{objectClassName}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getPhysicalNodeToObjectInWarehouse(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getPhysicalNodeToObjectInWarehouse", "127.0.0.1", sessionId);
            return bem.getPhysicalNodeToObjectInWarehouse(objectClassName, objectId);
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
     * Gets warehouse related to object.
     * @param objectClassName The class of the object.
     * @param objectId The id of the object.
     * @param sessionId The session token id.
     * @return Gets warehouse related to object.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getWarehouseToObject/{objectClassName}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getWarehouseToObject(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getWarehouseToObject", "127.0.0.1", sessionId);
            return bem.getWarehouseToObject(objectClassName, objectId);
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
    // </editor-fold>
    
    // <editor-fold desc="children" defaultstate="collapsed">
    /**
     * Gets the direct children of a given object of a given class.
     * @param parentId Parent id.
     * @param parentClassName Parent class name.
     * @param classNameToFilter Class name to be match against.
     * @param page The number of page of the number of elements to skip.
     * @param maxResults Max number of results. 0 to get all.
     * @param sessionId The session token id.
     * @return A list of children of parentid/parentClass instance that are instances of classNameToFilter.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getChildrenOfClass/{parentId}/{parentClassName}/{classNameToFilter}/{page}/{maxResults}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObject> getChildrenOfClass(
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.CLASS_NAME_TO_FILTER) String classNameToFilter,
            @PathVariable(RestConstants.PAGE) int page,
            @PathVariable(RestConstants.MAX_RESULTS) int maxResults,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getChildrenOfClass", "127.0.0.1", sessionId);
            return bem.getChildrenOfClass(parentId, parentClassName, classNameToFilter, page, maxResults);
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
     * Returns the special children of a given object as RemoteBusinessObjectLight instances.This method is not recursive.
     * @param parentId The id of the parent object.
     * @param parentClassName The class name of the parent object.
     * @param classNameToFilter The superclass/class to be used to filter the results. You can also use abstract superclasses.
     * @param maxResults The max number of results to fetch. Use -1 to retrieve all.
     * @param sessionId The session token id.
     * @return The list of special children of the given object, filtered using classNameToFilter.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getSpecialChildrenOfClassLight/{parentId}/{parentClassName}/{classNameToFilter}/{maxResults}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSpecialChildrenOfClassLight(
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.CLASS_NAME_TO_FILTER) String classNameToFilter,
            @PathVariable(RestConstants.MAX_RESULTS) int maxResults,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSpecialChildrenOfClassLight", "127.0.0.1", sessionId);
            return bem.getSpecialChildrenOfClassLight(parentId, parentClassName, classNameToFilter, maxResults);
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
     * Gets all class and abstract class children of a given class to filter in 
     * a hierarchy with root in the given parent.i.e.: all the ports in Router, all the Routers in a City.
     * @param parentId Object id of the root parent of the hierarchy.
     * @param parentClassName Class name of the root parent of the hierarchy.
     * @param classNameToFilter Class name of the expected children.
     * @param attributes If filtering by the attributes of the retrieved objects.
     * @param page The page or the number of elements to skip, no pagination -1.
     * @param limit Maximum number of results, -1 no limit.
     * @param sessionId The session token id.
     * @return The list of object instance of the given class to filter.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "getChildrenOfClassLightRecursive/{parentId}/{parentClassName}/{classNameToFilter}/{page}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getChildrenOfClassLightRecursive(
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.CLASS_NAME_TO_FILTER) String classNameToFilter,
            @RequestBody(required = false) HashMap<String, String> attributes,
            @PathVariable(RestConstants.PAGE) int page,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getChildrenOfClassLightRecursive", "127.0.0.1", sessionId);
            return bem.getChildrenOfClassLightRecursive(parentId, parentClassName,
                    classNameToFilter, attributes, page, limit);
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
     * Gets all class and abstract class special children of a given class to filter 
     * in a hierarchy with root in the given parent.Use case: used in some class level and inventory level reports script.
     * @param parentId Object id of the root parent of the hierarchy.
     * @param parentClassName Class name of the root parent of the hierarchy.
     * @param classNameToFilter Class name of the expected children.
     * @param maxResults Maximum number of results, -1 no limit.
     * @param sessionId The session token id.
     * @return The list of object instance of the given class to filter.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getSpecialChildrenOfClassLightRecursive/{parentId}/{parentClassName}/{classNameToFilter}/{maxResults}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSpecialChildrenOfClassLightRecursive(
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.CLASS_NAME_TO_FILTER) String classNameToFilter,
            @PathVariable(RestConstants.MAX_RESULTS) int maxResults,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSpecialChildrenOfClassLightRecursive", "127.0.0.1", sessionId);
            return bem.getSpecialChildrenOfClassLightRecursive(parentId, parentClassName, classNameToFilter, maxResults);
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
     * Same as getChildrenOfClass, but returns only the light version of the objects.
     * @param parentId Parent id.
     * @param parentClassName Parent class name.
     * @param classNameToFilter Class name to be match against.
     * @param maxResults Max number of results. 0 to get all.
     * @param sessionId The session token id.
     * @return A list of children of parentid/parentClass instance, instances of classToFilter.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getChildrenOfClassLight/{parentId}/{parentClassName}/{classNameToFilter}/{maxResults}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getChildrenOfClassLight(
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.CLASS_NAME_TO_FILTER) String classNameToFilter,
            @PathVariable(RestConstants.MAX_RESULTS) int maxResults,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getChildrenOfClassLight", "127.0.0.1", sessionId);
            return bem.getChildrenOfClassLight(parentId, parentClassName, classNameToFilter, maxResults);
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
     * Counts if an object has children.
     * @param objectClassName Object class name.
     * @param objectId Object id.
     * @param sessionId The session token id.
     * @return Number of children.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "countChildren/{objectClassName}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long countChildren(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName, 
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("countChildren", "127.0.0.1", sessionId);
            return bem.countChildren(objectClassName, objectId);
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
     * Counts if an object has special children.
     * @param objectClassName Object class name.
     * @param objectId Object id.
     * @param sessionId The session token id.
     * @return Number of special children.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "countSpecialChildren/{objectClassName}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long countSpecialChildren(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName, 
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("countSpecialChildren", "127.0.0.1", sessionId);
            return bem.countSpecialChildren(objectClassName, objectId);
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
    // </editor-fold>
    
    // <editor-fold desc="siblings" defaultstate="collapsed">
    /**
     * Gets the siblings of a given object in the containment hierarchy.
     * @param objectClassName Object class name.
     * @param objectId Object id.
     * @param maxResults Max number of results to be returned.
     * @param sessionId The session token id.
     * @return List of siblings.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getSiblings/{objectClassName}/{objectId}/{maxResults}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSiblings(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName, 
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.MAX_RESULTS) int maxResults,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSiblings", "127.0.0.1", sessionId);
            return bem.getSiblings(objectClassName, objectId, maxResults);
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
    // </editor-fold>
    
    // <editor-fold desc="attachments" defaultstate="collapsed">
    /**
     * Relates a file to an inventory object.
     * @param name The name of the file.
     * @param tags The tags that describe the contents of the file.
     * @param file The file itself as string Base64.
     * @param className The class of the object the file will be attached to.
     * @param objectId The id of the object the file will be attached to.
     * @param sessionId The session token id.
     * @return The id of the resulting file object.
     */
    @RequestMapping(method = RequestMethod.POST, 
            value = "attachFileToObject/{name}/{tags}/{file}/{className}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long attachFileToObject(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.TAGS) String tags,
            @PathVariable(RestConstants.FILE) String file,
            @PathVariable(RestConstants.CLASS_NAME) String className, 
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("attachFileToObject", "127.0.0.1", sessionId);
            if (RestUtil.isBase64(file)) {
                return bem.attachFileToObject(
                        name,
                        tags,
                        Base64.decodeBase64(file),
                        className,
                        objectId
                );
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "file", "attachFileToObject"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "file", "attachFileToObject"));
            }
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
    @RequestMapping(method = RequestMethod.GET, 
            value = "getFilesForObject/{className}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<FileObjectLight> getFilesForObject(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getFilesForObject", "127.0.0.1", sessionId);
            return bem.getFilesForObject(className, objectId);
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
     * Retrieves a particular file associated to an inventory object.This call returns the actual file.
     * @param fileObjectId The id of the file object.
     * @param className The class of the object the file is associated to.
     * @param objectId The id of the object the file is associated to.
     * @param sessionId The session token id.
     * @return The file.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value = "getFile/{fileObjectId}/{className}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public FileObject getFile(
            @PathVariable(RestConstants.FILE_OBJECT_ID) long fileObjectId,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getFile", "127.0.0.1", sessionId);
            return bem.getFile(fileObjectId, className, objectId);
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
     * Releases (and deletes) a file associated to an inventory object.
     * @param fileObjectId The id of the file object.
     * @param className The class of the object the file is associated to.
     * @param objectId The id of the object the file is associated to.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, 
            value = "detachFileFromObject/{fileObjectId}/{className}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void detachFileFromObject(
            @PathVariable(RestConstants.FILE_OBJECT_ID) long fileObjectId,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("detachFileFromObject", "127.0.0.1", sessionId);
            bem.detachFileFromObject(fileObjectId, className, objectId);
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
     * Updates the properties of a file object (name or tags).
     * @param fileObjectId The id of the file object.
     * @param properties The set of properties as a dictionary key-value. Valid keys are "name" and "tags".
     * @param className The class of the object the file is attached to.
     * @param objectId The id of the object the file is attached to.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, 
            value = "updateFileProperties/{fileObjectId}/{className}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateFileProperties(
            @PathVariable(RestConstants.FILE_OBJECT_ID) long fileObjectId,
            @RequestBody List<StringPair> properties,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateFileProperties", "127.0.0.1", sessionId);
            bem.updateFileProperties(fileObjectId, properties, className, objectId);
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
     * Retrieves a map with the files related to the list type item attributes of the given object.
     * @param objectId The object id.
     * @param sessionId The session token id.
     * @return The map with the files. The key is the list type item and the value a list with the related files.
     */
    @RequestMapping(method = RequestMethod.GET, 
            value = "getFilesFromRelatedListTypeItems/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map<BusinessObjectLight, List<FileObjectLight>> getFilesFromRelatedListTypeItems(
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getFilesFromRelatedListTypeItems", "127.0.0.1", sessionId);
            return bem.getFilesFromRelatedListTypeItems(objectId);
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