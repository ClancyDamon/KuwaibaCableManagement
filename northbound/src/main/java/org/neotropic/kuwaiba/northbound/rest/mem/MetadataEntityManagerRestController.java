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
package org.neotropic.kuwaiba.northbound.rest.mem;

import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.DatabaseException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
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
 * Manages the metadata entities of the data model, such as classes and attributes.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(MetadataEntityManagerRestController.PATH)
public class MetadataEntityManagerRestController implements MetadataEntityManagerRestOpenApi {
    
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    
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
    public static Logger LOG = LoggerFactory.getLogger(MetadataEntityManagerRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and core
     */
    public static final String PATH = "/v2.1/core/mem/"; //NOI18N
    
    // <editor-fold desc="classes" defaultstate="collapsed">
    
    /**
     * Creates a class metadata with its attributes (some new and others inherited from the parent class).
     * @param classDefinition The class definition, name, display name, etc.
     * @param sessionId The session token id.
     * @return The Id of the newClassMetadata.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createClass/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createClass(
            @RequestBody ClassMetadata classDefinition,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createClass", "127.0.0.1", sessionId);
            return mem.createClass(classDefinition);
        } catch (DatabaseException | InvalidArgumentException ex) {
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
     * Changes a class metadata definition.
     * @param classId The id of the class. A class metadata definition can not be updated using the name as the key, because the name itself could change.
     * @param properties An HashMap<String, Object> with the properties to be updated. The possible key values are: name, color, displayName, description, icon<String>, 
     * smallIcon<String>, countable, abstract, inDesign and custom. See user manual for a more complete explanation on what each one of them are for.
     * @param sessionId The session token id.
     * @return The summary of the changes that were made.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "setClassProperties/{classId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor setClassProperties(
            @PathVariable(RestConstants.CLASS_ID) long classId,
            @RequestBody HashMap<String, Object> properties,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("setClassProperties", "127.0.0.1", sessionId);
            return mem.setClassProperties(classId, properties);
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
     * Deletes a class metadata, its attributes and category relationships.
     * @param className The class name.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteClass/{className}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteClass(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("deleteClass", "127.0.0.1", sessionId);
            mem.deleteClass(className);
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
     * Deletes a class metadata, its attributes and category relationships.
     * @param classId The class id.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteClassWithId/{classId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteClassWithId(
            @PathVariable(RestConstants.CLASS_ID) long classId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("deleteClassWithId", "127.0.0.1", sessionId);
            mem.deleteClass(classId);
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
     * Retrieves the simplified list of classes, This list won't include either those classes marked as dummy.
     * @param includeListTypes To indicate if the list should include the subclasses of GenericObjectList.
     * @param includeIndesign Include all the data model classes or only the classes in production.
     * @param sessionId The session token id.
     * @return The list of type classes.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getAllClassesLight/{includeListTypes}/{includeIndesign}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getAllClassesLight(
            @PathVariable(RestConstants.INCLUDE_LIST_TYPES) boolean includeListTypes,
            @PathVariable(RestConstants.INCLUDE_INDESIGN) boolean includeIndesign,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getAllClassesLight", "127.0.0.1", sessionId);
            return mem.getAllClassesLight(includeListTypes, includeIndesign);
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
     * Gets the subclasses of a given class recursively.
     * @param className The class name.
     * @param includeAbstractClasses Should the list include the abstract subclasses.
     * @param includeSelf Should the list include the subclasses and the parent class?
     * @param sessionId The session token id.
     * @return The list of subclasses.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getSubClassesLight/{className}/{includeAbstractClasses}/{includeSelf}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getSubClassesLight(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.INCLUDE_ABSTRACT_CLASSES) boolean includeAbstractClasses,
            @PathVariable(RestConstants.INCLUDE_SELF) boolean includeSelf,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSubClassesLight", "127.0.0.1", sessionId);
            return mem.getSubClassesLight(className, includeAbstractClasses, includeSelf);
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
     * Gets the direct subclasses of a given class.
     * @param className The class name.
     * @param includeAbstractClasses If abstract classes should be included.
     * @param includeSelf Also return the metadata of class <code>className</code>
     * @param sessionId The session token id.
     * @return The list of subclasses.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getSubClassesLightNoRecursive/{className}/{includeAbstractClasses}/{includeSelf}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getSubClassesLightNoRecursive(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.INCLUDE_ABSTRACT_CLASSES) boolean includeAbstractClasses,
            @PathVariable(RestConstants.INCLUDE_SELF) boolean includeSelf,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSubClassesLightNoRecursive", "127.0.0.1", sessionId);
            return mem.getSubClassesLightNoRecursive(className, includeAbstractClasses, includeSelf);
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
     * Retrieves the superclasses of a given class up to InventoryObject.
     * @param className The class to be evaluated.
     * @param includeSelf If the result should include the the very class that was provided in <code>className</code> parameter.
     * @param sessionId The session token id.
     * @return The list of super classes.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getSuperClassesLight/{className}/{includeSelf}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getSuperClassesLight(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.INCLUDE_SELF) boolean includeSelf,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSuperClassesLight", "127.0.0.1", sessionId);
            return mem.getSuperClassesLight(className, includeSelf);
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
     * Get the subclasses count given a parent class name.
     * @param className Parent class Name.
     * @param sessionId The session token id.
     * @return Number of direct subclasses.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getSubClassesCount/{className}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getSubClassesCount(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSubClassesCount", "127.0.0.1", sessionId);
            return mem.getSubClassesCount(className);
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
     * Retrieves all the class metadata except for classes marked as dummy.
     * @param includeListTypes Boolean to indicate if the list should include the subclasses of GenericObjectList.
     * @param includeIndesign Include classes marked as "in design".
     * @param sessionId The session token id.
     * @return An array with the metadata of the classes.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getAllClasses/{includeListTypes}/{includeIndesign}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadata> getAllClasses(
            @PathVariable(RestConstants.INCLUDE_LIST_TYPES) boolean includeListTypes,
            @PathVariable(RestConstants.INCLUDE_INDESIGN) boolean includeIndesign,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getAllClasses", "127.0.0.1", sessionId);
            return mem.getAllClasses(includeListTypes, includeIndesign);
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
     * Gets a class metadata, its attributes and Category.
     * @param className The class name.
     * @param sessionId The session token id.
     * @return A ClassMetadata with the className.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getClass/{className}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ClassMetadata getClass(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getClass", "127.0.0.1", sessionId);
            return mem.getClass(className);
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
     * Gets a class metadata, its attributes and Category.
     * @param classId The class id.
     * @param sessionId The session token id.
     * @return A ClassMetadata with the classId.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getClassWithId/{classId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ClassMetadata getClassWithId(
            @PathVariable(RestConstants.CLASS_ID) long classId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getClassWithId", "127.0.0.1", sessionId);
            return mem.getClass(classId);
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
     * Gets a list of classes, its attributes and Category with a given class name to filter.
     * @param className The class name to filter for.
     * @param page The number of results to skip or the page.
     * @param limit The number of results per page.
     * @param sessionId The session token id.
     * @return A list of Classes that contains the filter in the classMetada name.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getClasses/{className}/{page}/{limit}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadata> getClasses(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.PAGE) int page,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getClasses", "127.0.0.1", sessionId);
            return mem.getClasses(className, page, limit);
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
     * Assess if a given class is subclass of another.
     * @param allegedParent Alleged super class.
     * @param className Class to be evaluated.
     * @param sessionId The session token id.
     * @return True if classToBeEvaluated is subclass of allegedParent. False otherwise. This method also returns true if allegedParent == className.
     */
    @RequestMapping(method = RequestMethod.GET, value = "isSubclassOf/{allegedParent}/{className}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean isSubclassOf(
            @PathVariable(RestConstants.ALLEGED_PARENT) String allegedParent,
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("isSubclassOf", "127.0.0.1", sessionId);
            return mem.isSubclassOf(allegedParent, className);
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
    
    // <editor-fold desc="attributes" defaultstate="collapsed">
    
    /**
     * Adds an attribute to a class.
     * @param className The class the attribute will be added to.
     * @param attributeDefinition An object with the definition of the attribute.
     * @param recursive Defines if the subclasses that has an attribute with name 
     *                  equal to the name of the new attribute can conserve (false) it 
     *                  or must be removed (true), in the case when the attribute must 
     *                  be removed throws an exception.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createAttribute/{className}/{recursive}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void createAttribute(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @RequestBody AttributeMetadata attributeDefinition,
            @PathVariable(RestConstants.RECURSIVE) boolean recursive,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createAttribute", "127.0.0.1", sessionId);
            mem.createAttribute(className, attributeDefinition, recursive);
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
     * Adds an attribute to a class.
     * @param classId The id of the class the attribute will be added to.
     * @param attributeDefinition An object with the definition of the attribute.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createAttributeForClassWithId/{classId}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void createAttributeForClassWithId(
            @PathVariable(RestConstants.CLASS_ID) long classId,
            @RequestBody AttributeMetadata attributeDefinition,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createAttributeForClassWithId", "127.0.0.1", sessionId);
            mem.createAttribute(classId, attributeDefinition);
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
     * Checks if a class has a attribute with a given name.
     * @param className Class name.
     * @param name Attribute name.
     * @param sessionId The session token id.
     * @return True if the given class has the attribute.
     */
    @RequestMapping(method = RequestMethod.GET, value = "hasAttribute/{className}/{name}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean hasAttribute(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("hasAttribute", "127.0.0.1", sessionId);
            return mem.hasAttribute(className, name);
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
     * Gets an attribute belonging to a class.
     * @param className Class name.
     * @param name Attribute name.
     * @param sessionId The session token id.
     * @return AttributeMetata of the requested attribute.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getAttribute/{className}/{name}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public AttributeMetadata getAttribute(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getAttribute", "127.0.0.1", sessionId);
            return mem.getAttribute(className, name);
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
     * Gets an attribute belonging to a class.
     * @param classId Class id.
     * @param id Attribute id.
     * @param sessionId The session token id.
     * @return AttributeMetata of the requested attribute.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getAttributeForClassWithId/{classId}/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public AttributeMetadata getAttributeForClassWithId(
            @PathVariable(RestConstants.CLASS_ID) long classId,
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getAttributeForClassWithId", "127.0.0.1", sessionId);
            return mem.getAttribute(classId, id);
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
     * Retrieves the list of the attributes marked as mandatory.
     * @param className The class name.
     * @param sessionId The session token id.
     * @return A list of AttributeMetadata.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getMandatoryAttributesInClass/{className}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<AttributeMetadata> getMandatoryAttributesInClass(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getMandatoryAttributesInClass", "127.0.0.1", sessionId);
            return mem.getMandatoryAttributesInClass(className);
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
     * Changes an attribute definition belonging to a class metadata use the class name as id.
     * @param className Class the attribute belongs to.
     * @param id Id of the attribute to be updated.
     * @param properties An HashMap<String, Object> with the properties to be updated. The possible key values are: name, color, displayName, description, icon, 
     * smallIcon, countable, abstract, inDesign and custom. See user manual for a more complete explanation on what each one of them are for.
     * @param sessionId The session token id.
     * @return The summary of the changes that were made.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "setAttributeProperties/{className}/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor setAttributeProperties(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.ID) long id,
            @RequestBody HashMap<String, Object> properties,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("setAttributeProperties", "127.0.0.1", sessionId);
            return mem.setAttributeProperties(className, id, properties);
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
     * Changes an attribute definition belonging to a class metadata using the class id as key.
     * @param classId Id of the class the attribute belongs to.
     * @param id Id of the attribute to be updated.
     * @param properties An HashMap<String, Object> with the properties to be updated. The possible key values are: name, color, displayName, description, icon, 
     * smallIcon, countable, abstract, inDesign and custom. See user manual for a more complete explanation on what each one of them are for.
     * @param sessionId The session token id.
     * @return The summary of the changes that were made.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "setAttributePropertiesForClassWithId/{classId}/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor setAttributePropertiesForClassWithId(
            @PathVariable(RestConstants.CLASS_ID) long classId,
            @PathVariable(RestConstants.ID) long id,
            @RequestBody HashMap<String, Object> properties,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("setAttributePropertiesForClassWithId", "127.0.0.1", sessionId);
            return mem.setAttributeProperties(classId, id, properties);
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
     * Deletes an attribute from a class.
     * @param className Class name.
     * @param name Attribute name.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteAttribute/{className}/{name}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteAttribute(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteAttribute", "127.0.0.1", sessionId);
            mem.deleteAttribute(className, name);
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
     * Deletes an attribute from a class.
     * @param classId Class id.
     * @param name Attribute name.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteAttributeForClassWithId/{classId}/{name}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteAttributeForClassWithId(
            @PathVariable(RestConstants.CLASS_ID) long classId,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteAttributeForClassWithId", "127.0.0.1", sessionId);
            mem.deleteAttribute(classId, name);
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
    
    // <editor-fold desc="children" defaultstate="collapsed">
    
    /**
     * Gets all classes whose instances can be contained into the given parent class.This method is recursive, so the result include the possible children in children classes.
     * @param parentClassName The name of the class.
     * @param ignoreAbstract true to ignore abstract classes.
     * @param sessionId The session token id.
     * @return An array with the list of direct possible children classes in the containment hierarchy.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getPossibleChildren/{parentClassName}/{ignoreAbstract}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getPossibleChildren(
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.IGNORE_ABSTRACT) boolean ignoreAbstract,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getPossibleChildren", "127.0.0.1", sessionId);
            return mem.getPossibleChildren(parentClassName, ignoreAbstract);
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
     * Gets all classes whose instances can be contained into the given parent class, but using a CHILD_OF_SPECIAL relationship instead of a CHILD_OF one. 
     * This is mostly used in complex models, such as the physical layer model. This method is recursive, so the result include the possible children in children classes.
     * @param parentClassName The name of the class.
     * @param sessionId The session token id.
     * @return An array with the list of direct possible children classes in the containment hierarchy.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getPossibleSpecialChildren/{parentClassName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getPossibleSpecialChildren(
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getPossibleSpecialChildren", "127.0.0.1", sessionId);
            return mem.getPossibleSpecialChildren(parentClassName);
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
     * Same as getPossibleChildren but this one only gets the direct possible children for the given class, this is, subclasses are not included.
     * @param parentClassName The name of the class.
     * @param sessionId The session token id.
     * @return An array with the list of possible children classes in the containment hierarchy, including the subclasses of the abstract classes.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getPossibleChildrenNoRecursive/{parentClassName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getPossibleChildrenNoRecursive(
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getPossibleChildrenNoRecursive", "127.0.0.1", sessionId);
            return mem.getPossibleChildrenNoRecursive(parentClassName);
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
     * Same as getPossibleSpecialChildren but this one only gets the direct special possible children for the given class, this is, subclasses are not included.
     * @param parentClassName The name of the class.
     * @param sessionId The session token id.
     * @return An array with the list of possible children classes in the containment hierarchy, including the subclasses of the abstract classes.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getPossibleSpecialChildrenNoRecursive/{parentClassName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getPossibleSpecialChildrenNoRecursive(
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getPossibleSpecialChildrenNoRecursive", "127.0.0.1", sessionId);
            return mem.getPossibleSpecialChildrenNoRecursive(parentClassName);
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
     * Finds out if an instance of a given class can be child of an instance of allegedParent.This is a sort of reverse getPossibleChildren.
     * @param allegedParent Possible parent.
     * @param childToBeEvaluated Class to be evaluated.
     * @param sessionId The session token id.
     * @return True an instance of class childToBeEvaluated be a contained into an instance of allegedParent. False otherwise.
     */
    @RequestMapping(method = RequestMethod.GET, value = "canBeChild/{allegedParent}/{childToBeEvaluated}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean canBeChild(
            @PathVariable(RestConstants.ALLEGED_PARENT) String allegedParent,
            @PathVariable(RestConstants.CHILD_TO_BE_EVALUATED) String childToBeEvaluated,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("canBeChild", "127.0.0.1", sessionId);
            return mem.canBeChild(allegedParent, childToBeEvaluated);
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
     * Same as <code>canBeChild</code>, but using the special containment hierarchy.
     * @param allegedParent Possible parent.
     * @param childToBeEvaluated Class to be evaluated.
     * @param sessionId The session token id.
     * @return True an instance of class childToBeEvaluated be a contained into an instance of allegedParent (as in the special containment hierarchy). False otherwise. 
     */
    @RequestMapping(method = RequestMethod.GET, value = "canBeSpecialChild/{allegedParent}/{childToBeEvaluated}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean canBeSpecialChild(
            @PathVariable(RestConstants.ALLEGED_PARENT) String allegedParent,
            @PathVariable(RestConstants.CHILD_TO_BE_EVALUATED) String childToBeEvaluated,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("canBeSpecialChild", "127.0.0.1", sessionId);
            return mem.canBeSpecialChild(allegedParent, childToBeEvaluated);
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
     * Adds to a given class a list of possible children classes whose instances can be contained using the class id to find the parent class.
     * @param parentClassId Id of the class whose instances can contain the instances of the classes in possibleChildren. Use -1 to refer to the DummyRoot.
     * @param possibleChildren Ids of the candidates to be contained.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "addPossibleChildrenForClassWithId/{parentClassId}/{possibleChildren}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addPossibleChildrenForClassWithId(
            @PathVariable(RestConstants.PARENT_CLASS_ID) long parentClassId,
            @PathVariable(RestConstants.POSSIBLE_CHILDREN) long[] possibleChildren,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("addPossibleChildrenForClassWithId", "127.0.0.1", sessionId);
            mem.addPossibleChildren(parentClassId, possibleChildren);
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
     * Adds to a given class a list of possible children classes whose instances can be contained using the class name to find the parent class.
     * @param parentClassName Parent class name. Use DummyRoot for the Navigation Tree root.
     * @param possibleChildren List of possible children.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "addPossibleChildren/{parentClassName}/{possibleChildren}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addPossibleChildren(
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.POSSIBLE_CHILDREN) String[] possibleChildren,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("addPossibleChildren", "127.0.0.1", sessionId);
            mem.addPossibleChildren(parentClassName, possibleChildren);
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
     * Adds to a given class a list of possible special children classes whose instances can be contained using the class id to find the parent class.
     * @param parentClassId Id of the class whose instances can contain the instances of the classes in possibleChildren.
     * @param possibleSpecialChildren Ids of the candidates to be contained.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "addPossibleSpecialChildrenWithId/{parentClassId}/{possibleSpecialChildren}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addPossibleSpecialChildrenWithId(
            @PathVariable(RestConstants.PARENT_CLASS_ID) long parentClassId,
            @PathVariable(RestConstants.POSSIBLE_SPECIAL_CHILDREN) long[] possibleSpecialChildren,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("addPossibleSpecialChildrenWithId", "127.0.0.1", sessionId);
            mem.addPossibleSpecialChildren(parentClassId, possibleSpecialChildren);
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
     * Adds to a given class a list of possible special children classes whose instances can be contained, using the class name to find the parent class.
     * @param parentClassName Parent class name.
     * @param possibleSpecialChildren List of possible children.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.POST, value = "addPossibleSpecialChildren/{parentClassName}/{possibleSpecialChildren}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addPossibleSpecialChildren(
            @PathVariable(RestConstants.PARENT_CLASS_NAME) String parentClassName,
            @PathVariable(RestConstants.POSSIBLE_SPECIAL_CHILDREN) String[] possibleSpecialChildren,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("addPossibleSpecialChildren", "127.0.0.1", sessionId);
            mem.addPossibleSpecialChildren(parentClassName, possibleSpecialChildren);
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
     * The opposite of addPossibleChildren. It removes the given possible children
     * TODO: Make this method safe. This is, check if there's already instances of the given
     * "children to be deleted" with parentClass as their parent.
     * @param parentClassId Id of the class whos instances can contain the instances of the next param.
     * @param childrenToBeRemoved Ids of the candidates to be deleted.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "removePossibleChildren/{parentClassId}/{childrenToBeRemoved}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removePossibleChildren(
            @PathVariable(RestConstants.PARENT_CLASS_ID) long parentClassId,
            @PathVariable(RestConstants.CHILDREN_TO_BE_REMOVED) long[] childrenToBeRemoved,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("removePossibleChildren", "127.0.0.1", sessionId);
            mem.removePossibleChildren(parentClassId, childrenToBeRemoved);
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
     * The opposite of addPossibleSpecialChildren. It removes the given possible special children.
     * @param parentClassId Id of the class whos instances can contain the instances of the next param.
     * @param childrenToBeRemoved Ids of the candidates to be deleted.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "removePossibleSpecialChildren/{parentClassId}/{childrenToBeRemoved}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removePossibleSpecialChildren(
            @PathVariable(RestConstants.PARENT_CLASS_ID) long parentClassId,
            @PathVariable(RestConstants.CHILDREN_TO_BE_REMOVED) long[] childrenToBeRemoved,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("removePossibleSpecialChildren", "127.0.0.1", sessionId);
            mem.removePossibleSpecialChildren(parentClassId, childrenToBeRemoved);
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
    
    // <editor-fold desc="relationship" defaultstate="collapsed">
    
    /**
     * Sets the display name of a special relationship used in a model.
     * @param relationshipName The name of the relationship the display name is going to be set.
     * @param relationshipDisplayName The display name.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "setSpecialRelationshipDisplayName/{relationshipName}/{relationshipDisplayName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void setSpecialRelationshipDisplayName(
            @PathVariable(RestConstants.RELATIONSHIP_NAME) String relationshipName,
            @PathVariable(RestConstants.RELATIONSHIP_DISPLAY_NAME) String relationshipDisplayName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("setSpecialRelationshipDisplayName", "127.0.0.1", sessionId);
            mem.setSpecialRelationshipDisplayName(relationshipName, relationshipDisplayName);
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
     * Returns the display name of a special relationship.The display name is useful to improve the way the relationship is displayed on trees and other modules.
     * @param relationshipName The name of the relationship.
     * @param sessionId The session token id.
     * @return The display name for the relationship name provided. If it can not be found, the relationship name is returned instead.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getSpecialRelationshipDisplayName/{relationshipName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSpecialRelationshipDisplayName(
            @PathVariable(RestConstants.RELATIONSHIP_NAME) String relationshipName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getSpecialRelationshipDisplayName", "127.0.0.1", sessionId);
            return mem.getSpecialRelationshipDisplayName(relationshipName);
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
    
    // <editor-fold desc="hierarchy" defaultstate="collapsed">
    
    /**
     * Get the upstream containment hierarchy for a given class, unlike getPossibleChildren (which will give you the downstream hierarchy).
     * @param className Class name.
     * @param recursive Get only the direct possible parents, or go up into the <strong>containment</strong> hierarchy. Beware: don't mistake the class hierarchy for the containment one.
     * @param sessionId The session token id.
     * @return An sorted list with the upstream containment hierarchy. Repeated elements are omitted.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getUpstreamContainmentHierarchy/{className}/{recursive}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getUpstreamContainmentHierarchy(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.RECURSIVE) boolean recursive,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getUpstreamContainmentHierarchy", "127.0.0.1", sessionId);
            return mem.getUpstreamContainmentHierarchy(className, recursive);
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
     * Gets the upstream special containment hierarchy for a given class, unlike getPossibleChildren (which will give you the downstream hierarchy).
     * @param className Class name.
     * @param recursive Get only the direct possible parents, or go up into the <strong>containment</strong> hierarchy. Beware: don't mistake the class hierarchy for the containment one.
     * @param sessionId The session token id.
     * @return An sorted list with the special upstream containment hierarchy. Repeated elements are omitted.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getUpstreamSpecialContainmentHierarchy/{className}/{recursive}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getUpstreamSpecialContainmentHierarchy(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.RECURSIVE) boolean recursive,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getUpstreamSpecialContainmentHierarchy", "127.0.0.1", sessionId);
            return mem.getUpstreamSpecialContainmentHierarchy(className, recursive);
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
     * Gets the parent classes of a given class up to <code>InventoryObject</code>. Please note that <code>RootObject</code> is being deliberately omitted.
     * @param className The class to get the superclasses from.
     * @param includeSelf If the result should also include the class in className.
     * @param sessionId The session token id.
     * @return The list of super classes until the root of the hierarchy.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getUpstreamClassHierarchy/{className}/{includeSelf}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getUpstreamClassHierarchy(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.INCLUDE_SELF) boolean includeSelf,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getUpstreamClassHierarchy", "127.0.0.1", sessionId);
            return mem.getUpstreamClassHierarchy(className, includeSelf);
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