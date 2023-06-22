/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.tools;

import com.neotropic.kuwaiba.modules.commercial.processman.components.ElementPropertyEditorDialog;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.ButtonElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.CheckBoxElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.ComboBoxElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.ElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.GridElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.HorizontalLayoutElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.LabelElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.TextFieldElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.VerticalLayoutElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.service.ArtifactDefinitionFunction;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.icon.ActionIcon;

/**
 *
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class ElementPropertyFactory {
        
    public static Component propertiesFromElementUi(ElementUi element, Command command, TranslationService ts, ElementPropertyEditorDialog elementPropertyEditorDialog) {
        VerticalLayout lytContent = new VerticalLayout();
        lytContent.setSpacing(false);
        lytContent.setPadding(false);
        lytContent.setMargin(false);
        lytContent.setWidthFull();
        
        List<String> listUnit = new ArrayList<>();
        listUnit.add("px");
        listUnit.add("%");
        
        List<String> listAlignment = new ArrayList<>();
        listAlignment.add("left");
        listAlignment.add("center");
        listAlignment.add("right");
        
        // --> init id
        HorizontalLayout lytId = new HorizontalLayout();
        lytId.setWidthFull();
        
        BoldLabel lblId = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-id"));
        lblId.getStyle().set("margin-top", "10px");
        lblId.setWidth("20%");
        
        TextField txtId = new TextField();
        txtId.setWidth("80%");
        
        txtId.setValue(element.getElementUiId() == null || element.getElementUiId().isEmpty()
                ? "" : element.getElementUiId());
        
        txtId.addKeyPressListener(Key.ENTER, listener -> {
            if (!txtId.getValue().equals(element.getElementUiId())) {
                element.setElementUiId(txtId.getValue());
                command.execute();
            }
        });
        
        lytId.add(lblId, txtId);
        // end width <--
        lytContent.add(lytId);
        
        if (element instanceof ButtonElementUi) {
            // --> init width
            HorizontalLayout lytWidth = new HorizontalLayout();
            lytWidth.setWidthFull();
            
            BoldLabel lblWidth = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-width"));
            lblWidth.getStyle().set("margin-top", "10px");
            lblWidth.setWidth("20%");
            
            NumberField nmbWidth = new NumberField();
            nmbWidth.setMin(30);
            nmbWidth.setStep(1);
            nmbWidth.setWidth("55%");
            nmbWidth.setHasControls(true);
            
            ComboBox cmbWidth = new ComboBox<String>();
            cmbWidth.setItems(listUnit);
            cmbWidth.setWidth("20%");
            
            if (((ButtonElementUi) element).getElementUiWidth() != null && !((ButtonElementUi) element).getElementUiWidth().isEmpty())
                setElementUiValue(((ButtonElementUi) element).getElementUiWidth(), nmbWidth, cmbWidth);
            
            nmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((ButtonElementUi) element).setElementUiWidth(value);
                ((ButtonElementUi) element).setWidth(value);
                command.execute();
            });
            
            cmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((ButtonElementUi) element).setElementUiWidth(value);
                ((ButtonElementUi) element).setWidth(value);
                command.execute();
            });
            
            lytWidth.add(lblWidth, nmbWidth, cmbWidth);
            // end width <--
            
            // --> init height
            HorizontalLayout lytHeight = new HorizontalLayout();
            lytHeight.setWidthFull();
           
            BoldLabel lblHeight = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-height"));
            lblHeight.getStyle().set("margin-top", "10px");
            lblHeight.setWidth("20%");
            
            NumberField nmbHeight = new NumberField();
            nmbHeight.setMin(30);
            nmbHeight.setStep(1);
            nmbHeight.setWidth("55%");
            nmbHeight.setHasControls(true);
            
            ComboBox cmbHeight = new ComboBox<String>();
            cmbHeight.setItems(listUnit);
            cmbHeight.setWidth("20%");
            
            if (((ButtonElementUi) element).getElementUiHeight() != null && !((ButtonElementUi) element).getElementUiHeight().isEmpty())
                setElementUiValue(((ButtonElementUi) element).getElementUiHeight(), nmbHeight, cmbHeight);
            
            nmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((ButtonElementUi) element).setElementUiHeight(value);
                ((ButtonElementUi) element).setHeight(value);
                command.execute();
            });
            
            cmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((ButtonElementUi) element).setElementUiHeight(value);
                ((ButtonElementUi) element).setHeight(value);
                command.execute();
            });
            
            lytHeight.add(lblHeight, nmbHeight, cmbHeight);
            // end height <--
            
            // --> init caption
            HorizontalLayout lytCaption = new HorizontalLayout();
            lytCaption.setWidthFull();
            
            BoldLabel lblCaption = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-caption"));
            lblCaption.getStyle().set("margin-top", "10px");
            lblCaption.setWidth("20%");
            
            TextField txtCaption = new TextField();
            txtCaption.setWidth("80%");
            
            txtCaption.setValue(((ButtonElementUi) element).getElementUiCaption() == null || ((ButtonElementUi) element).getElementUiCaption().isEmpty()
                    ? "" : ((ButtonElementUi) element).getElementUiCaption());
            
            txtCaption.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtCaption.getValue().equals(((ButtonElementUi) element).getElementUiCaption())) {
                    ((ButtonElementUi) element).setElementUiCaption(txtCaption.getValue());
                    ((ButtonElementUi) element).setText(txtCaption.getValue());
                    command.execute();
                }
            });
            
            lytCaption.add(lblCaption, txtCaption);
            // end caption <--
           
            // --> init stylename
            HorizontalLayout lytStyleName = new HorizontalLayout();
            lytStyleName.setWidthFull();
            
            BoldLabel lblStyleName = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-stylename"));
            lblStyleName.getStyle().set("margin-top", "10px");
            lblStyleName.setWidth("22%");
            
            TextField txtStyleName = new TextField();
            txtStyleName.setWidth("78%");
            
            txtStyleName.setValue(((ButtonElementUi) element).getElementUiStyleName() == null || ((ButtonElementUi) element).getElementUiStyleName().isEmpty()
                    ? "" : ((ButtonElementUi) element).getElementUiStyleName());
            
            txtStyleName.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtStyleName.getValue().equals(((ButtonElementUi) element).getElementUiStyleName())) {
                    ((ButtonElementUi) element).setElementUiStyleName(txtStyleName.getValue());
                    command.execute();
                }
            });
            
            lytStyleName.add(lblStyleName, txtStyleName);
            // end stylename <--
            
            lytContent.add(lytWidth, lytHeight, lytCaption, lytStyleName);
        } else if (element instanceof CheckBoxElementUi) {
            // --> init width
            HorizontalLayout lytWidth = new HorizontalLayout();
            lytWidth.setWidthFull();
            
            BoldLabel lblWidth = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-width"));
            lblWidth.getStyle().set("margin-top", "10px");
            lblWidth.setWidth("20%");
            
            NumberField nmbWidth = new NumberField();
            nmbWidth.setMin(30);
            nmbWidth.setStep(1);
            nmbWidth.setWidth("55%");
            nmbWidth.setHasControls(true);
            
            ComboBox cmbWidth = new ComboBox<String>();
            cmbWidth.setItems(listUnit);
            cmbWidth.setWidth("20%");
            
            if (((CheckBoxElementUi) element).getElementUiWidth() != null && !((CheckBoxElementUi) element).getElementUiWidth().isEmpty())
                setElementUiValue(((CheckBoxElementUi) element).getElementUiWidth(), nmbWidth, cmbWidth);
            
            nmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((CheckBoxElementUi) element).setElementUiWidth(value);
                ((CheckBoxElementUi) element).setWidth(value);
                command.execute();
            });
            
            cmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((CheckBoxElementUi) element).setElementUiWidth(value);
                ((CheckBoxElementUi) element).setWidth(value);
                command.execute();
            });
            
            lytWidth.add(lblWidth, nmbWidth, cmbWidth);
            // end width <--
            
            // --> init height
            HorizontalLayout lytHeight = new HorizontalLayout();
            lytHeight.setWidthFull();
           
            BoldLabel lblHeight = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-height"));
            lblHeight.getStyle().set("margin-top", "10px");
            lblHeight.setWidth("20%");
            
            NumberField nmbHeight = new NumberField();
            nmbHeight.setMin(30);
            nmbHeight.setStep(1);
            nmbHeight.setWidth("55%");
            nmbHeight.setHasControls(true);
            
            ComboBox cmbHeight = new ComboBox<String>();
            cmbHeight.setItems(listUnit);
            cmbHeight.setWidth("20%");
            
            if (((CheckBoxElementUi) element).getElementUiHeight() != null && !((CheckBoxElementUi) element).getElementUiHeight().isEmpty())
                setElementUiValue(((CheckBoxElementUi) element).getElementUiHeight(), nmbHeight, cmbHeight);
            
            nmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((CheckBoxElementUi) element).setElementUiHeight(value);
                ((CheckBoxElementUi) element).setHeight(value);
                command.execute();
            });
            
            cmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((CheckBoxElementUi) element).setElementUiHeight(value);
                ((CheckBoxElementUi) element).setHeight(value);
                command.execute();
            });
            
            lytHeight.add(lblHeight, nmbHeight, cmbHeight);
            // end height <--
            
             // --> init datatype
            HorizontalLayout lytDataType = new HorizontalLayout();
            lytDataType.setWidthFull();
            
            BoldLabel lblDataType = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-datatype"));
            lblDataType.getStyle().set("margin-top", "10px");
            lblDataType.setWidth("20%");
            
            TextField txtDataType = new TextField();
            txtDataType.setWidth("80%");
            
            txtDataType.setValue(((CheckBoxElementUi) element).getElementUiDataType() == null || ((CheckBoxElementUi) element).getElementUiDataType().isEmpty()
                    ? "" : ((CheckBoxElementUi) element).getElementUiDataType());
            
            txtDataType.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtDataType.getValue().equals(((CheckBoxElementUi) element).getElementUiDataType())) {
                    ((CheckBoxElementUi) element).setElementUiDataType(txtDataType.getValue());
                    command.execute();
                }
            });
            
            lytDataType.add(lblDataType, txtDataType);
            // end datatype <--
            
            lytContent.add(lytWidth, lytHeight, lytDataType);
        } else if (element instanceof ComboBoxElementUi) {
            // --> init width
            HorizontalLayout lytWidth = new HorizontalLayout();
            lytWidth.setWidthFull();
            
            BoldLabel lblWidth = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-width"));
            lblWidth.getStyle().set("margin-top", "10px");
            lblWidth.setWidth("20%");
            
            NumberField nmbWidth = new NumberField();
            nmbWidth.setMin(30);
            nmbWidth.setStep(1);
            nmbWidth.setWidth("55%");
            nmbWidth.setHasControls(true);
            
            ComboBox cmbWidth = new ComboBox<String>();
            cmbWidth.setItems(listUnit);
            cmbWidth.setWidth("20%");
            
            if (((ComboBoxElementUi) element).getElementUiWidth() != null && !((ComboBoxElementUi) element).getElementUiWidth().isEmpty())
                setElementUiValue(((ComboBoxElementUi) element).getElementUiWidth(), nmbWidth, cmbWidth);
            
            nmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((ComboBoxElementUi) element).setElementUiWidth(value);
                ((ComboBoxElementUi) element).setWidth(value);
                command.execute();
            });
            
            cmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((ComboBoxElementUi) element).setElementUiWidth(value);
                ((ComboBoxElementUi) element).setWidth(value);
                command.execute();
            });
            
            lytWidth.add(lblWidth, nmbWidth, cmbWidth);
            // end width <--
            
            // --> init height
            HorizontalLayout lytHeight = new HorizontalLayout();
            lytHeight.setWidthFull();
           
            BoldLabel lblHeight = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-height"));
            lblHeight.getStyle().set("margin-top", "10px");
            lblHeight.setWidth("20%");
            
            NumberField nmbHeight = new NumberField();
            nmbHeight.setMin(30);
            nmbHeight.setStep(1);
            nmbHeight.setWidth("55%");
            nmbHeight.setHasControls(true);
            
            ComboBox cmbHeight = new ComboBox<String>();
            cmbHeight.setItems(listUnit);
            cmbHeight.setWidth("20%");
            
            if (((ComboBoxElementUi) element).getElementUiHeight() != null && !((ComboBoxElementUi) element).getElementUiHeight().isEmpty())
                setElementUiValue(((ComboBoxElementUi) element).getElementUiHeight(), nmbHeight, cmbHeight);
            
            nmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((ComboBoxElementUi) element).setElementUiHeight(value);
                ((ComboBoxElementUi) element).setHeight(value);
                command.execute();
            });
            
            cmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((ComboBoxElementUi) element).setElementUiHeight(value);
                ((ComboBoxElementUi) element).setHeight(value);
                command.execute();
            });
            
            lytHeight.add(lblHeight, nmbHeight, cmbHeight);
            // end height <--

            // --> init value
            HorizontalLayout lytValue = new HorizontalLayout();
            lytValue.setWidthFull();
            
            BoldLabel lblValue = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-value"));
            lblValue.getStyle().set("margin-top", "10px");
            lblValue.setWidth("20%");
            
            TextField txtValue = new TextField();
            txtValue.setWidth("80%");
            
            txtValue.setValue(((ComboBoxElementUi) element).getElementUiValue() == null || ((ComboBoxElementUi) element).getElementUiValue().isEmpty()
                    ? "" : ((ComboBoxElementUi) element).getElementUiValue());
            
            txtValue.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtValue.getValue().equals(((ComboBoxElementUi) element).getElementUiValue())) {
                    ((ComboBoxElementUi) element).setElementUiValue(txtValue.getValue());
                    ((ComboBoxElementUi) element).setValue(txtValue.getValue());
                    command.execute();
                }
            });
            
            lytValue.add(lblValue, txtValue);
            // end value <--
            
            // --> init items
            HorizontalLayout lytItems = new HorizontalLayout();
            lytItems.setWidthFull();
            
            BoldLabel lblItems = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-items"));
            lblItems.getStyle().set("margin-top", "10px");
            lblItems.setWidth("20%");
            
            TextField txtItems = new TextField();
            txtItems.setWidth("80%");
            
            txtItems.setValue(((ComboBoxElementUi) element).getElementUiItems() == null || ((ComboBoxElementUi) element).getElementUiItems().isEmpty()
                    ? "" : ((ComboBoxElementUi) element).getElementUiItems());
            
            txtItems.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtItems.getValue().equals(((ComboBoxElementUi) element).getElementUiItems())) {
                    ((ComboBoxElementUi) element).setElementUiItems(txtItems.getValue());
                    command.execute();
                }
            });
            
            lytItems.add(lblItems, txtItems);
            // end items <-- 
            
            // --> init shared
            HorizontalLayout lytShared = new HorizontalLayout();
            lytShared.setWidthFull();
            
            BoldLabel lblShared = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-shared"));
            lblShared.setWidth("20%");
            
            Checkbox chxShared = new Checkbox();
            chxShared.setValue(((ComboBoxElementUi) element).getElementUiShared() == null || ((ComboBoxElementUi) element).getElementUiShared().isEmpty()
                    ? false : Boolean.valueOf(((ComboBoxElementUi) element).getElementUiShared()));
                        
            chxShared.addValueChangeListener(listener -> {
                ((ComboBoxElementUi) element).setElementUiShared(String.valueOf(listener.getValue()));
                command.execute();
            });
            
            lytShared.add(lblShared, chxShared);
            // end shared <--
            
            // --> init propertyChangeListener
            HorizontalLayout lytPropertyChangeListener = new HorizontalLayout();
            lytPropertyChangeListener.setWidthFull();
            
            BoldLabel lblPropertyChangeListener = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-propertychangeListener"));
            lblPropertyChangeListener.getStyle().set("margin-top", "10px");
            lblPropertyChangeListener.setWidth("50%");
            
            TextField txtPropertyChangeListener = new TextField();
            txtPropertyChangeListener.setWidth("50%");
            
            txtPropertyChangeListener.setValue(((ComboBoxElementUi) element).getElementUiPropertyChangeListener() == null || ((ComboBoxElementUi) element).getElementUiPropertyChangeListener().isEmpty()
                    ? "" : ((ComboBoxElementUi) element).getElementUiPropertyChangeListener());
            
            txtPropertyChangeListener.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtPropertyChangeListener.getValue().equals(((ComboBoxElementUi) element).getElementUiPropertyChangeListener())) {
                    ((ComboBoxElementUi) element).setElementUiPropertyChangeListener(txtPropertyChangeListener.getValue());
                    command.execute();
                }
            });
            
            lytPropertyChangeListener.add(lblPropertyChangeListener, txtPropertyChangeListener);
            // end propertyChangeListener <--
            
            // --> init datatype
            HorizontalLayout lytDataType = new HorizontalLayout();
            lytDataType.setWidthFull();
            
            BoldLabel lblDataType = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-datatype"));
            lblDataType.getStyle().set("margin-top", "10px");
            lblDataType.setWidth("20%");
            
            TextField txtDataType = new TextField();
            txtDataType.setWidth("80%");
            
            txtDataType.setValue(((ComboBoxElementUi) element).getElementUiDataType() == null || ((ComboBoxElementUi) element).getElementUiDataType().isEmpty()
                    ? "" : ((ComboBoxElementUi) element).getElementUiDataType());
            
            txtDataType.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtDataType.getValue().equals(((ComboBoxElementUi) element).getElementUiDataType())) {
                    ((ComboBoxElementUi) element).setElementUiDataType(txtDataType.getValue());
                    command.execute();
                }
            });
            
            lytDataType.add(lblDataType, txtDataType);
            // end datatype <--
            
            lytContent.add(lytWidth, lytHeight, lytValue, lytItems, lytDataType, lytPropertyChangeListener, lytShared);
        } else if (element instanceof GridElementUi) {
            // --> init width
            HorizontalLayout lytWidth = new HorizontalLayout();
            lytWidth.setWidthFull();
            
            BoldLabel lblWidth = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-width"));
            lblWidth.getStyle().set("margin-top", "10px");
            lblWidth.setWidth("20%");
            
            NumberField nmbWidth = new NumberField();
            nmbWidth.setMin(30);
            nmbWidth.setStep(1);
            nmbWidth.setWidth("55%");
            nmbWidth.setHasControls(true);
            
            ComboBox cmbWidth = new ComboBox<String>();
            cmbWidth.setItems(listUnit);
            cmbWidth.setWidth("20%");
            
            if (((GridElementUi) element).getElementUiWidth() != null && !((GridElementUi) element).getElementUiWidth().isEmpty())
                setElementUiValue(((GridElementUi) element).getElementUiWidth(), nmbWidth, cmbWidth);
            
            nmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((GridElementUi) element).setElementUiWidth(value);
                ((GridElementUi) element).setWidth(value);
                command.execute();
            });
            
            cmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((GridElementUi) element).setElementUiWidth(value);
                ((GridElementUi) element).setWidth(value);
                command.execute();
            });
            
            lytWidth.add(lblWidth, nmbWidth, cmbWidth);
            // end width <--
            
            // --> init height
            HorizontalLayout lytHeight = new HorizontalLayout();
            lytHeight.setWidthFull();
           
            BoldLabel lblHeight = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-height"));
            lblHeight.getStyle().set("margin-top", "10px");
            lblHeight.setWidth("20%");
            
            NumberField nmbHeight = new NumberField();
            nmbHeight.setMin(30);
            nmbHeight.setStep(1);
            nmbHeight.setWidth("55%");
            nmbHeight.setHasControls(true);
            
            ComboBox cmbHeight = new ComboBox<String>();
            cmbHeight.setItems(listUnit);
            cmbHeight.setWidth("20%");
            
            if (((GridElementUi) element).getElementUiHeight() != null && !((GridElementUi) element).getElementUiHeight().isEmpty())
                setElementUiValue(((GridElementUi) element).getElementUiHeight(), nmbHeight, cmbHeight);
            
            nmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((GridElementUi) element).setElementUiHeight(value);
                ((GridElementUi) element).setHeight(value);
                command.execute();
            });
            
            cmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((GridElementUi) element).setElementUiHeight(value);
                ((GridElementUi) element).setHeight(value);
                command.execute();
            });
            
            lytHeight.add(lblHeight, nmbHeight, cmbHeight);
            // end height <--
            
            // --> init shared
            HorizontalLayout lytShared = new HorizontalLayout();
            lytShared.setWidthFull();
            
            BoldLabel lblShared = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-shared"));
            lblShared.setWidth("20%");
            
            Checkbox chxShared = new Checkbox();
            chxShared.setValue(((GridElementUi) element).getElementUiShared() == null || ((GridElementUi) element).getElementUiShared().isEmpty()
                    ? false : Boolean.valueOf(((GridElementUi) element).getElementUiShared()));
                        
            chxShared.addValueChangeListener(listener -> {
                ((GridElementUi) element).setElementUiShared(String.valueOf(listener.getValue()));
                command.execute();
            });
            
            lytShared.add(lblShared, chxShared);
            // end shared <--
            
            // --> init columns
            HorizontalLayout lytColumns = new HorizontalLayout();
            lytColumns.setWidthFull();
            
            BoldLabel lblColumns = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-columns"));
            lblColumns.getStyle().set("margin-top", "10px");
            lblColumns.setWidth("20%");
            
            NumberField nmbColumns = new NumberField();
            nmbColumns.setHasControls(true);
            nmbColumns.setWidth("80%");
            nmbColumns.setMin(0);
            nmbColumns.setStep(1);
            
            nmbColumns.setValue(((GridElementUi) element).getElementUiColumns() == null || ((GridElementUi) element).getElementUiColumns().isEmpty()
            ? 0 : Double.parseDouble(((GridElementUi) element).getElementUiColumns()));
            
            nmbColumns.addValueChangeListener(listener -> {
                ((GridElementUi) element).setElementUiColumns(String.valueOf(Math.round(listener.getValue())));
                command.execute();
            });
            
            lytColumns.add(lblColumns, nmbColumns);
            // end columns <--
            
            // --> init rows
            HorizontalLayout lytRows = new HorizontalLayout();
            lytRows.setWidthFull();
            
            BoldLabel lblRows = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-rows"));
            lblRows.getStyle().set("margin-top", "10px");
            lblRows.setWidth("20%");
            
            NumberField nmbRows = new NumberField();
            nmbRows.setHasControls(true);
            nmbRows.setWidth("80%");
            nmbRows.setMin(0);
            nmbRows.setStep(1);
            
            nmbRows.setValue(((GridElementUi) element).getElementUiRows() == null || ((GridElementUi) element).getElementUiRows().isEmpty()
            ? 0 : Double.parseDouble(((GridElementUi) element).getElementUiRows()));
            
            nmbRows.addValueChangeListener(listener -> {
                ((GridElementUi) element).setElementUiRows(String.valueOf(Math.round(listener.getValue())));
                command.execute();
            });
            
            lytRows.add(lblRows, nmbRows);
            // end rows <--
            
            // --> init alignment
            HorizontalLayout lytAlignment = new HorizontalLayout();
            lytAlignment.setWidthFull();
            
            BoldLabel lblAlignment = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-alignment"));
            lblAlignment.getStyle().set("margin-top", "10px");
            lblAlignment.setWidth("20%");
            
            ComboBox<String> cmbAlignment = new ComboBox();
            cmbAlignment.setAllowCustomValue(false);
            cmbAlignment.setItems(listAlignment);
            cmbAlignment.setWidth("80%");
            
            cmbAlignment.setValue(((GridElementUi) element).getElementUiAlignment() == null || ((GridElementUi) element).getElementUiAlignment().isEmpty()
                    ? "" : ((GridElementUi) element).getElementUiAlignment());
            
            cmbAlignment.addValueChangeListener(listener -> {
                if (!cmbAlignment.getValue().equals(((GridElementUi) element).getElementUiAlignment())) {
                    ((GridElementUi) element).setElementUiAlignment(listener.getValue());
                    command.execute();
                }
            });
            
            lytAlignment.add(lblAlignment, cmbAlignment);
            // end alignment <--
            
            // --> init save
            HorizontalLayout lytSave = new HorizontalLayout();
            lytSave.setWidthFull();
            
            BoldLabel lblSave = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-save"));
            lblSave.setWidth("20%");
            
            Checkbox chxSave = new Checkbox();
            chxSave.setValue(((GridElementUi) element).getElementUiSave() == null || ((GridElementUi) element).getElementUiSave().isEmpty()
                    ? false : Boolean.valueOf(((GridElementUi) element).getElementUiSave()));
                        
            chxSave.addValueChangeListener(listener -> {
                ((GridElementUi) element).setElementUiSave(String.valueOf(listener.getValue()));
                command.execute();
            });
            
            lytSave.add(lblSave, chxSave);
            // end save <--
            
            // --> init datatype
            HorizontalLayout lytDataType = new HorizontalLayout();
            lytDataType.setWidthFull();
            
            BoldLabel lblDataType = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-datatype"));
            lblDataType.getStyle().set("margin-top", "10px");
            lblDataType.setWidth("20%");
            
            TextField txtDataType = new TextField();
            txtDataType.setWidth("80%");
            
            txtDataType.setValue(((GridElementUi) element).getElementUiDataType() == null || ((GridElementUi) element).getElementUiDataType().isEmpty()
                    ? "" : ((GridElementUi) element).getElementUiDataType());
            
            txtDataType.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtDataType.getValue().equals(((GridElementUi) element).getElementUiDataType())) {
                    ((GridElementUi) element).setElementUiDataType(txtDataType.getValue());
                    command.execute();
                }
            });
            
            lytDataType.add(lblDataType, txtDataType);
            // end datatype <--
            
            lytContent.add(lytWidth, lytHeight, lytColumns, lytRows, lytAlignment, lytDataType, lytShared, lytSave);
        } else if (element instanceof LabelElementUi) {
            // --> init width
            HorizontalLayout lytWidth = new HorizontalLayout();
            lytWidth.setWidthFull();
            
            BoldLabel lblWidth = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-width"));
            lblWidth.getStyle().set("margin-top", "10px");
            lblWidth.setWidth("20%");
            
            NumberField nmbWidth = new NumberField();
            nmbWidth.setMin(30);
            nmbWidth.setStep(1);
            nmbWidth.setWidth("55%");
            nmbWidth.setHasControls(true);
            
            ComboBox cmbWidth = new ComboBox<String>();
            cmbWidth.setItems(listUnit);
            cmbWidth.setWidth("20%");
            
            if (((LabelElementUi) element).getElementUiWidth() != null && !((LabelElementUi) element).getElementUiWidth().isEmpty())
                setElementUiValue(((LabelElementUi) element).getElementUiWidth(), nmbWidth, cmbWidth);
            
            nmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((LabelElementUi) element).setElementUiWidth(value);
                ((LabelElementUi) element).setWidth(value);
                command.execute();
            });
            
            cmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((LabelElementUi) element).setElementUiWidth(value);
                ((LabelElementUi) element).setWidth(value);
                command.execute();
            });
            
            lytWidth.add(lblWidth, nmbWidth, cmbWidth);
            // end width <--
            
            // --> init height
            HorizontalLayout lytHeight = new HorizontalLayout();
            lytHeight.setWidthFull();
           
            BoldLabel lblHeight = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-height"));
            lblHeight.getStyle().set("margin-top", "10px");
            lblHeight.setWidth("20%");
            
            NumberField nmbHeight = new NumberField();
            nmbHeight.setMin(30);
            nmbHeight.setStep(1);
            nmbHeight.setWidth("55%");
            nmbHeight.setHasControls(true);
            
            ComboBox cmbHeight = new ComboBox<String>();
            cmbHeight.setItems(listUnit);
            cmbHeight.setWidth("20%");
            
            if (((LabelElementUi) element).getElementUiHeight() != null && !((LabelElementUi) element).getElementUiHeight().isEmpty())
                setElementUiValue(((LabelElementUi) element).getElementUiHeight(), nmbHeight, cmbHeight);
            
            nmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((LabelElementUi) element).setElementUiHeight(value);
                ((LabelElementUi) element).setHeight(value);
                command.execute();
            });
            
            cmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((LabelElementUi) element).setElementUiHeight(value);
                ((LabelElementUi) element).setHeight(value);
                command.execute();
            });
            
            lytHeight.add(lblHeight, nmbHeight, cmbHeight);
            // end height <--
            
            // --> init value
            HorizontalLayout lytValue = new HorizontalLayout();
            lytValue.setWidthFull();
            
            BoldLabel lblValue = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-value"));
            lblValue.getStyle().set("margin-top", "10px");
            lblValue.setWidth("20%");
            
            TextField txtValue = new TextField();
            txtValue.setWidth("80%");
            
            txtValue.setValue(((LabelElementUi) element).getElementUiValue() == null || ((LabelElementUi) element).getElementUiValue().isEmpty()
                    ? ts.getTranslatedString("module.processeditor.editor-form-control-label-label") : ((LabelElementUi) element).getElementUiValue());
            
            txtValue.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtValue.getValue().equals(((LabelElementUi) element).getElementUiValue())) {
                    ((LabelElementUi) element).setElementUiValue(txtValue.getValue());
                    ((LabelElementUi) element).setText(txtValue.getValue());
                    command.execute();
                }
            });
            
            lytValue.add(lblValue, txtValue);
            // end value <--
            
            // --> init stylename
            HorizontalLayout lytStyleName = new HorizontalLayout();
            lytStyleName.setWidthFull();
            
            BoldLabel lblStyleName = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-stylename"));
            lblStyleName.getStyle().set("margin-top", "10px");
            lblStyleName.setWidth("22%");
            
            TextField txtStyleName = new TextField();
            txtStyleName.setWidth("78%");
            
            txtStyleName.setValue(((LabelElementUi) element).getElementUiStyleName() == null || ((LabelElementUi) element).getElementUiStyleName().isEmpty()
                    ? "" : ((LabelElementUi) element).getElementUiStyleName());
            
            txtStyleName.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtStyleName.getValue().equals(((LabelElementUi) element).getElementUiStyleName())) {
                    ((LabelElementUi) element).setElementUiStyleName(txtStyleName.getValue());
                    command.execute();
                }
            });
            
            lytStyleName.add(lblStyleName, txtStyleName);
            // end stylename <--
            
            // --> init alignment
            HorizontalLayout lytAlignment = new HorizontalLayout();
            lytAlignment.setWidthFull();
            
            BoldLabel lblAlignment = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-alignment"));
            lblAlignment.getStyle().set("margin-top", "10px");
            lblAlignment.setWidth("20%");
            
            ComboBox<String> cmbAlignment = new ComboBox();
            cmbAlignment.setAllowCustomValue(false);
            cmbAlignment.setItems(listAlignment);
            cmbAlignment.setWidth("80%");
            
            cmbAlignment.setValue(((LabelElementUi) element).getElementUiAlignment() == null || ((LabelElementUi) element).getElementUiAlignment().isEmpty()
                    ? "" : ((LabelElementUi) element).getElementUiAlignment());
            
            cmbAlignment.addValueChangeListener(listener -> {
                if (!cmbAlignment.getValue().equals(((LabelElementUi) element).getElementUiAlignment())) {
                    ((LabelElementUi) element).setElementUiAlignment(listener.getValue());
                    command.execute();
                }
            });
            
            lytAlignment.add(lblAlignment, cmbAlignment);
            // end alignment <--
            
            lytContent.add(lytWidth, lytHeight, lytValue, lytAlignment, lytStyleName);
        } else if (element instanceof TextFieldElementUi) {
            // --> init width
            HorizontalLayout lytWidth = new HorizontalLayout();
            lytWidth.setWidthFull();
            
            BoldLabel lblWidth = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-width"));
            lblWidth.getStyle().set("margin-top", "10px");
            lblWidth.setWidth("20%");
            
            NumberField nmbWidth = new NumberField();
            nmbWidth.setMin(30);
            nmbWidth.setStep(1);
            nmbWidth.setWidth("55%");
            nmbWidth.setHasControls(true);
            
            ComboBox cmbWidth = new ComboBox<String>();
            cmbWidth.setItems(listUnit);
            cmbWidth.setWidth("20%");
            
            if (((TextFieldElementUi) element).getElementUiWidth() != null && !((TextFieldElementUi) element).getElementUiWidth().isEmpty())
                setElementUiValue(((TextFieldElementUi) element).getElementUiWidth(), nmbWidth, cmbWidth);
            
            nmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((TextFieldElementUi) element).setElementUiWidth(value);
                ((TextFieldElementUi) element).setWidth(value);
                command.execute();
            });
            
            cmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((TextFieldElementUi) element).setElementUiWidth(value);
                ((TextFieldElementUi) element).setWidth(value);
                command.execute();
            });
            
            lytWidth.add(lblWidth, nmbWidth, cmbWidth);
            // end width <--
            
            // --> init height
            HorizontalLayout lytHeight = new HorizontalLayout();
            lytHeight.setWidthFull();
           
            BoldLabel lblHeight = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-height"));
            lblHeight.getStyle().set("margin-top", "10px");
            lblHeight.setWidth("20%");
            
            NumberField nmbHeight = new NumberField();
            nmbHeight.setMin(30);
            nmbHeight.setStep(1);
            nmbHeight.setWidth("55%");
            nmbHeight.setHasControls(true);
            
            ComboBox cmbHeight = new ComboBox<String>();
            cmbHeight.setItems(listUnit);
            cmbHeight.setWidth("20%");
            
            if (((TextFieldElementUi) element).getElementUiHeight() != null && !((TextFieldElementUi) element).getElementUiHeight().isEmpty())
                setElementUiValue(((TextFieldElementUi) element).getElementUiHeight(), nmbHeight, cmbHeight);
            
            nmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((TextFieldElementUi) element).setElementUiHeight(value);
                ((TextFieldElementUi) element).setHeight(value);
                command.execute();
            });
            
            cmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((TextFieldElementUi) element).setElementUiHeight(value);
                ((TextFieldElementUi) element).setHeight(value);
                command.execute();
            });
            
            lytHeight.add(lblHeight, nmbHeight, cmbHeight);
            // end height <--
            
            // --> init value
            HorizontalLayout lytValue = new HorizontalLayout();
            lytValue.setWidthFull();
            
            BoldLabel lblValue = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-value"));
            lblValue.getStyle().set("margin-top", "10px");
            lblValue.setWidth("20%");
            
            TextField txtValue = new TextField();
            txtValue.setWidth("80%");
            
            txtValue.setValue(((TextFieldElementUi) element).getElementUiValue() == null || ((TextFieldElementUi) element).getElementUiValue().isEmpty()
                    ? "" : ((TextFieldElementUi) element).getElementUiValue());
            
            txtValue.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtValue.getValue().equals(((TextFieldElementUi) element).getElementUiValue())) {
                    ((TextFieldElementUi) element).setElementUiValue(txtValue.getValue());
                    ((TextFieldElementUi) element).setValue(txtValue.getValue());
                    command.execute();
                }
            });
            
            lytValue.add(lblValue, txtValue);
            // end value <--
            
            // --> init enabled
            HorizontalLayout lytEnabled = new HorizontalLayout();
            lytEnabled.setWidthFull();
            
            BoldLabel lblEnabled = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-enable"));
            lblEnabled.setWidth("20%");
            
            Checkbox chxEnabled = new Checkbox();
            chxEnabled.setValue(((TextFieldElementUi) element).getElementUiEnabled() == null || ((TextFieldElementUi) element).getElementUiEnabled().isEmpty()
                    ? false : Boolean.valueOf(((TextFieldElementUi) element).getElementUiEnabled()));
                        
            chxEnabled.addValueChangeListener(listener -> {
                ((TextFieldElementUi) element).setElementUiEnabled(String.valueOf(listener.getValue()));
                command.execute();
            });
            
            lytEnabled.add(lblEnabled, chxEnabled);
            // end enabled <--
            
            // --> init datatype
            HorizontalLayout lytDataType = new HorizontalLayout();
            lytDataType.setWidthFull();
            
            BoldLabel lblDataType = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-datatype"));
            lblDataType.getStyle().set("margin-top", "10px");
            lblDataType.setWidth("20%");
            
            TextField txtDataType = new TextField();
            txtDataType.setWidth("80%");
            
            txtDataType.setValue(((TextFieldElementUi) element).getElementUiDataType() == null || ((TextFieldElementUi) element).getElementUiDataType().isEmpty()
                    ? "" : ((TextFieldElementUi) element).getElementUiDataType());
            
            txtDataType.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtDataType.getValue().equals(((TextFieldElementUi) element).getElementUiDataType())) {
                    ((TextFieldElementUi) element).setElementUiDataType(txtDataType.getValue());
                    command.execute();
                }
            });
            
            lytDataType.add(lblDataType, txtDataType);
            // end datatype <--
            
            // --> init hidden
            HorizontalLayout lytHidden = new HorizontalLayout();
            lytHidden.setWidthFull();
            
            BoldLabel lblHidden = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-hidden"));
            lblHidden.setWidth("20%");
            
            Checkbox chxHidden = new Checkbox();
            chxHidden.setValue(((TextFieldElementUi) element).getElementUiHidden() == null || ((TextFieldElementUi) element).getElementUiHidden().isEmpty()
                    ? false : Boolean.valueOf(((TextFieldElementUi) element).getElementUiHidden()));
                        
            chxHidden.addValueChangeListener(listener -> {
                ((TextFieldElementUi) element).setElementUiHidden(String.valueOf(listener.getValue()));
                command.execute();
            });
            
            lytHidden.add(lblHidden, chxHidden);
            // end hidden <--
            
            // --> init mandatory
            HorizontalLayout lytMandatory = new HorizontalLayout();
            lytMandatory.setWidthFull();
            
            BoldLabel lblMandatory = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-mandatory"));
            lblMandatory.setWidth("20%");
            
            Checkbox chxMandatory = new Checkbox();
            chxMandatory.setValue(((TextFieldElementUi) element).getElementUiMandatory() == null || ((TextFieldElementUi) element).getElementUiMandatory().isEmpty()
                    ? false : Boolean.valueOf(((TextFieldElementUi) element).getElementUiMandatory()));
                        
            chxMandatory.addValueChangeListener(listener -> {
                ((TextFieldElementUi) element).setElementUiMandatory(String.valueOf(listener.getValue()));
                command.execute();
            });
            
            lytMandatory.add(lblMandatory, chxMandatory);
            // end mandatory <--
            
            lytContent.add(lytWidth, lytHeight, lytValue, lytDataType, lytEnabled, lytHidden, lytMandatory);
        } else if (element instanceof HorizontalLayoutElementUi) {
            // --> init width
            HorizontalLayout lytWidth = new HorizontalLayout();
            lytWidth.setWidthFull();
            
            BoldLabel lblWidth = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-width"));
            lblWidth.getStyle().set("margin-top", "10px");
            lblWidth.setWidth("20%");
            
            NumberField nmbWidth = new NumberField();
            nmbWidth.setMin(30);
            nmbWidth.setStep(1);
            nmbWidth.setWidth("55%");
            nmbWidth.setHasControls(true);
            
            ComboBox cmbWidth = new ComboBox<String>();
            cmbWidth.setItems(listUnit);
            cmbWidth.setWidth("20%");
            
            if (((HorizontalLayoutElementUi) element).getElementUiWidth() != null && !((HorizontalLayoutElementUi) element).getElementUiWidth().isEmpty())
                setElementUiValue(((HorizontalLayoutElementUi) element).getElementUiWidth(), nmbWidth, cmbWidth);
            
            nmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((HorizontalLayoutElementUi) element).setElementUiWidth(value);
                ((HorizontalLayoutElementUi) element).setWidth(value);
                command.execute();
            });
            
            cmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((HorizontalLayoutElementUi) element).setElementUiWidth(value);
                ((HorizontalLayoutElementUi) element).setWidth(value);
                command.execute();
            });
            
            lytWidth.add(lblWidth, nmbWidth, cmbWidth);
            // end width <--
            
            // --> init height
            HorizontalLayout lytHeight = new HorizontalLayout();
            lytHeight.setWidthFull();
           
            BoldLabel lblHeight = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-height"));
            lblHeight.getStyle().set("margin-top", "10px");
            lblHeight.setWidth("20%");
            
            NumberField nmbHeight = new NumberField();
            nmbHeight.setMin(30);
            nmbHeight.setStep(1);
            nmbHeight.setWidth("55%");
            nmbHeight.setHasControls(true);
            
            ComboBox cmbHeight = new ComboBox<String>();
            cmbHeight.setItems(listUnit);
            cmbHeight.setWidth("20%");
            
            if (((HorizontalLayoutElementUi) element).getElementUiHeight() != null && !((HorizontalLayoutElementUi) element).getElementUiHeight().isEmpty())
                setElementUiValue(((HorizontalLayoutElementUi) element).getElementUiHeight(), nmbHeight, cmbHeight);
            
            nmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((HorizontalLayoutElementUi) element).setElementUiHeight(value);
                ((HorizontalLayoutElementUi) element).setHeight(value);
                command.execute();
            });
            
            cmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((HorizontalLayoutElementUi) element).setElementUiHeight(value);
                ((HorizontalLayoutElementUi) element).setHeight(value);
                command.execute();
            });
            
            lytHeight.add(lblHeight, nmbHeight, cmbHeight);
            // end height <--
            
            // --> init alignment
            HorizontalLayout lytAlignment = new HorizontalLayout();
            lytAlignment.setWidthFull();
            
            BoldLabel lblAlignment = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-alignment"));
            lblAlignment.getStyle().set("margin-top", "10px");
            lblAlignment.setWidth("20%");
            
            ComboBox<String> cmbAlignment = new ComboBox();
            cmbAlignment.setAllowCustomValue(false);
            cmbAlignment.setItems(listAlignment);
            cmbAlignment.setWidth("80%");
            
            cmbAlignment.setValue(((HorizontalLayoutElementUi) element).getElementUiAlignment() == null || ((HorizontalLayoutElementUi) element).getElementUiAlignment().isEmpty()
                    ? "" : ((HorizontalLayoutElementUi) element).getElementUiAlignment());
            
            cmbAlignment.addValueChangeListener(listener -> {
                if (!cmbAlignment.getValue().equals(((HorizontalLayoutElementUi) element).getElementUiAlignment())) {
                    ((HorizontalLayoutElementUi) element).setElementUiAlignment(listener.getValue());
                    command.execute();
                }
            });
            
            lytAlignment.add(lblAlignment, cmbAlignment);
            // end alignment <--
            
            lytContent.add(lytWidth, lytHeight, lytAlignment);
        } else if (element instanceof VerticalLayoutElementUi) {
            // --> init width
            HorizontalLayout lytWidth = new HorizontalLayout();
            lytWidth.setWidthFull();
            
            BoldLabel lblWidth = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-width"));
            lblWidth.getStyle().set("margin-top", "10px");
            lblWidth.setWidth("20%");
            
            NumberField nmbWidth = new NumberField();
            nmbWidth.setMin(30);
            nmbWidth.setStep(1);
            nmbWidth.setWidth("55%");
            nmbWidth.setHasControls(true);
            
            ComboBox cmbWidth = new ComboBox<String>();
            cmbWidth.setItems(listUnit);
            cmbWidth.setWidth("20%");
            
            if (((VerticalLayoutElementUi) element).getElementUiWidth() != null && !((VerticalLayoutElementUi) element).getElementUiWidth().isEmpty())
                setElementUiValue(((VerticalLayoutElementUi) element).getElementUiWidth(), nmbWidth, cmbWidth);
            
            nmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((VerticalLayoutElementUi) element).setElementUiWidth(value);
                ((VerticalLayoutElementUi) element).setWidth(value);
                command.execute();
            });
            
            cmbWidth.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbWidth.getValue())), cmbWidth.getValue());
                ((VerticalLayoutElementUi) element).setElementUiWidth(value);
                ((VerticalLayoutElementUi) element).setWidth(value);
                command.execute();
            });
            
            lytWidth.add(lblWidth, nmbWidth, cmbWidth);
            // end width <--
            
            // --> init height
            HorizontalLayout lytHeight = new HorizontalLayout();
            lytHeight.setWidthFull();
           
            BoldLabel lblHeight = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-height"));
            lblHeight.getStyle().set("margin-top", "10px");
            lblHeight.setWidth("20%");
            
            NumberField nmbHeight = new NumberField();
            nmbHeight.setMin(30);
            nmbHeight.setStep(1);
            nmbHeight.setWidth("55%");
            nmbHeight.setHasControls(true);
            
            ComboBox cmbHeight = new ComboBox<String>();
            cmbHeight.setItems(listUnit);
            cmbHeight.setWidth("20%");
            
            if (((VerticalLayoutElementUi) element).getElementUiHeight() != null && !((VerticalLayoutElementUi) element).getElementUiHeight().isEmpty())
                setElementUiValue(((VerticalLayoutElementUi) element).getElementUiHeight(), nmbHeight, cmbHeight);
            
            nmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((VerticalLayoutElementUi) element).setElementUiHeight(value);
                ((VerticalLayoutElementUi) element).setHeight(value);
                command.execute();
            });
            
            cmbHeight.addValueChangeListener(listener -> {
                String value = String.format("%s%s", String.valueOf(Math.round(nmbHeight.getValue())), cmbHeight.getValue());
                ((VerticalLayoutElementUi) element).setElementUiHeight(value);
                ((VerticalLayoutElementUi) element).setHeight(value);
                command.execute();
            });
            
            lytHeight.add(lblHeight, nmbHeight, cmbHeight);
            // end height <--
            
            // --> init alignment
            HorizontalLayout lytAlignment = new HorizontalLayout();
            lytAlignment.setWidthFull();
            
            BoldLabel lblAlignment = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-alignment"));
            lblAlignment.getStyle().set("margin-top", "10px");
            lblAlignment.setWidth("20%");
            
            ComboBox<String> cmbAlignment = new ComboBox();
            cmbAlignment.setAllowCustomValue(false);
            cmbAlignment.setItems(listAlignment);
            cmbAlignment.setWidth("80%");
            
            cmbAlignment.setValue(((VerticalLayoutElementUi) element).getElementUiAlignment() == null || ((VerticalLayoutElementUi) element).getElementUiAlignment().isEmpty()
                    ? "" : ((VerticalLayoutElementUi) element).getElementUiAlignment());
            
            cmbAlignment.addValueChangeListener(listener -> {
                if (!cmbAlignment.getValue().equals(((VerticalLayoutElementUi) element).getElementUiAlignment())) {
                    ((VerticalLayoutElementUi) element).setElementUiAlignment(listener.getValue());
                    command.execute();
                }
            });
            
            lytAlignment.add(lblAlignment, cmbAlignment);
            // end alignment <--
            
            lytContent.add(lytWidth, lytHeight, lytAlignment);
        }
        
        return lytContent;
    }
    
    public static Component eventsFromElementUi(ElementUi element, TranslationService ts, ElementPropertyEditorDialog elementPropertyEditorDialog, List<ArtifactDefinitionFunction> functions, Command command, String path) {
        VerticalLayout lytContent = new VerticalLayout();
        lytContent.setSpacing(false);
        lytContent.setPadding(false);
        lytContent.setMargin(false);
        lytContent.setWidthFull();

        if (element instanceof ButtonElementUi) {
            // --> init onclick
            HorizontalLayout lytOnclick = new HorizontalLayout();
            lytOnclick.setWidthFull();

            BoldLabel lblOnclick = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-onclick"));
            lblOnclick.getStyle().set("margin-top", "10px");
            lblOnclick.setWidth("20%");

            TextField txtOnclick = new TextField();
            txtOnclick.setReadOnly(true);
            txtOnclick.setWidth("70%");

            txtOnclick.setValue(((ButtonElementUi) element).getElementUiOnClick() == null || ((ButtonElementUi) element).getElementUiOnClick().isEmpty()
                    ? "" : ((ButtonElementUi) element).getElementUiOnClick());

            ActionIcon iconOnclick = new ActionIcon(VaadinIcon.CODE, ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-property-name"));
            iconOnclick.setClassName("process-editor-artifact-icon-property");
            
            iconOnclick.addClickListener(e -> {
                launchPropertyEditor(element, ArtifactDefinitionConstants.LABEL_ONCLICK,
                        ((ButtonElementUi) element).getElementUiOnClick(), ts,
                        elementPropertyEditorDialog, functions, command, path);
            });

            txtOnclick.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtOnclick.getValue().equals(((ButtonElementUi) element).getElementUiOnClick()))
                    ((ButtonElementUi) element).setElementUiOnClick(txtOnclick.getValue());
            });

            lytOnclick.add(lblOnclick, txtOnclick, iconOnclick);
            // end onclick <--
            
            // --> init onload
            HorizontalLayout lytOnLoad = new HorizontalLayout();
            lytOnLoad.setWidthFull();
            
            BoldLabel lblOnload = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-onload"));
            lblOnload.getStyle().set("margin-top", "10px");
            lblOnload.setWidth("20%");
            
            TextField txtOnload = new TextField();
            txtOnload.setReadOnly(true);
            txtOnload.setWidth("70%");
            
            txtOnload.setValue(((ButtonElementUi) element).getElementUiOnLoad() == null || ((ButtonElementUi) element).getElementUiOnLoad().isEmpty()
                    ? "" : ((ButtonElementUi) element).getElementUiOnLoad());
            
            ActionIcon iconOnload = new ActionIcon(VaadinIcon.CODE, ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-property-name"));
            iconOnload.setClassName("process-editor-artifact-icon-property");
            
            iconOnload.addClickListener(e -> {
                launchPropertyEditor(element, ArtifactDefinitionConstants.LABEL_ONLOAD,
                        ((ButtonElementUi) element).getElementUiOnLoad(), ts,
                        elementPropertyEditorDialog, functions, command, path);
            });
            
            txtOnload.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtOnload.getValue().equals(((ButtonElementUi) element).getElementUiOnLoad()))
                    ((ButtonElementUi) element).setElementUiOnLoad(txtOnload.getValue());
            });
            
            lytOnLoad.add(lblOnload, txtOnload, iconOnload);
            // end onload <--
            
             // --> init onPropertyChange
            HorizontalLayout lytOnPropertyChange = new HorizontalLayout();
            lytOnPropertyChange.setWidthFull();
            
            BoldLabel lblOnPropertyChange = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-onpropertychange"));
            lblOnPropertyChange.getStyle().set("margin-top", "10px");
            lblOnPropertyChange.setWidth("40%");
            
            TextField txtOnPropertyChange = new TextField();
            txtOnPropertyChange.setReadOnly(true);
            txtOnPropertyChange.setWidth("50%");
            
            txtOnPropertyChange.setValue(((ButtonElementUi) element).getElementUiOnPropertyChange() == null || ((ButtonElementUi) element).getElementUiOnPropertyChange().isEmpty()
                    ? "" : ((ButtonElementUi) element).getElementUiOnPropertyChange());
            
            ActionIcon iconOnPropertyChange = new ActionIcon(VaadinIcon.CODE, ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-property-name"));
            iconOnPropertyChange.setClassName("process-editor-artifact-icon-property");
            
            iconOnPropertyChange.addClickListener(e -> {
                launchPropertyEditor(element, ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE,
                        ((ButtonElementUi) element).getElementUiOnPropertyChange(), ts,
                        elementPropertyEditorDialog, functions, command, path);
            });
            
            txtOnPropertyChange.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtOnload.getValue().equals(((TextFieldElementUi) element).getElementUiOnPropertyChange()))
                    ((ButtonElementUi) element).setElementUiOnPropertyChange(txtOnPropertyChange.getValue());
            });
            
            lytOnPropertyChange.add(lblOnPropertyChange, txtOnPropertyChange, iconOnPropertyChange);
            // end onPropertyChange <--

            lytContent.add(lytOnLoad, lytOnclick, lytOnPropertyChange);
        } else if (element instanceof ComboBoxElementUi) {
            // --> init onLazyLoad
            HorizontalLayout lytOnLazyLoad = new HorizontalLayout();
            lytOnLazyLoad.setWidthFull();

            BoldLabel lblOnLazyLoad = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-onlazyload"));
            lblOnLazyLoad.getStyle().set("margin-top", "10px");
            lblOnLazyLoad.setWidth("20%");

            TextField txtOnLazyLoad = new TextField();
            txtOnLazyLoad.setReadOnly(true);
            txtOnLazyLoad.setWidth("70%");

            txtOnLazyLoad.setValue(((ComboBoxElementUi) element).getElementUiOnLazyLoad() == null || ((ComboBoxElementUi) element).getElementUiOnLazyLoad().isEmpty()
                    ? "" : ((ComboBoxElementUi) element).getElementUiOnLazyLoad());

            ActionIcon iconOnLazyLoad = new ActionIcon(VaadinIcon.CODE, ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-property-name"));
            iconOnLazyLoad.setClassName("process-editor-artifact-icon-property");

            iconOnLazyLoad.addClickListener(e -> {
                launchPropertyEditor(element, ArtifactDefinitionConstants.LABEL_ONLAZYLOAD,
                        ((ComboBoxElementUi) element).getElementUiOnLoad(), ts,
                        elementPropertyEditorDialog, functions, command, path);
            });

            txtOnLazyLoad.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtOnLazyLoad.getValue().equals(((ComboBoxElementUi) element).getElementUiOnLoad()))
                    ((ComboBoxElementUi) element).setElementUiOnLoad(txtOnLazyLoad.getValue());
            });

            lytOnLazyLoad.add(lblOnLazyLoad, txtOnLazyLoad, iconOnLazyLoad);
            // end onLazyLoad <--

            // --> init onload
            HorizontalLayout lytOnLoad = new HorizontalLayout();
            lytOnLoad.setWidthFull();

            BoldLabel lblOnload = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-onload"));
            lblOnload.getStyle().set("margin-top", "10px");
            lblOnload.setWidth("20%");

            TextField txtOnload = new TextField();
            txtOnload.setReadOnly(true);
            txtOnload.setWidth("70%");

            txtOnload.setValue(((ComboBoxElementUi) element).getElementUiOnLoad() == null || ((ComboBoxElementUi) element).getElementUiOnLoad().isEmpty()
                    ? "" : ((ComboBoxElementUi) element).getElementUiOnLoad());

            ActionIcon iconOnload = new ActionIcon(VaadinIcon.CODE, ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-property-name"));
            iconOnload.setClassName("process-editor-artifact-icon-property");

            iconOnload.addClickListener(e -> {
                launchPropertyEditor(element, ArtifactDefinitionConstants.LABEL_ONLOAD,
                        ((ComboBoxElementUi) element).getElementUiOnLoad(), ts,
                        elementPropertyEditorDialog, functions, command, path);
            });

            txtOnload.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtOnload.getValue().equals(((ComboBoxElementUi) element).getElementUiOnLoad()))
                    ((ComboBoxElementUi) element).setElementUiOnLoad(txtOnload.getValue());
            });

            lytOnLoad.add(lblOnload, txtOnload, iconOnload);
            // end onload <--

            // --> init onPropertyChange
            HorizontalLayout lytOnPropertyChange = new HorizontalLayout();
            lytOnPropertyChange.setWidthFull();

            BoldLabel lblOnPropertyChange = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-onpropertychange"));
            lblOnPropertyChange.getStyle().set("margin-top", "10px");
            lblOnPropertyChange.setWidth("40%");

            TextField txtOnPropertyChange = new TextField();
            txtOnPropertyChange.setReadOnly(true);
            txtOnPropertyChange.setWidth("50%");

            txtOnPropertyChange.setValue(((ComboBoxElementUi) element).getElementUiOnPropertyChange() == null || ((ComboBoxElementUi) element).getElementUiOnPropertyChange().isEmpty()
                    ? "" : ((ComboBoxElementUi) element).getElementUiOnPropertyChange());

            ActionIcon iconOnPropertyChange = new ActionIcon(VaadinIcon.CODE, ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-property-name"));
            iconOnPropertyChange.setClassName("process-editor-artifact-icon-property");

            iconOnPropertyChange.addClickListener(e -> {
                launchPropertyEditor(element, ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE,
                        ((ComboBoxElementUi) element).getElementUiOnPropertyChange(), ts,
                        elementPropertyEditorDialog, functions, command, path);
            });

            txtOnPropertyChange.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtOnPropertyChange.getValue().equals(((ComboBoxElementUi) element).getElementUiOnPropertyChange()))
                    ((ComboBoxElementUi) element).setElementUiOnPropertyChange(txtOnPropertyChange.getValue());
            });

            lytOnPropertyChange.add(lblOnPropertyChange, txtOnPropertyChange, iconOnPropertyChange);
            // end onPropertyChange <--

            lytContent.add(lytOnLoad, lytOnLazyLoad, lytOnPropertyChange);
        } else if (element instanceof GridElementUi) {
            // --> init onload
            HorizontalLayout lytOnLoad = new HorizontalLayout();
            lytOnLoad.setWidthFull();

            BoldLabel lblOnload = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-onload"));
            lblOnload.getStyle().set("margin-top", "10px");
            lblOnload.setWidth("20%");

            TextField txtOnload = new TextField();
            txtOnload.setReadOnly(true);
            txtOnload.setWidth("70%");

            txtOnload.setValue(((GridElementUi) element).getElementUiOnLoad() == null || ((GridElementUi) element).getElementUiOnLoad().isEmpty()
                    ? "" : ((GridElementUi) element).getElementUiOnLoad());

            ActionIcon iconOnload = new ActionIcon(VaadinIcon.CODE, ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-property-name"));
            iconOnload.setClassName("process-editor-artifact-icon-property");

            iconOnload.addClickListener(e -> {
                launchPropertyEditor(element, ArtifactDefinitionConstants.LABEL_ONLOAD,
                        ((GridElementUi) element).getElementUiOnLoad(), ts,
                        elementPropertyEditorDialog, functions, command, path);
            });

            txtOnload.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtOnload.getValue().equals(((GridElementUi) element).getElementUiOnLoad()))
                    ((GridElementUi) element).setElementUiOnLoad(txtOnload.getValue());
            });

            lytOnLoad.add(lblOnload, txtOnload, iconOnload);
            // end onload <--

            lytContent.add(lytOnLoad);
        } else if (element instanceof LabelElementUi) {
            // --> init onload
            HorizontalLayout lytOnLoad = new HorizontalLayout();
            lytOnLoad.setWidthFull();

            BoldLabel lblOnload = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-onload"));
            lblOnload.getStyle().set("margin-top", "10px");
            lblOnload.setWidth("20%");

            TextField txtOnload = new TextField();
            txtOnload.setReadOnly(true);
            txtOnload.setWidth("70%");

            txtOnload.setValue(((LabelElementUi) element).getElementUiOnLoad() == null || ((LabelElementUi) element).getElementUiOnLoad().isEmpty()
                    ? "" : ((LabelElementUi) element).getElementUiOnLoad());

            ActionIcon iconOnload = new ActionIcon(VaadinIcon.CODE, ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-property-name"));
            iconOnload.setClassName("process-editor-artifact-icon-property");

            iconOnload.addClickListener(e -> {
                launchPropertyEditor(element, ArtifactDefinitionConstants.LABEL_ONLOAD,
                        ((LabelElementUi) element).getElementUiOnLoad(), ts,
                        elementPropertyEditorDialog, functions, command, path);
            });

            txtOnload.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtOnload.getValue().equals(((LabelElementUi) element).getElementUiOnLoad()))
                    ((LabelElementUi) element).setElementUiOnLoad(txtOnload.getValue());
            });

            lytOnLoad.add(lblOnload, txtOnload, iconOnload);
            // end onload <--

            // --> init onPropertyChange
            HorizontalLayout lytOnPropertyChange = new HorizontalLayout();
            lytOnPropertyChange.setWidthFull();

            BoldLabel lblOnPropertyChange = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-onpropertychange"));
            lblOnPropertyChange.getStyle().set("margin-top", "10px");
            lblOnPropertyChange.setWidth("40%");

            TextField txtOnPropertyChange = new TextField();
            txtOnPropertyChange.setReadOnly(true);
            txtOnPropertyChange.setWidth("50%");

            txtOnPropertyChange.setValue(((LabelElementUi) element).getElementUiOnPropertyChange() == null || ((LabelElementUi) element).getElementUiOnPropertyChange().isEmpty()
                    ? "" : ((LabelElementUi) element).getElementUiOnPropertyChange());

            ActionIcon iconOnPropertyChange = new ActionIcon(VaadinIcon.CODE, ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-property-name"));
            iconOnPropertyChange.setClassName("process-editor-artifact-icon-property");

            iconOnPropertyChange.addClickListener(e -> {
                launchPropertyEditor(element, ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE,
                        ((LabelElementUi) element).getElementUiOnPropertyChange(), ts,
                        elementPropertyEditorDialog, functions, command, path);
            });

            txtOnPropertyChange.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtOnPropertyChange.getValue().equals(((LabelElementUi) element).getElementUiOnPropertyChange()))
                    ((LabelElementUi) element).setElementUiOnLoad(txtOnPropertyChange.getValue());
            });

            lytOnPropertyChange.add(lblOnPropertyChange, txtOnPropertyChange, iconOnPropertyChange);
            // end onPropertyChange <--

            lytContent.add(lytOnLoad, lytOnPropertyChange);
        } else if (element instanceof TextFieldElementUi) {
            // --> init onload
            HorizontalLayout lytOnLoad = new HorizontalLayout();
            lytOnLoad.setWidthFull();

            BoldLabel lblOnload = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-onload"));
            lblOnload.getStyle().set("margin-top", "10px");
            lblOnload.setWidth("20%");

            TextField txtOnload = new TextField();
            txtOnload.setReadOnly(true);
            txtOnload.setWidth("70%");

            txtOnload.setValue(((TextFieldElementUi) element).getElementUiOnLoad() == null || ((TextFieldElementUi) element).getElementUiOnLoad().isEmpty()
                    ? "" : ((TextFieldElementUi) element).getElementUiOnLoad());

            ActionIcon iconOnload = new ActionIcon(VaadinIcon.CODE, ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-property-name"), ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-property-name"));
            iconOnload.setClassName("process-editor-artifact-icon-property");
            
            iconOnload.addClickListener(e -> {
                launchPropertyEditor(element, ArtifactDefinitionConstants.LABEL_ONLOAD,
                        ((TextFieldElementUi) element).getElementUiOnLoad(), ts,
                        elementPropertyEditorDialog, functions, command, path);
            });

            txtOnload.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtOnload.getValue().equals(((TextFieldElementUi) element).getElementUiOnLoad()))
                    ((TextFieldElementUi) element).setElementUiOnLoad(txtOnload.getValue());
            });

            lytOnLoad.add(lblOnload, txtOnload, iconOnload);
            // end onload <--

            // --> init onPropertyChange
            HorizontalLayout lytOnPropertyChange = new HorizontalLayout();
            lytOnPropertyChange.setWidthFull();

            BoldLabel lblOnPropertyChange = new BoldLabel(ts.getTranslatedString("module.processeditor.editor-form-property-label-onpropertychange"));
            lblOnPropertyChange.getStyle().set("margin-top", "10px");
            lblOnPropertyChange.setWidth("40%");

            TextField txtOnPropertyChange = new TextField();
            txtOnPropertyChange.setReadOnly(true);
            txtOnPropertyChange.setWidth("50%");

            txtOnPropertyChange.setValue(((TextFieldElementUi) element).getElementUiOnPropertyChange() == null || ((TextFieldElementUi) element).getElementUiOnPropertyChange().isEmpty()
                    ? "" : ((TextFieldElementUi) element).getElementUiOnPropertyChange());

            ActionIcon iconOnPropertyChange = new ActionIcon(VaadinIcon.CODE, ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-property-name"));
            iconOnPropertyChange.setClassName("process-editor-artifact-icon-property");

            iconOnPropertyChange.addClickListener(e -> {
                launchPropertyEditor(element, ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE,
                        ((TextFieldElementUi) element).getElementUiOnPropertyChange(), ts,
                        elementPropertyEditorDialog, functions, command, path);
            });

            txtOnPropertyChange.addKeyPressListener(Key.ENTER, listener -> {
                if (!txtOnPropertyChange.getValue().equals(((TextFieldElementUi) element).getElementUiOnPropertyChange()))
                    ((TextFieldElementUi) element).setElementUiOnPropertyChange(txtOnPropertyChange.getValue());
            });

            lytOnPropertyChange.add(lblOnPropertyChange, txtOnPropertyChange, iconOnPropertyChange);
            // end onPropertyChange <--

            lytContent.add(lytOnLoad, lytOnPropertyChange);
        }

        return lytContent;
    }
    
    private static void setElementUiValue(String elementUiValue, NumberField nmbUnit, ComboBox cmbValue) {
        char [] cadena_div = elementUiValue.toCharArray();
        String number = "";
        String unit = "";
        
        for (int i = 0; i < cadena_div.length; i++) {
            if (Character.isDigit(cadena_div[i]))
                number += cadena_div[i];
            else
                unit += cadena_div[i];
        }
        
        nmbUnit.setValue(Double.parseDouble(number));
        cmbValue.setValue(unit);
    }
        
    private static void launchPropertyEditor(ElementUi element, String propertyName, String propertyValue, TranslationService ts, ElementPropertyEditorDialog elementPropertyEditorDialog, List<ArtifactDefinitionFunction> functions, Command command, String path) {
        if (elementPropertyEditorDialog != null) {
            Command event = () -> command.execute();
            elementPropertyEditorDialog.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("elementUi", element),
                    new ModuleActionParameter("propertyName", propertyName),
                    new ModuleActionParameter("propertyValue", propertyValue),
                    new ModuleActionParameter("functions", functions),
                    new ModuleActionParameter("event", event),
                    new ModuleActionParameter("path", path)
            )).open();
        } 
    }
}