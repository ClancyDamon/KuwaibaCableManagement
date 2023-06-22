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
package org.neotropic.kuwaiba.core.configuration.variables;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ConfigurationVariable;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.configuration.ConfigurationManagerLayout;
import org.neotropic.kuwaiba.core.configuration.variables.actions.DeleteConfigurationVariableVisualAction;
import org.neotropic.kuwaiba.core.configuration.variables.actions.NewConfigurationVariableVisualAction;
import org.neotropic.kuwaiba.core.configuration.variables.components.PoolConfigurationDialog;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Configuration Variables Manager module. This class manages how
 * the pages corresponding to different functionalities are presented in a single place.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "configuration/variables", layout = ConfigurationManagerLayout.class)
public class ConfigurationVariablesUI extends VerticalLayout implements ActionCompletedListener,
        PropertySheet.IPropertyValueChangedListener, HasDynamicTitle, AbstractUI {
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
     * The visual action to create a new configuration variable
     */
    @Autowired
    private NewConfigurationVariableVisualAction newConfigurationVariableVisualAction;
    /**
     * The visual action to delete a configuration variable
     */
    @Autowired
    private DeleteConfigurationVariableVisualAction deleteConfigurationVariableVisualAction;
    /**
     * The visual action for pool management
     */
    @Autowired
    private PoolConfigurationDialog poolConfigurationDialog;
    /**
     * Object to create a new configuration variable
     */
    private ActionButton btnAddConfigurationVariable;

    /**
     * Object to delete the selected configuration variable
     */
    private ActionButton btnDeleteConfigurationVariable;
    /**
     * Object to save the pool list
     */
    private List<InventoryObjectPool> listPools;
    /**
     * The grid with the configuration variables
     */
    private Grid<ConfigurationVariable> grdVariables;
    /**
     * Object to save the selected configuration variables pool
     */
    private InventoryObjectPool currentConfigVariablesPool;
    /**
     * Object to save the selected configuration variable
     */
    private ConfigurationVariable currentConfigVariable;
    /**
     * Object to filter for pool name
     */
    private ComboBox<InventoryObjectPool> cmbFilterPoolName;
    /**
     * Object to add a new Pool to filter
     */
    private InventoryObjectPool allVariables;
    /**
     * Layout of property sheet
     */
    private VerticalLayout lytPropertySheet;
    /**
     * Property sheet
     */
    private PropertySheet propertysheet;
    /**
     * Main layout
     */
    private VerticalLayout lytLeftSide;
    /**
     * Layout for configuration variables grid
     */
    private VerticalLayout lytVariables;
    /**
     * Contains variable selected name
     */
    private HorizontalLayout lytFilterName;
    /**
     * Layout for actions of configuration variables
     */
    private HorizontalLayout lytRightActionButtons;
    /**
     * Contains the script editor header
     */
    private VerticalLayout lytScriptHeader;
    /**
     * Variable selected name
     */
    private Label lblVariableName;
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    /**
     * main grid data provider
     */
    private ListDataProvider<ConfigurationVariable> dataProvider;

    public ConfigurationVariablesUI() {
        super();
        setSizeFull();
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
            refreshVariablesGrid();
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.newConfigurationVariableVisualAction.unregisterListener(this);
        this.deleteConfigurationVariableVisualAction.unregisterListener(this);
        this.poolConfigurationDialog.unregisterListener(this);
    }

    @Override
    public void initContent() {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);
        this.newConfigurationVariableVisualAction.registerActionCompletedLister(this);
        this.deleteConfigurationVariableVisualAction.registerActionCompletedLister(this);
        this.poolConfigurationDialog.registerActionCompletedLister(this);

        btnAddConfigurationVariable = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                this.newConfigurationVariableVisualAction.getModuleAction().getDisplayName());
        btnAddConfigurationVariable.addClickListener(event -> {
            this.newConfigurationVariableVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("pool", currentConfigVariablesPool))).open();
        });
        btnAddConfigurationVariable.setHeight("32px");

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
        lytCmb.setVerticalComponentAlignment(FlexComponent.Alignment.END, btnManagePools);
        // bottom grid  
        buildConfigurationVariablesGrid();
        // Layout for variables grid
        lytVariables = new VerticalLayout();
        lytVariables.setClassName("bottom-grid");
        lytVariables.setHeightFull();
        lytVariables.setSpacing(false);
        lytVariables.setMargin(false);
        lytVariables.setPadding(false);
        lytVariables.add(grdVariables);
        // Main left Layout
        lytLeftSide.add(lytCmb, lytVariables);
        //--Right side   
        VerticalLayout lytRightMain = new VerticalLayout();
        lytRightMain.setClassName("right-side");
        lytRightMain.setMargin(false);
        lytRightMain.setPadding(false);
        lytRightMain.setSpacing(false);
        // Layout for action buttons
        lytRightActionButtons = new HorizontalLayout();
        lytRightActionButtons.setClassName("button-toolbar");
        lytRightActionButtons.setPadding(false);
        lytRightActionButtons.setMargin(false);
        lytRightActionButtons.setSpacing(false);
        lytRightActionButtons.setJustifyContentMode(JustifyContentMode.END);
        lytRightActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        lytFilterName = new HorizontalLayout();
        lytFilterName.setMargin(false);
        lytFilterName.setPadding(false);
        lytFilterName.setWidth("80%");

        createRightControlButtons();
        HorizontalLayout lytScriptControls = new HorizontalLayout();
        lytScriptControls.setClassName("script-control");
        lytScriptControls.setPadding(false);
        lytScriptControls.setMargin(false);
        lytScriptControls.setSpacing(false);
        lytScriptControls.setWidthFull();
        lytScriptControls.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytScriptControls.add(lytFilterName, lytRightActionButtons);

        lytScriptHeader = new VerticalLayout();
        lytScriptHeader.setClassName("header-script-control");
        lytScriptHeader.setPadding(false);
        lytScriptHeader.setMargin(false);
        lytScriptHeader.setSpacing(false);
        lytScriptHeader.setSpacing(false);
        lytScriptHeader.add(lytScriptControls);
        lytScriptHeader.setVisible(false);

        //Property Sheet
        propertysheet = new PropertySheet(ts, new ArrayList<>());
        propertysheet.addPropertyValueChangedListener(this);

        lytPropertySheet = new VerticalLayout();
        lytPropertySheet.setClassName("script-editor");
        lytPropertySheet.setMargin(false);
        lytPropertySheet.setPadding(false);
        lytPropertySheet.setSpacing(false);
        lytPropertySheet.add(propertysheet);
        lytPropertySheet.setVisible(false);

        lytRightMain.add(lytScriptHeader, lytPropertySheet);
        // Layout for actions
        splitLayout.addToPrimary(lytLeftSide);
        splitLayout.addToSecondary(lytRightMain);
        add(splitLayout);
    }

    private void createRightControlButtons() {
        lblVariableName = new Label();
        lblVariableName.setClassName("dialog-title");
        Label lblVariable = new Label(ts.getTranslatedString("module.configman.configurationvariable"));
        lblVariable.setClassName("dialog-title");
        lytFilterName.add(new Html("<span>&nbsp;</span>"), lblVariable, lblVariableName);

        Command deleteVariable = () -> showFields(false);
        btnDeleteConfigurationVariable = new ActionButton(new ActionIcon(VaadinIcon.TRASH)
                , this.deleteConfigurationVariableVisualAction.getModuleAction().getDisplayName());
        btnDeleteConfigurationVariable.setEnabled(true);
        btnDeleteConfigurationVariable.addClickListener(event -> {
            this.deleteConfigurationVariableVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("configurationVariable", currentConfigVariable),
                    new ModuleActionParameter("commandClose", deleteVariable)
            )).open();
        });

        lytRightActionButtons.add(btnDeleteConfigurationVariable);
    }

    private void createComboPools() {
        // First filter
        cmbFilterPoolName = new ComboBox<>(ts.getTranslatedString("module.configman.configurationvariables"));
        cmbFilterPoolName.setPlaceholder(ts.getTranslatedString("module.configman.configvar.label.choose-pool"));
        cmbFilterPoolName.setWidthFull();
        cmbFilterPoolName.setAllowCustomValue(false);
        cmbFilterPoolName.setClearButtonVisible(true);
        cmbFilterPoolName.setValue(allVariables);
        buildComboBoxFilterProvider();
        
        cmbFilterPoolName.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                lytVariables.remove(grdVariables);
                grdVariables.removeAllColumns();
                if (event.getValue().equals(allVariables) || event.getValue() == allVariables)    
                    currentConfigVariablesPool = null;
                else
                    currentConfigVariablesPool = event.getValue();
                buildConfigurationVariablesGrid();
                lytVariables.add(grdVariables);
                lytVariables.setVisible(true);                
            } else {                          
                currentConfigVariablesPool = null;
                grdVariables.removeAllColumns();
                lytVariables.remove(grdVariables);
                lytVariables.setVisible(false);
            }
            showFields(false);
        });
    }

    /**
     * create configuration variables data provider
     */
    private void buildComboBoxFilterProvider() {
        // List of pools for filter
        listPools = aem.getConfigurationVariablesPools();
        allVariables = new InventoryObjectPool("", ts.getTranslatedString("module.configman.configvar.label.all-variables"), "", "", 0);
        listPools.add(allVariables);
        cmbFilterPoolName.setItems(listPools);
        cmbFilterPoolName.setValue(allVariables);
        
        // Validate pool list size
        if (listPools == null || listPools.isEmpty())
            btnAddConfigurationVariable.setEnabled(false);
        else
            btnAddConfigurationVariable.setEnabled(true);
    }

    /**
     * create main grid
     */
    private void buildConfigurationVariablesGrid() {
        try {
            List<ConfigurationVariable> configurationVariables;
            grdVariables = new Grid<>();
            grdVariables.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
            grdVariables.setSelectionMode(Grid.SelectionMode.SINGLE);
            grdVariables.setHeightFull();

            grdVariables.addItemClickListener(event -> {
                currentConfigVariable = event.getItem();
                if (event.getItem() != null) {
                    showFields(true);
                    updatePropertySheet();
                    lblVariableName.setText(event.getItem().getName());
                } else
                    lblVariableName.removeAll();
                showFields(event.getItem() != null);
            });

            Grid.Column<ConfigurationVariable> nameColumn = grdVariables.addColumn(TemplateRenderer.<ConfigurationVariable>of(
                    "<div>[[item.name]]</div>")
                    .withProperty("name", ConfigurationVariable::getName))
                    .setKey(ts.getTranslatedString("module.general.labels.name"));

            if (currentConfigVariablesPool != null) {
                configurationVariables = aem.getConfigurationVariablesInPool(currentConfigVariablesPool.getId());
                dataProvider = new ListDataProvider<>(configurationVariables);
            } else {
                configurationVariables = aem.getAllConfigurationVariables();
                dataProvider = new ListDataProvider<>(configurationVariables);
            }

            grdVariables.setDataProvider(dataProvider);
            // Validate variable list size
            if (configurationVariables.isEmpty())
                labelInfoVariable(nameColumn);
            else
                createTxtFieldVariableName(nameColumn, dataProvider);
        } catch (ApplicationObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }

    }
    
    private void refreshVariablesGrid() {
        lytVariables.remove(grdVariables);
        grdVariables.removeAllColumns();
        buildConfigurationVariablesGrid();
        lytVariables.add(grdVariables);
    }

    /**
     * create a new input field to configuration variables in the header row
     * @param dataProvider data provider to filter
     * @return the new input field filter
     */
    private void createTxtFieldVariableName(Grid.Column column, ListDataProvider<ConfigurationVariable> dataProvider) {
        TextField txtVariableName = new TextField();
        txtVariableName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtVariableName.setValueChangeMode(ValueChangeMode.EAGER);
        txtVariableName.setWidthFull();
        txtVariableName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH,
                ts.getTranslatedString("module.configman.label.filter-configuration-variable")));
        // object name filter
        txtVariableName.addValueChangeListener(event -> dataProvider.addFilter(
                variable -> StringUtils.containsIgnoreCase(variable.getName(),
                        txtVariableName.getValue())));

        // action button layout
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setClassName("left-action-buttons");
        lytActionButtons.setSpacing(false);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(txtVariableName, btnAddConfigurationVariable);

        HeaderRow filterRow = grdVariables.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }
    
    /**
     * Adds a Label to show information and button for add variable  
     */
    private void labelInfoVariable(Grid.Column column) {
        Label lblInfo = new Label();
        lblInfo.setVisible(true);
        lblInfo.setText(ts.getTranslatedString("module.configman.configvar.label.no-associated-variables"));
        lblInfo.setWidthFull();
         // action button layout
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setClassName("left-action-buttons");
        lytActionButtons.setSpacing(false);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(lblInfo, btnAddConfigurationVariable);
        
        HeaderRow filterRow = grdVariables.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }

    private void updatePropertySheet() {
        if (currentConfigVariable != null) {
            try {
                ConfigurationVariable aWholeConfigurationVariable = aem.getConfigurationVariable(currentConfigVariable.getName());
                propertysheet.setItems(PropertyFactory.propertiesFromConfigurationVariable(aWholeConfigurationVariable, ts));
            } catch (UnsupportedOperationException | ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }

    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        if (currentConfigVariable != null) {
            try {
                aem.updateConfigurationVariable(currentConfigVariable.getName()
                        , property.getName()
                        , String.valueOf(property.getValue())
                );
                String oldValue = "";
                switch (property.getName()) {
                    case Constants.PROPERTY_NAME:
                        oldValue = currentConfigVariable.getName();
                        currentConfigVariable.setName(String.valueOf(property.getValue()));
                        refreshVariablesGrid();
                        break;
                    case Constants.PROPERTY_DESCRIPTION:
                        oldValue = currentConfigVariable.getDescription();
                        currentConfigVariable.setDescription(String.valueOf(property.getValue()));
                        break;
                    case Constants.PROPERTY_VALUE:
                        oldValue = currentConfigVariable.getValueDefinition();
                        currentConfigVariable.setValueDefinition(String.valueOf(property.getValue()));
                        break;
                    case Constants.PROPERTY_MASKED:
                        oldValue = String.valueOf(currentConfigVariable.isMasked());
                        break;
                    default:
                        break;
                }
                updatePropertySheet();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();
                // activity log
                ChangeDescriptor changeDescriptor = new ChangeDescriptor(property.getName(), oldValue, String.valueOf(property.getValue()),
                        String.format(ts.getTranslatedString("module.configman.actions.update-configuration-variable.ui.updated-log"), currentConfigVariable.getId()));
                aem.createGeneralActivityLogEntry(UI.getCurrent().getSession().getAttribute(Session.class).getUser().getUserName(),
                        ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, changeDescriptor);
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
                propertysheet.undoLastEdit();
            }
        }
    }

    private void launchPoolDialog() {
        Command commandAddVariablesPool = () -> {
            buildComboBoxFilterProvider();
            refreshVariablesGrid();
        };
        Command commandDeleteVariablesPool = () -> {
            buildComboBoxFilterProvider();
            refreshVariablesGrid();
        };

        this.poolConfigurationDialog.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter("commandAddVariablesPool", commandAddVariablesPool),
                new ModuleActionParameter("commandDeleteVariablesPool", commandDeleteVariablesPool)
        )).open();
    }

    /**
     * Shows/Hides the labels and buttons in the header of the filter editor
     */
    private void showFields(boolean show) {
        lytScriptHeader.setVisible(show);
        lytPropertySheet.setVisible(show);
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.configman.title");
    }
}