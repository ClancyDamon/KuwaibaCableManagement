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
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.PopupAction;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.connectivityman.actions.NewLogicalCircuitVisualAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates new logic circuits given the type and the endpoints
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class NewLogicalCircuitModule extends AbstractModule implements PopupAction {
    /**
     * Module id.
     */
    public static final String MODULE_ID = "newlogicalcircuit";
    /**
     * Reference to the Module Registry.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the New Logical Circuit Visual Action.
     */
    @Autowired
    private NewLogicalCircuitVisualAction newLogicalCircuitVisualAction;
    
    @PostConstruct
    public void init() {
        moduleRegistry.registerModule(this);
    }

    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public String getName() {
        return ts.getTranslatedString("module.new-logical-circuit.name");
    }

    @Override
    public int getCategory() {
        return CATEGORY_LOGICAL;
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.new-logical-circuit.description");
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
        newLogicalCircuitVisualAction.getVisualComponent(null).open();
    }
    
}
