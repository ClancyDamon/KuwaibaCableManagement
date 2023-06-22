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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ManagePortMirroringVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.WindowManagePortMirroring;
import org.neotropic.util.visual.dialog.ConfirmDialog;

/**
 * Set of tools to devices
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowDeviceTools extends ConfirmDialog {
    public enum Tool {
        MANAGE_PORT_MIRRORING
    }
    public WindowDeviceTools(BusinessObjectLight device, TranslationService ts, 
        ManagePortMirroringVisualAction managePortMirroringVisualAction) {
        
        ListBox<Tool> lstTools = new ListBox();
        lstTools.setItems(Tool.MANAGE_PORT_MIRRORING);
        
        lstTools.setRenderer(new ComponentRenderer<>(tool -> {
            String text = "";
            if (Tool.MANAGE_PORT_MIRRORING.equals(tool))
                text = ts.getTranslatedString("module.physcon.actions.manage-port-mirroring.name");
            return new Label(text);
        }));
        lstTools.addValueChangeListener(event -> {
            Tool tool = event.getValue();
            if (Tool.MANAGE_PORT_MIRRORING.equals(tool)) {
                WindowManagePortMirroring wdwManagePortMirroring = managePortMirroringVisualAction.getVisualComponent(
                    new ModuleActionParameterSet(new ModuleActionParameter("businessObject", device))
                );
                wdwManagePortMirroring.open();
                close();
            }
        });
        setHeader(String.format(ts.getTranslatedString("module.ospman.tools.device.title"), device.getName()));
        setContent(lstTools);
        setFooter(new Button(ts.getTranslatedString("module.general.messages.cancel"), clickEvent -> close()));
        setDraggable(true);
    }
}
