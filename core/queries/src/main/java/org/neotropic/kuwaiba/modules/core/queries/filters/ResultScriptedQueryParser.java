/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.queries.filters;

import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ResultRecord;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryResult;
import org.neotropic.util.visual.exporters.grid.DataGridParser;

/**
 * Transforms the given list of result records to a new matrix
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class ResultScriptedQueryParser implements DataGridParser {

    @Override
    public String[][] getData(Object dataSource) {
            ScriptedQueryResult result = (ScriptedQueryResult) dataSource;
            List<List<Object>> records = (List<List<Object>>) result.getRows();
            int i = 0, j = 0;
            if (records.size() > 0) {
                String [][] data = new String [1 + records.size()][records.get(0).size()];
                for (int col = 0; col < result.getColumnsSize(); col ++)
                    if (result.getColumnLabels() != null && result.getColumnLabels().size() == result.getColumnsSize()) 
                        data[0][col] = result.getColumnLabels().get(col);
                    else 
                        data[0][col] = "";
                
                for (i = 0;  i < records.size(); i++) {
                    List<Object> row = records.get(i);
                    for (j = 0;  j < result.getColumnsSize(); j++) {
                        data[i + 1][j] = (String) row.get(j);                             
                    }
                }
                return data;
            }
            return new String [i][j];
        }
    }


