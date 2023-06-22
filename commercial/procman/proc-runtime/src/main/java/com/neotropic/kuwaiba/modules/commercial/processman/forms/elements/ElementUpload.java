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
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * POJO wrapper of a <b>upload</b> element in a Form Artifact Definition.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ElementUpload extends AbstractElementField<Object> {
    public static final String ELEMENT_UPLOAD_URL = "elementUploadUrl";
    private String caption;
    private String elementUploadUrl;
        
    public ElementUpload() {
    }
    
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;        
    }
    
    public String getElementUploadUrl() {
        return elementUploadUrl;
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.UPLOAD;
    }
    
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
    }
    
    private void setCaption(XMLStreamReader reader) {
        caption = reader.getAttributeValue(null, Constants.Attribute.CAPTION);
    }
    
    @Override
    public void onUiElementEvent(EventDescriptor event) {
        if (hasEventAttribute(Constants.EventAttribute.ONUPLOADSUCCEEDED) &&  Constants.EventAttribute.ONUPLOADSUCCEEDED.equals(event.getEventName())) {
            
            for (String key : getEvents().get(Constants.EventAttribute.ONUPLOADSUCCEEDED).keySet()) {
                
                if (key != null && key.contains(Constants.Function.PROPERTY_CHANGE)) {
                    List<String> propertyChangeLine = getEvents().get(Constants.EventAttribute.ONUPLOADSUCCEEDED).get(key);
                    
                    if (propertyChangeLine != null && propertyChangeLine.size() >= 3) {
                        
                        String elementId = propertyChangeLine.get(0);
                        String propertyName = propertyChangeLine.get(1);
                        String functionName = propertyChangeLine.get(2);
                        
                        AbstractElement element = getFormStructure().getElementById(elementId);
                        Runner runner = getFormStructure().getElementScript().getFunctionByName(functionName);
                                                
                        if (element != null && runner != null && element.hasProperty(propertyName)) {
                            
                            List parameterValues = new ArrayList();

                            for (int i = 3; i < propertyChangeLine.size(); i += 1) {
                                AbstractElement anElement = getFormStructure().getElementById(propertyChangeLine.get(i));

                                if (anElement == null) {
                                    if (getFormStructure().getElementScript() != null && 
                                        getFormStructure().getElementScript().getFunctions() != null) {

                                        if (getFormStructure().getElementScript().getFunctions().containsKey(propertyChangeLine.get(i))) {

                                            Runner paramRunner = getFormStructure().getElementScript().getFunctions().get(propertyChangeLine.get(i));

                                            if (paramRunner != null) {
                                                parameterValues.add(paramRunner);
                                                continue;
                                            }
                                        }
                                    }
                                }
                                parameterValues.add(anElement != null ? anElement : propertyChangeLine.get(i));
                            }
                            Object newValue = runner.run(parameterValues);
                            Object oldValue = element.getPropertyValue(propertyName);

                            element.onUiElementEvent(new EventDescriptor(
                                Constants.EventAttribute.ONPROPERTYCHANGE, 
                                propertyName, 
                                newValue, 
                                oldValue
                            ));
                            
                            element.fireElementEvent(new EventDescriptor(
                                Constants.EventAttribute.ONPROPERTYCHANGE, 
                                propertyName, 
                                newValue, 
                                oldValue
                            ));
//                            element.firePropertyChangeEvent();
                        }
                    }
                }                
            }
        }
        else if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            if (Constants.Property.CAPTION.equals(event.getPropertyName())) {
                
                if (event.getNewValue() instanceof String) {
                                        
                    setCaption((String) event.getNewValue());
                    firePropertyChangeEvent();
                }
            }
            if (ELEMENT_UPLOAD_URL.equals(event.getPropertyName())) {
                
                if (event.getNewValue() instanceof String) {
                    elementUploadUrl = (String) event.getNewValue();
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
            Object newValue = runner.run(parameters);

            setCaption((String) newValue);

            fireElementEvent(new EventDescriptor(
                Constants.EventAttribute.ONPROPERTYCHANGE, 
                Constants.Property.CAPTION, newValue, null));
        }
    }
        
    @Override
    public void fireOnLoad() {
        super.fireOnLoad(); 
        
        if (hasProperty(Constants.EventAttribute.ONLOAD, Constants.Property.CAPTION)) {
            
            List<String> list = getEvents().get(Constants.EventAttribute.ONLOAD).get(Constants.Property.CAPTION);
            
            loadValue(list);
        }                        
    }
}
