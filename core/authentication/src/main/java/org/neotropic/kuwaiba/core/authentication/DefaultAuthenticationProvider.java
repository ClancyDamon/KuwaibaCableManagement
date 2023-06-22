/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.core.authentication;

import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;

/**
 * Authenticates users using the built-in database.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DefaultAuthenticationProvider extends AbstractAuthenticationProvider {
    /**
     * Reference to the Application Entity Manager. 
     */
    private ApplicationEntityManager aem;
    /**
     * 
     * @param parameters The Application Entity Manager as aem.
     * @throws AuthenticationException If the aem variable is not supplied and it's not the expected type.
     */
    @Override
    public void configureProvider(HashMap<String, Object> parameters) throws AuthenticationException {
        if (parameters.containsKey("aem") && parameters.get("aem") instanceof ApplicationEntityManager) {
            this.aem = (ApplicationEntityManager)parameters.get("aem");
        } else
            throw new AuthenticationException("");
    }

    @Override
    public Session login(String user, String password, int sessionType) throws AuthenticationException {
        try {
            return this.aem.createSession(user, password, sessionType);
        } catch (InventoryException ex) {
            throw new AuthenticationException(ex.getMessage());
        }
    }

}
