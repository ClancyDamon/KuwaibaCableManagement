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
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
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
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
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
public class SplitSubnetVisualAction extends AbstractVisualInventoryAction {
    /**
     * Parameter to create the subnet, the parent id
     */
    public static final String PARAM_SUBNET = "subnet";
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
    private SplitSubnetAction splitSubnetAction;
    //GUI
    /**
     * The number of bits for the subnet
     */
    private IntegerField intBitMask;
    /**
     * The subnet Network address
     */
    private Grid<SubnetDetail> grdSubnets;
    /**
    * layout to hold the subnets details 
    */
    private VerticalLayout lytSubnetDetails;
    /**
     * A shor description of the subnet
     */
    private TextField txtDescription ;
    /**
     * subnets after split of the subnet
     */
    private List<SubnetDetail> splitedSubnets;
    
    public SplitSubnetVisualAction() {
        super(IpamModule.MODULE_ID);
    }
    
    /**
     * Creates the the component for a new subnet
     * @param parameters need it parameters
     * @return a dialog
     */
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        attributes = new HashMap();
        splitedSubnets = new ArrayList<>();
        
        grdSubnets = new Grid();
        grdSubnets.setSelectionMode(Grid.SelectionMode.NONE);
        grdSubnets.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grdSubnets.setWidthFull();
        grdSubnets.setHeightByRows(true);
        
        grdSubnets.addColumn(SubnetDetail::getCidr)
                .setFlexGrow(0).setWidth("130px")
                .setResizable(true)
                .setHeader(ts.getTranslatedString("module.ipam.lbl.ip-address"));
        grdSubnets.addColumn(SubnetDetail::getMask)
                .setFlexGrow(0).setWidth("130px")
                .setResizable(true)
                .setHeader(ts.getTranslatedString("module.ipam.subnet.net-mask"));
        grdSubnets.addColumn(subnet -> 
                subnet.getNetworkIpAddr() + "-" + subnet.getBroadCastIpAddr())
                .setFlexGrow(0).setWidth("180px")
                .setResizable(true)
                .setHeader(ts.getTranslatedString("module.ipam.subnet.range"));
        grdSubnets.addColumn(SubnetDetail::getNumberOfHosts)
                .setHeader(ts.getTranslatedString("module.ipam.subnet.hosts"))
                .setTextAlign(ColumnTextAlign.CENTER);
                
        //Desc
        txtDescription = new TextField();
        txtDescription.setPlaceholder(ts.getTranslatedString("module.ipam.actions.split-subnet.place-holder"));
        txtDescription.setWidthFull();
        
        lytSubnetDetails = new VerticalLayout();
        lytSubnetDetails.setWidthFull();
        lytSubnetDetails.setVisible(false);
        lytSubnetDetails.add(txtDescription, grdSubnets);
        
        BusinessObject subnet = (BusinessObject) parameters.get(PARAM_SUBNET);
        
        ConfirmDialog wdwSplitSubnet = new ConfirmDialog(ts
                , getModuleAction().getDisplayName()
                , ts.getTranslatedString("module.general.labels.split"));
        wdwSplitSubnet.setWidth("600px");
        wdwSplitSubnet.addThemeVariants(EnhancedDialogVariant.SIZE_LARGE);
        //Content 
        Label lblSubnet = new Label(String.format("%s: %s"
                , ts.getTranslatedString("module.ipam.lbl.subnet")
                , subnet.getName()));
        
        Label lblBitMaskToSplit =  new Label(ts.getTranslatedString("module.ipam.subnet.mask-bits"));
        String[] subnetName = subnet.getName().split("/");
        if(subnetName.length != 2)
            System.out.println("error");
        
        String subnetAddress = subnet.getName().split("/")[0];
        int maskBits = Integer.valueOf(subnet.getName().split("/")[1]);
        String broadcast = (String)subnet.getAttributes().get(Constants.PROPERTY_BROADCAST_IP);
        
        intBitMask = new IntegerField();
        intBitMask.setRequiredIndicatorVisible(true);
        intBitMask.setValue(Integer.valueOf(subnetName[1]));
        intBitMask.setStep(1);
        intBitMask.setMin(maskBits);
        intBitMask.setHasControls(true);
        
        if(subnet.getClassName().equals(Constants.CLASS_SUBNET_IPV4))
            intBitMask.setMax(32);
        else if(subnet.getClassName().equals(Constants.CLASS_SUBNET_IPV6))
            intBitMask.setMax(64);
        
        HorizontalLayout lytSubnetDefinition =  new HorizontalLayout(lblSubnet, lblBitMaskToSplit , intBitMask);
        lytSubnetDefinition.setPadding(false);
        lytSubnetDefinition.setMargin(false);
        lytSubnetDefinition.setWidthFull();
        lytSubnetDefinition.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        lytSubnetDefinition.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        wdwSplitSubnet.setContent(new VerticalLayout(lytSubnetDefinition, lytSubnetDetails));
        wdwSplitSubnet.getBtnConfirm().setEnabled(false);
        //Gui Subnet Details        
        intBitMask.addValueChangeListener(e -> {
            boolean validSubnet = validateSplit(subnet.getClassName(), subnetAddress, broadcast, maskBits, e.getValue());
            wdwSplitSubnet.getBtnConfirm().setEnabled(validSubnet);
        });
        
        //Buttons
        wdwSplitSubnet.getBtnConfirm().addClickListener(e -> {
            try {
                //the descripition is share with all the new splited subnets
                attributes.put(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue());
                
                ActionResponse executeResponse = splitSubnetAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_PARENT_ID, subnet.getId()),
                        new ModuleActionParameter<>(Constants.PROPERTY_CLASSNAME, subnet.getClassName()),
                        new ModuleActionParameter<>(SplitSubnetAction.PARAM_SUBNETS, splitedSubnets)
                ));

                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(ActionResponse.ActionType.ADD, "");
                actionResponse.put(SplitSubnetAction.PARAM_NEW_SUBNETS_IDS, executeResponse.get(SplitSubnetAction.PARAM_NEW_SUBNETS_IDS));
                
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.ipam.actions.add-subnet.notification"), 
                                SplitSubnetAction.class, actionResponse));

                wdwSplitSubnet.close();
                
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), SplitSubnetAction.class));
            }
        });
        
        return wdwSplitSubnet;
    }

    @Override
    public AbstractAction getModuleAction() {
        return splitSubnetAction;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
    
    private boolean validateSplit(String subnetVersion, String subnetAddress, String broadcastIpAddress, int subnetMaskbits, int bitsToSplit){
        boolean isValid = subnetMaskbits < bitsToSplit;
        intBitMask.setInvalid(!isValid);
        
        if(isValid){
            try {
                if(subnetVersion.equals(Constants.CLASS_SUBNET_IPV4)){
                    splitedSubnets = IpamEngine.ipv4Split(subnetAddress, subnetMaskbits, broadcastIpAddress, bitsToSplit);
                    lytSubnetDetails.setVisible(true);
                
                    grdSubnets.setItems(splitedSubnets);
                }
                else if(subnetVersion.equals(Constants.CLASS_SUBNET_IPV6)){
                    splitedSubnets = IpamEngine.ipv6Split(subnetAddress, subnetMaskbits, broadcastIpAddress, bitsToSplit);
                    lytSubnetDetails.setVisible(true);
                
                    grdSubnets.setItems(splitedSubnets);
                }
            } catch (InvalidArgumentException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                    ex.getMessage(), SplitSubnetAction.class));
            }
        }
        return isValid;
    }
}
