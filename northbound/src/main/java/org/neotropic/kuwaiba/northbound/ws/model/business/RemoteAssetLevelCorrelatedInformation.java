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

import com.neotropic.kuwaiba.modules.commercial.impact.AssetLevelCorrelatedInformation;
import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Groups an inventory object and the services (and corresponding services) somehow related to it. 
 * It's typically used to present correlated information in correlation-related API methods such as getAffectedServices
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteAssetLevelCorrelatedInformation implements Serializable {
    /**
     * The inventory object information
     */
    private List<RemoteObject> inventoryObjects;
    /**
     * The services and customers related to the provided inventory object
     */
    private List<RemoteServiceLevelCorrelatedInformation> services;

    /**
     * Mandatory parameterless constructor.
     */
    public RemoteAssetLevelCorrelatedInformation() { }

    public RemoteAssetLevelCorrelatedInformation(List<RemoteObject> inventoryObjects, List<RemoteServiceLevelCorrelatedInformation> services) {
        this.inventoryObjects = inventoryObjects;
        this.services = services;
    }

    public List<RemoteObject> getInventoryObjects() {
        return inventoryObjects;
    }

    public void setInventoryObjects(List<RemoteObject> inventoryObjects) {
        this.inventoryObjects = inventoryObjects;
    }

    public List<RemoteServiceLevelCorrelatedInformation> getServices() {
        return services;
    }

    public void setServices(List<RemoteServiceLevelCorrelatedInformation> services) {
        this.services = services;
    }
    
    public static RemoteAssetLevelCorrelatedInformation fromLocalResource(AssetLevelCorrelatedInformation localResource) {
        return new RemoteAssetLevelCorrelatedInformation(RemoteObject.toRemoteObjectArray(localResource.getInventoryObjects()), 
                RemoteServiceLevelCorrelatedInformation.fromLocalResources(localResource.getServices()));
    }
}