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
package org.neotropic.kuwaiba.modules.optional.pools.nodes;

import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * Nodes to display the pools module tree grid explorer.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>}
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class PoolObjectNode extends AbstractNode<Object>{

    private String id;
    private String name;
    private String className;
    private String description;
    private boolean pool;
    private Object object;
    
    public PoolObjectNode(BusinessObjectLight object) {
        super(object);
        this.id = object.getId();
        this.name = object.getName();
        this.className = (object.getClassDisplayName() != null && !object.getClassDisplayName().isEmpty()) ? object.getClassDisplayName() : object.getClassName();
        this.object = object;
        this.pool = false;
    }
    
    public PoolObjectNode(BusinessObjectLight object, String displayName) {
        super(object);
        this.id = object.getId();
        this.name = object.getName();
        this.displayName = displayName;
        this.className = (object.getClassDisplayName() != null && !object.getClassDisplayName().isEmpty()) ? object.getClassDisplayName() : object.getClassName();
        this.object = object;
        this.pool = false;
    }
    
    public PoolObjectNode(InventoryObjectPool pool) {
        super(pool);
        this.id = pool.getId();
        this.name = pool.getName();
        this.className = pool.getClassName();
        this.object = pool;
        this.pool = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPool() {
        return pool;
    }

    public void setPool(boolean pool) {
        this.pool = pool;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
    
    @Override
    public AbstractAction[] getActions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(boolean recursive) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }   
}