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
package org.neotropic.kuwaiba.modules.optional.contactman.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.business.Contact;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.contactman.ContactManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete contact action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteContactVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteContactAction deleteContactAction;
    /**
     * Close action command
     */
    private Command commandClose; 

    public DeleteContactVisualAction() {
        super(ContactManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        Contact selectedContact;
        if (parameters.containsKey("contact")) {
            selectedContact = (Contact) parameters.get("contact");
            commandClose = (Command) parameters.get("commandClose");
            
            ConfirmDialog wdwDeleteContact = new ConfirmDialog(ts
                    , this.deleteContactAction.getDisplayName()
                    , String.format(ts.getTranslatedString("module.contactman.actions.delete-contact.confirm"), selectedContact.getName())
                    , ts.getTranslatedString("module.general.labels.delete"));
            wdwDeleteContact.setDraggable(true);
            wdwDeleteContact.setResizable(true);
            
            wdwDeleteContact.getBtnConfirm().addClickListener((event) -> {
                try {
                    deleteContactAction.getCallback().execute(new ModuleActionParameterSet(new ModuleActionParameter<>("contact", selectedContact)));
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.contactman.actions.delete-contact.success"), DeleteContactAction.class));
                    wdwDeleteContact.close();
                    //refresh related grid
                    getCommandClose().execute();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteContactAction.class));
                }
            });
            return wdwDeleteContact;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.contactman.actions.delete-contact.error-param-contact")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteContactAction;
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