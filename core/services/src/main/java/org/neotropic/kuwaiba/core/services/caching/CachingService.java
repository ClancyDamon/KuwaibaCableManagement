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

package org.neotropic.kuwaiba.core.services.caching;

import org.neotropic.kuwaiba.core.services.scheduling.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A service that manages the caching strategies. Most cached data is related to the 
 * data model and is updated only when the data model itself is changed, but there's also 
 * session, process or synchronization related data, which is updated following different 
 * criteria, like timeout or fixed schedules.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class CachingService {
    @Autowired
    private SchedulingService schedulingService;
}
