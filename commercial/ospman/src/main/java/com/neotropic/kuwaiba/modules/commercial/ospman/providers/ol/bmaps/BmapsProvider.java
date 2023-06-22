/**
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman.providers.ol.bmaps;

import com.neotropic.flow.component.olmap.TileLayerSourceBmaps;
import com.neotropic.kuwaiba.modules.commercial.ospman.providers.ol.AbstractOlTileLayerProvider;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Map provider implementation for Bing Maps tile data.
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class BmapsProvider extends AbstractOlTileLayerProvider {

    public BmapsProvider() {
    }

    @Override
    public void createComponent(ApplicationEntityManager aem, MetadataEntityManager mem, ResourceFactory resourceFactory, TranslationService ts) {
        String key = null;
        String imagerySet = null;
        try {
            key = (String) aem.getConfigurationVariableValue("general.maps.apiKey");
        } catch (InventoryException ex) {
            new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"),
                    ts.getTranslatedString("module.ospman.not-set.general-maps-provider-bmaps-key"),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        try {
            imagerySet = (String) aem.getConfigurationVariableValue("general.maps.provider.bmaps.imagerySet");
        } catch (InventoryException ex) {
            new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"),
                    ts.getTranslatedString("module.ospman.not-set.general-maps-provider-bmaps-imagerySet"),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        if (key != null && imagerySet != null) {
            setTileLayerSource(new TileLayerSourceBmaps(key, imagerySet));
        }
        super.createComponent(aem, mem, resourceFactory, ts);
    }
}
