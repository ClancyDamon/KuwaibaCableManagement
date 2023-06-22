/**
 * Given a set of objects, this script will swap their current class for the one provided. An example of when to use this is if the user created a bunch of 
 * ElectricalLinks that should have been OpticalLinks, so instead of deleting the objects and creating them again, you can use this script to change the classes     
 * without affecting the object ids and relationships already established. IMPORTANT: Use this script ONLY if the two classes have the same attributes or this could
 * represent a threat to the consistency of the information. This task accesses directly the database, and as such is compatible only with Neo4J implementations.
 * Neotropic SAS - version 1.5
 * Parameters: -objectIds: The ids of the objects whose class should be swapped separated by semicolons (";")
 *             -destinationClass: The new class the given objects will be instance of.
 */
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.neo4j.graphdb.Label;

//Creates the task result instance using reflection
def taskResult = new TaskResult();

//Check if the parameters exist and are set
if (scriptParameters.get("objectIds") == null || scriptParameters.get("objectIds").isEmpty()) 
	return TaskResult.createErrorResult("Parameter objectIds not set");

//Check if the parameters exist and are set
if (scriptParameters.get("destinationClass") == null || scriptParameters.get("destinationClass").isEmpty()) 
	return TaskResult.createErrorResult("Parameter destinationClass not set");

def objectIds = scriptParameters.get("objectIds").split(";");
def destinationClassNode = connectionHandler.findNode(Label.label("classes"), Constants.PROPERTY_NAME, scriptParameters.get("destinationClass"));

if (destinationClassNode == null)
    return TaskResult.createErrorResult(String.format("Class %s does nor exist", scriptParameters.get("destinationClass")));

objectIds.each { objectId ->    
    def objectNode = connectionHandler.findNode(Label.label("inventoryObjects"), "_uuid", objectId);
    if (objectNode == null)
        taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Object with id %s could not be found", objectId)));
    else {
        if(!objectNode.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
            taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Object with id %s seems to be malformed. Check the INSTANCE_OF relationship", objectId)));
        else {
            objectNode.getRelationships(RelTypes.INSTANCE_OF).iterator().next().delete();
            objectNode.createRelationshipTo(destinationClassNode, RelTypes.INSTANCE_OF);
            taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("Class for object %s changed to %s successfully", 
                        objectNode.getProperty("name"), scriptParameters.get("destinationClass"))));
        }
    }

}

//Return the task results
taskResult
