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

package org.neotropic.kuwaiba.core.apis.integration.views;

import com.vaadin.flow.component.Component;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * A component designed to explore a model structures such as relationships, attachments, 
 * containment hierarchy, etc.Subclasses will behave like factories, just like
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <C> The type of the graphical component that contains the explorer.
 */
public abstract class AbstractExplorerWidget<C extends Component> {
    /**
     * Obtains the name of the explorer. This name will be used as label in lists 
     * and context menus.
     * @return The explorer name.
     */
    public abstract String getName();
    /**
     * Obtains the description of the explorer. This will be used in help texts and 
     * tool tip texts.
     * @return The description of what the explorer does.
     */
    public abstract String getDescription();
    /**
     * Tells the consumer what type of objects the explorer is suitable for. For example, a explorer of 
     * physical connection would be apply to GenericPhysicalConnection and so on.
     * @return The type of object the explorer is intended to.
     */
    public abstract String appliesTo();
    /**
     * Creates an embeddable visual component withe the explorer.
     * @param selectedObject The object from which the explorer will be built.
     * @return The embeddable component.
     */
    public abstract C build(BusinessObjectLight selectedObject);
}
