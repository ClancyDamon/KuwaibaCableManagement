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
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.contactman.ContactManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of release object action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class ReleaseObjectFromContactVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private ReleaseObjectFromContactAction releaseObjectFromContactAction;
    /**
     * Parameter contact 
     */
    private static final String PARAM_CONTACT = "contact";
    /**
     * Parameter business object
     */
    private static final String PARAM_BUSINESSOBJECT = "businessObject";    
    /**
     * Parameter command
     */
    private static final String PARAM_COMMAND = "command";
    
    public ReleaseObjectFromContactVisualAction() {
        super(ContactManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey(PARAM_CONTACT)) {
            BusinessObjectLight contact = (BusinessObjectLight) parameters.get(PARAM_CONTACT);
            if (parameters.containsKey(PARAM_BUSINESSOBJECT)) {
                BusinessObjectLight object = (BusinessObjectLight) parameters.get(PARAM_BUSINESSOBJECT);
                if (parameters.containsKey(PARAM_COMMAND)) {
                    Command command = (Command) parameters.get(PARAM_COMMAND);
                    
                    ConfirmDialog wdwReleaseObject = new ConfirmDialog(ts
                            , this.releaseObjectFromContactAction.getDisplayName()
                            , String.format(ts.getTranslatedString("module.contactman.actions.relase.object-from-contact.confirm")
                                    , object.getName(), contact.getName())
                            , ts.getTranslatedString("module.general.messages.release"));
                    wdwReleaseObject.setDraggable(true);
                    wdwReleaseObject.setResizable(true);
                    
                    wdwReleaseObject.getBtnConfirm().addClickListener(event -> {
                        try {
                            releaseObjectFromContactAction.getCallback().execute(new ModuleActionParameterSet(
                                    new ModuleActionParameter<>(PARAM_CONTACT, contact),
                                    new ModuleActionParameter<>(PARAM_BUSINESSOBJECT, object)
                            ));
                            command.execute();
                            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                    String.format(ts.getTranslatedString("module.contactman.actions.relase.object-from-contact.success"), object.getName()),
                                    ReleaseObjectFromContactAction.class));
                        } catch (ModuleActionException ex) {
                            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), ReleaseObjectFromContactAction.class));
                        }
                        wdwReleaseObject.close();
                    });
                    return wdwReleaseObject;
                } else
                    return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), PARAM_COMMAND)));
            } else
                return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), PARAM_BUSINESSOBJECT))); 
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), PARAM_CONTACT)));
    }

    @Override
    public AbstractAction getModuleAction() {
        return releaseObjectFromContactAction;
    }   
}