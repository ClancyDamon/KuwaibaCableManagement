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

import java.util.List;

/**
 * A group of users
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class GroupProfile extends GroupProfileLight {
    /**
     * Group's users
     */
    private List<UserProfile> users;
    /**
     * Group's privileges. See class Privileges for the complete list of supported privileges
     */
    private List<Privilege> privileges;

    public GroupProfile(long id, String name, String description, long creationDate) {
        super(id, name, description, creationDate);
        
    }

    public GroupProfile(long id, String name, String description, long creationDate,
            List<UserProfile> users, List<Privilege> privileges) {
        super(id, name, description, creationDate);
        this.users = users;
        this.privileges = privileges;
    }
    
    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }

    public List<UserProfile> getUsers() {
        return users;
    }

    public void setUsers(List<UserProfile> users) {
        this.users = users;
    }
}
