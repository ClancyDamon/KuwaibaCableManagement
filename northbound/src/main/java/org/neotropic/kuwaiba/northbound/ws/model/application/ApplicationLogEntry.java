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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;

/**
 * Wrapper of ActivityLogEntry
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */

@XmlAccessorType(XmlAccessType.FIELD)
public final class ApplicationLogEntry implements Serializable {
     /**
     * Entry id
     */
    private long id;
    /**
     * Id of the object related to the given action
     */
    private long objectId;
    /**
     * Entry type (see possible values below)
     */
    private int type;
    /**
     * Who performed the action
     */
    private String userName;
    /**
     * Entry timestamp
     */
    private long timestamp;
    /**
     * Property/field that was modified, if any
     */
    private String affectedProperty;
    /**
     * Old value of the modified property, if any
     */
    private String oldValue;
    /**
     * New value of the modified property, if any
     */
    private String newValue;
    /**
     * Additional notes to this action, if any
     */
    private String notes;

    //No-arg constructor required
    public ApplicationLogEntry() {   }
    
    public ApplicationLogEntry(ActivityLogEntry logEntry) {
        this.id = logEntry.getId();
        this.objectId = logEntry.getObjectId();
        this.type = logEntry.getType();
        this.userName = logEntry.getUserName();
        this.timestamp = logEntry.getTimestamp();
        this.oldValue = logEntry.getOldValue();
        this.newValue = logEntry.getNewValue();
        this.notes = logEntry.getNotes();
        this.affectedProperty = logEntry.getAffectedProperty();
    }
    
    public ApplicationLogEntry(long id, long objectId, int type, String userName, long timestamp, String affectedProperty, String oldValue, String newValue, String notes) {
        this.id = id;
        this.objectId = objectId;
        this.type = type;
        this.userName = userName;
        this.timestamp = timestamp;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.notes = notes;
        this.affectedProperty = affectedProperty;
    }

    public long getId() {
        return id;
    }

    public long getObjectId() {
        return objectId;
    }

    public int getType() {
        return type;
    }

    public String getUserName() {
        return userName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getAffectedProperty() {
        return affectedProperty;
    }
    
    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getNotes() {
        return notes;
    }
}
