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

import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.ButtonElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.ComboBoxElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUiItem.AbstractElementUiItem;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.ElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.GridElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.HorizontalLayoutElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.LabelElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.TextFieldElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.VerticalLayoutElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUiItem.ButtonElementUiItem;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUiItem.ComboBoxElementUiItem;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUiItem.GridElementUiItem;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUiItem.HorizontalLayoutElementUiItem;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUiItem.LabelElementUiItem;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUiItem.TextFieldElementUiItem;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUiItem.VerticalLayoutElementUiItem;
import com.neotropic.kuwaiba.modules.commercial.processman.ProcessEditorModule;
import com.neotropic.kuwaiba.modules.commercial.processman.actions.AbstractVisualElementAction;
import com.neotropic.kuwaiba.modules.commercial.processman.actions.DeleteElementVisualAction;
import com.neotropic.kuwaiba.modules.commercial.processman.actions.ElementActionsRegistry;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.CheckBoxElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUiItem.CheckBoxElementUiItem;
import com.neotropic.kuwaiba.modules.commercial.processman.service.ArtifactDefinition;
import com.neotropic.kuwaiba.modules.commercial.processman.service.ArtifactDefinitionFunction;
import com.neotropic.kuwaiba.modules.commercial.processman.tools.ArtifactDefinitionConstants;
import com.neotropic.kuwaiba.modules.commercial.processman.tools.ElementOptionsPanel;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.PersistenceService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Manage artifacts, add or remove items.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@org.springframework.stereotype.Component
public class ArtifactEditorDialog extends AbstractVisualAction<Dialog> implements ActionCompletedListener {
    /**
     * Reference to the Persistence Service.
     */
    @Autowired
    private PersistenceService persistenceService;
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Element Property Editor Dialog
     */
    @Autowired
    private ElementPropertyEditorDialog elementPropertyEditorDialog;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private ElementActionsRegistry elementActionsRegistry;
    /**
     * The visual action to delete a form element.
     */
    @Autowired
    private DeleteElementVisualAction deleteElementVisualAction;
    /**
     * Dialog to edit the artifact
     */
    private ConfirmDialog wdwEditor;
    /**
     * Object to save the current form
     */
    private String artifactUuid;
    /**
     * XML
     */
    private XMLOutputFactory xmlof;
    private XMLEventWriter xmlew;
    private XMLEventFactory xmlef;
    /**
     * Object to save the selected component
     */
    private Component component;
    private Component clickedComponent;
    /**
     * Object to save the selected elementUi
     */
    private ElementUi elementUi;
    /**
     * Command to add the form
     */
    private Command commandFormArtifact;
    /**
     * The right-side panel displaying the property sheet of the selected element plus some other options.
     */
    private VerticalLayout lytDetailsPanel;
    /**
     * Object to save the function list
     */
    private List<ArtifactDefinitionFunction> listFunctions;
    private String parameterNames;
    /**
     * Artifact path
     */
    private String artifactPath;
    /**
     * Process Id
     */
    private String processId;
    
    public ArtifactEditorDialog() {
        super(ProcessEditorModule.MODULE_ID);
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            if (ev.getActionResponse() != null && (ev.getActionResponse().containsKey(ActionResponse.ActionType.REMOVE)))
                lytDetailsPanel.removeAll();
            
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open(); 
    }

    @Override
    public void clearListeners() {
        this.deleteElementVisualAction.unregisterListener(this);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("artifact")) {
            ArtifactDefinition artifact = (ArtifactDefinition) parameters.get("artifact");
            artifactUuid = artifact.getId();
            processId = (String) parameters.get("processId");
            
            this.deleteElementVisualAction.registerActionCompletedLister(this);
            commandFormArtifact = (Command) parameters.get("commandFormArtifact");
            listFunctions = new ArrayList<>();
            
            Label lblTitle = new Label();
            wdwEditor = new ConfirmDialog(ts, ts.getTranslatedString("module.general.messages.save"));
            wdwEditor.setCloseOnOutsideClick(false);
            wdwEditor.setCloseOnEsc(false);
            wdwEditor.setDraggable(true);
            wdwEditor.setResizable(true);
            wdwEditor.setWidthFull();
            wdwEditor.setHeight("87%");

            HorizontalLayout lytContent = new HorizontalLayout();
            lytContent.setWidthFull();

            switch (artifact.getType()) {
                case 1:
                    lblTitle.setText(ts.getTranslatedString("module.processeditor.editor-form-type-artifact-name"));
                    lblTitle.addClassName("lbl-bold");
                    wdwEditor.setHeader(new H5(lblTitle));

                    // First Layout
                    VerticalLayout first = new VerticalLayout();
                    first.setWidth("15%");

                    Label lblFirst = new Label(ts.getTranslatedString("module.processeditor.editor-form-type-artifact-panel-options"));
                    first.add(lblFirst);

                    // Init Containers
                    Accordion accordionContainer = new Accordion();
                    accordionContainer.setWidthFull();
                    accordionContainer.close();

                    VerticalLayout lytContainer = new VerticalLayout();

                    HorizontalLayoutElementUiItem horizontalLayout = new HorizontalLayoutElementUiItem(
                            ts.getTranslatedString("module.processeditor.editor-form-container-label-horizontal"));
                    DragSource<HorizontalLayoutElementUiItem> dragHorizontalLayout = DragSource.create(horizontalLayout);
                    dragHorizontalLayout.setDraggable(true);
                    lytContainer.add(horizontalLayout);

                    VerticalLayoutElementUiItem verticalLayout = new VerticalLayoutElementUiItem(
                            ts.getTranslatedString("module.processeditor.editor-form-container-label-vertical"));
                    DragSource<VerticalLayoutElementUiItem> dragVerticalLayout = DragSource.create(verticalLayout);
                    dragVerticalLayout.setDraggable(true);
                    lytContainer.add(verticalLayout);

                    accordionContainer.add(ts.getTranslatedString("module.processeditor.editor-form-type-artifact-panel-container-option"), lytContainer);
                    first.add(accordionContainer);
                    // End Containers

                    // Init Controls
                    Accordion accordionControls = new Accordion();
                    accordionControls.setWidthFull();
                    accordionControls.close();

                    VerticalLayout lytControls = new VerticalLayout();

                    LabelElementUiItem label = new LabelElementUiItem(
                            ts.getTranslatedString("module.processeditor.editor-form-control-label-label"));
                    DragSource<LabelElementUiItem> dragLabel = DragSource.create(label);
                    dragLabel.setDraggable(true);
                    lytControls.add(label);

                    TextFieldElementUiItem textField = new TextFieldElementUiItem(
                            ts.getTranslatedString("module.processeditor.editor-form-control-label-textfield"));
                    DragSource<TextFieldElementUiItem> dragTextField = DragSource.create(textField);
                    dragTextField.setDraggable(true);
                    lytControls.add(textField);

                    ComboBoxElementUiItem comboBox = new ComboBoxElementUiItem(
                            ts.getTranslatedString("module.processeditor.editor-form-control-label-combobox"));
                    DragSource<ComboBoxElementUiItem> dragComboBox = DragSource.create(comboBox);
                    dragComboBox.setDraggable(true);
                    lytControls.add(comboBox);

                    CheckBoxElementUiItem checkBox = new CheckBoxElementUiItem(
                            ts.getTranslatedString("module.processeditor.editor-form-control-label-checkbox"));
                    DragSource<CheckBoxElementUiItem> dragCheckBox = DragSource.create(checkBox);
                    dragCheckBox.setDraggable(true);
                    lytControls.add(checkBox);

                    ButtonElementUiItem button = new ButtonElementUiItem(
                            ts.getTranslatedString("module.processeditor.editor-form-control-label-button"));
                    DragSource<ButtonElementUiItem> dragButton = DragSource.create(button);
                    dragButton.setDraggable(true);
                    lytControls.add(button);

                    GridElementUiItem grid = new GridElementUiItem(
                            ts.getTranslatedString("module.processeditor.editor-form-control-label-grid"));
                    DragSource<GridElementUiItem> dragGrid = DragSource.create(grid);
                    dragGrid.setDraggable(true);
                    lytControls.add(grid);

                    accordionControls.add(ts.getTranslatedString("module.processeditor.editor-form-type-artifact-panel-control-option"), lytControls);
                    first.add(accordionControls);
                    // End Controls

                    // Second Layout
                    VerticalLayout second = new VerticalLayout();
                    second.setWidth("55%");

                    Label lblSecond = new Label(ts.getTranslatedString("module.processeditor.editor-form-type-artifact-header"));
                    second.add(lblSecond);

                    DropTarget<VerticalLayout> dropTarget = DropTarget.create(second);
                    dropTarget.setDropEffect(DropEffect.COPY);

                    dropTarget.addDropListener(event -> {
                        if (event.getDropEffect() == DropEffect.COPY) {
                            component = event.getDragSourceComponent().get();

                            if (component instanceof AbstractElementUiItem) {
                                elementUi = ((AbstractElementUiItem) component).create();

                                if (elementUi instanceof HorizontalLayoutElementUi || elementUi instanceof VerticalLayoutElementUi)
                                    addContainerUi(second);
                                else
                                    addElementUi(second);
                            }
                        }
                    });

                    lytDetailsPanel = new VerticalLayout();
                    lytDetailsPanel.setWidth("30%");
                    lytDetailsPanel.setId("lytDetails");

                    lytContent.add(first, second, lytDetailsPanel);
                    lytContent.setSpacing(true);

                    wdwEditor.getBtnConfirm().addClickListener(event -> {
                        saveForm(second);
                    });
                    artifactPath = "";
                    loadForm(second);
                    break;
                /*case 2:
                    throw new UnsupportedOperationException("Not supported yet.");
                case 3:
                    throw new UnsupportedOperationException("Not supported yet.");*/
                default:
                    break;
            }

            wdwEditor.add(lytContent);
            return wdwEditor;
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "artifact")));
    }

    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Load a form if it exists.
     * @param layout Contains the elements.
     */
    private void loadForm(VerticalLayout layout) {
        String processEnginePath = String.valueOf(persistenceService.getApplicationProperties().get("processEnginePath")); //NOI18N
        String processDefPath = processEnginePath + "/form/definitions"; //NOI18N
        
        File file = new File(processDefPath + "/" + processId + "/" + artifactUuid + ".xml");
        if (file.exists()) {
            try {
                Document domDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
                domDocument.normalize();
               
                Element root = domDocument.getDocumentElement();
                loadForm(root, (Component) layout);     
                artifactPath = processDefPath + "/" + processId + "/" + artifactUuid + ".xml";
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                        ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }
    
    /**
     * Load the form.
     * @param element Root element.
     * @param container Contains the elements.
     */
    private void loadForm(Element root, Component container) {
        NodeList list = root.getChildNodes();
        if (list.getLength() > 0) {
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element node = (Element) list.item(i);
                    if (node.getNodeName().equals(ArtifactDefinitionConstants.LABEL_FORM))
                        loadElementUi(node, container);
                    if (node.getNodeName().equals(ArtifactDefinitionConstants.LABEL_SCRIPT))
                        loadFunctions(node);
                }
            }
        }
    }
        
    /**
     * Load the elements to the UI.
     * @param element Element to upload.
     * @param container Contains the elements.
     */
    private void loadElementUi(Element element, Component container) {
        NodeList list = element.getChildNodes();
        if (list.getLength() > 0) {
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element node = (Element) list.item(i);
                    switch (node.getNodeName()) {
                        case ArtifactDefinitionConstants.LABEL_HORIZONTAL_LAYOUT: {
                            HorizontalLayoutElementUi horizontalLayoutElementUi = new HorizontalLayoutElementUi();
                            horizontalLayoutElementUi.getStyle().set("border", "1px solid #E9E9E9");

                            DropTarget<HorizontalLayoutElementUi> dropHorizontal = DropTarget.create(horizontalLayoutElementUi);
                            dropHorizontal.setDropEffect(DropEffect.COPY);
                            dropHorizontal.addDropListener(listener -> {
                                if (listener.getDropEffect() == DropEffect.COPY) {
                                    component = listener.getDragSourceComponent().get();

                                    if (component instanceof AbstractElementUiItem) {
                                        elementUi = ((AbstractElementUiItem) component).create();

                                        if (elementUi instanceof Component) {
                                            if (elementUi instanceof HorizontalLayoutElementUi
                                                    || elementUi instanceof VerticalLayoutElementUi)
                                                addContainerUi(horizontalLayoutElementUi);
                                            else
                                                addElementUi(horizontalLayoutElementUi);
                                        }
                                    }
                                }
                            });

                            loadElementAttributes(node, horizontalLayoutElementUi);
                            horizontalLayoutElementUi.setWidth(horizontalLayoutElementUi.getElementUiWidth());
                            horizontalLayoutElementUi.setHeight(horizontalLayoutElementUi.getElementUiHeight());

                            if (container instanceof HorizontalLayout) {
                                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                                horizontalLayout.add(horizontalLayoutElementUi);
                            } else if (container instanceof VerticalLayout) {
                                VerticalLayout verticalLayout = (VerticalLayout) container;
                                verticalLayout.add(horizontalLayoutElementUi);
                            }

                            NodeList children = node.getChildNodes();
                            if (children.getLength() > 0)
                                loadElementUi(node, horizontalLayoutElementUi);
                            break;
                        }
                        case ArtifactDefinitionConstants.LABEL_VERTICAL_LAYOUT: {
                            VerticalLayoutElementUi verticalLayoutElementUi = new VerticalLayoutElementUi();
                            verticalLayoutElementUi.getStyle().set("border", "1px solid #E9E9E9");

                            DropTarget<VerticalLayout> dropVertical = DropTarget.create(verticalLayoutElementUi);
                            dropVertical.setDropEffect(DropEffect.COPY);
                            dropVertical.addDropListener(listener -> {
                                if (listener.getDropEffect() == DropEffect.COPY) {
                                    component = listener.getDragSourceComponent().get();

                                    if (component instanceof AbstractElementUiItem) {
                                        elementUi = ((AbstractElementUiItem) component).create();

                                        if (elementUi instanceof Component) {
                                            if (elementUi instanceof HorizontalLayoutElementUi
                                                    || elementUi instanceof VerticalLayoutElementUi)
                                                addContainerUi(verticalLayoutElementUi);
                                            else
                                                addElementUi(verticalLayoutElementUi);
                                        }
                                    }
                                }
                            });

                            loadElementAttributes(node, verticalLayoutElementUi);
                            verticalLayoutElementUi.setWidth(verticalLayoutElementUi.getElementUiWidth());
                            verticalLayoutElementUi.setHeight(verticalLayoutElementUi.getElementUiHeight());

                            if (container instanceof HorizontalLayout) {
                                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                                horizontalLayout.add(verticalLayoutElementUi);
                            } else if (container instanceof VerticalLayout) {
                                VerticalLayout verticalLayout = (VerticalLayout) container;
                                verticalLayout.add(verticalLayoutElementUi);
                            }

                            NodeList children = node.getChildNodes();
                            if (children.getLength() > 0)
                                loadElementUi(node, verticalLayoutElementUi);
                            break;
                        }
                        case ArtifactDefinitionConstants.LABEL_LABEL:
                            LabelElementUi labelElementUi = new LabelElementUi();
                            loadElementAttributes(node, labelElementUi);
                            labelElementUi.setWidth(labelElementUi.getElementUiWidth());
                            labelElementUi.setHeight(labelElementUi.getElementUiHeight());

                            if (container instanceof HorizontalLayout) {
                                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                                horizontalLayout.add(labelElementUi);
                            } else if (container instanceof VerticalLayout) {
                                VerticalLayout verticalLayout = (VerticalLayout) container;
                                verticalLayout.add(labelElementUi);
                            }
                            break;
                        case ArtifactDefinitionConstants.LABEL_TEXTFIELD:
                            TextFieldElementUi textFieldElementUi = new TextFieldElementUi();
                            loadElementAttributes(node, textFieldElementUi);
                            textFieldElementUi.setWidth(textFieldElementUi.getElementUiWidth());
                            textFieldElementUi.setHeight(textFieldElementUi.getElementUiHeight());

                            if (container instanceof HorizontalLayout) {
                                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                                horizontalLayout.add(textFieldElementUi);
                            } else if (container instanceof VerticalLayout) {
                                VerticalLayout verticalLayout = (VerticalLayout) container;
                                verticalLayout.add(textFieldElementUi);
                            }
                            break;
                        case ArtifactDefinitionConstants.LABEL_COMBOBOX:
                            ComboBoxElementUi comboBoxElementUi = new ComboBoxElementUi();
                            loadElementAttributes(node, comboBoxElementUi);
                            comboBoxElementUi.setWidth(comboBoxElementUi.getElementUiWidth());
                            comboBoxElementUi.setHeight(comboBoxElementUi.getElementUiHeight());

                            if (container instanceof HorizontalLayout) {
                                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                                horizontalLayout.add(comboBoxElementUi);
                            } else if (container instanceof VerticalLayout) {
                                VerticalLayout verticalLayout = (VerticalLayout) container;
                                verticalLayout.add(comboBoxElementUi);
                            }
                            break;
                        case ArtifactDefinitionConstants.LABEL_CHECKBOX:
                            CheckBoxElementUi checkBoxElementUi = new CheckBoxElementUi();
                            loadElementAttributes(node, checkBoxElementUi);
                            checkBoxElementUi.setWidth(checkBoxElementUi.getElementUiWidth());
                            checkBoxElementUi.setHeight(checkBoxElementUi.getElementUiHeight());

                            if (container instanceof HorizontalLayout) {
                                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                                horizontalLayout.add(checkBoxElementUi);
                            } else if (container instanceof VerticalLayout) {
                                VerticalLayout verticalLayout = (VerticalLayout) container;
                                verticalLayout.add(checkBoxElementUi);
                            }
                            break;
                        case ArtifactDefinitionConstants.LABEL_BUTTON:
                            ButtonElementUi buttonElementUi = new ButtonElementUi();
                            loadElementAttributes(node, buttonElementUi);
                            buttonElementUi.setWidth(buttonElementUi.getElementUiWidth());
                            buttonElementUi.setHeight(buttonElementUi.getElementUiHeight());

                            if (container instanceof HorizontalLayout) {
                                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                                horizontalLayout.add(buttonElementUi);
                            } else if (container instanceof VerticalLayout) {
                                VerticalLayout verticalLayout = (VerticalLayout) container;
                                verticalLayout.add(buttonElementUi);
                            }
                            break;
                        case ArtifactDefinitionConstants.LABEL_GRID:
                            GridElementUi gridElementUi = new GridElementUi();
                            loadElementAttributes(node, gridElementUi);
                            gridElementUi.setWidth(gridElementUi.getElementUiWidth());
                            gridElementUi.setHeight(gridElementUi.getElementUiHeight());

                            if (container instanceof HorizontalLayout) {
                                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                                horizontalLayout.add(gridElementUi);
                            } else if (container instanceof VerticalLayout) {
                                VerticalLayout verticalLayout = (VerticalLayout) container;
                                verticalLayout.add(gridElementUi);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }
    
    /**
     * Load element attributes.
     * @param element Element to upload.
     * @param component Element to which the attributes are loaded.
     */
    private void loadElementAttributes(Element element, Component component) {
        if (component instanceof LabelElementUi) {
            LabelElementUi labelElementUi = (LabelElementUi) component;
            // Init Attributes
            labelElementUi.setElementUiId((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ID) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ID).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ID) : "");
            labelElementUi.setElementUiWidth(element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) : "30px");
            labelElementUi.setElementUiHeight(element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) : "33px");
            if (element.getAttribute(ArtifactDefinitionConstants.LABEL_VALUE) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_VALUE).isEmpty()) {
                labelElementUi.setText(element.getAttribute(ArtifactDefinitionConstants.LABEL_VALUE));
                labelElementUi.setElementUiValue(element.getAttribute(ArtifactDefinitionConstants.LABEL_VALUE));
            }
            labelElementUi.setElementUiStyleName((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_STYLENAME) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_STYLENAME).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_STYLENAME) : "");
            labelElementUi.setElementUiAlignment((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ALIGNMENT) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ALIGNMENT).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ALIGNMENT) : "");
            labelElementUi.setElementUiOnLoad((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ONLOAD) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ONLOAD).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ONLOAD) : "");
            labelElementUi.setElementUiOnPropertyChange((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE) : "");
            // End Attributes
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(labelElementUi))
                        clickedComponent = clickedComponent.getParent().get();
                    else
                        buildDetails(labelElementUi);
                } else
                    buildDetails(labelElementUi);
            };
            labelElementUi.addEventListener(commandDetails);
        } else if (component instanceof TextFieldElementUi) {
            TextFieldElementUi textFieldElementUi = (TextFieldElementUi) component;
            // Init Attributes
            textFieldElementUi.setElementUiId((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ID) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ID).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ID) : "");
            textFieldElementUi.setElementUiWidth(element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) : "30px");
            textFieldElementUi.setElementUiHeight(element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) : "33px");
            textFieldElementUi.setElementUiValue((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_VALUE) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_VALUE).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_VALUE) : "");
            textFieldElementUi.setElementUiDataType((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_DATATYPE) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_DATATYPE).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_DATATYPE) : "");
            textFieldElementUi.setElementUiEnabled((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ENABLED) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ENABLED).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ENABLED) : "");
            textFieldElementUi.setElementUiHidden((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_HIDDEN) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_HIDDEN).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_HIDDEN) : "");
            textFieldElementUi.setElementUiMandatory((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_MANDATORY) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_MANDATORY).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_MANDATORY) : "");
            textFieldElementUi.setElementUiOnLoad((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ONLOAD) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ONLOAD).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ONLOAD) : "");
            textFieldElementUi.setElementUiOnPropertyChange((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE) : "");
            // End Attributes
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(textFieldElementUi))
                        clickedComponent = clickedComponent.getParent().get();
                    else
                        buildDetails(textFieldElementUi);
                } else
                    buildDetails(textFieldElementUi);
            };
            textFieldElementUi.addEventListener(commandDetails);
        } else if (component instanceof ButtonElementUi) {
            ButtonElementUi buttonElementUi = (ButtonElementUi) component;
            // Init Attributes
            buttonElementUi.setElementUiId((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ID) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ID).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ID) : "");
            buttonElementUi.setElementUiWidth(element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) : "30px");
            buttonElementUi.setElementUiHeight(element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) : "33px");
            buttonElementUi.setElementUiCaption((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_CAPTION) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_CAPTION).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_CAPTION) : "");
            buttonElementUi.setElementUiOnClick((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ONCLICK) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ONCLICK).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ONCLICK) : "");
            buttonElementUi.setElementUiStyleName((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_STYLENAME) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_STYLENAME).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_STYLENAME) : "");
            buttonElementUi.setElementUiOnLoad((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ONLOAD) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ONLOAD).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ONLOAD) : "");
            buttonElementUi.setElementUiOnPropertyChange((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE) : "");
            // End Attributes
            
            Command commandDetails = () -> {
                cleanBorderColor();
                if (clickedComponent != null) {
                    if (clickedComponent.getParent().get().equals(buttonElementUi))
                        clickedComponent = clickedComponent.getParent().get();
                    else
                        buildDetails(buttonElementUi);
                } else
                    buildDetails(buttonElementUi);
            };
            buttonElementUi.addEventListener(commandDetails);
        } else if (component instanceof CheckBoxElementUi) {
            CheckBoxElementUi checkBoxElementUi = (CheckBoxElementUi) component;
            // Init Attributes
            checkBoxElementUi.setElementUiId((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ID) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ID).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ID) : "");
            checkBoxElementUi.setElementUiWidth(element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) : "30px");
            checkBoxElementUi.setElementUiHeight(element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) : "33px");
            checkBoxElementUi.setElementUiDataType((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_DATATYPE) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_DATATYPE).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_DATATYPE) : "");
            // End Attributes
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(checkBoxElementUi))
                        clickedComponent = clickedComponent.getParent().get();
                    else
                        buildDetails(checkBoxElementUi);
                } else
                    buildDetails(checkBoxElementUi);
            };
            checkBoxElementUi.addEventListener(commandDetails);
        } else if (component instanceof ComboBoxElementUi) {
            ComboBoxElementUi comboBoxElementUi = (ComboBoxElementUi) component;
            // Init Attributes
            comboBoxElementUi.setElementUiId((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ID) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ID).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ID) : "");
            comboBoxElementUi.setElementUiWidth(element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) : "30px");
            comboBoxElementUi.setElementUiHeight(element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) : "33px");            
            comboBoxElementUi.setElementUiShared((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_SHARED) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_SHARED).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_SHARED) : "");
            comboBoxElementUi.setElementUiOnLazyLoad((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ONLAZYLOAD) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ONLAZYLOAD).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ONLAZYLOAD) : "");
            comboBoxElementUi.setElementUiPropertyChangeListener((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_PROPERTYCHANGELISTENER) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_PROPERTYCHANGELISTENER).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_PROPERTYCHANGELISTENER) : "");
            comboBoxElementUi.setElementUiOnLoad((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ONLOAD) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ONLOAD).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ONLOAD) : "");
            comboBoxElementUi.setElementUiOnPropertyChange((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE) : "");
            comboBoxElementUi.setElementUiDataType((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_DATATYPE) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_DATATYPE).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_DATATYPE) : "");
            // End Attributes
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(comboBoxElementUi))
                        clickedComponent = clickedComponent.getParent().get();
                    else
                        buildDetails(comboBoxElementUi);
                } else
                    buildDetails(comboBoxElementUi);
            };
            comboBoxElementUi.addEventListener(commandDetails);
        } else if (component instanceof GridElementUi) {
            GridElementUi gridElementUi = (GridElementUi) component;
            // Init Attributes
            gridElementUi.setElementUiId((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ID) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ID).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ID) : "");
            gridElementUi.setElementUiWidth(element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) : "30px");
            gridElementUi.setElementUiHeight(element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) : "40px");
            gridElementUi.setElementUiOnLoad((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ONLOAD) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ONLOAD).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ONLOAD) : "");
            gridElementUi.setElementUiShared((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_SHARED) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_SHARED).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_SHARED) : "");
            gridElementUi.setElementUiColumns((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_COLUMNS) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_COLUMNS).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_COLUMNS) : "");
            gridElementUi.setElementUiRows((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ROWS) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ROWS).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ROWS) : "");
            gridElementUi.setElementUiAlignment((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ALIGNMENT) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ALIGNMENT).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ALIGNMENT) : "");
            gridElementUi.setElementUiSave((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_SAVE) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_SAVE).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_SAVE) : "");
            gridElementUi.setElementUiDataType((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_DATATYPE) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_DATATYPE).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_DATATYPE) : "");
            // End Attributes
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(gridElementUi))
                        clickedComponent = clickedComponent.getParent().get();
                    else
                        buildDetails(gridElementUi);
                } else
                    buildDetails(gridElementUi);
            };
            gridElementUi.addEventListener(commandDetails);
        } else if (component instanceof HorizontalLayoutElementUi) {
            HorizontalLayoutElementUi horizontalLayoutElementUi = (HorizontalLayoutElementUi) component;
            // Init Attributes
            horizontalLayoutElementUi.setElementUiId((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ID) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ID).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ID) : "");
            horizontalLayoutElementUi.setElementUiWidth(element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) : "30px");
            horizontalLayoutElementUi.setElementUiHeight(element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) : "40px");
            horizontalLayoutElementUi.setElementUiAlignment((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ALIGNMENT) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ALIGNMENT).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ALIGNMENT) : "");
            // End Attributes
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(horizontalLayoutElementUi)) {
                        clickedComponent.getElement().getStyle().set("border", "1px solid #4FBDDD");
                        clickedComponent = clickedComponent.getParent().get();
                    } else
                        buildDetails(horizontalLayoutElementUi);
                } else
                    buildDetails(horizontalLayoutElementUi);
            };
            horizontalLayoutElementUi.addEventListener(commandDetails);
        } else if (component instanceof VerticalLayoutElementUi) {
            VerticalLayoutElementUi verticalLayoutElementUi = (VerticalLayoutElementUi) component;
            // Init Attributes
            verticalLayoutElementUi.setElementUiId((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ID) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ID).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ID) : "");
            verticalLayoutElementUi.setElementUiWidth(element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_WIDTH) : "30px");
            verticalLayoutElementUi.setElementUiHeight(element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT).isEmpty()
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_HEIGHT) : "40px");
            verticalLayoutElementUi.setElementUiAlignment((element.getAttribute(
                    ArtifactDefinitionConstants.LABEL_ALIGNMENT) != null
                    && !element.getAttribute(ArtifactDefinitionConstants.LABEL_ALIGNMENT).isEmpty())
                    ? element.getAttribute(ArtifactDefinitionConstants.LABEL_ALIGNMENT) : "");
            // End Attributes
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(verticalLayoutElementUi)) {
                        clickedComponent.getElement().getStyle().set("border", "1px solid #4FBDDD");
                        clickedComponent = clickedComponent.getParent().get();
                    } else
                        buildDetails(verticalLayoutElementUi);
                } else
                    buildDetails(verticalLayoutElementUi);
            };
            verticalLayoutElementUi.addEventListener(commandDetails);
        }
    }    
    
    private void loadFunctions(Element element) {
        NodeList list = element.getChildNodes();
        if (list.getLength() > 0) {
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element node = (Element) list.item(i);
                    if (node.getNodeName().equals(ArtifactDefinitionConstants.LABEL_FUNCTION)) {
                        ArtifactDefinitionFunction function = new ArtifactDefinitionFunction();
                        loadFunctionAttributes(node, function);
                        listFunctions.add(function);
                    }
                }
                    
            }
        }
    }
    
    private void loadFunctionAttributes(Element element, ArtifactDefinitionFunction function) {
        function.setType(element.getAttribute(ArtifactDefinitionConstants.LABEL_TYPE) != null
                && !element.getAttribute(ArtifactDefinitionConstants.LABEL_TYPE).isEmpty() 
                ? element.getAttribute(ArtifactDefinitionConstants.LABEL_TYPE) : "");
        function.setName(element.getAttribute(ArtifactDefinitionConstants.LABEL_NAME) != null
                && !element.getAttribute(ArtifactDefinitionConstants.LABEL_NAME).isEmpty() 
                ? element.getAttribute(ArtifactDefinitionConstants.LABEL_NAME) : "");
        function.setReturns(element.getAttribute(ArtifactDefinitionConstants.LABEL_RETURN) != null
                && !element.getAttribute(ArtifactDefinitionConstants.LABEL_RETURN).isEmpty() 
                ? element.getAttribute(ArtifactDefinitionConstants.LABEL_RETURN) : "");
        function.setValue(element.getTextContent() != null
                && !element.getTextContent().isEmpty()
                ? element.getTextContent() : "");
        
        List<StringPair> parameters = new ArrayList<>();
        if (element.getAttribute(ArtifactDefinitionConstants.LABEL_PARAMETERNAMES) != null && 
                !element.getAttribute(ArtifactDefinitionConstants.LABEL_PARAMETERNAMES).isEmpty()) {
            String [] parametersNames = element.getAttribute(ArtifactDefinitionConstants.LABEL_PARAMETERNAMES).split(" ");
            for (String parameterName : parametersNames) {
                StringPair parameter = new StringPair(parameterName, "");
                parameters.add(parameter);
            }
        }
        function.setParameters(parameters);
    }
     
    /**
     * Save or update a form.
     * @param layout Contains the elements.
     */
    private void saveForm(VerticalLayout layout) {
        byte[] structure = getAsXML(layout);
        String processEnginePath = String.valueOf(persistenceService.getApplicationProperties().get("processEnginePath")); //NOI18N
        String processDefPath = processEnginePath + "/form/definitions"; //NOI18N
        
        File file = new File(processDefPath + "/" + processId);
        if (file.exists()) {
            File fileArtifact = new File(artifactPath);
            if (fileArtifact.exists()) {
                try (FileOutputStream fos = new FileOutputStream(fileArtifact.getPath())) {
                    fos.write(structure);

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.processeditor.editor-form-type-artifact-name.update-success"),
                            AbstractNotification.NotificationType.INFO, ts).open();

                } catch (IOException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            } else {
                try (FileOutputStream fos = new FileOutputStream(file.getPath() + "/" + artifactUuid + ".xml")) {
                    fos.write(structure);

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.processeditor.editor-form-type-artifact-name.save-success"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                    commandFormArtifact.execute();
                    
                    artifactPath = file.getPath() + "/" + artifactUuid + ".xml";
                } catch (IOException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
        } else {
            if (file.mkdirs()) {
                try (FileOutputStream fos = new FileOutputStream(file.getPath() + "/" + artifactUuid + ".xml")) {
                    fos.write(structure);

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.processeditor.editor-form-type-artifact-name.save-success"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                    commandFormArtifact.execute();

                    artifactPath = file.getPath() + "/" + artifactUuid + ".xml";
                } catch (IOException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
        }
    }
    
    /**
     * Get the current form as a byte array with the XML document.
     * @param layout Contains the elements.
     * @return The byte array with the form.
     */
    public byte[] getAsXML(VerticalLayout layout) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            xmlof = XMLOutputFactory.newInstance();
            xmlew = xmlof.createXMLEventWriter(baos);
            xmlef = XMLEventFactory.newInstance();

            // init form definiton
            xmlew.add(xmlef.createStartElement(new QName(ArtifactDefinitionConstants.LABEL_FORMDEFINITION), null, null));
            // init form
            xmlew.add(xmlef.createStartElement(new QName(ArtifactDefinitionConstants.LABEL_FORM), null, null));
            getXmlElements(layout);
            xmlew.add(xmlef.createEndElement(new QName(ArtifactDefinitionConstants.LABEL_FORM), null));
            // end form
            // init script
            xmlew.add(xmlef.createStartElement(new QName(ArtifactDefinitionConstants.LABEL_SCRIPT), null, null));
            getXmlFunctions();
            xmlew.add(xmlef.createEndElement(new QName(ArtifactDefinitionConstants.LABEL_SCRIPT), null));
            // end script
            xmlew.add(xmlef.createEndElement(new QName(ArtifactDefinitionConstants.LABEL_FORMDEFINITION), null));                        
            // end form definition
            
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
            return new byte[0];
        }
    }

    /**
     * Get the current form elements as a byte array with the XML document.
     * @param container Contains the elements.
     */
    private void getXmlElements(Component container) {
        container.getChildren().forEach(child -> {
            try {
                if (child instanceof HorizontalLayoutElementUi) {
                    // init horizontalLayout
                    xmlew.add(xmlef.createStartElement(new QName(ArtifactDefinitionConstants.LABEL_HORIZONTAL_LAYOUT), null, null));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ID),
                            ((HorizontalLayoutElementUi) child).getElementUiId() != null && !((HorizontalLayoutElementUi) child).getElementUiId().isEmpty()
                            ? ((HorizontalLayoutElementUi) child).getElementUiId() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_WIDTH),
                            ((HorizontalLayoutElementUi) child).getElementUiWidth() != null && !((HorizontalLayoutElementUi) child).getElementUiWidth().isEmpty()
                            ? ((HorizontalLayoutElementUi) child).getElementUiWidth() : "30px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_HEIGHT),
                            ((HorizontalLayoutElementUi) child).getElementUiHeight() != null && !((HorizontalLayoutElementUi) child).getElementUiHeight().isEmpty()
                            ? ((HorizontalLayoutElementUi) child).getElementUiHeight() : "40px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ALIGNMENT),
                            ((HorizontalLayoutElementUi) child).getElementUiAlignment() != null && !((HorizontalLayoutElementUi) child).getElementUiAlignment().isEmpty()
                            ? ((HorizontalLayoutElementUi) child).getElementUiAlignment() : ""));

                    if (child.getChildren().count() > 0)
                        getXmlElements(child);

                    xmlew.add(xmlef.createEndElement(new QName(ArtifactDefinitionConstants.LABEL_HORIZONTAL_LAYOUT), null));
                    // end horizontalLayout
                } else if (child instanceof VerticalLayoutElementUi) {
                    // init verticalLayout
                    xmlew.add(xmlef.createStartElement(new QName(ArtifactDefinitionConstants.LABEL_VERTICAL_LAYOUT), null, null));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ID),
                            ((VerticalLayoutElementUi) child).getElementUiId() != null && !((VerticalLayoutElementUi) child).getElementUiId().isEmpty()
                            ? ((VerticalLayoutElementUi) child).getElementUiId() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_WIDTH),
                            ((VerticalLayoutElementUi) child).getElementUiWidth() != null && !((VerticalLayoutElementUi) child).getElementUiWidth().isEmpty()
                            ? ((VerticalLayoutElementUi) child).getElementUiWidth() : "30px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_HEIGHT),
                            ((VerticalLayoutElementUi) child).getElementUiHeight() != null && !((VerticalLayoutElementUi) child).getElementUiHeight().isEmpty()
                            ? ((VerticalLayoutElementUi) child).getElementUiHeight() : "40px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ALIGNMENT),
                            ((VerticalLayoutElementUi) child).getElementUiAlignment() != null && !((VerticalLayoutElementUi) child).getElementUiAlignment().isEmpty()
                            ? ((VerticalLayoutElementUi) child).getElementUiAlignment() : ""));

                    if (child.getChildren().count() > 0)
                        getXmlElements(child);

                    xmlew.add(xmlef.createEndElement(new QName(ArtifactDefinitionConstants.LABEL_VERTICAL_LAYOUT), null));
                    // end verticalLayout
                } else if (child instanceof LabelElementUi) {
                    // init label
                    xmlew.add(xmlef.createStartElement(new QName(ArtifactDefinitionConstants.LABEL_LABEL), null, null));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ID),
                            ((LabelElementUi) child).getElementUiId() != null && !((LabelElementUi) child).getElementUiId().isEmpty()
                            ? ((LabelElementUi) child).getElementUiId() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_WIDTH),
                            ((LabelElementUi) child).getElementUiWidth()!= null && !((LabelElementUi) child).getElementUiWidth().isEmpty()
                            ? ((LabelElementUi) child).getElementUiWidth() : "30px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_HEIGHT),
                            ((LabelElementUi) child).getElementUiHeight()!= null && !((LabelElementUi) child).getElementUiHeight().isEmpty()
                            ? ((LabelElementUi) child).getElementUiHeight() : "33px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_VALUE),
                            ((LabelElementUi) child).getElementUiValue() != null && !((LabelElementUi) child).getElementUiValue().isEmpty()
                            ? ((LabelElementUi) child).getElementUiValue() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_STYLENAME),
                            ((LabelElementUi) child).getElementUiStyleName() != null && !((LabelElementUi) child).getElementUiStyleName().isEmpty()
                            ? ((LabelElementUi) child).getElementUiStyleName() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ALIGNMENT),
                            ((LabelElementUi) child).getElementUiAlignment() != null && !((LabelElementUi) child).getElementUiAlignment().isEmpty()
                            ? ((LabelElementUi) child).getElementUiAlignment() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ONLOAD),
                            ((LabelElementUi) child).getElementUiOnLoad() != null && !((LabelElementUi) child).getElementUiOnLoad().isEmpty()
                            ? ((LabelElementUi) child).getElementUiOnLoad() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE),
                            ((LabelElementUi) child).getElementUiOnPropertyChange() != null && !((LabelElementUi) child).getElementUiOnPropertyChange().isEmpty()
                            ? ((LabelElementUi) child).getElementUiOnPropertyChange() : ""));
                    xmlew.add(xmlef.createEndElement(new QName(ArtifactDefinitionConstants.LABEL_LABEL), null));
                    // end label
                } else if (child instanceof TextFieldElementUi) {
                    // init textField
                    xmlew.add(xmlef.createStartElement(new QName(ArtifactDefinitionConstants.LABEL_TEXTFIELD), null, null));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ID),
                            ((TextFieldElementUi) child).getElementUiId() != null && !((TextFieldElementUi) child).getElementUiId().isEmpty()
                            ? ((TextFieldElementUi) child).getElementUiId() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_WIDTH),
                            ((TextFieldElementUi) child).getElementUiWidth() != null && !((TextFieldElementUi) child).getElementUiWidth().isEmpty()
                            ? ((TextFieldElementUi) child).getElementUiWidth() : "30px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_HEIGHT),
                            ((TextFieldElementUi) child).getElementUiHeight() != null && !((TextFieldElementUi) child).getElementUiHeight().isEmpty()
                            ? ((TextFieldElementUi) child).getElementUiHeight() : "33px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_VALUE),
                            ((TextFieldElementUi) child).getElementUiValue() != null && !((TextFieldElementUi) child).getElementUiValue().isEmpty()
                            ? ((TextFieldElementUi) child).getElementUiValue() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ENABLED),
                            ((TextFieldElementUi) child).getElementUiEnabled() != null && !((TextFieldElementUi) child).getElementUiEnabled().isEmpty()
                            ? ((TextFieldElementUi) child).getElementUiEnabled() : "false"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ONLOAD),
                            ((TextFieldElementUi) child).getElementUiOnLoad() != null && !((TextFieldElementUi) child).getElementUiOnLoad().isEmpty()
                            ? ((TextFieldElementUi) child).getElementUiOnLoad() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_DATATYPE),
                            ((TextFieldElementUi) child).getElementUiDataType() != null && !((TextFieldElementUi) child).getElementUiDataType().isEmpty()
                            ? ((TextFieldElementUi) child).getElementUiDataType() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_HIDDEN),
                            ((TextFieldElementUi) child).getElementUiHidden() != null && !((TextFieldElementUi) child).getElementUiHidden().isEmpty()
                            ? ((TextFieldElementUi) child).getElementUiHidden() : "false"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE),
                            ((TextFieldElementUi) child).getElementUiOnPropertyChange() != null && !((TextFieldElementUi) child).getElementUiOnPropertyChange().isEmpty()
                            ? ((TextFieldElementUi) child).getElementUiOnPropertyChange() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_MANDATORY),
                            ((TextFieldElementUi) child).getElementUiMandatory() != null && !((TextFieldElementUi) child).getElementUiMandatory().isEmpty()
                            ? ((TextFieldElementUi) child).getElementUiMandatory() : "false"));
                    xmlew.add(xmlef.createEndElement(new QName(ArtifactDefinitionConstants.LABEL_TEXTFIELD), null));
                    // end textField
                } else if (child instanceof ComboBoxElementUi) {
                    // init comboBox
                    xmlew.add(xmlef.createStartElement(new QName(ArtifactDefinitionConstants.LABEL_COMBOBOX), null, null));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ID),
                            ((ComboBoxElementUi) child).getElementUiId() != null && !((ComboBoxElementUi) child).getElementUiId().isEmpty()
                            ? ((ComboBoxElementUi) child).getElementUiId() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_WIDTH),
                            ((ComboBoxElementUi) child).getElementUiWidth() != null && !((ComboBoxElementUi) child).getElementUiWidth().isEmpty()
                            ? ((ComboBoxElementUi) child).getElementUiWidth() : "30px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_HEIGHT),
                            ((ComboBoxElementUi) child).getElementUiHeight() != null && !((ComboBoxElementUi) child).getElementUiHeight().isEmpty()
                            ? ((ComboBoxElementUi) child).getElementUiHeight() : "33px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_VALUE),
                            ((ComboBoxElementUi) child).getElementUiValue()!= null && !((ComboBoxElementUi) child).getElementUiValue().isEmpty()
                            ? ((ComboBoxElementUi) child).getElementUiValue() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ITEMS),
                            ((ComboBoxElementUi) child).getElementUiItems()!= null && !((ComboBoxElementUi) child).getElementUiItems().isEmpty()
                            ? ((ComboBoxElementUi) child).getElementUiItems() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_SHARED),
                            ((ComboBoxElementUi) child).getElementUiShared() != null && !((ComboBoxElementUi) child).getElementUiShared().isEmpty()
                            ? ((ComboBoxElementUi) child).getElementUiShared() : "false"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ONLAZYLOAD),
                            ((ComboBoxElementUi) child).getElementUiOnLazyLoad() != null && !((ComboBoxElementUi) child).getElementUiOnLazyLoad().isEmpty()
                            ? ((ComboBoxElementUi) child).getElementUiOnLazyLoad() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ONLOAD),
                            ((ComboBoxElementUi) child).getElementUiOnLoad() != null && !((ComboBoxElementUi) child).getElementUiOnLoad().isEmpty()
                            ? ((ComboBoxElementUi) child).getElementUiOnLoad() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE),
                            ((ComboBoxElementUi) child).getElementUiOnPropertyChange() != null && !((ComboBoxElementUi) child).getElementUiOnPropertyChange().isEmpty()
                            ? ((ComboBoxElementUi) child).getElementUiOnPropertyChange() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_DATATYPE),
                            ((ComboBoxElementUi) child).getElementUiDataType() != null && !((ComboBoxElementUi) child).getElementUiDataType().isEmpty()
                            ? ((ComboBoxElementUi) child).getElementUiDataType() : ""));
                    xmlew.add(xmlef.createEndElement(new QName(ArtifactDefinitionConstants.LABEL_COMBOBOX), null));
                    // end comboBox
                } else if (child instanceof CheckBoxElementUi) {
                    // init checkBox
                    xmlew.add(xmlef.createStartElement(new QName(ArtifactDefinitionConstants.LABEL_CHECKBOX), null, null));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ID),
                            ((CheckBoxElementUi) child).getElementUiId() != null && !((CheckBoxElementUi) child).getElementUiId().isEmpty()
                            ? ((CheckBoxElementUi) child).getElementUiId() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_WIDTH),
                            ((CheckBoxElementUi) child).getElementUiWidth() != null && !((CheckBoxElementUi) child).getElementUiWidth().isEmpty()
                            ? ((CheckBoxElementUi) child).getElementUiWidth() : "30px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_HEIGHT),
                            ((CheckBoxElementUi) child).getElementUiHeight() != null && !((CheckBoxElementUi) child).getElementUiHeight().isEmpty()
                            ? ((CheckBoxElementUi) child).getElementUiHeight() : "33px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_DATATYPE),
                            ((CheckBoxElementUi) child).getElementUiDataType() != null && !((CheckBoxElementUi) child).getElementUiDataType().isEmpty()
                            ? ((CheckBoxElementUi) child).getElementUiDataType() : ""));
                    xmlew.add(xmlef.createEndElement(new QName(ArtifactDefinitionConstants.LABEL_CHECKBOX), null));
                    // end checkBox
                } else if (child instanceof ButtonElementUi) {
                    // init button
                    xmlew.add(xmlef.createStartElement(new QName(ArtifactDefinitionConstants.LABEL_BUTTON), null, null));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ID),
                            ((ButtonElementUi) child).getElementUiId() != null && !((ButtonElementUi) child).getElementUiId().isEmpty()
                            ? ((ButtonElementUi) child).getElementUiId() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_WIDTH),
                            ((ButtonElementUi) child).getElementUiWidth() != null && !((ButtonElementUi) child).getElementUiWidth().isEmpty()
                            ? ((ButtonElementUi) child).getElementUiWidth() : "30px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_HEIGHT),
                            ((ButtonElementUi) child).getElementUiHeight() != null && !((ButtonElementUi) child).getElementUiHeight().isEmpty()
                            ? ((ButtonElementUi) child).getElementUiHeight() : "33px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_CAPTION),
                            ((ButtonElementUi) child).getElementUiCaption() != null && !((ButtonElementUi) child).getElementUiCaption().isEmpty()
                            ? ((ButtonElementUi) child).getElementUiCaption() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ONCLICK),
                            ((ButtonElementUi) child).getElementUiOnClick() != null && !((ButtonElementUi) child).getElementUiOnClick().isEmpty()
                            ? ((ButtonElementUi) child).getElementUiOnClick() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_STYLENAME),
                            ((ButtonElementUi) child).getElementUiStyleName() != null && !((ButtonElementUi) child).getElementUiStyleName().isEmpty()
                            ? ((ButtonElementUi) child).getElementUiStyleName() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ONLOAD),
                            ((ButtonElementUi) child).getElementUiOnLoad() != null && !((ButtonElementUi) child).getElementUiOnLoad().isEmpty()
                            ? ((ButtonElementUi) child).getElementUiOnLoad() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ONPROPERTYCHANGE),
                            ((ButtonElementUi) child).getElementUiOnPropertyChange() != null && !((ButtonElementUi) child).getElementUiOnPropertyChange().isEmpty()
                            ? ((ButtonElementUi) child).getElementUiOnPropertyChange() : ""));
                    xmlew.add(xmlef.createEndElement(new QName(ArtifactDefinitionConstants.LABEL_BUTTON), null));
                    // end button
                } else if (child instanceof GridElementUi) {
                    // init grid
                    xmlew.add(xmlef.createStartElement(new QName(ArtifactDefinitionConstants.LABEL_GRID), null, null));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ID),
                            ((GridElementUi) child).getElementUiId() != null && !((GridElementUi) child).getElementUiId().isEmpty()
                            ? ((GridElementUi) child).getElementUiId() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_WIDTH),
                            ((GridElementUi) child).getElementUiWidth() != null && !((GridElementUi) child).getElementUiWidth().isEmpty()
                            ? ((GridElementUi) child).getElementUiWidth() : "30px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_HEIGHT),
                            ((GridElementUi) child).getElementUiHeight() != null && !((GridElementUi) child).getElementUiHeight().isEmpty()
                            ? ((GridElementUi) child).getElementUiHeight() : "33px"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ONLOAD),
                            ((GridElementUi) child).getElementUiOnLoad() != null && !((GridElementUi) child).getElementUiOnLoad().isEmpty()
                            ? ((GridElementUi) child).getElementUiOnLoad() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_SHARED),
                            ((GridElementUi) child).getElementUiShared() != null && !((GridElementUi) child).getElementUiShared().isEmpty()
                            ? ((GridElementUi) child).getElementUiShared() : "false"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_COLUMNS),
                            ((GridElementUi) child).getElementUiColumns() != null && !((GridElementUi) child).getElementUiColumns().isEmpty()
                            ? ((GridElementUi) child).getElementUiColumns() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ROWS),
                            ((GridElementUi) child).getElementUiRows() != null && !((GridElementUi) child).getElementUiRows().isEmpty()
                            ? ((GridElementUi) child).getElementUiRows() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_ALIGNMENT),
                            ((GridElementUi) child).getElementUiAlignment() != null && !((GridElementUi) child).getElementUiAlignment().isEmpty()
                            ? ((GridElementUi) child).getElementUiAlignment() : ""));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_SAVE),
                            ((GridElementUi) child).getElementUiSave() != null && !((GridElementUi) child).getElementUiSave().isEmpty()
                            ? ((GridElementUi) child).getElementUiSave() : "false"));
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_DATATYPE),
                            ((GridElementUi) child).getElementUiDataType() != null && !((GridElementUi) child).getElementUiDataType().isEmpty()
                            ? ((GridElementUi) child).getElementUiDataType() : ""));
                    xmlew.add(xmlef.createEndElement(new QName(ArtifactDefinitionConstants.LABEL_GRID), null));
                    // end grid
                }
            } catch (XMLStreamException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                        ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });
    }
        
    private void buildDetails(ElementUi elementUi) {
        try {
            lytDetailsPanel.removeAll();

            clickedComponent = (Component) elementUi;
            clickedComponent.getElement().getStyle().set("border", "1px solid #4FBDDD");
            
            HorizontalLayout lytActions = new HorizontalLayout();
            ActionButton btnDelete = new ActionButton(new ActionIcon(VaadinIcon.TRASH), this.deleteElementVisualAction.getModuleAction().getDisplayName());
            btnDelete.addClickListener(event
                    -> this.deleteElementVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("element", elementUi)
                    )).open());
            lytActions.add(btnDelete);
            lytActions.setAlignSelf(FlexComponent.Alignment.END);
            
            Command refresh = () -> {
                buildDetails(elementUi);
                wdwEditor.getBtnConfirm().click();
            };
            ElementOptionsPanel pnlOptions = new ElementOptionsPanel(elementUi, elementActionsRegistry, ts, elementPropertyEditorDialog, listFunctions, refresh, artifactPath);
            pnlOptions.setShowCoreActions(false);
            pnlOptions.setSelectionListener(e -> {
                switch (e.getActionCommand()) {
                    case ElementOptionsPanel.EVENT_ACTION_SELECTION:
                        ModuleActionParameterSet parameters = new ModuleActionParameterSet(new ModuleActionParameter("element", elementUi));
                        Dialog wdwElementAction = (Dialog) ((AbstractVisualElementAction) e.getSource()).getVisualComponent(parameters);
                        wdwElementAction.open();
                        break;
                }
            });
            
            if (elementUi instanceof HorizontalLayoutElementUi || elementUi instanceof VerticalLayoutElementUi || elementUi instanceof CheckBoxElementUi)
                pnlOptions.setShowEvents(false);
            
            // Add content to layout
            lytDetailsPanel.add(lytActions, pnlOptions.build());
        } catch (Exception ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
 
    /**
     * Add a container to the form.
     * @param container Contains the elements.
     */
    private void addContainerUi(Component container) {
        if (elementUi instanceof HorizontalLayoutElementUi) {
            HorizontalLayoutElementUi horizontalLayoutElementUi = (HorizontalLayoutElementUi) elementUi;
            ((HorizontalLayoutElementUi) elementUi).setWidth(horizontalLayoutElementUi.getElementUiWidth());
            ((HorizontalLayoutElementUi) elementUi).setHeight(horizontalLayoutElementUi.getElementUiHeight());

            if (container instanceof HorizontalLayout) {
                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                horizontalLayout.add(horizontalLayoutElementUi);
            } else if (container instanceof VerticalLayout) {
                VerticalLayout verticalLayout = (VerticalLayout) container;
                verticalLayout.add(horizontalLayoutElementUi);
            }
            horizontalLayoutElementUi.getStyle().set("border", "1px solid #E9E9E9");
            
            cleanBorderColor();
            buildDetails(horizontalLayoutElementUi);

            DropTarget<HorizontalLayout> dropHorizontal = DropTarget.create(horizontalLayoutElementUi);
            dropHorizontal.setDropEffect(DropEffect.COPY);

            dropHorizontal.addDropListener(listener -> {
                if (listener.getDropEffect() == DropEffect.COPY) {
                    component = listener.getDragSourceComponent().get();

                    if (component instanceof AbstractElementUiItem) {
                        elementUi = ((AbstractElementUiItem) component).create();

                        if (elementUi instanceof Component) {
                            if (elementUi instanceof HorizontalLayoutElementUi || elementUi instanceof VerticalLayoutElementUi)
                                addContainerUi(horizontalLayoutElementUi);
                            else
                                addElementUi(horizontalLayoutElementUi);
                        }
                    }
                }
            });
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(horizontalLayoutElementUi)) {
                        clickedComponent.getElement().getStyle().set("border", "1px solid #4FBDDD");
                        clickedComponent = clickedComponent.getParent().get();
                    } else
                        buildDetails(horizontalLayoutElementUi);
                } else
                    buildDetails(horizontalLayoutElementUi);
            };           
            horizontalLayoutElementUi.addEventListener(commandDetails);
        } else if (elementUi instanceof VerticalLayoutElementUi) {
            VerticalLayoutElementUi verticalLayoutElementUi = (VerticalLayoutElementUi) elementUi;
            ((VerticalLayoutElementUi) elementUi).setWidth(verticalLayoutElementUi.getElementUiWidth());
            ((VerticalLayoutElementUi) elementUi).setHeight(verticalLayoutElementUi.getElementUiHeight());

            if (container instanceof HorizontalLayout) {
                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                horizontalLayout.add(verticalLayoutElementUi);
            } else if (container instanceof VerticalLayout) {
                VerticalLayout verticalLayout = (VerticalLayout) container;
                verticalLayout.add(verticalLayoutElementUi);
            }
            verticalLayoutElementUi.getStyle().set("border", "1px solid #E9E9E9");
            
            cleanBorderColor();
            buildDetails(verticalLayoutElementUi);

            DropTarget<VerticalLayout> dropVertical = DropTarget.create(verticalLayoutElementUi);
            dropVertical.setDropEffect(DropEffect.COPY);

            dropVertical.addDropListener(listener -> {
                if (listener.getDropEffect() == DropEffect.COPY) {
                    component = listener.getDragSourceComponent().get();

                    if (component instanceof AbstractElementUiItem) {
                        elementUi = ((AbstractElementUiItem) component).create();

                        if (elementUi instanceof Component) {
                            if (elementUi instanceof HorizontalLayoutElementUi || elementUi instanceof VerticalLayoutElementUi)
                                addContainerUi(verticalLayoutElementUi);
                            else
                                addElementUi(verticalLayoutElementUi);
                        }
                    }
                }
            });
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(verticalLayoutElementUi)) {
                        clickedComponent.getElement().getStyle().set("border", "1px solid #4FBDDD");
                        clickedComponent = clickedComponent.getParent().get();
                    } else
                        buildDetails(verticalLayoutElementUi);
                } else
                    buildDetails(verticalLayoutElementUi);
            };
            verticalLayoutElementUi.addEventListener(commandDetails);
        }
    }

    /**
     * Add an element to the form.
     * @param container Contains the elements.
     */
    private void addElementUi(Component container) {
        if (elementUi instanceof LabelElementUi) {
            LabelElementUi labelElementUi = (LabelElementUi) elementUi;
            ((LabelElementUi) elementUi).setText(labelElementUi.getElementUiValue());
            ((LabelElementUi) elementUi).setWidth(labelElementUi.getElementUiWidth());
            ((LabelElementUi) elementUi).setHeight(labelElementUi.getElementUiHeight());

            if (container instanceof HorizontalLayout) {
                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                horizontalLayout.add(labelElementUi);
            } else if (container instanceof VerticalLayout) {
                VerticalLayout verticalLayout = (VerticalLayout) container;
                verticalLayout.add(labelElementUi);
            }
            
            cleanBorderColor();
            buildDetails(labelElementUi);

            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(labelElementUi))
                        clickedComponent = clickedComponent.getParent().get();
                    else
                        buildDetails(labelElementUi);
                } else
                    buildDetails(labelElementUi);
            };
            labelElementUi.addEventListener(commandDetails);
        } else if (elementUi instanceof TextFieldElementUi) {
            TextFieldElementUi textFieldElementUi = (TextFieldElementUi) elementUi;
            ((TextFieldElementUi) elementUi).setWidth(textFieldElementUi.getElementUiWidth());
            ((TextFieldElementUi) elementUi).setHeight(textFieldElementUi.getElementUiHeight());
            
            if (container instanceof HorizontalLayout) {
                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                horizontalLayout.add(textFieldElementUi);
            } else if (container instanceof VerticalLayout) {
                VerticalLayout verticalLayout = (VerticalLayout) container;
                verticalLayout.add(textFieldElementUi);
            }
            
            cleanBorderColor();
            buildDetails(textFieldElementUi);
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(textFieldElementUi))
                        clickedComponent = clickedComponent.getParent().get();
                    else
                        buildDetails(textFieldElementUi);
                } else
                    buildDetails(textFieldElementUi);
            };
            textFieldElementUi.addEventListener(commandDetails);
        } else if (elementUi instanceof ComboBoxElementUi) {
            ComboBoxElementUi comboBoxElementUi = (ComboBoxElementUi) elementUi;
            ((ComboBoxElementUi) elementUi).setWidth(comboBoxElementUi.getElementUiWidth());
            ((ComboBoxElementUi) elementUi).setHeight(comboBoxElementUi.getElementUiHeight());

            if (container instanceof HorizontalLayout) {
                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                horizontalLayout.add(comboBoxElementUi);
            } else if (container instanceof VerticalLayout) {
                VerticalLayout verticalLayout = (VerticalLayout) container;
                verticalLayout.add(comboBoxElementUi);
            }
            
            cleanBorderColor();
            buildDetails(comboBoxElementUi);
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(comboBoxElementUi))
                        clickedComponent = clickedComponent.getParent().get();
                    else
                        buildDetails(comboBoxElementUi);
                } else
                    buildDetails(comboBoxElementUi);
            };
            comboBoxElementUi.addEventListener(commandDetails);
        }  else if (elementUi instanceof CheckBoxElementUi) {
            CheckBoxElementUi checkBoxElementUi = (CheckBoxElementUi) elementUi;
            ((CheckBoxElementUi) elementUi).setWidth(checkBoxElementUi.getElementUiWidth());
            ((CheckBoxElementUi) elementUi).setHeight(checkBoxElementUi.getElementUiHeight());

            if (container instanceof HorizontalLayout) {
                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                horizontalLayout.add(checkBoxElementUi);
            } else if (container instanceof VerticalLayout) {
                VerticalLayout verticalLayout = (VerticalLayout) container;
                verticalLayout.add(checkBoxElementUi);
            }
            
            cleanBorderColor();
            buildDetails(checkBoxElementUi);
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(checkBoxElementUi))
                        clickedComponent = clickedComponent.getParent().get();
                    else
                        buildDetails(checkBoxElementUi);
                } else
                    buildDetails(checkBoxElementUi);
            };
            checkBoxElementUi.addEventListener(commandDetails);
        } else if (elementUi instanceof ButtonElementUi) {
            ButtonElementUi buttonElementUi = (ButtonElementUi) elementUi;
            ((ButtonElementUi) elementUi).setWidth(buttonElementUi.getElementUiWidth());
            ((ButtonElementUi) elementUi).setHeight(buttonElementUi.getElementUiHeight());

            if (container instanceof HorizontalLayout) {
                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                horizontalLayout.add(buttonElementUi);
            } else if (container instanceof VerticalLayout) {
                VerticalLayout verticalLayout = (VerticalLayout) container;
                verticalLayout.add(buttonElementUi);
            }
            
            cleanBorderColor();
            buildDetails(buttonElementUi);
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(buttonElementUi))
                        clickedComponent = clickedComponent.getParent().get();
                    else
                        buildDetails(buttonElementUi);
                } else
                    buildDetails(buttonElementUi);
            };
            buttonElementUi.addEventListener(commandDetails);
        }  else if (elementUi instanceof GridElementUi) {
            GridElementUi gridElementUi = (GridElementUi) elementUi;
            ((GridElementUi) elementUi).setWidth(gridElementUi.getElementUiWidth());
            ((GridElementUi) elementUi).setHeight(gridElementUi.getElementUiHeight());

            if (container instanceof HorizontalLayout) {
                HorizontalLayout horizontalLayout = (HorizontalLayout) container;
                horizontalLayout.add(gridElementUi);
            } else if (container instanceof VerticalLayout) {
                VerticalLayout verticalLayout = (VerticalLayout) container;
                verticalLayout.add(gridElementUi);
            }
            
            cleanBorderColor();
            buildDetails(gridElementUi);
            
            Command commandDetails = () -> {
                if (clickedComponent != null) {
                    cleanBorderColor();
                    if (clickedComponent.getParent().get().equals(gridElementUi))
                        clickedComponent = clickedComponent.getParent().get();
                    else
                        buildDetails(gridElementUi);
                } else
                    buildDetails(gridElementUi);
            };
            gridElementUi.addEventListener(commandDetails);
        }
    }
        
    private void cleanBorderColor() {
        if (clickedComponent != null) {
            clickedComponent.getElement().getStyle().remove("border");
            if (clickedComponent instanceof VerticalLayoutElementUi || clickedComponent instanceof HorizontalLayoutElementUi) {
                clickedComponent.getElement().getStyle().set("border", "1px solid #E9E9E9");
                
                for (int i=0; i < clickedComponent.getChildren().count(); i++)
                    clickedComponent.getElement().getChildren().forEach(child -> child.getStyle().remove("border"));
            }
        }
    }

    private void getXmlFunctions() {
        try {
            xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_SRC), ""));
            if (listFunctions != null) {
                listFunctions.forEach(function -> {
                    try {
                        xmlew.add(xmlef.createStartElement(new QName(ArtifactDefinitionConstants.LABEL_FUNCTION), null, null));
                        xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_TYPE),
                                function.getType() != null && !function.getType().isEmpty() ? function.getType() : ""));
                        xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_NAME),
                                function.getName() != null && !function.getName().isEmpty() ? function.getName() : ""));
                        getXmlFunctionParameters(function);
                        xmlew.add(xmlef.createCData(function.getValue() != null && !function.getValue().isEmpty() ? function.getValue(): ""));
                        xmlew.add(xmlef.createEndElement(new QName(ArtifactDefinitionConstants.LABEL_FUNCTION), null));
                    } catch (XMLStreamException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                });
            }
        } catch (XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void getXmlFunctionParameters(ArtifactDefinitionFunction function) {
        if (function != null) {
            if (function.getParameters() != null && function.getParameters().size() > 0) {
                try {
                    parameterNames = "";
                    function.getParameters().forEach(param -> parameterNames += param.getKey() + " ");
                    parameterNames = StringUtils.chop(parameterNames);
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_PARAMETERNAMES), parameterNames));
                } catch (XMLStreamException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
            } else {
                try {
                    xmlew.add(xmlef.createAttribute(new QName(ArtifactDefinitionConstants.LABEL_PARAMETERNAMES), ""));
                } catch (XMLStreamException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
        }
    }
} 