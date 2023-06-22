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

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Simple root class for all types of SDH positions. Subclasses will simply
 * overwrite the method toString
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public abstract class AbstractPosition {

    protected int position;
    protected BusinessObjectLight container;

    public AbstractPosition(int position, BusinessObjectLight container) {
        this.position = position;
        this.container = container;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public BusinessObjectLight getContainer() {
        return container;
    }

    public void setContainer(BusinessObjectLight container) {
        this.container = container;
    }
 
    @Override
    public abstract String toString();
}
