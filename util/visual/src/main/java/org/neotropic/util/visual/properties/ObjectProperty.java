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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.List;
import java.util.function.Function;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;


/**
 * Support for object properties. These properties are not plain values unlike strings or numbers, 
 * these properties are references to other (complex) objects.
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class ObjectProperty extends AbstractProperty {
    /**
     * The whole list of list items available to fill the input combo boxes.
     */
    private List<Object> items;
    private ComponentRenderer componentRenderer;
    private String displayValue;
    private Function<Object, String> functionDisplayValue;
    
    public ObjectProperty(String name, String displayName, String description, Object value, 
            List<Object> items, String type, String displayValue, TranslationService ts) {
        super(name, displayName, description, value, ts);
        this.items = items;
        this.displayValue = displayValue;
        setType(type);
    }

    public ObjectProperty(String name, String displayName, String description, Object value, 
            List<Object> items, String type, String displayValue, TranslationService ts, boolean readOnly, 
            boolean mandatory, boolean unique) {
        super(name, displayName, description, value, ts, readOnly, mandatory, unique);
        this.items = items;
        this.displayValue = displayValue;
        setType(type);
    }
    
    public ObjectProperty(String name, String displayName, String description, Object value, 
            List<Object> items, String type, Function<Object, String> functionDisplayValue, TranslationService ts, boolean readOnly, 
            boolean mandatory, boolean unique) {
        super(name, displayName, description, value, ts, readOnly, mandatory, unique);
        this.items = items;
        this.functionDisplayValue = functionDisplayValue;
        setType(type);
    }
    
    public List getItems() {
        return items;
    }

    public void setItems(List listTypes) {
        this.items = listTypes;
    }
    
    public ComponentRenderer getComponentRenderer() {
        return componentRenderer;
    }

    public void setComponentRenderer(ComponentRenderer componentRenderer) {
        this.componentRenderer = componentRenderer;
    }

    @Override
    public AbstractField getAdvancedEditor() {
        ListBox<Object> lstBoxEditor = new ListBox<>();
        lstBoxEditor.setItems(items);
        lstBoxEditor.setWidthFull();
        lstBoxEditor.setValue(getValue());
        if (componentRenderer != null)
            lstBoxEditor.setRenderer(componentRenderer);
        return lstBoxEditor;
    }

    @Override
    public boolean supportsAdvancedEditor() {
        return true;
    }

    @Override
    public AbstractField getInplaceEditor() {
        ComboBox<Object> cmbListTypes = new ComboBox<>();
        cmbListTypes.setAllowCustomValue(false);
        cmbListTypes.setItems(items);
        cmbListTypes.setWidthFull();
        return cmbListTypes;
    }

    @Override
    public String getAsString() {
        return getValue() == null ? AbstractProperty.NULL_LABEL : functionDisplayValue != null ? functionDisplayValue.apply(getValue()) : getValue().toString();
    }
    
    @Override
    public boolean supportsInplaceEditor() {
        return false;
    }

    @Override
    public BusinessObjectLight getDefaultValue() {
        return null;
    }
    
    public interface ListTypeObject {
        public void hasId();
    }
}