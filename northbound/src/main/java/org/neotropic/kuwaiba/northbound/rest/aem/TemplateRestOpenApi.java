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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for template manager.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public interface TemplateRestOpenApi {
    // <editor-fold desc="template-manager" defaultstate="collapsed">
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/template-manager/"; //NOI18N
    
    @Operation(summary = "Creates a template.", description = "The id of the newly created template.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createTemplate/{className}/{name}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createTemplate(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class you want to create a template for.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the template. It can not be null.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Create an special object inside an template.", description = "The id of the new object.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createTemplateSpecialElement/{className}/{parentClassName}/{parentId}/{name}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createTemplateElement(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template special element class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template special element parent class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template special element parent Id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template special element name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Create an special object inside an template.", description = "The id of the new object.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createTemplateSpecialElement/{className}/{parentClassName}/{parentId}/{name}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createTemplateSpecialElement(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template special element class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template special element parent class name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template special element parent Id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template special element name.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NAME, required = true) String name,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates multiple template elements using a given name pattern.", description = "An array of ids for the new template elements.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String[].class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createBulkTemplateElement/{className}/{parentClassName}/{parentId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] createBulkTemplateElement(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name of the new set of template elements.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parent class name of the new set of template elements.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parent id of the new set of template elements.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Name pattern of the new set of template elements.", required = true, content = @Content(schema = @Schema(implementation = String.class)))
            @Valid @RequestBody String namePattern,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates multiple special template elements using a given name pattern.", description = "An array if ids for the new special template elements.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String[].class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST, value = "createBulkSpecialTemplateElement/{className}/{parentClassName}/{parentId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] createBulkSpecialTemplateElement(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class name of the new set of special template elements.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parent class name of the new set of special template elements.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_CLASS_NAME, required = true) String parentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The parent id of the new set of special template elements.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PARENT_ID, required = true) String parentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Name pattern of the new set of special template elements.", required = true, content = @Content(schema = @Schema(implementation = String.class)))
            @Valid @RequestBody String namePattern,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates the value of an attribute of a template element.", description = "The summary of the changes.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ChangeDescriptor.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateTemplateElement/{className}/{id}/{attributeNames}/{attributeValues}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateTemplateElement(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class of the element you want to update.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the element you want to update.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Names of the attributes that you want to be updated as an array of strings.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ATTRIBUTE_NAMES, required = true) String[] attributeNames,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The values of the attributes you want to update. For list types, it's the id of the related type.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ATTRIBUTE_VALUES, required = true) String[] attributeValues,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes an element within a template or a template itself.", description = "The summary of the changes.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ChangeDescriptor.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteTemplateElement/{className}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor deleteTemplateElement(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The template element class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The template element id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the templates available for a given class.", description = "A list of templates (actually, the top element) as a list of RemoteObjects.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = TemplateObjectLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getTemplatesForClass/{className}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<TemplateObjectLight> getTemplatesForClass(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class whose templates we need.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the children of a given template element.", description = "The template element's children as a list of RemoteBusinessObjectLight instances.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = TemplateObjectLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getTemplateElementChildren/{className}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<TemplateObjectLight> getTemplateElementChildren(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template element class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template element id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the children of a given template special element.", description = "The template element's children as a list of RemoteBusinessObjectLight instances.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = TemplateObjectLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getTemplateSpecialElementChildren/{className}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<TemplateObjectLight> getTemplateSpecialElementChildren(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template special element class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template special element id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves all the information of a given template element.", description = "The template element information.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = TemplateObject.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getTemplateElement/{className}/{id}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public TemplateObject getTemplateElement(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template element class.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Template element id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ID, required = true) String id,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Copy template elements within templates. Should not be used to copy entire templates.", description = "An array with the ids of the newly created elements in the same order they were provided.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String[].class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "copyTemplateElements/{sourceObjectsClassNames}/{sourceObjectsIds}/{newParentClassName}/{newParentId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] copyTemplateElements(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Array with the class names of the elements to be copied.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SOURCE_OBJECTS_NAMES, required = true) String[] sourceObjectsClassNames,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Array with the ids of the elements to be copied.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SOURCE_OBJECTS_IDS, required = true) String[] sourceObjectsIds,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class of the parent of the copied objects.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NEW_PARENT_CLASS_NAME, required = true) String newParentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the parent of the copied objects.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NEW_PARENT_ID, required = true) String newParentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Copy template special elements within templates.", description = "An array with the ids of the newly created special elements in the same order they were provided.", tags = {"template-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String[].class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "copyTemplateSpecialElement/{sourceObjectsClassNames}/{sourceObjectsIds}/{newParentClassName}/{newParentId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String[] copyTemplateSpecialElement(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Array with the class names of the special elements to be copied.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SOURCE_OBJECTS_NAMES, required = true) String[] sourceObjectsClassNames,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Array with the ids of the special elements to be copied.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SOURCE_OBJECTS_IDS, required = true) String[] sourceObjectsIds,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class of the parent of the copied objects.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NEW_PARENT_CLASS_NAME, required = true) String newParentClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the parent of the copied objects.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.NEW_PARENT_ID, required = true) String newParentId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
}