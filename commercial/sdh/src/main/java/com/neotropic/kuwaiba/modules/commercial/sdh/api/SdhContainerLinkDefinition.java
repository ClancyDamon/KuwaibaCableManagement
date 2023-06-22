/*
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

import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Instances of this class define a container
 * @author Charles Edward Bedon Cortazar{@literal <charles.bedon@kuwaiba.org>}
 */
public class SdhContainerLinkDefinition {
    /**
     * Container object
     */
    private BusinessObjectLight container;

    /**
     * Is this container structured?
     */
    private boolean structured;
    /**
     * The positions used by the container
     */
    private List<SdhPosition> positions;

    public SdhContainerLinkDefinition(BusinessObjectLight container, boolean structured, List<SdhPosition> positions) {
        this.container = container;
        this.structured = structured;
        this.positions = positions;
    }       

    public BusinessObjectLight getContainerName() {
        return container;
    }

    public void setContainerName(BusinessObjectLight container) {
        this.container = container;
    }

    public List<SdhPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<SdhPosition> positions) {
        this.positions = positions;
    }

    public BusinessObjectLight getContainer() {
        return container;
    }

    public boolean isStructured() {
        return structured;
    }        
}
