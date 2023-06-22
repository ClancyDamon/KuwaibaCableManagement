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
package org.neotropic.kuwaiba.modules.optional.physcon.actions;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;

/**
 * Confirm dialog to open the edit connection visual action.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ConfirmDialogEditConnections extends ConfirmDialog {
    /**
     * Reference to the translation service.
     */
    private final TranslationService ts;
    /**
     * Reference to the edit connections visual action.
     */
    private final EditConnectionsVisualAction editConnectionsVisualAction;
    private final BusinessObjectLight businessObject;
    
    public ConfirmDialogEditConnections(BusinessObjectLight businessObject, 
        EditConnectionsVisualAction editConnectionsVisualAction, TranslationService ts) {
        Objects.requireNonNull(businessObject);
        Objects.requireNonNull(editConnectionsVisualAction);
        Objects.requireNonNull(ts);
        this.businessObject = businessObject;
        this.editConnectionsVisualAction = editConnectionsVisualAction;
        this.ts = ts;
    }

    @Override
    public void open() {
        Button btnNo = new Button(ts.getTranslatedString("module.general.messages.no"), 
            clickEvent -> close()
        );
        Button btnYes = new Button(ts.getTranslatedString("module.general.messages.yes"), 
            clickEvent -> {
                close();
                editConnectionsVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter<>("businessObject", businessObject)
                )).open();
            }
        );
        HorizontalLayout lytFooter = new HorizontalLayout(btnNo, btnYes);
        lytFooter.setSizeFull();
        lytFooter.setFlexGrow(1, btnNo, btnYes);
        
        setHeader(ts.getTranslatedString("module.physcon.confirm-dialog.edit-connections.header"));
        setContent(new Label(ts.getTranslatedString("module.physcon.confirm-dialog.edit-connections.content")));
        setFooter(lytFooter);
        super.open();
    }
    
    
}
