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
import org.neotropic.kuwaiba.modules.commercial.sync.notification.Broadcaster;

/**
 * Represent job messages
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
public class JobProgressMessage {
    @Getter
    @Setter String deviceName;
    @Getter
    private final String jobName;
    @Getter
    @Setter
    private EJobState state;
    @Getter
    @Setter
    private EAsyncStep step;
    @Getter
    @Setter
    private float progress;
    @Getter
    @Setter
    private int totalElements;
    @Getter
    @Setter
    private int element;
    @Getter
    @Setter
    private Broadcaster broadcaster;

    public JobProgressMessage(String jobName) {
        this.jobName = jobName;
    }
}
