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
package org.neotropic.kuwaiba.core.apis.persistence;

/**
 * This class is used to summarize a changed made to an object or a set of objects. It's mainly used by those 
 * methods that modify objects, and should log those changes in the application's audit trail
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ChangeDescriptor {
    private String affectedProperties;
    private String oldValues;
    private String newValues;
    private String notes;

    public ChangeDescriptor() {    }

    public ChangeDescriptor(String affectedProperties, String oldValues, String newValues, String notes) {
        this.affectedProperties = affectedProperties;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.notes = notes;
    }

    public String getAffectedProperties() {
        return affectedProperties;
    }

    public void setAffectedProperties(String affectedProperties) {
        this.affectedProperties = affectedProperties;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
