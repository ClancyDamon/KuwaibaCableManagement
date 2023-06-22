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
package com.neotropic.kuwaiba.modules.commercial.ospman.api;

import java.util.List;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;

/**
 * Collection of methods to manage the selection in the map.
 * @param <T> Item Type.
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface MapSelectionManager<T> {
    /**
     * Performs the necessary actions to select a item in the map. 
     * For example change the background color of the node/edge labels.
     * @param items Items to select.
     */
    void select(T... items);
    /**
     * Performs the necessary actions to deselect a item in the map.
     * For example change the background color of the node/edge labels.
     * @param items Items to deselect.
     */
    void deselect(T... items);
    /**
     * Performs the necessary actions to deselect all the selected items in the map.
     * For example change the background color of the selected node/edge labels.
     */
    void deselectAll();
    /**
     * Gets the first selected item.
     * @return The first selected item.
     */
    T getFirstSelectedItem();
    /**
     * Gets the selected items.
     * @return the selected items.
     */
    List<T> getSelectedItems();
    /**
     * Collection of methods to manage the selection of the nodes in the map.
     */
    public interface NodeSelectionManager extends MapSelectionManager<BusinessObjectViewNode> {
    }
    /**
     * Collection of methods to manage the selection of the edges in the map.
     */
    public interface EdgeSelectionManager extends MapSelectionManager<BusinessObjectViewEdge> {
    }
}
