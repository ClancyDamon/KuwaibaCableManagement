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
package org.neotropic.util.visual.grids;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResourceRegistry;
import org.neotropic.util.visual.icons.IconGenerator;

/**
 * Represents a cell in a Grid that contains an icon and a label
 * to show a businessObjectLigth or a ClassMetadata.
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class IconNameCellGrid extends HorizontalLayout{
    
    /**
     * Constructor
     * @param displayName the  wished display name 
     * for the business object light: obj name + [ ]
     * @param className the object class name 
     * @param iconGenerator an icon generator
     */
    public IconNameCellGrid(String displayName, String className, IconGenerator iconGenerator) {
        Image objIcon = new Image(StreamResourceRegistry.getURI(iconGenerator.apply(className)).toString(), "-");
        Label lblTitle = new Label(displayName);
        
        this.add(objIcon, lblTitle);
        this.setBoxSizing(BoxSizing.BORDER_BOX);  
        this.setSpacing(true);
        this.setMargin(false);
        this.setPadding(false);
        this.setSizeUndefined();
        this.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    }
    
    /**
     * Constructor
     * @param cmpName a component that includes the object name, class name and it validators
     * @param className the object class name 
     * @param iconGenerator an icon generator
     */
    public IconNameCellGrid(Component cmpName, String className, IconGenerator iconGenerator) {
        Image objIcon = new Image(StreamResourceRegistry.getURI(iconGenerator.apply(className)).toString(), "-");
        
        this.add(objIcon, cmpName);
        this.setBoxSizing(BoxSizing.BORDER_BOX);  
        this.setSpacing(true);
        this.setMargin(false);
        this.setPadding(false);
        this.setSizeUndefined();
        this.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    }
}
