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

import java.util.HashMap;
import java.util.Map;

/**
 * Representing all allowed synchronization states
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
public enum ESyncResultState {
    ERROR("sync-result-state-error"),
    SUCCESS("sync-result-state-success"),
    WARNING("sync-result-state-warning"),
    INFORMATION("sync-result-state-information");

    private static final Map<String, ESyncResultState> lookup = new HashMap<>();

    static {
        for (ESyncResultState enumSelected : ESyncResultState.values()) {
            lookup.put(enumSelected.getValue(), enumSelected);
        }
    }

    private final String value;

    ESyncResultState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

