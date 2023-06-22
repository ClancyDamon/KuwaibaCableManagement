/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.queries.nodes;

import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;

/**
 * Represents an attributeMetadata object linked to any mxgraphCell
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class ObjectMxNode {

    private String id;
    private AttributeMetadata object;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AttributeMetadata getObject() {
        return object;
    }

    public void setObject(AttributeMetadata object) {
        this.object = object;
    }

    public ObjectMxNode(String id) {
        this.id = id;
    }

    public ObjectMxNode(String id, AttributeMetadata object) {
        this.id = id;
        this.object = object;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof ObjectMxNode))
            return false;
        
        ObjectMxNode theOtherAttribute = (ObjectMxNode)obj;
        //null checks are avoided here because the attribute name can not be null
    
         return (this.getId() == null ? ((ObjectMxNode)obj).getId() == null : this.getId().equals(((ObjectMxNode)obj).getId()));


    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.id);
        return hash;
    }
    
    
}
