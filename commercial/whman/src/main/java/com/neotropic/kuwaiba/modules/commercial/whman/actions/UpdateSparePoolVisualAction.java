/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of update a spare pool action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class UpdateSparePoolVisualAction extends AbstractVisualAction<Dialog> {
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
    private UpdateSparePoolAction updateSparePoolAction;
    

    public UpdateSparePoolVisualAction() {
        super(WarehousesManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
                InventoryObjectPool selectedPool;
        if (parameters.containsKey("sparePool")) {
            selectedPool = (InventoryObjectPool) parameters.get("sparePool");
            commandClose = (Command) parameters.get("commandClose");
            
            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setValue(selectedPool.getName());
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();
            
            TextField txtDescription = new TextField(ts.getTranslatedString("module.general.labels.description"));
            txtDescription.setValue(selectedPool.getDescription());
            txtDescription.setSizeFull();
            
            // Dialog
            ConfirmDialog wdwUpdatePool = new ConfirmDialog(ts
            , this.updateSparePoolAction.getDisplayName()
            , ts.getTranslatedString("module.general.messages.save"));
            wdwUpdatePool.setDraggable(true);
            wdwUpdatePool.setResizable(true);
            
            wdwUpdatePool.getBtnConfirm().addClickListener((event) -> {
                    try{
                        ActionResponse actionResponse
                                = updateSparePoolAction.getCallback().execute(new ModuleActionParameterSet(
                                        new ModuleActionParameter<>("sparePool", selectedPool),
                                        new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                                        new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue())));
                        
                        if (actionResponse.containsKey("exception"))
                            throw new ModuleActionException(((Exception)actionResponse.get("exception")).getLocalizedMessage());
                        //refresh related grid
                        commandClose.execute(); 
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.whman.actions.spare.update-spare.success"), UpdateSparePoolAction.class)); 
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), UpdateSparePoolAction.class));
                    }
                    wdwUpdatePool.close();
            });
            wdwUpdatePool.getBtnConfirm().setEnabled(true);
            txtName.addValueChangeListener(event -> {
              wdwUpdatePool.getBtnConfirm().setEnabled(!txtName.isEmpty());
            });
            
            wdwUpdatePool.setContent(txtName, txtDescription);
            return wdwUpdatePool;
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "sparePool")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return updateSparePoolAction;
    }
}