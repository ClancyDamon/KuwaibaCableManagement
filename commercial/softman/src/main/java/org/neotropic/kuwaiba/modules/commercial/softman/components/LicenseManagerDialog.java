/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.softman.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.softman.SoftwareManagerModule;
import org.neotropic.kuwaiba.modules.commercial.softman.SoftwareManagerService;
import org.neotropic.kuwaiba.modules.commercial.softman.actions.DeleteLicensePoolVisualAction;
import org.neotropic.kuwaiba.modules.commercial.softman.actions.DeleteLicenseVisualAction;
import org.neotropic.kuwaiba.modules.commercial.softman.actions.NewLicensePoolVisualAction;
import org.neotropic.kuwaiba.modules.commercial.softman.actions.NewLicenseVisualAction;
import org.neotropic.kuwaiba.modules.commercial.softman.actions.ReleaseLicenseVisualAction;
import org.neotropic.kuwaiba.modules.commercial.softman.nodes.SoftwareObjectNode;
import org.neotropic.kuwaiba.modules.commercial.softman.visual.IconLabelCellGrid;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.tree.NavTreeGrid;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog to manage licenses.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@org.springframework.stereotype.Component
public class LicenseManagerDialog extends AbstractVisualAction<Dialog> implements ActionCompletedListener,
        PropertySheet.IPropertyValueChangedListener {
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Software Manager Service
    */
    @Autowired
    private SoftwareManagerService sms;
    /**
     * An action to add license pools
     */
    @Autowired
    private NewLicensePoolVisualAction newLicensePoolVisualAction;
    /**
     * An action to delete license pools
     */
    @Autowired
    private DeleteLicensePoolVisualAction deleteLicensePoolVisualAction;
    /**
     * An action to add licenses
     */
    @Autowired
    private NewLicenseVisualAction newLicenseVisualAction;
    /**
     * An action to delete licenses
     */
    @Autowired
    private DeleteLicenseVisualAction deleteLicenseVisualAction;
    /**
     * An action to release licenses
     */
    @Autowired
    private ReleaseLicenseVisualAction releaseLicenseVisualAction;
    /**
     * Factory to build resources from data source
     */
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * Reference to the selected pool
     */
    private SoftwareObjectNode nodeSelected;
    /**
     * An icon generator for create icons
     */
    private ClassNameIconGenerator iconGenerator;
    /**
     * Center column
     */
    private VerticalLayout lytCenter;
    /**
     * Left column
     */
    private VerticalLayout lytLeft;
    /**
     * Right column
     */
    private VerticalLayout lytRight;
    private Label lblPoolNameTitle;
    private PropertySheet propertySheetPool;
    private SoftwareObjectNode poolSelected;
    private VerticalLayout lytPools;
    /**
     * Property sheet license
     */
    private PropertySheet propertySheet;
    /**
     * Header property sheet
     */
    private Label lblPropertySheet;
    /**
     * License list inside a pool
     */
    private List<BusinessObjectLight> licenses;
    
    public LicenseManagerDialog() {
        super(SoftwareManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        // Action affected node
        // To keep record of the item who launch an action
        List<SoftwareObjectNode> actionAffectedNode = new ArrayList<>();
        actionAffectedNode.add(null);
        // Register action listener
        this.deleteLicensePoolVisualAction.registerActionCompletedLister(this);
        this.newLicensePoolVisualAction.registerActionCompletedLister(this);
        this.releaseLicenseVisualAction.registerActionCompletedLister(this);
        this.deleteLicenseVisualAction.registerActionCompletedLister(this);
        this.newLicenseVisualAction.registerActionCompletedLister(this);
        
        this.lytPools = new VerticalLayout();
        this.lytPools.setId("lytPools");
        this.lytPools.setPadding(false);
        this.lytPools.setSpacing(false);
        this.lytPools.setMargin(false);
        this.lytPools.setWidthFull();
        
        this.lytLeft = new VerticalLayout();
        this.lytLeft.setPadding(false);
        this.lytLeft.setSpacing(false);
        this.lytLeft.setId("lytLeft");

        this.lytCenter = new VerticalLayout();
        this.lytCenter.setVisible(false);
        this.lytCenter.setPadding(false);
        this.lytCenter.setSpacing(false);
        this.lytCenter.setId("lytCenter");

        this.lytRight = new VerticalLayout();
        this.lytRight.setPadding(false);
        this.lytRight.setSpacing(false);
        this.lytRight.setVisible(false);
        this.lytRight.setId("lytRight");

        ConfirmDialog wdwManageLicense = new ConfirmDialog(ts,
                    ts.getTranslatedString("module.softman.actions.manage-licenses.name"),
                    ts.getTranslatedString("module.general.labels.create"));
        wdwManageLicense.getBtnConfirm().setVisible(false);
        wdwManageLicense.setDraggable(true);
        wdwManageLicense.setResizable(true);
        wdwManageLicense.setContentSizeFull();
        
        Command addPool = () -> {
            lytLeft.remove(lytPools);
            loadPoolsGrid();
        };
         // Pool options
        Button btnNewPool = new Button(this.newLicensePoolVisualAction.getModuleAction().getDisplayName());
        btnNewPool.addClickListener(event -> this.newLicensePoolVisualAction.getVisualComponent(
                new ModuleActionParameterSet(new ModuleActionParameter(EActionParameter.ADD_POOL.getPropertyValue()
                        , addPool))).open());
        btnNewPool.setWidthFull();
        lytLeft.add(btnNewPool);
        
        iconGenerator = new ClassNameIconGenerator(resourceFactory);
        loadPoolsGrid();
        
        HorizontalLayout lytContent = new HorizontalLayout(lytLeft, lytCenter, lytRight);
        lytContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        lytContent.setPadding(false);
        lytContent.setSpacing(true);
        lytContent.setFlexGrow(0.25, lytLeft);
        lytContent.setFlexGrow(0.4, lytCenter);
        lytContent.setFlexGrow(0.35, lytRight);

        wdwManageLicense.setContent(lytContent);
        return wdwManageLicense;
    }
    
    private void loadPoolsGrid() {
        try {
            lytPools.removeAll();
            List<InventoryObjectPool> listPool = sms.getLicensePools();
            List<SoftwareObjectNode> pools = listPool.stream().map(item -> {
                SoftwareObjectNode pool = new SoftwareObjectNode(item);
                pool.setPool(true);
                return pool;
            }).collect(Collectors.toList());

            //Factory to build resources from data source
            Grid<SoftwareObjectNode> gridPools = new Grid<>();
            gridPools.setWidthFull();
            gridPools.setItems(pools);
            gridPools.addThemeVariants(GridVariant.LUMO_COMPACT);
            gridPools.setSelectionMode(Grid.SelectionMode.SINGLE);
            gridPools.setPageSize(10);
            
            gridPools.addComponentColumn(pool -> new IconLabelCellGrid(pool, true, iconGenerator))
                    .setHeader(ts.getTranslatedString("module.softman.label-pools"));
            gridPools.addComponentColumn(this::createActions).setTextAlign(ColumnTextAlign.END).setFlexGrow(0);
            
            gridPools.addItemClickListener(item -> {
               poolSelected = item.getItem();
               nodeSelected = item.getItem();
               loadLicensesGrid(poolSelected);
               updatePropertySheet(nodeSelected);
               showFields(true);
            });            
            
            lytPools.add(gridPools);
            if(listPool.isEmpty()) {
                Label lblInfo = new Label(ts.getTranslatedString("module.general.label.no-pools"));
                lytPools.add(lblInfo);
            }             
            lytLeft.add(lytPools);
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void loadLicensesGrid(SoftwareObjectNode aNode) {
        lytCenter.removeAll();

        // Factory to build resources from data source
        NavTreeGrid<SoftwareObjectNode> gridLicenses = new NavTreeGrid<SoftwareObjectNode>() {
            @Override
            public List<SoftwareObjectNode> fetchData(SoftwareObjectNode node) {
                List<SoftwareObjectNode> childrenNode = new ArrayList<>();
                try {
                    if (node != null) {
                        if (node.isPool()) {
                            licenses = sms.getLicensesInPool(aNode.getId(), -1);
                            licenses.forEach(license -> {
                                SoftwareObjectNode object = new SoftwareObjectNode(license);
                                childrenNode.add(object);
                            });
                        }
                    }
                } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
                return childrenNode;
            }
        };
        // we load the data
        gridLicenses.createDataProvider(aNode);
        
        gridLicenses.addThemeVariants(GridVariant.LUMO_COMPACT);
        gridLicenses.setSelectionMode(Grid.SelectionMode.SINGLE);
        //gridLicenses.setHeightByRows(true);
        gridLicenses.setPageSize(10);
        
        gridLicenses.addComponentHierarchyColumn(item-> new IconLabelCellGrid(item, false, iconGenerator))
                .setHeader(ts.getTranslatedString("module.softman.label-licenses"));
        gridLicenses.addComponentColumn(this::createActions).setTextAlign(ColumnTextAlign.END).setFlexGrow(0);

        gridLicenses.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridLicenses.addItemClickListener(event -> {
            nodeSelected = event.getItem();
            updatePropertySheet(nodeSelected);
            showFields(true);
        });
        
        HorizontalLayout lytTitle = new HorizontalLayout();
        lytTitle.setPadding(false);
        lytTitle.setMargin(false);
        lytTitle.setSpacing(true);
        lytTitle.setHeight("25px");
        lytTitle.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
      
        lblPoolNameTitle = new Label(aNode.getName());
        lblPoolNameTitle.setClassName("pools-right-header");
        HorizontalLayout lytItemsHeader = new HorizontalLayout();
        Icon poolIcon = new Icon(VaadinIcon.FOLDER_OPEN);
        poolIcon.setSize("16px");
        lytItemsHeader.add(poolIcon, lblPoolNameTitle);
        lytItemsHeader.setAlignItems(FlexComponent.Alignment.BASELINE);
        lytItemsHeader.setWidthFull();
        
        lytCenter.add(lytItemsHeader);
        lytCenter.add(gridLicenses);
        if (licenses.isEmpty()) {
            Label lblInfo = new Label(ts.getTranslatedString("module.general.label.no-licenses"));
            lytCenter.add(lblInfo);
        } 
        lytCenter.setVisible(true);
    }
    
    private Component createActions(SoftwareObjectNode node) {
        HorizontalLayout lytActions = new HorizontalLayout();
        lytActions.setHeight("22px");
        lytActions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        lytActions.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActions.setPadding(false);
        lytActions.setMargin(false);
        lytActions.setSpacing(false);
        
        if (node.isPool()) {
            Command deletePool = () -> {
                lytLeft.remove(lytPools);
                loadPoolsGrid();
                showFields(false);
            };
            ActionButton btnDeletePool = new ActionButton(new Icon(VaadinIcon.TRASH),
                    this.deleteLicensePoolVisualAction.getModuleAction().getDisplayName());
            btnDeletePool.addClickListener(event
                    -> deleteLicensePoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter(EActionParameter.POOL.getPropertyValue(), node.getObject()),
                            new ModuleActionParameter(EActionParameter.DELETE_POOL.getPropertyValue(), deletePool)
                    )).open());

            Command addLicense = () ->  loadLicensesGrid(node);
            ActionButton btnAddLicense = new ActionButton(new Icon(VaadinIcon.PLUS_SQUARE_O),
                    ts.getTranslatedString("module.softman.actions.new-license.name"));
            btnAddLicense.addClickListener(event
                    -> newLicenseVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter(EActionParameter.POOL.getPropertyValue(), node.getObject()),
                            new ModuleActionParameter(EActionParameter.ADD_LICENSE.getPropertyValue(), addLicense)
                    )).open());
            lytActions.add(btnDeletePool, btnAddLicense);
        } else {
            Command deleteLicense = () -> {
                lytRight.removeAll();
                loadLicensesGrid(node);
            };
            ActionButton btnDeleteLicense = new ActionButton(new Icon(VaadinIcon.TRASH),
                    ts.getTranslatedString("module.softman.actions.delete-license.name"));
            btnDeleteLicense.addClickListener(event ->
                this.deleteLicenseVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(EActionParameter.LICENSE.getPropertyValue(), node.getObject()),
                        new ModuleActionParameter(EActionParameter.DELETE_LICENSE.getPropertyValue(), deleteLicense)
                )).open()
            );
            
            Command releaseLicense = () ->  loadLicensesGrid(node);
            ActionButton btnReleaseLicense = new ActionButton(new Icon(VaadinIcon.UNLINK),
                    ts.getTranslatedString("module.softman.actions.release-license.name"));
            btnReleaseLicense.addClickListener(event ->
                this.releaseLicenseVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(EActionParameter.LICENSE.getPropertyValue(), node.getObject()),
                        new ModuleActionParameter(EActionParameter.RELEASE_LICENSE.getPropertyValue(), releaseLicense)
                )).open()
            );
            lytActions.add(btnReleaseLicense, btnDeleteLicense);
        }
        return lytActions;
    }
    
    private void showFields(boolean show) {
        lytCenter.setVisible(show);
        lytRight.setVisible(show);
    }

    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
        else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
    }

    private void updatePropertySheet(SoftwareObjectNode node) {
        if(node != null) {
            lytRight.removeAll();
            lblPropertySheet = new Label(node.getName());
            lblPropertySheet.setClassName("softman-license-property-sheet-header");
            
            if (node.isPool()) {
                try {
                    propertySheetPool = new PropertySheet(ts, new ArrayList<>());
                    propertySheetPool.addPropertyValueChangedListener(this);
                    InventoryObjectPool pool = sms.getLicensePool(node.getId(), node.getClassName());
                    propertySheetPool.setItems(PropertyFactory.propertiesFromPoolWithoutClassName(pool, ts));
                    lytRight.add(lblPropertySheet, propertySheetPool);
                } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
            } else {
                try {
                    propertySheet = new PropertySheet(ts, new ArrayList<>());
                    propertySheet.addPropertyValueChangedListener(this);
                    BusinessObject license = sms.getLicense(node.getClassName(), node.getId());
                    propertySheet.setItems(PropertyFactory.propertiesFromBusinessObject(license, ts, aem, mem));
                    lytRight.add(lblPropertySheet, propertySheet);
                } catch (InventoryException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
        }
    }
    
    @Override
    public void updatePropertyChanged(AbstractProperty<?> property) {
        if (nodeSelected != null) {
            if (nodeSelected.isPool()) {
                try {
                    if (property.getName().equals(Constants.PROPERTY_NAME)) {
                        aem.setPoolProperties(nodeSelected.getId(), String.valueOf(property.getValue()), nodeSelected.getDescription());
                        nodeSelected.setName(String.valueOf(property.getValue()));
                        lblPoolNameTitle.setText(String.valueOf(property.getValue()));
                        lblPropertySheet.setText(String.valueOf(property.getValue()));
                        lytLeft.remove(lytPools);
                        loadPoolsGrid();
                    } else if (property.getDescription().equals(Constants.PROPERTY_DESCRIPTION)) {
                        aem.setPoolProperties(nodeSelected.getId(), nodeSelected.getName(), String.valueOf(property.getValue()));
                        nodeSelected.setDescription(String.valueOf(property.getValue()));
                    }
                } catch (Exception ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                    propertySheetPool.undoLastEdit();
                }
            } else {
                try {
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                    bem.updateObject(nodeSelected.getClassName(), nodeSelected.getId(), attributes);
                    if (property.getName().equals(Constants.PROPERTY_NAME)) {
                        nodeSelected.setName(String.valueOf(property.getValue()));
                        lblPropertySheet.setText(String.valueOf(property.getValue()));
                        loadLicensesGrid(poolSelected);
                    }
                } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                    propertySheet.undoLastEdit();
                }
            }
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                    AbstractNotification.NotificationType.INFO, ts).open();
        }
    }
}