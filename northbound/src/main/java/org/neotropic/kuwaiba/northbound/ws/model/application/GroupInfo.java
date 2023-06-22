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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfile;

/**
 * Wrapper for entity class UserGroup
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupInfo extends GroupInfoLight {
    
    /**
     * Membres of the group;
     */
    protected RemoteUserInfo[] users;
    
    /**
     * Group's privileges
     */
    private PrivilegeInfo[] privileges;

    //No-arg constructor required
    public GroupInfo(){ }
    
    public GroupInfo(GroupProfile group){
        super (group);
        
        users = new RemoteUserInfo[group.getUsers().size()];
        privileges = new PrivilegeInfo[group.getPrivileges().size()];
        
        for(int i = 0; i < group.getUsers().size(); i++)
            users[i]= new RemoteUserInfo(group.getUsers().get(i));
        
        for(int i = 0; i < group.getPrivileges().size(); i++)
            privileges[i] = new PrivilegeInfo(group.getPrivileges().get(i));
    } 

    public RemoteUserInfoLight[] getUsers() {
        return users;
    }

    public void setUsers(RemoteUserInfo[] users) {
        this.users = users;
    }

    public PrivilegeInfo[] getPrivileges() {
        return privileges;
    }

    public void setPrivileges(PrivilegeInfo[] privileges) {
        this.privileges = privileges;
    }
}
