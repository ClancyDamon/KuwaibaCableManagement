/**
 * Class level report that shows the Physical Path for a port in a table fashion
 * Neotropic SAS - version 1.5
 * Parameters: None
 * Applicable to: GenericPort
 */
import org.neotropic.kuwaiba.modules.optional.reports.html.*;

def report = new HTMLReport(String.format("Physical Path Report for %s", instanceNode.getProperty("name")),
				"Neotropic SAS", "1.5");

report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());


//This method will give us the full physical trace from the selected port
def physicalTraceElements = bem.getPhysicalPath(objectClassName, objectId)

def physicalTraceTable = new HTMLTable(["Name", "Type", "Location"] as String[]);

def i = 0;
physicalTraceElements.each { elementInPhysicalTrace ->
	def parents = bem.getParents(objectClassName, objectId);
	physicalTraceTable.getRows().add(new HTMLRow(null, (i % 2 == 0 ? "odd" : "even"),
									 [ new HTMLColumn(elementInPhysicalTrace.getName()),
									   new HTMLColumn(elementInPhysicalTrace.getClassName()),
									   new HTMLColumn(parents.join(' | '))
									 ] as HTMLColumn[]));
	i++;
}

//Simple function to format the location of an object. This will be added to the reporting API of Kuwaiba 1.5
//def formatLocation

//Assemble the components
report.getComponents().add(physicalTraceTable);

//Return the report
report;

