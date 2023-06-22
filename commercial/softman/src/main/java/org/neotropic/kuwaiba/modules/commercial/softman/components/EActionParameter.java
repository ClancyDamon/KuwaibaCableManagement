/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.modules.commercial.softman.components;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Common properties used by visual actions as mandatory properties, and some database relationships or node names.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 * created on 24/10/2023-15:29
 */
public enum EActionParameter {
    NAME("name"),
    CLASS_NAME("className"),
    ADD_LICENSE("addLicense"),
    DELETE_LICENSE("deleteLicense"),
    LICENSE("license"),
    LICENSE_NAME("licenseName"),
    LICENSE_TYPE("licenseType"),
    LICENSE_PRODUCT("licenseProduct"),
    RELEASE_LICENSE("releaseLicense"),
    OBJECT("object"),
    SOURCE_OBJECT("sourceObject"),
    TARGET_OBJECT("targetObject"),
    RELEASE_RELATIONSHIP("releaseRelationship"),
    BUSINESS_OBJECT("businessObject"),
    SOFTWARE_TYPE("SoftwareType"),
    ADD_POOL ("addPool"),
    DELETE_POOL("deletePool"),
    POOL("pool"),//node name,
    LICENSE_POOL("licensePool"),
    LICENSE_HAS("licenseHas")//relation
    ;
    @Getter
    private final String propertyValue;
    private static final Map<String, EActionParameter> lookup = new HashMap<>();


    EActionParameter(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    static {
        for (EActionParameter enumSelected : EActionParameter.values()) {
            lookup.put(enumSelected.getPropertyValue(), enumSelected);
        }
    }
    public static EActionParameter get(String abbreviation) {
        return lookup.get(abbreviation);
    }
}
