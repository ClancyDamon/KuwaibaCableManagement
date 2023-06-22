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
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.physcon.windows.PhysicalPathViewWindow;
import org.neotropic.kuwaiba.modules.optional.physcon.windows.PhysicalTreeViewWindow;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to show the set of fiber tools.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowFiberTools extends ConfirmDialog {
    public enum Tool {
        PHYSICAL_PATH_A,
        PHYSICAL_TREE_A,
        PHYSICAL_PATH_B,
        PHYSICAL_TREE_B,
        PROPERTY_SHEET
    }
    private final BusinessObjectLight fiber;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final PhysicalConnectionsService physicalConnectionsService;
    private final Consumer<BusinessObjectLight> consumerObjectChange;
    
    public WindowFiberTools(BusinessObjectLight fiber, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, 
        TranslationService ts, PhysicalConnectionsService physicalConnectionsService, 
        Consumer<BusinessObjectLight> consumerObjectChange) {
        
        Objects.requireNonNull(fiber);
        
        this.fiber = fiber;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.physicalConnectionsService = physicalConnectionsService;
        this.consumerObjectChange = consumerObjectChange;
    }

    @Override
    public void open() {
        try {
            HashMap<String, List<BusinessObjectLight>> endpoints = bem.getSpecialAttributes(fiber.getClassName(), fiber.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_A, OspConstants.SPECIAL_ATTR_ENDPOINT_B);
            boolean hasEndpointA = endpoints.containsKey(OspConstants.SPECIAL_ATTR_ENDPOINT_A);
            boolean hasEndpointB = endpoints.containsKey(OspConstants.SPECIAL_ATTR_ENDPOINT_B);
            
            ListBox<Tool> lstTools = new ListBox();
            lstTools.setItems(Tool.PROPERTY_SHEET, Tool.PHYSICAL_PATH_A, Tool.PHYSICAL_TREE_A, Tool.PHYSICAL_PATH_B, Tool.PHYSICAL_TREE_B);
            lstTools.addComponents(Tool.PROPERTY_SHEET, new Hr());
            lstTools.addComponents(Tool.PHYSICAL_TREE_A, new Hr());
            lstTools.setItemEnabledProvider(item -> {
                if (!hasEndpointA && (item.equals(Tool.PHYSICAL_PATH_A) || item.equals(Tool.PHYSICAL_TREE_A)))
                    return false;
                if (!hasEndpointB && (item.equals(Tool.PHYSICAL_PATH_B) || item.equals(Tool.PHYSICAL_TREE_B)))
                    return false;
                return true;
            });
            lstTools.setRenderer(new ComponentRenderer<>(tool -> {
                String text = "";
                if (Tool.PROPERTY_SHEET.equals(tool))
                    text = ts.getTranslatedString("module.propertysheet.labels.header");
                else if (Tool.PHYSICAL_PATH_A.equals(tool))
                    text = ts.getTranslatedString("module.ospman.fiber-tools.tool.physical-path.endpoint-a");
                else if (Tool.PHYSICAL_TREE_A.equals(tool))
                    text = ts.getTranslatedString("module.ospman.fiber-tools.tool.physical-tree.endpoint-a");
                else if (Tool.PHYSICAL_PATH_B.equals(tool))
                    text = ts.getTranslatedString("module.ospman.fiber-tools.tool.physical-path.endpoint-b");
                else if (Tool.PHYSICAL_TREE_B.equals(tool))
                    text = ts.getTranslatedString("module.ospman.fiber-tools.tool.physical-tree.endpoint-b");
                return new Label(text);
            }));
            lstTools.addValueChangeListener(valueChangeEvent -> {
                try {
                    close();
                    Tool tool = valueChangeEvent.getValue();
                    if (Tool.PROPERTY_SHEET.equals(tool)) {
                        new WindowObjectProperties(fiber, aem, bem, mem, ts, consumerObjectChange).open();
                        
                    } else if (Tool.PHYSICAL_PATH_A.equals(tool)) {
                        new PhysicalPathViewWindow(
                            endpoints.get(OspConstants.SPECIAL_ATTR_ENDPOINT_A).get(0), 
                            bem, aem, mem, ts, physicalConnectionsService
                        ).open();
                    } else if (Tool.PHYSICAL_TREE_A.equals(tool)) {
                        new PhysicalTreeViewWindow(
                            endpoints.get(OspConstants.SPECIAL_ATTR_ENDPOINT_A).get(0), 
                            bem, aem, mem, ts, physicalConnectionsService
                        ).open();
                    } else if (Tool.PHYSICAL_PATH_B.equals(tool)) {
                        new PhysicalPathViewWindow(
                            endpoints.get(OspConstants.SPECIAL_ATTR_ENDPOINT_B).get(0), 
                            bem, aem, mem, ts, physicalConnectionsService
                        ).open();
                    } else if (Tool.PHYSICAL_TREE_B.equals(tool)) {
                        new PhysicalTreeViewWindow(
                            endpoints.get(OspConstants.SPECIAL_ATTR_ENDPOINT_B).get(0), 
                            bem, aem, mem, ts, physicalConnectionsService
                        ).open();
                    }
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            });
            setHeader(String.format(ts.getTranslatedString("module.ospman.fiber-tools.title"), fiber.getName()));
            setContent(lstTools);
            setFooter(new Button(ts.getTranslatedString("module.general.messages.close"), event -> close()));
            setDraggable(true);
            super.open();
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }
}
