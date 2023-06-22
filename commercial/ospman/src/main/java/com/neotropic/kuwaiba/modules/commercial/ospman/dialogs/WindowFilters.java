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

import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapNode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Widows to set the Outside Plant View filters.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowFilters extends ConfirmDialog {
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final HashMap<BusinessObjectViewNode, MapNode> nodes;
    private final HashMap<BusinessObjectViewEdge, MapEdge> edges;
    
    public WindowFilters(
        HashMap<BusinessObjectViewNode, MapNode> nodes, 
        HashMap<BusinessObjectViewEdge, MapEdge> edges, 
        MetadataEntityManager mem, TranslationService ts) {
        
        Objects.requireNonNull(nodes);
        Objects.requireNonNull(edges);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        this.nodes = nodes;
        this.edges = edges;
        this.mem = mem;
        this.ts = ts;
        
        setModal(false);
        setDraggable(true);
        setContentSizeFull();
        setWidth("25%");
        setHeight("80%");
        getElement().getThemeList().add("wdw-osp-tool"); //NOI18N
    }
    
    @Override
    public void open() {
        List<ClassMetadata> classes = new ArrayList();
        HashMap<ClassMetadata, List<MapNode>> mapNodeClasses = new HashMap();
        HashMap<ClassMetadata, List<MapEdge>> mapEdgeClasses = new HashMap();
        
        nodes.forEach((viewNode, mapNode) -> {
            try {
                ClassMetadata mapNodeClass = mem.getClass(viewNode.getIdentifier().getClassName());
                if (!classes.contains(mapNodeClass))
                    classes.add(mapNodeClass);
                
                if (!mapNodeClasses.containsKey(mapNodeClass))
                    mapNodeClasses.put(mapNodeClass, new ArrayList());
                
                mapNodeClasses.get(mapNodeClass).add(mapNode);
                
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        });
        edges.forEach((viewEdge, mapEdge) -> {
            try {
                ClassMetadata mapEdgeClass = mem.getClass(viewEdge.getIdentifier().getClassName());
                if (!classes.contains(mapEdgeClass))
                    classes.add(mapEdgeClass);
                
                if (!mapEdgeClasses.containsKey(mapEdgeClass))
                    mapEdgeClasses.put(mapEdgeClass, new ArrayList());

                mapEdgeClasses.get(mapEdgeClass).add(mapEdge);
                
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        });
        if (classes.isEmpty())
            return;
        List<ClassMetadata> selectedClasses = new ArrayList();
        List<ClassMetadata> filteredClasses = new ArrayList();
        List<ClassMetadata> filteredSelectedClasses = new ArrayList();
        
        Collections.sort(classes, Comparator.comparing(ClassMetadata::toString));
        selectedClasses.addAll(classes);
        filteredClasses.addAll(classes);
        filteredSelectedClasses.addAll(classes);
        
        TextField txtSearchClass = new TextField();
        txtSearchClass.setWidthFull();
        txtSearchClass.setLabel(ts.getTranslatedString("module.ospman.filters.classes.label"));
        txtSearchClass.setPlaceholder(ts.getTranslatedString("module.ospman.filters.classes.search-class"));
        txtSearchClass.setClearButtonVisible(true);
        txtSearchClass.setSuffixComponent(VaadinIcon.SEARCH.create());
        txtSearchClass.setValueChangeMode(ValueChangeMode.EAGER);
        
        String strSelectAll = ts.getTranslatedString("module.ospman.filters.classes.select-all");
        
        MultiSelectListBox<String> lstSelectAll = new MultiSelectListBox();
        lstSelectAll.setWidthFull();
        lstSelectAll.setItems(strSelectAll);
        lstSelectAll.select(strSelectAll);
        
        MultiSelectListBox<ClassMetadata> lstClasses = new MultiSelectListBox();
        lstClasses.setSizeFull();
        lstClasses.setItems(classes);
        lstClasses.select(selectedClasses);
        lstClasses.setRenderer(new ComponentRenderer<>(item -> {
            return new Label(String.format("%s (%s)", item.toString(), 
                (mapNodeClasses.containsKey(item) ? mapNodeClasses.get(item).size() : 0) + 
                (mapEdgeClasses.containsKey(item) ? mapEdgeClasses.get(item).size() : 0)
            ));
        }));
        Command cmdLstSelectAll = () -> {
            boolean allSelected = true;
            for (ClassMetadata classItem : filteredClasses) {
                if (!filteredSelectedClasses.contains(classItem)) {
                    allSelected = false;
                    break;
                }
            }
            if (!filteredSelectedClasses.isEmpty() && allSelected)
                lstSelectAll.select(strSelectAll);
            else
                lstSelectAll.deselectAll();
        };
        txtSearchClass.addValueChangeListener(valueChangeEvent -> {
            String value = valueChangeEvent.getValue();
            filteredClasses.clear();
            filteredSelectedClasses.clear();
            if (value != null && !value.isEmpty()) {
                filteredClasses.addAll(classes.stream()
                    .filter(item -> item.toString().toLowerCase().contains(value.toLowerCase()))
                    .collect(Collectors.toList())
                );
                filteredSelectedClasses.addAll(selectedClasses.stream()
                    .filter(item -> item.toString().toLowerCase().contains(value.toLowerCase()))
                    .collect(Collectors.toList())
                );
            } else {
                filteredClasses.addAll(classes);
                filteredSelectedClasses.addAll(selectedClasses);
            }
            if (filteredClasses.isEmpty())
                lstSelectAll.setVisible(false);
            else
                lstSelectAll.setVisible(true);
            cmdLstSelectAll.execute();
            lstClasses.setItems(filteredClasses);
            lstClasses.select(filteredSelectedClasses);
        });
        lstSelectAll.addValueChangeListener(valueChangeEvent -> {
            if (valueChangeEvent.isFromClient()) {
                Set<String> values = valueChangeEvent.getValue();
                if (!values.isEmpty()) {
                    selectedClasses.addAll(filteredClasses);
                    filteredSelectedClasses.addAll(filteredClasses);
                    lstClasses.select(filteredClasses);
                } else {
                    selectedClasses.removeAll(filteredSelectedClasses);
                    filteredSelectedClasses.clear();
                    lstClasses.deselectAll();
                }
                filterClasses(selectedClasses, mapNodeClasses, mapEdgeClasses);
            }
        });
        lstClasses.addSelectionListener(event -> {
            if (event.isFromClient()) {
                event.getAddedSelection().forEach(item -> {
                    selectedClasses.add(item);
                    filteredSelectedClasses.add(item);
                });
                event.getRemovedSelection().forEach(item -> {
                    selectedClasses.remove(item);
                    filteredSelectedClasses.remove(item);
                });
                cmdLstSelectAll.execute();
                filterClasses(selectedClasses, mapNodeClasses, mapEdgeClasses);
            }
        });
        Scroller scroller = new Scroller();
        scroller.setContent(lstClasses);
        scroller.setSizeFull();
        
        FlexLayout lytContent = new FlexLayout();
        lytContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        lytContent.setSizeFull();
        
        lytContent.add(txtSearchClass, lstSelectAll, scroller);
        
        Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), clickEvent -> close());
        btnClose.setSizeFull();        
        
        setHeader(ts.getTranslatedString("module.ospman.filters.title"));
        setContent(lytContent);
        setFooter(btnClose);
        super.open();
    }

    @Override
    public void close() {
        nodes.values().forEach(node -> node.setNodeVisible(true));
        edges.values().forEach(edge -> edge.setEdgeVisible(true));
        super.close();
    }
    
    private void filterClasses(List<ClassMetadata> selectedClasses, 
        HashMap<ClassMetadata, List<MapNode>> mapNodeClasses, 
        HashMap<ClassMetadata, List<MapEdge>> mapEdgeClasses) {
        
        mapNodeClasses.forEach((mapNodeClass, mapNodes) -> {
            boolean visible = selectedClasses.contains(mapNodeClass);
            mapNodes.forEach(mapNode -> mapNode.setNodeVisible(visible));
        });
        mapEdgeClasses.forEach((mapEdgeClass, mapEdges) -> {
            boolean visible = selectedClasses.contains(mapEdgeClass);
            mapEdges.forEach(mapEdge -> mapEdge.setEdgeVisible(visible));
        });
    }
}
