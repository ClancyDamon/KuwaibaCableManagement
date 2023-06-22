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

package com.neotropic.kuwaiba.modules.commercial.sdh;

import com.neotropic.kuwaiba.modules.commercial.sdh.actions.DeleteSdhViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.sdh.actions.NewSdhViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.sdh.widgets.SdhDashboard;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the SDH module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
@Route(value = "sdh", layout = SdhLayout.class)
public class SdhManagerUI extends VerticalLayout implements HasDynamicTitle, AbstractUI {

    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    
    @Autowired
    private SdhService sdhService;
    
    @Autowired
    private DeleteSdhViewVisualAction deleteSdhViewVisualAction;
    
    @Autowired
    private NewSdhViewVisualAction newSdhViewVisualAction;
    
    @Autowired
    private ResourceFactory resourceFactory;
 
    private SdhDashboard dashboard; 
   
    public SdhManagerUI() {
        super();
        setSizeFull();
    } 
    
    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {                
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();                                          
            } catch (Exception ex) {
                Logger.getLogger(SdhManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.sdh.title");
    }

    @Override
    public void initContent() {
        setSizeFull();      
        try {
            dashboard = new SdhDashboard(ts, mem, aem, bem, resourceFactory, sdhService, 
                    deleteSdhViewVisualAction, newSdhViewVisualAction);
            dashboard.setSizeFull();
            add(dashboard);
        } catch (Exception ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
}
