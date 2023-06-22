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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;

/**
 * Wrapper for entity class User
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteUserInfo extends RemoteUserInfoLight {
   
    /**
     * User privileges
     */
    private List<PrivilegeInfo> privileges;
    
    //No-arg constructor required
    public RemoteUserInfo() { }
    
    public RemoteUserInfo(UserProfile user){
        super(user);
        
        this.privileges = new ArrayList<>();
        
        for (Privilege privilege : user.getPrivileges())
            privileges.add(new PrivilegeInfo(privilege));
    }

    public List<PrivilegeInfo> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<PrivilegeInfo> privileges) {
        this.privileges = privileges;
    }
}