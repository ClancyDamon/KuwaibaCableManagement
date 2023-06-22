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

import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * View for graphic visualization of physical Path
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class PhysicalPathView extends AbstractView<VerticalLayout> {
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
     * Width for the mxgraph component
     */
    private String componentWidth = "100%";
    /**
     * Height for the mxgraph component
     */
    private String componentHeight = "100%";
    /**
     * The source port.
     */
    private BusinessObjectLight businessObject;
    
    private LinkedHashMap<BusinessObjectLight, BusinessObjectLight> mapPathCircuit;

    public String getComponentWidth() {
        return componentWidth;
    }

    public void setComponentWidth(String componentWidth) {
        this.componentWidth = componentWidth;
    }

    public String getComponentHeight() {
        return componentHeight;
    }

    public void setComponentHeight(String componentHeight) {
        this.componentHeight = componentHeight;
    }

    public LinkedHashMap<BusinessObjectLight, BusinessObjectLight> getMapPathCircuit() {
        return mapPathCircuit;
    }

    public void setMapPathCircuit(LinkedHashMap<BusinessObjectLight, BusinessObjectLight> mapPathCircuit) {
        this.mapPathCircuit = mapPathCircuit;
    }
    
    public PhysicalPathView(BusinessObjectLight businessObject, BusinessEntityManager bem, 
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
    public VerticalLayout getAsUiElement() throws InvalidArgumentException {
        if (businessObject != null) {
            int widthPort = 60, heightPort = 50, startY = 100;
            VerticalLayout lytGraph = new VerticalLayout();
            lytGraph.setSizeFull();
            lytGraph.setPadding(false);
            mxGraph = new MxGraphCanvas(componentWidth, componentHeight);
            mxGraph.getMxGraph().setHasOutline(true);
            mxGraph.getMxGraph().setOutlineHeight("60px");
            mxGraph.getMxGraph().setCustomOutlinePosition("position:absolute;");
            mxGraph.getMxGraph().setOverflow("scroll");
            mxGraph.getMxGraph().setIsCellEditable(false);
            mxGraph.getMxGraph().setIsCellResizable(false);
            mxGraph.getMxGraph().enablePanning(true);
            lytGraph.add(mxGraph.getMxGraph());
            lytGraph.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, lytGraph);
            
            MxGraphNode mainBox = new MxGraphNode();
            mainBox.setUuid("main");
            mainBox.setFillColor("none");
            mainBox.setGeometry(0, startY, 900, 100);
            mainBox.setStrokeColor(Color.white.toString());
            mxGraph.addNode(new BusinessObjectLight("", "main", ""), mainBox);
            try {
                MxGraphNode lastPortMxNode = null;
                MxGraphEdge lastConnectionMxEdge = null;
                mapPathCircuit = new LinkedHashMap<>();
                LinkedHashMap<BusinessObjectLight, MxGraphNode> portNodesToAdd = new LinkedHashMap<>() ; // aux list due to the nodes must be added in reverse order
                ListOrderedMap parentNodesToAdd = new ListOrderedMap() ; // aux list due to the nodes must be added in reverse order
                LinkedHashMap<BusinessObjectLight, MxGraphEdge> edgesToAdd = new LinkedHashMap<>() ; 
                List<BusinessObjectLight> physicalPath = physicalConnectionsService.getPhysicalPath(businessObject.getClassName(), businessObject.getId());
                if (physicalPath.isEmpty())
                    return new VerticalLayout(new Label(ts.getTranslatedString("module.physcon.view.physical-path.empty")));
                
                for (BusinessObjectLight element : physicalPath) {
                    if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, element.getClassName())) { //It's a port
                        List<BusinessObjectLight> ancestors = bem.getParents(element.getClassName(), element.getId());
                        if (!portNodesToAdd.containsKey(element)) {//we should search if the physical parent port its already in the scene
                            ClassMetadata theClass = mem.getClass(element.getClassName());
                            String hexColor = String.format("#%06x", (0xFFFFFF & theClass.getColor())); 
                            lastPortMxNode = new MxGraphNode();
                            lastPortMxNode.setUuid(element.getId());
                            lastPortMxNode.setFillColor(hexColor);
                            lastPortMxNode.setLabel(FormattedObjectDisplayNameSpan.getFormattedDisplayName(element, true));                          
                            lastPortMxNode.setGeometry(0, 0, widthPort, heightPort);
//                            lastPortMxNode.setCellParent("gp" + i);
                            portNodesToAdd.put(element, lastPortMxNode);
                            if (lastConnectionMxEdge != null)
                                lastConnectionMxEdge.setTarget(lastPortMxNode.getUuid());
                            
                            lastConnectionMxEdge = null;
                            MxGraphNode lastWidget = lastPortMxNode;
                            BusinessObjectLight deviceParent = null;
                            for (int i = 0; i < ancestors.size() - 1; i++) { //We ignore the dummy root
                                if (mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, ancestors.get(i).getClassName())
                                        || mem.isSubclassOf(Constants.CLASS_GENERICDISTRIBUTIONFRAME, ancestors.get(i).getClassName())) { 
                                        deviceParent = ancestors.get(i);
                                }
                                if (!parentNodesToAdd.containsKey(ancestors.get(i))) {
                                    theClass = mem.getClass(ancestors.get(i).getClassName());
                                    hexColor = String.format("#%06x", (0xFFFFFF & theClass.getColor())); 
                                    MxGraphNode newParent = new MxGraphNode();
                                    newParent.setUuid(ancestors.get(i).getId());
                                    newParent.setLabel(ancestors.get(i).toString());
                                    newParent.setGeometry(0, 0, widthPort*(i+2), heightPort*(i+2));
                                    newParent.setFillColor(hexColor);
                                    lastWidget.setCellParent(ancestors.get(i).getId());                                   
                                    lastWidget = newParent;
                                    parentNodesToAdd.put(i, ancestors.get(i), lastWidget);
                                    if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALNODE, ancestors.get(i).getClassName())
                                        || //Only parents up to the first physical node (say a building) will be displayed
                                        i == ancestors.size() - 2) { //Or if the next level is the dummy root
                                        lastWidget.setCellParent("main");                                   
                                        break;
                                    }
                                    
                                } else {
                                    lastWidget.setCellParent(ancestors.get(i).getId());
                                    break;
                                }
                            }
                            mapPathCircuit.put(element, deviceParent);
                        }
                    } else {
                        if (lastPortMxNode != null) { // if the source port is added
                            lastConnectionMxEdge = new MxGraphEdge();
                            lastConnectionMxEdge.setSource(lastPortMxNode.getUuid());                        
                            lastConnectionMxEdge.setStrokeWidth(1);
                            lastConnectionMxEdge.setLabel(element.toString());  
                            edgesToAdd.put(element, lastConnectionMxEdge);
                            lastPortMxNode = null;
                            mapPathCircuit.put(element, null);
                        }
                    }
                }
                
                List<BusinessObjectLight> reverseOrderedKeys = new ArrayList<>(parentNodesToAdd.keySet());
                Collections.reverse(reverseOrderedKeys);
                for (int i = 0; i<reverseOrderedKeys.size(); i++) { 
                    BusinessObjectLight key = reverseOrderedKeys.get(i);            
                    if (i == (reverseOrderedKeys.size() - 1)) { // when the last cell is added, then execute the layouts
                        ((MxGraphNode) parentNodesToAdd.get(key)).addCellAddedListener(eventListener -> {
                          List<BusinessObjectLight> parentNodesKeys = new ArrayList<>(parentNodesToAdd.keySet());
                          for (BusinessObjectLight node : parentNodesKeys) {  
                                 if (portNodesToAdd.values().stream().filter(item -> node.getId().equals(item.getCellParent())).findAny().isPresent())
                                    mxGraph.getMxGraph().executeStackLayout(node.getId(), true, 50, 20, true);
                                 else
                                    mxGraph.getMxGraph().executeStackLayout(node.getId(), true, 150, 20, true);
                            }
                            mxGraph.getMxGraph().executeStackLayout("main", true, 170, 20, true); 
                              // when the cells are in the right place, then disable movement for inner cells
                          for (MxGraphNode node : portNodesToAdd.values()) {
                               node.setMovable(false);
//                               node.setIsSelectable(false);
                          }
                          List<MxGraphNode> parentNodesValue = new ArrayList<>(parentNodesToAdd.values());
                          for (MxGraphNode node : parentNodesValue) {  
                              if (!mainBox.getUuid().equals(node.getCellParent())) { // only let movable main nodes
                                  node.setMovable(false);
//                                  node.setIsSelectable(false);
                              }
                          }
                          mainBox.setMovable(false);
//                          mainBox.setIsSelectable(false);
                        });
                    }
                    mxGraph.addNode(key, (MxGraphNode) parentNodesToAdd.get(key));
                }
                for (BusinessObjectLight key : portNodesToAdd.keySet()) 
                    mxGraph.addNode(key, portNodesToAdd.get(key)); 
                
                for (BusinessObjectLight key : edgesToAdd.keySet()) 
                    mxGraph.addEdge(key, edgesToAdd.get(key)); 
                
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | IllegalStateException | ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                Logger.getLogger(PhysicalPathView.class.getName()).log(Level.SEVERE, null, ex);
            }
            return lytGraph;
        }
        return new VerticalLayout(new Label(ts.getTranslatedString("module.visualization.view.no-business-object-associated")));
 }

    @Override
    public void buildFromSavedView(byte[] view) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    @Override
    public void clean() { }
}
