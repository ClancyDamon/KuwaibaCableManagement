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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.reports.ReportsModule;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new class report action
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class NewClassReportVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewClassReportAction newClassReportAction;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    protected BusinessEntityManager bem;

    public NewClassReportVisualAction() {
        super(ReportsModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
              
        TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
        TextField txtDescription = new TextField(ts.getTranslatedString("module.general.labels.description"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();
        txtDescription.setSizeFull();
        Dialog wdwNewReport = new Dialog();
        ComboBox<Integer> cmbType = new ComboBox(ts.getTranslatedString("module.general.labels.type"));
        cmbType.setWidthFull();
        cmbType.setItems(Arrays.asList(ReportMetadataLight.TYPE_HTML,
                                       ReportMetadataLight.TYPE_CSV,                                     
                                       ReportMetadataLight.TYPE_PDF,
                                       ReportMetadataLight.TYPE_XLSX,
                                       ReportMetadataLight.TYPE_OTHER));
        cmbType.setValue(ReportMetadataLight.TYPE_HTML);
        cmbType.setItemLabelGenerator(item -> ReportMetadataLight.getTypeAsString(item));
        ClassMetadataLight selectedClass = (ClassMetadataLight) parameters.get("class");
        
        List<ClassMetadataLight> inventoryObjectClasses;
        try {
            inventoryObjectClasses = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, true, true);
        } catch (MetadataObjectNotFoundException ex) {
            Logger.getLogger(NewClassReportVisualAction.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
            return null;
        }
        ComboBox<ClassMetadataLight> cmbFilterClassReports = new ComboBox<>(ts.getTranslatedString("module.general.labels.class-name"));
        cmbFilterClassReports.setWidthFull();
        cmbFilterClassReports.setItems(inventoryObjectClasses);
        cmbFilterClassReports.setClearButtonVisible(true);
        if (selectedClass != null)
            cmbFilterClassReports.setValue(selectedClass);
        cmbFilterClassReports.setItemLabelGenerator(ClassMetadataLight::getName);
        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();
        
        Button btnOK = new Button(ts.getTranslatedString("module.general.labels.create"), (e) -> {
            try {
                if (txtName.getValue() == null || txtName.getValue().isEmpty() || cmbFilterClassReports.getValue() == null 
                        || cmbType.getValue() == null) 
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                 else {
                    
                    newClassReportAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("className", cmbFilterClassReports.getValue().getName()),
                            new ModuleActionParameter<>("reportName", txtName.getValue()),
                            new ModuleActionParameter<>("type", cmbType.getValue()),
                            new ModuleActionParameter<>("description", txtDescription.getValue())));
                    
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString(ts.getTranslatedString("module.reporting.actions.report-created")), NewClassReportAction.class));
                    wdwNewReport.close();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewClassReportAction.class));
            }
        });
        
        btnOK.setEnabled(false);
        txtName.addValueChangeListener((e) -> {
            btnOK.setEnabled(!txtName.isEmpty());
        });
        
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (e) -> {
            wdwNewReport.close();
        });
        
        FormLayout lytTextFields = new FormLayout(cmbFilterClassReports, txtName, txtDescription, cmbType, lblMessages);
        HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
        VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
        lytMain.setSizeFull();
        wdwNewReport.add(lytMain);
        
        return wdwNewReport;        
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newClassReportAction;
    }
}
