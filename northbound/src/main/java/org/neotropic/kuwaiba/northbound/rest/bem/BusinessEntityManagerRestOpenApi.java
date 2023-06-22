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
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.AnnotatedBusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for Business Entity Manager.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(BusinessEntityManagerRestOpenApi.PATH)
public interface BusinessEntityManagerRestOpenApi {
    /**
     * Path that includes the Kuwaiba version and core
     */
    public static final String PATH = "/v2.1/core/bem/"; //NOI18N
    
    // <editor-fold desc="objects" defaultstate="collapsed">
    @Operation(summary = "Creates a new inventory object.", description = "The object's id.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createObject/{className}/{parentClassName}/{parentId}/{templateId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Name of the class which this object will be instantiated from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent object class name. If \"null\", the parent will be the DummyRoot node.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent's oid. If \"-1\", the parent will be the DummyRoot node.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Attributes to be set by default in the new object. It's a HashMap<String, String> where the keys are the attribute names and the values,"
                    + " the values for such attributes. Note that binary type attributes can't be set here.", required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template id to be used to create the current object. Template values can be overridden if \"attributeValues\" is not empty."
                    + " Use an empty string or \"null\" to not use a Template.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TEMPLATE_ID, required = true) String templateId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates an object.", description = "The id of the new object.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createObjectWithCriteria/{className}/{parentClassName}/{templateId}/{criteria}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createObjectWithCriteria(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class the object will be instance of.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class of the parent the object will be instance of. Use <b>root</b> for the navigation tree.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dictionary with the names and the values of the attributes to be set.", required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the template to be used to create this object. "
                    + " This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before. "
                    + " Use a \"null\" or empty string to not use a template.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TEMPLATE_ID, required = true) String templateId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Criteria to search for the parent. This is a string with two parts: "
                    + " One is the name of the attribute and the other its value, both separated by a fixed colon <b>:</b>. Example: name:Colombia.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CRITERIA, required = true) String criteria,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates a new inventory object for a domain specific model (where the standard containment rules don't apply).", description = "The id of the new object.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createSpecialObject/{className}/{parentClassName}/{parentId}/{templateId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createSpecialObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Name of the class which this object will be instantiated from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent's oid.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Attributes to be set by default in the new object. It's a HashMap<String, String> where the keys are the attribute names and the values,"
                    + " the values for such attributes. Note that binary type attributes can't be set here.", required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the template to be used to create this object. This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before."
                    + " Use a \"null\" or empty string to not use a template.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TEMPLATE_ID, required = true) String templateId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Equal to {@link BusinessEntityManager#createSpecialObject(java.lang.String, java.lang.String, java.lang.String, java.util.HashMap, java.lang.String)} but the return is a map of ids."
            + "Creates a new inventory object for a domain specific model (where the standard containment rules don't apply).",
            description = "The id of the new object.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HashMap.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createSpecialObjectUsingTemplate/{className}/{parentClassName}/{parentId}/{templateId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public HashMap<String, String> createSpecialObjectUsingTemplate(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Name of the class which this object will be instantiated from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent's oid.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Attributes to be set by default in the new object. It's a HashMap<String, String> where the keys are the attribute names and the values,"
                    + " the values for such attributes. Note that binary type attributes can't be set here.", required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the template to be used to create this object. This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before."
                    + " Use a null or empty string to not use a template.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TEMPLATE_ID, required = true) String templateId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Breaks the special hierarchy to enable special children to have more than one parent.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema())),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "addParentToSpecialObject/{specialObjectClass}/{specialObjectId}/{parentClassName}/{parentId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void addParentToSpecialObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Special object class name", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SPECIAL_OBJECT_CLASS, required = true) String specialObjectClass,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Special Object Id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SPECIAL_OBJECT_ID, required = true) String specialObjectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates an object without parent.This might be particularly useful for complex models. Use it carefully to avoid leaving orphan objects. Always provide custom methods to delete.",
            description = "The id of the newly created object.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createHeadlessObject/{className}/{templateId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createHeadlessObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Name of the class which this object will be instantiated from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The initial set of attributes (as pairs attribute name - value <String, String>) to be set. These values will override those in the template used (if any).",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the template to be used to create the object. Use \"null\" or an empty string to not use any template.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TEMPLATE_ID, required = true) String templateId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates multiple objects using a given name pattern.", description = "An arrays of ids for the new objects.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createBulkObjects/{className}/{parentClassName}/{parentId}/{templateId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] createBulkObjects(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name for the new objects.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parent class name for the new objects.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The object id of the parent.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A pattern to create the names for the new objects.", required = true, content = @Content(schema = @Schema(implementation = String.class)))
            @Valid @RequestBody String namePattern,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "A template id for the objects creation, it could be \"null\" if no template is required.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TEMPLATE_ID, required = true) String templateId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates multiple special objects using a given name pattern.", description = "An array of ids for the new special objects.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createBulkSpecialObjects/{className}/{parentClassName}/{parentId}/{templateId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] createBulkSpecialObjects(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name for the new special objects.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parent class name for the new special objects.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The object id of the parent.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A pattern to create the names for the new special objects.", required = true, content = @Content(schema = @Schema(implementation = String.class)))
            @Valid @RequestBody String namePattern,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the template to be used for the set of objects to be created. Used \"null\" for none.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TEMPLATE_ID, required = true) String templateId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the detailed information about an object.", description = "A detailed representation of the requested object.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BusinessObject.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getObject/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObject getObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's oid.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the simplified information about an object.", description = "A detailed representation of the requested object.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BusinessObject.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getObjectLight/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getObjectLight(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's oid.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the children of a given object.", description = "The list of children.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getObjectChildren/{className}/{objectId}/{maxResults}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectChildren(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's oid.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max number of children to be returned, -1 to return all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.MAX_RESULTS, required = true) int maxResults,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the children of a given object, providing the class and object id.", description = "The list of children.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getObjectChildrenForClassWithId/{classId}/{objectId}/{maxResults}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectChildrenForClassWithId(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the class the object is instance of.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_ID, required = true) long classId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The oid of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The max number of results to be retrieved. Use 0 to retrieve all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.MAX_RESULTS, required = true) int maxResults,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the special children of a given object.", description = "The list of special children.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getObjectSpecialChildren/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectSpecialChildren(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the special children of a given object.", description = "The list of special children.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getObjectSpecialChildrenWithFilters/{className}/{objectId}/{childrenClassNamesToFilter}/{page}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectSpecialChildrenWithFilters(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class names values to filter the return.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CHILDREN_CLASS_NAMES_TO_FILTER, required = true) List<String> childrenClassNamesToFilter,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The number of results to skip or the page.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) int page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The limit of results per page.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the special children count of a given object.", description = "The count of special children.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getObjectSpecialChildrenCount/{className}/{objectId}/{childrenClassNamesToFilter}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getObjectSpecialChildrenCount(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class names values to filter the return.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CHILDREN_CLASS_NAMES_TO_FILTER, required = true) String[] childrenClassNamesToFilter,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves a list of light instances of a given class given a simple filter.This method will search for all objects with a string-based attribute (filterName) whose value matches a value provided (filterValue).",
            description = "The list of instances that matches the filterName/filterValue criteria.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getObjectsWithFilterLight/{className}/{filterName}/{filterValue}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectsWithFilterLight(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the objects to be searched. This method support abstract superclasses as well.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The attribute to be used as filter. This will work <b>only</b> with String-type attributes. Example: serialNumber."
                    + " To list type attributes the filter must be applied to the name. Example: filterName: model, filterValue: XYZ.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_NAME, required = true) String filterName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The value to be use to match the instances. Example \"Serial-12345\".", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_VALUE, required = true) String filterValue,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Same as {@link #getObjectsWithFilterLight(java.lang.String, java.lang.String, java.lang.String) }, but returns the full information about the objects involved.",
            description = "The list of instances that matches the filterName/filterValue criteria.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObject.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getObjectsWithFilter/{className}/{filterName}/{filterValue}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObject> getObjectsWithFilter(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the objects to be searched. This method support abstract superclasses as well.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The attribute to be used as filter. This will work <b>only</b> with String-type attributes. Example: serialNumber."
                    + " To list type attributes the filter must be applied to the name. Example: filterName: model, filterValue: XYZ.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_NAME, required = true) String filterName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The value to be use to match the instances. Example \"Serial-12345\".", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_VALUE, required = true) String filterValue,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Suggests a number of objects based on a search string.The search string is matched against the name of the object, its class name or its class display name.",
            description = "A list of up to #{@code limit} suggested objects matching the criteria, alphabetically sorted.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getSuggestedObjectsWithFilter/{filterValue}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSuggestedObjectsWithFilter(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The string to use as search filter.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_VALUE, required = true) String filterValue,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The limit of results. Use -1 to retrieve all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Same as {@link #getSuggestedObjectsWithFilter(java.lang.String, int) }, but the results will be instances of the super class provided or one of its subclasses."
            + "In reality, this method could do the same as {@link #getSuggestedObjectsWithFilter(java.lang.String, int)} with {@code superClass} set to <code>InventoryObject</code>,"
            + "but the implementation of both methods may differ significantly in terms of performance and complexity.",
            description = "A list of up to #{@code limit} suggested objects matching the criteria, alphabetically sorted.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObject.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getSuggestedObjectsWithFilterAndSuperClass/{filterValue}/{superClassName}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Deprecated
    public List<BusinessObjectLight> getSuggestedObjectsWithFilterAndSuperClass(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The search string.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_VALUE, required = true) String filterValue,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The results will be instances of this class or one of its subclasses.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SUPER_CLASS_NAME, required = true) String superClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The limit of results. Use -1 to retrieve all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a set of suggested objects with filter.", description = "List of suggested objects.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObject.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getSuggestedObjectsWithFilterAndClasses/{filterValue}/{skip}/{limit}/{classNames}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSuggestedObjectsWithFilterAndClasses(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "A possible part of the name of an object(s) or class(es).", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_VALUE, required = true) String filterValue,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The number of leading suggested objects to skip.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SKIP, required = true) int skip,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The number of suggested objects the result should be limited to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @Parameter(in = ParameterIn.PATH, description = "The suggested objects will be instance of this classes or subclasses. Used \"null\" for none.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAMES, required = true) String[] classNames,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a set of objects by its exact names and class names, used to know if an object with the same its already created in the inventory"
            + "e.g.an IP address or a subnet in the ipam module can not be repeated.", description = "A list of objects.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getObjectsByNameAndClassName/{names}/{skip}/{limit}/{classNames}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectsByNameAndClassName(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The exact names of the objects.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAMES, required = true) List<String> names,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The page.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SKIP, required = true) int skip,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The limit per page.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class names of the objects.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAMES, required = true) String[] classNames,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a set of suggested children with filter (no recursive).", description = "List of suggested children (no recursive).", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, 
            value = "getSuggestedChildrenWithFilter/{parentClassName}/{parentId}/{filterValue}/{ignoreSpecialChildren}/{skip}/{limit}/{classNames}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSuggestedChildrenWithFilter(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parent class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parent id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Filter children (no recursive) by name or class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_VALUE, required = true) String filterValue,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "True to ignore special children in the suggested children (no recursive).", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.IGNORE_SPECIAL_CHILDREN, required = true) boolean ignoreSpecialChildren,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The number of leading children to skip.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SKIP, required = true) int skip,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The number of children the result should be limited to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @Parameter(in = ParameterIn.PATH, description = "The suggested children will be instance of this classes or subclasses. If any, \"null\" otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAMES, required = true) String[] classNames,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Recursively gets all the light instances of given class, without filters.", description = "A set of instances of the class.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getObjectsOfClassLight/{className}/{page}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectsOfClassLight(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name. It must be a subclass of InventoryObject.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Page or number of elements to skip.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) long page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max count of child per page.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) long limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Recursively gets all the light instances of given class, without filters.", description = "A set of instances of the class.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value ="getObjectsOfClassLightWithFilter/{className}/{page}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectsOfClassLightWithFilter(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name. It must be a subclass of InventoryObject.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Map of filters key: attribute name, value: attribute value. <String,String>", required = false, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> filters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Page or number of elements to skip.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) long page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max count of child per page.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) long limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Recursively gets all the instances of given class.", description = "A list of instances.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObject.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getObjectsOfClass/{className}/{maxResults}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObject> getObjectsOfClass(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name. It mist be a subclass of InventoryObject.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max number of results. 0 to get all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.MAX_RESULTS, required = true) int maxResults,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Get the child count given the parent class name and id.", description = "The count of child.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value ="getObjectChildrenCount/{parentClassName}/{parentId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getObjectChildrenCount(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Map of filters key: attribute name, value: attribute value. <String,String>",
                    required = false, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> filters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Get a set of children to the given the parent class name and id.", description = "Set of children.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value ="getObjectChildrenWithFilter/{parentClassName}/{parentId}/{skip}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getObjectChildrenWithFilter(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Null for no filter, map of filters key: attribute name, value: attribute value. <String,String>",
                    required = false, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> filters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Skip index.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SKIP, required = true) long skip,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max count of child.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) long limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Suggests a number of objects based on a search string."
            + "This search string will be case-insensitive-matched against the name of the objects and classes in the inventory attributes to filter.",
            description = "Set of children.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET,
            value = "getSuggestedObjectsWithFilterGroupedByClassName/{classNames}/{filterValue}/{classesSkip}/{classesLimit}/{objectSkip}/{objectLimit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HashMap<String, List<BusinessObjectLight>> getSuggestedObjectsWithFilterGroupedByClassName(
            @Parameter(in = ParameterIn.PATH, description = "List<ClassMetadataLight> classesToFilter a list of classes to limit the search. Used \"null\" for none.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAMES, required = true) List<String> classNames,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Value to filter in the attribute name of every business object name or class name o class display name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_VALUE, required = true) String filterValue,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class skip index.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASSES_SKIP, required = true) long classesSkip,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max count of child.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASSES_LIMIT, required = true) long classesLimit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object skip index.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_SKIP, required = true) long objectSkip,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max count of objects.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_LIMIT, required = true) long objectLimit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Copy a set of objects.", description = "A list containing the newly created object ids.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value ="copyObjects/{targetObjectClassName}/{targetObjectId}/{recursive}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] copyObjects(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Target parent's class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TARGET_OBJECT_CLASS_NAME, required = true) String targetObjectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Target parent's oid.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TARGET_OBJECT_ID, required = true) String targetObjectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Hashmap<String, List<String>> with the objects class names as keys and their oids as values.",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, List<String>> objects,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If this operation should also copy the children objects recursively.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RECURSIVE, required = true) boolean recursive,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Copy a set of special objects (this is used to copy objects when they are containment are set in the special containment hierarchy)"
            + " use case: to move physical links into a wire Container.", description = "A list containing the newly created object ids.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value ="copySpecialObjects/{targetObjectClassName}/{targetObjectId}/{recursive}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] copySpecialObjects(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Target parent's class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TARGET_OBJECT_CLASS_NAME, required = true) String targetObjectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Target parent's oid.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TARGET_OBJECT_ID, required = true) String targetObjectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Hashmap<String, List<String>> with the objects class names as keys and their oids as values.",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, List<String>> objects,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If this operation should also copy the children objects recursively.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RECURSIVE, required = true) boolean recursive,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Move a list of objects to a new parent: this methods ignores those who can't be moved and raises"
            + "an OperationNotPermittedException, however, it will move those which can be moved.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value ="moveObjects/{targetObjectClassName}/{targetObjectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void moveObjects(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent's class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TARGET_OBJECT_CLASS_NAME, required = true) String targetObjectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent's oid.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TARGET_OBJECT_ID, required = true) String targetObjectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Hashmap<String, String[]> using the object class name as keys and the respective objects oids as values.",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String,String[]> objects,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Move a list of objects to a new parent(taking into account the special hierarchy containment): this methods ignores those who can't be moved and raises"
            + "an OperationNotPermittedException, however, it will move those which can be moved.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value ="moveSpecialObjects/{targetObjectClassName}/{targetObjectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void moveSpecialObjects(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent's class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TARGET_OBJECT_CLASS_NAME, required = true) String targetObjectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent's oid.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TARGET_OBJECT_ID, required = true) String targetObjectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Hashmap<String, String[]> using the object class name as keys and the respective objects oids as values.",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String,String[]> objects,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Checks recursively if it's safe to delete a single object.", description = "True if the object does not have relationships that keep it from being deleted. False otherwise.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = boolean.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="canDeleteObject/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean canDeleteObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a set of objects.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value ="deleteObjects/{releaseRelationships}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteObjects(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A HashMap where the class name is the key and the value is a list of long containing the ids of the objects to be deleted that are instance of the key class.",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, List<String>> objects,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If all the relationships should be release upon deleting the objects. If false, an OperationNotPermittedException  will be raised if the object has incoming relationships.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RELEASE_RELATIONSHIPS, required = true) boolean releaseRelationships,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a single object.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value ="deleteObject/{className}/{objectId}/{releaseRelationships}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Release relationships automatically. If set to false, it will fail if the object already has incoming relationships.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RELEASE_RELATIONSHIPS, required = true) boolean releaseRelationships,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates an object attributes. Note that you can't set binary attributes through this method. Use setBinaryAttributes instead.",
            description = "The summary of the changes that were made.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeDescriptor.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value ="updateObject/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The attributes to be updated (the key is the attribute name,"
                    + " the value is and array with the value -or values in case of MANY TO MANY list type attributes).",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Move a list of objects to a new parent: this methods ignores those who can't be moved and raises"
            + " an OperationNotPermittedException, however, it will move those which can be moved.", tags = {"objects"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value ="moveObjectsToPool/{targetObjectClassName}/{targetObjectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void moveObjectsToPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent's class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TARGET_OBJECT_CLASS_NAME, required = true) String targetObjectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent's id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TARGET_OBJECT_ID, required = true) String targetObjectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Map using the object class name as keys and the respective objects ids as values.",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String[]> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="pool-item" defaultstate="collapsed">
    
    @Operation(summary = "Creates an object inside a pool.", description = "The id of the newly created object.", tags = {"pool-item"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST,value = "createPoolItem/{poolId}/{className}/{templateId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createPoolItem(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent pool id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class this object is going to be instance of.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The list of attributes to be set initially. The values are serialized objects.",
                    required = true, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the template to be used to create this object. "
                    + "This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before. "
                    + "Use a \"null\" or empty string to not use a template.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TEMPLATE_ID, required = true) String templateId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Copy a pool item from a pool to another pool.", description = "The newly created object id.", tags = {"pool-item"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "copyPoolItem/{poolId}/{poolItemClassName}/{poolItemId}/{recursive}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String copyPoolItem(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the pool node.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name for the pool item.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ITEM_CLASS_NAME, required = true) String poolItemClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id for the pool item.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ITEM_ID, required = true) String poolItemId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "If this operation should also copy the children objects recursively.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RECURSIVE, required = true) boolean recursive,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Move a pool item from a pool to another pool.", tags = {"pool-item"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value ="movePoolItem/{poolId}/{poolItemClassName}/{poolItemId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void movePoolItem(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the pool node.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name for the pool item.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ITEM_CLASS_NAME, required = true) String poolItemClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id for the pool item.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ITEM_ID, required = true) String poolItemId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the list of objects from a pool.", description = "The list of items inside the pool.", tags = {"pool-item"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getPoolItems/{poolId}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getPoolItems(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent pool id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The results limit. Per page 0 to avoid the limit.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the list of objects from a pool.", description = "The list of items inside the pool.", tags = {"pool-item"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getPoolItemsByClassName/{poolId}/{className}/{page}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getPoolItemsByClassName(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent pool id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "A given className to retrieve a set of objects of that className form the pool"
                    + "used when the pool is a Generic class and could have objects of different class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The number of values of the result to skip or the page 0 to avoid.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) int page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The results limit. per page 0 to avoid the limit.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Count the total of objects in a pool.", description = "The count of items inside the pool.", tags = {"pool-item"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getPoolItemsCount/{poolId}/{className}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getPoolItemsCount(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent pool id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "A given className to retrieve a set of objects of that className form the pool"
                    + "used when the pool is a Generic class and could have objects of different class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="attributes" defaultstate="collapsed">
    @Operation(summary = "Utility method that returns the value of an attribute of a given object as a string. In date-type attributes, it will return "
            + "the formatted dated, while in list types, it will return the name of the linked element.", description = "The newly created object id.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getAttributeValueAsString/{objectClassName}/{objectId}/{attributeName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getAttributeValueAsString(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The attribute whose value will be retrieved.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ATTRIBUTE_NAME, required = true) String attributeName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Fetches the attributes of an inventory object (and their values) and returns them as strings. This is useful mainly to display property sheets and reports,"
            + "so it's not necessary to always check if an attribute is a list type and retrieve its string representation.", description = "A dictionary with the name of the attributes and their values represented as strings.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HashMap.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getAttributeValuesAsString/{objectClassName}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public HashMap<String, String> getAttributeValuesAsString(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the value of a special attribute.A special attribute is one belonging to a business domain specific attribute"
            + "(usually a model. Domain specific attribute information is not filed under the standard metadata but a special one. Implementations may vary).",
            description = "A list of objects related to the object through a special relationship. An empty array if the object provided is not related to others using that relationship.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getSpecialAttribute/{objectClassName}/{objectId}/{specialAttributeName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSpecialAttribute(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The object's id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Special attribute name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SPECIAL_ATTRIBUTE_NAME, required = true) String specialAttributeName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Returns the specified special relationships of a given object as a hashmap whose keys are the names of the relationships and the values the list of related objects."
            + "If no filter (attributeNames) is provided, all special attributes (relationships) will be returned.", description = "The hash map with the existing special relationships and the associated objects.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HashMap.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getSpecialAttributes/{objectClassName}/{objectId}/{attributeNames}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public HashMap<String,List<BusinessObjectLight>> getSpecialAttributes(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The list of special attributes (relationships) to be fetched. if none provided, the method will return all of them.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ATTRIBUTE_NAMES, required = true) String[] attributeNames,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "This method will extract the object at the other side of the special relationship and all the properties of the relationship itself.",
            description = "The list of elements related with such relationship plus the properties of theirs relationships.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = AnnotatedBusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getAnnotatedSpecialAttribute/{objectClassName}/{objectId}/{specialAttributeName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<AnnotatedBusinessObjectLight> getAnnotatedSpecialAttribute(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object whose special attribute will be retrieved from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object's id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the special attribute.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SPECIAL_ATTRIBUTE_NAME, required = true) String specialAttributeName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Check if an object has a given special attribute.", description = "True if the object has special attributes.", tags = {"attributes"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = boolean.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="hasSpecialAttribute/{objectClassName}/{objectId}/{attributeName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean hasSpecialAttribute(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Attribute name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ATTRIBUTE_NAME, required = true) String attributeName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="parents" defaultstate="collapsed">
    @Operation(summary = "Finds the common parent between two objects.", description = "The common parent or null if none.", tags = {"parents"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BusinessObjectLight.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getCommonParent/{aObjectClassName}/{aObjectId}/{bObjectClassName}/{bObjectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getCommonParent(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object A class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.A_OBJECT_CLASS_NAME, required = true) String aObjectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object A id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.A_OBJECT_ID, required = true) String aObjectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object B class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.B_OBJECT_CLASS_NAME, required = true) String bObjectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object B id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.B_OBJECT_ID, required = true) String bObjectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the parent of a given object in the containment hierarchy.", description = "The immediate parent. Null if the parent is null. A dummy object with id -1 if the parent is DummyRoot.", tags = {"parents"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BusinessObjectLight.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getParent/{objectClassName}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getParent(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves recursively the list of parents of an object in the containment hierarchy.", description = "The list of parents.", tags = {"parents"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getParents/{objectClassName}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getParents(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the list of parents (according to the special and standard containment hierarchy) until it finds an instance of class"
            + " objectToMatchClassName (for example \"give me the parents of this port until you find the nearest rack\").",
            description = "The list of parents until an instance of objectToMatchClassName is found. If no instance of that class is found, all parents until the Dummy Root will be returned.", tags = {"parents"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getParentsUntilFirstOfClass/{objectClassName}/{objectId}/{objectToMatchClassNames}/{sessionId}", 
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getParentsUntilFirstOfClass(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class of the object to get the parents from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the object to get the parents from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Classes of the objects that will limit the search. It can be a superclass, if you want to match many classes at once.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_TO_MATCH_CLASS_NAMES, required = true) String[] objectToMatchClassNames,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the first occurrence of a parent with a given class (according to the special and standard containment hierarchy) (for example \"give me the parent of this port until you find the nearest rack\"). ",
            description = "The the first occurrence of a parent with a given class. If no instance of that class is found, the child of Dummy Root related in this hierarchy will be returned.", tags = {"parents"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BusinessObject.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getFirstParentOfClass/{objectClassName}/{objectId}/{objectToMatchClassName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObject getFirstParentOfClass(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class of the object to get the parent from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the object to get the parent from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class of the object that will limit the search. It can be a superclass, if you want to match many classes at once.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_TO_MATCH_CLASS_NAME, required = true) String objectToMatchClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the parents of an object that breaks the containment rule of having only one parent."
            + " For example the links and containers in the Outside Plant Module.",
            description = "The set of parents.", tags = {"parents"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getMultipleParents/{objectClassName}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getMultipleParents(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object Class Name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object Id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Checks if a given object is parent to another, according to the standard or special containment hierarchy.",
            description = "True if the given parent has the given child (according to the special and standard containment hierarchy).", tags = {"parents"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = boolean.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="isParent/{parentClassName}/{parentId}/{childClassName}/{childId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public boolean isParent(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Alleged parent Class Name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Alleged parent id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Child Class Name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CHILD_CLASS_NAME, required = true) String childClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Child Id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CHILD_ID, required = true) String childId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="pools" defaultstate="collapsed">
    @Operation(summary = "Retrieves the pools that don't have any parent and are normally intended to be managed by the Pool Manager.", description = "A set of pools.", tags = {"pools"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = InventoryObjectPool.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getRootPools/{className}/{type}/{includeSubclasses}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<InventoryObjectPool> getRootPools(
            @Parameter(in = ParameterIn.PATH, description = "The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use \"null\" if you want to get all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The type of pools that should be retrieved. Root pools can be for general purpose, or as roots in models.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TYPE, required = true) int type,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Use <code>true</code> if you want to get only the pools whose <code>className</code> property matches exactly the one provided, and <code>false</code> if you want to also include the subclasses.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_SUBCLASSES, required = true) boolean includeSubclasses,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the pools associated to a particular object.", description = "A set of pools.", tags = {"pools"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = InventoryObjectPool.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getPoolsInObject/{objectClassName}/{objectId}/{poolClassName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<InventoryObjectPool> getPoolsInObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parent object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parent object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @Parameter(in = ParameterIn.PATH, description = "The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned."
                    + " Use \"null\" if you want to get all", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_CLASS_NAME, required = true) String poolClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the pools associated to a particular pool.", description = "A set of pools.", tags = {"pools"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = InventoryObjectPool.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value ="getPoolsInPool/{parentPoolId}/{poolClassName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<InventoryObjectPool> getPoolsInPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parent pool id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_POOL_ID, required = true) String parentPoolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_CLASS_NAME, required = true) String poolClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Suggests a number of pools based on a search string (the pool name).This search string will be case-insensitive-matched against "
            + " the name of the objects and classes in the inventory attributes to filter.", description = "Set of pools.", tags = {"pools"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = InventoryObjectPool.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getSuggestedPoolsByName/{classNames}/{filterValue}/{poolSkip}/{poolLimit}/{objectSkip}/{objectLimit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public HashMap<String, List<InventoryObjectPool>> getSuggestedPoolsByName(
            @Parameter(in = ParameterIn.PATH, description = "List <ClassMetadataLight> classesToFilter a list of classes to limit the search. Used \"null\" for none.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAMES, required = true) List<String> classNames,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Value to filter in the attribute name of every business object name or class name o class display name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILTER_VALUE, required = true) String filterValue,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Skip index.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_SKIP, required = true) long poolSkip,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max count of child.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_LIMIT, required = true) long poolLimit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object skip index.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_SKIP, required = true) long objectSkip,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max count of objects.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_LIMIT, required = true) long objectLimit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the pools associated to a particular pool.", description = "A set of pools.", tags = {"pools"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getPoolsInPoolCount/{parentPoolId}/{poolClassName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long getPoolsInPoolCount(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parent pool id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_POOL_ID, required = true) String parentPoolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_CLASS_NAME, required = true) String poolClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets a pool by its id.", description = "The pool as a Pool object.", tags = {"pools"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InventoryObjectPool.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getPool/{poolId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public InventoryObjectPool getPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The pool's id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="warehouses" defaultstate="collapsed">
    @Operation(summary = "Gets the warehouses in a object.", description = "Gets the warehouses in a object.", tags = {"warehouses"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getWarehousesInObject/{objectClassName}/{objectId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getWarehousesInObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the physical node of a warehouse item.", description = "Gets the physical node of a warehouse item.", tags = {"warehouses"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BusinessObjectLight.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getPhysicalNodeToObjectInWarehouse/{objectClassName}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getPhysicalNodeToObjectInWarehouse(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets warehouse related to object.", description = "Gets warehouse related to object.", tags = {"warehouses"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BusinessObjectLight.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getWarehouseToObject/{objectClassName}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObjectLight getWarehouseToObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="children" defaultstate="collapsed">
    @Operation(summary = "Gets the direct children of a given object of a given class.",
            description = "A list of children of parentid/parentClass instance that are instances of classNameToFilter.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObject.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getChildrenOfClass/{parentId}/{parentClassName}/{classNameToFilter}/{page}/{maxResults}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObject> getChildrenOfClass(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name to be match against.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME_TO_FILTER, required = true) String classNameToFilter,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The number of page of the number of elements to skip.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) int page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max number of results. 0 to get all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.MAX_RESULTS, required = true) int maxResults,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Returns the special children of a given object as RemoteBusinessObjectLight instances.This method is not recursive.",
            description = "The list of special children of the given object, filtered using classNameToFilter.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getSpecialChildrenOfClassLight/{parentId}/{parentClassName}/{classNameToFilter}/{maxResults}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSpecialChildrenOfClassLight(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the parent object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name of the parent object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The superclass/class to be used to filter the results. You can also use abstract superclasses.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME_TO_FILTER, required = true) String classNameToFilter,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The max number of results to fetch. Use -1 to retrieve all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.MAX_RESULTS, required = true) int maxResults,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets all class and abstract class children of a given class to filter in a hierarchy with root in the given parent.i.e.: all the ports in Router, all the Routers in a City.",
            description = "The list of object instance of the given class to filter.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "getChildrenOfClassLightRecursive/{parentId}/{parentClassName}/{classNameToFilter}/{page}/{limit}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getChildrenOfClassLightRecursive(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id of the root parent of the hierarchy.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name of the root parent of the hierarchy.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name of the expected children.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME_TO_FILTER, required = true) String classNameToFilter,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "If filtering by the attributes of the retrieved objects.",
                    required = false, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> attributes,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The page or the number of elements to skip, no pagination -1.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) int page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Maximum number of results, -1 no limit.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets all class and abstract class special children of a given class to filter in a hierarchy with root in the given parent.Use case: used in some class level and inventory level reports script.",
            description = "The list of object instance of the given class to filter.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getSpecialChildrenOfClassLightRecursive/{parentId}/{parentClassName}/{classNameToFilter}/{maxResults}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSpecialChildrenOfClassLightRecursive(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id of the root parent of the hierarchy.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name of the root parent of the hierarchy.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name of the expected children.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME_TO_FILTER, required = true) String classNameToFilter,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Maximum number of results, -1 no limit.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.MAX_RESULTS, required = true) int maxResults,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Same as getChildrenOfClass, but returns only the light version of the objects.",
            description = "A list of children of parentid/parentClass instance, instances of classToFilter.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getChildrenOfClassLight/{parentId}/{parentClassName}/{classNameToFilter}/{maxResults}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getChildrenOfClassLight(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Parent class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class name to be match against.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME_TO_FILTER, required = true) String classNameToFilter,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max number of results. 0 to get all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.MAX_RESULTS, required = true) int maxResults,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Counts if an object has children.", description = "Number of children.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "countChildren/{objectClassName}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long countChildren(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Counts if an object has special children.", description = "Number of special children.", tags = {"children"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "countSpecialChildren/{objectClassName}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long countSpecialChildren(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="siblings" defaultstate="collapsed">
    @Operation(summary = "Gets the siblings of a given object in the containment hierarchy.", description = "List of siblings.", tags = {"siblings"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getSiblings/{objectClassName}/{objectId}/{maxResults}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getSiblings(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Max number of results to be returned.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.MAX_RESULTS, required = true) int maxResults,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
    
    // <editor-fold desc="attachments" defaultstate="collapsed">
    @Operation(summary = "Relates a file to an inventory object.", description = "The id of the resulting file object.", tags = {"attachments"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "attachFileToObject/{name}/{tags}/{file}/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public long attachFileToObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the file.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The tags that describe the contents of the file.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.TAGS, required = true) String tags,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The file itself as string Base64.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILE, required = true) String file,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object the file will be attached to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object the file will be attached to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Fetches the files associated to an inventory object. Note that this call won't retrieve the actual files, but only references to them.",
            description = "The list of files.", tags = {"attachments"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = FileObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getFilesForObject/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<FileObjectLight> getFilesForObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object whose files will be fetched from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object whose files will be fetched from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves a particular file associated to an inventory object.This call returns the actual file.", description = "The file.", tags = {"attachments"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FileObject.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getFile/{fileObjectId}/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public FileObject getFile(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the file object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILE_OBJECT_ID, required = true) long fileObjectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object the file is associated to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object the file is associated to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Releases (and deletes) a file associated to an inventory object.", tags = {"attachments"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "detachFileFromObject/{fileObjectId}/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void detachFileFromObject(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the file object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILE_OBJECT_ID, required = true) long fileObjectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object the file is associated to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object the file is associated to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates the properties of a file object (name or tags).", tags = {"attachments"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateFileProperties/{fileObjectId}/{className}/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateFileProperties(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the file object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.FILE_OBJECT_ID, required = true) long fileObjectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The set of properties as a dictionary key-value. Valid keys are \"name\" and \"tags\".",
                    required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = StringPair.class))))
            @Valid @RequestBody List<StringPair> properties,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object the file is attached to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object the file is attached to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves a map with the files related to the list type item attributes of the given object.", description = "The map with the files. The key is the list type item and the value a list with the related files.", tags = {"attachments"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getFilesFromRelatedListTypeItems/{objectId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map<BusinessObjectLight, List<FileObjectLight>> getFilesFromRelatedListTypeItems(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The object id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
}