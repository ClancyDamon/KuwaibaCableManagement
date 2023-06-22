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
package com.neotropic.kuwaiba.modules.commercial.processman.actions;

import com.neotropic.kuwaiba.modules.commercial.processman.ProcessEditorModule;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.ElementUi;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete element action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteElementVisualAction  extends AbstractVisualElementAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteElementAction deleteElementAction;
    
    public DeleteElementVisualAction() {
        super(ProcessEditorModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("element")) {
            ElementUi elementUi = (ElementUi) parameters.get("element");
            
            ConfirmDialog wdwDelete = new ConfirmDialog(ts,
                    this.deleteElementAction.getDisplayName(),
                    String.format(ts.getTranslatedString("module.processeditor.editor-form-type-artifact-actions.delete-element-confirm"),
                            elementUi.getElementUiId()), ts.getTranslatedString("module.general.messages.delete"));
            wdwDelete.setDraggable(true);
            wdwDelete.setResizable(true);
            
            wdwDelete.getBtnConfirm().addClickListener(event -> {
                try {
                    deleteElementAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("element", elementUi)));
                 
                    ActionResponse actionResponse = new ActionResponse();
                    actionResponse.put(ActionResponse.ActionType.REMOVE, "");
                    
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                            ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            String.format(ts.getTranslatedString("module.processeditor.editor-form-type-artifact-actions.delete-element-success"), elementUi.getElementUiId()),
                            DeleteElementAction.class, actionResponse));

                    wdwDelete.close();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteElementAction.class));
                }
            });
            return wdwDelete;
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "element")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteElementAction;
    }
}