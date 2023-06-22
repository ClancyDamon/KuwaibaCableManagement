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
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;

/**
 * Represents the information to be exchanged when a call to createSeesion is successful. This is more or less
 * a wrapper of UserProfile plus a session id
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteSession implements Serializable {
    /**
     * Identifies a session opened from a desktop client
     */
    public static final int TYPE_DESKTOP = 1;
    /**
     * Identifies a session opened from a web client
     */
    public static final int TYPE_WEB = 2;
    /**
     * Identifies a session opened from a web service client
     */
    public static final int TYPE_WS = 3;
    /**
     * Identifies a session opened from a mobile client (a mobile application)
     */
    public static final int TYPE_MOBILE = 4;
    /**
     * A unique string representing the session id
     */
    private String sessionId;
    /**
     * The user name
     */
    private String username;
    /**
     * The id of the user
     */
    private long userId;
    /**
     * What type of session depending of the type of client. Only one session per session type is allowed. See RemoteSession.TYPE_XXX for possible values
     */
    private int sessionType;
    /**
     * The first name of the user
     */
    private String firstName;
    /**
     * The last name of the user
     */
    private String lastName;

    //No-arg constructor required
    public RemoteSession() {    }

    public RemoteSession(String sessionID, UserProfile user, int sessionType) {
        this.sessionId = sessionID;
        this.username = user.getUserName();
        this.userId = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.sessionType = sessionType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }
}
