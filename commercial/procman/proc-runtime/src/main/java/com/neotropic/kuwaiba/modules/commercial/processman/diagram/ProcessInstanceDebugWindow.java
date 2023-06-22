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
package com.neotropic.kuwaiba.modules.commercial.processman.diagram;

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to provide a set of tools to debug a process instance.
 * The set of tools provide are listed below:
 * Tool to update the artifacts date: updates the artifact creation date and end date
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ProcessInstanceDebugWindow extends ConfirmDialog {
    private final ProcessInstance processInstance;
    private final ApplicationEntityManager aem;
    private final TranslationService ts;
    
    public ProcessInstanceDebugWindow(ProcessInstance processInstance, ApplicationEntityManager aem, TranslationService ts) {
        this.aem = aem;
        this.ts = ts;
        this.processInstance = processInstance;
        
        setWidth("90%");
        setHeight("90%");
        setContentSizeFull();
    }

    @Override
    public void open() {
        try {
            List<ActivityDefinition> path = aem.getProcessInstanceActivitiesPath(processInstance.getId());
            
            Div divContent = new Div();
            divContent.setWidthFull();
            divContent.getStyle().set("display", "grid"); //NOI18N
            divContent.getStyle().set("grid-gap", "10px"); //NOI18N
            divContent.getStyle().set("grid-template-columns", "auto auto auto"); //NOI18N
            
            divContent.add(
                new H5(ts.getTranslatedString("module.processman.debug-process-instance.wdw.column-header.activity")),
                new H5(ts.getTranslatedString("module.processman.debug-process-instance.wdw.column-header.start-date")),
                new H5(ts.getTranslatedString("module.processman.debug-process-instance.wdw.column-header.end-date"))
            );
            LinkedHashMap<ActivityDefinition, Artifact> artifacts = new LinkedHashMap();
            List<ActivityDefinition> updateStarDate = new ArrayList();
            List<ActivityDefinition> updateEndDate = new ArrayList();
            
            path.forEach(activityDefinition -> {
                DateTimePicker dtpStartDate = new DateTimePicker();
                dtpStartDate.setStep(Duration.ofSeconds(1));
                
                DateTimePicker dtpEndDate = new DateTimePicker();
                dtpEndDate.setStep(Duration.ofSeconds(1));
                
                try {
                    Artifact artifact = aem.getArtifactForActivity(processInstance.getId(), activityDefinition.getId());
                    
                    dtpStartDate.setValue(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(artifact.getCreationDate()),
                        TimeZone.getDefault().toZoneId())
                    );
                    dtpEndDate.setValue(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(artifact.getCommitDate()),
                        TimeZone.getDefault().toZoneId())
                    );
                    dtpStartDate.addValueChangeListener(valueChangeEvent -> {
                        if (valueChangeEvent.getValue() != null) {
                            ZonedDateTime zonedDateTime = ZonedDateTime.of(valueChangeEvent.getValue(), ZoneId.systemDefault());
                            artifact.setCreationDate(zonedDateTime.toInstant().toEpochMilli());
                            
                            if (!updateStarDate.contains(activityDefinition))
                                updateStarDate.add(activityDefinition);
                        }
                    });
                    dtpEndDate.addValueChangeListener(valueChangeEvent -> {
                        if (valueChangeEvent.getValue() != null) {
                            ZonedDateTime zonedDateTime = ZonedDateTime.of(valueChangeEvent.getValue(), ZoneId.systemDefault());
                            artifact.setCommitDate(zonedDateTime.toInstant().toEpochMilli());
                            
                            if (!updateEndDate.contains(activityDefinition))
                                updateEndDate.add(activityDefinition);
                        }
                    });
                    artifacts.put(activityDefinition, artifact);
                } catch (ApplicationObjectNotFoundException ex) {
                    dtpStartDate.setEnabled(false);
                    dtpEndDate.setEnabled(false);
                }
                divContent.add(new Label(activityDefinition.getName()) , dtpStartDate, dtpEndDate);
            });
            
            ActionButton btnClose = new ActionButton(ts.getTranslatedString("module.general.messages.close"));
            btnClose.setSizeFull();
            btnClose.addClickListener(clickEvent -> close());
            
            ActionButton btnUpdateDates = new ActionButton(ts.getTranslatedString("module.processman.debug-process-instance.wdw.btn.update-button"));
            btnUpdateDates.setSizeFull();
            btnUpdateDates.addClickListener(clickEvent -> {
                artifacts.forEach((activityDefinition, artifact) -> {
                    try {
                        if (artifact.getCommitDate() > artifact.getCreationDate()) {
                            if (activityDefinition.isIdling()) {
                                StringPair sharedInfoIdleModified = null;
                                for (StringPair sharedInfo : artifact.getSharedInformation()) {
                                    if (Artifact.SHARED_KEY_IDLE_MODIFIED.equals(sharedInfo.getKey()))
                                        sharedInfoIdleModified = sharedInfo;
                                }
                                if (sharedInfoIdleModified != null)
                                    sharedInfoIdleModified.setValue(String.valueOf(artifact.getCommitDate()));
                                else {
                                    artifact.getSharedInformation().add(new StringPair(
                                        Artifact.SHARED_KEY_IDLE_MODIFIED, 
                                        String.valueOf(artifact.getCommitDate())
                                    ));
                                }
                            }
                            if (updateStarDate.contains(activityDefinition))
                                aem.updateActivity(processInstance.getId(), activityDefinition.getId(), artifact);
                            else if (updateEndDate.contains(activityDefinition))
                                aem.updateActivity(processInstance.getId(), activityDefinition.getId(), artifact);
                        } else {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.information"), 
                                String.format(ts.getTranslatedString("module.processman.debug-process-instance.wdw.btn.update-button.update-activity"), activityDefinition.getName()), 
                                AbstractNotification.NotificationType.INFO, 
                                ts
                            ).open();
                        }
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, 
                            ts
                        ).open();
                    }
                });
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    ts.getTranslatedString("module.processman.debug-process-instance.wdw.btn.update-button.activity-updated"), 
                    AbstractNotification.NotificationType.INFO, 
                    ts
                ).open();
            });
            HorizontalLayout lytFooter = new HorizontalLayout(btnClose, btnUpdateDates);
            lytFooter.setSizeFull();
            
            setHeader(ts.getTranslatedString("module.processman.debug-process-instance.wdw.header"));
            setContent(divContent);
            setFooter(lytFooter);
            super.open();
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
