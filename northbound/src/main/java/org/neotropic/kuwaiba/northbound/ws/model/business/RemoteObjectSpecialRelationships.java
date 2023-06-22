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
 * Wraps the special relationships of an object so a Hashmap doesn't have to be used
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteObjectSpecialRelationships implements Serializable {
    /**
     * The name of the relationships
     */
    private List<String> relationships;
    /**
     * The related objects. The indexes of this list are synced with those in #{@code relationships} and were separated from the 
     * original HashMap to allow an easier serialization of the data structure
     */
    private List<RemoteObjectLightList> relatedObjects;

    public RemoteObjectSpecialRelationships() { }

    public RemoteObjectSpecialRelationships(HashMap<String, List<BusinessObjectLight>> relationships) {
        this.relationships = new ArrayList<>();
        this.relatedObjects = new ArrayList<>();

        for (String relationshipName : relationships.keySet()){
            this.relationships.add(relationshipName);
            this.relatedObjects.add(new RemoteObjectLightList(relationships.get(relationshipName)));
        }
    }
    public List<String> getRelationships() {
        return relationships;
    }

    public List<RemoteObjectLightList> getRelatedObjects() {
        return relatedObjects;
    }
    
    /**
     * Organizes the relationships as a HashMap object
     * @return A HashMap object with the relationships
     */
    public HashMap<String, RemoteObjectLightList> asHashMap() {
        HashMap<String, RemoteObjectLightList> res = new HashMap<>();
        for (int i = 0; i < relationships.size(); i++)
            res.put(relationships.get(i), relatedObjects.get(i));
        
        return res;
    }
}
