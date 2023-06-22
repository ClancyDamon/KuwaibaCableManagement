/**
  * Kuwaiba Version: 2.1
  * 
  * description: Resets the "color" attribute of the classes to 0 (black) and 
  *              empties all icons and smallIcons.
  * commitOnExecute: false
  * parameters: none
  * version: 1.0
  * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
  */
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;

def taskResult = new TaskResult();
try {
    // This has to be done through the Persistence API because instead of Cypher, 
    // since in the latter all int values are stored as long, not int, and this 
    // will cause a ClassCastException as per https://github.com/neo4j/neo4j/issues/7652
    def allClasses = mem.getAllClasses(true /* include list types */, true /* include classes set as inDesign */);
    def propertySet = new HashMap<String, Object>();
    propertySet.put("color", 0);
    propertySet.put("icon", new byte[0]);
    propertySet.put("smallIcon", new byte[0]);
    
    allClasses.each { aClass -> 
        mem.setClassProperties(aClass.getId(), propertySet);
    };
    
    taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("%s classes were updated", allClasses.size())));
} catch(Exception ex) {
    taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Unexpected error: %s", ex.getMessage())));
}
return taskResult;
