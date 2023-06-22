/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.util.visual.tree.nodes;

import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;

/**
 * A node that represents a business domain object from the model.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <T> The type of the business object
  */
@RequiredArgsConstructor
public abstract class AbstractNode<T> {
    /**
     * Business object behind this node (model)
     */
    protected @Getter final T object;
    /**
     * Business object id
     */
    protected @Getter @Setter String id;
    /**
     * Business object class name
     */
    protected @Getter @Setter String className;
    /**
     * Node's displayName. If null, the toString method of the business object will be used
     */
    protected @Getter @Setter String displayName;
    /**
     * Business object name
     */
    protected @Getter @Setter String name;
    /*
     * if the current node is selected 
     */
    protected @Getter @Setter boolean selected; 
    /*
     * if the current node is selected 
     */
    protected @Getter @Setter boolean expanded; 
    /**
     * Used in nodes that use static images
     */
    protected @Getter @Setter String iconUrl;
   
    /**
     * Actions associated to this node
     * @return An array of actions
     */
    public abstract AbstractAction[] getActions();
    
    /**
     * What to do when commanded to refresh the node.
     * @param recursive Refresh the children nodes.
     */
    public abstract void refresh(boolean recursive);
     
    @Override
    public String toString() {
        return displayName == null ? object.toString() : displayName;
    }
   
    @Override
    public boolean equals(Object obj){
       if(obj == null)
           return false;
       if (!(obj instanceof AbstractNode))
           return false;
       return (this.getId() == null ? ((AbstractNode)obj).getId() == null : this.getId().equals(((AbstractNode)obj).getId()));
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.object);
        return hash;
    }
}