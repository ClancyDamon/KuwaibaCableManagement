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

import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.flow.component.googlemap.LatLngBounds;
import com.neotropic.flow.component.googlemap.OverlayView;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapOverlayView;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.ViewOverlayView;
import com.vaadin.flow.component.Component;
import java.util.Objects;

/**
 * Represents a overlay view on the Google Maps map provider.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapsOverlayView extends OverlayView implements MapOverlayView {
    private GeoCoordinate southWest;
    private GeoCoordinate northEast;
    
    public GoogleMapsOverlayView(ViewOverlayView viewOverlayView) {
        Objects.requireNonNull(viewOverlayView);
        
        this.southWest = viewOverlayView.getSouthWest();
        this.northEast = viewOverlayView.getNorthEast();
        
        LatLngBounds bounds = new LatLngBounds(
            new LatLng(southWest.getLatitude(), southWest.getLongitude()), 
            new LatLng(northEast.getLatitude(), northEast.getLongitude())
        );
        setBounds(bounds);
    }
    
    @Override
    public GeoCoordinate getSouthWest() {
        return southWest;
    }

    @Override
    public void setSouthWest(GeoCoordinate southWest) {
        this.southWest = southWest;
    }

    @Override
    public GeoCoordinate getNorthEast() {
        return northEast;
    }

    @Override
    public void setNorthEast(GeoCoordinate northEast) {
        this.northEast = northEast;
    }
    
    public void addComponent(Component component) {
        Objects.requireNonNull(component);
        add(component);
    }
}
