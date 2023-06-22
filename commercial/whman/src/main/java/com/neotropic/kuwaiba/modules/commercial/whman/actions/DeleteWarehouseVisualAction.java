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
package com.neotropic.kuwaiba.modules.commercial.whman.actions;

import com.neotropic.kuwaiba.modules.commercial.whman.WarehousesManagerModule;
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
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete warehouse action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteWarehouseVisualAction extends AbstractVisualAction<Dialog> {
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
    private DeleteWarehouseAction deleteWarehouseAction;

    public DeleteWarehouseVisualAction() {
        super(WarehousesManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight selectedWarehouse;
        if (parameters.containsKey("warehouse")) {
            selectedWarehouse = (BusinessObjectLight) parameters.get("warehouse");
            commandClose = (Command) parameters.get("commandClose");
            
            ConfirmDialog wdwDeleteWarehouse = new ConfirmDialog(ts, ts.getTranslatedString("module.general.labels.confirmation"),
                    String.format(ts.getTranslatedString("module.whman.actions.warehouses.delete-warehouse.confirm"), selectedWarehouse.getName()),
                    ts.getTranslatedString("module.general.labels.delete"));
            wdwDeleteWarehouse.setDraggable(true);
            wdwDeleteWarehouse.setResizable(true);
            
            wdwDeleteWarehouse.getBtnConfirm().addClickListener((event) -> {
                    try{
                        ActionResponse actionResponse =  
                        deleteWarehouseAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("warehouse", selectedWarehouse)));
                        
                        if (actionResponse.containsKey("exception"))
                            throw new ModuleActionException(((Exception)actionResponse.get("exception")).getLocalizedMessage());
                       
                        //refresh related grid
                        getCommandClose().execute(); 
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.whman.actions.warehouses.delete-warehouse.success"), DeleteWarehouseAction.class));  
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), DeleteWarehouseAction.class));
                    }
                    wdwDeleteWarehouse.close();
            });
            return wdwDeleteWarehouse;
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "warehouse")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteWarehouseAction;
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