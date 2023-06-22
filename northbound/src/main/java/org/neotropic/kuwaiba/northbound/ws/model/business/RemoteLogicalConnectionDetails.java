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
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Instances of this class represent the details of a logical connection and the physical resources associated to the endpoints of such connection.
 * This information is useful to build reports and end-to-end views
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteLogicalConnectionDetails implements Serializable {
    /**
     * The complete information of the connection (that is, all its attributes)
     */
    private RemoteObject connectionObject;
    /**
     * One endpoint of the connection
     */
    private RemoteObjectLight endpointA;
    /**
     * The other endpoint of the connection
     */
    private RemoteObjectLight endpointB;
    /**
     * Physical path of the connection's endpoint A (all the chain of physical elements -patches, mirror ports, etc- until reaching the next physical port)
     */
    private List<RemoteObjectLight> physicalPathForEndpointA;
    /**
     * Physical path of the connection's endpoint B (all the chain of physical elements -patches, mirror ports, etc- until reaching the next physical port)
     */
    private List<RemoteObjectLight> physicalPathForEndpointB;
    /**
     * Physical paths, one for every port that belongs to the same VLAN of the endpoint A
     */
    private RemoteObjectRelatedObjects physicalPathForVlansEndpointA;
    /**
     * Physical paths, one for every port that belongs to the same VLAN of the endpoint B
     */
    private RemoteObjectRelatedObjects physicalPathForVlansEndpointB;

    private RemoteObjectRelatedObjects physicalPathForBDisEndpointA;
    private RemoteObjectRelatedObjects physicalPathForBDisEndpointB;
    /**
     * Default parameter-less constructor
     */
    public RemoteLogicalConnectionDetails() { }

    //Used for getBGPMap
    public RemoteLogicalConnectionDetails(BusinessObject connectionObject, 
            BusinessObjectLight endpointA, BusinessObjectLight endpointB, 
            List<BusinessObjectLight> physicalPathForEndpointA, List<BusinessObjectLight> physicalPathForEndpointB) {
        this.connectionObject = new RemoteObject(connectionObject);
        this.endpointA = endpointA == null ? null : new RemoteObjectLight(endpointA);
        this.endpointB = endpointB == null ? null : new RemoteObjectLight(endpointB);
        this.physicalPathForEndpointA = new ArrayList<>();
        for (BusinessObjectLight physicalPathForEndpointAElement : physicalPathForEndpointA)
            this.physicalPathForEndpointA.add(new RemoteObjectLight(physicalPathForEndpointAElement));
        this.physicalPathForEndpointB = new ArrayList<>();
        for (BusinessObjectLight physicalPathForEndpointBElement : physicalPathForEndpointB)
            this.physicalPathForEndpointB.add(new RemoteObjectLight(physicalPathForEndpointBElement));
    }
    
    //Used for getLogicalLinkDetails
    public RemoteLogicalConnectionDetails(BusinessObject connectionObject, 
            BusinessObjectLight endpointA, BusinessObjectLight endpointB, 
            List<BusinessObjectLight> physicalPathForEndpointA, List<BusinessObjectLight> physicalPathForEndpointB,
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForVlansEndpointA,
            HashMap<BusinessObjectLight,List<BusinessObjectLight>> physicalPathForBDisEndpointA,
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForVlansEndpointB,
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForBDisEndpointB) {
        this.connectionObject = new RemoteObject(connectionObject);
        this.endpointA = endpointA == null ? null : new RemoteObjectLight(endpointA);
        this.endpointB = endpointB == null ? null : new RemoteObjectLight(endpointB);
        this.physicalPathForEndpointA = new ArrayList<>();
        for (BusinessObjectLight physicalPathForEndpointAElement : physicalPathForEndpointA)
            this.physicalPathForEndpointA.add(new RemoteObjectLight(physicalPathForEndpointAElement));
        this.physicalPathForEndpointB = new ArrayList<>();
        for (BusinessObjectLight physicalPathForEndpointBElement : physicalPathForEndpointB)
            this.physicalPathForEndpointB.add(new RemoteObjectLight(physicalPathForEndpointBElement));
        
        this.physicalPathForVlansEndpointA = new RemoteObjectRelatedObjects(physicalPathForVlansEndpointA);
        this.physicalPathForVlansEndpointB = new RemoteObjectRelatedObjects(physicalPathForVlansEndpointB);
        
        this.physicalPathForBDisEndpointA = new RemoteObjectRelatedObjects(physicalPathForVlansEndpointA);
        this.physicalPathForBDisEndpointB = new RemoteObjectRelatedObjects(physicalPathForVlansEndpointB);
    }
    
    public RemoteObject getConnectionObject() {
        return connectionObject;
    }

    public void setConnectionObject(RemoteObject connectionObject) {
        this.connectionObject = connectionObject;
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

    public RemoteObjectRelatedObjects getPhysicalPathForVlansEndpointA() {
        return physicalPathForVlansEndpointA;
    }

    public void setPhysicalPathForVlansEndpointA(RemoteObjectRelatedObjects physicalPathForVlansEndpointA) {
        this.physicalPathForVlansEndpointA = physicalPathForVlansEndpointA;
    }

    public RemoteObjectRelatedObjects getPhysicalPathForVlansEndpointB() {
        return physicalPathForVlansEndpointB;
    }

    public void setPhysicalPathForVlansEndpointB(RemoteObjectRelatedObjects physicalPathForVlansEndpointB) {
        this.physicalPathForVlansEndpointB = physicalPathForVlansEndpointB;
    }

    public RemoteObjectRelatedObjects getPhysicalPathForBDisEndpointA() {
        return physicalPathForBDisEndpointA;
    }

    public void setPhysicalPathForBDisEndpointA(RemoteObjectRelatedObjects physicalPathForBDisEndpointA) {
        this.physicalPathForBDisEndpointA = physicalPathForBDisEndpointA;
    }

    public RemoteObjectRelatedObjects getPhysicalPathForBDisEndpointB() {
        return physicalPathForBDisEndpointB;
    }

    public void setPhysicalPathForBDisEndpointB(RemoteObjectRelatedObjects physicalPathForBDisEndpointB) {
        this.physicalPathForBDisEndpointB = physicalPathForBDisEndpointB;
    }
}
