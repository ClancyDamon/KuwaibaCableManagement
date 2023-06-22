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

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfileLight;

/**
 * Wrapper for entity class GroupProfile. This light version has the basic info necessary
 * to display in a table or list. Members of each group can be retrieved querying for a UserGroupInfo object
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupInfoLight implements Serializable {
    /**
     * Object id
     */
    protected long id;
    /**
     * UserGroup's name
     */
    protected String name;
    /**
     * Object's creation date. Since there's no a seamless map for java.util.Date
     * (xsd:date has less information than Date, so it's mapped into Calendar), we use a long instead (a timestamp)
     */
    protected long creationDate;
    /**
     * UserGroup's description
     */
    protected String description;

    //No-arg constructor required
    public GroupInfoLight(){}

    public GroupInfoLight(GroupProfileLight group){
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.creationDate = group.getCreationDate();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}