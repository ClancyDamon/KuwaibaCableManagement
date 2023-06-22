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
package org.neotropic.kuwaiba.modules.optional.taskman.components;

import com.vaadin.flow.component.DetachEvent;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.Command;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.taskman.TaskManagerModule;
import org.neotropic.kuwaiba.modules.optional.taskman.actions.DeleteTaskParameterVisualAction;
import org.neotropic.kuwaiba.modules.optional.taskman.actions.NewTaskParameterVisualAction;
import org.neotropic.kuwaiba.modules.optional.taskman.actions.UpdateTaskParameterVisualAction;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of task parameters.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@CssImport(value = "./css/poolConfigurationDialog.css")
@Component
public class TaskParametersManagerDialog extends AbstractVisualAction<Dialog> implements ActionCompletedListener {
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
     * The visual action to create a task parameter
     */
    @Autowired
    private NewTaskParameterVisualAction newTaskParameterVisualAction;
    /**
     * The visual action to delete a task parameter
     */
    @Autowired
    private DeleteTaskParameterVisualAction deleteTaskParameterVisualAction;
    /**
     * The visual action to update a task parameter
     */
    @Autowired
    private UpdateTaskParameterVisualAction updateTaskParameterVisualAction;
    /**
     * Layouts for users
     */
    private HorizontalLayout lytActionButtons;
    private VerticalLayout lytParameters;
    /**
     * The grid with the list task parameters
     */
    private Grid<StringPair> gridParameters;
    /**
     * Object to save the selected task
     */
    private Task currentTask;
    /**
     * Object to save the select task parameter
     */
    private StringPair currentParameter;
    /**
     * Object to delete a task parameter
     */
    private ActionButton btnDeleteParameter; 
    /**
     * Object to create a task parameter
     */
    private ActionButton btnAddParameter;
    /**
     * Object to update a task parameter
     */
    private ActionButton btnUpdateParameter;

    public TaskParametersManagerDialog() {
        super(TaskManagerModule.MODULE_ID);
    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
                if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
            buildParametersGrid(currentTask);
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();  
    }
    
    public void freeResources(DetachEvent ev) {
        this.newTaskParameterVisualAction.unregisterListener(this);
        this.deleteTaskParameterVisualAction.unregisterListener(this);
        this.updateTaskParameterVisualAction.unregisterListener(this);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
         if (parameters.containsKey("task")) {
            currentTask = (Task) parameters.get("task");
            
            this.newTaskParameterVisualAction.registerActionCompletedLister(this);
            this.deleteTaskParameterVisualAction.registerActionCompletedLister(this);
            this.updateTaskParameterVisualAction.registerActionCompletedLister(this);

            ConfirmDialog wdwUser = new ConfirmDialog(ts,
                    ts.getTranslatedString("module.taskman.task.actions.manage-parameters.name"),
                    ts.getTranslatedString("module.general.messages.close"));
            wdwUser.setDraggable(true);
            wdwUser.getBtnConfirm().setVisible(false);
            wdwUser.setContentSizeFull();
            wdwUser.setMinWidth("60%");
            wdwUser.setMinHeight("50%");
            wdwUser.setResizable(true);
            wdwUser.addDetachListener(event -> freeResources(event));

            //--Main side
            VerticalLayout lytMain = new VerticalLayout();
            lytMain.setClassName("left-side-dialog");
            lytMain.setMargin(false);
            lytMain.setPadding(false);
            lytMain.setSpacing(true);
            lytMain.setHeightFull();
            lytMain.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
            // Layout for action buttons
            lytActionButtons = new HorizontalLayout();
            lytActionButtons.setClassName("button-container");
            lytActionButtons.setPadding(false);
            lytActionButtons.setMargin(false);
            lytActionButtons.setSpacing(false);
            lytActionButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
            createControlButtons(currentTask);
            // Grid
            lytParameters = new VerticalLayout();
            lytParameters.setClassName("grig-pool-container");
            lytParameters.setHeightFull();
            lytParameters.setMargin(false);
            lytParameters.setSpacing(false);
            lytParameters.setPadding(false);
            buildParametersGrid(currentTask);
            lytParameters.add(gridParameters);
            lytParameters.setVisible(true);
            // Add content to main layout
            lytMain.add(lytParameters);
            wdwUser.setContent(lytMain);
            return wdwUser;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.taskman.task.actions.delete-task-error")));
    }

    private void buildParametersGrid(Task task) {
        try {
            List<StringPair> listParameters;
            listParameters = aem.getTask(task.getId()).getParameters();
            ListDataProvider<StringPair> dataProviderParameters = new ListDataProvider<>(listParameters);
            // Create grid
            gridParameters = new Grid<>();
            gridParameters.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
            gridParameters.setSelectionMode(Grid.SelectionMode.SINGLE);
            gridParameters.setHeightFull();
            gridParameters.setDataProvider(dataProviderParameters);
                        
            Grid.Column<StringPair> nameColumn = gridParameters.addColumn(StringPair::getKey)
                    .setHeader(ts.getTranslatedString("module.taskman.task.parameters.name"));
            
            gridParameters.addColumn(StringPair::getValue)
                    .setHeader(ts.getTranslatedString("module.taskman.task.parameters.value"));
            
            gridParameters.asSingleSelect().addValueChangeListener(event -> {
                    currentParameter = event.getValue();
                    showFields(true);
            });
            
            lytParameters.removeAll();
            lytParameters.add(gridParameters);
            
            if (listParameters.isEmpty())
                labelInfoParameter(nameColumn);
            else
                createHeaderGrid(nameColumn, dataProviderParameters);
        } catch (ApplicationObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    private void createControlButtons(Task task) {
        btnAddParameter = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O), this.newTaskParameterVisualAction.getModuleAction().getDisplayName());
        btnAddParameter.addClickListener(event -> {
            this.newTaskParameterVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("task", task))).open();
        });
        
        Command deleteTaskParameter = () -> {
            showFields(false);
        };
        btnDeleteParameter = new ActionButton(new ActionIcon(VaadinIcon.TRASH), this.deleteTaskParameterVisualAction.getModuleAction().getDisplayName());
        btnDeleteParameter.setEnabled(false);
        btnDeleteParameter.addClickListener(event -> {
            this.deleteTaskParameterVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("parameter", currentParameter),
                    new ModuleActionParameter("task", task),
                    new ModuleActionParameter("commandClose", deleteTaskParameter)
            )).open();
        });
        
        btnUpdateParameter = new ActionButton(new ActionIcon(VaadinIcon.EDIT), this.updateTaskParameterVisualAction.getModuleAction().getDisplayName());
        btnUpdateParameter.setEnabled(false);
        btnUpdateParameter.addClickListener(event -> {
            this.updateTaskParameterVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("parameter", currentParameter),
                    new ModuleActionParameter("task", task)
            )).open();
        });
        lytActionButtons.add(btnAddParameter, btnUpdateParameter, btnDeleteParameter);
    }
    
    private void labelInfoParameter(Grid.Column column) {
        Label lblInfo = new Label();
        lblInfo.setVisible(true);
        lblInfo.setText(ts.getTranslatedString("module.taskman.task.parameters.info.no-parameters"));
        lblInfo.setWidthFull();
        // action button layout
        HorizontalLayout lytActionsButtons = new HorizontalLayout();
        lytActionsButtons.setClassName("left-action-buttons");
        lytActionsButtons.setSpacing(false);
        lytActionsButtons.setPadding(false);
        lytActionsButtons.setMargin(false);
        lytActionsButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionsButtons.add(lblInfo, lytActionButtons);

        HeaderRow filterRow = gridParameters.prependHeaderRow();
        filterRow.getCell(column).setComponent(lytActionsButtons);
    }

    private void createHeaderGrid(Grid.Column column, ListDataProvider<StringPair> dataProviderParameters) {
        TextField txtSearchParameterByName = new TextField();
        txtSearchParameterByName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtSearchParameterByName.setValueChangeMode(ValueChangeMode.EAGER);
        txtSearchParameterByName.setWidthFull();
        txtSearchParameterByName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH,
                ts.getTranslatedString("module.taskman.task.parameters.filter-parameters")));
        txtSearchParameterByName.addValueChangeListener(event -> dataProviderParameters.addFilter(
                parameter -> StringUtils.containsIgnoreCase((parameter.getKey()),
                        txtSearchParameterByName.getValue())));
        // action button layout
        HorizontalLayout lytActionsButtons = new HorizontalLayout();
        lytActionsButtons.setClassName("left-action-buttons");
        lytActionsButtons.setSpacing(false);
        lytActionsButtons.setPadding(false);
        lytActionsButtons.setMargin(false);
        lytActionsButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionsButtons.add(txtSearchParameterByName, lytActionButtons);
        
        HeaderRow filterRow = gridParameters.prependHeaderRow();
        filterRow.getCell(column).setComponent(lytActionsButtons);
    }
    
    private void showFields(boolean show) {
        btnDeleteParameter.setEnabled(show);
        btnUpdateParameter.setEnabled(show);
    }
    
    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
}