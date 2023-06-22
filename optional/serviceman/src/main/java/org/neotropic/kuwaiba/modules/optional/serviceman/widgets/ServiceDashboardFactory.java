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

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A factory class to build Service Manager dashboards as an strategy to avoid carrying around the XEMs and the translation service. 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class ServiceDashboardFactory {
    @Autowired
    private MetadataEntityManager mem;
    @Autowired
    private ApplicationEntityManager aem;
    @Autowired
    private BusinessEntityManager bem;
    @Autowired
    private TranslationService ts;
    /**
     * The property sheet with the attributes of the service.
     */
    private PropertySheet shtServiceProperties;
    
    public HorizontalLayout build(BusinessObjectLight aService) {
        HorizontalLayout dashboard = new HorizontalLayout();
        dashboard.setSizeFull();
        try {
            BusinessObject service = bem.getObject(aService.getClassName(), aService.getId());
            List<AbstractProperty> serviceAttributes = PropertyFactory.propertiesFromBusinessObject(service, ts, aem, mem);
            this.shtServiceProperties = new PropertySheet(ts, serviceAttributes);
            dashboard.add(this.shtServiceProperties);
            dashboard.add(new NetworkResourcesWidget(service, mem, aem, bem, ts));
        } catch (InventoryException ex) {
            dashboard.add(new Label(ex.getMessage()));
        }
        return dashboard;
    }
}
