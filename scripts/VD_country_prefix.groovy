/** 
 * Check the country shortcode, render the prefix with its corresponding flag.
 * Shotcode examples:
 * +----------------------+
 * | Country  | Shortcode |
 * +----------------------+
 * | Colombia | CO        |
 * | EEUU     | US        |
 * +----------------------+
 * See more on https://emojipedia.org/flags/
 * 
 * Applies to: Country.
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
        
        def regionalIndicators = [
            A: "🇦", B: "🇧", C: "🇨", D: "🇩", E: "🇪",
            F: "🇫", G: "🇬", H: "🇭", I: "🇮", J: "🇯",
            K: "🇰", L: "🇱", M: "🇲", N: "🇳", O: "🇴",
            P: "🇵", Q: "🇶", R: "🇷", S: "🇸", T: "🇹",
            U: "🇺", V: "🇻", W: "🇼", X: "🇽", Y: "🇾",
            Z: "🇿"
        ]
        String shortcode = bem.getAttributeValueAsString(objectClass, objectId, "shortcode")

        if (shortcode != null && shortcode.length() == 2) {
            Properties properties = new Properties()
            properties.setProperty(
                "prefix", 
                regionalIndicators[shortcode.getAt(0)] + regionalIndicators[shortcode.getAt(1)]
            )
            return new Validator(getName(), properties)
        }
        return null
    }
}