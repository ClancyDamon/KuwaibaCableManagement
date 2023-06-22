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
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;

/**
 * A view widget is composed by a view (a canvas where the graphical elements are drawn) and 
 * a container to the scene that provides tools to interact with the 
 * elements on it (create connections, launch reports, actions, etc). Subclasses of AbstractObjectRelatedViewWidget are 
 * special kind of views that are always built from a business object. They work as factories as
 * they are registered at application startup so they can be embedded dynamically in context menus or 
 * action lists, and when the user actions occurs, it builds on the fly a component that can be embedded in the page.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <C> The visual component 
 */
public abstract class AbstractObjectRelatedViewWidget<C extends Component> {
    /**
     * The name of the view. This might not be important for hard-coded views, but it is relevant in custom, scripted views.
     * @return A short display name of the view
     */
    public abstract String getName();
    /**
     * More details on what the view does. This might not be important for hard-coded views, but it is relevant in custom, scripted views.
     * @return The view description.
     */
    public abstract String getDescription();
    /**
     * The current version of the view.  This might not be important for hard-coded views, but it is relevant in custom, scripted views.
     * @return The version of the view.
     */
    public abstract String getVersion();
    /**
     * Who wrote the view.
     * @return A string with the name of the creator of the view, and preferably a way to contact it.
     */
    public abstract String getVendor();
    /**
     * The factory method that builds a view (a scene + container with tools).
     * @param businessObject The object used as input to build the view.
     * @return A visual component that can be embedded in dashboards and UIs.
     * @throws InventoryException In case the process of building the view raises an exception while 
     * interacting with the persistence service.
     */
    public abstract C build(BusinessObjectLight businessObject) throws InventoryException;
    /**
     * Indicates what inventory objects can be represented graphically using the toolkit.
     * @return The class of the instances.
     */
    public abstract String appliesTo();
    /**
     * The title of the view.
     * @return A short display title of the view
     */
    public abstract String getTitle();
}
