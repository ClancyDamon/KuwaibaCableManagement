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

import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
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
 * Frontend to the delete service pool action.
 * @author Mauricio Ruiz Beltran {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteServicePoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteServicePoolAction deleteServicePoolAction;
    /**
     * Parameter for service pool
     */
    private static final String PARAMETER_SERVICE_POOL = "servicePool";
    
    public DeleteServicePoolVisualAction() {
        super(ServiceManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        InventoryObjectPool servicePool;
        if (parameters.containsKey(PARAMETER_SERVICE_POOL)) {
            servicePool = (InventoryObjectPool) parameters.get(PARAMETER_SERVICE_POOL);
            ConfirmDialog wdwDeleteCustomer = new ConfirmDialog(ts,
                    this.deleteServicePoolAction.getDisplayName(),
                    String.format(ts.getTranslatedString("module.serviceman.actions.delete-service-pool.ui.confirmation-delete-service-pool"), servicePool),
                    ts.getTranslatedString("module.general.labels.delete"));
            wdwDeleteCustomer.setDraggable(true);
            wdwDeleteCustomer.setResizable(true);
            wdwDeleteCustomer.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);

            wdwDeleteCustomer.getBtnConfirm().addClickListener(event -> {
                try {
                    ActionResponse actionResponse = deleteServicePoolAction.getCallback()
                            .execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(PARAMETER_SERVICE_POOL, servicePool)));

                    if (actionResponse.containsKey("exception"))
                        throw new ModuleActionException(((Exception) actionResponse.get("exception")).getLocalizedMessage());
                    else
                        actionResponse.put(ActionResponse.ActionType.REMOVE, "");

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.serviceman.actions.delete-service-pool.ui.service-pool-deleted-success"),
                            DeleteServicePoolAction.class, actionResponse));
                    wdwDeleteCustomer.close();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteServicePoolAction.class));
                }
            });
            return wdwDeleteCustomer;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.serviceman.actions.delete-service-pool.ui.service-pool-error")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteServicePoolAction;
    }
}