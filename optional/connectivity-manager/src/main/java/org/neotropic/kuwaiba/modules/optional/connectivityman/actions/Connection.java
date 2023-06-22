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

import org.neotropic.util.visual.selectors.PortSelector;

/**
 * A connection is a set of the connection type, connection source
 * connection target, and action to execute.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class Connection {
    private Integer id;
    private AbstractConnectivityActionBuilder type;
    private PortSelector source;
    private PortSelector target;
    private AbstractConnectivityAction action;
    
    public Connection() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    public AbstractConnectivityActionBuilder getType() {
        return type;
    }
    
    public void setType(AbstractConnectivityActionBuilder type) {
        this.type = type;
    }
    
    public PortSelector getSource() {
        return source;
    }

    public void setSource(PortSelector source) {
        this.source = source;
    }

    public PortSelector getTarget() {
        return target;
    }

    public void setTarget(PortSelector target) {
        this.target = target;
    }
    
    public AbstractConnectivityAction getAction() {
        return action;
    }

    public void setAction(AbstractConnectivityAction action) {
        this.action = action;
    }
}
