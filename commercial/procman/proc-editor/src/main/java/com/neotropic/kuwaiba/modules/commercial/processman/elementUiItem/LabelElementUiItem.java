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
package com.neotropic.kuwaiba.modules.commercial.processman.elementUiItem;

import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.ElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.LabelElementUi;
import java.util.UUID;

/**
 *
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class LabelElementUiItem extends AbstractElementUiItem {
    
    public LabelElementUiItem(String displayName) {
        super(displayName);
    }

    @Override
    public ElementUi create() {
        LabelElementUi labelElementUi = new LabelElementUi();
        labelElementUi.setElementUiId(UUID.randomUUID().toString());
        labelElementUi.setElementUiValue("label");
        labelElementUi.setElementUiWidth("30%");
        labelElementUi.setElementUiMinWidth("30px");
        labelElementUi.setElementUiHeight("33px");
        
        return labelElementUi;
    }
}