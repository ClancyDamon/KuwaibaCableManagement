/** 
 * Checks if a port has a physical connection attached to it. If so, make it render red, with a simple prefix (a plus sign).
 * Neotropic SAS - version 1.5
 * Applies to: GenericPort or its subclasses.
 */
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;

public class %s extends ValidatorDefinition {
    //Mandatory, boiler-plate constructor
    public %s (long id, String name, String description, String classToBeApplied, String script, boolean enabled) {
        super(id, name, description, classToBeApplied, script, enabled);
    }

    public Validator run(String objectClass, String objectId, ConnectionManager cm, 
            MetadataEntityManager mem, BusinessEntityManager bem, ApplicationEntityManager aem) {
        if (!bem.getSpecialAttribute(objectClass, objectId, "endpointA").isEmpty() || !bem.getSpecialAttribute(objectClass, objectId, "endpointB").isEmpty()) {
            Properties properties = new Properties();
            properties.setProperty("color", "#0000FF");
            properties.setProperty("prefix", "[+]");
            return new Validator(getName(), properties);
        }
        
        //If the port is not connected, just move on
        return null;
    }
}
