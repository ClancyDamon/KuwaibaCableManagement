/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.commercial.mpls.actions;

import com.neotropic.kuwaiba.modules.commercial.mpls.MplsModule;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.BoldLabel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new MPLS view action
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class NewMplsViewVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewMplsViewAction newMPLSViewAction;
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

    public NewMplsViewVisualAction() {
        super(MplsModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {       
                
        TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));      
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();
        TextArea txtDescription = new TextArea(ts.getTranslatedString("module.general.labels.description"));
        txtDescription.setSizeFull();
        ConfirmDialog wdwNewClass = new ConfirmDialog(ts, ts.getTranslatedString("module.mpls.actions.new-mpls-view"), ts.getTranslatedString("module.general.messages.ok"));
        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();
        wdwNewClass.getBtnConfirm().addClickListener(e -> {
            try {
                if (txtName.getValue() == null || txtName.getValue().isEmpty()) {
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                } else {
                    
                    ActionResponse response = newMPLSViewAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("viewName", txtName.getValue()), 
                            new ModuleActionParameter<>("description", txtDescription.getValue())));
                    
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.mpls.actions.new-view-created-success"), NewMplsViewAction.class, response));
                    wdwNewClass.close();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewMplsViewAction.class));
            }
        });
        
        wdwNewClass.getBtnConfirm().setEnabled(false);
        txtName.addValueChangeListener((e) -> {
            wdwNewClass.getBtnConfirm().setEnabled(!txtName.isEmpty());
        });      
        
        FormLayout lytTextFields = new FormLayout(txtName, txtDescription);
        wdwNewClass.setContent(lytTextFields);
        
        return wdwNewClass;  
        
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newMPLSViewAction;
    }
}
