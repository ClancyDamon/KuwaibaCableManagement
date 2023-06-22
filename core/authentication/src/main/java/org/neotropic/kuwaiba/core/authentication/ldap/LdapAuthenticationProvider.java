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

package org.neotropic.kuwaiba.core.authentication.ldap;

import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.authentication.AbstractAuthenticationProvider;
import org.neotropic.kuwaiba.core.authentication.AuthenticationException;

/**
 * Provides LDAP authentication and deals with the authorization using local tokens 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LdapAuthenticationProvider extends AbstractAuthenticationProvider {

    @Override
    public void configureProvider(HashMap<String, Object> parameters) throws AuthenticationException {
        // Do nothing for now
    }

    @Override
    public Session login(String user, String password, int sessionType) throws AuthenticationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
