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
package org.neotropic.kuwaiba.core.apis.integration.external.services;

import java.util.ArrayList;
import java.util.List;

/**
 * An external service provider is a third-party application to which we connect 
 * to use their services.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ExternalServiceProvider {
    /**
     * Name of the third-party application.
     */
    protected String name;
    /**
     * List of the third-party application services implemented or supported.
     */
    protected final List<AbstractExternalService> externalServices = new ArrayList();
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Gets the external services.
     * @return The list of the third-party application services implemented.
     */
    public List<AbstractExternalService> getExternalServices() {
        return externalServices;
    }
}
