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
package org.neotropic.kuwaiba.modules.core.favorites;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.PopupAction;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.favorites.actions.FavoritesVisualAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

/**
 * Favorites module definition.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class FavoritesModule extends AbstractModule implements PopupAction {
    /**
     * Module Id.
     */
    public static final String MODULE_ID = "favorites";
    /**
     * Version
     */
    public static String VERSION = "2.1";
    /**
     * Vendor
     */
    public static String VENDOR = "Neotropic SAS <contact@neotropic.co>";
    /**
     * Reference to the module registry.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Favorites Visual Action.
     */
    @Autowired
    private FavoritesVisualAction favoritesVisualAction;
    
    public FavoritesModule() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.addBasenames("i18n/messages");
    }
    
    @PostConstruct
    public void init() {
        this.moduleRegistry.registerModule(this);
    }

    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public String getName() {
        return ts.getTranslatedString("module.favorites.name");
    }

    @Override
    public int getCategory() {
        return CATEGORY_NAVIGATION;
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.favorites.description");
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getVendor() {
        return VENDOR;
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_OPEN_SOURCE;
    }

    @Override
    public void open() {
        favoritesVisualAction.getVisualComponent(null).open();
    }
}