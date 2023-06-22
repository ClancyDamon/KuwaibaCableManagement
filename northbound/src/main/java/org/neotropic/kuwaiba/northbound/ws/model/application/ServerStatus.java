/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.northbound.ws.model.application;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * It's the public server status
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerStatus implements Serializable {
    /**
     * Up and running like a charm
     */
    public static final int STATUS_UP = 0;
    /**
     * Some slowlessness detected
     */
    public static final int STATUS_SLOWLESSNESS = 1;
    /**
     * The server is down for maintenance
     */
    public static final int STATUS_MAINTENANCE = 2;
    /**
     * The server is down, but the webservice is up
     */
    public static final int STATUS_DOWN = 3;
    /**
     * Server version
     */
    private String version;
    /**
     * Server status (see static fields for possible values)
     */
    private int status;

    //No-arg constructor required
    public ServerStatus() {    }

    public ServerStatus(String serverVersion) {
        this.version = serverVersion;
        this.status = STATUS_UP;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
