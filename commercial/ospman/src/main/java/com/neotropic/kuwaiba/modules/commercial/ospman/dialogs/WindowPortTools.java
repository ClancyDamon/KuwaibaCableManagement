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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ManagePortMirroringVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.WindowManagePortMirroring;
import org.neotropic.kuwaiba.modules.optional.physcon.windows.PhysicalPathViewWindow;
import org.neotropic.kuwaiba.modules.optional.physcon.windows.PhysicalTreeViewWindow;
import org.neotropic.util.visual.dialog.ConfirmDialog;

/**
 * Set of tools to manage ports.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowPortTools extends ConfirmDialog {
    public enum Tool {
        RELEASE_PORT,
        SHOW_PHYSICAL_PATH,
        SHOW_PHYSICAL_TREE,
        MANAGE_PORT_MIRRORING,
        PROPERTY_SHEET
    }
    
    public WindowPortTools(BusinessObjectLight port, 
        ApplicationEntityManager aem,
        BusinessEntityManager bem,
        MetadataEntityManager mem,
        TranslationService ts,
        PhysicalConnectionsService physicalConnectionsService,
        ManagePortMirroringVisualAction managePortMirroringVisualAction,
        BiConsumer<List<BusinessObjectLight>, String> consumerReleaseFiber,
        Consumer<BusinessObjectLight> consumerObjectChange) {
        ListBox<Tool> lstTools = new ListBox();
        lstTools.setItems(Tool.PROPERTY_SHEET, Tool.MANAGE_PORT_MIRRORING, Tool.SHOW_PHYSICAL_PATH, Tool.SHOW_PHYSICAL_TREE, Tool.RELEASE_PORT);
        
        lstTools.setRenderer(new ComponentRenderer<>(tool -> {
            String text = "";
            if (Tool.PROPERTY_SHEET.equals(tool))
                text = ts.getTranslatedString("module.propertysheet.labels.header");
            else if (Tool.RELEASE_PORT.equals(tool))
                text = ts.getTranslatedString("module.ospman.port-tools.tool.release-port");
            else if (Tool.SHOW_PHYSICAL_PATH.equals(tool))
                text = ts.getTranslatedString("module.ospman.port-tools.tool.show-physical-path");
            else if (Tool.SHOW_PHYSICAL_TREE.equals(tool))
                text = ts.getTranslatedString("module.ospman.port-tools.tool.show-physical-tree");
            else if (Tool.MANAGE_PORT_MIRRORING.equals(tool))
                text = ts.getTranslatedString("module.physcon.actions.manage-port-mirroring.name");
            return new Label(text);
        }));
        lstTools.addValueChangeListener(event -> {
            close();
            Tool tool = event.getValue();
            if (Tool.PROPERTY_SHEET.equals(tool)) {
                new WindowObjectProperties(port, aem, bem, mem, ts, consumerObjectChange).open();
                
            } if (Tool.RELEASE_PORT.equals(tool)) {
                if (consumerReleaseFiber != null) {
                    try {
                        HashMap<String, List<BusinessObjectLight>> endpoints = bem.getSpecialAttributes(port.getClassName(), port.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_A, OspConstants.SPECIAL_ATTR_ENDPOINT_B);
                        List<BusinessObjectLight> endpointsA = endpoints.get(OspConstants.SPECIAL_ATTR_ENDPOINT_A);
                        List<BusinessObjectLight> endpointsB = endpoints.get(OspConstants.SPECIAL_ATTR_ENDPOINT_B);
                        if (endpointsA != null && !endpointsA.isEmpty())
                            consumerReleaseFiber.accept(Arrays.asList(port, endpointsA.get(0)), OspConstants.SPECIAL_ATTR_ENDPOINT_A);
                        else if (endpointsB != null && !endpointsB.isEmpty())
                            consumerReleaseFiber.accept(Arrays.asList(port, endpointsB.get(0)), OspConstants.SPECIAL_ATTR_ENDPOINT_B);
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                }
            } else if (Tool.SHOW_PHYSICAL_PATH.equals(tool)) {
                try {
                    new PhysicalPathViewWindow(port, bem, aem, mem, ts, physicalConnectionsService).open();
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            } else if (Tool.SHOW_PHYSICAL_TREE.equals(tool)) {
                try {
                    new PhysicalTreeViewWindow(port, bem, aem, mem, ts, physicalConnectionsService).open();
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            } else if (Tool.MANAGE_PORT_MIRRORING.equals(tool)) {
                WindowManagePortMirroring wdwManagePortMirroring = managePortMirroringVisualAction.getVisualComponent(
                    new ModuleActionParameterSet(new ModuleActionParameter("businessObject", port))
                );
                wdwManagePortMirroring.open();
            }            
        });
        setHeader(String.format(ts.getTranslatedString("module.ospman.port-tools.title"), port.getName()));
        setContent(lstTools);
        setFooter(new Button(ts.getTranslatedString("module.general.messages.cancel"), event -> close()));
        setDraggable(true);
    }
}
