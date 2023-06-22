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

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FormStructure {
    private String version;
    private final HashMap<String, AbstractElement> elementsById;
    private final List<AbstractElement> elements;
    private final ElementScript elementScript;
    private final ElementI18N elementI18N;
    
    public FormStructure(List<AbstractElement> elements, ElementScript elementScript, ElementI18N elementI18N) {
        this.elements = elements;
        this.elementScript = elementScript;
        this.elementI18N = elementI18N;
        
        elementsById = new HashMap();
        
        for (AbstractElement element : elements) {
            
            if (element.getId() != null)
                elementsById.put(element.getId(), element);
        }
    }
    
    public AbstractElement getElementById(String elementId) {
        return elementsById.get(elementId);
    }
    
    public List<AbstractElement> getElements() {
        return elements;
    }
    
    public ElementScript getElementScript() {
        return elementScript;
    }
        
    public ElementI18N getElementI18N() {
        return elementI18N;
    }
    
    public String getVersion() {
        return version;                
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
}
