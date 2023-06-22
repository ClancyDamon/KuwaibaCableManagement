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

import com.neotropic.flow.component.googlemap.GoogleMap;
import com.neotropic.flow.component.googlemap.GoogleMapPolyline;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;

/**
 * Path selection helper. Paint edges that follow the path.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PathSelectionHelper {
    private final GoogleMap googleMap;
    private final Collection<GoogleMapNode> nodes;
    private final Collection<GoogleMapEdge> edges;
    private final HashMap<BusinessObjectViewEdge, GoogleMapPolyline> path = new HashMap();
    private final List<Registration> clickRegistrations = new ArrayList();
    private BiConsumer<List<BusinessObjectViewEdge>, Runnable> callbackPathSelectionComplete;
    
    protected PathSelectionHelper(
        BiConsumer<List<BusinessObjectViewEdge>, Runnable> callbackPathSelectionComplete,
        GoogleMap googleMap,
        Collection<GoogleMapNode> nodes, 
        Collection<GoogleMapEdge> edges) {
        
        Objects.requireNonNull(callbackPathSelectionComplete);
        Objects.requireNonNull(googleMap);
        Objects.requireNonNull(nodes);
        Objects.requireNonNull(edges);
        
        this.callbackPathSelectionComplete = callbackPathSelectionComplete;
        this.googleMap = googleMap;
        this.nodes = nodes;
        this.edges = edges;
    }
    
    public void setCallbackPathSelectionComplete(BiConsumer<List<BusinessObjectViewEdge>, Runnable> callbackPathSelectionComplete) {
        this.callbackPathSelectionComplete = callbackPathSelectionComplete;
    }
    
    public void init() {
        googleMap.setClickableIcons(false);
        nodes.forEach(node -> {
            node.setDraggable(false);
            node.setClickable(false);
        });
        edges.forEach(edge -> {
            edge.setEditable(false);
            edge.setClickableEdge(false);            
            
            Registration clickRegistration = edge.addPolylineClickListener(event -> {
                if (!path.containsKey(edge.getViewEdge())) {
                    edge.setPolylineVisible(false);
                    
                    GoogleMapPolyline tmpPolyline = new GoogleMapPolyline();
                    tmpPolyline.setStrokeWeight(OspConstants.EDGE_STROKE_WEIGHT);
                    path.put(edge.getViewEdge(), tmpPolyline);
                    
                    tmpPolyline.setPath(edge.getPath());
                    tmpPolyline.addPolylineMouseOverListener(mouseOverEvent -> tmpPolyline.setStrokeWeight(OspConstants.EDGE_STROKE_WEIGHT_MOUSE_OVER));
                    tmpPolyline.addPolylineMouseOutListener(mouseOutEvent -> tmpPolyline.setStrokeWeight(OspConstants.EDGE_STROKE_WEIGHT));
                    tmpPolyline.addPolylineClickListener(ClickEvent -> {
                        edge.setPolylineVisible(true);
                        googleMap.removePolyline(path.remove(edge.getViewEdge()));
                    });
                    tmpPolyline.addPolylineRightClickListener(rightClickEvent -> 
                        callbackPathSelectionComplete.accept(new ArrayList(path.keySet()), () -> {
                            cancel();
                            init();
                        })
                    );
                    googleMap.newPolyline(tmpPolyline);
                }
            });
            clickRegistrations.add(clickRegistration);
        });
    }
    
    public void cancel() {
        googleMap.setClickableIcons(true);
        
        nodes.forEach(node -> {
            node.setDraggable(true);
            node.setClickable(true);
        });
        
        clickRegistrations.forEach(clickRegistration -> clickRegistration.remove());
        clickRegistrations.clear();
        
        path.values().forEach(tmpPolyline -> googleMap.removePolyline(tmpPolyline));
        path.clear();
        
        edges.forEach(edge -> {
            edge.setPolylineVisible(true);
            edge.setClickableEdge(true);
        });
    }
}
