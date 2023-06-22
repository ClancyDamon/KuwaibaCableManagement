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
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper of ProcessDefinition. A process definition is the metadata of a process. It contains the set of 
 * activities, conditionals and the flow that connects everything
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlRootElement(name = "remoteProcessDefinition")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteProcessDefinition implements Serializable {
    /**
     * Process id
     */
    private String id;
    /**
     * Process name
     */
    private String name;
    /**
     * Process description
     */
    private String description;
    /**
     * Process creation date
     */
    private long creationDate;
    /**
     * Process version, expressed as a three numeric sections separated by a dot (e.g. 1.3.1)
     */
    private String version;
    /**
     * If instances of the current process can be created or not
     */
    private boolean enabled;
    /**
     * Reference to the start activity (typically a TYPE_START type of activity). The rest will be linked from this one
     */
    private RemoteActivityDefinition startActivity;
    /**
     * List of Key Performance Indicators to Process Definition
     */
    @XmlElement(name = "kpis")
    private List<RemoteKpi> kpis;
    /**
     * List of Key Performance Indicator Actions to Process Definition
     */
    @XmlElement(name = "kpiActions")
    private List<RemoteKpiAction> kpiActions;

    public RemoteProcessDefinition() { }

    public RemoteProcessDefinition(String id, String name, String description, long creationDate, 
            String version, boolean enabled, RemoteActivityDefinition startActivity, 
            List<RemoteKpi> kpis, List<RemoteKpiAction> kpiActions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.version = version;
        this.enabled = enabled;
        this.startActivity = startActivity;
        this.kpis = kpis;
        this.kpiActions = kpiActions;
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

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public RemoteActivityDefinition getStartActivity() {
        return startActivity;
    }

    public void setStartAction(RemoteActivityDefinition startActivity) {
        this.startActivity = startActivity;
    }
    
    public List<RemoteKpi> getKpis() {
        return kpis;
    }
    
    public void setKpis(List<RemoteKpi> kpis) {
        this.kpis = kpis;
    }
    
    public List<RemoteKpiAction> getKpiActions() {
        return kpiActions;
    }
    
    public void setKpiActions(List<RemoteKpiAction> kpiActions) {
        this.kpiActions = kpiActions;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteProcessDefinition other = (RemoteProcessDefinition) obj;
        return this.id != null && this.id.equals(other.id);
    }
        
}
