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
package com.neotropic.kuwaiba.modules.commercial.processman.diagram;

import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ParallelActivityDefinition;

/**
 * Set of functions to get information of process instance 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DiagramUtil {
    /**
     * True if the activity is running in parallel
     * @param path The process instance activities path
     * @param activityDefinition The activity definition to be evaluated
     * @return True if the activity is running in parallel
     */
    public static boolean isActivityRunningInParallel(List<ActivityDefinition> path, ActivityDefinition activityDefinition) {
        ParallelActivityDefinition forkActivity = null;
        ParallelActivityDefinition joinActivity = null;
        List<ActivityDefinition> activitiesRunningInParallel = new ArrayList();
        for (ActivityDefinition activityDef : path) {
            if (activityDef instanceof ParallelActivityDefinition) {
                ParallelActivityDefinition parallelActivityDef = (ParallelActivityDefinition) activityDef;
                if (forkActivity == null && (parallelActivityDef.getSequenceFlow() == ParallelActivityDefinition.FORK || parallelActivityDef.getSequenceFlow() == ParallelActivityDefinition.JOIN_FORK)) {
                    forkActivity = parallelActivityDef;
                } else if (parallelActivityDef.getSequenceFlow() == ParallelActivityDefinition.JOIN || parallelActivityDef.getSequenceFlow() == ParallelActivityDefinition.JOIN_FORK)
                    joinActivity = parallelActivityDef;
            }
            if (forkActivity != null) {
                if (joinActivity != null && joinActivity.getId().equals(forkActivity.getIncomingSequenceFlowId())) {
                    if (joinActivity.getSequenceFlow() == ParallelActivityDefinition.JOIN_FORK)
                        forkActivity = joinActivity;
                    else
                        forkActivity = null;
                    joinActivity = null;
                    activitiesRunningInParallel.clear();
                }
                else
                    activitiesRunningInParallel.add(activityDef);
            }
            if (activitiesRunningInParallel.contains(activityDefinition))
                return true;
        }
        return false;
    }
}
