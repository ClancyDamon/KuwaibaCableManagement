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

import com.neotropic.kuwaiba.modules.commercial.mpls.MplsService;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.connectivityman.ConnectivityManagerService;
import org.neotropic.kuwaiba.modules.optional.connectivityman.NewLogicalCircuitModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Action to create a new logical circuit.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class NewLogicalCircuitVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the MPLS Service.
     */
    @Autowired
    private MplsService mplsService;
    /**
     * Reference to the Outside Plant Service.
     */
    @Autowired
    private ConnectivityManagerService connectivityManagerService;
    /**
     * Reference to the new logical circuit action.
     */
    @Autowired
    private NewLogicalCircuitAction newLogicalCircuitAction;
    
    public NewLogicalCircuitVisualAction() {
        super(NewLogicalCircuitModule.MODULE_ID);
    }
    
    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }

    @Override
    public WindowNewLogicalCircuit getVisualComponent(ModuleActionParameterSet parameters) {
        return new WindowNewLogicalCircuit(aem, bem, mem, ts, mplsService, connectivityManagerService);
    }

    @Override
    public AbstractAction getModuleAction() {
        return newLogicalCircuitAction;
    }
    
}
