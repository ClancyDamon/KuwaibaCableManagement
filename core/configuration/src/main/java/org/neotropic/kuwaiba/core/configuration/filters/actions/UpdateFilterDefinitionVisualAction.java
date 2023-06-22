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

import org.neotropic.kuwaiba.core.configuration.validators.actions.*;
import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
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
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new filter definition action.
 *
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class UpdateFilterDefinitionVisualAction extends AbstractVisualAction<Dialog> {

    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private UpdateFilterDefinitionAction updateFilterDefinitionAction;

    public UpdateFilterDefinitionVisualAction() {
        super(FilterDefinitionModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        TextField txtName = new TextField(ts.getTranslatedString("module.configman.filters.actions.new-filter.label.name"));
        txtName.setValue((String) parameters.get(Constants.PROPERTY_NAME));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();

        TextField txtDescription = new TextField(ts.getTranslatedString("module.configman.filters.actions.new-filter.label.description"));
        txtDescription.setValue((String) parameters.get(Constants.PROPERTY_DESCRIPTION));
        txtDescription.setSizeFull();

        // Windows to create a new filter Definition
        ConfirmDialog cfdNewFilter = new ConfirmDialog(ts, ts.getTranslatedString("module.configman.filters.actions.edit-filter"),
                 ts.getTranslatedString("module.general.messages.save"));
        cfdNewFilter.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);

        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();

        cfdNewFilter.getBtnConfirm().setEnabled(false);
        cfdNewFilter.getBtnConfirm().addClickListener((e) -> {
            try {
                lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));

                updateFilterDefinitionAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_ID, parameters.get(Constants.PROPERTY_ID)),
                        new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                        new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue())
                ));

                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(ActionResponse.ActionType.UPDATE, ActionResponse.ActionType.UPDATE);
                actionResponse.put(Constants.PROPERTY_NAME, txtName.getValue());
                actionResponse.put(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue());

                fireActionCompletedEvent(
                        new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.configman.filters.notification-saved"),
                                NewValidatorDefinitionAction.class, actionResponse));

                cfdNewFilter.close();
            } catch (ModuleActionException ex) {
                throw new ScriptCompilationException(ex.getLocalizedMessage());
            }
        });

        //validations
        txtName.addValueChangeListener(e
                -> cfdNewFilter.getBtnConfirm().setEnabled(!txtName.isEmpty()));
        //create content

        cfdNewFilter.setContent(txtName, txtDescription);
        return cfdNewFilter;
    }

    @Override
    public AbstractAction getModuleAction() {
        return updateFilterDefinitionAction;
    }
}
