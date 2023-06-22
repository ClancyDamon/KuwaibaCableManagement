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
package com.neotropic.kuwaiba.modules.commercial.processman;

import com.neotropic.kuwaiba.modules.commercial.processman.diagram.ProcessInstanceDiagram;
import com.neotropic.kuwaiba.modules.commercial.processman.wdw.ProcessInstanceEditorWindow;
import com.neotropic.kuwaiba.modules.commercial.whman.persistence.WarehousesService;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActorAuthorizationManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Manages a process instance using the process diagram.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Route(value="process-diagram", layout= ProcessManagerLayout.class)
public class ProcessDiagramUi extends VerticalLayout implements AbstractUI, HasDynamicTitle, BeforeEnterObserver {
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
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Warehouse Service.
     */
    @Autowired
    private WarehousesService warehousesService;
    /**
     * Reference to the Actor Authorization Manager.
     */
    @Autowired
    private ActorAuthorizationManager actorAuthorizationManager;
    
    @Value("${aem.process-engine-path}")
    private String processEnginePath;
    
    private ProcessInstance processInstance;
    
    public ProcessDiagramUi() {
        setSizeFull();
        setPadding(false);
        setMargin(false);
        setSpacing(false);
    }
    
    @Override
    public String getPageTitle() {
        return String.format(ts.getTranslatedString("module.processman.ui.diagram.title"), processInstance.getName());
    }

    @Override
    public void initContent() {
        if (processInstance == null)
            return;
        try {
            ProcessDefinition processDefinition = aem.getProcessDefinition(processInstance.getProcessDefinitionId());
            
            BoldLabel lblHeader = new BoldLabel(String.format("%s : %s", processDefinition.getName(), processInstance.getName()));
                        
            ActionButton btnEdit = new ActionButton(
                VaadinIcon.EDIT.create(),
                ts.getTranslatedString("module.processman.process-instance.edit")
            );
            btnEdit.addClickListener(clickEvent -> {
                new ProcessInstanceEditorWindow(processInstance, aem, ts, () -> {
                    try {
                        processInstance = aem.getProcessInstance(processInstance.getId());
                        lblHeader.setText(String.format("%s : %s", processDefinition.getName(), processInstance.getName()));
                    } catch (ApplicationObjectNotFoundException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, 
                            ts
                        ).open();
                    }
                }).open();
            });
            ActionButton btnOutline = new ActionButton(
                VaadinIcon.SPLIT_V.create(),
                ts.getTranslatedString("module.processman.process-instance.outline")
            );
            ActionButton btnTimeline = new ActionButton(
                VaadinIcon.CLOCK.create(), 
                ts.getTranslatedString("module.processman.tooltip.open-process-timeline")
            );
            btnTimeline.addClickListener(clickEvent -> {
                getUI().ifPresent(ui -> {
                    ui.getPage().open(
                        String.format("%s?%s=%s", RouteConfiguration.forSessionScope().getUrl(ProcessTimelineUi.class), Constants.PROPERTY_ID, processInstance.getId()),
                        "_blank" //NOI18N
                    );
                });
            });
            aem.updateProcessInstance(processEnginePath, processEnginePath, processEnginePath);
            btnEdit.getStyle().set("margin-left", "3px"); //NOI18N
            btnTimeline.getStyle().set("margin-left", "3px"); //NOI18N
            btnOutline.getStyle().set("margin-left", "3px"); //NOI18N
            HorizontalLayout lytTools = new HorizontalLayout(btnEdit, btnTimeline, btnOutline);
            lytTools.setPadding(false);
            lytTools.setMargin(false);
            lytTools.setSpacing(false);
            
            HorizontalLayout lytHeader = new HorizontalLayout(lytTools, lblHeader);
            lytHeader.setWidthFull();
            lytHeader.setVerticalComponentAlignment(Alignment.CENTER, lblHeader);
            
            ProcessInstanceDiagram diagram = new ProcessInstanceDiagram(
                processEnginePath, processDefinition, processInstance, 
                actorAuthorizationManager, aem, bem, mem, warehousesService, ts,
                btnOutline
            );
            add(lytHeader);
            add(diagram);
        } catch (ApplicationObjectNotFoundException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        List<String> parameters = event.getLocation().getQueryParameters().getParameters().get(Constants.PROPERTY_ID);
        if (parameters != null && !parameters.isEmpty()) {
            try {
                processInstance = aem.getProcessInstance(parameters.get(0));
            } catch (ApplicationObjectNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        }
    }
}
