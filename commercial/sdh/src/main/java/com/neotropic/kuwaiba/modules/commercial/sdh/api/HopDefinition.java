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
 * A class defining a hop in a possible route for a virtual circuit
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class HopDefinition {

    BusinessObjectLight link;
    int position;

    public HopDefinition(BusinessObjectLight link) {
        this.link = link;
        this.position = -1; //The default position is unset
    }

    public BusinessObjectLight getLink() {
        return link;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    } 
    
    @Override
    public String toString() {
        return link + " - " + (position == -1 ? "NA" : position); //NOI18N
    }
}
