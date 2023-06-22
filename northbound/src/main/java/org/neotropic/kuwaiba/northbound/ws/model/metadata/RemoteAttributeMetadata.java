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

package org.neotropic.kuwaiba.northbound.ws.model.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;

/**
 * This is a wrapper class for AttributeMetadata, containing the info required for the clients
 * to render the object attributes in the right way
 *
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class RemoteAttributeMetadata implements Serializable {

    /**
     * Attribute's id
     */
    private long id;
    /**
     * Attribute's name
     */
    private String name;
    /**
     * Attribute's display name
     */
    private String displayName;
    /**
     * Attribute's type
     */
    private String type;
    /**
     * Flag to mark an attribute to be used for administrative purposes (beyond the operational inventory)
     */
    private Boolean administrative;
    /**
     * Attribute's visibility
     */
    private Boolean visible;
     /**
     * Marks the attribute as read only
     */
    private Boolean readOnly;
    /**
     * Marks the attribute as unique
     */
    private Boolean unique;
    /**
     * Marks the attribute as mandatory
     */
    private Boolean mandatory;
    /**
     * If true, this attribute is a multiple-selection list type. This flag has no effect in primitive attribute types (Strings, numbers, etc)
     */
    private Boolean multiple;
    /**
     * Attribute's short description
     */
    private String description;
    /**
     * Indicates if an attribute is copy when the copy/paste is made
     */
    private Boolean noCopy;
    /**
     * Cannot change or delete a locked attribute
     */
    private Boolean locked;
    /**
     * Tells the system how to sort the attributes. A call to any method that returns the attributes of a class will return them sorted by order.
     * This is useful to show the attributes in property sheets in order of importance, for example. The default value is 1000
     */
    private Integer order;

    public RemoteAttributeMetadata() {
    }

    public RemoteAttributeMetadata(String name, String displayName, String type, 
            Boolean administrative, Boolean visible, Boolean unique, Boolean mandatory, Boolean multiple, String description, Integer order, Boolean noCopy) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.administrative = administrative;
        this.visible = visible;
        this.unique = unique;
        this.mandatory = mandatory;
        this.description = description;
        this.order = order;
        this.multiple = multiple;
        this.noCopy = noCopy;
    }

    public RemoteAttributeMetadata(long id, String name, 
            String displayName, String type, 
            Boolean administrative,
            Boolean visible, Boolean readOnly, 
            Boolean unique, Boolean mandatory, Boolean multiple,
            String description, Boolean noCopy, Integer order) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.administrative = administrative;
        this.visible = visible;
        this.readOnly = readOnly;
        this.unique = unique;
        this.mandatory = mandatory;
        this.multiple = multiple;
        this.description = description;
        this.noCopy = noCopy;
        this.order = order;
    }
    
    public RemoteAttributeMetadata(String name, String displayName, String type, 
                         Boolean administrative, Boolean visible, 
                         Boolean readOnly, Boolean unique, Boolean mandatory, Boolean multiple, 
                         String description, Boolean noCopy, Integer order) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.administrative = administrative;
        this.visible = visible;
        this.readOnly = readOnly;
        this.unique = unique;
        this.mandatory = mandatory;
        this.multiple = multiple;
        this.description = description;
        this.noCopy = noCopy;
        this.order = order;
    }

    public Boolean isAdministrative() {
        return administrative;
    }

    public void setAdministrative(Boolean administrative) {
        this.administrative = administrative;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean isUnique() {
        return unique;
    }

    public void setUnique(Boolean unique) {
        this.unique = unique;
    }

    public Boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }

    public Boolean isNoCopy() {
        return noCopy;
    }

    public void setNoCopy(Boolean noCopy) {
        this.noCopy = noCopy;
    }

    public Boolean isLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
    
    /**
     * Converts a list of AttributeMetadata instances (as in the Persistence Service layer) to a list of RemoteAttributeMetadata instances
     * @param toBeWrapped The list of original AttributeMetadata instances
     * @return The list of converted RemoteAttributeMetadata instances
     */
    public static List<RemoteAttributeMetadata> toRemoteAttributeList(List<AttributeMetadata> toBeWrapped) {
        if (toBeWrapped == null)
            return null;
        
        List<RemoteAttributeMetadata> res = new ArrayList<>();
        
        for (AttributeMetadata toBeWrapped1 : toBeWrapped) 
            res.add(new RemoteAttributeMetadata(toBeWrapped1.getName(), toBeWrapped1.getDisplayName(), toBeWrapped1.getType(), 
                    toBeWrapped1.isAdministrative(), toBeWrapped1.isVisible(), toBeWrapped1.isUnique(), toBeWrapped1.isMandatory(), 
                    toBeWrapped1.isMultiple(), toBeWrapped1.getDescription(), toBeWrapped1.getOrder(), toBeWrapped1.isNoCopy()));
        
        return res;
    }
}