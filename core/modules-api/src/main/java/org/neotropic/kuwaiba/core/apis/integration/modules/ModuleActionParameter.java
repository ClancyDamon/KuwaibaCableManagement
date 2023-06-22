 /*  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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

/**
 * Each module action has an optional set of parameters (for example, creating a new inventory 
 * object requires the parent of the new object, its type and optionally a list of default values to be
 * set). Instances of this class encapsulates every parameter, and the respective action 
 * is responsible to define the type of the parameters that its capable of receiving. See {@link ModuleAction} 
 * for more details.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <T> The type of the parameter. 
 */
public class ModuleActionParameter<T> {
    private String name;
    private T value;

    public ModuleActionParameter(String name, T value) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
