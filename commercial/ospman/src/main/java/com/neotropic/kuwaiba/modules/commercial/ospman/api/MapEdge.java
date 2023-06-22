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
import java.util.function.Consumer;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;

/**
 * Edge to add in the Outside Plant View.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface MapEdge {
    /**
     * Gets the view edge.
     * @return the view edge.
     */
    BusinessObjectViewEdge getViewEdge();
    /**
     * Gets the edge label.
     * @return Edge label.
     */
    String getEdgeLabel();
    /**
     * Sets the edge label.
     * @param label Edge label.
     */
    void setEdgeLabel(String label);
    /**
     * Sets the edge control points.
     * @param controlPoints The edge control points.
     */
    void setControlPoints(List<GeoCoordinate> controlPoints);
    /**
     * Gets if the edge can receives mouse events.
     * @return If true, the edge can receives mouse events.
     */
    boolean getClickableEdge();
    /**
     * Sets if the edge can receives mouse events.
     * @param clickable True to receives mouse events.
     */
    void setClickableEdge(boolean clickable);
    /**
     * Gets if the edge can be edited.
     * @return If true, the edge can be edited.
     */
    boolean getEditableEdge();
    /**
     * Sets if the edge can be edited.
     * @param editable True to edit the edge.
     */
    void setEditableEdge(boolean editable);
    /**
     * Gets if animation is played.
     * @return If animation is played.
     */
    boolean getPlayAnimation();
    /**
     * Sets true to play animation.
     * @param playAnimation True to play animation.
     */
    void setPlayAnimation(boolean playAnimation);
    /**
     * Gets the label position.
     * @return The label position.
     */
    void getEdgeLabelPosition(Consumer<GeoCoordinate> consumerEdgeLabelPosition);
    /**
     * If true, the edge is visible.
     * @return If true, the edge is visible.
     */
    boolean getEdgeVisible();
    /**
     * If true, the edge is visible.
     * @param visible If true, the edge is visible.
     */
    void setEdgeVisible(boolean visible);
    /**
     * Gets the edge length.
     * @return The edge length.
     */
    Double getLength();
    /**
     * Sets the edge length.
     */
    void setLength(Double length);
    /**
     * Computes edge length.
     * @param consumerLength Accepted on compute length end.
     * @param controlPoints Set of control points.
     */
    void computeLength(List<GeoCoordinate> controlPoints, Consumer<Double> consumerLength);
    /**
     * Adds a click event listener.
     * @param clickEventListener Callback executed on edge click.
     */
    void addClickEventListener(ClickEvent.ClickEventListener clickEventListener);
    /**
     * Removes a click event listener.
     * @param clickEventListener Callback executed on edge click.
     */
    void removeClickEventListener(ClickEvent.ClickEventListener clickEventListener);
    /**
     * Removes all click event listener.
     */
    void removeAllClickEventListeners();
    /**
     * Adds a right click event listener.
     * @param rightClickEventListener Callback executed on edge right click.
     */
    void addRightClickEventListener(RightClickEvent.RightClickEventListener rightClickEventListener);
    /**
     * Removes a right click event listener.
     * @param rightClickEventListener Callback executed on edge right click.
     */
    void removeRightClickEventListener(RightClickEvent.RightClickEventListener rightClickEventListener);
    /**
     * Removes all right click event listener.
     */
    void removeAllRightClickEventListeners();
    /**
     * Adds a path changed event listener.
     * @param pathChangedEventListener Callback executed on edge path changed.
     */
    void addPathChangedEventListener(PathChangedEvent.PathChangedEventListener pathChangedEventListener);
    /**
     * Removes a path changed event listener.
     * @param pathChangedEventListener Callback executed on edge path changed.
     */
    void removePathChangedEventListener(PathChangedEvent.PathChangedEventListener pathChangedEventListener);
    /**
     * Removes all path changed event listener.
     */
    void removeAllPathChangedEventListeners();
}
