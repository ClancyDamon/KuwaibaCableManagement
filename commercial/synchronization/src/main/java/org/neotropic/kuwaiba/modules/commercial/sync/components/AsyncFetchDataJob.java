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
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.model.PollResult;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;

/**
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
public class AsyncFetchDataJob implements DetailedJob {

    private final float progress;

    @Getter
    private final String jobName;
    private final AbstractSyncProvider syncProvider;
    private final SyncDataSourceConfiguration dataSourceConfiguration;

    @Getter
    private volatile PollResult value;
    @Getter
    private EJobState state;


    public AsyncFetchDataJob(AbstractSyncProvider syncProvider
            , SyncDataSourceConfiguration dataSourceConfiguration) {

        this.jobName = dataSourceConfiguration.getName();
        String deviceName = dataSourceConfiguration.getBusinessObjectLight().getName();
        this.progress = 0;
        this.state = EJobState.NEW;
        this.syncProvider = syncProvider;
        this.dataSourceConfiguration = dataSourceConfiguration;
    }

    @Override
    public void run() {
        state = EJobState.IN_PROGRESS;
        value = syncProvider.fetchData(dataSourceConfiguration);
        state = EJobState.FINISH;
    }

    @Override
    public float getProgress() {
        return progress;
    }
}
