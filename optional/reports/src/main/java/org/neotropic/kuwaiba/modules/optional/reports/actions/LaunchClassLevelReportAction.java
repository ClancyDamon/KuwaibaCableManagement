/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.optional.reports.actions;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.reports.ReportsModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Shows the class level report of the given business Object
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class LaunchClassLevelReportAction extends AbstractVisualAdvancedAction {
   /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;

    public LaunchClassLevelReportAction() {
        super(ReportsModule.MODULE_ID);
    }
    
    /**
     * Executes the given report and creates a downloadable file 
     * @param selectedReport 
     */
    private void executeInventoryReport(BusinessObjectLight selectedObject, ReportMetadataLight selectedReport) {
        if (selectedObject == null)
             new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                                ts.getTranslatedString("module.general.messages.select-items"), AbstractNotification.NotificationType.WARNING, ts).open();
        else {
            try {
                byte[] reportBody = bem.executeClassLevelReport(selectedObject.getClassName(), selectedObject.getId(), 
                        selectedReport.getId());

                final StreamResource resource = new StreamResource("class-level-report" + selectedReport.getId(),
                    () -> new ByteArrayInputStream(reportBody));
                resource.setContentType(ReportMetadataLight.getMimeTypeForReport(selectedReport.getType()));         
                final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
                UI.getCurrent().getPage().open(registration.getResourceUri().toString());
            } catch (InventoryException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                                ex.getLocalizedMessage(), AbstractNotification.NotificationType.WARNING, ts).open();
            }
        }
    } 

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }

    @Override
    public ConfirmDialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            BusinessObjectLight selectedObject = (BusinessObjectLight)parameters.get("businessObject");
            ConfirmDialog wdwLaunchReport = new ConfirmDialog();
            Button btnClose = new Button(ts.getTranslatedString("module.general.messages.cancel"), ev -> wdwLaunchReport.close());
            btnClose.setWidthFull();
            wdwLaunchReport.setFooter(btnClose);
            
            if (selectedObject != null) {
                List<ReportMetadataLight> reports = bem.getClassLevelReports(selectedObject.getClassName(), true, false);
                wdwLaunchReport.setHeader(new Label(String.format(ts.getTranslatedString("module.reporting.actions.launch-report.class-level-report-available"), 
                            selectedObject.getClassName())));
                
                if (reports.isEmpty())
                    wdwLaunchReport.setContent(new Label(ts.getTranslatedString("module.reporting.actions.launch-report.no-reports")));
                else {
                    VerticalLayout lytContent = new VerticalLayout();
                    lytContent.setWidthFull();
                    lytContent.setSpacing(false);
                    lytContent.setPadding(false);

                    reports.stream().forEach( aReport -> {
                        Button btnReport = new Button(aReport.getName());
                        btnReport.addClickListener(ev -> {
                            executeInventoryReport(selectedObject, aReport);
                            wdwLaunchReport.close();
                        });
                        btnReport.setWidthFull();
                        lytContent.add(btnReport);
                    });
                    wdwLaunchReport.setContent(lytContent);
                }
            } else
                wdwLaunchReport.setContent(new Label(ts.getTranslatedString("module.general.messages.select-items")));

            return wdwLaunchReport;
        } catch (InventoryException ex) {
            return new ConfirmDialog(ts, ex.getMessage(), ts.getTranslatedString("module.general.messages.ok"));
        }
    }

    @Override
    public AbstractAction getModuleAction() {
        return null;
    }
    
    @Override
    public String getName() {
        return ts.getTranslatedString("module.reporting.actions.launch-report.title");
    }
}
