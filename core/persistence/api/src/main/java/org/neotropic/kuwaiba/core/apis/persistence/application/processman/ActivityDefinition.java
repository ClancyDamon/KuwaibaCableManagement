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

import java.util.List;

/**
 * An activity is an step in a process. Conditionals are a particular type of activities from the point of view of this API. This class
 * is a representation of a definition of an activity, which is basically a description of what it does (like presenting a form for the user 
 * to fill it in). The activity definition has at least one artifact definition, which contains (in our example) the actual form.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ActivityDefinition extends ActivityDefinitionLight {
    /**
     * The actor responsible to execute this activity
     */
    private Actor actor;
    /**
     * The next activity according to the flow defined in the process definition
     */
    private ActivityDefinition nextActivity;
    /**
     * List of Key Performance Indicators to Activity Definition
     */
    private List<Kpi> kpis;
    /**
     * List of Key Performance Indicator Actions to Activity Definition
     */
    private List<KpiAction> kpiActions;
    
    public ActivityDefinition(String id, String name, String description, 
            int type, ArtifactDefinition arfifact, Actor actor, List<Kpi> kpis, List<KpiAction> kpiActions, boolean idling, boolean confirm,String color) {
        super(id, name, description, type, arfifact, idling, confirm, color);
        this.actor = actor;
        this.kpis = kpis;
        this.kpiActions = kpiActions;
    }
    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public ActivityDefinition getNextActivity() {
        return nextActivity;
    }

    public void setNextActivity(ActivityDefinition nextActivity) {
        this.nextActivity = nextActivity;
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
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
        
}
