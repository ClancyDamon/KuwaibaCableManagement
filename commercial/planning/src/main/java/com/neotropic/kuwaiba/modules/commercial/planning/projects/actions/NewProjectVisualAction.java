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
import com.neotropic.kuwaiba.modules.commercial.planning.projects.ProjectsService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new project action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewProjectVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the Project Service
     */
    @Autowired
    private ProjectsService ps;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewProjectAction newProjectAction;
    /**
     * Dialog to create new Project
     */
    private ConfirmDialog wdwNewProject;

    public NewProjectVisualAction() {
        super(ProjectsModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            InventoryObjectPool selectedPool = null;
            if (parameters.containsKey("pool"))
                selectedPool = (InventoryObjectPool) parameters.get("pool");
            TextField txtPool = new TextField(ts.getTranslatedString("module.projects.project.label.pool-type"));
            txtPool.setSizeFull();
            txtPool.setEnabled(false);
            List<InventoryObjectPool> listPool = ps.getProjectPools();
            ComboBox<InventoryObjectPool> cmbPool = new ComboBox<>(ts.getTranslatedString("module.projects.project.label.pool-name"), listPool);
            cmbPool.addValueChangeListener((event) -> {
                if(event.getValue() != null)
                    txtPool.setValue(event.getValue().getClassName());
                else
                    txtPool.setValue("");
            });
            cmbPool.setAllowCustomValue(false);
            cmbPool.setRequiredIndicatorVisible(true);
            cmbPool.setSizeFull();
            //Contract pool selected if exists
            if (selectedPool != null) {
                cmbPool.setValue(selectedPool);
                cmbPool.setAllowCustomValue(false);
                txtPool.setValue(selectedPool.getClassName());
            }
            TextField txtName = new TextField(ts.getTranslatedString("module.projects.project.label.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();
            TextField txtNotes = new TextField(ts.getTranslatedString("module.projects.project.label.notes"));
            txtNotes.setSizeFull();
            // Dialog
            wdwNewProject = new ConfirmDialog(ts
                    , this.newProjectAction.getDisplayName()
                    , ts.getTranslatedString("module.general.labels.create"));
            wdwNewProject.setDraggable(true);
            wdwNewProject.setResizable(true);    
            // To show errors or warnings related to the input parameters.
            Label lblMessages = new Label();
            wdwNewProject.getBtnConfirm().addClickListener(event -> {
                try {
                    if (cmbPool.getValue() == null)
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                    else {
                        newProjectAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("pool", cmbPool.getValue()),
                                new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                                new ModuleActionParameter<>(Constants.PROPERTY_NOTES, txtNotes.getValue())
                        ));
                        wdwNewProject.close();
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.projects.actions.project.new-project-success"), NewProjectAction.class));
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), NewProjectAction.class));
                }
            });
            wdwNewProject.getBtnConfirm().setEnabled(false);
            txtName.addValueChangeListener(event -> {
                wdwNewProject.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty() && cmbPool.getValue() != null);
            });
            cmbPool.addValueChangeListener(event -> {
                wdwNewProject.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty() && cmbPool.getValue() != null);
            });
            // Add content to window
            wdwNewProject.setContent(cmbPool, txtPool, txtName, txtNotes);
        } catch (InvalidArgumentException ex) {
            ConfirmDialog wdwError = new ConfirmDialog(ts
                    , ts.getTranslatedString("module.general.messages.error")
                    , ts.getTranslatedString("module.general.messages.close"));

            ActionButton btnClose = new ActionButton(ts.getTranslatedString("module.general.messages.close"));
            btnClose.addClickListener(event -> wdwError.close());
            
            wdwError.setContent(new Label(ex.getMessage()));
            wdwError.setFooter(btnClose);
            return wdwError;
        }
        return wdwNewProject;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newProjectAction;
    }   
}