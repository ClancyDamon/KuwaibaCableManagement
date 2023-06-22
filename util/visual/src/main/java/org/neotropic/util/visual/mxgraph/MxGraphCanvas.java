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

package org.neotropic.util.visual.mxgraph;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.neotropic.flow.component.mxgraph.MxGraphCellSelectedEvent;
import com.neotropic.flow.component.mxgraph.MxGraphCellUnselectedEvent;
import com.neotropic.flow.component.mxgraph.MxGraphDeleteCellSelectedEvent;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.neotropic.flow.component.mxgraph.Point;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;


/**
 * Wrapper to manage mxgraph instance and his objects
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 * @param <N> Node object type
 * @param <E> Edge object type
 */
public class MxGraphCanvas<N, E> {
    
    MxGraph mxGraph;  
    
    private LinkedHashMap<N, MxGraphNode> nodes;
    /**
     * A dictionary with the edges in the current view
     */
    private HashMap<E, MxGraphEdge> edges;
    
    private HashMap<E,N> sourceEdgeNodes;
    
    private HashMap<E,N> targetEdgeNodes;
    
    private String selectedCellId;
    
    private String selectedCellType;
    
    private Command comObjectSelected;
    
    private Command comObjectDeleted;
    
    private Command comObjectUnselected;
    
    public MxGraph getMxGraph() {
        return mxGraph;
    }

    public void setMxGraph(MxGraph mxGraph) {
        this.mxGraph = mxGraph;
    }

    public LinkedHashMap<N, MxGraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(LinkedHashMap<N, MxGraphNode> nodes) {
        this.nodes = nodes;
    }

    public HashMap<E, MxGraphEdge> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<E, MxGraphEdge> edges) {
        this.edges = edges;
    }

    public HashMap<E, N> getSourceEdgeNodes() {
        return sourceEdgeNodes;
    }

    public void setSourceEdgeNodes(HashMap<E, N> sourceEdgeNodes) {
        this.sourceEdgeNodes = sourceEdgeNodes;
    }

    public HashMap<E, N> getTargetEdgeNodes() {
        return targetEdgeNodes;
    }

    public void setTargetEdgeNodes(HashMap<E, N> targetEdgeNodes) {
        this.targetEdgeNodes = targetEdgeNodes;
    }  

    public String getSelectedCellId() {
        return selectedCellId;
    }

    public void setSelectedCellId(String selectedCellId) {
        this.selectedCellId = selectedCellId;
    }

    public String getSelectedCellType() {
        return selectedCellType;
    }

    public void setSelectedCellType(String selectedCellType) {
        this.selectedCellType = selectedCellType;
    }

    public void setComObjectSelected(Command comObjectSelected) {
        this.comObjectSelected = comObjectSelected;
    }

    public void setComObjectDeleted(Command comObjectDeleted) {
        this.comObjectDeleted = comObjectDeleted;
    }

    public void setComObjectUnselected(Command comObjectUnselected) {
        this.comObjectUnselected = comObjectUnselected;
    }
    
    public MxGraphCanvas() {
        initGraph("100%", "100%");
    }  
    
    public MxGraphCanvas(String width, String height) {
        initGraph(width, height);
    } 
    
    private void initGraph(String width, String height) {       
       mxGraph = new MxGraph();
       mxGraph.setWidth(width);
       mxGraph.setHeight(height);    
//       mxGraph.setGrid("img/grid.gif"); // sets the grid by default
       nodes = new LinkedHashMap<>();
       edges = new HashMap<>();     
       sourceEdgeNodes = new HashMap<>();
       targetEdgeNodes = new HashMap<>();
       
       mxGraph.addCellUnselectedListener((MxGraphCellUnselectedEvent t) -> {
           this.selectedCellId = null;
           this.selectedCellType = null;
           if (comObjectUnselected != null)
               comObjectUnselected.execute();
       });
       
       mxGraph.addCellSelectedListener((MxGraphCellSelectedEvent t) -> {
           this.selectedCellId = t.getCellId();
           this.selectedCellType = t.isVertex() ? MxGraphCell.PROPERTY_VERTEX : MxGraphCell.PROPERTY_EDGE;
           if (comObjectSelected != null)
               comObjectSelected.execute();
       });
       
       mxGraph.addDeleteCellSelectedListener((MxGraphDeleteCellSelectedEvent t) -> {
           if (comObjectDeleted != null)
               comObjectDeleted.execute();
       });
    } 
    
    public MxGraphNode findMxGraphNode(N node) {
        return nodes.get(node);
    }
    
    public MxGraphEdge findMxGraphEdge(E edge) {
        return edges.get(edge);
    }
    
    public N findSourceEdgeObject(E edge) {
        return sourceEdgeNodes.get(edge);
    }
    
    public N findTargetEdgeObject(E edge) {
        return targetEdgeNodes.get(edge);
    }
    /**
     * Create and add a new node in the canvas
     * @param node the object that represent the node
     * @param nodeId the node id
     * @param xCoordinate the x coordinate in the canvas
     * @param yCoordinate the y coordinate in the canvas
     * @param imageUri the uri image
     * @return the new node
     */
    public MxGraphNode addNode(N node, String nodeId, int xCoordinate, int yCoordinate, String imageUri) {

        if (!nodes.containsKey(node)) {

            MxGraphNode newNode = new MxGraphNode();
            if (imageUri != null && !imageUri.isEmpty()) 
                newNode.setImage(imageUri);
            
            newNode.setUuid(nodeId);
            newNode.setLabel(node.toString());
            newNode.setWidth((int) Constants.DEFAULT_ICON_WIDTH);
            newNode.setHeight((int) Constants.DEFAULT_ICON_HEIGHT);
            newNode.setX((xCoordinate)); //The position is scaled
            newNode.setY((yCoordinate));
            newNode.setShape(MxConstants.SHAPE_IMAGE);
            nodes.put(node, newNode);
            mxGraph.addNode(newNode);
            return newNode;
        }
        return null;
    }
    /**
     * add the given node in the canvas     
     * @param node he object that represents the node
     * @param mxgraphNode the new mxGraphNode
     */
    public void addNode(N node, MxGraphNode mxgraphNode) {
        if (!nodes.containsKey(node)) {       
            nodes.put(node, mxgraphNode);
            mxGraph.addNode(mxgraphNode);
//            mxGraph.refreshGraph();
        }
    }
    /**
     * creates a new edge in the canvas
     * @param edgeObject the object that represents the edge
     * @param edgeId the edge id
     * @param sourceObject the source edge object
     * @param targetObject the target edge object
     * @param points the control points list
     * @param sourceLabel the source label
     * @param targetLabel the target label
     * @return the new edge
     */
    public MxGraphEdge addEdge(E edgeObject, String edgeId, N sourceObject, N targetObject, List<Point> points,String sourceLabel, String targetLabel) {
       
         if (!edges.containsKey(edgeObject)) {       
            MxGraphNode sourceNode = findMxGraphNode(sourceObject);
            MxGraphNode targetNode = findMxGraphNode(targetObject);
            MxGraphEdge newEdge = new MxGraphEdge();

            newEdge.setUuid(edgeId);
            newEdge.setSource(sourceNode.getUuid());
            newEdge.setTarget(targetNode.getUuid());
            newEdge.setSourceLabel(sourceLabel);
            newEdge.setTargetLabel(targetLabel);
            newEdge.setLabel(edgeObject.toString());
            newEdge.setPoints(points);

            edges.put(edgeObject, newEdge);
            sourceEdgeNodes.put(edgeObject, sourceObject);
            targetEdgeNodes.put(edgeObject, targetObject);
            mxGraph.addEdge(newEdge);
            return newEdge;       
        }        
        return null;
    }
    /**
     * creates a new edge in the canvas
     * @param edgeObject the object that represents the edge
     * @param edgeId the edge id
     * @param sourceObject the source edge object
     * @param targetObject the target edge object
     * @param points the control points list
     * @param sourceLabel the source label
     * @param targetLabel the target label
     * @param edgeColor the edge color
     * @return the new edge
     */
    public MxGraphEdge addEdge(E edgeObject, String edgeId, N sourceObject, N targetObject, List<Point> points,String sourceLabel, String targetLabel, String edgeColor) {
       
         if (!edges.containsKey(edgeObject)) {       
            MxGraphNode sourceNode = findMxGraphNode(sourceObject);
            MxGraphNode targetNode = findMxGraphNode(targetObject);
            MxGraphEdge newEdge = new MxGraphEdge();

            newEdge.setUuid(edgeId);
            newEdge.setSource(sourceNode.getUuid());
            newEdge.setTarget(targetNode.getUuid());
            newEdge.setSourceLabel(sourceLabel);
            newEdge.setTargetLabel(targetLabel);
            newEdge.setLabel(edgeObject.toString());
            newEdge.setPoints(points);
            if (edgeColor != null)
                newEdge.setStrokeColor(edgeColor);

            edges.put(edgeObject, newEdge);
            sourceEdgeNodes.put(edgeObject, sourceObject);
            targetEdgeNodes.put(edgeObject, targetObject);
            mxGraph.addEdge(newEdge);
            return newEdge;       
        }        
        return null;
    }
      /**
     * add the given edge in the canvas     
     * @param edge he object that represents the edge
     * @param mxgraphEdge the new mxgraphCell
     */
    public void addEdge(E edge, MxGraphEdge mxgraphEdge) {
        if (!edges.containsKey(edge)) {       
            edges.put(edge, mxgraphEdge);
            mxGraph.addEdge(mxgraphEdge);
            mxGraph.refreshGraph();
        }
    }
     /**
     * add the given edge in the canvas     
     * @param edge the object that represents the edge
     * @param sourceNode the object that represents the source node
     * @param targetNode the object that represents the target node
     * @param mxgraphEdge the new mxgraphCell
     */
    public void addEdge(E edge, N sourceNode, N targetNode, MxGraphEdge mxgraphEdge) {
        if (!edges.containsKey(edge)) {       
            edges.put(edge, mxgraphEdge);
            sourceEdgeNodes.put(edge, sourceNode);
            targetEdgeNodes.put(edge, targetNode);
            mxGraph.addEdge(mxgraphEdge);
//            mxGraph.refreshGraph();
        }
    }
    /**
     * Removes a node from the canvas
     * @param businessObject the object to be removed
     */
    public void removeNode(N businessObject) {
        
        try {
            mxGraph.removeNode(nodes.get(businessObject));
            nodes.remove(businessObject);

            //delete edges related to the object
            List<E> edgesToDelete = new ArrayList<>();

            for (Map.Entry<E, N> entry : sourceEdgeNodes.entrySet()) {
                if (entry.getValue().equals(businessObject)) {
                    edgesToDelete.add(entry.getKey());
                }
            }
            for (Map.Entry<E, N> entry : targetEdgeNodes.entrySet()) {
                if (entry.getValue().equals(businessObject)) {
                    edgesToDelete.add(entry.getKey());
                }
            }

            for (E edge : edgesToDelete) {   
                edges.remove(edge);
                sourceEdgeNodes.remove(edge);
                targetEdgeNodes.remove(edge);
            } 
        } catch (Exception e) {
            System.err.println("ex" + e);
        }
    }
    /**
     * Removes an edge from the canvas
     * @param businessObject the object to be removed
     */
    public void removeEdge(E businessObject) {       
        mxGraph.removeEdge(edges.get(businessObject));
        edges.remove(businessObject);
                    
        sourceEdgeNodes.remove(businessObject);
        targetEdgeNodes.remove(businessObject);            
    }

     /**
     * this method remove all cells(vertex and edges) in the graph
     */
    public void removeAllCells() {     
        
        mxGraph.removeAllCells();
        nodes.clear();
        edges.clear();
        sourceEdgeNodes.clear();
        targetEdgeNodes.clear();    
    }
        
}
