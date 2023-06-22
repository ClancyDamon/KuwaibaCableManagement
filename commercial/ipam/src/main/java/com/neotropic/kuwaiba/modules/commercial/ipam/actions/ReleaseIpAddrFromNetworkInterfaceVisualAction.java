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
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.selectors.BusinessObjectSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Action to connect inventory objects with the class which is subclasses of 
 * GenericPort
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class ReleaseIpAddrFromNetworkInterfaceVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Business Object Parameter.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N    
    /**
     * Business Object Parameter.
     */
    public static String PARAM_NETWORK_INTERFACE_CLASSNAME = "networkInterfaceClassName"; //NOI18N    
    /**
     * Business Object Parameter.
     */
    public static String PARAM_NETWORK_INTERFACE_ID = "networkInterfaceId"; //NOI18N    
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Physical Connections Service.
     */
    @Autowired
    private IpamService ipamService;
    /**
     * the target IP address to relate with 
     */
    private BusinessObjectLight targetIpAddr;
    /**
     * Reference to the connect port action.
     */
    @Autowired
    private RelateIpToNetworkInterfaceAction relateIpToNetworkInterfaceAction;


    public ReleaseIpAddrFromNetworkInterfaceVisualAction() {
        super(IpamModule.MODULE_ID);
    }

    @Override
    public String appliesTo() {
        return Constants.CLASS_GENERICPORT;
    }
    
    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }

    @Override
    public ConfirmDialog getVisualComponent(ModuleActionParameterSet parameters) {
        
        BusinessObjectLight networkInterface = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT); //NOI18N
        if (networkInterface == null)
            return null;
        ConfirmDialog wdw = new ConfirmDialog(ts,
                                    String.format(ts.getTranslatedString("module.ipam.actions.relate-ipaddr-to-network-interface.header"), networkInterface.getName()) 
                                    , String.format(ts.getTranslatedString("module.ipam.actions.relate-ipaddr-to-network-interface.name"), networkInterface.getName()) 
                                );
        wdw.setCloseOnOutsideClick(false);
        wdw.setDraggable(true);
        wdw.setResizable(true);
        wdw.setWidth("40%");

        BusinessObjectSelector lytContent = new BusinessObjectSelector(ts.getTranslatedString("module.navigation.actions.copy-business-object.placeholder")
                                                    , aem, bem, mem, ts, Constants.CLASS_IP_ADDRESS, false, Constants.CLASS_IP_ADDRESS);

        lytContent.addSelectedObjectChangeListener(event -> targetIpAddr = event.getSelectedObject());

        wdw.getBtnConfirm().addClickListener(evt -> {
            if (targetIpAddr != null && targetIpAddr.getClassName().equals(Constants.CLASS_IP_ADDRESS)) {
                ModuleActionParameterSet actionParameters = new ModuleActionParameterSet(
                    new ModuleActionParameter(Constants.PROPERTY_ID, targetIpAddr.getId()),
                    new ModuleActionParameter(PARAM_NETWORK_INTERFACE_CLASSNAME, networkInterface.getClassName()),
                    new ModuleActionParameter(PARAM_NETWORK_INTERFACE_ID, networkInterface.getId())
                );
                try {
                    ActionResponse actionResponse = relateIpToNetworkInterfaceAction.getCallback().execute(actionParameters);
                    actionResponse.put(ActionResponse.ActionType.RELATE, "");
                    actionResponse.put(PARAM_BUSINESS_OBJECT, networkInterface);
                    actionResponse.put(PARAM_NETWORK_INTERFACE_ID, targetIpAddr.getId());
                    
                    wdw.close();    
                    new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.information"), 
                            ts.getTranslatedString("module.ipam.actions.related-ipaddr-to-network-interface.success"),
                            AbstractNotification.NotificationType.INFO, 
                            ts
                        ).open();

                } catch (ModuleActionException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
                
                return;
            }
            else{
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.warning"), 
                    ts.getTranslatedString("module.ipam.actions.related-ipaddr-to-network-interface.warning"),
                    AbstractNotification.NotificationType.WARNING, 
                    ts
                ).open();
            }
        });
        
        wdw.setContent(lytContent);
        return wdw;
    }

    @Override
    public AbstractAction getModuleAction() {
        return relateIpToNetworkInterfaceAction;
    }
}
