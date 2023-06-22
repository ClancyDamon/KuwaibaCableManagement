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
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.taskman.TaskManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete a task parameter action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteTaskParameterVisualAction extends AbstractVisualAction<Dialog> {
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
    private DeleteTaskParameterAction deleteTaskParameterAction;

    public DeleteTaskParameterVisualAction() {
        super(TaskManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        StringPair selectedParameter;
        Task selectedTask = null;

        if (parameters.containsKey("parameter")) {
            selectedParameter = (StringPair) parameters.get("parameter");

            if (parameters.containsKey("task"))
                selectedTask = (Task) parameters.get("task");
            
            if (parameters.containsKey("commandClose"))
                commandClose = (Command) parameters.get("commandClose");

            List<Task> listTask = aem.getTasks();
            ComboBox<Task> cbxTask = new ComboBox<>("", listTask);
            cbxTask.setValue(selectedTask);

            List<StringPair> listParameters = cbxTask.getValue().getParameters();
            ComboBox<StringPair> cbxParameters = new ComboBox<>("", listParameters);
            cbxParameters.setValue(selectedParameter);

            ConfirmDialog wdwdeleteTaskParameter = new ConfirmDialog(ts
                    , this.deleteTaskParameterAction.getDisplayName()
                    , String.format(ts.getTranslatedString("module.taskman.task.actions.delete-task-parameter-confirm"), selectedParameter.getKey())
                    , ts.getTranslatedString("module.general.labels.delete"));

            wdwdeleteTaskParameter.getBtnConfirm().addClickListener((event) -> {
                try {
                    deleteTaskParameterAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("task", cbxTask.getValue()),
                            new ModuleActionParameter<>("key", cbxParameters.getValue().getKey())
                    ));

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.taskman.task.actions.delete-task-parameter-success"), DeleteTaskParameterAction.class));
                    wdwdeleteTaskParameter.close();
                    //refresh related grid
                    getCommandClose().execute();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteTaskParameterAction.class));
                    wdwdeleteTaskParameter.close();
                }
            });
            return wdwdeleteTaskParameter;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.taskman.task.actions.delete-task-parameter-error")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteTaskParameterAction;
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