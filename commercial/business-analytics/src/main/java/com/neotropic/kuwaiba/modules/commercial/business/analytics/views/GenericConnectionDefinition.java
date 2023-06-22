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

import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Instances of this class represent the details of a logical connection and the physical resources associated to the endpoints of such connection.
 * This information is useful to build reports and end-to-end views
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class GenericConnectionDefinition{
    /**
     * The complete information of the connection (that is, all its attributes)
     */
    private BusinessObject connectionObject;
    /**
     * One endpoint of the connection
     */
    private BusinessObjectLight endpointA;
    /**
     * The other endpoint of the connection
     */
    private BusinessObjectLight endpointB;
    /**
     * Physical path of the connection's endpoint A (all the chain of physical elements -patches, mirror ports, etc- until reaching the next physical port)
     */
    private List<BusinessObjectLight> physicalPathForEndpointA;
    /**
     * Physical path of the connection's endpoint B (all the chain of physical elements -patches, mirror ports, etc- until reaching the next physical port)
     */
    private List<BusinessObjectLight> physicalPathForEndpointB;
    /**
     * Default parameter-less constructor
     */
    public GenericConnectionDefinition() { }

    public GenericConnectionDefinition(BusinessObject connectionObject, 
            BusinessObjectLight endpointA, BusinessObjectLight endpointB, 
            List<BusinessObjectLight> physicalPathForEndpointA, List<BusinessObjectLight> physicalPathForEndpointB) {
        this.connectionObject = connectionObject;
        this.endpointA = endpointA;
        this.endpointB = endpointB;
        this.physicalPathForEndpointA = physicalPathForEndpointA;
        this.physicalPathForEndpointB = physicalPathForEndpointB;
    }

    public BusinessObject getConnectionObject() {
        return connectionObject;
    }

    public void setConnectionObject(BusinessObject connectionObject) {
        this.connectionObject = connectionObject;
    }

    public BusinessObjectLight getEndpointA() {
        return endpointA;
    }

    public void setEndpointA(BusinessObjectLight endpointA) {
        this.endpointA = endpointA;
    }

    public BusinessObjectLight getEndpointB() {
        return endpointB;
    }

    public void setEndpointB(BusinessObjectLight endpointB) {
        this.endpointB = endpointB;
    }

    public List<BusinessObjectLight> getPhysicalPathForEndpointA() {
        return physicalPathForEndpointA;
    }

    public void setPhysicalPathForEndpointA(List<BusinessObjectLight> physicalPathForEndpointA) {
        this.physicalPathForEndpointA = physicalPathForEndpointA;
    }

    public List<BusinessObjectLight> getPhysicalPathForEndpointB() {
        return physicalPathForEndpointB;
    }

    public void setPhysicalPathForEndpointB(List<BusinessObjectLight> physicalPathForEndpointB) {
        this.physicalPathForEndpointB = physicalPathForEndpointB;
    }  
}
