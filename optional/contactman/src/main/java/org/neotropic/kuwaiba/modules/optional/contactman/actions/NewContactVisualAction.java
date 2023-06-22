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
package org.neotropic.kuwaiba.modules.optional.contactman.actions;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.contactman.ContactManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new contact action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewContactVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewContactAction newContactAction;
    /**
     * Limit of objects. -1 get all.
     */
    private final int LIMIT = -1;
    /**
     * Window to create a new contact
     */
    private ConfirmDialog wdwNewContact;

    public NewContactVisualAction() {
        super(ContactManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            // Window
            wdwNewContact = new ConfirmDialog();
            // Lists
            List<ClassMetadataLight> contactTypes = mem.getSubClassesLight(Constants.CLASS_GENERICCONTACT, false, false);
            List<BusinessObjectLight> customers = bem.getObjectsOfClassLight(Constants.CLASS_GENERICCUSTOMER, null, LIMIT, LIMIT);
            // Contact Types Combo Box 
            ComboBox<ClassMetadataLight> cmbContactTypes = new ComboBox<>(ts.getTranslatedString("module.contactman.label.type"),  contactTypes);
            cmbContactTypes.setAllowCustomValue(false);
            cmbContactTypes.setRequiredIndicatorVisible(true);
            cmbContactTypes.setSizeFull();
            // Customers Combo Box 
            ComboBox<BusinessObjectLight> cmbCustomers = new ComboBox<>(ts.getTranslatedString("module.contactman.label.costumer"), customers);
            cmbCustomers.setAllowCustomValue(false);
            cmbCustomers.setRequiredIndicatorVisible(true);
            cmbCustomers.setSizeFull();
            // Name Text Field
            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();
            
            wdwNewContact = new ConfirmDialog(ts
                    , this.newContactAction.getDisplayName()
                    , ts.getTranslatedString("module.general.labels.create")
            );
            wdwNewContact.setDraggable(true);
            wdwNewContact.setResizable(true);
            
            wdwNewContact.getBtnConfirm().addClickListener((event) -> {
                try {
                    newContactAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                            new ModuleActionParameter<>("contact", cmbContactTypes.getValue()),
                            new ModuleActionParameter<>("customer", cmbCustomers.getValue())
                    ));
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.contactman.actions.new-contact.success"), NewContactAction.class));
                    wdwNewContact.close();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                            ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, ex.getMessage(), NewContactAction.class));
                }
            });

            // Validations
            txtName.addValueChangeListener((event) -> {
               wdwNewContact.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbContactTypes.isEmpty() && !cmbCustomers.isEmpty());
            });
            cmbContactTypes.addValueChangeListener((event) -> {
                wdwNewContact.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbContactTypes.isEmpty() && !cmbCustomers.isEmpty());
            });
            cmbCustomers.addValueChangeListener((event) -> {
                wdwNewContact.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbContactTypes.isEmpty() && !cmbCustomers.isEmpty());
            });

            // Add content to window
            wdwNewContact.setContent(txtName, cmbContactTypes, cmbCustomers);
        } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return wdwNewContact;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newContactAction;
    }
}