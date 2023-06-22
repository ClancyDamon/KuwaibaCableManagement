/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>
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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;


/**
 * Support for String like properties
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class StringProperty extends AbstractProperty<String>{

    private boolean masked = false;
    
    public StringProperty(String name, String displayName, String description, String value, TranslationService ts) {
        super(name, displayName, description, value, ts);
        setType(Constants.DATA_TYPE_STRING);
    }

    public StringProperty(String name, String displayName, String description, String value, TranslationService ts, boolean readOnly) {
        super(name, displayName, description, value, ts, readOnly);
        setType(Constants.DATA_TYPE_STRING);
    }
    
    public StringProperty(String name, String displayName, String description, String value, TranslationService ts, boolean readOnly, boolean masked) {
        super(name, displayName, description, value, ts, readOnly);
        setType(Constants.DATA_TYPE_STRING);
        this.masked = masked;
    }

    public StringProperty(String name, String displayName, String description, String value, TranslationService ts, boolean readOnly, boolean mandatory, boolean unique) {
        super(name, displayName, description, value, ts, readOnly, mandatory, unique);
         setType(Constants.DATA_TYPE_STRING);
    }
    
    
    
    @Override
    public AbstractField getAdvancedEditor() {
        if (masked) {
            PasswordField passwordField = new PasswordField(this.getName(), "...");
            passwordField.setWidthFull();
            passwordField.setValue(this.getValue());
            passwordField.setMinHeight("300px");
            passwordField.setRevealButtonVisible(false);
            return passwordField;
        } else {
            TextArea txtArea = new TextArea(this.getName(), this.getValue(), "...");  
            txtArea.setWidthFull();
            txtArea.setMinHeight("300px");
            return txtArea;
        }
    }

    @Override
    public boolean supportsAdvancedEditor() {
        return true;
    }

    @Override
    public AbstractField getInplaceEditor() {
        if (masked) {
            PasswordField passwordField = new PasswordField();
            passwordField.setSizeFull();
            passwordField.setRevealButtonVisible(false);
            return passwordField;
        } else {
            TextField txtPropertyEditor = new TextField();
            txtPropertyEditor.setSizeFull();
            return txtPropertyEditor;
        }
    }

    @Override
    public String getAsString() {
        return getValue() == null || getValue().isEmpty() ? getDefaultValue() : masked ? "***********" : getValue();
    }
    
    @Override
    public boolean supportsInplaceEditor() {
        return true;
    }

    @Override
    public String getDefaultValue() {
       return AbstractProperty.NULL_LABEL;
    }
}
