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

import org.snmp4j.smi.OID;

import java.util.HashMap;

/**
 * Oids that will be retrieved from the agent for
 *
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
public class SnmpifXTableResocurceDefinition extends HashMap<String, OID> {

    public SnmpifXTableResocurceDefinition() {
        put("ifName", new OID("1.3.6.1.2.1.31.1.1.1.1"));
        put("ifAlias", new OID("1.3.6.1.2.1.31.1.1.1.18"));
        put("ifHighSpeed", new OID("1.3.6.1.2.1.31.1.1.1.15"));
    }
}