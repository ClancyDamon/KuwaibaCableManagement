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
package org.neotropic.kuwaiba.modules.optional.contactman.widgets;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
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
 * Creates a new contact for customer.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewContactForCustomerAction extends AbstractVisualAdvancedAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Window to create a new contact
     */
    private ConfirmDialog wdwNewContact;
    /**
     * business object parameter, used to retrieve the parent as a parameter.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Object to validate window to show.
     */
    private Boolean equal = false;

    public NewContactForCustomerAction() {
        super(ContactManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            List<ClassMetadataLight> subclasses = mem.getSubClassesLight(Constants.CLASS_GENERICCUSTOMER, false, false);
            BusinessObjectLight selectedObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
            subclasses.forEach(subClass -> {
                if (subClass.getName().equals(selectedObject.getClassName())) {
                    equal = true;
                }
            });

            if (equal.equals(true) && equal == true) {
                try {
                    // Window
                    wdwNewContact = new ConfirmDialog(ts
                            , String.format(ts.getTranslatedString("module.contactman.actions.new-contact-for-customer.name"), selectedObject.toString()) 
                            , ts.getTranslatedString("module.general.labels.create")
                    );
                    wdwNewContact.setDraggable(true);
                    wdwNewContact.setResizable(true);
                    // List contact types
                    List<ClassMetadataLight> contactTypes = mem.getSubClassesLight(Constants.CLASS_GENERICCONTACT, false, false);
                    // Contact Types Combo Box
                    ComboBox<ClassMetadataLight> cmbContactTypes = new ComboBox<>(ts.getTranslatedString("module.contactman.label.type"), contactTypes);
                    cmbContactTypes.setAllowCustomValue(false);
                    cmbContactTypes.setRequiredIndicatorVisible(true);
                    cmbContactTypes.setSizeFull();
                    // Name Text Field
                    TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
                    txtName.setRequiredIndicatorVisible(true);
                    txtName.setSizeFull();
                    // Action
                    wdwNewContact.getBtnConfirm().addClickListener( (event) -> {
                                createContact(selectedObject, cmbContactTypes.getValue(), txtName.getValue());
                                wdwNewContact.close();
                            });
                    wdwNewContact.getBtnConfirm().setEnabled(false);
                    // Validations
                    txtName.addValueChangeListener((event) -> {
                        wdwNewContact.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbContactTypes.isEmpty());
                    });
                    cmbContactTypes.addValueChangeListener((event) -> {
                        wdwNewContact.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbContactTypes.isEmpty());
                    });
                    
                    // Add Content to dialog
                    wdwNewContact.setContent(txtName, cmbContactTypes);
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            } else {
                wdwNewContact = new ConfirmDialog(ts
                        , ts.getTranslatedString("module.general.messages.information")
                        , ts.getTranslatedString("module.general.messages.close"));
                wdwNewContact.setDraggable(true);
                wdwNewContact.setResizable(true);
                
                // Action
                wdwNewContact.getBtnConfirm().addClickListener(event -> wdwNewContact.close());
                // Content
                wdwNewContact.setContent(new Label(String.format(ts.getTranslatedString("module.contactman.actions.new-contact-for-customer.error-class"),
                        selectedObject.getClassName(), Constants.CLASS_GENERICCUSTOMER)));
            }
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return wdwNewContact;
    }

    private void createContact(BusinessObjectLight selectedCustomer, ClassMetadataLight contactType, String contactName) {
        if (selectedCustomer != null && !contactName.isEmpty()) {
            try {
                HashMap<String, String> property = new HashMap<>();
                property.put(Constants.PROPERTY_NAME, contactName);
                String oid;
                oid = bem.createContact(contactType.getName()
                        , selectedCustomer.getClassName()
                        , selectedCustomer.getId()
                        , UI.getCurrent().getSession().getAttribute(Session.class).getUser().getUserName()
                );
                bem.updateObject(contactType.getName(), oid, property);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.contactman.actions.new-contact.success"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException
                    | MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }

    @Override
    public String getName() {
        return ts.getTranslatedString("module.contactman.actions.new-contact.name");
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }

    @Override
    public AbstractAction getModuleAction() {
        return null;
    }

    @Override
    public String appliesTo() {
        return "GenericCustomer";
    }
}