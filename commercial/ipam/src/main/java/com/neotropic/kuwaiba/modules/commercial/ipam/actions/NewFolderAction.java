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
 * Creates a new folder that will contains subnets op IP addresses
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class NewFolderAction extends AbstractAction {
    /**
     * Reference to the IP address module service
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
        this.id = "ipam.new-folder";
        this.displayName = ts.getTranslatedString("module.ipam.actions.add-folder.name");
        this.description = ts.getTranslatedString("module.ipam.actions.add-folder.description");
        this.icon = new Icon(VaadinIcon.PLUS);
        this.order = 1;
        
        setCallback(parameters -> {
            try {
                String parentId = (String) parameters.get(NewFolderVisualAction.PARAM_PARENT_FOLDER_ID);
                String className = (String) parameters.get(NewFolderVisualAction.PARAM_PARENT_FOLDER_CLASSNAME);
                HashMap<String, String> attributes = (HashMap<String, String>) parameters.get(Constants.PROPERTY_ATTRIBUTES);

                String newId = ipamService.createFolder(parentId, attributes.get(Constants.PROPERTY_NAME), attributes.get(Constants.PROPERTY_DESCRIPTION), className);
                
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
