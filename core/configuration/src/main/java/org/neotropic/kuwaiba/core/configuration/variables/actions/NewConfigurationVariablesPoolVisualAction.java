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
package org.neotropic.kuwaiba.core.configuration.variables.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.configuration.variables.ConfigurationVariablesModule;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new configuration variable pool action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewConfigurationVariablesPoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Action command after finish add pool over configuration variable layout
     */
    private Command commandClose;
    /**
     * Action command after finish add pool over poll configuration dialog
     */
    private Command commandAdd;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewConfigurationVariablesPoolAction newConfigurationVariablesPoolAction;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    protected BusinessEntityManager bem;

    public NewConfigurationVariablesPoolVisualAction() {
        super(ConfigurationVariablesModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        commandClose = (Command) parameters.get("commandClose");
        commandAdd = (Command) parameters.get("commandAdd");

        TextField txtName = new TextField(ts.getTranslatedString("module.configman.configvar.label.name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();

        TextField txtDescription = new TextField(ts.getTranslatedString("module.configman.configvar.label.description"));
        txtDescription.setSizeFull();

        ConfirmDialog wdwNewConfigurationVariablePool = new ConfirmDialog(ts
                , this.newConfigurationVariablesPoolAction.getDisplayName()
                , ts.getTranslatedString("module.general.labels.create"));
        wdwNewConfigurationVariablePool.setDraggable(true);
        wdwNewConfigurationVariablePool.setResizable(true);

        wdwNewConfigurationVariablePool.getBtnConfirm().addClickListener( (e) -> {
            try {
                newConfigurationVariablesPoolAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                        new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue())
                ));

                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                        ts.getTranslatedString("module.configman.actions.new-configuration-variable-pool.ui.pool-created-success"), NewConfigurationVariablesPoolAction.class));
                wdwNewConfigurationVariablePool.close();
                //refresh related grid
                getCommandClose().execute();
                commandAdd.execute();
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewConfigurationVariablesPoolAction.class));
            }
        });

        wdwNewConfigurationVariablePool.getBtnConfirm().setEnabled(false);
        txtName.addValueChangeListener((e) -> {
            wdwNewConfigurationVariablePool.getBtnConfirm().setEnabled(!txtName.isEmpty());
        });

        
        wdwNewConfigurationVariablePool.setContent(txtName, txtDescription);

        return wdwNewConfigurationVariablePool;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newConfigurationVariablesPoolAction;
    }
    
    /**
     * refresh grid
     * @return commandClose; refresh action 
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