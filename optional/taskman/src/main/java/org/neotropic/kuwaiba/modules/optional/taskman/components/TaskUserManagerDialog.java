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
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfileLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.taskman.TaskManagerModule;
import org.neotropic.kuwaiba.modules.optional.taskman.actions.DeleteTaskUserVisualAction;
import org.neotropic.kuwaiba.modules.optional.taskman.actions.NewTaskUserVisualAction;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of task users.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@CssImport(value = "./css/poolConfigurationDialog.css")
@Component
public class TaskUserManagerDialog extends AbstractVisualAction<Dialog> implements ActionCompletedListener {
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
     * The visual action to subscribe a task user
     */
    @Autowired
    private NewTaskUserVisualAction newTaskUserVisualAction;
    /**
     * The visual action to unsubscribe a task user
     */
    @Autowired
    private DeleteTaskUserVisualAction deleteTaskUserVisualAction;
    /**
     * Layouts for users
     */
    private HorizontalLayout lytActionButtons;
    private VerticalLayout lytUsers;
    /**
     * The grid with the users
     */
    private Grid<UserProfileLight> gridUsers;
    /**
     * Object to save current task
     */
    private Task currentTask;
    /**
     * Object to save current user
     */
    private UserProfileLight currentUser;
    /**
     * Object to unsubscribe a task user
     */
    private ActionButton btnDeleteUser; 
    /**
     * Object to subscribe a task user
     */
    private ActionButton btnAddUser;
    
    public TaskUserManagerDialog() {
        super(TaskManagerModule.MODULE_ID);
    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
            buildUsersGrid(currentTask);
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();        
    }
    
    public void freeResources(DetachEvent ev) {
        this.newTaskUserVisualAction.unregisterListener(this);
        this.deleteTaskUserVisualAction.unregisterListener(this);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("task")) {
            currentTask = (Task) parameters.get("task");
            
            this.newTaskUserVisualAction.registerActionCompletedLister(this);
            this.deleteTaskUserVisualAction.registerActionCompletedLister(this);

            ConfirmDialog wdwUser = new ConfirmDialog(ts,
                    ts.getTranslatedString("module.taskman.task.actions.manage-users.name"),
                    ts.getTranslatedString("module.general.messages.close"));
            wdwUser.setDraggable(true);
            wdwUser.getBtnConfirm().setVisible(false);
            wdwUser.setContentSizeFull();
            wdwUser.setMinWidth("50%");
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
            lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
            // Create control buttons
            createControlButtons(currentTask);
            // Grid
            lytUsers = new VerticalLayout();
            lytUsers.setClassName("grig-pool-container");
            lytUsers.setHeightFull();
            lytUsers.setMargin(false);
            lytUsers.setSpacing(false);
            lytUsers.setPadding(false);
            buildUsersGrid(currentTask);
            lytUsers.add(gridUsers);
            lytUsers.setVisible(true);
            // Add content to main layout
            lytMain.add(lytUsers);
            wdwUser.setContent(lytMain);
            return wdwUser;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.taskman.task.actions.delete-task-error")));
    }

    private void buildUsersGrid(Task task) {
        try {
            List<UserProfileLight> listUsers;
            listUsers = aem.getSubscribersForTask(task.getId());
            ListDataProvider<UserProfileLight> dataProviderUsers = new ListDataProvider<>(listUsers);
            // Create grid
            gridUsers = new Grid<>();
            gridUsers.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
            gridUsers.setDataProvider(dataProviderUsers);
            gridUsers.setSelectionMode(Grid.SelectionMode.NONE);
            gridUsers.setHeightFull();
            
            gridUsers.addItemClickListener(event -> {
                currentUser = event.getItem();
                showFields(true);
            });
            
            Grid.Column<UserProfileLight> nameColumn = gridUsers.addColumn(UserProfileLight::getUserName)
                    .setHeader(ts.getTranslatedString("module.userman.user-name"));
                        
            lytUsers.removeAll();
            lytUsers.add(gridUsers);
            
            if (listUsers.isEmpty())
                labelInfoUser(nameColumn);
            else
                createHeaderGrid(nameColumn, dataProviderUsers);
        } catch (ApplicationObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void createControlButtons(Task task) {
        btnAddUser = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                this.newTaskUserVisualAction.getModuleAction().getDisplayName());
        btnAddUser.addClickListener(event -> {
            this.newTaskUserVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("task", task)
            )).open();
        });

        Command deleteTaskUser = () -> {
            showFields(false);
        };
        btnDeleteUser = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                this.deleteTaskUserVisualAction.getModuleAction().getDisplayName());
        btnDeleteUser.setEnabled(false);
        btnDeleteUser.addClickListener(event -> {
            this.deleteTaskUserVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("user", currentUser),
                    new ModuleActionParameter("task", task),
                    new ModuleActionParameter("commandClose", deleteTaskUser)
            )).open();
        });
        lytActionButtons.add(btnAddUser, btnDeleteUser);
    }
    
    private void createHeaderGrid(Grid.Column column, ListDataProvider<UserProfileLight> dataProviderUsers) {
        TextField txtSearchUserByName = new TextField();
        txtSearchUserByName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtSearchUserByName.setValueChangeMode(ValueChangeMode.EAGER);
        txtSearchUserByName.setWidthFull();
        txtSearchUserByName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH,
                ts.getTranslatedString("module.taskman.task.users.filter-users")));
        txtSearchUserByName.addValueChangeListener(event -> dataProviderUsers.addFilter(
                user -> StringUtils.containsIgnoreCase(user.getUserName(),
                        txtSearchUserByName.getValue())));
        // action button layout
        HorizontalLayout lytActionsButtons = new HorizontalLayout();
        lytActionsButtons.setClassName("left-action-buttons");
        lytActionsButtons.setSpacing(false);
        lytActionsButtons.setPadding(false);
        lytActionsButtons.setMargin(false);
        lytActionsButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionsButtons.add(txtSearchUserByName, lytActionButtons);
        
        HeaderRow filterRow = gridUsers.prependHeaderRow();
        filterRow.getCell(column).setComponent(lytActionsButtons);
    }
    
    private void labelInfoUser(Grid.Column column) {
        Label lblInfo = new Label();
        lblInfo.setVisible(true);
        lblInfo.setText(ts.getTranslatedString("module.taskman.task.users.info.no-user"));
        lblInfo.setWidthFull();
         // action button layout
        HorizontalLayout lytActionsButtons = new HorizontalLayout();
        lytActionsButtons.setClassName("left-action-buttons");
        lytActionsButtons.setSpacing(false);
        lytActionsButtons.setPadding(false);
        lytActionsButtons.setMargin(false);
        lytActionsButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionsButtons.add(lblInfo, lytActionButtons);
        
        HeaderRow filterRow = gridUsers.prependHeaderRow();
        filterRow.getCell(column).setComponent(lytActionsButtons);
    }
    
    private void showFields(boolean show) {
        btnDeleteUser.setEnabled(show);
    }
    
    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}