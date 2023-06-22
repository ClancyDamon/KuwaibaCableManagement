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
package org.neotropic.kuwaiba.modules.optional.serviceman.actions;

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
import org.neotropic.kuwaiba.modules.optional.serviceman.ServiceManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete costumer pool action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteCustomerPoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Close action command
     */
    private Command deleteCustomerPool;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteCustomerPoolAction deleteCustomerPoolAction;
    /**
     * Parameter for customer pool
     */
    private static final String PARAMETER_CUSTOMER_POOL = "customerPool";

    public DeleteCustomerPoolVisualAction() {
        super(ServiceManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        InventoryObjectPool selectedPool;
        if (parameters.containsKey(PARAMETER_CUSTOMER_POOL)) {
            selectedPool = (InventoryObjectPool) parameters.get(PARAMETER_CUSTOMER_POOL);
            deleteCustomerPool = (Command) parameters.get("deleteCustomerPool");
            
            ConfirmDialog wdwDeletePool = new ConfirmDialog(ts, 
                    this.deleteCustomerPoolAction.getDisplayName(),
                    String.format(
                            ts.getTranslatedString("module.serviceman.actions.delete-customer-pool.ui.confirmation-delete-customer-pool"),
                            selectedPool.getName()),
                    ts.getTranslatedString("module.general.labels.delete"));
            wdwDeletePool.setDraggable(true);
            
            wdwDeletePool.getBtnConfirm().addClickListener((event) -> {
                   try {
                       ActionResponse actionResponse = deleteCustomerPoolAction.getCallback()
                               .execute(new ModuleActionParameterSet(new ModuleActionParameter<>(PARAMETER_CUSTOMER_POOL, selectedPool)));

                       if (actionResponse.containsKey("exception"))
                           throw new ModuleActionException(((Exception) actionResponse.get("exception")).getLocalizedMessage());
                       
                       //refresh related grid
                       deleteCustomerPool.execute();
                       fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                               ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                               ts.getTranslatedString("module.serviceman.actions.delete-customer-pool.ui.customer-pool-deleted-success"),
                               DeleteCustomerPoolAction.class));
                } catch (ModuleActionException ex) {
                       fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                               ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                               ex.getMessage(), DeleteCustomerPoolAction.class));
                }
                wdwDeletePool.close();
            });
            return wdwDeletePool;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.serviceman.actions.delete-customer-pool.ui.customer-pool-error")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteCustomerPoolAction;
    }
}