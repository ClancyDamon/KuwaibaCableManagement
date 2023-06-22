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
package org.neotropic.kuwaiba.core.configuration.proxies.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.configuration.proxies.ProxyManagerModule;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new proxies pool action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewProxiesPoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewProxiesPoolAction newProxiesPoolAction;    
    /**
     * Close action command from main ui
     */
    private Command commandClose;
    /**
     * Close action command from dialog
     */
    private Command commandAdd;

    public NewProxiesPoolVisualAction() {
        super(ProxyManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        commandClose = (Command) parameters.get("commandClose");
        commandAdd = (Command) parameters.get("commandAdd");

        TextField txtName = new TextField(ts.getTranslatedString("module.configman.proxies.label.name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();

        TextField txtDescription = new TextField(ts.getTranslatedString("module.configman.proxies.label.description"));
        txtDescription.setSizeFull();

        // Dialog to create a new proxies pool
        ConfirmDialog wdwNewPool = new ConfirmDialog(ts
                , this.newProxiesPoolAction.getDisplayName()
                , ts.getTranslatedString("module.general.labels.create")
        );
        wdwNewPool.setCloseOnOutsideClick(false);
        wdwNewPool.setCloseOnEsc(false);

        wdwNewPool.getBtnConfirm().addClickListener( (event) -> {
            try {
                newProxiesPoolAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>("name", txtName.getValue()),
                        new ModuleActionParameter<>("desc", txtDescription.getValue())
                ));
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                        ts.getTranslatedString("module.configman.proxies.actions.new-pool.succes"), NewProxiesPoolAction.class));
                //refresh related grid
                commandAdd.execute();
                getCommandClose().execute();
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewProxiesPoolAction.class));
            }
            wdwNewPool.close();
        });


        txtName.addValueChangeListener((event) -> {
            wdwNewPool.getBtnConfirm().setEnabled(!txtName.isEmpty());
        });

        wdwNewPool.setContent(txtName, txtDescription);
        return wdwNewPool;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newProxiesPoolAction;
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