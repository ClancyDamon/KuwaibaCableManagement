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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ElementI18N implements Tag {
    private HashMap<String, HashMap<String, String>> keys;
    private String lang;
    
    public ElementI18N() {
        
    }
    
    public String getMessage(String key, String lang) {
        return keys != null && keys.containsKey(key) ? keys.get(key).get(lang) : null;
    }
    
    public void setLang(String lang) {
        this.lang = lang;        
    }
    
    public String getLang() {
        return lang;
    }
    
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        /*
        <i18n>
            <messages key="">
                <message lang="" value=""></message>
            </messages>
        </i18n>
        */
        QName tagMessageKey = new QName(Constants.Tag.MESSAGES);
        QName tagMessageValue = new QName(Constants.Tag.MESSAGE);
        QName tagI18N = new QName(Constants.Tag.I18N);
        
        if (keys == null)
            keys = new HashMap();
        
        while (true) {
            reader.nextTag();
            
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                
                if (reader.getName().equals(tagMessageKey)) {
                    String attrValue = reader.getAttributeValue(null, Constants.Attribute.KEY);
                    
                    if (attrValue == null)
                        throw new XMLStreamException(String.format("Missing attribute %s in tag %s", Constants.Attribute.KEY, Constants.Tag.MESSAGES));
                    
                    String key = attrValue;
                    HashMap<String, String> messages = new HashMap();
                    
                    while (true) {
                        
                        reader.nextTag();

                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {

                            if (reader.getName().equals(tagMessageValue)) {
                                
                                attrValue = reader.getAttributeValue(null, Constants.Attribute.LANG);
                                if (attrValue == null)
                                    throw new XMLStreamException(String.format("Missing attribute %s in tag %s", Constants.Attribute.VALUE, Constants.Tag.MESSAGE));
                                
                                String lang = attrValue;
                                
                                attrValue = reader.getAttributeValue(null, Constants.Attribute.VALUE);
                                if (attrValue == null)
                                    throw new XMLStreamException(String.format("Missing attribute %s in tag %s", Constants.Attribute.VALUE, Constants.Tag.MESSAGE));
                                
                                String value = attrValue;
                                
                                messages.put(lang, value);
                            }
                        } 
                        
                        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                            
                            if (reader.getName().equals(tagMessageKey))
                                break;
                        }
                    }
                    keys.put(key, messages);
                }
            }
            if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                
                if (reader.getName().equals(tagI18N))
                    break;
            }
        }
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.I18N;
    }
}
