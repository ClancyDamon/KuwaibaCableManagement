 /** 
 * Checks if a port status by parsing the value of its "state" attribute. By default 
 * only checks for two states: Available, Reserved, Expired and Disabled
 * Neotropic SAS - version 1.0
 * Applies to: GenericSoftwareAsset or its subclasses.
 */
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;

public class %s extends ValidatorDefinition {
    // Mandatory, boiler-plate constructor
    public %s (long id, String name, String description, String classToBeApplied, String script, boolean enabled) {
        super(id, name, description, classToBeApplied, script, enabled);
    }

    public Validator run(String objectClass, String objectId, ConnectionManager cm, 
            MetadataEntityManager mem, BusinessEntityManager bem, ApplicationEntityManager aem) {
        
        String state = bem.getAttributeValueAsString(objectClass, objectId, "state");
        
        switch (state) {
            case "Available":
                //Here we will put the color and additional text properties.
                Properties properties = new Properties();
                properties.setProperty("color", "#00FF00");
                properties.setProperty("prefix", "[🙂]");
                return new Validator(getName(), properties);
            case "Reserved":
                Properties properties = new Properties();
                properties.setProperty("color", "#800080");
                properties.setProperty("prefix", "[😑]");
                return new Validator(getName(), properties);
            case "Expired":
                Properties properties = new Properties();
                properties.setProperty("color", "#FF0000");
                properties.setProperty("prefix", "[🙁]");
                return new Validator(getName(), properties);
            case "Disabled":
                Properties properties = new Properties();
                properties.setProperty("color", "#FFFFFF");
                properties.setProperty("fill-color", "#0000FF");
                return new Validator(getName(), properties);
            default:
                return null;
        }
    }
}