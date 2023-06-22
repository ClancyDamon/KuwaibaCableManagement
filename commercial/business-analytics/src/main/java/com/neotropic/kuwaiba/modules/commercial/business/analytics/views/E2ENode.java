/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package com.neotropic.kuwaiba.modules.commercial.business.analytics.views;

import java.util.Objects;
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Represents a obj in the end to end view
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class E2ENode {
    private BusinessObjectLight obj;
    private Properties properties;

    public E2ENode(BusinessObjectLight node) {
        this.obj = node;
        properties = new Properties();
    }

    public BusinessObjectLight getBussinesObject() {
        return obj;
    }

    public void setBusinessObject(BusinessObjectLight obj) {
        this.obj = obj;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
     
    @Override
    public boolean equals(Object obj_) {
        if (!(obj_ instanceof E2ENode))
            return false;
        
        return obj.equals(((E2ENode)obj_).getBussinesObject());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.obj);
        return hash;
    }
    
}
