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

/**
 * A POJO representation of a pool. A pool is basically a bag where you put elements of the same type, 
 * for example, a pool of instances of Router, or Buildings, etc. Pools are heavily used in many modules 
 * to support tree-like structure, such as the Service Manager, which organizes customers and services 
 * in groups and those groups are implemented via pools.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class InventoryObjectPool extends Pool {
    /**
     * The class of the elements contained within
     */
    private String className;
    /**
     * Pool type
     */
    private int type;

    public InventoryObjectPool(String id, String name, String description, String className, int type) {
        super(id, name, description);
        this.className = className;
        this.type = type;
    }
    
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}