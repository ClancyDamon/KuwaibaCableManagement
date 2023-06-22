/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.serviceman.ServiceManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Frontend to the delete service action.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class DeleteServiceVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteServiceAction deleteServiceAction;
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
    /**
     * Parameter for service
     */
    private static final String PARAMETER_SERVICE = "service";

    public DeleteServiceVisualAction() {
        super(ServiceManagerModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight service = (BusinessObjectLight)parameters.get(PARAMETER_SERVICE);
        
        ConfirmDialog wdwDeleteCustomer = new ConfirmDialog(ts,
                this.deleteServiceAction.getDisplayName(),
                String.format(ts.getTranslatedString("module.serviceman.actions.delete-service.ui.confirmation-delete-service"), service), 
                ts.getTranslatedString("module.general.labels.delete"));
        wdwDeleteCustomer.setDraggable(true);
        wdwDeleteCustomer.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        
        wdwDeleteCustomer.getBtnConfirm().addClickListener(evt -> {
            try {
                deleteServiceAction.getCallback().execute(parameters);
                
                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(ActionResponse.ActionType.REMOVE, "");
                
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                        ts.getTranslatedString("module.serviceman.actions.delete-service.ui.service-deleted-success"),
                        DeleteServiceAction.class, actionResponse));
                wdwDeleteCustomer.close();
            } catch (ModuleActionException ex) {
                 fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, 
                                     ex.getMessage(), DeleteServiceVisualAction.class));
            } 
        });

         return wdwDeleteCustomer;
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteServiceAction;
    }
    
    @Override
    public String appliesTo() {
        return Constants.CLASS_GENERICSERVICE;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
}