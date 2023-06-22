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
package org.neotropic.kuwaiba.core.apis.integration.modules.actions;

import java.util.Properties;

/**
 * Wrapper of possible (since it's possible that an action returns nothing) return values of an action.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class ActionResponse extends Properties {
    
    /**
     * Used the actionCompleted method to know which action was made and what 
     * should be done in the view; e.g add, remove, hide/show an element
     */
    public enum ActionType {
        REMOVE, //an element was removed and should be removed from the view
        ADD, //an element was added and should also be added to the view
        UPDATE, //an element was udpdated and this chagnes updated in the view
        HIDE,  //an element status has chaged to hide = true and should be hide in the view
        SHOW, //an element status has chaged to hide = false and should be shown in the view
        COPY, //an element was copy an the action should be reflected in the view
        MOVE, //an element was copy an the action should be reflected in the view
        RELEASE, //an element was released an the action should be reflected in the view
        RELATE; //an element was related an the action should be reflected in the view
    }
}
