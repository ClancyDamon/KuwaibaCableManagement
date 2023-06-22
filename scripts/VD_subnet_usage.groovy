 /** 
 * Checks the usage of a subnet, how many IP Addresses are reserved or related to a network interface
 * Neotropic SAS - version 1.0
 * Applies to: GenericSubnet
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
    // Mandatory, boiler-plate constructor
    public %s (long id, String name, String description, String classToBeApplied, String script, boolean enabled) {
        super(id, name, description, classToBeApplied, script, enabled);
    }

    public Validator run(String objectClass, String objectId, ConnectionManager cm, 
            MetadataEntityManager mem, BusinessEntityManager bem, ApplicationEntityManager aem) {
        
        List<BusinessObjectLight> usdedIpAddrs = new ArrayList<>();
        List<BusinessObjectLight> reservedIpAddrs = new ArrayList<>();
        List<String> classNamesToFilter = new ArrayList<>();
        classNamesToFilter.add("IPAddress");
        
        List<BusinessObjectLight> ipAddrs = bem.getObjectSpecialChildrenWithFilters(objectClass, objectId, classNamesToFilter,  -1, -1);
        for (BusinessObjectLight ip : ipAddrs) {
            HashMap<String, List<BusinessObjectLight>> rels = bem.getSpecialAttributes("IPAddress", ip.getId(), "ipamHasIpAddress");
            if(!rels.isEmpty())
                usdedIpAddrs.add(ip);
            String state = bem.getAttributeValueAsString("IPAddress", ip.getId(), "state"); //this is
            if(state != null && state.toLowerCase().equals("reserved")) //TODO move this hard string reserved
                reservedIpAddrs.add(ip);
        }
        
        long busy = usdedIpAddrs.size();
        long hosts = Integer.valueOf(bem.getAttributeValueAsString(objectClass, objectId, "hosts"));
        long reserved = reservedIpAddrs.size();
        long usage = ((busy + reserved) * 100) / hosts
        
        if (usage < 60) {
            //Here we will put the color and additional text properties.
            Properties properties = new Properties();
            properties.setProperty("color", "#5bb327");
            properties.setProperty("suffix","(" + (String)usage + "%%)");
            return new Validator(getName(), properties);
        }
        else if(usage > 61 && usage < 95){
            //Here we will put the color and additional text properties.
            Properties properties = new Properties();
            properties.setProperty("color", "#bd9f00");
            properties.setProperty("suffix", "(" + (String)usage + "%%)");
            return new Validator(getName(), properties);
        }
        else if( usage >= 95){
            //Here we will put the color and additional text properties.
            Properties properties = new Properties();
            properties.setProperty("color", "#f04f47");
            properties.setProperty("suffix","(" + (String)usage + "%%)");
            return new Validator(getName(), properties);
        }
        
        //If the subnet attributes are not set, just pass along
        return null;
    }
}