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
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementListSelectFilter;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.EventDescriptor;
import com.vaadin.flow.component.listbox.ListBox;
import java.util.ArrayList;
import java.util.List;

/**
 * UI element to render the {@link ElementListSelectFilter listSelectFilter} element.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentListSelectFilter extends AbstractUiElement<ElementListSelectFilter, ListBox> {

    public ComponentListSelectFilter(ElementListSelectFilter element) {
        super(element, new ListBox());
    }

    @Override
    protected void postConstruct() {
        if (getElement().getItems() != null)
            getUiElement().setItems(getElement().getItems());
        getUiElement().addValueChangeListener(valueChangeEvent -> {
            if (valueChangeEvent.isFromClient()) {
                fireUiElementEvent(new EventDescriptor(
                    Constants.EventAttribute.ONPROPERTYCHANGE, 
                    Constants.Property.VALUE, 
                    valueChangeEvent.getValue(), 
                    valueChangeEvent.getOldValue()
                ));
            }
        });
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
            
            if (Constants.Property.ITEMS.equals(event.getPropertyName())) {
                List lst = new ArrayList();
                if (event.getNewValue() != null) {
                    for (Object obj : (List) event.getNewValue())
                        lst.add(obj);
                }
                getUiElement().setItems(lst);
            } else if (Constants.Property.VALUE.equals(event.getPropertyName())) {
                getUiElement().setValue(event.getNewValue());
            }
        }
    }
    
}
