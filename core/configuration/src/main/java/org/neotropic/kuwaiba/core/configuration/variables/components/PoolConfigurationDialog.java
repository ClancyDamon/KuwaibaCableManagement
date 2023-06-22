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
package org.neotropic.kuwaiba.core.configuration.variables.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ConfigurationVariable;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.configuration.variables.ConfigurationVariablesModule;
import org.neotropic.kuwaiba.core.configuration.variables.actions.DeleteConfigurationVariablesPoolVisualAction;
import org.neotropic.kuwaiba.core.configuration.variables.actions.NewConfigurationVariablesPoolVisualAction;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new configuration variable pool action.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@CssImport(value = "./css/poolConfigurationDialog.css")
@Component
public class PoolConfigurationDialog extends AbstractVisualAction<Dialog> implements ActionCompletedListener,
        PropertySheet.IPropertyValueChangedListener {
    /**
     * The grid with the configuration variables pool
     */
    private Grid<InventoryObjectPool> gridPools;
    /**
     * The grid with the configuration variables in the pool dialog
     */
    private Grid<ConfigurationVariable> gridVariablesInPool;
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    /**
     * Configuration Variable data provider variables in the pool
     */
    private ListDataProvider<ConfigurationVariable> dataProviderVariableInPool;
    /**
     * Configuration Variable list
     */
    private List<ConfigurationVariable> configurationVariables;
    /**
     * Contains the filters grid in the bottom left side
     */
    private VerticalLayout lytPoolsGrid;
    /**
     * Layout for actions of pool definition
     */
    private HorizontalLayout lytRightActionButtons;
    /**
     * Object to save the selected configuration variables pool
     */
    private InventoryObjectPool currentConfigVariablesPool;
    /**
     * Contains the script editor in the right side, bottom
     */
    private VerticalLayout lytScriptEditor;
    /**
     * Contains the script editor header
     */
    private VerticalLayout lytScriptHeader;
    /**
     * Property sheet
     */
    private PropertySheet propertySheetPool;
    /**
     * Contains all pool inside configuration variable
     */
    private VerticalLayout lytVariablesInPool;
    /**
     * action over main layout after add new pool 
     */
    private Command addVariablesPool;
    /**
     * action over main layout after delete pool
     */
    private Command deleteVariablesPool;
    /**
     * action after add new pool
     */
    private Command commandAddPool;
    /**
     * action after delete pool
     */
    private Command commandDeletePool;
    /**
     * list of pool objects
     */
    private List<InventoryObjectPool> listPools;
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
     * The visual action to create a new configuration variables pool
     */
    @Autowired
    private NewConfigurationVariablesPoolVisualAction newConfigurationVariablesPoolVisualAction;
    /**
     * The visual action to delete a configuration variables pool
     */
    @Autowired
    private DeleteConfigurationVariablesPoolVisualAction deleteConfigurationVariablesPoolVisualAction;

    public PoolConfigurationDialog() {
        super(ConfigurationVariablesModule.MODULE_ID);

    }

    public void freeResources() {
        this.newConfigurationVariablesPoolVisualAction.unregisterListener(this);
        this.deleteConfigurationVariablesPoolVisualAction.unregisterListener(this);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        this.newConfigurationVariablesPoolVisualAction.registerActionCompletedLister(this);
        this.deleteConfigurationVariablesPoolVisualAction.registerActionCompletedLister(this);

        ConfirmDialog cfdPoolDialog = new ConfirmDialog(ts,
                ts.getTranslatedString("module.configman.actions.manage-pool.name"),
                ts.getTranslatedString("module.general.messages.close")
        );
        cfdPoolDialog.setDraggable(true);
        cfdPoolDialog.getBtnConfirm().setVisible(false);
        cfdPoolDialog.setContentSizeFull();
        //load commands from parent layout    
        addVariablesPool = (Command) parameters.get("commandAddVariablesPool");
        deleteVariablesPool = (Command) parameters.get("commandDeleteVariablesPool");
        //create commands current layout
        commandAddPool = () -> {
            refreshPoolList();
        };
        commandDeletePool = () -> {
            refreshPoolList();
            showFields(false);
        };
        
        //create content
        splitLayout = new SplitLayout();
        splitLayout.setClassName("main-split");
        splitLayout.setSplitterPosition(36);
        //--left side
        // Main left Layout 
        VerticalLayout lytLeftSide = new VerticalLayout();
        lytLeftSide.setClassName("left-side-dialog");
        lytLeftSide.setSpacing(false);
        lytLeftSide.setPadding(false);
        lytLeftSide.setMargin(false);
        lytLeftSide.setHeightFull();
        lytLeftSide.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        //top grid
        VerticalLayout lytTopGrid = new VerticalLayout();
        lytTopGrid.setClassName("top-grid-dialog");
        lytTopGrid.setSpacing(true);
        lytTopGrid.setPadding(false);
        lytTopGrid.setMargin(false);
        //bottom grid
        lytPoolsGrid = new VerticalLayout();
        lytPoolsGrid.setClassName("bottom-grid");
        lytPoolsGrid.setSpacing(false);
        lytPoolsGrid.setPadding(false);
        lytPoolsGrid.setMargin(false);
        lytPoolsGrid.setHeightFull();
        buildLeftMainGrid();
        lytPoolsGrid.add(gridPools);
        lytLeftSide.add(lytPoolsGrid);
        //end left side
        splitLayout.addToPrimary(lytLeftSide);

        //--Right side     
        VerticalLayout lytRightMain = new VerticalLayout();
        lytRightMain.setClassName("right-side-dialog");
        lytRightMain.setMargin(false);
        lytRightMain.setPadding(false);
        lytRightMain.setSpacing(true);

        // Layout for action buttons
        lytRightActionButtons = new HorizontalLayout();
        lytRightActionButtons.setClassName("button-container");
        lytRightActionButtons.setPadding(false);
        lytRightActionButtons.setMargin(false);
        lytRightActionButtons.setSpacing(false);
        lytRightActionButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        lytRightActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        HorizontalLayout lytFilterName = new HorizontalLayout();
        lytFilterName.setMargin(false);
        lytFilterName.setPadding(false);
        lytFilterName.setWidth("65%");
        lytFilterName.add(new Label(ts.getTranslatedString("module.serviceman.actions.new-service-pool.ui.pool-name")));

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

        //propertySheet
        propertySheetPool = new PropertySheet(ts, new ArrayList<>());
        propertySheetPool.addPropertyValueChangedListener(this);
        propertySheetPool.setHeightFull();

        lytScriptEditor = new VerticalLayout();
        lytScriptEditor.setClassName("propertySheet");        
        lytScriptEditor.setWidthFull();
        lytScriptEditor.setMargin(false);
        lytScriptEditor.setSpacing(false);
        lytScriptEditor.setPadding(false);
        lytScriptEditor.add(propertySheetPool);
        lytScriptEditor.setVisible(false);

        //right grid
        createVariableInPool();
        lytVariablesInPool = new VerticalLayout();
        lytVariablesInPool.setClassName("grig-pool-container");
        lytVariablesInPool.setHeightFull();
        lytVariablesInPool.setMargin(false);
        lytVariablesInPool.setSpacing(false);
        lytVariablesInPool.setPadding(false);        
        lytVariablesInPool.add(gridVariablesInPool);
        lytVariablesInPool.setVisible(false);

        lytRightMain.add(lytScriptHeader, lytScriptEditor, lytVariablesInPool);
        //end right side
        splitLayout.addToSecondary(lytRightMain);
        cfdPoolDialog.setContent(splitLayout);
        
        cfdPoolDialog.addOpenedChangeListener(event -> {
            if(!event.isOpened())
                freeResources();
        });
        return cfdPoolDialog;
    }

    /**
     * Shows/Hides the labels and buttons in the header of the filter editor
     */
    private void showFields(boolean show) {
        lytScriptHeader.setVisible(show);
        lytScriptEditor.setVisible(show);
        lytVariablesInPool.setVisible(show);
    }

    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * create main left grid
     */
    private void buildLeftMainGrid() {
        try {
            //create data provider
            listPools = aem.getConfigurationVariablesPools();
            ListDataProvider<InventoryObjectPool> dataProviderPools = new ListDataProvider<>(listPools);
            //create grid
            gridPools = new Grid<>();
            gridPools.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
            gridPools.setSelectionMode(Grid.SelectionMode.SINGLE);
            gridPools.setDataProvider(dataProviderPools);
            gridPools.setHeight("100%");

            gridPools.addItemClickListener(listener -> {
                currentConfigVariablesPool = listener.getItem();
                updatePropertySheet(currentConfigVariablesPool);
                createVariableInPoolDataProvider(currentConfigVariablesPool);
                showFields(true);
            });

            Grid.Column<InventoryObjectPool> nameColumn = gridPools.addColumn(InventoryObjectPool::getName);

            lytPoolsGrid.removeAll();
            lytPoolsGrid.add(gridPools);
            createHeaderGrid(nameColumn, dataProviderPools);
        } catch (UnsupportedOperationException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    /**
     * create a new input field to configuration variables pool in the header row
     * @param dataProvider data provider to filter
     * @return the new input field filter
     */
    private void createHeaderGrid(Grid.Column column, ListDataProvider<InventoryObjectPool> dataProviderPools) {
        TextField txtSearchPoolByName = new TextField();
        txtSearchPoolByName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtSearchPoolByName.setValueChangeMode(ValueChangeMode.EAGER);
        txtSearchPoolByName.setWidthFull();
        txtSearchPoolByName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH,
                ts.getTranslatedString("module.configman.label.filter-configuration-variable-pool")));
        txtSearchPoolByName.addValueChangeListener(event -> dataProviderPools.addFilter(
                pool -> StringUtils.containsIgnoreCase(pool.getName(),
                        txtSearchPoolByName.getValue())));
        // action button layout
        ActionButton btnAddPool = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                this.newConfigurationVariablesPoolVisualAction.getModuleAction().getDisplayName());
        btnAddPool.addClickListener((event) -> {
            this.newConfigurationVariablesPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("commandClose", addVariablesPool),
                    new ModuleActionParameter("commandAdd", commandAddPool)
            )).open();
        });
        btnAddPool.setHeight("32px");
        
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setClassName("left-action-buttons");
        lytActionButtons.setSpacing(false);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setWidthFull();
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(txtSearchPoolByName, btnAddPool);

        HeaderRow filterRow = gridPools.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }

    private void updatePropertySheet(InventoryObjectPool pool) {
        if (pool != null) {
            propertySheetPool.setItems(PropertyFactory.propertiesFromPoolWithoutClassName(pool, ts));
        }
    }

    private void createVariableInPool() {
        gridVariablesInPool = new Grid<>();
        gridVariablesInPool.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        gridVariablesInPool.setSelectionMode(Grid.SelectionMode.NONE);
        gridVariablesInPool.setHeightFull();
        Grid.Column<ConfigurationVariable> nameColumn = gridVariablesInPool.addColumn(TemplateRenderer.<ConfigurationVariable>of(
                    "<div>[[item.name]]</div>")
                    .withProperty("name", ConfigurationVariable::getName));
        createVariableInPoolDataProvider(null);

        Label lblVariableInPool = new Label(ts.getTranslatedString("module.configman.configurationvariable"));
        HeaderRow filterRow = gridVariablesInPool.appendHeaderRow();
        filterRow.getCell(nameColumn).setComponent(lblVariableInPool);
        
    }

    private void createVariableInPoolDataProvider(InventoryObjectPool pool){
        if (pool != null) {
            try {
                configurationVariables = aem.getConfigurationVariablesInPool(pool.getId());
                dataProviderVariableInPool = new ListDataProvider<>(configurationVariables);
                gridVariablesInPool.setDataProvider(dataProviderVariableInPool);
                gridVariablesInPool.getDataProvider().refreshAll();
            } catch (UnsupportedOperationException | ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
        } else {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        try {
            if (currentConfigVariablesPool != null) {
                aem.updateConfigurationVariablesPool(currentConfigVariablesPool.getId()
                        , property.getName()
                        , String.valueOf(property.getValue())
                        , UI.getCurrent().getSession().getAttribute(Session.class).getUser().getUserName()
                );
                if (property.getName().equals(Constants.PROPERTY_NAME)) {
                    currentConfigVariablesPool.setName(String.valueOf(property.getValue()));
                } else if (property.getName().equals(Constants.PROPERTY_DESCRIPTION)) {
                    currentConfigVariablesPool.setDescription(String.valueOf(property.getValue()));
                }
                updatePropertySheet(currentConfigVariablesPool);                
                refreshPoolList();
                addVariablesPool.execute();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            }
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            propertySheetPool.undoLastEdit();
        }
    }

    private void createRightControlButtons() {        
        ActionButton btnDeletePool = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                this.deleteConfigurationVariablesPoolVisualAction.getModuleAction().getDisplayName());
        btnDeletePool.addClickListener(event -> {
            this.deleteConfigurationVariablesPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("configurationVariablePool", currentConfigVariablesPool),
                    new ModuleActionParameter("commandClose", deleteVariablesPool),
                    new ModuleActionParameter("commandDelete", commandDeletePool)
            )).open();
        });

        lytRightActionButtons.add(btnDeletePool);
    }

    /**
     * main grid refresh items
     */
    private void refreshPoolList() {
        //List of pool for filter
        listPools = aem.getConfigurationVariablesPools();                
        gridPools.setItems(listPools);
    }
}