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
package org.neotropic.kuwaiba.modules.core.ltman.actions;

import java.util.List;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.ltman.ListTypeManagerModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new list type item action
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class NewListTypeItemVisualAction extends AbstractVisualAction<Dialog> {
    
    public static final String PARAM_LIST_TYPE = "listtype";
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewListTypeItemAction newListTypeItemAction;
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

    public NewListTypeItemVisualAction() {
        super(ListTypeManagerModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            ClassMetadataLight seletedListType = (ClassMetadataLight)parameters.get(PARAM_LIST_TYPE);
            
            if (parameters.containsKey("listType"))
                seletedListType = (ClassMetadataLight) parameters.get("listType");
            
            List<ClassMetadataLight> listTypes = mem.getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, false, false);
            
            ComboBox<ClassMetadataLight> cmbListTypes = new ComboBox<>(ts.getTranslatedString("module.ltman.list-type"), listTypes);
            cmbListTypes.setAllowCustomValue(false);
            cmbListTypes.setRequiredIndicatorVisible(true);
            cmbListTypes.setSizeFull();
            
            if (seletedListType != null) {
                cmbListTypes.setValue(seletedListType);
                cmbListTypes.setEnabled(false);
            }            
            
            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();
            
            TextField txtDisplayName = new TextField(ts.getTranslatedString("module.general.labels.display-name"));
            txtDisplayName.setSizeFull();
            
            Dialog wdwNewListTypeItem = new Dialog();

            // To show errors or warnings related to the input parameters.
            Label lblMessages = new Label();
            
            Button btnOK = new Button(ts.getTranslatedString("module.general.labels.create"), (e) -> {
                try {
                    if (cmbListTypes.getValue() == null) {
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                    } else {
                        
                        ActionResponse execute = newListTypeItemAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("className", cmbListTypes.getValue().toString()),
                                new ModuleActionParameter<>("name", txtName.getValue()),
                                new ModuleActionParameter<>("displayName", txtDisplayName.getValue())));
                        
                        
                        ActionResponse actionResponse = new ActionResponse();
                        actionResponse.put(ActionResponse.ActionType.ADD, "");
                        actionResponse.put(Constants.PROPERTY_ID, execute.get(Constants.PROPERTY_ID));
                        actionResponse.put(PARAM_LIST_TYPE, cmbListTypes.getValue());
                
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.ltman.actions.new-list-type-item.ui.item-created-success"), NewListTypeItemAction.class, actionResponse));
                        
                        wdwNewListTypeItem.close();
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), NewListTypeItemAction.class));
                }
            });
            
            btnOK.setEnabled(false);
            txtName.addValueChangeListener((e) -> {
                btnOK.setEnabled(!txtName.isEmpty());
            });
            
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel")
                    , e -> wdwNewListTypeItem.close());
            
            FormLayout lytTextFields = new FormLayout(cmbListTypes, txtName, txtDisplayName);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
            lytMain.setSizeFull();
            
            wdwNewListTypeItem.add(lytMain);
            
            return wdwNewListTypeItem;
        } catch (InventoryException ex) {
            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                    ex.getMessage(), NewListTypeItemAction.class));
            return new Dialog(new Label(ex.getMessage()));
        }        
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newListTypeItemAction;
    }
}
