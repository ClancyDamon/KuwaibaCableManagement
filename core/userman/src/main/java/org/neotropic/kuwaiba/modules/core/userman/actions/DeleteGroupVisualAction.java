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

import com.vaadin.flow.component.dialog.Dialog;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfile;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.userman.UserManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete a group action.
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteGroupVisualAction extends AbstractVisualAction<Dialog>  {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteGroupAction deleteGroupAction;

    public DeleteGroupVisualAction() {
        super(UserManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        GroupProfile selectedGroup;

        if (parameters.containsKey("group"))
            selectedGroup = (GroupProfile) parameters.get("group");
        else 
            return null;
   
        ConfirmDialog wdwDeleteParameter = new ConfirmDialog(ts, ts.getTranslatedString("module.general.labels.confirmation"),
                String.format(ts.getTranslatedString("module.userman.confirm-delete-group"), selectedGroup.getName()),
                ts.getTranslatedString("module.general.labels.delete"));

        wdwDeleteParameter.getBtnConfirm().addClickListener((event) -> {
            try {
                deleteGroupAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>("group", selectedGroup)
                ));

                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                        ts.getTranslatedString(ts.getTranslatedString("module.userman.actions.group-deleted")), DeleteGroupVisualAction.class));
                wdwDeleteParameter.close();
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), DeleteGroupVisualAction.class));
                wdwDeleteParameter.close();
            }
        });
        return wdwDeleteParameter;
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteGroupAction;
    }
    
}
