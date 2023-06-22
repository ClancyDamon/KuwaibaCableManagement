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
 * 
 */
package org.neotropic.kuwaiba.modules.optional.reports.html;

/**
 * A simple HTML th tag.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class HTMLColumnHeader extends HTMLColumn {
    
    public HTMLColumnHeader(Object content) {
        super(content);
    }
    
    public HTMLColumnHeader(String style, String cssClass, Object content, String colspan, String rowspan) {
        super(style, cssClass, content, colspan, rowspan);
    }

    public HTMLColumnHeader(String style, String cssClass, Object content) {
        super(style, cssClass, content);
    }

    @Override
    public String asHTML() {
        String html = super.asHTML();
        html = html.replace("<td", "<th");
        html = html.replace("td>", "th>");
        return html;
    }
}
