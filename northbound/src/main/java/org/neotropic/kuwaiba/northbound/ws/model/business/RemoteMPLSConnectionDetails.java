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

import com.neotropic.kuwaiba.modules.commercial.mpls.model.MplsConnectionDefinition;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Instances of this class represent the details of a mpls link (a logical connection)
 * and the resources associated to the endpoints of such connection.
 * This information is useful to build reports and end-to-end views
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteMPLSConnectionDetails {
    /**
    * The complete information of the connection (that is, all its attributes)
    */
    private RemoteObject connectionObject;
    /**
     * the real endpoint of the mpls link side A
     */
    private RemoteObjectLight endpointA;
    /**
     * the device parent of the endpointA
     */
    private RemoteObjectLight deviceA;
    /**
     * the real endpoint of the mpls link side B
     */
    private RemoteObjectLight endpointB;
    /**
     * the device parent of the endpointB
     */
    private RemoteObjectLight deviceB;
    /**
     * the real endpoint of the mpls link, it could be a PhysicalPort or a VirtualPort
     */
    private RemoteObjectLight vfiA;
    /**
     * the real endpoint of the mpls link, it could be a PhysicalPort or a VirtualPort
     */
    private RemoteObjectLight vfiB;

    public RemoteMPLSConnectionDetails() {}
    
    

    public RemoteMPLSConnectionDetails(MplsConnectionDefinition mplsLinkEndpoints) {
        this.connectionObject = mplsLinkEndpoints.getConnectionObject() != null ? new RemoteObject(mplsLinkEndpoints.getConnectionObject()) : null;
        this.endpointA = mplsLinkEndpoints.getEndpointA() != null ? new RemoteObjectLight(mplsLinkEndpoints.getEndpointA()) : null;
        this.endpointB = mplsLinkEndpoints.getEndpointB() != null ? new RemoteObjectLight(mplsLinkEndpoints.getEndpointB()) : null;
        this.deviceA = mplsLinkEndpoints.getDeviceA() != null ? new RemoteObjectLight(mplsLinkEndpoints.getDeviceA()) : null;
        this.deviceB = mplsLinkEndpoints.getDeviceB() != null ? new RemoteObjectLight(mplsLinkEndpoints.getDeviceB()) : null;
        this.vfiA = mplsLinkEndpoints.getVfiA() != null ? new RemoteObjectLight(mplsLinkEndpoints.getVfiA()) : null;
        this.vfiB = mplsLinkEndpoints.getVfiB() != null ? new RemoteObjectLight(mplsLinkEndpoints.getVfiB()) : null;
    }

    public RemoteObject getConnectionObject() {
        return connectionObject;
    }

    public void setConnectionObject(RemoteObject connectionObject) {
        this.connectionObject = connectionObject;
    }

    public RemoteObjectLight getVfiA() {
        return vfiA;
    }

    public void setVfiA(RemoteObjectLight vfiA) {
        this.vfiA = vfiA;
    }

    public RemoteObjectLight getVfiB() {
        return vfiB;
    }

    public void setVfiB(RemoteObjectLight vfiB) {
        this.vfiB = vfiB;
    }
   
    public RemoteObjectLight getEndpointA() {
        return endpointA;
    }

    public void setEndpointA(RemoteObjectLight endpointA) {
        this.endpointA = endpointA;
    }

    public RemoteObjectLight getEndpointB() {
        return endpointB;
    }

    public void setEndpointB(RemoteObjectLight endpointB) {
        this.endpointB = endpointB;
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
