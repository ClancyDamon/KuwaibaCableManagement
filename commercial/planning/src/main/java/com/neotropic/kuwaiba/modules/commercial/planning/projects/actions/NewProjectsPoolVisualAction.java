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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new projects pool action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewProjectsPoolVisualAction extends AbstractVisualAction<Dialog> {
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
    private NewProjectsPoolAction newProjectsPoolAction;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * ComboBox for classes 
     */
    ComboBox cmbClasses;
    /**
     * List of classes
     */
    List<ClassMetadataLight> classes;

    public NewProjectsPoolVisualAction() {
        super(ProjectsModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        commandClose = (Command) parameters.get("commandClose");
        
        TextField txtName = new TextField(ts.getTranslatedString("module.projects.pool.label.name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();
        TextField txtDescription = new TextField(ts.getTranslatedString("module.projects.pool.label.description"));
        txtDescription.setSizeFull();
        try {
            classes = mem.getSubClassesLight(Constants.CLASS_GENERICPROJECT, true, false);
            cmbClasses = new ComboBox(ts.getTranslatedString("module.projects.pool.label.type"));
            cmbClasses.setItems(classes);
            cmbClasses.setSizeFull();
            cmbClasses.setAllowCustomValue(false);
            cmbClasses.setRequiredIndicatorVisible(true);
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }

        ConfirmDialog wdwProjectsPool = new ConfirmDialog(ts
            , this.newProjectsPoolAction.getDisplayName()
            , ts.getTranslatedString("module.general.labels.create"));
        wdwProjectsPool.setDraggable(true);
        wdwProjectsPool.setResizable(true);
        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();
        
        wdwProjectsPool.getBtnConfirm().addClickListener(event -> {
            try {
                if (cmbClasses.getValue() == null)
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                else {
                    ClassMetadataLight poolType = (ClassMetadataLight) cmbClasses.getValue();

                    newProjectsPoolAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>((Constants.PROPERTY_NAME), txtName.getValue()),
                            new ModuleActionParameter<>((Constants.PROPERTY_DESCRIPTION), txtDescription.getValue()),
                            new ModuleActionParameter<>((Constants.PROPERTY_CLASSNAME), poolType.getName())
                    ));
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.projects.actions.pool.new-pool-success"), NewProjectsPoolAction.class));
                    wdwProjectsPool.close();
                    // Refresh related grid
                    getCommandClose().execute();
                    if (parameters.containsKey("commandAdd")) {
                        Command commandAdd = (Command) parameters.get("commandAdd");
                        commandAdd.execute();
                    }
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewProjectsPoolAction.class));
            }
        });
        
        wdwProjectsPool.getBtnConfirm().setEnabled(false);
        txtName.addValueChangeListener((event) -> {
            wdwProjectsPool.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty() && cmbClasses.getValue() != null);
        });
        cmbClasses.addValueChangeListener((event) -> {
            wdwProjectsPool.getBtnConfirm().setEnabled(cmbClasses.getValue() != null && !txtName.getValue().isEmpty());
        });
        // Add content to window
        wdwProjectsPool.setContent(txtName, txtDescription, cmbClasses);
        return wdwProjectsPool;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newProjectsPoolAction;
    }
    
    /**
     * refresh grid
     * @return commandClose; refresh action 
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