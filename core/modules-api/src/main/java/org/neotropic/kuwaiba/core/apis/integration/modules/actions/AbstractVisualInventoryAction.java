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
 * Actions applicable to inventory objects must inherit from this class.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractVisualInventoryAction extends AbstractVisualAction {
    
    public static final int SELECTION_NO_OBJECTS = 0;
    public static final int SELECTION_ANY_OBJECTS = -1;

    public AbstractVisualInventoryAction(String moduleId) {
        super(moduleId);
    }
    /**
     * Tells the system what inventory objects support said action. Superclasses are also allowed. 
     * For example, returning GenericCommunicationsElement applies to all devices such as routers, multiplexers or switches, 
     * while InventoryObject will apply to any object.
     * @return The class or superclass whose instances the action applies to. If null, this indicate that while the action is 
     * related to inventory, it doesn't apply to a particular type, for example, when creating a Customer pool in the service manager 
     * (or any root pool, for that matter). By default, the action will apply to all instances.
     */
    public String appliesTo() {
        return null;
    }
    /**
     * How many objects have to be selected so this action is enabled. Use -1 for any ({@value AbstractVisualInventoryAction#SELECTION_ANY_OBJECTS}), 
     * 0 if no objects have to be selected ({@value AbstractVisualInventoryAction#SELECTION_NO_OBJECTS}), or a number greater than 0 for a fixed number.
     * @return The number of objects that have to be selected so the action is enabled.
     */
    public abstract int getRequiredSelectedObjects();
    
    public String getName() {
        return getModuleAction() == null ? "" : getModuleAction().getDisplayName();
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
