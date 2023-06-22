/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.util.visual.tree;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/** 
 * A tree grid that extends the features of the Tree Grid and makes use of the 
 * Nodes API also allows a custom data provider (with treeData) to facilitate 
 * the single node refreshing and lazy loading per level, 
 * Warning: still has no pagination!
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 * @param <T> an abstract node
 */
public abstract class NavTreeGrid <T extends AbstractNode> extends TreeGrid<T> {
    /**
     * A plain list of all the current nodes in the tree grid
     */
    private final List<T> rawData;
    /**
     * A custom lazy tree data provider that loads 2 levels at a time
     */
    private TreeDataProvider dataProvider;
    /**
     * Keeps track of the loadChildrenOf listener of the tree grid, to be removed 
     * and re-added every time a node of the tree is updated the loadChildrenOf 
     * listener is no need it
     */
    private Registration expandListener;
            
    public NavTreeGrid() {
        rawData = new ArrayList<>();
        addExpandListener_();
        //we set an isExpaded boolean in case we are updating the node's icon when collapses
        addCollapseListener(e -> 
            e.getItems().stream().findFirst().ifPresent(node -> {
                e.getItems().stream().findFirst().get().setExpanded(false);
                refreshNode(node);
            })
        );
    }
    
    public List<T> getAllNodesAsList(){
        return rawData;
    }
    
    public Optional<T> findNodeById(String id) throws NoSuchElementException {
        return rawData.stream()
            .filter(n -> n.getId().equals(id))
            .findFirst();
    }
    
    public boolean contains(T node){
        return this.getTreeData().contains(node);
    }
    
    public void refreshNode(T node){
        getDataProvider().refreshItem(node);
    } 
    
    /**
     * Retrieves data form the data base 
     * @param node the root node
     * @return the list of children
     */
    public abstract List<T> fetchData(T node);
    
    /**
     * Custom expand listener
     */
    private void addExpandListener_(){
        //we set an isExpaded boolean in case we are updating the node's icon when expands
        expandListener = this.addExpandListener(e -> 
            e.getItems().stream().findFirst().ifPresent(node -> {
                if (dataProvider != null)
                    dataProvider.loadChildrenOf(node); //we should the data provide beacause is level lazy load
                e.getItems().stream().findFirst().get().setExpanded(true);
                refreshNode(node);
            })
        );
    }
    
    /**
     * Populates the navigation tree 
     * @param node the root node of the tree grid
     */
    public void createDataProvider(T node){
        this.getTreeData().clear();
        this.dataProvider = new TreeDataProvider(){};
        this.dataProvider.loadDataProvider(node);
    }
    
    /**
     * When a node is added we update its father
     * @param node the parent node where the new node is been added
     */
    public void update(T node){
        if(dataProvider != null && node != null && this.contains(node)){
            dataProvider.update(node);
            if(getTreeData().contains(node)){
                getDataProvider().refreshItem(node, true);
                if(!isExpanded(node)){//maby its already expanded
                    expandListener.remove();
                    expand_(node);
                    addExpandListener_();
                }
            }
            else
                getDataProvider().refreshAll(); //we are adding to the first level
            
            getElement().executeJs("this.clearCache()");
        }
    }
    
    /**
     * Removes a node from the nav tree it will search the parent of the node 
     * to be remove in order to update it or if has no parent (is null) will 
     * refresh the all the nav tree because there is no other way to refresh 
     * the first level/root nodes
     * @param node the node to be remove
     */
    public void remove(T node){
        if(this.contains(node)){
            //We must check i fwe are on the first level of the tree, in order to update only the first level
            List<T> rootItems = getTreeData().getRootItems();
            boolean isRoot = rootItems.contains(node);
            T parent = null;
            //if is not in the first level we search for the parent to update it
            if(!isRoot)
                parent = getTreeData().getParent(node);

            getTreeData().removeItem(node);
            rawData.remove(node);

            if(isRoot)
                getDataProvider().refreshAll();
            else{
                getDataProvider().refreshItem(parent, true);
                if(getTreeData().getChildren(parent).isEmpty() && isExpanded(node))
                    this.collapse(parent);
            }
            getElement().executeJs("this.clearCache()");
        }
    }
    
    /**
     * Moves a node from one parent to another
     * @param newParentNode of the moved node
     * @param node the moved node
     */
    public void moveNode(T newParentNode, T node){
        remove(node);

        if(dataProvider != null && newParentNode != null && this.contains(newParentNode)){
            dataProvider.update(newParentNode);
            if(getTreeData().contains(newParentNode)){
                getDataProvider().refreshItem(newParentNode, true);
                
                if(!isExpanded(newParentNode)){
                    expandListener.remove();
                    expand_(newParentNode);
                    addExpandListener_();
                }
            }
        }
        getElement().executeJs("this.clearCache()");
    }
    
    /**
     * Copies a node from a parent to another
     * @param newParentNode the new parent of the copied node
     */
    public void copyNode(T newParentNode){
        if(dataProvider != null && newParentNode != null && this.contains(newParentNode)){
            dataProvider.update(newParentNode);
            if(getTreeData().contains(newParentNode)){
                getDataProvider().refreshItem(newParentNode, true);
                
                if(!isExpanded(newParentNode)){
                    expandListener.remove();
                    expand_(newParentNode);
                    addExpandListener_();
                }
            }
        }
        getElement().executeJs("this.clearCache()");
    }
    
    /**
     * If we are expanding a node that is deeper than the two first levels loaded
     * @param node the node to loadChildrenOf
     */
    public void expand_(T node){
        if(dataProvider != null && this.contains(node))
            dataProvider.loadChildrenOf(node);
    }
    
    /**
     * Custom data provider to be used with as a lazy Tree Data
     */
    public abstract class TreeDataProvider {
        private static final int DEFAULT_DEPTH = 2;
        /**
         * Used to load children when a new node is added to the tree
         * @param node parent node where were added the node
         */
        public void update(T node){
            reloadChildren(node);
        }
        /**
         * loads node's children
         * @param node that was expanded
         */
        public void loadChildrenOf(T node){
            TreeDataProvider.this.loadChildren(node, 0);
            NavTreeGrid.this.expand(node);
        }

        public void loadDataProvider(T node){
            loadData(node, 0);
        }

        /**
         * Used when a new child node is added
         * @param node the parent node
         */
        public void reloadChildren(T node){
            List<T> childrenNodes = fetchData(node);
            //We are updating all the children of the parent where we are adding the new node, because there is no other way to keep the nodes sorted
            //so we remove every children node
            for (T c : childrenNodes) {
                if(NavTreeGrid.this.getTreeData().contains(c))
                    NavTreeGrid.this.getTreeData().removeItem(c);
            }
            NavTreeGrid.this.getDataProvider().refreshItem(node, true);
            //and then we readd the nodes
            for(T childNode : childrenNodes){
                if(!NavTreeGrid.this.getTreeData().contains(childNode)){
                    NavTreeGrid.this.getTreeData().addItem(!NavTreeGrid.this.getTreeData().contains(node) ? null : node, childNode);
                    rawData.add(childNode);
                    //we must check if the new added childnode has children e.g. if we are adding a template
                    TreeDataProvider.this.loadChildren(childNode, 0);
                }
            }
        }
        
        /**
         * Expands the node
         * @param node the expanded node
         * @param level the depth of children to be loaded (by default only 2 levels)
         */
        public void loadChildren(T node, int level){
            if(level >= DEFAULT_DEPTH)
                return;
            //we get the children of the given node root
            List<T> childrenNodes = fetchData(node);
            for (T child : childrenNodes) {
                if(!NavTreeGrid.this.getTreeData().contains(child)){
                    level++;
                    NavTreeGrid.this.getTreeData().addItem(node, child); 
                    rawData.add(child);
                }
                TreeDataProvider.this.loadChildren(child, level);
            }
        }
        
        /**
         * Loading data first time, we load two levels
         * @param node the root node of the tree
         * @param level
         */
        public void loadData(T node, int level){
            if(level >= DEFAULT_DEPTH)
                return;
            
            level++;
            //we get the children of the given node root
            List<T> childrenNodes = fetchData(node);
            for (T child : childrenNodes) {
                NavTreeGrid.this.getTreeData().addItem(level == 1 ? null : node, child); 
                rawData.add(child);                    
                loadData(child, level);
            }
        }
    } 
}
