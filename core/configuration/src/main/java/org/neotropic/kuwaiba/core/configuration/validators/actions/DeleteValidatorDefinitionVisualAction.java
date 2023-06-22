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
package org.neotropic.kuwaiba.core.configuration.validators.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;
import org.neotropic.kuwaiba.core.configuration.validators.ValidatorDefinitionModule;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete validator definition action
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteValidatorDefinitionVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Close action command
     */
    private Command commandClose; 
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteValidatorDefinitionAction deleteValidatorDefinitionAction;

    public DeleteValidatorDefinitionVisualAction() {
        super(ValidatorDefinitionModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        ValidatorDefinition selectedValidatorDefinition;

        if (parameters.containsKey("validatorDefinition")) {
            selectedValidatorDefinition = (ValidatorDefinition) parameters.get("validatorDefinition");
            commandClose = (Command) parameters.get("commandClose");

            ConfirmDialog wdwDeleteValidatorDefinition = new ConfirmDialog(ts, ts.getTranslatedString("module.configman.validators.actions.delete-validator.name"),
                    String.format(ts.getTranslatedString("module.configman.validators.actions.delete-validator.confirm-delete-message"), selectedValidatorDefinition.getName()),
                    ts.getTranslatedString("module.general.labels.delete"));

            wdwDeleteValidatorDefinition.getBtnConfirm().addClickListener((event) -> {
                try {
                    deleteValidatorDefinitionAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("validatorId", selectedValidatorDefinition.getId())));

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.configman.validators.actions.delete-validator.ui.deleted-success"), DeleteValidatorDefinitionAction.class));
                    wdwDeleteValidatorDefinition.close();
                    //refresh related grid
                    getCommandClose().execute();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteValidatorDefinitionAction.class));
                    wdwDeleteValidatorDefinition.close();
                }
            });
            return wdwDeleteValidatorDefinition;
        } else 
            return new Dialog(new Label(ts.getTranslatedString("module.configman.validators.error-param-validator-definition")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteValidatorDefinitionAction;
    }
    
    /**
     * refresh grid
     * @return commandClose;Command; refresh action 
     */
    public Command getCommandClose() {
        return commandClose;
    }

    /**
     * @param commandClose;Command; refresh action 
     */
    public void setCommandClose(Command commandClose) {
        this.commandClose = commandClose;
    }   
}