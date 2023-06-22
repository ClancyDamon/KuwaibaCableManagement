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
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ManagePortMirroringVisualAction;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectEdge;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.BusinessObjectUtil;

/**
 * View to Outside Plant Locations (manholes, hand holes, etc.)
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OspLocationView extends MxGraph {
    /**
     * Reference to the Translation Service
     */
    private final TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    private final MetadataEntityManager mem;
    
    private final LinkedHashMap<String, String> EDGE_STYLE = new LinkedHashMap();
    {
        EDGE_STYLE.put(MxConstants.STYLE_STROKEWIDTH, String.valueOf(4));
        EDGE_STYLE.put(MxConstants.STYLE_ENDARROW, MxConstants.NONE);
        EDGE_STYLE.put(MxConstants.STYLE_STARTARROW, MxConstants.NONE);
        EDGE_STYLE.put(MxConstants.STYLE_EDGE, MxConstants.EDGESTYLE_ENTITY_RELATION);
    }
    private final BusinessObjectLight location;
    private final BiConsumer<List<BusinessObjectLight>, String> consumerReleaseFiber;
    
    private boolean spliceFiber = true;
    private boolean cutFiber = false;
    private boolean showLeftoverFiber = false;
    
    private TreeLayout treeLayout;
    private DeviceNode deviceNode;
    
    public OspLocationView(BusinessObjectLight location, BusinessObjectLight cable, BusinessObjectLight device,
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts,
        ManagePortMirroringVisualAction managePortMirroringVisualAction,
        PhysicalConnectionsService physicalConnectionsService, 
        boolean showLeftover, boolean exchange) throws InventoryException {
        
        Objects.requireNonNull(location);
        Objects.requireNonNull(cable);
        Objects.requireNonNull(device);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        this.location = location;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        
        setSizeFull();
        setOverrideCurrentStyle(true);
        setConnectable(true);
        setTooltips(true);
        setBeginUpdateOnInit(true);
        
        addGraphLoadedListener(event -> {
            event.unregisterListener();
            enablePanning(false);
        });
        consumerReleaseFiber = (portFiber, specialAttrName) -> {
            ConfirmDialog confirmDialog = new ConfirmDialog(ts, 
                ts.getTranslatedString("module.general.labels.confirmation"),
                new Label(ts.getTranslatedString("module.ospman.port-tools.tool.release-port.confirm")), 
                ts.getTranslatedString("module.general.messages.ok"), () -> {
                
                BusinessObjectLight port = portFiber.get(0);
                BusinessObjectLight fiber = portFiber.get(1);
                    
                MxBusinessObjectNode portNode = findNode(port);
                MxBusinessObjectEdge fiberEdge = findEdge(fiber);
                if (portNode instanceof PortNode && fiberEdge instanceof FiberEdge) {
                    
                    setCellsLocked(false);
                    removeEdge(fiberEdge);
                    ((PortNode) portNode).releasePort();
                    
                    MxBusinessObjectNode fiberNode = findNode(fiber);
                    try {
                        bem.releaseSpecialRelationship(
                            port.getClassName(), port.getId(),
                            fiber.getId(), specialAttrName);
                        if (fiberNode instanceof FiberNode)
                            ((FiberNode) fiberNode).release();
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                    setCellsLocked(true);
                    refreshGraph();
                }
                
            });
            confirmDialog.setDraggable(true);
            confirmDialog.open();
        };
        Consumer<BusinessObjectLight> consumerPortChange = port -> {
            MxBusinessObjectNode portNode = findNode(port);
            if (portNode != null) {
                portNode.setBusinessObject(port);
                portNode.setLabel(port.getName());
                portNode.setTooltip(port.getName());
            }
        };
        Consumer<BusinessObjectLight> consumerFiberChange = fiber -> {
            MxBusinessObjectNode fiberNode = findNode(fiber);
            MxBusinessObjectEdge fiberEdge = findEdge(fiber);
            if (fiberNode != null) {
                fiberNode.setBusinessObject(fiber);
                fiberNode.setLabel(fiber.getName());
                fiberNode.setTooltip(fiber.getName());
            }
            if (fiberEdge != null) {
                fiberEdge.setBusinessObject(fiber);
                fiberEdge.setTooltip(fiber.getName());
            }
        };
        if (!exchange) {
            treeLayout = new TreeLayout(this, 16);
            deviceNode = new DeviceNode(device, aem, bem, mem, ts, this, node -> 
                new WindowPortTools(node, aem, bem, mem, ts, physicalConnectionsService, 
                    managePortMirroringVisualAction, consumerReleaseFiber, consumerPortChange
                ).open()
            );
        } else {
            deviceNode = new DeviceNode(device, aem, bem, mem, ts, this, node -> 
                new WindowPortTools(node, aem, bem, mem, ts, physicalConnectionsService, 
                    managePortMirroringVisualAction, consumerReleaseFiber, consumerPortChange
                ).open()
            );
            treeLayout = new TreeLayout(this, 16);
        }
        
        treeLayout.setShowLeftover(showLeftover);
        treeLayout.addCellAddedListener(event -> {
            event.unregisterListener();
            endUpdate();
        });
        CableNode cableNode = new CableNode(cable, treeLayout, aem, bem, mem, ts, physicalConnectionsService, consumerFiberChange);
        cableNode.addCellAddedListener(event -> {
            event.unregisterListener();
            treeLayout.setRoots(cableNode);
            cableNode.setTreeLayout(treeLayout);
            cableNode.expand(false);
        });
        deviceNode.addRightClickCellListener(rightClickEvent -> new WindowDeviceTools(device, ts, managePortMirroringVisualAction).open());
        deviceNode.addCellAddedListener(event -> {
            event.unregisterListener();
            executeLayout();
        });
        addEdgeCompleteListener(event -> {
            MxBusinessObjectNode sourceNode = findNode(event.getSourceId());
            MxBusinessObjectNode targetNode = findNode(event.getTargetId());
            FiberNode fiberNode = getFiberNode(sourceNode, targetNode);
            PortNode portNode = getPortNode(sourceNode, targetNode);
            if (spliceFiber)
                spliceFiber(fiberNode, portNode, true);
            else if (cutFiber)
                cutFiber(fiberNode, portNode);
        });
        treeLayout.addExpandListener(expandEvent -> {
            List<BusinessObjectLight> children = new ArrayList();
            expandEvent.getSource().getNodeChildren().forEach(nodeChild -> children.add(nodeChild.getBusinessObject()));
            
            expandFibers(expandEvent.getSource().getBusinessObject(), children, deviceNode.getPorts());
        });
        treeLayout.addCollapseListener(collapseEvent -> collapsedFibers(collapseEvent.getSource().getBusinessObject()));
    }
    
    public boolean isSpliceFiber() {
        return spliceFiber;
    }

    public void setSpliceFiber(boolean spliceFiber) {
        this.spliceFiber = spliceFiber;
    }

    public boolean isCutFiber() {
        return cutFiber;
    }

    public void setCutFiber(boolean cutFiber) {
        this.cutFiber = cutFiber;
    }

    public boolean isShowLeftoverFiber() {
        return showLeftoverFiber;
    }

    public void setShowLeftoverFiber(boolean showLeftoverFiber) {
        this.showLeftoverFiber = showLeftoverFiber;
    }
    
    private void collapsedFibers(BusinessObjectLight parent) {
        if (parent == null)
            return;
        MxBusinessObjectNode parentNode = findNode(parent);
        if (parentNode == null)
            return;
        for (MxGraphEdge edge : getEdges()) {
            if (edge instanceof MxBusinessObjectEdge) {
                MxBusinessObjectEdge fiberEdge = (MxBusinessObjectEdge) edge;
                if (fiberEdge.getBusinessObject() != null) {
                    try {
                        if (bem.isParent(parent.getClassName(), parent.getId(), fiberEdge.getBusinessObject().getClassName(), fiberEdge.getBusinessObject().getId()))
                            fiberEdge.setSource(parentNode.getUuid());
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                }
            }
        }
    }
    
    private void expandFibers(BusinessObjectLight parent, List<BusinessObjectLight> children, List<BusinessObjectLight> ports) {
        if (parent == null || children == null || ports == null)
            return;
        ports.forEach(port -> expandFiber(port, children));
    }
    
    private void expandFiber(BusinessObjectLight port, List<BusinessObjectLight> children) {
        if (port == null || children == null)
            return;
        if (children.isEmpty())
            return;
        MxBusinessObjectNode businessObjectNode = findNode(port);
        if (!(businessObjectNode instanceof PortNode))
            return;
        PortNode portNode = (PortNode) businessObjectNode;
        BusinessObjectLight fiber = portNode.getFiber();
        if (fiber == null)
            return;
        
        try {
            BusinessObjectLight fiberParent = null;
            for (BusinessObjectLight child : children) {
                if (child.getId().equals(fiber.getId())) {
                    // The fiber parent are the same fiber
                    fiberParent = child;
                    break;
                }
            }
            if (fiberParent == null) {
                for (BusinessObjectLight child : children) {
                    if (bem.isParent(child.getClassName(), child.getId(), fiber.getClassName(), fiber.getId())) {
                        fiberParent = child;
                        break;
                    }
                }
            }
            if (fiberParent != null) {
                MxBusinessObjectNode fiberParentNode = findNode(fiberParent);
                MxBusinessObjectEdge fiberEdge = findEdge(fiber);
                
                if (fiberEdge == null) {
                    fiberEdge = new FiberEdge(fiber);
                    
                    LinkedHashMap<String, String> edgeStyle = new LinkedHashMap(EDGE_STYLE);
                    String color = BusinessObjectUtil.getBusinessObjectColor(fiber, aem, bem, mem);
                    if (color != null)
                        edgeStyle.put(MxConstants.STYLE_STROKECOLOR, color);
                    fiberEdge.setRawStyle(edgeStyle);
                    fiberEdge.setSource(fiberParentNode.getUuid());
                    fiberEdge.setTarget(portNode.getUuid());
                    fiberEdge.setIsSelectable(false);
                    
                    fiberEdge.addCellAddedListener(event -> {
                        event.unregisterListener();
                        setCellsLocked(false);
                        event.getSource().overrideStyle();
                        event.getSource().setTooltip(fiber.getName());                        
                        setCellsLocked(true);
                        
                        this.refreshGraph();
                    });
                    addEdge(fiberEdge);
                } else {
                    this.setCellsLocked(true);
                    fiberEdge.setSource(fiberParentNode.getUuid());
                    this.setCellsLocked(false);
                }
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }
    
    private void cutFiber(FiberNode fiberNode, PortNode portNode) {
        try {
            HashMap<String, List<BusinessObjectLight>> endpoints = bem.getSpecialAttributes(
                fiberNode.getBusinessObject().getClassName(), 
                fiberNode.getBusinessObject().getId(), 
                OspConstants.SPECIAL_ATTR_ENDPOINT_A, 
                OspConstants.SPECIAL_ATTR_ENDPOINT_B
            );
            if (endpoints.isEmpty()) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.warning"),
                    ts.getTranslatedString("module.ospman.fiber-tools.tool.cut-fiber.warning"), 
                    AbstractNotification.NotificationType.WARNING, ts
                ).open();
                return;
            }
            ConfirmDialog confirmDialog = new ConfirmDialog(ts,
                    ts.getTranslatedString("module.ospman.mid-span-access.confirmation.cut-fiber.title"),
                    new Label(ts.getTranslatedString("module.ospman.mid-span-access.confirmation.cut-fiber.text")),
                    ts.getTranslatedString("module.general.messages.ok"),
                    () -> {
                        try {
                            BusinessObjectLight fiber = fiberNode.getBusinessObject();
                            List<BusinessObjectLight> fiberParents = bem.getMultipleParents(fiber.getClassName(), fiber.getId());
                            BusinessObjectLight fiberParent = fiberParents.remove(0);//bem.getParent(fiber.getClassName(), fiber.getId());
                            
                            HashMap<String, List<String>> objects = new HashMap();
                            objects.put(fiber.getClassName(), Arrays.asList(fiber.getId()));
                            
                            String[] newFibersId = bem.copySpecialObjects(fiberParent.getClassName(), fiberParent.getId(), objects, false);
                            if (newFibersId.length == 1) {
                                BusinessObjectLight newFiber = bem.getObject(fiber.getClassName(), newFibersId[0]);
                                fiberParents.forEach(parent -> {
                                    try {
                                        bem.addParentToSpecialObject(newFiber.getClassName(), newFiber.getId(), parent.getClassName(), parent.getId());
                                    } catch (InventoryException ex) {
                                        new SimpleNotification(
                                                ts.getTranslatedString("module.general.messages.error"),
                                                ex.getLocalizedMessage(),
                                                AbstractNotification.NotificationType.ERROR, ts
                                        ).open();
                                    }
                                });
                                HashMap<String, String> attrs = new HashMap();
                                attrs.put(Constants.PROPERTY_NAME, fiber.getName());
                                attrs.put(Constants.PROPERTY_LEFTOVER, String.valueOf(true));
                                
                                bem.updateObject(newFiber.getClassName(), newFiber.getId(), attrs);
                                newFiber.setName(fiber.getName());
                                
                                spliceFiber(fiberNode, portNode, false);
                            } else {
                                for (String newFiberId : newFibersId)
                                    bem.deleteObject(fiber.getClassName(), newFiberId, true);
                                throw new InventoryException(
                                    ts.getTranslatedString(String.format("module.ospman.copy-fiber.error", fiber.getId()))
                                );
                            }
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"),
                                    ex.getLocalizedMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                    });
            confirmDialog.setDraggable(true);
            confirmDialog.open();
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }
    private void spliceFiber(FiberNode fiberNode, PortNode portNode, boolean confirm) {
        if (fiberNode == null && portNode == null)
            return;
        
        LinkedHashMap<String, String> edgeStyle = new LinkedHashMap(EDGE_STYLE);
        edgeStyle.put(MxConstants.STYLE_STROKECOLOR, fiberNode.getColor());
        Command cmdSpliceFiber = () -> {
            try {
                BusinessObjectLight fiberObject = fiberNode.getBusinessObject();
                BusinessObjectLight portObject = portNode.getBusinessObject();

                String specialRelationshipName = null;
                boolean portHasEndpointA = bem.hasSpecialAttribute(portObject.getClassName(), portObject.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_A);
                boolean portHasEndpointB = bem.hasSpecialAttribute(portObject.getClassName(), portObject.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_B);
                if (portHasEndpointA || portHasEndpointB) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"), 
                        String.format(ts.getTranslatedString("module.ospman.warning.port-connected"), portObject.getName()), 
                        AbstractNotification.NotificationType.WARNING, ts
                    ).open();
                    return;
                }
                boolean fiberHasEndpointA = bem.hasSpecialAttribute(fiberObject.getClassName(), fiberObject.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_A);
                boolean fiberHasEndpointB = bem.hasSpecialAttribute(fiberObject.getClassName(), fiberObject.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_B);
                if (fiberHasEndpointA && fiberHasEndpointB) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"), 
                        String.format(ts.getTranslatedString("module.ospman.warning.fiber-connected"), fiberObject.getName()), 
                        AbstractNotification.NotificationType.WARNING, ts
                    ).open();
                    return;
                } else if (fiberHasEndpointA) {
                    specialRelationshipName = OspConstants.SPECIAL_ATTR_ENDPOINT_B;
                } else if (fiberHasEndpointB) {
                    specialRelationshipName = OspConstants.SPECIAL_ATTR_ENDPOINT_A;
                } else {
                    specialRelationshipName = OspConstants.SPECIAL_ATTR_ENDPOINT_A;
                }
                bem.createSpecialRelationship(
                    fiberObject.getClassName(), fiberObject.getId(),
                    portObject.getClassName(), portObject.getId(),
                    specialRelationshipName, true
                );
                MxBusinessObjectEdge fiberEdge = new FiberEdge(fiberObject);
                fiberEdge.setRawStyle(edgeStyle);
                fiberEdge.setSource(fiberNode.getUuid());
                fiberEdge.setTarget(portNode.getUuid());

                fiberEdge.addCellAddedListener(fiberEvent -> {
                    fiberEvent.unregisterListener();
                    setCellsLocked(false);

                    MxBusinessObjectEdge cell = (MxBusinessObjectEdge) fiberEvent.getSource();
                    cell.overrideStyle();
                    cell.setTooltip(cell.getBusinessObject() != null ? cell.getBusinessObject().getName() : null);

                    setCellsLocked(true);

                    this.refreshGraph();
                });
                addEdge(fiberEdge);

                fiberNode.splice();
                portNode.setFiber(fiberObject);

                fiberNode.overrideStyle();
                portNode.overrideStyle();

            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        };
        if (confirm) {
            ConfirmDialog confirmDialog = new ConfirmDialog(ts,
                ts.getTranslatedString("module.ospman.mid-span-access.confirmation.splice-fiber.title"), 
                new Label(ts.getTranslatedString("module.ospman.mid-span-access.confirmation.splice-fiber.text")),
                ts.getTranslatedString("module.general.messages.ok"), 
                cmdSpliceFiber
            );
            confirmDialog.setDraggable(true);
            confirmDialog.open();
        }
        else
            cmdSpliceFiber.execute();
    }
    // <editor-fold defaultstate="collapsed" desc="Helpers">
    private void executeLayout() {
        setCellsLocked(false);
        executeStackLayout(null, true, 200, 15);
        setCellsLocked(true);
    }
    private MxBusinessObjectNode findNode(String nodeUuid) {
        if (nodeUuid != null) {
            for (MxGraphNode node : getNodes()) {
                if (node instanceof MxBusinessObjectNode && nodeUuid.equals(node.getUuid()))
                    return (MxBusinessObjectNode) node;
            }
        }
        return null;
    }
    private MxBusinessObjectNode findNode(BusinessObjectLight object) {
        if (object != null) {
            for (MxGraphNode node : getNodes()) {
                if (node instanceof MxBusinessObjectNode && 
                    ((MxBusinessObjectNode) node).getBusinessObject() != null && 
                    ((MxBusinessObjectNode) node).getBusinessObject().getId().equals(object.getId())) {
                    return (MxBusinessObjectNode) node;
                }
            }
        }
        return null;
    }
    private MxBusinessObjectEdge findEdge(BusinessObjectLight businessObject) {
        if (businessObject != null) {
            for (MxGraphEdge edge : getEdges()) {
                if (edge instanceof MxBusinessObjectEdge && 
                    ((MxBusinessObjectEdge) edge).getBusinessObject() != null && 
                    ((MxBusinessObjectEdge) edge).getBusinessObject().getId().equals(businessObject.getId())) {
                    return (MxBusinessObjectEdge) edge;
                }
            }
        }
        return null;
    }    
    private FiberNode getFiberNode(MxBusinessObjectNode sourceNode, MxBusinessObjectNode targetNode) {
        if (sourceNode instanceof FiberNode && targetNode instanceof FiberNode)
            return null;
        else if (sourceNode instanceof FiberNode)
            return (FiberNode) sourceNode;
        else if (targetNode instanceof FiberNode)
            return (FiberNode) targetNode;
        return null;
    }
    private PortNode getPortNode(MxBusinessObjectNode sourceNode, MxBusinessObjectNode targetNode) {
        if (sourceNode instanceof PortNode && targetNode instanceof PortNode)
            return null;
        else if (sourceNode instanceof PortNode)
            return (PortNode) sourceNode;
        else if (targetNode instanceof PortNode)
            return (PortNode) targetNode;
        return null;
    }
    // </editor-fold>
}
