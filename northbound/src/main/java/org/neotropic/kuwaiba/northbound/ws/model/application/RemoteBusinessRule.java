/**
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
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Wrapper of BusinessRule
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteBusinessRule implements Serializable {
    /**
     * Rule id
     */
    private long ruleId;
    /**
     * Rule name
    */
    private String name;
    /**
     * Rule description
     **/
    private String description;
    /**
     * Class this rule applies to (you will need to duplicate a rule to apply it to different classes of different class hierarchy branches). Abstract classes are allowed
     */
    private String appliesTo;
    /**
     * Rule definition may change with new versions, so we keep this value to correctly migrate instances when needed
     */
    private String version;
    /**
     * Rule type. See TYPE_* for possible values
     */
    private int type;
    /**
     * Not yet sure how this will be useful, but I'll leave it here for now. The general idea is that not all rules must be applicable globally, it may depend on some 
     */
    private int scope;
    /**
     * The list of constraints that define the rule. These constrains are the actual logic that should be checked against before to perform certain action
     */
    private List<RemoteBusinessRuleConstraint> constraints;

    public RemoteBusinessRule(long ruleId, String name, String description, String appliesTo, int type, int scope, String version) {
        this.ruleId = ruleId;
        this.name = name;
        this.description = description;
        this.appliesTo = appliesTo;
        this.type = type;
        this.scope = scope;
        this.version = version;
        this.constraints = new ArrayList<>();
    }

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
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

    public String getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<RemoteBusinessRuleConstraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<RemoteBusinessRuleConstraint> constraints) {
        this.constraints = constraints;
    }
}