/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.actions;

import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;

/**
 * Actions applicable to form elements.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public abstract class AbstractVisualElementAction extends AbstractVisualAction {

    public static final int SELECTION_NO_ELEMENTS = 0;
    public static final int SELECTION_ANY_ELEMENTS = -1;
    
    public AbstractVisualElementAction(String moduleId) {
        super(moduleId);
    }

    public String getName() {
        return getModuleAction() == null ? "" : getModuleAction().getDisplayName();
    }
    
    @Override
    public String toString() {
        return getName();
    }
}