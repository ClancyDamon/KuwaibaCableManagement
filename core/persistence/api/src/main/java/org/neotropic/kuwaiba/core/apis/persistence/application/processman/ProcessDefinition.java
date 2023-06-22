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
package org.neotropic.kuwaiba.core.apis.persistence.application.processman;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Wraps the definition of a process. The activities are represented as a linked list
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ProcessDefinition {
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
     * List of actors.
     */
    private HashMap<String, Actor> actors;
    /**
     * List of activities.
     */
    private HashMap<String, ActivityDefinition> activityDefinitions;
    /**
     * Reference to the start activity (typically a TYPE_START type of activity). The rest will be linked from this one
     */
    private ActivityDefinition startActivity;
    /**
     * List of Key Performance Indicators to Process Definition
     */
    private List<Kpi> kpis;
    /**
     * List of Key Performance Indicator Actions to Process Definition
     */
    private List<KpiAction> kpiActions;
    /**
     * Process definition as byte array.
     */
    private byte[] definition;
        
    public ProcessDefinition(String id, String name, String description, long creationDate, 
            String version, boolean enabled, ActivityDefinition startActivity, List<Kpi> kpis, List<KpiAction> kpiActions) {
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

    public ActivityDefinition getStartActivity() {
        return startActivity;
    }

    public void setStartActivity(ActivityDefinition startActivity) {
        this.startActivity = startActivity;
    }
    
    public List<Kpi> getKpis() {
        return kpis;
    }
    
    public void setKpis(List<Kpi> kpis) {
        this.kpis = kpis;
    }
    
    public List<KpiAction> getKpiActions() {
        return kpiActions;
    }
    
    public void setKpiActions(List<KpiAction> kpiActions) {
        this.kpiActions = kpiActions;
    }
    
    public byte[] getDefinition() {
        return definition;
    }
    
    public void setDefinition(byte[] definition) {
        this.definition = definition;
    }
    
    public HashMap<String, Actor> getActors() {
        return actors;
    }
    
    public void setActors(HashMap<String, Actor> actors) {
        this.actors = actors;
    }
    
    public HashMap<String, ActivityDefinition> getActivityDefinitions() {
        return activityDefinitions;
    }
    
    public void setActivityDefinitions(HashMap<String, ActivityDefinition> activityDefinitions) {
        this.activityDefinitions = activityDefinitions;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.id);
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
        final ProcessDefinition other = (ProcessDefinition) obj;
        return this.id != null && this.id.equals(other.id);
    }
        
}
