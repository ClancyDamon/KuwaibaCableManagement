/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        https://apache.org/licenses/LICENSE-2.0.txt
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.neotropic.kuwaiba.core.apis.persistence.application.processman;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class KpiResult {
    /** 
     * Compliance level values between 1 and 10, where 1 no compliant and is 10 fully compliant
     */
    private int complianceLevel;
    private List<String> observations;
    private HashMap<String, Object> values;
    
    public KpiResult(int complianceLevel, List<String> observations, HashMap<String, Object> values) {
        this.complianceLevel = complianceLevel;
        this.observations = observations;
        this.values = values;
    }
    
    public int getComplianceLevel() {
        return complianceLevel;
    }
    
    public void setComplianceLevel(int complianceLevel) {
        this.complianceLevel = complianceLevel;
    }
    
    public List<String> getObservations() {
        return observations;
    }
    
    public void setObservations(List<String> observations) {
        this.observations = observations;
    }
    
    public HashMap<String, Object> getValues() {
        return values;
    }
    
    public void getValues(HashMap<String, Object> values) {
        this.values = values;
    }
}
