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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * All inventory-object related actions from all modules (but most likely in the Navigation module) 
 * that are considered as core actions (i.e. general purpose actions, such as new object or delete object)
 * must be registered here at module startup. Then, the menus will be built using the registered actions 
 * and what kind of inventory actions they are applicable to.
 * There are three types of core actions: Delete actions (the actions intended to delete objects. Most objects will 
 * use the default delete object action implementation, however, some might require a special 
 * procedure -such as physical or logical connections-). The second type are the actions 
 * intended to manage (creating or releasing) special relationships, while the third type 
 * is any other core action, so called, <i>misc</i> core actions. They are kept in separate hashes so they can be displayed or grouped 
 * easily by the renderer.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class CoreActionsRegistry {
    /**
     * The list of registered misc actions.
     */
    private List<AbstractVisualInventoryAction> miscActions;
    /**
     * The list of registered delete actions.
     */
    private List<AbstractVisualInventoryAction> deleteActions;
    /**
     * The list of registered relationship management actions.
     */
    private List<AbstractRelationshipManagementAction> relationshipManagementActions;
    /**
     * All registered misc actions grouped by instances of what class are they applicable to.
     */
    private HashMap<String, List<AbstractVisualInventoryAction>> miscActionsByApplicableClass;
    /**
     * All registered delete actions grouped by instances of what class are they applicable to.
     */
    private HashMap<String, List<AbstractVisualInventoryAction>> deleteActionsByApplicableClass;
    /**
     * All registered relationship management actions grouped by instances of what class are they applicable to.
     */
    private HashMap<String, List<AbstractRelationshipManagementAction>> relationshipManagementActionsByApplicableClass;
    /**
     * All registered actions grouped by the module they are provided by.
     */
    private HashMap<String, List<AbstractVisualInventoryAction>> actionsByModule;
    /**
     * Reference to the MetadataEntityManager to access the data model cache.
     */
    @Autowired
    private MetadataEntityManager mem;
    
    public CoreActionsRegistry() {
        this.miscActions = new ArrayList<>();
        this.deleteActions = new ArrayList<>();
        this.relationshipManagementActions = new ArrayList<>();
        this.miscActionsByApplicableClass = new HashMap<>();
        this.deleteActionsByApplicableClass = new HashMap<>();
        this.relationshipManagementActionsByApplicableClass = new HashMap<>();
        this.actionsByModule = new HashMap<>();
    }
    
    /**
     * Checks what misc actions are associated to a given inventory class. For example, 
     * NewCustomer and ShowReports are part of the returned list if <code>filter</code> is
     * GenericCustomer. Note that the difference between this method and {@link #getMiscActionsApplicableToRecursive(java.lang.String) } is 
     * that this method will return the actions whose appliesTo matches exactly with the provided filter, while the latter 
     * might match even subclasses of the appliesTo return value.
     * @param filter The class to be evaluated.
     * @return The actions that can be executed from an instance of the given class or superclass.
     */
    public List<AbstractVisualInventoryAction> getMiscActionsApplicableTo(String filter) {
        return this.miscActionsByApplicableClass.containsKey(filter) ? this.miscActionsByApplicableClass.get(filter) : new ArrayList<>();
    }
    
    /**
     * Checks what misc actions are associated to a given inventory class. For example, 
     * NewCustomer and ShowReports are part of the returned list if <code>filter</code> is
     * CorporateCustomer.
     * @param filter The class to be evaluated.
     * @return The actions that can be executed from an instance of the given class or superclass.
     */
    public List<AbstractVisualInventoryAction> getActionsApplicableToRecursive(String filter) {
        return this.miscActions.stream().filter((anAction) -> {
            try {
                return anAction.appliesTo() == null ? false : mem.isSubclassOf(filter, anAction.appliesTo());
            } catch (MetadataObjectNotFoundException ex) { // No existing (or cached) classes will be ignored
                return false;
            }
        }).collect(Collectors.toList());
    }
    
    /**
     * Checks what delete actions (there might be more than one, if several delete action implementations are provided) 
     * are associated to a given inventory class. See {@link #getMiscActionsApplicableTo(java.lang.String) } for more 
     * details on its behavior.
     * @param filter The class to be evaluated.
     * @return The delete actions that can be executed from an instance of the given class or superclass.
     */
    public List<AbstractVisualInventoryAction> getDeleteActionsApplicableTo(String filter) {
        return this.deleteActionsByApplicableClass.containsKey(filter) ? this.deleteActionsByApplicableClass.get(filter) : new ArrayList<>();
    }
    
    /**
     * Checks recursively in the class hierarchy what delete actions are associated to a given inventory class. 
     * See {@link #getActionsApplicableToRecursive(java.lang.String) } for more details on its behavior.
     * @param filter The class to be evaluated.
     * @return The delete actions that can be executed from an instance of the given class or superclass.
     */
    public List<AbstractVisualInventoryAction> getDeleteActionsApplicableToRecursive(String filter) {
        return this.deleteActions.stream().filter((anAction) -> {
            try {
                return anAction.appliesTo() == null ? false : mem.isSubclassOf(filter, anAction.appliesTo());
            } catch (MetadataObjectNotFoundException ex) { // No existing (or cached) classes will be ignored
                return false;
            }
        }).collect(Collectors.toList());
    }
    
    /**
     * Checks what relationship management actions are associated to a given inventory class. 
     * See {@link #getMiscActionsApplicableTo(java.lang.String) } for more  details on its behavior.
     * @param filter The class to be evaluated.
     * @return The relationship management actions that can be executed from an instance of the given class or superclass.
     */
    public List<AbstractRelationshipManagementAction> getRelationshipManagementActionsApplicableTo(String filter) {
        return this.relationshipManagementActionsByApplicableClass.containsKey(filter) ? this.relationshipManagementActionsByApplicableClass.get(filter) : new ArrayList<>();
    }
    
    /**
     * Checks recursively in the class hierarchy what relationship management actions are associated to a given inventory class. 
     * See {@link #getActionsApplicableToRecursive(java.lang.String) } for more details on its behavior.
     * @param filter The class to be evaluated.
     * @return The relationship management actions that can be executed from an instance of the given class or superclass.
     */
    public List<AbstractRelationshipManagementAction> getRelationshipManagementActionsApplicableToRecursive(String filter) {
        return this.relationshipManagementActions.stream().filter((anAction) -> {
            try {
                return anAction.appliesTo() == null ? false : mem.isSubclassOf(filter, anAction.appliesTo());
            } catch (MetadataObjectNotFoundException ex) { // No existing (or cached) classes will be ignored
                return false;
            }
        }).collect(Collectors.toList());
    }
    
    
    /**
     * Adds an action to the registry.This method also feeds the action map cache structure, which is a hash map which keys are 
 all the possible super classes the actions are applicable to and the keys are the corresponding actions.
     * @param moduleId The id of the module this action is provided by. The id is returned by AbstractModule.getId().
     * @param action The action to be added. Duplicated action ids are allowed, as long as the duplicate can be used 
     * to overwrite default behaviors, for example, if an object (say a connection) has a specific delete routine  that should 
     * be executed instead of the general purpose delete action, both actions should have the same id, and the renderer should 
     * override the default action with the specific one.
     */
    public void registerAction(String moduleId, AbstractVisualInventoryAction action) {
        this.miscActions.add(action);
        
        if (!this.actionsByModule.containsKey(moduleId))
            this.actionsByModule.put(moduleId, new ArrayList<>());
        this.actionsByModule.get(moduleId).add(action);

        String applicableTo = action.appliesTo() == null ? "" : action.appliesTo(); // Actions not applicable to any particular class are classified under an empty string key
        
        if (action instanceof AbstractDeleteAction) {
            if (!this.deleteActionsByApplicableClass.containsKey(applicableTo))
                this.deleteActionsByApplicableClass.put(applicableTo, new ArrayList<>());

            this.deleteActionsByApplicableClass.get(applicableTo).add(action);
        } else if (action instanceof AbstractRelationshipManagementAction) {
            if (!this.relationshipManagementActionsByApplicableClass.containsKey(applicableTo))
                this.relationshipManagementActionsByApplicableClass.put(applicableTo, new ArrayList<>());

            this.relationshipManagementActionsByApplicableClass.get(applicableTo).add((AbstractRelationshipManagementAction)action);
        } else {
            if (!this.miscActionsByApplicableClass.containsKey(applicableTo))
                this.miscActionsByApplicableClass.put(applicableTo, new ArrayList<>());

            this.miscActionsByApplicableClass.get(applicableTo).add(action);
        }
    }
    
    /**
     * Returns all registered misc actions.
     * @return All registered misc actions.
     */
    public List<AbstractVisualInventoryAction> getMiscActions() {
        return this.miscActions;
    }

    /**
     * Returns all registered delete actions.
     * @return All registered delete actions.
     */
    public List<AbstractVisualInventoryAction> getDeleteActions() {
        return this.deleteActions;
    }
    
    /**
     * Returns all registered relationship management actions.
     * @return All registered relationship management actions.
     */
    public List<AbstractRelationshipManagementAction> getRelationshipManagementActions() {
        return this.relationshipManagementActions;
    }
    
    /**
     * Returns all actions registered by a particular module.
     * @param moduleId The id of the module. Usually the strings that comes from calling AbstractModule.getId().
     * @return The list of actions, even if none registered for the given module (in that case, an empty array will be returned).
     */
    public List<AbstractVisualInventoryAction> getActionsForModule(String moduleId) {
        return this.actionsByModule.containsKey(moduleId) ? this.actionsByModule.get(moduleId) : new ArrayList<>();
    }
}
