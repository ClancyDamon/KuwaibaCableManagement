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
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.DeleteProjectActivityVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.NewProjectActivityVisualAction;
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
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
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
import org.neotropic.kuwaiba.modules.optional.reports.actions.LaunchClassLevelReportAction;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
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
 * Visual wrapper of manage project activity action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@CssImport(value = "./css/poolConfigurationDialog.css")
@Component
public class ProjectActivityDialog extends AbstractVisualAction<Dialog> implements ActionCompletedListener,
        PropertySheet.IPropertyValueChangedListener {
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
     * The visual action to create a new project activity
     */
    @Autowired 
    private NewProjectActivityVisualAction newProjectActivityVisualAction;
    /**
     * The visual action to delete a project activity
     */
    @Autowired
    private DeleteProjectActivityVisualAction deleteProjectActivityVisualAction;
    /**
     * Launches class level reports given a selected object.
     */
    @Autowired
    private LaunchClassLevelReportAction launchClassLevelReportAction;
    /**
     * action after delete pool
     */
    private Command commandDeleteActivity;
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    /**
     * Contains the filters grid in the bottom left side
     */
    private VerticalLayout lytActivitiesGrid;
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
    private VerticalLayout lytPropertySheet;
    /**
     * The grid with the activities
     */
    private Grid<BusinessObjectLight> gridActivities;
    /**
     * Activities data provider
     */
    private ListDataProvider<BusinessObjectLight> dataProviderActivities;
    /**
     * Activities list
     */
    private List<BusinessObjectLight> listActivities;
    /**
     * Object to save the selected activity
     */
    private BusinessObjectLight currentActivity;
    /**
     * Object to save the selected project
     */
    private BusinessObjectLight currentProject;
    /**
     * Property sheet
     */
    private PropertySheet propertySheet;
    /**
     * Object to delete the selected activity
     */
    private ActionButton btnActivityReports;
    
    public ProjectActivityDialog() {
        super(ProjectsModule.MODULE_ID);
    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
            buildActivitiesGrid(currentProject);
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
    }

    public void freeResources(DetachEvent ev) {
        this.newProjectActivityVisualAction.unregisterListener(this);
        this.deleteProjectActivityVisualAction.unregisterListener(this);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        this.newProjectActivityVisualAction.registerActionCompletedLister(this);
        this.deleteProjectActivityVisualAction.registerActionCompletedLister(this);

        if (parameters.containsKey("project")) {
            currentProject = (BusinessObjectLight) parameters.get("project");
            ConfirmDialog wdwActivity = new ConfirmDialog(ts,
                    ts.getTranslatedString("module.projects.actions.activity.button-activity.name"),
                    ts.getTranslatedString("module.general.messages.close"));
            wdwActivity.setDraggable(true);
            wdwActivity.setResizable(true);
            wdwActivity.getBtnConfirm().setVisible(false);
            wdwActivity.setContentSizeFull();
            wdwActivity.addDetachListener(event -> freeResources(event));
            // create command current layout
            commandDeleteActivity = () -> {
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
            lytActivitiesGrid = new VerticalLayout();
            lytActivitiesGrid.setClassName("bottom-grid");
            lytActivitiesGrid.setSpacing(false);
            lytActivitiesGrid.setPadding(false);
            lytActivitiesGrid.setMargin(false);
            lytActivitiesGrid.setHeightFull();
            buildActivitiesGrid(currentProject);
            lytActivitiesGrid.add(gridActivities);
            lytLeftSide.add(lytActivitiesGrid);
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
            lytFilterName.add(new Label(ts.getTranslatedString("module.projects.activity.label.description")));
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
            propertySheet = new PropertySheet(ts, new ArrayList<>());
            propertySheet.addPropertyValueChangedListener(this);
            propertySheet.setHeightFull();
            lytPropertySheet = new VerticalLayout();
            lytPropertySheet.setClassName("propertySheet");
            lytPropertySheet.setHeightFull();
            lytPropertySheet.setWidthFull();
            lytPropertySheet.setMargin(false);
            lytPropertySheet.setPadding(false);
            lytPropertySheet.setSpacing(false);
            lytPropertySheet.add(propertySheet);
            lytPropertySheet.setVisible(false);
            // Add content to right main layout 
            lytRightMain.add(lytRightHeader, lytPropertySheet);
            // End right side
            splitLayout.addToSecondary(lytRightMain);
            wdwActivity.setContent(splitLayout);
            return wdwActivity;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.projects.actions.project.delete-project-error")));
    }
    
    private void buildActivitiesGrid(BusinessObjectLight project) {
        try {
            listActivities = ps.getProjectActivities(project.getClassName(), project.getId());
            dataProviderActivities = new ListDataProvider<>(listActivities);
            // Create grid
            gridActivities = new Grid<>();
            gridActivities.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
            gridActivities.setSelectionMode(Grid.SelectionMode.SINGLE);
            gridActivities.setDataProvider(dataProviderActivities);
            gridActivities.setHeight("100%");
            
            gridActivities.addItemClickListener(event -> {
               currentActivity = event.getItem();
               updatePropertySheet(currentActivity);
               showFields(true);
            });

            Grid.Column<BusinessObjectLight> nameColumn = gridActivities.addColumn(TemplateRenderer.<BusinessObjectLight>of(
                    "<div>[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.className]]</font></div>")
                    .withProperty("name", BusinessObjectLight::getName)
                    .withProperty("className", BusinessObjectLight::getClassName));
            
            lytActivitiesGrid.removeAll();
            lytActivitiesGrid.add(gridActivities);
            createHeaderGrid(nameColumn, dataProviderActivities);
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Create a new input field to activity in the header row
     * @param dataProvider data provider to filter
     * @return the new input field filter
     */
    private void createHeaderGrid(Grid.Column column, ListDataProvider<BusinessObjectLight> dataProviderPools) {
        TextField txtSearchActivityByName = new TextField();
        txtSearchActivityByName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtSearchActivityByName.setValueChangeMode(ValueChangeMode.EAGER);
        txtSearchActivityByName.setWidthFull();
        txtSearchActivityByName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH,
                ts.getTranslatedString("module.projects.activity.filter")));
        txtSearchActivityByName.addValueChangeListener(event -> dataProviderPools.addFilter(
                activity -> StringUtils.containsIgnoreCase(activity.getName(),
                        txtSearchActivityByName.getValue())));
        // action button layout
        ActionButton btnAddPool = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                this.newProjectActivityVisualAction.getModuleAction().getDisplayName());
        btnAddPool.addClickListener(event -> {
            this.newProjectActivityVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("project", currentProject) )).open();
        });
        btnAddPool.setHeight("32px");
        
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setClassName("left-action-buttons");
        lytActionButtons.setSpacing(false);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setWidthFull();
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(txtSearchActivityByName, btnAddPool);

        HeaderRow filterRow = gridActivities.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }
    
    private void createRightControlButtons() {
        ActionButton btnDeleteActivity = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                this.deleteProjectActivityVisualAction.getModuleAction().getDisplayName());
        btnDeleteActivity.addClickListener(event -> {
            this.deleteProjectActivityVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("activity", currentActivity),
                    new ModuleActionParameter("commandClose", commandDeleteActivity)
            )).open();
        });
        
        btnActivityReports = new ActionButton(new ActionIcon(VaadinIcon.FILE_TABLE),
                this.launchClassLevelReportAction.getName());
        btnActivityReports.addClickListener(event -> {
            this.launchClassLevelReportAction.getVisualComponent(new ModuleActionParameterSet(
                                new ModuleActionParameter("businessObject", currentActivity)
            )).open();
        });
        
        lytRightActionButtons.add(btnDeleteActivity, btnActivityReports);
    }

        /**
     * Shows/Hides the labels and buttons in the header, also the property sheet.
     */
    private void showFields(boolean show) {
        lytRightHeader.setVisible(show);
        lytPropertySheet.setVisible(show);
    }
    
    /**
     * Property sheet for activities
     * @param object Activity to update
     */
    private void updatePropertySheet(BusinessObjectLight activity) {
        try {
            BusinessObject aWholeActivity = ps.getActivity(activity.getClassName(), activity.getId());
            propertySheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeActivity, ts, aem, mem));
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        try {
            Session session = UI.getCurrent().getSession().getAttribute(Session.class);
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
            ps.updateActivity(currentActivity.getClassName()
                    , currentActivity.getId()
                    , attributes
                    , session.getUser().getUserName()
            );
            if (property.getName().equals(Constants.PROPERTY_NAME)) {
                currentActivity.setName(String.valueOf(property.getValue()));
                buildActivitiesGrid(currentProject);
            }
            updatePropertySheet(currentActivity);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), 
                    ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                    AbstractNotification.NotificationType.INFO, ts).open();
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException
                | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            propertySheet.undoLastEdit();
        }
    }   
    
    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}