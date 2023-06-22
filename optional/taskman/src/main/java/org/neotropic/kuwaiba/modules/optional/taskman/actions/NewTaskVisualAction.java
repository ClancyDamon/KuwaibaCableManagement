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

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.taskman.TaskManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new task action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewTaskVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewTaskAction newTaskAction;

    public NewTaskVisualAction() {
        super(TaskManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        TextField txtName = new TextField(ts.getTranslatedString("module.taskman.task.properties-general.name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();

        TextField txtDescription = new TextField(ts.getTranslatedString("module.taskman.task.properties-general.description"));
        txtDescription.setSizeFull();

        ConfirmDialog wdwNewTask = new ConfirmDialog(ts
            , this.newTaskAction.getDisplayName()
            , ts.getTranslatedString("module.general.labels.create"));
        wdwNewTask.setDraggable(true);

        wdwNewTask.getBtnConfirm().addClickListener(event -> {
            try {
                newTaskAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>("name", txtName.getValue()),
                        new ModuleActionParameter<>("description", txtDescription.getValue())
                ));

                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                        ts.getTranslatedString("module.taskman.task.actions.new-task-success"), NewTaskAction.class));
                wdwNewTask.close();

            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewTaskAction.class));
            }
        });

        wdwNewTask.getBtnConfirm().setEnabled(false);
        txtName.addValueChangeListener((e) -> {
            wdwNewTask.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty());
        });
        // Add content to window
        wdwNewTask.setContent(txtName, txtDescription);
        return wdwNewTask;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newTaskAction;
    } 
}