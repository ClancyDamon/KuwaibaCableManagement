/*
 * Copyright 2023-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.sync.components;

import lombok.Getter;
import lombok.Setter;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.model.PollResult;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
public class AsyncAnalizeDataJob implements DetailedJob {

    private float progress;
    @Getter
    private final String jobName;
    private final AbstractSyncProvider syncProvider;
    private final PollResult pollResult;
    @Setter
    private int totalJobs;
    @Setter
    private int jobNumber;
    @Setter
    private ProgressBroadcaster progressBroadcaster;
    @Getter
    private volatile List<SyncResult> value;
    @Getter
    private EJobState state;

    public AsyncAnalizeDataJob(PollResult pollResult
            , AbstractSyncProvider syncProvider) {

        UUID uuid = UUID.randomUUID();
        this.jobName = "job-" + uuid;
        this.progress = 0;
        this.state = EJobState.NEW;
        this.syncProvider = syncProvider;
        this.pollResult = pollResult;
        updateProgress(progressBroadcaster);
    }

    @Override
    public void run() {
        state = EJobState.IN_PROGRESS;
        updateProgress(progressBroadcaster);
        value = syncProvider.automatedSync(pollResult);
        state = EJobState.FINISH;
        progress = (float) jobNumber /totalJobs;
        updateProgress(progressBroadcaster);
    }

    /**
     * Update progress dialog with values
     * @param progressBroadcaster vaadin broadcaster
     */
    public void updateProgress(ProgressBroadcaster progressBroadcaster) {
        JobProgressMessage temp = new JobProgressMessage(jobName);
        temp.setProgress(progress);
        temp.setState(state);
        temp.setStep(EAsyncStep.ANALYZE);
        temp.setTotalElements(totalJobs);
        temp.setElement(jobNumber);
        Logger.getLogger(AsyncAnalizeDataJob.class.getName()).log(Level.INFO
                , String.format("Analized %s => job (%s / %s) progress: %s - State: %s\n", jobName, jobNumber, totalJobs
                , progress, state));
        if (progressBroadcaster != null && value != null && !value.isEmpty())
            progressBroadcaster.broadcast(temp, value);
        else if (progressBroadcaster != null) {
            progressBroadcaster.broadcast(temp, new ArrayList<>());
        }
    }

    @Override
    public float getProgress() {
        return progress;
    }
}
