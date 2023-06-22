/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.northbound.ws.model.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLightList;


/**
 * This class represents a list of RemoteObjectLight instances. It's basically a wrapper for ArrayList&lt;RemoteObjectLight&gt; 
 * It's used only to improve code readability. This class extended from ArrayList of RemoteObjectLight at the beginning, but JAX-WS just refused to treat it as a List
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteObjectLightList implements  Serializable {
    /**
     * The actual list. JAX-WS doesn't seem to serialize correctly classes inheriting from List, so instead of inheriting, we just use composition
     */
    private List<RemoteObjectLight> list;

    public RemoteObjectLightList() { }
    
    public RemoteObjectLightList (List<BusinessObjectLight> list) {
        this.list = new ArrayList<>();
        for (BusinessObjectLight item : list)
            this.list.add(new RemoteObjectLight(item));
    }
    
    public RemoteObjectLightList (BusinessObjectLightList list) {
        this.list = new ArrayList<>();
        for (BusinessObjectLight item : list.getList())
            this.list.add(new RemoteObjectLight(item));
    }

    public List<RemoteObjectLight> getList() {
        return list;
    }

    public void setList(List<RemoteObjectLight> list) {
        this.list = list;
    }
}