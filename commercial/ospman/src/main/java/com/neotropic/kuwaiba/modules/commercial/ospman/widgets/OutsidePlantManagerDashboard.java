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
package com.neotropic.kuwaiba.modules.commercial.ospman.widgets;

import com.neotropic.kuwaiba.modules.commercial.osp.external.services.OutsidePlantExternalServicesProvider;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.ElementConstants;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractDashboard;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ManagePortMirroringVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.EditConnectionsVisualAction;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * The visual entry point to the Outside Plant Module.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@CssImport(value = "./css/ospman.css")
@CssImport(value = "./css/custom-vcf-enhanced-dialog-overlay.css", themeFor="vcf-enhanced-dialog-overlay")
public class OutsidePlantManagerDashboard extends HorizontalLayout implements AbstractDashboard {
    /**
     * Reference to the translation service.
     */
    private final TranslationService ts;
    /**
     * Factory to build resources from data source.
     */
    private final ResourceFactory resourceFactory;
    /**
     * Reference to the Application Entity Manager.
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    private final MetadataEntityManager mem;
    /**
     * Reference to the Physical Connection Service.
     */
    private final PhysicalConnectionsService physicalConnectionService;
    /**
     * Reference to the new business object visual action.
     */
    private final NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    /**
     * Reference to the New Business Object From Template Visual Action.
     */
    private final NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction;
    /**
     * Reference to the New Multiple Business Objects Visual Action.
     */
    private final NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction;
    /**
     * Reference to action to manage port mirroring.
     */
    private final ManagePortMirroringVisualAction managePortMirroringVisualAction;
    /**
     * Reference to the core action registry.
     */
    private final CoreActionsRegistry coreActionsRegistry;
    /**
     * Reference to the module actions registry.
     */
    private final AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * Reference to the view widget registry.
     */
    private final ViewWidgetRegistry viewWidgetRegistry;
    /**
     * Reference to ht explorer widget registry.
     */
    private final ExplorerWidgetRegistry explorerWidgetRegistry;
    /**
     * Reference to the Outside Plant External Services Provider.
     */
    private final OutsidePlantExternalServicesProvider ospExternalServicesProvider;
    /**
     * Widget that allows connect and disconnect connections endpoints.
     */
    private final EditConnectionsVisualAction editConnectionEndPointsWidget;
    
    public OutsidePlantManagerDashboard(
        TranslationService ts, 
        ResourceFactory resourceFactory,
        ApplicationEntityManager aem, 
        BusinessEntityManager bem, 
        MetadataEntityManager mem,
        PhysicalConnectionsService physicalConnectionService, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction,
        NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction, 
        NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction,
        ManagePortMirroringVisualAction managePortMirroringVisualAction, 
        CoreActionsRegistry coreActionsRegistry, 
        AdvancedActionsRegistry advancedActionsRegistry, 
        ViewWidgetRegistry viewWidgetRegistry, 
        ExplorerWidgetRegistry explorerWidgetRegistry,
        OutsidePlantExternalServicesProvider ospExternalServicesProvider,
        EditConnectionsVisualAction editConnectionEndPointsWidget) {
        
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        this.physicalConnectionService = physicalConnectionService;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        this.newBusinessObjectFromTemplateVisualAction = newBusinessObjectFromTemplateVisualAction;
        this.newMultipleBusinessObjectsVisualAction = newMultipleBusinessObjectsVisualAction;
        this.managePortMirroringVisualAction = managePortMirroringVisualAction;
        this.coreActionsRegistry = coreActionsRegistry;
        this.advancedActionsRegistry = advancedActionsRegistry;
        this.viewWidgetRegistry = viewWidgetRegistry;
        this.explorerWidgetRegistry = explorerWidgetRegistry;
        this.ospExternalServicesProvider = ospExternalServicesProvider;
        this.editConnectionEndPointsWidget = editConnectionEndPointsWidget;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        setSpacing(false);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        init();
    }
    
    private void init() {
        removeAll();
        try {
            OutsidePlantAccordion lytAccordion = new OutsidePlantAccordion(aem, bem, mem, ts);
            lytAccordion.setWidth("25%");
            lytAccordion.addClassName("overflow-y-scroll");
            lytAccordion.setVisible(false);
            add(lytAccordion);
            
            OutsidePlantView ospView = new OutsidePlantView(this, aem, bem, mem, ts, resourceFactory, 
                physicalConnectionService, 
                newBusinessObjectVisualAction, 
                newBusinessObjectFromTemplateVisualAction, 
                newMultipleBusinessObjectsVisualAction, 
                managePortMirroringVisualAction, 
                coreActionsRegistry, 
                advancedActionsRegistry, 
                viewWidgetRegistry, 
                explorerWidgetRegistry, 
                lytAccordion,
                ospExternalServicesProvider,
                editConnectionEndPointsWidget
            );
            ospView.clean();
            
            Component ospViewComponent = ospView.getAsUiElement();
            if (ospViewComponent != null) {
                ospViewComponent.getElement().getStyle().set(ElementConstants.STYLE_WIDTH, "100%");
                add(ospViewComponent);                
            }
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
    }
}
