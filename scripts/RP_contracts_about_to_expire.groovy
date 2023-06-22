/**
* Wrapper for the original, hard-coded report that displays a list of contracts and their expiration dates
* Neotropic SAS - version 1.1
* Parameters: None
*/
import com.neotropic.kuwaiba.modules.reporting.defaults.DefaultReports;
import com.neotropic.kuwaiba.modules.reporting.html.*;

try {
    return defaultReports.buildContractStatusReport();
} catch (Exception ex) {
    def htmlReport = new HTMLReport("Contract Status", "Neotropic SAS", "1.1");
    htmlReport.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());
    htmlReport.getComponents().add(new HTMLMessage(null, "error", ex.getMessage()));
    return htmlReport;
}
