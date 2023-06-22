/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.business.analytics.views;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class implements the functionality corresponding to manage QinQ technology
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Service
public class E2EViewProvider{
    /**
     * Reference to the Translation Service
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
     * Reference to the Application Entity Manager
     */
    @Autowired
    private PhysicalConnectionsService physicalConnectionsService;
        
    private List<E2ENode> nodes;
    private List<E2EEdge> edges;
    private HashMap<E2EEdge, E2ENode> edgeSource;
    private HashMap<E2EEdge, E2ENode> edgeTarget;
      
    private ViewObject savedView;
    
    public final static String VIEW_CLASS = "EndToEndView"; 

    
    public static String CLASS_VIEW = "MPLSModuleView";
    /**
     * A side in a tributary link
     */
    public static String RELATIONSHIP_MPLSENDPOINTA = "mplsEndpointA";
    /**
     * B side in a tributary link
     */
    public static String RELATIONSHIP_MPLSENDPOINTB = "mplsEndpointB";
    /**
     * Relates a pseudowire and its output interface, the output interface is the endpoint of a MPLS link if is a port
     */
    public static String RELATIONSHIP_MPLS_PW_ISRELATEDWITH_VFI = "mplsPseudowireIsRelatedWithVFI";
    /**
     * Relates two pseudowires that are logical linked inside a MPLS device
     */
    public static String RELATIONSHIP_MPLS_PW_ISRELATEDWITH_PW = "mplsPseudowireIsRelatedWithPseudowire";
    /**
    /**
     * validates a saved end to end view
     * @param linkClasses the classes of the links, in the view
     * @param linkIds the ids of the links in the view
     * @param savedView saved structure, possibly outdated the end to end view
     * @return an updated end to end view 
     */
    public ViewObject validateSavedE2EView(List<String> linkClasses, 
            List<String> linkIds, ViewObject savedView) throws InventoryException
    {
        try{
//<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
//        try {
//            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/end2end_validate_view.xml");
//            fos.write(savedView.getStructure());
//            fos.close();
//        } catch(Exception e) {}
//</editor-fold>
            if(savedView != null){
                //first we create the current view
                createE2EView(linkClasses, linkIds, true, true, true, true);
                this.savedView = savedView;
                //the we load the saved view to compare
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                QName qNode = new QName("node"); //NOI18N
                QName qEdge = new QName("edge"); //NOI18N
                QName qControlPoint = new QName("controlpoint"); //NOI18N

                ByteArrayInputStream bais = new ByteArrayInputStream(savedView.getStructure());
                XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

                while (reader.hasNext()) {
                    int event = reader.next();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        if (reader.getName().equals(qNode)) {
                            String objectClass = reader.getAttributeValue(null, "class");

                            String xCoordinate = reader.getAttributeValue(null,"x");
                            String yCoordinate = reader.getAttributeValue(null,"y");
                            String objectId = reader.getElementText();

                            E2ENode node = new E2ENode(new BusinessObjectLight(objectClass, objectId, null));
                            if(nodes.contains(node)){
                               nodes.get(nodes.indexOf(node)).getProperties().put("x", xCoordinate);
                               nodes.get(nodes.indexOf(node)).getProperties().put("y", yCoordinate);
                            }
                        }else {
                            if (reader.getName().equals(qEdge)) {
                                String objectId = reader.getAttributeValue(null, "id"); //NOI18N
                                String className = reader.getAttributeValue(null,"class"); //NOI18N

                                E2EEdge connection = new E2EEdge(new BusinessObjectLight(className, objectId, null));

                                if (edges.contains(connection)){//the connection exists
                                    List<Point> localControlPoints = new ArrayList<>();
                                    while(true) {
                                        reader.nextTag();

                                        if (reader.getName().equals(qControlPoint)) {
                                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                                localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                        } else {
                                            edges.get(edges.indexOf(connection)).getProperties().put("controlPoints", localControlPoints);
                                            break;
                                        }
                                    }//end while
                                }
                            }
                        }//end if edge 
                    }//end xml
                }//end while
                return getAsXML();
            }
        } catch (Exception ex) {
            throw new InventoryException(ex.getMessage());
        }
        return null;
    }
    /**
     * Calls the methods that creates the end to end view
     * @param linkClasses list of the links classes
     * @param linkIds list of the links ids 
     * @param includePhyscalPaths include physical path continuity for the logical links in the end to end view
     * @param includeVlans include VLANs continuity in the view
     * @param includePhyscialLinks include physical links in the view
     * @param includeBDis include bridge domains in the end view
     * @throws InvalidArgumentException if you can not reach the back end
     */
    public void createE2EView(List<String> linkClasses, List<String> linkIds, 
            boolean includePhyscalPaths, boolean includeVlans, 
            boolean includePhyscialLinks, boolean includeBDis) 
            throws InvalidArgumentException, InventoryException
    {
        if (bem == null)
            throw new InvalidArgumentException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
       
        try{
            savedView = null;
            List<ObjectLinkObjectDefinition> e2eMap = new ArrayList<>();
            nodes = new ArrayList<>();
            edges = new ArrayList<>();
            edgeSource = new HashMap<>();
            edgeTarget = new HashMap<>();
            for(int x=0 ; x < linkIds.size(); x++){
                if(mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, linkClasses.get(x)))
                    e2eMap.add(getPhysicalLinkDetail(linkClasses.get(x), linkIds.get(x)));
                
                else if(mem.isSubclassOf(Constants.CLASS_GENERICLOGICALCONNECTION, linkClasses.get(x))){
                    //firts we get the logical link details
                    GenericConnectionDefinition logicalCircuitDetails = getLogicalLinkDetail(linkClasses.get(x), linkIds.get(x));
                    BusinessObjectLight physicalEndpointA = null, logicalEndpointA = null, physicalEndpointB = null, logicalEndpointB = null, deviceA = null, deviceB = null;
                    //start logical part         
                    if(logicalCircuitDetails.getEndpointA() != null){
                        //Side A, first we must check the endpoint if is physical or logical
                        
                        if(mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, logicalCircuitDetails.getEndpointA().getClassName()))
                            physicalEndpointA = logicalCircuitDetails.getEndpointA();
                        
                        else if(mem.isSubclassOf(Constants.CLASS_GENERICLOGICALPORT, logicalCircuitDetails.getEndpointA().getClassName())){
                            logicalEndpointA = logicalCircuitDetails.getEndpointA();
                            
                            if(!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty() && mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, logicalCircuitDetails.getPhysicalPathForEndpointA().get(0).getClassName()))
                                physicalEndpointA = logicalCircuitDetails.getPhysicalPathForEndpointA().get(0);
                        }
                        //we need the parent of the endpoint, it should be a GenericCommunicationsElmenet because only between these you can create logical connections 
                        BusinessObjectLight parentEquipmentA = bem.getFirstParentOfClass(logicalCircuitDetails.getEndpointA().
                                getClassName(), logicalCircuitDetails.getEndpointA().getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);

                        //now we found the device
                        if(parentEquipmentA != null &&  mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, parentEquipmentA.getClassName()))
                            deviceA = parentEquipmentA; //GenericCommunicationsElement
                        
                        //we must check the pseudowire-pseudowire, pseudowire-VFI and ServiceInstance continuity 
                        if (Constants.CLASS_PSEUDOWIRE.equals(logicalCircuitDetails.getEndpointA().getClassName())){
                            ObjectLinkObjectDefinition pw = getLogicalInterfaceContinuity(logicalCircuitDetails.getConnectionObject(), deviceA, logicalCircuitDetails.getEndpointA());
                            if(pw != null)
                                e2eMap.add(pw);
                        }
                        if(Constants.CLASS_SERVICE_INSTANCE.equals(logicalCircuitDetails.getEndpointA().getClassName())){
                            HashMap<BusinessObjectLight, List<BusinessObjectLight>> bdisPaths = getEndPointPhysicalContinuityByLogicalRels(logicalCircuitDetails.getEndpointA(), "networkHasBridgeInterface");
                            for (Map.Entry<BusinessObjectLight, List<BusinessObjectLight>> entry : bdisPaths.entrySet())
                                e2eMap.addAll(physicalPathReader(entry.getValue()));
                        }
                    }//logical part side B   
                    if(logicalCircuitDetails.getEndpointB() != null){
                        if(mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, logicalCircuitDetails.getEndpointB().getClassName()))
                            physicalEndpointB = logicalCircuitDetails.getEndpointB();
                        
                        else if(mem.isSubclassOf(Constants.CLASS_GENERICLOGICALPORT, logicalCircuitDetails.getEndpointB().getClassName())){
                            logicalEndpointB = logicalCircuitDetails.getEndpointB();
                            
                            if(!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty() && mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, logicalCircuitDetails.getPhysicalPathForEndpointB().get(0).getClassName()))
                                physicalEndpointB = logicalCircuitDetails.getPhysicalPathForEndpointB().get(0);
                        }
                        //we must found the parent device of the end point
                        BusinessObjectLight parentEquipmentB = bem.getFirstParentOfClass(logicalCircuitDetails.getEndpointB().
                                getClassName(), logicalCircuitDetails.getEndpointB().getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                        
                        //the parent will be the last found in te list of parents
                        if(parentEquipmentB != null &&  mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, parentEquipmentB.getClassName()))
                            deviceB = parentEquipmentB; //GenericCommunicationsElement
                        
                        //we must check the pseudowire-pseudowire, pseudowire-VFI and ServiceInstance continuity 
                        if (Constants.CLASS_PSEUDOWIRE.equals(logicalCircuitDetails.getEndpointB().getClassName())){
                            ObjectLinkObjectDefinition pw = getLogicalInterfaceContinuity(logicalCircuitDetails.getConnectionObject(), deviceB, logicalCircuitDetails.getEndpointB());
                            if(pw != null)
                                e2eMap.add(pw);
                        }
                        
                        if(Constants.CLASS_SERVICE_INSTANCE.equals(logicalCircuitDetails.getEndpointB().getClassName())){
                            HashMap<BusinessObjectLight, List<BusinessObjectLight>> bdisPaths = getEndPointPhysicalContinuityByLogicalRels(logicalCircuitDetails.getEndpointB(), "networkHasBridgeInterface");
                            for (Map.Entry<BusinessObjectLight, List<BusinessObjectLight>> entry : bdisPaths.entrySet())
                                e2eMap.addAll(physicalPathReader(entry.getValue()));
                        }   
                    }
                    //here we have have the first part of the logical connection [deviceA -- logical link -- deviceB]
                    e2eMap.add(new ObjectLinkObjectDefinition(deviceA, physicalEndpointA, logicalEndpointA, 
                            logicalCircuitDetails.getConnectionObject(), logicalEndpointB, physicalEndpointB, deviceB));
                    //physical part, side A
                    if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) 
                        e2eMap.addAll(physicalPathReader(logicalCircuitDetails.getPhysicalPathForEndpointA()));
                    //physical part, side B
                    if (!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()) 
                        e2eMap.addAll(physicalPathReader(logicalCircuitDetails.getPhysicalPathForEndpointB()));

                    if(includeVlans){
                        //VLANs side A
                        HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForVlansEndpointA = new HashMap<>(); 
                        if(logicalCircuitDetails.getEndpointA() != null && !logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty())
                            physicalPathForVlansEndpointA = getEndPointPhysicalContinuityByLogicalRels(logicalCircuitDetails.getPhysicalPathForEndpointA().get(logicalCircuitDetails.getPhysicalPathForEndpointA().size() -1), "portBelongsToVlan");
                        else
                            physicalPathForVlansEndpointA = getEndPointPhysicalContinuityByLogicalRels(physicalEndpointA == null ? logicalCircuitDetails.getEndpointA() : physicalEndpointA, "portBelongsToVlan");
                        
                        for (Map.Entry<BusinessObjectLight, List<BusinessObjectLight>> entry : physicalPathForVlansEndpointA.entrySet())
                            e2eMap.addAll(physicalPathReader(entry.getValue()));
                        //VLANs side B
                        HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForVlansEndpointB = new HashMap<>();    
                        if(logicalCircuitDetails.getEndpointB() != null && !logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty())
                            physicalPathForVlansEndpointB = getEndPointPhysicalContinuityByLogicalRels(logicalCircuitDetails.getPhysicalPathForEndpointB().get(logicalCircuitDetails.getPhysicalPathForEndpointB().size() -1), "portBelongsToVlan");
                        else if(logicalCircuitDetails.getEndpointB() != null)
                            physicalPathForVlansEndpointB = getEndPointPhysicalContinuityByLogicalRels(physicalEndpointB == null ? logicalCircuitDetails.getEndpointB() : physicalEndpointB, "portBelongsToVlan");
                        
                        for (Map.Entry<BusinessObjectLight, List<BusinessObjectLight>> entry : physicalPathForVlansEndpointB.entrySet()) 
                            e2eMap.addAll(physicalPathReader(entry.getValue()));
                    }
                    //if(includeBDis){//TODO include the BDIs logic}
                }//end if is logical link
            }//end for
            
            e2eMap.forEach(def -> {
                if(def.getDeviceA() != null && !nodes.contains(new E2ENode(def.getDeviceA())))
                    nodes.add(new E2ENode(def.getDeviceA()));
                if(def.getDeviceB() != null && !nodes.contains(new E2ENode(def.getDeviceB())))
                    nodes.add(new E2ENode(def.getDeviceB()));
                if(def.getConnectionObject() != null){
                    E2EEdge newEdge = new E2EEdge(def.getConnectionObject());
                    Properties properties = new Properties();
                    newEdge.setProperties(properties);
                    edges.add(newEdge);
                
                    if(def.getDeviceA() != null){
                        edgeSource.put(new E2EEdge(def.getConnectionObject()), new E2ENode(def.getDeviceA()));
                        properties.put("asidephysicalport", def.getPhysicalEndpointObjectA() != null ? def.getPhysicalEndpointObjectA().getName(): "");
                        properties.put("asidelogicalport", def.getLogicalEndpointObjectA() != null ? def.getLogicalEndpointObjectA().getName() : "");
                    }
                    if(def.getDeviceB() != null){
                        edgeTarget.put(new E2EEdge(def.getConnectionObject()), new E2ENode(def.getDeviceB()));
                        properties.put("bsidephysicalport", def.getPhysicalEndpointObjectB() != null ? def.getPhysicalEndpointObjectB().getName() : "");
                        properties.put("bsidelogicalport", def.getLogicalEndpointObjectB() != null ? def.getLogicalEndpointObjectB().getName() : "");
                    }
                }
            });
        } catch (Exception ex) {
            throw new InventoryException(ex.getMessage());
        }            
    }
    
    /**
     * The XML representation of the view. Typically used to serialize it
     * @return a XML representation of request end to end view
     */
    public ViewObject getAsXML() throws InventoryException
    {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            QName qnameView = new QName("view");
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), Constants.VIEW_FORMAT_VERSION));
            QName qnameClass = new QName("class");
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters(VIEW_CLASS));
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            if(savedView == null)
                orderNodes();
            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            int x = 15, y = 180, i = 2;
            for(E2ENode node : nodes){
                
                QName qnameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                
                xmlew.add(xmlef.createAttribute(new QName("x"), node.getProperties().get("x") != null ? (String)node.getProperties().get("x") : Integer.toString(x)));
                //                xmlew.add(xmlef.createAttribute(new QName("x"), node.getProperties().get("x") != null ?
                //                        (String)node.getProperties().get("x") : Integer.toString(x)));
                //we do this with the y in order to set the node one up an one down in the end to end view
                y += (i % 2 != 0) ? 175 + i * 2 : (-145);

                //                xmlew.add(xmlef.createAttribute(new QName("y"), node.getProperties().get("y") != null ? 
                //                        (String)node.getProperties().get("y") : Integer.toString(y)));
                xmlew.add(xmlef.createAttribute(new QName("y"),  node.getProperties().get("y") != null ? (String)node.getProperties().get("y") : Integer.toString(y)));

                xmlew.add(xmlef.createAttribute(new QName("class"), node.getBussinesObject().getClassName()));
                xmlew.add(xmlef.createCharacters(node.getBussinesObject().getId()));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
                x += 115; i++;
            }   
            
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            for (E2EEdge edge : edges) {
                if (!edgeSource.containsKey(edge) || !edgeTarget.containsKey(edge)) //This connection is malformed because one of the endpoints does not exist
                    continue;                                                       //probably, it was moved to another parent
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                
                xmlew.add(xmlef.createAttribute(new QName("id"), edge.getEdge().getId()));
                xmlew.add(xmlef.createAttribute(new QName("class"), edge.getEdge().getClassName()));
                //side a
                xmlew.add(xmlef.createAttribute(new QName("asideid"), edgeSource.get(edge).getBussinesObject().getId()));
                xmlew.add(xmlef.createAttribute(new QName("asideclass"), edgeSource.get(edge).getBussinesObject().getClassName()));
                //port a info side a
                xmlew.add(xmlef.createAttribute(new QName("asidephysicalport"), edge.getProperties().getProperty("asidephysicalport")));
                xmlew.add(xmlef.createAttribute(new QName("asidelogcialport"), edge.getProperties().getProperty("asidelogicalport")));
                //side b
                xmlew.add(xmlef.createAttribute(new QName("bsideid"), edgeTarget.get(edge).getBussinesObject().getId()));
                xmlew.add(xmlef.createAttribute(new QName("bsideclass"), edgeTarget.get(edge).getBussinesObject().getClassName()));
                //port b info side b
                xmlew.add(xmlef.createAttribute(new QName("bsidephysicalport"), edge.getProperties().getProperty("bsidephysicalport")));
                xmlew.add(xmlef.createAttribute(new QName("bsidelogcialport"), edge.getProperties().getProperty("bsidelogicalport")));
                
                List<Point> points = (List<Point>)edge.getProperties().get("controlPoints");
                if(points != null){
                    for(Point point : points){
                        QName qnameControlpoint = new QName("controlpoint");
                        xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                        xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(point.x)));
                        xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(point.y)));
                        xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                    }
                }
                
                xmlew.add(xmlef.createEndElement(qnameEdge, null));
            }   xmlew.add(xmlef.createEndElement(qnameEdges, null));
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            ViewObject updatedViewObject = new ViewObject(savedView != null ? savedView.getId() : -1,
                    savedView != null ? savedView.getName() : "",
                    savedView != null ? savedView.getDescription() : "Generated by e2e method",
                    savedView != null ? savedView.getViewClassName() : "EndToEndView"); //TODO the ViewClassName should be managed
            updatedViewObject.setStructure(baos.toByteArray());
            //<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
            //        try {
            //            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/end2end_created_view_in_module.xml");
            //            fos.write(updatedViewObject.getStructure());
            //            fos.close();
            //        } catch(Exception e) {}
            //</editor-fold>

            return updatedViewObject;
        
        } catch (XMLStreamException ex) {
            throw new InventoryException(ex.getMessage());
        }
    }
        
    /**
     * 
     * @param currentConnection the current connection to start the continuity
     * @param device the current device to start the continuity
     * @param logicalInterface the logical interface 
     * @return details of the connections
     * @throws MetadataObjectNotFoundException If the class provided does not exist
     * @throws BusinessObjectNotFoundException if the object does not exist
     * @throws InvalidArgumentException If the object id is null
     * @throws ApplicationObjectNotFoundException If the object provided is not in the standard containment hierarchy
     */
    private ObjectLinkObjectDefinition getLogicalInterfaceContinuity(
            BusinessObjectLight currentConnection, BusinessObjectLight device, 
            BusinessObjectLight logicalInterface) 
           throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, 
            InvalidArgumentException, ApplicationObjectNotFoundException
    {
        //Pseudowire has to kins of relationships:
        //other Pseudowire
        //a VFI, th VFI could be related with a BridgeDomain, (to be confirmed)
        if(logicalInterface.getClassName().equals(Constants.CLASS_PSEUDOWIRE)){
            HashMap<String, List<BusinessObjectLight>> rels = bem.getSpecialAttributes(logicalInterface.getClassName(), logicalInterface.getId());

            for (Map.Entry<String, List<BusinessObjectLight>> entry : rels.entrySet()) {
                String rel = entry.getKey();
                List<BusinessObjectLight> objs = entry.getValue();
                //the related VFIs 
                if(rel.equals(RELATIONSHIP_MPLS_PW_ISRELATEDWITH_VFI)){
                    for (BusinessObjectLight obj : objs) {
                        return new ObjectLinkObjectDefinition(
                                device, logicalInterface, 
                                new BusinessObject("VFI", "@" + UUID.randomUUID().toString(), logicalInterface.getName() + ":" + obj.getName()),
                                null, obj);
                    }
                }

                if(rel.equals(RELATIONSHIP_MPLS_PW_ISRELATEDWITH_PW)){
                    //we get the related pws
                    for (BusinessObjectLight siblingPw : objs) {
                        //we must check if the related pw has MPLS connections ongoig
                        HashMap<String, List<BusinessObjectLight>> siblingPwRels = bem.getSpecialAttributes(siblingPw.getClassName(), siblingPw.getId());
                        for (Map.Entry<String, List<BusinessObjectLight>> pwEntry : siblingPwRels.entrySet()) {
                            String siblingPwRel = pwEntry.getKey();
                            List<BusinessObjectLight> mplsLinks = pwEntry.getValue();
                            if(siblingPwRel.equals(RELATIONSHIP_MPLSENDPOINTA) || siblingPwRel.equals(RELATIONSHIP_MPLSENDPOINTB)){
                                for(BusinessObjectLight mplsLink : mplsLinks){
                                    if(!mplsLink.getId().equals(currentConnection.getId())){ //not the mplslink that we are evaluating
                                        //we need the device parent of the pw of the other side of the connection
                                        List<BusinessObjectLight> endPoints = bem.getSpecialAttribute(mplsLink.getClassName(), mplsLink.getId(), siblingPwRel.equals(RELATIONSHIP_MPLSENDPOINTA) ? RELATIONSHIP_MPLSENDPOINTB : RELATIONSHIP_MPLSENDPOINTA);
                                        if(!endPoints.isEmpty()){
                                            BusinessObjectLight otherSideDevice = bem.getFirstParentOfClass(endPoints.get(0).getClassName(), endPoints.get(0).getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                                            //to ensure that we really found the other side
                                            if(!device.getId().equals(otherSideDevice.getId()) && !siblingPw.getId().equals(endPoints.get(0).getId())){
                                                return new ObjectLinkObjectDefinition(
                                                    device, siblingPw, 
                                                    mplsLink,
                                                    endPoints.get(0), otherSideDevice);
                                                
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }//end for related pw
                }
            }//end for
        }//en pseudowire
        return null;
    }
        
    /**
     * Gets the details of logical connection (SDHTributaryLink or MPLSLink)
     * it gets the endpoints of the logical connections and the physical path 
     * from the end point until it finds continuity
     * @param linkClass the logical link class
     * @param linkId the logical link id
     * @return a GenericConnectionDefinition that contains the connection, end points, and their physical paths
     * @throws BusinessObjectNotFoundException If the object to evaluate can not be found
     * @throws MetadataObjectNotFoundException If any of the classes provided could not be found
     * @throws ApplicationObjectNotFoundException If the object provided is not in the standard containment hierarchy
     * @throws InvalidArgumentException If the oid is null
     */
    public GenericConnectionDefinition getLogicalLinkDetail(String linkClass,  String linkId) 
            throws InvalidArgumentException, MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException 
    {
        if (bem == null)
            throw new InvalidArgumentException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        BusinessObject linkObject = bem.getObject(linkClass, linkId);

        BusinessObjectLight endpointA = null;
        BusinessObjectLight endpointB = null;
        List<BusinessObjectLight> physicalPathA = null, physicalPathB = null;
        String endpointARelationshipName, endpointBRelationshipName;

        if (mem.isSubclassOf("GenericSDHTributaryLink", linkClass)) { //NOI18N
            endpointARelationshipName = "sdhTTLEndpointA"; //NOI18N
            endpointBRelationshipName = "sdhTTLEndpointB"; //NOI18N
        } else if (Constants.CLASS_MPLSLINK.equals(linkClass)) {
            endpointARelationshipName = "mplsEndpointA"; //NOI18N
            endpointBRelationshipName = "mplsEndpointB"; //NOI18N
        } else
             throw new InvalidArgumentException(String.format("Class %s is not a supported logical link", linkClass)); 

        List<BusinessObjectLight> endpointARelationship = bem.getSpecialAttribute(linkClass, linkId, endpointARelationshipName);
        if (!endpointARelationship.isEmpty()) {
            endpointA = endpointARelationship.get(0);
            //if the port is a GenericlogicalPort we need its physical parent port
            if(endpointA != null && mem.isSubclassOf(Constants.CLASS_GENERICLOGICALPORT, endpointA.getClassName())){
                BusinessObjectLight physicalPort = bem.getFirstParentOfClass(endpointA.getClassName(), endpointA.getId(), Constants.CLASS_GENERICPHYSICALPORT);
                if(physicalPort != null)
                    physicalPathA = physicalConnectionsService.getPhysicalPath(physicalPort.getClassName(), physicalPort.getId());
            }
            else if(endpointA != null && mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, endpointA.getClassName()))
                physicalPathA = physicalConnectionsService.getPhysicalPath(endpointA.getClassName(), endpointA.getId());
        }

        List<BusinessObjectLight> endpointBRelationship = bem.getSpecialAttribute(linkClass, linkId, endpointBRelationshipName);
        if (!endpointBRelationship.isEmpty()) {
            endpointB = endpointBRelationship.get(0);
            //if the port is a GenericlogicalPort we need its physical parent port
            if(endpointB != null && mem.isSubclassOf(Constants.CLASS_GENERICLOGICALPORT, endpointB.getClassName())){
                BusinessObjectLight physicalPort = bem.getFirstParentOfClass(endpointB.getClassName(), endpointB.getId(), Constants.CLASS_GENERICPHYSICALPORT);
                if(physicalPort != null)
                    physicalPathB = physicalConnectionsService.getPhysicalPath(physicalPort.getClassName(), physicalPort.getId());
            }
            else if(endpointB != null && mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, endpointB.getClassName()))
                physicalPathB = physicalConnectionsService.getPhysicalPath(endpointB.getClassName(), endpointB.getId());
        }

        return new GenericConnectionDefinition(linkObject, endpointA, endpointB, 
                physicalPathA == null ? new ArrayList<>() : physicalPathA, 
                physicalPathB == null ? new ArrayList<>() : physicalPathB);
    }
    
    /**
     * Gets the details of physical connection (GenericPhysicalLink)
     * it gets the endpoints of the connection and the physical path 
     * from the endpoint until it finds continuity
     * @param linkClass the physical link class
     * @param linkId the physical link id
     * @return a GenericConnectionDefinition that contains the connection, end points, and their physical paths
     * @throws BusinessObjectNotFoundException If the object to evaluate can not be found
     * @throws MetadataObjectNotFoundException If any of the classes provided could not be found
     * @throws ApplicationObjectNotFoundException If the object provided is not in the standard containment hierarchy
     * @throws InvalidArgumentException If the oid is null
     */
    public ObjectLinkObjectDefinition getPhysicalLinkDetail(String linkClass, String linkId) 
            throws InvalidArgumentException, MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException
    {
        if (bem == null)
            throw new InvalidArgumentException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));

        BusinessObject linkObject = bem.getObject(linkClass, linkId);

        BusinessObjectLight endpointA = null, endpointB = null;

        String endpointARelationshipName, endpointBRelationshipName;
        if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, linkClass)) { //NOI18N
            endpointARelationshipName = "endpointA"; //NOI18N
            endpointBRelationshipName = "endpointB"; //NOI18N
        }else 
            throw new InvalidArgumentException(String.format("Class %s is not a supported physical link", linkClass)); //NOI18N

        List<BusinessObjectLight> endpointARelationship = bem.getSpecialAttribute(linkClass, linkId, endpointARelationshipName); //NOI18N
        BusinessObjectLight deviceA = null;
        if (!endpointARelationship.isEmpty()){ 
            endpointA = endpointARelationship.get(0);
            deviceA = bem.getFirstParentOfClass(endpointA.getClassName(), endpointA.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
            if(deviceA == null)
                deviceA = bem.getFirstParentOfClass(endpointA.getClassName(), endpointA.getId(), Constants.CLASS_GENERICDISTRIBUTIONFRAME);
        }

        List<BusinessObjectLight> endpointBRelationship = bem.getSpecialAttribute(linkClass, linkId, endpointBRelationshipName); //NOI18N
        BusinessObjectLight deviceB = null;
        if (!endpointBRelationship.isEmpty()) {
            endpointB = endpointBRelationship.get(0);
            deviceB = bem.getFirstParentOfClass(endpointB.getClassName(), endpointB.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
            if(deviceB == null)
                deviceB = bem.getFirstParentOfClass(endpointB.getClassName(), endpointB.getId(), Constants.CLASS_GENERICDISTRIBUTIONFRAME);

        }
        return new ObjectLinkObjectDefinition(deviceA, endpointA, linkObject, endpointB, deviceB);
    }
    
    /**
     * Checks the continuity throw ports that belongs to the same VLAN
     * @param endpoint a given port to check if belong to a vlan
     * @return a map with key: port, value: physical path of that port
     * @throws BusinessObjectNotFoundException If the object to evaluate can not be found
     * @throws MetadataObjectNotFoundException If any of the classes provided could not be found
     * @throws ApplicationObjectNotFoundException If the object provided is not in the standard containment hierarchy
     * @throws InvalidArgumentException If the oid is null
     */
    private HashMap<BusinessObjectLight, List<BusinessObjectLight>> getVLANContinuity(BusinessObjectLight endpoint) 
            throws InvalidArgumentException, MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, InvalidArgumentException, 
            ApplicationObjectNotFoundException
    {
        if (bem == null)
            throw new InvalidArgumentException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        HashMap<BusinessObjectLight, List<BusinessObjectLight>> vlansPhysicalPath = new HashMap<>();
        if(endpoint != null){
            //we get the the vlans to which the port belongs
            List<BusinessObjectLight> vlans = bem.getSpecialAttribute(endpoint.getClassName(), endpoint.getId(), "portBelongsToVlan");
            for (BusinessObjectLight vlan : vlans) { //We get all the port of every vlan
                List<BusinessObjectLight> vlanPorts = bem.getSpecialAttribute(vlan.getClassName(), vlan.getId(), "portBelongsToVlan");
                for (BusinessObjectLight vlanPort : vlanPorts) {
                    if(vlanPort.getId() != null && endpoint.getId() != null && !vlanPort.getId().equals(endpoint.getId())){//we get the physical path for every port of the vlan except of the given endpoint 

                        List<BusinessObjectLight> vlanPhysicalPath;
                        if(!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, vlanPort.getClassName())){
                            BusinessObjectLight physicalPort = bem.getFirstParentOfClass(vlanPort.getClassName(), vlanPort.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                            vlanPhysicalPath = physicalConnectionsService.getPhysicalPath(physicalPort.getClassName(), physicalPort.getId());
                        }
                        else 
                            vlanPhysicalPath = physicalConnectionsService.getPhysicalPath(vlanPort.getClassName(), vlanPort.getId());
                        if(!vlanPhysicalPath.isEmpty())
                            vlansPhysicalPath.put(vlanPort, vlanPhysicalPath);
                    }
                }
            }
        }
        return vlansPhysicalPath;
    }
    
    /**
     * Given an endpoint and a relationship name,  it finds the other side of 
     * that relationship, that other side is a logical entity that groups interfaces
     * (e.g. VLANs, BridgeDomains)
     * This set of interfaces represents a logical continuity. 
     * Those interfaces could be logical or physical ports, endpoints of some 
     * connections, so we need to iterate over every interface and retrieve its 
     * physical path in a HashMap: interface -> PhysicalPath
     * 
     * @param endpoint a given port to check if belong to a set of other interfaces
     * @param relationshipName a relationship that groups interfaces.
     * @return a map with key: port, value: physical path of that port
     * @throws BusinessObjectNotFoundException If the object to evaluate can not be found
     * @throws MetadataObjectNotFoundException If any of the classes provided could not be found
     * @throws ApplicationObjectNotFoundException If the object provided is not in the standard containment hierarchy
     * @throws InvalidArgumentException If the oid is null
     */
    private HashMap<BusinessObjectLight, List<BusinessObjectLight>> getEndPointPhysicalContinuityByLogicalRels(BusinessObjectLight endpoint, String relationshipName) 
            throws InvalidArgumentException, MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, InvalidArgumentException, 
            ApplicationObjectNotFoundException
    {
        if (bem == null)
            throw new InvalidArgumentException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        HashMap<BusinessObjectLight, List<BusinessObjectLight>> portsPhysicalPath = new HashMap<>();
        if(endpoint != null){
            //we get all the parents to which the endpoint belongs
            List<BusinessObjectLight> groupsOfLogicalInterfaces = bem.getSpecialAttribute(endpoint.getClassName(), endpoint.getId(), relationshipName);
            //e.g if relationshipName is networkHasBridgeInterface here we have the BridgeDomains where the endpoint (the BridgeDomainInterface a serviceInstance) belongs to
            //e.g if relationshipName is portBelongsToVlan here we have all the VLANs where the endpoint (GenericPort) belongs to
            for (BusinessObjectLight groupOfInterface : groupsOfLogicalInterfaces) { 
                //now we get the other interfaces in that parent, siblings of the given endpoint
                List<BusinessObjectLight> groupedInterfaces = bem.getSpecialAttribute(groupOfInterface.getClassName(), groupOfInterface.getId(), relationshipName);
                for (BusinessObjectLight interface_ : groupedInterfaces) {
                    if(interface_.getId() != null && endpoint.getId() != null && !interface_.getId().equals(endpoint.getId())){//we get the physical path for every port of the vlan except of the given endpoint 

                        List<BusinessObjectLight> interfacePhysicalPath;
                        if(!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, interface_.getClassName())){
                            BusinessObjectLight physicalPort = bem.getFirstParentOfClass(interface_.getClassName(), interface_.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                            interfacePhysicalPath = physicalConnectionsService.getPhysicalPath(physicalPort.getClassName(), physicalPort.getId());
                        }
                        else 
                            interfacePhysicalPath = physicalConnectionsService.getPhysicalPath(interface_.getClassName(), interface_.getId());
                        if(!interfacePhysicalPath.isEmpty())
                            portsPhysicalPath.put(interface_, interfacePhysicalPath);
                    }
                }
            }
        }
        return portsPhysicalPath;
    }

    /**
     * Provides a generic way to get the parents(the GenericCommunicationsElement or 
     * the GenericBox) parents of the GenericPorts in a given path, then it parses 
     * between physical path and a simplified list, of objects connections objects
     * [Node1 - Node2]
     * [Node2 - Node3]
     * [Node3 - Node4]
     * @param path a physical path with endpoint, connection, endpoint(one or several ports, mirror, virtual, service instances)
     * @return a list of ConfigurationItem-connection-ConfigurationItem
     * @throws BusinessObjectNotFoundException If the object to evaluate can not be found
     * @throws MetadataObjectNotFoundException If any of the classes provided could not be found
     * @throws ApplicationObjectNotFoundException If the object provided is not in the standard containment hierarchy
     * @throws InvalidArgumentException If the oid is null
     */
    private List<ObjectLinkObjectDefinition> physicalPathReader(List<BusinessObjectLight> path) 
            throws InvalidArgumentException, MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException
    {
        List<ObjectLinkObjectDefinition> connectionsMap = new ArrayList<>();
        BusinessObject connection  = null;
        BusinessObjectLight device = null;
        BusinessObjectLight endpoint = null;

        BusinessObjectLight sourceDevice = null;

        //with this for we are reading the path 3 at a time, endpoint - connection -endpoint (ignoring the mirror ports)
        for (BusinessObjectLight obj : path) {

                if(mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, obj.getClassName()))
                    connection = bem.getObject(obj.getClassName(), obj.getId());
                else if(mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, obj.getClassName())) {
                    device = bem.getFirstParentOfClass(obj.getClassName(), obj.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                    //if the parent could not be found it should be aGenericCommunications element(e.g. Router, Cloud, MPLSRouter, etc)
                    if(device == null)
                        device = bem.getFirstParentOfClass(obj.getClassName(), obj.getId(), Constants.CLASS_GENERICDISTRIBUTIONFRAME);
                }
                if(sourceDevice == null){
                    sourceDevice = device;
                    endpoint = obj;
                }
                else if(sourceDevice.equals(device))//this is in case of mirror ports to ignore the case
                    continue;
                //if we found the deviceA, connection, deviceB we are available to save the data and create connecttion set
                if(connection != null && sourceDevice != null && device != null){//TODO check what happend with unconnected things
                    connectionsMap.add(new ObjectLinkObjectDefinition(sourceDevice, endpoint, connection, obj, device));
                    connection = null;
                    device = null;
                    endpoint = null;
                    sourceDevice = null;
                }
        }//end for
        return connectionsMap;
    }
    
    /**
     * Attempt to arrange nodes graphically 
     */
    private void orderNodes(){
        //initial coordinates, and a counter
        int x = 100, y = 0, fixer = 2;
        int direction = 0;
        int lastDirection = 0;
        int lastAddedNameLength = 0;
        int nodeDistanceX = 150, nodeDistanceY = 80;
        //number of rels of a node
        HashMap <E2ENode, Integer> rels = new HashMap<>();
        //initial coordinates
        List<E2ENode> addedNodes = new ArrayList<>();
        BusinessObjectLight lastAddedRel = null;
        BusinessObjectLight lastAddedObj = null;
                
        for (Map.Entry<E2EEdge, E2ENode> entry : edgeSource.entrySet()) {
            E2EEdge edge = entry.getKey();
            E2ENode sourceNode = entry.getValue();
            E2ENode targetNode = edgeTarget.get(edge);
            
            if(addedNodes.contains(sourceNode))
                rels.put(sourceNode, (rels.get(sourceNode) + 1));
            else{
                addedNodes.add(sourceNode);
                rels.put(sourceNode, 1);
            }
            if(sourceNode != null){
                //we must check if directions must be changed
                if ((lastAddedRel != null && lastAddedRel.getClassName().equals(edge.getEdge().getClassName()))
                        && (lastAddedObj != null && lastAddedObj.getClassName().equals(targetNode.getBussinesObject().getClassName())))
                    direction = 0;
                else
                    direction = 1;
                //if is first time it is always horizontal.
                if(lastAddedObj != null && lastAddedRel != null){
                    if(direction == 0)
                        x += nodeDistanceX + edge.getEdge().toString().length() * 5;
                    else if(direction == 1){
                        if(targetNode.getBussinesObject().toString().length() < lastAddedNameLength)
                            x += (targetNode.getBussinesObject().toString().length() * fixer) + 5;
                        else
                            x -= (targetNode.getBussinesObject().toString().length() * fixer) - 5;
                        y += nodeDistanceY;
                    }
                }
                //source
                if(nodes.get(nodes.indexOf(sourceNode)).getProperties().getProperty("x") == null)
                    nodes.get(nodes.indexOf(sourceNode)).getProperties().setProperty("x", Integer.toString(x));

                if(nodes.get(nodes.indexOf(sourceNode)).getProperties().getProperty("y") == null)
                    nodes.get(nodes.indexOf(sourceNode)).getProperties().setProperty("y", Integer.toString(y));

                if(direction == 0)
                    x -= edge.getEdge().toString().length() * 5;
                
                lastAddedObj = sourceNode.getBussinesObject();
                lastAddedRel = edge.getEdge();
                lastDirection = direction;
                lastAddedNameLength = sourceNode.getBussinesObject().toString().length();
            }
            
            if(targetNode != null){
                
                if((lastAddedRel != null && lastAddedRel.getClassName().equals(edge.getEdge().getClassName()))
                        && (lastAddedObj != null && lastAddedObj.getClassName().equals(targetNode.getBussinesObject().getClassName())))
                    direction = 0;
                else
                    direction = 1;
                
                //we must change the direction
                if(lastDirection == direction && direction == 1)          
                    direction = 0;
                
                if(direction == 0)
                    x += nodeDistanceX + edge.getEdge().toString().length() * 5;
                else if(direction == 1){
                    if(targetNode.getBussinesObject().toString().length() < lastAddedNameLength)
                        x += (targetNode.getBussinesObject().toString().length() * fixer) + 5;
                    else
                        x -= (targetNode.getBussinesObject().toString().length() * fixer) - 5;
                    
                    y += nodeDistanceY;
                }
                
                if(nodes.get(nodes.indexOf(targetNode)).getProperties().getProperty("x") == null)
                    nodes.get(nodes.indexOf(targetNode)).getProperties().setProperty("x", Integer.toString(x));
                if(nodes.get(nodes.indexOf(targetNode)).getProperties().getProperty("y") == null)
                    nodes.get(nodes.indexOf(targetNode)).getProperties().setProperty("y", Integer.toString(y));

                if(direction == 0)
                    x -= edge.getEdge().toString().length() * 5;
                
                lastAddedObj = targetNode.getBussinesObject();
                lastAddedRel = edge.getEdge();
                lastAddedNameLength = targetNode.getBussinesObject().toString().length();
            }
        }//end for
    }
}
