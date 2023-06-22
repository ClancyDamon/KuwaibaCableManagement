/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.ipAddr
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ipam.visual;

import com.neotropic.flow.component.gcharts.GoogleChart;
import com.neotropic.flow.component.gcharts.GoogleChartColumn;
import com.neotropic.flow.component.gcharts.GoogleChartColumnObject;
import com.neotropic.flow.component.gcharts.GoogleChartColumnType;
import com.neotropic.flow.component.gcharts.GoogleChartOptions;
import com.neotropic.flow.component.gcharts.GoogleChartRow;
import com.neotropic.flow.component.gcharts.GoogleChartType;
import com.neotropic.kuwaiba.modules.commercial.ipam.IpamService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Creates a Subnet usage chart
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class ChartSubnetUsage extends VerticalLayout{
    /**
     * Reference to the i18n service
     */
    private final TranslationService ts;

    public ChartSubnetUsage(String subnetId, String subnetClassName, IpamService ipamService, BusinessEntityManager bem, TranslationService ts){
        this.ts = ts;
        try{ 
            long busy = ipamService.getSubnetIpAddrsInUse(subnetId, subnetClassName).size();
            long reserved = ipamService.getSubnetIpAddrsReserved(subnetId, subnetClassName).size();
            long hosts = Integer.valueOf(bem.getAttributeValueAsString(subnetClassName, subnetId, Constants.PROPERTY_HOSTS));
            long free = hosts - busy - reserved; 
            this.setId("chart-wrapper");
            this.setSpacing(false);
            this.setMargin(false);
            this.setPadding(false);
            this.add(createChart(busy, reserved, free));


        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            this.add(createChart(0, 0, 100));
        }
    }
    
    private Component createChart(double busy, double reserved, double free){
        
        /**
         * The example consists of:
         * --two columns that containing the label and the column type
         * --three rows of items that containing their name and value
         * --a title.
         *
         * Format required by the component:
         *  cols='[{"label":"Name", "type":"string"},{"label":"Number", "type":"number"}]'
         *  rows='[["Monitors", 25],["Printers", 28],["keyboards", 31]]'
         *  options='{\"title\": \"This is the chart title\"}'
         */
            
        // --> Init Columns
        GoogleChartColumn column1 = new GoogleChartColumn("Name", GoogleChartColumnType.STRING);
        GoogleChartColumn column2 = new GoogleChartColumn("Number", GoogleChartColumnType.NUMBER);

        List<GoogleChartColumn> listCols = new ArrayList<>();
        listCols.add(column1);
        listCols.add(column2);
        // End Columns <--

        // --> Init Rows
        // First item
        GoogleChartColumnObject item1Column1 = new GoogleChartColumnObject(column1
                , ts.getTranslatedString("module.ipam.states.ip-addr.busy")
                , ts.getTranslatedString("module.ipam.states.ip-addr.busy"));
        GoogleChartColumnObject item1Column2 = new GoogleChartColumnObject(column2, busy);
        // Second item
        GoogleChartColumnObject item2Column1 = new GoogleChartColumnObject(column1
                , ts.getTranslatedString("module.ipam.states.ip-addr.reserved")
                , ts.getTranslatedString("module.ipam.states.ip-addr.reserved"));
        GoogleChartColumnObject item2Column2 = new GoogleChartColumnObject(column2, reserved);
        // third item
        GoogleChartColumnObject item3Column1 = new GoogleChartColumnObject(column1
                , ts.getTranslatedString("module.ipam.states.ip-addr.free")
                , ts.getTranslatedString("module.ipam.states.ip-addr.free"));
        GoogleChartColumnObject item3Column2 = new GoogleChartColumnObject(column2, free);
        
        List<GoogleChartColumnObject> listObject1 = new ArrayList<>();
        listObject1.add(0, item1Column1);
        listObject1.add(1, item1Column2);

        List<GoogleChartColumnObject> listObject2 = new ArrayList<>();
        listObject2.add(0, item2Column1);
        listObject2.add(1, item2Column2);

        List<GoogleChartColumnObject> listObject3 = new ArrayList<>();
        listObject3.add(0, item3Column1);
        listObject3.add(1, item3Column2);
        
        List<GoogleChartRow> listRows = new ArrayList();
        listRows.add(new GoogleChartRow(listObject1));
        listRows.add(new GoogleChartRow(listObject2));
        listRows.add(new GoogleChartRow(listObject3));
        // End Rows <--

        // Ttile
        HashMap<String, Object> options = new HashMap<>();
        options.put("pieHole", "0.2");
        
        options.put("legend", new HashMap<String, String>() {{
            put("position", "top");
        }});
        
        options.put("chartArea", new HashMap<String, String>() {{
            put("width", "80px");
            put("height", "30px");
        }});
        
        options.put("colors", new String[]{"#58ACFA", "#9E9E9E", "#5bb327"});
        GoogleChartOptions option = new GoogleChartOptions(options);
        // Pie Chart
        GoogleChart donutChart = new GoogleChart(GoogleChartType.PIECHART, listCols, listRows, option);
        return donutChart;
    }
}
