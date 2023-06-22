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
package com.neotropic.kuwaiba.modules.commercial.sdh.actions;

import com.neotropic.kuwaiba.modules.commercial.sdh.SdhModule;
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
import org.neotropic.util.visual.general.BoldLabel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new SDH view action
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class NewSdhViewVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewSdhViewAction newSDHViewAction;
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

    public NewSdhViewVisualAction() {
        super(SdhModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {       
                
        BoldLabel lblParentClass = new BoldLabel(ts.getTranslatedString("module.sdh.actions.new-view.name"));
        TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));      
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();
        TextArea txtDescription = new TextArea(ts.getTranslatedString("module.general.labels.description"));
        txtDescription.setSizeFull();
        Dialog wdwNewClass = new Dialog();
        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();
        Button btnOK = new Button(ts.getTranslatedString("module.general.labels.create"), (e) -> {
            try {
                if (txtName.getValue() == null || txtName.getValue().isEmpty()) {
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                } else {
                    
                    ActionResponse response = newSDHViewAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("viewName", txtName.getValue()), 
                            new ModuleActionParameter<>("description", txtDescription.getValue())));
                    
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.sdh.actions.new-view-created-success"), NewSdhViewAction.class, response));
                    wdwNewClass.close();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewSdhViewAction.class));
            }
        });
        
        btnOK.setEnabled(false);
        txtName.addValueChangeListener((e) -> {
            btnOK.setEnabled(!txtName.isEmpty());
        });
        
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (e) -> {
            wdwNewClass.close();
        });
        
        FormLayout lytTextFields = new FormLayout(lblParentClass, txtName, txtDescription);
        HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
        VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
        lytMain.setSizeFull();
        wdwNewClass.add(lytMain);
        
        return wdwNewClass;  
        
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newSDHViewAction;
    }
}
