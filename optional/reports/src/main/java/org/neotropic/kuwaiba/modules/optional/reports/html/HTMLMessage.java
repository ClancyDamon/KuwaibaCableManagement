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
 * A simple formated <code>span</code> tag.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class HTMLMessage extends HTMLComponent {
        
    private String message;

    public HTMLMessage(String message) {
        this.message = message;
    }

    public HTMLMessage(String style, String cssClass, String message) {
        super(style, cssClass);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String asHTML() {
        return new StringBuilder().append("<span").append(style == null ? "" : " style=\"" + style + "\"")   //NOI18N
                                    .append(cssClass == null ? "" : " class=\"" + cssClass + "\"").append(">")  //NOI18N
                                    .append(message).append("</span>").toString(); //NOI18N
    }

}
