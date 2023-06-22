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
 * POJO wrapper of a <b>button</b> element in a Form Artifact Definition.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ElementButton extends AbstractElement<Object> {
    /**
     * Value of the attribute <b>caption</b> in the button element
     */
    private String caption;
    
    public ElementButton() {
    }
    
    public void setCaption(String caption) {
        this.caption = caption;        
    }
    
    public String getCaption() {
        return caption;
    }
    
    @Override
    public void onUiElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONCLICK.equals(event.getEventName()) && 
            hasEventAttribute(Constants.EventAttribute.ONCLICK) && 
            getEvents().get(Constants.EventAttribute.ONCLICK) != null) {
            
            for (String key : getEvents().get(Constants.EventAttribute.ONCLICK).keySet()) {
                
                if (Constants.Function.OPEN.equals(key)) {

                    String elementId = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.OPEN).get(0);

                    AbstractElement anElement = getFormStructure().getElementById(elementId);

                    if (anElement instanceof ElementSubform) {
                        ElementSubform subform = (ElementSubform) anElement;
                        
                        for (AbstractElement child : subform.getChildrenRecursive())
                            child.fireOnLoad();
                                                
                        anElement.fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONCLICK, Constants.Function.OPEN));
                    }
                    
                    if (anElement instanceof ElementMiniApplication) {
                        ElementMiniApplication miniApp = (ElementMiniApplication) anElement;
                        miniApp.fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONCLICK, Constants.Function.OPEN));
                    }
                }
                else if (Constants.Function.ADD_GRID_ROW.equals(key)) {
                    List<String> functionParams = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.ADD_GRID_ROW);

                    String elementId = functionParams.get(0);
                    
                    List<Object> rowValues = new ArrayList();

                    for (int i = 1; i < functionParams.size(); i += 1) {
                        AbstractElement ae = getFormStructure().getElementById(functionParams.get(i));

                        if (ae instanceof AbstractElementField) {
                            AbstractElementField aef = (AbstractElementField) ae;
                            rowValues.add(aef.getValue() != null ? aef.getValue() : new NullObject());
                        } else
                            rowValues.add(functionParams.get(i));
                    }
                    AbstractElement anElement = getFormStructure().getElementById(elementId);

                    if (anElement instanceof ElementGrid) {
                        ((ElementGrid) anElement).addRow(rowValues);
                        anElement.fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.ROWS, rowValues, null));
                        anElement.firePropertyChangeEvent();
                    }

                }
                else if (Constants.Function.ADD_GRID_ROWS.equals(key)) {
                    List<String> functionParams = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.ADD_GRID_ROWS);

                    String elementId = functionParams.get(0);
                    
                    AbstractElement anElement = getFormStructure().getElementById(elementId);

                    if (anElement instanceof ElementGrid) {
                        ElementGrid grid = (ElementGrid) anElement;
                        // Removing the first parameter
                        List<String> list = new ArrayList();
                                                                        
                        for (int i = 1; i < functionParams.size(); i += 1)
                            list.add(functionParams.get(i));
                                                                        
                        grid.loadValue(list);
                    }
                    
                } 
                else if (Constants.Function.EDIT_GRID_ROW.equals(key)) {
                    
                    List<String> functionParams = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.EDIT_GRID_ROW);

                    String elementId = functionParams.get(0);
                    
                    List<Object> rowValues = new ArrayList();

                    for (int i = 1; i < functionParams.size(); i += 1) {
                        AbstractElement ae = getFormStructure().getElementById(functionParams.get(i));

                        if (ae instanceof AbstractElementField) {
                            AbstractElementField aef = (AbstractElementField) ae;
                            if (aef instanceof ElementUpload) {
                                ElementUpload elementUpload = (ElementUpload) aef;
                                
                                if (elementUpload.getCaption() != null && elementUpload.getValue() != null) {
                                    
                                    FileInformation fileInfo = new FileInformation(
                                        elementUpload.getCaption(), 
                                        (String) elementUpload.getValue());
                                    
                                    rowValues.add(fileInfo);                                
                                }
                            } else
                                rowValues.add(aef.getValue() != null ? aef.getValue() : new NullObject());
                        } else
                            rowValues.add(functionParams.get(i));
                    }
                    AbstractElement anElement = getFormStructure().getElementById(elementId);

                    if (anElement instanceof ElementGrid) {
                        
                        ElementGrid grid = (ElementGrid) anElement;
                        
                        grid.editRow(rowValues, grid.getSelectedRow());
                        
                        anElement.fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.ROWS, rowValues, null));
                        anElement.firePropertyChangeEvent();
                    }
                    
                } else if (Constants.Function.DELETE_GRID_ROW.equals(key)) {
                    List<String> functionParams = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.DELETE_GRID_ROW);
                    
                    String elementId = functionParams.get(0);                    
                    
                    AbstractElement anElement = getFormStructure().getElementById(elementId);

                    if (anElement instanceof ElementGrid) {
                        ElementGrid grid = (ElementGrid) anElement;
                        
                        grid.removeRow(grid.getSelectedRow());
                        
                        anElement.fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.ROWS, null, null));
                        anElement.firePropertyChangeEvent();
                    }
                    
                } else if (Constants.Function.CLOSE.equals(key)) {

                    String elementId = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.CLOSE).get(0);

                    AbstractElement anElement = getFormStructure().getElementById(elementId);

                    if (anElement instanceof ElementSubform)
                        anElement.fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONCLICK, Constants.Function.CLOSE));
                    
                } else if (Constants.Function.CLEAN.equals(key)) {
                    
                    String elementId = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.CLEAN).get(0);

                    AbstractElement anElement = getFormStructure().getElementById(elementId);

                    if (anElement instanceof AbstractElementContainer) {
                        ((AbstractElementContainer) anElement).clean();
                        anElement.fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONCLICK, Constants.Function.CLEAN));
                    }
                } else if (Constants.Function.SAVE.equals(key)) {
                    fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONCLICK, Constants.Function.SAVE));
                } else if (key != null && key.contains(Constants.Function.PROPERTY_CHANGE)) {
                    List<String> propertyChangeLine = getEvents().get(Constants.EventAttribute.ONCLICK).get(key);
                    
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
                        }
                    }
                }
                else {
                    Runner runner = getFormStructure().getElementScript().getFunctionByName(key);
                                        
                    if (runner != null) {
                        
                        List<String> parameterValues = getEvents().get(Constants.EventAttribute.ONCLICK).get(key);
                        
                        List parameters = new ArrayList();

                        for (String parameterValue : parameterValues) {
                            AbstractElement anElement = getFormStructure().getElementById(parameterValue);
                            
                            if (anElement == null) {
                                if (getFormStructure().getElementScript() != null && 
                                    getFormStructure().getElementScript().getFunctions() != null) {

                                    if (getFormStructure().getElementScript().getFunctions().containsKey(parameterValue)) {

                                        Runner paramRunner = getFormStructure().getElementScript().getFunctions().get(parameterValue);

                                        if (paramRunner != null) {
                                            parameters.add(paramRunner);
                                            continue;
                                        }
                                    }
                                }
                            } 
                            parameters.add(anElement != null ? anElement : parameterValue);
                        }
                        runner.run(parameters);
                    }
                }
            }
        }
    }
    
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
        setCaption(reader);
    }
    
    private void setCaption(XMLStreamReader reader) {
        caption = reader.getAttributeValue(null, Constants.Attribute.CAPTION);
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.BUTTON;       
    }
    
}
