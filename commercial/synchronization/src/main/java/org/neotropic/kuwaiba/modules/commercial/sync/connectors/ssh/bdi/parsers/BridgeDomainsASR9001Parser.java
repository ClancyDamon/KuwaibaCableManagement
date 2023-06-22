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
package org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.parsers;

import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.entities.BridgeDomain;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.entities.NetworkInterface;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractDataEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * A parser for the output of the command "sh l2vpn bridge-domain" in the Cisco ASR9001 router series
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class BridgeDomainsASR9001Parser {
    /**
     * Parses the raw input
     *
     * @param input The raw input that corresponds to the output of the command
     * @return The list of bridge domains in the given router (and inside, the related interfaces -VFI, service instances and BDI-)
     */
    public List<AbstractDataEntity> parse(String input) {
        String[] lines = input.split("\n");
        ParsingState state = ParsingState.START;

        BridgeDomain currentBridgeDomain = null;
        List<AbstractDataEntity> bridgeDomains = new ArrayList<>();
        for (String line : lines) {
            String[] lineTokens = line.trim().split(",");

            if (lineTokens.length > 1 && lineTokens[0].startsWith("Bridge group")) { //NOI18N
                currentBridgeDomain = new BridgeDomain(lineTokens[1].trim().split(" ")[1]);
                bridgeDomains.add(currentBridgeDomain);
                state = ParsingState.BRIDGE_GROUP;
            } else {

                if (state.equals(ParsingState.LIST_ACS)) {

                    if (lineTokens[0].startsWith("BV"))
                        currentBridgeDomain.getNetworkInterfaces().add(new NetworkInterface(lineTokens[0], NetworkInterface.TYPE_BDI));

                    else if (lineTokens[0].contains(".")) { //The dot separates the physical interface name and the sub interface id . Remember that split receives a regular expression, and we have to escape the dot as it is a reserved character
                        String[] subinterfaceTokens = lineTokens[0].split("\\.");
                        currentBridgeDomain.getNetworkInterfaces().add(new NetworkInterface(subinterfaceTokens[0] + " " + subinterfaceTokens[1], NetworkInterface.TYPE_GENERIC_SUBINTERFACE));
                    } else
                        currentBridgeDomain.getNetworkInterfaces().add(new NetworkInterface(line, NetworkInterface.TYPE_UNKNOWN));

                    continue;
                }

                if (lineTokens[0].equals("List of ACs:")) { //NOI18N
                    state = ParsingState.LIST_ACS;
                    continue;
                }

                if (lineTokens[0].equals("List of Access PWs:")) { //NOI18N
                    state = ParsingState.LIST_PWS;
                    continue;
                }

                if (lineTokens[0].equals("List of VFIs:")) { //NOI18N
                    state = ParsingState.LIST_VFIS;
                }
            }
        }
        return bridgeDomains;
    }

    /**
     * The possible states of the parsing process
     */
    private enum ParsingState {
        /**
         * The default state
         */
        START,
        /**
         * Since the line starting with "Bridge group:"
         */
        BRIDGE_GROUP,
        /**
         * Right after the "List of ACs:" section
         */
        LIST_ACS,
        /**
         * Right after the "List of Access PWs:" section
         */
        LIST_PWS,
        /**
         * Right after the "List of VFIs:" section
         */
        LIST_VFIS,
        /**
         * After the empty line after listing the interfaces associated to the bridge domain
         */
        END
    }
}