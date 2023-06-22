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
 * Contains the OIDs that will be retrieved from the agent for this reference implementation.
 * In this case, the OIDs correspond to columns in the tables  entPhysicalTable (branch 1.3.6.1.2.1.47.1.1.1.)
 *
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
public class ReferenceSnmpEntPhysicalTableResourceDefinition extends HashMap<String, OID> {

    public ReferenceSnmpEntPhysicalTableResourceDefinition() {
        put("entPhysicalDescr", new OID("1.3.6.1.2.1.47.1.1.1.1.2")); //NOI18N
        put("entPhysicalClass", new OID("1.3.6.1.2.1.47.1.1.1.1.5")); //NOI18N
        put("entPhysicalContainedIn", new OID("1.3.6.1.2.1.47.1.1.1.1.4")); //NOI18N
        put("entPhysicalName", new OID("1.3.6.1.2.1.47.1.1.1.1.7")); //NOI18N
        put("entPhysicalHardwareRev", new OID("1.3.6.1.2.1.47.1.1.1.1.8")); //NOI18N
        put("entPhysicalFirmwareRev", new OID("1.3.6.1.2.1.47.1.1.1.1.9")); //NOI18N
        put("entPhysicalSerialNum", new OID("1.3.6.1.2.1.47.1.1.1.1.11")); //NOI18N
        put("entPhysicalMfgName", new OID("1.3.6.1.2.1.47.1.1.1.1.12")); //NOI18N
        put("entPhysicalModelName", new OID("1.3.6.1.2.1.47.1.1.1.1.13")); //NOI18N
    }
}
