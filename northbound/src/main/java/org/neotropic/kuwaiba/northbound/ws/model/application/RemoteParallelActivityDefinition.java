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
import javax.xml.bind.annotation.XmlElement;

/**
 * This is just an Parallel Activity Definition wrapper
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteParallelActivityDefinition extends RemoteActivityDefinition implements Serializable {
    /**
     * List of paths
     */
    @XmlElement(name = "paths")
    private List<RemoteActivityDefinition> paths;
    /**
     * Outgoing or Incoming or Incoming/Outgoing  Sequence Flows
     */
    private int sequenceFlow;
    /**
     * Outgoing Sequence Flow Id (Fork Parallel Activity Definition Id)
     */
    private String outgoingSequenceFlowId;
    /**
     * Incoming Sequence Flow Id (Join Parallel Activity Definition Id)
     */
    private String incomingSequenceFlowId;
    
    public RemoteParallelActivityDefinition(String id, String name, String description, 
        int type, RemoteArtifactDefinition arfifact, RemoteActor actor, boolean confirm, 
        String color, List<RemoteKpi> kpis, List<RemoteKpiAction> kpiActions) {
        
        super(id, name, description, type, arfifact, actor, false, confirm, color, kpis, kpiActions);
    }
            
    @Override
    public boolean isIdling() {
        return false;
    }
        
    public List<RemoteActivityDefinition> getPaths() {
        return paths;
    }
        
    public void setPaths(List<RemoteActivityDefinition> paths) {
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
