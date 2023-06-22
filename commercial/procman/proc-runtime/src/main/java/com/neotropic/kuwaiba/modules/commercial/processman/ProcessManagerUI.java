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

import com.neotropic.kuwaiba.modules.commercial.processman.diagram.ProcessDefinitionDiagram;
import com.neotropic.kuwaiba.modules.commercial.processman.diagram.ProcessInstanceDebugWindow;
import com.neotropic.kuwaiba.modules.commercial.processman.wdw.NewProcessInstanceWindow;
import com.neotropic.kuwaiba.modules.commercial.processman.wdw.ProcessInstanceEditorWindow;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.ProcessManagerService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Actor;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActorAuthorizationManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Main UI for the process manager
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Route(value = "processman", layout = ProcessManagerLayout.class)
public class ProcessManagerUI extends VerticalLayout implements HasDynamicTitle, AbstractUI {
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
     * Reference to the process manager service.
     */
    @Autowired
    private ProcessManagerService processManagerService;
    /**
     * Reference to the actor authorization manager.
     */
    @Autowired
    private ActorAuthorizationManager actorAuthorizationManager;
    
    @Value("${process-manager.debug-mode}")
    private boolean debugMode;
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.processman.title");
    }
    
    @Override
    public void initContent() {
        List<ProcessDefinition> processDefinitions = processManagerService.getProcessDefinitions();
        processDefinitions.removeIf(processDefinition -> !processDefinition.isEnabled());
        
        ComboBox<ProcessDefinition> cmbProcessDefinition = new ComboBox();
        cmbProcessDefinition.setPlaceholder(ts.getTranslatedString("module.processman.process-definition-filter.placeholder"));
        cmbProcessDefinition.setRenderer(new TextRenderer<>(ProcessDefinition::getName));
        cmbProcessDefinition.setItems(processDefinitions);
        cmbProcessDefinition.setClearButtonVisible(true);
        cmbProcessDefinition.setItemLabelGenerator(ProcessDefinition::getName);
        
        ActionButton btnProcessDefinitionDiagram = new ActionButton(
            VaadinIcon.EYE.create(), 
            ts.getTranslatedString("module.processman.tooltip.open-process-definition-diagram")
        );
        btnProcessDefinitionDiagram.setVisible(false);
        btnProcessDefinitionDiagram.addClickListener(clickEvent -> {
            if (cmbProcessDefinition.getValue() != null) {
                ConfirmDialog wdw = new ConfirmDialog();
                wdw.setContentSizeFull();
                wdw.setWidth("90%");
                wdw.setHeight("90%");

                ProcessDefinitionDiagram processDefinitionDiagram = new ProcessDefinitionDiagram(cmbProcessDefinition.getValue(), ts);
                
                Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), e -> wdw.close());
                btnClose.setSizeFull();
                
                wdw.setHeader(String.format(
                    ts.getTranslatedString("module.processman.process-definition.wdw.header"), 
                    cmbProcessDefinition.getValue().getName()
                ));
                wdw.setContent(processDefinitionDiagram);
                wdw.setFooter(btnClose);
                wdw.open();
            }
        });
        ActionButton btnReload = new ActionButton(
            VaadinIcon.REFRESH.create(), 
            ts.getTranslatedString("module.processman.reload-process-definitions")
        );
        btnReload.addClickListener(clickEvent -> {
            try {
                aem.reloadProcessDefinitions();
                cmbProcessDefinition.clear();
                List<ProcessDefinition> lstProcessDefinitions = processManagerService.getProcessDefinitions();
                lstProcessDefinitions.removeIf(processDefinition -> !processDefinition.isEnabled());
                
                cmbProcessDefinition.setItems(lstProcessDefinitions);
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    ts.getTranslatedString("module.processman.process-definitions-reloaded"), 
                    AbstractNotification.NotificationType.INFO, 
                    ts
                ).open();
            } catch (InvalidArgumentException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ts.getTranslatedString("module.processman.notification.text.user-not-authorized"), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        });
        Button btnNewProcessInstance = new Button(
            ts.getTranslatedString("module.processman.button.new-process"),
            VaadinIcon.PLUS_SQUARE_O.create()
        );
        btnNewProcessInstance.getElement().setProperty("title", ts.getTranslatedString("module.processman.button.new-process"));
        btnNewProcessInstance.setVisible(false);
        btnNewProcessInstance.setIconAfterText(true);
        
        Grid<ProcessInstance> tblProcessInstances = new Grid();
        tblProcessInstances.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        
        Consumer<ProcessDefinition> consumerUpdateUi = processDefinition -> {
            if (processDefinition != null) {
                btnProcessDefinitionDiagram.setVisible(true);
                try {
                    List<ProcessInstance> processInstances = aem.getProcessInstances(processDefinition.getId());
                    
                    btnNewProcessInstance.setVisible(true);
                    
                    if (!processInstances.isEmpty()) {
                        tblProcessInstances.setItems(processInstances);
                        tblProcessInstances.setVisible(true);
                    }
                    else
                        tblProcessInstances.setVisible(false);
                    
                } catch (ApplicationObjectNotFoundException ex) {
                    btnNewProcessInstance.setVisible(false);
                    tblProcessInstances.setVisible(false);
                    
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            } else {
                btnProcessDefinitionDiagram.setVisible(false);
                btnNewProcessInstance.setVisible(false);
                tblProcessInstances.setVisible(false);
            }
        };
        tblProcessInstances.setWidth("80%");
       
        tblProcessInstances.addColumn(ProcessInstance::getName)
            .setHeader(new BoldLabel(ts.getTranslatedString("module.processman.new-process-instance.window.content.name")))
            .setResizable(true);
        tblProcessInstances.addColumn(ProcessInstance::getDescription)
            .setHeader(new BoldLabel(ts.getTranslatedString("module.processman.new-process-instance.window.content.description")))
            .setResizable(true);
        tblProcessInstances.addColumn(processInstance -> {
            ActivityDefinition activityDefinition = aem.getActivityDefinition(processInstance.getProcessDefinitionId(), processInstance.getCurrentActivityId());
            if (activityDefinition != null)
                return activityDefinition.getName();
            return "";
        })
            .setHeader(new BoldLabel(ts.getTranslatedString("module.processman.process-instances.table.column.id.current-activity")))
            .setResizable(true);
        tblProcessInstances.addColumn(processInstance -> {
            try {
                List<ActivityDefinition> activityDefinitions = aem.getProcessInstanceActivitiesPath(processInstance.getId());
                if (!activityDefinitions.isEmpty()) {
                    if (activityDefinitions.size() == 1)
                        return ts.getTranslatedString("module.processman.process-instance.state.created");
                    else if (activityDefinitions.get(activityDefinitions.size() - 1).getType() == ActivityDefinition.TYPE_END)
                        return ts.getTranslatedString("module.processman.process-instance.state.finished");
                    else
                        return ts.getTranslatedString("module.processman.process-instance.state.running");
                }
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
            return "";
        })
            .setHeader(new BoldLabel(ts.getTranslatedString("module.processman.process-instances.table.column.id.state")))
            .setResizable(true);
        
        tblProcessInstances.addComponentColumn(processInstance -> {
            ActionButton btnEditProcessInstance = new ActionButton(
                VaadinIcon.EDIT.create(),
                ts.getTranslatedString("module.processman.process-instance.edit")
            );
            btnEditProcessInstance.addClickListener(clickEvent -> {
                new ProcessInstanceEditorWindow(processInstance, aem, ts, () -> {
                    try {
                        ProcessInstance currentProcessInstance = aem.getProcessInstance(processInstance.getId());
                        processInstance.setName(currentProcessInstance.getName());
                        processInstance.setDescription(currentProcessInstance.getDescription());
                        tblProcessInstances.getDataProvider().refreshItem(processInstance);
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
            ActionButton btnDeleteProcessInstance = new ActionButton(
                VaadinIcon.TRASH.create(), 
                ts.getTranslatedString("module.processman.tooltip.delete-process-instance")
            );
            btnDeleteProcessInstance.addClickListener(clickEvent -> {
                new ConfirmDialog(ts, 
                    ts.getTranslatedString("module.general.labels.confirmation"), 
                    new Label(String.format(ts.getTranslatedString("module.processman.confirm-dialog.content.delete-process-instance"), processInstance.getName())), 
                    ts.getTranslatedString("module.general.messages.ok"), 
                    () -> {
                        try {
                            aem.deleteProcessInstance(processInstance.getId());
                            consumerUpdateUi.accept(cmbProcessDefinition.getValue());
                        } catch (OperationNotPermittedException ex) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getLocalizedMessage(), 
                                AbstractNotification.NotificationType.ERROR, 
                                ts
                            ).open();
                        }
                    }
                ).open();
            });
            ActionButton btnDebugProcessInstance = new ActionButton(
                VaadinIcon.BUG_O.create(), 
                ts.getTranslatedString("module.processman.button.debug-process-instance")
            );
            btnDebugProcessInstance.setVisible(debugMode);
            btnDebugProcessInstance.addClickListener(clickEvent -> 
                new ProcessInstanceDebugWindow(processInstance, aem, ts).open()
            );
            HorizontalLayout lyt = new HorizontalLayout(
                btnEditProcessInstance, 
                btnTimeline,
                btnDeleteProcessInstance,
                btnDebugProcessInstance
            );
            VerticalLayout lytCol = new VerticalLayout(lyt);
            lytCol.setSizeFull();
            lytCol.setHorizontalComponentAlignment(Alignment.END, lyt);
            return lytCol;
        });
        tblProcessInstances.setSelectionMode(Grid.SelectionMode.SINGLE);
        tblProcessInstances.asSingleSelect().addValueChangeListener(valueChangeEvent -> {
            ProcessInstance processInstance = valueChangeEvent.getValue();
            if (processInstance != null)
                tblProcessInstances.asSingleSelect().clear();
        });
        tblProcessInstances.setVisible(false);
        tblProcessInstances.addSelectionListener(selection -> {
            Optional<ProcessInstance> optionalProcessInstance = selection.getFirstSelectedItem();
            if (optionalProcessInstance.isPresent()) {
                ProcessInstance processInstance = optionalProcessInstance.get();
                getUI().ifPresent(ui -> {
                    ui.getPage().open(
                        String.format("%s?%s=%s", RouteConfiguration.forSessionScope().getUrl(ProcessDiagramUi.class), Constants.PROPERTY_ID, processInstance.getId()), 
                        "_blank" //NOI18N
                    );
                });
            }
        });
        
        cmbProcessDefinition.addValueChangeListener(valueChangeEvent -> 
            consumerUpdateUi.accept(valueChangeEvent.getValue())
        );
        btnNewProcessInstance.addClickListener(clickEvent -> {
            UserProfile user = UI.getCurrent().getSession().getAttribute(Session.class).getUser();
            ProcessDefinition processDefinition = cmbProcessDefinition.getValue();
            ActivityDefinition activityDefinition = processDefinition.getStartActivity();
            if (activityDefinition != null) {
                Actor actor = activityDefinition.getActor();
                if (actorAuthorizationManager.existGroup(user, actor)) {
                    NewProcessInstanceWindow wdwNewProcessInstance = new NewProcessInstanceWindow(
                        cmbProcessDefinition.getValue(), aem, ts
                    );
                    wdwNewProcessInstance.open();
                    wdwNewProcessInstance.addOpenedChangeListener(openedChangeEvent -> {
                        if (!openedChangeEvent.isOpened())
                            consumerUpdateUi.accept(processDefinition);
                    });
                } else {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ts.getTranslatedString("module.processman.notification.text.user-not-authorized"), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            } else {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ts.getTranslatedString("module.processman.notification.text.cannot-find-start-activity"), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        });
        HorizontalLayout lyt = new HorizontalLayout(cmbProcessDefinition, btnReload, btnProcessDefinitionDiagram);
        lyt.setWidthFull();
        lyt.expand(cmbProcessDefinition);
        lyt.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        
        VerticalLayout lytProcessDefinitions = new VerticalLayout(lyt);
        lytProcessDefinitions.setWidth("80%");
        
        HorizontalLayout lytProcessInstance = new HorizontalLayout(btnNewProcessInstance);
        lytProcessInstance.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        
        add(lytProcessDefinitions, lytProcessInstance, tblProcessInstances);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setHorizontalComponentAlignment(Alignment.CENTER, tblProcessInstances);
        setSizeFull();
        // TODO: Lock UI if there are not process definitions
        // TODO: Corregir el uso del process definition id, porque esta quemado
    }
}
