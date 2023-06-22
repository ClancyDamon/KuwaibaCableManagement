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

import com.neotropic.kuwaiba.modules.commercial.impact.ServiceLevelCorrelatedInformation;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Groups a customer and the services associated to it that are related somehow to an interface/device 
 * provided in the <code>getAffectedServices</code> method. It's actually a replacement for a Hashmap, but more <i>web service-friendly</i>
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteServiceLevelCorrelatedInformation implements Serializable {
    /**
     * The customer all the services belong to
     */
    private RemoteObjectLight customer;
    /**
     * The services related to the interface/device provided
     */
    private List<RemoteObjectLight> services;

    /**
     * Mandatory parameterless constructor.
     */
    public RemoteServiceLevelCorrelatedInformation() { }
    
    public RemoteServiceLevelCorrelatedInformation(RemoteObjectLight customer, List<RemoteObjectLight> services) {
        this.customer = customer;
        this.services = services;
    }

    public RemoteObjectLight getCustomer() {
        return customer;
    }

    public void setCustomer(RemoteObjectLight customer) {
        this.customer = customer;
    }

    public List<RemoteObjectLight> getServices() {
        return services;
    }

    public void setServices(List<RemoteObjectLight> services) {
        this.services = services;
    }
    
    public static RemoteServiceLevelCorrelatedInformation fromLocalResource(ServiceLevelCorrelatedInformation localResource) {
        return new RemoteServiceLevelCorrelatedInformation(new RemoteObjectLight(localResource.getCustomer()), 
                        RemoteObjectLight.toRemoteObjectLightArray(localResource.getServices()));
    }
    
    public static List<RemoteServiceLevelCorrelatedInformation> fromLocalResources(List<ServiceLevelCorrelatedInformation> localResources) {
        return localResources.stream().map(aLocalResource -> fromLocalResource(aLocalResource)).collect(Collectors.toList());
    }
}