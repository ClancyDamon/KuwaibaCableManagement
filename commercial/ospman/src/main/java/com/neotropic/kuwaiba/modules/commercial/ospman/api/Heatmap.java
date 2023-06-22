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

/**
 * Represents a heatmap on the map provider.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface Heatmap {
    /**
     * Gets if heatmaps dissipate on zoom.
     * @return True if heatmap dissipate on zoom.
     */
    boolean getDissipateOnZoom();
    /**
     * Sets if heatmaps dissipate on zoom.
     * @param dissipating True dissipate on zoom.
     */
    void setDissipateOnZoom(boolean dissipating);
    /**
     * Gets the points displayed by this heatmap.
     * @return The points displayed by this heatmap.
     */
    List<GeoCoordinate> getPoints();
    /**
     * Sets the points to be displayed by this heatmap.
     * @param points The points to be displayed by this heatmap.
     */
    void setPoints(List<GeoCoordinate> points);
}
