/**
 * Homologation script for the objectRelatedViews label.
 **/
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neo4j.graphdb.Result;

taskResult = new TaskResult();
taskResult.getMessages().add(TaskResult.createInformationMessage("Starting to add label"));
try {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append(String.format("MATCH (a)-[:HAS_VIEW]->(b) SET b:objectRelatedViews\n"));
    Result result = connectionHandler.execute(queryBuilder.toString());
    taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("Add label -> %s", "objectRelatedViews")));
} catch(Exception ex) {
    taskResult.getMessages().add(TaskResult.createErrorMessage(ex.getMessage()));
}

taskResult.getMessages().add(TaskResult.createInformationMessage("Label was added"));
return taskResult;