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
package org.neotropic.util.visual.paperdialog;

import com.neotropic.flow.component.paper.dialog.PaperDialog;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Emphasis;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import java.util.List;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.mxgraph.MxGraphCanvas;

/**
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class PaperDialogSearchObject extends Div {
    
    TranslationService ts;
    
    public PaperDialogSearchObject(TranslationService ts, MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> mxGraphCanvas) {
        TextField txtSearch = new TextField();
        txtSearch.setId("txtSearch");
        txtSearch.setWidth("300px");
        txtSearch.setValueChangeMode(ValueChangeMode.EAGER);
        txtSearch.setClearButtonVisible(true);
        txtSearch.setPlaceholder(ts.getTranslatedString("module.general.search-objects-view"));
        
        ActionIcon iconSearch = new ActionIcon(VaadinIcon.SEARCH);
        iconSearch.setId("iconSearch");
        txtSearch.setPrefixComponent(iconSearch);
        
        PaperDialog paperDialog = new PaperDialog();
        paperDialog.setNoOverlap(true);
        paperDialog.setHorizontalAlign(PaperDialog.HorizontalAlign.LEFT);
        paperDialog.setVerticalAlign(PaperDialog.VerticalAlign.TOP);
        paperDialog.setMargin(false);
        paperDialog.positionTarget(txtSearch);
        paperDialog.setWidth(txtSearch.getWidth());
        
        add(txtSearch);
        add(paperDialog);
        
        txtSearch.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<TextField, String>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<TextField, String> event) {
                paperDialog.removeAll();
                if (event.isFromClient()) {
                    VerticalLayout lytContent = new VerticalLayout();
                    lytContent.setPadding(false);
                    lytContent.setMargin(false);
                    List<BusinessObjectLight> lstEquipmentsSearch = mxGraphCanvas.getNodes().keySet().stream()
                                        .filter(item -> item.getName().toLowerCase().contains(event.getValue().toLowerCase()) ||
                                                item.getClassName().toLowerCase().contains(event.getValue().toLowerCase()) ).collect(Collectors.toList());
                    if (!lstEquipmentsSearch.isEmpty()) {
                        
                        Grid<BusinessObjectLight> gridObjects = new Grid();
                        gridObjects.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
                        gridObjects.setItems(lstEquipmentsSearch);
                        gridObjects.setHeightByRows(true);
                        gridObjects.addColumn(new ComponentRenderer<>((obj) -> {
                            HorizontalLayout hly = new HorizontalLayout();
                            hly.setMargin(false);
                            hly.setPadding(false);
                            Icon icon = new Icon(VaadinIcon.COG);
                            icon.setColor("#737373");
                            FlexLayout lytObjectName = new FlexLayout();
                            lytObjectName.setWidthFull();
                            lytObjectName.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                            lytObjectName.setWrapMode(FlexLayout.WrapMode.WRAP);
                            
                            Label lblObjName = new Label(obj.getName());
                            Emphasis emObjClass = new Emphasis(obj.getClassName());
                            lytObjectName.add(lblObjName, emObjClass);                          
                            
                            hly.add(icon, lytObjectName);
                            hly.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
                            hly.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

                            return hly;
                        }));
                        gridObjects.addItemClickListener(listener -> {
                             txtSearch.setValue(listener.getItem().getName());
                             fireEvent(new SelectObjectEvent(PaperDialogSearchObject.this, false, listener.getItem()));
                        });
                        
                        lytContent.add(new BoldLabel(ts.getTranslatedString("module.general.labels.objects")), gridObjects);
                    }
                    
                    if (!lstEquipmentsSearch.isEmpty()) {
                        paperDialog.add(lytContent);
                        paperDialog.open();
                        txtSearch.focus();
                    }
                }
            }
        });
    }

    public Registration addSelectObjectListener(ComponentEventListener<SelectObjectEvent> listener) {
        return addListener(SelectObjectEvent.class, listener);
    }

    public class SelectObjectEvent extends ComponentEvent<PaperDialogSearchObject> {
        private final BusinessObjectLight object;
        
        public SelectObjectEvent(PaperDialogSearchObject source, boolean fromClient, BusinessObjectLight object) {
            super(source, fromClient);
            this.object = object;
        }
        
        public BusinessObjectLight getObject() {
            return object;
        }
    }
}
