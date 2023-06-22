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

package org.neotropic.kuwaiba.core.apis.integration.modules.actions;

/**
 * Visual actions that are provided by each module (that is, all those actions that are loaded 
 * dynamically and are defined outside the module Navigation) must extend from this class and be 
 * registered at module startup.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractVisualAdvancedAction extends AbstractVisualInventoryAction {

    public AbstractVisualAdvancedAction(String moduleId) {
        super(moduleId);
    }
    /**
     * Some actions can be reused as contextual actions in different modules (managing connections, creating/deleting objects, etc), while 
     * others have use only within the very module, such as saving views or displaying information concerning the current screen. The 
     * former as shown in elements like <code>ObjectOptionsPanel</code> or in dynamically created menus, while the latter are only used in 
     * the UIs particular to the given module.
     * @return Whether the action is reusable or not. By default, all actions are reusable.
     */
    public boolean isReusable() {
        return true;
    }
}
