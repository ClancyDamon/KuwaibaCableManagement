/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.commercial.processman.diagram.provider;

import java.util.function.Consumer;

/**
 * Represents an activity in a Process Instance Diagram.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface ActivityNode {
    /**
     * @return True if the activity is enabled.
     */
    boolean isEnabled();
    /**
     * Sets if the activity is enabled.
     * @param enabled Set true if the activity is enabled.
     */
    void setEnabled(boolean enabled);
    /**
     * @return True if the activity was executed
     */
    boolean isExecuted();
    /**
     * Sets if the activity was executed.
     * @param execute Set true if the activity was executed.
     */
    void setExecuted(boolean execute);
    /**
     * Adds a click listener.
     * @param listener The click listener.
     */
    void addClickListener(ClickListener listener);
    /**
     * Removes a click listener.
     * @param listener Click listener.
     */
    void removeClickListener(ClickListener listener);
    /**
     * Removes all click listener.
     */
    void removeAllClickListeners();
    
    public interface Listener {}
    public interface Event<T extends Listener> {
        T getListener();
    }
    
    public interface ClickListener extends Consumer<ClickEvent>, Listener {}
    
    public class ClickEvent implements Event<ClickListener> {
        private final ClickListener listener;
        
        public ClickEvent(ClickListener listener) {
            this.listener = listener;
        }

        @Override
        public ClickListener getListener() {
            return listener;
        }
    }
}
