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
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A parser for the output of the command "sh bridge-domain" in the Cisco ASR1002 router series
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class BridgeDomainsASR1002Parser {
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
            String[] lineTokens = line.trim().split(" ");

            if (lineTokens.length > 2 && lineTokens[0].equals("Bridge-domain")) { //NOI18N
                currentBridgeDomain = new BridgeDomain(lineTokens[1]);
                bridgeDomains.add(currentBridgeDomain);
                state = ParsingState.BRIDGE_DOMAIN;
            } else {
                if (state.equals(ParsingState.BRIDGE_DOMAIN) && lineTokens.length == 3 && lineTokens[0].equals("Aging-Timer:")) { //NOI18N
                    state = ParsingState.AGING_TIMER;
                    continue;
                }

                if (state.equals(ParsingState.AGING_TIMER)) {

                    if (lineTokens[0].equals("AED")) {
                        bridgeDomains.add(currentBridgeDomain);
                        state = ParsingState.AED;
                    } else {
                        if (line.contains("ports belonging"))
                            continue;

                        if (lineTokens[0].startsWith("BDI"))
                            currentBridgeDomain.getNetworkInterfaces().add(new NetworkInterface(lineTokens[0], NetworkInterface.TYPE_BDI));

                        else if (lineTokens[0].startsWith("vfi"))
                            currentBridgeDomain.getNetworkInterfaces().add(new NetworkInterface(SyncUtil.normalizeVfiName(lineTokens[1]), NetworkInterface.TYPE_VFI));

                        else if (line.contains("service instance"))
                            currentBridgeDomain.getNetworkInterfaces().add(new NetworkInterface(lineTokens[0] + " " + lineTokens[1] + " " +
                                    lineTokens[2] + " " + lineTokens[3], NetworkInterface.TYPE_SERVICE_INSTANCE));

                        else
                            currentBridgeDomain.getNetworkInterfaces().add(new NetworkInterface(line, NetworkInterface.TYPE_UNKNOWN));
                    }
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
         * Right after a "Bridge-domain XXX (YY ports in all)" section
         */
        BRIDGE_DOMAIN,
        /**
         * Right after the "Aging-Timer: SSSS second(s)" section
         */
        AGING_TIMER,
        /**
         * The header of the table that looks like : AED MAC address    Policy  Tag       Age  Pseudoport
         */
        AED,
        /**
         * After the empty line after listing the interfaces associated to the bridge domain
         */
        END
    }
}