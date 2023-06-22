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
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.ArrayList;
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
 * Creates multiple items in a template.
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Component
public class NewBulkTemplateItemVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Name for new template item
     */
    private TextField txtItemName;
    /**
     * Class name of parent class selected
     */
    private String parentClassName;
    /**
     * Class name of parent id selected
     */
    private String parentId;   
    /**
     * all possible children of inventory class
     */
    private ComboBox<ClassMetadataLight> cmbPossibleChildren;
    /**
     * Confirmation dialog for create new template items
     */
    private ConfirmDialog cfdNewItem;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewBulkTemplateItemAction newBulkTemplateItemAction;
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

    public NewBulkTemplateItemVisualAction() {
        super(TemplateManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            //read parameter from parent layout
            parentClassName = (String) parameters.get(Constants.PROPERTY_PARENT_CLASS_NAME);
            parentId = (String) parameters.get(Constants.PROPERTY_PARENT_ID);
            //create dialog content
            txtItemName = new TextField();
            cmbPossibleChildren = new ComboBox<>();
            FormLayout lytvContentDialog = new FormLayout();
            cfdNewItem = new ConfirmDialog(ts,
                     this.newBulkTemplateItemAction.getDisplayName(),
                     lytvContentDialog,
                     ts.getTranslatedString("module.general.labels.create")
            );
            //define elements behavior
            txtItemName.setLabel(ts.getTranslatedString("module.general.labels.pattern-name"));
            txtItemName.setSizeFull();
            txtItemName.setRequiredIndicatorVisible(true);
            txtItemName.setValueChangeMode(ValueChangeMode.EAGER);
            txtItemName.setPlaceholder(ts.getTranslatedString("module.templateman.component.txt.special-item.placeholder"));
            txtItemName.addValueChangeListener(e -> validateCreateAction());

            cmbPossibleChildren.setLabel(ts.getTranslatedString("module.templateman.component.cbx.template-item.label"));
            cmbPossibleChildren.setSizeFull();
            cmbPossibleChildren.setAutofocus(true);
            cmbPossibleChildren.setItemLabelGenerator(element -> !element.getDisplayName().isEmpty() ? element.getDisplayName() : element.getName());
            cmbPossibleChildren.addValueChangeListener(e -> validateCreateAction());
            buildClasessItemsProvider();
            // add elements to dialog content 
            lytvContentDialog.add(cmbPossibleChildren, txtItemName);
            //validation listeners
            cfdNewItem.getBtnConfirm().addClickListener(e -> createNewTemplateItems());
            cfdNewItem.getBtnConfirm().setEnabled(false);
            return cfdNewItem;
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            ConfirmDialog erroDialog = new ConfirmDialog(ts
                    , ts.getTranslatedString("module.templateman.component.dialog.new-template-item-multiple.description")
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
        boolean enable = !txtItemName.isEmpty() && cmbPossibleChildren.getValue() != null;
        cfdNewItem.getBtnConfirm().setEnabled(enable);
    }
    
    /**
     * Create data provider for principal combo box
     * @throws MetadataObjectNotFoundException; not found or invalid query
     * search
     */
    private void buildClasessItemsProvider() throws MetadataObjectNotFoundException, InvalidArgumentException {
        List<ClassMetadataLight> possibleChildren = mem.getPossibleChildrenNoRecursive(parentClassName);
        if (possibleChildren.isEmpty()) {
            throw new MetadataObjectNotFoundException(String.format("The containment configuration does not allow instances of class %s to have children.", parentClassName));
        } else {
            List<ClassMetadataLight> possibleSpecialChildrensNoSpecial = new ArrayList<>();
            //get all special children in case an abstract class 
            for(ClassMetadataLight specialChildren: possibleChildren){
                if(specialChildren.isAbstract()){
                    List<ClassMetadataLight> directPossibleSpecialChildren = mem.getSubClassesLight(specialChildren.getName(), false, false);
                    possibleSpecialChildrensNoSpecial.addAll(directPossibleSpecialChildren);
                    
                } else
                    possibleSpecialChildrensNoSpecial.add(specialChildren);
            }
            //order alphabetically
            possibleSpecialChildrensNoSpecial.stream().sorted(Comparator.comparing(ClassMetadataLight::toString))
                    .collect(Collectors.toList());
            /*
            * Providing a custom item filter allows filtering based on all of the
            * rendered properties:
            */
            ComboBox.ItemFilter<ClassMetadataLight> filter = (element, filterString) -> element
                    .getName().toUpperCase().startsWith(filterString.toUpperCase());
            this.cmbPossibleChildren.setItems(filter, possibleSpecialChildrensNoSpecial);
        }
    }
    
    /**
     * database action, create new bulk template items
     */
    private void createNewTemplateItems() {
        try {
            newBulkTemplateItemAction.getCallback().execute(new ModuleActionParameterSet(
                    new ModuleActionParameter<>(Constants.PROPERTY_CLASSNAME, cmbPossibleChildren.getValue().getName()),
                    new ModuleActionParameter<>(Constants.PROPERTY_PARENT_CLASS_NAME, parentClassName),
                    new ModuleActionParameter<>(Constants.PROPERTY_PARENT_ID, parentId),
                    new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtItemName.getValue())
            ));
            
            ActionResponse actionResponse = new ActionResponse();
            actionResponse.put(ActionResponse.ActionType.ADD, "");
            actionResponse.put(Constants.PROPERTY_ID, parentId); //the affected node id
                        
            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS
                    , ts.getTranslatedString("module.templateman.actions.new-template-item.multiple-item-created-success")
                    , NewTemplateAction.class
                    , actionResponse));
            cfdNewItem.close();
        } catch (ModuleActionException ex) {
            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                    ex.getMessage(), NewTemplateAction.class));
        }
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newBulkTemplateItemAction;
    }
    
    public String getTitle(){
        return ts.getTranslatedString("module.templateman.actions.new-template-item-multiple.name");
    }
}