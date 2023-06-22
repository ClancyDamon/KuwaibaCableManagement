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

/**
 * A convenience class to better handle a group of parameters to be provided to a 
 * module action. It is a wrapper of a regular HashMap.
 */
public class ModuleActionParameterSet extends HashMap<String, Object> {
    public ModuleActionParameterSet(ModuleActionParameter... parameters) {
        for (ModuleActionParameter parameter : parameters)
            put(parameter.getName(), parameter.getValue());
    }
}
