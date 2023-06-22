/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.core.apis.persistence.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;

/**
 * A script query result its the return of the execute of a {@link ScriptingQuery#getScript()}
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ScriptedQueryResult {
    /**
     * Number of columns in the scripted query result.
     */
    private int columnsSize;
    /**
     * Set of column ids.
     */
    private List<String> columnLabels;
    /**
     * Set of column type. Use to fix the class of each cell in a row.
     */
    private List<Class> columnType;
    /**
     * Set of result rows.
     */
    private final List<List<Object>> rows = new ArrayList();
    
    public ScriptedQueryResult(int columnsSize) {
        this.columnsSize = columnsSize;
    }
    
    public ScriptedQueryResult(List<String> columnLabels) {
        if (columnLabels != null) {
            this.columnLabels = columnLabels;
            this.columnsSize = columnLabels.size();
        }
    }
    
    public List<List<Object>> getRows() {
        return rows;
    }
    
    public List<String> getColumnLabels() {
        return columnLabels;
    }

    public int getColumnsSize() {
        return columnsSize;
    }
  
    public void addRow(List<Object> row) throws InvalidArgumentException {
        Objects.requireNonNull(row);
        if (row.size() == columnsSize)
            rows.add(row);
        else
            throw new InvalidArgumentException("persistence.scripted-query-result.add-row.row-size-exception");
    }
}
