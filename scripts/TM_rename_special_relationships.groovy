/**
 * Name:            renameSpecialRelationships
 * Description:     Task to rename special relationships. In the hash map names can set the pair (oldSpecialRelationshipName, newSpecialRelationshipName)
 * commitOnExecute: true
 * Date:            2021/02/19
 * Author:          Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neo4j.graphdb.Result;
/**
  * Special relationship names standard
  * The name must be camel case
  * The name cannot contain spaces
  * The name must be a verb
  * The name start with the module name
  * This tasks access directly the database, and as such is compatible only with Neo4J implementations.
  *
  * Script Parameters
  * oldSpecialRelName Old Special Relationship Name
  * newSpecialRelName New Special Relationship Name
  */
TaskResult taskResult = new TaskResult();

def oldSpecialRelName = scriptParameters.get("oldSpecialRelName"); //NOI18N
def newSpecialRelName = scriptParameters.get("newSpecialRelName"); //NOI18N

if (oldSpecialRelName == null)
    taskResult.getMessages().add(TaskResult.createErrorMessage("Script parameter oldSpecialRelName not set"));
if (newSpecialRelName == null)
    taskResult.getMessages().add(TaskResult.createErrorMessage("Script parameter newSpecialRelName not set"));
if (oldSpecialRelName == null || newSpecialRelName == null)
    return taskResult;

HashMap<String, String> names = new HashMap();
names.put(oldSpecialRelName, newSpecialRelName);
//names.put("oldSpecialRelationshipName", "newSpecialRelationshipName");

taskResult.getMessages().add(TaskResult.createInformationMessage("Starting to rename special relationships"));

try {
    for (String oldRelName : names.keySet()) {
        String newRelName = names.get(oldRelName);
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(String.format("MATCH ()-[r:RELATED_TO_SPECIAL {name:'%s'}]-()\n", oldRelName)); //NOI18N
        queryBuilder.append(String.format("SET r.name = '%s'\n", newRelName)); //NOI18N

        Result result = connectionHandler.execute(queryBuilder.toString());
        taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("Rename the special relationship from %s to %s", oldRelName, newRelName)));
    }
} catch(Exception ex) {
    taskResult.getMessages().add(TaskResult.createErrorMessage(ex.getMessage()));
}

taskResult.getMessages().add(TaskResult.createInformationMessage("Renaming special relationships finished"));
return taskResult;