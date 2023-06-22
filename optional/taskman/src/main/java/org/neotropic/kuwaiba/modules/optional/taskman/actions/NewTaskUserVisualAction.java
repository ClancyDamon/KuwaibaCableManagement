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
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.taskman.TaskManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of subscribe a new task user action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewTaskUserVisualAction extends AbstractVisualAction<Dialog>  {
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
    private NewTaskUserAction newTaskUserAction;

    public NewTaskUserVisualAction() {
        super(TaskManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        Task selectedTask = null;
        if (parameters.containsKey("task"))
            selectedTask = (Task) parameters.get("task");

        List<Task> listTask = aem.getTasks();
        ComboBox<Task> cbxTask = new ComboBox<>(ts.getTranslatedString("module.taskman.task.label.name"), listTask);
        cbxTask.setAllowCustomValue(false);
        cbxTask.setItemLabelGenerator(item -> item.getName());
        cbxTask.setReadOnly(true);
        cbxTask.setSizeFull();

        if (selectedTask != null) {
            cbxTask.setValue(selectedTask);
            cbxTask.setEnabled(false);
        }

        List<UserProfile> listUsers = aem.getUsers();
        ComboBox<UserProfile> cbxUsers = new ComboBox<>(ts.getTranslatedString("module.taskman.task.actions.new-task-user-names"), listUsers);
        cbxUsers.setAllowCustomValue(false);
        cbxUsers.setItemLabelGenerator(item -> String.format("%s %s (%s)", item.getLastName(), item.getFirstName(), item.getUserName()));
        cbxUsers.setSizeFull();

        // Window to add a new task user
        ConfirmDialog wdwNewTaskUser = new ConfirmDialog(ts
            , this.newTaskUserAction.getDisplayName()
            , ts.getTranslatedString("module.general.labels.create"));
        wdwNewTaskUser.setDraggable(true);
        //To show errors or warnings related to the input parameters
        Label lblMessages = new Label();

        wdwNewTaskUser.getBtnConfirm().addClickListener(event -> {
            try {
                if (cbxTask.getValue() == null)
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                else {
                    ActionResponse actionResponse = newTaskUserAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("userId", cbxUsers.getValue().getId()),
                            new ModuleActionParameter<>("taskId", cbxTask.getValue().getId())
                    ));

                    if (actionResponse.containsKey("exception"))
                        throw new ModuleActionException(((Exception) actionResponse.get("exception")).getLocalizedMessage());

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.taskman.task.actions.new-task-user-success"), NewTaskUserAction.class));
                    wdwNewTaskUser.close();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewTaskUserAction.class));
            }
        });

        wdwNewTaskUser.getBtnConfirm().setEnabled(false);
        cbxUsers.addValueChangeListener((event) -> {
            wdwNewTaskUser.getBtnConfirm().setEnabled(!cbxUsers.isEmpty());
        });

        // Add content to window
        wdwNewTaskUser.setContent(cbxTask, cbxUsers);
        return wdwNewTaskUser;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newTaskUserAction;
    }
}