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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ElementForm extends AbstractElementContainer {
    private String title;
    private String formId;
    
    public ElementForm() {
    }
    
    public void setTitle(String title) {
        this.title = title;        
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getFormId() {
        return formId;
    }
    
    public void setFormId(String formId) {
        this.formId = formId;
    }

    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
        
        setTitle(reader);
        setFormId(reader);
    }
    
    private void setTitle(XMLStreamReader reader) {
        title = reader.getAttributeValue(null, Constants.Attribute.TITLE);
    }
    
    private void setFormId(XMLStreamReader reader) {
        formId = reader.getAttributeValue(null, Constants.Attribute.FORM_ID);
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.FORM;       
    }
    
}
