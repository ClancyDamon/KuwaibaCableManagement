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
package org.neotropic.kuwaiba.modules.core.userman.actions;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import java.util.Arrays;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.userman.UserManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new user action.
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewUserVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewUserAction newUserAction;
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

    public NewUserVisualAction() {
        super(UserManagerModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("group")) {
            GroupProfile selectedGroup = (GroupProfile) parameters.get("group");
            
            ConfirmDialog wdwAdd = new ConfirmDialog(ts,
                    this.newUserAction.getDisplayName(),
                    ts.getTranslatedString("module.general.labels.create"));
            wdwAdd.setDraggable(true);
            wdwAdd.setResizable(true);
            wdwAdd.setWidth("60%");
            
            TextField txtName = new TextField(ts.getTranslatedString("module.userman.user-name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();
            
            PasswordField txtPassword = new PasswordField(ts.getTranslatedString("module.login.ui.password"));
            txtPassword.setRequiredIndicatorVisible(true);
            txtPassword.setSizeFull();
            
            TextField txtFirstName = new TextField(ts.getTranslatedString("module.userman.first-name"));
            txtFirstName.setRequiredIndicatorVisible(true);
            txtFirstName.setSizeFull();
            
            TextField txtLastName = new TextField(ts.getTranslatedString("module.userman.last-name"));
            txtLastName.setRequiredIndicatorVisible(true);
            txtLastName.setSizeFull();
            
            TextField txtEmail = new TextField(ts.getTranslatedString("module.userman.email"));
            txtEmail.setRequiredIndicatorVisible(true);
            txtEmail.setSizeFull();
            
            ComboBox<Integer> cmbType = new ComboBox(ts.getTranslatedString("module.userman.user-type"));
            cmbType.setItems(Arrays.asList(UserProfile.USER_TYPE_GUI, UserProfile.USER_TYPE_WEB_SERVICE, UserProfile.USER_TYPE_SOUTHBOUND));
            cmbType.setValue(UserProfile.USER_TYPE_WEB_SERVICE);
            cmbType.setRequiredIndicatorVisible(true);
            cmbType.setAllowCustomValue(false);
            cmbType.setItemLabelGenerator(item -> {
                switch (item) {
                    case UserProfile.USER_TYPE_GUI:
                        return "GUI User"; // I18N
                    case UserProfile.USER_TYPE_WEB_SERVICE:
                        return "Web Service Interface User"; // I18N
                    default:
                        return "Southbound Interface User"; // I18N
                }
            });
            
            FormLayout lytFields = new FormLayout(txtName, txtPassword, txtFirstName, txtLastName, txtEmail, cmbType);
            wdwAdd.setContent(lytFields);
            wdwAdd.getBtnConfirm().addClickListener(evt -> {
                try {
                    if (txtName.getValue().trim().isEmpty())
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                        ts.getTranslatedString("module.userman.user-name")),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                    else if (txtPassword.getValue().trim().isEmpty())
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                        ts.getTranslatedString("module.login.ui.password")),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                    else if (txtFirstName.getValue().isEmpty())
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                        ts.getTranslatedString("module.userman.first-name")),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                    else if (txtLastName.getValue().isEmpty())
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                        ts.getTranslatedString("module.userman.last-name")),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                    else if (txtEmail.getValue().isEmpty())
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                        ts.getTranslatedString("module.userman.email")),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                    else if (cmbType.getValue() == null) 
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                        ts.getTranslatedString("module.userman.user-type")),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                    else {
                        newUserAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("userName", txtName.getValue()),
                                new ModuleActionParameter<>("firstName", txtFirstName.getValue()),
                                new ModuleActionParameter<>("lastName", txtLastName.getValue()),
                                new ModuleActionParameter<>("email", txtEmail.getValue()),
                                new ModuleActionParameter<>("type", cmbType.getValue()),
                                new ModuleActionParameter<>("groupId", selectedGroup.getId()),
                                new ModuleActionParameter<>("password", txtPassword.getValue())));

                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString(ts.getTranslatedString("module.userman.actions.user-created")), NewGroupAction.class));
                        wdwAdd.close();
                    }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewGroupAction.class));
            }
            });
            return wdwAdd;
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "group")));
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newUserAction;
    }
}