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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.List;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueriesPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQuery;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Shows the set of geographical queries.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowGeographicalQueries extends ConfirmDialog {
    private final String POOL_NAME = "ospman.geo"; //NOI18N
    
    private final UnitOfLength unitOfLength;
    private final AbstractViewNode<BusinessObjectLight> selectedNode;
    private final List<AbstractViewNode> viewNodes;
    private final CoreActionsRegistry coreActionsRegistry;
    private final AdvancedActionsRegistry advancedActionsRegistry;
    private final ViewWidgetRegistry viewWidgetRegistry;
    private final ExplorerWidgetRegistry explorerWidgetRegistry;
    private final BusinessEntityManager bem;
    private final ApplicationEntityManager aem;
    private final MetadataEntityManager mem;
    private final PhysicalConnectionsService physicalConnectionsService;
    private final TranslationService ts;
    private final Consumer<BusinessObjectLight> consumerLocateNode;
    private final Double lat;
    private final Double lng;
    private final MapProvider mapProvider;
    
    public WindowGeographicalQueries(UnitOfLength unitOfLength, 
        Double lat, Double lng, List<AbstractViewNode> viewNodes,
        CoreActionsRegistry coreActionsRegistry,
            AdvancedActionsRegistry advancedActionsRegistry1,
        ViewWidgetRegistry viewWidgetRegistry,
        ExplorerWidgetRegistry explorerWidgetRegistry,
        ApplicationEntityManager aem,
        BusinessEntityManager bem,
        MetadataEntityManager mem,
        PhysicalConnectionsService physicalConnectionsService,
        TranslationService ts,
        Consumer<BusinessObjectLight> consumerLocateNode, 
        MapProvider mapProvider) {
        
        this(unitOfLength, null, 
            lat, lng, viewNodes, 
            coreActionsRegistry, 
            advancedActionsRegistry1, 
            viewWidgetRegistry, 
            explorerWidgetRegistry, 
            aem, bem, mem, physicalConnectionsService, ts, 
            consumerLocateNode, mapProvider);
    }
    
    public WindowGeographicalQueries(UnitOfLength unitOfLength, AbstractViewNode<BusinessObjectLight> selectedNode, 
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
        this.selectedNode = selectedNode;
        this.viewNodes = viewNodes;
        this.coreActionsRegistry = coreActionsRegistry;
        this.advancedActionsRegistry = advancedActionsRegistry;
        this.viewWidgetRegistry = viewWidgetRegistry;
        this.explorerWidgetRegistry = explorerWidgetRegistry;
        this.bem = bem;
        this.aem = aem;
        this.mem = mem;
        this.physicalConnectionsService = physicalConnectionsService;
        this.ts = ts;
        this.consumerLocateNode = consumerLocateNode;
        this.lng = lng;
        this.lat = lat;
        this.mapProvider = mapProvider;
    }
    @Override
    public void open() {
        setContentSizeFull();
        setCloseOnOutsideClick(false);
        setDraggable(true);
        try {
            ScriptedQueriesPool scriptedQueriesPool = aem.getScriptedQueriesPoolByName(POOL_NAME);
            if (scriptedQueriesPool != null) {
                int scriptedQueryCount = aem.getScriptedQueryCountByPoolName(POOL_NAME, "");
                if (scriptedQueryCount > 0) {
                    List<ScriptedQuery> scriptedQueries = aem.getScriptedQueriesByPoolName(POOL_NAME, "", true, 0, scriptedQueryCount);
                    ListBox<ScriptedQuery> lstScriptedQueries = new ListBox();
                    lstScriptedQueries.setItems(scriptedQueries);
                    lstScriptedQueries.setRenderer(new ComponentRenderer<>(
                        scriptedQuery -> new Label(scriptedQuery.getName())
                    ));
                    lstScriptedQueries.addValueChangeListener(valueChangeEvent -> {
                        ScriptedQuery scriptedQuery = valueChangeEvent.getValue();
                        if (scriptedQuery != null) {
                            WindowGeographicalQuery wdwGeographicalQuery = new WindowGeographicalQuery(
                                scriptedQuery, lat, lng, viewNodes, unitOfLength, aem, bem, mem, ts, physicalConnectionsService, consumerLocateNode,
                                coreActionsRegistry, advancedActionsRegistry, viewWidgetRegistry, explorerWidgetRegistry, mapProvider
                            );
                            wdwGeographicalQuery.setSelectedNode(selectedNode);
                            wdwGeographicalQuery.open();
                            close();
                        }
                    });
                    Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), clickEvent -> close());
                    btnClose.setSizeFull();
                    
                    setHeader(ts.getTranslatedString("module.ospman.geographical-queries"));
                    setContent(lstScriptedQueries);
                    setFooter(btnClose);
                    super.open();
                } else {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.information"), 
                        ts.getTranslatedString("module.ospman.no-geographical-queries"), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
                
            } else {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    String.format(ts.getTranslatedString("module.ospman.geographical-queries.error.pool-name"), POOL_NAME), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
    }
}
