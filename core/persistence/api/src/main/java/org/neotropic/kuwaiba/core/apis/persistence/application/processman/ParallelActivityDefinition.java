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
 * Representation of a BPMN flow object -Parallel Gateway-
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ParallelActivityDefinition extends ActivityDefinition {
    /**
     * Outgoing Sequence Flows
     */
    public static int FORK = 1;
    /**
     * Incoming Sequence Flows
     */
    public static int JOIN = 2;
    /**
     * Incoming/Outgoing Sequence Flows
     */    
    public static int JOIN_FORK = 3;
    /**
     * List of paths
     */
    private List<ActivityDefinition> paths;
    /**
     * Outgoing or Incoming or Incoming/Outgoing  Sequence Flows
     */
    private int sequenceFlow = FORK;
    /**
     * Outgoing Sequence Flow Id (Fork Parallel Activity Definition Id)
     */
    private String outgoingSequenceFlowId;
    /**
     * Incoming Sequence Flow Id (Join Parallel Activity Definition Id)
     */
    private String incomingSequenceFlowId;
    
    public ParallelActivityDefinition(String id, String name, String description, int type, ArtifactDefinition arfifact, Actor actor, List<Kpi> kpis, List<KpiAction> kpiActions, boolean idling, boolean confirm, String color) {
        super(id, name, description, type, arfifact, actor, kpis, kpiActions, idling, confirm, color);
    }
    
    @Override
    public boolean isIdling() {
        return false;
    }
    
    public List<ActivityDefinition> getPaths() {
        return paths;
    }
    
    public void setPaths(List<ActivityDefinition> paths) {
        this.paths = paths;
    }
    
    public int getSequenceFlow() {
        return sequenceFlow;
    }
    
    public void setSequenceFlow(int sequenceFlow) {
        this.sequenceFlow = sequenceFlow;
    }
    
    public String getOutgoingSequenceFlowId() {
        return outgoingSequenceFlowId;
    }
    
    public void setOutgoingSequenceFlowId(String outgoingSequenceFlowId) {
        this.outgoingSequenceFlowId = outgoingSequenceFlowId;
    }
    
    public String getIncomingSequenceFlowId() {
        return incomingSequenceFlowId;
    }
    
    public void setIncomingSequenceFlowId(String incomingSequenceFlowId) {
        this.incomingSequenceFlowId = incomingSequenceFlowId;
    }
}
