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
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Parent of {@link TreeLayoutNode} used to execute a tree layout
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class TreeLayout extends MxGraphNode {
    private double width = 0;
    private double height = 0;
    public static final double BUTTON_SIZE = 16;
    
    private final double spacingBottom;
    private TreeLayoutNode[] roots;
    private final MxGraph graph;
    private double x = 0;
    private double y = 0;
    private final LinkedHashMap<String, String> treeLayoutStyle = new LinkedHashMap();
    {
        treeLayoutStyle.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        treeLayoutStyle.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        treeLayoutStyle.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
    }
    private boolean showLeftover = false;
    
    public TreeLayout(MxGraph graph, double spacingBottom) {
        Objects.requireNonNull(graph);
        
        this.spacingBottom = spacingBottom;
        this.graph = graph;        
        setRawStyle(treeLayoutStyle);
        addCellAddedListener(event -> {
            event.unregisterListener();
            setIsSelectable(false);
            setConnectable(false);
            overrideStyle();
        });
        graph.addNode(this);
    }
    
    public void setShowLeftover(boolean showLeftover) {
        this.showLeftover = showLeftover;
    }
    
    public MxGraph getGraph() {
        return graph;
    }

    @Override
    public void setGeometry(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        super.setGeometry(x, y, width, height);
    }

    @Override
    public void setGeometry(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        super.setGeometry(x, y, width, height);
    }
    
    public TreeLayoutNode[] getRoots() {
        return roots;
    }
    
    public void setRoots(TreeLayoutNode... roots) {
        this.roots = roots;
    }
    
    public void execute(TreeLayoutNode treeLayoutNode, boolean expand, boolean fromClient) {
        if (roots != null) {
            width = 0;
            height = 0;
            graph.setCellsLocked(false);
            graph.beginUpdate();
            for (TreeLayoutNode root : roots) {
                root.setCellVisible(true);
                setPoint(null, root);
                executeRecursive(root, true);
            }
            setWidth(width);
            setHeight(height);
            
            graph.endUpdate();
            graph.setCellsLocked(true);
            if (expand)
                fireExpandEvent(treeLayoutNode, fromClient);
            else
                fireCollapseEvent(treeLayoutNode, fromClient);
        }
    }
    
    private void executeRecursive(TreeLayoutNode parent, boolean visible) {
        if (!parent.isLeaf()) {
            List<TreeLayoutNode> children = parent.getNodeChildren();
            if (children != null) {
                for (TreeLayoutNode child : children) {
                    if (child instanceof FiberNode) {
                        FiberNode fiberNode = (FiberNode) child;
                        if (showLeftover && fiberNode.isLeftover())
                            fiberNode.setNodeVisible(true);
                    }
                    if (parent.isNodeExpanded() && child.isNodeVisible()) {
                        child.setCellVisible(visible);
                        if (visible)
                            setPoint(parent, child);
                        executeRecursive(child, visible);
                    }
                    else {
                        child.setGeometry(0, 0, 0, 0);
                        child.setCellVisible(false);
                        child.setVisible(false);
                        executeRecursive(child, false);
                    }
                }
            }
        }
    }
    
    private void setPoint(TreeLayoutNode parent, TreeLayoutNode child) {
        child.setTreeLayout(this);
        
        if (parent != null)
            child.setNodeLevel(parent.getNodeLevel() + 1);
        else
            child.setNodeLevel(0);
        
        double childX = BUTTON_SIZE + child.getNodeLevel() * BUTTON_SIZE;
        double oldHeight = height;
        
        double newWidth = childX + child.getNodeWidth();
        if (newWidth > width)
            width = newWidth;
        height += child.getNodeHeight() + spacingBottom;
        
        child.setNodeX(childX);
        child.setNodeY(oldHeight);
        
        child.getNode().setX(child.getNodeX());
        child.getNode().setY(child.getNodeY());
        child.getNode().setWidth(child.getNodeWidth());
        child.getNode().setHeight(child.getNodeHeight());
    }
    
    private void fireExpandEvent(TreeLayoutNode treeLayoutNode, boolean fromClient) {
        fireEvent(new ExpandEvent(treeLayoutNode, fromClient));
    }
    
    private void fireCollapseEvent(TreeLayoutNode treeLayoutNode, boolean fromClient) {
        fireEvent(new ExpandEvent(treeLayoutNode, fromClient));
    }
    
    public Registration addExpandListener(ComponentEventListener<ExpandEvent> listener) {
        return addListener(ExpandEvent.class, listener);
    }
    
    public Registration addCollapseListener(ComponentEventListener<CollapseEvent> listener) {
        return addListener(CollapseEvent.class, listener);
    }
    
    public class CollapseEvent extends ComponentEvent<TreeLayoutNode> {

        public CollapseEvent(TreeLayoutNode source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public class ExpandEvent extends ComponentEvent<TreeLayoutNode> {

        public ExpandEvent(TreeLayoutNode source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
