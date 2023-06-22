 /** 
 * Retrieves physical ports (instances of subclasses of GenericPhysicalPort) within a given parent.
 * Neotropic SAS - version 1.0
 * Applies to: Any, most likely GenericCommunicationsElement or its subclasses.
 */
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Filter;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FilterDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;

public class %s extends Filter {

    public %s(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem, ConnectionManager cm) {
        super(mem, aem, bem, cm);
    }

    public List<BusinessObjectLight> run(String objectId, String objectClass, HashMap<String, String> parameters, int page, int limit) throws InvalidArgumentException {
        def classNameToFilter = "GenericPhysicalPort"
        return bem.getChildrenOfClassLightRecursive(objectId, objectClass, classNameToFilter, parameters, page, limit);
    }
}