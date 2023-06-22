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
package org.neotropic.kuwaiba.core.configuration.filters.nodes;

import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.FilterDefinition;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * Represents a filter definition in the node.
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
public class FilterDefinitionNode extends AbstractNode<FilterDefinition> {

    public FilterDefinitionNode(FilterDefinition object) {
        super(object);
    }

    @Override
    public AbstractAction[] getActions() {
        return new AbstractAction[0];
    }

    @Override
    public void refresh(boolean recursive) {
    } 
}