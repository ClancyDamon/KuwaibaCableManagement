/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.whman.actions;

import com.neotropic.kuwaiba.modules.commercial.whman.persistence.WarehousesService;
import com.vaadin.flow.component.UI;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a new Object in Spare Pool.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewSparePartAction extends AbstractAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts; 
    /**
     * Reference to the warehouses service
     */
    @Autowired
    private WarehousesService ws;
    
    @PostConstruct
    protected void init() {
        this.id = "whman.new-object";
        this.displayName = ts.getTranslatedString("module.whman.actions.spare.new-spare-part.name");
        this.description = ts.getTranslatedString("module.whman.actions.spare.new-spare-part.description");
        this.order = 1000;
        
        setCallback((parameters) -> {
            Session session = UI.getCurrent().getSession().getAttribute(Session.class);
            InventoryObjectPool pool = (InventoryObjectPool) parameters.get("sparePool");
            ClassMetadataLight className = (ClassMetadataLight) parameters.get("className");
            String name = (String) parameters.get(Constants.PROPERTY_NAME);
            String desc = (String) parameters.get(Constants.PROPERTY_DESCRIPTION);
            HashMap<String, String> attributes = new HashMap();
            attributes.put(Constants.PROPERTY_NAME, name);
            attributes.put(Constants.PROPERTY_DESCRIPTION, desc); 
            try {
                ws.createSparePart(pool.getId(), className.getName(), attributes, "", session.getUser().getUserName());
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                throw new ModuleActionException(ex.getMessage());
            }
            return new ActionResponse();     
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