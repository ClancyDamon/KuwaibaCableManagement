/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.core.apis.persistence.business;

import java.util.Map;

/**
 * When an object is related to another using a special relationship, the other side of the relationship can be retrieved
 * using the method BusinessEntityManager.getSpecialAttribute or BusinessEntityManager.getSpecialAttributes. However, in some cases, 
 * the relationship itself has its own properties, and it's called an annotated relationship. In order to be able to retrieve not only the 
 * other side of the relationship, but also the annotated property, use this type.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class AnnotatedBusinessObjectLight {
    /**
     * The other side of the special relationship
     */
    private BusinessObjectLight object;
    /**
     * The properties of the annotated relationship
     */
    private Map<String, Object> properties;

    public AnnotatedBusinessObjectLight(BusinessObjectLight theObject, Map<String, Object> properties) {
        this.object = theObject;
        this.properties = properties;
    }

    public BusinessObjectLight getObject() {
        return object;
    }

    public void setObject(BusinessObjectLight theObject) {
        this.object = theObject;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
