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
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementComboBox;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.EventDescriptor;
import com.vaadin.flow.component.combobox.ComboBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * UI element to render the {@link ElementComboBox comboBox} element
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentComboBox extends AbstractUiElement<ElementComboBox, ComboBox> {

    public ComponentComboBox(ElementComboBox element) {
        super(element, new ComboBox());
    }

    @Override
    protected void postConstruct() {
        getUiElement().setClearButtonVisible(true);
        getUiElement().setItemLabelGenerator(item -> {
            if (item instanceof BusinessObjectLight)
                return ((BusinessObjectLight) item).getName();
            return item.toString();
        });
        if (getElement().getItems() != null) {
            if (getElement().getSort())
                Collections.sort(getElement().getItems());
            getUiElement().setItems(getElement().getItems());
        }
        if (getElement().getValue() != null) {
            if (getElement().getItems() == null)
                getUiElement().setItems(getElement().getValue());
            getUiElement().setValue(getElement().getValue());
        }
        getUiElement().setPageSize(10);
        getUiElement().setRequired(getElement().isMandatory());
        getUiElement().setRequiredIndicatorVisible(getElement().isMandatory());
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
        getUiElement().getElement().addEventListener(
            "click", clickEvent -> getElement().fireOnLazyLoad() //NOI18N
        );
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
                if (event.getNewValue() != null)
                    ((List) event.getNewValue()).forEach(obj -> lst.add(obj));
                if (getElement().getSort())
                    Collections.sort(lst);
                getUiElement().setItems(lst);
                if (getElement().getValue() != null)
                    getUiElement().setValue(getElement().getValue());
            }
            else if (Constants.Property.VALUE.equals(event.getPropertyName())) {
                if (getElement().getItems() == null)
                    getUiElement().setItems(getElement().getValue());
                getUiElement().setValue(event.getNewValue());
            }
            else if (Constants.Property.HIDDEN.equals(event.getPropertyName())) {
                getUiElement().setVisible(!((ElementComboBox) getUiElementEventListener()).isHidden());
            } 
            else if (Constants.Property.ENABLED.equals(event.getPropertyName())) {
                getUiElement().setEnabled((boolean) event.getNewValue());
            }
        }
    }
    
}
