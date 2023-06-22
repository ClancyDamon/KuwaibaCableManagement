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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.reports.ReportsModule;
import org.neotropic.util.visual.general.BoldLabel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new report parameter action.
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class NewParameterVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewParameterAction newTaskParameterAction;

    public NewParameterVisualAction() {
        super(ReportsModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        ReportMetadataLight selectedReport;
        if (parameters.containsKey("report")) 
            selectedReport = (ReportMetadataLight) parameters.get("report");
        else
            return null;

        BoldLabel lblReport = new BoldLabel(String.format("%s: %s", ts.getTranslatedString("module.reporting.report") , selectedReport.getName()));
        TextField txtParameterName = new TextField(ts.getTranslatedString("module.reporting.parameters.name"));
        txtParameterName.setRequiredIndicatorVisible(true);
        txtParameterName.setSizeFull();

        // Window to create a new task parameter
        Dialog wdwNewTaskParameter = new Dialog();

        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();

        Button btnOK = new Button(ts.getTranslatedString("module.general.labels.create"), (event) -> {
            try {
                if (txtParameterName.getValue() == null || txtParameterName.getValue().isEmpty()) 
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                else {
                    newTaskParameterAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("report", selectedReport.getId()),
                            new ModuleActionParameter<>("name", txtParameterName.getValue())
                    ));

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString(ts.getTranslatedString("module.reporting.actions.parameter-created")), NewParameterAction.class));
                    wdwNewTaskParameter.close();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewParameterAction.class));
            }
        });

        btnOK.setEnabled(false);
        txtParameterName.addValueChangeListener((event) -> {
            btnOK.setEnabled(!txtParameterName.isEmpty());
        });

        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (event) -> {
            wdwNewTaskParameter.close();
        });

        FormLayout lytAttributes = new FormLayout(lblReport, txtParameterName);
        HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
        VerticalLayout lytMain = new VerticalLayout(lytAttributes, lytMoreButtons);
        lytMain.setSizeFull();

        wdwNewTaskParameter.add(lytMain);
        return wdwNewTaskParameter;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newTaskParameterAction;
    }
    
}
