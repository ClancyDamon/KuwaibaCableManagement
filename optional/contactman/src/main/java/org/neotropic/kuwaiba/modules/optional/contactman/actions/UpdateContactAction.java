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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.Contact;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.contactman.ContactManagerModule;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Updates a contact
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class UpdateContactAction extends AbstractVisualAction<Dialog> implements  PropertySheet.IPropertyValueChangedListener {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to Application Entity Manager
     */
    @Autowired 
    private ApplicationEntityManager aem;
    /**
     * Reference to Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Close action command
     */
    private Command commandClose; 
    /**
     * Object to save the selected contact
     */
    private  Contact selectedContact;
    /**
     * Property sheet
     */
    private PropertySheet propertySheet;
    
    public UpdateContactAction() {
        super(ContactManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("contact")) {
            selectedContact = (Contact) parameters.get("contact");
            commandClose = (Command) parameters.get("commandClose");
            // Property Sheet
            propertySheet = new PropertySheet(ts, new ArrayList<>());
            propertySheet.addPropertyValueChangedListener(this);
            propertySheet.setHeightFull();
            propertySheet.setWidthFull();
            updatePropertySheet(selectedContact);
            // Window to update contact
            ConfirmDialog wdwUpdateContact = new ConfirmDialog(ts
                    , ts.getTranslatedString("module.contactman.actions.update-contact.name")
                    , ts.getTranslatedString("module.general.messages.close")
            );
            wdwUpdateContact.setMinHeight("50%");
            wdwUpdateContact.setMinWidth("50%");
            wdwUpdateContact.setDraggable(true);
            wdwUpdateContact.setResizable(true);
            // Action
            ActionButton btnClose = new ActionButton(ts.getTranslatedString("module.general.messages.close"));
            btnClose.addClickListener(event -> wdwUpdateContact.close());
            btnClose.setWidthFull();
            btnClose.setThemeName("primary");
            btnClose.setClassName("primary-button");
            // Add content to layout
            VerticalLayout lytPropertySheet = new VerticalLayout();
            lytPropertySheet.setClassName("contactman-contact-property-sheet");
            lytPropertySheet.setWidthFull();
            lytPropertySheet.setWidthFull();
            lytPropertySheet.setMargin(false);
            lytPropertySheet.setSpacing(false);
            lytPropertySheet.setPadding(false);
            lytPropertySheet.add(propertySheet);
            // Add content to window
            wdwUpdateContact.setContent(lytPropertySheet);
            wdwUpdateContact.setFooter(btnClose);
            // Return window
            return wdwUpdateContact;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.contactman.actions.delete-contact.error-param-contact")));
    }

    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void updatePropertySheet(Contact contact) {
        try {
            Contact aWholeContact = bem.getContact(contact.getClassName(), contact.getId());
            propertySheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeContact, ts, aem, mem));
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        if (selectedContact != null) {
            try {
                StringPair udpatedProperty = new StringPair(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                List<StringPair> properties = new ArrayList();
                properties.add(udpatedProperty);

                bem.updateContact(selectedContact.getClassName()
                        , selectedContact.getId()
                        , properties
                        , UI.getCurrent().getSession().getAttribute(Session.class).getUser().getUserName()
                );
                // Refresh data provider
                commandClose.execute();
                // Notification
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (BusinessObjectNotFoundException | InvalidArgumentException 
                    | MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
                propertySheet.undoLastEdit();
            }
        }
    }
}