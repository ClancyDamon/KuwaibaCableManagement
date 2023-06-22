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
package org.neotropic.kuwaiba.modules.optional.contactman.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.Command;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.ObjectOptionsPanel;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.modules.optional.contactman.ContactManagerModule;
import org.neotropic.kuwaiba.modules.optional.contactman.actions.ReleaseObjectFromContactVisualAction;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper to manage contact resources.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class ContactResourcesDialog extends AbstractVisualAction<Dialog> implements ActionCompletedListener {
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private CoreActionsRegistry coreActionRegistry;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * All the object-related views exposed by other modules.
     */
    @Autowired
    private ViewWidgetRegistry viewWidgetRegistry;
    /**
     * All the registered explorers.
     */
    @Autowired
    private ExplorerWidgetRegistry explorerWidgetRegistry;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;
    /**
     * The visual action to release object from contact
     */
    @Autowired
    private ReleaseObjectFromContactVisualAction releaseObjectFromContactVisualAction;
    /**
     * Parameter contact 
     */
    private static final String PARAM_CONTACT = "contact";
    /**
     * Parameter business object
     */
    private static final String PARAM_BUSINESSOBJECT = "businessObject";    
    /**
     * Parameter command
     */
    private static final String PARAM_COMMAND = "command";
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    /**
     * Layout for resources grid
     */
    private VerticalLayout lytResourcesGrid;
    /**
     * Layout for resource details
     */
    private VerticalLayout lytResources;
    private VerticalLayout lytDetailsPanel;
    /**
     * The grid with the list resources
     */
    private Grid<BusinessObjectLight> gridResources;
    /**
     * Current contact
     */
    private BusinessObjectLight contact;
    
    public ContactResourcesDialog() {
        super(ContactManagerModule.MODULE_ID);
    }
    
    private void freeResources() {
        this.releaseObjectFromContactVisualAction.unregisterListener(this);
    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
    }   

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        this.releaseObjectFromContactVisualAction.registerActionCompletedLister(this);

        if (parameters.containsKey(PARAM_CONTACT)) {
            contact = (BusinessObjectLight) parameters.get(PARAM_CONTACT);

            ConfirmDialog wdwResources = new ConfirmDialog(ts,
                    ts.getTranslatedString("module.contactman.actions.manage-resources.name"),
                    ts.getTranslatedString("module.general.messages.close"));
            wdwResources.getBtnConfirm().setVisible(false);
            wdwResources.setContentSizeFull();
            wdwResources.setDraggable(true);
            wdwResources.setResizable(true);

            //create content
            splitLayout = new SplitLayout();
            splitLayout.setClassName("contactman-contact-resources-split");
            splitLayout.setSplitterPosition(36);
            //--Left Side 
            VerticalLayout lytLeftSide = new VerticalLayout();
            lytLeftSide.setClassName("contactman-lyt-contact-resources-grid");
            lytLeftSide.setId("lyt-left");
            lytLeftSide.setSpacing(false);
            lytLeftSide.setPadding(false);
            lytLeftSide.setMargin(false);
            lytLeftSide.setHeightFull();
            lytLeftSide.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
            lytResourcesGrid = new VerticalLayout();
            lytResourcesGrid.setSpacing(false);
            lytResourcesGrid.setPadding(false);
            lytResourcesGrid.setMargin(false);
            lytResourcesGrid.setHeightFull();
            buildResourcesGrid(contact);
            lytLeftSide.add(lytResourcesGrid);
            //end left side
            splitLayout.addToPrimary(lytLeftSide);

            //--Right side
            lytResources = new VerticalLayout();
            lytResources.setClassName("contactman-lyt-contact-resources-details");
            lytResources.setId("lyt-Right");
            lytResources.setSpacing(false);
            lytResources.setMargin(false);
            lytResources.setPadding(false);
            //end right side
            splitLayout.addToSecondary(lytResources);
            wdwResources.setContent(splitLayout);
            
            ActionButton btnClose = new ActionButton(ts.getTranslatedString("module.general.messages.close"));
            btnClose.addClickListener(event -> wdwResources.close());
            btnClose.setWidthFull();
            btnClose.setThemeName("primary");
            btnClose.setClassName("primary-button");
            wdwResources.setFooter(btnClose);
            
            wdwResources.addOpenedChangeListener(event -> {
                if (!event.isOpened())
                    freeResources();
            });

            return wdwResources;
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), PARAM_CONTACT)));
    }
    
    private void buildResourcesGrid(BusinessObjectLight contact) {
        try {
            List<BusinessObjectLight> resources = bem.getContactResources(contact.getClassName(), contact.getId());
            ListDataProvider<BusinessObjectLight> dataProviderResources = new ListDataProvider<>(resources);
            
            gridResources = new Grid<>();
            gridResources.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
            gridResources.setSelectionMode(Grid.SelectionMode.SINGLE);
            gridResources.setDataProvider(dataProviderResources);
            gridResources.setHeightFull();
            
            gridResources.addItemClickListener(event -> {
                try {
                    buildResourceDetailsPanel(event.getItem());
                    lytDetailsPanel.setVisible(true);
                } catch (InventoryException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
                }
            });
            
            Grid.Column<BusinessObjectLight> nameColum = gridResources.addColumn(TemplateRenderer.<BusinessObjectLight>of(
                    "<div>[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.className]]</font></div>")
                    .withProperty(Constants.PROPERTY_NAME, BusinessObjectLight::getName)
                    .withProperty(Constants.PROPERTY_CLASSNAME, BusinessObjectLight::getClassName));
            gridResources.addComponentColumn(object -> createActionRelease(object))
                    .setTextAlign(ColumnTextAlign.END).setFlexGrow(0).setWidth("50px");
            
            lytResourcesGrid.removeAll();
            lytResourcesGrid.add(gridResources);
            
            if (resources.isEmpty())
                labelInfoResource(nameColum);
            else 
                createTxtFieldContactName(nameColum, dataProviderResources);
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
        
    private void createTxtFieldContactName(Grid.Column column, ListDataProvider<BusinessObjectLight> dataProvider) {
        TextField txtContactName = new TextField();
        txtContactName.setPlaceholder(ts.getTranslatedString("module.general.label.search-by-name"));
        txtContactName.setValueChangeMode(ValueChangeMode.EAGER);
        txtContactName.setWidthFull();
        txtContactName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH));
        
        txtContactName.addValueChangeListener(event -> dataProvider.addFilter(
                aContact -> StringUtils.containsIgnoreCase(aContact.getName(), txtContactName.getValue())));
        
        HeaderRow filterRow = gridResources.appendHeaderRow();
        filterRow.getCell(column).setComponent(txtContactName);
    }
    
    private void labelInfoResource(Grid.Column column) {
        Label lblInfo = new Label(ts.getTranslatedString("module.contactman.actions.manage-resources.info-no-resources"));
        lblInfo.setWidthFull();
        HeaderRow filterRow = gridResources.appendHeaderRow();
        filterRow.getCell(column).setComponent(lblInfo);
    }
    
    private HorizontalLayout createActionRelease(BusinessObjectLight object) {
        Command releaseObject = () -> {
            buildResourcesGrid(contact);
            lytDetailsPanel.setVisible(false);
        };
        ActionButton btnRelease = new ActionButton(new ActionIcon(VaadinIcon.UNLINK)
                , this.releaseObjectFromContactVisualAction.getModuleAction().getDisplayName());
        btnRelease.addClickListener(event -> {
            this.releaseObjectFromContactVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(PARAM_CONTACT, contact),
                    new ModuleActionParameter(PARAM_BUSINESSOBJECT, object),
                    new ModuleActionParameter(PARAM_COMMAND, releaseObject))).open();
        });        
        HorizontalLayout lytAction = new HorizontalLayout(btnRelease);
        lytAction.setSizeFull();
        return lytAction;
    }
    
    private void buildResourceDetailsPanel(BusinessObjectLight object) throws InventoryException {
        try {
            if (!object.getClassName().equals(Constants.DUMMY_ROOT)) {
                ObjectOptionsPanel pnlOptions = new ObjectOptionsPanel(object,
                        coreActionRegistry, advancedActionsRegistry, viewWidgetRegistry, explorerWidgetRegistry, mem, aem, bem, ts);
                pnlOptions.setShowViews(false);
                pnlOptions.setShowCoreActions(false);
                pnlOptions.setShowCustomActions(false);
                pnlOptions.setShowExplorers(false);

                pnlOptions.setPropertyListener((property) -> {
                    Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                    Object lastValue =  pnlOptions.lastValue(property.getName());
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put(property.getName(), String.valueOf(property.getValue()));
                    try {
                        bem.updateObject(object.getClassName(), object.getId(), attributes);
                        if (property.getName().equals(Constants.PROPERTY_NAME)) {
                            buildResourcesGrid(contact);
                        }
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                                AbstractNotification.NotificationType.INFO, ts).open();
                        // activity log
                        aem.createObjectActivityLogEntry(session.getUser().getUserName(), object.getClassName(),
                                object.getId(), ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT,
                                property.getName(), lastValue == null ? "" : lastValue.toString(), property.getAsString(), "");
                    } catch (InventoryException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                        pnlOptions.UndoLastEdit();
                    }
                });

                // Action
                Button btnInfo = new Button(this.windowMoreInformation.getDisplayName());
                btnInfo.setWidthFull();
                btnInfo.addClickListener(event -> {
                    this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("object", object))).open();
                });
                // Header layout
                H4 headerPropertySheet = new H4(object.toString());
                headerPropertySheet.setClassName("header-position");
                HorizontalLayout lytHeader = new HorizontalLayout(headerPropertySheet);
                lytHeader.setMargin(false);
                lytHeader.setPadding(false);
                // Add content to layout
                lytDetailsPanel = new VerticalLayout(lytHeader, btnInfo, pnlOptions.build(UI.getCurrent().getSession().getAttribute(Session.class).getUser()));
                lytDetailsPanel.setHeightFull();
                lytDetailsPanel.setPadding(false);
                lytDetailsPanel.setMargin(false);
                lytDetailsPanel.setSpacing(false);
                // Layout resources content
                lytResources.removeAll();
                lytResources.add(lytDetailsPanel);
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}