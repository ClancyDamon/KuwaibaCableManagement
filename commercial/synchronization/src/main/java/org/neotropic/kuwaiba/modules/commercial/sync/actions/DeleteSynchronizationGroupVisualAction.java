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
package org.neotropic.kuwaiba.modules.commercial.sync.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationModule;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete Synchronization Group action.
 *
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteSynchronizationGroupVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Parameter, group to be delete.
     */
    public static String PARAM_RELEASE_GROUP = "groups"; //NOI18N
    /**
     * Close action command
     */
    private Command commandClose;
    /**
     * Action command after finish delete group over sync group dialog
     */
    private Command commandDelete;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteSynchronizationGroupAction deleteSynchronizationGroupAction;

    public DeleteSynchronizationGroupVisualAction() {
        super(SynchronizationModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        SynchronizationGroup group;
        if (parameters.containsKey(PARAM_RELEASE_GROUP)) {
            group = (SynchronizationGroup) parameters.get(PARAM_RELEASE_GROUP);
            commandClose = (Command) parameters.get("commandClose");
            commandDelete = (Command) parameters.get("commandDelete");

            ConfirmDialog wdwDeleteGroup = new ConfirmDialog(ts,
                    this.deleteSynchronizationGroupAction.getDisplayName(),
                    String.format(ts.getTranslatedString("module.sync.actions.delete-sync-group.confirm"), group.getName()),
                    ts.getTranslatedString("module.general.labels.delete"));
            wdwDeleteGroup.setDraggable(true);

            wdwDeleteGroup.getBtnConfirm().addClickListener(event -> {
                try {
                    ActionResponse actionResponse
                            = deleteSynchronizationGroupAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(PARAM_RELEASE_GROUP, group)));

                    if (actionResponse.containsKey("exception"))
                        throw new ModuleActionException(((Exception) actionResponse.get("exception")).getLocalizedMessage());
                    //refresh related grid
                    if (getCommandClose() != null) getCommandClose().execute();
                    if (commandDelete != null) commandDelete.execute();
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                            ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.sync.actions.delete-sync-group.success"),
                            DeleteSynchronizationGroupAction.class));
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                            ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteSynchronizationGroupAction.class));
                }
                wdwDeleteGroup.close();
            });
            return wdwDeleteGroup;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.sync.actions.delete-sync-group.error")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteSynchronizationGroupAction;
    }

    /**
     * refresh grid
     *
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