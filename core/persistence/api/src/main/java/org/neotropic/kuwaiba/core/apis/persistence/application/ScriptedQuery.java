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
 * Query which execute a script that can be simple script or use database queries 
 * and the Persistence API to get a result.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ScriptedQuery {
    /**
     * Scripted query id.
     */
    private String id;
    /**
     * Scripted query name.
     */
    private String name; 
    /**
     * Scripted query description.
     */
    private String description; 
    /**
     * Scripted query script.
     */
    private String script; 
    /**
     * Scripted query enabled.
     */
    private boolean enabled; 
    
    public ScriptedQuery(String id, String name, String description, String script, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.script = script;
        this.enabled = enabled;
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
    
    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
