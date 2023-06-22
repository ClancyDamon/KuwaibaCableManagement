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
package com.neotropic.kuwaiba.modules.commercial.planning.projects.actions;

import com.neotropic.kuwaiba.modules.commercial.planning.projects.ProjectsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete activity action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteProjectActivityVisualAction extends AbstractVisualAction<Dialog> {
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
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteProjectActivityAction deleteProjectActivityAction;

    public DeleteProjectActivityVisualAction() {
        super(ProjectsModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
            BusinessObjectLight selectedActivity;
        if (parameters.containsKey("activity")) {
            selectedActivity = (BusinessObjectLight) parameters.get("activity");
            commandClose = (Command) parameters.get("commandClose");
            
            ConfirmDialog wdwDeleteActivity = new ConfirmDialog(ts
                    , this.deleteProjectActivityAction.getDisplayName()
                    , String.format(ts.getTranslatedString("module.projects.actions.activity.delete-activity.confirm"), selectedActivity.getName())
                    , ts.getTranslatedString("module.general.labels.delete"));
            wdwDeleteActivity.setDraggable(true);
            wdwDeleteActivity.setResizable(true);
            
            wdwDeleteActivity.getBtnConfirm().addClickListener(event -> {
                try {
                    deleteProjectActivityAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("activity", selectedActivity)));
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.projects.actions.activity.delete-activity.success"), DeleteProjectActivityAction.class));
                    wdwDeleteActivity.close();
                    //refresh related grid
                    getCommandClose().execute();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteProjectActivityAction.class));
                }
            });
            return wdwDeleteActivity;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.projects.actions.activity.delete-activity.error")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteProjectActivityAction;
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