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
public class ElementColumn extends AbstractElement {
    private String caption;
    
    public ElementColumn() {
    }
    
    public void setCaption(String caption) {
        this.caption = caption;        
    }
    
    public String getCaption() {
        return caption;
    }
    
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
        String attrValue = reader.getAttributeValue(null, Constants.Attribute.CAPTION);
                
        if (attrValue == null)
            throw new XMLStreamException(String.format("Missing attribute %s in tag %s", Constants.Attribute.CAPTION, Constants.Tag.COLUMN));

        caption = attrValue;
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.COLUMN;
    }

    @Override
    public String toString() {
        return caption != null ? caption : "";
    }
}
