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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;

/**
 * Window to select a set of objects to add like a node to the Outside Plant View 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowAddNodes extends ConfirmDialog {
    private final BusinessEntityManager bem;
    private final TranslationService ts;
    private final Consumer<Boolean> consumerDrawingNode;
    private final List<AbstractViewNode> viewNodes;
    private Supplier<BusinessObjectLight> supplierNode;
    private Consumer<BusinessObjectLight> consumerAddedNode;
    
    public WindowAddNodes(List<AbstractViewNode> viewNodes, BusinessEntityManager bem, TranslationService ts, Consumer<Boolean> consumerDrawingNode) {
        Objects.requireNonNull(ts);
        Objects.requireNonNull(consumerDrawingNode);
        
        this.bem = bem;
        this.ts = ts;
        this.consumerDrawingNode = consumerDrawingNode;
        this.viewNodes = viewNodes;
        
        setModal(false);
        setDraggable(true);
        setContentSizeFull();
        setWidth("25%");
        setHeight("80%");
        getElement().getThemeList().add("wdw-osp-tool"); //NOI18N
    }
    
    public BusinessObjectLight getNode() {
        if (supplierNode != null)
            return supplierNode.get();
        return null;
    }
    
    public void notifyAddedNode(BusinessObjectLight node) {
        if (consumerAddedNode != null)
            consumerAddedNode.accept(node);
    }
    
    @Override
    public void open() {
        consumerDrawingNode.accept(true);
        
        TextField txtSearch = new TextField();
        txtSearch.setWidthFull();
        txtSearch.setPlaceholder(ts.getTranslatedString("module.ospman.dialog.add-nodes.search"));
        txtSearch.setClearButtonVisible(true);
        txtSearch.setSuffixComponent(VaadinIcon.SEARCH.create());
        
        Label lblSelectNodes = new Label();
        Label lblDisabledNodes = new Label();
        
        MultiSelectListBox<String> lstSelectAll = new MultiSelectListBox();
                
        MultiSelectListBox<BusinessObjectLight> lstSelectedNodes = new MultiSelectListBox();
        
        Scroller lytSelectedNodes = new Scroller();        
        lytSelectedNodes.setSizeFull();
        lytSelectedNodes.setContent(lstSelectedNodes);
        
        FlexLayout lytContent = new FlexLayout(txtSearch, lblSelectNodes, lstSelectAll, lytSelectedNodes, lblDisabledNodes);
        lytContent.setSizeFull();
        lytContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);    
        lytContent.setAlignSelf(FlexComponent.Alignment.CENTER, lblDisabledNodes);
                
        ComponentEventListener<ClickEvent<Button>> closeClickEventListener = clickEvent -> close();
        
        Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"));
        btnClose.addClickListener(closeClickEventListener);
        
        HorizontalLayout lytFooter = new HorizontalLayout(btnClose);
        lytFooter.setSizeFull();
        lytFooter.setFlexGrow(1, btnClose);
        
        setHeader(ts.getTranslatedString("module.ospman.dialog.add-nodes.title"));
        setContent(lytContent);
        setFooter(lytFooter);
        // Adding Event Listeners
        List<BusinessObjectLight> selectedNodes = new ArrayList();
        Function<BusinessObjectLight, Boolean> functionNodeIsEnabled = node -> {
            for (AbstractViewNode viewNode : viewNodes) {
                if (node.equals((BusinessObjectLight) viewNode.getIdentifier()))
                    return false;
            }
            return true;
        };
        supplierNode = () -> !selectedNodes.isEmpty() ? selectedNodes.get(0) : null;
        consumerAddedNode = node -> {
            selectedNodes.remove(node);
            lstSelectedNodes.deselect(node);
            lstSelectedNodes.getDataProvider().refreshItem(node);
        };
        Runnable runnableSetText = () -> {            
            if (!selectedNodes.isEmpty()) {
                if (selectedNodes.size() > 1)
                    lblSelectNodes.setText(ts.getTranslatedString("module.ospman.dialog.add-nodes.selected-nodes"));
                else
                    lblSelectNodes.setText(ts.getTranslatedString("module.ospman.dialog.add-nodes.selected-node"));
            }
            else
                lblSelectNodes.setText(ts.getTranslatedString("module.ospman.dialog.add-nodes.no-selected-nodes"));
        };
        
        lstSelectedNodes.setItemEnabledProvider(selectedNode -> functionNodeIsEnabled.apply(selectedNode));
        lstSelectedNodes.addSelectionListener(valueChangeEvent -> {
            valueChangeEvent.getAddedSelection().forEach(selectedNode -> {
                if (!selectedNodes.contains(selectedNode))
                    selectedNodes.add(selectedNode);
            });
            if (!valueChangeEvent.getRemovedSelection().isEmpty())
                lstSelectAll.deselectAll();
            valueChangeEvent.getRemovedSelection().forEach(selectedNode -> selectedNodes.remove(selectedNode));
            runnableSetText.run();
        });
        
        txtSearch.addValueChangeListener(valueChangeEvent -> {
            String value = valueChangeEvent.getValue();
            if (value != null) {
                List<BusinessObjectLight> objects = bem.getSuggestedObjectsWithFilter(value, Constants.CLASS_VIEWABLEOBJECT, 0);
                Collections.sort(objects, Comparator.comparing(BusinessObjectLight::getName));
                lstSelectedNodes.setItems(objects);
                
                if (!objects.isEmpty()) {
                    runnableSetText.run();
                    if (objects.size() > 1)
                        lstSelectAll.setItems(ts.getTranslatedString("module.ospman.dialog.add-nodes.item.select-all"));
                    else {
                        if (functionNodeIsEnabled.apply(objects.get(0)))
                            lstSelectedNodes.select(objects.get(0));
                        lstSelectAll.setItems(Collections.EMPTY_LIST);
                    }
                    lblDisabledNodes.setText(ts.getTranslatedString("module.ospman.dialog.add-nodes.disabled-nodes"));
                } else {
                    lblSelectNodes.setText(ts.getTranslatedString("module.ospman.dialog.add-nodes.search-zero-results"));
                    lblDisabledNodes.setText("");
                }
            } else {
                lblSelectNodes.setText("");
                lblDisabledNodes.setText("");
                
                selectedNodes.clear();
                lstSelectedNodes.setItems(Collections.EMPTY_LIST);
            }
        });
        lstSelectAll.addSelectionListener(valueChangeEvent -> {
            if (valueChangeEvent.isFromClient()) {
                if (!valueChangeEvent.getAddedSelection().isEmpty()) {
                    lstSelectedNodes.getDataProvider().fetch(new Query<>()).forEach(selectedNode -> {
                        if (!lstSelectedNodes.isSelected(selectedNode) && functionNodeIsEnabled.apply(selectedNode))
                            lstSelectedNodes.select(selectedNode);
                    });
                } else
                    lstSelectedNodes.deselectAll();
            }
        });
        super.open();
    }
    
    @Override
    public void close() {
        consumerDrawingNode.accept(false);
        super.close();
    }
}