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
package com.neotropic.kuwaiba.modules.commercial.processman.wdw;

import com.neotropic.kuwaiba.modules.commercial.processman.diagram.DiagramUtil;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.artifacts.AbstractArtifactRender;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.artifacts.AttachmentArtifactRender;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.artifacts.ConditionalArtifactRender;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.artifacts.FormArtifactRender;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FormArtifactPrinter;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementScript;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FormDefinitionLoader;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FunctionRunner;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FunctionRunnerException;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.Runner;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.artifacts.FormRender;
import com.neotropic.kuwaiba.modules.commercial.processman.scripts.ScriptQueryExecutorImpl;
import com.neotropic.kuwaiba.modules.commercial.whman.persistence.WarehousesService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Actor;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActorAuthorizationManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ArtifactDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ConditionalActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ParallelActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to create or edit an artifact
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ArtifactWindow extends ConfirmDialog {
    private final String processEnginePath;
    private final ProcessDefinition processDefinition;
    private final ProcessInstance processInstance;
    private final ActorAuthorizationManager actorAuthorizationManager;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final WarehousesService ws;
    private final TranslationService ts;
    private final ActivityDefinition activityDefinition;
    private final Command cmdUpdateDiagram;
    
    private ArtifactDefinition artifactDefinition;
    private Artifact artifact;
    private AbstractArtifactRender artifactRender;
    private Component artifactRendererComponent;
    private boolean isActivityRunningInParallel = false;
    
    public ArtifactWindow(String processEnginePath, ProcessDefinition processDefinition, ProcessInstance processInstance, 
        ActivityDefinition activityDefinition, ActorAuthorizationManager actorAuthorizationManager, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, WarehousesService ws, TranslationService ts, 
        Command cmdUpdateDiagram) throws InventoryException {
        
        Objects.requireNonNull(processDefinition);
        Objects.requireNonNull(processInstance);
        Objects.requireNonNull(activityDefinition);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(cmdUpdateDiagram);
        
        this.processEnginePath = processEnginePath;
        this.actorAuthorizationManager = actorAuthorizationManager;
        this.processDefinition = processDefinition;
        this.activityDefinition = activityDefinition;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.cmdUpdateDiagram = cmdUpdateDiagram;
        this.ws = ws;
        this.processInstance = aem.getProcessInstance(processInstance.getId());
    }

    @Override
    public void open() {
        setWidth("90%");
        setHeight("90%");
        setContentSizeFull();
        Session session = UI.getCurrent().getSession().getAttribute(Session.class);
        try {
            List<ActivityDefinition> path = aem.getProcessInstanceActivitiesPath(processInstance.getId());
            isActivityRunningInParallel = DiagramUtil.isActivityRunningInParallel(path, activityDefinition);
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        Consumer<FunctionRunnerException> consumerFuncRunnerEx = funcRunnerEx -> {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                funcRunnerEx.getFunctionName(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        };
        HashMap<String, Object> funcRunnerParams = new HashMap();
        funcRunnerParams.put("aem", aem); //NOI18N
        funcRunnerParams.put("bem", bem); //NOI18N
        funcRunnerParams.put("mem", mem); //NOI18N
        funcRunnerParams.put("warehouseService", ws); //NOI18N
        
        Command cmdUpdateActivity = () -> {
            try {
                if (artifact == null) {
                    artifact = new Artifact(
                        UUID.randomUUID().toString(), 
                        "", 
                        "", 
                        artifactRender.getContent(), 
                        artifactRender.getSharedInformation(), 
                        new Date().getTime(), 
                        new Date().getTime()
                    );
                } else {
                    artifact.setContent(artifactRender.getContent());
                    artifact.setSharedInformation(artifactRender.getSharedInformation());
                    artifact.setCommitDate(new Date().getTime());
                }
                aem.updateActivity(processInstance.getId(), activityDefinition.getId(), artifact);
                if (artifactDefinition != null && artifactDefinition.getPostconditionsScript() != null) {
                    String script = new String(artifactDefinition.getPostconditionsScript());

                    ElementScript elementScript = FormDefinitionLoader.loadExternalScripts(processEnginePath, artifactDefinition.getExternalScripts(), consumerFuncRunnerEx, funcRunnerParams);
                    FunctionRunner funcRunner = new FunctionRunner(FunctionRunner.FUNC_NAME_POSTCONDITIONS, null, script, elementScript, consumerFuncRunnerEx, funcRunnerParams);
                    ScriptQueryExecutorImpl scriptedQuery = new ScriptQueryExecutorImpl(processInstance, session, aem, bem, mem, ts);
                    if (elementScript != null) {
                        for (Runner runner : elementScript.getFunctions().values())
                            runner.setScriptQueryExecutor(scriptedQuery);
                    }
                    funcRunner.setScriptQueryExecutor(scriptedQuery);
                    funcRunner.setParametersNames(Arrays.asList(
                        FunctionRunner.PARAM_NAME_ACTIVITY_DEFINITION_ID,
                        FunctionRunner.PARAM_NAME_NEXT_ACTIVITY_DEFINITION_ID,
                        FunctionRunner.PARAM_NAME_PRINTABLE_TEMPLATE_INSTANCE,
                        FunctionRunner.PARAM_NAME_PROCESS_INSTANCE_ID
                    ));
                    funcRunner.run(Arrays.asList(
                        activityDefinition.getId(), 
                        activityDefinition.getNextActivity() != null ? activityDefinition.getNextActivity().getId() : null, 
                        null, 
                        processInstance.getId()
                    ));
                }
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        };
        Command cmdCommitActivity = () -> {
            try {
                artifact.setCommitDate(new Date().getTime());
                aem.commitActivity(processInstance.getId(), activityDefinition.getId(), artifact);
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        };
        ActionButton btnSave = new ActionButton(
            ts.getTranslatedString("module.processman.process-instance.wdw.btn.save"),
            VaadinIcon.DOWNLOAD.create(), 
            ts.getTranslatedString("module.processman.process-instance.wdw.btn.save")
        );
        btnSave.addClickListener(clickEvent -> {
            if (artifactRender.getSharedMap().containsKey(Artifact.SHARED_KEY_IDLE)) {
                if (!Boolean.valueOf(artifactRender.getSharedMap().get(Artifact.SHARED_KEY_IDLE)))
                    artifactRender.getSharedMap().put(Artifact.SHARED_KEY_IDLE_MODIFIED, String.valueOf(new Date().getTime()));
            }
            cmdUpdateActivity.execute();
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.information"), 
                ts.getTranslatedString("module.processman.process-instance.wdw.notification.saved"), 
                AbstractNotification.NotificationType.INFO, 
                ts
            ).open();
            
            if (isActivityRunningInParallel)
                close();
            if (artifactRender.getSharedMap().containsKey(Artifact.SHARED_KEY_IDLE)) {
                if (!Boolean.valueOf(artifactRender.getSharedMap().get(Artifact.SHARED_KEY_IDLE)))
                    close();
            }
            cmdUpdateDiagram.execute();
        });
        ActionButton btnNext = new ActionButton(
            ts.getTranslatedString("module.processman.process-instance.wdw.btn.commit"),
            VaadinIcon.STEP_FORWARD.create(), 
            ts.getTranslatedString("module.processman.process-instance.wdw.btn.commit")
        );
        btnNext.addClickListener(clickEvent -> {
            cmdUpdateActivity.execute();
            cmdCommitActivity.execute();
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.information"), 
                ts.getTranslatedString("module.processman.process-instance.wdw.notification.commited"), 
                AbstractNotification.NotificationType.INFO, 
                ts
            ).open();
            close();
            cmdUpdateDiagram.execute();
        });
        HorizontalLayout lyt = new HorizontalLayout();
        
        Label lblInterruptedActivity = new Label(ts.getTranslatedString("module.processman.process-instance.wdw.text.interrupted"));
        lblInterruptedActivity.setVisible(false);
        lyt.add(lblInterruptedActivity);
        
        Checkbox chkIdlingActivity = new Checkbox(ts.getTranslatedString("module.processman.process-instance.wdw.text.idling"));
        chkIdlingActivity.setVisible(activityDefinition.isIdling() || isActivityRunningInParallel);
        chkIdlingActivity.addValueChangeListener(valueChange -> 
            artifactRender.getSharedMap().put(Artifact.SHARED_KEY_IDLE, String.valueOf(!valueChange.getValue()))
        );
        lyt.add(chkIdlingActivity);
        
        if (activityDefinition.getArfifact() != null) {
            if (activityDefinition.getArfifact().isPrintable()) {
                
                ActionButton btnPrint = new ActionButton(
                    VaadinIcon.PRINT.create(), 
                    ts.getTranslatedString("module.processman.process-instance.wdw.btn.print")
                );
                btnPrint.addClickListener(clickEvent -> {
                    if (artifactRendererComponent instanceof FormRender) {
                        FormRender formRenderer = (FormRender) artifactRendererComponent;
                        FormArtifactPrinter artifactPrintable = new FormArtifactPrinter(
                            processEnginePath, artifactDefinition, formRenderer.getFormStructure(), ts
                        );
                        byte[] bytes = artifactPrintable.getBytes();
                        StreamResource streamResource = new StreamResource(
                            String.format("%s.html", activityDefinition.getName()), //NOI18N
                            () -> new ByteArrayInputStream(bytes)
                        );
                        streamResource.setContentType("text/html"); //NOI18N
                        VaadinSession.getCurrent().getResourceRegistry().registerResource(streamResource);
                        URI uri = VaadinSession.getCurrent().getResourceRegistry().getTargetURI(streamResource);
                        UI.getCurrent().getPage().open(uri.toString());
                    }
                });
                lyt.add(btnPrint);
            }
        }
        Scroller lytArtifact = new Scroller();
        lytArtifact.setSizeFull();
        
        UserProfile user = session.getUser();
        Actor actor = activityDefinition.getActor();
        if (actorAuthorizationManager.existGroup(user, actor)) {
            try {
                artifactDefinition = aem.getArtifactDefinitionForActivity(
                    processDefinition.getId(), activityDefinition.getId()
                );
                if (artifactDefinition != null && artifactDefinition.getDefinition() != null) {
                    if (artifactDefinition.getPreconditionsScript() != null) {
                        String script = new String(artifactDefinition.getPreconditionsScript());

                        ElementScript elementScript = FormDefinitionLoader.loadExternalScripts(processEnginePath, artifactDefinition.getExternalScripts(), consumerFuncRunnerEx, funcRunnerParams);
                        
                        ScriptQueryExecutorImpl scriptQueryExecutorImpl = new ScriptQueryExecutorImpl(processInstance, session, aem, bem, mem, ts);

                        FunctionRunner funcRunner = new FunctionRunner(FunctionRunner.FUNC_NAME_PRECONDITIONS, null, script, elementScript, consumerFuncRunnerEx, funcRunnerParams);
                        funcRunner.setScriptQueryExecutor(scriptQueryExecutorImpl);
                        
                        for (Runner runner : elementScript.getFunctions().values())
                            runner.setScriptQueryExecutor(scriptQueryExecutorImpl);
                        
                        Object result = funcRunner.run(null);
                        if (!Boolean.valueOf(result.toString())) {
                            lytArtifact.setContent(new Label(result.toString()));
                            return;
                        }
                    }
                    artifact = null;
                    try {
                        artifact = aem.getArtifactForActivity(
                            processInstance.getId(), activityDefinition.getId()
                        );
                    } catch (ApplicationObjectNotFoundException ex) {

                    }
                    artifactRender = null;
                    switch (artifactDefinition.getType()) {
                        case ArtifactDefinition.TYPE_ATTACHMENT:
                            artifactRender = new AttachmentArtifactRender(artifactDefinition, artifact, aem, ts);
                        break;
                        case ArtifactDefinition.TYPE_CONDITIONAL:
                            if (activityDefinition instanceof ConditionalActivityDefinition) {
                                artifactRender = new ConditionalArtifactRender(
                                    activityDefinition, artifactDefinition, artifact, 
                                    ((ConditionalActivityDefinition) activityDefinition).getInformationArtifact(), 
                                    processEnginePath, processInstance, aem, bem, mem, ts, consumerFuncRunnerEx, funcRunnerParams
                                );
                            }
                        break;
                        case ArtifactDefinition.TYPE_FORM:
                            artifactRender = new FormArtifactRender(processEnginePath, processInstance, artifactDefinition, artifact, aem, bem, mem, ts, consumerFuncRunnerEx, funcRunnerParams);
                        break;
                    }
                    if (artifactRender != null) {
                        if (artifact != null) {
                            for (StringPair pair : artifact.getSharedInformation()) {
                                if (Artifact.SHARED_KEY_IDLE.equals(pair.getKey())) {
                                    artifactRender.getSharedMap().put(Artifact.SHARED_KEY_IDLE, pair.getValue());
                                    chkIdlingActivity.setValue(!Boolean.valueOf(pair.getValue()));
                                } else if (Artifact.SHARED_KEY_INTERRUPTED.equals(pair.getKey())) {
                                    artifactRender.getSharedMap().put(Artifact.SHARED_KEY_INTERRUPTED, pair.getValue());
                                    lblInterruptedActivity.setVisible(Boolean.valueOf(pair.getValue()));
                                }
                            }
                        }
                        artifactRendererComponent = artifactRender.render();
                        lytArtifact.setContent(artifactRendererComponent);
                    }
                } else {
                    //TODO:
                }
            } catch (ApplicationObjectNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messagesArtifact.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        } else {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ts.getTranslatedString("module.processman.notification.text.user-not-authorized"), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        chkIdlingActivity.setEnabled(!chkIdlingActivity.getValue());
        if (!isActivityRunningInParallel) {
            btnSave.setVisible(activityDefinition.getId().equals(processInstance.getCurrentActivityId()));
            btnNext.setVisible(activityDefinition.getId().equals(processInstance.getCurrentActivityId()));
            
            if (activityDefinition.getType() == ActivityDefinition.TYPE_START ||
                activityDefinition instanceof ConditionalActivityDefinition ||
                (activityDefinition instanceof ParallelActivityDefinition && ((ParallelActivityDefinition) activityDefinition).getSequenceFlow() == ParallelActivityDefinition.JOIN) ||
                activityDefinition.getType() == ActivityDefinition.TYPE_END) {
                lyt.add(btnNext);
            }
            else
                lyt.add(btnSave, btnNext);
        }
        else
            lyt.add(btnSave);
        if (activityDefinition.isIdling() || isActivityRunningInParallel)
            btnSave.setVisible(!chkIdlingActivity.getValue());
        VerticalLayout lytContent = new VerticalLayout(lyt, lytArtifact);
        lytContent.setSizeFull();
        lytContent.setSpacing(false);
        lytContent.setHorizontalComponentAlignment(FlexComponent.Alignment.END, lyt);
        lytContent.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, lytArtifact);
        lytContent.expand(lytArtifact);
        
        ActionButton btnClose = new ActionButton(ts.getTranslatedString("module.general.messages.close"));
        btnClose.addClickListener(clickEvent -> close());
        btnClose.setWidthFull();
        
        Image imgHeader = null;
        switch (activityDefinition.getType()) {
            case ActivityDefinition.TYPE_START:
                imgHeader = new Image("MXGRAPH/images/event.png", "");
            break;
            case ActivityDefinition.TYPE_NORMAL:
                imgHeader = new Image("MXGRAPH/images/task.png", "");
            break;
            case ActivityDefinition.TYPE_CONDITIONAL:
                imgHeader = new Image("MXGRAPH/images/exclusive.png", "");
            break;
            case ActivityDefinition.TYPE_PARALLEL:
                imgHeader = new Image("MXGRAPH/images/fork.png", "");
            break;
            case ActivityDefinition.TYPE_END:
                imgHeader = new Image("MXGRAPH/images/event_end.png", "");
            break;
        }
        Label lblHeader = new Label(String.format("%s > %s", processDefinition.getName(), activityDefinition.getName()));
        HorizontalLayout lytHeader = new HorizontalLayout();
        lytHeader.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        if (imgHeader != null) {
            imgHeader.setWidth("24px");
            imgHeader.setHeight("24px");
            if (activityDefinition.isIdling()) {
                Image imgIdling = new Image("MXGRAPH/images/timer.png", "");
                imgIdling.setWidth("18px");
                imgIdling.setHeight("18px");
                HorizontalLayout lytImgs = new HorizontalLayout(imgHeader, imgIdling);
                lytImgs.setSpacing(false);
                lytImgs.getThemeList().set("spacing-xs", true);
                lytImgs.setVerticalComponentAlignment(FlexComponent.Alignment.START, imgIdling);
                lytHeader.add(lytImgs, lblHeader);
            }
            else
                lytHeader.add(imgHeader, lblHeader);
        } else
            lytHeader.add(lblHeader);
        setHeader(lytHeader);
        setContent(lytContent);
        setFooter(btnClose);

        setCloseOnOutsideClick(false);
        setModal(true);
        setDraggable(false);
        super.open();
    }
}
