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

import java.util.Objects;
import java.util.Properties;

/**
 * The super class of all classes used to represent nodes inside views.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <N> The type of the business object represented by this node.
 */
public abstract class AbstractViewNode<N> {
    /**
     * The object behind the edge is know as its id and should be unique among the existing edges.
     */
    private N identifier;
    /**
     * Properties associated to the edge. These properties can be used, among other things, to render it.
     */
    private Properties properties;
    
    public AbstractViewNode(N identifier) {
        this.identifier = identifier;
        this.properties = new Properties();
    }
    
    public N getIdentifier() {
        return identifier;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractViewNode))
            return false;
        
        return identifier.equals(((AbstractViewNode)obj).getIdentifier());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.identifier);
        return hash;
    }
}