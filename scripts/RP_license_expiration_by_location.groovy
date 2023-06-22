/**
 * Shows all the licenses with an expiration date of 30, 60, 90 days in the inventory.
 * Neotropic SAS - version 1.0
 * Parameters: None
 * Applies to: GenericLocation
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

def report = new HTMLReport(String.format("License Expiration Report By site for %s", instanceNode.getProperty("name")),
				"Neotropic SAS", "1.1");

report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());

def pattern = "E MMM dd HH:mm:ss z yyyy";
def next30DaysDate = use(TimeCategory){new Date() + 30.days};
def next60DaysDate = use(TimeCategory){new Date() + 60.days};
def next90DaysDate = use(TimeCategory){new Date() + 90.days};

def totalPricePerLicense = [:];
def countLicense = [:];
def usageLicense = [:];
def countLicenseVendor = [:];
def count30ExpirationLicenses = [:]; 
def count60ExpirationLicenses = [:]; 
def count90ExpirationLicenses = [:]; 

//We get all the devices in the location
def devicesInLocation = bem.getChildrenOfClassLightRecursive(objectId, objectClassName, "GenericCommunicationsElement", null, -1, -1) ;
            
//We get all the license in the inventory
def licenses = bem.getObjectsOfClass("GenericSoftwareAsset", -1);
for (license in licenses){
    def licenseCounter = 1;
    def licenseVendorCounter = 1;
    
    def attributes = bem.getAttributeValuesAsString(license.getClassName(), license.getId());
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
    
    def input = attributes.get("expirationDate");
    if(input != null){
        def date = new SimpleDateFormat(pattern).parse(input);
        //We get the license deivece related
        def licenseRelatedDevices = bem.getSpecialAttribute(license.getClassName(), license.getId(), "licenseHas");
        def devices = [];
        
        //We only take the devices in the location
        licenseRelatedDevices.each{d ->
            if(devicesInLocation.contains(d)){
                devices.add(d);
            }
        };

        usageLicense.put(license, devices);
        
        if(!devices.isEmpty()){
            if(next30DaysDate > date){
                count30ExpirationLicenses.put(license, devices)
            }
            else if(next60DaysDate > date){
                count60ExpirationLicenses.put(license, devices)
            }
            else if(next90DaysDate > date){
                count90ExpirationLicenses.put(license, devices)
            }
        }
    }
}

//Chart
def chartsFactory = new GChartsFactory(report);

def dataTable = new DataTable([DataType.STRING, DataType.NUMBER] as DataType[], ["Licenses", "Name"] as String[]);

countLicense.each{key, vv ->
    dataTable.addRow([key, vv.toString()] as String[]);
};

def htmlDivPieChart = chartsFactory.createHTMLDivWrapperChart(ChartType.PIECHART, "divPieChart", "Licenses Inventory", dataTable);
report.getComponents().add(htmlDivPieChart);

//Vendors
def dataTableVendors = new DataTable([DataType.STRING, DataType.NUMBER] as DataType[], ["Licenses Vendors", "Name"] as String[]);
countLicenseVendor.each{key, vl ->
    dataTableVendors.addRow([key, vl.toString()] as String[]);
};
def htmlDivColumnChart = chartsFactory.createHTMLDivWrapperChart(ChartType.COLUMNCHART, "divColumChart", "Licenses Vendors", dataTableVendors);

report.getComponents().add(htmlDivColumnChart);
//End charts

report.getComponents().add(new HTMLTable());
report.getComponents().add(new HTMLBr());

def i = 0;
count30ExpirationLicenses.each{k, v->
    def attributes = bem.getAttributeValuesAsString(k.getClassName(), k.getId());
    def licenseNameTitle = new HTMLMessage("font-weight: 'bold'; font-size: 2em;", null, k.getName());
    def licenseExpirationDaysTitle = new HTMLMessage("font-size: 2em;", "error", "Expires in less than 30 Days");
    
    report.getComponents().add(new HTMLDiv("title", licenseNameTitle));
    report.getComponents().add(new HTMLDiv("expired-days", licenseExpirationDaysTitle));
    
    def usage = (attributes.get("licensedDevices") as Integer) - v.size();
    def price = attributes.get("price") as Integer;
    def formatPrice = attributes.get("price") + " USD";
 
    def licenseHeaderTable = new HTMLTable();
    def licensedDevicesOf = "(" + v.size() + " of " + attributes.get("licensedDevices") + ")";
    
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Vendor"), new HTMLColumn(attributes.get("vendor")) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Individual License Price"), new HTMLColumn(formatPrice) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Purchase Date"), new HTMLColumn(attributes.get("purchaseDate")) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Expiration Date"), new HTMLColumn(attributes.get("expirationDate")) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Licensed Devices"), new HTMLColumn(licensedDevicesOf) ] as HTMLColumn[]));
    //We add the header table.
    report.getComponents().add(licenseHeaderTable);

    if(!v.isEmpty()){
        def licenseDeviceTable = new HTMLTable(null, null, ["Name", "Location"] as String[]);
        v.each{de ->
            def parents = bem.getParentsUntilFirstOfClass(de.getClassName(), de.getId(), "City");
            def location = [];
            parents.each{ p ->
                location.add(p.getName());
            }
            licenseDeviceTable.getRows().add(new HTMLRow(null, (i % 2 == 0 ? "odd" : "even"),
                [ new HTMLColumn(de.getName()),
                    new HTMLColumn(location.join(", ") as String)] as HTMLColumn[]));
            i++;
        }
        report.getComponents().add(licenseDeviceTable);
    }
    else{
        report.getComponents().add(new HTMLHx(3, "No devices related"));
    }
    report.getComponents().add(new HTMLBr());
};

i = 0;
count60ExpirationLicenses.each{k, v->
    def attributes = bem.getAttributeValuesAsString(k.getClassName(), k.getId());
    def licenseNameTitle = new HTMLMessage("font-weight: 'bold'; font-size: 2em;", null, k.getName());
    def licenseExpirationDaysTitle = new HTMLMessage("font-size: 2em;", "error", "Expires in less than 60 Days");
    
    report.getComponents().add(new HTMLDiv("title", licenseNameTitle));
    report.getComponents().add(new HTMLDiv("expired-days", licenseExpirationDaysTitle));
    
    def usage = (attributes.get("licensedDevices") as Integer) - v.size();
    def price = attributes.get("price") as Integer;
    def formatPrice = attributes.get("price") + " USD";
 
    def licenseHeaderTable = new HTMLTable();
    def licensedDevicesOf = "(" + v.size() + " of " + attributes.get("licensedDevices") + ")";
    
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Vendor"), new HTMLColumn(attributes.get("vendor")) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Individual License Price"), new HTMLColumn(formatPrice) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Purchase Date"), new HTMLColumn(attributes.get("purchaseDate")) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Expiration Date"), new HTMLColumn(attributes.get("expirationDate")) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Licensed Devices"), new HTMLColumn(licensedDevicesOf) ] as HTMLColumn[]));
    //We add the header table.
    report.getComponents().add(licenseHeaderTable);

    if(!v.isEmpty()){
        def licenseDeviceTable = new HTMLTable(null, null, ["Name", "Location"] as String[]);
        v.each{de ->
            def parents = bem.getParentsUntilFirstOfClass(de.getClassName(), de.getId(), "City");
            def location = [];
            parents.each{ p ->
                location.add(p.getName());
            }
            licenseDeviceTable.getRows().add(new HTMLRow(null, (i % 2 == 0 ? "odd" : "even"),
                [ new HTMLColumn(de.getName()),
                    new HTMLColumn(location.join(", ") as String)] as HTMLColumn[]));
            i++;
        }
        report.getComponents().add(licenseDeviceTable);
    }
    else{
        report.getComponents().add(new HTMLHx(3, "No devices related"));
    }
    report.getComponents().add(new HTMLBr());
};

i = 0;
count90ExpirationLicenses.each{k, v->
    def attributes = bem.getAttributeValuesAsString(k.getClassName(), k.getId());
    def licenseNameTitle = new HTMLMessage("font-weight: 'bold'; font-size: 2em;", null, k.getName());
    def licenseExpirationDaysTitle = new HTMLMessage("font-size: 2em;", "error", "Expires in less than 90 Days");
    
    report.getComponents().add(new HTMLDiv("title", licenseNameTitle));
    report.getComponents().add(new HTMLDiv("expired-days", licenseExpirationDaysTitle));
    
    def usage = (attributes.get("licensedDevices") as Integer) - v.size();
    def price = attributes.get("price") as Integer;
    def formatPrice = attributes.get("price") + " USD";
 
    def licenseHeaderTable = new HTMLTable();
    def licensedDevicesOf = "(" + v.size() + " of " + attributes.get("licensedDevices") + ")";
    
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Vendor"), new HTMLColumn(attributes.get("vendor")) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Individual License Price"), new HTMLColumn(formatPrice) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Purchase Date"), new HTMLColumn(attributes.get("purchaseDate")) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Expiration Date"), new HTMLColumn(attributes.get("expirationDate")) ] as HTMLColumn[]));
    licenseHeaderTable.getRows().add(new HTMLRow([ new HTMLColumn("Licensed Devices"), new HTMLColumn(licensedDevicesOf) ] as HTMLColumn[]));
    //We add the header table.
    report.getComponents().add(licenseHeaderTable);

    if(!v.isEmpty()){
        def licenseDeviceTable = new HTMLTable(null, null, ["Name", "Location"] as String[]);
        v.each{de ->
            def parents = bem.getParentsUntilFirstOfClass(de.getClassName(), de.getId(), "City");
            def location = [];
            parents.each{ p ->
                location.add(p.getName());
            }
            licenseDeviceTable.getRows().add(new HTMLRow(null, (i % 2 == 0 ? "odd" : "even"),
                [ new HTMLColumn(de.getName()),
                    new HTMLColumn(location.join(", ") as String)] as HTMLColumn[]));
            i++;
        }
        report.getComponents().add(licenseDeviceTable);
    }
    else{
        report.getComponents().add(new HTMLHx(3, "No devices related"));
    }
    report.getComponents().add(new HTMLBr());
};

//Return the report
report;