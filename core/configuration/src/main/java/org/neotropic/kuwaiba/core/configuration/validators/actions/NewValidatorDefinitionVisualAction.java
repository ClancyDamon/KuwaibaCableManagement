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
package org.neotropic.kuwaiba.core.configuration.validators.actions;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.configuration.validators.ValidatorDefinitionModule;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new validator definition action.
 *
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewValidatorDefinitionVisualAction extends AbstractVisualAction<Dialog> {
    
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
    private NewValidatorDefinitionAction newValidatorDefinitionAction;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;
    /**
     * Confirm dialog
     */
    private ConfirmDialog cfdValidatorDefinition;
    /**
     * class selector
     */
    private ComboBox<ClassMetadataLight> cmbClasses;
    /**
     * new validator name
     */
    private TextField txtName;

    public NewValidatorDefinitionVisualAction() {
        super(ValidatorDefinitionModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        ClassMetadataLight selectedClass = null;

        if (parameters.containsKey("class")) {
            selectedClass = (ClassMetadataLight) parameters.get("class");
        }

        commandClose = (Command) parameters.get("commandClose");
        
        List<ClassMetadataLight> classes = new ArrayList<>();
        try {
            classes = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, true, true);
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }

        cmbClasses = new ComboBox<>(ts.getTranslatedString("module.configman.classes"), classes);
        cmbClasses.setWidthFull();
        cmbClasses.setAllowCustomValue(false);
        cmbClasses.setRequired(true);
        cmbClasses.setRequiredIndicatorVisible(true);

        // Selected class if exists
        if (selectedClass != null) {
            cmbClasses.setValue(selectedClass);
            cmbClasses.setAllowCustomValue(false);
        }

        txtName = new TextField(ts.getTranslatedString("module.configman.validators.label.name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();

        TextField txtDescription = new TextField(ts.getTranslatedString("module.configman.validators.label.description"));
        txtDescription.setSizeFull();

        TextArea txtScript = new TextArea(ts.getTranslatedString("module.general.labels.script"));
        txtScript.setSizeFull();

        // Windows to create a new Validator Definition
        cfdValidatorDefinition = new ConfirmDialog(ts, ts.getTranslatedString("module.configman.validators.actions.new-validator.name"),
                 ts.getTranslatedString("module.general.labels.create"));
        cfdValidatorDefinition.getBtnConfirm().setEnabled(false);
        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();

        cfdValidatorDefinition.getBtnConfirm().addClickListener((e) -> {
            try {
                if (cmbClasses.getValue() == null) {
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                } else {
                    newValidatorDefinitionAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                            new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue()),
                            new ModuleActionParameter<>(Constants.PROPERTY_CLASSNAME, cmbClasses.getValue().getName()),
                            new ModuleActionParameter<>(Constants.PROPERTY_SCRIPT, txtScript.getValue()),
                            new ModuleActionParameter<>(Constants.PROPERTY_ENABLED, true)
                    ));

                    ActionResponse actionResponse = new ActionResponse();
                    actionResponse.put(ActionResponse.ActionType.ADD, ActionResponse.ActionType.ADD);
                    actionResponse.put(Constants.PROPERTY_CLASSNAME, cmbClasses.getValue().getName());

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.configman.validators.actions.new-validator.ui.created-success")
                            , NewValidatorDefinitionAction.class, actionResponse));
                    
                    if(commandClose != null) commandClose.execute() ;
                    cfdValidatorDefinition.close();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewValidatorDefinitionAction.class));
            }
        });

        txtName.addValueChangeListener((event) -> validate());
        cmbClasses.addValueChangeListener((event) -> validate());

        //create content    
        cfdValidatorDefinition.setContent(cmbClasses, txtName, txtDescription);
        return cfdValidatorDefinition;
    }

    private void validate() {
        boolean isValid = !txtName.isEmpty() && !cmbClasses.isEmpty();
        cfdValidatorDefinition.getBtnConfirm().setEnabled(isValid);
    }

    @Override
    public AbstractAction getModuleAction() {
        return newValidatorDefinitionAction;
    }
}
