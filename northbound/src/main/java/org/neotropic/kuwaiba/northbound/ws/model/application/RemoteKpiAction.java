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

import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.KpiAction;

/**
 * Wrapper of KPI Action
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RemoteKpiAction {
    private int type;
    private String name;
    private String description;
    private String script;
    
    public RemoteKpiAction() {
    }
    
    public RemoteKpiAction(int type, String name, String description, String script) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.script = script;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;        
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
    
    public static RemoteKpiAction asRemoteKpiAction(KpiAction kpiAction) {
        return kpiAction != null ? new RemoteKpiAction(kpiAction.getType(), kpiAction.getName(), kpiAction.getDescription(), kpiAction.getScript()) : null;
    }
    
    public static List<RemoteKpiAction> asRemoteKpiActions(List<KpiAction> kpiActions) {
        if (kpiActions != null) {
            List<RemoteKpiAction> remoteKpiActions = new ArrayList();
            
            for (KpiAction kpiAction : kpiActions) {
                RemoteKpiAction remoteKpiAction = asRemoteKpiAction(kpiAction);
                if (remoteKpiAction != null)
                    remoteKpiActions.add(remoteKpiAction);
            }
            return remoteKpiActions;
        }
        return null;
    }
}
