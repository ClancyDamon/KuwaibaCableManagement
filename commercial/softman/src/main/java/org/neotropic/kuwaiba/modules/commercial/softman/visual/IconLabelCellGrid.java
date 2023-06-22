/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.softman.visual;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.modules.commercial.softman.nodes.SoftwareObjectNode;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.grids.IconNameCellGrid;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;

/**
 * Represents a cell in a Grid that contains an icon and a label. 
 * To show a pool or business object.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class IconLabelCellGrid extends HorizontalLayout {
    /**
     * Constructor
     * @param object Pool or business Object 
     * @param isPool Validates if object is a pool
     * @param iconGenerator An icon generator for create icons
     */
    public IconLabelCellGrid(SoftwareObjectNode object, boolean isPool, ClassNameIconGenerator iconGenerator) {
        if (isPool) {
            Icon icon = new Icon(VaadinIcon.FOLDER);
            icon.setSize("16px");

            Label lblTitle = new Label(object.getName());
            this.add(icon, lblTitle);
        } else if (object.getObject() instanceof BusinessObjectLight) {
            BusinessObjectLight businessObject = (BusinessObjectLight) object.getObject();
            FormattedObjectDisplayNameSpan itemName = new FormattedObjectDisplayNameSpan(
                    businessObject, false, false, true, false);
            IconNameCellGrid iconNameCell = new IconNameCellGrid(itemName, businessObject.getClassName(), iconGenerator);

            this.add(iconNameCell);
        }
        this.setBoxSizing(BoxSizing.BORDER_BOX);
        this.setSpacing(true);
        this.setMargin(false);
        this.setPadding(false);
        this.setSizeUndefined();
        this.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    }
}