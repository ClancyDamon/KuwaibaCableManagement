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

package org.neotropic.kuwaiba.northbound.ws.model.application;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Represents the configuration of a validator. A validator is a mechanism that 
 * allows to check certain conditions related to a given inventory object and embed the results in a 
 * BusinessObjectLight instance. For example, a validator may check if an object has a particular operationalState (say "In Operation", "Decommissioned")
 * and return the result in the BusinessObjectLight instance, instead of having to retrieve the whole object (a BusinessObject instance) to 
 * check the condition at client side. This is particularly useful when calling methods like getObjectChildren, since the result is a list of BusinessObjectLight 
 * instances, not BusinessObjects. The value of a validator can be then used to, among other things, render the objects matching a condition in a certain way, 
 * for example, in a Navigation Tree, all connected ports can be displayed in red, while those reserved in orange.
 * 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteValidatorDefinition implements Serializable {
    /**
     * Id of the validator definition
     */
    private long id;
    /**
     * The name of the validator
     */
    private String name;
    /**
     * validator description
     */
    private String description;
    /**
     * The validators check conditions for instances of certain classes. Here you specify which one. It supports abstract superclasses
     */
    private String classToBeApplied;
    /**
     * A Groovy script that receiving the object id and class name as parameters, performs the logic that does the validation
     */
    private String script;
    /**
     * Is the validator be enabled for execution?
     */
    private boolean enabled;

    public RemoteValidatorDefinition() { }
    
    public RemoteValidatorDefinition(long id, String name, String description, String classToBeApplied, String script, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.classToBeApplied = classToBeApplied;
        this.script = script;
        this.enabled = enabled;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getClassToBeApplied() {
        return classToBeApplied;
    }

    public void setClassToBeApplied(String classToBeApplied) {
        this.classToBeApplied = classToBeApplied;
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
