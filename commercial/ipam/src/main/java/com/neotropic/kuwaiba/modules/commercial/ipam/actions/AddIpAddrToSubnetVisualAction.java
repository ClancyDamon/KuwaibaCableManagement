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
import com.neotropic.kuwaiba.modules.commercial.ipam.engine.SubnetDetail;
import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of a new business object action.
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class AddIpAddrToSubnetVisualAction extends AbstractVisualInventoryAction {
    /**
     * New IP address visual action parameter ip address next possible ip
     */
    public static String PARAM_SUBNET = "subnet"; //NOI18N
    /**
     * Attributes for the new business object
     */
    private List<HashMap<String, String>> ipAddressesAttributes;
    /**
     * input field for the ip address
     */
    private TextField txtIpAddr;
    /**
     * Show the last IP address created
     */
    private Label lblNextPossibleIpAddress;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    
    @Autowired
    private IpamService ipamService;
    /**
     * Reference to module action.
     */
    @Autowired
    private AddIpAddrToSubnetAction addIpAddressAction;
    /**
     * IP Addreses created in the subnet
     */    
    private List<BusinessObjectLight> ipAddressesCreatedInSubnet;
    /**
     *
     */    
    private List<String> ipAddressToCreate;

    public AddIpAddrToSubnetVisualAction() {
        super(IpamModule.MODULE_ID);
    }

    /**
     * Creates the visual component to add a new ip address
     * @param parameters need it parameters
     * @return a dialog
     */
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        ipAddressesAttributes = new ArrayList<>();
                
        //create a several ip address
        BusinessObjectLight subnet = (BusinessObjectLight) parameters.get(PARAM_SUBNET);
        SubnetDetail subnetDetail = new SubnetDetail(subnet.getName());
        
        try {
            ipAddressesCreatedInSubnet = ipamService.getSubnetIpAddrCreated(subnet.getId(), subnet.getClassName(), -1, -1);
            if(subnet.getClassName().equals(Constants.CLASS_SUBNET_IPV4))
                IpamEngine.ipv4SubnetCalculation(subnetDetail);
            if(subnet.getClassName().equals(Constants.CLASS_SUBNET_IPV6))
                IpamEngine.ipv6SubnetCalculation(subnetDetail);
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | NotAuthorizedException | InvalidArgumentException ex) {
            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                ex.getMessage(), AddIpAddrToSubnetAction.class));
        }
        
        ConfirmDialog wdwAddIpAddr = new ConfirmDialog(ts
                , getModuleAction().getDisplayName()
                , ts.getTranslatedString("module.general.labels.create"));
        wdwAddIpAddr.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        wdwAddIpAddr.setDraggable(true);
        
        txtIpAddr = new TextField(ts.getTranslatedString("module.ipam.lbl.ip-address"));
        txtIpAddr.setRequiredIndicatorVisible(true);
        txtIpAddr.setWidthFull();
        
        Checkbox chkReserved = new Checkbox(ts.getTranslatedString("module.ipam.states.ip-addr.reserved"));
        chkReserved.setValue(false);
        
        Checkbox chkCreateAllIpAddress = new Checkbox(ts.getTranslatedString("module.ipam.actions.add-ip-addr.create-all"));
        chkCreateAllIpAddress.setValue(false);
        
        calculateFreeIpAddress(subnetDetail);
                
        TextField txtDesc = new TextField(ts.getTranslatedString("module.general.labels.description"));
        txtDesc.setWidthFull();
        
        HorizontalLayout lytCheckers = new HorizontalLayout(chkCreateAllIpAddress, chkReserved);

        chkReserved.getElement().getStyle().set("margin-left", "auto");
        lytCheckers.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytCheckers.setWidthFull();

        VerticalLayout lytwrapper = new VerticalLayout(txtIpAddr, lytCheckers, txtDesc);
        lytwrapper.setWidthFull();
        
        wdwAddIpAddr.add(lytwrapper);
        
        txtIpAddr.addValueChangeListener(e -> {
                boolean isValid = false;
            ipAddressToCreate = new ArrayList<>();
            //We are creating several IP Addresses
            if(e.getValue().contains(",")){
                String[] ipAddrs = e.getValue().replaceAll(" ", "").split(",");
                subnetDetail.getRange();
                for (String ipAddr : ipAddrs) {
                    isValid = validateIpAddress(ipAddr, subnetDetail);
                    if(!alreadyExists(ipAddr) && isValid)
                        ipAddressToCreate.add(ipAddr);
                }
            }
            else if(e.getValue().contains("-")){
                String[] ipAddrs = e.getValue().replaceAll(" ", "").split("-");
                isValid = validateIpAddress(ipAddrs[0], subnetDetail) && validateIpAddress(ipAddrs[1], subnetDetail);
                
                if(isValid){
                    String ipv4nextAddr =  ipAddrs[0];
                    ipAddressToCreate.add(ipv4nextAddr);
                    while(!ipv4nextAddr.equals(ipAddrs[1])){
                        ipv4nextAddr = IpamEngine.ipv4nextAddr(subnetDetail.getNetworkIpAddr(), subnetDetail.getBroadCastIpAddr(), ipv4nextAddr, subnetDetail.getMaskBits());
                        if(!alreadyExists(ipv4nextAddr))
                            ipAddressToCreate.add(ipv4nextAddr);
                    }
                }
            }
            else{
                isValid = validateIpAddress(e.getValue(), subnetDetail);
                txtIpAddr.setInvalid(!isValid);
                if(!alreadyExists(txtIpAddr.getValue()))
                    ipAddressToCreate.add(txtIpAddr.getValue());
                else
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning")
                        , ts.getTranslatedString(String.format(ts.getTranslatedString("module.ipam.actions.add-ipddr-exists.warning"), subnetDetail.getCidr()))
                                , AbstractNotification.NotificationType.INFO
                                , ts).open();
            }
            wdwAddIpAddr.getBtnConfirm().setEnabled(isValid);
        });
       
        chkCreateAllIpAddress.addValueChangeListener(c -> {
            ipAddressToCreate = new ArrayList<>();
            if(c.getValue()){
                String ipv4nextAddr = subnetDetail.getNetworkIpAddr();
                while(!ipv4nextAddr.equals(subnetDetail.getBroadCastIpAddr())){
                    ipv4nextAddr = IpamEngine.ipv4nextAddr(subnetDetail.getNetworkIpAddr(), subnetDetail.getBroadCastIpAddr(), ipv4nextAddr, subnetDetail.getMaskBits());
                    if(!alreadyExists(ipv4nextAddr))
                        ipAddressToCreate.add(ipv4nextAddr);
                }
                txtIpAddr.setEnabled(false);
                wdwAddIpAddr.getBtnConfirm().setEnabled(true);
            }
            else{
                wdwAddIpAddr.getBtnConfirm().setEnabled(false);
                txtIpAddr.setEnabled(true);
                ipAddressToCreate.clear();
            }
        });

        wdwAddIpAddr.getBtnConfirm().addClickListener(e -> {
            try {  
                for (String ipAddr : ipAddressToCreate) {
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put(Constants.PROPERTY_NAME, ipAddr);
                    attributes.put(Constants.PROPERTY_DESCRIPTION, txtDesc.getValue());
                    if(chkReserved.getValue())
                        attributes.put(Constants.PROPERTY_STATE, "");
                    attributes.put(Constants.PROPERTY_MASK, subnetDetail.getMask());
                    ipAddressesAttributes.add(attributes);
                }

                addIpAddressAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_PARENT_ID, subnet.getId()),
                        new ModuleActionParameter<>(Constants.PROPERTY_PARENT_CLASS_NAME, subnet.getClassName()),
                        new ModuleActionParameter<>(Constants.PROPERTY_ATTRIBUTES, ipAddressesAttributes)
                ));

                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(ActionResponse.ActionType.ADD, txtIpAddr.getValue());

                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.ipam.actions.add-subnet.notification"), 
                                AddIpAddrToSubnetAction.class, actionResponse));

                wdwAddIpAddr.close();
                    
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), AddIpAddrToSubnetAction.class));
            }
        });
        wdwAddIpAddr.getBtnConfirm().setEnabled(false);
        return wdwAddIpAddr;
    }

    /**
     * Validates if a given string si a valid IP Address an belongs to the subnet where its been created
     */    
    private boolean validateIpAddress(String ipAddr, SubnetDetail subnetDetail){
        if(subnetDetail.getIpAddrV() == 4 && IpamEngine.ipv4addrBelongsToSubnet(subnetDetail.getNetworkIpAddr(), ipAddr, subnetDetail.getMaskBits()))
            return IpamEngine.isIpv4Address(ipAddr);

        else if(subnetDetail.getIpAddrV() == 6 && IpamEngine.ipv6AddrBelongsToSubnet(subnetDetail.getNetworkIpAddr(), ipAddr, subnetDetail.getMaskBits()))
            return IpamEngine.isIpv6Address(ipAddr);

        
        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning")
            , ts.getTranslatedString(String.format(ts.getTranslatedString("module.ipam.actions.add-ipddr-not-belongs-to-subnet.warning"), subnetDetail.getCidr()))
            , AbstractNotification.NotificationType.INFO
            , ts).open();

        return false;
    }

    private void calculateFreeIpAddress(SubnetDetail subnetDetail){
        if(subnetDetail.getIpAddrV() == 4){//first segment
            if(subnetDetail.getMaskBits() <= 8)
                txtIpAddr.setPlaceholder(String.format("e.g. %s."
                        , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 0))); 

            else if(subnetDetail.getMaskBits() > 8 && subnetDetail.getMaskBits() <= 16)
                txtIpAddr.setPlaceholder(String.format("e.g. %s.%s."
                        , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 0) 
                        , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 1))); 

            else if(subnetDetail.getMaskBits() > 16 && subnetDetail.getMaskBits() <= 24)
                txtIpAddr.setPlaceholder(String.format("e.g. %s.%s.%s."
                        , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 0) 
                        , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 1)
                        , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 2))); 

            else if(subnetDetail.getMaskBits() > 24 && subnetDetail.getMaskBits() <= 32)
                txtIpAddr.setPlaceholder(String.format("e.g. %s.%s.%s."
                        , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 0) 
                        , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 1)
                        , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 2)
                        )); 
        }
        else if(subnetDetail.getIpAddrV() == 6){
            txtIpAddr.setPlaceholder("e.g. %s:%s:%s::"); 
        }
    }

    /**
     * Checks if a given IP Address is already created
     */
    private boolean alreadyExists(String ipAddr){
        for (BusinessObjectLight createdIpAddr : ipAddressesCreatedInSubnet) {
            if(createdIpAddr.getName().equals(ipAddr))
                return true;
        }
        return false;
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
