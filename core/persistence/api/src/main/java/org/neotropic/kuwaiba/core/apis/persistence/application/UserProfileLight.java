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
 * Base class of all classes representing application users
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class UserProfileLight {
    public static final String PROPERTY_ID = "id"; //NOI18N
    public static final String PROPERTY_NAME = "name"; //NOI18N
    public static final String PROPERTY_PASSWORD = "password"; //NOI18N
    public static final String PROPERTY_FIRST_NAME = "firstName"; //NOI18N
    public static final String PROPERTY_LAST_NAME = "lastName"; //NOI18N
    public static final String PROPERTY_CREATION_DATE = "creationDate"; //NOI18N
    public static final String PROPERTY_ENABLED = "enabled"; //NOI18N
    public static final String PROPERTY_TYPE = "type"; //NOI18N
    public static final String PROPERTY_EMAIL = "email"; //NOI18N
    
    public static final String DEFAULT_ADMIN = "admin";
    /**
     * "Hard-coded" users that should not be deleted or modified in any way (nor can they log in).
     */
    public static final int USER_TYPE_SYSTEM = 0;
    /**
     * Users that will access the system via desktop client or web interface
     */
    public static final int USER_TYPE_GUI = 1;
    /**
     * Users that will access the system via web service
     */
    public static final int USER_TYPE_WEB_SERVICE = 2;
    /**
     * Users that will access the system via automated interfaces, such as southbound interfaces or scheduled tasks
     */
    public static final int USER_TYPE_SOUTHBOUND = 3;
    /**
     * User id
     */
    private long id;
    /**
     * User username
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
     * User's creation date (a time stamp)
     */
    private long creationDate;
    /**
     * Is this user enabled?
     */
    private boolean enabled;
    /**
     * Type of user. This property is used mostly to restrict what kind of interfaces 
     * the user is able to use (web interface, desktop client, web service, synchronization, etc). 
     * See USER_TYPE* for possible values.
     */
    private int type;
    /**
     * User's email
     */
    private String email;

    public UserProfileLight(long id, String userName, String firstName, String lastName, boolean enabled, long creationDate, int type, String email) {
        this.id = id;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.creationDate = creationDate;
        this.enabled = enabled;
        this.type = type;
        this.email = email;
    }
    
    public UserProfileLight(long id, String userName) {
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public String toString() {
        return String.format("%s %s (%s)", firstName != null ? firstName : "", 
                lastName != null ? lastName : "", userName);
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof UserProfileLight ? this.userName.equals(((UserProfileLight)o).getUserName()) : false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.userName);
        return hash;
    }
}
