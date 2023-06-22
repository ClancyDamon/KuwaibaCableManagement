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

package org.neotropic.kuwaiba.core.apis.integration.dashboards;

/**
 * Implementors of this interface listen for selection events on lists, views and explorers
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <O> The expected type of the selected object.
 */
public interface SelectionListener<O> {
    /**
     * Callback method invoked when the selection event occurs.
     * @param source The component that caused the event.
     * @param selectedObjects The objects in the selection event. It might be 0 if 
     * the user clear the selection.
     */
    public void selectionCompleted(Object source, O... selectedObjects);
}
