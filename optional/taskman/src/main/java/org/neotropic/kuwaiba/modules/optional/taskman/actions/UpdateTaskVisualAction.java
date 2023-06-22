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
package org.neotropic.kuwaiba.modules.optional.taskman.actions;

import com.neotropic.flow.component.paper.toggle.PaperToggleButton;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.taskman.TaskManagerModule;
import org.neotropic.kuwaiba.modules.optional.taskman.tools.TaskManagerExecutionType;
import org.neotropic.kuwaiba.modules.optional.taskman.tools.TaskManagerNotificationType;
import org.neotropic.kuwaiba.modules.optional.taskman.tools.TaskManagerRenderingTools;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of update a task action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class UpdateTaskVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private UpdateTaskAction updateTaskAction;
    /**
     * Dialog to update the task
     */
    private ConfirmDialog wdwUpdateTask;
    /**
     * action over main layout after update task
     */
    private Command updateTask;
    /**
     * Parameters
     */
    private static final String PARAM_TASK = "task";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_DESCRIPTION = "description";
    private static final String PARAM_ENABLE = "enabled";
    private static final String PARAM_COMMIT_ON_EXECUTE = "commitOnExecute";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_NOTIFICATIONTYPE = "notificationType";
    private static final String PARAM_START_TIME = "startTime";
    private static final String PARAM_EVERY_MINUTES = "everyxMinutes";
    private static final String PARAM_EXECUTIONTYPE = "executionType";
    private static final String PARAM_EXCEPTION = "exception";
    
    public UpdateTaskVisualAction() {
        super(TaskManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        Task task;
        ModuleActionParameterSet actionParameterSet = new ModuleActionParameterSet();
        
        if (parameters.containsKey(PARAM_TASK)) {
            task = (Task) parameters.get(PARAM_TASK);
            actionParameterSet.put(PARAM_TASK, task);
            
            String notificationType;
            String executeType;
            
            // load command from parent layout
            updateTask = (Command) parameters.get("commandUpdateTask");
                        
            // Init general properties
            Label headerGeneral = new Label(ts.getTranslatedString("module.taskman.task.properties-general.header"));
            
            TextField txtName = new TextField(ts.getTranslatedString("module.taskman.task.properties-general.name"));
            txtName.setValue(task.getName());
            txtName.setWidth("50%");
            txtName.addValueChangeListener(listener -> {
               if (!txtName.getValue().equals(task.getName()))
                   actionParameterSet.put(PARAM_NAME, txtName.getValue());
            });
            
            TextField txtDescription = new TextField(ts.getTranslatedString("module.taskman.task.properties-general.description"));
            txtDescription.setValue(task.getDescription());
            txtDescription.setWidth("50%");
            txtDescription.addValueChangeListener(listener -> {
               if (!txtDescription.getValue().equals(task.getDescription()))
                   actionParameterSet.put(PARAM_DESCRIPTION, txtDescription.getValue());
            });
            
            PaperToggleButton btnEnable = new PaperToggleButton(ts.getTranslatedString("module.general.labels.enable"));
            btnEnable.setChecked(task.isEnabled());
            btnEnable.setWidth("50%");
            btnEnable.addValueChangeListener(listener -> {
               if (btnEnable.getChecked() != task.isEnabled())
                   actionParameterSet.put(PARAM_ENABLE, btnEnable.getChecked());
            });
            
            PaperToggleButton btnCommit = new PaperToggleButton(ts.getTranslatedString("module.taskman.task.properties-general.commit-on-execute"));
            btnCommit.setChecked(task.commitOnExecute());
            btnCommit.setWidth("50%");
            btnCommit.addValueChangeListener(listener -> {
               if (btnCommit.getChecked() != task.commitOnExecute())
                   actionParameterSet.put(PARAM_COMMIT_ON_EXECUTE, btnCommit.getChecked());
            });
            // End general properties
            
            // Init scheduling properties
            Label headerScheduling = new Label(ts.getTranslatedString("module.taskman.task.properties-scheduling.header"));
            
            DateTimePicker dateStart = new DateTimePicker(ts.getTranslatedString("module.taskman.task.properties-scheduling.start-time"));
            dateStart.setValue(TaskManagerRenderingTools.convertLongToLocalDateTime(task.getSchedule().getStartTime()));
            dateStart.addValueChangeListener(listener -> {
               if (!dateStart.getValue().equals(TaskManagerRenderingTools.convertLongToLocalDateTime(task.getSchedule().getStartTime())))
                   actionParameterSet.put(PARAM_START_TIME, TaskManagerRenderingTools.convertLocalDateTimeToLong(dateStart.getValue()));
            });
                    
            IntegerField intEveryxMinutes = new IntegerField(ts.getTranslatedString("module.taskman.task.properties-scheduling.every-x-minutes"));
            intEveryxMinutes.setValue(task.getSchedule().getEveryXMinutes());
            intEveryxMinutes.addValueChangeListener(listener -> {
               if (!intEveryxMinutes.getValue().equals(task.getSchedule().getEveryXMinutes()))
                   actionParameterSet.put(PARAM_EVERY_MINUTES, intEveryxMinutes.getValue());
            });
            
            switch (task.getSchedule().getExecutionType()) {
                case 1:
                    executeType = ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.system-startup");
                    break;
                case 2:
                    executeType = ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.user-login");
                    break;
                case 3:
                    executeType = ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.loop");
                    break;
                default:
                    executeType = ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.on-demand");
                    break;
            }
            ComboBox<TaskManagerExecutionType> cmbExecutionType = new ComboBox<>(ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type"));
            cmbExecutionType.setItems(
                    new TaskManagerExecutionType(ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.on-demand"), 0),
                    new TaskManagerExecutionType(ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.system-startup"), 1),
                    new TaskManagerExecutionType(ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.user-login"), 2),
                    new TaskManagerExecutionType(ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.loop"), 3)
            );
            cmbExecutionType.setValue(new TaskManagerExecutionType(executeType, task.getSchedule().getExecutionType()));
            cmbExecutionType.setAllowCustomValue(false);
            cmbExecutionType.addValueChangeListener(listener -> {
               if (cmbExecutionType.getValue().getType() != task.getSchedule().getExecutionType())
                   actionParameterSet.put(PARAM_EXECUTIONTYPE, cmbExecutionType.getValue().getType());
            });
            // End scheduling properties
            
            // Init notification type properties
            Label headerNotificationType = new Label(ts.getTranslatedString("module.taskman.task.properties-notification.header"));

            TextField txtEmail = new TextField(ts.getTranslatedString("module.taskman.task.properties-notification.email"));
            txtEmail.setValue(task.getNotificationType().getEmail());
            txtEmail.setWidth("50%");
            txtEmail.addValueChangeListener(listener -> {
                if (!txtEmail.getValue().equals(task.getNotificationType().getEmail()))
                    actionParameterSet.put(PARAM_EMAIL, txtEmail.getValue());
            });
            
            switch (task.getNotificationType().getNotificationType()) {
                case 1:
                    notificationType = ts.getTranslatedString("module.taskman.task.properties-notification.type.client-managed");
                    break;
                case 2:
                    notificationType = ts.getTranslatedString("module.taskman.task.properties-notification.type.email");
                    break;
                default:
                    notificationType = ts.getTranslatedString("module.taskman.task.properties-notification.type.no-notification");
                    break;
            }
            ComboBox<TaskManagerNotificationType> cmbnotificationType = new ComboBox<>(ts.getTranslatedString("module.taskman.task.properties-notification.type"));
            cmbnotificationType.setItems(
                    new TaskManagerNotificationType(ts.getTranslatedString("module.taskman.task.properties-notification.type.no-notification"), 0),
                    new TaskManagerNotificationType(ts.getTranslatedString("module.taskman.task.properties-notification.type.client-managed"), 1),
                    new TaskManagerNotificationType(ts.getTranslatedString("module.taskman.task.properties-notification.type.email"), 2)
            );
            cmbnotificationType.setValue(new TaskManagerNotificationType(notificationType, task.getNotificationType().getNotificationType()));
            cmbnotificationType.setAllowCustomValue(false);
            cmbnotificationType.setWidth("50%");
            cmbnotificationType.addValueChangeListener(listener -> {
               if (cmbnotificationType.getValue().getType() != task.getNotificationType().getNotificationType())
                   actionParameterSet.put(PARAM_NOTIFICATIONTYPE, cmbnotificationType.getValue().getType());
            });
            
            // Dialog
            wdwUpdateTask = new ConfirmDialog(ts
            , this.updateTaskAction.getDisplayName()
            , ts.getTranslatedString("module.general.messages.save"));
            wdwUpdateTask.setDraggable(true);
            
            wdwUpdateTask.getBtnConfirm().addClickListener(event -> { 
                try {
                    ActionResponse actionResponse = updateTaskAction.getCallback().execute(actionParameterSet);
                  
                    if (actionResponse.containsKey(PARAM_EXCEPTION))
                        throw new ModuleActionException(((Exception) actionResponse.get(PARAM_EXCEPTION)).getLocalizedMessage());

                    getUpdateTask().execute();
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.taskman.task.actions.update-task-success"), UpdateTaskAction.class));
                    wdwUpdateTask.close();
                } catch (ModuleActionException ex) {
                     fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), UpdateTaskAction.class));
                }
            });
            wdwUpdateTask.getBtnConfirm().setEnabled(true);
            txtName.addValueChangeListener((event) -> {
               wdwUpdateTask.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbExecutionType.isEmpty() && !cmbnotificationType.isEmpty());
            });
            cmbExecutionType.addValueChangeListener((event) -> {
                wdwUpdateTask.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbExecutionType.isEmpty() && !cmbnotificationType.isEmpty());
            });
            cmbnotificationType.addValueChangeListener((event) -> {
                wdwUpdateTask.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbExecutionType.isEmpty() && !cmbnotificationType.isEmpty());
            });
            
            HorizontalLayout lytGeneralProperties = new HorizontalLayout(txtName, txtDescription);
            lytGeneralProperties.setWidthFull();
            
            HorizontalLayout lytGeneralToggleBtn = new HorizontalLayout(btnCommit, btnEnable);
            lytGeneralToggleBtn.setWidthFull();
            
            HorizontalLayout lytSchedulingProperties = new HorizontalLayout(dateStart, intEveryxMinutes, cmbExecutionType);
            lytSchedulingProperties.setWidthFull();
            
            HorizontalLayout lytNotificationProperties = new HorizontalLayout(txtEmail, cmbnotificationType);
            lytNotificationProperties.setWidthFull();
            
            VerticalLayout lytMain = new VerticalLayout(headerGeneral, lytGeneralProperties, lytGeneralToggleBtn,
                    headerScheduling, lytSchedulingProperties, headerNotificationType, lytNotificationProperties);
            
            // Add content to window
            wdwUpdateTask.setContent(lytMain);
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.taskman.task.actions.delete-task-error")));
        return wdwUpdateTask;
    }

    @Override
    public AbstractAction getModuleAction() {
        return updateTaskAction;
    }

    public Command getUpdateTask() {
        return updateTask;
    }

    public void setUpdateTask(Command updateTask) {
        this.updateTask = updateTask;
    }
}