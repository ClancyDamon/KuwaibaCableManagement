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
package com.neotropic.kuwaiba.modules.commercial.planning.projects;

import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.RelateObjectToProjectVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.CopyProjectToPoolVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.DeleteProjectVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.MoveProjectToPoolVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.NewProjectVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.ReleaseObjectFromProjectVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.components.ProjectActivityDialog;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.components.ProjectPoolDialog;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
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
import org.neotropic.kuwaiba.modules.core.navigation.ObjectDashboard;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.modules.optional.reports.actions.LaunchClassLevelReportAction;
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
 * Main for the Projects Module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "planning/projects", layout = ProjectsLayout.class) 
public class ProjectsUI extends VerticalLayout implements ActionCompletedListener,
        PropertySheet.IPropertyValueChangedListener, HasDynamicTitle, AbstractUI {
    /**
     * Reference to the Project Service
     */
    @Autowired
    private ProjectsService ps;
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
     * The visual action to create a new project
     */
    @Autowired
    private NewProjectVisualAction newProjectVisualAction;
    /**
     * The visual action to delete a project
     */
    @Autowired
    private DeleteProjectVisualAction deleteProjectVisualAction;
    /**
     * The visual action for pool management
     */
    @Autowired
    private ProjectPoolDialog projectPoolDialog;
    /**
     * The visual action for activity management
     */
    @Autowired
    private ProjectActivityDialog projectActivityDialog;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;
    /**
     * Launches class level reports given a selected object.
     */
    @Autowired
    private LaunchClassLevelReportAction launchClassLevelReportAction;
    /**
     * The visual action to associate objects of a project
     */
    @Autowired
    private RelateObjectToProjectVisualAction relateObjectToProjectVisualAction;
    /**
     * The visual action to release a business object of a project
     */
    @Autowired
    private ReleaseObjectFromProjectVisualAction releaseObjectVisualAction;
    /**
     * The visual action to copy a project from a project pool
     */
    @Autowired
    private CopyProjectToPoolVisualAction copyProjectToPoolVisualAction;
    /**
     * The visual action to copy a project from a pool to another pool
     */
    @Autowired
    private MoveProjectToPoolVisualAction moveProjectToPoolVisualAction;
    /**
     * Object used to show more information about a project
     */    
    private ActionButton btnInfo;
    /**
     * Button used to create a new project
     */
    private ActionButton btnAddProject;
    /*
     * Button used to delete a project
     */
    private ActionButton btnDeleteProject;
    /**
     * Button used to manage activities
     */
    private ActionButton btnManageActivities;
    /**
     * Button used to show project reports
     */
    private ActionButton btnProjectReports;
    /**
     * Button used to copy a project preselected
     */
    private ActionButton btnCopy;
    /**
     * Button used to move a project preselected
     */
    private ActionButton btnMove;
    /**
     * The grid with the list projects
     */
    private Grid<BusinessObjectLight> gridProjects;
    /**
     * The grid with the list objects
     */
    private Grid<BusinessObjectLight> gridObjects;
    /**
     * Pool items limit. -1 To return all
     */
    public static final int LIMIT = -1;
    /**
     * Object to save the selected pool
     */
    private InventoryObjectPool currentPool;
    /**
     * Object to save the selected project
     */
    private BusinessObjectLight currentProject;
    /**
     * Object to save the pool list
     */
    private List<InventoryObjectPool> listPool;
    /**
     * Object to add a new Pool 
     */
    private InventoryObjectPool allProjects;
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
    private PropertySheet propertySheet;
    /**
     * Layout of projects 
     */
    private VerticalLayout lytProjects;
    /**
     * Layout of objects associated with a project
     */
    private VerticalLayout lytObjects;
    /**
     * Object to filter for pool name
     */
    private ComboBox<InventoryObjectPool> cmbFilterPoolName;
    /**
     * Object to show info of projects
     */
    private Label lblProjectName;
    /**
     * Relationship project to object
     */
    public static String RELATIONSHIP_PROJECTSPROJECTUSES = "projectsProjectUses";
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    
    public ProjectsUI() {
        super();
        setSizeFull();
    }
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            refreshProjectsGrid();
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open(); 
    }
        
    @Override
    public void onDetach(DetachEvent ev) {
        this.projectPoolDialog.unregisterListener(this);
        this.projectActivityDialog.unregisterListener(this);
        this.newProjectVisualAction.unregisterListener(this);
        this.deleteProjectVisualAction.unregisterListener(this);
        this.releaseObjectVisualAction.unregisterListener(this);
        this.copyProjectToPoolVisualAction.unregisterListener(this);
        this.moveProjectToPoolVisualAction.unregisterListener(this);
        this.relateObjectToProjectVisualAction.unregisterListener(this);
    }
    
    @Override
    public void initContent() {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);
        
        this.projectPoolDialog.registerActionCompletedLister(this);
        this.projectActivityDialog.registerActionCompletedLister(this);
        this.newProjectVisualAction.registerActionCompletedLister(this);
        this.deleteProjectVisualAction.registerActionCompletedLister(this);
        this.releaseObjectVisualAction.registerActionCompletedLister(this);
        this.copyProjectToPoolVisualAction.registerActionCompletedLister(this);
        this.moveProjectToPoolVisualAction.registerActionCompletedLister(this);
        this.relateObjectToProjectVisualAction.registerActionCompletedLister(this);
               
        btnAddProject = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O)
                , this.newProjectVisualAction.getModuleAction().getDisplayName());
        btnAddProject.addClickListener(event -> {
            this.newProjectVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("pool", currentPool))).open();
        });
        btnAddProject.setHeight("32px");
        
        ActionButton btnManagePools = new ActionButton(new ActionIcon(VaadinIcon.COG),
                ts.getTranslatedString("module.projects.actions.pool.manage-pool.name"));
        btnManagePools.addClickListener(event -> launchPoolDialog());
        btnManagePools.setHeight("32px");
        
        // Split Layout
        splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25);
        
        // --> Left side
        buildProjectsGrid(null);
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
        // Layout for projects grid
        lytProjects = new VerticalLayout();
        lytProjects.setClassName("bottom-grid");
        lytProjects.setHeightFull();
        lytProjects.setPadding(false);
        lytProjects.setMargin(false);
        lytProjects.setSpacing(false);
        lytProjects.add(gridProjects);
        // Main left Layout 
        lytLeftSide = new VerticalLayout();
        lytLeftSide.setClassName("left-side");
        lytLeftSide.setSizeFull();
        lytLeftSide.setMargin(false);
        lytLeftSide.setSpacing(false);
        lytLeftSide.setId("left-lyt");
        lytLeftSide.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        lytLeftSide.add(lytCmb, lytProjects);
        
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
        propertySheet = new PropertySheet(ts, new ArrayList<>());
        propertySheet.addPropertyValueChangedListener(this);
        lytPropertySheet = new VerticalLayout();
        lytPropertySheet.setId("lyt-property-sheet");
        lytPropertySheet.setWidthFull();
        lytPropertySheet.setBoxSizing(BoxSizing.BORDER_BOX);
        lytPropertySheet.setMargin(false);
        lytPropertySheet.setPadding(true);
        lytPropertySheet.setSpacing(false);
        lytPropertySheet.add(propertySheet);
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
    private void createRightControlButtons() {
        Label lblProject = new Label(ts.getTranslatedString("module.projects.project.label"));
        lblProject.setClassName("dialog-title");
        lblProjectName = new Label();
        lblProjectName.setClassName("dialog-title");
        lytFilterName.add(new Html("<span>&nbsp;</span>"), lblProject, lblProjectName);
        
        Command addPool = () -> {
            buildComboBoxFilterProvider();
            refreshProjectsGrid();

            new SimpleNotification(ts.getTranslatedString("module.general.messages.success")
                    , ts.getTranslatedString("module.projects.actions.pool.new-pool-success")
                    , AbstractNotification.NotificationType.INFO, ts).open();
        };
        
        btnCopy = new ActionButton(new ActionIcon(VaadinIcon.COPY),
                this.copyProjectToPoolVisualAction.getModuleAction().getDisplayName());
        btnCopy.addClickListener(event -> {
            this.copyProjectToPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("project", currentProject),
                    new ModuleActionParameter("commandAddProjectPool", addPool)
            )).open();
        });
        
        Command moveProject = () -> {
            currentProject = null;
            showFields(false);
        };
        btnMove = new ActionButton(new ActionIcon(VaadinIcon.PASTE),
                 this.moveProjectToPoolVisualAction.getModuleAction().getDisplayName());
        btnMove.addClickListener(event -> {
            this.moveProjectToPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("pool", currentPool),
                    new ModuleActionParameter("project", currentProject),
                    new ModuleActionParameter("command", moveProject),
                    new ModuleActionParameter("commandAddProjectPool", addPool)
            )).open();
        });
        
        btnManageActivities = new ActionButton(new ActionIcon(VaadinIcon.COG),
                ts.getTranslatedString("module.projects.actions.activity.button-activity.name"));
        btnManageActivities.setEnabled(true);
        btnManageActivities.addClickListener(event -> launchActivityDialog());
        
        btnProjectReports = new ActionButton(new ActionIcon(VaadinIcon.FILE_TABLE), this.launchClassLevelReportAction.getName());
        btnProjectReports.setEnabled(true);
        btnProjectReports.addClickListener(event -> {
            this.launchClassLevelReportAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("businessObject", currentProject))).open();
        });
        
        Command deleteProject = () -> {
            showFields(false);
        };
        btnDeleteProject = new ActionButton(new ActionIcon(VaadinIcon.TRASH), this.deleteProjectVisualAction.getModuleAction().getDisplayName());
        btnDeleteProject.setEnabled(true);
        btnDeleteProject.addClickListener(event -> {
            this.deleteProjectVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("project", currentProject),
                    new ModuleActionParameter("commandClose", deleteProject)
            )).open();
        });
        
        btnInfo = new ActionButton(new ActionIcon(VaadinIcon.INFO_CIRCLE), this.windowMoreInformation.getDisplayName()); 
        btnInfo.setEnabled(true);
        btnInfo.addClickListener(event -> {
            this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("object", currentProject))).open();
        });
        
        lytRightActionButtons.add(btnDeleteProject, btnManageActivities, btnProjectReports, btnInfo, btnCopy, btnMove);
    }
    
    private void createComboPools() {
        // First filter
        cmbFilterPoolName = new ComboBox<>(ts.getTranslatedString("module.projects.pool.header"));
        cmbFilterPoolName.setPlaceholder(ts.getTranslatedString("module.projects.pool.label.choose-pool"));
        cmbFilterPoolName.setWidthFull();
        cmbFilterPoolName.setAllowCustomValue(false);
        cmbFilterPoolName.setClearButtonVisible(true);
        buildComboBoxFilterProvider();
        cmbFilterPoolName.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                if (event.getValue().equals(allProjects) || event.getValue() == allProjects) {
                    currentPool = null;
                    lytProjects.remove(gridProjects);
                    gridProjects.removeAllColumns();
                    buildProjectsGrid(null);
                } else {
                    lytProjects.remove(gridProjects);
                    gridProjects.removeAllColumns();
                    buildProjectsGrid(event.getValue());
                    currentPool = event.getValue();
                }
                lytProjects.add(gridProjects);
                lytProjects.setVisible(true);
            } else {
                gridProjects.removeAllColumns();
                lytProjects.remove(gridProjects);
                lytProjects.setVisible(false);
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
            listPool = ps.getProjectPools();
            allProjects = new InventoryObjectPool("", ts.getTranslatedString("module.projects.pool.label.all-projects"), "", "", 0);
            listPool.add(allProjects);
            cmbFilterPoolName.setItems(listPool);
            cmbFilterPoolName.setValue(allProjects);
            // Validate pool list size
            if (listPool.isEmpty())
                btnAddProject.setEnabled(false);
            else
                btnAddProject.setEnabled(true);
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void buildProjectsGrid(InventoryObjectPool pool) {
        try {
            ListDataProvider<BusinessObjectLight> dataProvider;
            List<BusinessObjectLight> projects;
            gridProjects = new Grid<>();
            gridProjects.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
            gridProjects.setSelectionMode(Grid.SelectionMode.SINGLE);
            gridProjects.setHeightFull();

            gridProjects.addSelectionListener(event -> {
                event.getFirstSelectedItem().ifPresent(obj -> {
                    showFields(true);
                    currentProject = obj;
                    lblProjectName.setText(obj.getName());
                    updatePropertySheet();
                    buildObjectsGrid(obj);
                });
            });

            Grid.Column<BusinessObjectLight> nameColumn = gridProjects.addColumn(TemplateRenderer.<BusinessObjectLight>of(
                    "<div>[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.className]]</font></div>")
                    .withProperty("name", BusinessObjectLight::getName)
                    .withProperty("className", BusinessObjectLight::getClassName));

            if (pool != null)
                projects = ps.getProjectsInPool(pool.getId(), LIMIT);
            else
                projects = ps.getAllProjects(LIMIT, LIMIT);
            dataProvider = new ListDataProvider<>(projects);
            gridProjects.setDataProvider(dataProvider);
            // Validate project list size
            if (projects.isEmpty())
                labelInfoProject(nameColumn);
            else
                createTxtFieldProjectName(nameColumn, dataProvider);// Filter project by name
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void refreshProjectsGrid() {
        if (cmbFilterPoolName.getValue() != null) {
            if (cmbFilterPoolName.getValue().equals(allProjects) || cmbFilterPoolName.getValue() == allProjects) {
                lytProjects.remove(gridProjects);
                gridProjects.removeAllColumns();
                buildProjectsGrid(null);
            } else {
                lytProjects.remove(gridProjects);
                gridProjects.removeAllColumns();
                buildProjectsGrid(cmbFilterPoolName.getValue());
            }
            lytProjects.add(gridProjects);
        }
    }
    
    /**
     * create a new input field to project in the header row
     * @param dataProvider data provider to filter
     */
    private void createTxtFieldProjectName(Grid.Column column, ListDataProvider<BusinessObjectLight> dataProvider) {
        TextField txtProjectName = new TextField();
        txtProjectName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtProjectName.setValueChangeMode(ValueChangeMode.EAGER);
        txtProjectName.setWidthFull();
        txtProjectName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH,
                ts.getTranslatedString("module.projects.pool.filter")));
        // object name filter
        txtProjectName.addValueChangeListener(event -> dataProvider.addFilter(
                project -> StringUtils.containsIgnoreCase(project.getName(),
                        txtProjectName.getValue())));
        // action button layout
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setClassName("left-action-buttons");
        lytActionButtons.setSpacing(false);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(txtProjectName, btnAddProject);

        HeaderRow filterRow = gridProjects.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }
             
    /**
     * Adds a Label to show information and button for add project  
     */
    private void labelInfoProject(Grid.Column column) {
        Label lblInfo = new Label();
        lblInfo.setVisible(true);
        lblInfo.setText(ts.getTranslatedString("module.projects.pool.label.no-associated-projects"));
        lblInfo.setWidthFull();
         // action button layout
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setClassName("left-action-buttons");
        lytActionButtons.setSpacing(false);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(lblInfo, btnAddProject);
        
        HeaderRow filterRow = gridProjects.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }
    
    /**
     * Property sheet for projects
     * @param project Project to update
     */
    private void updatePropertySheet() {
        try {
            if (currentProject != null) {
                BusinessObject aWholeProject = ps.getProject(currentProject.getClassName(), currentProject.getId());
                propertySheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeProject, ts, aem, mem));
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
        
    /**
     * Update project properties. 
     * @param property the property to update
     */
    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        try {
            if (currentProject != null) {
                Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                ps.updateProject(currentProject.getClassName(), currentProject.getId(), attributes, session.getUser().getUserName());
                if (property.getName().equals(Constants.PROPERTY_NAME)) { // Update project name in the list.
                    currentProject.setName(String.valueOf(property.getValue()));
                    lblProjectName.setText(String.valueOf(property.getValue()));
                    refreshProjectsGrid();
                }
                updatePropertySheet();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            }
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException
                | BusinessObjectNotFoundException | OperationNotPermittedException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            propertySheet.undoLastEdit();
        }
    }
       
    private void launchPoolDialog() {
        Command commandAddPool = () -> {
            buildComboBoxFilterProvider();
            refreshProjectsGrid();
        };
        
        Command commandDeletePool = () -> {
            buildComboBoxFilterProvider();
            refreshProjectsGrid();
        };
        
        this.projectPoolDialog.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter("commandAddPool", commandAddPool),
                new ModuleActionParameter("commandDeletePool", commandDeletePool)
        )).open();
    }
    
    private void launchActivityDialog() {
        this.projectActivityDialog.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter("project", currentProject)
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
    
    private void buildObjectsGrid(BusinessObjectLight project) {
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
        if (project != null) {
            try {
                VerticalLayout lytContent = new VerticalLayout(lblHeader);
                lytContent.setMargin(false);
                lytContent.setPadding(false);
                lytContent.setSizeFull();
                
                listObjects = ps.getProjectResources(project.getClassName(), project.getId());
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
            buildObjectsGrid(currentProject);
        };
        ActionButton btnRelease = new ActionButton(new ActionIcon(VaadinIcon.UNLINK)
                , this.releaseObjectVisualAction.getModuleAction().getDisplayName());
        btnRelease.addClickListener(event -> {
            this.releaseObjectVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("project", currentProject),
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
        return ts.getTranslatedString("module.projects.title");
    }
}