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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * All actions aimed to relate two or more inventory objects or to release the relationships 
 * between them should be registered in the respective registries offered by this service. 
 * The actions will be placed in separate categories in menus and toolbars. 
 * Actions registered here <b>do not</b> need to also be registered in the advanced or core actions registries.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class RelationshipManagementRegistry {
    /**
     * The list of registered relate-to actions.
     */
    private List<AbstractVisualAdvancedAction> relateToActions;
    /**
     * The list of registered release-from actions.
     */
    private List<AbstractVisualAdvancedAction> releaseFromActions;
    /**
     * All registered relate-to actions grouped by instances of what class are they applicable to.
     */
    private HashMap<String, List<AbstractVisualAdvancedAction>> relateToActionsByApplicableClass;
    /**
     * All registered release-from actions grouped by instances of what class are they applicable to.
     */
    private HashMap<String, List<AbstractVisualAdvancedAction>> releaseFromActionsByApplicableClass;
    /**
     * All registered actions grouped by the module they are provided by.
     */
    private HashMap<String, List<AbstractVisualAdvancedAction>> actionsByModule;
    /**
     * Reference to the MetadataEntityManager to access the data model cache.
     */
    @Autowired
    private MetadataEntityManager mem;

    public RelationshipManagementRegistry() {
        this.relateToActions = new ArrayList<>();
        this.releaseFromActions = new ArrayList<>();
        this.relateToActionsByApplicableClass = new HashMap<>();
        this.releaseFromActionsByApplicableClass = new HashMap<>();
        this.actionsByModule = new HashMap<>();
    }
    
    /**
     * Checks what actions are associated to a given inventory class.For example, 
 NewCustomer and DeleteCustomer are part of the returned list if <code>filter</code> is
 GenericCustomer. Note that the difference between this method and {@link #getActionsApplicableToRecursive(java.lang.String) } is 
     * that this method will return the actions whose appliesTo matches exactly with the provided filter, while the latter 
     * might match even subclasses of the appliesTo return value.
     * @param filter The class to be evaluated.
     * @param includeNonReusable Should the method include the reusable actions? See {@link AbstractVisualAdvancedAction#isReusable() }.
     * for more details on reusable actions.
     * @return The actions that can be executed from an instance of the given class or superclass.
     */
    public List<AbstractVisualAdvancedAction> getRelateToActionsApplicableTo(String filter, boolean includeNonReusable) {
        if (this.relateToActionsByApplicableClass.containsKey(filter)) 
            return this.relateToActionsByApplicableClass.get(filter).stream().filter( anAction -> anAction.isReusable() == !includeNonReusable).collect(Collectors.toList());
        else 
            return Collections.EMPTY_LIST;
    }
    
    public List<AbstractVisualAdvancedAction> getReleaseFromActionsApplicableTo(String filter, boolean includeNonReusable) {
        if (this.releaseFromActionsByApplicableClass.containsKey(filter)) 
            return this.releaseFromActionsByApplicableClass.get(filter).stream().filter( anAction -> anAction.isReusable() == !includeNonReusable).collect(Collectors.toList());
        else 
            return Collections.EMPTY_LIST;
    }
    
    /**
     * Checks what actions are associated to a given inventory class. For example, 
     * NewCustomer and DeleteCustomer are part of the returned list if <code>filter</code> is
     * CorporateCustomer.
     * @param filter The class to be evaluated.
     * @param includeNonReusable Should the method include the reusable actions? See {@link AbstractVisualAdvancedAction#isReusable() }.
     * @return The actions that can be executed from an instance of the given class or superclass.
     */
    public List<AbstractVisualAdvancedAction> getRelateToActionsApplicableToRecursive(String filter, boolean includeNonReusable) {
        return this.relateToActions.stream().filter((anAction) -> {
            try {
                return anAction.appliesTo() == null ? false : (includeNonReusable || anAction.isReusable()) && mem.isSubclassOf(anAction.appliesTo(), filter);
            } catch (MetadataObjectNotFoundException ex) { // No existing (or cached) classes will be ignored
                return false;
            }
        }).collect(Collectors.toList());
    }
    
    public List<AbstractVisualAdvancedAction> getReleaseFromActionsApplicableToRecursive(String filter, boolean includeNonReusable) {
        return this.releaseFromActions.stream().filter((anAction) -> {
            try {
                return anAction.appliesTo() == null ? false : (includeNonReusable || anAction.isReusable()) && mem.isSubclassOf(anAction.appliesTo(), filter);
            } catch (MetadataObjectNotFoundException ex) { // No existing (or cached) classes will be ignored
                return false;
            }
        }).collect(Collectors.toList());
    }
    
    /**
     * Adds an action to the registry.This method also feeds the action map cache structure, which is a hash map whose keys are 
     * all the possible super classes the actions are applicable to and the keys are the corresponding actions.
     * @param moduleId The id of the module this action is provided by. The id is returned by AbstractModule.getId().
     * @param action The action to be added. Duplicated action ids are allowed, as long as the duplicate can be used 
     * to overwrite default behaviors, for example, if an object (say a connection) has a specific delete routine  that should 
     * be executed instead of the general purpose delete action, both actions should have the same id, and the renderer should 
     * override the default action with the specific one.
     */
    public void registerRelateToAction(String moduleId, AbstractVisualAdvancedAction action) {
        this.relateToActions.add(action);
        
        if (!this.actionsByModule.containsKey(moduleId))
            this.actionsByModule.put(moduleId, new ArrayList<>());
        this.actionsByModule.get(moduleId).add(action);

        String applicableTo = action.appliesTo() == null ? "" : action.appliesTo(); // Actions not applicable to any particular class are classified under an empty string key
        
        if (!this.relateToActionsByApplicableClass.containsKey(applicableTo))
            this.relateToActionsByApplicableClass.put(applicableTo, new ArrayList<>());
        
        this.relateToActionsByApplicableClass.get(applicableTo).add(action);
    }
    
    public void registerReleaseFromAction(String moduleId, AbstractVisualAdvancedAction action) {
        this.releaseFromActions.add(action);
        
        if (!this.actionsByModule.containsKey(moduleId))
            this.actionsByModule.put(moduleId, new ArrayList<>());
        this.actionsByModule.get(moduleId).add(action);

        String applicableTo = action.appliesTo() == null ? "" : action.appliesTo(); // Actions not applicable to any particular class are classified under an empty string key
        
        if (!this.releaseFromActionsByApplicableClass.containsKey(applicableTo))
            this.releaseFromActionsByApplicableClass.put(applicableTo, new ArrayList<>());
        
        this.releaseFromActionsByApplicableClass.get(applicableTo).add(action);
    }
}
