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
package org.neotropic.util.visual.general;

import com.vaadin.flow.component.html.Span;
import java.util.Map;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Simple <code>Span</code> that takes a BusinessObjectLight instance and processes its validators 
 * to generate a formatted enclosure that can be used in trees, titles, views, or in general anywhere where 
 * the display name of an object must be shown.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class FormattedObjectDisplayNameSpan extends Span {
    /**
     * Ignore the "color" property expected in a validator. By default this is set to <code>false</code>.
     */
    private boolean ignoreTextColor;
    /**
     * Ignore the "fill-color" property expected in a validator. By default this is set to <code>false</code>.
     */
    private boolean ignoreFillColor;
    /**
     * Should the class name be included in the displayed name? By default this is set to <code>true</code>.
     */
    private boolean includeClassName;
    /**
     * If the span enclosing the display name should have a 5px padding. By default this is set to <code>true</code>.
     */
    private boolean enablePadding;
    
    /**
     * Default constructor with the default format settings (ignoreTextColor = false, ignoreFillColor = false, includeClassName = true, enablePadding = true)
     * @param businessObject The business object whose display will be formatted.
     */
    public FormattedObjectDisplayNameSpan(BusinessObjectLight businessObject) {
        this(businessObject, false, false, true, true);
    }

    /**
     * A customizable constructor.
     * @param businessObject The business object whose display will be formatted.
     * @param ignoreTextColor Ignore the "color" property expected in a validator. By default this is set to <code>false</code>.
     * @param ignoreFillColor Ignore the "fill-color" property expected in a validator. By default this is set to <code>false</code>.
     * @param includeClassName Should the class name be included in the displayed name? By default this is set to <code>true</code>.
     * @param enablePadding If the span enclosing the display name should have a 5px padding. By default this is set to <code>true</code>.
     */
    public FormattedObjectDisplayNameSpan(BusinessObjectLight businessObject, boolean ignoreTextColor, 
            boolean ignoreFillColor, boolean includeClassName, boolean enablePadding) {
        this.ignoreTextColor = ignoreTextColor;
        this.ignoreFillColor = ignoreFillColor;
        this.includeClassName = includeClassName;
        this.enablePadding = enablePadding;
        
        String text = includeClassName ? businessObject.toString() : businessObject.getName();
        if (businessObject.getValidators() != null) {
            for (Validator aValidator : businessObject.getValidators()) {
                if (aValidator.getProperties() != null) {
                    for (Map.Entry aProperty : aValidator.getProperties().entrySet()) {
                        switch ((String)aProperty.getKey()) {
                            case Validator.PROPERTY_PREFIX:
                                text = aProperty.getValue() + " " + text;
                                break;
                            case Validator.PROPERTY_SUFFIX:
                                text +=  " " + aProperty.getValue();
                                break;
                            case Validator.PROPERTY_COLOR:
                                if (!ignoreTextColor)
                                    getStyle().set("color", String.valueOf(aProperty.getValue()));
                                break;
                            case Validator.PROPERTY_FILLCOLOR:
                                if (!ignoreFillColor)
                                    getStyle().set("background-color", String.valueOf(aProperty.getValue()));
                                break;
                        }
                    }
                }
            }
        }
        
        if (enablePadding)
            getStyle().set("padding", "5px");
        
        setText(text);
    }

    public boolean isIgnoreTextColor() {
        return ignoreTextColor;
    }

    public void setIgnoreTextColor(boolean ignoreTextColor) {
        this.ignoreTextColor = ignoreTextColor;
    }

    public boolean isIgnoreFillColor() {
        return ignoreFillColor;
    }

    public void setIgnoreFillColor(boolean ignoreFillColor) {
        this.ignoreFillColor = ignoreFillColor;
    }

    public boolean isIncludeClassName() {
        return includeClassName;
    }

    public void setIncludeClassName(boolean includeClassName) {
        this.includeClassName = includeClassName;
    }

    public boolean isEnablePadding() {
        return enablePadding;
    }

    public void setEnablePadding(boolean enablePadding) {
        this.enablePadding = enablePadding;
    }
    
    /**
     * This helper method should be used in scenarios when it's not necessary a whole HTML component (a Span in this case), 
     * but just the text after processing the validators. This is useful in views, where the object display name uses its own renderer
     * which is probably not Vaadin-made, in this case, it's only necessary the text.
     * @param businessObject The business object whose display will be formatted.
     * @param includeClassName Should the class name be included in the displayed name?
     * @return The business object display name decorated with the prefixes and suffixes defined by its validators.
     */
    public static String getFormattedDisplayName(BusinessObjectLight businessObject, boolean includeClassName) {
        String displayName = includeClassName ? businessObject.toString() : businessObject.getName();
        if (businessObject.getValidators() != null) {
            for (Validator aValidator : businessObject.getValidators()) {
                if (aValidator.getProperties() != null) {
                    for (Map.Entry aProperty : aValidator.getProperties().entrySet()) {
                        switch ((String)aProperty.getKey()) {
                            case Validator.PROPERTY_PREFIX:
                                displayName = aProperty.getValue() + " " + displayName;
                                break;
                            case Validator.PROPERTY_SUFFIX:
                                displayName +=  " " + aProperty.getValue();
                                break;
                        }
                    }
                }
            }
        }
        return displayName;
    }
}
