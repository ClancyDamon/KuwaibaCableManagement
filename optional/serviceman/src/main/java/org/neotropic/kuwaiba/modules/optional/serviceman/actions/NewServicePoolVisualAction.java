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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.serviceman.ServiceManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of a new service pool action.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class NewServicePoolVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewServicePoolAction newServicePoolAction;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    protected BusinessEntityManager bem;

    public NewServicePoolVisualAction() {
        super(ServiceManagerModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            ConfirmDialog wdwNewCustomerPool = new ConfirmDialog(ts
                    , this.newServicePoolAction.getDisplayName()
                    , ts.getTranslatedString("module.general.labels.create")
            );
            wdwNewCustomerPool.setDraggable(true);
            
            ComboBox<BusinessObjectLight> cmbCustomers = new ComboBox<>(ts.getTranslatedString("module.serviceman.actions.new-service-pool.ui.customer"));
            cmbCustomers.setWidthFull();
            List<BusinessObjectLight> items = bem.getObjectsOfClassLight(Constants.CLASS_GENERICCUSTOMER, null, -1, -1);
            if (parameters.containsKey("service") && !items.isEmpty()){ // The action is launched from a customer pool
                BusinessObjectLight selectemItem = (BusinessObjectLight)parameters.get("service");
                cmbCustomers.setItems(items);
                cmbCustomers.setValue(selectemItem);
                cmbCustomers.setReadOnly(true);                
            }
            
            TextField txtName = new TextField(ts.getTranslatedString("module.serviceman.actions.new-service-pool.ui.pool-name"));
            txtName.setRequired(true);
            txtName.setRequiredIndicatorVisible(true);
            txtName.setWidthFull();

            TextField txtDescription = new TextField(ts.getTranslatedString("module.serviceman.actions.new-service-pool.ui.pool-description"));
            txtDescription.setWidthFull();

            Label lblMessages = new Label();
            lblMessages.setClassName("embedded-notification-error");
            lblMessages.setWidthFull();
            lblMessages.setVisible(false);

            wdwNewCustomerPool.getBtnConfirm().addClickListener( e -> {
                try {
                    if (txtName.isEmpty() || cmbCustomers.getValue() == null) {
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                        lblMessages.setVisible(true);
                    } else {
                        newServicePoolAction.getCallback().execute(new ModuleActionParameterSet(new ModuleActionParameter<>(Constants.PROPERTY_PARENT, cmbCustomers.getValue()), 
                                new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()), 
                                new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue())));
                        
                        ActionResponse actionResponse = new ActionResponse();
                        actionResponse.put(ActionResponse.ActionType.ADD, "");
                        
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS, 
                                ts.getTranslatedString("module.serviceman.actions.new-service-pool.ui.pool-created-success"),
                                NewServicePoolAction.class, actionResponse));
                        wdwNewCustomerPool.close();
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, 
                                ex.getMessage(), NewServicePoolAction.class));
                }
            });
            wdwNewCustomerPool.getBtnConfirm().setEnabled(false);
            
            txtName.addValueChangeListener(e -> {
                wdwNewCustomerPool.getBtnConfirm().setEnabled(!txtName.isEmpty());
            });

            FormLayout lytTextFields = new FormLayout(cmbCustomers, txtName, txtDescription);
            lytTextFields.setWidthFull();

            wdwNewCustomerPool.setContent(lblMessages, lytTextFields);

            return wdwNewCustomerPool;
            
        } catch(InventoryException ex) {
            return new Dialog(new Label(ex.getLocalizedMessage()));
        }
    }

    @Override
    public AbstractAction getModuleAction() {
        return newServicePoolAction;
    }

    @Override
    public String appliesTo() {
        return Constants.CLASS_GENERICCUSTOMER;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 0;
    }
}