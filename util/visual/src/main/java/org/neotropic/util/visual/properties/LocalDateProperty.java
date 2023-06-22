/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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
import com.vaadin.flow.component.datepicker.DatePicker;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Support for local-date-type properties
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class LocalDateProperty extends AbstractProperty<LocalDate> {
    /**
     * Default formatter.
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE dd MMM yyyy", Locale.ENGLISH);

    public LocalDateProperty(String name, String displayName, String description, LocalDate value, TranslationService ts) {
        super(name, displayName, description, value, ts);
        setType(Constants.DATA_TYPE_DATE);
    }  
    
    public LocalDateProperty(String name, String displayName, String description, long value, TranslationService ts) {
        super(name, displayName, description, Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate(),  ts);
         setType(Constants.DATA_TYPE_DATE);
    }

    public LocalDateProperty(String name, String displayName, String description, long value, TranslationService ts, boolean readOnly, boolean mandatory, boolean unique) {
        super(name, displayName, description, Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate(), ts, readOnly, mandatory, unique);
        setType(Constants.DATA_TYPE_DATE);
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
        DatePicker datePicker = new DatePicker();
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setDateFormat("EEE dd MMM yyyy");
        datePicker.setI18n(i18n);
        datePicker.setPlaceholder("...");
        return datePicker;
    }

    @Override
    public String getAsString() {
        return getValue() == null ? AbstractProperty.NULL_LABEL : getValue().format(DATE_FORMATTER);
    }
    
    @Override
    public boolean supportsInplaceEditor() {
        return true;
    }

    @Override
    public LocalDate getDefaultValue() {
        return LocalDate.now();
    }
}