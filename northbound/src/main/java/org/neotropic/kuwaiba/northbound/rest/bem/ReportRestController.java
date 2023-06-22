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

import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
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
 * The definition of the Report Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(ReportRestController.PATH)
public class ReportRestController implements ReportRestOpenApi {
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
    public static Logger LOG = LoggerFactory.getLogger(ReportRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and core
     */
    public static final String PATH = "/v2.1/reports/"; //NOI18N
    
    // <editor-fold desc="reports" defaultstate="collapsed">
    /**
     * Creates a class level report (a report that will be available for all instances of a given class -and its subclasses-).
     * @param className Class this report is going to be related to. It can be ab abstract class and the report will be available for all its subclasses.
     * @param reportName Name of the report.
     * @param reportDescription ReportMetadata description.
     * @param script Script text. If any, "null" otherwise.
     * @param outputType What will be the default output of this report? See RemoteReportLight for possible values.
     * @param enabled If enabled, a report can be executed.
     * @param sessionId The session token id.
     * @return The id of the newly created report.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "createClassLevelReport/{className}/{reportName}/{reportDescription}/{script}/{outputType}/{enabled}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createClassLevelReport(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.REPORT_NAME) String reportName,
            @PathVariable(RestConstants.REPORT_DESCRIPTION) String reportDescription,
            @PathVariable(RestConstants.SCRIPT) String script, 
            @PathVariable(RestConstants.OUTPUT_TYPE) int outputType,
            @PathVariable(RestConstants.ENABLED) boolean enabled,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createClassLevelReport", "127.0.0.1", sessionId);
            return bem.createClassLevelReport(
                    className,
                    reportName,
                    reportDescription,
                    script.equals("null") ? null : script,
                    outputType,
                    enabled
            );
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
     * Creates an inventory level report (a report that is not tied to a particular instance or class.In most cases, they also receive parameters).
     * @param reportName Name of the report.
     * @param reportDescription ReportMetadata description.
     * @param script Script text. If any, "null" otherwise.
     * @param outputType What will be the default output of this report? See InventoryLevelReportDescriptor for possible values.
     * @param enabled If enabled, a report can be executed.
     * @param parameters Optional (it might be either null or an empty list). The list of the parameters that this report will support and optional default values.
     * They will always be captured as strings, so it's up to the author of the report the sanitization and conversion of the inputs.
     * @param sessionId The session token id.
     * @return The id of the newly created report.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "createInventoryLevelReport/{reportName}/{reportDescription}/{script}/{outputType}/{enabled}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createInventoryLevelReport(
            @PathVariable(RestConstants.REPORT_NAME) String reportName,
            @PathVariable(RestConstants.REPORT_DESCRIPTION) String reportDescription,
            @PathVariable(RestConstants.SCRIPT) String script, 
            @PathVariable(RestConstants.OUTPUT_TYPE) int outputType, 
            @PathVariable(RestConstants.ENABLED) boolean enabled,
            @RequestBody(required = false) List<StringPair> parameters,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createInventoryLevelReport", "127.0.0.1", sessionId);
            return bem.createInventoryLevelReport(
                    reportName,
                    reportDescription,
                    script.equals("null") ? null : script,
                    outputType,
                    enabled,
                    parameters == null ? new ArrayList<>() : parameters
            );
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }  catch (NotAuthorizedException ex) {
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
     * Deletes a report.
     * @param reportId The id of the report.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.DELETE,
            value = "deleteReport/{reportId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor deleteReport(
            @PathVariable(RestConstants.REPORT_ID) long reportId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteReport", "127.0.0.1", sessionId);
            return bem.deleteReport(reportId);
        }  catch (NotAuthorizedException ex) {
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
     * Updates the properties of an existing class level report.
     * @param reportId Id of the report.
     * @param reportName The name of the report. Null to leave it unchanged.
     * @param reportDescription The description of the report. Null to leave it unchanged.
     * @param enabled Is the report enabled? . Null to leave it unchanged.
     * @param outputType Type of the output of the report. See LocalReportLight for possible values.
     * @param script Text of the script. If any, "null" otherwise.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateReport/{reportId}/{reportName}/{reportDescription}/{enabled}/{outputType}/{script}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateReport(
            @PathVariable(RestConstants.REPORT_ID) long reportId,
            @PathVariable(RestConstants.REPORT_NAME) String reportName,
            @PathVariable(RestConstants.REPORT_DESCRIPTION) String reportDescription,
            @PathVariable(RestConstants.ENABLED) boolean enabled,
            @PathVariable(RestConstants.OUTPUT_TYPE) int outputType,
            @PathVariable(RestConstants.SCRIPT) String script,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateReport", "127.0.0.1", sessionId);
            return bem.updateReport(
                    reportId,
                    reportName,
                    reportDescription,
                    enabled,
                    outputType,
                    script.equals("null") ? null : script
            );
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }  catch (NotAuthorizedException ex) {
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
     * Updates the parameters of a report.
     * @param reportId The id of the report.
     * @param parameters The list of parameters and optional default values.
     * Those with null values will be deleted and the ones that didn't exist previously will be created.
     * @param sessionId The session token id.
     * @return The summary of the changes.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateReportParameters/{reportId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChangeDescriptor updateReportParameters(
            @PathVariable(RestConstants.REPORT_ID) long reportId, 
            @RequestBody List<StringPair> parameters,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateReportParameters", "127.0.0.1", sessionId);
            return bem.updateReportParameters(reportId, parameters);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }  catch (NotAuthorizedException ex) {
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
     * Gets the class level reports associated to the given class (or its superclasses).
     * @param className The class to extract the reports from.
     * @param recursive False to get only the directly associated reports. True top get also the reports associate top its superclasses.
     * @param includeDisabled True to also include the reports marked as disabled. False to return only the enabled ones.
     * @param sessionId The session token id.
     * @return The list of reports.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getClassLevelReports/{className}/{recursive}/{includeDisabled}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ReportMetadataLight> getClassLevelReports(
            @PathVariable(RestConstants.CLASS_NAME) String className,
            @PathVariable(RestConstants.RECURSIVE) boolean recursive,
            @PathVariable(RestConstants.INCLUDE_DISABLED) boolean includeDisabled,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getClassLevelReports", "127.0.0.1", sessionId);
            return bem.getClassLevelReports(className, recursive, includeDisabled);
        }  catch (NotAuthorizedException ex) {
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
     * Gets the inventory class reports.
     * @param includeDisabled True to also include the reports marked as disabled. False to return only the enabled ones.
     * @param sessionId The session token id.
     * @return The list of reports.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getInventoryLevelReports/{includeDisabled}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ReportMetadataLight> getInventoryLevelReports(
            @PathVariable(RestConstants.INCLUDE_DISABLED) boolean includeDisabled,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getInventoryLevelReports", "127.0.0.1", sessionId);
            return bem.getInventoryLevelReports(includeDisabled);
        }  catch (NotAuthorizedException ex) {
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
     * Gets the information related to a class level report.
     * @param reportId The id of the report.
     * @param sessionId The session token id.
     * @return The report.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getReport/{reportId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ReportMetadata getReport(
            @PathVariable(RestConstants.REPORT_ID) long reportId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("reportId", "127.0.0.1", sessionId);
            return bem.getReport(reportId);
        }  catch (NotAuthorizedException ex) {
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
     * Executes a class level report and returns the result.
     * @param objectClassName The class of the instance that will be used as input for the report.
     * @param objectId The id of the instance that will be used as input for the report.
     * @param reportId The id of the report.
     * @param sessionId The session token id.
     * @return The result of the report execution.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "executeClassLevelReport/{objectClassName}/{objectId}/{reportId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public byte[] executeClassLevelReport(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.REPORT_ID) long reportId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("executeClassLevelReport", "127.0.0.1", sessionId);
            return bem.executeClassLevelReport(objectClassName, objectId, reportId);
        }  catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }  catch (NotAuthorizedException ex) {
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
     * Executes an inventory level report and returns the result.
     * @param reportId The id of the report.
     * @param parameters List of pairs param name - param value.
     * @param sessionId The session token id.
     * @return The result of the report execution.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "executeInventoryLevelReport/{reportId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public byte[] executeInventoryLevelReport(
            @PathVariable(RestConstants.REPORT_ID) long reportId,
            @RequestBody(required = false) List<StringPair> parameters,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("executeInventoryLevelReport", "127.0.0.1", sessionId);
            return bem.executeInventoryLevelReport(
                    reportId,
                    parameters == null ? new ArrayList<>() : parameters
            );
        }  catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }  catch (NotAuthorizedException ex) {
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