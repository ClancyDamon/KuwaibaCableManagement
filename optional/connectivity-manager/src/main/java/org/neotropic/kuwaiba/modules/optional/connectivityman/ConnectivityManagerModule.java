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
package org.neotropic.kuwaiba.modules.optional.connectivityman;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.PopupAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.connectivityman.actions.ConnectivityManagerVisualAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

/**
 * Connectivity Manager module definition.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class ConnectivityManagerModule extends AbstractModule implements PopupAction {
    /**
     * Module id.
     */
    public static final String MODULE_ID = "connectivityman";
    
    @Autowired
    private ModuleRegistry moduleRegistry;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Connectivity Manager Visual Action.
     */
    @Autowired
    private ConnectivityManagerVisualAction connectivityManagerVisualAction;
    
    @PostConstruct
    public void init() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.addBasenames("i18n/messages");
        
        this.moduleRegistry.registerModule(this);
    }
    
    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public String getName() {
        return ts.getTranslatedString("module.connectivity-manager.name");
    }

    @Override
    public int getCategory() {
        return CATEGORY_PHYSICAL;
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.connectivity-manager.description");
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
        return ModuleType.TYPE_OTHER;
    }
    
    @Override
    public void open() {
        connectivityManagerVisualAction.getVisualComponent(null).open();
    }
}
