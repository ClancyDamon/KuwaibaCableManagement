/**
 * Calculates the usage of a given software license.
 * Neotropic SAS - version 1.0
 * Parameters: None
 * Applies to: GenericSoftwareAsset
 */
import org.neo4j.graphdb.Direction;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.neotropic.kuwaiba.modules.optional.reports.html.*;
import groovy.time.TimeCategory;
import java.text.SimpleDateFormat;
import org.neotropic.kuwaiba.modules.optional.reports.html.HTMLReport
import org.neotropic.kuwaiba.modules.optional.reports.html.HTMLDiv;
import org.neotropic.kuwaiba.modules.optional.reports.javascript.DataTable
import org.neotropic.kuwaiba.modules.optional.reports.javascript.DataTable.DataType;
import org.neotropic.kuwaiba.modules.optional.reports.plugins.gcharts.GChartsFactory;
import org.neotropic.kuwaiba.modules.optional.reports.plugins.gcharts.GChartsFactory.ChartType;

def report = new HTMLReport(String.format("License Usage Report for %s", instanceNode.getProperty("name")),
				"Neotropic SAS", "1.1");

report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());

def license = bem.getObject(objectClassName, objectId);

def pattern = "E MMM dd HH:mm:ss z yyyy";
def patternSimple = "E MMM dd yyyy";
def next30DaysDate = use(TimeCategory){new Date() + 30.days};
def next60DaysDate = use(TimeCategory){new Date() + 60.days};
def next90DaysDate = use(TimeCategory){new Date() + 90.days};

def totalPricePerLicense = [:];
def countLicense = [:];
def count30ExpirationLicenses = [:]; 
def count60ExpirationLicenses = [:]; 
def count90ExpirationLicenses = [:]; 

def headerTable = new HTMLTable();
def licensesTable = new HTMLTable(null, null, ["Device", "Location", "Expiration Date"] as String[]);

def attributes = bem.getAttributeValuesAsString(license.getClassName(), license.getId());
def licensedDevices = attributes.get("licensedDevices") as Integer;
def input = attributes.get("expirationDate");
def date = new SimpleDateFormat(pattern).parse(input);

def devices = bem.getSpecialAttribute(license.getClassName(), license.getId(), "licenseHas");

headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Name"), new HTMLColumn(attributes.get("name")) ] as HTMLColumn[]));
headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Vendor"), new HTMLColumn(attributes.get("vendor")) ] as HTMLColumn[]));
headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Purchased Date"), new HTMLColumn(attributes.get("purchaseDate")) ] as HTMLColumn[]));
headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Expiration Date"), new HTMLColumn(attributes.get("expirationDate")) ] as HTMLColumn[]));
headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Price"), new HTMLColumn(attributes.get("price") + " USD") ] as HTMLColumn[]));
headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Licenced Devices"), new HTMLColumn(attributes.get("licensedDevices")) ] as HTMLColumn[]));
headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Devices"), new HTMLColumn(devices.size()) ] as HTMLColumn[]));

def licenseUsage = (100 * devices.size())/licensedDevices;
def i = 0;

devices.each{d ->
    def parents = bem.getParentsUntilFirstOfClass(d.getClassName(), d.getId(), "City");
    def location = [];
    parents.each{ p ->
        location.add(p.getName());
    }
    
    def exp = new SimpleDateFormat("MMM dd yyyy").format(date) ;
    if(next30DaysDate < date){
        exp = "Expires in less than 30 days";
    }
    else if(next60DaysDate < date){
        exp = "Expires in less than 60 days";
    }
    else if(next90DaysDate < date){
        exp = "Expires in less than 90 days";
    }

    licensesTable.getRows().add(new HTMLRow(null, (i % 2 == 0 ? "even" : "odd"),
	      [ new HTMLColumn(d.getName()),
	      new HTMLColumn(location.join(", ") as String),
	      new HTMLColumn(exp) ] as HTMLColumn[]));
    i++;
}

def dataTable = new DataTable([DataType.STRING, DataType.NUMBER] as DataType[], ["Licenses", "Usage"] as String[]);
dataTable.addRow(["Used", licenseUsage.toString()] as String[]);
dataTable.addRow(["Free", (100 - licenseUsage).toString()] as String[]);

def chartsFactory = new GChartsFactory(report);
def htmlDivPieChart = chartsFactory.createHTMLDivWrapperChart(ChartType.PIECHART, "divPieChart", "Chart Pie Sample", dataTable);

report.getComponents().add(htmlDivPieChart);
report.getComponents().add(headerTable);
report.getComponents().add(priceTable);

//Return the report
report;