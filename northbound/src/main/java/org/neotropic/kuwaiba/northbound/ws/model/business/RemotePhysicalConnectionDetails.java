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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Instances of this class represent the details of a physical connection and the complete structure of both sides 
 * This information is useful to build reports and end-to-end views
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemotePhysicalConnectionDetails implements Serializable {
    /**
     * The complete information of the connection (that is, all its attributes)
     */
    private RemoteObject connectionObject;
    /**
     * Physical path of the connection's endpoint A (all the chain of physical elements -patches, mirror ports, etc- until reaching the next physical port)
     */
    private List<RemoteObjectLight> physicalPathForEndpointA;
    /**
     * Physical path of the connection's endpoint B (all the chain of physical elements -patches, mirror ports, etc- until reaching the next physical port)
     */
    private List<RemoteObjectLight> physicalPathForEndpointB;
    
    /**
     * Default parameter-less constructor
     */
    public RemotePhysicalConnectionDetails() { }

    public RemotePhysicalConnectionDetails(BusinessObject connectionObject, 
            List<BusinessObjectLight> physicalPathForEndpointA, List<BusinessObjectLight> physicalPathForEndpointB) {
        this.connectionObject = new RemoteObject(connectionObject);
        this.physicalPathForEndpointA = new ArrayList<>();
        for (BusinessObjectLight physicalPathForEndpointAElement : physicalPathForEndpointA)
            this.physicalPathForEndpointA.add(new RemoteObjectLight(physicalPathForEndpointAElement));
        this.physicalPathForEndpointB = new ArrayList<>();
        for (BusinessObjectLight physicalPathForEndpointBElement : physicalPathForEndpointB)
            this.physicalPathForEndpointB.add(new RemoteObjectLight(physicalPathForEndpointBElement));
    }
    
    public RemoteObject getConnectionObject() {
        return connectionObject;
    }

    public void setConnectionObject(RemoteObject connectionObject) {
        this.connectionObject = connectionObject;
    }

    public List<RemoteObjectLight> getPhysicalPathForEndpointA() {
        return physicalPathForEndpointA;
    }

    public void setPhysicalPathForEndpointA(List<RemoteObjectLight> physicalPathForEndpointA) {
        this.physicalPathForEndpointA = physicalPathForEndpointA;
    }

    public List<RemoteObjectLight> getPhysicalPathForEndpointB() {
        return physicalPathForEndpointB;
    }

    public void setPhysicalPathForEndpointB(List<RemoteObjectLight> physicalPathForEndpointB) {
        this.physicalPathForEndpointB = physicalPathForEndpointB;
    }  

}
