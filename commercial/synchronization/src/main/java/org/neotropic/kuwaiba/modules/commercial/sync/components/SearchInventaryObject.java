/*
 * Copyright 2023-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.sync.components;

import com.neotropic.flow.component.paper.dialog.PaperDialog;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.explorers.DialogNavigationSearch;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.NavResultGrid;
import org.neotropic.util.visual.grids.IconNameCellGrid;
import org.neotropic.util.visual.icons.IconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
public class SearchInventaryObject extends Div {

    /**
     * limit of the amount of classes that will be search
     */
    public static final int MAX_CLASSES_SEARCH_LIMIT = 4;
    /**
     * The limit of object that will be search by class
     */
    public static final int MAX_OBJECTS_SEARCH_LIMIT = 5;

    private PaperDialog paperDialog;
    private TextField txtSearch;

    public SearchInventaryObject(TranslationService ts,
                                 BusinessEntityManager bem, IconGenerator iconGenerator,
                                 Consumer<Object> consumerSearch) {

        Icon iconSearch = new Icon(VaadinIcon.SEARCH);
        iconSearch.setSize("20px");
        txtSearch = new TextField();
        txtSearch.setAutofocus(true);
        txtSearch.setClassName("search-box-large");
        txtSearch.setPlaceholder(ts.getTranslatedString("module.sync.search-by-name-or-class"));
        txtSearch.setPrefixComponent(iconSearch);
        txtSearch.setClearButtonVisible(true);
        txtSearch.setWidthFull();
        txtSearch.setValueChangeMode(ValueChangeMode.TIMEOUT);
        txtSearch.setValueChangeTimeout(550);

        paperDialog = new PaperDialog();
        paperDialog.setNoOverlap(true);
        paperDialog.setHorizontalAlign(PaperDialog.HorizontalAlign.LEFT);
        paperDialog.setVerticalAlign(PaperDialog.VerticalAlign.TOP);
        paperDialog.setMargin(false);
        paperDialog.positionTarget(txtSearch);
        paperDialog.setWidth(txtSearch.getWidth());
        paperDialog.setClassName("paper-dialog-fix-style");

        add(paperDialog);
        add(txtSearch);

        txtSearch.addKeyPressListener(e -> {
            if (e.getKey().getKeys().get(0).equals(Key.ENTER.getKeys().get(0))) //Weirdly enough, event.getKey().equals(Key.Enter) ALWAYS returns false
                consumerSearch.accept(txtSearch.getValue());
        });

        txtSearch.addValueChangeListener(e -> {
            paperDialog.removeAll();
            paperDialog.setVisible(true);

            if (e.isFromClient() && e.getValue().length() > 2) {
                try {
                    VerticalLayout lytContent = new VerticalLayout();
                    lytContent.setPadding(false);
                    lytContent.setMargin(false);
                    lytContent.setSpacing(false);
                    lytContent.setClassName("search-results-content");
                    lytContent.setHeight("auto");

                    HashMap<String, List<BusinessObjectLight>> searchResults =
                            bem.getSuggestedObjectsWithFilterGroupedByClassName(
                                    null, e.getValue()
                                    , 0, MAX_CLASSES_SEARCH_LIMIT
                                    , 0, MAX_OBJECTS_SEARCH_LIMIT);
                    if (!searchResults.isEmpty()) {
                        for (Map.Entry<String, List<BusinessObjectLight>> entry : searchResults.entrySet()) {
                            String className = entry.getKey();
                            NavResultGrid<BusinessObjectLight> grid = new NavResultGrid<>(bem,
                                    className, e.getValue(), entry.getValue());
                            grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
                            grid.addComponentColumn(item -> new IconNameCellGrid(item.toString(), item.getClassName(), iconGenerator));
                            grid.setItems(entry.getValue());
                            grid.setHeightByRows(true);
                            lytContent.add(new Label(className + " (" + entry.getValue().size() + ") "), grid);
                            grid.addItemClickListener(t -> consumerSearch.accept(t.getItem()));
                        }
                    } else
                        lytContent.add(new Label(ts.getTranslatedString("module.general.messages.no-search-results")));

                    paperDialog.add(lytContent);
                    paperDialog.open();
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
            } else if (e.getValue().isEmpty())
                paperDialog.close();

            txtSearch.focus();
        });
    }

    public Registration addSelectObjectListener(ComponentEventListener<SelectObjectEvent> listener) {
        return addListener(SelectObjectEvent.class, listener);
    }

    public void close() {
        paperDialog.removeAll();
        paperDialog.setVisible(false);
    }

    public void clearSearch() {
        txtSearch.setValue("");
    }

    public class SelectObjectEvent extends ComponentEvent<DialogNavigationSearch> {
        private final BusinessObjectLight object;

        public SelectObjectEvent(DialogNavigationSearch source, boolean fromClient, BusinessObjectLight object) {
            super(source, fromClient);
            this.object = object;
        }

        public BusinessObjectLight getObject() {
            return object;
        }
    }
}
