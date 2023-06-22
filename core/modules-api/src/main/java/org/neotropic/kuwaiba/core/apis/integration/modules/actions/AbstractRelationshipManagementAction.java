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

import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Former Relate to.../Release from... actions (used in complex models to create/remove relationships 
 * between inventory objects) should now handled using a single action, subclass of the present class. 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractRelationshipManagementAction extends AbstractVisualInventoryAction {
    
    public AbstractRelationshipManagementAction(String moduleId) {
        super(moduleId);
    }
    
    /**
     * Returns the type of inventory objects that can be related through the given relationship to 
     * the selected objects.
     * @return The type (it can be an abstract class)
     */
    public abstract String getTargetObjectClass();
    /**
     * Gets the actual name of the relationship in the database.
     * @return Said relationship name. 
     */
    public abstract String getRelationshipName();
    /**
     * The display name that should be used to show the relationship between the 
     * target object(s) and the selected object(s).
     * @return Said display name.
     */
    public abstract String getIncomingRelationshipDisplayName();
    /**
     * The display name that should be used to show the relationship between the 
     * selected object(s) and the target object(s).
     * @return Said display name.
     */
    public abstract String getOutgoingRelationshipDisplayName();
    /**
     * Indicates the cardinality of the relationship. See {@link RelationshipCardinality} for possible 
     * values.
     * @return Said cardinality.
     */
    public abstract RelationshipCardinality getCardinality();
    /**
     * Creates the actual relationships between the the source object and the target objects, depending on the 
     * cardinality defined in {@link #getCardinality() }.
     * 
     * @param sourceObjects The list of source objects to be related.
     * @param targetObjects The list of target objects to be related.
     * @throws IllegalArgumentException If the cardinality or the type of target objects does not match the one specified in the {@link #getCardinality() } and {@link #getTargetObjectClass() } methods.
     */
    public abstract void createRelationship(List<BusinessObjectLight> sourceObjects, List<BusinessObjectLight> targetObjects) throws IllegalArgumentException;
    /**
     * Releases the actual relationships between the the source object and the target objects. What "releases" means can be 
     * 
     * @param sourceObjects The list of source objects to be related.
     * @param targetObjects The list of target objects to be related.
     * @throws IllegalArgumentException If the cardinality or the type of target objects does not match the one specified in the {@link #getCardinality() } and {@link #getTargetObjectClass() } methods.
     */
    public abstract void releaseRelationship(List<BusinessObjectLight> sourceObjects, List<BusinessObjectLight> targetObjects) throws IllegalArgumentException;
    /**
     * Defines the cardinality between inventory objects. There are only two entries because those 
     * are the ones used in practice.
     */
    public enum RelationshipCardinality {
        /**
        * A single source object can be related to only one target object.
        */
        ONE_TO_ONE,
        /**
        * Multiple source objects can be related to only one target object.
        */
        ONE_TO_MANY,
        /**
        * Multiple source objects can be related to multiple target objects.
        */
        MANY_TO_MANY
    }
}
