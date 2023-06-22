/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.colorpicker.ColorPicker;

/**
 * Support for local-date-type properties
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class ColorProperty extends AbstractProperty<String>{

    public ColorProperty(String name, String displayName, String description, String value, TranslationService ts) {
        super(name, displayName, description, value, ts);
        setType(Constants.DATA_TYPE_COLOR);
    }

    public ColorProperty(String name, String displayName, String description, String value, TranslationService ts, boolean readOnly, boolean mandatory, boolean unique) {
        super(name, displayName, description, value, ts, readOnly, mandatory, unique);
         setType(Constants.DATA_TYPE_COLOR);
    }

    @Override
    public AbstractField getAdvancedEditor() {
        throw new UnsupportedOperationException("This property type does not support an advanced editor."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsAdvancedEditor() {
        return false;
    }

    @Override
    public AbstractField getInplaceEditor() {       
        ColorPicker colorPicker = new ColorPicker();
        return colorPicker;
    }

    @Override
    public String getAsString() {
        return getValue() == null ? AbstractProperty.NULL_LABEL : getValue();
    }
    
    @Override
    public boolean supportsInplaceEditor() {
        return true;
    }

    @Override
    public String getDefaultValue() {
        return "#212121"; 
    }

}
