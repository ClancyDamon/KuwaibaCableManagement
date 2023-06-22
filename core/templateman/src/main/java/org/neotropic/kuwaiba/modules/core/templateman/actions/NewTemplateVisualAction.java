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
package org.neotropic.kuwaiba.modules.core.templateman.actions;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.templateman.TemplateManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Create a new template.
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Component
public class NewTemplateVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Class metadata Parameter.
     */
    public static String PARAM_CLASS_METADATA = "classmetadata"; //NOI18N
    /**
     * Class selected in inventory class combo box
     */
    private ClassMetadataLight selectedClass;
    /**
     * Rendered all inventory classes
     */
    private ComboBox<ClassMetadataLight> cmbAllClasess;
    /**
     * Name for new template
     */
    private TextField txtTemplateName;
    /**
     * Confirmation dialog for create new template
     */
    private ConfirmDialog cfdNewTemplateItem;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewTemplateAction newTemplateItemAction;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;

    public NewTemplateVisualAction() {
        super(TemplateManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            //read parameter from parent layout
            selectedClass = (ClassMetadataLight) parameters.get(PARAM_CLASS_METADATA);
            //create dialog content
            cmbAllClasess = new ComboBox<>();
            txtTemplateName = new TextField();
            VerticalLayout lytvContentDialog = new VerticalLayout();
            cfdNewTemplateItem  = new ConfirmDialog(ts
                    , this.newTemplateItemAction.getDisplayName() 
                    , lytvContentDialog  
                    , ts.getTranslatedString("module.general.labels.create")
                    );
            cfdNewTemplateItem.setWidth("30%");
            //define elements behavior
            txtTemplateName.setLabel(ts.getTranslatedString("module.general.labels.name"));
            txtTemplateName.setSizeFull();
            txtTemplateName.setRequiredIndicatorVisible(true);
            txtTemplateName.setValueChangeMode(ValueChangeMode.EAGER);
            txtTemplateName.addValueChangeListener(e -> validateCreateAction());
            
            cmbAllClasess.setLabel(String.format("%s", ts.getTranslatedString("module.templateman.clases")));
            cmbAllClasess.setWidthFull();
            cmbAllClasess.setAutofocus(true);
            cmbAllClasess.setClearButtonVisible(true);
            cmbAllClasess.setItemLabelGenerator(element -> !element.getDisplayName().isEmpty() ? element.getDisplayName() : element.getName());
            cmbAllClasess.addValueChangeListener(event -> {
                selectedClass = event.getValue();
                validateCreateAction();
            });            
            buildClasessItemsProvider();
            //add elements to content dialog 
            lytvContentDialog.add(cmbAllClasess, txtTemplateName);
            
            cfdNewTemplateItem.getBtnConfirm().addClickListener(event-> createNewTemplate());            
            cfdNewTemplateItem.getBtnConfirm().setEnabled(false);
            return cfdNewTemplateItem;
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            ConfirmDialog erroDialog = new ConfirmDialog(ts
                    , ts.getTranslatedString("module.templateman.component.dialog.new-template.description") 
                    , ex.getMessage()
                    , ts.getTranslatedString("module.general.messages.ok") 
            );       
            erroDialog.getBtnConfirm().addClickListener(e -> erroDialog.close());
            return erroDialog;
        }
    }

    /**
     * enable button action in case all fields are correct
     */
    private void validateCreateAction() {
        boolean enable = !txtTemplateName.isEmpty() && selectedClass != null;
        cfdNewTemplateItem.getBtnConfirm().setEnabled(enable);
    }

    /**
     * database action, create new template
     */
    private void createNewTemplate() {
        try {
            newTemplateItemAction.getCallback().execute(new ModuleActionParameterSet(
                    new ModuleActionParameter<>("className", (String) cmbAllClasess.getValue().getName()),
                    new ModuleActionParameter<>("name", txtTemplateName.getValue())
            ));
            
            ActionResponse actionResponse = new ActionResponse();
                        actionResponse.put(ActionResponse.ActionType.ADD, "");
                        actionResponse.put(PARAM_CLASS_METADATA, cmbAllClasess.getValue()); //the affected node id
            
            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                    ts.getTranslatedString("module.templateman.actions.new-template-item.ui.item-created-success")
                    , NewTemplateAction.class, actionResponse));
            
            cfdNewTemplateItem.close();
            
        } catch (ModuleActionException ex) {
            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                    ex.getMessage(), NewTemplateAction.class));
        }
    }
    
    /**
     * Create data provider for principal combo box
     * @throws MetadataObjectNotFoundException; not found or invalid query
     * search
     */
    private void buildClasessItemsProvider() throws MetadataObjectNotFoundException, InvalidArgumentException {
        List<ClassMetadataLight> allClassesLight = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, false, false);
        //order alphabetically
        allClassesLight.stream().sorted(Comparator.comparing(ClassMetadataLight::toString))
                .collect(Collectors.toList());

        /*
        * Providing a custom item filter allows filtering based on all of the
        * rendered properties:
         */
        ComboBox.ItemFilter<ClassMetadataLight> filter = (element, filterString) -> element
                .getName().toUpperCase().startsWith(filterString.toUpperCase());
        this.cmbAllClasess.setItems(filter, allClassesLight);
        this.cmbAllClasess.setValue(selectedClass);   
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newTemplateItemAction;
    }
}