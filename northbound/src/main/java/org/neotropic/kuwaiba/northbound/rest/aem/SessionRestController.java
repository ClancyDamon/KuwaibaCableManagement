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

import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
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
 * Session Manager Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(SessionRestController.PATH)
public class SessionRestController implements SessionRestOpenApi {
    
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
    public static Logger LOG = LoggerFactory.getLogger(SessionRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and the module id
     */
    public static final String PATH = "/v2.1/session-manager/"; //NOI18N
    
    // <editor-fold desc="session-manager" defaultstate="collapsed">
    /**
     * Creates a session. System users can not create sessions.
     * @param user User name.
     * @param password Password.
     * @param sessionType The type of session to be created. This type depends on what kind of client is trying to access (a desktop client, a web client, a web service user, etc. See Session.TYPE_XXX for possible session types).
     * @return A session object with information about the session itself plus information about the user.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "createSession/{user}/{password}/{sessionType}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Session createSession(
            @PathVariable(RestConstants.USER) String user,
            @PathVariable(RestConstants.PASSWORD) String password,
            @PathVariable(RestConstants.SESSION_TYPE) int sessionType)
    {
        try {
            return aem.createSession(user, password, sessionType);
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
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
     * Closes a session.
     * @param sessionId The session id (token).
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "closeSession/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void closeSession(@PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("closeSession", "127.0.0.1", sessionId);
            aem.closeSession(sessionId);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    //  </editor-fold>
}