/**
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.modules.optional.reports.html;

/**
 * A simple HTML div tag.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class HTMLDiv extends HTMLComponent {
    /**
     * Div attribute
     */
    private String id;
    private String divClass;
    private Object content;
    
    public HTMLDiv(String id) {
        this.id = id;
    }
    
    public HTMLDiv(String id, Object content) {
        this.id = id;
        this.content = content;
    }
    
    public HTMLDiv(String id, String divClass, Object content) {
        this.id = id;
        this.divClass = divClass;
        this.content = content;
    }
    
    public HTMLDiv(String style, String cssClass, String id, Object content) {
        super(style, cssClass);
        this.id = id;
        this.content = content;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;                
    }

    @Override
    public String asHTML() {
        Object theContent = content;
        
        if (theContent instanceof HTMLComponent)
            theContent = ((HTMLComponent) content).asHTML();
        
        return new StringBuilder().append("<div ") //NOI18N
            .append(style == null ? "" : " style=\"" + style + "\"") //NOI18N
            .append(divClass == null ? "" : " class=\"" + divClass + "\"") //NOI18N
            .append(id == null ? "" : " id=\"" + id + "\"").append(">") //NOI18N
            .append(theContent == null ? "" : theContent)
            .append("</div>").toString(); //NOI18N
    }
}
