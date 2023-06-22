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

package com.neotropic.kuwaiba.modules.commercial.sdh.api;

import com.neotropic.kuwaiba.modules.commercial.sdh.SdhService;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;

/**
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class Route {

    String name;
    List<BusinessObjectLight> hops;
    int numberOfHops;
    MetadataEntityManager mem;

    public Route(String name, List<BusinessObjectLight> hops, MetadataEntityManager mem) throws MetadataObjectNotFoundException {
        this.name = name;
        this.hops = hops;
        this.mem = mem;
        this.numberOfHops = getNodes().size() - 1; //Ignores the first node, because it's the start node
    }

    public String getName() {
        return name;
    }

    public List<BusinessObjectLight> getNodes() throws MetadataObjectNotFoundException {
        List<BusinessObjectLight> res = new ArrayList<>();
        for (BusinessObjectLight hop : hops) {
            if (mem.isSubclassOf(SdhService.CLASS_GENERICEQUIPMENT, hop.getClassName())) {
                res.add(hop);
            }
        }
        return res;
    }

    public List<BusinessObjectLight> getLinks() throws MetadataObjectNotFoundException {
        List<BusinessObjectLight> res = new ArrayList<>();
        for (BusinessObjectLight hop : hops) {
            if (mem.isSubclassOf(SdhService.CLASS_GENERICLOGICALCONNECTION, hop.getClassName())) {
                res.add(hop);
            }
        }
        return res;
    }

    public List<BusinessObjectLight> getHops() {
        return hops;
    }

    @Override
    public String toString() {
        return String.format("%s - %s %s", name, numberOfHops, (numberOfHops == 1 ? "hop" : "hops"));
    }
}
