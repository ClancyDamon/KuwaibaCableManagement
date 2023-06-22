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
package org.neotropic.kuwaiba.core.apis.integration.views;

/**
 * Interface to be implemented by those interested in receiving notifications about events related to views.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public interface ViewEventListener {
    /**
     * The callback method used to notify when a view event has occurred.
     * @param source
     * @param type 
     */
    public void eventProcessed(Object source, EventType type);
    
    public enum EventType {
        /**
         * A simple click event (selection).
         */
        TYPE_CLICK,
        /**
         * A simple right-click event (to be used to display context actions, mostly).
         */
        TYPE_RIGHTCLICK
    }
}