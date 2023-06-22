/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.queries.nodes;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class ObjectMxNodeMap {
    private ObjectMxNodeMap parent;
    private ObjectMxNode object;
    private List<ObjectMxNode> childrens;
    private List<ObjectMxNodeMap> joins;

    public ObjectMxNodeMap() {
        childrens = new ArrayList<>();
        joins = new ArrayList<>();
    }
  
    public ObjectMxNodeMap getParent() {
        return parent;
    }

    public void setParent(ObjectMxNodeMap parent) {
        this.parent = parent;
    }

    public List<ObjectMxNode> getChildrens() {
        return childrens;
    }

    public void setChildrens(List<ObjectMxNode> childrens) {
        this.childrens = childrens;
    }

    public ObjectMxNode getObject() {
        return object;
    }

    public void setObject(ObjectMxNode object) {
        this.object = object;
    }

    public List<ObjectMxNodeMap> getJoins() {
        return joins;
    }

    public void setJoins(List<ObjectMxNodeMap> joins) {
        this.joins = joins;
    }
          
}
