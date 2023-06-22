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
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementSubform;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.EventDescriptor;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * UI element to render the {@link ElementSubform subform} element.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentSubform extends AbstractUiElement<ElementSubform, VerticalLayout> implements UiElementContainer {
    private final List<AbstractUiElement<? extends AbstractElement, ? extends Component>> children = new ArrayList();
    private Dialog wdw;

    public ComponentSubform(ElementSubform element) {
        super(element, new VerticalLayout());
    }

    @Override
    protected void postConstruct() {}

    @Override
    public void setId(String id) {
        getUiElement().setId(id);
    }

    @Override
    public void setWidth(String width) {
        getUiElement().setWidthFull();
    }

    @Override
    public void setHeight(String height) {
        getUiElement().setHeightFull();
    }

    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONCLICK.equals(event.getEventName())) {
            if (Constants.Function.OPEN.equals(event.getPropertyName())) {
                if (getElement().isEnabled()) {
                    wdw = new Dialog();
                    if (getElement().getWidth() != null)
                        wdw.setWidth(getElement().getWidth());
                    if (getElement().getHeight()!= null)
                        wdw.setHeight(getElement().getHeight());
                    wdw.add(getUiElement());
                    wdw.open();
                }
            } else if (Constants.Function.CLOSE.equals(event.getPropertyName())) {
                if (wdw != null)
                    wdw.close();
            } else if (Constants.Function.CLEAN.equals(event.getPropertyName())) {
            }
        } else if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
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
