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
package com.neotropic.kuwaiba.modules.commercial.processman.forms.elements;

import com.neotropic.kuwaiba.modules.commercial.processman.forms.components.uielement.AbstractUiElement;

/**
 * Listens to the event fire by an {@link AbstractElement element}
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface ElementEventListener {
    /**
     * Executes a set of actions to update a {@link AbstractUiElement UI element} when a element change
     * @param event Gets the event details
     */
    void onElementEvent(EventDescriptor event);    
}