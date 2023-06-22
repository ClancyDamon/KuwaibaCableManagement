/** 
 * Checks if the attribute "state" of a service is set to "Ceased" and marks it to be displayed magenta. It also adds a prefix and a suffix to the object's display name.
 * Neotropic SAS - version 1.5
 * Applies to: GenericService or its subclasses.
 */
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;

public class %s extends ValidatorDefinition {

    //Mandatory, boiler-plate constructor. 
    public %s (long id, String name, String description, String classToBeApplied, String script, boolean enabled) {
        super(id, name, description, classToBeApplied, script, enabled);
    }

    public Validator run(String objectClass, String objectId, ConnectionManager cm, 
            MetadataEntityManager mem, BusinessEntityManager bem, ApplicationEntityManager aem) {
        String state = bem.getAttributeValueAsString(objectClass, objectId, "state");

        if ("Ceased".equals(state)) {
            //Here we will put the color and additional text properties.
            Properties properties = new Properties();
            properties.setProperty("color", "#8632D9");
            properties.setProperty("prefix", "[CEASED]");
            properties.setProperty("suffix", "[XXX]");
            return new Validator(getName(), properties);
        }
        
        //If the service is not ceased, just pass along
        return null;
    }
}