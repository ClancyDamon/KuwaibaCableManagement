/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.business.analytics.views;

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Instances of this class represent a structure object-(endpoint)-link-(endpoint)-object
 * a set of this objects represents the complete structure of an end to end view
 * e.g.
 * [device1 -endpoint- link -endpoint- device2]
 * [device3 -endpoint- link -endpoint- device2]
 * [device5 -endpoint- link -endpoint- device3]
 *  .
 *  .
 *  .
 * [deviceN -endpoint- link -endpoint- device5]
 * 
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class ObjectLinkObjectDefinition {
    /**
     * The complete information of the connection (that is, all its attributes)
     */
    private BusinessObjectLight connectionObject;
    /**
     * One endpoint of the connection (a physical port)
     */
    private BusinessObjectLight physicalEndpointObjectA;
    /**
     * One endpoint of the connection (a physical port)
     */
    private BusinessObjectLight physicalEndpointObjectB;
    /**
     * One endpoint of the connection (a virtual port)
     */
    private BusinessObjectLight logicalEndpointObjectA;
    /**
     * One endpoint of the connection (a virtual port)
     */
    private BusinessObjectLight logicalEndpointObjectB;
    /**
     * The complete information of the connection (that is, all its attributes)
     */
    private BusinessObjectLight deviceA;
     /**
     * The complete information of the connection (that is, all its attributes)
     */
    private BusinessObjectLight deviceB;

    public ObjectLinkObjectDefinition() { }
   
    public ObjectLinkObjectDefinition(BusinessObjectLight deviceA, 
            BusinessObjectLight physicalEndpointObjectA,
            BusinessObjectLight connectionObject,
            BusinessObjectLight physicalEndpointObjectB, 
            BusinessObjectLight deviceB){
        this.connectionObject = connectionObject;
        this.physicalEndpointObjectA = physicalEndpointObjectA;
        this.physicalEndpointObjectB = physicalEndpointObjectB;
        this.deviceA = deviceA;
        this.deviceB = deviceB;
    }
    
    public ObjectLinkObjectDefinition(BusinessObjectLight deviceA, 
            BusinessObjectLight physicalEndpointObjectA, BusinessObjectLight logicalEndpointObjectA,
            BusinessObjectLight connectionObject, BusinessObjectLight logicalEndpointObjectB,
            BusinessObjectLight physicalEndpointObjectB, 
            BusinessObjectLight deviceB){
        this.connectionObject = connectionObject;
        this.physicalEndpointObjectA = physicalEndpointObjectA;
        this.logicalEndpointObjectA = logicalEndpointObjectA;
        this.logicalEndpointObjectB = logicalEndpointObjectB;
        this.physicalEndpointObjectB = physicalEndpointObjectB;
        this.deviceA = deviceA;
        this.deviceB = deviceB;
    }

    public BusinessObjectLight getConnectionObject() {
        return connectionObject;
    }

    public void setConnectionObject(BusinessObjectLight connectionObject) {
        this.connectionObject = connectionObject;
    }

    public BusinessObjectLight getPhysicalEndpointObjectA() {
        return physicalEndpointObjectA;
    }

    public void setPhysicalEndpointObjectA(BusinessObjectLight physicalEndpointObjectA) {
        this.physicalEndpointObjectA = physicalEndpointObjectA;
    }

    public BusinessObjectLight getPhysicalEndpointObjectB() {
        return physicalEndpointObjectB;
    }

    public void setPhysicalEndpointObjectB(BusinessObjectLight physicalEndpointObjectB) {
        this.physicalEndpointObjectB = physicalEndpointObjectB;
    }

    public BusinessObjectLight getLogicalEndpointObjectA() {
        return logicalEndpointObjectA;
    }

    public void setLogicalEndpointObjectA(BusinessObjectLight logicalEndpointObjectA) {
        this.logicalEndpointObjectA = logicalEndpointObjectA;
    }

    public BusinessObjectLight getLogicalEndpointObjectB() {
        return logicalEndpointObjectB;
    }

    public void setLogicalEndpointObjectB(BusinessObjectLight logicalEndpointObjectB) {
        this.logicalEndpointObjectB = logicalEndpointObjectB;
    }

    public BusinessObjectLight getDeviceA() {
        return deviceA;
    }

    public void setDeviceA(BusinessObjectLight deviceA) {
        this.deviceA = deviceA;
    }

    public BusinessObjectLight getDeviceB() {
        return deviceB;
    }

    public void setDeviceB(BusinessObjectLight deviceB) {
        this.deviceB = deviceB;
    }
}
