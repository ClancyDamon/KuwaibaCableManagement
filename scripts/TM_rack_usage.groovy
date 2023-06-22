/**
 * Finds the racks whose occupation level surpasses certain threholds. This task accesses directly the database, and as such is compatible only with Neo4J implementations.
 * Neotropic SAS - version  2.1
 * Parameters: -thresholdHigh: The upper threshold (usage percentage). It will generate a red (critical) notification.
 *             -thresholdMid: The lower threshold (usage percentage). It will generate an orange (warning) notification.
 */
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;

//Creates the task result instance using reflection
def taskResult = new TaskResult();

//Check if the parameters exist and are set
if (scriptParameters.get("thresholdHigh") == null || scriptParameters.get("thresholdHigh").isEmpty()) 
	return TaskResult.createErrorResult("Parameter thresholdHigh not set");

//Check if the parameters exist and are set
if (scriptParameters.get("thresholdMid") == null || scriptParameters.get("thresholdMid").isEmpty()) 
	return TaskResult.createErrorResult("Parameter thresholdMid not set");

def thresholdHigh = Integer.valueOf(scriptParameters.get("thresholdHigh"));
def thresholdMid = Integer.valueOf(scriptParameters.get("thresholdMid"));

if (thresholdHigh <= thresholdMid)
	return TaskResult.createErrorResult("thresholdHigh must be greater than thresholdMid");

// Gets all racks in inventory
bem.getObjectsOfClass("Rack", -1).each { aRack -> 
    // Do not continue if the rackUnits attribute in the rack is empty
    if (aRack.getAttributes().get("rackUnits") == null)
        taskResult.getMessages().add(TaskResult.createWarningMessage(String.format("The rackUnits attribute is not set in rack %s", 
				aRack.getName())));
	else {
        def totalRackUnits = aRack.getAttributes().get("rackUnits");
        def occupiedRackUnits = 0;
        // Loop through the elements inside the rack, and if they have an attribute rackUnits 
        // set to a valid value, increment a counter
        bem.getObjectChildren(aRack.getClassName(), aRack.getId(), -1).each { aChildLight ->
            aChild = bem.getObject(aChildLight.getClassName(), aChildLight.getId());
            if (aChild.getAttributes().get("rackUnits") != null)
                occupiedRackUnits += aChild.getAttributes().get("rackUnits");
        };
        
        // Calculate the usage percentage, and depending on the value compared to the 
        // thresholds, print a message
        def percentage = Math.round(100 * occupiedRackUnits / totalRackUnits); 
		
		if (percentage > thresholdHigh)
			taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("The rack %s with serial number %s has %s%% occupation", 
				aRack.getName(), 
				aRack.getAttributes().get("serialNumber") == null ? "NOT SET" : aRack.getAttributes().get("serialNumber"), 
				Math.round(percentage))));
		else if (percentage > thresholdMid)
			taskResult.getMessages().add(TaskResult.createWarningMessage(String.format("The rack %s with serial number %s has %s%% occupation", 
				aRack.getName(), 
				aRack.getAttributes().get("serialNumber") == null ? "NOT SET" : aRack.getAttributes().get("serialNumber"), 
				Math.round(percentage))));
			else
			    taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("The rack %s with serial number %s has %s%% occupation", 
				aRack.getName(), 
				aRack.getAttributes().get("serialNumber") == null ? "NOT SET" : aRack.getAttributes().get("serialNumber"), 
				Math.round(percentage))));
	}
};

//Returns the result
taskResult;