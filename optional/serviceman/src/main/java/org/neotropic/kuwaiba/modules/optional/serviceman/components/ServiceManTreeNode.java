/*
 * Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.neotropic.kuwaiba.modules.optional.serviceman.components;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * Nodes to display the service manager tree grid explorer
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class ServiceManTreeNode extends AbstractNode<Object> {

    private String id;
    private String name;
    private String className;
    private String description;
    private Icon icon;
    private Image image;
    private boolean customer;
    private boolean service;    
    private boolean resource;    
    private boolean pool;
    private boolean selected;
    private Object object;

    public ServiceManTreeNode(BusinessObjectLight object) {
        super(object);
        this.id = object.getId();
        this.name = object.getName();
        this.className = object.getClassName();
        this.object = object;
        this.customer = false;
        this.service = false;
        this.resource = false;
        this.pool = false;
        this.selected = false;
    }
    
    public ServiceManTreeNode(InventoryObjectPool pool) {
        super(pool);
        this.id = pool.getId();
        this.name = pool.getName();
        this.className = pool.getClassName();
        this.object = pool;
        this.customer = false;
        this.service = false;
        this.resource = false;
        this.pool = true;
        this.selected = false;
    }
    
    public ServiceManTreeNode getThis() {
        return this;
    }
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the service
     */
    public boolean isCustomer() {
        return customer;
    }

    /**
     * @param customer the service to set
     */
    public void setCustomer(boolean customer) {
        this.customer = customer;
    }

    /**
     * @return the object
     */
    public Object getObject() {
        return object;
    }

    /**
     * @param object the object to set
     */
    public void setObject(Object object) {
        this.object = object;
    }
       
    /**
     * @return the service
     */
    public boolean isService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(boolean service) {
        this.service = service;
    }

    /**
     * @return the resource
     */
    public boolean isResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(boolean resource) {
        this.resource = resource;
    }

    /**
     * @return the pool
     */
    public boolean isPool() {
        return pool;
    }

    /**
     * @param pool the pool to set
     */
    public void setPool(boolean pool) {
        this.pool = pool;
    }
    
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }
    
        /**
     * @return the image
     */
    public Image getImage() {
        return image;
    }

    /**
     * @param image the image to set
     */
    public void setImage(Image image) {
        this.image = image;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.getId());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {       
        if (obj == null)
            return false;
        if (!(obj instanceof ServiceManTreeNode))
            return false;
        return (this.getId() == null ? ((ServiceManTreeNode)obj).getId() == null : this.getId().equals(((ServiceManTreeNode)obj).getId()));
    }    

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ServiceManTreeNode{" + "id=" + id + ", name=" + name + ", className=" + className + ", icon=" + icon + ", image=" + image + ", customer=" + customer + ", service=" + service + ", resource=" + resource + ", pool=" + pool + ", object=" + object + '}';
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