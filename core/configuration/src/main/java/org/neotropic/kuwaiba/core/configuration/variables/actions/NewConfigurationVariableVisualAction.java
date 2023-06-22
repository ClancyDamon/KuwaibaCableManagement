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
package org.neotropic.kuwaiba.core.configuration.variables.actions;

import com.neotropic.flow.component.paper.toggle.PaperToggleButton;
import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import java.util.List;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.configuration.variables.ConfigurationVariablesModule;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new configuration variable action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewConfigurationVariableVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewConfigurationVariableAction newConfigurationVariableAction;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    protected BusinessEntityManager bem;

    public NewConfigurationVariableVisualAction() {
        super(ConfigurationVariablesModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        InventoryObjectPool selectedConfigurationVariablePool = null;

        if (parameters.containsKey("pool"))
            selectedConfigurationVariablePool = (InventoryObjectPool) parameters.get("pool");

        List<InventoryObjectPool> configurationVariablesPool = aem.getConfigurationVariablesPools();
        ComboBox<InventoryObjectPool> cmbConfigurationVariablesPool = new ComboBox<>(ts.getTranslatedString("module.configman.configurationvariablespool"), configurationVariablesPool);
        cmbConfigurationVariablesPool.setAllowCustomValue(false);
        cmbConfigurationVariablesPool.setRequiredIndicatorVisible(true);
        cmbConfigurationVariablesPool.setSizeFull();

        // Configuration Variable Pool selected if exists
        if (selectedConfigurationVariablePool != null) {
            cmbConfigurationVariablesPool.setValue(selectedConfigurationVariablePool);
            cmbConfigurationVariablesPool.setAllowCustomValue(false);
        }

        TextField txtName = new TextField(ts.getTranslatedString("module.configman.configvar.label.name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();

        TextField txtDescription = new TextField(ts.getTranslatedString("module.configman.configvar.label.description"));
        txtDescription.setSizeFull();

        TextField txtValue = new TextField(ts.getTranslatedString("module.configman.configvar.label.value"));
        txtValue.setSizeFull();
                
        PaperToggleButton btnMasked = new PaperToggleButton(ts.getTranslatedString("module.configman.configvar.label.masked"));
        btnMasked.setChecked(false);
        btnMasked.setClassName("green", true);
        btnMasked.addClassName("icon-button");

        ComboBox<ConfigurationVariableType> cmbTypes = new ComboBox<>(ts.getTranslatedString("module.configman.configvar.label.type"));
        cmbTypes.setItems(new ConfigurationVariableType(ts.getTranslatedString("module.configman.configvar.type.integer"), 0),
                new ConfigurationVariableType(ts.getTranslatedString("module.configman.configvar.type.float"), 1),
                new ConfigurationVariableType(ts.getTranslatedString("module.configman.configvar.type.string"), 2),
                new ConfigurationVariableType(ts.getTranslatedString("module.configman.configvar.type.boolean"), 3),
                new ConfigurationVariableType(ts.getTranslatedString("module.configman.configvar.type.array"), 4),
                new ConfigurationVariableType(ts.getTranslatedString("module.configman.configvar.type.table"), 5)
        );
        cmbTypes.setValue(new ConfigurationVariableType(ts.getTranslatedString("module.configman.configvar.type.string"), 2));
        cmbTypes.setAllowCustomValue(false);
        cmbTypes.setRequiredIndicatorVisible(true);
        cmbTypes.setSizeFull();
        
        // Window to create a new Configuration Variable
        ConfirmDialog wdwNewConfigurationVariable = new ConfirmDialog(ts
                , this.newConfigurationVariableAction.getDisplayName()
                , ts.getTranslatedString("module.general.labels.create"));
        wdwNewConfigurationVariable.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        wdwNewConfigurationVariable.setDraggable(true);
        wdwNewConfigurationVariable.setResizable(true);

        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();

        wdwNewConfigurationVariable.getBtnConfirm().addClickListener(e -> {
            try {
                if (cmbConfigurationVariablesPool.getValue() == null) {
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                } else {
                    ActionResponse actionResponse = newConfigurationVariableAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(Constants.PROPERTY_ID, cmbConfigurationVariablesPool.getValue().getId()),
                            new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                            new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue()),
                            new ModuleActionParameter<>(Constants.PROPERTY_VALUE, txtValue.getValue()),
                            new ModuleActionParameter<>(Constants.PROPERTY_MASKED, btnMasked.getChecked()),
                            new ModuleActionParameter<>(Constants.PROPERTY_TYPE, cmbTypes.getValue().getType())             
                    ));
                    if (actionResponse.containsKey("exception"))
                            throw new ModuleActionException(((Exception)actionResponse.get("exception")).getLocalizedMessage());

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.configman.actions.new-configuration-variable.ui.created-success"), NewConfigurationVariableAction.class));
                    wdwNewConfigurationVariable.close();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewConfigurationVariableAction.class));
            }
        });

        wdwNewConfigurationVariable.getBtnConfirm().setEnabled(false);
        txtName.addValueChangeListener((event) -> {
            wdwNewConfigurationVariable.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbConfigurationVariablesPool.isEmpty() && !cmbTypes.isEmpty());
        });
        
        cmbConfigurationVariablesPool.addValueChangeListener((event) -> {
            wdwNewConfigurationVariable.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbConfigurationVariablesPool.isEmpty() && !cmbTypes.isEmpty());
        });
        
        cmbTypes.addValueChangeListener((event) -> {
            wdwNewConfigurationVariable.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbConfigurationVariablesPool.isEmpty() && !cmbTypes.isEmpty());
        });
        

        wdwNewConfigurationVariable.setContent(cmbConfigurationVariablesPool, txtName, txtDescription, txtValue, cmbTypes, btnMasked);

        return wdwNewConfigurationVariable;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newConfigurationVariableAction;
    }

    /**
     * Dummy class to be used in the configuration variable type combo box
     */
    private class ConfigurationVariableType {
        private final String displayName;
        private final int type;

        public ConfigurationVariableType(String displayName, int type) {
            this.displayName = displayName;
            this.type = type;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getType() {
            return type;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}