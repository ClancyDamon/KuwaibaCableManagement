/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.util.visual.selectors;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FilterDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Cell component to select an object
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CellPortSelector extends FlexLayout {
    private final String placeholder;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    
    private BusinessObjectLight selectedObject;
    private final List<ComboBox<BusinessObjectLight>> cmbObjectSelectors = new ArrayList();
    private final List<HorizontalLayout> rows = new ArrayList();
    private final ValueChangeListener<ComponentValueChangeEvent<ComboBox<BusinessObjectLight>, BusinessObjectLight>> valueChangeListener;
    
    public CellPortSelector(BusinessObjectLight selectedObject, List<BusinessObjectLight> selectedObjects, String placeholder, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) throws InventoryException {
        Objects.requireNonNull(placeholder);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        this.placeholder = placeholder;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        
        if (selectedObject != null) {
            if (selectedObjects == null || selectedObjects.isEmpty())
                selectedObjects = Arrays.asList(selectedObject);
        }
        this.selectedObject = selectedObject;
        
        valueChangeListener = valueChangeEvent -> {
            try {
                ComboBox<BusinessObjectLight> cmbSource = valueChangeEvent.getSource();
                BusinessObjectLight value = valueChangeEvent.getValue();

                int index = cmbObjectSelectors.indexOf(cmbSource);
                List<ComboBox<BusinessObjectLight>> removeCmbs = new ArrayList();
                List<HorizontalLayout> removeRows = new ArrayList();

                for (int i = index + 1; i < cmbObjectSelectors.size(); i++) {
                    removeCmbs.add(cmbObjectSelectors.get(i));
                    HorizontalLayout row = rows.get(i);
                    removeRows.add(row);
                    remove(row);
                }
                cmbObjectSelectors.removeAll(removeCmbs);
                rows.removeAll(removeRows);

                if (value != null) {
                    this.selectedObject = value;
                    add(addRow(value, null));
                    getStyle().set("margin-bottom", "25px");

                } else {
                    if (index - 1 >= 0)
                        this.selectedObject = cmbObjectSelectors.get(index - 1).getValue();
                    else
                        this.selectedObject = null;
                }
                fireEvent(new SelectedObjectChangeEvent(this, this.selectedObject, getSelectedObjects(), valueChangeEvent.isFromClient()));
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        };
        if (selectedObjects != null && !selectedObjects.isEmpty()) {
            for (int i = 0; i < selectedObjects.size(); i++) {
                if (i == 0)
                    add(addRow(null, selectedObjects.get(i)));
                else
                    add(addRow(selectedObjects.get(i - 1), selectedObjects.get(i)));
            }
            getStyle().set("margin-bottom", "25px");
        }
        add(addRow());
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
    }
    
    public HorizontalLayout addRow() throws InventoryException {
        return addRow(null, null);
    }
    
    public HorizontalLayout addRow(BusinessObjectLight parent, BusinessObjectLight businessObject) throws InventoryException {
        HorizontalLayout lytRow = new HorizontalLayout();
        lytRow.setWidthFull();
        lytRow.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        lytRow.setMargin(false);
        lytRow.setPadding(false);
        
        ComboBox<BusinessObjectLight> cmbPortSelector = getObjectSelector(businessObject);
        cmbPortSelector.setWidthFull();
        cmbObjectSelectors.add(cmbPortSelector);
        lytRow.add(cmbPortSelector);
        
        if (parent != null) {
            cmbPortSelector.setWidth("70%");
            
            HashMap<String, Object> attrToFilter = new HashMap();
            attrToFilter.put(Constants.PROPERTY_ENABLED, true);
            
            ComboBox<FilterDefinition> cmbFilters = new ComboBox();
            cmbFilters.setClearButtonVisible(true);
            cmbFilters.setItemLabelGenerator(FilterDefinition::getName);
            cmbFilters.setItems(aem.getFilterDefinitionsForClass(parent.getClassName(), true, true, attrToFilter, -1, -1));
            cmbFilters.setPlaceholder(ts.getTranslatedString("module.connectivity-manager.cell-port-selector.filters"));
            cmbFilters.setRenderer(new ComponentRenderer<>(item -> {
                HorizontalLayout lytItem = new HorizontalLayout(
                    VaadinIcon.FILTER.create(), new Label(item.getName())
                );
                lytItem.setDefaultVerticalComponentAlignment(Alignment.CENTER);
                return lytItem;
            }));
            cmbFilters.setWidthFull();
            cmbFilters.addValueChangeListener(valueChangeEvent -> {
                FilterDefinition value = valueChangeEvent.getValue();
                if (value != null)
                    cmbPortSelector.setDataProvider(SelectorsUtils.getFilterDataProvider(parent, value, ts));
                else
                    cmbPortSelector.setDataProvider(getObjectDataProvider(cmbPortSelector));
            });
            HorizontalLayout lytFilters = new HorizontalLayout(VaadinIcon.FILTER.create(), cmbFilters);
            lytFilters.setWidth("30%");
            lytFilters.setDefaultVerticalComponentAlignment(Alignment.CENTER);
            lytFilters.setMargin(false);
            lytFilters.setPadding(false);

            lytRow.add(lytFilters);
        }
        rows.add(lytRow);
        return lytRow;
    }
    
    private List<BusinessObjectLight> getSelectedObjects() {
        List<BusinessObjectLight> selectedObjects = new ArrayList();
        cmbObjectSelectors.forEach(cmb -> {
            if (cmb.getValue() != null)
                selectedObjects.add(cmb.getValue());
        });
        return selectedObjects;
    }
    
    private ComboBox<BusinessObjectLight> getObjectSelector(BusinessObjectLight selectedObject) {
        ComboBox<BusinessObjectLight> cmbObjectSelector = new ComboBox();
        cmbObjectSelector.setWidthFull();
        cmbObjectSelector.setItemLabelGenerator(BusinessObjectLight::getName);
        cmbObjectSelector.setRenderer(new ObjectRenderer(mem, ts));
        cmbObjectSelector.setClearButtonVisible(true);
        cmbObjectSelector.setDataProvider(getObjectDataProvider(cmbObjectSelector));
        cmbObjectSelector.setValue(selectedObject);
        cmbObjectSelector.addValueChangeListener(valueChangeListener);
        if (placeholder != null)
            cmbObjectSelector.setPlaceholder(placeholder);
        return cmbObjectSelector;
    }
    
    private DataProvider<BusinessObjectLight, String> getObjectDataProvider(ComboBox<BusinessObjectLight> cmbObjectSelector) {
        return DataProvider.fromFilteringCallbacks(
            query -> {
                int index = cmbObjectSelectors.indexOf(cmbObjectSelector);
                BusinessObjectLight object = null;
                if (index - 1 >= 0)
                    object = cmbObjectSelectors.get(index - 1).getValue();

                if (object == null) {
                    String filter = query.getFilter().orElse(null);
                    if (filter != null && !filter.isEmpty()) {
                        return bem.getSuggestedObjectsWithFilter(
                            filter, 
                            query.getOffset(), 
                            query.getLimit(), 
                            Constants.CLASS_VIEWABLEOBJECT, 
                            Constants.CLASS_GENERICPORT
                        ).stream();
                    }
                    else
                        return null;
                } else {
                    return bem.getSuggestedChildrenWithFilter(
                        object.getClassName(), 
                        object.getId(), 
                        query.getFilter().orElse(null), 
                        true,
                        query.getOffset(), 
                        query.getLimit(), 
                        Constants.CLASS_VIEWABLEOBJECT, 
                        Constants.CLASS_GENERICPORT
                    ).stream();
                }
            }, 
            query -> {
                int index = cmbObjectSelectors.indexOf(cmbObjectSelector);
                BusinessObjectLight object = null;
                if (index - 1 >= 0)
                    object = cmbObjectSelectors.get(index - 1).getValue();

                if (object == null) {
                    String filter = query.getFilter().orElse(null);
                    if (filter != null && !filter.isEmpty()) {
                        return (int) bem.getSuggestedObjectsWithFilter(
                            filter, 
                            query.getOffset(), 
                            query.getLimit(), 
                            Constants.CLASS_VIEWABLEOBJECT, 
                            Constants.CLASS_GENERICPORT
                        )
                        .stream()
                        .count();
                    }
                    else
                        return 0;
                } else {
                    return (int) bem.getSuggestedChildrenWithFilter(
                        object.getClassName(), 
                        object.getId(), 
                        query.getFilter().orElse(null), 
                        true,
                        query.getOffset(), 
                        query.getLimit(), 
                        Constants.CLASS_VIEWABLEOBJECT, 
                        Constants.CLASS_GENERICPORT
                    )
                    .stream()
                    .count();
                }
            }
        );
    }
    
    public Registration addSelectedObjectChangeListener(ComponentEventListener<SelectedObjectChangeEvent> listener) {
        return addListener(SelectedObjectChangeEvent.class, listener);
    }
    
    public class SelectedObjectChangeEvent extends ComponentEvent<CellPortSelector> {
        private final BusinessObjectLight selectedObject;
        private final List<BusinessObjectLight> selectedObjects;
        
        public SelectedObjectChangeEvent(CellPortSelector source, BusinessObjectLight selectedObject, List<BusinessObjectLight> selectedObjects, boolean fromClient) {
            super(source, fromClient);
            this.selectedObject = selectedObject;
            this.selectedObjects = selectedObjects;
        }
        
        public BusinessObjectLight getSelectedObject() {
            return selectedObject;
        }
        
        public List<BusinessObjectLight> getSelectedObjects() {
            return selectedObjects;
        }
    }
}
