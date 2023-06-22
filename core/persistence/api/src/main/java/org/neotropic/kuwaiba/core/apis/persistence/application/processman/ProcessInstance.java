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

import java.util.Objects;

/**
 * The representation of a a running process
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ProcessInstance {
    /**
     * The id of the process
     */
    private String id;
    /**
     * The name of the process
     */
    private String name;
    /**
     * The description of the process
     */
    private String description;
    /**
     * The pointer to the current activity (that is, the las activity that was committed successfully). 
     * When a process was just created, the pointer will point to a dummy TYPE_START activity. When it's finished, will point to a dummy TYPE_END activity
     */
    private String currentActivityId;
    /**
     * A reference to the process definition the current process instance was spawned from
     */
    private String processDefinitionId;
    /**
     * A XML Structure that storage the content of the artifacts to this process instance
     */
    private byte[] artifactsContent;
        
    public ProcessInstance(String id, String name, String description, String currentActivityId, String processDefinitionId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currentActivityId = currentActivityId;
        this.processDefinitionId = processDefinitionId;
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

    public String getCurrentActivityId() {
        return currentActivityId;
    }

    public void setCurrentActivity(String currentActivityId) {
        this.currentActivityId = currentActivityId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinition(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }
    
    public void setArtifactsContent(byte[] artifactsContent) {
        this.artifactsContent = artifactsContent;
    }
    
    public byte[] getArtifactsContent() {
        return artifactsContent;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.id);
        
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
        final ProcessInstance other = (ProcessInstance) obj;
        return this.id != null && this.id.equals(other.id);
    }
        
}
