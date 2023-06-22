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

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Move a business object from a pool to another pool
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class MoveBusinessObjectToPoolAction extends AbstractAction {
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
     * Parameter business object.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Parameter pool.
     */
    public static String PARAM_POOL = "pool"; //NOI18N
    
    @PostConstruct
    protected void init() {
        this.id = "pools.move-business-object-to-pool";
        this.displayName = ts.getTranslatedString("module.pools.actions.move-business-object-to-pool.name");
        this.description = ts.getTranslatedString("module.pools.actions.move-business-object-to-pool.description");
        this.order = 1000;
        
        setCallback(parameters -> {
            try {
                BusinessObjectLight object = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
                InventoryObjectPool pool = (InventoryObjectPool) parameters.get(PARAM_POOL);
                
                bem.movePoolItem(pool.getId(), object.getClassName(), object.getId());
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