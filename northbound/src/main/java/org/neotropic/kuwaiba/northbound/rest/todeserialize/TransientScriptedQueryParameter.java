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
package org.neotropic.kuwaiba.northbound.rest.todeserialize;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * A script parameter is a name and value pair to be processed on the execute 
 * of the script in the {@link ScriptingQuery#getScript()}.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Setter
@Getter
public class TransientScriptedQueryParameter implements Serializable {
    /**
     * Scripted Query Parameter Id.
     */
    private String id;
    /**
     * Scripted Query Parameter Name.
     */
    private String name;
    /**
     * Scripted Query Parameter Description.
     */
    private String description;
    /**
     * Scripted Query Parameter Type.
     */
    private String type;
    /**
     * Scripted Query Parameter Default Value.
     */
    private Object defaultValue;
    /**
     * Scripted Query Parameter Value.
     */
    private Object value;
    /**
     * If the Scripted Query Parameter is Mandatory.
     */
    private boolean mandatory;

    public TransientScriptedQueryParameter() { }
    
    public TransientScriptedQueryParameter(String name, Object value) {
        this.name = name;
        this.value = value;
    }
    
    public TransientScriptedQueryParameter(String id, String name, String description, String type, boolean mandatory, Object defaultValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.defaultValue = defaultValue;
        this.mandatory = mandatory;
    }
}