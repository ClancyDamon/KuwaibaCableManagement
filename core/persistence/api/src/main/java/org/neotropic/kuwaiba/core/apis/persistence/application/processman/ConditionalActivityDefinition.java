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

import java.util.List;

/**
 * Representation of a BPMN flow object -Exclusive Gateway-
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ConditionalActivityDefinition extends ActivityDefinition {
    /**
     * Next activity for true path.
     */
    private ActivityDefinition nextActivityIfTrue;
    /**
     * Next activity for false path.
     */
    private ActivityDefinition nextActivityIfFalse;
    /**
     * Artifact of Read Only to show the information necessary which is it used 
     * by the user to take the decision of use the true or the false path.
     */
    private ArtifactDefinition informationArtifact;
    
    public ConditionalActivityDefinition(String id, String name, String description, 
        int type, boolean confirm, String color, ArtifactDefinition arfifact, Actor actor, List<Kpi> kpis, List<KpiAction> kpiActions, ArtifactDefinition informationArtifact) {
        
        super(id, name, description, type, arfifact, actor, kpis, kpiActions, false, confirm, color);
        this.informationArtifact = informationArtifact;
    }
    
    @Override
    public boolean isIdling() {
        return false;
    }
    
    public ActivityDefinition getNextActivityIfTrue() {
        return nextActivityIfTrue;
    }
    
    public void setNextActivityIfTrue(ActivityDefinition nextActivityIfTrue) {
        this.nextActivityIfTrue = nextActivityIfTrue;
    }
    
    public ActivityDefinition getNextActivityIfFalse() {
        return nextActivityIfFalse;
    }
    
    public void setNextActivityIfFalse(ActivityDefinition nextActivityIfFalse) {
        this.nextActivityIfFalse = nextActivityIfFalse;
    }
    
    public ArtifactDefinition getInformationArtifact() {
        return informationArtifact;
    }
    
    public void setInformationArfifact(ArtifactDefinition informationArtifact) {
        this.informationArtifact = informationArtifact;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
