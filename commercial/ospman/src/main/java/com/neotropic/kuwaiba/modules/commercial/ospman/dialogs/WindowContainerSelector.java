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

import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.window.ObjectSelectorWindow;

/**
 * Window to select a container that must be a special child of one of the 
 * container endpoints of a given location.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowContainerSelector extends ObjectSelectorWindow {
    private final BusinessObjectLight location;
    private final BusinessEntityManager bem;
    private final TranslationService ts;
    
    public WindowContainerSelector(BusinessObjectLight location, BusinessEntityManager bem, 
        MetadataEntityManager mem, TranslationService ts, Consumer<BusinessObjectLight> consumerSelectedContainer) {
        super(location, mem, ts, consumerSelectedContainer);
        Objects.requireNonNull(bem);
        this.location = location;
        this.bem = bem;
        this.ts = ts;
    }
    
    @Override
    public void open() {
        super.open();
        setHeader(String.format(ts.getTranslatedString("module.ospman.window.select-container.title"), location.getName()));
    }

    @Override
    public List<BusinessObjectLight> getItems(BusinessObjectLight selectedObject) throws InventoryException {
        List<BusinessObjectLight> fibers = bem.getSpecialChildrenOfClassLight(
            selectedObject.getId(), selectedObject.getClassName(), 
            Constants.CLASS_GENERICPHYSICALLINK, -1
        );
        setButtonOkEnabled(!fibers.isEmpty());
        
        List<BusinessObjectLight> containers = new ArrayList();
        if (location.equals(selectedObject)) {
            HashMap<String, List<BusinessObjectLight>> endpoints = bem.getSpecialAttributes(
                location.getClassName(), location.getId(), 
                OspConstants.SPECIAL_ATTR_ENDPOINT_A, OspConstants.SPECIAL_ATTR_ENDPOINT_B
            );
            for (List<BusinessObjectLight> objects : endpoints.values()) {
                for (BusinessObjectLight object : objects)
                    containers.add(object);
            }
        } else {
            containers = bem.getSpecialChildrenOfClassLight(
                selectedObject.getId(), selectedObject.getClassName(), 
                Constants.CLASS_GENERICPHYSICALCONTAINER, 0
            );
        }
        Collections.sort(containers, Comparator.comparing(BusinessObjectLight::getName));
        return containers;
    }
}
