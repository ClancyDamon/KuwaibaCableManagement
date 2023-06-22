/*
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
 * 
 */
package org.neotropic.kuwaiba.modules.optional.reports.html;

/**
 * A simple HTML h1, h2, h3, ... tag.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class HTMLHx extends HTMLComponent {
    private String text;
    private int x;
    
    /**
     * Default constructor
     * @param x 1 for H1 tags, 2 for H2, etc
     * @param text The enclosed text
     */
    public HTMLHx(int x, String text) {
        this.text = text;
        this.x = x;
    }
    
    public HTMLHx(String style, String cssClass, int x, String text) {
        super(style, cssClass);
        this.x = x;
        this.text = text;
    }
    
    public String getContent() {
        return text;
    }

    public void setContent(String text) {
        this.text = text;
    }

    @Override
    public String asHTML() {
        return new StringBuilder().append("<h").append(x).append("").append(style == null ? "" : " style=\"" + style + "\"")   //NOI18N
                                  .append(cssClass == null ? "" : " class=\"" + cssClass + "\"").append(">")  //NOI18N
                                  .append(text).append("</h1>").toString(); //NOI18N
    }
    
}
