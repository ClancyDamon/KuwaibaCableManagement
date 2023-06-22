/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.northbound.ws.model.business;

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
public class RemoteObjectLinkObject {
    
    /**
     * The complete information of the connection (that is, all its attributes)
     */
    private RemoteObject connectionObject;
    /**
     * One endpoint of the connection (a physical port)
     */
    private RemoteObjectLight physicalEndpointObjectA;
    /**
     * One endpoint of the connection (a physical port)
     */
    private RemoteObjectLight physicalEndpointObjectB;
    /**
     * One endpoint of the connection (a virtual port)
     */
    private RemoteObjectLight logicalEndpointObjectA;
    /**
     * One endpoint of the connection (a virtual port)
     */
    private RemoteObjectLight logicalEndpointObjectB;
    /**
     * The complete information of the connection (that is, all its attributes)
     */
    private RemoteObjectLight deviceA;
     /**
     * The complete information of the connection (that is, all its attributes)
     */
    private RemoteObjectLight deviceB;

    public RemoteObjectLinkObject() { }
   
    public RemoteObjectLinkObject(RemoteObjectLight deviceA, 
            RemoteObjectLight physicalEndpointObjectA,
            RemoteObject connectionObject,
            RemoteObjectLight physicalEndpointObjectB, 
            RemoteObjectLight deviceB){
        this.connectionObject = connectionObject;
        this.physicalEndpointObjectA = physicalEndpointObjectA;
        this.physicalEndpointObjectB = physicalEndpointObjectB;
        this.deviceA = deviceA;
        this.deviceB = deviceB;
    }
    
    public RemoteObjectLinkObject(RemoteObjectLight deviceA, 
            RemoteObjectLight physicalEndpointObjectA, RemoteObjectLight logicalEndpointObjectA,
            RemoteObject connectionObject, RemoteObjectLight logicalEndpointObjectB,
            RemoteObjectLight physicalEndpointObjectB, 
            RemoteObjectLight deviceB){
        this.connectionObject = connectionObject;
        this.physicalEndpointObjectA = physicalEndpointObjectA;
        this.logicalEndpointObjectA = logicalEndpointObjectA;
        this.logicalEndpointObjectB = logicalEndpointObjectB;
        this.physicalEndpointObjectB = physicalEndpointObjectB;
        this.deviceA = deviceA;
        this.deviceB = deviceB;
    }

    public RemoteObject getConnectionObject() {
        return connectionObject;
    }

    public void setConnectionObject(RemoteObject connectionObject) {
        this.connectionObject = connectionObject;
    }

    public RemoteObjectLight getPhysicalEndpointObjectA() {
        return physicalEndpointObjectA;
    }

    public void setPhysicalEndpointObjectA(RemoteObjectLight physicalEndpointObjectA) {
        this.physicalEndpointObjectA = physicalEndpointObjectA;
    }

    public RemoteObjectLight getPhysicalEndpointObjectB() {
        return physicalEndpointObjectB;
    }

    public void setPhysicalEndpointObjectB(RemoteObjectLight physicalEndpointObjectB) {
        this.physicalEndpointObjectB = physicalEndpointObjectB;
    }

    public RemoteObjectLight getLogicalEndpointObjectA() {
        return logicalEndpointObjectA;
    }

    public void setLogicalEndpointObjectA(RemoteObjectLight logicalEndpointObjectA) {
        this.logicalEndpointObjectA = logicalEndpointObjectA;
    }

    public RemoteObjectLight getLogicalEndpointObjectB() {
        return logicalEndpointObjectB;
    }

    public void setLogicalEndpointObjectB(RemoteObjectLight logicalEndpointObjectB) {
        this.logicalEndpointObjectB = logicalEndpointObjectB;
    }

    public RemoteObjectLight getDeviceA() {
        return deviceA;
    }

    public void setDeviceA(RemoteObjectLight deviceA) {
        this.deviceA = deviceA;
    }

    public RemoteObjectLight getDeviceB() {
        return deviceB;
    }

    public void setDeviceB(RemoteObjectLight deviceB) {
        this.deviceB = deviceB;
    }
    
}
