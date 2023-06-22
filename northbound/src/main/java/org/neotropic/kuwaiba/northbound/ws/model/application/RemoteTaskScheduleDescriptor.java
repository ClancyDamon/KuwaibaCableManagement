/**
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.northbound.ws.model.application;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskScheduleDescriptor;

/**
 * Describes when a task should be scheduled and executed
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */

@XmlAccessorType(XmlAccessType.FIELD)
public final class RemoteTaskScheduleDescriptor implements Serializable {
    /**
     * Execute once, on system start-up.
     */
    public static final int TYPE_STARTUP = 1;
    /**
     * Execute once, on user's log in
     */
    public static final int TYPE_LOGIN = 2;
    /**
     * Execute periodically, based on the execution time 
     */
    public static final int TYPE_LOOP = 3;
    /**
     * Start time as milliseconds from Epoch time. What time should this start start executing? Use with executionType TYPE_LOOP
     */
    private long startTime;
    /**
     * Execute every X minutes since startTime. Use with executionType TYPE_LOOP
     */
    private int everyXMinutes;
    /**
     * Type of execution. See the static fields in this class for possible values
     */
    private int executionType;

    //No-arg constructor required
    public RemoteTaskScheduleDescriptor() {   }

    public RemoteTaskScheduleDescriptor(long startTime, int everyXMinutes, int executionType) {
        this.startTime = startTime;
        this.everyXMinutes = everyXMinutes;
        this.executionType = executionType;
    }

    public RemoteTaskScheduleDescriptor(TaskScheduleDescriptor schedule) {
        this.startTime = schedule.getStartTime();
        this.everyXMinutes = schedule.getEveryXMinutes();
        this.executionType = schedule.getExecutionType();
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getEveryXMinutes() {
        return everyXMinutes;
    }

    public void setEveryXMinutes(int everyXMinutes) {
        this.everyXMinutes = everyXMinutes;
    }

    public int getExecutionType() {
        return executionType;
    }

    public void setExecutionType(int executionType) {
        this.executionType = executionType;
    }
}    
    