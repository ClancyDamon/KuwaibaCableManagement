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

import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;

/**
 * Groups an inventory object and the services (and corresponding services) somehow related to it. 
 * It's typically used to present correlated information in correlation-related API methods such as getAffectedServices
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class AssetLevelCorrelatedInformation {
    /**
     * The inventory object information
     */
    private List<BusinessObject> inventoryObjects;
    /**
     * The services and customers related to the provided inventory object
     */
    private List<ServiceLevelCorrelatedInformation> services;

    
    public AssetLevelCorrelatedInformation(List<BusinessObject> inventoryObjects, List<ServiceLevelCorrelatedInformation> services) {
        this.inventoryObjects = inventoryObjects;
        this.services = services;
    }

    public List<BusinessObject> getInventoryObjects() {
        return inventoryObjects;
    }

    public void setInventoryObjects(List<BusinessObject> inventoryObjects) {
        this.inventoryObjects = inventoryObjects;
    }

    public List<ServiceLevelCorrelatedInformation> getServices() {
        return services;
    }

    public void setServices(List<ServiceLevelCorrelatedInformation> services) {
        this.services = services;
    }
}