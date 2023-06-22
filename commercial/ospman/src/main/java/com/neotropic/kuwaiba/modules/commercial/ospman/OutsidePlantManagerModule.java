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
package com.neotropic.kuwaiba.modules.commercial.ospman;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractCommercialModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Outside plant module definition.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class OutsidePlantManagerModule extends AbstractCommercialModule {
    /**
     * The module id.
     */
    private static final String MODULE_ID = "ospman"; //NOI18N
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private CoreActionsRegistry actionRegistry;
    /**
     * Reference to the module registry.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;

    @PostConstruct
    public void init() {
        // Register all actions provided by this module
        
        // Now register the module itself
        this.moduleRegistry.registerModule(this);
    }
    
    @Override
    public int getCategory() {
        return CATEGORY_PHYSICAL;
    }

    @Override
    public void validate() throws OperationNotPermittedException { }

    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public String getName() {
        return ts.getTranslatedString("module.ospman.name"); //NOI18N
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.ospman.description"); //NOI18N
    }

    @Override
    public String getVersion() {
        return "2.1";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>"; //NOI18N
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_OPEN_SOURCE;
    }
}
