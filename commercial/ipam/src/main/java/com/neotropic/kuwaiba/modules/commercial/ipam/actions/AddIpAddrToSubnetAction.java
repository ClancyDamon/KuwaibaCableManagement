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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
 * Adds a new IP address to a subnet
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class AddIpAddrToSubnetAction extends AbstractAction {
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
        this.id = "ipam.add-ip-addr-in-subnet";
        this.displayName = ts.getTranslatedString("module.ipam.actions.add-ip-addr.name");
        this.description = ts.getTranslatedString("module.ipam.actions.add-ip-addr.description");
        this.icon = new Icon(VaadinIcon.PLUS);
        this.order = 1;
        
        setCallback(parameters -> {
            try {
                String parentClassName = (String) parameters.get(Constants.PROPERTY_PARENT_CLASS_NAME);
                String parentOid = (String) parameters.get(Constants.PROPERTY_PARENT_ID);
                List<HashMap<String, String>> ipAddressAttributes = (List) parameters.get(Constants.PROPERTY_ATTRIBUTES);
                String newId = ipamService.addIPAddressesToSubnet(parentOid, parentClassName, ipAddressAttributes);
                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(Constants.PROPERTY_ID, newId);
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
