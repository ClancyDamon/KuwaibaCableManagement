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
package org.neotropic.kuwaiba.core.apis.persistence.application;

import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A class representing a business rule. A business rule is composed by constraints, which 
 * are conditions that must be met in order to allow certain actions to be executed.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class BusinessRule {
    /**
     * Object of class #{appliesTo} can't be related to object of class B
     */
    public static int TYPE_RELATIONSHIP_BY_CLASS = 1;
    /**
     * Object of class #{appliesTo} can only be related to object B with this set of relationship names (defined in the rule constraints)
     */
    public static int TYPE_RELATIONSHIP_BY_RELATIONSHIP_NAME = 2;
    /**
     * Object of class #{appliesTo} can only be related to object B if ObjectA.attributeA = Z and ObjectB.attributeB = Y
     */
    public static int TYPE_RELATIONSHIP_BY_ATTRIBUTE_VALUE = 3;
    /**
     * Object of class #{appliesTo} can only be child of Object B if Object B has its attribute C set to X (as defined by a rule constraint)
     */
    public static int TYPE_STANDARD_CONTAINMENT = 5;
    /**
     * Object of class #{appliesTo} can only be special child of Object B if Object B has its attribute C set to X (as defined by a rule constraint)
     */
    public static int TYPE_SPECIAL_CONTAINMENT = 6;
    /**
     * See #{scope} for details
     */
    public static int SCOPE_GLOBAL = 1;
    /**
    * Rule id
    **/
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
    private List<BusinessRuleConstraint> constraints;

    public BusinessRule(long ruleId, String name, String description, String appliesTo, int type, int scope, String version) {
        this.ruleId = ruleId;
        this.name = name;
        this.description = description;
        this.appliesTo = appliesTo;
        this.type = type;
        this.scope = scope;
        this.version = version;
        this.constraints = new ArrayList<>();
    }
    
    public BusinessRule(long ruleId, Map<String, Object> ruleProperties) {
        this.ruleId = ruleId;
        this.name = (String)ruleProperties.get(Constants.PROPERTY_NAME);
        this.description = (String)ruleProperties.get(Constants.PROPERTY_DESCRIPTION);
        this.appliesTo = (String)ruleProperties.get(Constants.PROPERTY_APPLIES_TO);
        this.type = (int)ruleProperties.get(Constants.PROPERTY_TYPE);
        this.scope = (int)ruleProperties.get(Constants.PROPERTY_SCOPE);
        this.version = (String)ruleProperties.get(Constants.PROPERTY_VERSION);
        this.constraints = new ArrayList<>();
        for (int i = 1; i < ruleProperties.size() - 5; i++) {
            String constraintName = "constraint" + i;
            constraints.add(new BusinessRuleConstraint(constraintName, (String)ruleProperties.get(constraintName)));
        }
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

    public List<BusinessRuleConstraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<BusinessRuleConstraint> constraints) {
        this.constraints = constraints;
    }
}
