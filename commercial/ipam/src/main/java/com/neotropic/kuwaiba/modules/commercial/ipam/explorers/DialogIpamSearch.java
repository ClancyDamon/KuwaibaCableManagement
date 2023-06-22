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
package com.neotropic.kuwaiba.modules.commercial.ipam.explorers;

import com.neotropic.flow.component.paper.dialog.PaperDialog;
import com.neotropic.kuwaiba.modules.commercial.ipam.visual.IconLabelCellGrid;
import com.neotropic.kuwaiba.modules.commercial.ipam.visual.IpamNode;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Dynamic dialog that shows the search results while the user is typing in the search box.
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class DialogIpamSearch extends Div {
    
    private static final int MAX_CLASSES_SEARCH_LIMIT = 4;
    private static final int MAX_OBJECTS_SEARCH_LIMIT = 8;
    private PaperDialog paperDialog;
    private TextField txtSearch ;
    private List<IpamNode> results;
        
    public DialogIpamSearch(TranslationService ts, 
            BusinessEntityManager bem, 
            Consumer<Object> consumerSearch)  
    {
        Icon icnSearch = new Icon(VaadinIcon.SEARCH);
        icnSearch.setSize("20px");
        icnSearch.setClassName("search-ico");
        txtSearch = new TextField();
        txtSearch.setAutofocus(true);
        txtSearch.setClassName("search-box-large");
        txtSearch.setPlaceholder(ts.getTranslatedString("module.ipam.lbl.search-by-ip-or-folder"));
        txtSearch.setPrefixComponent(icnSearch);
        txtSearch.setClearButtonVisible(true);
        txtSearch.setTabIndex(0);
        txtSearch.setWidth("520px");
        txtSearch.setValueChangeMode(ValueChangeMode.EAGER);
        
        paperDialog = new PaperDialog();
        paperDialog.setNoOverlap(true);
        paperDialog.setHorizontalAlign(PaperDialog.HorizontalAlign.LEFT);
        paperDialog.setVerticalAlign(PaperDialog.VerticalAlign.TOP);
        paperDialog.setVerticalAlign(PaperDialog.VerticalAlign.TOP);
        paperDialog.setMargin(false);
        paperDialog.positionTarget(txtSearch);
        paperDialog.setWidth(txtSearch.getWidth());
        paperDialog.setClassName("paper-dialog-fix-style");
        
        add(paperDialog);
        add(txtSearch);
        
        txtSearch.addKeyPressListener(e-> {
            if (e.getKey().getKeys().get(0).equals(Key.ENTER.getKeys().get(0))) //Weirdly enough, event.getKey().equals(Key.Enter) ALWAYS returns false
                consumerSearch.accept(txtSearch.getValue());     
        });
        
        icnSearch.addClickListener(e -> consumerSearch.accept(txtSearch.getValue()));
        
        txtSearch.addValueChangeListener(e -> {
            paperDialog.removeAll();
            paperDialog.setVisible(true);
            results = new ArrayList<>();
            //we start searching after two characters
            if (e.isFromClient() && !e.getValue().isEmpty()) {
                try {
                    results.clear();
                    VerticalLayout lytContent = new VerticalLayout();
                    lytContent.setPadding(false);
                    lytContent.setMargin(false);
                    lytContent.setSpacing(false);
                    lytContent.setClassName("search-results-content");
                    lytContent.setHeight("auto");
                    
                    List<String> classes = new ArrayList<>();
                    classes.add(Constants.CLASS_SUBNET_IPV4);
                    classes.add(Constants.CLASS_SUBNET_IPV6);
                    classes.add(Constants.CLASS_IP_ADDRESS);
                    
                    HashMap<String, List<BusinessObjectLight>> searchResults = 
                        bem.getSuggestedObjectsWithFilterGroupedByClassName(
                                classes, e.getValue()
                                , 0, MAX_CLASSES_SEARCH_LIMIT
                                , 0, MAX_OBJECTS_SEARCH_LIMIT);
                    
                    List<String> folders = new ArrayList<>();
                    folders.add(Constants.CLASS_GENERICADDRESS);

                    HashMap<String, List<InventoryObjectPool>> suggestedPoolsByName =
                        bem.getSuggestedPoolsByName(folders, e.getValue(), 0, MAX_CLASSES_SEARCH_LIMIT, 0, MAX_OBJECTS_SEARCH_LIMIT);
                    
                    Grid<IpamNode> grid = new Grid<>();
                    grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
                    
                    searchResults.entrySet().forEach(entry -> 
                        entry.getValue().forEach(o -> results.add(new IpamNode(o)))
                    );

                    suggestedPoolsByName.entrySet().forEach(entry -> 
                        entry.getValue().forEach(o -> results.add(new IpamNode(o)))
                    );
                    
                    grid.addComponentColumn(obj -> new IconLabelCellGrid(((IpamNode)obj).getName()
                        , ((IpamNode)obj).getClassName()
                        , ((IpamNode)obj).isPool()
                        , ((IpamNode)obj).isSelected()));
                    
                    grid.setItems(results);
                    grid.addItemClickListener(t -> consumerSearch.accept(t.getItem()));
                    grid.setHeightByRows(true);
                    
                    if(results.isEmpty())
                        lytContent.add(new Label(ts.getTranslatedString("module.general.messages.no-search-results")));
                    
                    lytContent.add(grid);
                    lytContent.getHeight();
                    
                    paperDialog.add(lytContent);
                    paperDialog.open();
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
            else if(e.getValue().isEmpty())
                paperDialog.close();
            
            txtSearch.focus();
        });
    }

            
    public Registration addSelectObjectListener(ComponentEventListener<SelectObjectEvent> listener) {
        return addListener(SelectObjectEvent.class, listener);
    }

    public class SelectObjectEvent extends ComponentEvent<DialogIpamSearch> {
        private final BusinessObjectLight object;
        
        public SelectObjectEvent(DialogIpamSearch source, boolean fromClient, BusinessObjectLight object) {
            super(source, fromClient);
            this.object = object;
        }
        
        public BusinessObjectLight getObject() {
            return object;
        }
    }
    
    public void close(){
        paperDialog.removeAll();
        paperDialog.setVisible(false);
    }
    
    public void clearSearch(){
        txtSearch.setValue("");
    }

    public String getSearchedText(){
        return txtSearch.getValue();
    }

    public List<IpamNode> getResults() {
        return results;
    }
}