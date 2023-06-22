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
 * A class representing a timeslot in a ContainerLink
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class AvailableContainerLinkPosition extends AbstractPosition {

    public AvailableContainerLinkPosition(int position, BusinessObjectLight container) {
        super(position, container);
    }

    @Override
    public String toString() {
        return String.format("%s - %s", asKLM(), container == null ? "Free" : container.getName());
    }

    private String asKLM() {
        int k, l, m;

        if (position % 21 == 0) {
            k = position / 21;
            l = 7;
            m = 3;
        } else {
            k = (position / 21) + 1;
            if ((position % 21) % 3 == 0) {
                l = (position % 21) / 3;
            } else {
                l = (position % 21) / 3 + 1;
            }

            if ((position % 21) % 3 == 0) {
                m = 3;
            } else {
                m = (position % 21) % 3;
            }
        }

        return String.format("%s [%s - %s - %s]", position, k, l, m);
    }
}
