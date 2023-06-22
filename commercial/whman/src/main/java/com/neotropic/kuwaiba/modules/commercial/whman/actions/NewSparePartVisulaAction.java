/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.whman.actions;

import com.neotropic.kuwaiba.modules.commercial.whman.WarehousesManagerModule;
import com.vaadin.flow.component.combobox.ComboBox;
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
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new object action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewSparePartVisulaAction extends AbstractVisualAction<Dialog> {
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
    private NewSparePartAction newSparePartAction;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Dialog to create new spare part
     */
    private ConfirmDialog wdwNewObject;

    public NewSparePartVisulaAction() {
        super(WarehousesManagerModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        InventoryObjectPool selectedPool;
        if (parameters.containsKey("sparePool")) {
            try {
                selectedPool = (InventoryObjectPool) parameters.get("sparePool");
                commandClose = (Command) parameters.get("commandClose");
                
                TextField txtPoolName = new TextField(ts.getTranslatedString("module.whman.label-spare-name"));
                txtPoolName.setValue(selectedPool.getName());
                txtPoolName.setSizeFull();
                txtPoolName.setEnabled(false);
                
                ComboBox cmbClasses = new ComboBox(ts.getTranslatedString("module.general.labels.type"));
                cmbClasses.setAllowCustomValue(false);
                cmbClasses.setRequiredIndicatorVisible(true);
                cmbClasses.setSizeFull();
                
                ClassMetadata poolClass = mem.getClass(selectedPool.getClassName());
                if (poolClass.getName().equals(Constants.CLASS_INVENTORYOBJECT) || poolClass.isAbstract()) {
                    cmbClasses.setItems(mem.getSubClassesLight(poolClass.getName(), false, false));
                    cmbClasses.setReadOnly(false);
                } else {
                    cmbClasses.setItems(poolClass);
                    cmbClasses.setValue(poolClass);
                    cmbClasses.setReadOnly(true);
                }
                
                TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
                txtName.setRequiredIndicatorVisible(true);
                txtName.setSizeFull();
                
                TextField txtDescription = new TextField(ts.getTranslatedString("module.general.labels.description"));
                txtDescription.setSizeFull();
                // Dialog
                wdwNewObject = new ConfirmDialog(ts,
                        String.format(this.newSparePartAction.getDisplayName(), selectedPool.getClassName()),
                        ts.getTranslatedString("module.general.labels.create"));
                wdwNewObject.setDraggable(true);
                wdwNewObject.setResizable(true);
                
                wdwNewObject.getBtnConfirm().addClickListener((event) -> {
                    try {
                        newSparePartAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("sparePool", selectedPool),
                                new ModuleActionParameter<>("className", cmbClasses.getValue()),
                                new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                                new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue())
                        ));
                        wdwNewObject.close();
                        //refresh related grid
                        getCommandClose().execute();
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.whman.actions.spare.new-spare-part.success"), NewSparePartAction.class));
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), NewSparePartAction.class));
                    }
                });
                wdwNewObject.getBtnConfirm().setEnabled(false);
                txtName.addValueChangeListener(event -> {
                    wdwNewObject.getBtnConfirm().setEnabled(!txtName.isEmpty() && cmbClasses.getValue() != null);
                });
                
                cmbClasses.addValueChangeListener(event -> {
                    wdwNewObject.getBtnConfirm().setEnabled(!txtName.isEmpty() && cmbClasses.getValue() != null);
                });
                // Add content to window
                wdwNewObject.setContent(txtPoolName, cmbClasses, txtName, txtDescription);
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "sparePool")));
        return wdwNewObject;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newSparePartAction;
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