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
package com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.components;

import com.neotropic.kuwaiba.modules.commercial.processman.forms.components.uielement.AbstractUiElement;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.Constants;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementGrid;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.EventDescriptor;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.util.visual.general.BoldLabel;
/**
 * UI element to render the {@link ElementGrid grid} element
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentGrid extends AbstractUiElement<ElementGrid, Grid<LinkedHashMap<String, Object>>> {
    private final List<LinkedHashMap<String, Object>> rows = new ArrayList();

    public ComponentGrid(ElementGrid element) {
        super(element, new Grid());
    }

    @Override
    protected void postConstruct() {
        getUiElement().setHeightByRows(true);
        getUiElement().addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        
        if (getElement().getSelectionMode() != null) {
            switch (getElement().getSelectionMode()) {
                case ElementGrid.SELECTION_MODE_MULTI:
                    getUiElement().setSelectionMode(Grid.SelectionMode.MULTI);
                break;
                case ElementGrid.SELECTION_MODE_NONE:
                    getUiElement().setSelectionMode(Grid.SelectionMode.NONE);
                break;
                case ElementGrid.SELECTION_MODE_SINGLE:
                    getUiElement().setSelectionMode(Grid.SelectionMode.SINGLE);
                break;
                default:
                    getUiElement().setSelectionMode(Grid.SelectionMode.SINGLE);
                break;
            }
        }
        if (getElement().getColums() != null) {
            getElement().getColums().forEach(column -> {
                getUiElement().addColumn(item  -> {
                    Object cell = item.get(column.getCaption());
                    if (cell instanceof BusinessObjectLight)
                        return ((BusinessObjectLight) cell).getName();
                    return cell;
                }).setHeader(new BoldLabel(column.getCaption())).setResizable(true);
            });
        }
        if (getElement().getRows() != null)
            setRows();
        
        getUiElement().setEnabled(getElement().isEnabled());
        getUiElement().addSelectionListener(selectionEvent -> {
            
            if (ElementGrid.SELECTION_MODE_MULTI.equals(getElement().getSelectionMode())) {
                List<Integer> selectedRows = new ArrayList();
                
                selectionEvent.getAllSelectedItems().forEach(selectedItem -> {
                    if (rows.contains(selectedItem))
                        selectedRows.add(rows.indexOf(selectedItem));
                });
                fireUiElementEvent(new EventDescriptor(
                    Constants.EventAttribute.ONPROPERTYCHANGE, 
                    Constants.Property.SELECTED_ROWS, 
                    selectedRows, 
                    null
                ));
            } else {
                long selectedItemIndex = -1;
                if (!Optional.empty().equals(selectionEvent.getFirstSelectedItem())) {
                    LinkedHashMap<String, Object> selectedItem = selectionEvent.getFirstSelectedItem().get();

                    if (selectedItem != null && rows.contains(selectedItem))
                        selectedItemIndex = rows.indexOf(selectedItem);
                }
                fireUiElementEvent(new EventDescriptor(
                    Constants.EventAttribute.ONPROPERTYCHANGE, 
                    Constants.Property.SELECTED_ROW, 
                    selectedItemIndex, 
                    -1
                ));
            }
        });
    }
    
    private void setRows() {
        rows.clear();
        
        if (getElement().getRows() != null) {
            getElement().getRows().forEach(elementRow -> {
                LinkedHashMap<String, Object> row = new LinkedHashMap();
                for (int i = 0; i < elementRow.size(); i++)
                    row.put(getElement().getColums().get(i).getCaption(), elementRow.get(i));
                rows.add(row);
            });
        }
        getUiElement().setItems(rows);
    }
    
    @Override
    public void setId(String id) {
        getUiElement().setId(id);
    }

    @Override
    public void setWidth(String width) {
        getUiElement().setWidth(width);
    }

    @Override
    public void setHeight(String height) {
        getUiElement().setHeight(height);
    }

    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
            if (Constants.Property.ROWS.equals(event.getPropertyName()))
                setRows();
        }
    }
    
}
