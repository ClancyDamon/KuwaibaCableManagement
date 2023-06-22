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

package org.neotropic.kuwaiba.core.apis.persistence.application;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Instances of this class represent a simplified representation of an inventory object defined within a template. 
 * This is actually very similar, if not identical, to #{@link BusinessObjectLight}, the only 
 * difference is that these objects don't exist, they are used as templates to create actual inventory objects. Also,
 * validators are not supported
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Getter @Setter
@AllArgsConstructor
public class TemplateObjectLight implements Comparable<TemplateObjectLight> {
    /**
     * The class the object is instance of
     */
    private String className;
    /**
     * The id of the template object
     */
    private String id;
    /**
     * The name of the template object
     */
    private String name;
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.id);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj){
       if(obj == null)
           return false;
       if (!(obj instanceof TemplateObjectLight))
           return false;
       return (this.getId() == null ? ((TemplateObjectLight) obj).getId() == null : this.getId().equals(((TemplateObjectLight) obj).getId()));
    }
    
    @Override
    public int compareTo(TemplateObjectLight o) {
        return o.getName().compareTo(name);
    }

    @Override
    public String toString() {
        return getName() + " [" + getClassName() + "]";
    }
}