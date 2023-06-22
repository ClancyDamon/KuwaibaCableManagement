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
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ArraySizeMismatchException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
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
 * Template Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(TemplateRestController.PATH)
public class TemplateRestController implements TemplateRestOpenApi {
    
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
    public static Logger LOG = LoggerFactory.getLogger(TemplateRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/template-manager/"; //NOI18N
    
    // <editor-fold desc="template-manager" defaultstate="collapsed">
    
    /**
     * Creates a template.
     * @param className The class you want to create a template for.
     * @param name The name of the template. It can not be null.
     * @param sessionId The session token id.
     * @return The id of the newly created template.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createTemplate/{className}/{name}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createTemplate(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createTemplate", "127.0.0.1", sessionId);
            return aem.createTemplate(className, name);
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
     * Creates an object inside a template.
     * @param className Class of the object you want to create.
     * @param parentClassName Class of the parent to the object you want to create.
     * @param parentId Id of the parent to the object you want to create.
     * @param name Name of the element.
     * @param sessionId The session token id.
     * @return The id of the new object.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createTemplateElement/{className}/{parentClassName}/{parentId}/{name}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createTemplateElement(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createTemplateElement", "127.0.0.1", sessionId);
            return aem.createTemplateElement(className, parentClassName, parentId, name);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
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
     * Create an special object inside an template.
     * @param className Template special element class.
     * @param parentClassName Template special element parent class name.
     * @param parentId Template special element parent Id.
     * @param name Template special element name.
     * @param sessionId The session token id.
     * @return The id of the new object.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createTemplateSpecialElement/{className}/{parentClassName}/{parentId}/{name}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createTemplateSpecialElement(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createTemplateSpecialElement", "127.0.0.1", sessionId);
            return aem.createTemplateSpecialElement(className, parentClassName, parentId, name);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
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
     * Creates multiple template elements using a given name pattern.
     * @param className The class name of the new set of template elements.
     * @param parentClassName The parent class name of the new set of template elements.
     * @param parentId The parent id of the new set of template elements.
     * @param namePattern Name pattern of the new set of template elements.
     * @param sessionId The session token id.
     * @return An array of ids for the new template elements.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createBulkTemplateElement/{className}/{parentClassName}/{parentId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] createBulkTemplateElement(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @RequestBody String namePattern,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createBulkTemplateElement", "127.0.0.1", sessionId);
            return aem.createBulkTemplateElement(className, parentClassName, parentId, namePattern);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
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
     * Creates multiple special template elements using a given name pattern.
     * @param className The class name of the new set of special template elements.
     * @param parentClassName The parent class name of the new set of special template elements.
     * @param parentId The parent id of the new set of special template elements.
     * @param namePattern Name pattern of the new set of special template elements.
     * @param sessionId The session token id.
     * @return An array if ids for the new special template elements.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createBulkSpecialTemplateElement/{className}/{parentClassName}/{parentId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] createBulkSpecialTemplateElement(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.PARENT_ID) String parentId,
            @RequestBody String namePattern,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createBulkSpecialTemplateElement", "127.0.0.1", sessionId);
            return aem.createBulkSpecialTemplateElement(className, parentClassName, parentId, namePattern);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
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
     * Updates the value of an attribute of a template element.
     * @param className Class of the element you want to update.
     * @param id Id of the element you want to update.
     * @param attributeNames Names of the attributes that you want to be updated as an array of strings.
     * @param attributeValues The values of the attributes you want to update. For list types, it's the id of the related type.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateTemplateElement/{className}/{id}/{attributeNames}/{attributeValues}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateTemplateElement(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.ATTRIBUTE_NAMES) String[] attributeNames,
            @PathVariable(RestConstants.ATTRIBUTE_VALUES) String[] attributeValues,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateTemplateElement", "127.0.0.1", sessionId);
            return aem.updateTemplateElement(className, id, attributeNames, attributeValues);
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
     * Deletes an element within a template or a template itself.
     * @param className The template element class.
     * @param id The template element id.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteTemplateElement/{className}/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor deleteTemplateElement(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteTemplateElement", "127.0.0.1", sessionId);
            return aem.deleteTemplateElement(className, id);
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
     * Gets the templates available for a given class.
     * @param className Class whose templates we need.
     * @param sessionId The session token id.
     * @return A list of templates (actually, the top element) as a list of RemoteObjects.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getTemplatesForClass/{className}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<TemplateObjectLight> getTemplatesForClass(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getTemplatesForClass", "127.0.0.1", sessionId);
            return aem.getTemplatesForClass(className);
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
     * Retrieves the children of a given template element.
     * @param className Template element class.
     * @param id Template element id.
     * @param sessionId The session token id.
     * @return The template element's children as a list of RemoteBusinessObjectLight instances.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getTemplateElementChildren/{className}/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<TemplateObjectLight> getTemplateElementChildren(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getTemplateElementChildren", "127.0.0.1", sessionId);
            return aem.getTemplateElementChildren(className, id);
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
     * Retrieves the children of a given template special element.
     * @param className Template special element class.
     * @param id Template special element id.
     * @param sessionId The session token id.
     * @return The template element's children as a list of RemoteBusinessObjectLight instances.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getTemplateSpecialElementChildren/{className}/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<TemplateObjectLight> getTemplateSpecialElementChildren(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getTemplateSpecialElementChildren", "127.0.0.1", sessionId);
            return aem.getTemplateSpecialElementChildren(className, id);
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
     * Retrieves all the information of a given template element.
     * @param className Template element class.
     * @param id Template element id.
     * @param sessionId The session token id.
     * @return The template element information.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getTemplateElement/{className}/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public TemplateObject getTemplateElement(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.ID) String id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getTemplateElement", "127.0.0.1", sessionId);
            return aem.getTemplateElement(className, id);
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
     * Copy template elements within templates. Should not be used to copy entire templates.
     * @param sourceObjectsClassNames Array with the class names of the elements to be copied.
     * @param sourceObjectsIds  Array with the ids of the elements to be copied.
     * @param newParentClassName Class of the parent of the copied objects.
     * @param newParentId Id of the parent of the copied objects.
     * @param sessionId The session token id.
     * @return An array with the ids of the newly created elements in the same order they were provided.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "copyTemplateElements/{sourceObjectsClassNames}/{sourceObjectsIds}/{newParentClassName}/{newParentId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] copyTemplateElements(
            @PathVariable(RestConstants.SOURCE_OBJECTS_NAMES) String[] sourceObjectsClassNames,
            @PathVariable(RestConstants.SOURCE_OBJECTS_IDS) String[] sourceObjectsIds,
            @PathVariable(RestConstants.NEW_PARENT_CLASS_NAME) String newParentClassName,
            @PathVariable(RestConstants.NEW_PARENT_ID) String newParentId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("copyTemplateElements", "127.0.0.1", sessionId);
            return aem.copyTemplateElements(sourceObjectsClassNames, sourceObjectsIds, newParentClassName, newParentId);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (ArraySizeMismatchException ex) {
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
     * Copy template special elements within templates.
     * @param sourceObjectsClassNames Array with the class names of the special elements to be copied.
     * @param sourceObjectsIds Array with the ids of the special elements to be copied.
     * @param newParentClassName Class of the parent of the copied objects.
     * @param newParentId Id of the parent of the copied objects.
     * @param sessionId The session token id.
     * @return An array with the ids of the newly created special elements in the same order they were provided.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "copyTemplateSpecialElement/{sourceObjectsClassNames}/{sourceObjectsIds}/{newParentClassName}/{newParentId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] copyTemplateSpecialElement(
            @PathVariable(RestConstants.SOURCE_OBJECTS_NAMES) String[] sourceObjectsClassNames,
            @PathVariable(RestConstants.SOURCE_OBJECTS_IDS) String[] sourceObjectsIds,
            @PathVariable(RestConstants.NEW_PARENT_CLASS_NAME) String newParentClassName,
            @PathVariable(RestConstants.NEW_PARENT_ID) String newParentId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("copyTemplateSpecialElement", "127.0.0.1", sessionId);
            return aem.copyTemplateSpecialElement(sourceObjectsClassNames, sourceObjectsIds, newParentClassName, newParentId);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (ArraySizeMismatchException ex) {
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
    // </editor-fold>
}