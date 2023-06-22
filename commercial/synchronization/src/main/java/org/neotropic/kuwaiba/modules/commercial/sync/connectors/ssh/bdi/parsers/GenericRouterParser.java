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

import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.entities.Router;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractDataEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * A parser for the output of the command "sh bridge-domain" in the Cisco ME3600 router series
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class GenericRouterParser {
    /**
     * Parses the raw input
     *
     * @param input The raw input that corresponds to the output of the command
     * @return The list of bridge domains in the given router (and inside, the related interfaces -VFI, service instances and BDI-)
     */
    public List<AbstractDataEntity> parse(String input) {
        String[] lines = input.split("\n");
        ParsingState state = ParsingState.START;

        Router currentRouter = null;
        List<AbstractDataEntity> routers = new ArrayList<>();
        for (String line : lines) {
            String[] lineTokens = line.trim().split(" ");

            currentRouter = new Router(lineTokens[0]);
            routers.add(currentRouter);
        }
        return routers;
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
         * Right after the "Maximum address limit: MMMM" section
         */
        MAXIMUM_ADDRESS_LIMIT,
        /**
         * After the empty line after listing the interfaces associated to the bridge domain
         */
        END
    }
}