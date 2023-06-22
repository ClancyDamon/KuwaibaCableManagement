/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.util.visual.selectors;

import com.vaadin.flow.data.provider.DataProvider;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.persistence.application.FilterDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Set of methods to use in selectors.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class SelectorsUtils {
    /**
     * Gets a combo box data provider of business objects filtered by the business object name.
     * @param parent The business object to apply the filter.
     * @param filterDefinition Filter Definition. See Filter Definition Manager.
     * @param ts Translation Service.
     * @return The combo box data provider.
     */
    public static DataProvider<BusinessObjectLight, String> getFilterDataProvider(BusinessObjectLight parent, FilterDefinition filterDefinition, TranslationService ts) {
        return DataProvider.fromFilteringCallbacks(
            query -> {
                try {
                    List<BusinessObjectLight> businessObjects = filterDefinition.getFilter().run(parent.getId(), parent.getClassName(), null, -1, -1);
                    return businessObjects.stream()
                        .filter(businessObject -> StringUtils.containsIgnoreCase(businessObject.getName(), query.getFilter().orElse(null)))
                        .skip(query.getOffset())
                        .limit(query.getLimit());
                    
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
                return null;
            }, 
            query -> {
                try {
                    List<BusinessObjectLight> businessObjects = filterDefinition.getFilter().run(parent.getId(), parent.getClassName(), null, -1, -1);
                    return (int) businessObjects.stream()
                        .filter(businessObject -> StringUtils.containsIgnoreCase(businessObject.getName(), query.getFilter().orElse(null)))
                        .skip(query.getOffset())
                        .limit(query.getLimit())
                        .count();
                    
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
                return 0;
            }
        );
    }
}
