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

import com.neotropic.kuwaiba.modules.commercial.osp.external.services.OutsidePlantExternalServicesProvider;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import com.neotropic.kuwaiba.modules.commercial.ospman.widgets.OutsidePlantManagerDashboard;
import com.vaadin.flow.router.HasDynamicTitle;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ManagePortMirroringVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.EditConnectionsVisualAction;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the outside plant manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Route(value = "ospman", layout = OutsidePlantManagerLayout.class)
public class OutsidePlantManagerUI extends VerticalLayout implements HasDynamicTitle, AbstractUI {
    /**
     * The main dashboard.
     */
    private OutsidePlantManagerDashboard dashboard;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Factory to build resources from data source.
     */
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
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
     * Reference to the New Business Object Action.
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
    private NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction;
    /**
     * Reference to the Physical Connection Service.
     */
    @Autowired
    private PhysicalConnectionsService physicalConnectionService;
    /**
     * Reference to action to manage port mirroring.
     */
    @Autowired
    private ManagePortMirroringVisualAction managePortMirroringVisualAction;
    /**
     * Reference to the core actions registry.
     */
    @Autowired
    private CoreActionsRegistry coreActionsRegistry;
    /**
     * Reference to the module actions registry.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * Reference to the view widget registry.
     */
    @Autowired
    private ViewWidgetRegistry viewWidgetRegistry;
    /**
     * Reference to ht explorer widget registry.
     */
    @Autowired
    private ExplorerWidgetRegistry explorerWidgetRegistry;
    /**
     * Reference to the Outside Plant External Services Provider.
     */
    @Autowired
    private OutsidePlantExternalServicesProvider ospExternalServicesProvider;
    /**
     * Widget that allows connect and disconnect connections endpoints.
     */
    @Autowired
    private EditConnectionsVisualAction editConnectionEndPointsWidget;
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.ospman.title");
    }

    @Override
    public void initContent() {
        setSizeFull();
        setMargin(false);
        setPadding(false);
        setSpacing(false);
        
        this.dashboard = new OutsidePlantManagerDashboard(ts, resourceFactory, 
            aem, bem, mem, physicalConnectionService, 
            newBusinessObjectVisualAction, 
            newBusinessObjectFromTemplateVisualAction, 
            newMultipleBusinessObjectsVisualAction, 
            managePortMirroringVisualAction, 
            coreActionsRegistry, 
            advancedActionsRegistry, 
            viewWidgetRegistry, 
            explorerWidgetRegistry,
            ospExternalServicesProvider,
            editConnectionEndPointsWidget
        );
        add(this.dashboard);
    }
}
