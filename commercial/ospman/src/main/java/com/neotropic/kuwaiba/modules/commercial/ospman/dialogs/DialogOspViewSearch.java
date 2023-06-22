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

import com.neotropic.flow.component.paper.dialog.PaperDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Dialog to search between the nodes and edges in the outside plant view
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DialogOspViewSearch extends PaperDialog {
    private final int MIN_FILTER_LENGTH = 3;    
    
    public DialogOspViewSearch(Component componentTarget, TranslationService ts, List<AbstractViewNode> viewNodes, List<AbstractViewEdge> viewEdges, Consumer<BusinessObjectLight> callbackAnimate) {
        Objects.requireNonNull(componentTarget);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(viewNodes);
        Objects.requireNonNull(viewEdges);
        Objects.requireNonNull(callbackAnimate);
        setWidth("20%");
        setHeight("30%");
        
        TextField txtFilter = new TextField();
        txtFilter.setWidthFull();
        txtFilter.focus();
        txtFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtFilter.setClearButtonVisible(true);
        txtFilter.setPlaceholder(ts.getTranslatedString("module.ospman.tools.search.filter"));
        txtFilter.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        
        ListBox<BusinessObjectLight> lstObjects = new ListBox();
        lstObjects.setRenderer(new ComponentRenderer<>(object -> new Label(object.getName())));
        lstObjects.addValueChangeListener(valueChangeEvent -> {
            if (valueChangeEvent.getValue() != null)
                callbackAnimate.accept(valueChangeEvent.getValue());
        });
        Scroller lytObjects = new Scroller();
        lytObjects.setSizeFull();
        lytObjects.setContent(lstObjects);
        
        positionTarget(componentTarget);
        setNoOverlap(true);
        setHorizontalAlign(PaperDialog.HorizontalAlign.LEFT);
        setVerticalAlign(PaperDialog.VerticalAlign.TOP);
        setMargin(false);
        
        FlexLayout lytContent = new FlexLayout();
        lytContent.setSizeFull();
        lytContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        lytContent.getStyle().set("margin", "0px"); //NOI18N
        lytContent.getStyle().set("padding", "0px"); //NOI18N
        
        lytContent.add(txtFilter, lytObjects);
        add(lytContent);
        dialogConfirm(lytObjects);
        
        List<BusinessObjectLight> items = new ArrayList();
        viewNodes.forEach(viewNode -> items.add((BusinessObjectLight) viewNode.getIdentifier()));
        viewEdges.forEach(viewEdge -> items.add((BusinessObjectLight) viewEdge.getIdentifier()));
        Collections.sort(items, (item1, item2) -> item1.getName().compareToIgnoreCase(item2.getName()));
        
        txtFilter.addValueChangeListener(event -> {
            if (event.getValue().length() >= MIN_FILTER_LENGTH) {
                List<BusinessObjectLight> filteredItems = new ArrayList();
                items.forEach(item -> {
                    if (item.getName() != null && item.getName().toLowerCase().contains(event.getValue().toLowerCase()))
                        filteredItems.add(item);
                });
                Collections.sort(filteredItems, (item1, item2) -> item1.getName().compareToIgnoreCase(item2.getName()));
                
                lstObjects.setItems(filteredItems);
            } else if (event.getValue().length() == 0) {
                lstObjects.setItems(items);
            } else {
                lstObjects.setItems(Collections.EMPTY_LIST);
            }
        });
        lstObjects.setItems(items);
    }
}
