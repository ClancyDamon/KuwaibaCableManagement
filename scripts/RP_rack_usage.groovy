/**
 * Calculates the occupation of a given rack using some of the new user-defined reporting capabilities.
 * Neotropic SAS - version 1.2
 * Parameters: None
 * Applies to: Rack
 */
import org.neo4j.graphdb.Direction;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.neotropic.kuwaiba.modules.optional.reports.html.*;
       
def report = new HTMLReport(String.format("Rack Usage Report for %s", instanceNode.getProperty("name")),
				"Neotropic SAS", "1.1");

report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());

//Detailed table
def totalRackUnits = instanceNode.hasProperty("rackUnits") ? instanceNode.getProperty("rackUnits") : 0;
def usedRackUnits = 0;

def detailsTable = new HTMLTable(null, null, ["Name", "Serial Number", "Rack Units", "Operational State"] as String[]);

def i = 0;

instanceNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF).each { childOfRelationship ->
	def rackableNode = childOfRelationship.getStartNode();
	def rackableRackUnits = rackableNode.hasProperty("rackUnits") ? rackableNode.getProperty("rackUnits") : 0;
	usedRackUnits += rackableRackUnits;
	detailsTable.getRows().add(new HTMLRow(null, (i % 2 == 0 ? "odd" : "even"),
		      [ new HTMLColumn(rackableNode.getProperty("name")),
		      new HTMLColumn(rackableNode.hasProperty("serialNumber") ? rackableNode.getProperty("serialNumber") : "Not Set"),
		      new HTMLColumn(rackableRackUnits),
		      new HTMLColumn("Working")] as HTMLColumn[]));
	i++;
}

//Basic info table
def headerTable = new HTMLTable();
headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Name"), new HTMLColumn(instanceNode.getProperty("name")) ] as HTMLColumn[]));

headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Serial Number"), new HTMLColumn(instanceNode.hasProperty("serialNumber") ? instanceNode.getProperty("serialNumber") : "Not Set") ] as HTMLColumn[]));

headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Total Rack Units"), new HTMLColumn(totalRackUnits) ] as HTMLColumn[]));

headerTable.getRows().add(new HTMLRow([ new HTMLColumn("Used Rack Units"), new HTMLColumn(usedRackUnits + " (" + (usedRackUnits == 0 ? 0 : Math.round(100 * usedRackUnits/totalRackUnits)) + "%)") ] as HTMLColumn[]));

//Assemble the components
report.getComponents().add(headerTable);
report.getComponents().add(detailsTable);

//Return the report
report;