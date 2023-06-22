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
package org.neotropic.kuwaiba.core.configuration.filters.actions;

import com.vaadin.flow.component.dialog.Dialog;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.configuration.filters.FilterDefinitionModule;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete filter definition action
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class DeleteFilterDefinitionVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteFilterDefinitionAction deleteFilterDefinitionAction;

    public DeleteFilterDefinitionVisualAction() {
        super(FilterDefinitionModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        
        ConfirmDialog dlgDeleteFilterDefinitionConfirmation = new ConfirmDialog(ts, ts.getTranslatedString("module.configman.filters.actions.delete-filter"),
                ts.getTranslatedString("module.configman.filters.actions.delete-confirm-message"),
                ts.getTranslatedString("module.general.labels.delete"));


        dlgDeleteFilterDefinitionConfirmation.getBtnConfirm().addClickListener((event) -> {
            try {
                deleteFilterDefinitionAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_ID, parameters.get(Constants.PROPERTY_ID)),
                        new ModuleActionParameter<>(Constants.PROPERTY_CLASSNAME, parameters.get(Constants.PROPERTY_CLASSNAME))));

                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(ActionResponse.ActionType.REMOVE, ActionResponse.ActionType.REMOVE);
                actionResponse.put(Constants.PROPERTY_CLASSNAME, (String)parameters.get(Constants.PROPERTY_CLASSNAME));
                
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                        ts.getTranslatedString("module.configman.filters.notification-deleted"), DeleteFilterDefinitionVisualAction.class, actionResponse));
                dlgDeleteFilterDefinitionConfirmation.close();

            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), DeleteFilterDefinitionVisualAction.class));
                dlgDeleteFilterDefinitionConfirmation.close();
            }
        });
        return dlgDeleteFilterDefinitionConfirmation;
        
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteFilterDefinitionAction;
    }
}