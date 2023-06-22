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
package org.neotropic.kuwaiba.modules.core.navigation.explorers.grids;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.awt.Color;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;

/**
 * Represents a cell in a Grid that contains an icon and a label
 * to show a relationship.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class IconRelationshipCellGrid extends HorizontalLayout {
    /**
     * Default no icon width (used in navigation trees)
     */
    public static final int DEFAULT_SMALL_NO_ICON_WIDTH = 10;
    /**
     * Default no icon height (used in navigation trees)
     */
    public static final int DEFAULT_SMALL_NO_ICON_HEIGHT = 10;
    
    /**
     * Constructor
     * @param relationship The type relationship
     * @param resourceFactory Factory resources
     */
    public IconRelationshipCellGrid(String relationship, ResourceFactory resourceFactory) {
        Label lblIcon = new Label(relationship);
        Image objIcon = new Image();
        
        if (relationship.equals("parent"))
            objIcon.setSrc(StreamResourceRegistry.getURI(resourceFactory.getRelationshipIcon(
                    Color.decode("#f04f47"), DEFAULT_SMALL_NO_ICON_WIDTH, DEFAULT_SMALL_NO_ICON_HEIGHT)).toString());
        else 
            objIcon.setSrc(StreamResourceRegistry.getURI(resourceFactory.getRelationshipIcon(
                    Color.decode("#5bb327"), DEFAULT_SMALL_NO_ICON_WIDTH, DEFAULT_SMALL_NO_ICON_HEIGHT)).toString());
        
        this.add(objIcon, lblIcon);
        this.setBoxSizing(BoxSizing.BORDER_BOX);  
        this.setSpacing(true);
        this.setMargin(false);
        this.setPadding(false);
        this.setSizeUndefined();
        this.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    }
}