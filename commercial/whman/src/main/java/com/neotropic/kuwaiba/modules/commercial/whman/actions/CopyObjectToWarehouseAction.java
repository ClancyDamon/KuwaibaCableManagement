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
package com.neotropic.kuwaiba.modules.commercial.whman.actions;

import com.neotropic.kuwaiba.modules.commercial.whman.persistence.WarehousesService;
import com.vaadin.flow.component.UI;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Copies an object in a spare pool.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class CopyObjectToWarehouseAction extends AbstractAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Warehouses Services
     */
    @Autowired
    private WarehousesService ws;
    /**
     * Parameter business object.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Parameter pool.
     */
    public static String PARAM_POOL = "pool"; //NOI18N
    
    @PostConstruct
    protected void init() {
        this.id = "whman.copy-object-to-warehouse";
        this.displayName = ts.getTranslatedString("module.whman.actions.copy-object-to-warehouse.name");
        this.description = ts.getTranslatedString("module.whman.actions.copy-object-to-warehouse.decription");
        this.order = 1000;
        
        setCallback(parameters -> {
            try {
                Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                BusinessObjectLight object = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
                InventoryObjectPool pool = (InventoryObjectPool) parameters.get(PARAM_POOL);
                
                ws.copyObjectToWarehouse(pool.getId(), object.getClassName(), object.getId(), true, session.getUser().getUserName());
                return new ActionResponse();
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
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