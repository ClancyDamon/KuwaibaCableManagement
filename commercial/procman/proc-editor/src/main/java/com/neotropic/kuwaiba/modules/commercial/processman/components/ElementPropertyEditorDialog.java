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
package com.neotropic.kuwaiba.modules.commercial.processman.components;

import com.neotropic.flow.component.aceeditor.AceEditor;
import com.neotropic.flow.component.aceeditor.AceMode;
import com.neotropic.kuwaiba.modules.commercial.processman.ProcessEditorModule;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.ButtonElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.ComboBoxElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.ElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.GridElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.LabelElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.TextFieldElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.service.ArtifactDefinitionFunction;
import com.neotropic.kuwaiba.modules.commercial.processman.tools.ArtifactDefinitionConstants;
import com.neotropic.kuwaiba.modules.commercial.processman.tools.ElementPropertyType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.server.Command;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create or edit a property value and manage artifact functions.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@org.springframework.stereotype.Component
public class ElementPropertyEditorDialog extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired 
    private MetadataEntityManager mem;
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired 
    private BusinessEntityManager bem;
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired 
    private ApplicationEntityManager aem;
    /**
     * The selected Element.
     */
    private ElementUi selectedElement;
    private String propertyName;
    private String propertyValue;
    private String artifactPath;
    /**
     * Attributes
     */
    private ComboBox<ElementPropertyType> cmbAttributes;
    private List<ElementPropertyType> listAttributes;
    /*
     * Parameters
     */
    private VerticalLayout lytParameters;
    private HorizontalLayout lytListParams;
    private Grid<StringPair> gridParameters;
    private List<StringPair> listParameters; 
    /**
     * Variables
     */
    private HorizontalLayout lytPredefinedVariables;
    /**
     * Functions
     */
    private ComboBox<ArtifactDefinitionFunction> cmbFunctions;
    private List<ArtifactDefinitionFunction> listFunctions;
    private String functionString;
    /**
     * AceEditor 
     */
    private AceEditor editorScript;
    private VerticalLayout lytScript;
    /**
     * Update action
     */
    private ActionButton btnUpdate;
    /**
     * Dialog that updates the property value
     */
    private ConfirmDialog wdwEditor;
    /**
     * Dialog that update the function
     */
    private ConfirmDialog wdwScript;
    /**
     * Elements grid
     */
    private Grid<HashMap<String, StringPair>> gridElements;
    /**
     * Show additional info
     */
    private HorizontalLayout lytInfo;
    
    public ElementPropertyEditorDialog() {
        super(ProcessEditorModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("elementUi")) {
            if (parameters.containsKey("propertyName")) {
                if (parameters.containsKey("propertyValue")) {
                    if (parameters.containsKey("path")) {
                        selectedElement = (ElementUi) parameters.get("elementUi");
                        propertyName = (String) parameters.get("propertyName");
                        propertyValue = (String) parameters.get("propertyValue");
                        Command event = (Command) parameters.get("event");
                        listFunctions = (List<ArtifactDefinitionFunction>) parameters.get("functions");
                        artifactPath = (String) parameters.get("path");

                        wdwEditor = new ConfirmDialog(ts, ts.getTranslatedString("module.general.messages.save"));
                        wdwEditor.setDraggable(true);
                        wdwEditor.setResizable(true);
                        wdwEditor.setSizeUndefined();
                        wdwEditor.setWidth("50%");

                        // --> init attributes
                        cmbAttributes = new ComboBox(ts.getTranslatedString("module.processeditor.editor-form-property-editor-attributes"));
                        cmbAttributes.setPlaceholder(ts.getTranslatedString("module.processeditor.editor-form-property-editor-attributes-placeholder"));
                        cmbAttributes.setRequiredIndicatorVisible(true);
                        cmbAttributes.setAllowCustomValue(false);
                        cmbAttributes.setWidth("45%");

                        validateElementProperties(selectedElement);
                        // end attributes <--

                        // --> init functions
                        HorizontalLayout lytFunctions = new HorizontalLayout();
                        lytFunctions.setWidthFull();

                        cmbFunctions = new ComboBox(ts.getTranslatedString("module.processeditor.editor-form-property-editor-select-function"));
                        cmbFunctions.setPlaceholder(ts.getTranslatedString("module.processeditor.editor-form-property-editor-select-function-placeholder"));
                        cmbFunctions.setRequiredIndicatorVisible(true);
                        cmbFunctions.setItems(listFunctions);
                        cmbFunctions.setAllowCustomValue(false);
                        cmbFunctions.setEnabled(false);
                        cmbFunctions.setWidth("45%");

                        lytParameters = new VerticalLayout();
                        listParameters = new ArrayList<>();

                        ActionButton btnAdd = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O), ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions-new-function-name"));
                        btnAdd.getStyle().set("martin-top", "auto");
                        btnAdd.setHeight("33px");
                        btnAdd.setEnabled(false);

                        btnUpdate = new ActionButton(new ActionIcon(VaadinIcon.EDIT), ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions-update-function-name"));
                        btnUpdate.getStyle().set("martin-top", "auto");
                        btnUpdate.setHeight("33px");
                        btnUpdate.setEnabled(false);

                        functionString = "";
                        cmbAttributes.addValueChangeListener(e -> {
                            cmbFunctions.setEnabled(e.getValue() != null);
                            btnAdd.setEnabled(e.getValue() != null);
                            createFunctionString();
                        });
                        cmbFunctions.addValueChangeListener(e -> {
                            if (e.getValue() != null) {
                                btnUpdate.setEnabled(true);
                                createGridParameters(e.getValue(), propertyValue);
                                createFunctionString();
                            } else
                                btnUpdate.setEnabled(false);
                        });

                        if (propertyValue != null && !propertyValue.trim().isEmpty())
                            loadPropertyValue(propertyValue);

                        wdwEditor.getBtnConfirm().addClickListener(listener -> {
                            if (validateConfirm()) {
                                createFunctionString();
                                updateSourcePropertyValue(selectedElement, propertyName, cmbAttributes.getValue().getName(), functionString);
                                event.execute();
                                wdwEditor.close();
                            }
                        });

                        btnAdd.addClickListener(e -> launchScripEditor(null, aem, bem, mem));
                        btnUpdate.addClickListener(e -> launchScripEditor(cmbFunctions.getValue(), aem, bem, mem));

                        HorizontalLayout lytActions = new HorizontalLayout(btnAdd, btnUpdate);
                        lytActions.getStyle().set("margin-top", "auto");
                        // end functions <--

                        HorizontalLayout lytSelectors = new HorizontalLayout();
                        lytSelectors.add(cmbAttributes, cmbFunctions, lytActions);

                        wdwEditor.setHeader(ts.getTranslatedString("module.processeditor.editor-form-property-editor-header"));
                        wdwEditor.setContent(lytSelectors, lytParameters);
                        return wdwEditor;
                    } else
                        return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "path")));
                } else
                    return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "propertyValue")));
            } else
                return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "propertyName")));
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "elementUi")));
    }

    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }   
    
    private boolean validateConfirm() {
        boolean close = true;
        if (cmbAttributes.getValue() == null) {
            close = false;
            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                    ts.getTranslatedString("module.processeditor.editor-form-property-editor-attributes-placeholder"),
                    AbstractNotification.NotificationType.WARNING, ts).open();
        } else if (cmbFunctions.getValue() == null) {
            close = false;
            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                    ts.getTranslatedString("module.processeditor.editor-form-property-editor-select-function-placeholder"),
                    AbstractNotification.NotificationType.WARNING, ts).open();
        } else {
            for (StringPair parameter : listParameters) {
                if (parameter.getValue().trim().isEmpty()) {
                    close = false;
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                            ts.getTranslatedString("module.general.messages.must-fill-all-fields"),
                            AbstractNotification.NotificationType.WARNING, ts).open();
                    break;
                } else if (parameter.getValue().contains(" ")) {
                    close = false;
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                            ts.getTranslatedString("module.general.messages.field-value-not-space"),
                            AbstractNotification.NotificationType.WARNING, ts).open();
                    break;
                } else {
                    Pattern pattern = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(parameter.getValue());

                    boolean result = matcher.find();
                    if (result) {
                        close = false;
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                ts.getTranslatedString("module.general.messages.field-value-not-special-characters"),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                        break;
                    }
                }
            }
        }
        return close;
    }
    
    
    /**
     * Validates which properties of the selected element are editable from the function.
     * @param element The selected element.
     */
    private void validateElementProperties(ElementUi element) {
        listAttributes = new ArrayList<>();
        if (element instanceof ButtonElementUi) {
            listAttributes.add(new ElementPropertyType(ArtifactDefinitionConstants.LABEL_CAPTION,
                    ts.getTranslatedString("module.processeditor.editor-form-property-label-caption")));
        } else if (element instanceof ComboBoxElementUi) {
            listAttributes.add(new ElementPropertyType(ArtifactDefinitionConstants.LABEL_VALUE,
                    ts.getTranslatedString("module.processeditor.editor-form-property-label-value")));
            listAttributes.add(new ElementPropertyType(ArtifactDefinitionConstants.LABEL_ITEMS,
                    ts.getTranslatedString("module.processeditor.editor-form-property-label-items")));
        } else if (element instanceof GridElementUi) {
            listAttributes.add(new ElementPropertyType(ArtifactDefinitionConstants.LABEL_ROWS,
                    ts.getTranslatedString("module.processeditor.editor-form-property-label-rows")));
        } else if (element instanceof LabelElementUi || element instanceof TextFieldElementUi) {
            listAttributes.add(new ElementPropertyType(ArtifactDefinitionConstants.LABEL_VALUE,
                    ts.getTranslatedString("module.processeditor.editor-form-property-label-value")));
        }
        
        cmbAttributes.setItems(listAttributes);
    }
    
    /**
     * Allows you to create or edit a function for the property of the selected element.
     * @param selectedFunction The selected function if exists.
     */
    private void launchScripEditor(ArtifactDefinitionFunction selectedFunction, ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem) {
        wdwScript = new ConfirmDialog(ts, ts.getTranslatedString("module.general.messages.ok"));
        wdwScript.setDraggable(true);
        wdwScript.setResizable(true);
        wdwScript.setWidth("58%");
        wdwScript.setHeight("87%");
        
        // info
        lytInfo = new HorizontalLayout();
        lytInfo.setVisible(false);
        lytInfo.removeAll();
        
        gridElements = new Grid<>();
        gridElements.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        gridElements.setSelectionMode(Grid.SelectionMode.NONE);
        gridElements.setHeightByRows(true);
        gridElements.setWidthFull();
        gridElements.addColumn(item -> item.keySet().toString())
                .setHeader(ts.getTranslatedString("module.general.labels.type"));
        gridElements.addColumn(item -> item.values().iterator().next().getKey())
                .setHeader(ts.getTranslatedString("module.processeditor.editor-form-property-label-id"));
        gridElements.addColumn(item -> item.values().iterator().next().getValue())
                .setHeader(ts.getTranslatedString("module.general.labels.property"));
        
        // name
        TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
        txtName.setPlaceholder(ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.parameter-info"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setWidthFull();
        
        if(selectedFunction != null) {
            txtName.setValue(selectedFunction.getName() == null || selectedFunction.getName().isEmpty() ? "" : selectedFunction.getName());
            listParameters = selectedFunction.getParameters();
            searchFunctions(artifactPath);
        } else 
            listParameters = new ArrayList<>();
            
        Button btnParameters = new Button(ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.manage-parameters-name"));
        btnParameters.addClickListener(event -> launchParametersDialog());
        btnParameters.setWidthFull();
        Label lblTitleParams = new Label(String.format("%s:", ts.getTranslatedString("module.general.labels.parameters")));
        lblTitleParams.setClassName("bold-font");        
        lytListParams = new HorizontalLayout();
        VerticalLayout lytParams = new VerticalLayout(lblTitleParams, lytListParams);
        lytParams.setSpacing(false);
        refreshParametersInfo(listParameters);
        // variables
        Label lblTitleVariables = new Label(String.format("%s:", ts.getTranslatedString("module.general.labels.predefined-variables")));
        lblTitleVariables.setClassName("bold-font");
        lytPredefinedVariables = new HorizontalLayout();
        createVariablesLabel();
        VerticalLayout lytVariables = new VerticalLayout(lblTitleVariables, lytPredefinedVariables);
        lytVariables.setSpacing(false);
        // script
        lytScript = new VerticalLayout();
        createScript(selectedFunction);
        
        wdwScript.getBtnConfirm().addClickListener(e -> {
            if (validateFunctionValue(txtName.getValue())) {           
                if (selectedFunction != null) {
                    for (ArtifactDefinitionFunction function : listFunctions) {
                        if (function.equals(selectedFunction)) {
                            function.setName(txtName.getValue());
                            function.setParameters(listParameters);
                            function.setValue(editorScript.getValue());
                            cmbFunctions.setValue(function);
                            createGridParameters(function, propertyValue);
                            
                            break;
                        }
                    }
                } else {
                    ArtifactDefinitionFunction function = new ArtifactDefinitionFunction();
                    function.setType("function");
                    function.setName(txtName.getValue());
                    function.setParameters(listParameters);
                    function.setValue(editorScript.getValue());

                    listFunctions.add(function);
                    cmbFunctions.setItems(listFunctions);
                    cmbFunctions.setValue(function);
                }
                wdwScript.close();
            }
        });
                
        wdwScript.setHeader(ts.getTranslatedString("module.processeditor.editor-form-script-editor-header"));
        wdwScript.setContent(lytInfo, txtName, btnParameters, lytParams, lytVariables, lytScript);
        wdwScript.open();
    }
    
    /**
     * Creates the function script.
     * @param selectedFunction  The selected function if exists.
     */
    private void createScript(ArtifactDefinitionFunction selectedFunction) {
        Label lblScript = new Label(ts.getTranslatedString("module.general.labels.script"));
        lblScript.setClassName("bold-font");
        editorScript = new AceEditor();
        editorScript.setMode(AceMode.groovy);
        editorScript.setHeight("250px");
        if (selectedFunction != null)
            editorScript.setValue(selectedFunction.getValue() != null
                    && !selectedFunction.getValue().isEmpty()
                    ? selectedFunction.getValue() : "");
        lytScript.removeAll();
        lytScript.add(lblScript, editorScript);
    }

    /**
     * Dialog to manage function parameters.
     */
    private void launchParametersDialog() {
        if (listParameters != null) {
            ConfirmDialog wdwParameter = new ConfirmDialog(ts, ts.getTranslatedString("module.general.messages.close"));
            wdwParameter.getBtnCancel().setText(ts.getTranslatedString("module.general.messages.close"));
            wdwParameter.getBtnConfirm().setVisible(false);
            wdwParameter.setResizable(true);
            wdwParameter.setDraggable(true);
            wdwParameter.setSizeUndefined();
            wdwParameter.setWidth("50%");

            Button btnAddParameter = new Button(ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.new-parameter-name"));
            btnAddParameter.addClickListener(event -> launchParameterActions(null, true, false, false));

            gridParameters = new Grid<>();
            gridParameters.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
            gridParameters.setSelectionMode(Grid.SelectionMode.SINGLE);
            gridParameters.setHeightByRows(true);
            gridParameters.setItems(listParameters);
            gridParameters.addColumn(StringPair::getKey).setHeader(ts.getTranslatedString("module.general.labels.parameters"));
            gridParameters.addComponentColumn(param -> addParameterActions(param))
                    .setTextAlign(ColumnTextAlign.END).setFlexGrow(0);
            gridParameters.setId("grid-man-parameter");

            wdwParameter.setHeader(ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.manage-parameters-name"));
            wdwParameter.setContent(btnAddParameter, gridParameters);
            wdwParameter.open();
        }
    }
    
    private void searchFunctions(String artifactPath) {
        try {
            if (artifactPath != null && !artifactPath.trim().isEmpty()) {

                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                QName tagTextField = new QName(ArtifactDefinitionConstants.LABEL_TEXTFIELD);
                QName tagButton = new QName(ArtifactDefinitionConstants.LABEL_BUTTON);
                QName tagComboBox = new QName(ArtifactDefinitionConstants.LABEL_COMBOBOX);
                QName tagGrid = new QName(ArtifactDefinitionConstants.LABEL_GRID);
                QName tagLabel = new QName(ArtifactDefinitionConstants.LABEL_LABEL);

                File file = new File(artifactPath);
                InputStream theStream = new FileInputStream(file);
                XMLStreamReader reader = inputFactory.createXMLStreamReader(theStream);

                List<HashMap<String, StringPair>> listElements = new ArrayList<>();
                while (reader.hasNext()) {
                    int event = reader.next();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        if (reader.getName().equals(tagTextField)) {
                            String onload = reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ONLOAD);
                            if (onload != null && !onload.trim().isEmpty()) {
                                if (validateContainsFunction(onload)) {
                                    StringPair value = new StringPair(reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ID), ts.getTranslatedString("module.processeditor.editor-form-property-label-onload"));
                                    HashMap<String, StringPair> textField = new HashMap<>();
                                    textField.put(ts.getTranslatedString("module.processeditor.editor-form-control-label-textfield"), value);

                                    listElements.add(textField);
                                }
                            }
                            
                            String onPropertyChange = reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE);
                            if (onPropertyChange != null && !onPropertyChange.trim().isEmpty()) {
                                if (validateContainsFunction(onPropertyChange)) {
                                    StringPair value = new StringPair(reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ID), ts.getTranslatedString("module.processeditor.editor-form-property-label-onpropertychange"));
                                    HashMap<String, StringPair> textField = new HashMap<>();
                                    textField.put(ts.getTranslatedString("module.processeditor.editor-form-control-label-textfield"), value);

                                    listElements.add(textField);
                                }
                            }
                        } else if (reader.getName().equals(tagButton)) {
                            String onclick = reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ONCLICK);
                            if (onclick != null && !onclick.trim().isEmpty()) {
                                if (validateContainsFunction(onclick)) {
                                    StringPair value = new StringPair(reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ID), ts.getTranslatedString("module.processeditor.editor-form-property-label-onclick"));
                                    HashMap<String, StringPair> button = new HashMap<>();
                                    button.put(ts.getTranslatedString("module.processeditor.editor-form-control-label-button"), value);

                                    listElements.add(button);
                                }
                            }
                            
                            String onload = reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ONLOAD);
                            if (onload != null && !onload.trim().isEmpty()) {
                                if (validateContainsFunction(onload)) {
                                    StringPair value = new StringPair(reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ID), ts.getTranslatedString("module.processeditor.editor-form-property-label-onload"));
                                    HashMap<String, StringPair> button = new HashMap<>();
                                    button.put(ts.getTranslatedString("module.processeditor.editor-form-control-label-button"), value);

                                    listElements.add(button);
                                }
                            }
                            
                            String onPropertyChange = reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE);
                            if (onPropertyChange != null && !onPropertyChange.trim().isEmpty()) {
                                if (validateContainsFunction(onPropertyChange)) {
                                    StringPair value = new StringPair(reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ID), ts.getTranslatedString("module.processeditor.editor-form-property-label-onpropertychange"));
                                    HashMap<String, StringPair> button = new HashMap<>();
                                    button.put(ts.getTranslatedString("module.processeditor.editor-form-control-label-button"), value);

                                    listElements.add(button);
                                }
                            }
                        } else if (reader.getName().equals(tagComboBox)) {
                            String onlazyload = reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ONLAZYLOAD);
                            if (onlazyload != null && !onlazyload.trim().isEmpty()) {
                                if (validateContainsFunction(onlazyload)) {
                                    StringPair value = new StringPair(reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ID), ts.getTranslatedString("module.processeditor.editor-form-property-label-onlazyload"));
                                    HashMap<String, StringPair> combobox = new HashMap<>();
                                    combobox.put(ts.getTranslatedString("module.processeditor.editor-form-control-label-combobox"), value);

                                    listElements.add(combobox);
                                }
                            }
                            
                            String onload = reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ONLOAD);
                            if (onload != null && !onload.trim().isEmpty()) {
                                if (validateContainsFunction(onload)) {
                                    StringPair value = new StringPair(reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ID), ts.getTranslatedString("module.processeditor.editor-form-property-label-onload"));
                                    HashMap<String, StringPair> combobox = new HashMap<>();
                                    combobox.put(ts.getTranslatedString("module.processeditor.editor-form-control-label-combobox"), value);

                                    listElements.add(combobox);
                                }
                            }
                            
                            String onPropertyChange = reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE);
                            if (onPropertyChange != null && !onPropertyChange.trim().isEmpty()) {
                                if (validateContainsFunction(onPropertyChange)) {
                                    StringPair value = new StringPair(reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ID), ts.getTranslatedString("module.processeditor.editor-form-property-label-onpropertychange"));
                                    HashMap<String, StringPair> combobox = new HashMap<>();
                                    combobox.put(ts.getTranslatedString("module.processeditor.editor-form-control-label-combobox"), value);

                                    listElements.add(combobox);
                                }
                            }
                        } else if (reader.getName().equals(tagGrid)) {
                            String onload = reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ONLOAD);
                            if (onload != null && !onload.trim().isEmpty()) {
                                if (validateContainsFunction(onload)) {
                                    StringPair value = new StringPair(reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ID), ts.getTranslatedString("module.processeditor.editor-form-property-label-onload"));
                                    HashMap<String, StringPair> grid = new HashMap<>();
                                    grid.put(ts.getTranslatedString("module.processeditor.editor-form-control-label-grid"), value);

                                    listElements.add(grid);
                                }
                            }
                        } else if (reader.getName().equals(tagLabel)) {
                            String onload = reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ONLOAD);
                            if (onload != null && !onload.trim().isEmpty()) {
                                if (validateContainsFunction(onload)) {
                                    StringPair value = new StringPair(reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ID), ts.getTranslatedString("module.processeditor.editor-form-property-label-onload"));
                                    HashMap<String, StringPair> label = new HashMap<>();
                                    label.put(ts.getTranslatedString("module.processeditor.editor-form-control-label-label"), value);

                                    listElements.add(label);
                                }
                            }
                            
                            String onPropertyChange = reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE);
                            if (onPropertyChange != null && !onPropertyChange.trim().isEmpty()) {
                                if (validateContainsFunction(onPropertyChange)) {
                                    StringPair value = new StringPair(reader.getAttributeValue(null, ArtifactDefinitionConstants.LABEL_ID), ts.getTranslatedString("module.processeditor.editor-form-property-label-onpropertychange"));
                                    HashMap<String, StringPair> label = new HashMap<>();
                                    label.put(ts.getTranslatedString("module.processeditor.editor-form-control-label-label"), value);

                                    listElements.add(label);
                                }
                            }
                        }
                    }
                }

                ListDataProvider<HashMap<String, StringPair>> dataProviderParameters = new ListDataProvider<>(listElements);
                gridElements.setDataProvider(dataProviderParameters);
                
                if (listElements.size() > 0) {
                    lytInfo.removeAll();
                    lytInfo.setVisible(true);
                    
                    ActionIcon iconInfo = new ActionIcon(VaadinIcon.INFO_CIRCLE_O, ts.getTranslatedString("module.processeditor.editor-form-script-editor-elements-show"));
                    iconInfo.addClickListener(e -> showElementsContainsFunction());
                    Label lblInfo = new Label(ts.getTranslatedString("module.processeditor.editor-form-script-editor-elements-info"));
                    lblInfo.setClassName("bold-font");  
                    
                    lytInfo.add(iconInfo, lblInfo);
                }
            }
        } catch (FileNotFoundException | XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private boolean validateContainsFunction(String property) {
        if (cmbFunctions.getValue() != null) {
            String[] array = property.split("\\.");
            String aFunction = array[1];
            
            return cmbFunctions.getValue().getName().contains(aFunction);
        } else
            return false;
    }
    
    private void showElementsContainsFunction() {
        ConfirmDialog wdwElements = new ConfirmDialog(ts, ts.getTranslatedString("module.general.messages.close"));
        wdwElements.getBtnCancel().setText(ts.getTranslatedString("module.general.messages.close"));
        wdwElements.getBtnConfirm().setVisible(false);
        wdwElements.setResizable(true);
        wdwElements.setDraggable(true);
        wdwElements.setSizeUndefined();
        wdwElements.setWidth("62%");
        
        wdwElements.setHeader(ts.getTranslatedString("module.processeditor.editor-form-script-editor-elements-header"));
        wdwElements.setContent(gridElements);
        wdwElements.open();
    }
    
    /**
     * Component with actions that allow managing parameters.
     * @param parameter The parameter to which the component is added. 
     * @return The actions to manage the parameter.
     */
    private Component addParameterActions(StringPair parameter) {
        HorizontalLayout lytActions = new HorizontalLayout();
        lytActions.setSpacing(false);
        
        ActionButton btnUpdateParameter = new ActionButton(new ActionIcon(VaadinIcon.EDIT),
                ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.update-parameter-name"));
        btnUpdateParameter.addClickListener(event -> launchParameterActions(parameter, false, true, false));
        
        ActionButton btnDeleteParameter = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.delete-parameter-name"));
        btnDeleteParameter.addClickListener(event -> launchParameterActions(parameter, false, false, true));
        
        lytActions.add(btnUpdateParameter, btnDeleteParameter);
        return lytActions;
    }

    /**
     * Manages the a property parameter list.
     * @param parameters property parameter list.
     * @param parameter parameter to update or delete.
     * @param add validate if it is the add action.
     * @param update validate if it is the update action.
     * @param delete validate if it is the delete action.
     */
    private void launchParameterActions(StringPair parameter, boolean add, boolean update, boolean delete) {
        if (listParameters != null) {
            ConfirmDialog wdwAction = new ConfirmDialog(ts, ts.getTranslatedString("module.general.messages.ok"));
            wdwAction.setDraggable(true);
            wdwAction.setResizable(true);
            wdwAction.setWidth("40%");

            if (add) {
                wdwAction.setHeader(ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.new-parameter-name"));

                TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
                txtName.setPlaceholder(ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.parameter-info"));
                txtName.setRequiredIndicatorVisible(true);
                txtName.setWidthFull();
                
                wdwAction.getBtnConfirm().addClickListener(e -> {
                    if (validateFunctionValue(txtName.getValue()) && !validateParameterKey(txtName.getValue())) {
                        StringPair param = new StringPair(txtName.getValue(), "");
                        listParameters.add(param);
                        wdwAction.close();
                        refreshParametersGrid(listParameters);
                        refreshParametersInfo(listParameters);

                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.new-parameter-success"),
                                AbstractNotification.NotificationType.INFO, ts).open();
                        wdwAction.close();
                    }
                });
                wdwAction.setContent(txtName);
            } else if (update) {
                if (parameter != null) {
                    wdwAction.setHeader(ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.update-parameter-name"));

                    TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
                    txtName.setPlaceholder(ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.parameter-info"));
                    txtName.setRequiredIndicatorVisible(true);
                    txtName.setValue(parameter.getKey());
                    txtName.setWidthFull();

                    wdwAction.getBtnConfirm().addClickListener(e -> {
                        if (validateFunctionValue(txtName.getValue()) && !validateParameterKey(txtName.getValue())) {
                            listParameters.forEach(param -> {
                                if (param.getKey().equals(parameter.getKey()))
                                    param.setKey(txtName.getValue());
                            });

                            wdwAction.close();
                            refreshParametersGrid(listParameters);
                            refreshParametersInfo(listParameters);

                            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                    ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.update-parameter-success"),
                                    AbstractNotification.NotificationType.INFO, ts).open();
                            wdwAction.close();
                        }
                    });

                    wdwAction.setContent(txtName);
                }
            } else if (delete) {
                if (parameter != null) {
                    wdwAction.getBtnConfirm().setText(ts.getTranslatedString("module.general.messages.delete"));
                    wdwAction.setHeader(ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.delete-parameter-name"));
                    wdwAction.setContent(new Span(String.format(ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.delete-parameter-confirm"), parameter.getKey())));
                    
                    wdwAction.getBtnConfirm().addClickListener(e -> {
                        listParameters.remove(parameter);
                        wdwAction.close();
                        refreshParametersGrid(listParameters);
                        refreshParametersInfo(listParameters);

                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.delete-parameter-success"),
                                AbstractNotification.NotificationType.INFO, ts).open();
                    });
                }
            }

            wdwAction.open();
        }
    }
    
    /**
     * Validates if function values are empty or contain spaces.
     * @param value The value to evaluate.
     * @return Returns true if the value is valid, otherwise it will be false.
     */
    private boolean validateFunctionValue(String value) {
        boolean close = true;
        if (value.trim().isEmpty()) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                    ts.getTranslatedString("module.general.messages.field-value-not-empty"),
                    AbstractNotification.NotificationType.WARNING, ts).open();
            close = false;
        } else if (value.contains(" ")) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                    ts.getTranslatedString("module.general.messages.field-value-not-space"),
                    AbstractNotification.NotificationType.WARNING, ts).open();
            close = false;
        } else {
            Pattern pattern = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(value);

            boolean result = matcher.find();
            if (result) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                        ts.getTranslatedString("module.general.messages.field-value-not-special-characters"),
                        AbstractNotification.NotificationType.WARNING, ts).open();
                close = false;
            }
        }
        
        return close;
    }
    
    /**
     * Validates if there is a parameter with the same name.
     * @param value The value to evaluate.
     * @return Returns true if there is a parameter with the same name, otherwise it will be false.
     */
    private boolean validateParameterKey(String value) {
        boolean exists = false;
        for (StringPair param : listParameters) {
            if (param.getKey().equals(value)) {
                exists = true;
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                        String.format(ts.getTranslatedString("module.general.labels.parameter-already-exists"), value),
                        AbstractNotification.NotificationType.WARNING, ts).open();
                break;
            }
        }
        return exists;
    }
    
    /**
     * Refresh parameters grid. When adding, editing or deleting a parameter.
     * @param parameters 
     */
    private void refreshParametersGrid(List<StringPair> parameters) {
        gridParameters.setItems(parameters);
        gridParameters.getDataProvider().refreshAll();
    }
    
    /**
     * Refresh parameters info. When adding, editing or deleting a parameter.
     * @param parameters 
     */
    private void refreshParametersInfo(List<StringPair> parameters) {        
        lytListParams.removeAll();
        parameters.forEach(param -> {
            Span parameter = new Span(param.getKey());
            parameter.setClassName("process-editor-artifact-function-parameters");
            lytListParams.add(parameter);
        });
    }
    
    /**
     * Display predefined variables
     */
    private void createVariablesLabel() {
        lytPredefinedVariables.removeAll();
        
        Span varAem = new Span("aem"); //I18N
        varAem.setClassName("process-editor-artifact-function-variables");
        
        Span varBem = new Span("bem"); //I18N
        varBem.setClassName("process-editor-artifact-function-variables");
        
        Span varMem = new Span("mem"); //I18N
        varMem.setClassName("process-editor-artifact-function-variables");
        
        Span varWarehouse = new Span("warehouseService"); //I18N
        varWarehouse.setClassName("process-editor-artifact-function-variables");
        varWarehouse.setMinWidth("140px");
        
        lytPredefinedVariables.add(varAem, varBem, varMem, varWarehouse);
    }

    /**
     * Update the element source property.
     * @param element Selected element.
     * @param sourceProperty Element source property.
     * @param targetProperty Element target property.
     * @param functionString Value that the element source property will take.
     */
    private void updateSourcePropertyValue(ElementUi element, String sourceProperty, String targetProperty, String functionString) {
        if (sourceProperty != null && targetProperty != null && element != null && functionString != null) {
            if (element instanceof TextFieldElementUi) {
                TextFieldElementUi textFieldElementUi = (TextFieldElementUi) element; 
                switch(sourceProperty) {
                    case ArtifactDefinitionConstants.LABEL_ONLOAD:
                        textFieldElementUi.setElementUiOnLoad(functionString);
                        break;
                    case ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE:
                        textFieldElementUi.setElementUiOnPropertyChange(functionString);
                        break;
                    default:
                        break;
                }
            } else if (element instanceof ButtonElementUi) {
                ButtonElementUi buttonElementUi = (ButtonElementUi) element; 
                switch(sourceProperty) {
                    case ArtifactDefinitionConstants.LABEL_ONCLICK:
                        buttonElementUi.setElementUiOnClick(functionString);
                        break;
                    case ArtifactDefinitionConstants.LABEL_ONLOAD:
                        buttonElementUi.setElementUiOnLoad(functionString);
                        break;
                    case ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE:
                        buttonElementUi.setElementUiOnPropertyChange(functionString);
                        break;   
                    default:
                        break;
                }
            } else if (element instanceof ComboBoxElementUi) {
                ComboBoxElementUi comboBoxElementUi = (ComboBoxElementUi) element; 
                switch(sourceProperty) {
                    case ArtifactDefinitionConstants.LABEL_ONLOAD:
                        comboBoxElementUi.setElementUiOnLoad(functionString);
                        break;
                    case ArtifactDefinitionConstants.LABEL_ONLAZYLOAD:
                        comboBoxElementUi.setElementUiOnLazyLoad(functionString);
                        break;
                    case ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE:
                        comboBoxElementUi.setElementUiOnPropertyChange(functionString);
                        break;
                    default:
                        break;
                }
            } else if (element instanceof GridElementUi) {
                GridElementUi gridElementUi = (GridElementUi) element; 
                switch(sourceProperty) {
                    case ArtifactDefinitionConstants.LABEL_ONLOAD:
                        gridElementUi.setElementUiOnLoad(functionString);
                        break;
                    default:
                        break;
                }
            } else if (element instanceof LabelElementUi) {
                LabelElementUi labelElementUi = (LabelElementUi) element; 
                switch(sourceProperty) {
                    case ArtifactDefinitionConstants.LABEL_ONLOAD:
                        labelElementUi.setElementUiOnLoad(functionString);
                        break;
                    case ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE:
                        labelElementUi.setElementUiOnPropertyChange(functionString);
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    /**
     * Builds the parameter grid in the property editor.
     * @param function The selected function if exists.
     * @param propertyValue The current property value.
     */
    private void createGridParameters(ArtifactDefinitionFunction function, String propertyValue) {
        if (listParameters != null && function != null) {
            lytParameters.removeAll();
            if (listParameters.size() > 0) {
                listParameters = function.getParameters();
                if (propertyValue == null || propertyValue.trim().isEmpty())
                    listParameters.forEach(param -> param.setValue(""));

                Grid<StringPair> gridParams = new Grid<>();
                gridParams.setId("grid-parameter");
                gridParams.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
                gridParams.setSelectionMode(Grid.SelectionMode.NONE);
                gridParams.setHeightByRows(true);
                gridParams.setItems(listParameters);
                Grid.Column<StringPair> key = gridParams.addColumn(StringPair::getKey).setHeader(ts.getTranslatedString("module.general.property.name"));
                Grid.Column<StringPair> editor = gridParams.addComponentColumn(param -> {
                    TextField txtValue = new TextField();
                    txtValue.setPlaceholder(ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.parameter-info"));
                    txtValue.setRequiredIndicatorVisible(true);
                    txtValue.setValue(param.getValue());
                    txtValue.setWidthFull();
                    txtValue.addValueChangeListener(e -> {
                        param.setValue(e.getValue());
                        gridParams.getDataProvider().refreshItem(param);
                    });
                    return txtValue;
                }).setHeader(ts.getTranslatedString("module.general.labels.value"));

                HeaderRow headerRow = gridParams.prependHeaderRow();
                headerRow.join(key, editor).setText(ts.getTranslatedString("module.general.labels.parameters"));
                lytParameters.add(gridParams);
            } else {
                Label lblInfo = new Label(ts.getTranslatedString("module.processeditor.editor-form-property-editor-actions.edit-propert-no-parameters"));
                lblInfo.setClassName("bold-font");
                lytParameters.add(lblInfo);
            }           
        }
    }

    /**
     * Builds the value of the attribute, function and parameters from the property value.
     * @param propertyValue The current property value.
     */
    private void loadPropertyValue(String propertyValue) {
        String [] array = propertyValue.split("\\.");
        String anAttribute = array[0];
        String aFunction = array[1];
        
        if (listAttributes != null)
            listAttributes.stream().filter(attribute -> (attribute.getName().equals(anAttribute)))
                    .forEachOrdered(attribute -> cmbAttributes.setValue(attribute));
        
        if (listFunctions != null)
            listFunctions.stream().filter(function -> (function.getName().equals(aFunction)))
                    .forEachOrdered(function -> cmbFunctions.setValue(function));
        
        LinkedList<String> params = new LinkedList<>();
        
        int count = anAttribute.length() + aFunction.length() + 2;
        if (propertyValue.length() > count) {
            String parameterValues = propertyValue.substring(count, propertyValue.length());
            String[] parameters = parameterValues.split("\\.");
            params.addAll(Arrays.asList(parameters));
        }
        
        if (cmbAttributes.getValue() != null && cmbFunctions.getValue() != null) {            
            listParameters.forEach(param -> param.setValue(""));
            if (listParameters.size() >= params.size()) {
                for (int i = 0; i < params.size(); i++)
                    listParameters.get(i).setValue(params.get(i));
            } else {
                int parameterCount = listParameters.size();
                for (int i = 0; i < params.size(); i++) {
                    if (i < parameterCount)
                        listParameters.get(i).setValue(params.get(i));
                    else
                        break;                        
                }
            }
        }
              
        btnUpdate.setEnabled(cmbAttributes.getValue() != null && cmbFunctions.getValue() != null);
    }
    
    /**
     * Builds the value of the property from the selected attribute, the function and the created parameters.
     */
    private void createFunctionString() {
        functionString = "";
        if (cmbAttributes.getValue() != null && cmbFunctions.getValue() != null) {
            functionString += cmbAttributes.getValue() +"."+cmbFunctions.getValue();
            listParameters.forEach(param -> functionString += "."+param.getValue());
        }        
    }
}