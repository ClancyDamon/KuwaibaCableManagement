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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.EditConnectionsVisualAction;
import org.neotropic.util.visual.dialog.ConfirmDialog;

/**
 * Dialog to the edge tool set
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowEdge extends ConfirmDialog {
    public WindowEdge(AbstractViewEdge<BusinessObjectLight> edge, TranslationService ts, Command cmdDeleteEdge, 
        EditConnectionsVisualAction editConnectionAction) {
        ListBox<Tool> lstTools = new ListBox();
        lstTools.setItems(Tool.EDIT_CONNECTION, Tool.DELETE);
        lstTools.setRenderer(new ComponentRenderer<>(tool -> {
            if (Tool.DELETE.equals(tool)) {
                return new HorizontalLayout(VaadinIcon.TRASH.create(), 
                    new Label(ts.getTranslatedString("module.ospman.view-edge.tool.remove"))
                );
            } else if (Tool.EDIT_CONNECTION.equals(tool)) {
                return new HorizontalLayout(VaadinIcon.EDIT.create(), 
                    new Label(editConnectionAction.getName())
                );
            }
            return new HorizontalLayout();
        }));
        lstTools.addValueChangeListener(event -> {
            if (Tool.DELETE.equals(event.getValue())) {
                if (cmdDeleteEdge != null)
                    cmdDeleteEdge.execute();
            } else if (Tool.EDIT_CONNECTION.equals(event.getValue())) {
                editConnectionAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter<>("businessObject", edge.getIdentifier()))).open();
            }
            close();
        });
        setHeader(String.format(ts.getTranslatedString("module.ospman.container.tools"), edge.getIdentifier().getName()));
        setContent(lstTools);
        setFooter(new Button(ts.getTranslatedString("module.general.messages.cancel"), event -> close()));
        setDraggable(true);
    }
    
    private enum Tool {
        DELETE,
        EDIT_CONNECTION
    }
}
