/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ipam.visual;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * A node used in the Tree Grid of the IPAM module
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class IpamNode extends AbstractNode<Object>{
    
    public static String ID_ACTION_BTN_MARK_AS_FAVORITE = "btn-mark_as_favorite";
    //folder actions list
    public static String ID_ACTION_ADD_FOLDER = "btn-add_folder";
    public static String ID_ACTION_DEL_FOLDER = "btn-delete_folder";
    public static String ID_ACTION_ADD_SUBNET_TO_FOLDER = "btn-add_subnet_to_folder";
    public static String ID_ACTION_DEL_SUBNET_FROM_FOLDER = "btn-delete_subnet_from_folder";
    public static String ID_ACTION_BTN_ADD_IP_TO_FOLDER = "btn-add_ip_to_folder";
    public static String ID_ACTION_BTN_DEL_IP_TO_SUBNET = "btn-del_individual_ip";
    //subnet actions list
    public static String ID_ACTION_BTN_ADD_SUBNET_IN_SUBNET = "btn-add_subnet_in_subnet";
    public static String ID_ACTION_BTN_DEL_SUBNET_FROM_SUBNET = "btn-delete_subnet_from_subnet";
    public static String ID_ACTION_BTN_SPLIT_SUBNET = "btn-split_subnet";
    public static String ID_ACTION_BTN_ADD_IP_TO_SUBNET = "btn_add_ip_in_subnet";
    /**
     * if the node is a pool or a Business ObjectLight
     */
    private final boolean pool;
    /**
     * layout that contains the action buttons
     */
    private HorizontalLayout lytActions;
    /**
     * the action buttons to show or hide
     */
    private List<ActionButton> actionButtons;
    
    public IpamNode(BusinessObjectLight subnet) {
        super(subnet);
        this.id = subnet.getId();
        this.name = subnet.getName();
        this.className = (subnet.getClassDisplayName() != null 
                && !subnet.getClassDisplayName().isEmpty()) ? subnet.getClassDisplayName() : subnet.getClassName();
        this.pool = false;
        this.selected = false;
    }

    public IpamNode(InventoryObjectPool folder) {
        super(folder);
        this.id = folder.getId();
        this.name = folder.getName();
        this.className = folder.getClassName();
        this.pool = true;
        this.selected = false;
    }

    public boolean isPool() {
        return pool;
    }

    public HorizontalLayout getActionsVisualComponent() {
        return lytActions;
    }

    /**
     * Creates a layout and add the action buttons created from the UI
     * @param visualActions a layout fulfill with buttons
     */
    public void addVisualActions(ActionButton... visualActions){
        if(lytActions == null){
            lytActions = new HorizontalLayout();
            lytActions.setHeight("22px");
            
            lytActions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            lytActions.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
            lytActions.setPadding(false);
            lytActions.setMargin(false);
            lytActions.setSpacing(false);
        }
        if(actionButtons == null)
            actionButtons = new ArrayList<>();
        
        boolean added = false;
        for (ActionButton actionButton : visualActions){
            for (ActionButton addedButton : actionButtons) {
                if(actionButton.getId().get().equals(addedButton.getId().get())){
                    added = true;
                    break;
                }
            }
            if(!added){
                actionButtons.add(actionButton);
                lytActions.add(visualActions);
            }
        }
    }
    
    public void enableSubnetsActions(boolean enable){
        if(actionButtons != null){
            for (ActionButton button : actionButtons) {
                if(button.getId().get().equals(ID_ACTION_BTN_ADD_SUBNET_IN_SUBNET)
                    || button.getId().get().equals(ID_ACTION_BTN_SPLIT_SUBNET))
                    button.setEnabled(enable);
            }
        }
    }
    
    public void enableIpAddressActions(boolean enable){
        if(actionButtons != null){
            for (ActionButton button : actionButtons) {
                if(button.getId().get().equals(ID_ACTION_BTN_ADD_IP_TO_SUBNET))
                    button.setEnabled(enable);
            }
        }
    }
    
    /**
     * Returns the node icon label
     * @return an icon and label(includes validators) of the node
     */
    public Component getNodeIconLabel() {
        HorizontalLayout lytNodeLabel = new HorizontalLayout();
        
        lytNodeLabel.setBoxSizing(BoxSizing.BORDER_BOX);  
        lytNodeLabel.setSpacing(true);
        lytNodeLabel.setMargin(false);
        lytNodeLabel.setPadding(false);
        lytNodeLabel.setSizeUndefined();
        lytNodeLabel.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
            
        Icon icon;
        if(pool)
            icon = expanded ? new Icon(VaadinIcon.FOLDER_OPEN) : new Icon(VaadinIcon.FOLDER);
        else {
            if(className.equals(Constants.CLASS_IP_ADDRESS)) 
                icon = new Icon(VaadinIcon.PASSWORD);
            else //is a subnet
                icon = new Icon(VaadinIcon.SITEMAP);
        }
        icon.setSize("16px");
        lytNodeLabel.add(icon);
        
        if(getObject() instanceof BusinessObjectLight)
            lytNodeLabel.add(new FormattedObjectDisplayNameSpan((BusinessObjectLight)getObject(),
                    false, false, false, false));
        else if(getObject() instanceof InventoryObjectPool) {
            Span spanLabel = new Span(((InventoryObjectPool)getObject()).getName());
            spanLabel.setClassName("wrap-item-label");
            lytNodeLabel.add(spanLabel);
        }
            
        return lytNodeLabel;
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public AbstractAction[] getActions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(boolean recursive) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}