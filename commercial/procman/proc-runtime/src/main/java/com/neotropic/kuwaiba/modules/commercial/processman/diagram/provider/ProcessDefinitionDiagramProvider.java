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
package com.neotropic.kuwaiba.modules.commercial.processman.diagram.provider;

import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Actor;

/**
 * Provides a process definition diagram
 * @param <T> Process Definition Diagram UI Element Type
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface ProcessDefinitionDiagramProvider<T> {
    /**
     * Gets the process definition diagram.
     * @return the process definition diagram.
     */
    T getUiElement();
    /**
     * Adds an actor.
     * @param actor The actor to be added.
     * @return An actor node.
     */
    ActorNode addActor(Actor actor);
    /**
     * Adds an activity node
     * @param nextActivity The activity to be added.
     * @param previousActivity The previous activity.
     * @param pathName The connection label
     * @return An activity node.
     */
    ActivityNode addActivity(ActivityDefinition nextActivity, ActivityDefinition previousActivity, String pathName);
    /**
     * TODO: Removes once the coordinates are loaded from the xml
     * @param activityDefinition activity definition to execute layout
     */
    @Deprecated
    void executeLayout(ActivityDefinition activityDefinition);
}
