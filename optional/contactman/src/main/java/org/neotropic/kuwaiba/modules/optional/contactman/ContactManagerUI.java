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
package org.neotropic.kuwaiba.modules.optional.contactman;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.Contact;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.contactman.actions.DeleteContactVisualAction;
import org.neotropic.kuwaiba.modules.optional.contactman.actions.NewContactVisualAction;
import org.neotropic.kuwaiba.modules.optional.contactman.actions.RelateObjectToContactVisualAction;
import org.neotropic.kuwaiba.modules.optional.contactman.actions.UpdateContactAction;
import org.neotropic.kuwaiba.modules.optional.contactman.components.ContactResourcesDialog;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Contact Manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "contactman", layout = ContactManagerLayout.class)
public class ContactManagerUI extends VerticalLayout implements ActionCompletedListener, HasDynamicTitle, AbstractUI {
    /**
     * Reference to translation service
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
     * The visual action to create a new contact
     */
    @Autowired
    private NewContactVisualAction newContactVisualAction;
    /**
     * The visual action to delete a contact preselect
     */
    @Autowired
    private DeleteContactVisualAction deleteContactVisualAction;
    /**
     * The visual action to update a contact preselect
     */
    @Autowired
    private UpdateContactAction updateContactAction;
    /**
     * The visual action to manager contact resources
     */
    @Autowired
    private ContactResourcesDialog contactResourcesDialog;
    /**
     * The visual action to relate inventory objects to contacts
     */
    @Autowired
    private RelateObjectToContactVisualAction relateObjectToContactVisualAction;
    /**
     * Object to update contact
     */
    private ActionButton btnUpdateContact;
    /**
     * Object to delete contact
     */
    private ActionButton btnDeleteContact;
    /**
     * Object to manage contact resources
     */
    private ActionButton btnManageResources;
    /**
     * Object to save current contact
     */
    private Contact currentContact;
    /**
     * The grid with the contacts
     */
    private final Grid<Contact> tblContacts;
    /**
     * Objects to save filter values
     */
    private final HashMap<String, Object> filters;
    private ComboBox<BusinessObjectLight> cmbCustomer;
    private ComboBox<ClassMetadataLight> cmbType;
    private TextField txtName;
    private TextField txtEmail1;
    private TextField txtEmail2;
    /**
     * Limit of objects. -1 get all.
     */
    private final int LIMIT = -1;
    
    public ContactManagerUI() {
        super();
        setSizeFull();
        tblContacts = new Grid<>();
        filters = new HashMap<>();
    }

    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                        AbstractNotification.NotificationType.INFO, ts).open();
                loadDataProvider();
            } catch (UnsupportedOperationException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.contactResourcesDialog.unregisterListener(this);
        this.newContactVisualAction.unregisterListener(this);
        this.deleteContactVisualAction.unregisterListener(this);
        this.relateObjectToContactVisualAction.unregisterListener(this);
    }
       
    @Override
    public void initContent() {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);  
        HorizontalLayout lytMainContent = new HorizontalLayout();
        lytMainContent.setSizeFull();
        lytMainContent.setMargin(false);
        lytMainContent.setPadding(false);
        
        this.contactResourcesDialog.registerActionCompletedLister(this);
        this.newContactVisualAction.registerActionCompletedLister(this);
        this.deleteContactVisualAction.registerActionCompletedLister(this);
        this.relateObjectToContactVisualAction.registerActionCompletedLister(this);
        
        ActionButton btnAddContact = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                 this.newContactVisualAction.getModuleAction().getDisplayName());
        btnAddContact.addClickListener(event -> {
            this.newContactVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
        });
        
        Command deleteContact = () -> {
            btnDeleteContact.setEnabled(false);
            btnUpdateContact.setEnabled(false);
            currentContact = null;
        };
        btnDeleteContact = new ActionButton(new ActionIcon(VaadinIcon.TRASH), this.deleteContactVisualAction.getModuleAction().getDisplayName());
        btnDeleteContact.addClickListener(event -> {
            this.deleteContactVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("contact", currentContact),
                    new ModuleActionParameter("commandClose", deleteContact)
            )).open();
        });
        btnDeleteContact.setEnabled(false);
        
        Command updateContact = () -> loadDataProvider();
        btnUpdateContact = new ActionButton(new ActionIcon(VaadinIcon.EDIT), ts.getTranslatedString("module.contactman.actions.update-contact.name"));
        btnUpdateContact.addClickListener(event -> {
            this.updateContactAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("contact", currentContact),
                    new ModuleActionParameter("commandClose", updateContact))).open();
        });
        btnUpdateContact.setEnabled(false);

        ActionButton btnClearFilters = new ActionButton(new ActionIcon(VaadinIcon.ERASER)
                , ts.getTranslatedString("module.contactman.actions.clear-all-filters.name")); 
        btnClearFilters.addClickListener(event -> {
            cleanFilters();
            refreshDataProvider();
        });
        
        btnManageResources = new ActionButton(new ActionIcon(VaadinIcon.COG),
                ts.getTranslatedString("module.contactman.actions.manage-resources.name"));
        btnManageResources.addClickListener(event -> launchResourceDialog());
        btnManageResources.setEnabled(false);
        
        buildContactGrid();
        Label header = new Label(ts.getTranslatedString("module.contactman.header"));
        header.setClassName("contactman-lbl-header");
        HorizontalLayout lytHeader = new HorizontalLayout(header);
        lytHeader.setWidth("90%");
        HorizontalLayout lytActions = new HorizontalLayout(btnAddContact, btnDeleteContact, btnUpdateContact, btnClearFilters, btnManageResources);
        lytActions.setClassName("contactman-contact-actions-position");
        lytActions.setSpacing(false);
        HorizontalLayout lytFooter = new HorizontalLayout(lytHeader, lytActions);
        lytFooter.setWidthFull();
        VerticalLayout lytContact = new VerticalLayout(lytFooter, tblContacts);
        
        lytMainContent.add(lytContact);
        add(lytMainContent);
    }  
    
    private void buildContactGrid() {
        loadDataProvider();
        tblContacts.setHeightFull();
        tblContacts.addColumn(item -> item.getCustomer().getName())
                .setHeader(ts.getTranslatedString("module.contactman.label.costumer"))
                .setKey(ts.getTranslatedString("module.contactman.label.costumer")).setResizable(true);
        tblContacts.addColumn(Contact::getClassName)
                .setHeader(ts.getTranslatedString("module.contactman.label.type"))
                .setKey(ts.getTranslatedString("module.contactman.label.type")).setResizable(true);
        tblContacts.addColumn(Contact::getName)
                .setHeader(ts.getTranslatedString("module.contactman.label.name"))
                .setKey(ts.getTranslatedString("module.contactman.label.name")).setResizable(true);
        tblContacts.addColumn(item -> item.getAttributes().get(ts.getTranslatedString("module.contactman.label.attribute.email1")))
                .setHeader(ts.getTranslatedString("module.contactman.label.email1"))
                .setKey(ts.getTranslatedString("module.contactman.label.email1")).setResizable(true);
        tblContacts.addColumn(item -> item.getAttributes().get(ts.getTranslatedString("module.contactman.label.attribute.email2")))
                .setHeader(ts.getTranslatedString("module.contactman.label.email2"))
                .setKey(ts.getTranslatedString("module.contactman.label.email2")).setResizable(true);
        tblContacts.addColumn(item -> item.getAttributes().get(ts.getTranslatedString("module.contactman.label.attribute.cellphone")))
                .setHeader(ts.getTranslatedString("module.contactman.label.cellphone"))
                .setKey(ts.getTranslatedString("module.contactman.label.cellphone")).setResizable(true);
        tblContacts.addColumn(item -> item.getAttributes().get(ts.getTranslatedString("module.contactman.label.attribute.role")))
                .setHeader(ts.getTranslatedString("module.contactman.label.role"))
                .setKey(ts.getTranslatedString("module.contactman.label.role")).setResizable(true);
        tblContacts.addColumn(item -> item.getAttributes().get(ts.getTranslatedString("module.contactman.label.attribute.skype")))
                .setHeader(ts.getTranslatedString("module.contactman.label.skype"))
                .setKey(ts.getTranslatedString("module.contactman.label.skype")).setResizable(true);
        tblContacts.addColumn(item -> item.getAttributes().get(ts.getTranslatedString("module.contactman.label.attribute.fax")))
                .setHeader(ts.getTranslatedString("module.contactman.label.fax"))
                .setKey(ts.getTranslatedString("module.contactman.label.fax")).setResizable(true);
        tblContacts.addColumn(item -> item.getAttributes().get(ts.getTranslatedString("module.contactman.label.attribute.telephone1")))
                .setHeader(ts.getTranslatedString("module.contactman.label.telephone1"))
                .setKey(ts.getTranslatedString("module.contactman.label.telephone1")).setResizable(true);
        tblContacts.addColumn(item -> item.getAttributes().get(ts.getTranslatedString("module.contactman.label.attribute.telephone2")))
                .setHeader(ts.getTranslatedString("module.contactman.label.telephone2"))
                .setKey(ts.getTranslatedString("module.contactman.label.telephone2")).setResizable(true);
        tblContacts.addColumn(item -> item.getAttributes().get(ts.getTranslatedString("module.contactman.label.attribute.availability")))
                .setHeader(ts.getTranslatedString("module.contactman.label.availability"))
                .setKey(ts.getTranslatedString("module.contactman.label.availability")).setResizable(true);
        tblContacts.addComponentColumn(item -> {
            BusinessObject object;
            try {
                object = aem.getListTypeItem(Constants.CLASS_LANGUAGE_TYPE,
                        String.valueOf(item.getAttributes().get(ts.getTranslatedString("module.contactman.label.attribute.language"))));
            } catch (MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                return new Label();
            }
            return new Label(object.getName());
        }).setHeader(ts.getTranslatedString("module.contactman.label.language"))
                .setKey(ts.getTranslatedString("module.contactman.label.language")).setResizable(true);
        // Listener
        tblContacts.addItemClickListener(event -> {
           btnUpdateContact.setEnabled(true);
           btnDeleteContact.setEnabled(true);
           btnManageResources.setEnabled(true);
           currentContact = event.getItem();
        });
        getFilters();
    }
       
    private void loadDataProvider() {
        try {
            List<Contact> contacts = bem.getContacts(0, LIMIT, filters);
            DataProvider<Contact, Void> dataProvider
                    = DataProvider.fromFilteringCallbacks(
                            query -> {
                                List<Contact> contact = new ArrayList<>();
                                try {
                                    contact = bem.getContacts(query.getOffset(), query.getLimit(), filters);
                                } catch (InvalidArgumentException ex) {
                                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                            AbstractNotification.NotificationType.ERROR, ts).open();
                                }
                                return contact.stream();// Sequence of objects
                            },
                            query -> {
                                return contacts.size();// Max possible items to fetch
                            }
                    );
            tblContacts.setDataProvider(dataProvider);
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void refreshDataProvider() {
        try {
            List<Contact> contacts = bem.getContacts(0, LIMIT, null);
            DataProvider<Contact, Void> dataProvider
                    = DataProvider.fromFilteringCallbacks(
                            query -> {
                                List<Contact> contact = new ArrayList<>();
                                try {
                                    contact = bem.getContacts(query.getOffset(), query.getLimit(), null);
                                } catch (InvalidArgumentException ex) {
                                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                            AbstractNotification.NotificationType.ERROR, ts).open();
                                }
                                return contact.stream();// Sequence of objects
                            },
                            query -> {
                                return contacts.size();// Max possible items to fetch
                            }
                    );
            tblContacts.setDataProvider(dataProvider);
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void getFilters() {
        try {
            // Lists
            List<ClassMetadataLight> contactTypes = mem.getSubClassesLight(Constants.CLASS_GENERICCONTACT, false, false);
            List<BusinessObjectLight> customers = bem.getObjectsOfClassLight(Constants.CLASS_GENERICCUSTOMER, null, LIMIT, LIMIT);
            // Header Row
            HeaderRow headerRow = tblContacts.prependHeaderRow();
            // Customer filter
            cmbCustomer = new ComboBox<>();
            cmbCustomer.setSizeFull();
            cmbCustomer.setItems(customers);
            cmbCustomer.setItemLabelGenerator(customer -> ts.getTranslatedString(customer.getName()));
            cmbCustomer.setAllowCustomValue(false);
            cmbCustomer.setClearButtonVisible(true);
            cmbCustomer.setPlaceholder(ts.getTranslatedString("module.general.labels.filter"));
            cmbCustomer.addValueChangeListener(event -> {
               if (event.getValue() != null)
                   filters.put("customer", event.getValue().getName());
               else
                   filters.remove("customer");
               loadDataProvider();
            });
            headerRow.getCell(tblContacts.getColumnByKey(ts.getTranslatedString("module.contactman.label.costumer"))).setComponent(cmbCustomer);
            // Type filter
            cmbType = new ComboBox<>();
            cmbType.setSizeFull();
            cmbType.setItems(contactTypes);
            cmbType.setAllowCustomValue(false);
            cmbType.setClearButtonVisible(true);
            cmbType.setPlaceholder(ts.getTranslatedString("module.general.labels.filter"));
            cmbType.addValueChangeListener(event -> {
               if (event.getValue() != null)
                   filters.put("type", event.getValue().getName());
               else 
                   filters.remove("type");
               loadDataProvider();
            });
            headerRow.getCell(tblContacts.getColumnByKey(ts.getTranslatedString("module.contactman.label.type"))).setComponent(cmbType);
            // Name filter
            txtName = new TextField();
            txtName.setSizeFull();
            txtName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter"));
            txtName.setValueChangeMode(ValueChangeMode.EAGER);
            txtName.addValueChangeListener(event -> {
                if (event.getValue() != null && !event.getValue().isEmpty())
                    filters.put("contact_name", event.getValue());
                else 
                    filters.remove("contact_name");
                loadDataProvider();
            });
            headerRow.getCell(tblContacts.getColumnByKey(ts.getTranslatedString("module.contactman.label.name"))).setComponent(txtName);
            // Email1 filter
            txtEmail1 = new TextField();
            txtEmail1.setSizeFull();
            txtEmail1.setPlaceholder(ts.getTranslatedString("module.general.labels.filter"));
            txtEmail1.setValueChangeMode(ValueChangeMode.EAGER);
            txtEmail1.addValueChangeListener(event -> {
               if (event.getValue() != null && !event.getValue().isEmpty())
                   filters.put("contact_email1", event.getValue());
               else
                   filters.remove("contact_email1");
               loadDataProvider();
            });
            headerRow.getCell(tblContacts.getColumnByKey(ts.getTranslatedString("module.contactman.label.email1"))).setComponent(txtEmail1);
            // Email2 filter
            txtEmail2 = new TextField();
            txtEmail2.setSizeFull();
            txtEmail2.setPlaceholder(ts.getTranslatedString("module.general.labels.filter"));
            txtEmail2.setValueChangeMode(ValueChangeMode.EAGER);
            txtEmail2.addValueChangeListener(event -> {
               if (event.getValue() != null && !event.getValue().isEmpty())
                   filters.put("contact_email2", event.getValue());
               else
                   filters.remove("contact_email2");
               loadDataProvider();
            });
            headerRow.getCell(tblContacts.getColumnByKey(ts.getTranslatedString("module.contactman.label.email2"))).setComponent(txtEmail2);
        } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void cleanFilters() {
        if (cmbCustomer.getValue() != null) {
            cmbCustomer.clear();
            filters.remove("customer");
        }
        if (cmbType.getValue() != null) {
            cmbType.clear();
            filters.remove("type");
        }
        if (txtName.getValue() != null) {
            txtName.clear();
            filters.remove("contact_name");
        }
        if (txtEmail1.getValue() != null) {
            txtEmail1.clear();
            filters.remove("contact_email1");
        }
        if (txtEmail2.getValue() != null) {
            txtEmail2.clear();
            filters.remove("contact_email2");
        }
    }
    
    private void launchResourceDialog() {
        this.contactResourcesDialog.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter<>("contact", currentContact)
        )).open();
    }
 
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.contactman.title");
    } 
}