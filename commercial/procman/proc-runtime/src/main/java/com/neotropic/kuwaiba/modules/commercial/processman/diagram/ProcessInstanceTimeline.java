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

import com.neotropic.flow.component.gantt.model.GanttActivity;
import com.neotropic.flow.component.gantt.model.GanttChart;
import com.neotropic.flow.component.gantt.model.GanttColumn;
import com.neotropic.flow.component.gantt.services.ProjectsService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.KpiResult;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.KpiManagerService;

/**
 * Class to build a process instance timeline.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ProcessInstanceTimeline implements ProjectsService {
    private final List<ActivityDefinition> path;
    private final ProcessDefinition processDefinition;
    private final ProcessInstance processInstance;
    private final ApplicationEntityManager aem;
    private final TranslationService ts;
    private final KpiManagerService kpiManagerService;
    private static final String PROPERTY_ACTIVITY = "activity";
    private static final String PROPERTY_KPI_TIME = "kpiTime";
    private static final String DUMMY_PROPERTY_KPI_TIME = "dummyPropertyKpiTime";
    private static final String PROPERTY_REAL_EXPECTED = "realExpected";
    private static final String PROPERTY_ACTOR = "actor";
    private static final String PROPERTY_START_DATE = "startDate";
    private static final String PROPERTY_END_DATE = "endDate";
    private static final String PROPERTY_EXPECTED_DURATION = "expectedDuration";
    private static final String PROPERTY_REAL_DURATION = "realDuration";
    private static final String COLUMN_EXPRESSION = "object.%s";
    private static final String KPI_TIME = "time";
    
    public ProcessInstanceTimeline(List<ActivityDefinition> path, ProcessDefinition processDefinition, ProcessInstance processInstance, ApplicationEntityManager aem, TranslationService ts, KpiManagerService kpiManagerService) throws InventoryException {
        this.processDefinition = processDefinition;
        this.processInstance = processInstance;
        this.path = path;
        this.aem = aem;
        this.ts = ts;
        this.kpiManagerService = kpiManagerService;
    }
    
    
    @Override
    public String createProject() {
        GanttChart gantt;
        List<GanttActivity> ganttActivities = new ArrayList();
        for (ActivityDefinition activityDef : path) {
            LinkedHashMap<String, Object> properties = new LinkedHashMap();
            properties.put(PROPERTY_ACTOR, activityDef.getActor().getName());
            properties.put(DUMMY_PROPERTY_KPI_TIME, "");
            Artifact artifact = null;
            try {
                artifact = aem.getArtifactForActivity(processInstance.getId(), activityDef.getId());
            } catch (InventoryException ex) {
                // Nothing to do here
            }
            if (artifact != null  && artifact.getCreationDate() > 0 && artifact.getCommitDate() > 0 ) {
                KpiResult kpiResult = kpiManagerService.runActivityKpiAction(KPI_TIME, artifact, processDefinition, activityDef);
                if (kpiResult != null) {
                    switch (kpiResult.getComplianceLevel()) {
                        case 10:
                            properties.put(PROPERTY_KPI_TIME, "warning"); //NOI18N
                            break;
                        case 5:
                            properties.put(PROPERTY_KPI_TIME, "normal"); //NOI18N
                            break;
                        case 0:
                            properties.put(PROPERTY_KPI_TIME, "critical"); //NOI18N
                            break;
                        default:
                            properties.put(PROPERTY_KPI_TIME, "null"); //NOI18N
                            break;
                    }
                }
                else
                    properties.put(PROPERTY_KPI_TIME, "null"); //NOI18N
                
                long start = artifact.getCreationDate();
                long end = artifact.getCommitDate();
                
                String idle = null;
                String idleModified = null;
                
                for (StringPair sharedInfo : artifact.getSharedInformation()) {
                    if (sharedInfo.getKey().equals(Artifact.SHARED_KEY_IDLE))
                        idle = sharedInfo.getValue();
                    else if (sharedInfo.getKey().equals(Artifact.SHARED_KEY_IDLE_MODIFIED))
                        idleModified = sharedInfo.getValue();
                }
                if (idle != null && !Boolean.valueOf(idle) && idleModified != null)
                    end = Long.valueOf(idleModified);
                
                if (start == end)
                    ganttActivities.add(new GanttActivity(activityDef.getId(), activityDef.getName(), start, new Date().getTime(), "", properties));
                else
                    ganttActivities.add(new GanttActivity(activityDef.getId(), activityDef.getName(), start, end, "", properties));
            } else {
                Date date = new Date();
                ganttActivities.add(new GanttActivity(activityDef.getId(), activityDef.getName(), date.getTime(), date.getTime(), "", properties));
            }
        }
        
        List<GanttColumn> columns = new ArrayList();
        GanttColumn columnActor = new GanttColumn(
            ts.getTranslatedString("module.processman.timeline.column.actor"), 
            String.format(COLUMN_EXPRESSION, PROPERTY_ACTOR)
        );
        GanttColumn columKpiTime = new GanttColumn(" ", String.format(COLUMN_EXPRESSION, DUMMY_PROPERTY_KPI_TIME));
        HashMap<String, String> palette = new HashMap();
        palette.put("null", "transparent"); //NOI18N
        palette.put("warning", "#f7eea0"); //NOI18N
        palette.put("normal", "#bffcb3"); //NOI18N
        palette.put("critical", "#db9090"); //NOI18N
        columKpiTime.setBackground(String.format(COLUMN_EXPRESSION, PROPERTY_KPI_TIME), palette);
        
        columns.add(columKpiTime);
        columns.add(columnActor);
        
        gantt = new GanttChart(ganttActivities, columns, null);
        /*
        KPI
        Real - Expected
        Expected Duration
        Real Duration
        */
        return gantt.activityChart();
    }
}