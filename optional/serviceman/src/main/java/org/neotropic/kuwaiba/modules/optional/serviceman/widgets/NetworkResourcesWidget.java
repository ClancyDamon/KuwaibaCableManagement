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

package org.neotropic.kuwaiba.modules.optional.serviceman.widgets;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.widgets.AbstractDashboardWidget;

/**
 * Shows the network resources related to a service.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class NetworkResourcesWidget extends AbstractDashboardWidget {
    /**
     * The service the network resources belong to.
     */
    private BusinessObjectLight service;
    public NetworkResourcesWidget(BusinessObjectLight service, MetadataEntityManager mem, ApplicationEntityManager aem, 
            BusinessEntityManager bem, TranslationService ts) {
        super(mem, aem, bem, ts);
        this.service = service;
        setTitle(ts.getTranslatedString("module.serviceman.widgets.network-resources.title"));
        createCover();
        coverComponent.addClassName("widgets-colors-magenta");
    }
    
    @Override
    public void createContent() {
        try {
            List<BusinessObjectLight> relatedNetworkResources = bem.getSpecialAttribute(service.getClassName(), service.getId(), "uses");
            if (relatedNetworkResources.isEmpty()) {
                contentComponent = new Label(ts.getTranslatedString("module.serviceman.widgets.network-resources.ui.no-network-resources"));
                return;
            }
                
            Grid<BusinessObjectLight> tblNetworkResources = new Grid<>();
            tblNetworkResources.setItems(relatedNetworkResources);
            tblNetworkResources.addColumn(BusinessObjectLight::getName).setHeader(ts.getTranslatedString("module.widgets.messages.general.name"));
            tblNetworkResources.addColumn(BusinessObjectLight::getClassName).setHeader(ts.getTranslatedString("module.widgets.messages.general.type"));
            tblNetworkResources.setSizeFull();
            VerticalLayout lytContent = new VerticalLayout(tblNetworkResources);
            lytContent.addClassName("widgets-layout-dialog-list");
            contentComponent = lytContent;
        } catch (InventoryException ex) {
            contentComponent = new Label(ex.getLocalizedMessage());
        }
    }
}
