/**
 * Shows all invetory licenses that has expired also show how many days of expiration has the license.
 * Neotropic SAS - version 1.0
 * Parameters: None
 * Applies to: GenericLocation
 * @author Adrian Martinez {@literal <adrian.martinez@neotropic.co>}
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

def report = new HTMLReport("Expired Licenses Report", "Neotropic SAS", "1.1");

report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());

def pattern = "E MMM dd HH:mm:ss z yyyy";
def today = use(TimeCategory){new Date()};
def next30DaysDate = use(TimeCategory){new Date() + 30.days};
def next60DaysDate = use(TimeCategory){new Date() + 60.days};
def next90DaysDate = use(TimeCategory){new Date() + 90.days};

def totalPricePerLicense = [:];
def countLicense = [:];
def usageLicense = [:];
def expiredLicense = [:];
def countLicenseVendor = [:];

//We get all the existin licenses in the inventory
def licenses = bem.getObjectsOfClass("GenericSoftwareAsset", -1);

for (license in licenses){
    def licenseCounter = 1;
    def licenseVendorCounter = 1;
    
    def attributes = bem.getAttributeValuesAsString(license.getClassName(), license.getId());
    //We get the number of alloewd licensed devices per license
    def licensedDevices = attributes.get("licensedDevices");
    //counter By Name
    if(countLicense.containsKey(license.getName())){
        licenseCounter += countLicense.get(license.getName()); 
    }
    countLicense.put(license.getName(), licenseCounter); 
    
    //counter By Vendor
    if(countLicenseVendor.containsKey(attributes.get("vendor"))){
        licenseVendorCounter += countLicenseVendor.get(attributes.get("vendor")); 
    }
    countLicenseVendor.put(attributes.get("vendor"), licenseVendorCounter);
    //end by vendor
    
    def devices = bem.getSpecialAttribute(license.getClassName(), license.getId(), "licenseHas");
    usageLicense.put(license, devices);
     
    def input = attributes.get("expirationDate");
    def date = new SimpleDateFormat(pattern).parse(input);
    def expiresIn = "";
    if(today >= date){
        expiredLicense.put(license, devices);
    }
}

//Charts
def chartsFactory = new GChartsFactory(report);

//First chart the PieChart the number of licenses in the inventory
def dataTable = new DataTable([DataType.STRING, DataType.NUMBER] as DataType[], ["Licenses", "Name"] as String[]);

//We create the pie chart data rows licenses names and how many licenses exists in the inventory
countLicense.each{licenseName, numberOfLincensByName ->
    dataTable.addRow([licenseName, numberOfLincensByName.toString()] as String[]);
};

//Pie chart declaration, we set the title
def htmlDivPieChart = chartsFactory.createHTMLDivWrapperChart(ChartType.PIECHART, "divPieChart", "Licenses Inventory", dataTable);
report.getComponents().add(htmlDivPieChart);

//Bar vendor license participatip in inventory
def dataTableVendors = new DataTable([DataType.STRING, DataType.NUMBER] as DataType[], ["Licenses Vendors", "Name"] as String[]);
countLicenseVendor.each{key, vl ->
    dataTableVendors.addRow([key, vl.toString()] as String[]);
};

def htmlDivColumnChart = chartsFactory.createHTMLDivWrapperChart(ChartType.COLUMNCHART, "divColumChart", "Licenses Vendors", dataTableVendors);
report.getComponents().add(htmlDivColumnChart);

report.getComponents().add(new HTMLTable());
report.getComponents().add(new HTMLBr());
//End Charts

def i = 0;
expiredLicense.each{k, devices->
    def attributes = bem.getAttributeValuesAsString(k.getClassName(), k.getId());
    //We calculate how many days of expirations has the license.
    def input = attributes.get("expirationDate");
    def date = new SimpleDateFormat(pattern).parse(input);
    //Notation required by Groovy to compare dates
    use(groovy.time.TimeCategory) {
        def duration = today - date;
        expiresInMsg = " (expired " + duration.days + " days ago)"
    }
    
    def licenseNameTitle = new HTMLMessage("font-weight: 'bold'; font-size: 2em;", null, k.getName());
    def licenseExpirationDaysTitle = new HTMLMessage("font-size: 2em;", "error", expiresInMsg);
    report.getComponents().add(new HTMLDiv("title", licenseNameTitle));
    report.getComponents().add(new HTMLDiv("expired-days", licenseExpirationDaysTitle));
    
    //Header table data
    def usage = (attributes.get("licensedDevices") as Integer) - devices.size();
    def price = attributes.get("price") as Integer;
    def formatPrice = attributes.get("price") + " USD";
    def licenseHeaderTable = new HTMLTable();
    def licensedDevicesOf = "(" + devices.size() + " of " + attributes.get("licensedDevices") + ")";
    
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Vendor"), new HTMLColumn(attributes.get("vendor")) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Individual License Price"), new HTMLColumn(formatPrice) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Purchase Date"), new HTMLColumn(attributes.get("purchaseDate")) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Expiration Date"), new HTMLColumn(attributes.get("expirationDate")) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Licensed Devices"), new HTMLColumn(licensedDevicesOf) ] as HTMLColumn[]));
    //We add the table to the report
    report.getComponents().add(licenseHeaderTable);
    
    //We only add the Device table if the license has devices related
    if(!devices.isEmpty()){
        def deviceExpiredLicenseTable = new HTMLTable(null, null, ["Name", "Location"] as String[]);
        devices.each{de ->
            def parents = bem.getParentsUntilFirstOfClass(de.getClassName(), de.getId(), "City");
            def location = [];
            parents.each{ p ->
                location.add(p.toString());
            }
            deviceExpiredLicenseTable.getRows().add(new HTMLRow(null, (i % 2 == 0 ? "odd" : "even"),
        	      [ new HTMLColumn(de.getName()),
        	        new HTMLColumn(location.join("< ") as String)] as HTMLColumn[]));
            i++;
        }
        report.getComponents().add(deviceExpiredLicenseTable);
    }
    else{
        report.getComponents().add(new HTMLHx(3, "No devices related"));
    }
    report.getComponents().add(new HTMLBr());
};

//Return the report
report;