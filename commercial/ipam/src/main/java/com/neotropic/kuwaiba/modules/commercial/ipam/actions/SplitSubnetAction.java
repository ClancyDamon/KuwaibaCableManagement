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

import com.neotropic.kuwaiba.modules.commercial.ipam.IpamService;
import com.neotropic.kuwaiba.modules.commercial.ipam.engine.SubnetDetail;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a new subnet
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class SplitSubnetAction extends AbstractAction {
    /**
     * Parameter to create the splited subnets, contains all the new subnets details
     */
    public static final String PARAM_SUBNETS = "subnets";
    /**
     * Parameter of the created the subnets its ids
     */
    public static final String PARAM_NEW_SUBNETS_IDS = "newSubnetsIds";
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private IpamService ipamService;
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    
    @PostConstruct
    protected void init() {
        this.id = "ipam.split-subnet";
        this.displayName = ts.getTranslatedString("module.ipam.actions.split-subnet.name");
        this.description = ts.getTranslatedString("module.ipam.actions.split-subnet.description");
        this.icon = new Icon(VaadinIcon.PLUS);
        this.order = 1;
        
        setCallback(parameters -> {
            try {
                String className = (String) parameters.get(Constants.PROPERTY_CLASSNAME);
                String parentOid = (String) parameters.get(Constants.PROPERTY_PARENT_ID);
                String desc = (String) parameters.get(Constants.PROPERTY_DESCRIPTION);
                
                List<HashMap<String, String>> subnetsAttributes =  new ArrayList<>();
                List<SubnetDetail> subnets = (List<SubnetDetail>) parameters.get(PARAM_SUBNETS);
                List<String> subnetsNames = new ArrayList<>();
                
                for (SubnetDetail subnet : subnets) {
                    HashMap<String, String> attributes = new HashMap<>();
                    
                    attributes.put(Constants.PROPERTY_NAME, subnet.getCidr());
                    attributes.put(Constants.PROPERTY_BROADCAST_IP, subnet.getBroadCastIpAddr());
                    attributes.put(Constants.PROPERTY_NETWORK_IP, subnet.getNetworkIpAddr());
                    attributes.put(Constants.PROPERTY_HOSTS, Integer.toString(subnet.getNumberOfHosts()));
                    attributes.put(Constants.PROPERTY_DESCRIPTION, desc);
                
                    subnetsAttributes.add(attributes);
                    subnetsNames.add(subnet.getCidr());
                }
                
                ipamService.existInInventory(subnetsNames, className);
                
                List<String> createSubnetsIds = ipamService.createSubnets(parentOid, className, subnetsAttributes);
                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(PARAM_NEW_SUBNETS_IDS, createSubnetsIds);
                
                return actionResponse;
                
            } catch (InventoryException ex) {
                throw new ModuleActionException(ex.getMessage());
            }
        });
    }
    
    @Override
    public int getRequiredAccessLevel() {
        return Privilege.ACCESS_LEVEL_READ_WRITE;
    }
    
    @Override
    public boolean requiresConfirmation() {
        return false;
    }
}
