/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.util.visual.tree;

import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.server.StreamResourceRegistry;
import org.neotropic.util.visual.icons.IconGenerator;
import org.neotropic.util.visual.tree.nodes.AbstractNode;


/**
 * A tree that extends the features of the Tree Grid and makes use of the Nodes API
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @param <T> Entity name for the tree grid
 */
public class BasicTree<T extends AbstractNode> extends TreeGrid<T> {
    
    public BasicTree(HierarchicalDataProvider<T, Void> dataProvider, IconGenerator<T> iconGenerator) {
        StringBuilder template = new StringBuilder();
        template.append("<vaadin-grid-cell-content ");
        template.append("<p style = \"white-space: normal; overflow-wrap: anywhere;\"></p>");
        
        template.append("<vaadin-grid-tree-toggle ");
        template.append("leaf='[[item.leaf]]' ");
        template.append("expanded='{{expanded}}' ");
        template.append("level='[[level]]'");
        template.append(">");
        
        template.append("<img ");
        template.append("width='15px' ");
        template.append("height='15px' ");
        template.append("src='[[item.icon]]' ");
        template.append("alt='' ");
        template.append(">");
        
        template.append("<p style=\"margin:0 0 0 10px;\">[[item.name]]</p>");
        
        template.append("</vaadin-grid-tree-toggle>");
        template.append("</vaadin-grid-cell-content>");
        
        TemplateRenderer<T> renderer = TemplateRenderer.<T> of (template.toString());
        renderer.withProperty("icon", item -> {               
            return item.getIconUrl() == null ? StreamResourceRegistry.getURI(iconGenerator.apply(item)).toString() :
                           item.getIconUrl();          
            });
        renderer.withProperty("name", item -> item.getDisplayName());
        
        addColumn(renderer).setAutoWidth(true);
        addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        setDataProvider(dataProvider);
    }    
}