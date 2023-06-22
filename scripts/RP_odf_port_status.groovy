/**
 * Displays the connection status in ODF and DDFs. It can also be used in splice boxes or fiber splitters with a few modifications.
 * Neotropic SAS - version 1.6
 * Applicable to: GenericDistributionFrame
 */

import org.neotropic.kuwaiba.modules.optional.reports.html.*;

// Gets the information related to the object the report is lanched from. The variables "objectClassName" and "objectId" are injected 
// automatically and can be used in any report.
def selectedDistributionFrame = bem.getObject(objectClassName, objectId);
// Now we get the ports in the ODF/DDF. We know that the ports are right under the distribution frame and that's why we use the method
// getChildrenOfClass. If we were to check for the ports in, say, Routers, where the ports are in boards and slots rather that directly 
// under device, we could use getChildrenOfClassLightRecursive instead. 
def portsInDistributionFrame = bem.getChildrenOfClassLight(objectId, objectClassName, 
                                    "GenericCommunicationsPort" /* This is the superclass of all communications ports, including ElectricalPorts and OpticalPorts, but excluding PowerPorts*/, 
                                    0 /* Retrieve all results at once */ )

// General report settings
def report = new HTMLReport(String.format("ODF/DDF Usage Report for %s", selectedDistributionFrame.getName()),
				"Neotropic SAS", "1.5");

// Use the default style sheet. You can also embed your own styles to match your preferred color scheme (see commented lines below).
report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());
// Or use your own stylesheet providing a valid URL instead.
// report.getLinkedStyleSheets().add("http://localhost:8080/kuwaiba/my_custom_styles.css");

// First, we build a small table with some basic information about the object we are launching the report from (an ODF or DDF)
def headerTable = new HTMLTable();
headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Name"), new HTMLColumn(selectedDistributionFrame.getName()) ] as HTMLColumn[]));
headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Serial Number"), 
                            new HTMLColumn(selectedDistributionFrame.getAttributes().get("serialNumber") != null ? 
                                    selectedDistributionFrame.getAttributes().get("serialNumber") : "Not Set") ] as HTMLColumn[]));
headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Number of Ports"), new HTMLColumn(portsInDistributionFrame.size()) ] as HTMLColumn[]));

// Now we create the table with list of ports and their respective attributes
def detailsTable = new HTMLTable(null, null, ["Name", "Cable/Fiber Connected", "Operational State"] as String[]);

def i = 0; // Simple counter used to apply a different color to each row

portsInDistributionFrame.each { aPort ->
    // First the port name, which can be retrieved immediately
    def portName = aPort.getName();
    
    // Now the operational state. The operational state (at least in the default data model shipped with the installer) is a list type. 
    def portStatus = bem.getAttributeValueAsString (aPort.getClassName(), aPort.getId(), "state" /* Change this attribute depending on your data model */) ;

    // Now we check if there's something connected to the port, and if so, what's its name. We do this by using the relationships
    // of the port with the potential link, that is, either "endpointA" or "endpointB". Those relationships can also be checked using 
    // the Relationship Explorer window in the Kuwaiba client.
    def linkConnected = "Not Connected"; // By default, we will assume that the port is not connected
    def links = bem.getSpecialAttribute(aPort.getClassName(), aPort.getId(), "endpointA"); // First we check for "endpointA"
    if (links.isEmpty()) { // If there is no "endpointA" relationship, we should also check for "endpointB"
        links = bem.getSpecialAttribute(aPort.getClassName(), aPort.getId(), "endpointB");
        if (!links.isEmpty()) 
            linkConnected = links.get(0).getName(); // getSpecialAttribute returns an array, because there could be many objects related depending 
                                                    // on the relationship. In this case, we know for sure that there can be only 1 cable attached.
    } else
        linkConnected = links.get(0).getName(); // getSpecialAttribute returns an array, because there could be many objects related depending 
                                                    // on the relationship. In this case, we know for sure that there can be only 1 cable attached.

    // Finally, we add the row with the information we just got
    detailsTable.getRows().add(new HTMLRow(null, (i % 2 == 0 ? "odd" : "even"),
            [
                new HTMLColumn(portName),
                new HTMLColumn(linkConnected),
                new HTMLColumn(portStatus == null ? "Not Set" : portStatus)
            ] as HTMLColumn[]));
    i++;
}

// Now we assemble all the tables inside the report
report.getComponents().add(new HTMLHx(1, String.format("ODF/DDF Usage Report for %s", instanceNode.getProperty("name"))));
report.getComponents().add(headerTable);
report.getComponents().add(detailsTable);
report.getComponents().add(new HTMLDiv("width:100%;text-align:center", "footer", "", "This report is powered by <a href=\"https://www.kuwaiba.org\">Kuwaiba Open Network Inventory</a>"));
report.getComponents().add(new HTMLDiv("width:100%;text-align:center", "footer", "", "Get commercial support from <a href=\"https://www.neotropic.co\">Neotropic SAS</a>"));

return report;
