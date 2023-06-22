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
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractImageExporter;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractView;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * View for graphic visualization of fiber splitter equipment
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class FiberSplitterView extends AbstractView<VerticalLayout> {
    /**
     * Reference to the main canvas of the view
     */
    private MxGraph mxGraph;
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
     * 
     */
    private BusinessObjectLight businessObject;
    
    public FiberSplitterView(BusinessObjectLight businessObject, BusinessEntityManager bem, ApplicationEntityManager aem, 
            MetadataEntityManager mem, TranslationService ts) {
        this.businessObject = businessObject;
        this.bem = bem;  
        this.aem = aem;
        this.mem = mem;
        this.ts = ts;
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
            int widthPort = 50, heightPort = 40, widthExternalPort= 30, heightExternalPort=30, startY = 30, widthMain = 240, spacingGroup = 20;
            VerticalLayout lytGraph = new VerticalLayout();
            lytGraph.setSizeFull();
            mxGraph = new MxGraph();
            mxGraph.setWidth("100%");
            mxGraph.setHeight("100%");
            mxGraph.setGrid("");
            mxGraph.setOverflow("scroll");
            mxGraph.setIsCellEditable(false);
            mxGraph.setIsCellResizable(false);
            mxGraph.setIsCellMovable(false);
            lytGraph.add(mxGraph);
            lytGraph.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, mxGraph);
            MxGraphNode mainBox = new MxGraphNode();
            mainBox.setUuid("main");
            mainBox.setLabel(businessObject.getName());
            mainBox.setGeometry(220, startY, widthMain, 100);
            mxGraph.addNode(mainBox);
            try {
                List<BusinessObjectLight> lstOpticalPorts = bem.getChildrenOfClassLight(businessObject.getId(), businessObject.getClassName(), "OpticalPort", -1);
                lstOpticalPorts = lstOpticalPorts.stream().sorted((object1, object2) -> object1.getName().compareTo(object2.getName())).collect(Collectors.toList());
                BusinessObjectLight inPort = null;
                for (BusinessObjectLight port : lstOpticalPorts) {
                    if (port.getName().toLowerCase().startsWith("in")) {
                        inPort = port;       // take the first in port
                        break;
                    }
                }
                if (inPort == null) 
                    return new VerticalLayout(new Label(ts.getTranslatedString("module.visualization.fiber-splitter-view-no-input-port")));
                
                List<BusinessObjectLight> lstMirrors = bem.getSpecialAttribute(inPort.getClassName(), inPort.getId(), "mirrorMultiple");
                lstMirrors = lstMirrors.stream().sorted((object1, object2) -> object1.getName().compareTo(object2.getName())).collect(Collectors.toList());
                
                int i = 1;
                MxGraphNode groupOutPorts;
                MxGraphNode nodeIn;
                MxGraphNode nodeOut;
                MxGraphNode startIn;
                MxGraphNode endOut;
                MxGraphEdge edgeIn;
                MxGraphEdge edgeOut;
                MxGraphEdge edgeMirror;

                groupOutPorts = new MxGraphNode();
                groupOutPorts.setUuid("gout");
                groupOutPorts.setLabel("");
                groupOutPorts.setGeometry(widthMain - widthPort, 0, widthPort, heightPort * (lstMirrors.size()));
                groupOutPorts.setCellParent("main");
                mxGraph.addNode(groupOutPorts);

                nodeIn = new MxGraphNode();
                nodeIn.setUuid("in");
                nodeIn.setLabel(inPort.getName());
                nodeIn.setGeometry(0, (int) (((lstMirrors.size() / 2.0) * heightPort) - (heightPort / 2)), widthPort, heightPort);
                nodeIn.setCellParent("main");
                nodeIn.setVerticalLabelPosition(MxConstants.ALIGN_MIDDLE);
                mxGraph.addNode(nodeIn);
                List<BusinessObjectLight> inLinks = bem.getSpecialAttribute(inPort.getClassName(), inPort.getId(), "endpointA");
                if (inLinks == null || inLinks.isEmpty()) 
                    inLinks = bem.getSpecialAttribute(inPort.getClassName(), inPort.getId(), "endpointB");
                
                if (inLinks != null && inLinks.size() > 0) {
                    BusinessObject theWholeLink = bem.getObject(inLinks.get(0).getClassName(), inLinks.get(0).getId());
////                    String hexColor;
////                    if (theWholeLink.getAttributes().containsKey(Constants.PROPERTY_COLOR) && theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR) != null)
////                        hexColor =  (String) theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR);//String.format("#%06x", (0xFFFFFF & (int) theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR)));
////                    else
////                        hexColor = "steelblue";  //default color
                    String hexColor = null;
                    if (theWholeLink.getAttributes().containsKey(Constants.PROPERTY_COLOR)) {
                        hexColor = theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR) instanceof String ? (String) theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR) : null;
                        if (hexColor != null) {
                            ClassMetadata classInLink = mem.getClass(inLinks.get(0).getClassName());
                            String colorType = classInLink.getType(Constants.PROPERTY_COLOR);
                            if(mem.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, colorType)) {
                                BusinessObject colorObject = aem.getListTypeItem(colorType, hexColor);
                                hexColor = colorObject.getAttributes().containsKey("value") && colorObject.getAttributes().get("value") instanceof String ? (String) colorObject.getAttributes().get("value") : null; //NOI18N
                            }
                        }
                    }
                    if (hexColor == null)
                        hexColor = "steelblue";  //default color
                    
                    nodeIn.setFillColor(hexColor);
                    startIn = new MxGraphNode();
                    startIn.setUuid("s1");
                    startIn.setLabel("");
                    startIn.setGeometry(10, startY + (int) (((lstMirrors.size() / 2.0) * heightPort) - (heightPort / 2) + ((heightPort - heightExternalPort)/2)), widthExternalPort, heightExternalPort);
                    startIn.setFillColor("white");
                    startIn.setVerticalLabelPosition(MxConstants.ALIGN_MIDDLE);
                    edgeIn = new MxGraphEdge();
                    edgeIn.setSource("s1");
                    edgeIn.setTarget("in");
                    edgeIn.setStrokeWidth(1);
                    edgeIn.setLabel(inLinks.get(0).getName());
                    mxGraph.addNode(startIn);
                    mxGraph.addEdge(edgeIn);
                } else 
                    nodeIn.setFillColor("gray");
                

                for (BusinessObjectLight outPort : lstMirrors) {

                    nodeOut = new MxGraphNode();
                    nodeOut.setUuid("out" + i);
                    nodeOut.setGeometry(0, heightPort * (i - 1), widthPort, heightPort);
                    nodeOut.setCellParent("gout");
                    nodeOut.setVerticalLabelPosition(MxConstants.ALIGN_MIDDLE);
                    mxGraph.addNode(nodeOut);
                    edgeMirror = new MxGraphEdge();
                    edgeMirror.setSource("in");
                    edgeMirror.setTarget("out" + i);
                    edgeMirror.setStrokeWidth(1);
                    mxGraph.addEdge(edgeMirror);
                    if (outPort != null) {
                        nodeOut.setLabel(outPort.getName());
                        List<BusinessObjectLight> outLinks = bem.getSpecialAttribute(outPort.getClassName(), outPort.getId(), "endpointA");
                        if (outLinks == null || outLinks.isEmpty()) 
                            outLinks = bem.getSpecialAttribute(outPort.getClassName(), outPort.getId(), "endpointB");
                        
                        if (outLinks != null && outLinks.size() > 0) {
                            BusinessObject theWholeLink = bem.getObject(outLinks.get(0).getClassName(), outLinks.get(0).getId());
////                            String hexColor;
////                            if (theWholeLink.getAttributes().containsKey(Constants.PROPERTY_COLOR) && theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR) != null)
////                                hexColor =  (String) theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR);//String.format("#%06x", (0xFFFFFF & (int) theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR)));
////                            else
////                                hexColor = "steelblue";  //default color
                            String hexColor = null;
                            if (theWholeLink.getAttributes().containsKey(Constants.PROPERTY_COLOR)) {
                                hexColor = theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR) instanceof String ? (String) theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR) : null;
                                if (hexColor != null) {
                                    ClassMetadata classInLink = mem.getClass(outLinks.get(0).getClassName());
                                    String colorType = classInLink.getType(Constants.PROPERTY_COLOR);
                                    if(mem.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, colorType)) {
                                        BusinessObject colorObject = aem.getListTypeItem(colorType, hexColor);
                                        hexColor = colorObject.getAttributes().containsKey("value") && colorObject.getAttributes().get("value") instanceof String ? (String) colorObject.getAttributes().get("value") : null; //NOI18N
                                    }
                                }
                            }
                            if (hexColor == null)
                                hexColor = "steelblue";  //default color
                            
                            nodeOut.setFillColor(hexColor);
                            endOut = new MxGraphNode();
                            endOut.setUuid("e" + i);
                            endOut.setLabel("");
                            endOut.setGeometry(600, startY + (heightPort) * (i - 1) + ((heightPort - heightExternalPort)/2), widthExternalPort, heightExternalPort);
                            endOut.setFillColor("white");
                            nodeOut.setVerticalLabelPosition(MxConstants.ALIGN_MIDDLE);
                            edgeOut = new MxGraphEdge();
                            edgeOut.setSource("out" + i);
                            edgeOut.setTarget("e" + i);
                            edgeOut.setStrokeWidth(1);
                            edgeOut.setLabel(outLinks.get(0).getName());
                            mxGraph.addNode(endOut);
                            mxGraph.addEdge(edgeOut);
                        } else 
                            nodeOut.setFillColor("gray"); //doesn't have an endpoint
                        
                    } else // doesnt have a mirror port
                        nodeOut.setFillColor("black");
                    i++;
                }
            } catch (InventoryException ex) {
                Logger.getLogger(FiberSplitterView.class.getName()).log(Level.SEVERE, null, ex);
            }

            return lytGraph;
        }
        return new VerticalLayout(new Label(ts.getTranslatedString("module.visualization.view.no-business-object-associated")));
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
