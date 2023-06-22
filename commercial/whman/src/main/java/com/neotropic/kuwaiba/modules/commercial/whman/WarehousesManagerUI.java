/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.commercial.whman;

import com.neotropic.kuwaiba.modules.commercial.whman.actions.CopyObjectToWarehouseVisualAction;
import com.neotropic.kuwaiba.modules.commercial.whman.actions.DeleteSparePoolVisualAction;
import com.neotropic.kuwaiba.modules.commercial.whman.actions.DeleteWarehouseVisualAction;
import com.neotropic.kuwaiba.modules.commercial.whman.actions.MoveObjectToWarehouseVisualAction;
import com.neotropic.kuwaiba.modules.commercial.whman.actions.NewSparePartVisulaAction;
import com.neotropic.kuwaiba.modules.commercial.whman.actions.NewSparePoolVisualAction;
import com.neotropic.kuwaiba.modules.commercial.whman.actions.NewWarehouseVisualAction;
import com.neotropic.kuwaiba.modules.commercial.whman.actions.UpdateSparePoolVisualAction;
import com.neotropic.kuwaiba.modules.commercial.whman.nodes.WarehouseManagerNode;
import com.neotropic.kuwaiba.modules.commercial.whman.persistence.WarehousesService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
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
import org.neotropic.kuwaiba.modules.core.navigation.ObjectOptionsPanel;
import org.neotropic.kuwaiba.modules.core.navigation.actions.CopyBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.DefaultDeleteBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.MoveBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.grids.IconNameCellGrid;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Warehouses Module. 
 * This class manages how the pages corresponding to different functionalities are presented in a single place.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "whman", layout = WarehousesManagerLayout.class)
public class WarehousesManagerUI extends VerticalLayout implements ActionCompletedListener, PropertySheet.IPropertyValueChangedListener, HasDynamicTitle, AbstractUI {
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
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Business Entity Manager
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
     * Reference to the Warehouses Services
     */
    @Autowired
    private WarehousesService ws;
    /**
     * The visual action to create a new warehouse
     */
    @Autowired
    private NewWarehouseVisualAction newWarehouseVisualAction;
    /**
     * The visual action to create a new spare pool
     */
    @Autowired
    private NewSparePoolVisualAction newSparePoolVisualAction;
    /**
     * The visual action to create a new object in spare pool
     */
    @Autowired
    private NewSparePartVisulaAction newSparePartVisulaAction;
    /**
     * The visual action to update a spare pool
     */
    @Autowired
    private UpdateSparePoolVisualAction updateSparePoolVisualAction;
    /**
     * The visual action to delete a spare pool
     */
    @Autowired
    private DeleteSparePoolVisualAction deleteSparePoolVisualAction;
    /**
     * The visual action to delete a warehouse
     */
    @Autowired
    private DeleteWarehouseVisualAction deleteWarehouseVisualAction;
    /**
     * Reference to the action that creates a new Business Object.
     */
    @Autowired
    private NewBusinessObjectVisualAction actNewObj;
    /**
     * Reference to the action that creates a new Business Object from a template.
     */
    @Autowired
    private NewBusinessObjectFromTemplateVisualAction actNewObjFromTemplate;
    /**
     * Reference to the action that creates a multiple new Business Object from a pattern.
     */
    @Autowired
    private NewMultipleBusinessObjectsVisualAction actNewMultipleObj;
    /**
     * Reference to the action that deletes a Business Object.
     */
    @Autowired
    private DefaultDeleteBusinessObjectVisualAction actDeleteObj;
    /**
     * Reference to the action that copies a business object to another business object.
     */
    @Autowired
    private CopyBusinessObjectVisualAction actCopyObj;
    /**
     * Reference to the action that moves a business object to another business object.
     */
    @Autowired
    private MoveBusinessObjectVisualAction actMoveObj;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;
    /**
     * Factory to build resources from data source.
     */
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * Reference to the action that copy a business object to a spare pool.
     */
    @Autowired
    private CopyObjectToWarehouseVisualAction actCopyObjToWarehouse;
    /**
     * Reference to the action that move a business object to a spare pool.
     */
    @Autowired
    private MoveObjectToWarehouseVisualAction actMoveObjToWarehouse;
    /**
     * Items limit. -1 To return all
     */
    private static final int LIMIT = -1;
    /**
     * The grid with the list warehouses
     */
    private Grid<BusinessObjectLight> gridWarehouses;
    /**
     * The grid with the list objects
     */
    private TreeGrid<WarehouseManagerNode> gridObjects;
    /**
     * Object to save the selected root pool
     */
    private InventoryObjectPool currentRootPool;
    /**
     * Object to save the selected pool
     */
    private InventoryObjectPool currentSparePool;
    /**
     * Object to save the selected warehouse
     */
    private BusinessObjectLight currentWarehouse;
    /**
     * Object to save the selected object
     */
    private BusinessObjectLight currentObject;
    /**
     * Layouts
     */
    private HorizontalLayout lytMain;
    private VerticalLayout lytTabs;
    private VerticalLayout lytWarehousesPools;
    private HorizontalLayout lytSparePools;
    private VerticalLayout lytObjects; 
    private VerticalLayout lytDetailsPanel;
    private VerticalLayout lytObjectPropertySheet;
    private VerticalLayout lytWarehousePropertySheet;
    /**
     * Property sheet
     */
    private PropertySheet propertysheetWarehouse;
    /**
     * Button used to create a new warehouse
     */
    private Button btnNewWarehouse;
    /**
     * Button used to create a new spare pool
     */
    private ActionButton btnNewSparePool;
    /**
     * Button used to create a new object in spare pool
     */
    private Button btnNewSparePart;
    /**
     * Button used to delete a spare pool
     */
    private ActionButton btnDeleteSparePool;
    /**
     * Button used to delete a warehouse
     */
    private Button btnDeleteWarehouse;
    /**
     * Button used to update a spare pool
     */
    private ActionButton btnUpdateSparePool;
    /**
     * Object to show info of warehouses
     */
    private Label lblInfo;
    /**
     * An icon generator for create icons
     */
    private ClassNameIconGenerator iconGenerator;
    /**
     * Object to save the navigation tabs
     */
    private Tabs tabsRootPool;
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) { 
            if (ev.getActionResponse() == null || ev.getActionResponse().isEmpty()) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                        AbstractNotification.NotificationType.INFO, ts).open();
                refreshWarehousesGrid();              
                if(currentWarehouse != null)
                    refreshSparePoolsGrid();
            } else if (ev.getActionResponse() != null && !ev.getActionResponse().isEmpty()) {
                if (ev.getActionResponse().containsKey(ActionResponse.ActionType.REMOVE)) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.navigation.actions.delete-business-object.name-success"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                    refreshObjectsGrid();
                } else if (ev.getActionResponse().containsKey(ActionResponse.ActionType.ADD)
                        && ev.getActionResponse().containsKey(Constants.PROPERTY_PARENT_CLASS_NAME)) {
                    Object addedObj = ev.getActionResponse().get(ActionResponse.ActionType.ADD);
                    
                    if (addedObj instanceof BusinessObjectLight) //notification for single object creation 
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                ts.getTranslatedString("module.navigation.actions.new-business-object.name-success"),
                                AbstractNotification.NotificationType.INFO, ts).open();
                    else if (addedObj instanceof Integer) //notification for bulk object creation 
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                String.format(ts.getTranslatedString("module.navigation.actions.new-business-objects.name-success"), addedObj),
                                AbstractNotification.NotificationType.INFO, ts).open();
                    refreshObjectsGrid();
                } else if (ev.getActionResponse().containsKey(ActionResponse.ActionType.COPY) ||
                        ev.getActionResponse().containsKey(ActionResponse.ActionType.MOVE)) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                        AbstractNotification.NotificationType.INFO, ts).open();
                    if (currentSparePool != null)
                        refreshObjectsGrid();
                }
            }
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.newWarehouseVisualAction.unregisterListener(this);
        this.newSparePoolVisualAction.unregisterListener(this);
        this.newSparePartVisulaAction.unregisterListener(this);
        this.updateSparePoolVisualAction.unregisterListener(this);
        this.deleteSparePoolVisualAction.unregisterListener(this);
        this.deleteWarehouseVisualAction.unregisterListener(this);
        // Core Actions
        this.actNewObjFromTemplate.unregisterListener(this);
        this.actNewMultipleObj.unregisterListener(this);
        this.actNewObj.unregisterListener(this);
        this.actDeleteObj.unregisterListener(this);
        this.actCopyObj.unregisterListener(this);
        this.actMoveObj.unregisterListener(this);
        // Advanced Actions
        this.actCopyObjToWarehouse.unregisterListener(this);
        this.actMoveObjToWarehouse.unregisterListener(this);
    }
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.whman.title");
    }

    @Override
    public void initContent() {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);
        
        this.newWarehouseVisualAction.registerActionCompletedLister(this);
        this.newSparePoolVisualAction.registerActionCompletedLister(this);
        this.newSparePartVisulaAction.registerActionCompletedLister(this);
        this.updateSparePoolVisualAction.registerActionCompletedLister(this);
        this.deleteSparePoolVisualAction.registerActionCompletedLister(this);
        this.deleteWarehouseVisualAction.registerActionCompletedLister(this);
        // Core Actions
        this.actNewMultipleObj.registerActionCompletedLister(this);
        this.actNewObjFromTemplate.registerActionCompletedLister(this);
        this.actNewObj.registerActionCompletedLister(this);
        this.actDeleteObj.registerActionCompletedLister(this);
        this.actCopyObj.registerActionCompletedLister(this);
        this.actMoveObj.registerActionCompletedLister(this);
        // Advanced Actions
        this.actCopyObjToWarehouse.registerActionCompletedLister(this);
        this.actMoveObjToWarehouse.registerActionCompletedLister(this);
        
        Command newWarehouse = () -> refreshWarehousesGrid();
        btnNewWarehouse = new Button(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                (event) -> {
                    this.newWarehouseVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("pool", currentRootPool),
                            new ModuleActionParameter("commandClose", newWarehouse))).open();
                });
        btnNewWarehouse.setText(this.newWarehouseVisualAction.getModuleAction().getDisplayName());
        btnNewWarehouse.getElement().setProperty("title", this.newWarehouseVisualAction.getModuleAction().getDescription());
        btnNewWarehouse.setWidth("50%");
                
        Command newSparePool = () -> refreshSparePoolsGrid();
        btnNewSparePool = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                 this.newSparePoolVisualAction.getModuleAction().getDisplayName());
        btnNewSparePool.addClickListener(event -> {
            this.newSparePoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("warehouse", currentWarehouse),
                    new ModuleActionParameter("commandClose", newSparePool))).open();
        });
        btnNewSparePool.setEnabled(false);
        btnNewSparePool.setHeight("32px");
        
        Command newObject = () -> refreshObjectsGrid();
        btnNewSparePart = new Button(this.newSparePartVisulaAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS_SQUARE_O),
                (event) -> {
                    this.newSparePartVisulaAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("sparePool", currentSparePool),
                            new ModuleActionParameter("commandClose", newObject))).open();
                });
        btnNewSparePart.getElement().setProperty("title", this.newSparePartVisulaAction.getModuleAction().getDescription());
        btnNewSparePart.setClassName("whman-btn-new-spare-part");
        btnNewSparePart.setVisible(false);
        btnNewSparePart.setEnabled(false);
        
        Command deleteSparePool = () -> {
            currentSparePool = null;
            btnDeleteSparePool.setEnabled(false);
            btnUpdateSparePool.setEnabled(false);
            lytDetailsPanel.removeAll();
            lytObjects.removeAll();
            refreshSparePoolsGrid();
        };
        btnDeleteSparePool = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                this.deleteSparePoolVisualAction.getModuleAction().getDisplayName());
        btnDeleteSparePool.addClickListener(event -> {
            this.deleteSparePoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("sparePool", currentSparePool),
                    new ModuleActionParameter("commandClose", deleteSparePool))).open();
        });
        btnDeleteSparePool.setEnabled(false);
        btnDeleteSparePool.setHeight("32px");
        
        Command deleteWarehouse = () -> {
            currentWarehouse = null;
            btnDeleteWarehouse.setEnabled(false);
            btnNewSparePart.setVisible(false);
            lytDetailsPanel.removeAll();
            lytObjects.removeAll();
            lytSparePools.removeAll();
            lytWarehousePropertySheet.removeAll();
            refreshWarehousesGrid();
        };
        btnDeleteWarehouse = new Button(this.deleteWarehouseVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH), 
                (event) -> {
                    this.deleteWarehouseVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("warehouse", currentWarehouse),
                            new ModuleActionParameter("commandClose", deleteWarehouse))).open();
                });
        btnDeleteWarehouse.getElement().setProperty("title", this.deleteWarehouseVisualAction.getModuleAction().getDescription());
        btnDeleteWarehouse.setEnabled(false);
        btnDeleteWarehouse.setWidth("50%");

        Command updateSparePool = () -> refreshSparePoolsGrid();
        btnUpdateSparePool = new ActionButton(new ActionIcon(VaadinIcon.EDIT),
                this.updateSparePoolVisualAction.getModuleAction().getDisplayName());
        btnUpdateSparePool.addClickListener(event -> {
            this.updateSparePoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("sparePool", currentSparePool),
                    new ModuleActionParameter("commandClose", updateSparePool))).open();
        });
        btnUpdateSparePool.setEnabled(false);
        btnUpdateSparePool.setHeight("32px");
        
        // Layout for root pool
        lytTabs = new VerticalLayout();
        lytTabs.setHeightFull();
        lytTabs.setWidth("30%");
        lytTabs.setSpacing(true);
        lytTabs.setMargin(false);
        // Layout for warehouses pools grid
        lytWarehousesPools = new VerticalLayout();
        lytWarehousesPools.setWidthFull();
        lytWarehousesPools.setHeightFull();
        lytWarehousesPools.setPadding(false);
        lytWarehousesPools.setMargin(false);
        lytWarehousesPools.setSpacing(false);
        // Layout for details
        lytDetailsPanel = new VerticalLayout();
        lytDetailsPanel.setWidth("30%");
        lytDetailsPanel.setHeightFull();
        lytDetailsPanel.setSpacing(false);
        lytDetailsPanel.setMargin(false);
        // Layout for spare pools
        lytSparePools = new HorizontalLayout();
        lytSparePools.setWidthFull();
        lytSparePools.setPadding(false);
        lytSparePools.setMargin(false);
        lytSparePools.setSpacing(true);
        lytSparePools.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        // Layout for objects           
        lytObjects = new VerticalLayout();
        lytObjects.setWidthFull();
        lytObjects.setHeightFull();
        lytObjects.setPadding(false);
        lytObjects.setSpacing(false);
        lytObjects.setMargin(false);
        // Layout for objects grid
        VerticalLayout lytObjectsGrid = new VerticalLayout(lytSparePools, btnNewSparePart, lytObjects);
        lytObjectsGrid.setWidth("50%");
        lytObjectsGrid.setHeightFull();
        lytObjectsGrid.setSpacing(true);
        lytObjectsGrid.setMargin(false);
        // Create tabs
        createTabsRootPools();
        // Main layout
        lytMain = new HorizontalLayout(lytTabs, lytObjectsGrid, lytDetailsPanel);
        lytMain.setHeightFull();
        lytMain.setSizeFull();
        lytMain.setPadding(false);
        lytMain.setMargin(false);
        lytMain.setSpacing(false);
        lytMain.setId("main-lyt");
        add(lytMain);
    }
    
    private void createTabsRootPools() {
        try {
            List<InventoryObjectPool> warehousePools = bem.getRootPools(Constants.CLASS_WAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
            List<InventoryObjectPool> virtualWarehousePools = bem.getRootPools(Constants.CLASS_VIRTUALWAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);

            Tab tabPhysycalWarehouses = new Tab();
            Tab tabVirtualWarehouses = new Tab();
            Div pageWarehouses = new Div();
            
            Map<Tab, Component> tabsToPages = new HashMap<>();
            tabsToPages.put(tabPhysycalWarehouses, pageWarehouses);
            tabsToPages.put(tabVirtualWarehouses, pageWarehouses);
            
            if (!warehousePools.isEmpty() && !virtualWarehousePools.isEmpty()) {
                tabPhysycalWarehouses.setLabel(ts.getTranslatedString("module.whman.label-physical-warehouse"));
                tabVirtualWarehouses.setLabel(ts.getTranslatedString("module.whman.label-virtual-warehouse"));

                tabsRootPool = new Tabs(tabPhysycalWarehouses, tabVirtualWarehouses);
                tabsRootPool.setFlexGrowForEnclosedTabs(1);
                tabsRootPool.setSelectedTab(tabPhysycalWarehouses);
                currentRootPool = warehousePools.get(0);
                buildWarehousesGrid(currentRootPool);
                // Listener
                tabsRootPool.addSelectedChangeListener((event) -> {
                    lytWarehousesPools.removeAll();
                    lytSparePools.removeAll();
                    lytObjects.removeAll();
                    btnNewSparePart.setVisible(false);
                
                    if (event.getSelectedTab().equals(tabPhysycalWarehouses))
                        currentRootPool = warehousePools.get(0);
                    else if (event.getSelectedTab().equals(tabVirtualWarehouses))
                        currentRootPool = virtualWarehousePools.get(0);
                   
                    buildWarehousesGrid(currentRootPool);
                });
                pageWarehouses.setSizeFull();
                pageWarehouses.add(lytWarehousesPools);
                pageWarehouses.setWidthFull();
                
                HorizontalLayout lytButtons = new HorizontalLayout(btnNewWarehouse, btnDeleteWarehouse);
                lytButtons.setMargin(false);
                lytButtons.setSpacing(true);
                lytButtons.setWidthFull();
                
                lytTabs.add(tabsRootPool, lytButtons, pageWarehouses);
            }
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void buildWarehousesGrid(InventoryObjectPool parentPool) {
        try {
            gridWarehouses = new Grid<>();
            List<BusinessObjectLight> warehouses = ws.getWarehousesInPool(parentPool.getId(), LIMIT);
            ListDataProvider<BusinessObjectLight> dataProvider = new ListDataProvider<>(warehouses);
            gridWarehouses.setDataProvider(dataProvider);
            gridWarehouses.setHeightFull();
            
            gridWarehouses.addColumn(TemplateRenderer.<BusinessObjectLight>of(
                "<div style = \"white-space: normal; overflow-wrap: anywhere;\">[[item.name]] &nbsp;</div>")
                .withProperty("name", BusinessObjectLight::getName))
                .setKey(ts.getTranslatedString("module.general.labels.name"));
            
            gridWarehouses.addItemClickListener(event -> {
                lytSparePools.removeAll();
                lytObjects.removeAll();
                currentWarehouse = event.getItem();
                createFilterSparePools(currentWarehouse);
                btnDeleteWarehouse.setEnabled(true);
                btnNewSparePool.setEnabled(true);
                btnDeleteSparePool.setEnabled(false);
                btnUpdateSparePool.setEnabled(false);
                btnNewSparePart.setVisible(false);
                buildWarehouseDetailPanel(currentWarehouse);        
                updatePropertySheet(currentWarehouse);
            });
            // Validate warehouses list size
            lblInfo = new Label();
            if (warehouses.isEmpty()) {
                lblInfo.setVisible(true);
                lblInfo.setText(ts.getTranslatedString("module.whman.class-warehouse.label.no-associated-warehouse"));
                lytWarehousesPools.add(lblInfo);
            } else {
                lytWarehousesPools.add(gridWarehouses);
                // Filter warehouse by name
                HeaderRow filterRow = gridWarehouses.appendHeaderRow();
                TextField txtWarehouseName = createTxtFieldWarehouseName(dataProvider);
                filterRow.getCell(gridWarehouses.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtWarehouseName);
                lblInfo.setVisible(false);
            }      
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void createFilterSparePools(BusinessObjectLight businessObject) {
        try {            
            List<InventoryObjectPool> listPool = ws.getPoolsInWarehouse(businessObject.getClassName(), businessObject.getId());
            // First filter
            ComboBox<InventoryObjectPool> cmbFilterPoolName = new ComboBox<>();
            cmbFilterPoolName.setPlaceholder(ts.getTranslatedString("module.whman.filter.choose.spare-pool"));
            cmbFilterPoolName.setItems(listPool);
            cmbFilterPoolName.setWidth("90%");
            cmbFilterPoolName.setAllowCustomValue(false);
            cmbFilterPoolName.setClearButtonVisible(true);
            cmbFilterPoolName.addValueChangeListener(event -> {
                lytObjects.removeAll();
                if (event.getValue() != null) {
                    currentSparePool = event.getValue();
                    buildObjectsGrid(currentSparePool);
                    btnDeleteSparePool.setEnabled(true);
                    btnUpdateSparePool.setEnabled(true);
                    btnNewSparePart.setVisible(true);
                    btnNewSparePart.setEnabled(true);
                } else {
                    btnDeleteSparePool.setEnabled(false);
                    btnUpdateSparePool.setEnabled(false);
                    btnNewSparePart.setVisible(false);
                    btnNewSparePart.setEnabled(false);
                    lytDetailsPanel.removeAll();
                    lytObjects.removeAll();
                }
            });
            HorizontalLayout lytActions = new HorizontalLayout();
            lytActions.removeAll();
            lytActions.setMargin(false);
            lytActions.setPadding(false);
            lytActions.setSpacing(false);
            
            lytActions.add( btnNewSparePool, btnUpdateSparePool, btnDeleteSparePool);
            lytActions.setClassName("whman-lyt-actions-spare-pool");
            lytSparePools.add(cmbFilterPoolName, lytActions);
        } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void buildObjectsGrid(InventoryObjectPool parentPool) {
        try {
            gridObjects = new TreeGrid<>();
            gridObjects.setPageSize(10);
            gridObjects.setSizeFull();
            gridObjects.setHeightFull();
            gridObjects.setWidthFull();
            gridObjects.setSelectionMode(Grid.SelectionMode.SINGLE);
            List<BusinessObjectLight> poolItems = bem.getPoolItems(parentPool.getId(), LIMIT);
            gridObjects.setDataProvider(buildHierarchicalDataProvider(poolItems));
            // Icon generator
            iconGenerator = new ClassNameIconGenerator(resourceFactory);
            gridObjects.addComponentHierarchyColumn(item -> {
                FormattedObjectDisplayNameSpan spnItemName = new FormattedObjectDisplayNameSpan(
                        item.getObject(), false, false, true, false);
                IconNameCellGrid iconNameCellGrid = new IconNameCellGrid(spnItemName, item.getObject().getClassName(), iconGenerator);
                iconNameCellGrid.setClassName("wrap-item-label");
                return iconNameCellGrid;                        
            });
            
            gridObjects.addItemClickListener(event -> {
                currentObject = event.getItem().getObject();
                buildObjectDetailsPanel(currentObject);
            });
            lytObjects.add(gridObjects);
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    public HierarchicalDataProvider buildHierarchicalDataProvider(List<BusinessObjectLight> objects) {
        return new AbstractBackEndHierarchicalDataProvider<WarehouseManagerNode, Void>() {
            @Override
            protected Stream<WarehouseManagerNode> fetchChildrenFromBackEnd(HierarchicalQuery<WarehouseManagerNode, Void> query) {
                WarehouseManagerNode parent = query.getParent();
                if (parent != null) {
                    BusinessObjectLight object = parent.getObject();
                    try {
                        List<BusinessObjectLight> children = bem.getObjectChildren(object.getClassName(), object.getId(), -1);
                        List<WarehouseManagerNode> theChildrenNodes = new ArrayList();
                        children.forEach(child -> {
                            theChildrenNodes.add(new WarehouseManagerNode(child, child.toString()));
                        });
                        return theChildrenNodes.stream();
                    } catch (MetadataObjectNotFoundException ex) {
                        return new ArrayList().stream();
                    } catch (BusinessObjectNotFoundException | InvalidArgumentException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                        return new ArrayList().stream();
                    }
                } else {
                    List<WarehouseManagerNode> objectNodes = new ArrayList();
                    objects.forEach(obj -> objectNodes.add(new WarehouseManagerNode(obj, obj.toString())));
                    return objectNodes.stream();
                }
            }

            @Override
            public int getChildCount(HierarchicalQuery<WarehouseManagerNode, Void> query) {
                WarehouseManagerNode parent = query.getParent();
                if (parent != null) {
                    BusinessObjectLight object = parent.getObject();
                    try {
                        return (int) bem.getObjectChildrenCount(object.getClassName(), object.getId(), null);
                    } catch (InvalidArgumentException ex) {
                         new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                        return 0;
                    }
                } else
                    return objects.size();
            }

            @Override
            public boolean hasChildren(WarehouseManagerNode node) {
                return true;
            }
        };
    }
    
    /**
     * create a new input field to warehouse in the header row
     * @param dataProvider data provider to filter
     * @return the new input field filter
     */
    private TextField createTxtFieldWarehouseName(ListDataProvider<BusinessObjectLight> dataProvider) {
        Icon iconSearch = VaadinIcon.SEARCH.create();
        iconSearch.getElement().setProperty("title", ts.getTranslatedString("module.general.label.search-by-name"));
        iconSearch.setSize("16px");

        TextField txtWarehouseName = new TextField();
        txtWarehouseName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtWarehouseName.setClassName("search");
        txtWarehouseName.setValueChangeMode(ValueChangeMode.EAGER);
        txtWarehouseName.setWidthFull();
        txtWarehouseName.setSuffixComponent(iconSearch);
        txtWarehouseName.addValueChangeListener(event -> dataProvider.addFilter(
                project -> StringUtils.containsIgnoreCase(project.getName(),
                        txtWarehouseName.getValue())));
        return txtWarehouseName;
    }
     
     /**
     * Creates the right-most layout with the options for the selected object
     * @param selectedObject the selected object in the nav tree
     */
    private void buildObjectDetailsPanel(BusinessObjectLight selectedObject) {
        try {
            lytDetailsPanel.removeAll();
            if (!selectedObject.getClassName().equals(Constants.DUMMY_ROOT)) {
                ObjectOptionsPanel pnlOptions = new ObjectOptionsPanel(selectedObject,
                        coreActionRegistry, advancedActionsRegistry, viewWidgetRegistry, explorerWidgetRegistry, mem, aem, bem, ts);
                pnlOptions.setShowViews(false);
                pnlOptions.setShowExplorers(false);
                pnlOptions.setSelectionListener((event) -> {
                    switch (event.getActionCommand()) {
                        case ObjectOptionsPanel.EVENT_ACTION_SELECTION:
                            ModuleActionParameterSet parameters = new ModuleActionParameterSet(new ModuleActionParameter<>("businessObject", selectedObject));
                            Dialog wdwObjectAction = (Dialog) ((AbstractVisualInventoryAction) event.getSource()).getVisualComponent(parameters);
                            wdwObjectAction.open();
                            break;
                    }
                });
                pnlOptions.setPropertyListener((property) -> {
                    HashMap<String, String> attributes = new HashMap<>();
                    Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                    Object lastValue =  pnlOptions.lastValue(property.getName());
                    attributes.put(property.getName(), String.valueOf(property.getValue()));
                    try {
                        bem.updateObject(selectedObject.getClassName(), selectedObject.getId(), attributes);
                        if(property.getName().equals(Constants.PROPERTY_NAME))
                            refreshObjectsGrid();
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                                AbstractNotification.NotificationType.INFO, ts).open();
                        // activity log
                        aem.createObjectActivityLogEntry(session.getUser().getUserName(), selectedObject.getClassName(),
                                selectedObject.getId(), ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT,
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
                            new ModuleActionParameter("object", selectedObject))).open();
                });
                // Header layout
                Label headerPropertySheet = new Label(selectedObject.toString());
                headerPropertySheet.setClassName("whman-properties-header");
                HorizontalLayout lytHeader = new HorizontalLayout(headerPropertySheet);
                lytHeader.setMargin(false);
                lytHeader.setPadding(false);
                // Add content to layout
                lytObjectPropertySheet = new VerticalLayout(lytHeader, btnInfo, pnlOptions.build(UI.getCurrent().getSession().getAttribute(Session.class).getUser()));
                lytObjectPropertySheet.setHeightFull();
                lytObjectPropertySheet.setVisible(true);
                lytObjectPropertySheet.setPadding(false);
                lytObjectPropertySheet.setMargin(false);
                lytObjectPropertySheet.setSpacing(false);
                lytDetailsPanel.add(lytObjectPropertySheet);
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
        
    /**
     * Creates the right-most layout with the options for the selected warehouse
     * @param selectedWarehouse the selected warehouse
     */
    private void buildWarehouseDetailPanel(BusinessObjectLight selectedWarehouse) {
        lytDetailsPanel.removeAll();
        Label headerPropertySheet = new Label(selectedWarehouse.toString());
        headerPropertySheet.setClassName("whman-properties-header");
        // Action
        Button btnInfo = new Button(this.windowMoreInformation.getDisplayName());
        btnInfo.setWidthFull();
        btnInfo.addClickListener(event -> {
            this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("object", selectedWarehouse))).open();
        });
        HorizontalLayout lytHeader = new HorizontalLayout(headerPropertySheet);
        lytHeader.setMargin(false);
        lytHeader.setPadding(false);
        // Property Sheet
        propertysheetWarehouse = new PropertySheet(ts, new ArrayList<>());
        propertysheetWarehouse.addPropertyValueChangedListener(WarehousesManagerUI.this);
        // Layout Property Sheet
        lytWarehousePropertySheet = new VerticalLayout(lytHeader, btnInfo, propertysheetWarehouse);
        lytWarehousePropertySheet.setHeightFull();
        lytWarehousePropertySheet.setMargin(false);
        lytWarehousePropertySheet.setSpacing(false);
        lytWarehousePropertySheet.setPadding(false);
        // Add content to layout   
        lytDetailsPanel.add(lytWarehousePropertySheet);
    }
    
    /**
     * Update warehouse property sheet
     * @param warehouse the warehouse to update
     */
    private void updatePropertySheet(BusinessObjectLight warehouse) {
        try {
            BusinessObject aWholeWarehouse = bem.getObject(warehouse.getClassName(), warehouse.getId());
            propertysheetWarehouse.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeWarehouse, ts, aem, mem));
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
     
    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        try {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
            bem.updateObject(currentWarehouse.getClassName(), currentWarehouse.getId(), attributes);
            if (property.getName().equals(Constants.PROPERTY_NAME)) {
                currentWarehouse.setName(String.valueOf(property.getValue()));
                refreshWarehousesGrid();
            }
            updatePropertySheet(currentWarehouse);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                    AbstractNotification.NotificationType.INFO, ts).open();
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            propertysheetWarehouse.undoLastEdit();
        }
    }  
        
    private void refreshWarehousesGrid() {
        lytWarehousesPools.removeAll();
        buildWarehousesGrid(currentRootPool);
    }
    
    private void refreshSparePoolsGrid() {
        lytSparePools.removeAll();
        createFilterSparePools(currentWarehouse);
    }
    
    private void refreshObjectsGrid() {
        lytObjects.removeAll();
        buildObjectsGrid(currentSparePool);
    }
}