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
package com.neotropic.kuwaiba.modules.commercial.ospman.providers.google;

import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapSelectionManager.NodeSelectionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;

/**
 * Selection manager to nodes in Google Map.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapsNodeSelectionManager implements NodeSelectionManager {
    private final HashMap<BusinessObjectViewNode, GoogleMapNode> nodes;
    
    private final List<BusinessObjectViewNode> selectedItems = new ArrayList();
    
    public GoogleMapsNodeSelectionManager(HashMap<BusinessObjectViewNode, GoogleMapNode> nodes) {
        this.nodes = nodes;
    }
    
    @Override
    public void select(BusinessObjectViewNode... items) {
        for (BusinessObjectViewNode item : items) {
            if(nodes.containsKey(item)) {
                selectedItems.add(item);
                // Changing the background color of the node label
                nodes.get(item).setLabelClassName(
                    GoogleMapsMapProvider.LABEL_CLASS_NAME_FOR_SELECTED_MARKERS
                );
            }
            else
                deselect(items);
        }
    }

    @Override
    public void deselect(BusinessObjectViewNode... items) {
        for (BusinessObjectViewNode item : items) {
            if (nodes.containsKey(item)) {
                // Changing the background color of the node label
                nodes.get(item).setLabelClassName(
                    GoogleMapsMapProvider.LABEL_CLASS_NAME_FOR_MARKERS
                );
            }
            selectedItems.remove(item);
        }
    }
    
    @Override
    public void deselectAll() {
        deselect(selectedItems.toArray(new BusinessObjectViewNode[0]));
    }
    
    @Override
    public BusinessObjectViewNode getFirstSelectedItem() {
        return !selectedItems.isEmpty() ? selectedItems.get(0) : null;
    }
    
    @Override
    public List<BusinessObjectViewNode> getSelectedItems() {
        return selectedItems;
    }
    
}
