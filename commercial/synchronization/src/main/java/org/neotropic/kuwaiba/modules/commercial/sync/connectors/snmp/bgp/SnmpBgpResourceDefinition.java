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

package org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.bgp;

import org.snmp4j.smi.OID;

import java.util.HashMap;

/**
 * This class contains the oids that will be retrieved from the agent
 * for the BGP addresses synchronization.
 * In this case, the oids correspond to columns in the table
 * mgmt
 * └─mib-2
 * └─bgp
 * └─bgpPeerTable oid(1.3.6.1.2.1.15.3)
 *
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
public class SnmpBgpResourceDefinition extends HashMap<String, OID> {

    public SnmpBgpResourceDefinition() {
        put("bgpPeerIdentifier", new OID("1.3.6.1.2.1.15.3.1.1")); //The BGP Identifier of this entry's BGP peer.
        put("bgpPeerRemoteAs", new OID("1.3.6.1.2.1.15.3.1.9"));   //Remote ASN (remote autonomous system number).
        put("bgpPeerRemoteAddr", new OID("1.3.6.1.2.1.15.3.1.7")); //The remote IP address of this entry's BGP peer.
        put("bgpPeerRemotePort", new OID("1.3.6.1.2.1.15.3.1.8")); //The remote port, not really useful but it is been set as port name in external neighbors
        put("bgpPeerLocalAddr", new OID("1.3.6.1.2.1.15.3.1.5"));  //The local IP address of this entry's BGP connection.
    }
}