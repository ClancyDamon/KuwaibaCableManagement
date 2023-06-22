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

package org.neotropic.kuwaiba.modules.optional.physcon.views;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.neotropic.flow.component.mxgraph.Point;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractImageExporter;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractView;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.mxgraph.MxGraphCanvas;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 * View for object view representation
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class ObjectView extends AbstractView<VerticalLayout> {
    /**
     * Reference to the main canvas of the view
     */
    private MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> mxGraphCanvas;
    /**
     * Reference to the translation service.
     */
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    private MetadataEntityManager mem;
    /**
     * Utility class that help to load resources like icons and images
     */
    private ResourceFactory resourceFactory;
    /**
     * The object used to build the view.
     */
    private BusinessObjectLight businessObject;
    
    public ObjectView(BusinessObjectLight businessObject, MetadataEntityManager mem, ApplicationEntityManager aem, 
            BusinessEntityManager bem, TranslationService ts, ResourceFactory resourceFactory) {
        this.businessObject = businessObject;
        this.bem = bem;  
        this.aem = aem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        mxGraphCanvas = new MxGraphCanvas("90%", "80%");
        mxGraphCanvas.getMxGraph().setIsCellEditable(false);
        mxGraphCanvas.getMxGraph().setIsCellResizable(false);
    }
    
    /**
     * Builds the view from scratch and tries to recover the last saved state, if it exists.
     * @param businessObject The inventory object used to build the view. 
     */
    public void initialize(BusinessObjectLight businessObject) {
         try {
            this.viewMap = new ViewMap();            
            //First we build the default view
            
            BusinessObjectLight asLocalBusinessObject = (BusinessObjectLight) businessObject;
            
            //First the direct children that not connections
            List<BusinessObjectLight> nodeChildren = bem.getObjectChildren(asLocalBusinessObject.getClassName(), asLocalBusinessObject.getId(), -1);
            
            if (!nodeChildren.isEmpty()) { 
                int lastX = 0;

                for (BusinessObjectLight child : nodeChildren) { // Add the nodes
                    BusinessObjectViewNode childNode = new BusinessObjectViewNode(child);
                    childNode.getProperties().put("x", lastX);
                    childNode.getProperties().put("y", 0);
                    viewMap.addNode(childNode);
                    lastX += 200;
                }
                
                List<BusinessObjectLight> connectionChildren = bem.getSpecialChildrenOfClassLight(asLocalBusinessObject.getId(), 
                        asLocalBusinessObject.getClassName(), Constants.CLASS_GENERICPHYSICALCONNECTION, -1);

                for (BusinessObjectLight child : connectionChildren) {
                    BusinessObjectViewEdge connection = new BusinessObjectViewEdge(child);
                    viewMap.addEdge(connection);
                    
                    List<BusinessObjectLight> aSide = bem.getSpecialAttribute(child.getClassName(), child.getId(), "endpointA"); 
                    if (aSide.isEmpty()) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.information"), String.format(ts.getTranslatedString("module.visualization.object-view-connection-loose-endpoint"), child), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                        continue;
                    }

                    List<BusinessObjectLight> bSide = bem.getSpecialAttribute(child.getClassName(), child.getId(), "endpointB"); //NOI18N
                    if (bSide.isEmpty()) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.information"), String.format(ts.getTranslatedString("module.visualization.object-connection-with-loose-endpoint"), child), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                        continue;
                    }
                    //The endpoints of the connections are ports, but the direct children of the selected object are (most likely) communication devices,
                    //so we need to find the equipment these ports belong to and try to find them among the nodes that were just added above. 
                    List<BusinessObjectLight> parentsASide = bem.getParents(aSide.get(0).getClassName(), aSide.get(0).getId());
                    int currentObjectIndexASide = parentsASide.indexOf(asLocalBusinessObject);
                    if (currentObjectIndexASide == -1) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.information"), String.format(ts.getTranslatedString("module.visualization.object-endpoint-a-not-located-in-object"), child), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                        continue;
                    }
                    AbstractViewNode sourceNode = currentObjectIndexASide == 0 ? viewMap.getNode(aSide.get(0)) : viewMap.getNode(parentsASide.get(currentObjectIndexASide - 1));

                    List<BusinessObjectLight> parentsBSide = bem.getParents(bSide.get(0).getClassName(), bSide.get(0).getId());
                    int currentObjectIndexBSide = parentsBSide.indexOf(asLocalBusinessObject);
                    if (currentObjectIndexBSide == -1) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.information"), String.format(ts.getTranslatedString("module.visualization.object-endpoint-b-not-located-in-object"), child), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                        continue;
                    }
                    AbstractViewNode targetNode = currentObjectIndexBSide == 0 ? viewMap.getNode(bSide.get(0)) : viewMap.getNode(parentsBSide.get(currentObjectIndexBSide - 1));

                    viewMap.attachSourceNode(connection, sourceNode);
                    viewMap.attachTargetNode(connection, targetNode);
                }
            }
              
            //Now, we check if there's a view saved previously..If so, the default location of the nodes will be updated accordingly
            List<ViewObjectLight> objectViews = aem.getObjectRelatedViews(asLocalBusinessObject.getId(), asLocalBusinessObject.getClassName(), -1);

            if (!objectViews.isEmpty()) {
                ViewObject currentView = aem.getObjectRelatedView(asLocalBusinessObject.getId(), 
                        asLocalBusinessObject.getClassName(), objectViews.get(0).getId());
                if (currentView.getBackground() != null && currentView.getBackground().length > 0) {
                    StreamResource resource = new StreamResource("bgimage.jpg", () -> new ByteArrayInputStream(currentView.getBackground()));
                    VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
                    mxGraphCanvas.getMxGraph().setBackgroundImage(StreamResourceRegistry.getURI(resource).toString());
                } else 
                    mxGraphCanvas.getMxGraph().setBackgroundImage("");
                this.buildFromSavedView(currentView.getStructure()); 
            }
            
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    public MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> getMxGraphCanvas() {
        return mxGraphCanvas;
    }

    public void setMxGraphCanvas(MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> mxGraph) {
        this.mxGraphCanvas = mxGraph;
    }
    
    public void setHeight(Integer height) { 
        this.mxGraphCanvas.getMxGraph().setHeight(height.toString());
    }
    
    public void setWidth(Integer width) {
        this.mxGraphCanvas.getMxGraph().setWidth(width.toString());
    }
    
    public void resetView() {
        this.mxGraphCanvas.removeAllCells();
        this.viewMap = new ViewMap();
    }

    @Override
    public byte[] getAsXml() {
        try {
            syncViewMap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameView = new QName("view"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), Constants.VIEW_FORMAT_VERSION)); //NOI18N
            
            QName qnameClass = new QName("class"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters("ObjectView")); //NOI18N
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            
            QName qnameViewPosition = new QName("viewPosition"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameViewPosition, null, null));
            xmlew.add(xmlef.createAttribute(new QName("zoom"), mxGraphCanvas.getMxGraph().getScale() + "")); //NOI18N
            xmlew.add(xmlef.createEndElement(qnameViewPosition, null));
            
            QName qnameNodes = new QName("nodes"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            
            for (AbstractViewNode nodeEntry : viewMap.getNodes()) {
                QName qnameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x"), ((Double) nodeEntry.getProperties().get("x")).toString()));
                xmlew.add(xmlef.createAttribute(new QName("y"), ((Double) nodeEntry.getProperties().get("y")).toString()));
                xmlew.add(xmlef.createAttribute(new QName("class"),  ((BusinessObjectLight)nodeEntry.getIdentifier()).getClassName())); //NOI18N
                xmlew.add(xmlef.createCharacters( ((BusinessObjectLight) nodeEntry.getIdentifier()).getId())); //NOI18N
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
              for (AbstractViewEdge edgeEntry : this.viewMap.getEdges()) {
                
                BusinessObjectLight edgeObject = (BusinessObjectLight)edgeEntry.getIdentifier();         
                BusinessObjectLight sourceObject = (BusinessObjectLight)this.viewMap.getEdgeSource(edgeEntry).getIdentifier();
                BusinessObjectLight targetObject = (BusinessObjectLight)this.viewMap.getEdgeTarget(edgeEntry).getIdentifier();
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                xmlew.add(xmlef.createAttribute(new QName("id"), edgeObject.getId()));
                xmlew.add(xmlef.createAttribute(new QName("class"), edgeObject.getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("asideid"), sourceObject.getId().contains("-*") ? 
                        "-1" : sourceObject.getId()));
                xmlew.add(xmlef.createAttribute(new QName("asideclass"), sourceObject.getId().contains("-*") ? 
                        "" : sourceObject.getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("bsideid"), targetObject.getId().contains("-*") ? 
                        "-1" : targetObject.getId()));
                xmlew.add(xmlef.createAttribute(new QName("bsideclass"), targetObject.getId().contains("-*") ? 
                        "" : targetObject.getClassName()));
                
                List<Point> cPoints = (List<Point>) edgeEntry.getProperties().get("controlPoints");
                if (cPoints != null && cPoints.size() > 0) {
                    // add start control point to desktop client compatibility
                    QName qnameControlpoint = new QName("controlpoint");
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), ((Double) viewMap.getNode(sourceObject).getProperties().get("x")).intValue() + ""));
                    xmlew.add(xmlef.createAttribute(new QName("y"), ((Double) viewMap.getNode(sourceObject).getProperties().get("y")).intValue() + ""));
                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                    for (Point point : cPoints) {                     
                        qnameControlpoint = new QName("controlpoint");
                        xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                        xmlew.add(xmlef.createAttribute(new QName("x"), ((int) point.getX()) + ""));
                        xmlew.add(xmlef.createAttribute(new QName("y"), ((int) point.getY()) + ""));
                        xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                    } 
                    // add end control point to desktop client compatibility
                    qnameControlpoint = new QName("controlpoint");
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), ((Double) viewMap.getNode(targetObject).getProperties().get("x")).intValue() + ""));
                    xmlew.add(xmlef.createAttribute(new QName("y"), ((Double) viewMap.getNode(targetObject).getProperties().get("y")).intValue() + ""));
                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                }
                
                xmlew.add(xmlef.createEndElement(qnameEdge, null));
            }
            xmlew.add(xmlef.createEndElement(qnameEdges, null));
            
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
         //   <editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">           
//            try (FileOutputStream fos = new FileOutputStream("/home/opaz/ObjectViewGetAs.xml")) {
//                fos.write(baos.toByteArray());
//                 fos.close();
//            } catch(Exception e) { }
            //                     </editor-fold>
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return null;
    }

    @Override
    public byte[] getAsImage(AbstractImageExporter exporter) {
        return exporter.export(this.getMxGraphCanvas().getMxGraph());
    }

    @Override
    public VerticalLayout getAsUiElement() throws InvalidArgumentException {
        if (this.viewMap == null || this.viewMap.getNodes().isEmpty())
            initialize(businessObject);
     
        VerticalLayout lytObjectView = new VerticalLayout(mxGraphCanvas.getMxGraph());
                   
        MxGraphNode node;
        MxGraphEdge edge;
        for (AbstractViewNode bObject : viewMap.getNodes()) {
            node = new MxGraphNode();
            BusinessObjectLight theObject = (BusinessObjectLight)bObject.getIdentifier();
            node.setUuid(theObject.getId());
            node.setLabel(FormattedObjectDisplayNameSpan.getFormattedDisplayName(theObject, true));
            node.setGeometry(((int)(bObject.getProperties().get("x"))), (int)(bObject.getProperties().get("y")), 32, 32);
            
            String uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(((BusinessObjectLight)bObject.getIdentifier()).getClassName())).toString();                                
            node.setImage(uri);
            node.setShape(MxConstants.SHAPE_IMAGE);
            node.setUsePortToConnect(true);
            mxGraphCanvas.addNode(theObject, node);
        } //        

        for (AbstractViewEdge anEdge : viewMap.getEdges()) {
                edge = new MxGraphEdge();
                edge.setLabel(((BusinessObjectLight) anEdge.getIdentifier()).getName());
                 AbstractViewNode sourceNode = viewMap.getEdgeSource(anEdge);
                 if (sourceNode != null) {
                    edge.setSource(((BusinessObjectLight)sourceNode.getIdentifier()).getId());
                    AbstractViewNode targetNode = viewMap.getEdgeTarget(anEdge);
                    if (targetNode != null) { //The edge is only added if both sides are found
                        edge.setTarget(((BusinessObjectLight)targetNode.getIdentifier()).getId());
                        List<Point> cPoints = (List<Point>) anEdge.getProperties().get("controlPoints");
                        if (cPoints != null && cPoints.size() > 2)  // ignore default control points of desktop client
                            edge.setPoints(cPoints.subList(1, cPoints.size()-1)); 
                        
                        try {
                            ClassMetadata theClass = mem.getClass(((BusinessObjectLight)anEdge.getIdentifier()).getClassName());
                            edge.setStrokeColor(UtilHtml.toHexString(new Color(theClass.getColor())));
                        } catch (MetadataObjectNotFoundException ex) {
                            //In case of error, use a default black line
                        }
                        mxGraphCanvas.addEdge((BusinessObjectLight) anEdge.getIdentifier(), 
                                (BusinessObjectLight)sourceNode.getIdentifier(), 
                                (BusinessObjectLight)targetNode.getIdentifier(), edge);
                    }
                }
        }
        MxGraphNode dummyNode = new MxGraphNode();
        dummyNode.setGeometry(0, 0, 0, 0);
        dummyNode.setMovable(false);
        mxGraphCanvas.getMxGraph().addNode(dummyNode);
        //execute the layout and disable moving when the last cell is added
        dummyNode.addCellAddedListener(eventListener -> mxGraphCanvas.getMxGraph().refreshGraph());
        lytObjectView.setSizeFull();
        lytObjectView.setPadding(false);
        lytObjectView.setId("lyt-object-view");
        return lytObjectView;
    }

    @Override
    public void buildFromSavedView(byte[] theSavedView) {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qViewPosition = new QName("viewPosition"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(theSavedView);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
   //          <editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
//            try (FileOutputStream fos = new FileOutputStream("/home/opaz/ObjectView.xml")) {
//                fos.write(theSavedView);
//                 fos.close();
//            } catch(Exception e) { }
            //                     </editor-fold>
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qViewPosition)) {
                        try {
                            if (reader.getAttributeValue(null, "zoom") != null) {
                                double zoom = new Double(reader.getAttributeValue(null, "zoom")); //NOI18N
                                mxGraphCanvas.getMxGraph().setScale(zoom);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println(e.getMessage());
                        }             
                    } else if (reader.getName().equals(qNode)) {
                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue(); //NOI18N
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue(); //NOI18N
                        String className = reader.getAttributeValue(null, "class"); //NOI18N
                        String objectId = reader.getElementText();
                        
                        AbstractViewNode node = viewMap.getNode(new BusinessObjectLight(className, objectId, "")); //NOI18N
                        
                        if (node != null) {
                            node.getProperties().put("x", xCoordinate);
                            node.getProperties().put("y", yCoordinate);
                        }
                    } else {
                        if (reader.getName().equals(qEdge)) {
                            String objectId = reader.getAttributeValue(null, "id"); //NOI18N
                            String className = reader.getAttributeValue(null, "class"); //NOI18N
                            AbstractViewEdge edge = viewMap.getEdge(new BusinessObjectLight(className, objectId, "")); //NOI18N
                            
                            if (edge != null) {
                                List<Point> controlPoints = new ArrayList<>();
                                while(true) {
                                    reader.nextTag();
                                    if (reader.getName().equals(qControlPoint)) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                            controlPoints.add(new Point(Double.valueOf(reader.getAttributeValue(null,"x")), Double.valueOf(reader.getAttributeValue(null,"y"))));
                                    }  else {
                                            edge.getProperties().put("controlPoints",controlPoints);
                                            break;
                                        }
                                }
                            }
                        } 
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"),
                    AbstractNotification.NotificationType.INFO, ts).open();
        }
    }
    
    @Override
    public void clean() { 
        this.viewMap.clear();
        mxGraphCanvas.removeAllCells();
    }
   
    @Override
    public AbstractViewNode addNode(BusinessObjectLight businessObject, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractViewEdge addEdge(BusinessObjectLight businessObject, BusinessObjectLight sourceBusinessObject, BusinessObjectLight targetBusinessObject, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeNode(BusinessObjectLight businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeEdge(BusinessObjectLight businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void nodeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void edgeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /**
     * Syncs the view model (the view map) with the elements in the canvas.
     */
    private void syncViewMap() {
        this.viewMap.clear();
        if (mxGraphCanvas == null)
            return;
        
        for (Map.Entry<BusinessObjectLight, MxGraphNode> entry : mxGraphCanvas.getNodes().entrySet()) {
            BusinessObjectViewNode aNode = new BusinessObjectViewNode(entry.getKey());
            aNode.getProperties().put("x", entry.getValue().getX());
            aNode.getProperties().put("y", entry.getValue().getY());
            this.viewMap.getNodes().add(aNode);
        }
        
        for (Map.Entry<BusinessObjectLight, MxGraphEdge> entry : mxGraphCanvas.getEdges().entrySet()) {
            BusinessObjectViewEdge anEdge = new BusinessObjectViewEdge(entry.getKey());
            anEdge.getProperties().put("controlPoints", entry.getValue().getPointList());
            anEdge.getProperties().put("sourceLabel", entry.getValue().getSourceLabel() == null ? "" : entry.getValue().getSourceLabel());
            anEdge.getProperties().put("targetLabel", entry.getValue().getTargetLabel() == null ? "" : entry.getValue().getTargetLabel());
            
            this.viewMap.getEdges().add(anEdge);
            this.viewMap.attachSourceNode(anEdge, new BusinessObjectViewNode(mxGraphCanvas.findSourceEdgeObject(entry.getKey())));
            this.viewMap.attachTargetNode(anEdge, new BusinessObjectViewNode(mxGraphCanvas.findTargetEdgeObject(entry.getKey())));
        }
    }
}