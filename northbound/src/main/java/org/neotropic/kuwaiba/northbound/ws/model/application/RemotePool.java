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

package org.neotropic.kuwaiba.northbound.ws.model.application;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;

/**
 * Wrapper for a simple pool
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemotePool {
    /**
     * Pool id
     */
    private String id;
    /**
     * Pool name
     */
    private String name;
    /**
     * Pool description
     */
    private String description;
    /**
     * The class of the elements contained within
     */
    private String className;
    /**
     * Pool type
     */
    private int type;

    //No-arg constructor required
    public RemotePool(){}

    public RemotePool(String id, String name, String description, String className, int type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.className = className;
        this.type = type;
    }
    
    public RemotePool(InventoryObjectPool pool){
        this.id = pool.getId();
        this.name = pool.getName();
        this.className = pool.getClassName();
        this.description = pool.getDescription();
        this.type = pool.getType();
    } 

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    
    @Override
    public String toString() {
        return this.name;
    }

    public static RemotePool[] toRemotePoolArray(List<RemotePool> toBeWrapped){
        if (toBeWrapped == null)
            return null;

        RemotePool[] res = new RemotePool[toBeWrapped.size()];
        for (int i = 0; i < toBeWrapped.size(); i++)
            res[i] = toBeWrapped.get(i);

        return res;
    }
}
