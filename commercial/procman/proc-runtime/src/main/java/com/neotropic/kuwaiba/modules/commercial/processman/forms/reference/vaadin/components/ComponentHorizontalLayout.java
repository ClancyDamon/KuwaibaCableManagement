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
import com.neotropic.kuwaiba.modules.commercial.processman.forms.components.uielement.UiElementContainer;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.AbstractElement;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.Constants;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementHorizontalLayout;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.EventDescriptor;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * UI element to render the {@link ElementHorizontalLayout horizontalLayout} element.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentHorizontalLayout extends AbstractUiElement<ElementHorizontalLayout, HorizontalLayout> implements UiElementContainer {
    private final List<AbstractUiElement<? extends AbstractElement, ? extends Component>> children = new ArrayList();

    public ComponentHorizontalLayout(ElementHorizontalLayout element) {
        super(element, new HorizontalLayout());
    }

    @Override
    protected void postConstruct() {}

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
            
            if (Constants.Property.REPAINT.equals(event.getPropertyName()))
                repaint();
        }
    }

    @Override
    public void addChildren(AbstractUiElement child) {
        children.add(child);
    }

    @Override
    public List<AbstractUiElement<? extends AbstractElement, ? extends Component>> getChildren() {
        return children;
    }

    @Override
    public void repaint() {
        getUiElement().removeAll();
        getChildren().forEach(child -> {
            if (!child.getElement().isHidden())
                getUiElement().add(child.getUiElement());
        });
    }
    
}
