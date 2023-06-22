/*
 * Copyright 2022 Neotropic SAS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.core.userman.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfile;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.userman.UserManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of update a group action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class UpdateGroupVisualAction extends AbstractVisualAction<Dialog>{
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private UpdateGroupAction updateGroupAction;
    
    public UpdateGroupVisualAction() {
        super(UserManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("group")) {
            GroupProfile group = (GroupProfile) parameters.get("group");
            
            ConfirmDialog wdwUpdate = new ConfirmDialog(ts,
                    ts.getTranslatedString("module.userman.actions.update-group.name"),
                    ts.getTranslatedString("module.general.messages.save"));
            wdwUpdate.setDraggable(true);
            wdwUpdate.setResizable(true);
            
            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setValue(group.getName());
            txtName.setWidthFull();
            
            TextField txtDescription = new TextField(ts.getTranslatedString("module.general.labels.description"));
            txtDescription.setValue(group.getDescription());
            txtDescription.setWidthFull();
            
            wdwUpdate.setContent(txtName, txtDescription);
            wdwUpdate.getBtnConfirm().addClickListener(event -> {
                try {
                    if (txtName.getValue() == null || txtName.getValue().trim().isEmpty())
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                        ts.getTranslatedString("module.general.labels.name")),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                    else {
                        updateGroupAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("groupId", group.getId()),
                                new ModuleActionParameter<>("groupName", txtName.getValue()),
                                new ModuleActionParameter<>("groupDesc", txtDescription.getValue())
                        ));

                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString(ts.getTranslatedString("module.userman.actions.group-updated")), UpdateGroupVisualAction.class));
                        wdwUpdate.close();
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteGroupVisualAction.class));
                }
            });
            return wdwUpdate;
        } else 
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "group")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return updateGroupAction;
    }
}