/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.service;

import com.neotropic.flow.component.mxgraph.bpmn.BPMNNode;
import java.util.HashMap;

/**
 * Represents the definition of an artifact.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class ArtifactDefinition {
    /**
     * Node to which the artifact corresponds
     */
    private BPMNNode bpmnNode;
    /**
     * Artifact id
     */
    private String id;
    /**
     * Artifact type
     */
    private int type;
    /**
     * Artifact parameters
     */
    private HashMap<String, String> parameters;

    public ArtifactDefinition() { }

    public BPMNNode getBpmnNode() {
        return bpmnNode;
    }

    public void setBpmnNode(BPMNNode bpmnNode) {
        this.bpmnNode = bpmnNode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }    
}