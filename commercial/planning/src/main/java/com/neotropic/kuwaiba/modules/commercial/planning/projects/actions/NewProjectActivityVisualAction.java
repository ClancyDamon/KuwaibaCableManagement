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
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
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
 * Visual wrapper of create a new activity action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewProjectActivityVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Refresh action command
     */
    private Command commandRefresh;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewProjectActivityAction newProjectActivityAction;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * ComboBox for classes 
     */
    private ComboBox cmbClasses;
    /**
     * List of classes
     */
    private List<ClassMetadataLight> classes;     
    /**
     * Pool items limit. -1 To return all
     */
    public static final int LIMIT = -1;    
    /**
     * Dialog to create new activity project
     */
    private ConfirmDialog wdwNewActivity;

    public NewProjectActivityVisualAction() {
        super(ProjectsModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            BusinessObjectLight selectedProject = null;
       
            if (parameters.containsKey("project"))
                selectedProject = (BusinessObjectLight) parameters.get("project");
            
            List<BusinessObjectLight> projects = bem.getObjectsOfClassLight(Constants.CLASS_GENERICPROJECT, null, LIMIT, LIMIT);
            ComboBox<BusinessObjectLight> cmbProject = new ComboBox<>(ts.getTranslatedString("module.projects.activity.label.project-name"), projects);
            cmbProject.setAllowCustomValue(false);
            cmbProject.setRequiredIndicatorVisible(true);
            cmbProject.setSizeFull();

            //Project selected if exists
            if (selectedProject != null) {
                cmbProject.setValue(selectedProject);
                cmbProject.setAllowCustomValue(false);
                cmbProject.setEnabled(false);
            }

            TextField txtName = new TextField(ts.getTranslatedString("module.projects.activity.label.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            try {
                classes = mem.getSubClassesLight(Constants.CLASS_GENERICACTIVITY, true, false);
                cmbClasses = new ComboBox(ts.getTranslatedString("module.projects.activity.label.type"));
                cmbClasses.setItems(classes);
                cmbClasses.setSizeFull();
                cmbClasses.setAllowCustomValue(false);
                cmbClasses.setRequiredIndicatorVisible(true);
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();     
            }
            // Dialog
            wdwNewActivity = new ConfirmDialog(ts
                , this.newProjectActivityAction.getDisplayName()
                , ts.getTranslatedString("module.general.labels.create"));
            wdwNewActivity.setDraggable(true);
            wdwNewActivity.setResizable(true);
            // To show errors or warnings related to the input parameters.
            Label lblMessages = new Label();
            wdwNewActivity.getBtnConfirm().addClickListener(event -> {
                try {
                    if (cmbProject.getValue() == null) {
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                    } else {
                        ClassMetadataLight activityType = (ClassMetadataLight) cmbClasses.getValue();

                        newProjectActivityAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("project", cmbProject.getValue()),
                                new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                                new ModuleActionParameter<>(Constants.PROPERTY_CLASSNAME, activityType.getName())
                        ));
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.projects.actions.activity.new-activity-success"), NewProjectActivityAction.class));
                        wdwNewActivity.close();
                        //refresh related grid
                        if (parameters.containsKey("commandRefresh")) {
                            commandRefresh = (Command) parameters.get("commandRefresh");
                            getCommandRefresh().execute();
                        }
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), NewProjectActivityAction.class));
                }
            });
            wdwNewActivity.getBtnConfirm().setEnabled(false);
            txtName.addValueChangeListener(event -> {
                wdwNewActivity.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty() && cmbClasses.getValue() != null);
            });
            cmbClasses.addValueChangeListener(event -> {
               wdwNewActivity.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty() && cmbClasses.getValue() != null);
            });
            cmbProject.addValueChangeListener(event -> {
                wdwNewActivity.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty() && cmbClasses.getValue() != null);
            });
            // Add content to window
            wdwNewActivity.setContent(cmbProject, txtName, cmbClasses);
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return wdwNewActivity;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newProjectActivityAction;
    }
    
    /**
     * refresh grid 
     * @return commandClose;Command; refresh action 
     */
    public Command getCommandRefresh() {
        return commandRefresh;
    }

    /**
     * @param commandRefresh
     */
    public void setCommandRefresh(Command commandRefresh) {
        this.commandRefresh = commandRefresh;
    }    
}