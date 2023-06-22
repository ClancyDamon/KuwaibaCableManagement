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

package org.neotropic.kuwaiba.core.apis.persistence.application;

/**
 * Represents an entry registering an event related to an object. It's usually related to updates
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class HistorycEntry {
    
    /**
     * An object's property has been changed
     */
    public static final int EVENT_FIELD_UPDATE = 1;
    /**
     * The object's parent has been changed
     */
    public static final int EVENT_PARENT_UPDATE = 2;
    /**
     * Creation date
     */
    private long creationDate;
    /**
     * Event type according to those defined in this class
     */
    private int eventType;
    /**
     * Read only notes placed by the application in order to indicate the past and current state of the object after the event happened
     */
    private String applicationNotes;
    /**
     * Custom observations related to this event
     */
    private String customNotes;

    public HistorycEntry(long creationDate, int eventType, String applicationNotes, String customNotes) {
        this.creationDate = creationDate;
        this.eventType = eventType;
        this.applicationNotes = applicationNotes;
        this.customNotes = customNotes;
    }

    public String getApplicationNotes() {
        return applicationNotes;
    }

    public void setApplicationNotes(String applicationNotes) {
        this.applicationNotes = applicationNotes;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getCustomNotes() {
        return customNotes;
    }

    public void setCustomNotes(String customNotes) {
        this.customNotes = customNotes;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}
