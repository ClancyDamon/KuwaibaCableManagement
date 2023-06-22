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

package com.neotropic.kuwaiba.modules.commercial.mpls.model;

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;


/**
 * Instances of this class represent the details of a MPLSLink (logical connection)
 * and the resources associated to the physical an logical endpoints of such connection.
 * This information is useful to build reports and end-to-end views
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MplsConnectionDefinition {
    /**
    * The complete information of the connection (that is, all its attributes)
    */
    private BusinessObject connectionObject;
    /**
     * the real endpoint of the mplsLink side A
     */
    private BusinessObjectLight endpointA;
    /**
     * the parent of the endpointA
     */
    private BusinessObjectLight deviceA;
    /**
     * the real endpoint of the mplsLink side B
     */
    private BusinessObjectLight endpointB;
    /**
     * the parent of the endpointA
     */
    private BusinessObjectLight deviceB;
    /**
     * If the pseudowireA has a VFI related
     */
    private BusinessObjectLight vfiA;
    /**
     * If the pseudowireB has a VFI related
     */
    private BusinessObjectLight vfiB;

    public MplsConnectionDefinition(BusinessObject connectionObject) {
        this.connectionObject = connectionObject;
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

    public BusinessObjectLight getVfiA() {
        return vfiA;
    }

    public void setVfiA(BusinessObjectLight vfiA) {
        this.vfiA = vfiA;
    }

    public BusinessObjectLight getVfiB() {
        return vfiB;
    }

    public void setVfiB(BusinessObjectLight vfiB) {
        this.vfiB = vfiB;
    }

    
}
