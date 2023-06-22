/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neotropic.kuwaiba.core.configuration.validators.actions;

import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.configuration.validators.ValidatorDefinitionModule;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of update a validator definition action.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Component
public class UpdateValidatorDefinitionVisualAction extends AbstractVisualAction<Dialog> {

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
    private UpdateValidatorDefinitionAction updateValidatorDefinitionAction;

    public UpdateValidatorDefinitionVisualAction() {
        super(ValidatorDefinitionModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {

        TextField txtName = new TextField(ts.getTranslatedString("module.configman.validators.label.name"));
        txtName.setValue((String) parameters.get(Constants.PROPERTY_NAME));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();
        
        commandClose = (Command) parameters.get("commandClose");
        
        //create confirm dialog
        ConfirmDialog wdwUpdateValidator = new ConfirmDialog(ts,
                 this.updateValidatorDefinitionAction.getDisplayName(),
                 ts.getTranslatedString("module.general.messages.save"));
        wdwUpdateValidator.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        wdwUpdateValidator.setDraggable(true);
        wdwUpdateValidator.setResizable(true);

        TextField txtDescription = new TextField(ts.getTranslatedString("module.configman.validators.label.description"));
        txtDescription.setValue((String) parameters.get(Constants.PROPERTY_DESCRIPTION));
        txtDescription.setSizeFull();

        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();

        wdwUpdateValidator.getBtnConfirm().setEnabled(false);
        wdwUpdateValidator.getBtnConfirm().addClickListener(event -> {
            try {
                lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));

                updateValidatorDefinitionAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_ID, parameters.get(Constants.PROPERTY_ID)),
                        new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                        new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue())
                ));

                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(ActionResponse.ActionType.UPDATE, ActionResponse.ActionType.UPDATE);
                actionResponse.put(Constants.PROPERTY_NAME, txtName.getValue());
                actionResponse.put(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue());

                fireActionCompletedEvent(
                        new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.configman.validators.actions.update-validator.ui.updated-success"),
                                NewValidatorDefinitionAction.class, actionResponse));
                if(commandClose != null) commandClose.execute() ;
                wdwUpdateValidator.close();
            } catch (ModuleActionException ex) {
                throw new ScriptCompilationException(ex.getLocalizedMessage());
            }
        });

        //validations
        txtName.addValueChangeListener(event
                -> wdwUpdateValidator.getBtnConfirm().setEnabled(!txtName.isEmpty())
        );
        
        //create content
        wdwUpdateValidator.setContent(txtName, txtDescription);
        return wdwUpdateValidator;
    }

    @Override
    public AbstractAction getModuleAction() {
        return updateValidatorDefinitionAction;
    }
}