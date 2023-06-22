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

package org.neotropic.kuwaiba.modules.optional.serviceman;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.RelateObjectToServiceVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.DeleteCustomerVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.DeleteServiceVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewCustomerPoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewCustomerVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewServicePoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewServiceVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.ReleaseObjectFromServiceVisualAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * Manage customers, services and their relationships with network resources.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class ServiceManagerModule extends AbstractModule {
    /**
     * The module id.
     */
    public static final String MODULE_ID = "serviceman"; 
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the action that associates business objects to services.
     */
    @Autowired
    private RelateObjectToServiceVisualAction relateObjectToServiceVisualAction;
    /**
     * Reference to the action that releases business objects from services.
     */
    @Autowired
    private ReleaseObjectFromServiceVisualAction releaseObjectFromServiceVisualAction;
    /**
     * Reference to the action that creates customer pools.
     */
    @Autowired
    private NewCustomerPoolVisualAction newCustomerPoolVisualAction;
    /**
     * Reference to the action that creates customers.
     */
    @Autowired
    private NewCustomerVisualAction newCustomerVisualAction;
    /**
     * Reference to the action that creates service pools.
     */
    @Autowired
    private NewServicePoolVisualAction newServicePoolVisualAction;
    /**
     * Reference to the action that creates services.
     */
    @Autowired
    private NewServiceVisualAction newServiceVisualAction;
    /**
     * Reference to the action that deletes customers and its services.
     */
    @Autowired
    private DeleteCustomerVisualAction deleteCustomerVisualAction;
    /**
     * Reference to the action that deletes services and releases their network resources.
     */
    @Autowired
    private DeleteServiceVisualAction deleteServiceVisualAction;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * Reference to the module registry.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;

    @PostConstruct
    public void init() {
        // Register all actions provided by this module
        this.advancedActionsRegistry.registerAction(MODULE_ID, newCustomerPoolVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, newCustomerVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, newServicePoolVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, newServiceVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, deleteCustomerVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, deleteServiceVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, releaseObjectFromServiceVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, relateObjectToServiceVisualAction);
        // Now register the module itself
        this.moduleRegistry.registerModule(this);
    }
    
    @Override
    public String getId() {
        return MODULE_ID;
    }
    
    @Override
    public String getName() {
        return ts.getTranslatedString("module.serviceman.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.serviceman.description");
    }

    @Override
    public String getVersion() {
        return "2.1";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_OPEN_SOURCE;
    }

    @Override
    public int getCategory() {
        return CATEGORY_BUSINESS;
    }
}