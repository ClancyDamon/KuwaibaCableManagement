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
package org.neotropic.kuwaiba.northbound.ws.model.application;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteConditionalActivityDefinition extends RemoteActivityDefinition implements Serializable {
    /**
     * Next activity for true path.
     */    
    private RemoteActivityDefinition nextActivityIfTrue;
    /**
     * Next activity for false path.
     */
    private RemoteActivityDefinition nextActivityIfFalse;
    /**
     * Artifact of Read Only to show the information necessary which is it used 
     * by the user to take the decision of use the true or the false path.
     */
    private RemoteArtifactDefinition informationArtifact;
        
    public RemoteConditionalActivityDefinition(String id, String name, String description, 
        int type, RemoteArtifactDefinition arfifact, RemoteActor actor, boolean confirm, 
        String color, List<RemoteKpi> kpis, List<RemoteKpiAction> kpiActions) {
        
        super(id, name, description, type, arfifact, actor, false, confirm, color, kpis, kpiActions);
    }
    
    @Override
    public boolean isIdling() {
        return false;
    }
    
    public RemoteActivityDefinition getNextActivityIfTrue() {
        return nextActivityIfTrue;
    }
    
    public void setNextActivityIfTrue(RemoteActivityDefinition nextActivityIfTrue) {
        this.nextActivityIfTrue = nextActivityIfTrue;
    }
    
    public RemoteActivityDefinition getNextActivityIfFalse() {
        return nextActivityIfFalse;
    }
    
    public void setNextActivityIfFalse(RemoteActivityDefinition nextActivityIfFalse) {
        this.nextActivityIfFalse = nextActivityIfFalse;
    }    
    
    public RemoteArtifactDefinition getInformationArtifact() {
        return informationArtifact;
    }
    
    public void setInformationArtifact(RemoteArtifactDefinition informationArtifact) {
        this.informationArtifact = informationArtifact;
    }
        
}
