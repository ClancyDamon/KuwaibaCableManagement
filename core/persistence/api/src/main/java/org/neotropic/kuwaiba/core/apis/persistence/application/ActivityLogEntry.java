/*
 * Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>
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

import java.io.Serializable;

/**
 * Represents an activity log entry
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ActivityLogEntry implements Serializable {
    /**
     * Entry id
     */
    private long id;
    /**
     * The id of the object related to the action
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
    public static final int ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT = 1;
    public static final int ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT = 2;
    public static final int ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT = 3;
    public static final int ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT = 4;
    public static final int ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT = 5;
    public static final int ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT = 6;
    public static final int ACTIVITY_TYPE_CREATE_METADATA_OBJECT = 7;
    public static final int ACTIVITY_TYPE_DELETE_METADATA_OBJECT = 8;
    public static final int ACTIVITY_TYPE_UPDATE_METADATA_OBJECT = 9;
    public static final int ACTIVITY_TYPE_CHANGE_PARENT = 10;
    public static final int ACTIVITY_TYPE_MASSIVE_DELETE_APPLICATION_OBJECT = 11;
    public static final int ACTIVITY_TYPE_UPDATE_VIEW = 12;
    public static final int ACTIVITY_TYPE_OPEN_SESSION = 13;
    public static final int ACTIVITY_TYPE_CLOSE_SESSION = 14;
    public static final int ACTIVITY_TYPE_CREATE_USER = 15;
    public static final int ACTIVITY_TYPE_MASSIVE_UPDATE_APPLICATION_OBJECT = 16;
    public static final int ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT = 17;
    public static final int ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT = 18;

    public ActivityLogEntry(long id, long objectId, int type, String userName, long timestamp, String affectedProperty, String oldValue, String newValue, String notes) {
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

    public void setId(long id) {
        this.id = id;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAffectedProperty() {
        return affectedProperty;
    }

    public void setAffectedProperty(String affectedProperty) {
        this.affectedProperty = affectedProperty;
    }
    
    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}