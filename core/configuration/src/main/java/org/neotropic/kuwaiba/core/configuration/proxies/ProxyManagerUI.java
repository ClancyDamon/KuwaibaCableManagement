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
package org.neotropic.kuwaiba.core.configuration.proxies;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
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
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryProxy;
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
import org.neotropic.kuwaiba.core.configuration.ConfigurationManagerLayout;
import org.neotropic.kuwaiba.core.configuration.proxies.actions.AssociateObjectToProxyVisualAction;
import org.neotropic.kuwaiba.core.configuration.proxies.actions.AssociateProjectVisualAction;
import org.neotropic.kuwaiba.core.configuration.proxies.actions.CopyProxyToPoolVisualAction;
import org.neotropic.kuwaiba.core.configuration.proxies.actions.DeleteProxyVisualAction;
import org.neotropic.kuwaiba.core.configuration.proxies.actions.MoveProxyToPoolVisualAction;
import org.neotropic.kuwaiba.core.configuration.proxies.actions.NewProxyVisualAction;
import org.neotropic.kuwaiba.core.configuration.proxies.actions.ReleaseObjectVisualAction;
import org.neotropic.kuwaiba.core.configuration.proxies.actions.ReleaseProjectVisualAction;
import org.neotropic.kuwaiba.core.configuration.proxies.components.PoolProxiesDialog;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.ObjectDashboard;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Proxy Manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "configuration/proxies", layout = ConfigurationManagerLayout.class)
public class ProxyManagerUI extends VerticalLayout implements ActionCompletedListener, 
        PropertySheet.IPropertyValueChangedListener, HasDynamicTitle, AbstractUI {
    /**
     * Reference o the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager .
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager .
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Proxy Manager Service
     */
    @Autowired
    private ProxyManagerService pms;    
    /**
     * The visual action to create a new proxy
     */
    @Autowired
    private NewProxyVisualAction newProxyVisualAction;    
    /**
     * The visual action to create a new proxy pool
     */
    @Autowired
    private PoolProxiesDialog poolProxiesDialog;    
    /**
     * The visual action to delete a proxy preselected
     */
    @Autowired
    private DeleteProxyVisualAction deleteProxyVisualAction;
    /**
     * The visual action to associate projects to a proxy
     */
    @Autowired
    private AssociateProjectVisualAction associateProjectVisualAction;
    /**
     * The visual action to release a project from a proxy
     */
    @Autowired
    private ReleaseProjectVisualAction releaseProjectVisualAction;
    /**
     * The visual action to associate objects to a proxy
     */
    @Autowired
    private AssociateObjectToProxyVisualAction associateObjectToProxyVisualAction;
    /**
     * The visual action to release an object from a proxy
     */
    @Autowired
    private ReleaseObjectVisualAction releaseObjectVisualAction;
    /**
     * The visual action to copy a proxy from a pool to another pool
     */
    @Autowired 
    private CopyProxyToPoolVisualAction copyProxyToPoolVisualAction;
    /**
     * The visual action to move a proxy from a pool to another pool 
     */
    @Autowired
    private MoveProxyToPoolVisualAction moveProxyToPoolVisualAction;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;
    /**
     * Object used to create a new proxy
     */
    private ActionButton btnAddProxy;
    /**
     * Object used to delete a proxy preselected
     */
    private ActionButton btnDeleteProxy;    
    /**
     * Object used to open window to associate object to proxy
     */
    private ActionButton btnAssociate;
    /**
     * Object used to move a proxy preselected
     */
    private ActionButton btnMove;
    /**
     * Object used to copy a proxy preselected
     */
    private ActionButton btnCopy;
    /**
     * Object to save the pool list
     */
    private List<InventoryObjectPool> listPools;
    /**
     * Object to add a new Pool and get all proxies
     */
    private InventoryObjectPool allProxies;
    /**
     * Object to filter for pool name
     */
    private ComboBox<InventoryObjectPool> cmbFilterPoolName;
    /**
     * Object to save the selected pool
     */
    private InventoryObjectPool currentPool;
    /**
     * Object to save the selected proxy
     */
    private BusinessObjectLight currentProxy;    
    /**
     * Left Main layout
     */
    private VerticalLayout lytLeftSide;
    /**
     * Layout for proxies grid
     */
    private VerticalLayout lytProxies;
    /**
     * Layout of projects/objects associated with a proxy
     */
    private VerticalLayout lytProjects;
    private VerticalLayout lytObjects;
    /**
     * Layout of property sheet
     */
    private VerticalLayout lytPropertySheet;
    /**
     * Property sheet
     */
    private PropertySheet propertysheet;
    /**
     * Contains proxy selected name
     */
    private HorizontalLayout lytFilterName;
    /**
     * Contains the right header
     */
    private VerticalLayout lytRightHeader;
    /**
     * Layout for actions of proxy definition
     */
    private HorizontalLayout lytRightActionButtons; 
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    /**
     * Proxy selected name
     */
    private Label lblProxyName;
    /**
     * Proxies data provider
     */
    private ListDataProvider<InventoryProxy> dataProvider;
    /**
     * The grid with the list proxies
     */
    private Grid<InventoryProxy> gridProxies;
    /**
     * The grid with the list projects
     */
    private Grid<BusinessObjectLight> gridProjects;
    /**
     * The grid with the list objects
     */
    private Grid<BusinessObjectLight> gridObjects;
    /**
     * Relationship Name
     */
    private final String RELATIONSHIP_NAME = "hasProxy";

    public ProxyManagerUI() {
        super();
        setSizeFull();
    }
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
            refreshProxiesGrid();
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.associateObjectToProxyVisualAction.unregisterListener(this);
        this.associateProjectVisualAction.unregisterListener(this);
        this.releaseProjectVisualAction.unregisterListener(this);
        this.releaseObjectVisualAction.unregisterListener(this);
        this.copyProxyToPoolVisualAction.unregisterListener(this);
        this.moveProxyToPoolVisualAction.unregisterListener(this);
        this.deleteProxyVisualAction.unregisterListener(this);
        this.newProxyVisualAction.unregisterListener(this);
        this.poolProxiesDialog.unregisterListener(this);
    }

    @Override
    public void initContent() {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);
        
        this.associateObjectToProxyVisualAction.registerActionCompletedLister(this);
        this.associateProjectVisualAction.registerActionCompletedLister(this);
        this.releaseProjectVisualAction.registerActionCompletedLister(this);
        this.releaseObjectVisualAction.registerActionCompletedLister(this);
        this.copyProxyToPoolVisualAction.registerActionCompletedLister(this);
        this.moveProxyToPoolVisualAction.registerActionCompletedLister(this);
        this.deleteProxyVisualAction.registerActionCompletedLister(this);
        this.newProxyVisualAction.registerActionCompletedLister(this);
        this.poolProxiesDialog.registerActionCompletedLister(this);
        
        btnAddProxy = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                 this.newProxyVisualAction.getModuleAction().getDisplayName());
        btnAddProxy.addClickListener(event -> {
            this.newProxyVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("pool", currentPool)
            )).open();
        });
        btnAddProxy.setHeight("32px");
        
        ActionButton btnManagePools = new ActionButton(new ActionIcon(VaadinIcon.COG),
                ts.getTranslatedString("module.configman.actions.manage-pool.name"));
        btnManagePools.addClickListener(event -> launchPoolDialog());
        btnManagePools.setHeight("32px");
         
        // Split Layout
        splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25);
        //--left side   
        // Main left Layout 
        lytLeftSide = new VerticalLayout();
        lytLeftSide.setClassName("left-side");
        lytLeftSide.setMargin(false);
        lytLeftSide.setSpacing(false);
        lytLeftSide.setPadding(false);
        lytLeftSide.setId("main-lyt");
        lytLeftSide.setHeightFull();
        lytLeftSide.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        //top grid
        VerticalLayout lytClasses = new VerticalLayout();
        lytClasses.setClassName("top-grid");
        lytClasses.setSpacing(true);
        lytClasses.setMargin(false);
        lytClasses.setPadding(false);
        createComboPools();
        HorizontalLayout lytCmb = new HorizontalLayout();
        lytCmb.setClassName("left-action-combobox");
        lytCmb.setSpacing(false);
        lytCmb.setMargin(false);
        lytCmb.setPadding(false);
        lytCmb.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytCmb.add(cmbFilterPoolName, btnManagePools);
        lytCmb.setVerticalComponentAlignment(FlexComponent.Alignment.END,
                btnManagePools);
        // bottom grid  
        buildProxiesGrid();
        // Layout for proxies grid
        lytProxies = new VerticalLayout();
        lytProxies.setClassName("bottom-grid");
        lytProxies.setHeightFull();
        lytProxies.setSpacing(false);
        lytProxies.setMargin(false);
        lytProxies.setPadding(false);
        lytProxies.add(gridProxies);
        // Main left Layout
        lytLeftSide.add(lytCmb, lytProxies);
        //end left side
        splitLayout.addToPrimary(lytLeftSide);
        
        //--Right side 
        VerticalLayout lytRightMain = new VerticalLayout();
        lytRightMain.setClassName("right-side");
        lytRightMain.setMargin(false);
        lytRightMain.setPadding(false);
        lytRightMain.setSpacing(true);
        // Layout for action buttons
        lytRightActionButtons = new HorizontalLayout();
        lytRightActionButtons.setClassName("button-toolbar");
        lytRightActionButtons.setPadding(false);
        lytRightActionButtons.setMargin(false);
        lytRightActionButtons.setSpacing(false);
        lytRightActionButtons.setJustifyContentMode(JustifyContentMode.END);
        lytRightActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        // Layout for filter
        lytFilterName = new HorizontalLayout();
        lytFilterName.setMargin(false);
        lytFilterName.setPadding(false);
        lytFilterName.setWidth("80%");
        // Right control buttons
        createRightControlButtons();
        HorizontalLayout lytRightControls = new HorizontalLayout();
        lytRightControls.setClassName("script-control");
        lytRightControls.setPadding(false);
        lytRightControls.setMargin(false);
        lytRightControls.setSpacing(false);
        lytRightControls.setWidthFull();
        lytRightControls.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytRightControls.add(lytFilterName, lytRightActionButtons);
        
        lytRightHeader = new VerticalLayout();
        lytRightHeader.setClassName("header-script-control");
        lytRightHeader.setPadding(false);
        lytRightHeader.setMargin(false);
        lytRightHeader.setSpacing(false);
        lytRightHeader.setSpacing(false);
        lytRightHeader.add(lytRightControls);
        lytRightHeader.setVisible(false);
        //projects layout
        lytProjects = new VerticalLayout();
        lytProjects.setClassName("grig-pool-container");
        lytProjects.setHeightFull();
        lytProjects.setMargin(false);
        lytProjects.setSpacing(true);
        lytProjects.setPadding(false);
        lytProjects.setVisible(false);
        //objects layout    
        lytObjects = new VerticalLayout();
        lytObjects.setClassName("grig-pool-container");
        lytObjects.setHeightFull();
        lytObjects.setMargin(false);
        lytObjects.setSpacing(true);
        lytObjects.setPadding(false);
        lytObjects.setVisible(false);
        // Layout for relationships
        HorizontalLayout lytRelationships = new HorizontalLayout();
        lytRelationships.setHeight("50%");
        lytRelationships.setWidthFull();
        lytRelationships.setMargin(false);
        lytRelationships.setPadding(false);
        lytRelationships.add(lytObjects, lytProjects);
        //Property Sheet
        propertysheet = new PropertySheet(ts, new ArrayList<>());
        propertysheet.addPropertyValueChangedListener(this);
        lytPropertySheet = new VerticalLayout();
        lytPropertySheet.setId("lyt-property-sheet");
        lytPropertySheet.setMinHeight("15%");
        lytPropertySheet.setMargin(false);
        lytPropertySheet.setPadding(false);
        lytPropertySheet.setSpacing(true);
        lytPropertySheet.add(propertysheet);
        lytPropertySheet.setVisible(false);
        // Add to right layout
        lytRightMain.add(lytRightHeader, lytPropertySheet, lytRelationships);        
        splitLayout.addToSecondary(lytRightMain);
        add(splitLayout);
    }
    
    private void createComboPools() {               
        // First filter
        cmbFilterPoolName = new ComboBox<>(ts.getTranslatedString("module.configman.proxies.label.pool"));
        cmbFilterPoolName.setPlaceholder(ts.getTranslatedString("module.configman.proxies.label.choose-pool"));
        cmbFilterPoolName.setWidthFull();
        cmbFilterPoolName.setAllowCustomValue(false);
        cmbFilterPoolName.setClearButtonVisible(true);
        buildComboBoxProvider();
        
        cmbFilterPoolName.addValueChangeListener(event -> {
            lytProxies.remove(gridProxies);
            gridProxies.removeAllColumns();
            if (event.getValue() != null) {
                if (event.getValue().equals(allProxies) || event.getValue() == allProxies) 
                    currentPool = null;
                else
                    currentPool = event.getValue();
                buildProxiesGrid();
                lytProxies.add(gridProxies);
                showFields(true);
            } else {
                currentPool = null;
                showFields(false);
            }
        });
    }
    
    /**
     * create proxies data provider
     */
    private void buildComboBoxProvider(){
         // List of pools for filter
        listPools = pms.getProxyPools();
        
        // Add new pool for get all proxies
        allProxies = new InventoryObjectPool("", ts.getTranslatedString("module.configman.proxies.label.all-proxies"), "", "", 0);
        listPools.add(allProxies);
        cmbFilterPoolName.setItems(listPools);
        cmbFilterPoolName.setValue(allProxies);
        
         // Validate pool list size
        if (listPools == null || listPools.isEmpty())
            btnAddProxy.setEnabled(false);
        else
            btnAddProxy.setEnabled(true);
    }
    
    private void createRightControlButtons() {
        lblProxyName = new Label();
        lblProxyName.setClassName("dialog-title");
        Label lblValidator = new Label(ts.getTranslatedString("module.configman.proxies.label.proxy"));
        lblValidator.setClassName("dialog-title");
        lytFilterName.add(new Html("<span>&nbsp;</span>"), lblValidator, lblProxyName);

        Command addPool = () -> {
            buildComboBoxProvider();
            refreshProxiesGrid();
            
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success")
                    , ts.getTranslatedString("module.configman.proxies.actions.new-pool.succes")
                    , AbstractNotification.NotificationType.INFO, ts).open();
        };
        
        Command moveProxy = () -> {
            currentProxy = null;
            showFields(false);
        };
        btnMove = new ActionButton(new ActionIcon(VaadinIcon.PASTE),
                 this.moveProxyToPoolVisualAction.getModuleAction().getDisplayName());
        btnMove.addClickListener(event -> {
            this.moveProxyToPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("pool", currentPool),
                    new ModuleActionParameter("proxy", currentProxy),
                    new ModuleActionParameter("command", moveProxy),
                    new ModuleActionParameter("commandAddProxyPool", addPool)
            )).open();
        });
        
        btnCopy = new ActionButton(new ActionIcon(VaadinIcon.COPY),
                 this.copyProxyToPoolVisualAction.getModuleAction().getDisplayName());
        btnCopy.addClickListener(event -> {
            this.copyProxyToPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("pool", currentPool),
                    new ModuleActionParameter("proxy", currentProxy),
                    new ModuleActionParameter("commandAddProxyPool", addPool)
            )).open();
        });
        
        Command deleteProxy = () -> {
            currentProxy = null;
            showFields(false);
        };
        btnDeleteProxy = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                 this.deleteProxyVisualAction.getModuleAction().getDisplayName());
        btnDeleteProxy.addClickListener(event -> {
            this.deleteProxyVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("proxy", currentProxy),
                    new ModuleActionParameter("commandClose", deleteProxy)
            )).open();
        });
      
        Command associated = () -> {
            buildProjectsGrid(currentProxy);
        };
        btnAssociate = new ActionButton(new ActionIcon(VaadinIcon.EXCHANGE),
                 this.associateProjectVisualAction.getModuleAction().getDisplayName());
        btnAssociate.addClickListener(event -> {
            this.associateProjectVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("proxy", currentProxy),
                    new ModuleActionParameter("commandClose", associated))).open();
        });
       
        lytRightActionButtons.add(btnDeleteProxy, btnAssociate, btnCopy, btnMove);
    }
    
    /**
     * create left main grid
     */
    private void buildProxiesGrid() {
        gridProxies = new Grid<>();
        gridProxies.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        gridProxies.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridProxies.setHeightFull();

        gridProxies.addItemClickListener(event -> {
            currentProxy = event.getItem();
            if (currentProxy != null) {
                lblProxyName.setText(currentProxy.getName());
                updatePropertySheet();
                buildProjectsGrid(currentProxy);
                buildObjectsGrid(currentProxy);
                showFields(true);
            }
        });

        Grid.Column<InventoryProxy> nameColumn = gridProxies.addColumn(TemplateRenderer.<InventoryProxy>of(
                "<div>[[item.name]]</div>")
                .withProperty("name", InventoryProxy::getName))
                .setKey(ts.getTranslatedString("module.general.labels.name"));
        
        List<InventoryProxy> proxies;
        try {
            if (currentPool != null) {
                proxies = pms.getProxiesInPool(currentPool.getId());
                dataProvider = new ListDataProvider<>(proxies);
            } else {
                proxies = pms.getAllProxies();
                dataProvider = new ListDataProvider<>(proxies);
            }
            // Validate proxy list size
            if (proxies.isEmpty())
                labelInfoProxy(nameColumn);
            else
                createTxtFieldProxyName(nameColumn);// Filter proxy by name
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        gridProxies.setDataProvider(dataProvider);
    }
   
    /*
     * create a new input field to proxies in the header row
     * @param dataProvider data provider to filter
     * @return the new input field filter
     */
    private void createTxtFieldProxyName(Grid.Column column) {
        TextField txtProxyName = new TextField();
        txtProxyName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtProxyName.setValueChangeMode(ValueChangeMode.EAGER);
        txtProxyName.setWidthFull();
        txtProxyName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH,
                ts.getTranslatedString("module.configman.label.filter-configuration-variable")));
        // object name filter
        txtProxyName.addValueChangeListener(event -> dataProvider.addFilter(
                proxy -> StringUtils.containsIgnoreCase(proxy.getName(),
                        txtProxyName.getValue())));

        // action button layout
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setClassName("left-action-buttons");
        lytActionButtons.setSpacing(false);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(txtProxyName, btnAddProxy);

        HeaderRow filterRow = gridProxies.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);        
    }
    
    /**
     * Adds a Label to show information and button for add proxy  
     */
    private void labelInfoProxy(Grid.Column column) {
        Label lblInfo = new Label();
        lblInfo.setVisible(true);
        lblInfo.setText(ts.getTranslatedString("module.configman.proxies.label.no-proxies"));
        lblInfo.setWidthFull();
         // action button layout
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setClassName("left-action-buttons");
        lytActionButtons.setSpacing(false);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(lblInfo, btnAddProxy);
        
        HeaderRow filterRow = gridProxies.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }
    
    
    private void refreshProxiesGrid() {
        if (cmbFilterPoolName.getValue() != null) {
            lytProxies.remove(gridProxies);
            gridProxies.removeAllColumns();
            buildProxiesGrid();
            lytProxies.add(gridProxies);
        }
    }
        
    private void buildProjectsGrid(BusinessObjectLight proxy) {
        Label lblHeader = new Label(ts.getTranslatedString("module.general.label.related-projects"));
        lblHeader.setClassName("dialog-title");
        lblHeader.setWidthFull();
        
        Label lblInfoProject = new Label(ts.getTranslatedString("module.general.label.no-related-projects"));
        lblInfoProject.setWidthFull();
        
        gridProjects = new Grid<>();
        gridProjects.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        gridProjects.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridProjects.setHeightFull();
        gridProjects.addColumn(TemplateRenderer.<BusinessObjectLight>of(
                "<div>[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.className]]</font></div>")
                .withProperty("name", BusinessObjectLight::getName)
                .withProperty("className", BusinessObjectLight::getClassName));
        gridProjects.addComponentColumn(object -> createActionReleaseProject(object))
                .setTextAlign(ColumnTextAlign.END).setFlexGrow(0).setWidth("50px");
        lytProjects.removeAll();
        
        if (proxy != null) {
            try {
                VerticalLayout lytContent = new VerticalLayout(lblHeader);
                lytContent.setMargin(false);
                lytContent.setPadding(false);
                lytContent.setSizeFull();
                
                List<BusinessObjectLight> listObjects = bem.getSpecialAttribute(proxy.getClassName(), proxy.getId(), RELATIONSHIP_NAME);
                List<BusinessObjectLight> listProjects = new ArrayList<>(); 
                for (BusinessObjectLight object : listObjects) {
                    if (mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, object.getClassName()))
                        listProjects.add(object);
                }
                if (!listProjects.isEmpty()) {
                    ListDataProvider<BusinessObjectLight> dataProviderProjects = new ListDataProvider<>(listProjects);
                    gridProjects.setDataProvider(dataProviderProjects);
                    lytContent.add(gridProjects);
                } else
                    lytContent.add(lblInfoProject);
                
                lytProjects.add(lytContent);
            } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }
    
    private void buildObjectsGrid(BusinessObjectLight proxy) {
        Label lblHeader = new Label(ts.getTranslatedString("module.general.label.related-resources"));
        lblHeader.setClassName("dialog-title");
        lblHeader.setWidthFull();
        
        Label lblInfoObject = new Label(ts.getTranslatedString("module.general.label.no-related-resources"));
        lblInfoObject.setWidthFull();
        
        gridObjects = new Grid<>();
        gridObjects.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        gridObjects.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridObjects.setHeightFull();
        gridObjects.addColumn(TemplateRenderer.<BusinessObjectLight>of(
                "<div>[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.className]]</font></div>")
                .withProperty("name", BusinessObjectLight::getName)
                .withProperty("className", BusinessObjectLight::getClassName));
        gridObjects.addComponentColumn(object -> createObjectAction(object))
                .setTextAlign(ColumnTextAlign.END).setFlexGrow(0).setWidth("100px");
        lytObjects.removeAll();
        
        if (proxy != null) {
            try {
                VerticalLayout lytContent = new VerticalLayout(lblHeader);
                lytContent.setMargin(false);
                lytContent.setPadding(false);
                lytContent.setSizeFull();
                
                List<BusinessObjectLight> listObjects = bem.getSpecialAttribute(proxy.getClassName(), proxy.getId(), RELATIONSHIP_NAME);
                List<BusinessObjectLight> newListObjects = new ArrayList<>(); 
                for (BusinessObjectLight object : listObjects) {
                    if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, object.getClassName()))
                        newListObjects.add(object);
                }
                if (!newListObjects.isEmpty()) {
                    ListDataProvider<BusinessObjectLight>  dataProviderObjects = new ListDataProvider<>(newListObjects);
                    gridObjects.setDataProvider(dataProviderObjects);
                    lytContent.add(gridObjects);
                } else
                    lytContent.add(lblInfoObject);
                
                lytObjects.add(lytContent);
            } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }
            
    private HorizontalLayout createActionReleaseProject(BusinessObjectLight project) {        
        Command releaseProject = () -> {
            buildProjectsGrid(currentProxy);
        };
        ActionButton btnRelease = new ActionButton(new ActionIcon(VaadinIcon.UNLINK),
                 this.releaseProjectVisualAction.getModuleAction().getDisplayName());
        btnRelease.addClickListener(event -> {
            this.releaseProjectVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("proxy", currentProxy),
                    new ModuleActionParameter("project", project),
                    new ModuleActionParameter("commandClose", releaseProject))).open();
        });
        HorizontalLayout lytAction = new HorizontalLayout(btnRelease);
        lytAction.setSizeFull();
        return lytAction;
    }
    
    private HorizontalLayout createObjectAction(BusinessObjectLight object) {        
        Command releaseObject = () -> {
            buildObjectsGrid(currentProxy);
        };
        ActionButton btnRelease = new ActionButton(new ActionIcon(VaadinIcon.UNLINK)
                , this.releaseObjectVisualAction.getModuleAction().getDisplayName());
        btnRelease.addClickListener(event -> {
            this.releaseObjectVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("proxy", currentProxy),
                    new ModuleActionParameter("businessObject", object),
                    new ModuleActionParameter("commandClose", releaseObject))).open();
        });        
        
        ActionButton btnGoToDashboard = new ActionButton(new ActionIcon(VaadinIcon.ARROW_FORWARD),
                ts.getTranslatedString("module.navigation.widgets.object-dashboard.open-to-dashboard"));
        btnGoToDashboard.addClickListener(event -> {
            getUI().ifPresent(ui -> {
                ui.getSession().setAttribute(BusinessObjectLight.class, (BusinessObjectLight) object);
                ui.getPage().open(RouteConfiguration.forRegistry(VaadinService.getCurrent().getRouter().getRegistry()).getUrl(ObjectDashboard.class), "_blank");
            });
        });
        
        ActionButton btnShowInfo = new ActionButton(new ActionIcon(VaadinIcon.INFO_CIRCLE)
                ,ts.getTranslatedString("module.navigation.actions.show-more-information-button-name"));
        btnShowInfo.addClickListener(event -> {
            this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("object", object))).open();
        });
               
        HorizontalLayout lytAction = new HorizontalLayout(btnGoToDashboard, btnShowInfo, btnRelease);
        lytAction.setSpacing(false);
        lytAction.setSizeFull();
        return lytAction;
    }

    private void launchPoolDialog() {
        Command commandAddVariablesPool = () -> {
            buildComboBoxProvider();
            refreshProxiesGrid();
        };
        Command commandDeleteVariablesPool = () -> {
            buildComboBoxProvider();
            refreshProxiesGrid();
            showFields(false);
        };

        this.poolProxiesDialog.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter("commandAddProxyPool", commandAddVariablesPool),
                new ModuleActionParameter("commandDeleteProxyPool", commandDeleteVariablesPool)
        )).open();
    }
            
    /**
     * Shows/Hides the labels and buttons in the header of the filter editor
     */
    private void showFields(boolean show) {
        lytRightHeader.setVisible(show);
        lytPropertySheet.setVisible(show);
        lytProjects.setVisible(show);
        lytObjects.setVisible(show);
    }
  
    private void updatePropertySheet() {
        if (currentProxy != null) {
            try {
                BusinessObject aWholeProxy = bem.getObject(currentProxy.getClassName(), currentProxy.getId());
                propertysheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeProxy, ts, aem, mem));
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            } catch (InventoryException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }
    
    /**
     * Update properties for proxies.
     * @param property the property to update
     */
    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        try {
            if (currentProxy != null) {
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                bem.updateObject(currentProxy.getClassName(), currentProxy.getId(), attributes);
                if (property.getName().equals(Constants.PROPERTY_NAME)) {
                    currentProxy.setName(String.valueOf(property.getValue()));
                    refreshProxiesGrid();
                }
                updatePropertySheet();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success")
                        , ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            propertysheet.undoLastEdit();
        }
    }
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.configman.proxies.title");
    }
}