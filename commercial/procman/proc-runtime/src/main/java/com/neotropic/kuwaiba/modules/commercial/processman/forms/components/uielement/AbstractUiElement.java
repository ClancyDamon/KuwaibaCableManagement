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
package com.neotropic.kuwaiba.modules.commercial.processman.forms.components.uielement;

import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.AbstractElement;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementEventListener;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.EventDescriptor;
import java.util.Objects;

/**
 * UI element used to render the form artifact definition element.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @param <E> Form artifact definition element type
 * @param <T> UI element type
 */
public abstract class AbstractUiElement<E extends AbstractElement, T> implements ElementEventListener {
    /**
     * UI element event listener.
     */
    private UiElementEventListener listener;
    /**
     * Form artifact definition element POJO.
     */
    private final E element;
    /**
     * UI element.
     */
    protected T uiElement;
    
    public AbstractUiElement(E element, T uiElement) {
        Objects.requireNonNull(element);
        this.element = element;
        this.uiElement = uiElement;
    }
    /**
     * Init the {@link #getUiElement() UI element} with the properties in the 
     * {@link #getElement() element}.
     */
    protected abstract void postConstruct();
    
    /**
     * Sets the UI element id.
     * @param id UI element id.
     */
    public abstract void setId(String id);
    /**
     * Sets the UI element width.
     * @param width UI element width.
     */
    public abstract void setWidth(String width);
    /**
     * Sets the UI element height.
     * @param height UI element height.
     */
    public abstract void setHeight(String height);
    
    public void build() {
        postConstruct();
        if (element.getId() != null)
            setId(element.getId());
        if (element.getWidth() != null)
            setWidth(element.getWidth());
        if (element.getHeight() != null)
            setHeight(element.getHeight());
        element.setElementEventListener(this);
        setUiElementEventListener(element);
    }
    
    public E getElement() {
        return element;
    }
    
    public T getUiElement() {
        return uiElement;
    }
    
    public UiElementEventListener getUiElementEventListener() {
        return listener;
    }
    
    public void setUiElementEventListener(UiElementEventListener listener) {
        this.listener = listener;
    }
    
    public void fireUiElementEvent(EventDescriptor eventDescriptor) {
        listener.onUiElementEvent(eventDescriptor);
    }
}
