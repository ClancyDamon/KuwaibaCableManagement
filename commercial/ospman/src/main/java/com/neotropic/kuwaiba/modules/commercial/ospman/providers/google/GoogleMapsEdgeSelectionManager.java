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

import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapSelectionManager.EdgeSelectionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;

/**
 * Selection manager to edges in Google Map
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapsEdgeSelectionManager implements EdgeSelectionManager {
    
    private final List<BusinessObjectViewEdge> selectedItems = new ArrayList();
    private final HashMap<BusinessObjectViewEdge, GoogleMapEdge> edges;
    
    public GoogleMapsEdgeSelectionManager(HashMap<BusinessObjectViewEdge, GoogleMapEdge> edges) {
        this.edges = edges;
    }
    @Override
    public void select(BusinessObjectViewEdge... items) {
        for (BusinessObjectViewEdge item : items) {
            if (edges.containsKey(item)) {
                selectedItems.add(item);
                // Changing the background color of the edge label
                edges.get(item).setLabelClassName(
                    GoogleMapsMapProvider.LABEL_CLASS_NAME_FOR_SELECTED_POLYLINES
                );
            }
            else
                deselect(item);
        }
    }

    @Override
    public void deselect(BusinessObjectViewEdge... items) {
        for (BusinessObjectViewEdge item : items) {
            if (edges.containsKey(item)) {
                // Changing the background color of the edge label
                edges.get(item).setLabelClassName(
                    GoogleMapsMapProvider.LABEL_CLASS_NAME_FOR_POLYLINES
                );
            }
            selectedItems.remove(item);
        }
    }

    @Override
    public void deselectAll() {
        deselect(selectedItems.toArray(new BusinessObjectViewEdge[0]));
    }

    @Override
    public BusinessObjectViewEdge getFirstSelectedItem() {
        return !selectedItems.isEmpty() ? selectedItems.get(0) : null;
    }
    
    @Override
    public List<BusinessObjectViewEdge> getSelectedItems() {
        return selectedItems;
    }
    
}
