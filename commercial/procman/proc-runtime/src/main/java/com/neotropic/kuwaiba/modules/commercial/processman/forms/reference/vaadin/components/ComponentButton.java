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
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementButton;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.EventDescriptor;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * UI element to render the {@link ElementButton button} element
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentButton extends AbstractUiElement<ElementButton, Button> {
    
    public ComponentButton(ElementButton elementButton) {
        super(elementButton, new Button());
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONCLICK.equals(event.getEventName())) {
            if (Constants.Function.SAVE.equals(event.getPropertyName())) {
            }
        }
        else if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
                        
            if (Constants.Property.HIDDEN.equals(event.getPropertyName())) {
                getUiElement().setVisible(!getElement().isHidden());
            } 
            else if (Constants.Property.ENABLED.equals(event.getPropertyName())) {
                getUiElement().setEnabled((boolean) event.getNewValue());
            }
        }
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
    public void setHeight(String heigth) {
        getUiElement().setHeight(heigth);
    }
    
    @Override
    protected void postConstruct() {
        ElementButton elementButton = getElement();
        Button button = getUiElement();
        
        button.setText(elementButton.getCaption());
        button.setEnabled(elementButton.isEnabled());
        if (elementButton.getStyleName() != null) {
            String[] styleNames = elementButton.getStyleName().split(";");
            for (int i = 0; i < styleNames.length; i++) {
                switch (styleNames[i]) {
                    case Constants.Attribute.StyleName.BUTTON_CLOSE_ICON:
                        button.setIcon(VaadinIcon.CLOSE.create());
                    break;
                    case Constants.Attribute.StyleName.BUTTON_PENCIL_ICON:
                        button.setIcon(VaadinIcon.PENCIL.create());
                    break;
                    case Constants.Attribute.StyleName.BUTTON_PLUS_ICON:
                        button.setIcon(VaadinIcon.PLUS.create());
                    break;
                    case Constants.Attribute.StyleName.BUTTON_COGS_ICON:
                        button.setIcon(VaadinIcon.COGS.create());
                    break;
                    case Constants.Attribute.StyleName.BUTTON_PLUS_ICON_ONLY:
                        button.setIcon(VaadinIcon.PLUS.create());
                    break;
                    case Constants.Attribute.StyleName.BUTTON_PRIMARY:
                    break;
                    case Constants.Attribute.StyleName.BUTTON_DANGER:
                    break;

                    
                }
            }
        }
        button.addClickListener(clickEvent -> fireUiElementEvent(
            new EventDescriptor(Constants.EventAttribute.ONCLICK)
        ));
    }
}
