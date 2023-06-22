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

import java.util.ArrayList;
import java.util.List;

/**
 * A simple HTML table.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class HTMLTable extends HTMLComponent {
        
    private String[] columnHeaders;
    private List<HTMLRow> rows;

    {
        this.rows = new ArrayList<>();
    }

    public HTMLTable(String[] columnHeaders) {
        this.columnHeaders = columnHeaders;

    }

    public HTMLTable(String style, String cssClass, String[] columnHeaders) {
        super(style, cssClass);
        this.columnHeaders = columnHeaders;
    }

    public String[] getColumnHeaders() {
        return columnHeaders;
    }

    public void setColumnHeaders(String[] columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

    public List<HTMLRow> getRows() {
        return rows;
    }

    public void setRows(List<HTMLRow> rows) {
        this.rows = rows;
    }

    @Override
    public String asHTML() {
        StringBuilder builder = new StringBuilder();
        builder.append("<table").append(style == null ? "" : " style=\"" + style + "\"") //NOI18N
                .append(cssClass == null ? "" : " class=\"" + cssClass + "\"").append(">"); //NOI18N
        if (columnHeaders != null) {
            builder.append("<tr>"); //NOI18N
            for (String columnHeader : columnHeaders)
                builder.append("<th>").append(columnHeader).append("</th>"); //NOI18N
            builder.append("</tr>"); //NOI18N
        }

        for (HTMLRow row : rows)
            builder.append(row.asHTML());

        builder.append("</table>"); //NOI18N

        return builder.toString();
    }
}