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
package org.neotropic.kuwaiba.core.apis.integration.miniapps;

/**
 * A listener to be implemented by those using mini applications. The idea is that once the mini application ends its task, the interested actors are notified accordingly
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public interface MiniApplicationEventListener {
    /**
     * To be called once the mini application completes its execution
     * @param event The type of event. See enumeration MiniApplicationEvent for possible types
     */
    public void doOnEvent(MiniApplicationEvent event);
    
    /**
     * The different types of mini application events
     */
    public enum MiniApplicationEvent {
        /**
         * The application has just been launched
         */
        EVENT_APP_LAUNCHED,
        /**
         * Raised when data was changed on the mini application
         */
        EVENT_DATA_CHANGED,
        /**
         * The application was closed without ending its task or doing anything (like using a CLOSE button)
         */
        EVENT_APP_ABORTED,
        /**
         * The application was closed after performing its task (Like clicking on a SAVE button)
         */
        EVENT_APP_ENDED
    }
}