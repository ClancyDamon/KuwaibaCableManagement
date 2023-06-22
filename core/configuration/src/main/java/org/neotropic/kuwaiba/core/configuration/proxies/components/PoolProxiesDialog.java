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
package org.neotropic.kuwaiba.core.configuration.proxies.components;

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
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryProxy;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.configuration.proxies.ProxyManagerModule;
import org.neotropic.kuwaiba.core.configuration.proxies.ProxyManagerService;
import org.neotropic.kuwaiba.core.configuration.proxies.actions.DeleteProxiesPoolVisualAction;
import org.neotropic.kuwaiba.core.configuration.proxies.actions.NewProxiesPoolVisualAction;
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
 * Visual wrapper of create a new proxy variable pool action.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Component
public class PoolProxiesDialog extends AbstractVisualAction<Dialog> implements ActionCompletedListener,
        PropertySheet.IPropertyValueChangedListener {
    /**
     * action over main layout after add new pool
     */
    private Command addVariablesPool;
    /**
     * action over main layout after delete pool
     */
    private Command deleteVariablesPool;
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    /**
     * Contains the filters grid in the bottom left side
     */
    private VerticalLayout lytPoolsGrid;
    /**
     * Layout for actions of pool definition
     */
    private HorizontalLayout lytRightActionButtons;
    /**
     * Contains the script editor in the right side, bottom
     */
    private VerticalLayout lytScriptEditor;
    /**
     * Contains the script editor header
     */
    private VerticalLayout lytScriptHeader;
    /**
     * Contains all pool inside configuration variable
     */
    private VerticalLayout lytVariablesInPool;
    /**
     * Property sheet
     */
    private PropertySheet propertySheetPool;
    /**
     * The grid with the list proxy pools
     */
    private Grid<InventoryObjectPool> grdPools;
    /**
     * The grid with the list proxies in the pool dialog
     */
    private Grid<InventoryProxy> grdProxiesInPool;
    /**
     * selected pool;
     */
    private InventoryObjectPool currentPool;
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
     * Reference to the Proxy Manager Service
     */
    @Autowired
    private ProxyManagerService pms;
    /**
     * The visual action to create a new proxies pool
     */
    @Autowired
    private NewProxiesPoolVisualAction newProxiesPoolVisualAction;
    /**
     * The visual action to delete a proxies pool preselected
     */
    @Autowired
    private DeleteProxiesPoolVisualAction deleteProxiesPoolVisualAction;

    public PoolProxiesDialog() {
        super(ProxyManagerModule.MODULE_ID);
    }

    public void freeResources() {
        this.deleteProxiesPoolVisualAction.unregisterListener(this);
        this.newProxiesPoolVisualAction.unregisterListener(this);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        this.deleteProxiesPoolVisualAction.registerActionCompletedLister(this);
        this.newProxiesPoolVisualAction.registerActionCompletedLister(this);

        ConfirmDialog cfdPoolDialog = new ConfirmDialog(ts,
                ts.getTranslatedString("module.configman.proxies.actions.manage-pools.name"),
                ts.getTranslatedString("module.general.messages.close")
        );
        cfdPoolDialog.setDraggable(true);
        cfdPoolDialog.getBtnConfirm().setVisible(false);
        cfdPoolDialog.setContentSizeFull();
        //load commands from parent layout 
        addVariablesPool = (Command) parameters.get("commandAddProxyPool");
        deleteVariablesPool = (Command) parameters.get("commandDeleteProxyPool");
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
        lytPoolsGrid.add(grdPools);
        lytLeftSide.add(lytPoolsGrid);
        //end left side
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
        lytVariablesInPool.add(grdProxiesInPool);
        lytVariablesInPool.setVisible(false);

        lytRightMain.add(lytScriptHeader, lytScriptEditor, lytVariablesInPool);
        //end right side
        splitLayout.addToSecondary(lytRightMain);
        cfdPoolDialog.setContent(splitLayout);

        cfdPoolDialog.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                freeResources();
            }
        });
        return cfdPoolDialog;
    }

    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            if (property.getName().equals(Constants.PROPERTY_NAME)) {
                aem.setPoolProperties(currentPool.getId(), String.valueOf(property.getValue()), currentPool.getDescription());
                currentPool.setName(String.valueOf(property.getValue()));
                refreshPoolGrid();
            } else if (property.getDescription().equals(Constants.PROPERTY_DESCRIPTION)) {
                aem.setPoolProperties(currentPool.getId(), currentPool.getName(), String.valueOf(property.getValue()));
                currentPool.setDescription(String.valueOf(property.getValue()));
            }
            updatePropertySheet(currentPool);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                    AbstractNotification.NotificationType.INFO, ts).open();
        } catch (Exception ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            propertySheetPool.undoLastEdit();
        }
    }

    /**
     * create main left grid
     */
    private void buildLeftMainGrid() {
        try {
            //create data provider
            List<InventoryObjectPool> listPools = pms.getProxyPools();
            ListDataProvider<InventoryObjectPool> dataProviderPools = new ListDataProvider<>(listPools);
            //create grid
            grdPools = new Grid<>();
            grdPools.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
            grdPools.setSelectionMode(Grid.SelectionMode.SINGLE);
            grdPools.setDataProvider(dataProviderPools);
            grdPools.setHeight("100%");

            grdPools.addItemClickListener(listener -> {
                currentPool = listener.getItem();
                updatePropertySheet(currentPool);
                createVariableInPoolDataProvider(currentPool);
                showFields(true);
            });

            Grid.Column<InventoryObjectPool> nameColumn = grdPools
                    .addColumn(InventoryObjectPool::getName);

            lytPoolsGrid.removeAll();
            lytPoolsGrid.add(grdPools);
            createHeaderGrid(nameColumn, dataProviderPools);
        } catch (UnsupportedOperationException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    /**
     * create a new input field to configuration variables pool in the header
     * row
     *
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
        Command addPool = () -> {
            refreshPoolGrid();
        };

        ActionButton btnAddPool = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                this.newProxiesPoolVisualAction.getModuleAction().getDisplayName());
        btnAddPool.addClickListener((event) -> {
            this.newProxiesPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("commandClose", addVariablesPool),
                    new ModuleActionParameter("commandAdd", addPool)
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

        HeaderRow filterRow = grdPools.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }

    private void refreshPoolGrid() {
        //List of pool for filter
        List<InventoryObjectPool> listPools = pms.getProxyPools();
        grdPools.setItems(listPools);
    }

    private void createRightControlButtons() {
        Command deletePool = () -> {
            refreshPoolGrid();
            showFields(false);
        };

        ActionButton btnDeletePool = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                this.deleteProxiesPoolVisualAction.getModuleAction().getDisplayName());
        btnDeletePool.addClickListener(event -> {
            this.deleteProxiesPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("pool", currentPool),
                    new ModuleActionParameter("commandClose", deleteVariablesPool),
                    new ModuleActionParameter("commandDelete", deletePool)
            )).open();
        });

        lytRightActionButtons.add(btnDeletePool);
    }

    private void createVariableInPool() {
        grdProxiesInPool = new Grid<>();
        grdProxiesInPool.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        grdProxiesInPool.setSelectionMode(Grid.SelectionMode.NONE);
        grdProxiesInPool.setHeightFull();

        Grid.Column<InventoryProxy> nameColumn = grdProxiesInPool.addColumn(TemplateRenderer.<InventoryProxy>of(
                "<div>[[item.name]]</div>")
                .withProperty("name", InventoryProxy::getName));
        createVariableInPoolDataProvider(null);

        Label lblVariableInPool = new Label(ts.getTranslatedString("module.configman.proxies.label.pool"));
        HeaderRow filterRow = grdProxiesInPool.appendHeaderRow();
        filterRow.getCell(nameColumn).setComponent(lblVariableInPool);

    }

    private void createVariableInPoolDataProvider(InventoryObjectPool pool) {
        if (pool != null) {
            try {
                List<InventoryProxy> proxies = pms.getProxiesInPool(pool.getId());
                ListDataProvider<InventoryProxy> dataProvider = new ListDataProvider<>(proxies);
                if (!proxies.isEmpty()) {
                    grdProxiesInPool.setDataProvider(dataProvider);
                    grdProxiesInPool.getDataProvider().refreshAll();
                    grdProxiesInPool.setVisible(true);
                } else {
                    grdProxiesInPool.setVisible(false);
                }
            } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }

    
    private void updatePropertySheet(InventoryObjectPool pool) {
        if (pool != null) {
            propertySheetPool.setItems(PropertyFactory.propertiesFromPoolWithoutClassName(pool, ts));
        }
    }

    /**
     * Shows/Hides the labels and buttons in the header of the filter editor
     */
    private void showFields(boolean show) {
        lytScriptHeader.setVisible(show);
        lytScriptEditor.setVisible(show);
        lytVariablesInPool.setVisible(show);
    }

}
