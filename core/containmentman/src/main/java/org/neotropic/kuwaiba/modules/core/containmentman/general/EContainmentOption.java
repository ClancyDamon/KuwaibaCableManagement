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
package org.neotropic.kuwaiba.modules.core.containmentman.general;

import java.util.HashMap;
import java.util.Map;

/**
 * Options display in combo box used in main containment manager UI.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public enum EContainmentOption {
    SIMPLE("module.containmentman.combobox.standard")
    , SPECIAL("module.containmentman.combobox.special")    
    ;
    private final String value;
    private static final Map<String, EContainmentOption> lookup = new HashMap<>();

    EContainmentOption(String type) {
        this.value = type;
    }

    static {
        for (EContainmentOption enumSelected : EContainmentOption.values()) {
            lookup.put(enumSelected.getValue(), enumSelected);
        }
    }

    /**
     * Return description for enum value
     *
     * @return the state
     */
    public String getValue() {
        return value;
    }

    public static EContainmentOption get(String abbreviation) {
        return lookup.get(abbreviation);
    }
}
