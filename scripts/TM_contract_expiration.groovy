/**
 * Watches for all support contracts about to expire.  This task accesses directly the database, and as such is compatible only with Neo4J implementations.
 * Neotropic SAS - version 1.5
 * Parameters: -daysBeforeHigh: The upper threshold (number of days). It will generate a red (critical) notification.
 *             -daysBeforeMid: The lower threshold (number of days). It will generate a orange (warning) notification.
 */

import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;

//Creates the task result instance using reflection
def taskResult = new TaskResult();

//Check if the parameters exist and are set
if (scriptParameters.get("daysBeforeHigh") == null || scriptParameters.get("daysBeforeHigh").isEmpty()) 
	return TaskResult.createErrorResult("Parameter daysBeforeHigh not set");

if (scriptParameters.get("daysBeforeMid") == null || scriptParameters.get("daysBeforeMid").isEmpty()) 
	return TaskResult.createErrorResult("Parameter daysBeforeMid not set");

def daysBeforeHigh = Integer.valueOf(scriptParameters.get("daysBeforeHigh"));
def daysBeforeMid = Integer.valueOf(scriptParameters.get("daysBeforeMid"));

if (daysBeforeMid <= daysBeforeHigh)
	return TaskResult.createErrorResult("daysBeforeMid parameter has to be greater than daysBeforeHigh");

//Gets the contract class node
def supportContractClassNode = connectionHandler.findNode(Label.label("classes"), Constants.PROPERTY_NAME, "SupportContract");

if (supportContractClassNode == null)
	taskResult = TaskResult.createErrorResult("Class SupportContract not found");
else {
	supportContractClassNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF).each { instanceRel ->
		def supportContractInstanceNode = instanceRel.getStartNode();

		if (!supportContractInstanceNode.hasProperty("expirationDate"))
			expirationDate = Calendar.getInstance().getTime();
		else
			expirationDate = new Date(supportContractInstanceNode.getProperty("expirationDate"));

		if (expirationDate.before(Calendar.getInstance().getTime())) //Ignore inactive contracts
			return;
		
		formattedExpirationDate = new SimpleDateFormat("MMMM dd, yyyy").format(expirationDate);

		Calendar aCalendar = Calendar.getInstance();
		aCalendar.add(Calendar.DAY_OF_YEAR, daysBeforeHigh);

		if (expirationDate.before(aCalendar.getTime()))		
			taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("The contract %s with id %s will expire on %s", 
				supportContractInstanceNode.getProperty("name"), supportContractInstanceNode.getId(), formattedExpirationDate)));

		else {
			aCalendar = Calendar.getInstance();
			aCalendar.add(Calendar.DAY_OF_YEAR, daysBeforeMid);

			if (expirationDate.before(aCalendar.getTime()))
				taskResult.getMessages().add(TaskResult.createWarningMessage(String.format("The contract %s with id %s will expire on %s", 
					supportContractInstanceNode.getProperty("name"), supportContractInstanceNode.getId(), formattedExpirationDate)));
		}
	}
}

//Returns the result
taskResult;