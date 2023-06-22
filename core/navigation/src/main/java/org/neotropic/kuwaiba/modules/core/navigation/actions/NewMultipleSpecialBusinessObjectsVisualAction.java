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
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener.ActionCompletedEvent;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.NavigationModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create bulk business objects action.
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class NewMultipleSpecialBusinessObjectsVisualAction extends AbstractVisualInventoryAction {
    /**
     * business object parameter, used to retrieve the parent as a parameter.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to module action.
     */
    @Autowired
    private NewMultipleSpecialBusinessObjectsAction newMultipleSpecialBusinessObjectsAction;

    public NewMultipleSpecialBusinessObjectsVisualAction() {
        super(NavigationModule.MODULE_ID);
    }
    
    /**
     * Creates the visual component for new multiple object visual action
     * @param parameters The required parameters, in this case, the parent of the new objects. The parameter must be labeled <code>businessObject</code>.
     * @return A dialog requiring an object type, a template and a naming pattern. 
     */
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
        ConfirmDialog wdw = new ConfirmDialog(ts, getModuleAction().getDisplayName(), ts.getTranslatedString("module.general.labels.create"));
        wdw.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        wdw.setDraggable(true);
        
        if (businessObject != null) {
            try {
                //Header
                Label lblTitle = new Label(ts.getTranslatedString("module.navigation.actions.new-multiple-business-object"));
                lblTitle.setClassName("dialog-title");
                Label lblParent;
                if(businessObject.getClassName().equals(Constants.DUMMY_ROOT))
                    lblParent = new Label(String.format("%s [%s]"
                            , businessObject.getName()
                            , ts.getTranslatedString("module.general.labels.root")));
                else
                    lblParent = new Label(businessObject.toString());
                HorizontalLayout lytTitleHeader = new HorizontalLayout(lblTitle, lblParent);
                lytTitleHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                lytTitleHeader.setVerticalComponentAlignment(FlexComponent.Alignment.END, lblParent);
                lytTitleHeader.setSpacing(true);
                lytTitleHeader.setPadding(false);
                lytTitleHeader.setMargin(false);

                //Content
                ComboBox<ClassMetadataLight> cmbPossibleChildren = new ComboBox(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.object-class"));
                cmbPossibleChildren.setItems(mem.getPossibleSpecialChildren(businessObject.getClassName()));
                cmbPossibleChildren.setRequired(true);
                cmbPossibleChildren.setEnabled(true);
                cmbPossibleChildren.setPlaceholder(ts.getTranslatedString("module.navigation.actions.new-business-object.select-class"));
                cmbPossibleChildren.setItemLabelGenerator(aClass -> 
                    aClass.getDisplayName() != null && !aClass.getDisplayName().isEmpty() ? aClass.getDisplayName() : aClass.getName()
                );
                
                //To keep the AccordionPanel for templates to remove it when the selected class has no template
                cmbPossibleChildren.addValueChangeListener(e -> {
                    cmbPossibleChildren.setInvalid(e.getValue() != null);
                    wdw.getBtnConfirm().setEnabled(e.getValue() != null);
                });
                //Pattern
                TextField txtPattern = new TextField(ts.getTranslatedString("module.navigation.actions.new-multiple-business-objects-pattern"),
                        e -> {
                            e.getSource().setInvalid(!e.getValue().isEmpty());
                            wdw.getBtnConfirm().setEnabled(e.getValue() != null);
                        });
                txtPattern.setRequiredIndicatorVisible(true);
                txtPattern.setPlaceholder(ts.getTranslatedString("module.navigation.actions.new-multiple-business-objects-pattern-example"));
                txtPattern.setSizeFull();
                
                HorizontalLayout lytFields = new HorizontalLayout(cmbPossibleChildren, txtPattern);
                lytFields.setWidthFull();
                wdw.setContent(lytFields);
                //Buttons
                ShortcutRegistration btnOkShortcut = wdw.getBtnConfirm().addClickShortcut(Key.ENTER).listenOn(wdw);
                wdw.getBtnConfirm().setEnabled(false);
                wdw.getBtnConfirm().addClickListener(event -> {
                    try {
                        if(txtPattern.getValue() != null){
                            ModuleActionParameterSet params = new ModuleActionParameterSet(
                                    new ModuleActionParameter(Constants.PROPERTY_CLASSNAME, cmbPossibleChildren.getValue().getName()),
                                    new ModuleActionParameter(Constants.PROPERTY_PARENT_ID, businessObject.getId()),
                                    new ModuleActionParameter(Constants.PROPERTY_PARENT_CLASS_NAME, businessObject.getClassName()),
                                    new ModuleActionParameter(Constants.PROPERTY_PATTERN, txtPattern.getValue()),
                                    new ModuleActionParameter(Constants.PROPERTY_TEMPLATE_ID, ""));

                            //Here we create the objects and we return the number of created objects                                                
                            ActionResponse actionResponse = newMultipleSpecialBusinessObjectsAction.getCallback().execute(params);
                            wdw.close();

                            actionResponse.put(Constants.PROPERTY_PARENT_ID, businessObject.getId());
                            actionResponse.put(Constants.PROPERTY_PARENT_CLASS_NAME, businessObject.getClassName());
                            actionResponse.put(PARAM_BUSINESS_OBJECT, businessObject);

                            fireActionCompletedEvent(new ActionCompletedEvent(
                                    ActionCompletedEvent.STATUS_SUCCESS,
                                    ts.getTranslatedString("module.navigation.actions.new-business-object.ui.success"),
                                    NewBusinessObjectAction.class, actionResponse)
                            );
                        }
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedEvent(
                                ActionCompletedEvent.STATUS_ERROR, ex.getMessage(),
                                NewMultipleBusinessObjectsAction.class));
                    }
                    btnOkShortcut.remove();
                    event.unregisterListener();
                });
            } catch (InventoryException ex) {
                fireActionCompletedEvent(new ActionCompletedEvent(
                    ActionCompletedEvent.STATUS_ERROR, ex.getMessage(), 
                    NewMultipleBusinessObjectsAction.class)
                );
                wdw.add(new Label(ex.getMessage()));
            }
        }
        return wdw;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newMultipleSpecialBusinessObjectsAction;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
}
