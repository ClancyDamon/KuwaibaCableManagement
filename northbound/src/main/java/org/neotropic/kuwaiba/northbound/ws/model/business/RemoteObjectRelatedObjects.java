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
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Wraps the object and objects related with it so a Hashmap doesn't have to be used
 * e.g. a list of ports and its parents when vlan maps its generated
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteObjectRelatedObjects implements Serializable {
    /**
     * The object
     */
    private List<RemoteObjectLight> objs;
    /**
     * The related objects. The indexes of this list are synchronized with those in #{@code relationships} and were separated from the 
     * original HashMap to allow an easier serialization of the data structure
     */
    private List<RemoteObjectLightList> relatedObjects;

    public RemoteObjectRelatedObjects() { }

    public RemoteObjectRelatedObjects(HashMap<BusinessObjectLight, List<BusinessObjectLight>> relationships) {
        this.objs = new ArrayList<>();
        this.relatedObjects = new ArrayList<>();

        for (BusinessObjectLight obj : relationships.keySet()){
            this.objs.add(new RemoteObjectLight(obj));
            this.relatedObjects.add(new RemoteObjectLightList(relationships.get(obj)));
        }
    }
    public List<RemoteObjectLight> getObjs() {
        return objs;
    }

    public List<RemoteObjectLightList> getRelatedObjects() {
        return relatedObjects;
    }
    
    /**
     * Organizes the relationships as a HashMap object
     * @return A HashMap object with the relationships
     */
    public HashMap<RemoteObjectLight, RemoteObjectLightList> asHashMap() {
        HashMap<RemoteObjectLight, RemoteObjectLightList> res = new HashMap<>();
        for (int i = 0; i < objs.size(); i++)
            res.put(objs.get(i), relatedObjects.get(i));
        
        return res;
    }
}
