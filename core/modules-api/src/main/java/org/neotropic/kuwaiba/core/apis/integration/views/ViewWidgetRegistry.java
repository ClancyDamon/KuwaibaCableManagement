/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Widgets with object-related views exposed by modules are to be registered here, so they can be embedded or added to context menus at will.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class ViewWidgetRegistry {
    /**
     * The list of registered object-related widgets.
     */
    private List<AbstractObjectRelatedViewWidget> widgets;
    /**
     * Reference to the MetadataEntityManager to access the data model cache.
     */
    @Autowired
    private MetadataEntityManager mem;
    
    public ViewWidgetRegistry() {
        this.widgets = new ArrayList<>();
    }
    
    /**
     * Checks what object-related widgets are associated to a given inventory class. For example, 
     * ObjectView to any ViewableObject or RackView to Rack.
     * @param filter The class to be evaluated.
     * @return The actions that can be executed from an instance of the given class or superclass.
     */
    public List<AbstractObjectRelatedViewWidget> getViewWidgetsApplicableTo(String filter) {
        return this.widgets.stream().filter((aWidget) -> {
            try {
                return aWidget.appliesTo() == null ? true : mem.isSubclassOf(aWidget.appliesTo(), filter); // Null means any inventory object
            } catch (MetadataObjectNotFoundException ex) { // No existing (or cached) classes will be ignored
                return false;
            }
        }).collect(Collectors.toList());
    }
    
    /**
     * Adds a object-related widget to the registry. This method also feeds the detailed view map cache structure, which is a hash map which keys are 
     * all the possible super classes the detailed views are applicable to and the keys are the corresponding detailed views.
     * @param widget The widget to be added. Duplicated view ids are not allowed.
     */
    public void registerWidget(AbstractObjectRelatedViewWidget widget) {
        this.widgets.add(widget);
    }
    
    /**
     * Returns all registered widgets.
     * @return All registered widgets.
     */
    public List<AbstractObjectRelatedViewWidget> getViewWidgets() {
        return this.widgets;
    }
}
