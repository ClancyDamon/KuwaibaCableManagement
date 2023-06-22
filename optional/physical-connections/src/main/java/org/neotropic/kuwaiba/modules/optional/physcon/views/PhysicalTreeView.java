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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.collections.map.ListOrderedMap;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractImageExporter;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractView;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.mxgraph.MxGraphCanvas;

/**
 * View for graphic visualization of physical tree. Physical trees are used in the context of FTTx networks. In 
 * these scenarios, the connection from the exchange office branches out through splice boxes, 
 * fiber splitters and other equipment till it reaches the subscribers, forming a tree. Note that the 
 * backwards path (from the subscriber to the XO) is linear.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class PhysicalTreeView extends AbstractView<HorizontalLayout>  {
     /**
     * Reference to the main canvas of the view
     */
    private MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> mxGraph;
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
     * Reference to the Physical Connection Service
     */
    private PhysicalConnectionsService physicalConnectionsService;
    /**
     * The source port.
     */
    private BusinessObjectLight businessObject;
    
    public PhysicalTreeView(BusinessObjectLight businessObject, BusinessEntityManager bem, 
            ApplicationEntityManager aem, MetadataEntityManager mem, TranslationService ts, 
            PhysicalConnectionsService physicalConnectionsService) {
        this.businessObject = businessObject;
        this.bem = bem;  
        this.aem = aem;
        this.mem = mem;
        this.ts = ts;
        this.physicalConnectionsService = physicalConnectionsService;
    }

    @Override
    public byte[] getAsXml() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getAsImage(AbstractImageExporter exporter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HorizontalLayout getAsUiElement() throws InvalidArgumentException {
        if (businessObject != null) {
            Button btnZoomIn = new Button(new Icon(VaadinIcon.PLUS), evt -> {
                    mxGraph.getMxGraph().zoomIn();
                });
            btnZoomIn.getElement().setProperty("title", ts.getTranslatedString("module.visualization.rack-view-zoom-in"));
                
            Button btnZoomOut = new Button(new Icon(VaadinIcon.MINUS), evt -> {
                    mxGraph.getMxGraph().zoomOut();
                });
            btnZoomOut.getElement().setProperty("title", ts.getTranslatedString("module.visualization.rack-view-zoom-out"));
             
            int widthPort = 25, heightPort = 25, startY = 100;
            HorizontalLayout lytGraph = new HorizontalLayout();
            lytGraph.setHeightFull();
            lytGraph.setSpacing(false);
            lytGraph.setMargin(false);
            mxGraph = new MxGraphCanvas("100%", "100%");
            mxGraph.getMxGraph().setOverflow("scroll");
            mxGraph.getMxGraph().setHasOutline(true);
            mxGraph.getMxGraph().setOutlineHeight("100px");
            mxGraph.getMxGraph().setCustomOutlinePosition("position:absolute;");
            mxGraph.getMxGraph().setIsCellEditable(false);
            mxGraph.getMxGraph().setIsCellResizable(false);
            mxGraph.getMxGraph().enablePanning(true);
            
            VerticalLayout lytActions = new VerticalLayout(btnZoomIn, btnZoomOut);
            lytActions.setPadding(false);
            lytActions.setWidth("30px");
//            lytGraph.add(lytActions);
            lytGraph.setWidth("70vw");
            lytGraph.add(mxGraph.getMxGraph());
            MxGraphNode mainBox = new MxGraphNode();
            mainBox.setUuid("main");
            mainBox.setFillColor("none");
            mainBox.setGeometry(0, startY, 900, 100);
            mainBox.setStrokeColor(Color.white.toString());
            mxGraph.addNode(new BusinessObjectLight("", "main", ""), mainBox);
            try {
                MxGraphNode lastPortMxNode = null;
                MxGraphEdge lastConnectionMxEdge = null;
                Entry<BusinessObjectLight, List<BusinessObjectLight>> lastConnectionEntry = null;
                LinkedHashMap<BusinessObjectLight, MxGraphNode> portNodesToAdd = new LinkedHashMap<>() ; 
                ListOrderedMap parentNodesToAdd = new ListOrderedMap() ; // aux list due to the nodes must be added in reverse order
                LinkedHashMap<BusinessObjectLight, MxGraphEdge> edgesToAdd = new LinkedHashMap<>() ; 
                HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalTree = physicalConnectionsService.getPhysicalTree(businessObject.getClassName(), businessObject.getId());
                if (physicalTree.isEmpty())
                    return new HorizontalLayout(new Label(ts.getTranslatedString("module.physcon.view.physical-tree.empty")));
                
                for (Entry<BusinessObjectLight, List<BusinessObjectLight>> entryTree : physicalTree.entrySet()) {
                    if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, entryTree.getKey().getClassName())) { //It's a port
                        List<BusinessObjectLight> ancestors = bem.getParents(entryTree.getKey().getClassName(), entryTree.getKey().getId());
                        if (!portNodesToAdd.containsKey(entryTree.getKey())) {//we should search if the physical parent port its already in the scene
                            
                            ClassMetadata theClass = mem.getClass(entryTree.getKey().getClassName());
                            String hexColor = String.format("#%06x", (0xFFFFFF & theClass.getColor())); 
                            lastPortMxNode = new MxGraphNode();
                            lastPortMxNode.setUuid(entryTree.getKey().getId());
                            lastPortMxNode.setFillColor(hexColor);
                            lastPortMxNode.setShape(MxConstants.SHAPE_ELLIPSE);
                            lastPortMxNode.setLabel(FormattedObjectDisplayNameSpan.getFormattedDisplayName(entryTree.getKey(), true));
                            lastPortMxNode.setGeometry(0, 0, widthPort, heightPort);
                            portNodesToAdd.put(entryTree.getKey(), lastPortMxNode);
                            if (lastConnectionMxEdge != null && lastConnectionEntry != null) {
                                if (lastConnectionEntry.getValue().stream().filter(obj -> obj.equals(entryTree.getKey())).findAny().isPresent())
                                    lastConnectionMxEdge.setTarget(lastPortMxNode.getUuid());   // if the port is the other endpoint of the last edge
                            }
                            if (entryTree.getValue() != null && entryTree.getValue().size() > 0 && 
                                    !mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, entryTree.getValue().get(0).getClassName())) { //It's a port
                                for (BusinessObjectLight portMirror : entryTree.getValue()) {
                                    MxGraphEdge mirrorMxEdge = new MxGraphEdge();
                                    mirrorMxEdge.setSource(entryTree.getKey().getId());                        
                                    mirrorMxEdge.setTarget(portMirror.getId());                        
                                    mirrorMxEdge.setStrokeWidth(1);
                                    mirrorMxEdge.setIsDashed(Boolean.TRUE); 
                                    edgesToAdd.put(new BusinessObjectLight("", mirrorMxEdge.getUuid(), ""), mirrorMxEdge);
                                }
                            }
                            lastConnectionMxEdge = null;
                            lastConnectionEntry = null;
                            MxGraphNode lastWidget = lastPortMxNode;

                            for (int i = 0; i < ancestors.size() - 1; i++) { //We ignore the dummy root
                                if (!parentNodesToAdd.containsKey(ancestors.get(i))) {
                                    theClass = mem.getClass(ancestors.get(i).getClassName());
                                    hexColor = String.format("#%06x", (0xFFFFFF & theClass.getColor())); 
                                    MxGraphNode newParent = new MxGraphNode();
                                    newParent.setUuid(ancestors.get(i).getId());
                                    newParent.setLabel(ancestors.get(i).toString());
                                    newParent.setGeometry(0, 0, widthPort*(i+2), heightPort*(i+2));
                                    newParent.setFillColor(hexColor);
                                    // Validation to put the in nodes in the left side in all devices
                                    lastWidget.setCellParent(ancestors.get(i).getId());    
                                    lastWidget = newParent;
                                    parentNodesToAdd.put(i, ancestors.get(i), lastWidget);
                                    // Commented segment because at the moment due to the change in the structure 
//                                    of the view, the parents will no longer be traversed until looking for an object of type
//                                    if ( mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALNODE, ancestors.get(i).getClassName())
//                                        || //Only parents up to the first physical node (say a building) will be displayed
//                                        i == ancestors.size() - 2) { //Or if the next level is the dummy root
//                                    lastWidget.setCellParent("main"); 
                                    break;
//                                    }
                                    
                                } else {
                                    lastWidget.setCellParent(ancestors.get(i).getId());
                                    break;
                                }
                            }
                        }
                    } else {
                        if (lastPortMxNode != null) { // if the source port is added
                            lastConnectionMxEdge = new MxGraphEdge();
                            lastConnectionMxEdge.setSource(lastPortMxNode.getUuid());                        
                            lastConnectionMxEdge.setStrokeWidth(1);
                            lastConnectionMxEdge.setEdgeStyle(MxConstants.EDGESTYLE_SIDETOSIDE);
                            lastConnectionMxEdge.setLabel(entryTree.getKey().toString());  
                            edgesToAdd.put(entryTree.getKey(), lastConnectionMxEdge);
                            lastConnectionEntry = entryTree;
                            lastPortMxNode = null;
                        }
                    }
                }
                ListOrderedMap mapContainerNodes = new ListOrderedMap(); // Used to align main cells in the middle
                List<BusinessObjectLight> reverseOrderedKeys = new ArrayList<>(parentNodesToAdd.keySet());
                Collections.reverse(reverseOrderedKeys);
                MxGraphNode nodeContainer, lastNodeContainer = null;
                ArrayList<MxGraphNode> lstContainerNodes = new ArrayList<>();
                for (int i = 0; i < reverseOrderedKeys.size(); i++) { 
                   
                    BusinessObjectLight key = reverseOrderedKeys.get(i);   
                    MxGraphNode node = (MxGraphNode) parentNodesToAdd.get(key);
                    
                    nodeContainer = new MxGraphNode();
                    nodeContainer.setUuid("container"+ (mapContainerNodes.size() + 1));
                    nodeContainer.setFillColor(MxConstants.NONE);
                    nodeContainer.setStrokeColor(MxConstants.NONE);
                    nodeContainer.setCellParent("main"); 
                    nodeContainer.setGeometry(0, 0, widthPort, heightPort);
                    node.setCellParent(nodeContainer.getUuid());
                    
                    if (mapContainerNodes.size() < 2) {  // First Two devices                  
                        lstContainerNodes.add(node);
                        mapContainerNodes.put(nodeContainer, lstContainerNodes);
                        mxGraph.addNode(new BusinessObjectLight("", nodeContainer.getUuid(), ""), nodeContainer);
                        lastNodeContainer = nodeContainer;
                        lstContainerNodes = new ArrayList<>();
                    } else if (lstContainerNodes.isEmpty()) {
                            lstContainerNodes.add(node);
                            mapContainerNodes.put(nodeContainer, lstContainerNodes);
                            mxGraph.addNode(new BusinessObjectLight("", nodeContainer.getUuid(), ""), nodeContainer);               
                            lastNodeContainer = nodeContainer;
                    } else {
                        boolean hasConnection = false; // Boolean to know if the current node (device) must be in a new container
                                                       // (if has not any connection with the previous container) or
                                                       // the node must be in the current container (has any connection )
                        List<MxGraphNode> nodePorts = portNodesToAdd.values().stream().filter(item -> item.getCellParent().equals(node.getUuid())).collect(Collectors.toList()); 
                        for (MxGraphNode cn : (ArrayList<MxGraphNode>) mapContainerNodes.getValue(mapContainerNodes.size() - 2)) {
                             List<MxGraphNode> containerNodePorts = portNodesToAdd.values().stream().filter(item -> item.getCellParent().equals(cn.getUuid())).collect(Collectors.toList());                        
                             for (MxGraphNode cnp: containerNodePorts) {
                                 List<BusinessObjectLight> lstLinks = physicalTree.get(new BusinessObjectLight("", cnp.getUuid(), ""));
                                 if (lstLinks != null && lstLinks.size() == 1) { // if the port has a connection 
                                     List<BusinessObjectLight> lstOtherPort = physicalTree.get(new BusinessObjectLight("", lstLinks.get(0).getId(), ""));
                                     if (lstOtherPort != null && !lstOtherPort.isEmpty()) {
                                       if(nodePorts.stream().filter(item -> lstOtherPort.get(0).getId().equals(item.getUuid())).findAny().isPresent())
                                           hasConnection = true;
                                 }
                             }
                           }
                        }
                      if (hasConnection) {
                          node.setCellParent(lastNodeContainer.getUuid());
                          lstContainerNodes.add(node);
                      } else {
                          lstContainerNodes = new ArrayList<>();
                          lstContainerNodes.add(node);
                          mapContainerNodes.put(nodeContainer, lstContainerNodes);
                          mxGraph.addNode(new BusinessObjectLight("", nodeContainer.getUuid(), ""), nodeContainer);               
                          lastNodeContainer = nodeContainer;
                      }                     
                  }
                mxGraph.addNode(key, node);
                // Now add the nodes to organize the device content
                MxGraphNode nodeContent = new MxGraphNode(); // Node used to put the device content
                nodeContent.setUuid("leftContent-"+ node.getUuid());
                nodeContent.setFillColor(MxConstants.NONE);
                nodeContent.setStrokeColor(MxConstants.NONE);
                nodeContent.setCellParent(node.getUuid()); 
                nodeContent.setGeometry(0, 0, 1, 1);
                
                mxGraph.addNode(new BusinessObjectLight("", nodeContent.getUuid(), ""), nodeContent);
                
                nodeContent = new MxGraphNode();
                nodeContent.setUuid("rightContent-"+ node.getUuid());
                nodeContent.setFillColor(MxConstants.NONE);
                nodeContent.setStrokeColor(MxConstants.NONE);
                nodeContent.setCellParent(node.getUuid()); 
                nodeContent.setGeometry(0, 0, 1, 1);
                mxGraph.addNode(new BusinessObjectLight("", nodeContent.getUuid(), ""), nodeContent);
                
                }
                for (BusinessObjectLight key : portNodesToAdd.keySet()) {
                    MxGraphNode node = portNodesToAdd.get(key);
                    if (node.getLabel() != null && node.getLabel().toLowerCase().startsWith("in")) {
                        node.setCellParent("leftContent-" + node.getCellParent());  
                    } else {
                        node.setCellParent("rightContent-" + node.getCellParent());    
                    } 
                    mxGraph.addNode(key, node);
                }
                
                for (BusinessObjectLight key : edgesToAdd.keySet()) 
                    mxGraph.addEdge(key, edgesToAdd.get(key)); 
                
                MxGraphNode dummyNode = new MxGraphNode();
                dummyNode.setUuid("dummyNode");
                dummyNode.setGeometry(0, 0, 0, 0);
                mxGraph.addNode(new BusinessObjectLight("", "dummyNode", ""), dummyNode);
                //  execute the layout and disable moving when the last cell is added
                dummyNode.addCellAddedListener(eventListener -> {
                    List<BusinessObjectLight> parentNodesKeys = new ArrayList<>(parentNodesToAdd.keySet());
                    int spacing;
                    for (BusinessObjectLight node : parentNodesKeys) {
                        if (portNodesToAdd.values().stream().filter(item -> 
                                ("leftContent-" + node.getId()).equals(item.getCellParent())).findAny().isPresent()) 
                           spacing = 50;
                        else 
                           spacing = 0;
                        
                        mxGraph.getMxGraph().executeStackLayout("leftContent-" + node.getId(), false, 20, 20, true);
                        mxGraph.getMxGraph().executeStackLayout("rightContent-" + node.getId(), false, 20, 20, true);
                        mxGraph.getMxGraph().executeStackLayout(node.getId(), true, spacing, 20, true);
                        mxGraph.getMxGraph().alignCells(MxConstants.ALIGN_MIDDLE, new String[] {"leftContent-" + node.getId(), "rightContent-" + node.getId()});
                   
                    }
                    for (MxGraphNode node : (List<MxGraphNode>) mapContainerNodes.keyList())
                        mxGraph.getMxGraph().executeStackLayout(node.getUuid(), false, 20, 20, true);
                    mxGraph.getMxGraph().executeStackLayout("main", true, 150, 20, true);
                    mxGraph.getMxGraph().alignCells(MxConstants.ALIGN_MIDDLE, ((List<MxGraphNode>) mapContainerNodes.keyList()).stream().map(item -> item.getUuid()).collect(Collectors.toList()).toArray(new String[0]));
//                     when the cells are in the right place, then disable movement for inner cells
                    for (MxGraphNode node : portNodesToAdd.values()) {
                        node.setMovable(false);
                    }
                    List<MxGraphNode> parentNodesValue = new ArrayList<>(parentNodesToAdd.values());
                    for (MxGraphNode node : parentNodesValue) {
                        if (!mainBox.getUuid().equals(node.getCellParent())) // only let movable main nodes
                            node.setMovable(false);                       
                    }
                    mainBox.setMovable(false);                  
                });
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | IllegalStateException | ApplicationObjectNotFoundException ex) {
                Logger.getLogger(PhysicalTreeView.class.getName()).log(Level.SEVERE, null, ex);
            }
            return lytGraph;
        }
        return new HorizontalLayout(new Label(ts.getTranslatedString("module.visualization.view.no-business-object-associated")));
    }

    @Override
    public void buildFromSavedView(byte[] view) { }

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

    @Override
    public void clean() { }
}
