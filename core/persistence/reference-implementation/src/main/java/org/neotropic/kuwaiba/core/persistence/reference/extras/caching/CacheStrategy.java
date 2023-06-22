/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>
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

package org.neotropic.kuwaiba.core.persistence.reference.extras.caching;

/**
 * Implementors implement different algorithms to manage the cache
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public interface CacheStrategy {
    /**
     * Refreshes the cache
     */
    public void refresh();
    /**
     * Fills the cache with initial values
     */
    public void start();
    /**
     * Purges the cache
     */
    public void purge();
    /**
     * Runs the caching strategy
     */
    public void run();
}
