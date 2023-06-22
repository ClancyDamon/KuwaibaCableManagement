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
package org.neotropic.kuwaiba.modules.commercial.sync.components;

import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.StringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that build property sets given sync groups.
 *
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class SyncGroupPropertySheet {
    /**
     * Display sync group elements; used in sync framework
     *
     * @param group; Synchronization Group
     * @param ts;    TranslationService
     * @return object properties
     */
    public static List<AbstractProperty> propertiesFromSyncGroup(SynchronizationGroup group, TranslationService ts) {
        ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
        AbstractProperty property;

        property = new StringProperty(Constants.PROPERTY_NAME,
                Constants.PROPERTY_NAME, Constants.PROPERTY_NAME,
                group.getName() == null || group.getName().isEmpty()
                        ? AbstractProperty.NULL_LABEL : group.getName(), ts);
        objectProperties.add(property);

        return objectProperties;
    }
}