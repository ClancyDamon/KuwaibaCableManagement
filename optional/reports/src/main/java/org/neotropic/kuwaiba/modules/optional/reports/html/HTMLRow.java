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
 * A simple HTML table row (<code>tr</code>)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class HTMLRow extends HTMLComponent {
    private HTMLColumn[] columns;

    public HTMLRow(HTMLColumn[] columns) {
        this.columns = columns;
    }
    
    public HTMLRow(String cssClass, HTMLColumn[] columns) {
        setCssClass(cssClass);
        this.columns = columns;
    }

    public HTMLRow(String style, String cssClass, HTMLColumn[] columns) {
        super(style, cssClass);
        this.columns = columns;
    }

    public HTMLColumn[] getColumns() {
        return columns;
    }

    public HTMLRow setColumns(HTMLColumn[] columns) {
        this.columns = columns;
        return this;
    }

    @Override
    public String asHTML() {
        StringBuilder builder = new StringBuilder();
        builder.append("<tr").append(style == null ? "" : " style=\"" + style + "\"") //NOI18N
                .append(cssClass == null ? "" : " class=\"" + cssClass + "\"").append(">"); //NOI18N
        for (HTMLColumn column : columns)
            builder.append(column.asHTML());
        builder.append("</tr>"); //NOI18N

        return builder.toString();
    }
}
