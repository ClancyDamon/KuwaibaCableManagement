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
import com.neotropic.kuwaiba.modules.commercial.ipam.engine.IpamEngine;
import com.neotropic.kuwaiba.modules.commercial.ipam.engine.SubnetDetail;
import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
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
public class NewSubnetVisualAction extends AbstractVisualInventoryAction {
    /**
     * Parameter to create the subnet, the parent id
     */
    public static final String PARAM_PARENT_ID = "parentId";
    /**
     * Parameter to create the subnet, the parent id
     */
    public static final String PARAM_PARENT_CLASSNAME = "parentClassName";
    /**
     * Parameter to create the subnet, parent type if is a folder or a subnet
     */
    public static final String PARAM_CIDR = "subnetCidr";
    /**
     * Parameter to create the subnet, the parent class name
     */
    public static final String PARAM_CLASSNAME = "className";
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
    private NewSubnetAction newSubnetAction;
    //GUI
    /**
     * an IP address to create the subnet  
     */
    private TextField txtSubnet;
    /**
     * The number of bits for the subnet
     */
    private IntegerField intBitMask;
    /**
     * The subnet Network address
     */
    private Label lblNetworkAddress = new Label();
    /**
     * The broadcast subnet address
     */
    private Label lblBroadcastkAddress = new Label();
    /**
     * The subnet number of hosts
     */
    private Label lblNumberOfHosts = new Label();
    /**
     * The subnet mask
     */
    private Label lblMask = new Label();
    /**
    * Form layout to hold the subnet details 
    */
    private FormLayout lytSubnetDetails;
    /**
     * A shor description of the subnet
     */
    private TextField txtDescription ;
    /**
     * To load the parent current bit mask
     */
    private int currentBitMask;
    /**
     * To load the parent subnet address
     */
    private String currentSubnetAddr;
    
    public NewSubnetVisualAction() {
        super(IpamModule.MODULE_ID);
        currentSubnetAddr = null;
    }
    
    /**
     * Creates the the component for a new subnet
     * @param parameters need it parameters
     * @return a dialog
     */
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        attributes = new HashMap();
        
        String parentId = (String) parameters.get(PARAM_PARENT_ID);
        String className = (String) parameters.get(PARAM_CLASSNAME);
        String parentClassName = (String) parameters.get(PARAM_PARENT_CLASSNAME);
        String subnetCidr = (String) parameters.get(PARAM_CIDR);
        int version = className.equals(Constants.CLASS_SUBNET_IPV4) ? 4 : (className.equals(Constants.CLASS_SUBNET_IPV6) ? 6 : 0);
        
        intBitMask = new IntegerField(ts.getTranslatedString("module.ipam.subnet.mask-bits"));
        
        if(subnetCidr != null && IpamEngine.isCIDRFormat(subnetCidr)){
            String[] split = subnetCidr.split("/");
            currentSubnetAddr = split[0];
            currentBitMask = Integer.valueOf(split[1]) + 1;
            intBitMask.setMin(currentBitMask);
        }else{
            currentBitMask = (version == 4 ? 24 : (version == 6 ? 64 : 0));
            intBitMask.setMin(1);
        }
        intBitMask.setValue(currentBitMask);
        intBitMask.setStep(1);
        intBitMask.setMax(version == 4 ? 32 : (version == 6 ? 128 : 0));
        intBitMask.setHasControls(true);
        intBitMask.getElement().getStyle().set("margin-left", "auto");
        
        ConfirmDialog wdwNewSubnet = new ConfirmDialog(ts
                , getModuleAction().getDisplayName()
                , ts.getTranslatedString("module.general.labels.create"));
        wdwNewSubnet.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        
        txtSubnet = new TextField(ts.getTranslatedString("module.ipam.lbl.subnet"));
        if(version == 4)
            txtSubnet.setPlaceholder(ts.getTranslatedString("module.ipam.actions.add-subnet.lbl.type-subnetv4"));
        else if(version == 6)
            txtSubnet.setPlaceholder(ts.getTranslatedString("module.ipam.actions.add-subnet.lbl.type-subnetv6"));
        
        txtSubnet.setRequiredIndicatorVisible(true);
        txtSubnet.setValueChangeMode(ValueChangeMode.EAGER);
        txtSubnet.setWidth("250px");
        loadSubnetParentAddress(subnetCidr, version);
        
        
        HorizontalLayout lytSubnetDefinition =  new HorizontalLayout(txtSubnet, intBitMask);
        lytSubnetDefinition.setPadding(false);
        lytSubnetDefinition.setMargin(false);
        lytSubnetDefinition.setWidthFull();
        lytSubnetDefinition.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        
        //Gui Subnet Details        
        //Desc
        txtDescription = new TextField();
        txtDescription.setPlaceholder(ts.getTranslatedString("module.ipam.actions.add-subnet.place-holder"));
        
        lblNetworkAddress = new Label();
        lblBroadcastkAddress = new Label();
        lblNumberOfHosts = new Label();
        lblMask = new Label();
        
        lytSubnetDetails = new FormLayout();
        lytSubnetDetails.setVisible(false);
        lytSubnetDetails.setResponsiveSteps(
                new ResponsiveStep("15em", 1),
                new ResponsiveStep("40em", 2));
        lytSubnetDetails.add(txtDescription);
        lytSubnetDetails.setColspan(txtDescription, 2);
        lytSubnetDetails.addFormItem(lblNetworkAddress, ts.getTranslatedString("module.ipam.subnet.network-address"));
        lytSubnetDetails.addFormItem(lblBroadcastkAddress, ts.getTranslatedString("module.ipam.subnet.broadcast-address"));
        lytSubnetDetails.addFormItem(lblNumberOfHosts, ts.getTranslatedString("module.ipam.subnet.hosts-number"));
        if(version == 4)
            lytSubnetDetails.addFormItem(lblMask, ts.getTranslatedString("module.ipam.subnet.net-mask"));
        
        wdwNewSubnet.setContent(new VerticalLayout(lytSubnetDefinition, lytSubnetDetails));
        
        txtSubnet.addValueChangeListener(e -> 
            wdwNewSubnet.getBtnConfirm().setEnabled(validateSubnet(subnetCidr, version))
        );
        
        intBitMask.addValueChangeListener(e -> {
            boolean validSubnet = validateSubnet(subnetCidr, version);
            wdwNewSubnet.getBtnConfirm().setEnabled(e.getValue() >= currentBitMask && e.getValue() <= 32 && validSubnet);
        });
        
        //Buttons
        wdwNewSubnet.getBtnConfirm().setEnabled(false);
        wdwNewSubnet.getBtnConfirm().addClickListener(e -> {
            try {
                attributes.put(Constants.PROPERTY_NAME, txtSubnet.getValue() + "/" + Integer.toString(intBitMask.getValue()));
                attributes.put(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue());
                
                newSubnetAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_PARENT_ID, parentId),
                        new ModuleActionParameter<>(Constants.PROPERTY_PARENT_CLASS_NAME, parentClassName),
                        new ModuleActionParameter<>(Constants.PROPERTY_CLASSNAME, className),
                        new ModuleActionParameter<>(Constants.PROPERTY_ATTRIBUTES, attributes)
                ));

                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(ActionResponse.ActionType.ADD, "");
                
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.ipam.actions.add-subnet.notification"), 
                                NewSubnetAction.class, actionResponse));

                wdwNewSubnet.close();
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewSubnetAction.class));
            }
        });
        
        return wdwNewSubnet;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newSubnetAction;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
    
    private boolean validateSubnet(String subnetCidr, int version){
        boolean isValid = false;
        SubnetDetail subnetDetail = null;
        DecimalFormat df = new DecimalFormat("###,###,###");
        try {
            if(version == 4){        
                isValid = IpamEngine.isIpv4Address(txtSubnet.getValue());
                if(isValid){
                    String subnetCidrFormat = txtSubnet.getValue() + "/" + intBitMask.getValue();
                    boolean isValidCidrFormat = IpamEngine.isCIDRFormat(subnetCidrFormat);
                    lytSubnetDetails.setVisible(isValidCidrFormat);
                    //We calculate everithig about the subnet
                    subnetDetail = new SubnetDetail(subnetCidrFormat);
                    IpamEngine.ipv4SubnetCalculation(subnetDetail);
                    attributes.put(Constants.PROPERTY_BROADCAST_IP, subnetDetail.getBroadCastIpAddr());
                    attributes.put(Constants.PROPERTY_NETWORK_IP, subnetDetail.getNetworkIpAddr());
                    attributes.put(Constants.PROPERTY_HOSTS, Integer.toString(subnetDetail.getNumberOfHosts()));
                    attributes.put(Constants.PROPERTY_MASK, subnetDetail.getMask());
                    
                    //Gui feedback
                    lblBroadcastkAddress.setText(subnetDetail.getBroadCastIpAddr());
                    lblNetworkAddress.setText(subnetDetail.getNetworkIpAddr());
                    lblNumberOfHosts.setText(String.format("%,d", subnetDetail.getNumberOfHosts()));
                    lblMask.setText(subnetDetail.getMask());

                    if(subnetCidr != null){
                        isValid = currentBitMask <= intBitMask.getValue();

                        if(isValid){
                            if(currentBitMask <= 8){
                                isValid = IpamEngine.getIpv4Segment(currentSubnetAddr, 0).equals(
                                    IpamEngine.getIpv4Segment(txtSubnet.getValue(), 0));
                            }else if(currentBitMask > 8 && currentBitMask <= 16){
                                isValid =  IpamEngine.getIpv4Segment(currentSubnetAddr, 0).equals(
                                        IpamEngine.getIpv4Segment(txtSubnet.getValue(), 0)) 
                                        && IpamEngine.getIpv4Segment(currentSubnetAddr, 1).equals(
                                    IpamEngine.getIpv4Segment(txtSubnet.getValue(), 1));
                            } else if(currentBitMask > 16 && currentBitMask <= 24){
                                isValid =  IpamEngine.getIpv4Segment(currentSubnetAddr, 0).equals(
                                            IpamEngine.getIpv4Segment(txtSubnet.getValue(), 0)) 
                                        && IpamEngine.getIpv4Segment(currentSubnetAddr, 1).equals(
                                            IpamEngine.getIpv4Segment(txtSubnet.getValue(), 1))
                                        && IpamEngine.getIpv4Segment(currentSubnetAddr, 2).equals(
                                            IpamEngine.getIpv4Segment(txtSubnet.getValue(), 2));
                            }else if(currentBitMask > 24 && currentBitMask <= 32){
                                isValid =  IpamEngine.getIpv4Segment(currentSubnetAddr, 0).equals(
                                            IpamEngine.getIpv4Segment(txtSubnet.getValue(), 0)) 
                                        && IpamEngine.getIpv4Segment(currentSubnetAddr, 1).equals(
                                            IpamEngine.getIpv4Segment(txtSubnet.getValue(), 1))
                                        && IpamEngine.getIpv4Segment(currentSubnetAddr, 2).equals(
                                            IpamEngine.getIpv4Segment(txtSubnet.getValue(), 2));
                            }
                        if(!isValid)
                            txtSubnet.setErrorMessage(ts.getTranslatedString(
                                String.format(ts.getTranslatedString("module.ipam.actions.add-subnet.error-invalid-subnet")
                                    , subnetCidr, currentSubnetAddr)));
                    }
                }
            }
        }
        else if(version == 6){
            isValid = IpamEngine.isIpv6Address(txtSubnet.getValue());
            if(isValid){
                String subnetCidrFormat = txtSubnet.getValue() + "/" + intBitMask.getValue();
                boolean isValidCidrFormat = IpamEngine.isCIDRFormat(subnetCidrFormat);
                lytSubnetDetails.setVisible(isValidCidrFormat);
                //We calculate everithig about the subnet
                subnetDetail = new SubnetDetail(subnetCidrFormat);
                IpamEngine.ipv6SubnetCalculation(subnetDetail);
                attributes.put(Constants.PROPERTY_BROADCAST_IP, IpamEngine.compressIpv6(subnetDetail.getBroadCastIpAddr()));
                attributes.put(Constants.PROPERTY_NETWORK_IP, IpamEngine.compressIpv6(subnetDetail.getNetworkIpAddr()));
                attributes.put(Constants.PROPERTY_HOSTS, Integer.toString(subnetDetail.getNumberOfHosts()));
                //Gui feedback
                lblNetworkAddress.setText(IpamEngine.compressIpv6(subnetDetail.getNetworkIpAddr()));
                lblBroadcastkAddress.setText(IpamEngine.compressIpv6(subnetDetail.getBroadCastIpAddr()));
                lblNumberOfHosts.setText(String.format("%,d", subnetDetail.getNumberOfHosts()));
            }
        }
        
        txtSubnet.setInvalid(!isValid);
        
         } catch (InvalidArgumentException ex) {
            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
            ex.getMessage(), NewSubnetAction.class));
        }
         
        return isValid;
    }
    
    private void loadSubnetParentAddress(String subnetCidr, int version) {
        if(subnetCidr != null){
            if(version == 4){
                //first segment
                if(currentBitMask <= 8)
                    txtSubnet.setPlaceholder(String.format("e.g. %s.0.0.0"
                            , IpamEngine.getIpv4Segment(currentSubnetAddr, 0))); 

                else if(currentBitMask > 8 && currentBitMask <= 16)
                    txtSubnet.setPlaceholder(String.format("e.g. %s.%s.0.0"
                            , IpamEngine.getIpv4Segment(currentSubnetAddr, 0) 
                            , IpamEngine.getIpv4Segment(currentSubnetAddr, 1))); 

                else if(currentBitMask > 16 && currentBitMask <= 24)
                    txtSubnet.setPlaceholder(String.format("e.g. %s.%s.%s.0"
                            , IpamEngine.getIpv4Segment(currentSubnetAddr, 0) 
                            , IpamEngine.getIpv4Segment(currentSubnetAddr, 1)
                            , IpamEngine.getIpv4Segment(currentSubnetAddr, 2))); 

                else if(currentBitMask > 24 && currentBitMask <= 32)
                    txtSubnet.setPlaceholder(String.format("e.g. %s.%s.%s.%s"
                            , IpamEngine.getIpv4Segment(currentSubnetAddr, 0) 
                            , IpamEngine.getIpv4Segment(currentSubnetAddr, 1)
                            , IpamEngine.getIpv4Segment(currentSubnetAddr, 2)
                            , IpamEngine.getIpv4Segment(currentSubnetAddr, 4))); 
            }
            else if(version == 6){
            
            }
        }
    }
}
