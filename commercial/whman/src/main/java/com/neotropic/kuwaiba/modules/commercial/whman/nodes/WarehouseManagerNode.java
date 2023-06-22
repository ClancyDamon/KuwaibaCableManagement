/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.whman.nodes;

import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * Represents a node in the pool items tree.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class WarehouseManagerNode extends AbstractNode<BusinessObjectLight> {

    public WarehouseManagerNode(BusinessObjectLight object) {
        super(object);
    }
    
    public WarehouseManagerNode(BusinessObjectLight object, String displayName) {
        super(object);
        this.displayName = displayName;
    }

    @Override
    public AbstractAction[] getActions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refresh(boolean recursive) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}