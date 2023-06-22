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
package org.neotropic.kuwaiba.modules.commercial.sync;

import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.components.AsyncAnalizeDataJob;
import org.neotropic.kuwaiba.modules.commercial.sync.components.AsyncFetchDataJob;
import org.neotropic.kuwaiba.modules.commercial.sync.components.EAsyncStep;
import org.neotropic.kuwaiba.modules.commercial.sync.components.EJobState;
import org.neotropic.kuwaiba.modules.commercial.sync.components.JobProgressMessage;
import org.neotropic.kuwaiba.modules.commercial.sync.components.ProgressBroadcaster;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.model.PollResult;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncResult;
import org.neotropic.kuwaiba.modules.commercial.sync.notification.Broadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The service creates asynchronous jobs to obtain and deliver information about a synchronization process.
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
@Service
public class JobService {
    /**
     * Vaadin thread broadcaster
     */
    @Autowired
    private Broadcaster broadcaster;
    /**
     * Broadcaster for synchronization dialog
     */
    @Autowired
    private ProgressBroadcaster progressBroadcaster;
    /**
     * This service provides I18N support for the application
     */
    @Autowired
    private TranslationService ts;

    /**
     * Async thread to fetch data using a data source configuration
     * @param syncProvider synchronization provider
     * @param dataSourceConfiguration data source configuration
     * @return completale future thread
     */
    @Async
    public CompletableFuture<PollResult> createAsyncFetchJob(AbstractSyncProvider syncProvider
            , SyncDataSourceConfiguration dataSourceConfiguration) {
        AsyncFetchDataJob newJob = new AsyncFetchDataJob(syncProvider, dataSourceConfiguration);
        return CompletableFuture.supplyAsync(() -> {
            newJob.run();
            return newJob.getValue();
        });
    }

    /**
     * Asynchronous thread to obtain the results of the previous data collection process.
     * @param pollResult  results of  data collection process
     * @param syncProvider synchronization provider
     * @param totalElements total jobs
     * @param currentElement current job
     * @return completale future thread
     */
    @Async
    public CompletableFuture<List<SyncResult>> createAsyncActionsJob(PollResult pollResult
            , AbstractSyncProvider syncProvider
            , int totalElements
            , int currentElement) {
        AsyncAnalizeDataJob newJob = new AsyncAnalizeDataJob(pollResult, syncProvider);
        newJob.setProgressBroadcaster(progressBroadcaster);
        newJob.setTotalJobs(totalElements);
        newJob.setJobNumber(currentElement);
        return CompletableFuture.supplyAsync(() -> {
            newJob.run();
            return newJob.getValue();
        });
    }

    /**
     * Create a thread to execute a result capture to be sent to the user in a dialog.
     * @param syncProvider synchronization provider
     * @param datasources data soruce sonfiguration
     * @return completale future thread
     */
    public CompletableFuture<List<SyncResult>> createJob(AbstractSyncProvider syncProvider
            , List<SyncDataSourceConfiguration> datasources) {
        // create contents of all users asynchronously
        List<CompletableFuture<PollResult>> firstStepFetchData = datasources
                .stream()
                .map(item -> createAsyncFetchJob(syncProvider, item))
                .collect(Collectors.toList());

        // Create a combined Future using allOf()
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(firstStepFetchData.toArray(new CompletableFuture[0]));

        // When all the Futures are completed, call `future.join()` to get their results and collect the results in a list -
        CompletableFuture<PollResult> secondStepData = allFutures.thenApply(v -> {
            PollResult pollResult = new PollResult();
            firstStepFetchData.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList())
                    .stream()
                    .map(item -> {
                        pollResult.getResult().putAll(item.getResult());
                        return pollResult;
                    })
                    .map(item -> {
                        pollResult.getExceptions().putAll(item.getExceptions());
                        return pollResult;
                    })
                    //.findFirst();
                    .forEach( item -> Logger.getLogger(JobService.class.getName()).log(Level.INFO
                            , String.format("PollResult collected %d\n", item.getResult().size())));

            return pollResult;
        });

        // update progess for fetching tasks
        AtomicInteger completedTasks = new AtomicInteger(0); // completed tasks counter
        firstStepFetchData.forEach((future) -> {
            int taskIndex = firstStepFetchData.indexOf(future);
            future.thenAccept(result -> fecthUpdateProgress(taskIndex, completedTasks, firstStepFetchData.size()));
        });

        // Apply the second step once the first step has been completed.
        try {
            AtomicInteger secondJobCounter = new AtomicInteger(1);
            CompletableFuture<List<SyncResult>> listCompletableFuture = secondStepData.thenApply(item ->
                    createAsyncActionsJob(item, syncProvider, 1, secondJobCounter.getAndIncrement())).get();
            listCompletableFuture.thenAccept(syncResults ->
                    broadcaster.broadcast(ts.getTranslatedString("module.sync.actions.sync.success")));
            return listCompletableFuture;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Update progress dialog with values
     * @param taskIndex Task index
     * @param completedTasks Current task completed number
     * @param taskSize Total tasks to completre
     */
    private void fecthUpdateProgress(int taskIndex, AtomicInteger completedTasks, int taskSize) {
        completedTasks.incrementAndGet(); // incrementar el contador de tareas completadas
        float progress = (float)  completedTasks.get()/taskSize;
        Logger.getLogger(JobService.class.getName()).log(Level.INFO
                , String.format("Task %d completed. Progress: %s\n"
                        , taskIndex+1,  progress));
        JobProgressMessage temp = new JobProgressMessage("job_"+taskIndex);
        temp.setProgress(progress);
        if(completedTasks.get() < taskSize)
            temp.setState(EJobState.IN_PROGRESS);
        else
            temp.setState(EJobState.FINISH);
        temp.setStep(EAsyncStep.FETCH);
        temp.setTotalElements(taskSize);
        temp.setElement(completedTasks.get());

        if (progressBroadcaster != null)
            progressBroadcaster.broadcast(temp, new ArrayList<>());
    }

}
