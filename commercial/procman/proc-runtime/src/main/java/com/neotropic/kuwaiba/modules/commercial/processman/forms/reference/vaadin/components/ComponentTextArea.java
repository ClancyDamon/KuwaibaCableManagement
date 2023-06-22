/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.components;

import com.neotropic.kuwaiba.modules.commercial.processman.forms.components.uielement.AbstractUiElement;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.Constants;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementTextArea;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.EventDescriptor;
import com.vaadin.flow.component.textfield.TextArea;

/**
 * UI element to render the {@link ElementTextArea textArea} element
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentTextArea extends AbstractUiElement<ElementTextArea, TextArea> {

    public ComponentTextArea(ElementTextArea element) {
        super(element, new TextArea());
    }
    
    @Override
    protected void postConstruct() {
        if (getElement().getValue() != null)
            getUiElement().setValue(getElement().getValue().toString());
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
            
            if (Constants.Property.VALUE.equals(event.getPropertyName()))
                getUiElement().setValue(event.getNewValue() != null ? event.getNewValue().toString() : "");
            
            if (Constants.Property.ENABLED.equals(event.getPropertyName()))
                getUiElement().setEnabled((boolean) event.getNewValue());
            
            if (Constants.Property.HIDDEN.equals(event.getPropertyName()))
                getUiElement().setVisible(!getElement().isHidden());
        }
    }
    
}
