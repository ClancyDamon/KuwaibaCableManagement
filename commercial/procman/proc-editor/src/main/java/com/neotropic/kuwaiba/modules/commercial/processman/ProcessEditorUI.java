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
package com.neotropic.kuwaiba.modules.commercial.processman;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.Point;
import com.neotropic.flow.component.mxgraph.bpmn.BPMNConnection;
import com.neotropic.flow.component.mxgraph.bpmn.BPMNDiagram;
import com.neotropic.flow.component.mxgraph.bpmn.BPMNNode;
import com.neotropic.flow.component.mxgraph.bpmn.LabelNode;
import com.neotropic.flow.component.mxgraph.bpmn.SwimlaneNode;
import com.neotropic.flow.component.mxgraph.bpmn.SymbolNode;
import com.neotropic.flow.component.mxgraph.bpmn.TaskNode;
import com.neotropic.flow.component.paper.toggle.PaperToggleButton;
import com.neotropic.kuwaiba.modules.commercial.processman.components.ArtifactDefinitionDialog;
import com.neotropic.kuwaiba.modules.commercial.processman.service.ArtifactDefinition;
import com.neotropic.kuwaiba.modules.commercial.processman.tools.ArtifactDefinitionConstants;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.ProcessManagerService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinitionLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Actor;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActorAuthorizationManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ConditionalActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ParallelActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.ProcessDefinitionLoader.Attribute;
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.ProcessDefinitionLoader.Tag;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.PersistenceService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Main UI for the process editor
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "processeditor", layout = ProcessEditorLayout.class)
public class ProcessEditorUI extends VerticalLayout implements HasDynamicTitle, AbstractUI {
    /**
     * Reference to the Persistence Service.
     */
    @Autowired
    private PersistenceService persistenceService;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the process manager service.
     */
    @Autowired
    private ProcessManagerService processManagerService;
    /**
     * Reference to the actor authorization manager.
     */
    @Autowired
    private ActorAuthorizationManager actorAuthorizationManager;
    /**
     * Diagram
     */
    private BPMNDiagram diagram;
    /**
     * Process properties
     */
    private String currentProcessName;
    private String currentProcessId;
    private String currentVersion;
    private String currentDescription;
    private long currentCreationDate;
    private BoldLabel lblProcessDefinition;
    private String processDefPath; 
    
    private LinkedHashMap<Actor, SwimlaneNode> mapActorLanes;
    
    private SymbolNode startNode;
    private SymbolNode newGateway;
    private BPMNNode lastNodeAdded;
    private Map<ActivityDefinition, BPMNNode> mapNodes;
    private Map<Actor, SwimlaneNode> mapActors;
    /**
     *  Reference to the artifact definition dialog
     */
    @Autowired
    private ArtifactDefinitionDialog artifactDefinitionDialog;
    private Command commandFormArtifact;
    private LinkedList<ArtifactDefinition> listArtifact;
    private LinkedList<ArtifactDefinition> listCurrentArtifacts;
    /**
     * Actions buttons
     */
    private ActionButton btnUpdate;
    private ActionButton btnSaveDiagram;
    private ActionButton btnEvent;
    private ActionButton btnSwimlane;
    private ActionButton btnGateway;
    private ActionButton btnTask;
    /**
     * Action enable
     */
    private Checkbox chxEnabled;
    private String processEnabled;
    /**
     * Saves the activity information to relate to their artifacts.
     */
    private BPMNNode bpmnActivity;
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.processman.ui.process-editor.title");
    }
    
    @Override
    public void initContent() {
        String processEnginePath = String.valueOf(persistenceService.getApplicationProperties().get("processEnginePath")); //NOI18N
        processDefPath = processEnginePath + "/process/definitions"; //NOI18N
        
        mapNodes = new HashMap<>();
        mapActors = new HashMap<>();
        
        mapActorLanes = new LinkedHashMap<>();
        List<ProcessDefinition> processDefinitions = processManagerService.getProcessDefinitions();
        
        diagram = new BPMNDiagram();
        diagram.setWidthFull();
        diagram.setHeightFull();
        
        btnSaveDiagram = new ActionButton(new ActionIcon(VaadinIcon.DOWNLOAD_ALT),
                ts.getTranslatedString("module.processeditor.process-actions.save-process-name"));
        btnSaveDiagram.addClickListener(evt -> {
            if (!mapActorLanes.isEmpty()) {
                if (startNode != null) {
                    renderEnable(chxEnabled.getValue());
                    saveProcessDefinition();
                } else 
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                            ts.getTranslatedString("module.processeditor.no-start-added"),
                            AbstractNotification.NotificationType.WARNING, ts).open();
            } else
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                        ts.getTranslatedString("module.processeditor.one-actor-must-be-added"),
                        AbstractNotification.NotificationType.WARNING, ts).open();
        });
        btnSaveDiagram.setEnabled(false);
        
        btnEvent = new ActionButton(new ActionIcon(VaadinIcon.CIRCLE_THIN),
                ts.getTranslatedString("module.processeditor.add-event"));
        btnEvent.addClickListener(evt -> addEvent());
                        
        btnTask = new ActionButton(new ActionIcon(VaadinIcon.THIN_SQUARE),
                ts.getTranslatedString("module.processeditor.add-task"));
        btnTask.addClickListener(evt -> {
            if (!mapActorLanes.isEmpty()) {
                if (startNode != null) {
                    TaskNode newNode = new TaskNode(diagram);
                    newNode.setX(lastNodeAdded.getX() + 250);
                    newNode.setY(lastNodeAdded.getY());
                    
                    newNode.addCellParentChangedListener(event -> {
                        newNode.setCellParent(lastNodeAdded.getCellParent());
                    });
                    
                    BPMNConnection conn = new BPMNConnection(diagram);
                    conn.setSource(lastNodeAdded.getUuid());
                    conn.setTarget(newNode.getUuid());
                    
                    addBpmnTool(newNode);
                    diagram.addNode(newNode);
                    diagram.addEdge(conn);
                } else
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                            ts.getTranslatedString("module.processeditor.one-start-must-be-added"),
                            AbstractNotification.NotificationType.WARNING, ts).open();
            } else
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                        ts.getTranslatedString("module.processeditor.one-actor-must-be-added"),
                        AbstractNotification.NotificationType.WARNING, ts).open();
        });
        
        btnSwimlane = new ActionButton(new ActionIcon(VaadinIcon.COMBOBOX),
                ts.getTranslatedString("module.processeditor.add-lane"));
        btnSwimlane.addClickListener(evt -> addLane());
          
        btnGateway = new ActionButton(new ActionIcon(VaadinIcon.RHOMBUS),
                ts.getTranslatedString("module.processeditor.add-gateway"));
        btnGateway.addClickListener(evt -> {
            if (!mapActorLanes.isEmpty())
                addGateway();
            else
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                        ts.getTranslatedString("module.processeditor.one-actor-must-be-added"),
                        AbstractNotification.NotificationType.WARNING, ts).open(); 
        });
                    
        ActionButton btnNewDiagram = new ActionButton(new ActionIcon(VaadinIcon.FILE_ADD),
                ts.getTranslatedString("module.processeditor.process-actions.new-process-name"));
        btnNewDiagram.addClickListener(evt -> {
            if (startNode != null)
                saveProcessDefinition();
            
            createNewProcessDialog();
        });
        
        ActionButton btnOpenDiagram = new ActionButton(new ActionIcon(VaadinIcon.FOLDER_OPEN_O),
                ts.getTranslatedString("module.processeditor.process-actions.open-process-name"));
        btnOpenDiagram.addClickListener(evt -> {
            mapActors = new HashMap<>();
    /**
     * Temporal Block used to load existent process definitions. Change the index of the list
     * to load the desired process. This is temporary until all process definitions
     * are parsed.
     */
//         List<ProcessDefinition> existentProcessLst = processManagerService.getProcessDefinitions();
//       
//        ActivityDefinition activity = existentProcessLst.get(0).getStartActivity();
//        
//        addActivity(activity, null, "");
//        
//        if (lastNodeAdded != null)
//            lastNodeAdded.addCellAddedListener(eventListener -> {
//                if (mapActors.size() > 0) {
//                    for (Actor act : mapActors.keySet())
//                        diagram.executeHierarchicalLayout(mapActors.get(act).getUuid()); 
//                } else
//                    diagram.executeHierarchicalLayout("1"); // Default Parent     
//            });
/*******************************/
            loadProcessDefinitions();
        });
        
        btnUpdate = new ActionButton(new ActionIcon(VaadinIcon.EDIT),
                ts.getTranslatedString("module.general.label.action-edit-properties"));
        btnUpdate.addClickListener(evt -> updateProcessDialog());
        
        chxEnabled = new Checkbox(ts.getTranslatedString("module.general.labels.enabled"));
        chxEnabled.setClassName("process-editor-chx-enable");
        
        enableActionButtons(false);
        
        lblProcessDefinition = new BoldLabel();
        lblProcessDefinition.setVisible(false);
        lblProcessDefinition.getElement().getStyle().set("margin-top", "5px");
        
        HorizontalLayout lytActions = new HorizontalLayout(btnNewDiagram, btnOpenDiagram, btnUpdate, btnSaveDiagram, btnSwimlane, btnEvent, btnTask, btnGateway);
        lytActions.setSpacing(false);
        
        HorizontalLayout lytTools = new HorizontalLayout(lytActions, lblProcessDefinition);     
        VerticalLayout lytContent = new VerticalLayout(lytTools, diagram);
        lytContent.setId("lytContent");
        lytContent.setSizeFull();
        
        setPadding(false);
        add(lytContent);
        setSizeFull();
    }
    
    /**
     * Shows options for events, start or end
     */
    private void addEvent() {
        ConfirmDialog wdwEvent = new ConfirmDialog(ts,
                ts.getTranslatedString("module.processeditor.add-event"),
                ts.getTranslatedString("module.general.label.action-add"));
        wdwEvent.setDraggable(true);
        wdwEvent.setResizable(true);
        
        PaperToggleButton btnEvents = new PaperToggleButton();
        btnEvents.setChecked(false);
        btnEvents.setClassName("green", true);
        btnEvents.addClassName("icon-button");
        
        Label lblStart = new Label(ts.getTranslatedString("module.processeditor.label-start-event"));
        Label lblEnd = new Label(ts.getTranslatedString("module.processeditor.label-end-event"));
        
        HorizontalLayout lytEvent = new HorizontalLayout(lblStart, btnEvents, lblEnd);
        VerticalLayout lytContent = new VerticalLayout(lytEvent);
        lytContent.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, lytEvent);
        lytContent.setWidthFull();
        
        wdwEvent.getBtnConfirm().addClickListener(evt -> {
            if (!mapActorLanes.isEmpty()) {
                if (!btnEvents.getChecked()) {
                    if (startNode == null) {
                        SymbolNode newNode = new SymbolNode(diagram, SymbolNode.SymbolType.Event);
                        newNode.setX(100);
                        newNode.setY(100);
                        addBpmnTool(newNode);
                        diagram.addNode(newNode);
                        startNode = newNode;
                        newNode.setCellParent(mapActorLanes.get(mapActorLanes.keySet().stream().findFirst().get()).getUuid());
                        
                        btnSaveDiagram.setEnabled(true);
                        wdwEvent.close();
                    } else
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                ts.getTranslatedString("module.processeditor.only-one-start-event-ca-be-added"),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                } else {
                    if (startNode != null) {
                        SymbolNode newNode = new SymbolNode(diagram, SymbolNode.SymbolType.Event_End);
                        newNode.setX(lastNodeAdded.getX() + 250);
                        newNode.setY(lastNodeAdded.getY());

                        newNode.addCellParentChangedListener(event -> {
                            newNode.setCellParent(lastNodeAdded.getCellParent());
                        });
                        
                        BPMNConnection newEdge = new BPMNConnection(diagram);
                        newEdge.setSource(lastNodeAdded.getUuid());
                        newEdge.setTarget(newNode.getUuid());
                        
                        addBpmnTool(newNode);
                        diagram.addNode(newNode);
                        diagram.addEdge(newEdge);
                        
                        wdwEvent.close();
                    } else
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                ts.getTranslatedString("module.processeditor.one-start-must-be-added"),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                }
            } else
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                         ts.getTranslatedString("module.processeditor.one-actor-must-be-added"), 
                            AbstractNotification.NotificationType.WARNING, ts).open(); 
        });
        
        wdwEvent.setContent(lytContent);
        wdwEvent.open();
    }
    
    /**
     * Shows options for gateway, parallel or conditional.
     */
    private void addGateway() {
        ConfirmDialog wdwGateway = new ConfirmDialog(ts,
                ts.getTranslatedString("module.processeditor.add-gateway"),
                ts.getTranslatedString("module.general.label.action-add"));
        wdwGateway.setDraggable(true);
        wdwGateway.setResizable(true);
        
        List<String> listGateway = new ArrayList<>();
        listGateway.add(ts.getTranslatedString("module.processeditor.add-gateway-parallel"));
        listGateway.add(ts.getTranslatedString("module.processeditor.add-gateway-conditional"));
        
        ComboBox<String> cmbGateway = new ComboBox(ts.getTranslatedString("module.general.labels.type"));
        cmbGateway.setRequiredIndicatorVisible(true);
        cmbGateway.setAllowCustomValue(false);
        cmbGateway.setItems(listGateway);
        cmbGateway.setWidthFull();
        
        wdwGateway.getBtnConfirm().addClickListener(event -> {
            if (cmbGateway.getValue() != null) {
                
                if (cmbGateway.getValue().equals(ts.getTranslatedString("module.processeditor.add-gateway-parallel")))
                    newGateway = new SymbolNode(diagram, SymbolNode.SymbolType.Fork);
                else if (cmbGateway.getValue().equals(ts.getTranslatedString("module.processeditor.add-gateway-conditional")))
                    newGateway = new SymbolNode(diagram, SymbolNode.SymbolType.Exclusive);
                    
                if (newGateway != null) {
                    if (startNode != null) {
                        newGateway.setX(lastNodeAdded.getX() + 250);
                        newGateway.setY(lastNodeAdded.getY());

                        newGateway.addCellParentChangedListener(evt -> {
                            newGateway.setCellParent(lastNodeAdded.getCellParent());
                        });
                        
                        BPMNConnection newEdge = new BPMNConnection(diagram);
                        newEdge.setSource(lastNodeAdded.getUuid());
                        newEdge.setTarget(newGateway.getUuid());
                        
                        addBpmnTool(newGateway);
                        diagram.addNode(newGateway);
                        diagram.addEdge(newEdge);
                    } else
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                ts.getTranslatedString("module.processeditor.one-start-must-be-added"),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                }
                wdwGateway.close();
            } else
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                        ts.getTranslatedString("module.processeditor.add-gateway-error"),
                        AbstractNotification.NotificationType.WARNING, ts).open();
        });
        
        wdwGateway.setContent(cmbGateway);
        wdwGateway.open();
    }
    
    /**
     * Adds additional tools to the selected node. 
     * For example, overlay button to add/update artifacts.
     * @param bpmnNode selected node. 
     */
    private void addBpmnTool(BPMNNode bpmnNode) {
        if (bpmnNode != null && !bpmnNode.isLabel()) {
            lastNodeAdded = bpmnNode;
            
            bpmnNode.addRightClickCellListener(event -> {
                bpmnNode.clearCellOverlays();
                if (!bpmnNode.isLabel()) {
                    bpmnNode.addOverlayButton(ArtifactDefinitionConstants.LABEL_ARTIFACT,
                            ArtifactDefinitionConstants.LABEL_ARTIFACT, "img/artifact.svg",
                            MxConstants.ALIGN_RIGHT, MxConstants.ALIGN_TOP, 12, 10,
                            BPMNNode.ICON_WIDTH, BPMNNode.ICON_HEIGHT);
                }
            });

            bpmnNode.addOnClickOverlayButtonListener(event -> {
                if (event.getSource() != null && event.getResultCell() != null)
                    addBpmnTool((BPMNNode) event.getResultCell());
            });

            bpmnNode.addClickOverlayButtonListener(event -> {
                if (ArtifactDefinitionConstants.LABEL_ARTIFACT.equals(event.getButtonId()))
                    launchArtifactDialog(bpmnNode);
                else if (("removeNode").equals(event.getButtonId())) { 
                    BPMNNode node = (BPMNNode) event.getSource();
                    if (node.isSymbol()) {
                        SymbolNode symbolNode = (SymbolNode) node;
                        if (symbolNode.getType().equals(SymbolNode.SymbolType.Event)) // Checks if the start event has been removed
                            startNode = null;
                    }
                }
            });
        }
    }
    
    private void launchArtifactDialog(BPMNNode bpmnNode) {
        listCurrentArtifacts = new LinkedList<>();
        loadCurrentArtifactDefinition(listArtifact, bpmnNode);
        
        commandFormArtifact  = () -> {
            listCurrentArtifacts.forEach(artifact -> {
                if (listArtifact.size() > 0) {
                    if (!listArtifact.contains(artifact))
                        listArtifact.add(artifact);
                } else
                    listArtifact.add(artifact);
            });
            btnSaveDiagram.click();
        };
        this.artifactDefinitionDialog.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter("artifacts", listCurrentArtifacts),
                new ModuleActionParameter("task", bpmnNode),
                new ModuleActionParameter("processId", currentProcessId),
                new ModuleActionParameter("commandFormArtifact", commandFormArtifact)
        )).open();
    }
    
    
    private void loadCurrentArtifactDefinition(List<ArtifactDefinition> artifacts, BPMNNode bpmnNode) {
        artifacts.forEach(artifact ->  {
            if (artifact.getBpmnNode().equals(bpmnNode))
                listCurrentArtifacts.add(artifact);
        });
    }
    
    /**
     * Save the current process.
     * Process definition path: /data/processEngine/process/definitions
     * The process is saved taking into account the define path and the current process id.
     */
    private void saveProcessDefinition() {
        byte[] structure = getAsXML();
        File file = new File(processDefPath + "/" + currentProcessId + ".xml");
        if (file.exists()) {
            try (FileOutputStream fos = new FileOutputStream(processDefPath + "/" + currentProcessId + ".xml")) {
                fos.write(structure);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        String.format(ts.getTranslatedString("module.processeditor.process-actions.update-process-success"),
                                currentProcessName), AbstractNotification.NotificationType.INFO, ts).open();
            } catch (IOException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else {
            try (FileOutputStream fos = new FileOutputStream(processDefPath + "/" + currentProcessId + ".xml")) {
                fos.write(structure);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        String.format(ts.getTranslatedString("module.processeditor.process-actions.save-process-success"),
                                currentProcessName), AbstractNotification.NotificationType.INFO, ts).open();
            } catch (IOException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }
    
    /**
     * Loads the list of available process definitions.
     */
    public void loadProcessDefinitions() {     
        String processEnginePath = String.valueOf(persistenceService.getApplicationProperties().get("processEnginePath")); //NOI18N
        File processDefDir = new File(processEnginePath + "/process/definitions/"); 
        Grid<StringPair> gridProcess = new Grid();
        List<StringPair> lstProcess = new ArrayList<>();
        
        ConfirmDialog wdwOpenDiagram = new ConfirmDialog(ts, ts.getTranslatedString("module.general.messages.cancel"));
        wdwOpenDiagram.getBtnConfirm().setEnabled(false);
        wdwOpenDiagram.getBtnConfirm().setVisible(false);
        wdwOpenDiagram.setDraggable(true);
        wdwOpenDiagram.setResizable(true);
        wdwOpenDiagram.setSizeUndefined();

        if (processDefDir.exists()) {
            for (File processDefFile : processDefDir.listFiles()) {
                if (processDefFile.isFile() ) {
                    try {
                        Document domDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(processDefFile);
                        domDocument.normalize();

                        Element root = domDocument.getDocumentElement();
                        if (root.getNodeType() == Node.ELEMENT_NODE && root.getNodeName().equals(Tag.PROCESS_DEFINITION)) {
                            lstProcess.add(new StringPair(processDefFile.getName(), root.getAttribute(Attribute.NAME) != null
                            && !root.getAttribute(Attribute.NAME).isEmpty() ? root.getAttribute(Attribute.NAME) : ""));
                        }
                    } catch (ParserConfigurationException | SAXException | IOException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                }
            }
            gridProcess.setItems(lstProcess);
            gridProcess.addColumn(StringPair::getValue);
            gridProcess.addSelectionListener(listener -> {   
                resetCanvas();
                openProcessDefinition(processDefDir.getAbsolutePath() + "/" + listener.getFirstSelectedItem().get().getKey()); 
                wdwOpenDiagram.close();
            });
        } else
            System.out.println(String.format(ts.getTranslatedString("module.processeditor.directory-definitions-not-found"),
                    Calendar.getInstance().getTime(), processDefDir.getAbsolutePath()));
        
        wdwOpenDiagram.setHeader(ts.getTranslatedString("module.processeditor.process-actions.open-process-name"));
        wdwOpenDiagram.setContent(gridProcess);
        gridProcess.setWidthFull();
        wdwOpenDiagram.open();
    }
    
    /**
     * Get the current process diagram as a byte array with the XML document. 
     * @return The byte array with the process definition.
     */
     public byte[] getAsXML() {
        try {
            QName tagProcessDefinition = new QName(Tag.PROCESS_DEFINITION);
            QName tagActors = new QName(Tag.ACTORS);
            QName tagActor = new QName(Tag.ACTOR);
            QName tagActivityDefinitions = new QName(Tag.ACTIVITY_DEFINITIONS);
            QName tagActivityDefinition = new QName(Tag.ACTIVITY_DEFINITION);
            QName tagArtifactDefinition = new QName(Tag.ARTIFACT_DEFINITION);
            QName tagPaths = new QName(Tag.PATHS);
            QName tagPath = new QName(Tag.PATH);
            QName tagParameters = new QName(Tag.PARAMETERS);
            QName tagParameter = new QName(Tag.PARAMETER);
            QName tagKpis = new QName(Tag.KPIS);
            QName tagKpi = new QName(Tag.KPI);
            QName tagActions = new QName(Tag.ACTIONS);
            QName tagAction = new QName(Tag.ACTION);
            QName tagThreshold = new QName(Tag.THRESHOLD); 
            QName tagShape = new QName(Tag.BPMNSHAPE); 
            QName tagEdge = new QName(Tag.BPMNEDGE); 
            QName tagControlPoint = new QName(Tag.CONTROLPOINT); 
            QName tagDiagram = new QName(Tag.BPMNDIAGRAM); 
            QName tagSwimlane= new QName(Tag.BPMNSWIMLANE); 

            Map<BusinessObjectLight, ViewObject> mapCustomShapes = new HashMap<>();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();

            xmlew.add(xmlef.createStartElement(tagProcessDefinition, null, null));
            xmlew.add(xmlef.createAttribute(new QName(Attribute.VERSION), currentVersion)); 
            xmlew.add(xmlef.createAttribute(new QName(Attribute.ID), currentProcessId));
            xmlew.add(xmlef.createAttribute(new QName(Attribute.NAME), currentProcessName)); 
            xmlew.add(xmlef.createAttribute(new QName(Attribute.DESCRIPTION), currentDescription)); 
            xmlew.add(xmlef.createAttribute(new QName(Attribute.CREATION_DATE), String.valueOf(currentCreationDate))); 
            xmlew.add(xmlef.createAttribute(new QName(Attribute.START_ACTIVITY_ID), startNode.getUuid())); 
            xmlew.add(xmlef.createAttribute(new QName(Attribute.ENABLED), processEnabled)); 
    
            List<BPMNConnection> connections = diagram.getBPMNConnection();

            xmlew.add(xmlef.createStartElement(tagActors, null, null));
            for (Actor act : mapActorLanes.keySet()) {
                xmlew.add(xmlef.createStartElement(tagActor, null, null));
                xmlew.add(xmlef.createAttribute(new QName(Attribute.ID), mapActorLanes.get(act).getUuid())); 
                xmlew.add(xmlef.createAttribute(new QName(Attribute.NAME), act.getName())); 
                xmlew.add(xmlef.createAttribute(new QName(Attribute.TYPE), "2")); 
                //
                xmlew.add(xmlef.createEndElement(tagActor, null));
            }
            xmlew.add(xmlef.createEndElement(tagActors, null));
            
            xmlew.add(xmlef.createStartElement(tagActivityDefinitions, null, null));
                        
            for (BPMNNode objectNode : diagram.getBPMNNodes()) {
                if (objectNode instanceof SymbolNode || objectNode instanceof TaskNode) {
                     xmlew.add(xmlef.createStartElement(tagActivityDefinition, null, null));
                     xmlew.add(xmlef.createAttribute(new QName(Attribute.ID), objectNode.getUuid())); 
                     xmlew.add(xmlef.createAttribute(new QName(Attribute.NAME), objectNode.getLabel() == null ? "" : objectNode.getLabel())); 
                     xmlew.add(xmlef.createAttribute(new QName(Attribute.DESCRIPTION), objectNode.getLabel() == null ? "" : objectNode.getLabel())); 
                     xmlew.add(xmlef.createAttribute(new QName(Attribute.ACTOR_ID), objectNode.getCellParent())); 
                     if (objectNode instanceof SymbolNode) {
                         SymbolNode node = (SymbolNode) objectNode;
                         if (node.getType().equals(SymbolNode.SymbolType.Event))
                             xmlew.add(xmlef.createAttribute(new QName(Attribute.TYPE), ActivityDefinitionLight.TYPE_START + "")); 
                         if (node.getType().equals(SymbolNode.SymbolType.Event_End))
                             xmlew.add(xmlef.createAttribute(new QName(Attribute.TYPE), ActivityDefinitionLight.TYPE_END + "")); 
                         if (node.getType().equals(SymbolNode.SymbolType.Fork))
                             xmlew.add(xmlef.createAttribute(new QName(Attribute.TYPE), ActivityDefinitionLight.TYPE_PARALLEL + "")); 
                         if (node.getType().equals(SymbolNode.SymbolType.Exclusive))
                             xmlew.add(xmlef.createAttribute(new QName(Attribute.TYPE), ActivityDefinitionLight.TYPE_CONDITIONAL + ""));               
                     } else
                             xmlew.add(xmlef.createAttribute(new QName(Attribute.TYPE), ActivityDefinitionLight.TYPE_NORMAL + ""));               
                     xmlew.add(xmlef.createStartElement(tagPaths, null, null));
                     for (BPMNConnection conn : connections) {
                         if (conn.getSource().equals(objectNode.getUuid())) {
                             xmlew.add(xmlef.createStartElement(tagPath, null, null));
                             xmlew.add(xmlef.createCharacters(conn.getTarget()));
                             xmlew.add(xmlef.createEndElement(tagPath, null));
                         }
                     }
                     xmlew.add(xmlef.createEndElement(tagPaths, null));
                     
                    // --> init artifactDefinition
                    if (listArtifact != null) {
                        for (ArtifactDefinition artifact : listArtifact) {
                            if (objectNode.getUuid().equals(artifact.getBpmnNode().getUuid())) {
                                xmlew.add(xmlef.createStartElement(tagArtifactDefinition, null, null));
                                xmlew.add(xmlef.createAttribute(new QName(Attribute.ID), artifact.getId()));
                                xmlew.add(xmlef.createAttribute(new QName(Attribute.TYPE), String.valueOf(artifact.getType())));
                                
                                xmlew.add(xmlef.createStartElement(tagParameters, null, null));
                                if (artifact.getParameters() != null && artifact.getParameters().size() > 0) {
                                    artifact.getParameters().forEach((k, v) -> {
                                        try {
                                            xmlew.add(xmlef.createStartElement(tagParameter, null, null));
                                            xmlew.add(xmlef.createAttribute(new QName(Attribute.NAME), k));
                                            xmlew.add(xmlef.createCharacters(v));
                                            xmlew.add(xmlef.createEndElement(tagParameter, null));
                                        } catch (XMLStreamException ex) {
                                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                                    AbstractNotification.NotificationType.ERROR, ts).open();
                                        }
                                    });
                                }
                                xmlew.add(xmlef.createEndElement(tagParameters, null));
                                xmlew.add(xmlef.createEndElement(tagArtifactDefinition, null));
                            }
                        }
                    }
                    // end artifactDefinition <--
                    xmlew.add(xmlef.createEndElement(tagActivityDefinition, null));
                }
            }
            xmlew.add(xmlef.createEndElement(tagActivityDefinitions, null));
             
            xmlew.add(xmlef.createStartElement(tagDiagram, null, null));
             
            for (BPMNNode sw : mapActorLanes.values()) {
                xmlew.add(xmlef.createStartElement(tagSwimlane, null, null));
                xmlew.add(xmlef.createAttribute(new QName(Attribute.BPMN_ELEMENT), sw.getUuid())); 
                xmlew.add(xmlef.createAttribute(new QName(Attribute.WIDTH), String.valueOf(sw.getWidth()))); 
                xmlew.add(xmlef.createAttribute(new QName(Attribute.HEIGHT), String.valueOf(sw.getHeight()))); 
                xmlew.add(xmlef.createAttribute(new QName(Attribute.X), String.valueOf(sw.getX()))); 
                xmlew.add(xmlef.createAttribute(new QName(Attribute.Y), String.valueOf(sw.getY()))); 
                xmlew.add(xmlef.createEndElement(tagSwimlane, null));
            }
            
            for (BPMNNode objectNode : diagram.getBPMNNodes()) {
                if (objectNode instanceof SymbolNode || objectNode instanceof TaskNode 
                        || objectNode instanceof LabelNode) {
                    xmlew.add(xmlef.createStartElement(tagShape, null, null));
                    xmlew.add(xmlef.createAttribute(new QName(Attribute.ID), objectNode.getUuid()));
                    xmlew.add(xmlef.createAttribute(new QName(Attribute.ACTOR_ID), objectNode.getCellParent()));
                    xmlew.add(xmlef.createAttribute(new QName(Attribute.WIDTH), String.valueOf(objectNode.getWidth())));
                    xmlew.add(xmlef.createAttribute(new QName(Attribute.HEIGHT), String.valueOf(objectNode.getHeight())));
                    xmlew.add(xmlef.createAttribute(new QName(Attribute.X), String.valueOf(objectNode.getX())));
                    xmlew.add(xmlef.createAttribute(new QName(Attribute.Y), String.valueOf(objectNode.getY())));
                    xmlew.add(xmlef.createAttribute(new QName(Attribute.IS_LABEL), objectNode instanceof LabelNode ? "true" : "false"));
                    if (objectNode instanceof SymbolNode) {
                        SymbolNode node = (SymbolNode) objectNode;
                        if (node.getType().equals(SymbolNode.SymbolType.Event))
                            xmlew.add(xmlef.createAttribute(new QName(Attribute.TYPE), ActivityDefinitionLight.TYPE_START + ""));
                        if (node.getType().equals(SymbolNode.SymbolType.Event_End))
                            xmlew.add(xmlef.createAttribute(new QName(Attribute.TYPE), ActivityDefinitionLight.TYPE_END + ""));
                        if (node.getType().equals(SymbolNode.SymbolType.Fork))
                            xmlew.add(xmlef.createAttribute(new QName(Attribute.TYPE), ActivityDefinitionLight.TYPE_PARALLEL + ""));
                        if (node.getType().equals(SymbolNode.SymbolType.Exclusive))
                            xmlew.add(xmlef.createAttribute(new QName(Attribute.TYPE), ActivityDefinitionLight.TYPE_CONDITIONAL + ""));
                    } else
                        xmlew.add(xmlef.createAttribute(new QName(Attribute.TYPE), ActivityDefinitionLight.TYPE_NORMAL + ""));

                    xmlew.add(xmlef.createCharacters(objectNode.getLabel()));
                    xmlew.add(xmlef.createEndElement(tagShape, null));                
                }                     
            }
            
            for (BPMNConnection conn : connections) {
                 xmlew.add(xmlef.createStartElement(tagEdge, null, null));
                 xmlew.add(xmlef.createAttribute(new QName(Attribute.SOURCE), conn.getSource()));
                 xmlew.add(xmlef.createAttribute(new QName(Attribute.TARGET), conn.getTarget()));
                 xmlew.add(xmlef.createAttribute(new QName(Attribute.NAME), conn.getLabel()));
                 for (Point point : conn.getPointList()) {
                    xmlew.add(xmlef.createStartElement(tagControlPoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName(Attribute.X), String.valueOf(point.getX())));
                    xmlew.add(xmlef.createAttribute(new QName(Attribute.Y), String.valueOf(point.getY())));
                    xmlew.add(xmlef.createEndElement(tagControlPoint, null));
                 }
                 xmlew.add(xmlef.createEndElement(tagEdge, null));                
            }
             
            xmlew.add(xmlef.createEndElement(tagDiagram, null));
            xmlew.add(xmlef.createEndElement(tagProcessDefinition, null));

            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            return new byte[0];
        }
    }

    /**
     * Shows the available user groups. 
     * Adds a lane with the selected group.
     */ 
    private void addLane() {
        ConfirmDialog wdwAddLane = new ConfirmDialog(ts, ts.getTranslatedString("module.processeditor.add-lane"),
                ts.getTranslatedString("module.general.messages.ok"));
        
        List<GroupProfile> users = aem.getGroups();
        ComboBox<GroupProfile> cmbGroups = new ComboBox<>(ts.getTranslatedString("module.processeditor.select-user-group"));
        cmbGroups.setItems(users);
        cmbGroups.setSizeFull();
        
        wdwAddLane.setContent(cmbGroups);
        wdwAddLane.getBtnConfirm().addClickListener(listener -> {
            if (cmbGroups.getValue() == null) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.processeditor.select-actor-first"), 
                            AbstractNotification.NotificationType.WARNING, ts).open();  
                return;
            }
            if (mapActorLanes.keySet().stream().filter(item ->
                    item.getName().equals(cmbGroups.getValue().getName())).findAny().isPresent()) {
                 new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.processeditor.select-actor-already-added"), 
                            AbstractNotification.NotificationType.WARNING, ts).open();   
                 return;
            }
            
            SwimlaneNode newNode = new SwimlaneNode(diagram);
            newNode.getProperties().put(Tag.ACTOR, cmbGroups.getValue());
            newNode.setLabel(cmbGroups.getValue().getName());
            newNode.setX(100);
            newNode.setY(100);
            diagram.addNode(newNode);
            mapActorLanes.put(new Actor(newNode.getUuid(), cmbGroups.getValue().getName(), 2), newNode);
            
            wdwAddLane.close();
        });
         
        wdwAddLane.open();
    }

    /**
     * Open the process with the given name. 
     * The process is searched in the process definition folder.
     * @param processName The process file name. 
     */
    private void openProcessDefinition(String processName) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
//      <editor-fold defaultstate="collapsed" desc="Uncomment this for debugging purposes. This outputs the XML view as a file">
//        try (FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/TOPO_VIEW_V2.1.xml")) {
//            fos.write(structure);
//        } catch (Exception e) {
//        }
//      </editor-fold>
        QName tagProcessDefinition = new QName(Tag.PROCESS_DEFINITION);
        QName tagActors = new QName(Tag.ACTORS);
        QName tagActor = new QName(Tag.ACTOR);
        QName tagActivityDefinitions = new QName(Tag.ACTIVITY_DEFINITIONS);
        QName tagActivityDefinition = new QName(Tag.ACTIVITY_DEFINITION);
        QName tagArtifactDefinition = new QName(Tag.ARTIFACT_DEFINITION);
        QName tagPaths = new QName(Tag.PATHS);
        QName tagPath = new QName(Tag.PATH);
        QName tagParameters = new QName(Tag.PARAMETERS);
        QName tagParameter = new QName(Tag.PARAMETER);
        QName tagKpis = new QName(Tag.KPIS);
        QName tagKpi = new QName(Tag.KPI);
        QName tagActions = new QName(Tag.ACTIONS);
        QName tagAction = new QName(Tag.ACTION);
        QName tagThreshold = new QName(Tag.THRESHOLD);
        QName tagShape = new QName(Tag.BPMNSHAPE);
        QName tagEdge = new QName(Tag.BPMNEDGE);
        QName tagControlPoint = new QName(Tag.CONTROLPOINT);
        QName tagDiagram = new QName(Tag.BPMNDIAGRAM);
        QName tagSwimlane = new QName(Tag.BPMNSWIMLANE);

        try {
            List<Actor> lstActors = new ArrayList<>();
            cleanDiagram();
            File file = new File(processName);             
            InputStream theStream = new FileInputStream(file);
            List<GroupProfile> groups = aem.getGroups();
            XMLStreamReader reader = inputFactory.createXMLStreamReader(theStream);
            String startAct = "";
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagProcessDefinition)) {
                        startAct =  reader.getAttributeValue(null, Attribute.START_ACTIVITY_ID);
                        currentProcessId = reader.getAttributeValue(null, Attribute.ID);
                        currentProcessName = reader.getAttributeValue(null, Attribute.NAME);
                        currentVersion = reader.getAttributeValue(null, Attribute.VERSION);
                        currentDescription = reader.getAttributeValue(null, Attribute.DESCRIPTION);
                        processEnabled = reader.getAttributeValue(null, Attribute.ENABLED);
                        
                        switch (processEnabled) {
                            case "1":
                                chxEnabled.setValue(true);
                                break;
                            default:
                                chxEnabled.setValue(false);
                                break;
                        }

                        lblProcessDefinition.setText(String.format(ts.getTranslatedString("module.processeditor.header"), currentProcessName));
                        lblProcessDefinition.setVisible(true);
                    } else if (reader.getName().equals(tagActor)) {
                         String actorId = reader.getAttributeValue(null, Attribute.ID);
                         String name = reader.getAttributeValue(null, Attribute.NAME);
                         String type = reader.getAttributeValue(null, Attribute.TYPE);   
                         lstActors.add(new Actor(actorId, name, new Integer(type)));
                    }  else if (reader.getName().equals(tagActivityDefinition)) {
                        int type = Integer.valueOf(reader.getAttributeValue(null, Attribute.TYPE));
                        switch (type) {
                            case ActivityDefinitionLight.TYPE_PARALLEL:
                                bpmnActivity = new SymbolNode(diagram, SymbolNode.SymbolType.Fork);
                                break;
                            case ActivityDefinitionLight.TYPE_CONDITIONAL:
                                bpmnActivity = new SymbolNode(diagram, SymbolNode.SymbolType.Exclusive);
                                break;
                            case ActivityDefinitionLight.TYPE_START:
                                bpmnActivity = new SymbolNode(diagram, SymbolNode.SymbolType.Event);
                                break;
                            case ActivityDefinitionLight.TYPE_END:
                                bpmnActivity = new SymbolNode(diagram, SymbolNode.SymbolType.Event_End);
                                break;
                            default:
                                bpmnActivity = new TaskNode(diagram);
                                break;
                        }
                        bpmnActivity.setUuid(reader.getAttributeValue(null, Attribute.ID));
                    } else if (tagArtifactDefinition.equals(reader.getName())) {
                        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
                        artifactDefinition.setType(Integer.valueOf(reader.getAttributeValue(null, Attribute.TYPE) != null
                                && !reader.getAttributeValue(null, Attribute.TYPE).isEmpty()
                                ? reader.getAttributeValue(null, Attribute.TYPE) : "0"));
                        artifactDefinition.setId(reader.getAttributeValue(null, Attribute.ID) != null
                                && !reader.getAttributeValue(null,Attribute.ID).isEmpty()
                                ? reader.getAttributeValue(null,Attribute.ID) : "");
                        artifactDefinition.setBpmnNode(bpmnActivity);

                        while (true) {
                            HashMap parameterList = new HashMap();
                            if (tagArtifactDefinition.equals(reader.getName()))
                                reader.nextTag();
                            while (true) {
                                reader.nextTag();
                                if (tagParameter.equals(reader.getName())) {
                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                        parameterList.put(reader.getAttributeValue(null, Attribute.NAME) != null
                                                && !reader.getAttributeValue(null, Attribute.NAME).isEmpty()
                                                ? reader.getAttributeValue(null, Attribute.NAME) : "",
                                                reader.getElementText());
                                } else
                                    break;
                                artifactDefinition.setParameters(parameterList);
                            }
                            break;
                        }
                        listArtifact.add(artifactDefinition);
                    } else if (reader.getName().equals(tagSwimlane)) {
                        String laneId = reader.getAttributeValue(null, Attribute.BPMN_ELEMENT);
                        double width = Double.valueOf(reader.getAttributeValue(null, Attribute.WIDTH));
                        double height = Double.valueOf(reader.getAttributeValue(null, Attribute.HEIGHT));
                        String color = reader.getAttributeValue(null, Attribute.COLOR);
                        
                        Optional<Actor> op = lstActors.stream().filter(item -> item.getId().equals(laneId)).findAny();
                        if (op.isPresent()) {
                            SwimlaneNode actorNode = new SwimlaneNode(diagram);
                            actorNode.getProperties().put(Tag.ACTOR, op.get());
                            actorNode.setLabel(op.get().getName());
                            actorNode.setUuid(laneId);
                            actorNode.setWidth(width);
                            actorNode.setHeight(height);
                            
                            HashMap<Actor, String> colors = new HashMap();
                            LinkedHashMap<String, String> mapStyle = new LinkedHashMap();
                            mapStyle.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_SWIMLANE);
                            mapStyle.put("fontStyle", "1");
                            mapStyle.put("html", "1");
                            mapStyle.put("startSize", "40");
                            mapStyle.put(MxConstants.STYLE_HORIZONTAL, "0");
                            mapStyle.put(MxConstants.STYLE_VERTICAL_ALIGN, "middle");
                            if (color != null) {
                                mapStyle.put(MxConstants.STYLE_FILLCOLOR, color);
                                colors.put(op.get(), color);
                            }
                            actorNode.setRawStyle(mapStyle);
                            
                            diagram.addNode(actorNode);
                            mapActorLanes.put(op.get(), actorNode);
                        }
                    } else if (reader.getName().equals(tagShape)) {
                        String actorId = reader.getAttributeValue(null, Attribute.ACTOR_ID);
                        String id = reader.getAttributeValue(null, Attribute.ID);
                        boolean isLabel = Boolean.valueOf(reader.getAttributeValue(null, Attribute.IS_LABEL));
                        double width = Double.valueOf(reader.getAttributeValue(null, Attribute.WIDTH));
                        double height = Double.valueOf(reader.getAttributeValue(null, Attribute.HEIGHT));
                        double x = Double.valueOf(reader.getAttributeValue(null, Attribute.X));
                        double y = Double.valueOf(reader.getAttributeValue(null, Attribute.Y));
                        Optional<SwimlaneNode> op = mapActorLanes.values().stream().filter(item -> item.getUuid().equals(actorId)).findAny();
                        if (op.isPresent()) {
                            BPMNNode newNode;
                            if (!isLabel) {
                                int type = Integer.valueOf(reader.getAttributeValue(null, Attribute.TYPE));
                                switch (type) {
                                    case ActivityDefinitionLight.TYPE_PARALLEL:
                                        newNode = new SymbolNode(diagram, SymbolNode.SymbolType.Fork);
                                        break;
                                    case ActivityDefinitionLight.TYPE_CONDITIONAL:
                                        newNode = new SymbolNode(diagram, SymbolNode.SymbolType.Exclusive);
                                        break;
                                    case ActivityDefinitionLight.TYPE_START:
                                        newNode = new SymbolNode(diagram, SymbolNode.SymbolType.Event);
                                        break;
                                    case ActivityDefinitionLight.TYPE_END:
                                        newNode = new SymbolNode(diagram, SymbolNode.SymbolType.Event_End);
                                        break;
                                    default:
                                        newNode = new TaskNode(diagram);
                                        break;
                                }
                            } else
                                newNode = new LabelNode(diagram);

                            newNode.setLabel(reader.getElementText());
                            newNode.setUuid(id);
                            newNode.setGeometry(x, y, width, height);
                            newNode.addCellParentChangedListener(evt -> newNode.setCellParent(actorId));
                            
                            if (id.equals(startAct))
                               startNode = (SymbolNode) newNode;
                            addBpmnTool(newNode);
                            diagram.addNode(newNode);
                        }
                    } else if (reader.getName().equals(tagEdge)) {
                        String source = reader.getAttributeValue(null, Attribute.SOURCE);
                        String target = reader.getAttributeValue(null, Attribute.TARGET);
                        String name = reader.getAttributeValue(null, Attribute.NAME);
                        
                        List<Point> controlPoints = new ArrayList();
                        while (true) {
                            reader.nextTag();
                            if (tagControlPoint.equals(reader.getName())) {
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                    controlPoints.add(new Point(
                                            Double.valueOf(reader.getAttributeValue(null, Attribute.X)),
                                            Double.valueOf(reader.getAttributeValue(null, Attribute.Y))
                                    ));
                            } else
                                break;
                        }
                        
                        BPMNConnection conn = new BPMNConnection(diagram);
                        conn.setLabel(name);
                        conn.setSource(source);
                        conn.setTarget(target);
                        conn.setPoints(controlPoints);
                        diagram.addEdge(conn);
                    }
                }        
             }
            btnSaveDiagram.setEnabled(false);
            enableActionButtons(true);
        } catch (IOException | XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Method used to render existent process definitions. Temporal method used
     * only to render existent diagrams.
     * @param activity the start activity node
     * @param previousNode previous node to build edges
     * @param linkLabel helper parameter to send the edge label.
     */
    void addActivity(ActivityDefinition activity, BPMNNode previousNode, String linkLabel) {
        if (activity == null)
            return;
        Actor actor = activity.getActor();
            SwimlaneNode swimlaneActor;
            if (actor != null) {
                swimlaneActor = mapActors.get(actor);
                if (swimlaneActor == null) {
                    swimlaneActor = new SwimlaneNode(diagram);
                    swimlaneActor.setUuid("0" + actor.getId());
                    swimlaneActor.setLabel(actor.getName());
                    mapActors.put(actor, swimlaneActor);
                    List<GroupProfile> groups = aem.getGroups();
//                    Optional<GroupProfile> op = groups.stream().filter(item -> item.getName().equals(actor.getName())).findAny();
                    mapActorLanes.put(actor, swimlaneActor);
                    diagram.addNode(swimlaneActor);
                }                          
            } 
            BPMNNode currentNode = mapNodes.get(activity);
            boolean newNode = false;
            if (currentNode == null) {
                newNode = true;
                switch (activity.getType()) {

                    case ActivityDefinitionLight.TYPE_START:
                        currentNode = new SymbolNode(diagram, SymbolNode.SymbolType.Event);
                        startNode = (SymbolNode) currentNode;
                        break;
                    case ActivityDefinitionLight.TYPE_CONDITIONAL:
                         currentNode = new SymbolNode(diagram, SymbolNode.SymbolType.Exclusive);
                        break;
                    case ActivityDefinitionLight.TYPE_END:
                        currentNode = new SymbolNode(diagram, SymbolNode.SymbolType.Event_End);
                        break;
                    case ActivityDefinitionLight.TYPE_PARALLEL:
                        currentNode = new SymbolNode(diagram, SymbolNode.SymbolType.Fork);
                        break;
                    default:
                        currentNode = new TaskNode(diagram);                       
                        break;             
                }
                currentNode.setUuid("act_" + activity.getId());
                currentNode.setCellParent("0" + actor.getId());
                currentNode.setLabel(activity.getName());
                diagram.addNode(currentNode);
                mapNodes.put(activity, currentNode);
            }
            
            if (previousNode != null) {
                BPMNConnection link = new BPMNConnection(diagram);
                link.setSource(previousNode.getUuid());
                link.setTarget(currentNode.getUuid());
                link.setLabel(linkLabel);
                diagram.addEdge(link);
            }
            
            lastNodeAdded = currentNode;
            if (!newNode)
                return;
            if (activity instanceof ConditionalActivityDefinition) {
                addActivity(((ConditionalActivityDefinition) activity).getNextActivityIfFalse(), currentNode, "FALSE");
                addActivity(((ConditionalActivityDefinition) activity).getNextActivityIfTrue(), currentNode, "TRUE");
                
            } else if (activity instanceof ParallelActivityDefinition) {
                ParallelActivityDefinition pad = ((ParallelActivityDefinition) activity);
                if (pad.getPaths() != null) {
                    for (ActivityDefinition act : pad.getPaths()) 
                         addActivity(act, currentNode, "");
                }
            } else
                addActivity(activity.getNextActivity(), currentNode, "");
    }

    public void cleanDiagram() {
        this.diagram.removeAllCells();
    }

    /**
     * Dialog to create a new process definition.
     * Defines a name, version and description (optional).
     */
    private void createNewProcessDialog() {
        TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));      
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();
        
        TextField txtVersion = new TextField(ts.getTranslatedString("module.general.labels.version"));
        txtVersion.setRequiredIndicatorVisible(true);
        txtVersion.setValue("2.0");
        txtVersion.setSizeFull();
        
        TextArea txtDescription = new TextArea(ts.getTranslatedString("module.general.labels.description"));
        txtDescription.setSizeFull();
        
        ConfirmDialog wdwNewProcess = new ConfirmDialog(ts
            , ts.getTranslatedString("module.processeditor.process-actions.new-process-name")
            , ts.getTranslatedString("module.general.labels.create"));
        wdwNewProcess.setDraggable(true);
        wdwNewProcess.setResizable(true);
        wdwNewProcess.getBtnConfirm().addClickListener(e -> {
            try {
                if (txtName.getValue() == null || txtName.getValue().trim().isEmpty())
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                            String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                     ts.getTranslatedString("module.general.labels.name")),
                            AbstractNotification.NotificationType.WARNING, ts).open();
                else if (txtVersion.getValue() == null || txtVersion.getValue().trim().isEmpty())
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                            String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                    ts.getTranslatedString("module.general.labels.version")),
                            AbstractNotification.NotificationType.WARNING, ts).open();
                else {
                    currentProcessName = txtName.getValue();
                    currentVersion = txtVersion.getValue();
                    currentDescription = txtDescription.getValue();
                    currentCreationDate = System.currentTimeMillis();
                    currentProcessId = UUID.randomUUID().toString();
                    
                    chxEnabled.setValue(false);
                    renderEnable(chxEnabled.getValue());
                    
                    wdwNewProcess.close();
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            String.format(ts.getTranslatedString("module.processeditor.process-actions.new-process-success"),
                                    currentProcessName), AbstractNotification.NotificationType.INFO, ts).open();
                    
                    resetCanvas();
                    btnSaveDiagram.setEnabled(false);
                    enableActionButtons(true);
                    
                    lblProcessDefinition.setText(String.format(ts.getTranslatedString("module.processeditor.header"), currentProcessName));
                    lblProcessDefinition.setVisible(true);
                }
            } catch (Exception ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
                wdwNewProcess.close();
            }
        });        
        wdwNewProcess.setContent(txtName, txtVersion, txtDescription);
        wdwNewProcess.open();
    }
    
    /**
     * Updates process definition properties.
     * Name, version, description and enable.
     */
    private void updateProcessDialog() {
        TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));      
        txtName.setRequiredIndicatorVisible(true);
        txtName.setValue(currentProcessName);
        txtName.setSizeFull();
        
        TextField txtVersion = new TextField(ts.getTranslatedString("module.general.labels.version"));
        txtVersion.setRequiredIndicatorVisible(true);
        txtVersion.setValue(currentVersion);
        txtVersion.setSizeFull();
        
        TextArea txtDescription = new TextArea(ts.getTranslatedString("module.general.labels.description"));
        txtDescription.setValue(currentDescription);
        txtDescription.setSizeFull();
        
        if (processEnabled.equals("1"))
            chxEnabled.setValue(true);
        else
            chxEnabled.setValue(false);
        
        ConfirmDialog wdwUpdateProcess = new ConfirmDialog(ts
            , ts.getTranslatedString("module.general.label.action-edit-properties")
            , ts.getTranslatedString("module.general.messages.ok"));
        wdwUpdateProcess.setDraggable(true);
        wdwUpdateProcess.setResizable(true);
        wdwUpdateProcess.getBtnConfirm().addClickListener(e -> {
            try {
                if (txtName.getValue() == null || txtName.getValue().trim().isEmpty())
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                            String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                     ts.getTranslatedString("module.general.labels.name")),
                            AbstractNotification.NotificationType.WARNING, ts).open();
                else if (txtVersion.getValue() == null || txtVersion.getValue().trim().isEmpty())
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                            String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                    ts.getTranslatedString("module.general.labels.version")),
                            AbstractNotification.NotificationType.WARNING, ts).open();
                else {
                    currentProcessName = txtName.getValue();
                    currentVersion = txtVersion.getValue();
                    currentDescription = txtDescription.getValue();
                    currentCreationDate = System.currentTimeMillis();
                    currentProcessId = UUID.randomUUID().toString();
                    renderEnable(chxEnabled.getValue());
                    
                    lblProcessDefinition.setText(String.format(ts.getTranslatedString("module.processeditor.header"), currentProcessName));
                    lblProcessDefinition.setVisible(true);
                    
                    wdwUpdateProcess.close();
                }
            } catch (Exception ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
                wdwUpdateProcess.close();
            }
        });        
        wdwUpdateProcess.setContent(txtName, txtVersion, chxEnabled, txtDescription);
        wdwUpdateProcess.open();
    }

    private void resetCanvas() {
        diagram.removeAllCells();
        startNode = null;
        mapActorLanes = new LinkedHashMap<>();   
        listArtifact = new LinkedList<>();
        listCurrentArtifacts = new LinkedList<>();
    }
    
    private void enableActionButtons(boolean show) {
        if (startNode != null)
            btnSaveDiagram.setEnabled(show);
        
        btnEvent.setEnabled(show);
        btnUpdate.setEnabled(show);
        btnSwimlane.setEnabled(show);
        btnGateway.setEnabled(show);
        btnTask.setEnabled(show);
    }

    private void renderEnable(Boolean value) {
        processEnabled = "";
        if (value)
            processEnabled = "1";
        else
            processEnabled = "0";
    }
}