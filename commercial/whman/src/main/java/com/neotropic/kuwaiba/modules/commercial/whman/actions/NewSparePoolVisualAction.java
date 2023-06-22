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
package com.neotropic.kuwaiba.modules.commercial.whman.actions;

import com.neotropic.kuwaiba.modules.commercial.whman.WarehousesManagerModule;
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
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
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
 * Visual wrapper of create a new spare pool action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewSparePoolVisualAction extends AbstractVisualAction<Dialog> {
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
    private NewSparePoolAction newSparePoolAction;
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired 
    private MetadataEntityManager mem;
    /**
     * Dialog to create new warehouse
     */
    private ConfirmDialog wdwNewSparePool;

    public NewSparePoolVisualAction() {
        super(WarehousesManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight selectedObject;
        if (parameters.containsKey("warehouse")) {
            selectedObject = (BusinessObjectLight) parameters.get("warehouse");
            commandClose = (Command) parameters.get("commandClose");
            
            List<ClassMetadataLight> listClasses = null;
            try {
                listClasses = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, true, true);
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }

            TextField txtPool = new TextField(ts.getTranslatedString("module.whman.label-warehouse-name"));
            txtPool.setValue(selectedObject.getName());
            txtPool.setRequiredIndicatorVisible(true);
            txtPool.setEnabled(false);
            txtPool.setSizeFull();
            
            ComboBox<ClassMetadataLight> cmbClasses = new ComboBox<>(ts.getTranslatedString("module.whman.label-spare-class"), listClasses);
            cmbClasses.setAllowCustomValue(false);
            cmbClasses.setRequiredIndicatorVisible(true);
            cmbClasses.setSizeFull();
            
            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            TextField txtDescription = new TextField(ts.getTranslatedString("module.general.labels.description"));
            txtDescription.setSizeFull();

            // Dialog
            wdwNewSparePool = new ConfirmDialog(ts,
                    this.newSparePoolAction.getDisplayName(),
                    ts.getTranslatedString("module.general.labels.create"));
            wdwNewSparePool.setDraggable(true);
            wdwNewSparePool.setResizable(true);
            wdwNewSparePool.getBtnConfirm().addClickListener((event) -> {
                try {
                    newSparePoolAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(Constants.LABEL_POOLS, selectedObject),
                            new ModuleActionParameter<>(Constants.LABEL_CLASS, cmbClasses.getValue()),
                            new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                            new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue())));
                    wdwNewSparePool.close();
                    //refresh related grid
                    getCommandClose().execute();
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.whman.actions.spare.new-spare.success"), NewSparePoolAction.class));
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), NewSparePoolAction.class));
                }
            });
            wdwNewSparePool.getBtnConfirm().setEnabled(false);
            txtName.addValueChangeListener((event) -> {
                wdwNewSparePool.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbClasses.isEmpty());
            });
            cmbClasses.addValueChangeListener((event) -> {
                wdwNewSparePool.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbClasses.isEmpty());
            });
            // Add content to window
            wdwNewSparePool.setContent(txtPool, cmbClasses, txtName, txtDescription);
            return wdwNewSparePool;
        } else 
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "warehouse")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return newSparePoolAction; 
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