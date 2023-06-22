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
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
public enum EAsyncStep {
    FETCH("sync.step.fetch"),
    ANALYZE("sync.step.analize"),
    ;
    private static final Map<String, EAsyncStep> lookup = new HashMap<>();

    static {
        for (EAsyncStep enumSelected : EAsyncStep.values()) {
            lookup.put(enumSelected.getValue(), enumSelected);
        }
    }

    private final String value;

    EAsyncStep(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
