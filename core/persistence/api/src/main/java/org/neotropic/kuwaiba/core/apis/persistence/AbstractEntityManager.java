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

package org.neotropic.kuwaiba.core.apis.persistence;

import java.util.Properties;

/**
 * An entity manager handles the business entities inside the database, that is, performs 
 * queries, creates, relates and deletes objects, etc. There are three types of entities in Kuwaiba: 
 * Application (users, pools, views), Metadata (classes, attributes and their relationships) and 
 * business (inventory objects). Each type has its own manager and all of them must extend 
 * from this interface.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public interface AbstractEntityManager {
    /**
     * This optional method clears and builds the cache components needed in the current entity manager.
     */
    public void initCache();
    /**
     * Configuration variables (usually, yet not necessarily read from a config file) that will be used to process some calls 
     * (for example file paths or constants).
     * @param properties The set of properties. Each EM should document its variables and what are their default values.
     */
    public void setConfiguration(Properties properties);
}
