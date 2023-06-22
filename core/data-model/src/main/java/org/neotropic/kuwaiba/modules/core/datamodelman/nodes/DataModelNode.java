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
package org.neotropic.kuwaiba.modules.core.datamodelman.nodes;

import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * Represents a node in the data model manager tree.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>}
 */
public class DataModelNode extends AbstractNode<ClassMetadataLight>{

    public DataModelNode(ClassMetadataLight object) {
        super(object);
        this.id = String.valueOf(object.getId());
        this.name = object.getName();
        this.displayName = object.getDisplayName();
    }
    
    public DataModelNode(ClassMetadataLight object, String displayName) {
        super(object);
        this.id = String.valueOf(object.getId());
        this.name = object.getName();
        this.displayName = object.getDisplayName();
    }
    
    @Override
    public AbstractAction[] getActions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

    @Override
    public void refresh(boolean recursive) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}