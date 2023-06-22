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

import com.neotropic.flow.component.mxgraph.bpmn.BPMNDiagram;
import com.neotropic.flow.component.mxgraph.bpmn.TaskNode;
import com.neotropic.kuwaiba.modules.commercial.processman.diagram.provider.ActivityNode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MxTaskActivityNode extends TaskNode implements ActivityNode {
    private final List<ClickListener> clickListeners = new ArrayList();
    private boolean enabled;

    public MxTaskActivityNode(BPMNDiagram graph) {
        super(graph);
        addClickCellListener(clickEvent -> 
            new ArrayList<>(clickListeners).forEach(listener -> {
                if (clickListeners.contains(listener))
                    listener.accept(new ClickEvent(listener));
            })
        );
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public boolean isExecuted() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setExecuted(boolean execute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void addClickListener(ClickListener listener) {
        clickListeners.add(listener);
    }

    @Override
    public void removeClickListener(ClickListener listener) {
        clickListeners.removeIf(l -> l.equals(l));
    }

    @Override
    public void removeAllClickListeners() {
        clickListeners.clear();
    }
}
