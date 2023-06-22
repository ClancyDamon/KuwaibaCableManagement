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
package org.neotropic.kuwaiba.core.apis.integration.views;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * A representation of a view (which in turn a graphical representation of an inventory object or a function in the domain of the inventory system) as a set of java objects. 
 * In general terms, a ViewMap instance is a group of nodes and connections between those nodes, as well as auxiliary components, such as comments, or groups of nodes. This map 
 * does not contain rendering information, such as dimensions or positions, but it is rather a description of the internal structure of the view, which can be used by the consumer 
 * to perform analysis on the information contained by the view.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ViewMap {
    /**
     * The list of nodes in the map.
     */
    private List<AbstractViewNode> nodes;
    /**
     * The list of edges in the map.
     */
    private List<AbstractViewEdge> edges;
    /**
     * A hashmap containing the edges and their source nodes.
     */
    private HashMap<AbstractViewEdge, AbstractViewNode> sourceNodes;
    /**
     * A hashmap containing the edges and their target nodes.
     */
    private HashMap<AbstractViewEdge, AbstractViewNode> targetNodes;
    /**
     * Extra settings that might be applicable to the view map. Typical examples are properties such as "zoom", or "units".
     */
    private Properties properties;

    public ViewMap() {
        this.nodes  = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.sourceNodes = new HashMap<>();
        this.targetNodes = new HashMap<>();
        this.properties = new Properties();
    }
    
    /**
     * Adds a node to the map.
     * @param node The node to be added. If the node is already in the map, nothing will be done.
     */
    public void addNode(AbstractViewNode node) {
        if (!nodes.contains(node))
            nodes.add(node);
    }
    
    /**
     * Adds an edge to the map.
     * @param edge The edge to be added. If the edge is already in the map, nothing will be done.
     */
    public void addEdge(AbstractViewEdge edge) {
        if (!edges.contains(edge))
            edges.add(edge);
    }
    
    /**
     * Sets the source node of a connection (edge). If the edge already has a source node, it will be disconnected.
     * @param edge The edge to be connected to the node.
     * @param sourceNode The node to be connected to the edge.
     */
    public void attachSourceNode(AbstractViewEdge edge, AbstractViewNode sourceNode) {
        assert (!edges.contains(edge) || !nodes.contains(sourceNode)) : "The map does not contain either the source node or the edge provided";
        sourceNodes.remove(edge);
        sourceNodes.put(edge, sourceNode);
    }
    
    /**
     * Sets the target node of a connection (edge). If the edge already has a target node, it will be disconnected.
     * @param edge The edge to be connected to the node.
     * @param targetNode The node to be connected to the edge.
     */
    public void attachTargetNode(AbstractViewEdge edge, AbstractViewNode targetNode) {
        assert (!edges.contains(edge) || !nodes.contains(targetNode)) : "The map does not contain either the target node or the edge provided";
        targetNodes.remove(edge);
        targetNodes.put(edge, targetNode);
    }
    
    /**
     * Gets the object behind a node whose identifier is the one provider.
     * @param identifier The object to search.
     * @return The node or null if such identifier does not belong to any node.
     */
    public AbstractViewNode getNode(Object identifier) {
        return nodes.stream().filter((aNode) -> {
            return aNode.getIdentifier().equals(identifier);
        }).findFirst().orElse(null);
    }
    
    /**
     * Gets the object behind a node whose identifier is the one provider.
     * @param identifier The object to search.
     * @return The node or null if such identifier does not belong to any node.
     */
    public AbstractViewEdge getEdge(Object identifier) {
        return edges.stream().filter((anEdge) -> {
            return anEdge.getIdentifier().equals(identifier);
        }).findFirst().orElse(null);
    }
    
    /**
     * Gets the registered source node for a given edge.
     * @param edge The edge.
     * @return The related source node or null if none was registered.
     */
    public AbstractViewNode getEdgeSource(AbstractViewEdge edge) {
        return sourceNodes.get(edge);
    }
    
    /**
     * Gets the registered target node for a given edge.
     * @param edge The edge.
     * @return The related target node or null if none was registered.
     */
    public AbstractViewNode getEdgeTarget(AbstractViewEdge edge) {
        return targetNodes.get(edge);
    }
    
    /**
     * Returns the available nodes.
     * @return The nodes in the map.
     */
    public List<AbstractViewNode> getNodes() {
        return this.nodes;
    }
    
    /**
     * Returns the available edges.
     * @return The edges in the map.
     */
    public List<AbstractViewEdge> getEdges() {
        return this.edges;
    }

    /**
     * Returns the extra information associated to the view that uses this map. This 
     * can be stuff like the coordinates of the center of a map, or the zoom of an object view. 
     * @return The set of properties set for this map.
     */
    public Properties getProperties() {
        return this.properties;
        
    }
    /**
     * Removes all the entries in the lists and hashmaps.
     */
    public void clear() {
        this.edges.clear();
        this.nodes.clear();
        this.sourceNodes.clear();
        this.targetNodes.clear();
        this.properties.clear();
    }
    
    /**
     * Tries to match a node in the view with a business object provided as parameter. 
     * The <code>identifier</code> is compared with the business object behind each one of the existing nodes 
     * using its <code>equals</code> method, thus being able to support multiple types of comparisons.
     * @param identifier The business object to be matched against.
     * @return The view node if existent, null otherwise.
     */
    public AbstractViewNode findNode(Object identifier) {
        try {
            return this.nodes.stream().filter((aNode) -> {
                return aNode.equals(identifier);
            }).findFirst().get();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }
    
    /**
     * Tries to match an edge in the view with a business object provided as parameter. 
     * The <code>identifier</code> is compared with the business object behind each one of the existing edges 
     * using its <code>equals</code> method, thus being able to support multiple types of comparisons.
     * @param identifier The business object to be matched against.
     * @return The view edge if existent, null otherwise.
     */
    public AbstractViewEdge findEdge(Object identifier) {
        try {
            return this.edges.stream().filter((anEdge) -> {
                return anEdge.equals(identifier);
            }).findFirst().get();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }
}
