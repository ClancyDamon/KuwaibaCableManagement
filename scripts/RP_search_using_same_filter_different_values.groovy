/**
 * This report is aimed to showcase how to overcome a feature lacking in the Query Manager: How to perform a query that matches two (or more)
 * possible values of the same attribute, and also, how to fake parameters in a report (reports don't support custom parameters by default). 
 * This script shows how to query for all instances of a given class (provided as parameter to the 
 * report as a value set in the attribute "className" of the object the report is being lunched from) whose attribute called as another parameter
 * to the report (called "filterAttribute") matches, with a logical OR, the values provided in attributes "filterValue1, filterValue2, ..., filterValueN".
 * The steps to setup the report are the following:
 * 1. Create a class (anywhere in the datamodel, but preferably not a physical inventory object, and if possible, using its own branch). 
 *    In this example, the class will be named MultipleValueFilter.
 * 2. Create the attributes named "className" and "filterAttribute" of type string.
 * 2a. Create as many attributes as possible values you wish to match. In this case, we will use only two: value1 and value2
 * 3. Create a pool of MultipleValueFilter instances (or whatever class you created) and create an instance.
 * 4. Create a class level report applicable to the class you created and paste the contents of this script.
 * 5. Fill-in the attributes you created in the datamodel. For example, set "className" to "Router", "filterAttribute" to "vendor", 
 *    value1 to "Cisco" and value2 to "Huawei". The report will display all the router whose vendors are either Cisco or Huawei.
 * 6. Run the report right-clicking on the instance and selecting "Reports" from the context menu.
 * Neotropic SAS - version 1.5
 * Applicable To: A custom class created by the user. This script assumes that the class is named MultipleValueFilter
 */
import org.neotropic.kuwaiba.modules.optional.reports.html.*;

// Gets Business Entity Manager. This will handle the manipulation of inventory objects. For details about the API, check
// https://kuwaiba.org/docs/dev/javadoc/current/org/kuwaiba/apis/persistence/business/BusinessEntityManager.html

def report = new HTMLReport("Multiple Attribute Filter Query", "Neotropic SAS", "1.5");

// Use the default style sheet. You can also embed your own styles to match your preferred color scheme (see commented lines below).
report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());
// Or use your own stylesheet providing a valid URL instead.
// report.getLinkedStyleSheets().add("http://localhost:8080/kuwaiba/my_custom_styles.css");

// First, we get all the parameters from the object the report was launched from.
def queryDefinition = bem.getObject(objectClassName, objectId); /* These two variables are injected in all class level reports*/

def instancesToFilter = queryDefinition.getAttributes().get("className"); // Here you can user leaf classes such as Router or Switch or abstract higher level classes such as GenericCommunicationsElement
def filterAttribute = queryDefinition.getAttributes().get("filterAttribute");
def filterValue1 = queryDefinition.getAttributes().get("filterValue1");
def filterValue2 = queryDefinition.getAttributes().get("filterValue2");
// Add as many filter values you want, just add the respective attributes to the class MultipleValueFilter
// def filterValueN = queryDefinition.getAttributes().get("filterValueN");

if (instancesToFilter == null || filterAttribute == null || filterValue1 == null || filterValue2 == null)
    throw new RuntimeException("Make sure the attributes className, filterAttribute, filterValue1 and filterValue2 are correctly created in class MultipleValueFilter");

// Initilize the tables with the information we want. The script assumes that the searched objects have the attributes name, serialNumber, rackUnits and state.
def filter1Table = new HTMLTable(null, null, ["Name", "Serial Number", "Rack Units", "Operational State"] as String[]);
def filter2Table = new HTMLTable(null, null, ["Name", "Serial Number", "Rack Units", "Operational State"] as String[]);

// Now we do the actual search. This is not the most optimized way to do it, but it will do the trick. Another option is to use
// Neo4J's Cypher sentences (that is, direct access to the database), which would run *a lot* faster, but depending on the size 
// of your database, the difference might be negligible.
bem.getObjectsOfClass(instancesToFilter, -1 /* fetch all results */).each { anObject ->
    def theFilter = bem.getAttributeValueAsString(anObject.getClassName(), anObject.getId(), filterAttribute);
    if (theFilter != null) {
        switch (theFilter) { // This comparison is case sensitive and does only exact matches. Use equalsIgnoreCase() and "if" blocks for more permissive matches
            case filterValue1:
                filter1Table.getRows().add(new HTMLRow(null, null,
                    [   new HTMLColumn(anObject.getName()),
                        new HTMLColumn(anObject.getAttributes().get("serialNumber") == null ? "Not Set" : anObject.getAttributes().get("serialNumber")),
                        new HTMLColumn(anObject.getAttributes().get("rackUnits") == null ? "Not Set" : anObject.getAttributes().get("rackUnits")),
                        new HTMLColumn(bem.getAttributeValueAsString(anObject.getClassName(), anObject.getId(), "state")), // List types are not fetched like the other types
                    ] as HTMLColumn[]));
            break;
            case filterValue2:
                filter2Table.getRows().add(new HTMLRow(null, null,
                    [   new HTMLColumn(anObject.getName()),
                        new HTMLColumn(anObject.getAttributes().get("serialNumber") == null ? "Not Set" : anObject.getAttributes().get("serialNumber")),
                        new HTMLColumn(anObject.getAttributes().get("rackUnits") == null ? "Not Set" : anObject.getAttributes().get("rackUnits")),
                        new HTMLColumn(bem.getAttributeValueAsString(anObject.getClassName(), anObject.getId(), "state")), // List types are not fetched like the other types
                    ] as HTMLColumn[]));
            break;
            // Add other filters if desired
            // ...
        }

    } 
}

// Now we assemble the tables and add a couple titles
report.getComponents().add(new HTMLHx(1, String.format("Results for %s", filterValue1)));
report.getComponents().add(filter1Table);
report.getComponents().add(new HTMLHx(1, String.format("Results for %s", filterValue2)));
report.getComponents().add(filter2Table);

// And that's it!
return report;