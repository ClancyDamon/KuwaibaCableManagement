 /** 
  * Checks the state ot the IP addreses if is related to a network interface
  * Neotropic SAS - version 1.5
  * Applies to: IPAddress
  * Preconditions: the attribute isManagement (boolean) should exists in the classes OpticalPort, ElectricalPort 
  */
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
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

        HashMap<String, List<BusinessObjectLight>> rels = bem.getSpecialAttributes(objectClass, objectId, "ipamHasIpAddress");
        String state = bem.getAttributeValueAsString(objectClass, objectId, "state");
        //🔒📍
        if (!rels.isEmpty()) {
            String hasRelsSuffix = "🔒";
            Properties properties = new Properties();
            properties.setProperty("color", "#46739f");
            
            for (Map.Entry<String, List<BusinessObjectLight>> entry : rels.entrySet()) {
                List<BusinessObjectLight> ports = entry.getValue();
                if(ports.size() == 1 && mem.isSubclassOf("GenericPort", ports.get(0).getClassName())){
                    if((boolean)bem.getAttributeValueAsString(ports.get(0).getClassName(), ports.get(0).getId(), "isManagement")){
                        hasRelsSuffix += " 🔧"; 
                    }
                    break;
                }
            }

            properties.setProperty("suffix", hasRelsSuffix);

            return new Validator(getName(), properties);
        }
        else if(state != null && state.toLowerCase().equals("reserved")){
            Properties properties = new Properties();
            properties.setProperty("suffix", "📌");    
            return new Validator(getName(), properties);
        }
        else{
            Properties properties = new Properties();
            properties.setProperty("color", "#5bb327");
            properties.setProperty("suffix", "🔓");    
            return new Validator(getName(), properties);
        }
        //If the IP Address has no relationships, just pass along
        return null;
    }
}