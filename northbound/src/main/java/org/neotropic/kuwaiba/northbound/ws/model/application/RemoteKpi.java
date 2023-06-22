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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Kpi;

/**
 * Wrapper of KPI
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteKpi implements Serializable {
    private String name;
    private String description;
    private String action;
    private Properties thresholds;
    
    public RemoteKpi() {
    }
    
    public RemoteKpi(String name, String description, String action, Properties thresholds) {
        this.name = name;
        this.description = description;
        this.action = action;
        this.thresholds = thresholds;
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
    
    public void setDecription(String description) {
        this.description = description;                
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public Properties getThresholds() {
        return thresholds;
    }
    
    public void setThresholds(Properties thresholds) {
        this.thresholds = thresholds;
    }
    
    public static RemoteKpi asRemoteKpi(Kpi kpi) {
        return kpi != null ? new RemoteKpi(kpi.getName(), kpi.getDescription(), kpi.getAction(), kpi.getThresholds()) : null;
    }
    
    public static List<RemoteKpi> asRemoteKpis(List<Kpi> kpis) {
        if (kpis != null) {
            List<RemoteKpi> remoteKpis = new ArrayList();
            
            for (Kpi kpi : kpis) {
                RemoteKpi remoteKpi = asRemoteKpi(kpi);
                if (remoteKpi != null)
                    remoteKpis.add(remoteKpi);
            }
            return remoteKpis;
        }
        return null;
    }
}
