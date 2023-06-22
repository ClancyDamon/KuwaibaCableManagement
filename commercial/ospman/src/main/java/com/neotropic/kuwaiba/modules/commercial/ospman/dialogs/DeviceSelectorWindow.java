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
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.window.ObjectSelectorWindow;

/**
 * Window to select a Device or Viewable Object.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeviceSelectorWindow extends ObjectSelectorWindow {
    private final BusinessEntityManager bem;
    
    public DeviceSelectorWindow(BusinessObjectLight object, MetadataEntityManager mem, 
        TranslationService ts, Consumer<BusinessObjectLight> consumerSelectedObject, 
        BusinessEntityManager bem) {
        super(object, mem, ts, consumerSelectedObject);
        this.bem = bem;
    }

    @Override
    public void open() {
        super.open();
        setHeader(String.format(getTranslationService().getTranslatedString("module.ospman.window.select-device.title"), getObject().getName()));
    }
    
    @Override
    public List<BusinessObjectLight> getItems(BusinessObjectLight selectedObject) throws InventoryException {
        List<BusinessObjectLight> items = bem.getChildrenOfClassLightRecursive(
            selectedObject.getId(), selectedObject.getClassName(), 
            Constants.CLASS_VIEWABLEOBJECT, null, -1, -1);
        Collections.sort(items, Comparator.comparing(BusinessObjectLight::getName));
        return items;
    }
}
