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

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ResultMessage;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.taskman.TaskManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of show a task notification.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@CssImport(value = "./css/poolConfigurationDialog.css")
@Component
public class TaskNotificationDialog extends AbstractVisualAction<Dialog> {
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
     * Object to save the selected task
     */
    private Task currentTask;
    /**
     * Layout for notification
     */
    private VerticalLayout lytNotification;
    /**
     * The grid for task notification
     */
    private Grid<ResultMessage> gridResult;
    
    public TaskNotificationDialog() {
        super(TaskManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("task")) {
            currentTask = (Task) parameters.get("task");
            
            ConfirmDialog wdwNotification = new ConfirmDialog(ts
                    , String.format(ts.getTranslatedString("module.taskman.task.actions.execute-task-result"), currentTask.getName())
                    , ts.getTranslatedString("module.taskman.task.actions.execute-task-result.close"));
            wdwNotification.setDraggable(true);
            wdwNotification.getBtnConfirm().setVisible(false);
            wdwNotification.setContentSizeFull();
            wdwNotification.setMinWidth("70%");
            wdwNotification.setResizable(true);
            //--Main side
            VerticalLayout lytMain = new VerticalLayout();
            lytMain.setClassName("left-side-dialog");
            lytMain.setMargin(false);
            lytMain.setPadding(false);
            lytMain.setSpacing(true);
            lytMain.setHeightFull();
            lytMain.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
            // Grid
            lytNotification = new VerticalLayout();
            lytNotification.setClassName("grig-pool-container");
            lytNotification.setHeightFull();
            lytNotification.setMargin(false);
            lytNotification.setSpacing(false);
            lytNotification.setPadding(false);
            buildResultsGrid(currentTask);
            lytNotification.add(gridResult);
            lytNotification.setVisible(true);
            // Add content to main layout
            lytMain.add(lytNotification);
            wdwNotification.setContent(lytMain);
            return wdwNotification;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.taskman.task.actions.delete-task-error")));
    }

    private void buildResultsGrid(Task task) {
        try {
            TaskResult taskResult = aem.executeTask(task.getId());
            gridResult = new Grid<>();
            gridResult.setItems(taskResult.getMessages());
            gridResult.getDataProvider().refreshAll();
            gridResult.addComponentColumn(result -> getResultMessage(result))
                .setHeader(ts.getTranslatedString("module.taskman.task.actions.execute-task-header"));
            gridResult.setClassNameGenerator(result -> result.getMessage() != null && !result.getMessage().isEmpty() ? "text-result" : "");
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    /**
     * Create result message template
     * @param result
     * @return htmlStatus
     */
    private static Div getResultMessage(ResultMessage result) {
        Div htmlStatus = new Div();
        Span html;

        HorizontalLayout lytResult = new HorizontalLayout();
        lytResult.setPadding(true);
        lytResult.setSpacing(false);

        switch (result.getMessageType()) {
            case ResultMessage.STATUS_SUCCESS:
                html = new Span(result.getMessage());   
                htmlStatus.addClassNames("success", "task-result");
                htmlStatus.add(html);
                break;
            case ResultMessage.STATUS_WARNING:
                html = new Span(result.getMessage());  
                htmlStatus.addClassNames("warning", "task-result");
                htmlStatus.add(html);
                break;
            case ResultMessage.STATUS_ERROR:
                html = new Span(result.getMessage());  
                htmlStatus.addClassNames("error", "task-result");
                htmlStatus.add(html);
                break;
            default:
                return null;
        }
        return htmlStatus;
    }
   
    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}