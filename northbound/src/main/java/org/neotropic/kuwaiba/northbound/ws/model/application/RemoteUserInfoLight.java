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
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfileLight;

/**
 * Wrapper for entity class UserProfile.
 * @author Adrian Fernando Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteUserInfoLight implements Serializable {
    /**
     * User id
     */
    private long id;
   /**
    * User's login name
    */
    private String userName;
    /**
    * User's first name
    */
    private String firstName;
   /**
    * User's last name
    */
    private String lastName;
   /**
    * User's creation date
    */
    private long creationDate;
   /**
    * Indicates if this account is enabled
    */
    private boolean enabled;
    /**
     * User type. See UserProfileLight.USER_TYPE* for possible values
     */
    private int type;
    /**
     * User's email
     */
    private String email;
    
    //No-arg constructor required
    public RemoteUserInfoLight() { }
    
    public RemoteUserInfoLight(UserProfileLight user) {
        this.id = user.getId();
        this.userName = user.getUserName();
        this.enabled = user.isEnabled();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.creationDate = user.getCreationDate();
        this.type = user.getType();
        this.email = user.getEmail();
    }

    public RemoteUserInfoLight(long id, String userName) {
        this.id = id;
        this.userName = userName;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
