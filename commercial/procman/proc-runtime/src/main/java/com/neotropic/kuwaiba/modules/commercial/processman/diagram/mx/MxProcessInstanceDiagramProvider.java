/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.commercial.processman.diagram.mx;

import com.neotropic.flow.component.mxgraph.bpmn.BPMNConnection;
import com.neotropic.flow.component.mxgraph.bpmn.BPMNDiagram;
import com.neotropic.flow.component.mxgraph.bpmn.BPMNNode;
import com.neotropic.flow.component.mxgraph.bpmn.SymbolNode;
import com.neotropic.kuwaiba.modules.commercial.processman.diagram.provider.ActivityNode;
import com.neotropic.kuwaiba.modules.commercial.processman.diagram.provider.ActorNode;
import com.neotropic.kuwaiba.modules.commercial.processman.diagram.provider.ProcessDefinitionDiagramProvider;
import java.util.LinkedHashMap;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinitionLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Actor;

/**
 * Implements the Process Instance Diagram Provider using mxGraph.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MxProcessInstanceDiagramProvider implements ProcessDefinitionDiagramProvider<BPMNDiagram> {
    private final BPMNDiagram diagram;
    private final LinkedHashMap<ActivityDefinition, ActivityNode> activities = new LinkedHashMap();
    
    public MxProcessInstanceDiagramProvider() {
        diagram = new BPMNDiagram();
        diagram.setSizeFull();
    }
    
    @Override
    public BPMNDiagram getUiElement() {
        return diagram;
    }

    @Override
    public ActorNode addActor(Actor actor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ActivityNode addActivity(ActivityDefinition nextActivity, ActivityDefinition previousActivity, String pathName) {
        Objects.requireNonNull(nextActivity);
        BPMNNode nextNode = (BPMNNode) activities.get(nextActivity);
        if (nextNode == null) {
            switch(nextActivity.getType()) {
                case ActivityDefinitionLight.TYPE_CONDITIONAL:
                    nextNode = new MxSymbolActivityNode(diagram, SymbolNode.SymbolType.Fork);
                break;
                case ActivityDefinition.TYPE_END:
                    nextNode = new MxSymbolActivityNode(diagram, SymbolNode.SymbolType.Event_End);
                break;
                case ActivityDefinition.TYPE_NORMAL:
                    nextNode = new MxTaskActivityNode(diagram);
                break;
                case ActivityDefinition.TYPE_PARALLEL:
                    nextNode = new MxSymbolActivityNode(diagram, SymbolNode.SymbolType.Merge);
                break;
                case ActivityDefinition.TYPE_START:
                    nextNode = new MxSymbolActivityNode(diagram, SymbolNode.SymbolType.Event);
                break;
                default:
                    nextNode = new MxTaskActivityNode(diagram);
                break;
            }
            nextNode.setLabel(nextActivity.getName());
            diagram.addNode(nextNode);
            activities.put(nextActivity, (ActivityNode) nextNode);
        }
        if (previousActivity != null && activities.containsKey(previousActivity)) {
            BPMNNode previousNode = (BPMNNode) activities.get(previousActivity);
            
            BPMNConnection connection = new BPMNConnection(diagram);
            connection.setSource(previousNode.getUuid());
            connection.setTarget(nextNode.getUuid());
            if (pathName != null)
                connection.setLabel(pathName);
            diagram.addEdge(connection);
        }
        return activities.get(nextActivity);
    }

    @Override
    public void executeLayout(ActivityDefinition activityDefinition) {
        if (activities.containsKey(activityDefinition)) {
            ((BPMNNode) activities.get(activityDefinition)).addCellAddedListener(
                event -> diagram.executeHierarchicalLayout("1")
            );
        }
    }
}
