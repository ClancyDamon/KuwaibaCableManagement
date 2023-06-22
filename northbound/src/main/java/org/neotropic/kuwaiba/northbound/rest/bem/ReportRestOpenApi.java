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
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for reports.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(ReportRestOpenApi.PATH)
public interface ReportRestOpenApi {
    // <editor-fold desc="reports" defaultstate="collapsed">
    /**
     * Path that includes the Kuwaiba version and core
     */
    public static final String PATH = "/v2.1/reports/"; //NOI18N
    
    @Operation(summary = "Creates a class level report (a report that will be available for all instances of a given class -and its subclasses-).",
            description = "The id of the newly created report.", tags = {"reports"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST,
            value = "createClassLevelReport/{className}/{reportName}/{reportDescription}/{script}/{outputType}/{enabled}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createClassLevelReport(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class this report is going to be related to. It can be ab abstract class and the report will be available for all its subclasses.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Name of the report.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.REPORT_NAME, required = true) String reportName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "ReportMetadata description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.REPORT_DESCRIPTION, required = true) String reportDescription,
            @Parameter(in = ParameterIn.PATH, description = "Script text. If any, \"null\" otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SCRIPT, required = true) String script,
            @Parameter(in = ParameterIn.PATH, description = "What will be the default output of this report? See RemoteReportLight for possible values.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OUTPUT_TYPE, required = true) int outputType,
            @Parameter(in = ParameterIn.PATH, description = "If enabled, a report can be executed.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ENABLED, required = true) boolean enabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates an inventory level report (a report that is not tied to a particular instance or class.In most cases, they also receive parameters).",
            description = "The id of the newly created report.", tags = {"reports"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = long.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST,
            value = "createInventoryLevelReport/{reportName}/{reportDescription}/{script}/{outputType}/{enabled}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createInventoryLevelReport(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Name of the report.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.REPORT_NAME, required = true) String reportName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "ReportMetadata description.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.REPORT_DESCRIPTION, required = true) String reportDescription,
            @Parameter(in = ParameterIn.PATH, description = "Script text. If any, \"null\" otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SCRIPT, required = true) String script,
            @Parameter(in = ParameterIn.PATH, description = "What will be the default output of this report? See InventoryLevelReportDescriptor for possible values.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OUTPUT_TYPE, required = true) int outputType,
            @Parameter(in = ParameterIn.PATH, description = "If enabled, a report can be executed.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ENABLED, required = true) boolean enabled,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Optional (it might be either null or an empty list). The list of the parameters that this report will support and optional default values."
                            + " They will always be captured as strings, so it's up to the author of the report the sanitization and conversion of the inputs.",
                    required = false, content = @Content(array = @ArraySchema(schema = @Schema(implementation = StringPair.class)))) 
            @Valid @RequestBody List<StringPair> parameters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a report.", description = "The summary of the changes.", tags = {"reports"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeDescriptor.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteReport/{reportId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor deleteReport(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the report.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.REPORT_ID, required = true) long reportId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates the properties of an existing class level report.", description = "The summary of the changes.", tags = {"reports"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeDescriptor.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateReport/{reportId}/{reportName}/{reportDescription}/{enabled}/{outputType}/{script}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateReport(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Id of the report.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.REPORT_ID, required = true) long reportId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The name of the report. Null to leave it unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.REPORT_NAME, required = true) String reportName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The description of the report. Null to leave it unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.REPORT_DESCRIPTION, required = true) String reportDescription,
            @Parameter(in = ParameterIn.PATH, description = "Is the report enabled? . Null to leave it unchanged.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.ENABLED, required = true) boolean enabled,
            @Parameter(in = ParameterIn.PATH, description = "Type of the output of the report. See LocalReportLight for possible values.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OUTPUT_TYPE, required = true) int outputType,
            @Parameter(in = ParameterIn.PATH, description = "Text of the script. If any, \"null\" otherwise.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SCRIPT, required = true) String script,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates the parameters of a report.", description = "The summary of the changes.", tags = {"reports"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeDescriptor.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateReportParameters/{reportId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateReportParameters(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the report.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.REPORT_ID, required = true) long reportId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The list of parameters and optional default values. "
                            + " Those with null values will be deleted and the ones that didn't exist previously will be created.",
                    required = false, content = @Content(array = @ArraySchema(schema = @Schema(implementation = StringPair.class)))) 
            @Valid @RequestBody List<StringPair> parameters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the class level reports associated to the given class (or its superclasses).", description = "The list of reports.", tags = {"reports"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ReportMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getClassLevelReports/{className}/{recursive}/{includeDisabled}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ReportMetadataLight> getClassLevelReports(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class to extract the reports from.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CLASS_NAME, required = true) String className,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "False to get only the directly associated reports. True top get also the reports associate top its superclasses.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.RECURSIVE, required = true) boolean recursive,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "True to also include the reports marked as disabled. False to return only the enabled ones.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_DISABLED, required = true) boolean includeDisabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the inventory class reports.", description = "The list of reports.", tags = {"reports"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ReportMetadataLight.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getInventoryLevelReports/{includeDisabled}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ReportMetadataLight> getInventoryLevelReports(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "True to also include the reports marked as disabled. False to return only the enabled ones.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.INCLUDE_DISABLED, required = true) boolean includeDisabled,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the information related to a class level report.", description = "The report.", tags = {"reports"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReportMetadata.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getReport/{reportId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ReportMetadata getReport(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the report.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.REPORT_ID, required = true) long reportId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Executes a class level report and returns the result.", description = "The result of the report execution.", tags = {"reports"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = byte.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "executeClassLevelReport/{objectClassName}/{objectId}/{reportId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public byte[] executeClassLevelReport(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the instance that will be used as input for the report.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the instance that will be used as input for the report.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the report.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.REPORT_ID, required = true) long reportId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Executes an inventory level report and returns the result.", description = "The result of the report execution.", tags = {"reports"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = byte.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "executeInventoryLevelReport/{reportId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public byte[] executeInventoryLevelReport(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the report.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.REPORT_ID, required = true) long reportId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of pairs param name - param value.", required = false,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StringPair.class)))) 
            @Valid @RequestBody List<StringPair> parameters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
}