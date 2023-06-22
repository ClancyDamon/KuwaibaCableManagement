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
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.GridElementUi;
import java.util.UUID;

/**
 *
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class GridElementUiItem extends AbstractElementUiItem {

    public GridElementUiItem(String displayName) {
        super(displayName);
    }

    @Override
    public ElementUi create() {
        GridElementUi gridElementUi = new GridElementUi();
        gridElementUi.setElementUiId(UUID.randomUUID().toString());
        gridElementUi.setElementUiWidth("30%");
        gridElementUi.setElementUiMinWidth("30px");
        gridElementUi.setElementUiHeight("33px");
        
        return gridElementUi;
    }
}