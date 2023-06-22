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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
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
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of unsubscribe a task user action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteTaskUserVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Close action command
     */
    private Command commandClose;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteTaskUserAction deleteTaskUserAction;

    public DeleteTaskUserVisualAction() {
        super(TaskManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        UserProfileLight selectedUser;
        Task selectedTask = null;

        if (parameters.containsKey("user")) {
            selectedUser = (UserProfileLight) parameters.get("user");

            if (parameters.containsKey("task"))
                selectedTask = (Task) parameters.get("task");

            if (parameters.containsKey("commandClose"))
                commandClose = (Command) parameters.get("commandClose");

            List<Task> listTask = aem.getTasks();
            ComboBox<Task> cbxTask = new ComboBox<>("", listTask);
            cbxTask.setValue(selectedTask);

            ConfirmDialog wdwUnsubscribeTaskUser = new ConfirmDialog(ts
                    , this.deleteTaskUserAction.getDisplayName()
                    , String.format(ts.getTranslatedString("module.taskman.task.actions.delete-task-user-confirm"), selectedUser.getUserName())
                    , ts.getTranslatedString("module.general.labels.delete"));
            try {
                List<UserProfileLight> listUsers = aem.getSubscribersForTask(cbxTask.getValue().getId());
                ComboBox<UserProfileLight> cbxUser = new ComboBox<>("", listUsers);
                cbxUser.setValue(selectedUser);

                wdwUnsubscribeTaskUser.getBtnConfirm().addClickListener((event) -> {
                    try {
                        deleteTaskUserAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("userId", cbxUser.getValue().getId()),
                                new ModuleActionParameter<>("taskId", cbxTask.getValue().getId())
                        ));

                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.taskman.task.actions.delete-task-user-success"), DeleteTaskUserAction.class));
                        wdwUnsubscribeTaskUser.close();
                        //refresh related grid
                        getCommandClose().execute();
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), DeleteTaskUserAction.class));
                        wdwUnsubscribeTaskUser.close();
                    }
                });
            } catch (ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
            return wdwUnsubscribeTaskUser;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.taskman.task.actions.delete-task-user-error")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteTaskUserAction;
    }
    
    /**
     * refresh grid
     * @return commandClose;Command; refresh action 
     */
    public Command getCommandClose() {
        return commandClose;
    }

    /**
     * @param commandClose;Command; refresh action 
     */
    public void setCommandClose(Command commandClose) {
        this.commandClose = commandClose;
    }
}