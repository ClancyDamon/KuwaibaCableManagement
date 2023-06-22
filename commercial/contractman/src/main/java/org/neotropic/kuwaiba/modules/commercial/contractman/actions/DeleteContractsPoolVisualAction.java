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
package org.neotropic.kuwaiba.modules.commercial.contractman.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.contractman.ContractManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete contracts pool action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteContractsPoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Close action command
     */
    private Command commandClose;
    /**
     * Action command after finish delete pool over pool contract dialog
     */
    private Command commandDelete;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteContractsPoolAction deleteContractsPoolAction; 

    public DeleteContractsPoolVisualAction() {
        super(ContractManagerModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        InventoryObjectPool selectedPool;
        if (parameters.containsKey("pool")) {
            selectedPool = (InventoryObjectPool) parameters.get("pool");
            commandClose = (Command) parameters.get("commandClose");
            commandDelete = (Command) parameters.get("commandDelete");
            
            ConfirmDialog wdwDeletePool = new ConfirmDialog(ts, ts.getTranslatedString("module.general.label.action-delete"),
                    String.format(ts.getTranslatedString("module.contractman.actions.pool.delete-pool-confirm"), selectedPool.getName()),
                    ts.getTranslatedString("module.general.labels.delete"));
            wdwDeletePool.setDraggable(true);
            
            wdwDeletePool.getBtnConfirm().addClickListener((event) -> {
                    try{
                        ActionResponse actionResponse =  
                        deleteContractsPoolAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("pool", selectedPool)));
                        
                        if (actionResponse.containsKey("exception"))
                            throw new ModuleActionException(((Exception)actionResponse.get("exception")).getLocalizedMessage());
                        //refresh related grid
                        getCommandClose().execute(); 
                        commandDelete.execute();
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.contractman.actions.pool.delete-pool-success"), DeleteContractsPoolAction.class)); 
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), DeleteContractsPoolAction.class));
                    }
                    wdwDeletePool.close();
            });
            return wdwDeletePool;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.contractman.actions.pool.delete-pool-error")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteContractsPoolAction;
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