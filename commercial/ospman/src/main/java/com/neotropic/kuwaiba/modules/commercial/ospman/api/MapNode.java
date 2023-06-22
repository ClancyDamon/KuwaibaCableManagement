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

import com.neotropic.kuwaiba.modules.commercial.ospman.api.ClickEvent.ClickEventListener;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.PositionChangedEvent.PositionChangedEventListener;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.RightClickEvent.RightClickEventListener;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;

/**
 * Node to add in the Outside Plant View.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface MapNode {
    /**
     * Gets the view node.
     * @return the view node.
     */
    BusinessObjectViewNode getViewNode();
    /**
     * Sets the node position.
     * @param position Node position.
     */
    void setPosition(GeoCoordinate position);
    /**
     * Gets the node label.
     * @return Node label.
     */
    String getNodeLabel();
    /**
     * Sets the node label.
     * @param label Node label.
     */
    void setNodeLabel(String label);
    /**
     * Gets the node title.
     * @return Node title.
     */
    String getNodeTitle();
    /**
     * Sets the node title.
     * @param title Node title.
     */
    void setNodeTitle(String title);
    /**
     * Gets if the node can receives mouse events.
     * @return If true, the node can receives mouse events.
     */
    boolean getClickableNode();
    /**
     * Sets if the node can receives mouse events.
     * @param clickable True to receives mouse events.
     */
    void setClickableNode(boolean clickable);
    /**
     * Gets if the node can be dragged.
     * @return If true, the node can be dragged.
     */
    boolean getDraggableNode();
    /**
     * Sets if the node can be dragged.
     * @param draggable True to drag the node.
     */
    void setDraggableNode(boolean draggable);
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
     * If true, the node is visible.
     * @return If true, the node is visible.
     */
    boolean getNodeVisible();
    /**
     * If true, the node is visible.
     * @param visible If true, the node is visible.
     */
    void setNodeVisible(boolean visible);
    /**
     * Adds a click event listener.
     * @param clickEventListener Callback executed on node click.
     */
    void addClickEventListener(ClickEventListener clickEventListener);
    /**
     * Removes a click event listener.
     * @param clickEventListener Callback executed on node click.
     */
    void removeClickEventListener(ClickEventListener clickEventListener);
    /**
     * Removes all click event listener.
     */
    void removeAllClickEventListeners();
    /**
     * Adds a right click event listener.
     * @param rightClickEventListener Callback executed on node right click.
     */
    void addRightClickEventListener(RightClickEventListener rightClickEventListener);
    /**
     * Removes a right click event listener.
     * @param rightClickEventListener Callback executed on node right click.
     */
    void removeRightClickEventListener(RightClickEventListener rightClickEventListener);
    /**
     * Removes all right click event listener.
     */
    void removeAllRightClickEventListeners();
    /**
     * Adds position changed event listener.
     * @param positionChangedEventListener Callback executed on node position changed.
     */
    void addPositionChangedEventListener(PositionChangedEventListener positionChangedEventListener);
    /**
     * Removes position changed event listener.
     * @param positionChangedEventListener Callback executed on node position changed.
     */
    void removePositionChangedEventListener(PositionChangedEventListener positionChangedEventListener);
    /**
     * Removes all position changed event listeners.
     */
    void removeAllPositionChangedEventListeners();
}
