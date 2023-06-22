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
package com.neotropic.kuwaiba.modules.commercial.processman.diagram;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.Point;
import com.neotropic.flow.component.mxgraph.bpmn.BPMNConnection;
import com.neotropic.flow.component.mxgraph.bpmn.BPMNDiagram;
import com.neotropic.flow.component.mxgraph.bpmn.BPMNNode;
import com.neotropic.flow.component.mxgraph.bpmn.LabelNode;
import com.neotropic.flow.component.mxgraph.bpmn.SwimlaneNode;
import com.neotropic.flow.component.mxgraph.bpmn.SymbolNode;
import com.neotropic.flow.component.mxgraph.bpmn.TaskNode;
import com.neotropic.kuwaiba.modules.commercial.processman.diagram.mx.MxProcessInstanceDiagramProvider;
import com.neotropic.kuwaiba.modules.commercial.processman.diagram.provider.ActivityNode;
import com.neotropic.kuwaiba.modules.commercial.processman.diagram.provider.ProcessDefinitionDiagramProvider;
import com.neotropic.kuwaiba.modules.commercial.processman.wdw.ArtifactWindow;
import com.neotropic.kuwaiba.modules.commercial.whman.persistence.WarehousesService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.Command;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Actor;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActorAuthorizationManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ConditionalActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ParallelActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.ProcessDefinitionLoader.Tag;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.ProcessDefinitionLoader.Attribute;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * UI diagram to process instance.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ProcessInstanceDiagram extends Div {
    private final String processEnginePath;
    private final ProcessDefinition processDefinition;
    private final ProcessInstance processInstance;
    private final ActorAuthorizationManager actorAuthorizationManager;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final WarehousesService ws;
    private final TranslationService ts;
    
    private ProcessDefinitionDiagramProvider diagramProvider;
    private final HashMap<ActivityDefinition, ActivityNode> activities = new HashMap();
    
    private final HashMap<ActivityDefinition, BPMNNode> nodes = new HashMap();
    private final HashMap<Actor, BPMNNode> actors = new HashMap();
    /**
     * TODO: Remove once the coordinates are loaded from the xml
     */
    @Deprecated
    private ActivityDefinition lastActivity;
    private Command cmdUpdateDiagram;
    private final ActionButton btnOutline;
    private boolean outline = false;
    
    public ProcessInstanceDiagram(String processEnginePath, 
        ProcessDefinition processDefinition, ProcessInstance processInstance, 
        ActorAuthorizationManager actorAuthorizationManager,
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, WarehousesService ws, TranslationService ts,
        ActionButton btnOutline) {
        setSizeFull();
        Objects.requireNonNull(processEnginePath);
        Objects.requireNonNull(processDefinition);
        Objects.requireNonNull(processInstance);
        Objects.requireNonNull(actorAuthorizationManager);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        this.processEnginePath = processEnginePath;
        this.processDefinition = processDefinition;
        this.processInstance = processInstance;
        this.actorAuthorizationManager = actorAuthorizationManager;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.ws = ws;
        this.btnOutline = btnOutline;
    }
    
    private void buildDiagramVersion1() {
        diagramProvider = new MxProcessInstanceDiagramProvider();

        cmdUpdateDiagram = () -> {
            try {
                List<ActivityDefinition> path = aem.getProcessInstanceActivitiesPath(processInstance.getId());
                path.forEach(activity -> activities.get(activity).setEnabled(true));
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        };
        addActivity(processDefinition.getStartActivity(), null, null);
        if (lastActivity != null)
            diagramProvider.executeLayout(lastActivity);
        add((Component) diagramProvider.getUiElement());
    }
    
    private void buildDiagramVersion2() {
        if (processDefinition.getDefinition() != null) {
            HashMap<Actor, String> colors = new HashMap();
            List<BPMNConnection> edges = new ArrayList();
            
            BPMNDiagram diagram = new BPMNDiagram();
            // To display resolution greater than or equal to 1280x720
            Command cmdOutline = () -> {
                outline = !outline;
                diagram.setHasOutline(outline);
                if (outline)
                    diagram.setHeight("79vh");
                else
                    diagram.setHeight("91vh");
            };
            btnOutline.addClickListener(clickEvent -> cmdOutline.execute());
            cmdOutline.execute();
            diagram.setWidth("100vw");
            diagram.setOverflow("scroll");
            diagram.setOutlineWidth("20vw");
            diagram.setOutlineHeight("10vh");
            diagram.setCustomOutlinePosition("margin-top: 5px; margin-left: 40vw; margin-right: 40vw;");
            diagram.setBeginUpdateOnInit(true);
            
            cmdUpdateDiagram = () -> {
                try {
                    List<ActivityDefinition> path = aem.getProcessInstanceActivitiesPath(processInstance.getId());
                    HashMap<String, ActivityDefinition> pathMap = new HashMap();
                    path.forEach(activityDefinition -> pathMap.put(activityDefinition.getId(), activityDefinition));
                    
                    edges.forEach(edge -> {
                        if (pathMap.containsKey(edge.getSource()) && pathMap.containsKey(edge.getTarget()))
                            edge.setStyle(MxConstants.STYLE_STROKECOLOR, "#32CD32");
                    });
                    for (int i = 0; i < path.size() - 1; i++) {
                        ActivityDefinition activityDefinition = path.get(i);
                        
                        if (colors.containsKey(activityDefinition.getActor())) {
                            nodes.get(activityDefinition).setStyle(MxConstants.STYLE_FILLCOLOR, colors.get(activityDefinition.getActor()));
                            nodes.get(activityDefinition).setStyle(MxConstants.STYLE_STROKEWIDTH, "1");
                            nodes.get(activityDefinition).setStyle(MxConstants.STYLE_STROKECOLOR, "1");
                            nodes.get(activityDefinition).setStyle(MxConstants.STYLE_STROKECOLOR, "inherit");
                        }
                        boolean isActivityRunningInParallel = DiagramUtil.isActivityRunningInParallel(path, activityDefinition);
                        if (activityDefinition.isIdling() || isActivityRunningInParallel) {
                            boolean idling = true;
                            try {
                                Artifact artifact = aem.getArtifactForActivity(processInstance.getId(), activityDefinition.getId());
                                
                                for (StringPair shared : artifact.getSharedInformation()) {
                                    if (Artifact.SHARED_KEY_IDLE.equals(shared.getKey())) {
                                        if (Boolean.valueOf(shared.getValue())) {
                                            nodes.get(activityDefinition).setStyle(MxConstants.STYLE_STROKEWIDTH, "3");
                                            nodes.get(activityDefinition).setStyle(MxConstants.STYLE_STROKECOLOR, "#FFD700");
                                        }
                                        idling = false;
                                        break;
                                    }
                                }
                            } catch (InventoryException ex) {
                            }
                            if (idling) {
                                nodes.get(activityDefinition).setStyle(MxConstants.STYLE_STROKEWIDTH, "3");
                                nodes.get(activityDefinition).setStyle(MxConstants.STYLE_STROKECOLOR, "#FFD700");
                            }
                        }
                    }
                    if (!path.isEmpty()) {
                        ActivityDefinition activity = path.get(path.size() - 1);
                        nodes.get(activity).setStyle(MxConstants.STYLE_STROKEWIDTH, "3");
                        nodes.get(activity).setStyle(MxConstants.STYLE_STROKECOLOR, "#00FF00");
                    }
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            };
            diagram.addGraphLoadedListener(event -> {
                event.unregisterListener();
                diagram.enablePanning(true);
                try {
                    UserProfile user = UI.getCurrent().getSession().getAttribute(Session.class).getUser();
                    List<ActivityDefinition> path = aem.getProcessInstanceActivitiesPath(processInstance.getId());
                    HashMap<String, ActivityDefinition> pathMap = new HashMap();
                    path.forEach(activityDefinition -> pathMap.put(activityDefinition.getId(), activityDefinition));
                    
                    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                    ByteArrayInputStream bais = new ByteArrayInputStream(processDefinition.getDefinition());
                    XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

                    QName tagBpmnSwimlane = new QName(Tag.BPMNSWIMLANE);
                    QName tagBmpnShape = new QName(Tag.BPMNSHAPE);
                    QName tagBpmnEdge = new QName(Tag.BPMNEDGE);
                    QName tagControlPoint = new QName(Tag.CONTROLPOINT);
                    
                    while (reader.hasNext()) {
                        reader.next();
                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                            if (reader.getName().equals(tagBpmnSwimlane)) {
                                String bpmnElement = reader.getAttributeValue(null, Attribute.BPMN_ELEMENT);
                                if (processDefinition.getActors().containsKey(bpmnElement)) {
                                    Actor actor = processDefinition.getActors().get(bpmnElement);

                                    double width = Double.valueOf(reader.getAttributeValue(null, Attribute.WIDTH));
                                    double height = Double.valueOf(reader.getAttributeValue(null, Attribute.HEIGHT));
                                    String color = reader.getAttributeValue(null, Attribute.COLOR);

                                    SwimlaneNode actorNode = new SwimlaneNode(diagram);
                                    actorNode.setAddsOverlayButtons(false);
                                    actorNode.setLabel(actor.getName());
                                    actorNode.setUuid(bpmnElement);
                                    actorNode.setWidth(width);
                                    actorNode.setHeight(height);
                                    actorNode.setIsEditable(false);
                                    
                                    LinkedHashMap<String, String> mapStyle = new LinkedHashMap();
                                    mapStyle.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_SWIMLANE);
                                    mapStyle.put("fontStyle", "1");
                                    mapStyle.put("html", "1");
                                    mapStyle.put("startSize", "40");
                                    mapStyle.put(MxConstants.STYLE_HORIZONTAL, "0");
                                    mapStyle.put(MxConstants.STYLE_VERTICAL_ALIGN, "middle");
                                    if (color != null) {
                                        mapStyle.put(MxConstants.STYLE_FILLCOLOR, color);
                                        colors.put(actor, color);
                                    }
                                    if (actorAuthorizationManager.existGroup(user, actor))
                                        mapStyle.put(MxConstants.STYLE_SWIMLANE_FILLCOLOR, "#F5F5F5");
                                    actorNode.setRawStyle(mapStyle);
                                    
                                    diagram.addNode(actorNode);
                                    actors.put(actor, actorNode);
                                }
                            } else if (reader.getName().equals(tagBmpnShape)) {
                                String actorId = reader.getAttributeValue(null, Attribute.ACTOR_ID);
                                String id = reader.getAttributeValue(null, Attribute.ID);
                                double x = Double.valueOf(reader.getAttributeValue(null, Attribute.X));
                                double y = Double.valueOf(reader.getAttributeValue(null, Attribute.Y));
                                double width = Double.valueOf(reader.getAttributeValue(null, Attribute.WIDTH));
                                double height = Double.valueOf(reader.getAttributeValue(null, Attribute.HEIGHT));
                                boolean isLabel = Boolean.valueOf(reader.getAttributeValue(null, Attribute.IS_LABEL));

                                if (processDefinition.getActors().containsKey(actorId)) {
                                    BPMNNode node;
                                    if (!isLabel) {
                                        int type = Integer.valueOf(reader.getAttributeValue(null, Attribute.TYPE));
                                        switch (type) {
                                            case ActivityDefinition.TYPE_PARALLEL:
                                                node = new SymbolNode(diagram, SymbolNode.SymbolType.Fork);
                                                break;
                                            case ActivityDefinition.TYPE_CONDITIONAL:
                                                node = new SymbolNode(diagram, SymbolNode.SymbolType.Exclusive);
                                                break;
                                            case ActivityDefinition.TYPE_START:
                                                node = new SymbolNode(diagram, SymbolNode.SymbolType.Event);
                                                break;
                                            case ActivityDefinition.TYPE_END:
                                                node = new SymbolNode(diagram, SymbolNode.SymbolType.Event_End);
                                                break;
                                            default:
                                                node = new TaskNode(diagram);
                                                break;
                                        }
                                        LinkedHashMap<String, String> mapStyle = new LinkedHashMap();
                                        mapStyle.put(MxConstants.STYLE_ROUNDED, "1");
                                        mapStyle.put(MxConstants.STYLE_STROKEWIDTH, "1");
                                        mapStyle.put(MxConstants.STYLE_WHITE_SPACE, "wrap");
                                        mapStyle.put("html", "1");
                                        mapStyle.put(MxConstants.STYLE_FILLCOLOR, "#FFFFFF");
                                        
                                        if (pathMap.containsKey(id)) {
                                            if (colors.containsKey(pathMap.get(id).getActor())) {
                                                
                                                if (!id.equals(path.get(path.size() - 1).getId()))
                                                    mapStyle.put(MxConstants.STYLE_FILLCOLOR, colors.get(pathMap.get(id).getActor()));
                                                else {
                                                    mapStyle.put(MxConstants.STYLE_STROKEWIDTH, "3");
                                                    mapStyle.put(MxConstants.STYLE_STROKECOLOR, "#00FF00");
                                                }
                                            }
                                            if (processDefinition.getActivityDefinitions().containsKey(id)) {
                                                ActivityDefinition activityDefinition = processDefinition.getActivityDefinitions().get(id);
                                                boolean isActivityRunningInParallel = DiagramUtil.isActivityRunningInParallel(path, activityDefinition);
                                                if (activityDefinition.isIdling() || isActivityRunningInParallel) {
                                                    boolean idling = true;
                                                    try {
                                                        Artifact artifact = aem.getArtifactForActivity(processInstance.getId(), activityDefinition.getId());

                                                        for (StringPair shared : artifact.getSharedInformation()) {
                                                            if (Artifact.SHARED_KEY_IDLE.equals(shared.getKey())) {
                                                                if (Boolean.valueOf(shared.getValue())) {
                                                                    mapStyle.put(MxConstants.STYLE_STROKEWIDTH, "3");
                                                                    mapStyle.put(MxConstants.STYLE_STROKECOLOR, "#FFD700");
                                                                }
                                                                idling = false;
                                                                break;
                                                            }
                                                        }

                                                    } catch (InventoryException ex) {
                                                    }
                                                    if (idling) {
                                                        mapStyle.put(MxConstants.STYLE_STROKEWIDTH, "3");
                                                        mapStyle.put(MxConstants.STYLE_STROKECOLOR, "#FFD700");
                                                    }
                                                }
                                            }
                                        }
                                        node.setRawStyle(mapStyle);
                                    }
                                    else
                                        node = new LabelNode(diagram);
                                    
                                    node.setUsePortToConnect(false);
                                    node.setAddsOverlayButtons(false);
                                    node.setLabel(reader.getElementText());
                                    node.setUuid(id);
                                    node.setCellParent(actorId);
                                    node.setGeometry(x, y, width, height);
                                    node.setIsEditable(true);
                                    diagram.addNode(node);
                                    if (processDefinition.getActivityDefinitions().containsKey(id)) {
                                        ActivityDefinition activityDefinition = processDefinition.getActivityDefinitions().get(id);
                                        nodes.put(activityDefinition, node);
                                        
                                        node.addClickCellListener(clickEvent -> {
                                            if (activityIsEnabled(activityDefinition)) {
                                                try {
                                                    new ArtifactWindow(processEnginePath, processDefinition, 
                                                        processInstance, activityDefinition, actorAuthorizationManager, 
                                                        aem, bem, mem, ws, ts, cmdUpdateDiagram
                                                    ).open();
                                                } catch (InventoryException ex) {
                                                    new SimpleNotification(
                                                        ts.getTranslatedString("module.general.messages.error"), 
                                                        ex.getLocalizedMessage(), 
                                                        AbstractNotification.NotificationType.ERROR, 
                                                        ts
                                                    ).open();
                                                }
                                            } else {
                                                new SimpleNotification(
                                                    ts.getTranslatedString("module.general.messages.information"), 
                                                    ts.getTranslatedString("module.processman.path-does-not-contain-selected-activity"), 
                                                    AbstractNotification.NotificationType.INFO, ts
                                                ).open();
                                            }
                                        });
                                    }
                                }
                            } else if (reader.getName().equals(tagBpmnEdge)) {
                                String source = reader.getAttributeValue(null, Attribute.SOURCE);
                                String target = reader.getAttributeValue(null, Attribute.TARGET);
                                String name = reader.getAttributeValue(null, Attribute.NAME);

                                List<Point> controlPoints = new ArrayList();
                                while (true) {
                                    reader.nextTag();
                                    if (tagControlPoint.equals(reader.getName())) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                            controlPoints.add(new Point(
                                                Double.valueOf(reader.getAttributeValue(null, Attribute.X)),
                                                Double.valueOf(reader.getAttributeValue(null, Attribute.Y))
                                            ));
                                        }
                                    }
                                    else
                                        break;
                                }
                                BPMNConnection connection = new BPMNConnection(diagram);
                                connection.setLabel(name);
                                connection.setAddsOverlayButtons(false);
                                connection.setSource(source);
                                connection.setTarget(target);
                                connection.setPoints(controlPoints);
                                connection.addCellAddedListener(e -> {
                                    e.unregisterListener();
                                    if (edges.get(edges.size() - 1).equals(connection)) {
                                        diagram.endUpdate();
                                        diagram.setCellsResizable(false);
                                        diagram.setCellsMovable(false);
                                        diagram.setConnectable(false);
                                        diagram.setCellsEditable(false);
                                    }
                                });
                                connection.setIsEditable(false);
                                if (pathMap.containsKey(source) && pathMap.containsKey(target)) {
                                    LinkedHashMap<String, String> mapStyle = new LinkedHashMap();
                                    mapStyle.put("html", "1");
                                    mapStyle.put(MxConstants.STYLE_ENDARROW, MxConstants.ARROW_CLASSIC_THIN);
                                    mapStyle.put(MxConstants.STYLE_STROKECOLOR, "#32CD32");
                                    connection.setRawStyle(mapStyle);
                                }
                                edges.add(connection);
                            }
                        }
                    }
                    reader.close();
                    edges.forEach(edge -> diagram.addEdge(edge));
                } catch (XMLStreamException ex) {
                    Logger.getLogger(ProcessInstanceDiagram.class.getName()).log(Level.SEVERE, null, ex);
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ts.getTranslatedString("module.general.messages.unexpected-error"), 
                        AbstractNotification.NotificationType.ERROR, ts 
                    ).open();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ts.getTranslatedString("module.general.messages.unexpected-error"), 
                        AbstractNotification.NotificationType.ERROR, ts 
                    ).open();
                }
            });
            add(diagram);
        }
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        final String VERSION_1 = "1.";
        final String VERSION_2 = "2.";
        if (processDefinition.getVersion() != null) {
            if (processDefinition.getVersion().startsWith(VERSION_1))
                buildDiagramVersion1();
            else if (processDefinition.getVersion().startsWith(VERSION_2))
                buildDiagramVersion2();
        }
        else
            buildDiagramVersion1();
    }
    
    private void addActivity(ActivityDefinition nextActivity, ActivityDefinition previousActivity, String pathName) {
        if (nextActivity != null) {
            boolean isNewNode = !activities.containsKey(nextActivity);
            ActivityNode activityNode = diagramProvider.addActivity(nextActivity, previousActivity, pathName);
            activities.put(nextActivity, activityNode);
            lastActivity = nextActivity;
            if (!isNewNode)
                return;
            
            activityNode.addClickListener(clickEvent -> {
                activityNode.setEnabled(activityIsEnabled(nextActivity));
                
                if (activityNode.isEnabled()) {
                    try {
                        new ArtifactWindow(processEnginePath, processDefinition, 
                            processInstance, nextActivity, actorAuthorizationManager, 
                            aem, bem, mem, ws, ts, cmdUpdateDiagram
                        ).open();
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, 
                            ts
                        ).open();
                    }
                }
            });
            if (nextActivity instanceof ConditionalActivityDefinition) {
                addActivity(((ConditionalActivityDefinition) 
                    nextActivity).getNextActivityIfTrue(), 
                    nextActivity,
                    ts.getTranslatedString("module.general.messages.yes")
                );
                addActivity(((ConditionalActivityDefinition) 
                    nextActivity).getNextActivityIfFalse(), 
                    nextActivity, 
                    ts.getTranslatedString("module.general.messages.no")
                );
            } else if (nextActivity instanceof ParallelActivityDefinition) {
                ParallelActivityDefinition activity = (ParallelActivityDefinition) nextActivity;
                if (activity.getPaths() != null) {
                    activity.getPaths().forEach(activityDef -> 
                        addActivity(activityDef, nextActivity, null)
                    );
                }
            } else {
                addActivity(nextActivity.getNextActivity(), nextActivity, null);
            }
        }
    }
    
    private boolean activityIsEnabled(ActivityDefinition activityDefinition) {
        try {
            String activityId = activityDefinition.getId();
            List<ActivityDefinition> lstActivities = aem.getProcessInstanceActivitiesPath(processInstance.getId());
            
            if (lstActivities != null && !lstActivities.isEmpty()) {
                
                int activityIndex = -1;
                
                for (int i = 0; i < lstActivities.size(); i += 1) {
                    
                    if (lstActivities.get(i).getId().equals(activityId))
                        activityIndex = i;
                }
                if (activityIndex - 1 >= 0) {
                    //TODO: The parallel activity JOIN_FORK is not supported yet
                    for (ActivityDefinition activityDef : lstActivities) {
                        
                        if (activityDef instanceof ParallelActivityDefinition) {
                                                        
                            ParallelActivityDefinition parallelAcvitityDef = (ParallelActivityDefinition) activityDef;
                                                        
                            if (parallelAcvitityDef.getSequenceFlow() == ParallelActivityDefinition.FORK && 
                                parallelAcvitityDef.getPaths() != null) {
                                
                                for (ActivityDefinition anActivityDef : parallelAcvitityDef.getPaths()) {
                                    if (anActivityDef.getId().equals(activityId))
                                        return true;
                                }
                            }
                        }
                    }
                    if (lstActivities.get(activityIndex) instanceof ParallelActivityDefinition) {
                        ParallelActivityDefinition parallelActivityDefinition = (ParallelActivityDefinition) lstActivities.get(activityIndex);
                        if (parallelActivityDefinition.getSequenceFlow() == ParallelActivityDefinition.JOIN) {
                            ParallelActivityDefinition join = parallelActivityDefinition;
                            try {                    
                                List<ActivityDefinition> path = aem.getProcessInstanceActivitiesPath(processInstance.getId());
                                List<ActivityDefinition> incomingActivityDefs = new ArrayList();
                                for (ActivityDefinition item : path) {
                                    /*
                                    Get incoming RemoteConditionalActivityDefinition &  
                                    RemoteParallelActivityDefinition activities to join
                                    parallel flow are not supported yet, because
                                    in the current process definitions no are
                                    presented cases that use it, in the case of be
                                    needed this method must be recursive.
                                    */
                                    if (item.getNextActivity() != null && 
                                        item.getNextActivity().getId().equals(join.getId())){
                                        incomingActivityDefs.add(item);
                                    }
                                }
                                for (ActivityDefinition incomingActivityDef : incomingActivityDefs) {
                                    try {
                                        aem.getArtifactForActivity(
                                            processInstance.getId(),
                                            incomingActivityDef.getId());  
                                    } catch (InventoryException ex) {
                                        //Expected exception when the artifact is not found
                                        String text = String.format(
                                            ts.getTranslatedString("module.processman.process-instance.diagram.activity-disable"), 
                                            join.getName() != null ? join.getName() : "Activity" //NOI18N
                                        );
                                        new SimpleNotification(
                                            ts.getTranslatedString("module.general.messages.warning"),
                                            text,
                                            AbstractNotification.NotificationType.WARNING,
                                            ts
                                        ).open();
                                        return false;
                                    }
                                }
                                return true;
                            } catch(InventoryException ex) {
                                
                            }
                        }
                    }
                    ActivityDefinition activityDef = lstActivities.get(activityIndex - 1);
                                        
                    if (!(activityDef instanceof ParallelActivityDefinition)) {
                        
                        aem.getArtifactForActivity(
                            processInstance.getId(), 
                            activityDef.getId());
                    }
                }
                return lstActivities.contains(activityDefinition);
            }
            
        } catch (InventoryException ex) {
        }
        return false;
    }
}
