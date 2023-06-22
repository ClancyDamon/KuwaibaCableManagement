/**
 * Subnet Usage report that shows information about related ip addresses and subnets.
 * Neotropic SAS - version 1.5
 * Parameters: None
 * Applicable to: Subnet
 */
import org.neotropic.kuwaiba.modules.optional.reports.html.*;
import org.neotropic.kuwaiba.modules.optional.reports.javascript.DataTable;
import org.neotropic.kuwaiba.modules.optional.reports.javascript.DataTable.DataType;
import org.neotropic.kuwaiba.modules.optional.reports.plugins.gcharts.GChartsFactory;
import org.neotropic.kuwaiba.modules.optional.reports.plugins.gcharts.GChartsFactory.ChartType;
import org.neotropic.kuwaiba.modules.optional.reports.defaults.Util;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;

def report = new HTMLReport("Subnet Usage", "Neotropic SAS", "1.5");

report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());

def chartsFactory = new GChartsFactory(report);

// Place the company logo
def corporateLogo = aem.getConfiguration().getProperty("corporateLogo");
if (corporateLogo == null)
    corporateLogo = "logo.jpg"
else
    corporateLogo = aem.getConfiguration().getProperty("corporateLogo");

// Get the current subnet object
def subnet = bem.getObject(objectClassName, objectId);

def subnetChildren = bem.getObjectSpecialChildren(objectClassName, objectId);

def subnetAttributes = subnet.getAttributes();
def hosts = subnetAttributes.get("hosts") == null ? 2 : (subnetAttributes.get("hosts") + 2);
def usedIps = 0;

def ips = [];
def subnets = [];

subnetChildren.each {subnetChild ->
    if (subnetChild.getClassName() == Constants.CLASS_IP_ADDRESS) {
        ips.add(subnetChild);
    }
    if (subnetChild.getClassName() == Constants.CLASS_SUBNET_IPV4 || subnetChild.getClassName() == Constants.CLASS_SUBNET_IPV6) {
        subnets.add(subnetChild);
    }
}

ips.each {ip -> 
    def ipDevices = bem.getSpecialAttribute(Constants.CLASS_IP_ADDRESS, ip.getId(), "ipamHasIpAddress");
    if (ipDevices != null && ipDevices.size() > 0)
        usedIps++;
}

// There are no hosts but the
// gateway and the broadcast
// are in use
if (hosts == 0 && usedIps == 0) {
    usedIps = ips.size();
    
   // if (usedIps > hosts) {
    //    hosts += 2;
   // }
}

int freeIps = hosts - usedIps;

def vrf = "";
def vlan = "";
def service = "";
def title = "";
def subnetUsageReportText = "";

def vlans = bem.getSpecialAttribute(objectClassName, objectId, "ipamBelongsToVlan");
def vrfs = bem.getSpecialAttribute(objectClassName, objectId, "ipamBelongsToVrfInstance");
def services = bem.getSpecialAttribute(objectClassName, objectId, "uses");
        
if(!vlans.isEmpty())
    vlan = "<b>" + vlans.get(0) + "</b> |"+
    Util.formatLocation(bem.getParents(vlans.get(0).getClassName(), vlans.get(0).getId()));
        
if(!vrfs.isEmpty())
    vrf = "<b>" + vrfs.get(0) + "</b>";

if(!services.isEmpty())
    service = services.get(0).toString();


def tblInfo = new HTMLTable(null);

if (subnet == null) {
    report.setTitle("Error");
    report.getComponents().add(new HTMLDiv("", "error", "", "No information about this subnet could be found"));
} else {
    tblInfo.getRows().add(new HTMLRow([new HTMLColumn(new HTMLHx(1, "Subnet Usage Detail Report for " + subnet.getName())), new HTMLColumn(new HTMLImage(corporateLogo))] as HTMLColumn[]));
    report.getComponents().add(tblInfo);
    
    HTMLTable tblSubnets = new HTMLTable(null);
    HTMLColumn columnLabelIP = new HTMLColumn("", "generalInfoLabel", "Network IP Address");
    HTMLColumn columnIP = new HTMLColumn("<b>" + subnetAttributes.get("networkIp") + "</b>");

    def dataTable = new DataTable([DataType.STRING, DataType.NUMBER] as DataType[], ["IP", "Usage %"] as String[]);
    dataTable.addRow(["Used", "" + usedIps] as String[]);
    dataTable.addRow(["Free", "" + freeIps] as String[]);
    // Creating the Pie Chart
    def htmlDivPieChart = chartsFactory.createHTMLDivWrapperChart(ChartType.PIECHART, "divPieChart", "Subnet Usage", dataTable);
    HTMLColumn columnChart = new HTMLColumn("width: 350px; height: 250px;", "", htmlDivPieChart);
    columnChart.setRowspan("8");

    tblSubnets.getRows().add(new HTMLRow([columnLabelIP, columnIP, columnChart] as HTMLColumn[]));

    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "Broadcast IP Address"), new HTMLColumn(subnetAttributes.get("broadcastIp"))] as HTMLColumn[]));
    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "Description"), new HTMLColumn(subnetAttributes.get("description") == null ? "" : subnetAttributes.get("description"))] as HTMLColumn[]));
    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "Number of hosts(Network and Broadcast IP address are inlcuded)"), new HTMLColumn(hosts)] as HTMLColumn[]));
    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "IP addresses assigned to an interface"), new HTMLColumn(usedIps*100/hosts + "% (" + usedIps + ")")] as HTMLColumn[]));
    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "Free IPs"), new HTMLColumn(freeIps*100/hosts + "% (" + freeIps + ")")] as HTMLColumn[]));
//    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "VLAN"), new HTMLColumn(vlan)] as HTMLColumn[]));
//    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "VRF"), new HTMLColumn(vrf)] as HTMLColumn[]));
    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "Service"), new HTMLColumn(service)] as HTMLColumn[]));
    report.getComponents().add(tblSubnets);
}

// i is an index to define if a row are even or odd
def i = 0;
// Table for Subnets
def tblSubnets= new HTMLTable(["Subnet", "Description", "Service"] as String[]);
if (subnets.isEmpty())
    report.getComponents().add(new HTMLDiv("", "error", "", "There are no nested subnets"));
else {
    subnets.each { subnetElement ->
        service = "";
        def subnetServices = bem.getSpecialAttribute(subnetElement.getClassName(), subnetElement.getId(), "uses");
        if(subnetServices.size() > 0) {
            service = subnetServices.get(0).toString();
        }
        def subnetObj = bem.getObject(objectClassName, subnetElement.getId());
        def attributes = subnetObj.getAttributes();

        def subnetSubnet = new HTMLColumn(subnetElement.getName());
        def subnetDescription = new HTMLColumn(attributes.get("description") == null ? "N/A" : attributes.get("description"));
        def subnetElementService = new HTMLColumn(service);

        def row = new HTMLRow(i % 2 == 0 ? "even" :"odd", [subnetSubnet, subnetDescription, subnetElementService] as HTMLColumn[]);
        tblSubnets.getRows().add(row);

        i++;
    }
    //Assemble the components
    report.getComponents().add(new HTMLBR());
    report.getComponents().add(new HTMLHx(2, "Subnets"));
    report.getComponents().add(tblSubnets);
}
// Table for IP Addresses
def tblIPAddresses= new HTMLTable(["IP Address", "Description", "Interface", "Device", "Service"] as String[]);
if (ips.isEmpty()) {
    report.getComponents().add(new HTMLDiv("", "error", "", "There are no IP Addresses in use"));
} else {
    i = 0;
    ips.each { ip -> 
        service = "";
        def theInterface = "";

        def ipDevices = bem.getSpecialAttribute(Constants.CLASS_IP_ADDRESS, ip.getId(), "ipamHasIpAddress");
        def device = "";
        if (ipDevices.size() > 0) {
            theInterface = ipDevices.get(0).toString();
            device = bem.getFirstParentOfClass(ipDevices.get(0).getClassName(), ipDevices.get(0).getId(), "GenericCommunicationsElement");
        }
        def ipServices = bem.getSpecialAttribute(Constants.CLASS_IP_ADDRESS, ip.getId(), "uses");
        if(ipServices.size() > 0)
            service = ipServices.get(0).toString();
        def ipObject = bem.getObject(Constants.CLASS_IP_ADDRESS, ip.getId());
        def attributes = ipObject.getAttributes();

        def ipAddress = new HTMLColumn(ip.getName());
        def ipDescription = new HTMLColumn(attributes.get("description") ==  null ? "N/A" : attributes.get("description"));
        def ipPort = new HTMLColumn(theInterface);
        if(device == null)
            device = "Not a Generic Communications Element";
        def ipLocation = new HTMLColumn(device);
        def ipService = new HTMLColumn(service);

        def row = new HTMLRow(i % 2 == 0 ? "even" :"odd", [ipAddress, ipDescription, ipPort, ipLocation, ipService] as HTMLColumn[]);
        tblIPAddresses.getRows().add(row);

        i++;
    }
    //Assemble the components
    report.getComponents().add(new HTMLBR());
    report.getComponents().add(new HTMLHx(2, "IP Addresses"));
    report.getComponents().add(tblIPAddresses);
    report.getComponents().add(new HTMLDiv("", "footer", "", "This report is powered by <a href=\"http://www.kuwaiba.org\">Kuwaiba Open Network Inventory</a>"));
}
//Return the report
return report;