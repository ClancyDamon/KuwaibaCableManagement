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

/**
 * Set of constants used in the Outside Plant Module
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OspConstants {
    /**
     * XML Outside Plant View attribute lat
     */
    public static final String ATTR_LAT = "lat"; //NOI18N
    /**
     * XML Outside Plant View attribute lon
     */
    public static final String ATTR_LON = "lon"; //NOI18N
    /**
     * Object class attribute latitude.
     */
    public static final String ATTR_LATITUDE = "latitude"; //NOI18N
    /**
     * Object class attribute longitude.
     */
    public static final String ATTR_LONGITUDE = "longitude"; //NOI18N
    /**
     * View Edge property controlPoints
     */
    public static final String PROPERTY_CONTROL_POINTS = "controlPoints"; //NOI18N
    
    public static final String BUSINESS_OBJECT_SOURCE = "source"; //NOI18N
    
    public static final String BUSINESS_OBJECT_TARGET = "target"; //NOI18N
    
    public static final String MAP_PROPERTY_CENTER_LATITUDE = "centerLatitude"; //NOI18N
    
    public static final String MAP_PROPERTY_CENTER_LONGITUDE = "centerLongitude"; //NOI18N
    
    public static final String MAP_PROPERTY_ZOOM = "zoom"; //NOI18N
    
    public static final String MAP_PROPERTY_TYPE_ID = "typeId"; //NOI18N
    
    public static final String MAP_PROPERTY_SYNC_GEO_POSITION = "syncGeoPosition"; //NOI18N
    
    public static final String MAP_PROPERTY_UNIT_OF_LENGTH = "unitOfLength"; //NOI18N
    
    public static final String MAP_PROPERTY_COMPUTE_EDGES_LENGTH = "computeEdgesLength"; //NOI18N
    
    public static final String MAP_PROPERTY_DEFAULT_PARENT = "defaultParent"; //NOI18N
    
    public static final double EDGE_STROKE_WEIGHT = 2;
    
    public static final double EDGE_STROKE_WEIGHT_MOUSE_OVER = 4;
    /**
     * Special relationship endpointA
     */
    public static final String SPECIAL_ATTR_ENDPOINT_A = "endpointA"; //NOI18N
    /**
     * Special relationship endpointB
     */
    public static final String SPECIAL_ATTR_ENDPOINT_B = "endpointB"; //NOI18N
    
    public static final String SPECIAL_REL_MIRROR = "mirror"; //NOI18N
    public static final String SPECIAL_REL_MIRROR_MULTIPLE = "mirrorMultiple"; //NOI18N
}
