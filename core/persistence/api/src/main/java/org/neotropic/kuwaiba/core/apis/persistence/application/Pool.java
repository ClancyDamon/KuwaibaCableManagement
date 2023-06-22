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
package org.neotropic.kuwaiba.core.apis.persistence.application;

import java.util.Objects;

/**
 * A POJO representation of a pool. A pool is basically a bag where you put elements of the same type
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Pool {
    /**
     * Pool id.
     */
    private String id;
    /**
     * Pool name.
     */
    private String name;
    /**
     * Pool description.
     */
    private String description;
    
    public Pool(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    @Override
    public String toString() {
        return this.name;
    }
    
    @Override
    public boolean equals(Object obj){
       if(obj == null)
           return false;
       if (!(obj instanceof Pool))
           return false;
       return (this.getId() == null ? ((Pool)obj).getId() == null : this.getId().equals(((Pool)obj).getId()));
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.id);
        return hash;
    }
}
