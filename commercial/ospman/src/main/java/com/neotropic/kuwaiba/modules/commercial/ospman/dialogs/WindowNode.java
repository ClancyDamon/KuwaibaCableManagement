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
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.neotropic.kuwaiba.modules.commercial.osp.external.services.OutsidePlantExternalServicesProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.UnitOfLength;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.internal.Pair;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.integration.external.services.AbstractExternalService;
import org.neotropic.kuwaiba.core.apis.integration.external.services.AbstractInventoryExternalService;
import org.neotropic.kuwaiba.core.apis.integration.external.services.ExternalServiceProvider;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ManagePortMirroringVisualAction;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Dialog to the node tool set
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowNode extends ConfirmDialog {
    private final AbstractViewNode<BusinessObjectLight> node;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final OutsidePlantExternalServicesProvider ospExternalServicesProvider;
    
    public WindowNode(UnitOfLength unitOfLength, AbstractViewNode<BusinessObjectLight> node, 
        List<AbstractViewNode> viewNodes, 
        CoreActionsRegistry coreActionsRegistry,
        AdvancedActionsRegistry advancedActionsRegistry,
        ViewWidgetRegistry viewWidgetRegistry,
        ExplorerWidgetRegistry explorerWidgetRegistry,
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, 
        TranslationService ts, PhysicalConnectionsService physicalConnectionsService, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction, 
        NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction,
        NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction,
        ManagePortMirroringVisualAction managePortMirroringVisualAction, 
        Command cmdDeleteNode,
        Consumer<BusinessObjectLight> consumerLocateNode, 
        MapProvider mapProvider,
        OutsidePlantExternalServicesProvider ospExternalServicesProvider) {
        this.node = node;
        this.mem = mem;
        this.ts = ts;
        this.ospExternalServicesProvider = ospExternalServicesProvider;
        
        Pair<Integer, String> toolMidSpanAccess = new Pair(0, ts.getTranslatedString("module.ospman.mid-span-access.tool"));
        Pair<Integer, String> toolViewContent = new Pair(1, ts.getTranslatedString("module.ospman.view-node.tool.view-content"));
        Pair<Integer, String> toolRemove = new Pair(2, ts.getTranslatedString("module.ospman.view-node.tool.remove"));
        Pair<Integer, String> toolGeographicalQueries = new Pair(3, ts.getTranslatedString("module.ospman.geographical-queries"));
        Pair<Integer, String> toolExternalServices = new Pair(4, ts.getTranslatedString("module.ospman.external-services"));
        
        ListBox<Pair<Integer, String>> lstTools = new ListBox();
        
        List<Pair<Integer, String>> items = new ArrayList();
        items.add(toolMidSpanAccess);
        items.add(toolViewContent);
        items.add(toolGeographicalQueries);
        if (hasExternalServices())
            items.add(toolExternalServices);
        items.add(toolRemove);
        
        lstTools.setItems(items);
        
        lstTools.setRenderer(new ComponentRenderer<>(tool -> {
            if (tool != null) {
                switch (tool.getFirst()) {
                    case 0:
                        Image imgConnect = new Image("plug-plug.svg", "connect");
                        imgConnect.setWidth("var(--iron-icon-width)");
                        imgConnect.setHeight("var(--iron-icon-height)");
                        return new HorizontalLayout(imgConnect, new Label(tool.getSecond()));
                    case 1:
                        return new HorizontalLayout(VaadinIcon.EYE.create(), new Label(tool.getSecond()));
                    case 2:
                        return new HorizontalLayout(VaadinIcon.TRASH.create(), new Label(tool.getSecond()));
                    case 3:
                        return new HorizontalLayout(VaadinIcon.GLOBE.create(), new Label(tool.getSecond()));
                    case 4:
                        return new HorizontalLayout(VaadinIcon.COG.create(), new Label(tool.getSecond()));
                }
            }
            return new Div();
        }));
        lstTools.addValueChangeListener(valueChangeEvent -> {
            if (valueChangeEvent.getValue() != null) {
                close();
                switch(valueChangeEvent.getValue().getFirst()) {
                    case 0:
                        List<BusinessObjectLight> nodes = new ArrayList();
                        if (viewNodes != null)
                            viewNodes.forEach(viewNode -> nodes.add((BusinessObjectLight) viewNode.getIdentifier()));
                            
                        new WindowMidSpanAccess(node.getIdentifier(), nodes, aem, bem, mem, ts, 
                            newBusinessObjectVisualAction, 
                            newBusinessObjectFromTemplateVisualAction, 
                            newMultipleBusinessObjectsVisualAction, 
                            managePortMirroringVisualAction, 
                            physicalConnectionsService
                        ).open();
                    break;
                    case 1:
                        new WindowViewContent(node.getIdentifier(), aem, bem, mem, ts, physicalConnectionsService).open();
                    break;
                    case 2:
                        if (cmdDeleteNode != null)
                            cmdDeleteNode.execute();
                    break;
                    case 3:
                        Double lat = (Double) node.getProperties().get(OspConstants.ATTR_LAT);
                        Double lng = (Double) node.getProperties().get(OspConstants.ATTR_LON);
                        
                        new WindowGeographicalQueries(
                            unitOfLength, node, lat, lng, viewNodes, 
                            coreActionsRegistry, 
                            advancedActionsRegistry, 
                            viewWidgetRegistry, 
                            explorerWidgetRegistry, 
                            aem, bem, mem, physicalConnectionsService, ts, 
                            consumerLocateNode, mapProvider
                        ).open();
                    break;
                    case 4:
                        new WindowExternalServices(
                            node.getIdentifier(), 
                            ospExternalServicesProvider, 
                            mem, 
                            ts, 
                            mapProvider
                        ).open();
                    break;
                }
            }
        });
        Button btnClose = new Button(ts.getTranslatedString("module.general.messages.cancel"), event -> close());
        btnClose.setWidthFull();
        
        setHeader(String.format(ts.getTranslatedString("module.ospman.location.tools"), node.getIdentifier().getName()));
        setContent(lstTools);
        setFooter(btnClose);
        setDraggable(true);
    }
    
    private boolean hasExternalServices() {
        BusinessObjectLight businessObject = node.getIdentifier();
        if (businessObject != null) {
            for (ExternalServiceProvider externalServiceProvider : ospExternalServicesProvider.getExternalServiceProviders()) {
                for (AbstractExternalService externalService : externalServiceProvider.getExternalServices()) {
                    if (externalService instanceof AbstractInventoryExternalService) {
                        String appliesTo = ((AbstractInventoryExternalService) externalService).appliesTo();
                        if (appliesTo != null) {
                            try {
                                if (mem.isSubclassOf(appliesTo, businessObject.getClassName()))
                                    return true;
                            } catch (MetadataObjectNotFoundException ex) {
                                new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"), 
                                    ex.getLocalizedMessage(), 
                                    AbstractNotification.NotificationType.ERROR, 
                                    ts
                                ).open();
                            }
                        }
                        else
                            return true;
                    }
                }
            }
        }
        return false;
    }
}
