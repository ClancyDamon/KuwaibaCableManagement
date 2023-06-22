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

import com.neotropic.flow.component.googlemap.HeatmapLayer;
import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.Heatmap;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.ViewHeatmap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a heatmap on the Google Maps map provider. 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapsHeatmapLayer extends HeatmapLayer implements Heatmap {
    private List<GeoCoordinate> points;
    private boolean dissipating = false;
    
    public GoogleMapsHeatmapLayer(ViewHeatmap viewHeatmap) {
        Objects.requireNonNull(viewHeatmap);
        setDissipateOnZoom(dissipating);
        setRadius(24);
        setPoints(viewHeatmap.getPoints());
    }
    
    @Override
    public boolean getDissipateOnZoom() {
        return dissipating;
    }
    
    @Override
    public void setDissipateOnZoom(boolean dissipating) {
        this.dissipating = dissipating;
        setDissipating(dissipating);
    }
    
    @Override
    public List<GeoCoordinate> getPoints() {
        return points;
    }

    @Override
    public void setPoints(List<GeoCoordinate> points) {
        this.points = points;
        
        if (points != null) {
            List<LatLng> data = new ArrayList();
            points.forEach(point -> data.add(new LatLng(
                point.getLatitude(), 
                point.getLongitude()
            )));
            setData(data);
        }
        else
            setData(null);
    }
}
