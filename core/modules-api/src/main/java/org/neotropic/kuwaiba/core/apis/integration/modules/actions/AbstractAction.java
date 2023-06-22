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

import com.vaadin.flow.component.icon.Icon;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;

/**
 * All actions in a module must extend from this class, no matter if the are business, application 
 * or metadata related.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractAction {
    /**
     * A unique identifier for the action within the module .
     */
    protected String id;
    /**
     * The label to be used in menus or buttons.
     */
    protected String displayName;
    /**
     * A short description of what the action does, mainly to be used as tool text tip.
     */
    protected String description;
    /**
     * An icon to represent the action.
     */
    protected Icon icon;
    /**
     * This number will be used to position the action in menus (0 is the highest priority/importance). The default value is 1000.
     */
    protected int order;
    /**
     * What is to be execute once the action is triggered.
     */
    protected ModuleActionCallback callback;
  
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public void setCallback(ModuleActionCallback callback) {
        this.callback = callback;
    }

    public ModuleActionCallback getCallback() {
        return callback;
    }
    
    /**
     * Returns the privilege needed to execute the current action. This access level should be matched with the permissions granted 
     * to the user and the renderer must decide if it will gray-out the action or simply not show it.
     * @return The access level needed to execute this action. See {@link Privilege}.ACCESS_LEVEL_XXX for possible values. 
     */
    public abstract int getRequiredAccessLevel();
    
    /**
     * Does this action requires confirmation before executing it? This is typically true 
     * for actions that will delete or otherwise dispose of some resources. The consumer of the action 
     * might ignore this flag and simply execute the action. 
     * @return 
     */
    public abstract boolean requiresConfirmation();
    
    /**
     * A functional interface callback code to be executed once the action is triggered.
     */
    public interface ModuleActionCallback {
        public ActionResponse execute(ModuleActionParameterSet parameters) throws ModuleActionException;
    }
}