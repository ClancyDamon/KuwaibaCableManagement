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
package org.neotropic.kuwaiba.modules.core.navigation.navtree;

import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.server.StreamResourceRegistry;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.icons.IconGenerator;

/**
 * Component to navigate through the relationships of the inventory objects. 
 * The nature of the navigation is defined by the data provider.
 * For example show containment relations, special relations, ...
 * @param <T> Node type for inventory object.
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NavigationTree<T extends InventoryObjectNode> extends TreeGrid<T> {
    public NavigationTree(IconGenerator<T> iconGenerator) {
        addComponentHierarchyColumn(item -> {
            Image imgIcon = new Image(StreamResourceRegistry.getURI(iconGenerator.apply(item)).toString(), "");
            imgIcon.setWidth("15px");
            imgIcon.setHeight("15px");
            FormattedObjectDisplayNameSpan spanItem = new FormattedObjectDisplayNameSpan(item.getObject());
            HorizontalLayout lytItem = new HorizontalLayout(imgIcon, spanItem);
            lytItem.setMargin(false);
            lytItem.setPadding(false);
            lytItem.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
            return lytItem;
        });
        addThemeVariants(
            GridVariant.LUMO_NO_BORDER, 
            GridVariant.LUMO_NO_ROW_BORDERS, 
            GridVariant.LUMO_COMPACT
        );
    }
    
     public NavigationTree(HierarchicalDataProvider<T, Void> dataProvider, IconGenerator<T> iconGenerator) {
        this(iconGenerator);
        setDataProvider(dataProvider);
    }
}
