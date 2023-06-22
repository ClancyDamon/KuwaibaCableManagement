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

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * All actions related to form elements.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Service
public class ElementActionsRegistry {
    /**
     * The list of registered misc actions.
     */
    private List<AbstractVisualElementAction> miscActions;
    
    public ElementActionsRegistry() {
        this.miscActions = new ArrayList<>();
    }
    
    public void registerAction(AbstractVisualElementAction action) {
        this.miscActions.add(action);
    }
    
    /**
     * Returns all registered misc actions.
     * @return All registered misc actions.
     */
    public List<AbstractVisualElementAction> getMiscActions() {
        return this.miscActions;
    }
}