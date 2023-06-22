/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.softman.actions;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.softman.SoftwareManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Visual wrapper of create a new license pool action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewLicensePoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the underlying action
     */
    @Autowired
    private NewLicensePoolAction newLicensePoolAction;
    /**
     * ComboBox for classes 
     */
    private ComboBox cmbClasses;
    /**
     * List of classes
     */
    private List<ClassMetadataLight> classes;
    /**
     * Window to create new pool
     */
    private ConfirmDialog wdwNewPool;
    
    public NewLicensePoolVisualAction() {
        super(SoftwareManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();
            
            TextField txtDescription = new TextField(ts.getTranslatedString("module.general.labels.description"));
            txtDescription.setSizeFull();
            
            classes = mem.getSubClassesLight(Constants.CLASS_GENERICSOFTWAREASSET, true, true);
            cmbClasses = new ComboBox(ts.getTranslatedString("module.general.labels.type"));
            cmbClasses.setItems(classes);
            cmbClasses.setSizeFull();
            cmbClasses.setAllowCustomValue(false);
            cmbClasses.setRequiredIndicatorVisible(true);
            
            wdwNewPool = new ConfirmDialog(ts,
                    this.newLicensePoolAction.getDisplayName(),
                    ts.getTranslatedString("module.general.labels.create"));
            wdwNewPool.setDraggable(true);
            wdwNewPool.setResizable(true);
            
            wdwNewPool.getBtnConfirm().addClickListener(event -> {
                try {
                    ClassMetadataLight poolType = (ClassMetadataLight) cmbClasses.getValue();
                    newLicensePoolAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()),
                            new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue()),
                            new ModuleActionParameter<>(Constants.PROPERTY_CLASSNAME, poolType.getName())
                    ));
                    
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString(ts.getTranslatedString("module.softman.actions.new-pool.success")), NewLicensePoolAction.class));
                    wdwNewPool.close();
                    
                    if (parameters.containsKey("addPool")) {
                        Command addPool = (Command) parameters.get("addPool");
                        addPool.execute();
                    }
                } catch (ModuleActionException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
                }
            });
            
            wdwNewPool.getBtnConfirm().setEnabled(false);
            txtName.addValueChangeListener(listener -> {
                wdwNewPool.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty() && cmbClasses.getValue() != null);
            });
            cmbClasses.addValueChangeListener(listener -> {
                wdwNewPool.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty() && cmbClasses.getValue() != null);
            });

            wdwNewPool.setContent(txtName, txtDescription, cmbClasses);
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return wdwNewPool;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newLicensePoolAction;
    }   
}