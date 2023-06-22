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
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;

/**
 * Defines the general behavior of the classes that will authenticate users against 
 * different providers, such an Active Directory or the built-in database.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractAuthenticationProvider {
    /**
     * Sets the parameters necessary for the authentication to work (database handle, Active Directory settings, etc).
     * @param parameters The set of parameters. The implementor must document what is required.
     * @throws AuthenticationException If something goes wrong, most likely, the parameters are insufficient or not correct.
     */
    public abstract void configureProvider(HashMap<String, Object> parameters) throws AuthenticationException;
    /**
     * Attempts to authenticate a user. 
     * @param user The user.
     * @param password The password.
     * @param sessionType The session type (web, desktop, web service, other)
     * @return A session object containing the user profile and its privileges, basically.
     * @throws AuthenticationException If the authentication process is incorrect. 
     */
    public abstract Session login(String user, String password, int sessionType) throws AuthenticationException;
}
