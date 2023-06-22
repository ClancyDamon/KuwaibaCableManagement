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
package org.neotropic.kuwaiba.modules.core.datamodelman.actions;

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
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.datamodelman.DataModelManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new class action.
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewClassVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * New class visual action parameter class.
     */
    public static String PARAM_CLASS = "class"; //NOI18N
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewClassAction newClassAction;
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

    public NewClassVisualAction() {
        super(DataModelManagerModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey(PARAM_CLASS)) {
            ClassMetadataLight parentClass = (ClassMetadataLight) parameters.get(PARAM_CLASS);
            
            ConfirmDialog wdwAdd = new ConfirmDialog(ts,
                    ts.getTranslatedString("module.datamodelman.actions.new-class.name"),
                    ts.getTranslatedString("module.general.labels.create"));
            wdwAdd.setResizable(true);
            wdwAdd.setDraggable(true);
            
            BoldLabel lblParentClass = new BoldLabel("Parent Class: " + parentClass.getName());
            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();
            txtName.focus();
            
            wdwAdd.setContent(lblParentClass, txtName);
            wdwAdd.getBtnConfirm().addClickListener(event -> {
                try {
                    if (txtName.getValue() == null || txtName.getValue().trim().isEmpty()) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                        ts.getTranslatedString("module.general.labels.name")),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                    } else {
                        newClassAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("parentClass", parentClass.getName()),
                                new ModuleActionParameter<>("name", txtName.getValue())));

                        ActionResponse actionResponse = new ActionResponse();
                        actionResponse.put(ActionResponse.ActionType.ADD, "");
                        actionResponse.put(Constants.PROPERTY_PARENT_ID, parentClass.getId() <= 0 ? "-1" : parentClass.getId());
                        actionResponse.put(Constants.PROPERTY_PARENT_CLASS_NAME, parentClass.getName());
                        actionResponse.put(PARAM_CLASS, parentClass);
                        
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.datamodelman.actions.new-class-created-success"), NewClassAction.class, actionResponse));

                        wdwAdd.close();
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener
                            .ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), NewClassAction.class));
                }
            });
            return wdwAdd;
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), PARAM_CLASS)));
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newClassAction;
    }
}