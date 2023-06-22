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

import com.neotropic.flow.component.gantt.Gantt;
import com.neotropic.kuwaiba.modules.commercial.processman.diagram.ProcessInstanceTimeline;
import com.vaadin.flow.component.html.Label;
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
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.KpiManagerService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shows the process instance timeline.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Route(value="process-timeline", layout= ProcessManagerLayout.class)
public class ProcessTimelineUi extends VerticalLayout implements AbstractUI, HasDynamicTitle, BeforeEnterObserver {
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the KPI Manager Service.
     */
    @Autowired
    private KpiManagerService kpiManagerService;
    
    private ProcessInstance processInstance;
    
    public ProcessTimelineUi() {
        setSizeFull();
        setPadding(false);
        setMargin(false);
        setSpacing(false);
    }
    
    @Override
    public String getPageTitle() {
        return String.format(ts.getTranslatedString("module.processman.ui.timeline.title"), processInstance.getName());
    }
    
    @Override
    public void initContent() {
        if (processInstance == null)
            return;
        try {
            List<ActivityDefinition> path = aem.getProcessInstanceActivitiesPath(processInstance.getId());
            
            if (!path.isEmpty()) {
                ProcessDefinition processDefinition = aem.getProcessDefinition(processInstance.getProcessDefinitionId());
                
                Label lblHeader = new Label(processDefinition.getName());
                ActionButton btnDiagram = new ActionButton(
                    VaadinIcon.EDIT.create(), 
                    ts.getTranslatedString("module.processman.tooltip.open-process-diagram")
                );
                btnDiagram.addClickListener(clickEvent -> {
                    getUI().ifPresent(ui -> {
                        ui.getPage().open(
                            String.format("%s?%s=%s", RouteConfiguration.forSessionScope().getUrl(ProcessDiagramUi.class), Constants.PROPERTY_ID, processInstance.getId()),
                            "_blank" //NOI18N
                        );
                    });
                });
                HorizontalLayout lytHeader = new HorizontalLayout(lblHeader, btnDiagram);
                lytHeader.setWidthFull();
                lytHeader.expand(lblHeader);
                
                Gantt gantt = new Gantt(new ProcessInstanceTimeline(path, processDefinition, processInstance, aem, ts, kpiManagerService));
                
                add(lytHeader);
                add(gantt);
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ts.getTranslatedString(ex.getLocalizedMessage()), 
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
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ts.getTranslatedString(ex.getLocalizedMessage()), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        }
    }
}
