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
package org.neotropic.kuwaiba.modules.optional.reports.javascript;

import java.util.ArrayList;
import java.util.List;

/**
 * A data table is a matrix used represent the data set to be consumed by a chart.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DataTable {
    /**
     * An array to store the type of each column of the table.
     */
    private DataType[] types;
    /**
     * An array to store the label for the column.
     */
    private String[] labels;
    /**
     * The actual data set.
     */
    private List<String[]> rows;
    /**
     * The colors of each subset of data.
     */
    private String[] colors;
    
    public DataTable(DataType[] types, String[] labels) {
        if (types.length != labels.length)
            return;
        
        rows = new ArrayList();
        
        this.types = types;
        this.labels = labels;
    }
    
    public DataTable(DataType[] types, String[] labels, String [] colors) {
        if (types.length != labels.length)
            return;
        
        rows = new ArrayList();
        
        this.types = types;
        this.labels = labels;
        this.colors = colors;
    }
    
    public void addRow(String[] row) {
        if (row.length != labels.length)        
            return;
        
        rows.add(row);
    }

    public DataType[] getTypes() {
        return types;
    }

    public void setTypes(DataType[] types) {
        this.types = types;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public String[] getColors() {
        return colors;
    }

    public void setColors(String[] colors) {
        this.colors = colors;
    }

    public List<String[]> getRows() {
        return rows;
    }

    public void setRows(List<String[]> rows) {
        this.rows = rows;
    }
    
    public static String cellValue(DataType dataType, String value) {
        if (dataType == DataType.NUMBER)
            return value;
        
        if (dataType == DataType.STRING)
            return String.format("'%s'", value);
        //TODO: The conditions to the rest of data types
        return "";
    }
    /**
     * Enum of the possible data types support by the cells
     */
    public enum DataType {
        STRING ("string"), //NOI18N
        NUMBER ("number"), //NOI18N
        BOOLEAN ("boolean"), //NOI18N
        DATE ("date"), //NOI18N
        DATETIME ("datetime"), //NOI18N
        TIMEOFDAY ("timeofday"); //NOI18N
        
        private final String value;
        
        private DataType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}
