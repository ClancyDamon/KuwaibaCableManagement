/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.queries.filters;

import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ResultRecord;
import org.neotropic.util.visual.exporters.grid.DataGridParser;

/**
 * Transforms the given list of result records to a new matrix
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class ResultRecordParser  implements DataGridParser {

    @Override
    public String[][] getData(Object dataSource) {
            List<ResultRecord> records = (List<ResultRecord>) dataSource;
            int i = 0, j = 0;
            if (records.size() > 0) {
                String [][] data = new String [records.size()][1 + records.get(0).getExtraColumns().size()];
                for (i = 0;  i < records.size(); i++) {
                    ResultRecord rec = records.get(i);
                    if (i == 0)
                        data[i][0] = "Default Column";
                    else
                        data[i][0] = rec.toString();
                    for (j = 0;  j < rec.getExtraColumns().size(); j++) {
                        String col = rec.getExtraColumns().get(j);
                        data[i][j + 1] = col;                             
                    }
                }
                return data;
            }
            return new String [i][j];
        }
    }


