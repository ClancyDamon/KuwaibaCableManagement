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
package org.neotropic.kuwaiba.modules.commercial.contractman.actions;

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
import org.neotropic.kuwaiba.modules.commercial.contractman.ContractManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of release object action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class ReleaseObjectFromContractVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private ReleaseObjectFromContractAction releaseObjectFromContractAction;
    /**
     * Close action command
     */
    private Command commandClose;
    
    public ReleaseObjectFromContractVisualAction() {
        super(ContractManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight selectedContract;
        BusinessObjectLight selectedObject;

        if (parameters.containsKey("contract")) {
            selectedContract = (BusinessObjectLight) parameters.get("contract");
            if (parameters.containsKey("businessObject")) {
                selectedObject = (BusinessObjectLight) parameters.get("businessObject");
                commandClose = (Command) parameters.get("commandClose");

                ConfirmDialog wdwReleaseContract = new ConfirmDialog(ts, 
                        this.releaseObjectFromContractAction.getDisplayName(),
                        String.format(ts.getTranslatedString("module.contractman.actions.release-object-from-contract.confirm"), 
                                selectedObject.getName(), selectedContract.getName()),
                        ts.getTranslatedString("module.general.messages.release"));
                wdwReleaseContract.setDraggable(true);
                wdwReleaseContract.setResizable(true);

                wdwReleaseContract.getBtnConfirm().addClickListener((event) -> {
                    try {
                        releaseObjectFromContractAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("contract", selectedContract),
                                new ModuleActionParameter<>("businessObject", selectedObject)));
                        //refresh related grid
                        getCommandClose().execute();
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                String.format(ts.getTranslatedString("module.contractman.actions.release-object-from-contract.success"), selectedObject.getName()),
                                ReleaseObjectFromContractAction.class));
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), ReleaseObjectFromContractAction.class));
                    }
                    wdwReleaseContract.close();
                });
                return wdwReleaseContract;
            } else
                return new Dialog(new Label(ts.getTranslatedString("module.general.messages.object-not-found")));
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "contract")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return releaseObjectFromContractAction;
    }
    
    /**
     * refresh grid
     * @return commandClose; Command; refresh action 
     */
    public Command getCommandClose() {
        return commandClose;
    }

    /**
     * @param commandClose; Command; refresh action 
     */
    public void setCommandClose(Command commandClose) {
        this.commandClose = commandClose;
    }
}