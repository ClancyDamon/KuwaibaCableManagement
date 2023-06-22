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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ArtifactDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ConditionalActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ParallelActivityDefinition;

/**
 * wrapper of ActivityDefinition. An activity is an step in a process. Conditionals are a particular type of activities from the point of view of this API. This class
 * is a representation of a definition of an activity, which is basically a description of what it does (like presenting a form for the user 
 * to fill it in). The activity definition has at least one artifact definition, which contains (in our example) the actual form.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlRootElement(name = "remoteActivityDefinition")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteActivityDefinition implements Serializable {
    /**
     * Activity definition id
     */
    private String id;
    /**
     * Activity definition name
     */
    private String name;
    /**
     * Activity definition description
     */
    private String description;
    /**
     * Activity type. See TYPE_* for valid values
     */
    private int type;
    /**
     * Define if an Activity can be mark as idle activity
     */    
    private boolean idling;
    /**
     * Define if is necessary Confirm before commit the Activity
     */
    private boolean confirm;
    /**
     * Define the activity color
     */
    private String color;
    /**
     * Artifact associated to the activity definition
     */
    private RemoteArtifactDefinition arfifact;
    /**
     * The actor responsible to execute this activity
     */
    private RemoteActor actor;
    /**
     * The next activity according to the flow defined in the process definition
     */
    private RemoteActivityDefinition nextActivity;
    /**
     * List of Key Performance Indicators
     */
    @XmlElement(name = "kpis")
    private List<RemoteKpi> kpis;
    /**
     * List of Key Performance Indicator Actions
     */
    @XmlElement(name = "kpiActions")
    private List<RemoteKpiAction> kpiActions;
    
    public RemoteActivityDefinition() {
    }

    public RemoteActivityDefinition(String id, String name, String description, 
            int type, RemoteArtifactDefinition arfifact, RemoteActor actor, boolean idling, boolean confirm, String color, List<RemoteKpi> kpis, List<RemoteKpiAction> kpiActions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.arfifact = arfifact;
        this.actor = actor;
        this.idling = idling;
        this.confirm = confirm;
        this.color = color;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public RemoteArtifactDefinition getArfifact() {
        return arfifact;
    }

    public void setArfifact(RemoteArtifactDefinition arfifact) {
        this.arfifact = arfifact;
    }

    public RemoteActor getActor() {
        return actor;
    }

    public void setActor(RemoteActor actor) {
        this.actor = actor;
    }

    public RemoteActivityDefinition getNextActivity() {
        return nextActivity;
    }

    public void setNextActivity(RemoteActivityDefinition nextActivity) {
        this.nextActivity = nextActivity;
    }
    
    public boolean isIdling() {
        return idling;
    }
        
    public void setIdling(boolean idling) {
        this.idling = idling;                
    }
    
    public boolean confirm() {
        return confirm;
    }
        
    public void setConfirm(boolean confirm) {
        this.confirm = confirm;        
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
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
    
    public static RemoteActivityDefinition asRemoteActivityDefinition(ActivityDefinition activityDefinition) {
        RemoteActivityDefinition res;
        
        if (activityDefinition instanceof ConditionalActivityDefinition) {
            res = new RemoteConditionalActivityDefinition(activityDefinition.getId(), activityDefinition.getName(), 
                activityDefinition.getDescription(), activityDefinition.getType(), 
                new RemoteArtifactDefinition(
                    activityDefinition.getArfifact().getId(), 
                    activityDefinition.getArfifact().getName(), 
                    activityDefinition.getArfifact().getDescription(), 
                    activityDefinition.getArfifact().getVersion(), 
                    activityDefinition.getArfifact().getType(), 
                    activityDefinition.getArfifact().getDefinition(), 
                    activityDefinition.getArfifact().getPreconditionsScript(), 
                    activityDefinition.getArfifact().getPostconditionsScript(), 
                    activityDefinition.getArfifact().isPrintable(), 
                    activityDefinition.getArfifact().getPrintableTemplate(),
                    activityDefinition.getArfifact().getExternalScripts()), 
                activityDefinition.getActor() != null ? new RemoteActor(activityDefinition.getActor().getId(), activityDefinition.getActor().getName(), activityDefinition.getActor().getType()) : null,
                activityDefinition.confirm(), 
                activityDefinition.getColor(),
                RemoteKpi.asRemoteKpis(activityDefinition.getKpis()),
                RemoteKpiAction.asRemoteKpiActions(activityDefinition.getKpiActions()));
            
            ConditionalActivityDefinition conditionalActivityDefinition = (ConditionalActivityDefinition) activityDefinition;
            RemoteConditionalActivityDefinition remoteConditionalActivityDefinition = (RemoteConditionalActivityDefinition) res;
            
            if (conditionalActivityDefinition.getNextActivityIfTrue() != null) 
                remoteConditionalActivityDefinition.setNextActivityIfTrue(RemoteActivityDefinition.asRemoteActivityDefinition(conditionalActivityDefinition.getNextActivityIfTrue()));
            
            if (conditionalActivityDefinition.getNextActivityIfFalse() != null) 
                remoteConditionalActivityDefinition.setNextActivityIfFalse(RemoteActivityDefinition.asRemoteActivityDefinition(conditionalActivityDefinition.getNextActivityIfFalse()));
            
            if (conditionalActivityDefinition.getInformationArtifact() != null) {
                ArtifactDefinition informationArtifactDef = conditionalActivityDefinition.getInformationArtifact();
                remoteConditionalActivityDefinition.setInformationArtifact(new RemoteArtifactDefinition(
                    informationArtifactDef.getId(), 
                    informationArtifactDef.getName(), 
                    informationArtifactDef.getDescription(), 
                    informationArtifactDef.getVersion(), 
                    informationArtifactDef.getType(), 
                    informationArtifactDef.getDefinition(), 
                    informationArtifactDef.getPreconditionsScript(), 
                    informationArtifactDef.getPostconditionsScript(), 
                    informationArtifactDef.isPrintable(), 
                    informationArtifactDef.getPrintableTemplate(), 
                    informationArtifactDef.getExternalScripts()));
            }
            
        } 
        else if (activityDefinition instanceof ParallelActivityDefinition) {
            res = new RemoteParallelActivityDefinition(activityDefinition.getId(), activityDefinition.getName(), 
                activityDefinition.getDescription(), activityDefinition.getType(), 
                new RemoteArtifactDefinition(
                    activityDefinition.getArfifact().getId(), 
                    activityDefinition.getArfifact().getName(), 
                    activityDefinition.getArfifact().getDescription(), 
                    activityDefinition.getArfifact().getVersion(), 
                    activityDefinition.getArfifact().getType(), 
                    activityDefinition.getArfifact().getDefinition(), 
                    activityDefinition.getArfifact().getPreconditionsScript(), 
                    activityDefinition.getArfifact().getPostconditionsScript(), 
                    activityDefinition.getArfifact().isPrintable(), 
                    activityDefinition.getArfifact().getPrintableTemplate(),
                    activityDefinition.getArfifact().getExternalScripts()), 
                activityDefinition.getActor() != null ? new RemoteActor(activityDefinition.getActor().getId(), activityDefinition.getActor().getName(), activityDefinition.getActor().getType()) : null,
                activityDefinition.confirm(), 
                activityDefinition.getColor(),
                RemoteKpi.asRemoteKpis(activityDefinition.getKpis()),
                RemoteKpiAction.asRemoteKpiActions(activityDefinition.getKpiActions()));
            
            ParallelActivityDefinition parallelActivityDef = (ParallelActivityDefinition) activityDefinition;
            RemoteParallelActivityDefinition remoteParallelActivityDef = (RemoteParallelActivityDefinition) res;
            if (parallelActivityDef.getPaths() != null) {
                if (remoteParallelActivityDef.getPaths() == null)
                    remoteParallelActivityDef.setPaths(new ArrayList());
                                                                                                                
                for (ActivityDefinition path : parallelActivityDef.getPaths()) {
                    remoteParallelActivityDef.getPaths().add(RemoteActivityDefinition.asRemoteActivityDefinition(path));
                }
                remoteParallelActivityDef.setSequenceFlow(parallelActivityDef.getSequenceFlow());
                remoteParallelActivityDef.setOutgoingSequenceFlowId(parallelActivityDef.getOutgoingSequenceFlowId());
                remoteParallelActivityDef.setIncomingSequenceFlowId(parallelActivityDef.getIncomingSequenceFlowId());
            }
        }
        else {
            
            
            res = new RemoteActivityDefinition(activityDefinition.getId(), activityDefinition.getName(), 
                activityDefinition.getDescription(), activityDefinition.getType(), 
                new RemoteArtifactDefinition(
                    activityDefinition.getArfifact().getId(), 
                    activityDefinition.getArfifact().getName(), 
                    activityDefinition.getArfifact().getDescription(), 
                    activityDefinition.getArfifact().getVersion(), 
                    activityDefinition.getArfifact().getType(), 
                    activityDefinition.getArfifact().getDefinition(), 
                    activityDefinition.getArfifact().getPreconditionsScript(), 
                    activityDefinition.getArfifact().getPostconditionsScript(), 
                    activityDefinition.getArfifact().isPrintable(), 
                    activityDefinition.getArfifact().getPrintableTemplate(), 
                    activityDefinition.getArfifact().getExternalScripts()), 
                activityDefinition.getActor() != null ? new RemoteActor(activityDefinition.getActor().getId(), activityDefinition.getActor().getName(), activityDefinition.getActor().getType()) : null,
                activityDefinition.isIdling(),
                activityDefinition.confirm(), 
                activityDefinition.getColor(),
                RemoteKpi.asRemoteKpis(activityDefinition.getKpis()),
                RemoteKpiAction.asRemoteKpiActions(activityDefinition.getKpiActions()));
            
            if (activityDefinition.getNextActivity() != null) 
                res.setNextActivity(RemoteActivityDefinition.asRemoteActivityDefinition(activityDefinition.getNextActivity()));
        }
        return res;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
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
        final RemoteActivityDefinition other = (RemoteActivityDefinition) obj;
        return this.id != null && this.id.equals(other.id);
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
    
}
