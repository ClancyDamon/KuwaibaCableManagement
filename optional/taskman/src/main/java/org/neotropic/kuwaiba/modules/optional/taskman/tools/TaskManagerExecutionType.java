/*
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
package org.neotropic.kuwaiba.modules.optional.taskman.tools;

/**
 * Dummy class to be used in the scheduling properties for execution type combo box
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class TaskManagerExecutionType {
    private final String displayName;
    private final int type;

    public TaskManagerExecutionType(String displayName, int type) {
        this.displayName = displayName;
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return displayName;
    }
}