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

package org.neotropic.kuwaiba.core.apis.integration.modules;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.springframework.stereotype.Service;

/**
 * All modules (core, optional and commercial) must be registered here at startup. The registry 
 * will later be used to create menus and check privileges.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class ModuleRegistry {
    /**
     * A map with the id of the module as key and the module itself as value.
     */
    private HashMap<String, AbstractModule> registeredModules;
    
    public ModuleRegistry() {
        this.registeredModules = new LinkedHashMap<>();
    }

    public void registerModule(AbstractModule module) {
        this.registeredModules.put(module.getId(), module);
    }
    
    public HashMap<String, AbstractModule> getModules() {
        return registeredModules;
    }
}
