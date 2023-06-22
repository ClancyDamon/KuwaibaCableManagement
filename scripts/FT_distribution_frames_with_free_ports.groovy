/** 
 * Retrieves all the distribution frames (ODFs, DDFs/MDFs) with free ports (ports without cables/fibers attached to them) within a given parent.
 * Neotropic SAS - version 1.1
 * Applies to: GenericPhysicalNode or its subclasses.
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
        def filteredDistributionFrames = [];
        def distributionFrames = bem.getChildrenOfClassLightRecursive(
            objectId, objectClass, "GenericDistributionFrame", null, -1, -1 //NOI18N
        ); 
        for (distributionFrame in distributionFrames) {
            def ports = bem.getChildrenOfClassLightRecursive(
                distributionFrame.getId(), distributionFrame.getClassName(), "GenericPhysicalPort", null, -1, -1  //NOI18N
            );
            for (port in ports) {
                def endpoints = bem.getSpecialAttributes(
                    port.getClassName(), port.getId(), "endpointA", "endpointB" //NOI18N
                );
                if (endpoints.isEmpty()) {
                    filteredDistributionFrames.add(distributionFrame);
                    break;
                }
            }
        }
        return filteredDistributionFrames;
    }
}