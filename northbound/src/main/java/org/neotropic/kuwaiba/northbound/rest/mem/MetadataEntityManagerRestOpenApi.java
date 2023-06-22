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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.HashMap;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for Metadata Entity Manager.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(MetadataEntityManagerRestOpenApi.PATH)
public interface MetadataEntityManagerRestOpenApi {
    /**
     * Path that includes the Kuwaiba version and core
     */
    public static final String PATH = "/v2.1/core/mem/"; //NOI18N
    
    // <editor-fold desc="classes" defaultstate="collapsed">
    
    @Operation(summary = "Creates a class metadata with its attributes (some new and others inherited from the parent class).", description = "The Id of the newClassMetadata.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createClass/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createClass(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The class definition, name, display name, etc.", required = true,
                    content = @Content(schema = @Schema(implementation = ClassMetadata.class)))
            @Valid @RequestBody ClassMetadata classDefinition,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Changes a class metadata definition.", description = "The summary of the changes that were made.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeDescriptor.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "setClassProperties/{classId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor setClassProperties(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the class. A class metadata definition can not be updated using the name as the key, because the name itself could change.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_ID, required = true) long classId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "An HashMap<String, Object> with the properties to be updated. The possible key values are: name, color, displayName, description, icon<String>,"
                    + "smallIcon<String>, countable, abstract, inDesign and custom. See user manual for a more complete explanation on what each one of them are for.", required = true,
                    content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, Object> properties,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a class metadata, its attributes and category relationships.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteClass/{className}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteClass(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a class metadata, its attributes and category relationships.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteClassWithId/{classId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteClassWithId(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_ID, required = true) long classId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the simplified list of classes, This list won't include either those classes marked as dummy.", description = "The list of type classes.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getAllClassesLight/{includeListTypes}/{includeIndesign}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getAllClassesLight(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "To indicate if the list should include the subclasses of GenericObjectList.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_LIST_TYPES, required = true) boolean includeListTypes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Include all the data model classes or only the classes in production.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_INDESIGN, required = true) boolean includeIndesign,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the subclasses of a given class recursively.", description = "The list of subclasses.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getSubClassesLight/{className}/{includeAbstractClasses}/{includeSelf}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getSubClassesLight(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Should the list include the abstract subclasses.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_ABSTRACT_CLASSES, required = true) boolean includeAbstractClasses,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Should the list include the subclasses and the parent class?", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_SELF, required = true) boolean includeSelf,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the direct subclasses of a given class.", description = "The list of subclasses.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getSubClassesLightNoRecursive/{className}/{includeAbstractClasses}/{includeSelf}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getSubClassesLightNoRecursive(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If abstract classes should be included.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_ABSTRACT_CLASSES, required = true) boolean includeAbstractClasses,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Also return the metadata of class <code>className</code>", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_SELF, required = true) boolean includeSelf,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the superclasses of a given class up to InventoryObject.", description = "The list of super classes.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getSuperClassesLight/{className}/{includeSelf}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getSuperClassesLight(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class to be evaluated.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If the result should include the the very class that was provided in <code>className</code> parameter.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_SELF, required = true) boolean includeSelf,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Get the subclasses count given a parent class name.", description = "Number of direct subclasses.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getSubClassesCount/{className}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getSubClassesCount(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent class Name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves all the class metadata except for classes marked as dummy.", description = "An array with the metadata of the classes.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadata.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getAllClasses/{includeListTypes}/{includeIndesign}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadata> getAllClasses(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Boolean to indicate if the list should include the subclasses of GenericObjectList.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_LIST_TYPES, required = true) boolean includeListTypes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Include classes marked as in design.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_INDESIGN, required = true) boolean includeIndesign,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a class metadata, its attributes and Category.", description = "A ClassMetadata with the className.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClassMetadata.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getClass/{className}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ClassMetadata getClass(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a class metadata, its attributes and Category.", description = "A ClassMetadata with the classId.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClassMetadata.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getClassWithId/{classId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ClassMetadata getClassWithId(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_ID, required = true) long classId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a list of classes, its attributes and Category with a given class name to filter.", description = "A list of Classes that contains the filter in the classMetada name.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadata.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getClasses/{className}/{page}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadata> getClasses(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Gets a list of classes, its attributes and Category with a given class name to filter.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The number of results to skip or the page.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) int page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The number of results per page.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Assess if a given class is subclass of another.", description = "True if classToBeEvaluated is subclass of allegedParent. False otherwise. This method also returns true if allegedParent == className.", tags = {"classes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = boolean.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "isSubclassOf/{allegedParent}/{className}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean isSubclassOf(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Alleged super class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ALLEGED_PARENT, required = true) String allegedParent,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class to be evaluated.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="attributes" defaultstate="collapsed">
    
    @Operation(summary = "Adds an attribute to a class.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createAttribute/{className}/{recursive}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void createAttribute(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class the attribute will be added to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "An object with the definition of the attribute.", required = true,
                    content = @Content(schema = @Schema(implementation = AttributeMetadata.class)))
            @Valid @RequestBody AttributeMetadata attributeDefinition,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Defines if the subclasses that has an attribute with name "
                    + "equal to the name of the new attribute can conserve (false) it "
                    + "or must be removed (true), in the case when the attribute must "
                    + "be removed throws an exception.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RECURSIVE, required = true) boolean recursive,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Adds an attribute to a class.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createAttributeForClassWithId/{classId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void createAttributeForClassWithId(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the class the attribute will be added to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_ID, required = true) long classId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "An object with the definition of the attribute.", required = true,
                    content = @Content(schema = @Schema(implementation = AttributeMetadata.class)))
            @Valid @RequestBody AttributeMetadata attributeDefinition,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Checks if a class has a attribute with a given name.", description = "True if the given class has the attribute.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = boolean.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "hasAttribute/{className}/{name}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean hasAttribute(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Attribute name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets an attribute belonging to a class.", description = "AttributeMetata of the requested attribute.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AttributeMetadata.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getAttribute/{className}/{name}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public AttributeMetadata getAttribute(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Attribute name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets an attribute belonging to a class.", description = "AttributeMetata of the requested attribute.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AttributeMetadata.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getAttributeForClassWithId/{classId}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public AttributeMetadata getAttributeForClassWithId(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_ID, required = true) long classId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Attribute id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the list of the attributes marked as mandatory.", description = "A list of AttributeMetadata.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = AttributeMetadata.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getMandatoryAttributesInClass/{className}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<AttributeMetadata> getMandatoryAttributesInClass(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Changes an attribute definition belonging to a class metadata use the class name as id.", description = "The summary of the changes that were made.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeDescriptor.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "setAttributeProperties/{className}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor setAttributeProperties(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class the attribute belongs to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the attribute to be updated.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = ""
                    + " An HashMap<String, Object> with the properties to be updated."
                    + " The possible key values are: name, color, displayName, description, icon, "
                    + " smallIcon, countable, abstract, inDesign and custom. "
                    + " See user manual for a more complete explanation on what each one of them are for.",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, Object> properties,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Changes an attribute definition belonging to a class metadata using the class id as key.", description = "The summary of the changes that were made.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeDescriptor.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "setAttributePropertiesForClassWithId/{classId}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor setAttributePropertiesForClassWithId(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the class the attribute belongs to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_ID, required = true) long classId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the attribute to be updated.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = ""
                    + " An HashMap<String, Object> with the properties to be updated."
                    + " The possible key values are: name, color, displayName, description, icon, "
                    + " smallIcon, countable, abstract, inDesign and custom. "
                    + " See user manual for a more complete explanation on what each one of them are for.",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, Object> properties,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes an attribute from a class.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteAttribute/{className}/{name}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteAttribute(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Attribute name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes an attribute from a class.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteAttributeForClassWithId/{classId}/{name}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteAttributeForClassWithId(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_ID, required = true) long classId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Attribute name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="children" defaultstate="collapsed">
    
    @Operation(summary = "Gets all classes whose instances can be contained into the given parent class.This method is recursive, so the result include the possible children in children classes.",
            description = "An array with the list of direct possible children classes in the containment hierarchy.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getPossibleChildren/{parentClassName}/{ignoreAbstract}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getPossibleChildren(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Attribute name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IGNORE_ABSTRACT, required = true) boolean ignoreAbstract,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets all classes whose instances can be contained into the given parent class, but using a CHILD_OF_SPECIAL relationship instead of a CHILD_OF one."
            + "This is mostly used in complex models, such as the physical layer model. This method is recursive, so the result include the possible children in children classes.",
            description = "An array with the list of direct possible children classes in the containment hierarchy.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getPossibleSpecialChildren/{parentClassName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getPossibleSpecialChildren(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Same as getPossibleChildren but this one only gets the direct possible children for the given class, this is, subclasses are not included.",
            description = "An array with the list of possible children classes in the containment hierarchy, including the subclasses of the abstract classes.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getPossibleChildrenNoRecursive/{parentClassName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getPossibleChildrenNoRecursive(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Same as getPossibleSpecialChildren but this one only gets the direct special possible children for the given class, this is, subclasses are not included.",
            description = "An array with the list of possible children classes in the containment hierarchy, including the subclasses of the abstract classes.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getPossibleSpecialChildrenNoRecursive/{parentClassName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getPossibleSpecialChildrenNoRecursive(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Finds out if an instance of a given class can be child of an instance of allegedParent.This is a sort of reverse getPossibleChildren.",
            description = "True an instance of class childToBeEvaluated be a contained into an instance of allegedParent. False otherwise.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = boolean.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "canBeChild/{allegedParent}/{childToBeEvaluated}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean canBeChild(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Possible parent.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ALLEGED_PARENT, required = true) String allegedParent,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class to be evaluated.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CHILD_TO_BE_EVALUATED, required = true) String childToBeEvaluated,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Same as <code>canBeChild</code>, but using the special containment hierarchy.",
            description = "True an instance of class childToBeEvaluated be a contained into an instance of allegedParent (as in the special containment hierarchy). False otherwise.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = boolean.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "canBeSpecialChild/{allegedParent}/{childToBeEvaluated}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean canBeSpecialChild(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Possible parent.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ALLEGED_PARENT, required = true) String allegedParent,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class to be evaluated.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CHILD_TO_BE_EVALUATED, required = true) String childToBeEvaluated,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Adds to a given class a list of possible children classes whose instances can be contained using the class id to find the parent class.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "addPossibleChildrenForClassWithId/{parentClassId}/{possibleChildren}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addPossibleChildrenForClassWithId(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the class whose instances can contain the instances of the classes in possibleChildren. Use -1 to refer to the DummyRoot.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_ID, required = true) long parentClassId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Ids of the candidates to be contained.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POSSIBLE_CHILDREN, required = true) long[] possibleChildren,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Adds to a given class a list of possible children classes whose instances can be contained using the class name to find the parent class.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "addPossibleChildren/{parentClassName}/{possibleChildren}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addPossibleChildren(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent class name. Use DummyRoot for the Navigation Tree root.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List of possible children.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POSSIBLE_CHILDREN, required = true) String[] possibleChildren,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Adds to a given class a list of possible special children classes whose instances can be contained using the class id to find the parent class.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "addPossibleSpecialChildrenWithId/{parentClassId}/{possibleSpecialChildren}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addPossibleSpecialChildrenWithId(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the class whose instances can contain the instances of the classes in possibleChildren.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_ID, required = true) long parentClassId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Ids of the candidates to be contained.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POSSIBLE_SPECIAL_CHILDREN, required = true) long[] possibleSpecialChildren,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Adds to a given class a list of possible special children classes whose instances can be contained, using the class name to find the parent class.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "addPossibleSpecialChildren/{parentClassName}/{possibleSpecialChildren}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addPossibleSpecialChildren(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "List of possible children.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POSSIBLE_SPECIAL_CHILDREN, required = true) String[] possibleSpecialChildren,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "The opposite of addPossibleChildren. It removes the given possible children"
            + "TODO: Make this method safe. This is, check if there's already instances of the given"
            + "children to be deleted with parentClass as their parent.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "removePossibleChildren/{parentClassId}/{childrenToBeRemoved}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removePossibleChildren(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the class whos instances can contain the instances of the next param.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_ID, required = true) long parentClassId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Ids of the candidates to be deleted.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CHILDREN_TO_BE_REMOVED, required = true) long[] childrenToBeRemoved,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "The opposite of addPossibleSpecialChildren. It removes the given possible special children.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "removePossibleSpecialChildren/{parentClassId}/{childrenToBeRemoved}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removePossibleSpecialChildren(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the class whos instances can contain the instances of the next param.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_ID, required = true) long parentClassId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Ids of the candidates to be deleted.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CHILDREN_TO_BE_REMOVED, required = true) long[] childrenToBeRemoved,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="relationship" defaultstate="collapsed">
    @Operation(summary = "Sets the display name of a special relationship used in a model.", tags = {"relationship"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "setSpecialRelationshipDisplayName/{relationshipName}/{relationshipDisplayName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void setSpecialRelationshipDisplayName(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the relationship the display name is going to be set.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RELATIONSHIP_NAME, required = true) String relationshipName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The display name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RELATIONSHIP_DISPLAY_NAME, required = true) String relationshipDisplayName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Returns the display name of a special relationship.The display name is useful to improve the way the relationship is displayed on trees and other modules.",
            description = "The display name for the relationship name provided. If it can not be found, the relationship name is returned instead.", tags = {"relationship"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getSpecialRelationshipDisplayName/{relationshipName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSpecialRelationshipDisplayName(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the relationship.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RELATIONSHIP_NAME, required = true) String relationshipName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="hierarchy" defaultstate="collapsed">
    
    @Operation(summary = "Get the upstream containment hierarchy for a given class, unlike getPossibleChildren (which will give you the downstream hierarchy).",
            description = "An sorted list with the upstream containment hierarchy. Repeated elements are omitted.", tags = {"hierarchy"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getUpstreamContainmentHierarchy/{className}/{recursive}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getUpstreamContainmentHierarchy(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Get only the direct possible parents, or go up into the <strong>containment</strong> hierarchy. Beware: don't mistake the class hierarchy for the containment one.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RECURSIVE, required = true) boolean recursive,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the upstream special containment hierarchy for a given class, unlike getPossibleChildren (which will give you the downstream hierarchy).",
            description = "An sorted list with the special upstream containment hierarchy. Repeated elements are omitted.", tags = {"hierarchy"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getUpstreamSpecialContainmentHierarchy/{className}/{recursive}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getUpstreamSpecialContainmentHierarchy(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Get only the direct possible parents, or go up into the <strong>containment</strong> hierarchy. Beware: don't mistake the class hierarchy for the containment one.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RECURSIVE, required = true) boolean recursive,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the parent classes of a given class up to <code>InventoryObject</code>. Please note that <code>RootObject</code> is being deliberately omitted.",
            description = "The list of super classes until the root of the hierarchy.", tags = {"hierarchy"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ClassMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getUpstreamClassHierarchy/{className}/{includeSelf}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ClassMetadataLight> getUpstreamClassHierarchy(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class to get the superclasses from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If the result should also include the class in className.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_SELF, required = true) boolean includeSelf,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
}