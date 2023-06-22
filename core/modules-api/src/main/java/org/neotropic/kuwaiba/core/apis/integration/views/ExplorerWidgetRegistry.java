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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The registry where explorers provided by different modules are registered so
 * -among other things- 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class ExplorerWidgetRegistry {
    /**
     * The list of all registered explorers.
     */
    private List<AbstractExplorerWidget> explorerWidgets;
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    
    public ExplorerWidgetRegistry() {
        this.explorerWidgets = new ArrayList<>();
    }
    
    public void registerWidget(AbstractExplorerWidget widget) {
        this.explorerWidgets.add(widget);
    }
    
    public List<AbstractExplorerWidget> getExplorerWidgets() {
        return this.explorerWidgets;
    }
    
    /**
     * Checks what object-related explorer are associated to a given inventory class. For example, 
     * a connections explorer to instances of class Rack.
     * @param filter The class to be evaluated.
     * @return The actions that can be executed from an instance of the given class or superclass.
     */
    public List<AbstractExplorerWidget> getExplorerWidgetsApplicableToRecursive(String filter) {
        return this.explorerWidgets.stream().filter((aWidget) -> {
            try {
                return aWidget.appliesTo() == null ? true : mem.isSubclassOf(aWidget.appliesTo(), filter); // Null means any inventory object
            } catch (MetadataObjectNotFoundException ex) { // No existing (or cached) classes will be ignored
                return false;
            }
        }).collect(Collectors.toList());
    }
}
