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

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.serviceman.ServiceManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of a new customer pool action.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class NewCustomerPoolVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Close action command
     */
    private Command addCustomerPool;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewCustomerPoolAction newCustomerPoolAction;

    public NewCustomerPoolVisualAction() {
        super(ServiceManagerModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        addCustomerPool = (Command) parameters.get("addCustomerPool");
        
        ConfirmDialog wdwNewCustomerPool = new ConfirmDialog(ts
                , this.newCustomerPoolAction.getDisplayName()
                , ts.getTranslatedString("module.general.labels.create")
        );
        wdwNewCustomerPool.setDraggable(true);
        
        TextField txtName = new TextField(ts.getTranslatedString("module.serviceman.actions.new-customer-pool.ui.pool-name"));
        txtName.setRequired(true);
        txtName.setRequiredIndicatorVisible(true);
        txtName.setWidthFull();

        TextField txtDescription = new TextField(ts.getTranslatedString("module.serviceman.actions.new-customer-pool.ui.pool-description"));
        txtDescription.setWidthFull();

        Label lblMessages = new Label();
        lblMessages.setClassName("embedded-notification-error");
        lblMessages.setVisible(false);
        lblMessages.setWidthFull();

        wdwNewCustomerPool.getBtnConfirm().addClickListener( (e) -> {
            try {
                if (txtName.isEmpty()) {
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                    lblMessages.setVisible(true);
                } else {
                    newCustomerPoolAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()), 
                            new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue())));

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS, 
                            ts.getTranslatedString("module.serviceman.actions.new-customer-pool.ui.customer-pool-created-success"), NewCustomerPoolAction.class));
                    wdwNewCustomerPool.close();
                    // Refresh related grid
                    addCustomerPool.execute();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, 
                            ex.getMessage(), NewCustomerPoolAction.class));
            }
        });
        wdwNewCustomerPool.getBtnConfirm().setEnabled(false);
        
        txtName.addValueChangeListener((e) -> {
            wdwNewCustomerPool.getBtnConfirm().setEnabled(!txtName.isEmpty());
        });
        
        FormLayout lytTextFields = new FormLayout(txtName, txtDescription);
        lytTextFields.setWidthFull();
        wdwNewCustomerPool.setContent(lblMessages, lytTextFields);

        return wdwNewCustomerPool;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newCustomerPoolAction;
    }

    @Override
    public String appliesTo() {
        return null;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 0;
    }
}