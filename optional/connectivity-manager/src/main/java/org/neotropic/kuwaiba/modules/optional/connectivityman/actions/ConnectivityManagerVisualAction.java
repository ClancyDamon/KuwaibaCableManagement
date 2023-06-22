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
package org.neotropic.kuwaiba.modules.optional.connectivityman.actions;

import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.NavigationModule;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.TemplateManagerModule;
import org.neotropic.kuwaiba.modules.optional.connectivityman.ConnectivityManagerService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsModule;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Action to manage connections.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class ConnectivityManagerVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Reference to Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the module action to manage connections.
     */
    @Autowired
    private ConnectivityManagerAction manageConnectionsAction;
    /**
     * Reference to the New Business Object Visual Action.
     */
    @Autowired
    private NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    /**
     * Reference to the New Business Object From Template Visual Action.
     */
    @Autowired
    private NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction;
    /**
     * Reference to the New Multiple Business Objects Visual Action.
     */
    @Autowired
    private NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsViusalAction;
    /**
     * Reference Physical Connections Service
     */
    @Autowired
    private PhysicalConnectionsService physicalConnectionsService;
    /**
     * Reference to the Template Manager Module.
     */
    @Autowired
    private TemplateManagerModule templateManagerModule;
    /**
     * Reference to the Navigation Module.
     */
    @Autowired
    private NavigationModule navigationModule;
    /**
     * Reference to  the Outside Plant Service
     */
    @Autowired
    private ConnectivityManagerService connectivityManagerService;

    public ConnectivityManagerVisualAction() {
        super(PhysicalConnectionsModule.MODULE_ID);
    }
    
    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }

    @Override
    public WindowConnectivityManager getVisualComponent(ModuleActionParameterSet parameters) {
        return new WindowConnectivityManager(aem, bem, mem, ts, 
            newBusinessObjectVisualAction, 
            newBusinessObjectFromTemplateVisualAction, 
            newMultipleBusinessObjectsViusalAction, 
            physicalConnectionsService, 
            templateManagerModule, 
            navigationModule,
            connectivityManagerService
        );
    }

    @Override
    public AbstractAction getModuleAction() {
        return manageConnectionsAction;
    }
    
}
