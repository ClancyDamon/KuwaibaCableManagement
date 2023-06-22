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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of a new business object action.
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class NewFolderVisualAction extends AbstractVisualInventoryAction {
    /**
     * Parameter to create a new folder, the parent oid of the new folder
     */
    public static final String PARAM_PARENT_FOLDER_ID = "parentId";
    /**
     * Parameter to create a new folder, the parent class of the new folder(pool)
     */
    public static final String PARAM_PARENT_FOLDER_CLASSNAME = "parentClassName";
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to module action.
     */
    @Autowired
    private NewFolderAction newFolderAction;
    
    public NewFolderVisualAction() {
        super(IpamModule.MODULE_ID);
    }

    /**
     * Creates the visual component for new folder visual action
     * @param parameters need it parameters
     * @return a dialog
     */
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        HashMap<String, String> attributes = new HashMap();
        
        HorizontalLayout lytExtraFields =  new HorizontalLayout();
        lytExtraFields.setWidthFull();
        
        String parentId = (String) parameters.get(PARAM_PARENT_FOLDER_ID);
        String folderClassName = (String) parameters.get(PARAM_PARENT_FOLDER_CLASSNAME);
        
        ConfirmDialog wdwNewFolder = new ConfirmDialog(ts
                , getModuleAction().getDisplayName()
                , ts.getTranslatedString("module.general.labels.create"));
 
        //Content
        TextField txtFolderName = new TextField(ts.getTranslatedString("module.general.labels.name"));
        txtFolderName.addValueChangeListener(e-> {
            e.getSource().setInvalid(e.getValue().isEmpty());
            wdwNewFolder.getBtnConfirm().setEnabled(!e.getValue().isEmpty());
        });
        txtFolderName.addValueChangeListener(e -> wdwNewFolder.getBtnConfirm().setEnabled(!e.getValue().isEmpty()));
        txtFolderName.setRequiredIndicatorVisible(true);
        txtFolderName.setWidthFull();
        
        TextField txtDesc = new TextField(ts.getTranslatedString("module.general.labels.description"));
        txtDesc.setRequired(true);
        txtDesc.setRequiredIndicatorVisible(true);
        txtDesc.setWidthFull();
        
        VerticalLayout lytSubentBitMask =  new VerticalLayout(txtFolderName, txtDesc);
        lytSubentBitMask.setPadding(false);
        lytSubentBitMask.setMargin(false);
        lytSubentBitMask.setWidthFull();
        lytSubentBitMask.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
                
        wdwNewFolder.setContent(lytSubentBitMask);
        wdwNewFolder.getBtnConfirm().setEnabled(false);
        wdwNewFolder.getBtnConfirm().addClickListener(e -> {
            try {
                attributes.put(Constants.PROPERTY_NAME, txtFolderName.getValue());
                attributes.put(Constants.PROPERTY_DESCRIPTION, txtDesc.getValue());
                
                newFolderAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(PARAM_PARENT_FOLDER_ID, parentId),
                        new ModuleActionParameter<>(PARAM_PARENT_FOLDER_CLASSNAME, folderClassName),
                        new ModuleActionParameter<>(Constants.PROPERTY_ATTRIBUTES, attributes)
                ));

                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(ActionResponse.ActionType.ADD, "");
                
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.ipam.actions.add-folder.notification"), 
                                NewSubnetAction.class, actionResponse));

                wdwNewFolder.close();
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewSubnetAction.class));
            }
        });
        
        return wdwNewFolder;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newFolderAction;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
}
