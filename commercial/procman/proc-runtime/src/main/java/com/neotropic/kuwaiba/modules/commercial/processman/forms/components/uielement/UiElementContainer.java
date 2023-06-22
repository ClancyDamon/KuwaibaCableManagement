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
import com.vaadin.flow.component.Component;
import java.util.List;

/**
 * A UI element container is a UI element that contains UI elements.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface UiElementContainer {
    /**
     * Adds a UI element.
     * @param child UI element child.
     */
    void addChildren(AbstractUiElement child);
    /**
     * Gets children.
     * @return The UI element container children.
     */
    List<AbstractUiElement<? extends AbstractElement, ? extends Component>> getChildren();
    /**
     * Repaints the UI element container.
     */
    void repaint();
}
