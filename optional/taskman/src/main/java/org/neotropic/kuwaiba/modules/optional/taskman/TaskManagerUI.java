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
package org.neotropic.kuwaiba.modules.optional.taskman;

import com.neotropic.flow.component.aceeditor.AceEditor;
import com.neotropic.flow.component.aceeditor.AceMode;
import com.neotropic.flow.component.paper.toggle.PaperToggleButton;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
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
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.taskman.actions.DeleteTaskVisualAction;
import org.neotropic.kuwaiba.modules.optional.taskman.actions.NewTaskVisualAction;
import org.neotropic.kuwaiba.modules.optional.taskman.actions.UpdateTaskVisualAction;
import org.neotropic.kuwaiba.modules.optional.taskman.components.TaskNotificationDialog;
import org.neotropic.kuwaiba.modules.optional.taskman.components.TaskParametersManagerDialog;
import org.neotropic.kuwaiba.modules.optional.taskman.components.TaskUserManagerDialog;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Task Manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "taskman", layout = TaskManagerLayout.class)
public class TaskManagerUI extends VerticalLayout implements ActionCompletedListener, HasDynamicTitle, AbstractUI {
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
     * The visual action to create a task
     */
    @Autowired
    private NewTaskVisualAction newTaskVisualAction;
    /**
     * The visual action to delete a task
     */
    @Autowired
    private DeleteTaskVisualAction deleteTaskVisualAction;
    /**
     * The visual action to update a task
     */
    @Autowired
    private UpdateTaskVisualAction updateTaskVisualAction;
    /**
     * The visual action to manage task users
     */
    @Autowired
    private TaskUserManagerDialog taskUserManagerDialog;
    /**
     * The visual action to manage task parameters
     */
    @Autowired
    private TaskParametersManagerDialog taskParametersManagerDialog;
    /**
     * The visual action to show task result notification
     */
    @Autowired
    private TaskNotificationDialog taskNotificationDialog;
    /**
     * The grid with the list task
     */
    private Grid<Task> gridTask;
    /**
     * Object to save the selected task
     */
    private Task currentTask;
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    /**
     * Button used to create a new task
     */
    private ActionButton btnAddTask;
    /**
     * Button used to delete task preselected
     */
    private ActionButton btnDeleteTask;
    /**
     * Task name
     */
    private Label lblTaskName;
    /**
     * Content layouts 
     */
    private VerticalLayout lytListTask;
    private HorizontalLayout lytFilterName;
    private VerticalLayout lytLeftSide;
    private HorizontalLayout lytRightActionButtons;
    private VerticalLayout lytRightHeader;
    private VerticalLayout lytScript;
    /**
     * Task AceEditor 
     */
    private AceEditor editorScript;
    /**
     * Button to save the value of commit on execute 
     */
    private PaperToggleButton btnCommit;
    
    public TaskManagerUI() {
        setSizeFull();
    }

    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
            refreshTaskGrid();
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
       this.newTaskVisualAction.unregisterListener(this);
       this.deleteTaskVisualAction.unregisterListener(this);
       this.updateTaskVisualAction.unregisterListener(this);
       this.taskUserManagerDialog.unregisterListener(this);
       this.taskParametersManagerDialog.unregisterListener(this);
       this.taskNotificationDialog.unregisterListener(this);
    }
    
     @Override
    public void initContent() {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);

        this.newTaskVisualAction.registerActionCompletedLister(this);
        this.deleteTaskVisualAction.registerActionCompletedLister(this);
        this.updateTaskVisualAction.registerActionCompletedLister(this);
        this.taskUserManagerDialog.registerActionCompletedLister(this);
        this.taskParametersManagerDialog.registerActionCompletedLister(this);
        this.taskNotificationDialog.registerActionCompletedLister(this);
                
        btnAddTask = new ActionButton(new Icon(VaadinIcon.PLUS_SQUARE_O), this.newTaskVisualAction.getModuleAction().getDisplayName());
        btnAddTask.addClickListener(event -> {
            this.newTaskVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
        });
        btnAddTask.setHeight("32px");
        
        splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25);
        // -> Left side
        buildTaskGrid();        
        // Layout for task grid
        lytListTask = new VerticalLayout();
        lytListTask.setClassName("bottom-grid");
        lytListTask.setHeightFull();
        lytListTask.setPadding(false);
        lytListTask.setSpacing(false);
        lytListTask.setMargin(false);
        lytListTask.add(gridTask);
        // Main left Layout 
        lytLeftSide = new VerticalLayout();
        lytLeftSide.setClassName("left-side");
        lytLeftSide.setSizeFull();
        lytLeftSide.setMargin(false);
        lytLeftSide.setSpacing(false);
        lytLeftSide.setId("left-lyt");
        lytLeftSide.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        lytLeftSide.add(lytListTask);
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
        lytRightControls.setClassName("left-action-combobox");
        lytRightControls.setPadding(false);
        lytRightControls.setMargin(false);
        lytRightControls.setSpacing(false);
        lytRightControls.setWidthFull();
        lytRightControls.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytRightControls.add(lytFilterName, lytRightActionButtons);
        // Layout right header
        lytRightHeader = new VerticalLayout();
        lytRightHeader.setClassName("header-script-control");
        lytRightHeader.setPadding(false);
        lytRightHeader.setMargin(false);
        lytRightHeader.setSpacing(false);
        lytRightHeader.add(lytRightControls);
        lytRightHeader.setVisible(false);
        // Layout script
        lytScript = new VerticalLayout();
        lytScript.setClassName("script-editor");
        lytScript.setMargin(false);
        lytScript.setPadding(true);
        lytScript.setSpacing(false);
        lytScript.add();
        // Add content to right main layout
        lytRightMain.add(lytRightHeader, lytScript);
        // Add to split layout
        splitLayout.addToPrimary(lytLeftSide);
        splitLayout.addToSecondary(lytRightMain);
        add(splitLayout);
    }
    
    private void createRightControlButtons() {
        Label lblTask = new Label(ts.getTranslatedString("module.taskman.task.header-main"));
        lblTask.setClassName("dialog-title");
        lblTaskName = new Label();
        lblTaskName.setClassName("dialog-title");
        lytFilterName.add(new Html("<span>&nbsp;</span>"), lblTask, lblTaskName);

        Command deleteTask = () -> {
            currentTask = null;
            showFields(false);
        };
        btnDeleteTask = new ActionButton(new Icon(VaadinIcon.TRASH), this.deleteTaskVisualAction.getModuleAction().getDisplayName());
        btnDeleteTask.addClickListener(event -> {
            this.deleteTaskVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("task", currentTask),
                    new ModuleActionParameter("commandClose", deleteTask)
            )).open();
        });

        ActionButton btnEditProperties = new ActionButton(new Icon(VaadinIcon.EDIT)
                , ts.getTranslatedString(this.updateTaskVisualAction.getModuleAction().getDisplayName()));
        btnEditProperties.addClickListener(event -> launchPropertiesDialog(currentTask));
        
        ActionButton btnUsers = new ActionButton(new ActionIcon(VaadinIcon.USERS)
                , ts.getTranslatedString("module.taskman.task.actions.manage-users.name"));
        btnUsers.addClickListener(event -> launchUsersDialog(currentTask));

        ActionButton btnParameters = new ActionButton(new ActionIcon(VaadinIcon.COG)
                , ts.getTranslatedString("module.taskman.task.actions.manage-parameters.name"));
        btnParameters.addClickListener(event -> launchParametersDialog(currentTask));

        ActionButton btnExecuteTask = new ActionButton(new Icon(VaadinIcon.PLAY)
                , ts.getTranslatedString("module.taskman.task.actions.execute-task.name"));
        btnExecuteTask.addClickListener(event -> {
            try {
                aem.updateTaskProperties(currentTask.getId(), Constants.PROPERTY_SCRIPT, editorScript.getValue());
                launchTaskNotificationDialog(currentTask);
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });

        ActionButton btnSave = new ActionButton(new Icon(VaadinIcon.DOWNLOAD), ts.getTranslatedString("module.taskman.task.properties-button.save"));
        btnSave.addClickListener(event -> {
            try {
                aem.updateTaskProperties(currentTask.getId(), Constants.PROPERTY_SCRIPT, editorScript.getValue());
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.taskman.task.properties-button.notification-saved"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });
        
        btnCommit = new PaperToggleButton();
        btnCommit.setClassName("green", true);
        btnCommit.addClassName("icon-button");
        btnCommit.addValueChangeListener(event -> {
            try {
                currentTask.setCommitOnExecute(event.getValue());
                aem.updateTaskProperties(currentTask.getId(), Constants.PROPERTY_COMMIT_ON_EXECUTE, Boolean.toString(event.getValue()));
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });
        btnCommit.getElement().setProperty("title", ts.getTranslatedString("module.taskman.task.properties-general.commit-on-execute"));

        lytRightActionButtons.add(btnDeleteTask, btnEditProperties, btnUsers, btnParameters, btnExecuteTask, btnSave, btnCommit);
    }
        
    private void createScript(Task task) {
        Label lblScript = new Label(ts.getTranslatedString("module.general.labels.script"));
        lblScript.setClassName("bold-font");
        editorScript = new AceEditor();
        editorScript.setMode(AceMode.groovy);
        editorScript.setValue(task.getScript());
        editorScript.addAceEditorValueChangedListener(event -> {
            task.setScript(editorScript.getValue());
        });
        lytScript.removeAll();
        lytScript.add(lblScript, editorScript);
    }

    private void buildTaskGrid() {
        List<Task> listTask = aem.getTasks();
        ListDataProvider<Task> dataProvider = new ListDataProvider<>(listTask);
        gridTask = new Grid<>();
        gridTask.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        gridTask.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridTask.setHeightFull();
        gridTask.setDataProvider(dataProvider);
        
        gridTask.addSelectionListener(event -> {
           event.getFirstSelectedItem().ifPresent(obj -> {
               showFields(true);
               currentTask = obj;
               lblTaskName.setText(obj.getName());
               createScript(obj);
               btnCommit.setChecked(currentTask.commitOnExecute());
           });
        });
        
        Grid.Column<Task> nameColumn = gridTask.addColumn(TemplateRenderer.<Task>of(
                "<div>[[item.name]]</div>")
                .withProperty("name", Task::getName))
                .setKey(ts.getTranslatedString("module.general.labels.name"));
        
        if (listTask.isEmpty())
            labelInfoVariable(nameColumn);
        else
            createTxtFieldTaskName(nameColumn, dataProvider);
    }
    
    /**
     * Create a new input field to filter tasks in the header row.
     * @param dataProvider Data provider to filter.
     */
    private void createTxtFieldTaskName(Grid.Column column, ListDataProvider<Task> dataProvider) {
        TextField txtTaskName = new TextField(ts.getTranslatedString("module.taskman.task.label.tasks"));
        txtTaskName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtTaskName.setValueChangeMode(ValueChangeMode.EAGER);
        txtTaskName.setWidthFull();
        txtTaskName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH, ts.getTranslatedString("module.taskman.task.label.filter-task")));
        // object name filter
        txtTaskName.addValueChangeListener(event -> dataProvider.addFilter(
                task -> StringUtils.containsIgnoreCase(task.getName(),
                        txtTaskName.getValue())));
        // action button layout
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setId("actions-left-lyt");
        lytActionButtons.setSpacing(true);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        lytActionButtons.add(txtTaskName, btnAddTask);

        HeaderRow filterRow = gridTask.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }
    
    /**
     * Adds a Label to show information and button for add task  
     */
    private void labelInfoVariable(Grid.Column column) {
        Label lblInfo = new Label(ts.getTranslatedString("module.taskman.task.label.no-tasks"));
        lblInfo.setVisible(true);
        lblInfo.setWidthFull();
         // action button layout
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setId("actions-left-lyt");
        lytActionButtons.setSpacing(true);
        lytActionButtons.setMargin(false);
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(lblInfo, btnAddTask);
        
        HeaderRow filterRow = gridTask.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }
    
    private void refreshTaskGrid() {
        lytListTask.remove(gridTask);
        buildTaskGrid();
        lytListTask.add(gridTask);
    }
    
    /**
     * Shows/Hides the labels and buttons in the header, also the script. 
     */
    private void showFields(boolean show) {
        lytRightHeader.setVisible(show);
        lytScript.setVisible(show);
    }
    
    private void launchPropertiesDialog(Task task) {
        Command commandUpdateTask = () -> {
          refreshTaskGrid();
        };
        this.updateTaskVisualAction.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter("task", task),
                new ModuleActionParameter("commandUpdateTask", commandUpdateTask)
        )).open();
    }
    
    private void launchUsersDialog(Task task) {
        this.taskUserManagerDialog.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter("task", task)
        )).open();
    }
    
    private void launchParametersDialog(Task task) {
        this.taskParametersManagerDialog.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter("task", task)
        )).open();
    }
    
    private void launchTaskNotificationDialog(Task task) {
        this.taskNotificationDialog.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter("task", task)
        )).open();
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.taskman.title");
    }    
}