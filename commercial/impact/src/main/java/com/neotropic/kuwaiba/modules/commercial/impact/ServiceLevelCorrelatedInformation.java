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

package com.neotropic.kuwaiba.modules.commercial.impact;

import java.io.Serializable;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Groups a customer and the services associated to it that are related somehow to an interface/device 
 * provided in the <code>getAffectedServices</code> method. It's actually a replacement for a Hashmap, but more <i>web service-friendly</i>
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ServiceLevelCorrelatedInformation implements Serializable {
    /**
     * The customer all the services belong to
     */
    private BusinessObjectLight customer;
    /**
     * The services related to the interface/device provided
     */
    private List<BusinessObjectLight> services;
    
    public ServiceLevelCorrelatedInformation(BusinessObjectLight customer, List<BusinessObjectLight> services) {
        this.customer = customer;
        this.services = services;
    }

    public BusinessObjectLight getCustomer() {
        return customer;
    }

    public void setCustomer(BusinessObjectLight customer) {
        this.customer = customer;
    }

    public List<BusinessObjectLight> getServices() {
        return services;
    }

    public void setServices(List<BusinessObjectLight> services) {
        this.services = services;
    }
}