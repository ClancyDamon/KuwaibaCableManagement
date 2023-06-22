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

package org.neotropic.kuwaiba.core.sessman.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.sessman.SessionManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A frontend to {@link TerminateSessionAction}
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class TerminateSessionVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the backend action.
     */
    @Autowired
    private TerminateSessionAction terminateSessionAction;
    @Autowired
    private TranslationService ts;

    public TerminateSessionVisualAction() {
        super(SessionManagerModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        ConfirmDialog wdwTerminateSession = new ConfirmDialog(ts, new Label(ts.getTranslatedString("module.sessman.actions.terminate-session.are-you-sure")), 
        ts.getTranslatedString("module.general.messages.ok"), () -> {
            try {
                terminateSessionAction.getCallback().execute(parameters);
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS, 
                        ts.getTranslatedString("module.sessman.actions.terminate-session.success"), TerminateSessionVisualAction.class));
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, 
                        ex.getMessage(), TerminateSessionVisualAction.class));
            }
        });
        return wdwTerminateSession;
    }

    @Override
    public AbstractAction getModuleAction() {
        return this.terminateSessionAction;
    }
}
