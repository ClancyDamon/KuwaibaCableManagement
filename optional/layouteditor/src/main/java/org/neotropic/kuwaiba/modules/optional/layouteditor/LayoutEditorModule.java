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
package org.neotropic.kuwaiba.modules.optional.layouteditor;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * This class implements the functionality corresponding to the Layout Editor module
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
@Component
public class LayoutEditorModule extends AbstractModule {
    /**
     * Module id.
     */
    public static String MODULE_ID = "layouteditor";
    /**
     * Translation service.
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
              
    
     @Override
    public String getName() {
        return ts.getTranslatedString("module.layout-editor.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.layout-editor.description");
    }
    
    @Override
    public String getVersion() {
        return "2.1";
    }
    
    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>"; //NOI18N
    }
    
    @PostConstruct
    public void init() {       
        // Now register the module itself
        this.moduleRegistry.registerModule(this);
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_PERPETUAL_LICENSE;
    }

    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public int getCategory() {
        return CATEGORY_OTHER;
    }
}