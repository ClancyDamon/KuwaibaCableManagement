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
package org.neotropic.kuwaiba.modules.optional.connectivityman.actions;

import java.util.List;

/**
 * Parent class of connectivity actions that apply to links.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class AbstractLinkConnectivityAction extends AbstractConnectivityVisualAction {
    private String name;
    private Object selectedLink;
    private List selectedObjects;
    
    public AbstractLinkConnectivityAction(Connection connection) {
        super(connection);
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setSelectedLink(Object selectedLink) {
        this.selectedLink = selectedLink;
    }
    
    public Object getSelectedLink() {
        return selectedLink;
    }
    
    public void setSelectedObjects(List selectedObjects) {
        this.selectedObjects = selectedObjects;
    }
    
    public List getSelectedObjects() {
        return selectedObjects;
    }
}
