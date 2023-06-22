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
package org.neotropic.kuwaiba.modules.optional.physcon;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.DistributionFramePortSummaryVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ConnectDistributionFrameVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ConnectLocationVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ConnectPortVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ConnectSplicingDeviceVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ManagePortMirroringVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.EditConnectionsVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.MirroringAntennaVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.MirroringDistributionFrameVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.MirroringSplicingDeviceVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.SplicingDevicePortSummaryVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.PortSummaryVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.widgets.FiberSplitterViewWidget;
import org.neotropic.kuwaiba.modules.optional.physcon.widgets.ObjectViewWidget;
import org.neotropic.kuwaiba.modules.optional.physcon.widgets.PhysicalPathViewWidget;
import org.neotropic.kuwaiba.modules.optional.physcon.widgets.PhysicalTreeViewWidget;
import org.neotropic.kuwaiba.modules.optional.physcon.widgets.RackViewWidget;
import org.neotropic.kuwaiba.modules.optional.physcon.widgets.SpliceBoxViewWidget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides tools and services that allow the creation of physical connections (optical, electrical and power-related).
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class PhysicalConnectionsModule extends AbstractModule {
    /**
     * Module id.
     */
    public static final String MODULE_ID = "physcon";
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the cache of enabled modules.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;
    /**
     * Reference to the list of object-related views exposed by all modules.
     */
    @Autowired
    private ViewWidgetRegistry viewWidgetRegistry;
    /**
     * The index of all actions provided by this module that are not of general purpose.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * A widget that shows the direct children of an object and allows to create physical connections between them.
     */
    @Autowired
    private ObjectViewWidget objectViewWidget;
    /**
     * A widget that shows the distribution of elements inside a rack.
     */
    @Autowired
    private RackViewWidget rackViewWidget;
    /**
     * A widget that shows how a fiber splitter is wired.
     */
    @Autowired
    private FiberSplitterViewWidget fiberSplitterViewWidget;
    /**
     * A widget that shows how a splice box is wired.
     */
    @Autowired
    private SpliceBoxViewWidget spliceBoxViewWidget;
    /**
     * A linear physical trace from a port as long as there's physical connectivity.
     */
    @Autowired
    private PhysicalPathViewWidget physicalPathViewWidget;
    /**
     * A tree-like physical trace from a port as long as there's physical connectivity.
     */
    @Autowired
    private PhysicalTreeViewWidget physicalTreeViewWidget;
    /**
     * Widget that allows connect and disconnect connections endpoints.
     */
    @Autowired
    private EditConnectionsVisualAction editConnectionEndPointsWidget;
    /**
     * Reference to action to manage port mirroring.
     */
    @Autowired
    private ManagePortMirroringVisualAction managePortMirroringVisualAction;
    /**
     * Reference to action to show port summary.
     */
    @Autowired
    private PortSummaryVisualAction portSummaryVisualAction;
    /**
     * Reference to action to show port summary in distribution frame devices.
     */
    @Autowired
    private DistributionFramePortSummaryVisualAction distributionFramePortSummaryVisualAction;
    /**
     * Reference to action to show port summary in splicing devices.
     */
    @Autowired
    private SplicingDevicePortSummaryVisualAction splicingDevicePortSummaryVisualAction;
    /**
     * Reference to connect port visual action.
     */
    @Autowired
    private ConnectPortVisualAction connectPortVisualAction;
    /**
     * Reference to connect location visual action.
     */
    @Autowired
    private ConnectLocationVisualAction connectLocationVisualAction;
    /**
     * Reference to connect distribution frame visual action.
     */
    @Autowired
    private ConnectDistributionFrameVisualAction connectDistributionFrameVisualAction;
    /**
     * Reference to connect splicing device visual action.
     */
    @Autowired
    private ConnectSplicingDeviceVisualAction connectSplicingDeviceVisualAction;
    /**
     * Reference to mirroring distribution frames visual action.
     */
    @Autowired
    private MirroringDistributionFrameVisualAction mirroringDistributionFrameVisualAction;
    /**
     * Reference to mirroring splicing device visual action.
     */
    @Autowired
    private MirroringSplicingDeviceVisualAction mirroringSplicingDeviceVisualAction;
    /**
     * Reference to mirroring antennas visual action.
     */
    @Autowired
    private MirroringAntennaVisualAction mirroringAntennaVisualAction;
    
    @PostConstruct
    public void init() {
        // Register global actions
        this.advancedActionsRegistry.registerAction(this.getId(), managePortMirroringVisualAction);
        // Register the object related views
        this.viewWidgetRegistry.registerWidget(objectViewWidget);
        this.viewWidgetRegistry.registerWidget(rackViewWidget);
        this.viewWidgetRegistry.registerWidget(fiberSplitterViewWidget);
        this.viewWidgetRegistry.registerWidget(spliceBoxViewWidget);
        this.viewWidgetRegistry.registerWidget(physicalPathViewWidget);
        this.viewWidgetRegistry.registerWidget(physicalTreeViewWidget);
        this.advancedActionsRegistry.registerAction(MODULE_ID, portSummaryVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, distributionFramePortSummaryVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, splicingDevicePortSummaryVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, editConnectionEndPointsWidget);
        this.advancedActionsRegistry.registerAction(MODULE_ID, connectPortVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, connectLocationVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, connectDistributionFrameVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, connectSplicingDeviceVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, mirroringDistributionFrameVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, mirroringSplicingDeviceVisualAction);
        this.advancedActionsRegistry.registerAction(MODULE_ID, mirroringAntennaVisualAction);
        // Register the module itself
        this.moduleRegistry.registerModule(this);
        setEnabled(false); // For now there isn't a frontend for this, so disable it.
    }
    
    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public String getName() {
        return ts.getTranslatedString("module.physcon.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.physcon.description");
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
        return ModuleType.TYPE_OPEN_SOURCE;
    }

    @Override
    public int getCategory() {
        return CATEGORY_PHYSICAL;
    }
}
