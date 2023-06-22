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
package org.neotropic.kuwaiba.modules.commercial.contractman;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
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
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
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
import org.neotropic.kuwaiba.modules.commercial.contractman.actions.CopyContractToPoolVisualAction;
import org.neotropic.kuwaiba.modules.commercial.contractman.actions.DeleteContractVisualAction;
import org.neotropic.kuwaiba.modules.commercial.contractman.actions.MoveContractToPoolVisualAction;
import org.neotropic.kuwaiba.modules.commercial.contractman.actions.NewContractVisualAction;
import org.neotropic.kuwaiba.modules.commercial.contractman.actions.RelateObjectToContractVisualAction;
import org.neotropic.kuwaiba.modules.commercial.contractman.actions.ReleaseObjectFromContractVisualAction;
import org.neotropic.kuwaiba.modules.commercial.contractman.components.PoolContractDialog;
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
 * Main for the Contract Manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "contractman", layout = ContractManagerLayout.class) 
public class ContractManagerUI extends VerticalLayout implements ActionCompletedListener,
        PropertySheet.IPropertyValueChangedListener, HasDynamicTitle, AbstractUI {
    /**
     * Reference to the Contract Manager Service
     */
    @Autowired 
    private ContractManagerService cms;
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * The visual action to delete a contract
     */
    @Autowired
    private NewContractVisualAction newContractVisualAction;
    /**
     * The visual action to delete a contract
     */
    @Autowired
    private DeleteContractVisualAction deleteContractVisualAction;
    /**
     * The visual action for pool management
     */
    @Autowired
    private PoolContractDialog poolContractDialog;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;
    /**
     * The visual action to relate business object to project
     */
    @Autowired
    private RelateObjectToContractVisualAction relateObjectToContractVisualAction;
    /**
     * The visual action to release business object from project
     */
    @Autowired
    private ReleaseObjectFromContractVisualAction releaseObjectFromContractVisualAction;
    /**
     * The visual action to copy a contract from a contract pool
     */
    @Autowired
    private CopyContractToPoolVisualAction copyContractToPoolVisualAction;
    /**
     * The visual action to copy a contract from a pool to another pool
     */
    @Autowired
    private MoveContractToPoolVisualAction moveContractToPoolVisualAction;
    /**
     * Object used to create a new contract
     */
    private ActionButton btnAddContract;
    /**
     * Object used to delete a contract
     */
    private ActionButton btnDeleteContract;
    /**
     * Object used to show more information about a contract
     */    
    private ActionButton btnInfo;
    /**
     * Button used to copy a contract preselected
     */
    private ActionButton btnCopy;
    /**
     * Button used to move a contract preselected
     */
    private ActionButton btnMove;
    /**
     * The grid with the contract list
     */
    private Grid<BusinessObjectLight> gridContracts;
    /**
     * The grid with the list objects
     */
    private Grid<BusinessObjectLight> gridObjects;
    /**
     * Object to save the pool list
     */
    private List<InventoryObjectPool> listPool;
    /**
     * Pool items limit. -1 To return all
     */
    private static final int LIMIT = -1;
    /**
     * Layout of contracts
     */
    private VerticalLayout lytContracts;
    /**
     * Layout of objects associated with a contract
     */
    private VerticalLayout lytObjects;
    /**
     * Left side layout
     */
    private VerticalLayout lytLeftSide;
    /**
     * Right action buttons layout
     */
    private HorizontalLayout lytRightActionButtons;
    /**
     * Contains the right header
     */
    private VerticalLayout lytRightHeader;
    /**
     * Filter name layout
     */
    private HorizontalLayout lytFilterName;
    /**
     * Layout of property sheet
     */
    private VerticalLayout lytPropertySheet;
    /**
     * Property sheet
     */
    private PropertySheet propertysheet;
    /**
     * Object to save the selected contract
     */
    private BusinessObjectLight currentContract;
    /**
     * Object to save the selected pool
     */
    private InventoryObjectPool currentPool;
     /**
     * Object to filter for pool name
     */
    private ComboBox<InventoryObjectPool> cmbFilterPoolName;
    /**
     * Object to add a new Pool 
     */
    private InventoryObjectPool allContracts;
    /**
     * Contract name
     */
    private Label lblContractName;
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    
    public ContractManagerUI() {
        super();
        setSizeFull();
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
            refreshContractsGrid();
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();            
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.poolContractDialog.unregisterListener(this);
        this.newContractVisualAction.unregisterListener(this);
        this.deleteContractVisualAction.unregisterListener(this);
        this.copyContractToPoolVisualAction.unregisterListener(this);
        this.moveContractToPoolVisualAction.unregisterListener(this);
        this.relateObjectToContractVisualAction.unregisterListener(this);
        this.releaseObjectFromContractVisualAction.unregisterListener(this);
    }
    
    @Override
    public void initContent() {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false); 
        this.poolContractDialog.registerActionCompletedLister(this);
        this.newContractVisualAction.registerActionCompletedLister(this);
        this.deleteContractVisualAction.registerActionCompletedLister(this);
        this.copyContractToPoolVisualAction.registerActionCompletedLister(this);
        this.moveContractToPoolVisualAction.registerActionCompletedLister(this);
        this.relateObjectToContractVisualAction.registerActionCompletedLister(this);
        this.releaseObjectFromContractVisualAction.registerActionCompletedLister(this);

        btnAddContract = new ActionButton(new Icon(VaadinIcon.PLUS_SQUARE_O), this.newContractVisualAction.getModuleAction().getDisplayName());
        btnAddContract.addClickListener(event -> {
            this.newContractVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("pool", currentPool))).open();
        });
        btnAddContract.setHeight("32px");

        ActionButton btnManagePools = new ActionButton(new ActionIcon(VaadinIcon.COG),
                ts.getTranslatedString("module.contractman.actions.pool.manage-pool.name"));
        btnManagePools.addClickListener(event -> launchPoolDialog());
        btnManagePools.setHeight("32px");
 
        // Split Layout
        splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25);
        
        // --> Left side
        buildContractsGrid(null);
        // Layout for combo box
        createComboPools();
        HorizontalLayout lytCmb = new HorizontalLayout();
        lytCmb.setClassName("left-action-combobox");
        lytCmb.setSpacing(false);
        lytCmb.setMargin(false);
        lytCmb.setPadding(false);
        lytCmb.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytCmb.add(cmbFilterPoolName, btnManagePools);
        lytCmb.setVerticalComponentAlignment(FlexComponent.Alignment.END, btnManagePools);
        // Layout for contracts grid
        lytContracts = new VerticalLayout();
        lytContracts.setClassName("bottom-grid");
        lytContracts.setHeightFull();
        lytContracts.setPadding(false);
        lytContracts.setMargin(false);
        lytContracts.setSpacing(false);
        lytContracts.add(gridContracts);
        // Main left Layout 
        lytLeftSide = new VerticalLayout();
        lytLeftSide.setClassName("left-side");
        lytLeftSide.setSizeFull();
        lytLeftSide.setMargin(false);
        lytLeftSide.setSpacing(false);
        lytLeftSide.setId("left-lyt");
        lytLeftSide.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        lytLeftSide.add(lytCmb, lytContracts);
        
        // --> Right side   
        VerticalLayout lytRightMain = new VerticalLayout();
        lytRightMain.setClassName("rigth-side");
        lytRightMain.setId("right-lyt");
        lytRightMain.setMargin(false);
        lytRightMain.setPadding(false);
        lytRightMain.setSpacing(false);
        // Layout for action buttons
        lytRightActionButtons = new HorizontalLayout();
        lytRightActionButtons.setClassName("button-toolbar");
        lytRightActionButtons.setPadding(false);
        lytRightActionButtons.setMargin(false);
        lytRightActionButtons.setSpacing(false);
        lytRightActionButtons.setId("right-actions-lyt");
        lytRightActionButtons.setJustifyContentMode(JustifyContentMode.END);
        lytRightActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        // Layout for filter name        
        lytFilterName = new HorizontalLayout();
        lytFilterName.setMargin(false);
        lytFilterName.setPadding(false);
        lytFilterName.setWidth("80%");
        // Create right control buttons
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
        lytRightHeader.add(lytRightControls);
        lytRightHeader.setVisible(false);
        //objects layout    
        lytObjects = new VerticalLayout();
        lytObjects.setClassName("grig-pool-container");
        lytObjects.setHeightFull();
        lytObjects.setWidth("50%");
        lytObjects.setMinWidth("50%");
        lytObjects.setMargin(false);
        lytObjects.setSpacing(true);
        lytObjects.setVisible(false);
        // Property Sheet
        propertysheet = new PropertySheet(ts, new ArrayList<>());
        propertysheet.addPropertyValueChangedListener(this);
        lytPropertySheet = new VerticalLayout();
        //lytPropertySheet.setClassName("script-editor");   
        lytPropertySheet.setId("lyt-property-sheet");
        lytPropertySheet.setWidthFull();
        lytPropertySheet.setBoxSizing(BoxSizing.BORDER_BOX);
        lytPropertySheet.setMargin(false);
        lytPropertySheet.setPadding(true);
        lytPropertySheet.setSpacing(false);
        lytPropertySheet.add(propertysheet);
        lytPropertySheet.setVisible(false);
        // Add content to right main layout 
        lytRightMain.add(lytRightHeader, lytPropertySheet, lytObjects);
        //Split Layout
        splitLayout.addToPrimary(lytLeftSide);
        splitLayout.addToSecondary(lytRightMain);
        add(splitLayout);
    }
    
    /**
     * Define control buttons and behavior
     */
    void createRightControlButtons() {
        Label lblContract = new Label(ts.getTranslatedString("module.contractman.contract.label"));
        lblContract.setClassName("dialog-title");
        lblContractName = new Label();
        lblContractName.setClassName("dialog-title");
        lytFilterName.add(new Html("<span>&nbsp;</span>"), lblContract, lblContractName);
        
        Command deleteContract = () -> {
            showFields(false);
        };
        btnDeleteContract = new ActionButton(new Icon(VaadinIcon.TRASH), this.deleteContractVisualAction.getModuleAction().getDisplayName());
        btnDeleteContract.setEnabled(true);
        btnDeleteContract.addClickListener(event -> {
            this.deleteContractVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("contract", currentContract),
                    new ModuleActionParameter("commandClose", deleteContract)
            )).open();
        });
        
        btnInfo = new ActionButton(new Icon(VaadinIcon.INFO_CIRCLE), this.windowMoreInformation.getDisplayName());
        btnInfo.setEnabled(true);
        btnInfo.addClickListener(event -> {
            this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("object", currentContract))).open();
        });

        Command addPool = () -> {
            buildComboBoxFilterProvider();
            refreshContractsGrid();

            new SimpleNotification(ts.getTranslatedString("module.general.messages.success")
                    , ts.getTranslatedString("module.projects.actions.pool.new-pool-success")
                    , AbstractNotification.NotificationType.INFO, ts).open();
        };
        
        btnCopy = new ActionButton(new ActionIcon(VaadinIcon.COPY),
                this.copyContractToPoolVisualAction.getModuleAction().getDisplayName());
        btnCopy.addClickListener(event -> {
            this.copyContractToPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("contract", currentContract),
                    new ModuleActionParameter("commandAddContractPool", addPool)
            )).open();
        });
        
        Command moveProject = () -> {
            currentContract = null;
            showFields(false);
        };
        btnMove = new ActionButton(new ActionIcon(VaadinIcon.PASTE),
                 this.moveContractToPoolVisualAction.getModuleAction().getDisplayName());
        btnMove.addClickListener(event -> {
            this.moveContractToPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("pool", currentPool),
                    new ModuleActionParameter("contract", currentContract),
                    new ModuleActionParameter("command", moveProject),
                    new ModuleActionParameter("commandAddContractPool", addPool)
            )).open();
        });
        
        lytRightActionButtons.add(btnDeleteContract, btnInfo, btnCopy, btnMove);
    }
    
    private void createComboPools() {
        // First filter
        cmbFilterPoolName = new ComboBox<>(ts.getTranslatedString("module.contractman.pool.header"));
        cmbFilterPoolName.setPlaceholder(ts.getTranslatedString("module.contractman.pool.label.choose-pool"));
        cmbFilterPoolName.setWidthFull();
        cmbFilterPoolName.setAllowCustomValue(false);
        cmbFilterPoolName.setClearButtonVisible(true);
        buildComboBoxFilterProvider();
        cmbFilterPoolName.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                if (event.getValue().equals(allContracts) || event.getValue() == allContracts) {
                    lytContracts.remove(gridContracts);
                    gridContracts.removeAllColumns();
                    buildContractsGrid(null);
                    currentPool = null;
                } else {
                    lytContracts.remove(gridContracts);
                    gridContracts.removeAllColumns();
                    buildContractsGrid(event.getValue());
                    currentPool = event.getValue();
                }
                lytContracts.add(gridContracts);
                lytContracts.setVisible(true);
            } else {
                gridContracts.removeAllColumns();
                lytContracts.remove(gridContracts);
                lytContracts.setVisible(false);
                currentPool = null;
            }
            showFields(false);
        });
    }
    
    /**
     * Create pools data provider
     */
    private void buildComboBoxFilterProvider() {
        try {
            // List of pools for filter
            listPool = cms.getContractPools();
            allContracts = new InventoryObjectPool("", ts.getTranslatedString("module.contractman.pool.label.all-contracts"), "", "", 0);
            listPool.add(allContracts);
            cmbFilterPoolName.setItems(listPool);
            cmbFilterPoolName.setValue(allContracts);
            // Validate pool list size
            if (listPool.isEmpty())
                btnAddContract.setEnabled(false);
            else 
                btnAddContract.setEnabled(true);
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
        
    private void buildContractsGrid(InventoryObjectPool pool) {
        try {
            ListDataProvider<BusinessObjectLight> dataProvider;
            List<BusinessObjectLight> contracts;
            gridContracts = new Grid<>();
            gridContracts.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
            gridContracts.setSelectionMode(Grid.SelectionMode.SINGLE);
            gridContracts.setHeightFull();
            
            gridContracts.addSelectionListener(event -> {
               event.getFirstSelectedItem().ifPresent(obj -> {
                   showFields(true);
                   currentContract = obj;
                   lblContractName.setText(obj.getName());
                   updatePropertySheet();
                   buildObjectsGrid(obj);
               });
            });
            
            Grid.Column<BusinessObjectLight> nameColumn = gridContracts.addColumn(TemplateRenderer.<BusinessObjectLight>of(
                    "<div>[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.className]]</font></div>")
                    .withProperty("name", BusinessObjectLight::getName)
                    .withProperty("className", BusinessObjectLight::getClassName));

            if (pool != null) {
                contracts = cms.getContractsInPool(pool.getId(), LIMIT);
                dataProvider = new ListDataProvider<>(contracts);
            } else {
                contracts = cms.getAllContracts(LIMIT, LIMIT);
                dataProvider = new ListDataProvider<>(contracts);
            }
            gridContracts.setDataProvider(dataProvider);
            // Validate contract list size
            if (contracts.isEmpty())
                labelInfoContract(nameColumn);
            else 
                createTxtFieldContractName(nameColumn, dataProvider);// Filter contract by name
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void refreshContractsGrid() {
        if (cmbFilterPoolName.getValue() != null) {
            if (cmbFilterPoolName.getValue().equals(allContracts) || cmbFilterPoolName.getValue() == allContracts) {
                lytContracts.remove(gridContracts);
                gridContracts.removeAllColumns();
                buildContractsGrid(null);
            } else {
                lytContracts.remove(gridContracts);
                gridContracts.removeAllColumns();
                buildContractsGrid(cmbFilterPoolName.getValue());
            }
            lytContracts.add(gridContracts);
        }
    }
            
    /**
     * create a new input field to contract in the header row
     * @param dataProvider data provider to filter
     */
    private void createTxtFieldContractName(Grid.Column column, ListDataProvider<BusinessObjectLight> dataProvider) {
        TextField txtContractName = new TextField();
        txtContractName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtContractName.setValueChangeMode(ValueChangeMode.EAGER);
        txtContractName.setWidthFull();
        txtContractName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH));
        // object name filter
        txtContractName.addValueChangeListener(event -> dataProvider.addFilter(
                project -> StringUtils.containsIgnoreCase(project.getName(),
                        txtContractName.getValue())));
        // action button layout
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setClassName("left-action-buttons");
        lytActionButtons.setSpacing(false);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(txtContractName, btnAddContract);

        HeaderRow filterRow = gridContracts.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }
    
    /**
     * Adds a Label to show information and button for add contract  
     */
    private void labelInfoContract(Grid.Column column) {
        Label lblInfo = new Label();
        lblInfo.setVisible(true);
        lblInfo.setText(ts.getTranslatedString("module.contractman.pool.label.no-associated-contracts"));
        lblInfo.setWidthFull();
         // action button layout
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setClassName("left-action-buttons");
        lytActionButtons.setSpacing(false);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(lblInfo, btnAddContract);
        
        HeaderRow filterRow = gridContracts.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }
    
    private void updatePropertySheet() {
        try {
            if (currentContract != null) {
                BusinessObject aWholeContract = cms.getContract(currentContract.getClassName(), currentContract.getId());
                propertysheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeContract, ts, aem, mem));
            }
        }catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
   
    /**
     * Update properties for contracts.
     * @param property the property to update
     */
    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        try {
            if (currentContract != null) {
                Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                cms.updateContract(currentContract.getClassName(), currentContract.getId(), attributes, session.getUser().getUserName());
                if (property.getName().equals(Constants.PROPERTY_NAME)) {
                    currentContract.setName(String.valueOf(property.getValue()));
                    refreshContractsGrid();
                }
                updatePropertySheet();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException
                | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            propertysheet.undoLastEdit();
        }
    }
    
    private void launchPoolDialog() {
        Command commandAddContractsPool = () -> {
            buildComboBoxFilterProvider();
            refreshContractsGrid();
        };
        
        Command commandDeleteContractsPool = () -> {
            buildComboBoxFilterProvider();
            refreshContractsGrid();
        };
        
        this.poolContractDialog.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter("commandAddContractsPool", commandAddContractsPool),
                new ModuleActionParameter("commandDeleteContractsPool", commandDeleteContractsPool)
        )).open();
    }
    
    /**
     * Shows/Hides the labels and buttons in the header, also the property sheet.
     */
    private void showFields(boolean show) {
        lytRightHeader.setVisible(show);
        lytPropertySheet.setVisible(show);
        lytObjects.setVisible(show);
    }
    
    private void buildObjectsGrid(BusinessObjectLight contract) {
        Label lblHeader = new Label(ts.getTranslatedString("module.general.label.related-resources"));
        lblHeader.setClassName("dialog-title");
        lblHeader.setWidthFull();
        
        Label lblInfoObject = new Label(ts.getTranslatedString("module.general.label.no-related-resources"));
        lblInfoObject.setWidthFull();
        
        ListDataProvider<BusinessObjectLight> dataProviderObjects;
        List<BusinessObjectLight> listObjects;
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
        if (contract != null) {
            try {
                VerticalLayout lytContent = new VerticalLayout(lblHeader);
                lytContent.setMargin(false);
                lytContent.setPadding(false);
                lytContent.setSizeFull();
                
                listObjects = cms.getContractResources(contract.getClassName(), contract.getId());
                if (!listObjects.isEmpty()) {
                    dataProviderObjects = new ListDataProvider<>(listObjects);
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
    
    private HorizontalLayout createObjectAction(BusinessObjectLight object) {        
        Command releaseObject = () -> {
            buildObjectsGrid(currentContract);
        };
        ActionButton btnRelease = new ActionButton(new ActionIcon(VaadinIcon.UNLINK)
                , this.releaseObjectFromContractVisualAction.getModuleAction().getDisplayName());
        btnRelease.addClickListener(event -> {
            this.releaseObjectFromContractVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("contract", currentContract),
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
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.contractman.title");
    }
}