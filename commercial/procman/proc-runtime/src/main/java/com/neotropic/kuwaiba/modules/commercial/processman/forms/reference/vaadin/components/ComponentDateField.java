/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.components;

import com.neotropic.kuwaiba.modules.commercial.processman.forms.components.uielement.AbstractUiElement;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.Constants;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementDateField;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.EventDescriptor;
import com.vaadin.flow.component.datepicker.DatePicker;
import java.time.LocalDate;

/**
 * UI element to render the {@link ElementDateField dateField} element
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentDateField extends AbstractUiElement<ElementDateField, DatePicker> {

    public ComponentDateField(ElementDateField element) {
        super(element, new DatePicker());
    }

    @Override
    protected void postConstruct() {
        getUiElement().setValue(getElement().getValue());
        getUiElement().setEnabled(getElement().isEnabled());
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
            
            if (Constants.Property.VALUE.equals(event.getPropertyName())) {
                if (event.getNewValue() instanceof String) {
                    getUiElement().setValue(LocalDate.parse((String) event.getNewValue()));
                }
                else
                    getUiElement().setValue((LocalDate) event.getNewValue());
            }
        }
    }
    
}
