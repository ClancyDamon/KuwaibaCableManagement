/**
 * Showcases how to create different types of charts
 * Neotropic SAS - version 1.1
 * Parameters: None
 */
import org.neotropic.kuwaiba.modules.optional.reports.html.HTMLReport
import org.neotropic.kuwaiba.modules.optional.reports.html.HTMLDiv;
import org.neotropic.kuwaiba.modules.optional.reports.javascript.DataTable
import org.neotropic.kuwaiba.modules.optional.reports.javascript.DataTable.DataType;
import org.neotropic.kuwaiba.modules.optional.reports.plugins.gcharts.GChartsFactory;
import org.neotropic.kuwaiba.modules.optional.reports.plugins.gcharts.GChartsFactory.ChartType;

def report = new HTMLReport("Sample Report with Charts", "Neotropic SAS", "1.1");

def dataTable = new DataTable([DataType.STRING, DataType.NUMBER] as DataType[], ["Column 1", "Column 2"] as String[]);
dataTable.addRow(["Row 1", "20"] as String[]);
dataTable.addRow(["Row 2", "30"] as String[]);
dataTable.addRow(["Row 3", "50"] as String[]);
def chartsFactory = new GChartsFactory(report);
def htmlDivPieChart = chartsFactory.createHTMLDivWrapperChart(ChartType.PIECHART, "divPieChart", "Chart Pie Sample", dataTable);
def htmlDivLineChart = chartsFactory.createHTMLDivWrapperChart(ChartType.LINECHART, "divLinechart", "Chart Line Sample", dataTable);
def htmlDivColumnChart = chartsFactory.createHTMLDivWrapperChart(ChartType.COLUMNCHART, "divColumChart", "Chart Column Sample", dataTable);

report.getComponents().add(htmlDivPieChart);
report.getComponents().add(htmlDivLineChart);
report.getComponents().add(htmlDivColumnChart);

//Return the report
report;
