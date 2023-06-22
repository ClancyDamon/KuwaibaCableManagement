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
import com.vaadin.flow.component.textfield.IntegerField;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;


/**
 * Support for Integer properties
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class IntegerProperty extends AbstractProperty<Integer>{

    public IntegerProperty(String name, String displayName, String description, Integer value, TranslationService ts) {
        super(name, displayName, description, value, ts);
        setType(Constants.DATA_TYPE_INTEGER);
    }

    public IntegerProperty(String name, String displayName, String description, Integer value , TranslationService ts, boolean readOnly) {
        super(name, displayName, description, value, ts, readOnly);
        setType(Constants.DATA_TYPE_INTEGER);
    }

    public IntegerProperty(String name, String displayName, String description, Integer value, TranslationService ts, boolean readOnly, boolean mandatory, boolean unique) {
        super(name, displayName, description, value, ts, readOnly, mandatory, unique);
        setType(Constants.DATA_TYPE_INTEGER);
    }
    
    
    
    @Override
    public AbstractField getAdvancedEditor() {
        IntegerField intField = new IntegerField(this.getName(), "...");  
        intField.setWidthFull();
        return intField;
    }

    @Override
    public boolean supportsAdvancedEditor() {
        return false;
    }

    @Override
    public AbstractField getInplaceEditor() {
        IntegerField intField = new IntegerField("", "...");  
        intField.setWidthFull();
        return intField;
    }

    @Override
    public String getAsString() {
        return getValue() == null ? AbstractProperty.NULL_LABEL : getValue() + "";
    }
    
    @Override
    public boolean supportsInplaceEditor() {
        return true;
    }

    @Override
    public Integer getDefaultValue() {
        return 0;
    }
}
