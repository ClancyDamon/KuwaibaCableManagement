/**
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.commercial.sdh.api;

import java.io.Serializable;

/**
 * It's a simple class representing a single position used by a container within a transport link
 * @author Charles Edward Bedon Cortazar{@literal <charles.bedon@kuwaiba.org>}
 */
public class SdhPosition implements Serializable {
    /**
     * Id of the connection being used (a TransportLink or a ContainerLink)
     */
    private String connectionId;
    /**
     * Id of the connection being used (a TransportLink or a ContainerLink)
     */
    private String connectionClass;
    /**
     * Actual position (STM timeslot or VC4 timeslot)
     */
    private int position;

    public SdhPosition(String connectionClass, String connectionId, int position) {
        this.connectionId = connectionId;
        this.connectionClass = connectionClass;
        this.position = position;
    }

    public String getLinkId() {
        return connectionId;
    }

    public void setLinkId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getLinkClass() {
        return connectionClass;
    }

    public void setLinkClass(String linkClass) {
        this.connectionClass = linkClass;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
