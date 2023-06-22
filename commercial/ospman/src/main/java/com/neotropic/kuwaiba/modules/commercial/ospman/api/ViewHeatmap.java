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
 * Represents a heatmap on the View. 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ViewHeatmap implements Heatmap {
    private List<GeoCoordinate> points;
    private boolean dissipating;
    
    public ViewHeatmap() {
    }
    
    public ViewHeatmap(List<GeoCoordinate> points) {
        this.points = points;
    }
    
    public boolean getDissipateOnZoom() {
        return dissipating;
    }
    
    public void setDissipateOnZoom(boolean dissipating) {
        this.dissipating = dissipating;
    }
    
    @Override
    public List<GeoCoordinate> getPoints() {
        return points;
    }

    @Override
    public void setPoints(List<GeoCoordinate> points) {
        this.points = points;
    }
}
