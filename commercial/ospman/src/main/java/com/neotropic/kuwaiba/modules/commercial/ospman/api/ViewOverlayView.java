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

import com.vaadin.flow.component.Component;

/**
 * Represents a overlay view on the view
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ViewOverlayView implements MapOverlayView {
    private GeoCoordinate southWest;
    private GeoCoordinate northEast;
    
    public ViewOverlayView(GeoCoordinate southWest, GeoCoordinate northEast) {
        this.southWest = southWest;
        this.northEast = northEast;
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

    @Override
    public void addComponent(Component component) {
        
    }
}
