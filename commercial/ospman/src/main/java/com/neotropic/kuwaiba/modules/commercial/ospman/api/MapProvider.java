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

import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapSelectionManager.EdgeSelectionManager;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapSelectionManager.NodeSelectionManager;
import com.vaadin.flow.component.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;

/**
 * Operations to implement a map in the Outside Plant module.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface MapProvider {
    /**
     * Create a component that represents a map.
     * @param ts The Translation Service
     * @param resourceFactory The resource factory. Used to get node icons
     * @param aem The Application Entity Manager
     * @param mem The Metadata Entity Manager
     */
    void createComponent(ApplicationEntityManager aem, MetadataEntityManager mem, ResourceFactory resourceFactory, TranslationService ts);
    /**
     * Gets a component that represents a map.
     * @return The map provider component.
     */
    Component getComponent();
    /**
     * Gets the map center
     * @return map center
     */
    GeoCoordinate getCenter();
    /**
     * Set map center
     * @param center map center
     */
    void setCenter(GeoCoordinate center);
    /**
     * Gets the map zoom
     * @return map zoom
     */
    double getZoom();
    /**
     * Set the map zoom
     * @param zoom map zoom
     */
    void setZoom(double zoom);
    /**
     * Gets the minimum zoom level for the map when displaying labels.
     * @return The minimum zoom level for the map when displaying labels.
     */
    double getMinZoomForLabels();
    /**
     * Sets the minimum zoom level for the map when displaying labels.
     * @param minZoomForLabels The minimum zoom level for the map when displaying labels.
     */
    void setMinZoomForLabels(double minZoomForLabels);
    /**
     * Gets the map type id.
     * @return The map type id.
     */
    String getMapTypeId();
    /**
     * Sets the map type id.
     * @param mapTypeId The map type id.
     */
    void setMapTypeId(String mapTypeId);
    /**
     * Gets the map type ids.
     * @return The map type ids.
     */
    List<String> getMapTypeIds();
    /**
     * Set the map hand mode.
     */
    void setHandMode();
    /**
     * Sets the drawing mode to marker.
     * @param drawingMarkerComplete Operation that accepts a coordinate
     */
    void setDrawingMarkerMode(Consumer<GeoCoordinate> drawingMarkerComplete);
    /**
     * Sets the drawing mode to edge.
     * @param callbackEdgeComplete Callback to execute when drawing edge complete.
     */
    void setDrawingEdgeMode(BiConsumer<HashMap<String, Object>, Runnable> callbackEdgeComplete);
    /**
     * Sets the path selection mode.
     * @param callbackPathSelectionComplete  Callback to execute when path selection complete.
     */
    void setPathSelectionMode(BiConsumer<List<BusinessObjectViewEdge>, Runnable> callbackPathSelectionComplete);
    /**
     * Sets the measure mode.
     */
    void setMeasureMode();
    /**
     * Gets if compute edges length.
     * @return If compute edges length.
     */
    boolean getComputeEdgesLength();
    /**
     * Sets if compute edges length.
     * @param computeEdgesLength True to compute edges length.
     */
    void setComputeEdgesLength(boolean computeEdgesLength);
    /**
     * Gets the unit of length of map.
     * @return The unit of length of map.
     */
    UnitOfLength getUnitOfLength();
    /**
     * Sets the unit of length of map.
     * @param unitOfLength Unit of length of map.
     */
    void setUnitOfLength(UnitOfLength unitOfLength);
    /**
     * Gets the map node selection manager.
     * @return The nodes selection manager.
     */
    NodeSelectionManager getNodeSelectionManager();
    /**
     * Gets the map edge selection manager.
     * @return The edges selection manager.
     */
    EdgeSelectionManager getEdgeSelectionManager();
    /**
     * Adds an idle event listener.
     * @param listener Callback executed when idle.
     */
    void addIdleEventListener(IdleEvent.IdleEventListener listener);
    /**
     * Removes an idle event listener.
     * @param listener Callback executed when idle.
     */
    void removeIdleEventListener(IdleEvent.IdleEventListener listener);
    /**
     * Removes all idle event listener.
     */
    void removeAllIdleEventListener();
    /**
     * Adds a zoom changed event listener.
     * @param listener
     */
    void addZoomChangedEventListener(ZoomChangedEvent.ZoomChangedEventListener listener);
    /**
     * Removes a zoom changed event listener.
     * @param listener
     */
    void removeZoomChangedEventListener(ZoomChangedEvent.ZoomChangedEventListener listener);
    /**
     * Removes all zoom changed event listener.
     */
    void removeAllsZoomChangedEventListener();
    /**
     * Adds a mouse move event listener.
     * @param listener
     */
    void addMouseMoveEventListener(MouseMoveEvent.MouseMoveEventListener listener);
    /**
     * Removes a mouse move event listener.
     * @param listener
     */
    void removeMouseMoveEventListener(MouseMoveEvent.MouseMoveEventListener listener);
    /**
     * Removes all mouse move event listener.
     */
    void removeAllMouseMoveEventListener();
    /**
     * Adds a right click event listener.
     * @param listener
     */
    void addRightClickEventListener(RightClickEvent.RightClickEventListener listener);
    /**
     * Removes a right click event listener.
     * @param listener
     */
    void removeRightClickEventListener(RightClickEvent.RightClickEventListener listener);
    /**
     * Removes all right click event listener.
     */
    void removeAllRightClickEventListener();
    /**
     * Executes callback to calculate whether the given coordinate exist inside the specified path.
     * @param coordinate Coordinate to calculate if exist inside the specified path.
     * @param paths Polygon paths.
     * @param callback Callback to execute.
     */
//    void callbackContainsLocation(GeoCoordinate coordinate, List<List<GeoCoordinate>> paths, Consumer<Boolean> callback);
    /**
     * Executes callback to calculate whether the given coordinates exist inside the specified path.
     * @param coordinates Coordinates to calculate if exist inside the specified path.
     * @param paths Polygon paths.
     * @param callback Callback to execute.
     */
//    void callbackContainsLocations(HashMap<String, GeoCoordinate> coordinates, List<List<GeoCoordinate>> paths, Consumer<HashMap<String, Boolean>> callback);
    /**
     * Adds a node to map.
     * @param viewNode Node to add to map.
     * @return
     */
    MapNode addNode(BusinessObjectViewNode viewNode);
    /**
     * Adds an edge to map.
     * @param viewEdge Edge to add to map.
     * @return
     */
    MapEdge addEdge(BusinessObjectViewEdge viewEdge);
    /**
     * Removes a node to map.
     * @param viewNode Node to remove from map.
     */
    void removeNode(BusinessObjectViewNode viewNode);
    /**
     * Removes an edge to map.
     * @param viewEdge Edge to remove from map.
     */
    void removeEdge(BusinessObjectViewEdge viewEdge);
    /**
     * Adds a heatmap to be displayed by the map provider.
     * @param viewHeatmap The heatmap to be displayed by the map provider.
     * @return The heatmap displayed by the map provider.
     */
    Heatmap addHeatmap(ViewHeatmap viewHeatmap);
    /**
     * Removes a heatmap displayed by the map provider.
     * @param viewHeatmap The heatmap displayed by the map provider.
     */
    void removeHeatmap(ViewHeatmap viewHeatmap);
    /**
     * Adds a view overlay view to be displayed by the ma provider.
     * @param viewOverlayView The view overlay view to be displayed by the map provider.
     * @return The overlay view displayed by the map provider.
     */
//    MapOverlayView addOverlayView(ViewOverlayView viewOverlayView);
    /**
     * Removes a view overlay view displayed by the map provider.
     * @param viewOverlayView The view overlay view displayed by the map provider.
     */
//    void removeOverlayView(ViewOverlayView viewOverlayView);
    /**
     * Callback executed when idle.
     */
    public class IdleEvent {
        /**
         * Consumer accepted on idle event.
         */
        public interface IdleEventListener extends Consumer<IdleEvent> {
        }
        private final IdleEventListener listener;
        
        public IdleEvent(IdleEventListener listener) {
            Objects.requireNonNull(listener);
            this.listener = listener;
        }
        public IdleEventListener getListener() {
            return listener;
        }
    }
    /**
     * Map zoom change event
     */
    public class ZoomChangedEvent {
        /**
         * Consumer accepted on zoom changed event.
         */
        public interface ZoomChangedEventListener extends Consumer<ZoomChangedEvent> {
        }
        private final ZoomChangedEventListener listener;
        
        public ZoomChangedEvent(ZoomChangedEventListener listener) {
            Objects.requireNonNull(listener);
            this.listener = listener;
        }
        public ZoomChangedEventListener getListener() {
            return listener;
        }
    }
    /**
     * Map mouse move event.
     */
    public class MouseMoveEvent {
        /**
         * Consumer accepted on mouse move event.
         */
        public interface MouseMoveEventListener extends Consumer<MouseMoveEvent> {
        }
        private final MouseMoveEventListener listener;
        private final double lat;
        private final double lng;
        
        public MouseMoveEvent(MouseMoveEventListener listener, double lat, double lng) {
            Objects.requireNonNull(listener);
            this.listener = listener;
            this.lat = lat;
            this.lng = lng;
        }
        public MouseMoveEventListener getListener() {
            return listener;
        }
        public double getLat() {
            return lat;
        }
        public double getLng() {
            return lng;
        }
    }
    /**
     * Map right click event.
     */
    public class RightClickEvent {
        /**
         * Consumer accepted on right click event.
         */
        public interface RightClickEventListener extends Consumer<RightClickEvent> {
        }
        private final RightClickEventListener listener;
        private final double lat;
        private final double lng;
        
        public RightClickEvent(RightClickEventListener listener, double lat, double lng) {
            this.listener = listener;
            this.lat = lat;
            this.lng = lng;
        }
        public RightClickEventListener getListener() {
            return listener;
        }
        public double getLat() {
            return lat;
        }
        public double getLng() {
            return lng;
        }
    }
}
