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
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
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
 * Outside Plant Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(OutsidePlantRestController.PATH)
public class OutsidePlantRestController implements OutsidePlantRestOpenApi {
    
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
    public static Logger LOG = LoggerFactory.getLogger(OutsidePlantRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/osp-manager/"; //NOI18N
    
    // <editor-fold desc="osp-manager" defaultstate="collapsed">
    
    /**
     * Creates an Outside Plant View.
     * @param name The name of the new view.
     * @param description The description of the new view.
     * @param structure The structure of the view as string Base64, 
     * from the XML document with the contents of the view. The format of the XML document is consistent with the other views:
     * <pre>
     * {@code
     *  <view version="">
     *      <class>OSPView</class>
     *      <center lon="" lat=""></center>
     *      <zoom>0</zoom>
     *      <nodes>
     *          <node lon="" lat="" class="businessObjectClass">businessObjectId</node>
     *      </nodes>
     *      <edges>
     *          <edge id="" class="" asideid="" asideclass="" bsideid="" bsideclass="">
     *              <controlpoint lon="" lat=""></controlpoint>
     *          </edge>
     *      </edges>
     *  </view>
     * }
     * </pre>
     * @param sessionId The session token id.
     * @return The id of the newly created view.
     */
    @RequestMapping(method = RequestMethod.POST, value = "createOSPView/{name}/{description}/{structure}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public long createOSPView(
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.SESSION_ID) String sessionId
    ) {
        try {
            aem.validateCall("createOSPView", "127.0.0.1", sessionId);
            if (RestUtil.isBase64(structure)) {
                byte [] queryStructure =  Base64.decodeBase64(structure);
                return aem.createOSPView(name, description, queryStructure);
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure", "createOSPView"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure", "createOSPView"));
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
     * Retrieves the specific information about an existing OSP view.
     * @param id The id of the view.
     * @param sessionId The session token id.
     * @return An object containing the view details and structure.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getOSPView/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ViewObject getOSPView(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId
    ) {
        try {
            aem.validateCall("getOSPView", "127.0.0.1", sessionId);
            return aem.getOSPView(id);
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
     * Retrieves the existing OSP views.
     * @param sessionId The session token id.
     * @return The list of existing OSP views.
     */
    @RequestMapping(method = RequestMethod.GET, value = "getOSPViews/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ViewObjectLight> getOSPViews(
            @PathVariable(RestConstants.SESSION_ID) String sessionId
    ) {
        try {
            aem.validateCall("getOSPViews", "127.0.0.1", sessionId);
            return aem.getOSPViews();
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
     * Updates an existing OSP view.
     * @param id The id of the view.
     * @param name The new name of the view. Null if to remain unchanged.
     * @param description The new description of the view. Null if to remain unchanged.
     * @param structure The structure of the view as string Base64, 
     * from the XML document with the contents of the view. The format of the XML document is consistent with the other views:
     * <pre>
     * {@code
     *  <view version="">
     *      <class>OSPView</class>
     *      <center lon="" lat=""></center>
     *      <zoom>0</zoom>
     *      <nodes>
     *          <node lon="" lat="" class="businessObjectClass">businessObjectId</node>
     *      </nodes>
     *      <edges>
     *          <edge id="" class="" asideid="" asideclass="" bsideid="" bsideclass="">
     *              <controlpoint lon="" lat=""></controlpoint>
     *          </edge>
     *      </edges>
     *  </view>
     * }
     * </pre>
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "updateOSPView/{id}/{name}/{description}/{structure}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateOSPView(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.NAME) String name,
            @PathVariable(RestConstants.DESCRIPTION) String description,
            @PathVariable(RestConstants.STRUCTURE) String structure,
            @PathVariable(RestConstants.SESSION_ID) String sessionId
    ) {
        try {
            aem.validateCall("updateOSPView", "127.0.0.1", sessionId);
            if (RestUtil.isBase64(structure)) {
                byte [] queryStructure =  Base64.decodeBase64(structure);
                aem.updateOSPView(id, name, description, queryStructure);
            } else {
                LOG.error(String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure", "updateOSPView"));
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format(ts.getTranslatedString("api.rest.general.error.base64-format-attribute-error"), "structure", "updateOSPView"));
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
     * Deletes an existing OSP view.
     * @param id The id of the view to be deleted.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteOSPView/{id}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteOSPView(
            @PathVariable(RestConstants.ID) long id,
            @PathVariable(RestConstants.SESSION_ID) String sessionId
    ) {
        try {
            aem.validateCall("deleteOSPView", "127.0.0.1", sessionId);
            aem.deleteOSPView(id);
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
    //</editor-fold>
}