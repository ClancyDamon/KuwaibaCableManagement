/**
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
package org.neotropic.kuwaiba.northbound.ws.model.business.modules.sdh;

import com.neotropic.kuwaiba.modules.commercial.sdh.api.SdhContainerLinkDefinition;
import com.neotropic.kuwaiba.modules.commercial.sdh.api.SdhPosition;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteObjectLight;

/**
 * Wrapper of {@link SDHContainerLinkDefinition}
 * @author Charles Edward Bedon Cortazar{@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteSDHContainerLinkDefinition implements Serializable {
    /**
     * Container object
     */
    private RemoteObjectLight container;

    /**
     * Is this container structured?
     */
    private boolean structured;
    /**
     * The positions used by the container
     */
    private List<RemoteSDHPosition> positions;

    public RemoteSDHContainerLinkDefinition() { } //Required by JAX-WS

    public RemoteSDHContainerLinkDefinition(SdhContainerLinkDefinition containerLinkDefinition) {
        this.container = new RemoteObjectLight(containerLinkDefinition.getContainer());
        this.structured = containerLinkDefinition.isStructured();
        this.positions = new ArrayList<>();
        for (SdhPosition position : containerLinkDefinition.getPositions())
            positions.add(new RemoteSDHPosition(position));
    }
    
    public RemoteSDHContainerLinkDefinition(RemoteObjectLight container, boolean structured, List<RemoteSDHPosition> positions) {
        this.container = container;
        this.structured = structured;
        this.positions = positions;
    }       

    public RemoteObjectLight getContainerName() {
        return container;
    }

    public void setContainerName(RemoteObjectLight container) {
        this.container = container;
    }

    public List<RemoteSDHPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<RemoteSDHPosition> positions) {
        this.positions = positions;
    }

    public RemoteObjectLight getContainer() {
        return container;
    }

    public boolean isStructured() {
        return structured;
    }
}
