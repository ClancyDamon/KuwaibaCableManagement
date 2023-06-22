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
 * Represents a overlay view on the map
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface MapOverlayView {
    /**
     * Gets the south-west.
     */
    GeoCoordinate getSouthWest();
    /**
     * Sets the south-west.
     * @param southWest 
     */
    void setSouthWest(GeoCoordinate southWest);
    /**
     * Gets the north-east.
     */
    GeoCoordinate getNorthEast();
    /**
     * Sets the north.east.
     * @param northEast 
     */
    void setNorthEast(GeoCoordinate northEast);
    /**
     * Adds a component.
     * @param component Component to add
     */
    void addComponent(Component component);
}
