/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.sync.model;

/**
 * The information from the sync data sources (devices, NMS, third-party databases, etc)
 * id retrieved and stored in high-level data types to be processed later. This is the root class
 * of all these high-level data types.
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractDataEntity {
    /**
     * The name of the entity (a parameter name, for example ifTable or serialNumber)
     */
    protected String name;
    /**
     * The actual value. Mapped to a Java type using the <b>type</b> attribute.
     */
    protected Object value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract DATA_TYPE getType();

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public enum DATA_TYPE {
        STRING,
        INTEGER,
        FLOAT,
        BOOLEAN,
        TABLE,
        JAVA_OBJECT
    }
}
