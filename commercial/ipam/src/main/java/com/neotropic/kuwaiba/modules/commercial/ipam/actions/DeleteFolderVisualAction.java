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
package com.neotropic.kuwaiba.modules.commercial.ipam.actions;

import com.neotropic.kuwaiba.modules.commercial.ipam.IpamModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete a folder in ipam module
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class DeleteFolderVisualAction extends AbstractVisualInventoryAction {
    /**
     * New business object visual action parameter business object.
     */
    public static String PARAM_FOLDER = "folder"; //NOI18N
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to module action.
     */
    @Autowired
    private DeleteFolderAction deleteFolderAction;

    public DeleteFolderVisualAction() {
        super(IpamModule.MODULE_ID);
    }

    /**
     * Creates the visual component for delete folder visual action
     * @param parameters need it parameters
     * @return a dialog
     */
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        
        HorizontalLayout lytExtraFields =  new HorizontalLayout();
        lytExtraFields.setWidthFull();
        
        InventoryObjectPool folder = (InventoryObjectPool) parameters.get(PARAM_FOLDER);
        
        ConfirmDialog wdwDeleteSubnet = new ConfirmDialog(ts
                , getModuleAction().getDisplayName()
                , ts.getTranslatedString("module.general.labels.delete"));
        
        Label lblConfimation = new Label(String.format(ts.getTranslatedString("module.ipam.actions.delete-folder.confirmation"), folder.getName()));
        
        wdwDeleteSubnet.setContent(lblConfimation);

        //buttons
        wdwDeleteSubnet.getBtnConfirm().addClickListener(e -> {
            try {
                deleteFolderAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_ID, folder.getId())
                ));

                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(ActionResponse.ActionType.REMOVE, "");
                
                fireActionCompletedEvent(
                        new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.ipam.actions.delete-folder.notification"), 
                                DeleteFolderAction.class, actionResponse));

                wdwDeleteSubnet.close();
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), DeleteFolderAction.class));
            }
        });
        return wdwDeleteSubnet;
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteFolderAction;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
}
