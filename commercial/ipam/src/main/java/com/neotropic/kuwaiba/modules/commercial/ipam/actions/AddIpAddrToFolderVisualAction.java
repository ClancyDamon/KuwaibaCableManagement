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
import com.neotropic.kuwaiba.modules.commercial.ipam.IpamService;
import com.neotropic.kuwaiba.modules.commercial.ipam.engine.IpamEngine;
import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class AddIpAddrToFolderVisualAction extends AbstractVisualInventoryAction {
    /**
     * New IP address visual action parameter folder id
     */
    public static String PARAM_PARENT_ID = "folderId"; //NOI18N
    /**
     * To know if we are creating a ipv4 or ipv6
     */
    public static String PARAM_IPV = "ipv"; //NOI18N
    /**
     * New IP address parent type
     */
    public static final String PARAM_PARENT_TYPE = "parentType";
    /**
     * Attributes for the new business object
     */
    private HashMap<String, String> attributes;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to module action.
     */
    @Autowired
    private AddIpAddrToFolderAction addIpAddressAction;

    public AddIpAddrToFolderVisualAction() {
        super(IpamModule.MODULE_ID);
    }

    /**
     * Creates the visual component to add a new ip address
     * @param parameters need it parameters
     * @return a dialog
     */
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        attributes = new HashMap();
        
        int ipVersion = (int) parameters.get(PARAM_IPV);
        String folderId = (String) parameters.get(PARAM_PARENT_ID);
        
        ConfirmDialog wdwAddIpAddr = new ConfirmDialog(ts
                , getModuleAction().getDisplayName()
                , ts.getTranslatedString("module.general.labels.create"));
        wdwAddIpAddr.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        wdwAddIpAddr.setDraggable(true);
        
        TextField txtIpAddr = new TextField(ts.getTranslatedString("module.ipam.lbl.ip-address"));
        txtIpAddr.setRequiredIndicatorVisible(true);
        if(ipVersion == 4)
            txtIpAddr.setPlaceholder("e.g. 0.0.0.0");
        else if(ipVersion == 6) 
            txtIpAddr.setPlaceholder("e.g. 0:0:0:0:0:0:0:0");
        
        TextField txtDesc = new TextField(ts.getTranslatedString("module.general.labels.description"));
        txtDesc.setWidthFull();
        
        VerticalLayout lytwrapper = new VerticalLayout(txtIpAddr, txtDesc);
        lytwrapper.setWidthFull();
        lytwrapper.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        wdwAddIpAddr.add(lytwrapper);
        
        txtIpAddr.addValueChangeListener(e -> {
            boolean isValid = false;
            if(ipVersion == 4)
                isValid = IpamEngine.isIpv4Address(e.getValue());
            
            else if(ipVersion == 6)
                isValid = IpamEngine.isIpv6Address(e.getValue());
            
            txtIpAddr.setInvalid(!isValid);
            wdwAddIpAddr.getBtnConfirm().setEnabled(isValid);
        });
        
        wdwAddIpAddr.getBtnConfirm().addClickListener(e -> {
            try {
                attributes.put(Constants.PROPERTY_NAME, txtIpAddr.getValue());
                attributes.put(Constants.PROPERTY_DESCRIPTION, txtDesc.getValue());
                attributes.put(Constants.PROPERTY_MASK, IpamService.DEFAULT_MASK);
                
                addIpAddressAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_PARENT_ID, folderId),
                        new ModuleActionParameter<>(Constants.PROPERTY_ATTRIBUTES, attributes)
                ));

                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(ActionResponse.ActionType.ADD, "");
                
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.ipam.actions.add-ip-addr-in-folder.notification"), 
                                AddIpAddrToFolderAction.class, actionResponse));

                wdwAddIpAddr.close();
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), AddIpAddrToFolderAction.class));
            }
        });
        wdwAddIpAddr.getBtnConfirm().setEnabled(false);
        
        return wdwAddIpAddr;
    }

    @Override
    public AbstractAction getModuleAction() {
        return addIpAddressAction;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
}
