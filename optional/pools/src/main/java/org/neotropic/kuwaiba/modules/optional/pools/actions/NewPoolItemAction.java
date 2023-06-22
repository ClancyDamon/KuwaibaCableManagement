/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.modules.optional.pools.actions;

import java.util.HashMap;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a new pool item
 *  @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class NewPoolItemAction extends AbstractAction {
     /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Parameter pool
     */
    public static String PARAMETER_POOL = "pool";
    
    @PostConstruct
    protected void init() {
        this.id = "pool.new-pool-item";
        this.displayName = ts.getTranslatedString("module.pools.actions.new-pool-item.name");
        this.description = ts.getTranslatedString("module.pools.actions.new-pool-item.description");
        this.order = 1000;
    
        setCallback((parameters) -> {
            String className = (String) parameters.get(Constants.PROPERTY_CLASSNAME);
            InventoryObjectPool pool = (InventoryObjectPool) parameters.get(PARAMETER_POOL);
            HashMap<String, String> attributes = (HashMap) parameters.get(Constants.PROPERTY_ATTRIBUTES);
            
            try {
                bem.createPoolItem(pool.getId(), className, attributes, null);
                return new ActionResponse();
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