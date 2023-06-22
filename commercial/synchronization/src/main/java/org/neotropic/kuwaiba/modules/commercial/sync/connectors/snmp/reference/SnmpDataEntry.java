/*
 * Copyright 2023-2023. Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.reference;

import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractDataEntity;

import java.util.HashMap;
import java.util.List;

/**
 * SNMP data entry configuration
 *
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
public class SnmpDataEntry extends AbstractDataEntity {

    public SnmpDataEntry(String name, HashMap<String, List<String>> value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.TABLE;
    }
}

