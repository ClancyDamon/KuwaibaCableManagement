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
package org.neotropic.kuwaiba.modules.optional.serviceman.visual;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.modules.optional.serviceman.components.ServiceManTreeNode;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.grids.IconNameCellGrid;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;

/**
 * Represents a cell in a Grid that contains an icon and a label
 * to show a businessObjectLigth or a inventoryObjectPool.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class IconLabelCellGrid extends HorizontalLayout {
    /**
     * Constructor
     * @param object Business Object or Inventory Object Pool
     * @param isPool Validates if object is a pool
     * @param isSelected Validates if selected
     * @param iconGenerator An icon generator for create icons
     */
    public IconLabelCellGrid(ServiceManTreeNode object
            , boolean isPool, boolean isSelected
            , ClassNameIconGenerator iconGenerator) {

        if (isPool) {
            Icon icon = isSelected ? new Icon(VaadinIcon.FOLDER_OPEN) : new Icon(VaadinIcon.FOLDER);
            icon.setSize("16px");

            Label lblTitle = new Label(object.getName());
            lblTitle.setClassName("wrap-item-label");
            lblTitle.setId("lblTitle");
            this.add(icon, lblTitle);
        } else if (object.getObject() instanceof BusinessObjectLight) {
            BusinessObjectLight businessObject = (BusinessObjectLight) object.getObject();
            FormattedObjectDisplayNameSpan itemName = new FormattedObjectDisplayNameSpan(
                    businessObject, false, false, true, false);
            IconNameCellGrid iconNameCell = new IconNameCellGrid(itemName, businessObject.getClassName(), iconGenerator);
            iconNameCell.setClassName("wrap-item-label");
            iconNameCell.setId("iconNameCell");
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