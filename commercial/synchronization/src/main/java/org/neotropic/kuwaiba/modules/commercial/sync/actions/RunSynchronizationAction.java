/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.modules.commercial.sync.actions;

import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.JobService;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationService;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncResult;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
import org.neotropic.kuwaiba.modules.commercial.sync.notification.Broadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Run Synchronization
 *
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class RunSynchronizationAction extends AbstractAction {

    /**
     * Parameter, group to be released from data source.
     */
    public static String PARAM_SYNC_GROUP = "group"; //NOI18N
    /**
     * Parameter, synchronization provider
     */
    public static String PARAM_SYNC_PROVIDER = "syncProvider"; //NOI18N
    /**
     * Parameter, data source configuration.
     */
    public static String PARAM_SYNC_DATA_SOURCE = "syncDataSourceConfiguration"; //NOI18N
    /**
     * Vaadin thread broadcaster
     */
    @Autowired
    private Broadcaster broadcaster;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private SynchronizationService ss;
    /**
     * Reference to the job service
     */
    @Autowired
    private JobService jobService;

    @Async
    @PostConstruct
    protected void init() {
        this.id = ts.getTranslatedString("sync.new-sync-run-sync");
        this.displayName = ts.getTranslatedString("module.sync.actions.sync-run-sync.name");
        this.description = ts.getTranslatedString("module.sync.actions.sync-run-sync.description");
        this.order = 1000;

        setCallback((parameters) -> {
            AbstractSyncProvider syncProvider = (AbstractSyncProvider) parameters.get(PARAM_SYNC_PROVIDER);
            SynchronizationGroup syncGroup = (SynchronizationGroup) parameters.get(PARAM_SYNC_GROUP);
            SyncDataSourceConfiguration configuration = (SyncDataSourceConfiguration) parameters.get(PARAM_SYNC_DATA_SOURCE);
            ActionResponse actionReesponse = new ActionResponse();

            if (syncGroup != null) {
                CompletableFuture<List<SyncResult>> createJob = jobService.createJob(syncProvider
                        , syncGroup.getSyncDataSourceConfigurations());
                createJob.thenAcceptAsync(item -> broadcaster.broadcast(item));

            } else if(configuration != null){ // create  temp sync group
                SynchronizationGroup tempSyncGroup = new SynchronizationGroup( -1, "temporal_group"
                        , "", Collections.singletonList(configuration));
                CompletableFuture<List<SyncResult>> createJob = jobService.createJob(syncProvider
                        , tempSyncGroup.getSyncDataSourceConfigurations());
                createJob.thenAcceptAsync(item -> broadcaster.broadcast(item));
            }
            return actionReesponse;
        });

    }

    @Override
    public int getRequiredAccessLevel() {
        return Privilege.ACCESS_LEVEL_READ_WRITE;
    }

    @Override
    public boolean requiresConfirmation() {
        return false;
    }
}