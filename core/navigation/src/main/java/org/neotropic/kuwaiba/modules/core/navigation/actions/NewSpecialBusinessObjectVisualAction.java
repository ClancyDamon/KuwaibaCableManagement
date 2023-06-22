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
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener.ActionCompletedEvent;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.NavigationModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of a new special business object action.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class NewSpecialBusinessObjectVisualAction extends AbstractVisualInventoryAction {
    /**
     * New business object visual action parameter business object.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Attributes for the new business object
     */
    private HashMap<String, String> attributes;
    /**
     * To keep track of the fulfilled mandatory attributes
     */
    private HashMap<String, Boolean> mandatoryAttrtsState;
    /**
     * To keep the mandatory attributes of the selected class
     */
    private List<AttributeMetadata> mandatoryAttributesInSelectedClass;
    /**
     * The action dialog
     */
    private ConfirmDialog wdwConfirm;
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
    private NewSpecialBusinessObjectAction newSpecialBusinessObjectAction;
    /**
     * The name attribute in new single business object 
     * it is optional by default, but if the name is included in the mandatory 
     * attributes we need to turn it into mandatory
     */
    private TextField txtName;

    public NewSpecialBusinessObjectVisualAction() {
        super(NavigationModule.MODULE_ID);
    }

    /**
     * Creates the visual component for new object visual action
     * @param parameters need it parameters
     * @return a dialog
     */
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        attributes = new HashMap();
        mandatoryAttrtsState =  new HashMap<>();
        mandatoryAttributesInSelectedClass = new ArrayList<>();
        
        HorizontalLayout lytExtraFields =  new HorizontalLayout();
        lytExtraFields.setWidthFull();
        
        BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
        wdwConfirm = new ConfirmDialog(ts, getModuleAction().getDisplayName(), ts.getTranslatedString("module.general.labels.create"));
        wdwConfirm.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        wdwConfirm.setDraggable(true);
        wdwConfirm.getBtnConfirm().setEnabled(false);
        
        if (businessObject != null) {
            try {
                Label lblTitle = new Label(newSpecialBusinessObjectAction.getDisplayName());
                lblTitle.setClassName("dialog-title");
                Label lblParent;
                if(businessObject.getClassName().equals(Constants.DUMMY_ROOT))
                    lblParent = new Label(String.format("%s [%s]", businessObject.getName(), 
                            ts.getTranslatedString("module.general.labels.root")));
                else
                    lblParent = new Label(businessObject.toString());
                HorizontalLayout lytTitleHeader = new HorizontalLayout(lblTitle, lblParent);
                lytTitleHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                lytTitleHeader.setVerticalComponentAlignment(FlexComponent.Alignment.END, lblParent);
                lytTitleHeader.setSpacing(true);
                lytTitleHeader.setPadding(false);
                lytTitleHeader.setMargin(false);
                // Content
                ComboBox<ClassMetadataLight> cmbPossibleSpecialChildren = new ComboBox(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.object-class"));
                cmbPossibleSpecialChildren.setItems(mem.getPossibleSpecialChildren(businessObject.getClassName()));
                cmbPossibleSpecialChildren.setRequired(true);
                cmbPossibleSpecialChildren.setEnabled(true);
                cmbPossibleSpecialChildren.setPlaceholder(ts.getTranslatedString("module.navigation.actions.new-business-object.select-class"));
                cmbPossibleSpecialChildren.setItemLabelGenerator(aClass -> 
                    aClass.getDisplayName() != null && !aClass.getDisplayName().isEmpty() ? aClass.getDisplayName() : aClass.getName()
                );
                
                cmbPossibleSpecialChildren.addValueChangeListener(event -> {
                    if (event.getValue() != null) {
                        try {
                            lytExtraFields.removeAll();
                            txtName.setRequired(false);
                            txtName.clear();
                            // Mandatory attributes
                            mandatoryAttributesInSelectedClass = mem.getMandatoryAttributesInClass(event.getValue().getName());
                            if(!mandatoryAttributesInSelectedClass.isEmpty())
                                lytExtraFields.add(createFieldsForMandatoryAttributes(mandatoryAttributesInSelectedClass));
                            else
                                wdwConfirm.getBtnConfirm().setEnabled(true);
                            lytExtraFields.setSizeUndefined();
                        } catch (InventoryException ex) {
                            lytExtraFields.removeAll();
                            wdwConfirm.getBtnConfirm().setEnabled(false);
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                        }
                    }
                    else
                        wdwConfirm.getBtnConfirm().setEnabled(false);
                });

                txtName = new TextField(ts.getTranslatedString("module.navigation.actions.new-single-business-object.ui.object-name"));
                txtName.setClearButtonVisible(true);
                txtName.setValueChangeMode(ValueChangeMode.EAGER);
                
                FormLayout lytFields = new FormLayout(cmbPossibleSpecialChildren, txtName);
                lytFields.setWidthFull();
                lytFields.setResponsiveSteps(
                    new ResponsiveStep("40em", 2),
                    new ResponsiveStep("60em", 3));
                
                wdwConfirm.setContent(lytFields, lytExtraFields);
                //footer Buttons
                ShortcutRegistration btnOkShortcut = wdwConfirm.getBtnConfirm().addClickShortcut(Key.ENTER).listenOn(wdwConfirm);
                wdwConfirm.getBtnConfirm().addClickListener(event -> {
                    try {
                        attributes.put(Constants.PROPERTY_NAME, txtName.getValue());
                        
                        ModuleActionParameterSet params = new ModuleActionParameterSet(
                                new ModuleActionParameter(Constants.PROPERTY_CLASSNAME, cmbPossibleSpecialChildren.getValue().getName()),
                                new ModuleActionParameter(Constants.PROPERTY_PARENT_CLASS_NAME, businessObject.getClassName()),
                                new ModuleActionParameter(Constants.PROPERTY_PARENT_ID, businessObject.getId() == null ? "-1" : businessObject.getId()));
                        
                        params.put(Constants.PROPERTY_ATTRIBUTES, attributes);
                        
                        ActionResponse actionResponse = new ActionResponse();
                        actionResponse.put(ActionResponse.ActionType.ADD, "");
                        actionResponse.put(Constants.PROPERTY_PARENT_ID, businessObject.getId() == null ? "-1" : businessObject.getId());
                        actionResponse.put(Constants.PROPERTY_PARENT_CLASS_NAME, businessObject.getClassName());
                        actionResponse.put(PARAM_BUSINESS_OBJECT, businessObject);
                        
                        // The object is created here.                                              
                        newSpecialBusinessObjectAction.getCallback().execute(params);
                        wdwConfirm.close();
                        
                        fireActionCompletedEvent(new ActionCompletedEvent(
                                ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.navigation.actions.new-business-object.ui.success"),
                                NewBusinessObjectAction.class, actionResponse)
                        );
                        
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedEvent(
                                ActionCompletedEvent.STATUS_ERROR, ex.getMessage(),
                                NewBusinessObjectAction.class));
                    }
                    btnOkShortcut.remove();
                    event.unregisterListener();
                });
            } catch (InventoryException ex) {
                fireActionCompletedEvent(new ActionCompletedEvent(
                    ActionCompletedEvent.STATUS_ERROR, ex.getMessage(), 
                    NewBusinessObjectAction.class)
                );
                wdwConfirm.add(new Label(ex.getMessage()));
            }
        }
        return wdwConfirm;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newSpecialBusinessObjectAction;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
    
    //helpers
    /**
     * Creates a form layout with the mandatory attributes for new object
     * @param mandatoryAttributesInClass
     * @return 
     */
    private FormLayout createFieldsForMandatoryAttributes(List<AttributeMetadata> mandatoryAttributesInClass){
        FormLayout lytMandatoryAttributes = new FormLayout();
        lytMandatoryAttributes.setWidthFull();
        lytMandatoryAttributes.setResponsiveSteps(
                new ResponsiveStep("40em", 2),
                new ResponsiveStep("50em", 3));
        mandatoryAttributesInClass.forEach(attr -> {
            mandatoryAttrtsState.put(attr.getName(), false);
            if(attr.isMandatory() && AttributeMetadata.isPrimitive(attr.getType())){
                //String
                if(attr.getType().equals(String.class.getSimpleName())){
                    //the attribute name 
                    if(attr.getName().equals(Constants.PROPERTY_NAME)){
                        txtName.setRequired(true);
                        txtName.addValueChangeListener(e -> {
                            attributes.put(attr.getName(), e.getValue());
                            mandatoryAttrtsState.put(attr.getName(), (e.getValue() != null && !e.getValue().isEmpty()));
                            checkIfRequieredFieldsHasValue();
                        });
                    }
                    else{
                        TextField txtAttr = new TextField(attr.getName());
                        txtAttr.setRequiredIndicatorVisible(true);
                        lytMandatoryAttributes.add(txtAttr);
                    
                        txtAttr.addValueChangeListener(e -> {
                            attributes.put(attr.getName(), e.getValue());
                            mandatoryAttrtsState.put(attr.getName(), (e.getValue() != null && !e.getValue().isEmpty()));
                            checkIfRequieredFieldsHasValue();
                        });
                    }
                }//int and long
                else if(attr.getType().equals(Integer.class.getSimpleName()) || attr.getType().equals(Long.class.getSimpleName())){
                    IntegerField nbfAttr = new IntegerField(attr.getName());
                    nbfAttr.setHasControls(true);
                    nbfAttr.setStep(1);
                    nbfAttr.setValue(0);
                    nbfAttr.setRequiredIndicatorVisible(true);
                    lytMandatoryAttributes.add(nbfAttr);
                    
                    nbfAttr.addValueChangeListener(e -> {
                        attributes.put(attr.getName(), e.getValue().toString());
                        mandatoryAttrtsState.put(attr.getName(), (e.getValue() != null && !e.getValue().toString().isEmpty()));
                        checkIfRequieredFieldsHasValue();
                    });
                }//float and double
                else if(attr.getType().equals(Float.class.getSimpleName()) || attr.getType().equals(Double.class.getSimpleName())){
                    NumberField nbfAttr = new NumberField(attr.getName());
                    nbfAttr.setHasControls(true);
                    nbfAttr.setStep(0.1);
                    nbfAttr.setValue(0.0);
                    nbfAttr.setRequiredIndicatorVisible(true);
                    lytMandatoryAttributes.add(nbfAttr);
                    
                    nbfAttr.addValueChangeListener(e -> {
                        attributes.put(attr.getName(), e.getValue().toString());
                        mandatoryAttrtsState.put(attr.getName(), (e.getValue() != null && !e.getValue().toString().isEmpty()));
                        checkIfRequieredFieldsHasValue();
                    });
                }//boolean
                else if(attr.getType().equals(Boolean.class.getSimpleName())){
                    Checkbox cbxAttr = new Checkbox(attr.getName());
                    cbxAttr.setRequiredIndicatorVisible(true);
                    lytMandatoryAttributes.add(cbxAttr);
                    attributes.put(attr.getName(), Boolean.toString(false));
                    mandatoryAttrtsState.put(attr.getName(), true);
                   
                    cbxAttr.addValueChangeListener(e -> {
                        attributes.put(attr.getName(), e.getValue().toString());
                        checkIfRequieredFieldsHasValue();
                    });
                }//Date
                else if(attr.getType().equals(Date.class.getSimpleName())){
                    DatePicker dtpAttr = new DatePicker(attr.getName());
                    dtpAttr.setRequiredIndicatorVisible(true);
                    lytMandatoryAttributes.add(dtpAttr);
                    
                    dtpAttr.addValueChangeListener(e -> {
                        //TODO
                        //attributes.put(attr.getName(), dtpAttr.getValue().to);
                        //mandatoryAttrtsState.put(attr.getName(), (e.getValue() != null && !e.getValue().toString().isEmpty()));
                        //canBeSave();
                    });
                }//timesptap
                else if(attr.getType().equals("Timestamp")){
                    TimePicker tmpAttr = new TimePicker(attr.getName());
                    tmpAttr.setRequiredIndicatorVisible(true);
                    lytMandatoryAttributes.add(tmpAttr);

                    tmpAttr.addValueChangeListener(e -> {
                        //TODO
                        attributes.put(attr.getName(), Long.toString(e.getValue().toNanoOfDay()));
                        mandatoryAttrtsState.put(attr.getName(), (e.getValue() != null && !e.getValue().toString().isEmpty()));
                        checkIfRequieredFieldsHasValue();
                    });
                }
            }
            else{//ListTypes
                try {
                    List<BusinessObjectLight> listTypeItems = aem.getListTypeItems(attr.getType());
                    ComboBox<BusinessObjectLight> cbxListType = new ComboBox<>(attr.getName());
                    cbxListType.setAllowCustomValue(false);
                    cbxListType.setRequiredIndicatorVisible(true);
                    cbxListType.setItems(listTypeItems);
                    cbxListType.setItemLabelGenerator(listTypeItem -> listTypeItem.getName());
                    lytMandatoryAttributes.add(cbxListType);
                    
                    cbxListType.addValueChangeListener(e -> {
                        attributes.put(attr.getName(), e.getValue().getId());
                        mandatoryAttrtsState.put(attr.getName(), cbxListType.getValue() != null);
                        checkIfRequieredFieldsHasValue();
                    });
                } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(NewSpecialBusinessObjectVisualAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        return lytMandatoryAttributes;
    }
    
    /**
     * Checks if every field of the mandatory attributes has a value and 
     * enables/disables the OK button accordingly.
     */
    private void checkIfRequieredFieldsHasValue(){
        for (Map.Entry<String, Boolean> entry : mandatoryAttrtsState.entrySet()) {
            if(!entry.getValue()){
                wdwConfirm.getBtnConfirm().setEnabled(false);
                return;
            }
        }
        wdwConfirm.getBtnConfirm().setEnabled(true);
    }
}
