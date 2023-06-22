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

import com.neotropic.flow.component.googlemap.Animation;
import com.neotropic.flow.component.googlemap.GoogleMapPolyline;
import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.ClickEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.PathChangedEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.RightClickEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.UnitOfLength;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 * An edge wrapper to Google Map Polyline
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapEdge extends GoogleMapPolyline implements MapEdge {
    private final BusinessObjectViewEdge viewEdge;
    private boolean clickable = true;
    private final TranslationService ts;
    private final MapProvider mapProvider;
    
    private Double length;
    
    private final List<ClickEvent.ClickEventListener> clickEventListeners = new ArrayList();
    private final List<RightClickEvent.RightClickEventListener> rightClickEventListeners = new ArrayList();
    private final List<PathChangedEvent.PathChangedEventListener> pathChangedEventListeners = new ArrayList();
        
    public GoogleMapEdge(BusinessObjectViewEdge viewEdge, MetadataEntityManager mem, TranslationService ts, MapProvider mapProvider) {
        Objects.requireNonNull(viewEdge);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(mapProvider);
        
        this.viewEdge = viewEdge;
        this.ts = ts;
        this.mapProvider = mapProvider;
        
        List<GeoCoordinate> controlPoints = (List) viewEdge.getProperties().get(OspConstants.PROPERTY_CONTROL_POINTS);
        
        if (controlPoints != null && !controlPoints.isEmpty()) {
            try {
                setStrokeColor(UtilHtml.toHexString(new Color(mem.getClass(viewEdge.getIdentifier().getClassName()).getColor())));
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
            List<LatLng> path = new ArrayList();
            controlPoints.forEach(controlPoint -> 
                path.add(new LatLng(controlPoint.getLatitude(), controlPoint.getLongitude()))
            );
            if (mapProvider.getZoom() >= mapProvider.getMinZoomForLabels()) {
                if (mapProvider.getComputeEdgesLength()) {
                    
                    computeLength(controlPoints, length -> {
                        this.length = length;
                        setEdgeLabel(getFormattedLabel());
                    });
                }
                else
                    setLabel(getFormattedLabel());
            }
            
            setPath(path);
            setStrokeWeight(OspConstants.EDGE_STROKE_WEIGHT);
            setLabelIconUrl("marker.png"); //NOI18N
            
            addPolylineMouseOverListener(event -> setStrokeWeight(OspConstants.EDGE_STROKE_WEIGHT_MOUSE_OVER));
            addPolylineMouseOutListener(event -> setStrokeWeight(OspConstants.EDGE_STROKE_WEIGHT));
            
            addPolylinePathChangedListener(event -> {
                List<GeoCoordinate> geoCoordinates = new ArrayList();
                getPath().forEach(latLng -> geoCoordinates.add(
                    new GeoCoordinate(latLng.getLat(), latLng.getLng()))
                );
                new ArrayList<>(pathChangedEventListeners).forEach(listener -> {
                    if (pathChangedEventListeners.contains(listener))
                        listener.accept(new PathChangedEvent(geoCoordinates, listener));
                });
            });
            addPolylineClickListener(event -> new ArrayList<>(clickEventListeners).forEach(listener -> {
                if (clickable && clickEventListeners.contains(listener))
                    listener.accept(new ClickEvent(listener));
            }));
            addPolylineRightClickListener(event -> new ArrayList<>(rightClickEventListeners).forEach(listener -> {
                if (clickable && rightClickEventListeners.contains(listener))
                    listener.accept(new RightClickEvent(listener));
            }));
            addVertexRightClickListener(event -> {
                List<LatLng> edgePath = getPath();
                edgePath.remove((int) event.getVertex());
                setPath(edgePath);
                
                List<GeoCoordinate> geoCoordinates = new ArrayList();
                edgePath.forEach(latLng -> geoCoordinates.add(
                    new GeoCoordinate(latLng.getLat(), latLng.getLng()))
                );
                viewEdge.getProperties().put(OspConstants.PROPERTY_CONTROL_POINTS, geoCoordinates);
                
                if (mapProvider.getComputeEdgesLength()) {
                    computeLength(geoCoordinates, length -> {
                        this.length = length;
                        
                        if (mapProvider.getZoom() >= mapProvider.getMinZoomForLabels())
                            setEdgeLabel(getFormattedLabel());
                    });
                }
            });
        }
    
    }
    
    @Override
    public BusinessObjectViewEdge getViewEdge() {
        return viewEdge;
    }
    
    @Override
    public String getEdgeLabel() {
        return getLabel();
    }
    
    @Override
    public void setEdgeLabel(String label) {
        if (mapProvider.getComputeEdgesLength() && label != null) {
            if (length == null) {
                List<GeoCoordinate> controlPoints = (List) viewEdge.getProperties().get(OspConstants.PROPERTY_CONTROL_POINTS);
                if (controlPoints != null && !controlPoints.isEmpty()) {
                    computeLength(controlPoints, length -> {
                        this.length = length;
                        
                        if (mapProvider.getZoom() >= mapProvider.getMinZoomForLabels())
                            setEdgeLabel(getFormattedLabel());
                    });
                }
            } else {
                setLabel(String.format(
                    ts.getTranslatedString("module.ospman.map.edge.length"), 
                    getFormattedLabel(), 
                    UnitOfLength.convertMeters(length, mapProvider.getUnitOfLength()), 
                    UnitOfLength.getTranslatedString(mapProvider.getUnitOfLength(), ts)
                ));
            }
        }
        else
            setLabel(getFormattedLabel());
    }
    
    @Override
    public void setControlPoints(List<GeoCoordinate> controlPoints) {
        List<LatLng> path = new ArrayList();
        if (controlPoints != null) {
            controlPoints.forEach(controlPoint -> 
                path.add(new LatLng(controlPoint.getLatitude(), controlPoint.getLongitude()))
            );
        }
        setPath(path);
    }
    
    @Override
    public boolean getClickableEdge() {
        return clickable;
    }
    
    @Override
    public void setClickableEdge(boolean clickable) {
        this.clickable = clickable;
    }
    
    @Override
    public boolean getEditableEdge() {
        return getEditable();
    }
    
    @Override
    public void setEditableEdge(boolean editable) {
        setEditable(editable);
    }
    
    @Override
    public boolean getPlayAnimation() {
        return getLabelAnimation() != null;
    }
    
    @Override
    public void setPlayAnimation(boolean playAnimation) {
        setLabelAnimation(playAnimation ? Animation.BOUNCE : null);
    }
    
    @Override
    public void getEdgeLabelPosition(Consumer<GeoCoordinate> consumerEdgeLabelPosition) {
        LatLng latLng = getLabelPosition();
        GeoCoordinate geoCoordinate = new GeoCoordinate(latLng.getLat(), latLng.getLng());
        consumerEdgeLabelPosition.accept(geoCoordinate);
    }
    
    @Override
    public boolean getEdgeVisible() {
        return getPolylineVisible();
    }
    
    @Override
    public void setEdgeVisible(boolean visible) {
        setPolylineVisible(visible);
    }
    
    @Override
    public Double getLength() {
        return length;
    }
    
    @Override
    public void setLength(Double length) {
        this.length = length;
    }
    
    @Override
    public void computeLength(List<GeoCoordinate> controlPoints, Consumer<Double> consumerLength) {
        Objects.requireNonNull(controlPoints);
        Objects.requireNonNull(consumerLength);
        
        JsonArray path = Json.createArray();
        for (int i = 0; i < controlPoints.size(); i++) {
            
            JsonObject latLng = Json.createObject();
            latLng.put("lat", controlPoints.get(i).getLatitude());
            latLng.put("lng", controlPoints.get(i).getLongitude());
            
            path.set(i, latLng);
        }
        StringBuilder expressionBuilder = new StringBuilder();
        expressionBuilder.append("var p = [];");
        expressionBuilder.append("$0.forEach(element => p.push(new google.maps.LatLng(element.lat, element.lng)));");
        expressionBuilder.append("return google.maps.geometry.spherical.computeLength(p);");
        
        getElement()
            .executeJs(expressionBuilder.toString(), path)
            .then(Double.class, length -> consumerLength.accept(length));
    }
    
    @Override
    public void addClickEventListener(ClickEvent.ClickEventListener clickEventListener) {
        clickEventListeners.add(clickEventListener);
    }

    @Override
    public void removeClickEventListener(ClickEvent.ClickEventListener clickEventListener) {
        clickEventListeners.removeIf(l -> l.equals(clickEventListener));
    }

    @Override
    public void removeAllClickEventListeners() {
        clickEventListeners.clear();
    }

    @Override
    public void addRightClickEventListener(RightClickEvent.RightClickEventListener rightClickEventListener) {
        rightClickEventListeners.add(rightClickEventListener);
    }

    @Override
    public void removeRightClickEventListener(RightClickEvent.RightClickEventListener rightClickEventListener) {
        rightClickEventListeners.removeIf(l -> l.equals(rightClickEventListener));
    }

    @Override
    public void removeAllRightClickEventListeners() {
        rightClickEventListeners.clear();
    }
    
    @Override            
    public void addPathChangedEventListener(PathChangedEvent.PathChangedEventListener pathChangedEventListener) {
        pathChangedEventListeners.add(pathChangedEventListener);
    }
    
    @Override
    public void removePathChangedEventListener(PathChangedEvent.PathChangedEventListener pathChangedEventListener) {
        pathChangedEventListeners.removeIf(l -> l.equals(pathChangedEventListener));
    }
    
    @Override
    public void removeAllPathChangedEventListeners() {
        pathChangedEventListeners.clear();
    }
    
    private String getFormattedLabel() {
        BusinessObjectLight businessObject = getViewEdge().getIdentifier();
        if (businessObject.getValidators() != null) {
            StringBuilder prefixBuilder = new StringBuilder();
            StringBuilder suffixBuilder = new StringBuilder();

            businessObject.getValidators().forEach(validator -> {
                if (validator.getProperties() != null) {
                    if (validator.getProperties().containsKey(Validator.PROPERTY_PREFIX))
                        prefixBuilder.append(String.format("%s ", validator.getProperties().getProperty(Validator.PROPERTY_PREFIX)));
                    if (validator.getProperties().containsKey(Validator.PROPERTY_SUFFIX))
                        suffixBuilder.append(String.format("%s ", validator.getProperties().getProperty(Validator.PROPERTY_SUFFIX)));
                }
            });
            return String.format("%s%s %s", prefixBuilder.toString(), businessObject.getName(), suffixBuilder.toString());
        }
        return businessObject.getName();
    }
}
