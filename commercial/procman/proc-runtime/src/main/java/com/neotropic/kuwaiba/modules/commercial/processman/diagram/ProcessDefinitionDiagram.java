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
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Actor;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.ProcessDefinitionLoader.Attribute;
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.ProcessDefinitionLoader.Tag;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * UI diagram to process diagram.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ProcessDefinitionDiagram extends Div {
    private final TranslationService ts;
    private final ProcessDefinition processDefinition;
    
    public ProcessDefinitionDiagram(ProcessDefinition processDefinition, TranslationService ts) {
        this.processDefinition = processDefinition;
        this.ts = ts;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        setSizeFull();
        if (processDefinition.getDefinition() != null) {
            HashMap<Actor, String> colors = new HashMap();
            List<BPMNConnection> edges = new ArrayList();
            
            BPMNDiagram diagram = new BPMNDiagram();
            // To display resolution greater than or equal to 1280x720
            diagram.setWidth("100vw");
            diagram.setHeight("59vh");
            diagram.setOverflow("scroll");
            diagram.setHasOutline(true);
            diagram.setOutlineWidth("20vw");
            diagram.setOutlineHeight("10vh");
            diagram.setCustomOutlinePosition("margin-top: 5px; margin-left: 40vw; margin-right: 40vw;");
            diagram.setBeginUpdateOnInit(true);
            
            diagram.addGraphLoadedListener(event -> {
                event.unregisterListener();
                diagram.enablePanning(true);
                try {
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
                                    actorNode.setRawStyle(mapStyle);
                                    
                                    diagram.addNode(actorNode);
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
                }
            });
            add(diagram);
        }
    }
    
}
