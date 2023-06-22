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
package com.neotropic.kuwaiba.modules.commercial.ospman.providers.ol;

import com.neotropic.flow.component.olmap.Feature;
import com.neotropic.flow.component.olmap.OlMap;
import com.neotropic.flow.component.olmap.VectorSource;
import com.neotropic.flow.component.olmap.interaction.Modify;
import com.neotropic.flow.component.olmap.interaction.Select;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.RightClickEvent.RightClickEventListener;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;

/**
 * Path selection helper. Paint edges that follow the path.
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OlPathSelectionHelper {

    private final OlMap olMap;
    private final Modify modify;
    private final Select select;
    private final VectorSource vectorSource;
    private Registration registration;
    private BiConsumer<List<BusinessObjectViewEdge>, Runnable> consumerPathSelectionComplete;
    private final List<FeatureEdge> path = new ArrayList();
    private final HashMap<FeatureEdge, RightClickEventListener> listeners = new HashMap();

    public OlPathSelectionHelper(OlMap olMap, Modify modify, Select select, VectorSource vectorSource) {
        this.olMap = olMap;
        this.modify = modify;
        this.select = select;
        this.vectorSource = vectorSource;

    }

    public void setConsumerPathSelectionComplete(BiConsumer<List<BusinessObjectViewEdge>, Runnable> consumerPathSelectionComplete) {
        this.consumerPathSelectionComplete = consumerPathSelectionComplete;
    }

    public void init() {
        cancel();
        modify.setActive(false);
        olMap.updateInteraction(modify);
        registration = select.addSelectListener(event -> {
            event.getFeatureSelectedIds().forEach(featureSelectedId -> {
                Feature feature = vectorSource.getFeatureById(featureSelectedId);
                if (feature instanceof FeatureEdge) {
                    FeatureEdge featureEdge = (FeatureEdge) feature;
                    if (!path.contains(featureEdge)) {
                        featureEdge.getStroke().setColor("red");
                        vectorSource.updateFeature(featureEdge);
                        
                        path.add(featureEdge);
                        RightClickEventListener listener = rightClickEvent -> {
                            List<BusinessObjectViewEdge> edges = new ArrayList();
                            path.forEach(item -> edges.add(item.getViewEdge()));

                            consumerPathSelectionComplete.accept(edges, () -> init());
                        };
                        featureEdge.addRightClickEventListener(listener);
                        listeners.put(featureEdge, listener);
                    } else {
                        featureEdge.getStroke().setColor(featureEdge.getColor());
                        vectorSource.updateFeature(featureEdge);
                        
                        featureEdge.removeRightClickEventListener(listeners.remove(featureEdge));
                        path.remove(featureEdge);
                    }
                }
            });
        });

    }

    public void cancel() {
        modify.setActive(true);
        olMap.updateInteraction(modify);

        if (registration != null) {
            registration.remove();
        }
        registration = null;
        
        listeners.forEach((key, value) -> key.removeRightClickEventListener(value));
        listeners.clear();
        
        path.forEach(featureEdge -> {
            featureEdge.getStroke().setColor(featureEdge.getColor());
            vectorSource.updateFeature(featureEdge);
        });
        path.clear();
    }
}
