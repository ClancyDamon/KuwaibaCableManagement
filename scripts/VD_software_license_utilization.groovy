/** 
 * Calculates and displays how many licensed devices are in used from the total set in the "licensedDevices" attribute. 
 * This script requires that the selected GenericSoftwareAsset subclass has a "licensedDevices" attribute of type integer
 * (though the type is not particularly relevant here).
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
        
        // First we try to fetch the value of the licensedDevices attribute from the selected license
        String licensedDevices = bem.getAttributeValueAsString(objectClass, objectId, "licensedDevices");
        // Then the related invetory objects, if any
        def licensees = bem.getSpecialAttribute(objectClass, objectId, "licenseHas");
        
        Properties properties = new Properties();
        properties.setProperty("prefix", "(" +  licensees.size() + "/" + (licensedDevices == null ? "N/A" : licensedDevices) +  ")");
        return new Validator(getName(), properties);
    }
}