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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.shared.Registration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncResult;
import org.neotropic.kuwaiba.modules.commercial.sync.notification.Broadcaster;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * Broadcaster for synchronization dialog
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
@Component
public class ProgressBroadcaster {
    /**
     * Thread for notification executions
     */
    private static final Executor executor = Executors.newSingleThreadExecutor();
    /**
     * Used to refresh pages
     */
    private static final List<BiConsumer<JobProgressMessage, List<SyncResult>>> syncResultListener = new ArrayList<>();

    public synchronized Registration registerSyncResults(BiConsumer<JobProgressMessage, List<SyncResult>> listener) {
        syncResultListener.add(listener);
        return () -> {
            synchronized (Broadcaster.class) {
                syncResultListener.remove(listener);
            }
        };
    }

    public synchronized void broadcast(JobProgressMessage progress, List<SyncResult> syncResults) {
        for (BiConsumer<JobProgressMessage, List<SyncResult>> listener : syncResultListener) {
            executor.execute(() -> {
                listener.accept(progress, syncResults);
            });
        }
    }

    public static class ProgressUpdateEvent extends ComponentEvent<Label> {
        public ProgressUpdateEvent() {
            super(new Label(""), false);
        }
    }
}
