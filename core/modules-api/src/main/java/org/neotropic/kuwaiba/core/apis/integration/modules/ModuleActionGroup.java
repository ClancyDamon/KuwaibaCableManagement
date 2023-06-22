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

package org.neotropic.kuwaiba.core.apis.integration.modules;

import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;

/**
 * An action group is typically used in menus. It groups several related actions.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ModuleActionGroup {
    /**
     * A unique identifier of this group. This should be unique even compared to AbstractModuleAction subclasses instances.
     */
    protected String id;
    /**
     * The label that will be displayed.
     */
    protected String displayName;
    /**
     * The actions inside the group. For now, subgroups are not allowed.
     */
    protected List<AbstractVisualAction> actions;

    public ModuleActionGroup(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<AbstractVisualAction> getActions() {
        return actions;
    }

    public void setActions(List<AbstractVisualAction> actions) {
        this.actions = actions;
    }
}
