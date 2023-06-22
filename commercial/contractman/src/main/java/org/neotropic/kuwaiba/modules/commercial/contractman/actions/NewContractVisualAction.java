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
package org.neotropic.kuwaiba.modules.commercial.contractman.actions;

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
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.contractman.ContractManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new contract action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewContractVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewContractAction newContractAction;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    private BusinessEntityManager bem; 
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Type of pool module root. These pools are used in models and are the root of such model
     */
    public static final int POOL_TYPE_MODULE_ROOT = 2;
    /**
     * Dialog to create new Contract
     */
    private ConfirmDialog wdwNewContract;
    /**
     * Parameter pool
     */
    private static final String PARAMETER_POOL = "pool";
    /**
     * Subclasses list when the selected pool is of abstract class, eg GenericContract
     */
    private List<ClassMetadataLight> listClasses;
    
    public NewContractVisualAction() {
        super(ContractManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            InventoryObjectPool selectedPool = null;
            if (parameters.containsKey(PARAMETER_POOL))
                selectedPool = (InventoryObjectPool) parameters.get(PARAMETER_POOL);
            
            List<InventoryObjectPool> listPool = bem.getRootPools(Constants.CLASS_GENERICCONTRACT, POOL_TYPE_MODULE_ROOT, true);
            
            ComboBox<InventoryObjectPool> cmbPool = new ComboBox<>(ts.getTranslatedString("module.contractman.contract.label.pool-name"), listPool);
            cmbPool.setAllowCustomValue(false);
            cmbPool.setRequiredIndicatorVisible(true);
            cmbPool.setSizeFull();
            
            ComboBox<ClassMetadataLight> cmbClasses = new ComboBox<>(ts.getTranslatedString("module.contractman.contract.label.pool-type"));
            cmbClasses.setAllowCustomValue(false);
            cmbClasses.setRequiredIndicatorVisible(true);
            cmbClasses.setSizeFull();
            
            cmbPool.addValueChangeListener((event) -> {
                if (event.getValue() != null) {
                    try {
                        ClassMetadata poolClass = mem.getClass(event.getValue().getClassName());
                        if (poolClass.getName().equals(Constants.CLASS_GENERICCONTRACT) || poolClass.isAbstract()) {
                            listClasses = mem.getSubClassesLight(poolClass.getName(), false, false);
                            cmbClasses.setItems(listClasses);
                            cmbClasses.setReadOnly(false);
                        } else {
                            cmbClasses.setValue(poolClass);
                            cmbClasses.setReadOnly(true);
                        }
                    } catch (MetadataObjectNotFoundException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                } else {
                    cmbClasses.setValue(null);
                }
            });
            
            if (selectedPool != null) 
                cmbPool.setValue(selectedPool);
                                    
            TextField txtName = new TextField(ts.getTranslatedString("module.contractman.contract.label.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            TextField txtDescription = new TextField(ts.getTranslatedString("module.contractman.contract.label.description"));
            txtDescription.setSizeFull();
            
            //Dialog
            wdwNewContract = new ConfirmDialog(ts
                , this.newContractAction.getDisplayName()
                , ts.getTranslatedString("module.general.labels.create"));
            wdwNewContract.setDraggable(true);
            // To show errors or warnings related to the input parameters.
            Label lblMessages = new Label();
            wdwNewContract.getBtnConfirm().addClickListener(event -> {
                try {
                    if (cmbPool.getValue() == null)
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                    else {
                            newContractAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>(PARAMETER_POOL, cmbPool.getValue()),
                                new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                                new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue()),
                                new ModuleActionParameter<>(Constants.PROPERTY_CLASSNAME, cmbClasses.getValue().getName())
                        ));
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.contractman.actions.contract.new-contract-success"), NewContractAction.class));
                        wdwNewContract.close();
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), NewContractAction.class));
                }
            });
            wdwNewContract.getBtnConfirm().setEnabled(false);
            txtName.addValueChangeListener(event -> {
                wdwNewContract.getBtnConfirm().setEnabled(!txtName.isEmpty() && cmbClasses.getValue() != null);
            });
            cmbClasses.addValueChangeListener(event -> {
                wdwNewContract.getBtnConfirm().setEnabled(!txtName.isEmpty() && cmbClasses.getValue() != null);
            });
            // Add content to window
            wdwNewContract.setContent(cmbPool, cmbClasses, txtName, txtDescription);
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return wdwNewContract;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newContractAction;
    }
}