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

package org.neotropic.kuwaiba.modules.optional.reports.plugins.gcharts;

import java.util.Random;
import org.neotropic.kuwaiba.modules.optional.reports.html.HTMLDiv;
import org.neotropic.kuwaiba.modules.optional.reports.html.HTMLReport;
import org.neotropic.kuwaiba.modules.optional.reports.javascript.DataTable;

/**
 * Factory to create charts
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GChartsFactory {
    private final HTMLReport report;
    /**
     * Used to load the load library
     */
    private final String googleChartsLibrary = "https://www.gstatic.com/charts/loader.js"; //NOI18N
    /**
     * Used to load the packages
     */
    private final String loadGoogleChartsLib = "google.charts.load('current', {'packages':['corechart']});"; //NOI18N
    
    public GChartsFactory(HTMLReport report) {
        this.report = report;
        report.getLinkedJavascriptFiles().add(googleChartsLibrary);
        report.getEmbeddedJavascript().add(loadGoogleChartsLib);
    }
    
    /**
     * @param chartType Type of chart
     * @param chartDivId An id to render the chart
     * @param chartTitle The title of the chart
     * @param data The data to show in the chart
     * @return A HTML Div tag that wrapped the chart
     */
    public HTMLDiv createHTMLDivWrapperChart(ChartType chartType, String chartDivId, String chartTitle, DataTable data) {
        Random random = new Random();
        
        String functionName = "createChartFunction" + random.nextInt(1000); //NOI18N
        HTMLDiv htmlDiv = new HTMLDiv(chartDivId);
                
        DrawGChartFunction drawChartFunction = new DrawGChartFunction(functionName, chartTitle, data, chartType, htmlDiv);
        report.getEmbeddedJavascript().add("google.charts.setOnLoadCallback(" + drawChartFunction.getFunctionName() + ");"); //NOI18N
        
        report.getEmbeddedJavascript().add(drawChartFunction.toString());
        return htmlDiv;
    }
    
    /**
     * Enum of the available type of charts 
     */
    public enum ChartType {
        PIECHART ("PieChart"), //NOI18N
        LINECHART ("LineChart"), //NOI18N
        COLUMNCHART ("ColumnChart"); //NOI18N
        
        private final String value;
        
        private ChartType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}
