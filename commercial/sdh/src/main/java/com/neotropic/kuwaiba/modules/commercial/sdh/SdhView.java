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

package com.neotropic.kuwaiba.modules.commercial.sdh;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.kuwaiba.modules.commercial.sdh.widgets.SdhDashboard;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.neotropic.flow.component.mxgraph.Point;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractImageExporter;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.util.visual.mxgraph.MxGraphCanvas;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractView;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewMap;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.notifications.AbstractNotification;

/**
 * Custom view implementation for sdh view module with a mxgraph component as canvas.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class SdhView extends AbstractView<Component> {
    /**
     * The version of the format of the XML document used to store the view in the database.
     */
    public static final String SAVED_VIEW_VERSION = "1.1";
    /**
     * Reference to the main canvas of the view
     */
    private MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> mxgraphCanvas;
    /**
     * Reference to the translation service.
     */
    private final TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    private final ApplicationEntityManager aem;
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

    public MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> getMxgraphCanvas() {
        return mxgraphCanvas;
    }

    public void setMxgraphCanvas(MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> mxgraphCanvas) {
        this.mxgraphCanvas = mxgraphCanvas;
    }

    public SdhView(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem, TranslationService ts, ResourceFactory
             resourceFactory) {
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        mxgraphCanvas = new MxGraphCanvas("100%", "70vh");
        mxgraphCanvas.getMxGraph().setIsCellEditable(false);
        mxgraphCanvas.getMxGraph().setIsCellResizable(false);
        this.viewMap = new ViewMap();
    }

    @Override
    public byte[] getAsXml() {
        try {
        //First we make sure that the view map reflects the graph in the graphic component. 
        //If syncViewMap is not called, the XML document generated by this method will 
        //correspond to the latest loaded version of the map.
            this.syncViewMap();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameView = new QName("view");
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), SAVED_VIEW_VERSION)); // NOI18N
            
            QName qnameClass = new QName("class");
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters("SDHModuleView"));
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            
            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            for ( AbstractViewNode nodeEntry : viewMap.getNodes()) {
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
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            Logger.getLogger(SdhDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();           
        }
        return null;
    }

    @Override
    public byte[] getAsImage(AbstractImageExporter exporter) {
         return exporter.export(this.getMxgraphCanvas().getMxGraph());
    }

    @Override
    public Component getAsUiElement() {
        try {
            if (this.mxgraphCanvas == null) 
                mxgraphCanvas = new MxGraphCanvas<>();

            return this.mxgraphCanvas.getMxGraph();
        } catch (Exception ex) {
            return new Label(String.format(ts.getTranslatedString("module.sdh.unexpected-error-loading-view"), ex.getLocalizedMessage()));
        }
    }

    @Override
    public void buildFromSavedView(byte[] structure) {
        if (structure == null || structure.length == 0)
            return;
        
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        List<BusinessObjectLight> emptySides = new ArrayList<>();
//      <editor-fold defaultstate="collapsed" desc="Uncomment this for debugging purposes. This outputs the XML view as a file">
//        try (FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/oview_SDH_VIEW .xml")) {
//            fos.write(structure);
//        } catch(Exception e) { }
//      </editor-fold>
        QName qNode = new QName("node"); //NOI18N
        QName qEdge = new QName("edge"); //NOI18N
        QName qControlPoint = new QName("controlpoint"); //NOI18N
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT){
                    if (reader.getName().equals(qNode)){
                        String objectClass = reader.getAttributeValue(null, "class");

                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        String objectId = reader.getElementText();
                        //this side is connected
                        BusinessObjectLight lol = bem.getObjectLight(objectClass, objectId);
                        if (lol != null) {
                           String uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(lol.getClassName())).toString();       
                           
                           Properties props = new Properties();
                           props.put("imageUrl", uri);
                           props.put("x", xCoordinate );
                           props.put("y", yCoordinate );
                           addNode(lol, props);
                        }
                        else if(objectId.equals("-1")) { // we create an empty side
                            emptySides.add(new BusinessObjectLight("", UUID.randomUUID().toString() + "-" + (objectId), ""));
                        }
                    }else {
                        if (reader.getName().equals(qEdge)){
                            String linkId = reader.getAttributeValue(null, "id");
                            
                            String aSideId = reader.getAttributeValue(null, "asideid");
                            String aSideClass = reader.getAttributeValue(null, "asideclass");
                            String bSideId = reader.getAttributeValue(null, "bsideid");
                            String bSideClass = reader.getAttributeValue(null, "bsideclass");
                            
                            String className = reader.getAttributeValue(null, "class");
                            BusinessObject theLink = bem.getObject(className, linkId);
                            BusinessObjectLight endPointA = null;
                            BusinessObjectLight endPointB = null;
                            if (theLink != null) {
                                BusinessObjectLight aSideObject, bSideObject;
                                if(!aSideId.equals("-1"))
                                    aSideObject = new BusinessObjectLight(aSideClass, aSideId, null);
                                else{
                                    aSideObject = emptySides.remove(0);
                                    endPointA = aSideObject;
                                }
                                if(!bSideId.equals("-1"))
                                    bSideObject = new BusinessObjectLight(bSideClass, bSideId, null);
                                else {
                                    bSideObject = emptySides.remove(0);
                                    endPointA = bSideObject;
                                }            
                                HashMap<String, List<BusinessObjectLight>> specialAttributes = bem.getSpecialAttributes(theLink.getClassName(), linkId);
                                for (Map.Entry<String, List<BusinessObjectLight>> entry : specialAttributes.entrySet()) {
                                    if(entry.getKey().equals("mplsEndpointA")){
                                        endPointA = entry.getValue().get(0);
                                        BusinessObjectLight parentA = bem.getFirstParentOfClass(endPointA.getClassName(), endPointA.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                                        if(parentA != null && !parentA.getId().equals(aSideId)){
                                            aSideObject = parentA;
                                        }
                                    }
                                    if(entry.getKey().equals("mplsEndpointB")){
                                        endPointB = entry.getValue().get(0);
                                        BusinessObjectLight parentB = bem.getFirstParentOfClass(endPointB.getClassName(), endPointB.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                                        if(parentB != null && !parentB.getId().equals(bSideId)) {                                        
                                            bSideObject = parentB;
                                        }
                                    }
                                }
                                                             
                                List<Point> controlPoints = new ArrayList<>();
                                while(true){
                                    reader.nextTag();

                                    if (reader.getName().equals(qControlPoint)) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                            controlPoints.add(new Point(Double.valueOf(reader.getAttributeValue(null,"x")), Double.valueOf(reader.getAttributeValue(null,"y"))));
                                    } else {
                                        break;
                                    }
                                }
                                Properties props = new Properties();
                                if (controlPoints.size() >= 2)  // ignore default control points of desktop client
                                   props.put("controlPoints", controlPoints.subList(1, controlPoints.size() -1 )); 
                                else                                         
                                    props.put("controlPoints", controlPoints);
                                props.put("sourceLabel", endPointA == null ? "" : endPointA.getName());
                                props.put("targetLabel", endPointB == null ? "" : endPointB.getName());
                                                               
                                addEdge(theLink, aSideObject, bSideObject, props);
                            }
                        }
                    }
                }
            }
            reader.close();
            MxGraphNode dummyNode = new MxGraphNode();
            dummyNode.setGeometry(0, 0, 0, 0);
            dummyNode.setMovable(false);
            mxgraphCanvas.getMxGraph().addNode(dummyNode);
                //  execute the layout and disable moving when the last cell is added
            dummyNode.addCellAddedListener(eventListener -> {
                mxgraphCanvas.getMxGraph().refreshGraph();
            });
        } catch (NumberFormatException | XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.sdh.view-corrupted"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
             Logger.getLogger(SdhDashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(SdhDashboard.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    @Override
    public void clean() {
        this.viewMap.clear();
        mxgraphCanvas.setNodes(new LinkedHashMap<>());
        mxgraphCanvas.setEdges(new HashMap<>());
        mxgraphCanvas.getMxGraph().removeAllCells();
    }

    @Override
    public AbstractViewNode addNode(BusinessObjectLight businessObject, Properties properties) {
        AbstractViewNode aNode = this.viewMap.findNode(businessObject);
        if (aNode == null) {            
            BusinessObjectViewNode newNode = new BusinessObjectViewNode(businessObject);
            newNode.setProperties(properties);           
            this.viewMap.addNode(newNode);
            
            if (this.mxgraphCanvas != null) { //The view could be created without a graphical representation (the canvas). so here we make sure that's not the case
                int x = (int) properties.get("x");
                int y = (int) properties.get("y");
                String urlImage = (String) properties.get("imageUrl");

                MxGraphNode newMxNode = new MxGraphNode();
                if (urlImage != null && !urlImage.isEmpty()) 
                    newMxNode.setImage(urlImage);

                newMxNode.setUuid(businessObject.getId());
                newMxNode.setLabel(FormattedObjectDisplayNameSpan.getFormattedDisplayName(businessObject, true));
                newMxNode.setWidth((int) Constants.DEFAULT_ICON_WIDTH);
                newMxNode.setHeight((int) Constants.DEFAULT_ICON_HEIGHT);
                newMxNode.setX((x)); //The position is scaled
                newMxNode.setY((y));
                newMxNode.setUsePortToConnect(true);
                newMxNode.setShape(MxConstants.SHAPE_IMAGE);
                newMxNode.setUsePortToConnect(true);
                
                mxgraphCanvas.addNode(businessObject, newMxNode);                           
            }        
            return newNode;
        } else
            return aNode;   
    }

    @Override
    public AbstractViewEdge addEdge(BusinessObjectLight businessObject, BusinessObjectLight sourceBusinessObject, BusinessObjectLight targetBusinessObject, Properties properties) {
                      
        AbstractViewEdge anEdge = this.viewMap.findEdge(businessObject);
        if (anEdge == null) {
            BusinessObjectViewEdge newEdge = new BusinessObjectViewEdge(businessObject);
            
            //if any of the end points is missing, the edge is not added
            AbstractViewNode aSourceNode = this.viewMap.findNode(sourceBusinessObject.getId());
            if (aSourceNode == null)
                return null;
            
            AbstractViewNode aTargetNode = this.viewMap.findNode(targetBusinessObject.getId());
            if (aTargetNode == null)
                return null;
            
            newEdge.setProperties(properties);
            this.viewMap.addEdge(newEdge);
            this.viewMap.attachSourceNode(anEdge, aSourceNode);
            this.viewMap.attachTargetNode(anEdge, aTargetNode);
        
            if (this.mxgraphCanvas != null) { //The view could be created without a graphical representation (the map). so here we make sure that's not the case
                List<Point> controlPoints = (List<Point>) properties.get("controlPoints");
                String sourceLabel =  (String) properties.get("sourceLabel");
                String targetLabel =  (String) properties.get("targetLabel");               
                String hexColor = null;
                try {
                    ClassMetadata remoteClass = mem.getClass(businessObject.getClassName());
                    hexColor = String.format("#%06x", (0xFFFFFF & remoteClass.getColor()));                       
                } catch (MetadataObjectNotFoundException ex) {
                    // Use default color
                }
                
                mxgraphCanvas.addEdge(businessObject, businessObject.getId(), sourceBusinessObject, targetBusinessObject, controlPoints, sourceLabel, targetLabel, hexColor);
            }
            return newEdge;
        } else
            return anEdge;
    }
    
    /**
     * The view map is created originally by calling the {@link  #buildWithSavedView(byte[])} 
     * method, but it can change due to user interactions, so it's necessary to update it in order to 
     * export it in other formats. This method wipes the existing view map and builds it again from 
     * whatever it is on the map currently
     */
    public void syncViewMap() {
        this.viewMap.clear();
        if (mxgraphCanvas == null)
            return;
        
        for (Map.Entry<BusinessObjectLight, MxGraphNode> entry : mxgraphCanvas.getNodes().entrySet()) {
            BusinessObjectViewNode aNode = new BusinessObjectViewNode(entry.getKey());
            aNode.getProperties().put("x", entry.getValue().getX());
            aNode.getProperties().put("y", entry.getValue().getY());
            this.viewMap.getNodes().add(aNode);
        }
        
        for (Map.Entry<BusinessObjectLight, MxGraphEdge> entry : mxgraphCanvas.getEdges().entrySet()) {
            BusinessObjectViewEdge anEdge = new BusinessObjectViewEdge(entry.getKey());
            anEdge.getProperties().put("controlPoints", entry.getValue().getPointList());
            anEdge.getProperties().put("sourceLabel", entry.getValue().getSourceLabel() == null ? "" : entry.getValue().getSourceLabel());
            anEdge.getProperties().put("targetLabel", entry.getValue().getTargetLabel() == null ? "" : entry.getValue().getTargetLabel());
            
            this.viewMap.getEdges().add(anEdge);
            this.viewMap.attachSourceNode(anEdge, new BusinessObjectViewNode(mxgraphCanvas.findSourceEdgeObject(entry.getKey())));
            this.viewMap.attachTargetNode(anEdge, new BusinessObjectViewNode(mxgraphCanvas.findTargetEdgeObject(entry.getKey())));
        }
    }

    @Override
    public void removeNode(BusinessObjectLight businessObject) {       
        mxgraphCanvas.removeNode(businessObject);
        syncViewMap();            
    }

    @Override
    public void removeEdge(BusinessObjectLight businessObject) {
        mxgraphCanvas.removeEdge(businessObject);
        syncViewMap();
    }

    @Override
    public void nodeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void edgeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
