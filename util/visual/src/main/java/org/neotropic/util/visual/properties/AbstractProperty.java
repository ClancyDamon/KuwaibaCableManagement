/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.util.visual.properties;

import com.vaadin.flow.component.AbstractField;
import org.neotropic.kuwaiba.core.i18n.TranslationService;


/**
 * A property in a property sheet.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <T> The type of the property.
 */
public abstract class AbstractProperty<T> {
    private String name;
    private String displayName;
    private String description;
    private T value;
    private boolean hasBinder;
    private String type;
    private boolean readOnly;
    private boolean mandatory;
    private boolean unique;
    private TranslationService ts;
    // Constants
    public static String NULL_LABEL;

    public AbstractProperty(String name, String displayName, String description, T value, TranslationService ts) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.value = value;
        this.hasBinder = false;
        this.readOnly = false;
        this.ts = ts;
        NULL_LABEL = ts.getTranslatedString("module.propertysheet.labels.null-value-property");
    }
    
    public AbstractProperty(String name, String displayName, String description, T value, TranslationService ts, boolean readOnly) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.value = value;
        this.hasBinder = false;
        this.readOnly = readOnly;
        this.ts = ts;
        NULL_LABEL = ts.getTranslatedString("module.propertysheet.labels.null-value-property");
    }
    
    public AbstractProperty(String name, String displayName, String description, T value, TranslationService ts, boolean readOnly, boolean mandatory, boolean unique) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.value = value;
        this.hasBinder = false;
        this.readOnly = readOnly;
        this.ts = ts;
        this.mandatory = mandatory;
        this.unique = unique;
         NULL_LABEL = ts.getTranslatedString("module.propertysheet.labels.null-value-property");
    }

    public AbstractProperty(String name, T value, TranslationService ts) {
        this.name = name;
        this.value = value;
        this.displayName = name;
        this.description = "";
        this.hasBinder = false;
        this.readOnly = false;
        this.ts = ts;
        NULL_LABEL = ts.getTranslatedString("module.propertysheet.labels.null-value-property");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public T getValue() {
        return value == null ? getDefaultValue() : value;
    }
    
    public abstract T getDefaultValue();

    public void setValue(T value) {
        this.value = value;
    }

    public boolean hasBinder() {
        return hasBinder;
    }

    public void setHasBinder(boolean hasBinder) {
        this.hasBinder = hasBinder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }
    
    
 
    @Override
    public String toString() {
        if (this.displayName == null || this.displayName.isEmpty())
            return this.name;
        return displayName;
    }
    
    /**
     * An advanced editor for the property
     * @return 
     */
    public abstract AbstractField getAdvancedEditor();
    /**
     * Does it support an advanced editor? If true  
     * @return 
     */
    public abstract boolean supportsAdvancedEditor();
    /**
     * Does it support in place editor? If true  
     * @return 
     */
    public abstract boolean supportsInplaceEditor();
    /**
     * A simple field that will be used to edit the property
     * @return 
     */
    public abstract AbstractField getInplaceEditor();
    /**
     * The value to be displayed on screen 
     * @return The string representation of the value of the property
     */
    public abstract String getAsString();
     
}
