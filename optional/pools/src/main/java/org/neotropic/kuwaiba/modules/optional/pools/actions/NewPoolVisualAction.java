/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.modules.optional.pools.actions;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.pools.PoolsModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new pool action
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewPoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewPoolAction newPoolAction;
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
    /**
     * Dialog to add new pool.
     */
    private ConfirmDialog wdwNewPool;

    public NewPoolVisualAction() {
        super(PoolsModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();
            TextField txtDescription = new TextField(ts.getTranslatedString("module.general.labels.description"));
            txtDescription.setSizeFull();

            List<ClassMetadataLight> inventoryObjectClasses = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, true, true);
            ComboBox<ClassMetadataLight> cmbClassType = new ComboBox<>(ts.getTranslatedString("module.general.labels.class-name"));
            cmbClassType.setSizeFull();
            cmbClassType.setItems(inventoryObjectClasses);
            cmbClassType.setClearButtonVisible(true);
            cmbClassType.setRequiredIndicatorVisible(true);
            cmbClassType.setAllowCustomValue(false);
            cmbClassType.setItemLabelGenerator(ClassMetadataLight::getName);
            
            wdwNewPool = new ConfirmDialog(ts,
                     this.newPoolAction.getDisplayName(),
                     ts.getTranslatedString("module.general.labels.create"));
            wdwNewPool.setDraggable(true);
            wdwNewPool.setResizable(true);

            // To show errors or warnings related to the input parameters.
            Label lblMessages = new Label();
            wdwNewPool.getBtnConfirm().addClickListener(event -> {
                try {
                    if (txtName.getValue() == null || txtName.getValue().isEmpty() || cmbClassType.getValue() == null)
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                    else {
                        newPoolAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("className", cmbClassType.getValue().getName()),
                                new ModuleActionParameter<>("poolName", txtName.getValue()),
                                new ModuleActionParameter<>("description", txtDescription.getValue())));

                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString(ts.getTranslatedString("module.pools.actions.pool-created")), NewPoolAction.class));
                        wdwNewPool.close();
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), NewPoolAction.class));
                }
            });
            wdwNewPool.getBtnConfirm().setEnabled(false);

            txtName.addValueChangeListener(e -> wdwNewPool.getBtnConfirm().setEnabled(!txtName.isEmpty() && cmbClassType.getValue() != null));
            cmbClassType.addValueChangeListener(e -> wdwNewPool.getBtnConfirm().setEnabled(!txtName.isEmpty() && cmbClassType.getValue() != null));

            wdwNewPool.setContent(txtName, txtDescription, cmbClassType);

        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return wdwNewPool;
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newPoolAction;
    }
}