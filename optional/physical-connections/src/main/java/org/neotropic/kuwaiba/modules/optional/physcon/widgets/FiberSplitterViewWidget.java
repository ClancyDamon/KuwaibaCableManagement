/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.modules.optional.physcon.widgets;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractObjectRelatedViewWidget;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.views.FiberSplitterView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A tooled component tat wraps a {@link FiberSplitterView}.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class FiberSplitterViewWidget extends AbstractObjectRelatedViewWidget<VerticalLayout> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    
    @Override
    public String appliesTo() {
        return "FiberSplitter";
    }

    @Override
    public String getName() {
        return ts.getTranslatedString("module.visualization.fiber-splitter-view-name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString(ts.getTranslatedString("module.visualization.fiber-splitter-view-description"));
    }

    @Override
    public String getVersion() {
        return "1.1";
    }
    
    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>"; //NOI18N
    }

    @Override
    public String getTitle() {
        return ts.getTranslatedString("module.navigation.widgets.object-dashboard.fiber-splitter-view-name");
    }
    
    @Override
    public VerticalLayout build(BusinessObjectLight businessObject) throws InventoryException {
        return new FiberSplitterView(businessObject, bem, aem, mem, ts).getAsUiElement();
    }
}