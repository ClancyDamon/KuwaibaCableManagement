/**
 * Scripted query to search licenses without pool, adds a default pool and move found licenses to the new pool.
 * Pool Name: default license pool.
 * Name: Licenses without pool.
 * 
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryResult;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.modules.commercial.softman.SoftwareManagerService;

//Creates the task result instance using reflection
def taskResult = new TaskResult();
try {
    taskResult.getMessages().add(TaskResult.createInformationMessage("Starting to add a default license pool"));
    def poolId = aem.createRootPool('default license pool', 'default pool to add the licenses that are not children of a pool'
        , 'GenericSoftwareAsset', ApplicationEntityManager.POOL_TYPE_MODULE_ROOT)
    taskResult.getMessages().add(TaskResult.createInformationMessage("Default license pool created"));
    
    def query = """
        MATCH (class:classes)<-[:EXTENDS*]-(:classes)<-[:INSTANCE_OF]-(license:inventoryObjects)
        WHERE class.name='GenericSoftwareAsset' 
        AND NOT (license)-[:CHILD_OF_SPECIAL]->(:pools)
        RETURN license As license
    """
    def queryResult = connectionHandler.execute(query)
    def licenseNodes = new ArrayList()
        while (queryResult.hasNext()) {
            def next = queryResult.next()
            def node = next.get("license")
            def queryClass = """
                MATCH (class:classes)<-[:INSTANCE_OF]-(license:inventoryObjects) 
                WHERE license._uuid=\$uuid 
                RETURN class as class
            """
            def parameters = ['uuid': node.getProperty("_uuid")]
            def queryClassResult = connectionHandler.execute(queryClass, parameters)
            def nextClass = queryClassResult.next()
            def nodeClass = nextClass.get("class")
            def businessObject = new BusinessObject(nodeClass.getProperty("name"), node.getProperty("_uuid"), node.getProperty("name"))
            licenseNodes.add(businessObject)   
    }
    
    taskResult.getMessages().add(TaskResult.createInformationMessage("Licenses are moved to the default license pool"));
    licenseNodes.each {
        bem.movePoolItem(poolId, it.getClassName(), it.getId())
    }
    taskResult.getMessages().add(TaskResult.createInformationMessage("Task ends successfully!!"));
} catch (Exception ex) {
    taskResult.getMessages().add(TaskResult.createErrorMessage(ex.getMessage()));
}
return taskResult;