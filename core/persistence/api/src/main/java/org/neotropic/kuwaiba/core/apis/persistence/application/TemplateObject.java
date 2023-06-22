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

import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;

/**
 * Instances of this class represent an inventory object defined within a template. 
 * This is actually very similar, if not identical, to #{@link BusinessObject}, the only 
 * difference is that these objects don't exist, they are used as templates to create actual inventory objects. Also,
 * validators are not supported
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class TemplateObject extends TemplateObjectLight {
    /**
     * Map of attributes and values. Multiple selection list types are represented by comma (",") separated long values. These values
     * are the ids of the list types the object is referring to
     */
    private HashMap <String, String> attributes;
    
    public TemplateObject(String className, String id, String name) {
        super(className, id, name);
        this.attributes = new HashMap<>();
    }
    
    public TemplateObject(String className, String id, String name, HashMap<String, String> attributes) {
        super(className, id, name);
        this.attributes = attributes;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }
}
