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
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;

/**
 * A node that can be used in a tree layout
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class TreeLayoutNode extends MxBusinessObjectNode {
    private double x = 0;
    private double y = 0;
    private double width = 0;
    private double height = 0;
    private int level;
    private boolean expanded = false;
    private final String btnExpandId = "btnExpand";
    private final String angleRight = "img/angle-right.svg";
    private final String angleDown = "img/angle-down.svg";
    private List<TreeLayoutNode> nodeChildren;
    private boolean isLeaf = false;
    private TreeLayoutNode parentNode;
    private TreeLayout treeLayout;
    private boolean isNodeVisible = true;
    private boolean isCellVisible = false;
    
    public TreeLayoutNode(BusinessObjectLight businessObject) {
        super(businessObject);
        
        setGeometry(x, y, width, height);
        addCellAddedListener(event -> {
            event.unregisterListener();
            
            if (!isLeaf)
                addToggleButton();
            setCellVisible(isCellVisible);
            setGeometry(x, y, width, height);
        });
        addClickOverlayButtonListener(event -> {
            if (btnExpandId.equals(event.getButtonId())) {
                if (isNodeExpanded())
                    collapse(true);
                else
                    expand(true);
                removeToggleButton();
                addToggleButton();
            }
        });
    }
    
    private void addToggleButton() {
        addOverlayButton(btnExpandId, "", isNodeExpanded() ? angleDown : angleRight, MxConstants.ALIGN_LEFT, MxConstants.ALIGN_MIDDLE, -8, 0, (int) TreeLayout.BUTTON_SIZE, (int) TreeLayout.BUTTON_SIZE);
    }
    
    private void removeToggleButton() {
        removeOverlayButton(btnExpandId);
    }
    
    public double getNodeWidth() {
        return width;
    }
    
    public double getNodeX() {
        return x;
    }
    
    public void setNodeX(double x) {
        this.x = x;
    }
    
    public double getNodeY() {
        return y;
    }
    
    public void setNodeY(double y) {
        this.y = y;
    }
    
    public void setNodeWidth(double width) {
        this.width = width;
    }
    
    public double getNodeHeight() {
        return height;
    }
    
    public void setNodeHeight(double height) {
        this.height = height;
    }
    
    public List<TreeLayoutNode> getNodeChildren() {
        return nodeChildren;
    }

    public void setNodeChildren(List<TreeLayoutNode> nodeChildren) {        
        this.nodeChildren = nodeChildren;
        if (nodeChildren != null)
            nodeChildren.forEach(nodeChild -> nodeChild.setParentNode(this));
    }
    
    public MxGraphNode getNode() {
        return this;
    }
    
    public boolean isLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
        if (isLeaf)
            removeToggleButton();
        else
            addToggleButton();
    }
    
    protected int getNodeLevel() {
        return level;
    }
    
    protected void setNodeLevel(int level) {
        this.level = level;
    }
    
    public TreeLayoutNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(TreeLayoutNode parentNode) {
        this.parentNode = parentNode;
    }
    
    public boolean isNodeExpanded() {
        return expanded;
    }
    
    public void setNodeExpanded(boolean expanded) {
        this.expanded = expanded;
    }
    
    public boolean isNodeVisible() {
        return isNodeVisible;
    }
    
    public void setNodeVisible(boolean isNodeVisible) {
        this.isNodeVisible = isNodeVisible;
    }
    
    protected TreeLayout getTreeLayout() {
        return treeLayout;
    }
    
    protected void setTreeLayout(TreeLayout treeLayout) {
        this.treeLayout = treeLayout;
    }

    @Override
    public void setCellVisible(boolean cellVisible) {
        this.isCellVisible = cellVisible;
        super.setCellVisible(cellVisible);
    }
    
    public void expand(boolean fromClient) {
        setNodeExpanded(true);
        if (treeLayout != null)
            treeLayout.execute(this, false, fromClient);
    }
    
    public void collapse(boolean fromClient) {
        setNodeExpanded(false);
        if (treeLayout != null)
            treeLayout.execute(this, false, fromClient);
    }
    
}
