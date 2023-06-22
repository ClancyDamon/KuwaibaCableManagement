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

import com.neotropic.kuwaiba.modules.commercial.osp.external.services.OutsidePlantExternalServicesProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapProvider;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.renderer.TextRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.integration.external.services.AbstractExternalService;
import org.neotropic.kuwaiba.core.apis.integration.external.services.AbstractInventoryExternalService;
import org.neotropic.kuwaiba.core.apis.integration.external.services.ExternalServiceProvider;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to show the external services for a business object.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowExternalServices extends ConfirmDialog {
    private final BusinessObjectLight businessObject;
    private final OutsidePlantExternalServicesProvider ospExternalServicesProvider;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final MapProvider mapProvider;
    
    public WindowExternalServices(BusinessObjectLight businessObject, 
        OutsidePlantExternalServicesProvider ospExternalServicesProvider,
        MetadataEntityManager mem, TranslationService ts, MapProvider mapProvider) {
        Objects.requireNonNull(businessObject);
        Objects.requireNonNull(ospExternalServicesProvider);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(mapProvider);
        this.businessObject = businessObject;
        this.ospExternalServicesProvider = ospExternalServicesProvider;
        this.mem = mem;
        this.ts = ts;
        this.mapProvider = mapProvider;
    }

    @Override
    public void open() {
        Accordion accordion = new Accordion();
        for (ExternalServiceProvider externalServiceProvider : ospExternalServicesProvider.getExternalServiceProviders()) {
            if (hasExternalServices(externalServiceProvider)) {
                List<AbstractExternalService> externalServices = new ArrayList();
                for (AbstractExternalService externalService : externalServiceProvider.getExternalServices()) {
                    if (externalService instanceof AbstractInventoryExternalService) {
                        String appliesTo = ((AbstractInventoryExternalService) externalService).appliesTo();
                        if (appliesTo != null) {
                            try {
                                if (mem.isSubclassOf(appliesTo, businessObject.getClassName()))
                                    externalServices.add(externalService);
                            } catch (MetadataObjectNotFoundException ex) {
                                new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"), 
                                    ex.getLocalizedMessage(), 
                                    AbstractNotification.NotificationType.ERROR, 
                                    ts
                                ).open();
                            }
                        }
                        else
                            externalServices.add(externalService);
                    }
                }
                ListBox<AbstractExternalService> lstExternalServices = new ListBox();
                lstExternalServices.setItems(externalServices);
                lstExternalServices.setRenderer(new TextRenderer<>(item -> item.getName()));
                lstExternalServices.addValueChangeListener(valueChangeEvent -> {
                    AbstractExternalService value = valueChangeEvent.getValue();
                    if (value != null) {
                        HashMap<String, Object> parameters = new HashMap();
                        parameters.put("mapProvider", mapProvider);
                        parameters.put("businessObject", businessObject);
                        value.run(parameters);
                        close();
                    }
                });
                AccordionPanel accordionPanel = new AccordionPanel();
                accordionPanel.setSummaryText(externalServiceProvider.getName());
                accordionPanel.setContent(lstExternalServices);
                accordionPanel.setOpened(true);
                accordion.add(accordionPanel);
            }
        }
        Button btnCancel = new Button(
            ts.getTranslatedString("module.general.messages.cancel"), 
            clickEvent -> close()
        );
        btnCancel.setWidthFull();
        
        setContentSizeFull();
        setWidth("40%");
        setHeader(ts.getTranslatedString("module.ospman.external-services"));
        setContent(accordion);
        setFooter(btnCancel);
        super.open();
    }
    
    private boolean hasExternalServices(ExternalServiceProvider externalServiceProvider) {
        for (AbstractExternalService externalService : externalServiceProvider.getExternalServices()) {
            if (externalService instanceof AbstractInventoryExternalService) {
                String appliesTo = ((AbstractInventoryExternalService) externalService).appliesTo();
                if (appliesTo != null) {
                    try {
                        if (mem.isSubclassOf(appliesTo, businessObject.getClassName()))
                            return true;
                    } catch (MetadataObjectNotFoundException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, 
                            ts
                        ).open();
                    }
                }
                else
                    return true;
            }
        }
        return false;
    }
}
