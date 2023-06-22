/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.softman;

import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.softman.actions.RelateToLicenseVisualAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * The definition of the Software Manager module.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class SoftwareManagerModule extends AbstractModule {
    /**
     * Module id.
     */
    public static final String MODULE_ID = "softman";
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the module registry.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;
    /**
     * The registry with actions specific to this module.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * The action that serves to relate inventory objects to existing software or hardware licenses.
     */
    @Autowired
    private RelateToLicenseVisualAction relateToLicenseVisualAction;

    @PostConstruct
    public void init() {
        this.advancedActionsRegistry.registerAction(MODULE_ID, relateToLicenseVisualAction);
        // Now the module itself
        this.moduleRegistry.registerModule(this);
    }
    
    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public String getName() {
        return ts.getTranslatedString("module.softman.name");
    }

    @Override
    public int getCategory() {
        return CATEGORY_LOGICAL;
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.softman.description");
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
        return AbstractModule.ModuleType.TYPE_OPEN_SOURCE;
    }   
}