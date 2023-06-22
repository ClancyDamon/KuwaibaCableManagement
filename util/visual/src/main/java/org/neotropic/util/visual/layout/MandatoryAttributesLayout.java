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
package org.neotropic.util.visual.layout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.util.visual.dialog.ConfirmDialog;

/**
 * Creates a form layout with the mandatory attributes for new object
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MandatoryAttributesLayout extends FormLayout {
    /**
     * Attributes for the new business object
     */
    private final HashMap<String, String> attributes;
    /**
     * To keep track of the fulfilled mandatory attributes
     */
    private final HashMap<String, Boolean> mandatoryAttrtsState = new HashMap();
    /**
     * The name attribute in new single business object 
     * it is optional by default, but if the name is included in the mandatory 
     * attributes we need to turn it into mandatory
     */
    private final TextField txtName;
    /**
     * The action dialog
     */
    private final ConfirmDialog wdw;
    /**
     * To keep the mandatory attributes of the selected class
     */
    private final List<AttributeMetadata> mandatoryAttributesInSelectedClass;
    /**
     * Reference to the Application Entity Manager.
     */
    private final ApplicationEntityManager aem;
    
    /**
     * Creates a form layout with the mandatory attributes for new object
     * @param txtName The name attribute in new single business object 
     * it is optional by default, but if the name is included in the mandatory 
     * attributes we need to turn it into mandatory
     * @param wdw The action dialog
     * @param attributes Attributes for the new business object
     * @param mandatoryAttributesInSelectedClass To keep the mandatory attributes of the selected class
     * @param aem Reference to the Application Entity Manager
     */
    public MandatoryAttributesLayout(TextField txtName, ConfirmDialog wdw, HashMap<String, String> attributes, 
        List<AttributeMetadata> mandatoryAttributesInSelectedClass,
        ApplicationEntityManager aem) {
        this.txtName = txtName;
        this.wdw = wdw;
        this.attributes = attributes;
        this.mandatoryAttributesInSelectedClass = mandatoryAttributesInSelectedClass;
        this.aem = aem;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        setWidthFull();
        setResponsiveSteps(
                new ResponsiveStep("40em", 2),
                new ResponsiveStep("50em", 3));
        mandatoryAttributesInSelectedClass.forEach(attr -> {
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
                        add(txtAttr);
                    
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
                    add(nbfAttr);
                    
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
                    add(nbfAttr);
                    
                    nbfAttr.addValueChangeListener(e -> {
                        attributes.put(attr.getName(), e.getValue().toString());
                        mandatoryAttrtsState.put(attr.getName(), (e.getValue() != null && !e.getValue().toString().isEmpty()));
                        checkIfRequieredFieldsHasValue();
                    });
                }//boolean
                else if(attr.getType().equals(Boolean.class.getSimpleName())){
                    Checkbox cbxAttr = new Checkbox(attr.getName());
                    cbxAttr.setRequiredIndicatorVisible(true);
                    add(cbxAttr);
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
                    add(dtpAttr);
                    
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
                    add(tmpAttr);

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
                    add(cbxListType);
                    
                    cbxListType.addValueChangeListener(e -> {
                        attributes.put(attr.getName(), e.getValue().getId());
                        mandatoryAttrtsState.put(attr.getName(), cbxListType.getValue() != null);
                        checkIfRequieredFieldsHasValue();
                    });
                } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(MandatoryAttributesLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    /**
     * Checks if every filed of the mandatory attributes has a value and 
     * enables or disables the ok button
     */
    private void checkIfRequieredFieldsHasValue(){
        for (Map.Entry<String, Boolean> entry : mandatoryAttrtsState.entrySet()) {
            if(!entry.getValue()){
                wdw.getBtnConfirm().setEnabled(false);
                return;
            }
        }
        wdw.getBtnConfirm().setEnabled(true);
    }
}
