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
package org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.entities;

import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractDataEntity;

/**
 * An entity class that represents a network interface that can be used in a bridge domain
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class NetworkInterface extends AbstractDataEntity {
    /**
     * If it's not possible to determine the type of interface associated to the bridge domain
     */
    public static final int TYPE_UNKNOWN = 1;
    /**
     * If the interface is a bridge domain interface
     */
    public static final int TYPE_BDI = 2;
    /**
     * If the interface is a service instance
     */
    public static final int TYPE_SERVICE_INSTANCE = 3;
    /**
     * If the interface is a VFI
     */
    public static final int TYPE_VFI = 4;
    /**
     * If the interface is a generic subinterface (mapped as a VirtualPort)
     */
    public static final int TYPE_GENERIC_SUBINTERFACE = 5;
    /**
     * Interface type. See possible values in TYPE_*
     */
    private int networkInterfaceType;

    public NetworkInterface(String name, int networkInterfaceType) {
        this.name = name;
        this.networkInterfaceType = networkInterfaceType;
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.JAVA_OBJECT;
    }

    public int getNetworkInterfaceType() {
        return networkInterfaceType;
    }

    public void setNetworkInterfaceType(int networkInterfaceType) {
        this.networkInterfaceType = networkInterfaceType;
    }
}