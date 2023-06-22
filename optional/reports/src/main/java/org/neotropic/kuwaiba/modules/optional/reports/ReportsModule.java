/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.optional.reports;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.reports.actions.LaunchClassLevelReportAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * The definition of the Report module
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class ReportsModule  extends AbstractModule {
    /**
     * Module id.
     */
    public static String MODULE_ID = "reports";
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
    /**
     * Launches class level reports given a selected object.
     */
    @Autowired
    private LaunchClassLevelReportAction launchClassLevelReportAction;

    @PostConstruct
    public void init() {
        // Register all actions provided by this module
        actionRegistry.registerAction(getId(), launchClassLevelReportAction);
        // Now register the module itself
        this.moduleRegistry.registerModule(this);
    }
    
     /**
    * translation service
    */
    @Autowired
    private TranslationService ts;
    
    @Override
    public String getName() {
        return ts.getTranslatedString("module.reporting.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.reporting.description");
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
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_OPEN_SOURCE;        
    }

    @Override
    public int getCategory() {
        return CATEGORY_OTHER;
    }
}
