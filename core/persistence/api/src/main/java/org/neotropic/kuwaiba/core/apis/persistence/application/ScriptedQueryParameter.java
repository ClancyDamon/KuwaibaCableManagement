/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.core.apis.persistence.application;

/**
 * A script parameter is a name and value pair to be processed on the execute 
 * of the script in the {@link ScriptingQuery#getScript()}.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ScriptedQueryParameter {
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
    
    public ScriptedQueryParameter(String name, Object value) {
        this.name = name;
        this.value = value;
    }
    
    public ScriptedQueryParameter(String id, String name, String description, String type, boolean mandatory, Object defaultValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.defaultValue = defaultValue;
        this.mandatory = mandatory;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Object getDefaultValue() {
        return this.defaultValue;
    }
    
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }
}
