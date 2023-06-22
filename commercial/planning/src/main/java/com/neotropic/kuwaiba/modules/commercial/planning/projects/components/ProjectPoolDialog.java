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
package com.neotropic.kuwaiba.modules.commercial.planning.projects.components;

import com.neotropic.kuwaiba.modules.commercial.planning.projects.ProjectsModule;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.ProjectsService;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.DeleteProjectsPoolVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.NewProjectsPoolVisualAction;
import com.vaadin.flow.component.DetachEvent;
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
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
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
 * Visual wrapper of manage project pool action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@CssImport(value = "./css/poolConfigurationDialog.css")
@Component
public class ProjectPoolDialog extends AbstractVisualAction<Dialog> implements ActionCompletedListener,
        PropertySheet.IPropertyValueChangedListener {
    /**
     * Reference to the Project Service
     */
    @Autowired
    ProjectsService ps;
    /**
     * Reference to the Translation Service
     */            
    @Autowired
    private TranslationService ts;
    /**
     * The visual action to create a new project pool
     */
    @Autowired
    private NewProjectsPoolVisualAction newProjectsPoolVisualAction;
    /**
     * The visual action to delete a project pool
     */
    @Autowired    
    private DeleteProjectsPoolVisualAction deleteProjectsPoolVisualAction;
    /**
     * action over main layout after add new pool 
     */
    private Command addPool;
    /**
     * action over main layout after delete pool
     */
    private Command deletePool;
    /**
     * action after delete pool
     */
    private Command commandDeletePool;
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    /**
     * Contains the filters grid in the bottom left side
     */
    private VerticalLayout lytPoolsGrid;
    /**
     * Contains all pool inside projects
     */
    private VerticalLayout lytProjects;
    /**
     * Layout for actions of pool definition
     */
    private HorizontalLayout lytRightActionButtons;
    /**
     * Contains the right header
     */
    private VerticalLayout lytRightHeader;
    /**
     * Layout of property sheet
     */
    private VerticalLayout lytPropertySheetPool;
    /**
     * The grid with the contracts pool
     */
    private Grid<InventoryObjectPool> gridPools;
    /**
     * The grid with the projects in the pool dialog
     */
    private Grid<BusinessObjectLight> gridProjects;
    /**
     * Projects data provider
     */
    private ListDataProvider<BusinessObjectLight> dataProviderProjects;
    /**
     * Projects list
     */
    private List<BusinessObjectLight> projects;
    /**
     * Object to save the selected pool
     */
    private InventoryObjectPool currentPool;
    /**
     * Property sheet
     */
    private PropertySheet propertySheetPool;

    public ProjectPoolDialog() {
        super(ProjectsModule.MODULE_ID);
    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
            buildPoolsGrid();
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
    }
    
    public void freeResources(DetachEvent ev) {
        this.newProjectsPoolVisualAction.unregisterListener(this);
        this.deleteProjectsPoolVisualAction.unregisterListener(this);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
         this.newProjectsPoolVisualAction.registerActionCompletedLister(this);
        this.deleteProjectsPoolVisualAction.registerActionCompletedLister(this);
        
        ConfirmDialog wdwPool = new ConfirmDialog(ts,
                ts.getTranslatedString("module.projects.actions.pool.manage-pool.name"),
                ts.getTranslatedString("module.general.messages.close"));
        wdwPool.setDraggable(true);
        wdwPool.getBtnConfirm().setVisible(false);
        wdwPool.setContentSizeFull();
        wdwPool.addDetachListener(event -> freeResources(event));
        // load commands from parent layout 
        addPool = (Command) parameters.get("commandAddPool");
        deletePool = (Command) parameters.get("commandDeletePool");
        //create command current layout
        commandDeletePool = () -> {
            showFields(false);
        };
        // create content
        splitLayout = new SplitLayout();
        splitLayout.setClassName("main-split");
        splitLayout.setSplitterPosition(36);
        // --left side
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
        buildPoolsGrid();
        lytPoolsGrid.add(gridPools);
        lytLeftSide.add(lytPoolsGrid);
        splitLayout.addToPrimary(lytLeftSide);
        //end left side
        
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
        
        //Property Sheet
        propertySheetPool = new PropertySheet(ts, new ArrayList<>());
        propertySheetPool.addPropertyValueChangedListener(this);
        propertySheetPool.setHeightFull();
        lytPropertySheetPool = new VerticalLayout();
        lytPropertySheetPool.setClassName("propertySheet");
        lytPropertySheetPool.setWidthFull();
        lytPropertySheetPool.setMargin(false);
        lytPropertySheetPool.setPadding(false);
        lytPropertySheetPool.setSpacing(false);
        lytPropertySheetPool.add(propertySheetPool);
        lytPropertySheetPool.setVisible(false);
        // Right grid
        createProjectsGrid();
        lytProjects = new VerticalLayout();
        lytProjects.setClassName("grig-pool-container");
        lytProjects.setHeightFull();
        lytProjects.setMargin(false);
        lytProjects.setSpacing(false);
        lytProjects.setPadding(false);        
        lytProjects.add(gridProjects);
        lytProjects.setVisible(false);
        // Add content to right main layout 
        lytRightMain.add(lytRightHeader, lytPropertySheetPool, lytProjects);
        // End right side
        splitLayout.addToSecondary(lytRightMain);
        wdwPool.setContent(splitLayout);
        return wdwPool;
    }
    
    private void buildPoolsGrid() {
        try {
            List<InventoryObjectPool> listPools;
            listPools = ps.getProjectPools();
            ListDataProvider<InventoryObjectPool> dataProvider = new ListDataProvider<>(listPools);
            // Create grid
            gridPools = new Grid<>();
            gridPools.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
            gridPools.setSelectionMode(Grid.SelectionMode.SINGLE);
            gridPools.setDataProvider(dataProvider);
            gridPools.setHeight("100%");
            
            gridPools.addItemClickListener(event -> {
                currentPool = event.getItem();
                updatePropertySheet(currentPool);
                createProjectsDataProvider(currentPool);
                showFields(true);
            });
            
            Grid.Column<InventoryObjectPool> nameColumn = gridPools.addColumn(TemplateRenderer.<InventoryObjectPool>of(
                    "<div>[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.className]]</font></div>")
                    .withProperty("name", InventoryObjectPool::getName)
                    .withProperty("className", InventoryObjectPool::getClassName));
            
            lytPoolsGrid.removeAll();
            lytPoolsGrid.add(gridPools);
            createHeaderGrid(nameColumn, dataProvider);
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Create a new input field to pool in the header row
     * @param dataProvider data provider to filter
     * @return the new input field filter
     */
    private void createHeaderGrid(Grid.Column column, ListDataProvider<InventoryObjectPool> dataProviderPools) {
        TextField txtSearchPoolByName = new TextField();
        txtSearchPoolByName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtSearchPoolByName.setValueChangeMode(ValueChangeMode.EAGER);
        txtSearchPoolByName.setWidthFull();
        txtSearchPoolByName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH,
                ts.getTranslatedString("module.projects.pool.filter")));
        txtSearchPoolByName.addValueChangeListener(event -> dataProviderPools.addFilter(
                pool -> StringUtils.containsIgnoreCase(pool.getName(),
                        txtSearchPoolByName.getValue())));
        // action button layout
        ActionButton btnAddPool = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                this.newProjectsPoolVisualAction.getModuleAction().getDisplayName());
        btnAddPool.addClickListener((event) -> {
            this.newProjectsPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("commandClose", addPool)
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
    
    private void createProjectsGrid() {
        gridProjects = new Grid<>();
        gridProjects.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        gridProjects.setSelectionMode(Grid.SelectionMode.NONE);
        gridProjects.setHeightFull();
        Grid.Column<BusinessObjectLight> nameColumn = gridProjects.addColumn(TemplateRenderer.<BusinessObjectLight>of(
                    "<div>[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.className]]</font></div>")
                    .withProperty("name", BusinessObjectLight::getName)
                    .withProperty("className", BusinessObjectLight::getClassName));
        createProjectsDataProvider(null);
        Label lblProjectInPool = new Label(ts.getTranslatedString("module.projects.project.header"));
        HeaderRow filterRow = gridProjects.appendHeaderRow();
        filterRow.getCell(nameColumn).setComponent(lblProjectInPool);
    }

    private void createProjectsDataProvider(InventoryObjectPool pool) {
        if (pool != null) {
            try {
                projects = ps.getProjectsInPool(pool.getId(), -1);
                dataProviderProjects = new ListDataProvider<>(projects);
                gridProjects.setDataProvider(dataProviderProjects);
                gridProjects.getDataProvider().refreshAll();
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }
    
    private void createRightControlButtons() {
        ActionButton btnDeletePool = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                this.deleteProjectsPoolVisualAction.getModuleAction().getDisplayName());
        btnDeletePool.addClickListener(event -> {
            this.deleteProjectsPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("pool", currentPool),
                    new ModuleActionParameter("commandClose", deletePool),
                    new ModuleActionParameter("commandDelete", commandDeletePool)
            )).open();
        });
        lytRightActionButtons.add(btnDeletePool);
    }
    
    /**
     * Shows/Hides the labels and buttons in the header, also the property sheet.
     */
    private void showFields(boolean show) {
        lytRightHeader.setVisible(show);
        lytPropertySheetPool.setVisible(show);
        lytProjects.setVisible(show);
    }
    
    private void updatePropertySheet(InventoryObjectPool pool) {
        if (pool != null)
            propertySheetPool.setItems(PropertyFactory.propertiesFromPoolWithoutClassName(pool, ts));
    } 
    
    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        if (currentPool != null) {
            try {
                Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                if (property.getName().equals(Constants.PROPERTY_NAME)) {
                    ps.updateProjectPool(currentPool.getId()
                            , currentPool.getClassName()
                            , String.valueOf(property.getValue())
                            , currentPool.getDescription()
                            , session.getUser().getUserName());
                    currentPool.setName(String.valueOf(property.getValue()));
                    buildPoolsGrid();
                } else if (property.getDescription().equals(Constants.PROPERTY_DESCRIPTION)) {
                    ps.updateProjectPool(currentPool.getId()
                            , currentPool.getClassName()
                            , currentPool.getName()
                            , String.valueOf(property.getValue())
                            , session.getUser().getUserName());
                    currentPool.setDescription(String.valueOf(property.getValue()));
                }
                updatePropertySheet(currentPool);
                addPool.execute();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
                propertySheetPool.undoLastEdit();
            }
        }
    }

    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}