/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.visualization.api;

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;

/**
 * An edge that represents a business, inventory object.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class BusinessObjectViewEdge extends AbstractViewEdge<BusinessObjectLight> {

    public BusinessObjectViewEdge(BusinessObjectLight identifier) {
        super(identifier);
    }
    
    @Override
    public boolean equals(Object obj) {
        //A node can be matched using an instance of the its identifier, or simply its id
        if (obj instanceof String)
            return getIdentifier().getId().equals(obj);
        
        if (obj instanceof BusinessObjectLight)
            return getIdentifier().equals(obj);
        
        if (obj instanceof AbstractViewEdge)
            return getIdentifier().equals(((AbstractViewEdge)obj).getIdentifier());
        
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
    
    @Override
    public String toString() {
        return getIdentifier().toString();
    }
}
