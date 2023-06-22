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
package org.neotropic.kuwaiba.modules.optional.pools;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractExplorerWidget;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.ObjectDashboard;
import org.neotropic.kuwaiba.modules.core.navigation.ObjectOptionsPanel;
import org.neotropic.kuwaiba.modules.core.navigation.actions.DefaultDeleteBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.modules.optional.pools.actions.CopyBusinessObjectToPoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.pools.actions.DeletePoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.pools.actions.MoveBusinessObjectToPoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.pools.actions.NewPoolItemVisualAction;
import org.neotropic.kuwaiba.modules.optional.pools.actions.NewPoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.pools.nodes.PoolObjectNode;
import org.neotropic.kuwaiba.modules.optional.pools.visual.IconLabelCellGrid;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.tree.NavTreeGrid;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for Pools. This class manages how the pages corresponding to different functionalities are presented in a single place.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "pools", layout = PoolsLayout.class)
public class PoolsUI extends VerticalLayout implements ActionCompletedListener, HasDynamicTitle
        , AbstractUI , PropertySheet.IPropertyValueChangedListener {
    /**
     * Type of pool general purpose. These pools are not linked to any particular model
     */
    public static final int POOL_TYPE_GENERAL_PURPOSE = 1;
    @Autowired
    private TranslationService ts;
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
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * factory to build resources from data source
     */
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * the visual action to create a new pool
     */
    @Autowired
    private NewPoolVisualAction newPoolVisualAction;
    /**
     * the visual action to create a new pool item
     */
    @Autowired
    private NewPoolItemVisualAction newPoolItemVisualAction;
    /**
     * the visual action to delete a class
     */
    @Autowired
    private DeletePoolVisualAction deletePoolVisualAction;
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
     * Reference to the action that copies a business object to a pool.
     */
    @Autowired
    private CopyBusinessObjectToPoolVisualAction actCopyToPool;
    /**
     * Reference to the action that moves a business object to a pool.
     */
    @Autowired
    private MoveBusinessObjectToPoolVisualAction actMoveToPool;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private CoreActionsRegistry actionRegistry;
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
     * Reference to the action registry.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;
    /**
     * factory to build resources from data source
     */
    private Grid<PoolObjectNode> gridPools;
    /**
     * combo filter for pools grid
     */
    private ComboBox<InventoryObjectPool> cmbFilterPools;
    /**
     * Layout enclosing the pool content
     */
    private VerticalLayout lytPoolItems;
    /*
     label to show the selected pool name
     */
    private Label lblPoolNameTitle;
    /**
     * listener to new class action
     */
    private ActionCompletedListener listenerNewPoolAction;
    /**
     * listener to delete pool action
     */
    private ActionCompletedListener listenerDeletePoolAction;
    /**
     * Reference to the selected pool
     */
    private PoolObjectNode nodeSelected;
    /**
     * factory to build resources from data source
     */
    private NavTreeGrid<PoolObjectNode> gridItems;
    /**
     * Sort the content
     */
    private VerticalLayout lytLeftSide;
    private VerticalLayout lytCenter;
    private VerticalLayout lytDetails;
    private HorizontalLayout lytDetailsTitle;
    private Label lblDetailsTitle;
    /**
     * Property sheet pool
     */
    private PropertySheet propertySheetPool;
    /**
     * An icon generator for create icons
     */
    private ClassNameIconGenerator iconGenerator;
    /**
     * to keep record of the item who launch an action
     */
    private List<PoolObjectNode> actionAffectedNode;
    /**
     * Pool options
     */
    private HorizontalLayout lytPoolActions;
    private ActionButton btnNewPool;
    private Label lblPoolInfo;
    private List<PoolObjectNode> pools; 
    
    public PoolsUI() {
        super();
        setSizeFull();
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.newPoolVisualAction.unregisterListener(listenerNewPoolAction);
        this.deletePoolVisualAction.unregisterListener(listenerDeletePoolAction);
        this.newPoolItemVisualAction.unregisterListener(this);
        this.actNewMultipleObj.unregisterListener(this);
        this.actNewObjFromTemplate.unregisterListener(this);
        this.actNewObj.unregisterListener(this);
        this.actDeleteObj.unregisterListener(this);
        this.actCopyToPool.unregisterListener(this);
        this.actMoveToPool.unregisterListener(this);
    }
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            if (ev.getActionResponse() != null && (ev.getActionResponse().containsKey(ActionResponse.ActionType.ADD))) {
                if (!actionAffectedNode.isEmpty() && actionAffectedNode.get(0) != null) {
                    if (gridItems != null) {
                        if (!ev.getActionResponse().isEmpty())
                            gridItems.update(actionAffectedNode.get(0));
                        else {
                            if (gridItems.getTreeData().contains(actionAffectedNode.get(0)))
                                gridItems.getDataProvider().refreshItem(actionAffectedNode.get(0), false);
                        }
                    }
                }
            } else if (ev.getActionResponse() != null && ev.getActionResponse().containsKey(ActionResponse.ActionType.REMOVE)) {
                if(!actionAffectedNode.isEmpty() && actionAffectedNode.get(0) != null && gridItems.getTreeData().contains(actionAffectedNode.get(0)))
                    gridItems.remove(actionAffectedNode.get(0));
                
                //we must deselect the current selected node if the affecetated node is the same
                if (nodeSelected != null && actionAffectedNode != null && actionAffectedNode.get(0) != null) {
                    if (actionAffectedNode.get(0).getId().equals(nodeSelected.getId()))
                        nodeSelected = null;
                }

                lytDetails.removeAll();
            }
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
    }
    
    private void poolActionCompleted() {
        listenerNewPoolAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            updatePoolList();
            refreshComboPools();
            showActionCompledMessages(ev);
        };
        this.newPoolVisualAction.registerActionCompletedLister(listenerNewPoolAction);

        listenerDeletePoolAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            nodeSelected = null;
            lytPoolItems.setVisible(false);
            lytDetails.removeAll();
            updatePoolList();
            refreshComboPools();

            showActionCompledMessages(ev);
        };
        this.deletePoolVisualAction.registerActionCompletedLister(listenerDeletePoolAction);
    }
    
    private void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
    }

    @Override
    public void initContent() {
        setSizeFull();
        // Action affected node
        this.actionAffectedNode = new ArrayList<>();
        this.actionAffectedNode.add(null);
        // Action listeners
        this.newPoolItemVisualAction.registerActionCompletedLister(this);
        this.actNewMultipleObj.registerActionCompletedLister(this);
        this.actNewObjFromTemplate.registerActionCompletedLister(this);
        this.actNewObj.registerActionCompletedLister(this);
        this.actDeleteObj.registerActionCompletedLister(this);
        this.actCopyToPool.registerActionCompletedLister(this);
        this.actMoveToPool.registerActionCompletedLister(this);

        iconGenerator = new ClassNameIconGenerator(resourceFactory);
        
        initializePoolsGrid();
        poolActionCompleted();

        lblPoolInfo = new Label(ts.getTranslatedString("module.pools.label.no-pools"));
        lblPoolInfo.setWidthFull();
        
        btnNewPool = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O), this.newPoolVisualAction.getModuleAction().getDisplayName());
        btnNewPool.addClickListener(event -> this.newPoolVisualAction.getVisualComponent(new ModuleActionParameterSet()).open());
        btnNewPool.setHeight("32px");

        //--> Left side
        lytPoolActions = new HorizontalLayout();
        lytPoolActions.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytPoolActions.setVerticalComponentAlignment(FlexComponent.Alignment.END, btnNewPool);
        lytPoolActions.setClassName("left-action-combobox");
        lytPoolActions.setSpacing(false);
        lytPoolActions.setMargin(false);
        lytPoolActions.setPadding(false);
        createComboPools();
        
        VerticalLayout lytPoolsGrid = new VerticalLayout(lytPoolActions, gridPools);
        lytPoolsGrid.setId("pool-lyt");
        lytPoolsGrid.setMargin(false);
        lytPoolsGrid.setPadding(false);
        lytPoolsGrid.setSpacing(false);
        lytPoolsGrid.setWidthFull();
        lytPoolsGrid.setHeightFull();

        lytLeftSide = new VerticalLayout(lytPoolsGrid);
        lytLeftSide.setId("left-main-lyt");
        lytLeftSide.setSpacing(false);
        lytLeftSide.setMargin(false);
        lytLeftSide.setPadding(false);
        lytLeftSide.setWidth("25%");
        lytLeftSide.setHeightFull();
        //--> End left side
        
        // --> Center
        lytPoolItems = new VerticalLayout();
        lytPoolItems.setVisible(false);
        lytPoolItems.setMargin(false);
        lytPoolItems.setSpacing(false);
        lytPoolItems.setHeightFull();
        lytPoolItems.setWidthFull();
        
        lytCenter = new VerticalLayout(lytPoolItems);
        lytCenter.setId("center-main-lyt");
        lytCenter.setWidth("40%");
        lytCenter.setHeightFull();
        // --> End center
        
        //--> Right side
        lblDetailsTitle = new Label();
        lblDetailsTitle.setClassName("pools-details-header");
        
        lytDetailsTitle = new HorizontalLayout(lblDetailsTitle);
        lytDetailsTitle.setId("right-side-title");
        lytDetailsTitle.setPadding(false);
        lytDetailsTitle.setMargin(false);
        lytDetailsTitle.setSpacing(false);
        lytDetailsTitle.setJustifyContentMode(JustifyContentMode.START);
        lytDetailsTitle.setWidthFull();
        
        lytDetails = new VerticalLayout();
        lytDetails.setClassName("serviceman-property-sheet-details-panel");
        lytDetails.setDefaultHorizontalComponentAlignment(Alignment.START);
        lytDetails.setBoxSizing(BoxSizing.BORDER_BOX);
        lytDetails.setSpacing(false);
        lytDetails.setMargin(false);
        lytDetails.setPadding(false);
        lytDetails.setWidth("35%");
        lytDetails.setHeightFull();
        //--> end right side

        // --> Main layout
        HorizontalLayout lytMainNav = new HorizontalLayout(lytLeftSide, lytCenter, lytDetails);
        lytMainNav.setId("nav-main-lyt");
        lytMainNav.setSpacing(false);
        lytMainNav.setMargin(false);
        lytMainNav.setPadding(false);
        lytMainNav.setWidthFull();
        lytMainNav.setHeightFull();
        add(lytMainNav);
    }
    
    /**
     * Initialize the pools grid
     */
    private void initializePoolsGrid() {
        try {
            List<InventoryObjectPool> listPool = bem.getRootPools(null, POOL_TYPE_GENERAL_PURPOSE, false);
            pools = listPool.stream().map(item -> {
               PoolObjectNode pool = new PoolObjectNode(item);
               pool.setPool(true);
               return pool;
            }).collect(Collectors.toList());
            
            gridPools = new Grid<>();
            gridPools.setItems(pools);
            gridPools.addComponentColumn(pool -> new IconLabelCellGrid(pool, true, iconGenerator));
            gridPools.addComponentColumn(pool -> createActions(pool)).setTextAlign(ColumnTextAlign.END).setFlexGrow(0);
            
            gridPools.addItemClickListener(item -> {
                nodeSelected = (PoolObjectNode) item.getItem();
                loadTreeGrid(nodeSelected);
                lytPoolItems.setVisible(true);
                lytDetails.removeAll();
                lblPoolNameTitle.setText(nodeSelected.getName());
                buildDetailsPanel(nodeSelected);
            });
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private Component createActions(PoolObjectNode node) {
        HorizontalLayout lytActions = new HorizontalLayout();
        lytActions.setHeight("22px");
        lytActions.setJustifyContentMode(JustifyContentMode.END);
        lytActions.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        lytActions.setPadding(false);
        lytActions.setMargin(false);
        lytActions.setSpacing(false);

        if (node.isPool()) {
            
            Command addPoolItem = () -> lytDetails.removeAll();
            ActionButton btnNewPoolItem = new ActionButton(new Icon(VaadinIcon.PLUS_SQUARE_O)
                    , this.newPoolItemVisualAction.getModuleAction().getDisplayName());
            btnNewPoolItem.addClickListener(event -> {
                nodeSelected = node;
                actionAffectedNode.set(0, nodeSelected);

                this.newPoolItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("pool", node.getObject()),
                        new ModuleActionParameter("poolItem", addPoolItem))
                ).open();
            });

            ActionButton btnDeletePool = new ActionButton(new ActionIcon(VaadinIcon.TRASH)
                    , this.deletePoolVisualAction.getModuleAction().getDisplayName());
            btnDeletePool.addClickListener(event -> {
                actionAffectedNode.set(0, node);
                
                this.deletePoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("pool", node.getObject()))).open();
            });

            lytActions.add(btnNewPoolItem, btnDeletePool);
        }

        return lytActions;
    }
    
    private void createComboPools() {
        try {
            lytPoolActions.removeAll();
            List<InventoryObjectPool> listPool = bem.getRootPools(null, POOL_TYPE_GENERAL_PURPOSE, false);
            pools = listPool.stream().map(item -> {
               PoolObjectNode pool = new PoolObjectNode(item);
               pool.setPool(true);
               return pool;
            }).collect(Collectors.toList());
            
            InventoryObjectPool aPool = new InventoryObjectPool("", ts.getTranslatedString("module.pools.label.all-pools"), "", "", POOL_TYPE_GENERAL_PURPOSE);
            listPool.add(0, aPool);
            
            cmbFilterPools = new ComboBox<>(ts.getTranslatedString("module.pools.name"));
            cmbFilterPools.setPlaceholder(ts.getTranslatedString("module.pools.label.select-pool"));
            cmbFilterPools.setWidthFull();
            cmbFilterPools.setItems(listPool);
            cmbFilterPools.setClearButtonVisible(true);
            cmbFilterPools.setAllowCustomValue(false);
            cmbFilterPools.setItemLabelGenerator(InventoryObjectPool::getName);
            
            cmbFilterPools.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    PoolObjectNode pool = new PoolObjectNode(event.getValue());
                    gridPools.setItems(pool);
                    
                    if (event.getValue().getName().equals(ts.getTranslatedString("module.pools.label.all-pools")))
                        gridPools.setItems(pools);
                } else
                    gridPools.setItems(pools);
            });
            cmbFilterPools.setValue(aPool);
            
            if (!pools.isEmpty())
                lytPoolActions.add(cmbFilterPools, btnNewPool);
            else
                lytPoolActions.add(lblPoolInfo, btnNewPool);
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void refreshComboPools() {
        try {
            lytPoolActions.removeAll();
            InventoryObjectPool aPool = new InventoryObjectPool("", ts.getTranslatedString("module.pools.label.all-pools"), "", "", POOL_TYPE_GENERAL_PURPOSE);
            List listPool = bem.getRootPools(null, POOL_TYPE_GENERAL_PURPOSE, false);
            if (!pools.isEmpty()) {
                listPool.add(0, aPool);
                cmbFilterPools.setItems(listPool);
                lytPoolActions.add(cmbFilterPools, btnNewPool);
            } else
                lytPoolActions.add(lblPoolInfo, btnNewPool);
            
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void loadTreeGrid(PoolObjectNode aNode) {
        lytPoolItems.removeAll();
        gridItems = new NavTreeGrid<PoolObjectNode>() {
            @Override
            public List<PoolObjectNode> fetchData(PoolObjectNode node) {
                List<PoolObjectNode> childrenNode = new ArrayList<>();
                try {
                    if (node.isPool()) {
                        List<BusinessObjectLight> poolItems = bem.getPoolItems(nodeSelected.getId(), -1);
                        poolItems.forEach(item -> {
                            PoolObjectNode object = new PoolObjectNode(item);
                            childrenNode.add(object);
                        });
                    } else {
                        List<BusinessObjectLight> children = bem.getObjectChildren(node.getClassName(), node.getId(), -1);

                        children.forEach(aChild -> {
                            PoolObjectNode object = new PoolObjectNode(aChild);
                            childrenNode.add(object);
                        });
                    }
                } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException
                        | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                             ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
                return childrenNode;
            }
        };
        // we load the data
        gridItems.createDataProvider(aNode);
        gridItems.addThemeVariants(GridVariant.LUMO_COMPACT);
        gridItems.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridItems.setHeightByRows(true);
        gridItems.setPageSize(10);
        
        gridItems.addComponentHierarchyColumn(item -> new IconLabelCellGrid(item, false, iconGenerator));
        gridItems.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridItems.addItemClickListener(event -> { 
            nodeSelected = event.getItem();
            actionAffectedNode.set(0, nodeSelected);
            buildDetailsPanel(event.getItem());
        });
        
        lblPoolNameTitle = new Label();
        lblPoolNameTitle.setClassName("pools-right-header");
        HorizontalLayout lytItemsHeader = new HorizontalLayout();
        Icon icon = new Icon(VaadinIcon.FOLDER_OPEN);
        icon.setSize("16px");
        lytItemsHeader.add(icon, lblPoolNameTitle);
        lytItemsHeader.setAlignItems(Alignment.BASELINE);
        lytItemsHeader.setWidthFull();
        
        lytPoolItems.add(lytItemsHeader, gridItems);
    }
    
    /**
     * Update the grid that shows the pools
     */
    private void updatePoolList() {
        try {
            List<InventoryObjectPool> listPool = bem.getRootPools(null, POOL_TYPE_GENERAL_PURPOSE, false);
            pools = listPool.stream().map(item -> {
                PoolObjectNode pool = new PoolObjectNode(item);
                pool.setPool(true);
                return pool;
            }).collect(Collectors.toList());
            gridPools.setItems(pools);
            gridPools.getDataProvider().refreshAll();
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Creates the right-most layout with the options for the selected pool or object
     * @param selectedObject the selected pool or object
     */
    private void buildDetailsPanel(PoolObjectNode selectedObject) {
        try {
            lytDetails.removeAll();
            // Header
            lblDetailsTitle.setText(selectedObject.getName());
            
            if (!selectedObject.isPool()) {
                ObjectOptionsPanel pnlOptions = new ObjectOptionsPanel((BusinessObjectLight) selectedObject.getObject(),
                        actionRegistry, advancedActionsRegistry, viewWidgetRegistry, explorerWidgetRegistry, mem, aem, bem, ts);
                pnlOptions.setShowViews(false);
                pnlOptions.setShowExplorers(true);
                pnlOptions.setSelectionListener((event) -> {
                    switch (event.getActionCommand()) {
                        case ObjectOptionsPanel.EVENT_ACTION_SELECTION:
                            ModuleActionParameterSet parameters = new ModuleActionParameterSet(
                                    new ModuleActionParameter<>("businessObject", selectedObject.getObject()));
                            Dialog wdwObjectAction = (Dialog) ((AbstractVisualInventoryAction) event.getSource())
                                    .getVisualComponent(parameters);
                            wdwObjectAction.open();
                            break;
                        case ObjectOptionsPanel.EVENT_EXPLORER_SELECTION:
                            Dialog wdwExplorer = new Dialog(
                                    ((AbstractExplorerWidget) event.getSource())
                                            .build((BusinessObjectLight) selectedObject.getObject())
                            );
                            wdwExplorer.setHeight("90%");
                            wdwExplorer.setMinWidth("70%");
                            wdwExplorer.setResizable(true);
                            wdwExplorer.setDraggable(true);
                            wdwExplorer.setCloseOnEsc(true);
                            wdwExplorer.open();
                            break;
                    }
                });
                
                pnlOptions.setPropertyListener((property) -> {
                    Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                    Object lastValue =  pnlOptions.lastValue(property.getName());
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put(property.getName(), String.valueOf(property.getValue()));
                    try {
                        bem.updateObject(selectedObject.getClassName(), selectedObject.getId(), attributes);
                        if (property.getName().equals(Constants.PROPERTY_NAME)) {
                            nodeSelected.setName(String.valueOf(property.getValue()));
                            ((BusinessObjectLight) nodeSelected.getObject()).setName(String.valueOf(property.getValue()));
                            lblDetailsTitle.setText(String.valueOf(property.getValue()));
                        }
                        // activity log
                        aem.createObjectActivityLogEntry(session.getUser().getUserName(), selectedObject.getClassName(),
                                selectedObject.getId(), ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT,
                                property.getName(), lastValue == null ? "" : lastValue.toString(), property.getAsString(), "");
                    } catch (InventoryException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                        pnlOptions.UndoLastEdit();
                    }
                    
                    if (gridItems.getTreeData().contains(nodeSelected))
                        gridItems.getDataProvider().refreshItem(nodeSelected);
                    
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                });
                
                HorizontalLayout lytExtraActions = new HorizontalLayout();
                lytExtraActions.setWidthFull();
                
                Button btnGoToDashboard = new Button(ts.getTranslatedString("module.navigation.widgets.object-dashboard.open-to-dashboard"));
                Button btnInfo = new Button(ts.getTranslatedString("module.navigation.actions.show-more-information-button-name"));
                
                // Action go to Dashboard                
                btnGoToDashboard.addClickListener(ev -> {
                    getUI().ifPresent(ui -> {
                        ui.getSession().setAttribute(BusinessObjectLight.class, (BusinessObjectLight) selectedObject.getObject());
                        ui.getPage().open(RouteConfiguration.forRegistry(VaadinService.getCurrent().getRouter().getRegistry()).getUrl(ObjectDashboard.class), "_blank");
                    });
                });

                btnInfo.addClickListener(e -> {
                    this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("object", selectedObject.getObject()))).open();
                });
                
                lytExtraActions.addAndExpand(btnGoToDashboard, btnInfo);    

                 lytDetails.add(lytDetailsTitle, lytExtraActions, pnlOptions.build(UI.getCurrent().getSession().getAttribute(Session.class).getUser()));
                
            } else {
            
            propertySheetPool = new PropertySheet(ts, new ArrayList<>());
            propertySheetPool.addPropertyValueChangedListener(this);
            InventoryObjectPool aWholeCustomerPool = bem.getPool(selectedObject.getId());
            propertySheetPool.setItems(PropertyFactory.propertiesFromPoolWithoutClassName((InventoryObjectPool) aWholeCustomerPool, ts));
            
            lytDetails.add(lytDetailsTitle, propertySheetPool);
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        if (nodeSelected != null) {
            if (property.getName().equals(Constants.PROPERTY_NAME)) {
                aem.setPoolProperties(nodeSelected.getId(), String.valueOf(property.getValue()), nodeSelected.getDescription());
                nodeSelected.setName(String.valueOf(property.getValue()));
                lblPoolNameTitle.setText(String.valueOf(property.getValue()));
                lblDetailsTitle.setText(String.valueOf(property.getValue()));
                updatePoolList();
                refreshComboPools();
            } else if (property.getDescription().equals(Constants.PROPERTY_DESCRIPTION)) {
                aem.setPoolProperties(nodeSelected.getId(), nodeSelected.getName(), String.valueOf(property.getValue()));
                nodeSelected.setDescription(String.valueOf(property.getValue()));
            }
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                    ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                    AbstractNotification.NotificationType.INFO, ts).open();
        }
    }
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.pools.title");
    }
}