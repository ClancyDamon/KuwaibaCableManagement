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
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 * Shows in a box the ports in a device.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeviceNode extends MxBusinessObjectNode {
    private final int MARGIN = 10;
    private final int SPACING = 10;
    private final LinkedHashMap<String, String> styleCell = new LinkedHashMap();
    {
        styleCell.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        styleCell.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        styleCell.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
    }
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final MxGraph graph;
    private final Consumer<BusinessObjectLight> portCallback;
    
    private final List<BusinessObjectLight> physicalPorts;
    private final LinkedHashMap<BusinessObjectLight, List<BusinessObjectLight>> ports;
    
    public DeviceNode(BusinessObjectLight device, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts, 
        MxGraph graph, Consumer<BusinessObjectLight> portCallback
    ) throws InventoryException {
        
        super(device);
        Objects.requireNonNull(device);
        Objects.requireNonNull(aem);        
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(graph);
        graph.addNode(this);
        
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.graph = graph;
        this.portCallback = portCallback;
        
        physicalPorts = bem.getChildrenOfClassLightRecursive(
            device.getId(), device.getClassName(), 
            Constants.CLASS_GENERICPORT, null, -1, -1);
        // Sorting the physical ports to rise those start with, in/IN or back/BACK...
        Collections.sort(physicalPorts, Comparator.comparing(BusinessObjectLight::getName));
        
        ports = new LinkedHashMap();
        
        for (BusinessObjectLight physicalPort : physicalPorts) {
            if (!containsPort(physicalPort)) {
                
                HashMap<String, List<BusinessObjectLight>> specialAttributes = bem.getSpecialAttributes(
                    physicalPort.getClassName(), physicalPort.getId(), 
                    OspConstants.SPECIAL_REL_MIRROR, OspConstants.SPECIAL_REL_MIRROR_MULTIPLE
                );                

                if (specialAttributes.containsKey(OspConstants.SPECIAL_REL_MIRROR)) {
                    List<BusinessObjectLight> mirrors = specialAttributes.get(OspConstants.SPECIAL_REL_MIRROR);
                    Collections.sort(mirrors, Comparator.comparing(BusinessObjectLight::getName));
                    if (mirrors.size() == 1)
                        ports.put(physicalPort, mirrors);
                    else
                        throw new InventoryException("");

                } else if (specialAttributes.containsKey(OspConstants.SPECIAL_REL_MIRROR_MULTIPLE)) {
                    List<BusinessObjectLight> mirrors = specialAttributes.get(OspConstants.SPECIAL_REL_MIRROR_MULTIPLE);
                    Collections.sort(mirrors, Comparator.comparing(BusinessObjectLight::getName));
                    if (mirrors.size() > 1)
                        ports.put(physicalPort, mirrors);
                } else {
                    ports.put(physicalPort, null);
                }
            }
        }
        int rows = ports.keySet().size();
        int columns = 1;
        for (BusinessObjectLight port : ports.keySet()) {
            if (ports.get(port) != null) {
                columns = 2;
                break;
            }
        }
        setLabel(ts.getTranslatedString("module.ospman.mid-span-access.manage-ports"));
        
        setGeometry(0, 0, 
            MARGIN * 2 + (columns > 1 ? columns * PortNode.WIDTH + (columns - 1) * SPACING : PortNode.WIDTH), 
            MARGIN * 2 + (rows > 1 ? rows * PortNode.HEIGHT + (rows - 1) * SPACING : PortNode.HEIGHT)
        );
        ClassMetadata deviceClass = mem.getClass(device.getClassName());
        
        LinkedHashMap<String, String> style = new LinkedHashMap();
        style.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        style.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
        style.put(MxConstants.STYLE_FILLCOLOR, UtilHtml.toHexString(new Color(deviceClass.getColor())));
                
        setRawStyle(style);
        addCellAddedListener(event -> {
            event.unregisterListener();
            graph.setCellsLocked(false);
            setIsSelectable(false);
            setConnectable(false);
            overrideStyle();
            graph.setCellsLocked(true);
        });
        for (BusinessObjectLight physicalPort : ports.keySet())
            addNode(physicalPort);
    }
    
    public List<BusinessObjectLight> getPorts() {
        return physicalPorts;
    }
    
    private void addNode(BusinessObjectLight physicalPort) throws InventoryException {
        List<BusinessObjectLight> mirrors = ports.get(physicalPort);
        if (mirrors == null) {
            PortNode objectNode = new PortNode(physicalPort, graph, aem, bem, mem, ts);
            objectNode.addRightClickCellListener(event -> {
                if (portCallback != null)
                    portCallback.accept(physicalPort);
            });
            objectNode.setCellParent(this.getUuid());
            
            if (lastPort(physicalPort)) {
                objectNode.addCellAddedListener(event -> {
                    event.unregisterListener();
                    graph.setCellsLocked(false);
                    graph.executeStackLayout(this.getUuid(), false, SPACING, MARGIN);
                    graph.setCellsLocked(true);
                });
            }
            
        } else if (mirrors.size() == 1)
            addMirrorNode(physicalPort, mirrors.get(0));
        else if (mirrors.size() > 1)
            addMirrorMultipleNode(physicalPort, mirrors);
        else
            throw new InventoryException("");
    }
    
    private void addMirrorNode(BusinessObjectLight physicalPort, BusinessObjectLight mirror) {
        MxGraphNode rowNode = new MxGraphNode();
        rowNode.setCellParent(this.getUuid());
        rowNode.setRawStyle(styleCell);
        rowNode.addCellAddedListener(event -> {
            event.unregisterListener();
            graph.setCellsLocked(false);
            rowNode.setIsSelectable(false);
            rowNode.setConnectable(false);
            rowNode.overrideStyle();
            graph.setCellsLocked(true);
        });
        graph.addNode(rowNode);
        
        PortNode portNode = new PortNode(physicalPort, graph, aem, bem, mem, ts);
        portNode.addRightClickCellListener(event -> {
            if (portCallback != null)
                portCallback.accept(physicalPort);
        });
        portNode.setCellParent(rowNode.getUuid());
        
        PortNode mirrorNode = new PortNode(mirror, graph, aem, bem, mem, ts);
        mirrorNode.addRightClickCellListener(event -> {
            if (portCallback != null)
                portCallback.accept(mirror);
        });
        mirrorNode.setCellParent(rowNode.getUuid());
        
        mirrorNode.addCellAddedListener(event -> {
            event.unregisterListener();
            graph.setCellsLocked(false);
            graph.executeStackLayout(rowNode.getUuid(), true, SPACING, 0);
            graph.setCellsLocked(true);
        });
        
        if (lastPort(mirror)) {
            mirrorNode.addCellAddedListener(event -> {
                event.unregisterListener();
                graph.setCellsLocked(false);
                graph.executeStackLayout(this.getUuid(), false, SPACING, MARGIN);
                graph.setCellsLocked(true);
            });
        }
    }
    
    private void addMirrorMultipleNode(BusinessObjectLight physicalPort, List<BusinessObjectLight> mirrors) {
        MxGraphNode rowNode = new MxGraphNode();
        rowNode.setCellParent(this.getUuid());
        rowNode.setRawStyle(styleCell);
        rowNode.addCellAddedListener(event -> {
            event.unregisterListener();
            graph.setCellsLocked(false);
            rowNode.setIsSelectable(false);
            rowNode.setConnectable(false);
            rowNode.overrideStyle();
            graph.setCellsLocked(true);
        });
        graph.addNode(rowNode);
        
        MxGraphNode portWrapNode = new MxGraphNode();
        portWrapNode.setCellParent(rowNode.getUuid());
        portWrapNode.setRawStyle(styleCell);
        portWrapNode.addCellAddedListener(event -> {
            event.unregisterListener();
            graph.setCellsLocked(false);
            portWrapNode.setIsSelectable(false);
            portWrapNode.setConnectable(false);
            portWrapNode.overrideStyle();
            graph.setCellsLocked(true);
        });
        graph.addNode(portWrapNode);
        
        PortNode portNode = new PortNode(physicalPort, graph, aem, bem, mem, ts);
        portNode.addRightClickCellListener(event -> {
            if (portCallback != null)
                portCallback.accept(physicalPort);
        });
        portNode.setCellParent(portWrapNode.getUuid());
        
        MxGraphNode mirrorsNode = new MxGraphNode();
        mirrorsNode.setCellParent(rowNode.getUuid());
        mirrorsNode.setRawStyle(styleCell);
        mirrorsNode.addCellAddedListener(event -> {
            event.unregisterListener();
            graph.setCellsLocked(false);
            mirrorsNode.setIsSelectable(false);
            mirrorsNode.setConnectable(false);
            mirrorsNode.overrideStyle();
            graph.setCellsLocked(true);
        });
        graph.addNode(mirrorsNode);
        
        mirrors.forEach(mirror -> {
            PortNode mirrorNode = new PortNode(mirror, graph, aem, bem, mem, ts);
            mirrorNode.addRightClickCellListener(event -> {
                if (portCallback != null)
                    portCallback.accept(mirror);
            });
            mirrorNode.setCellParent(mirrorsNode.getUuid());
            
            if (mirrors.indexOf(mirror) == mirrors.size() - 1) {
                mirrorNode.addCellAddedListener(event -> {
                    event.unregisterListener();
                    graph.setCellsLocked(false);
                    int margin = mirrors.size() > 1 ? (mirrors.size() * PortNode.HEIGHT + (mirrors.size() - 1) * SPACING - PortNode.HEIGHT) / 2 : 0;
                    graph.executeStackLayout(portWrapNode.getUuid(), false, 0, margin, 0, margin, 0);
                    graph.executeStackLayout(mirrorsNode.getUuid(), false, SPACING, 0);
                    graph.executeStackLayout(rowNode.getUuid(), true, SPACING, 0);
                    graph.setCellsLocked(true);
                });
            }
            if (lastPort(mirror)) {
                mirrorNode.addCellAddedListener(event -> {
                    event.unregisterListener();
                    graph.setCellsLocked(false);
                    graph.executeStackLayout(this.getUuid(), false, SPACING, MARGIN);
                    graph.setCellsLocked(true);
                });
            }
        });
    }
    
    private boolean containsPort(BusinessObjectLight port) {
        if (ports.containsKey(port))
            return true;
        List<BusinessObjectLight> values = new ArrayList();
        ports.values().forEach(value -> {
            if (value != null)
                values.addAll(value);
        });
        return values.contains(port);
    }
    
    private boolean lastPort(BusinessObjectLight port) {
        BusinessObjectLight[] keys = ports.keySet().toArray(new BusinessObjectLight[0]);
        
        BusinessObjectLight lastPort = keys[keys.length - 1];
        
        if (ports.get(lastPort) == null && port.equals(lastPort))
            return true;
        List<BusinessObjectLight> values = new ArrayList();
        
        ports.values().forEach(value -> {
            if (value != null)
                values.addAll(value);
        });
        return !values.isEmpty() && port.equals(values.get(values.size() - 1));
    }
}
