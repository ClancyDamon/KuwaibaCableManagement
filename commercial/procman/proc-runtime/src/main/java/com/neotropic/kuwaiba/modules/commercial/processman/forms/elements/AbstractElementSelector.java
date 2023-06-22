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

/**
 * Selector field element in a Form Artifact Definition.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class AbstractElementSelector extends AbstractElementField<Object> {
    /**
     * Value of the attribute <b>items</b> in the selector field element.
     */
    private List items;
    
    public void setItems(List items) {
        this.items = items;
    }
    
    public List getItems() {
        return items;
    }
    
    private void loadItems(List<String> list) {
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

            setItems((List) newValue);

            fireElementEvent(new EventDescriptor(
                Constants.EventAttribute.ONPROPERTYCHANGE, 
                Constants.Property.ITEMS, newValue, null));
        }
    }
    
    @Override
    public void fireOnLoad() {
        super.fireOnLoad();
        
        if (hasProperty(Constants.EventAttribute.ONLOAD, Constants.Property.ITEMS)) {
            
            List<String> list = getEvents().get(Constants.EventAttribute.ONLOAD).get(Constants.Property.ITEMS);
            
            loadItems(list);
        }                        
    }
    
    @Override
    public void fireOnLazyLoad() {
        super.fireOnLazyLoad();
        
        if (hasProperty(Constants.EventAttribute.ONLAZYLOAD, Constants.Property.ITEMS)) {
            
            List<String> list = getEvents().get(Constants.EventAttribute.ONLAZYLOAD).get(Constants.Property.ITEMS);
            loadItems(list);
        }
    }
    
    @Override
    public boolean hasProperty(String propertyName) {
        switch (propertyName) {
            case Constants.Property.ITEMS:
                return true;
            default:
                return super.hasProperty(propertyName);
        }
    }
    
    @Override
    public Object getPropertyValue(String propertyName) {
        switch (propertyName) {
            case Constants.Property.ITEMS:
                return getItems();
            default:
                return super.getPropertyValue(propertyName);
        }
    }  
}
