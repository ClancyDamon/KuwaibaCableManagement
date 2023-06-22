/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.forms.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A field is a terminal element that contain data
 * @param <T> Type Value
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class AbstractElementField<T> extends AbstractElement<T> {
    /**
     * Value as string of the attribute <b>value</b> in the element field.
     */
    private String valueAttributeValue;
    /**
     * Value of the attribute <b>value</b> in the element field.
     */
    protected T value;
    /**
     * Value of the attribute <b>mandatory</b> in the element field.
     */
    private boolean mandatory = false;
    /**
     * Value of the attribute <b>cleanable</b> in the element field.
     */
    private boolean cleanable = true;
    /**
     * Value of the attribute <b>datatype</b> in the element field.
     */
    private String dataType;
    /**
     * Value of the attribute <b>shared</b> in the element field.
     */
    private boolean shared = false;
    
    public String getValueAttributeValue() {
        return valueAttributeValue;
    }
    
    public void setValueAttributeValue(String valueAttributeValue) {
        this.valueAttributeValue = valueAttributeValue;
    }
    
    public T getValue() {
        return value;        
    }
    
    public void setValue(T value) {
        this.value = value;        
    }
    
    public boolean isMandatory() {
        return mandatory;
    }
    
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;                
    }
    
    public boolean isCleanable() {
        return cleanable;
    }
    
    public void setCleanable(boolean cleanable) {
        this.cleanable = cleanable;        
    }
    
    public String getDataType() {
        return dataType;                
    }
        
    public void setDataType(String dataType) {
        this.dataType = dataType;        
    }
    
    public boolean isShared() {
        return shared;
    }
    
    public void setShared(boolean shared) {
        this.shared = shared;        
    }
    
    @Override
    public void onUiElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            if (event.getNewValue() != null || event.getOldValue() != null) {
                
                if (Constants.Property.VALUE.equals(event.getPropertyName())) {
                    
                    setValue((T) event.getNewValue()); // TODO: rollback
                    
                    if (hasProperty(Constants.Function.VALIDATE, Constants.Property.VALUE)) {
                            
                        List<String> list = getEvents().get(Constants.Function.VALIDATE).get(Constants.Property.VALUE);

                        if (list != null && !list.isEmpty()) {

                            String functionName = list.get(0);

                            ValidatorRunner validator = (ValidatorRunner) getFormStructure().getElementScript().getFunctionByName(functionName);

                            List parameters = new ArrayList();

                            for (int i = 1; i < list.size(); i += 1) {
                                AbstractElement anElement = getFormStructure().getElementById(list.get(i));
                                                                
                                if (anElement == null) {
                                    if (getFormStructure().getElementScript() != null && 
                                        getFormStructure().getElementScript().getFunctions() != null) {

                                        if (getFormStructure().getElementScript().getFunctions().containsKey(list.get(i))) {

                                            Runner paramRunner = getFormStructure().getElementScript().getFunctions().get(list.get(i));

                                            if (paramRunner != null) {
                                                parameters.add(paramRunner);
                                                continue;
                                            }
                                        }
                                    }
                                }
                                parameters.add(anElement != null ? anElement : list.get(i));
                            }
                            
                            Object newValue = validator.run(parameters);
                            if (newValue instanceof Boolean && !((Boolean) newValue)) {
                                if (validator.getMessage() != null) {
                                    String[] messageArray = validator.getMessage().split("\\.");
                                    if (messageArray.length == 2 && Constants.Function.I18N.equals(messageArray[0])) {
                                        String message = (String) getFormStructure().getElementScript().getFunctionByName(Constants.Function.I18N).run(Arrays.asList(messageArray[1]));
                                        validator.getScriptQueryExecutor().openMessage(message);
                                    }
                                    else
                                        validator.getScriptQueryExecutor().openMessage(validator.getMessage());
                                }
                            }
                        }
                    }
                    firePropertyChangeEvent();
                }
            }
        }
        super.onUiElementEvent(event);
    }
    
    private void loadValue(List<String> list) {
        if (list != null && !list.isEmpty()) {

            String functionName = list.get(0);

            Runner runner = getFormStructure().getElementScript().getFunctionByName(functionName);

            List parameters = new ArrayList();

            for (int i = 1; i < list.size(); i += 1) {
                AbstractElement anElement = getFormStructure().getElementById(list.get(i));
                
                if (anElement == null) {
                    if (getFormStructure().getElementScript() != null && 
                        getFormStructure().getElementScript().getFunctions() != null) {
                        
                        if (getFormStructure().getElementScript().getFunctions().containsKey(list.get(i))) {
                            
                            Runner paramRunner = getFormStructure().getElementScript().getFunctions().get(list.get(i));
                            
                            if (paramRunner != null) {
                                parameters.add(paramRunner);
                                continue;
                            }
                        }
                    }
                }
                parameters.add(anElement != null ? anElement : list.get(i));
            }
            
            T newValue = (T) runner.run(parameters);

            setValue(newValue);

            fireElementEvent(new EventDescriptor(
                Constants.EventAttribute.ONPROPERTYCHANGE, 
                Constants.Property.VALUE, newValue, null));
        }
    }
    
    @Override
    public void fireOnLoad() {
        super.fireOnLoad(); 
        
        if (hasProperty(Constants.EventAttribute.ONLOAD, Constants.Property.VALUE)) {
            
            List<String> list = getEvents().get(Constants.EventAttribute.ONLOAD).get(Constants.Property.VALUE);
            
            loadValue(list);
        }                        
    }
    
    @Override
    public void fireOnLazyLoad() {
        super.fireOnLazyLoad();
        
        if (hasProperty(Constants.EventAttribute.ONLAZYLOAD, Constants.Property.VALUE)) {
            
            List<String> list = getEvents().get(Constants.EventAttribute.ONLAZYLOAD).get(Constants.Property.VALUE);
            
            loadValue(list);
        }
    }
            
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
        setValueAttributeValue(reader);
        setMandatory(reader);
        setDataType(reader);
        setShared(reader);
    }
    
    private void setValueAttributeValue(XMLStreamReader reader) {
        valueAttributeValue = reader.getAttributeValue(null, Constants.Attribute.VALUE);        
    }
    
    private void setMandatory(XMLStreamReader reader) {
        String attrValue = reader.getAttributeValue(null, Constants.Attribute.MANDATORY);
                
        if (attrValue != null)
            mandatory = Boolean.valueOf(attrValue);
    }
    
    private void setDataType(XMLStreamReader reader) {
        dataType = reader.getAttributeValue(null, Constants.Attribute.DATA_TYPE);
    }
    
    private void setShared(XMLStreamReader reader) {
        shared = Boolean.valueOf(reader.getAttributeValue(null, Constants.Attribute.SHARED));
    }
    
    @Override
    public void propertyChange() {
        if (hasProperty(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.VALUE)) {
            
            Object oldValue = getValue();
            T newValue = (T) getNewValue(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.VALUE);

            setValue(newValue);

            firePropertyChangeEvent();
            
            fireElementEvent(new EventDescriptor(
                Constants.EventAttribute.ONPROPERTYCHANGE, 
                Constants.Property.VALUE, newValue, oldValue));
        }
        super.propertyChange();
    }
    
    @Override
    public boolean hasProperty(String propertyName) {
        switch (propertyName) {
            case Constants.Property.VALUE:
                return true;
            default:
                return super.hasProperty(propertyName);
        }
    }
    
    @Override
    public Object getPropertyValue(String propertyName) {
        switch (propertyName) {
            case Constants.Property.VALUE:
                return getValue();
            default:
                return super.getPropertyValue(propertyName);
        }
    }       
}