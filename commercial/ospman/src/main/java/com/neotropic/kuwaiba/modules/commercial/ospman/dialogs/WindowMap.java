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

import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.UnitOfLength;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.internal.Pair;
import java.util.List;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.dialog.ConfirmDialog;

/**
 * Window Map Tool Set
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowMap extends ConfirmDialog {
    private final UnitOfLength unitOfLength;
    private final Double lat;
    private final Double lng;
    private final List<AbstractViewNode> viewNodes;
    private final CoreActionsRegistry coreActionsRegistry;
    private final AdvancedActionsRegistry advancedActionsRegistry;
    private final ViewWidgetRegistry viewWidgetRegistry;
    private final ExplorerWidgetRegistry explorerWidgetRegistry;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final PhysicalConnectionsService physicalConnectionsService;
    private final TranslationService ts;
    private final Consumer<BusinessObjectLight> consumerLocateNode;
    private final MapProvider mapProvider;
    
    public WindowMap(UnitOfLength unitOfLength, 
        Double lat, Double lng, List<AbstractViewNode> viewNodes,
        CoreActionsRegistry coreActionsRegistry,
        AdvancedActionsRegistry advancedActionsRegistry,
        ViewWidgetRegistry viewWidgetRegistry,
        ExplorerWidgetRegistry explorerWidgetRegistry,
        ApplicationEntityManager aem,
        BusinessEntityManager bem,
        MetadataEntityManager mem,
        PhysicalConnectionsService physicalConnectionsService,
        TranslationService ts,
        Consumer<BusinessObjectLight> consumerLocateNode, 
        MapProvider mapProvider) {
        
        this.unitOfLength = unitOfLength;
        this.lat = lat;
        this.lng = lng;
        this.viewNodes = viewNodes;
        this.coreActionsRegistry = coreActionsRegistry;
        this.advancedActionsRegistry = advancedActionsRegistry;
        this.viewWidgetRegistry = viewWidgetRegistry;
        this.explorerWidgetRegistry = explorerWidgetRegistry;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.physicalConnectionsService = physicalConnectionsService;
        this.ts = ts;
        this.consumerLocateNode = consumerLocateNode;
        this.mapProvider = mapProvider;
    }

    @Override
    public void open() {
        Pair<Integer, String> toolGeographicalQueries = new Pair(0, ts.getTranslatedString("module.ospman.geographical-queries"));
        
        ListBox<Pair<Integer, String>> lstTools = new ListBox();
        lstTools.setItems(toolGeographicalQueries);
        lstTools.setRenderer(new ComponentRenderer<>(tool -> {
            switch (tool.getFirst()) {
                case 0:
                    return new HorizontalLayout(VaadinIcon.GLOBE.create(), new Label(tool.getSecond()));
            }
            return new Div();
        }));
        lstTools.addValueChangeListener(valueChangeEvent -> {
            Pair<Integer, String> value = valueChangeEvent.getValue();
            if (value != null) {
                close();
                switch (value.getFirst()) {
                    case 0:
                        new WindowGeographicalQueries(
                            unitOfLength, lat, lng, viewNodes, 
                            coreActionsRegistry, advancedActionsRegistry, 
                            viewWidgetRegistry, explorerWidgetRegistry, 
                            aem, bem, mem, physicalConnectionsService, ts, 
                            consumerLocateNode,
                            mapProvider
                        ).open();
                    break;
                }
            }
        });
        Button btnClose = new Button(
            ts.getTranslatedString("module.general.messages.close"), 
            event -> close()
        );
        btnClose.setWidthFull();
        
        setHeader(ts.getTranslatedString("module.ospman.map.tools"));
        setContent(lstTools);
        setFooter(btnClose);
        setDraggable(true);
        super.open();
    }
}
