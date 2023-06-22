/**
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

import java.io.Serializable;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.GenericObjectList;

/**
 * Wrapper of GenericObjectList
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class ObjectList implements Serializable {
    /**
     * Class representing the list type (Vendor, AntennaType, etc)
     */
    private String className;
    /**
     * List type display name
     */
    private String displayName;
    /**
     * 1 for Many to One<br>
     * 2 for Many to Many
     */
    private int type;
    /**
     * Items
     */
    private HashMap<String,String> list;

    //No-arg constructor required
    public ObjectList() {    }

    public ObjectList(GenericObjectList listType){
        this.className = listType.getClassName();
        this.type = listType.getType();
        this.displayName = listType.getDisplayName();
        this.list = new HashMap<>();
        for (org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight item : listType.getList())
            this.list.put(item.getId(), item.getName());
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public HashMap<String, String> getList() {
        return list;
    }

    public void setList(HashMap<String, String> list) {
        this.list = list;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
