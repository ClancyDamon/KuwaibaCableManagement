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
package org.neotropic.kuwaiba.core.configuration.filters.actions;

import org.neotropic.kuwaiba.core.configuration.validators.actions.*;
import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
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
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.configuration.filters.FilterDefinitionModule;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new filter definition action.
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class NewFilterDefinitionVisualAction extends AbstractVisualAction<Dialog> {
    
    public static final String ALL_PARAM_CLASSES = "classes";
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewFilterDefinitionAction newFilterDefinitionAction;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;

    public NewFilterDefinitionVisualAction() {
        super(FilterDefinitionModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        // Windows to create a new filter Definition
        ConfirmDialog cfdNewFilter = new ConfirmDialog(ts, ts.getTranslatedString("module.configman.filters.actions.new-filter.name")
                , ts.getTranslatedString("module.general.labels.create"));
        cfdNewFilter.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        
        TextField txtName = new TextField(ts.getTranslatedString("module.configman.filters.actions.new-filter.label.name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();

        TextField txtDescription = new TextField(ts.getTranslatedString("module.configman.filters.actions.new-filter.label.description"));
        txtDescription.setSizeFull();

        ComboBox<ClassMetadataLight> cmbClasses = new ComboBox<>(ts.getTranslatedString("module.datamodelman.inventory-classes"));
        cmbClasses.setSizeFull();
        cmbClasses.setAutofocus(true);
        cmbClasses.setAllowCustomValue(false);
        cmbClasses.setClearButtonVisible(true);
        cmbClasses.setPlaceholder(ts.getTranslatedString("module.configman.filter.label.select-a-classs"));
        cmbClasses.setItems((List<ClassMetadataLight>)parameters.get(ALL_PARAM_CLASSES));

        if((String)parameters.get(Constants.PROPERTY_CLASSNAME) != null){
            for (ClassMetadataLight c : (List<ClassMetadataLight>)parameters.get(ALL_PARAM_CLASSES)) {
                if(c.getName().equals((String)parameters.get(Constants.PROPERTY_CLASSNAME))){
                    cmbClasses.setValue(c);
                    cmbClasses.setEnabled(false);
                    break;
                }
            }
        }
        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();

        cfdNewFilter.getBtnConfirm().addClickListener((e) -> {
            try {
                //lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));

                newFilterDefinitionAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_CLASSNAME, cmbClasses.getValue().getName()),
                        new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                        new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue()),
                        new ModuleActionParameter<>(Constants.PROPERTY_SCRIPT, null),
                        new ModuleActionParameter<>(Constants.PROPERTY_ENABLED, true)
                ));

                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(ActionResponse.ActionType.ADD, ActionResponse.ActionType.ADD);
                actionResponse.put(Constants.PROPERTY_CLASSNAME, cmbClasses.getValue().getName());

                fireActionCompletedEvent(
                        new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.configman.filters.notification-created"), 
                                NewValidatorDefinitionAction.class, actionResponse));

                cfdNewFilter.close();
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewValidatorDefinitionAction.class));
            }
        });
        cfdNewFilter.getBtnConfirm().setEnabled(false);

        txtName.addValueChangeListener(e -> 
            cfdNewFilter.getBtnConfirm().setEnabled(!txtName.isEmpty()));

        cmbClasses.addValueChangeListener(e -> 
            cfdNewFilter.getBtnConfirm().setEnabled(e.getValue() != null));
               
        cfdNewFilter.setContent(cmbClasses, txtName, txtDescription);

        return cfdNewFilter;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newFilterDefinitionAction;
    }
}