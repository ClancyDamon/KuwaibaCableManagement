/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.modules.optional.reports.html;

/**
 * A simple HTML table cell (<code>td</code>).
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class HTMLColumn extends HTMLComponent {
    private Object content;
    private String rowspan;
    private String colspan;

    public HTMLColumn(Object content) {
        this.content = content;
    }
    
    public HTMLColumn(String style, String cssClass, Object content, String colspan, String rowspan) {
        this(style, cssClass, content);
        this.colspan = colspan;
        this.rowspan = rowspan;
    }

    public HTMLColumn(String style, String cssClass, Object content) {
        super(style, cssClass);
        this.content = content;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
    
    public String getRowspan() {
        return rowspan;
    }
        
    public void setRowspan(String rowspan) {
        this.rowspan = rowspan;
    }
    
    public String getColspan() {
        return colspan;
    }
    
    public void setColspan(String colspan) {
        this.colspan = colspan;
    }

    @Override
    public String asHTML() {
        Object theContent = content;
        
        if (theContent instanceof HTMLComponent)
            theContent = ((HTMLComponent) content).asHTML();
        
        return new StringBuilder().append("<td").append(style == null ? "" : " style=\"" + style + "\"")   //NOI18N
            .append(cssClass == null ? "" : " class=\"" + cssClass + "\"") //NOI18N
            .append(rowspan == null ? "" : " rowspan=\"" + rowspan + "\"") //NOI18N
            .append(colspan == null ? "" : " colspan=\"" + colspan + "\"") //NOI18N
            .append(">")  //NOI18N
            .append(theContent).append("</td>").toString(); //NOI18N
    }
}