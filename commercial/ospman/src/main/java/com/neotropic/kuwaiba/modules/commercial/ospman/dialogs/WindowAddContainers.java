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

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import static org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA;
import static org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to add containers in the Outside Plant View
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowAddContainers extends ConfirmDialog {
    private final List<AbstractViewNode> viewNodes;
    private final List<AbstractViewEdge> viewEdges;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final Consumer<WindowAddContainers> consumerAddContainer;
    
    private BusinessObjectLight source;
    private BusinessObjectLight target;
    private BusinessObjectLight container;
    private Command cmdSetItems;
    
    public WindowAddContainers(List<AbstractViewNode> viewNodes, List<AbstractViewEdge> viewEdges, 
        BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts, 
        Consumer<WindowAddContainers> consumerAddContainer) {
        
        Objects.requireNonNull(viewNodes);
        Objects.requireNonNull(viewEdges);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(consumerAddContainer);
        
        this.viewNodes = viewNodes;
        this.viewEdges = viewEdges;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.consumerAddContainer = consumerAddContainer;
        
        setModal(false);
        setDraggable(true);
        setContentSizeFull();
        setWidth("25%");
        setHeight("80%");
        getElement().getThemeList().add("wdw-osp-tool"); //NOI18N
    }
    
    public BusinessObjectLight getSource() {
        return source;
    }
    
    public BusinessObjectLight getTarget() {
        return target;
    }
    
    public BusinessObjectLight getContainer() {
        return container;
    }
    
    public void updateContainers() {
        if (cmdSetItems != null)
            cmdSetItems.execute();
    }
    
    @Override
    public void open() {
        ComboBox<BusinessObjectLight> cmbSource = new ComboBox();
        cmbSource.setWidthFull();
        cmbSource.setClearButtonVisible(true);
        cmbSource.setDataProvider(getObjectDataProvider(viewNodes));
        cmbSource.setPlaceholder(ts.getTranslatedString("module.ospman.tools.add-containers.select-container-source"));
        cmbSource.setLabel(ts.getTranslatedString("module.ospman.tools.add-containers.container-source"));
        cmbSource.setItemLabelGenerator(BusinessObjectLight::getName);
        cmbSource.setRenderer(getObjectRenderer());
        cmbSource.addValueChangeListener(valueChangeEvent -> source = valueChangeEvent.getValue());
        
        ComboBox<BusinessObjectLight> cmbTarget = new ComboBox();
        cmbTarget.setWidthFull();
        cmbTarget.setClearButtonVisible(true);
        cmbTarget.setDataProvider(getObjectDataProvider(viewNodes));
        cmbTarget.setPlaceholder(ts.getTranslatedString("module.ospman.tools.add-containers.select-container-target"));
        cmbTarget.setLabel(ts.getTranslatedString("module.ospman.tools.add-containers.container-target"));
        cmbTarget.setItemLabelGenerator(BusinessObjectLight::getName);
        cmbTarget.setRenderer(getObjectRenderer());
        cmbTarget.addValueChangeListener(valueChangeEvent -> {
            target = valueChangeEvent.getValue();
            if (source != null && source.equals(target))
                cmbTarget.clear();
        });
        Label lblMessage = new Label();
        ListBox<BusinessObjectLight> lstContainers = new ListBox();
        lstContainers.setSizeFull();
        lstContainers.setRenderer(getObjectRenderer());
        
        cmdSetItems = () -> {
            lstContainers.clear();
            lstContainers.setItems(Collections.EMPTY_LIST);
            lblMessage.setText(null);
            
            if (source != null && target != null && !source.equals(target)) {
                try {
                    HashMap<String, List<BusinessObjectLight>> mapSourceEndpoints = bem.getSpecialAttributes(source.getClassName(), source.getId(), RELATIONSHIP_ENDPOINTA, RELATIONSHIP_ENDPOINTB);
                    HashMap<String, List<BusinessObjectLight>> mapTargetEndpoints = bem.getSpecialAttributes(target.getClassName(), target.getId(), RELATIONSHIP_ENDPOINTA, RELATIONSHIP_ENDPOINTB);
                    List<BusinessObjectLight> sourceEndpoints = new ArrayList();
                    List<BusinessObjectLight> targetEndpoints = new ArrayList();
                    
                    mapSourceEndpoints.values().forEach(endpoints -> endpoints.forEach(endpoint -> sourceEndpoints.add(endpoint)));
                    mapTargetEndpoints.values().forEach(endpoints -> endpoints.forEach(endpoint -> targetEndpoints.add(endpoint)));
                    
                    List<BusinessObjectLight> allEndpoints = new ArrayList();
                    sourceEndpoints.forEach(sourceEndpoint -> targetEndpoints.forEach(targetEndpoint -> {
                        if (sourceEndpoint.equals(targetEndpoint))
                            allEndpoints.add(sourceEndpoint);
                    }));
                    List<BusinessObjectLight> selectedEndpoints = new ArrayList();
                    viewEdges.forEach(viewEdge -> allEndpoints.forEach(endpoint -> {
                        if (endpoint.equals((BusinessObjectLight) viewEdge.getIdentifier()))
                            selectedEndpoints.add(endpoint);
                    }));
                    List<BusinessObjectLight> endpoints = new ArrayList();
                    endpoints.addAll(allEndpoints);
                    endpoints.removeAll(selectedEndpoints);
                    if (!endpoints.isEmpty())
                        lblMessage.setText(ts.getTranslatedString("module.ospman.tools.add-containers.label.existing-containers"));
                    
                    if (allEndpoints.isEmpty())
                        lblMessage.setText(ts.getTranslatedString("module.ospman.tools.add-containers.label.no-containers"));
                    else if (endpoints.isEmpty())
                        lblMessage.setText(ts.getTranslatedString("module.ospman.tools.add-containers.label.no-available-containers"));
                    
                    Collections.sort(endpoints, Comparator.comparing(BusinessObjectLight::getName));
                    lstContainers.setItems(endpoints);
                    
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            }
        };
        lstContainers.addValueChangeListener(valueChangeEvent -> {
            container = valueChangeEvent.getValue();
            if (container != null) {
                if (source != null && target != null && !source.equals(target))
                    consumerAddContainer.accept(this);
                cmdSetItems.execute();
            }
        });
        ValueChangeListener<ComponentValueChangeEvent<ComboBox<BusinessObjectLight>, BusinessObjectLight>> listener = valueChangeEvent -> cmdSetItems.execute();
        cmbSource.addValueChangeListener(listener);
        cmbTarget.addValueChangeListener(listener);
        
        Scroller scroller = new Scroller();
        scroller.setSizeFull();
        scroller.setContent(lstContainers);
        
        FlexLayout lytContent = new FlexLayout(cmbSource, cmbTarget, lblMessage, scroller);
        lytContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        lytContent.setSizeFull();
        
        Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), clickEvent -> close());
        btnClose.setSizeFull();
        
        setHeader(ts.getTranslatedString("module.ospman.tools.add-containers.header"));
        setContent(lytContent);
        setFooter(btnClose);
        super.open();
    }
    
    private ComponentRenderer<FlexLayout, BusinessObjectLight> getObjectRenderer() {
        return new ComponentRenderer<>(object -> {
            Label lblName = new Label(object.getName());
            Label lblClass = new Label();
            try {
                lblClass.setText(mem.getClass(object.getClassName()).toString());
            } catch (MetadataObjectNotFoundException ex) {                
                lblClass.setText(object.getClassName());
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
            FlexLayout lytObject = new FlexLayout(lblName, lblClass);
            lytObject.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            lytObject.setSizeFull();
            return lytObject;
        });
    }
    
    private DataProvider<BusinessObjectLight, String> getObjectDataProvider(List<AbstractViewNode> viewNodes) {
        Objects.requireNonNull(viewNodes);
        
        return DataProvider.fromFilteringCallbacks(
            query -> {
                String filter = query.getFilter().orElse(null);
                if (filter != null) {
                    List<BusinessObjectLight> items = new ArrayList();
                    viewNodes.forEach(viewNode -> items.add((BusinessObjectLight) viewNode.getIdentifier()));
                    
                    return items.stream()
                        .sorted(Comparator.comparing(BusinessObjectLight::getName))
                        .filter(item -> item.getName().toLowerCase().contains(filter.toLowerCase()))
                        .skip(query.getOffset())
                        .limit(query.getLimit());
                }
                return null;
            },
            query -> {
                String filter = query.getFilter().orElse(null);
                if (filter != null) {
                    List<BusinessObjectLight> items = new ArrayList();
                    viewNodes.forEach(viewNode -> items.add((BusinessObjectLight) viewNode.getIdentifier()));
                    
                    return (int) items.stream()
                        .sorted(Comparator.comparing(BusinessObjectLight::getName))
                        .filter(item -> item.getName().toLowerCase().contains(filter.toLowerCase()))
                        .skip(query.getOffset())
                        .limit(query.getLimit())
                        .count();
                }
                return 0;
            });
    }
}
