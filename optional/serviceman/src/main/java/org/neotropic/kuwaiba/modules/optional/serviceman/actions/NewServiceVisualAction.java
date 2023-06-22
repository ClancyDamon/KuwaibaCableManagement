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

import java.util.HashMap;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.serviceman.ServiceManagerModule;
import org.neotropic.kuwaiba.modules.optional.serviceman.ServiceManagerUI;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of a new customer action that provides means to choose the service pool and type.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class NewServiceVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewServiceAction newServiceAction;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    private MetadataEntityManager mem;

    public NewServiceVisualAction() {
        super(ServiceManagerModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        // This action might be called with or without parameters depending on who launches it. 
        // For example, if launched from the dashboard, it won't received any initial parameter and all the 
        // necessary information will have to be requested (the parent customer pool and the customer type), 
        // but if launched from a customer pool, only the customer type will be requested.

        if (parameters.containsKey(ServiceManagerUI.PARAMETER_SERVICE_POOL)) {
            try {
                InventoryObjectPool pool = (InventoryObjectPool) parameters.get(ServiceManagerUI.PARAMETER_SERVICE_POOL);
                ConfirmDialog wdwNewCustomer = new ConfirmDialog(ts,
                         this.newServiceAction.getDisplayName(),
                         ts.getTranslatedString("module.general.labels.create")
                );
                wdwNewCustomer.setDraggable(true);
                wdwNewCustomer.setResizable(true);

                ComboBox<ClassMetadataLight> cmbServiceTypes = new ComboBox<>(ts.getTranslatedString("module.serviceman.actions.new-service.ui.service-type"),
                                mem.getSubClassesLight(Constants.CLASS_GENERICSERVICE, false, false));
                cmbServiceTypes.setRequiredIndicatorVisible(true);
                cmbServiceTypes.setSizeFull();

                TextField txtName = new TextField(ts.getTranslatedString("module.serviceman.actions.new-service.ui.service-name"));
                txtName.setRequiredIndicatorVisible(true);
                txtName.setSizeFull();

                wdwNewCustomer.getBtnConfirm().addClickListener((e) -> {
                    try {
                            HashMap<String, String> attributes = new HashMap<>();
                            attributes.put(Constants.PROPERTY_NAME, txtName.getValue());
                            
                            ActionResponse actionResponse = newServiceAction.getCallback().execute(new ModuleActionParameterSet(
                                    new ModuleActionParameter<>("poolId", pool.getId()),
                                    new ModuleActionParameter<>("serviceClass", cmbServiceTypes.getValue().getName()),
                                    new ModuleActionParameter<>("attributes", attributes)));

                            actionResponse.put(ActionResponse.ActionType.ADD, "");

                            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                    ts.getTranslatedString("module.serviceman.actions.new-service.ui.service-created-success"),
                                    NewServiceAction.class, actionResponse));
                            wdwNewCustomer.close();
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), NewServiceAction.class));
                    }
                });
                wdwNewCustomer.getBtnConfirm().setEnabled(false);

                cmbServiceTypes.addValueChangeListener(e -> {
                    wdwNewCustomer.getBtnConfirm().setEnabled(cmbServiceTypes.getValue() != null);
                });
                
                txtName.addValueChangeListener(e -> {
                    wdwNewCustomer.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty());
                });

                wdwNewCustomer.setContent(cmbServiceTypes, txtName);
                return wdwNewCustomer;
            } catch (InventoryException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewServiceAction.class));
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                        ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                return null;
            }
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"),
                    ServiceManagerUI.PARAMETER_SERVICE_POOL)));
    }

    @Override
    public AbstractAction getModuleAction() {
        return newServiceAction;
    }

    @Override
    public String appliesTo() {
        return Constants.CLASS_GENERICCUSTOMER;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
}