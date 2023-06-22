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
 * Visual wrapper of release object action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class ReleaseObjectFromProjectVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private ReleaseObjectFromProjectAction releaseObjectAction;
    /**
     * Close action command
     */
    private Command commandClose;
    
    public ReleaseObjectFromProjectVisualAction() {
        super(ProjectsModule.MODULE_ID);
    }

@Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight selectedProject;
        BusinessObjectLight selectedObject;

        if (parameters.containsKey("project")) {
            selectedProject = (BusinessObjectLight) parameters.get("project");
            if (parameters.containsKey("businessObject")) {
                selectedObject = (BusinessObjectLight) parameters.get("businessObject");
                commandClose = (Command) parameters.get("commandClose");

                ConfirmDialog wdwReleaseProject = new ConfirmDialog(ts, 
                        this.releaseObjectAction.getDisplayName(),
                        String.format(ts.getTranslatedString("module.projects.actions.release-object-from-project.confirm"), 
                                selectedObject.getName(), selectedProject.getName()),
                        ts.getTranslatedString("module.general.messages.release"));
                wdwReleaseProject.setDraggable(true);
                wdwReleaseProject.setResizable(true);

                wdwReleaseProject.getBtnConfirm().addClickListener((event) -> {
                    try {
                        releaseObjectAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("project", selectedProject),
                                new ModuleActionParameter<>("businessObject", selectedObject)));
                        //refresh related grid
                        getCommandClose().execute();
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                String.format(ts.getTranslatedString("module.projects.actions.release-object-from-project.success"), selectedObject.getName()),
                                ReleaseObjectFromProjectAction.class));
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), ReleaseObjectFromProjectAction.class));
                    }
                    wdwReleaseProject.close();
                });
                return wdwReleaseProject;
            } else
                return new Dialog(new Label(ts.getTranslatedString("module.general.messages.object-not-found")));
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.projects.actions.project.delete-project-error")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return releaseObjectAction;
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