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
package com.neotropic.kuwaiba.modules.commercial.ipam.visual;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;

/**
 * Represents a cell in a Grid that contains an icon and a label
 * to show a businessObjectLigth or a ClassMetadata.
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class IconLabelCellGrid extends HorizontalLayout{
    /**
     * Constructor
     * @param displayName the  wished display name 
     * for the business object light: obj name + [ ]
     * @param className the object class name 
     * @param iconGenerator an icon generator
     */
    public IconLabelCellGrid(String displayName, String className, boolean isPool, boolean isSelected) {
        Label lblTitle = new Label(displayName);
        Icon icon;
        if(isPool)
            icon = isSelected ? new Icon(VaadinIcon.FOLDER_OPEN) : new Icon(VaadinIcon.FOLDER);
        else {
            if(className.equals(Constants.CLASS_IP_ADDRESS)) 
                icon = new Icon(VaadinIcon.PASSWORD);
            else //is a subnet
                icon = new Icon(VaadinIcon.SITEMAP);
        }
        icon.setSize("16px");
        this.add(icon, lblTitle);
        this.setBoxSizing(BoxSizing.BORDER_BOX);  
        this.setSpacing(true);
        this.setMargin(false);
        this.setPadding(false);
        this.setSizeUndefined();
        this.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    }
    
}
