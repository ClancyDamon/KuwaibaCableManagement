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
 * Simplified version of a group of users. It contains only the basic information, without the users or privileges
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class GroupProfileLight {
    public static final String PROPERTY_NAME = "name";
    
    /**
     * Group's id (oid)
     */
    private long id;
    /**
     * Group's name
     */
    private String name;
    /**
     * Group's description
     */
    private String description;
    /**
     * Group's creation date (in milliseconds, it's a timestamp)
     */
    private long creationDate;

    public GroupProfileLight(long id, String name, String description, long creationDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long groupId) {
        this.id = groupId;
    }

    
    public String getDescription() {
        return description;
    }

    public void setDescription(String groupDescription) {
        this.description = groupDescription;
    }

    public String getName() {
        return name;
    }

    public void setName(String groupName) {
        this.name = groupName;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return name;
    }  
}
