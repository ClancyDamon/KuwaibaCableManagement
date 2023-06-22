/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        https://apache.org/licenses/LICENSE-2.0.txt
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.neotropic.kuwaiba.core.persistence.reference.extras.processman;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Actor;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ArtifactDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ConditionalActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Kpi;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.KpiAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ParallelActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ProcessDefinitionLoader {
    public class Tag {
        public static final String PROCESS_DEFINITION = "processDefinition"; //NOI18N
        public static final String ACTIVITY_DEFINITIONS = "activityDefinitions"; //NOI18N
        public static final String ACTIVITY_DEFINITION = "activityDefinition"; //NOI18N
        public static final String ARTIFACT_DEFINITION = "artifactDefinition"; //NOI18N
        public static final String DEFINITION = "definition"; //NOI18N
        public static final String ACTORS = "actors"; //NOI18N
        public static final String ACTOR = "actor"; //NOI18N
        public static final String PATHS = "paths"; //NOI18N
        public static final String PATH = "path"; //NOI18N
        public static final String PARAMETERS = "parameters"; //NOI18N
        public static final String PARAMETER = "parameter"; //NOI18N
        public static final String KPIS = "kpis"; //NOI18N
        public static final String KPI = "kpi"; //NOI18N
        public static final String ACTIONS = "actions"; //NOI18N
        public static final String ACTION = "action"; //NOI18N
        public static final String THRESHOLD = "threshold"; //NOI18N
        public static final String BPMNDIAGRAM = "bpmnDiagram"; //NOI18N
        public static final String BPMNSHAPE = "bpmnShape"; //NOI18N
        public static final String BPMNLABEL = "bpmnLabel"; //NOI18N
        public static final String BPMNSWIMLANE = "bpmnSwimlane"; //NOI18N
        public static final String BPMNEDGE = "bpmnEdge";
        public static final String CONTROLPOINT = "controlPoint";
    }
    
    public class Attribute {
        public static final String NAME = "name"; //NOI18N
        public static final String DESCRIPTION = "description"; //NOI18N
        public static final String CREATION_DATE = "creationDate"; //NOI18N
        public static final String VERSION = "version"; //NOI18N
        public static final String ENABLED = "enabled"; //NOI18N
        public static final String ID = "id"; //NOI18N
        public static final String TYPE = "type"; //NOI18N
        public static final String SHARED = "shared"; //NOI18N
        public static final String NEXT_ACTIVITY = "nextActivity"; //NOI18N
        public static final String START_ACTIVITY_ID="startActivityId"; //NOI18N
        public static final String NEXT_ACTIVITY_DEFINITION_ID = "nextActivityDefinitionId"; //NOI18N
        public static final String ACTOR_ID = "actorId"; //NOI18N
        public static final String IDLING = "idling"; //NOI18N
        public static final String CONFIRM = "confirm"; //NOI18N
        public static final String COLOR = "color"; //NOI18N
        public static final String ACTION = "action"; //NOI18N
        public static final String VALUE = "value"; //NOI18N
        public static final String SEQUENCE_FLOW = "sequenceFlow"; //NOI18N
        public static final String OUTGOING_SEQUENCE_FLOW_ID = "outgoingSequenceFlowId"; //NOI18N
        public static final String INCOMING_SEQUENCE_FLOW_ID = "incomingSequenceFlowId"; //NOI18N
        public static final String BPMN_ELEMENT = "bpmnElement"; //NOI18N
        public static final String WIDTH = "width"; //NOI18N
        public static final String HEIGHT = "height"; //NOI18N
        public static final String IS_LABEL = "isLabel"; //NOI18N
        public static final String X = "x"; //NOI18N
        public static final String Y = "y"; //NOI18N
        public static final String SOURCE = "source"; //NOI18N
        public static final String TARGET = "target"; //NOI18N
    }
    public class XMLProcessDefinitionException extends Exception {
        
        public XMLProcessDefinitionException(String message) {
            super(message);
        }      
    }
    private final String processEnginePath;
    private final String processDefinitionId;
    private final TranslationService ts;
        
    public ProcessDefinitionLoader(String processEnginePath, String processDefinitionId, TranslationService ts) {
        this.processEnginePath = processEnginePath;
        this.processDefinitionId = processDefinitionId;    
        this.ts = ts;
    }
    
    public ProcessDefinition loadProcessDefinition(byte[] processDefinitionStructure) throws XMLStreamException, NumberFormatException, XMLProcessDefinitionException {
        
        if (processDefinitionStructure == null)
            return null;
        
        ProcessDefinition processDefinition = null;
        String startActivityId = null;
        
        HashMap<String, Actor> actors = new HashMap();
        HashMap<String, ActivityDefinition> activityDefinitions = new HashMap();
        HashMap<String, ArtifactDefinition> artifactDefinitions = new HashMap();
        HashMap<String, List<String>> paths = new HashMap();
        HashMap<String, List<Kpi>> activityKpis = new HashMap();
        HashMap<String, List<KpiAction>> activityKpiActions = new HashMap();
        HashMap<String, List<String>> activityartifact = new HashMap();
        HashMap<String, String> activityactor = new HashMap();
        List<Kpi> processKpis = new ArrayList();
        List<KpiAction> processKpiActions = new ArrayList();
        
        QName tagProcessDefinition = new QName(Tag.PROCESS_DEFINITION);
        QName tagActors = new QName(Tag.ACTORS);
        QName tagActor = new QName(Tag.ACTOR);
        QName tagActivityDefinitions = new QName(Tag.ACTIVITY_DEFINITIONS);
        QName tagActivityDefinition = new QName(Tag.ACTIVITY_DEFINITION);
        QName tagArtifactDefinition = new QName(Tag.ARTIFACT_DEFINITION);
        QName tagPaths = new QName(Tag.PATHS);
        QName tagPath = new QName(Tag.PATH);
        QName tagParameters = new QName(Tag.PARAMETERS);
        QName tagParameter = new QName(Tag.PARAMETER);
        QName tagKpis = new QName(Tag.KPIS);
        QName tagKpi = new QName(Tag.KPI);
        QName tagActions = new QName(Tag.ACTIONS);
        QName tagAction = new QName(Tag.ACTION);
        QName tagThreshold = new QName(Tag.THRESHOLD);
                
        XMLInputFactory xif = XMLInputFactory.newInstance();
        ByteArrayInputStream bais = new ByteArrayInputStream(processDefinitionStructure);
        XMLStreamReader reader = xif.createXMLStreamReader(bais);
                
        while (reader.hasNext()) {
            reader.next();
            
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                if (reader.getName().equals(tagProcessDefinition)) {
                    
                    processDefinition = new ProcessDefinition(
                        processDefinitionId,
                        reader.getAttributeValue(null, Attribute.NAME),
                        reader.getAttributeValue(null, Attribute.DESCRIPTION),
                        Long.valueOf(reader.getAttributeValue(null, Attribute.CREATION_DATE)),
                        reader.getAttributeValue(null, Attribute.VERSION),
                        "1".equals(reader.getAttributeValue(null, Attribute.ENABLED)),
                        null, 
                        null,
                        null
                    );
                    processDefinition.setDefinition(processDefinitionStructure);
                    startActivityId = reader.getAttributeValue(null, Attribute.START_ACTIVITY_ID);
                }
                if (reader.getName().equals(tagActors)) {
                    while(true) {
                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                            reader.getName().equals(tagActor)) {
                            
                            Actor actor = new Actor(
                                reader.getAttributeValue(null, Attribute.ID), 
                                reader.getAttributeValue(null, Attribute.NAME), 
                                Integer.valueOf(reader.getAttributeValue(null, Attribute.TYPE)));
                            
                            if (!actors.containsKey(actor.getId()))
                                actors.put(actor.getId(), actor);
                            else
                                throw new XMLProcessDefinitionException(String.format(ts.getTranslatedString("module.processman.process-def-loader.structure-malformed.actor-id-is-in-used"), actor.getId()));
                        }
                        
                        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT && 
                            reader.getName().equals(tagActors)) {
                            
                            break;
                        }
                        reader.next();
                    }
                }
                if (reader.getName().equals(tagActivityDefinitions)) {
                    while(true) {
                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                            reader.getName().equals(tagActivityDefinition)) {
                            
                            String activityDefinitionId = reader.getAttributeValue(null, Attribute.ID);
                            if (!activityDefinitions.containsKey(activityDefinitionId)) {
                                
                                int activityDefinitionType = Integer.valueOf(reader.getAttributeValue(null, Attribute.TYPE));
                                
                                ActivityDefinition activityDefinition = null;
                                
                                if (activityDefinitionType == ActivityDefinition.TYPE_CONDITIONAL) {
                                    
                                    activityDefinition = new ConditionalActivityDefinition(
                                        activityDefinitionId, 
                                        reader.getAttributeValue(null, Attribute.NAME), 
                                        reader.getAttributeValue(null, Attribute.DESCRIPTION), 
                                        activityDefinitionType, 
                                        Boolean.valueOf(reader.getAttributeValue(null, Attribute.CONFIRM)), 
                                        reader.getAttributeValue(null, Attribute.COLOR),
                                        null, 
                                        null, 
                                        null, 
                                        null, 
                                        null);
                                    
                                } else if (activityDefinitionType == ActivityDefinition.TYPE_PARALLEL) {
                                    activityDefinition = new ParallelActivityDefinition(
                                        activityDefinitionId, 
                                        reader.getAttributeValue(null, Attribute.NAME), 
                                        reader.getAttributeValue(null, Attribute.DESCRIPTION), 
                                        activityDefinitionType, 
                                        null, 
                                        null,
                                        null,
                                        null, 
                                        Boolean.valueOf(reader.getAttributeValue(null, Attribute.IDLING)),
                                        Boolean.valueOf(reader.getAttributeValue(null, Attribute.CONFIRM)), 
                                        reader.getAttributeValue(null, Attribute.COLOR));
                                    
                                    String sequenceFlow = reader.getAttributeValue(null, Attribute.SEQUENCE_FLOW);
                                    String outgoingSequenceFlowId = reader.getAttributeValue(null, Attribute.OUTGOING_SEQUENCE_FLOW_ID);
                                    String incomingSequenceFlowId = reader.getAttributeValue(null, Attribute.INCOMING_SEQUENCE_FLOW_ID);
                                    
                                    if (sequenceFlow != null)
                                        ((ParallelActivityDefinition) activityDefinition).setSequenceFlow(Integer.valueOf(sequenceFlow));
                                    
                                    if (outgoingSequenceFlowId != null)
                                        ((ParallelActivityDefinition) activityDefinition).setOutgoingSequenceFlowId(outgoingSequenceFlowId);
                                    
                                    if (incomingSequenceFlowId != null)
                                        ((ParallelActivityDefinition) activityDefinition).setIncomingSequenceFlowId(incomingSequenceFlowId);
                                }
                                else {
                                    
                                    activityDefinition = new ActivityDefinition(
                                        activityDefinitionId, 
                                        reader.getAttributeValue(null, Attribute.NAME), 
                                        reader.getAttributeValue(null, Attribute.DESCRIPTION), 
                                        activityDefinitionType, 
                                        null, 
                                        null,
                                        null,
                                        null, 
                                        Boolean.valueOf(reader.getAttributeValue(null, Attribute.IDLING)),
                                        Boolean.valueOf(reader.getAttributeValue(null, Attribute.CONFIRM)), 
                                        reader.getAttributeValue(null, Attribute.COLOR));
                                }
                                activityDefinitions.put(activityDefinitionId, activityDefinition);
                                
                                String strActorId = reader.getAttributeValue(null, Attribute.ACTOR_ID);
                                if (strActorId != null)
                                    activityactor.put(activityDefinitionId, strActorId);
                                                                                            
                            while (true) {
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                                    reader.getName().equals(tagPath)) {
                                                                        
                                    if (!paths.containsKey(activityDefinitionId))
                                        paths.put(activityDefinitionId, new ArrayList());
                                    
                                    String pathId = reader.getElementText();
                                    
                                    if (!paths.get(activityDefinitionId).contains(pathId)) {
                                        paths.get(activityDefinitionId).add(pathId);
                                    }
                                    else {
                                        throw new XMLProcessDefinitionException(String.format(
                                            ts.getTranslatedString("module.processman.process-def-loader.structure-malformed.next-activity-definition-id-is-in-used"), 
                                            pathId, activityDefinitionId));
                                    }
                                    reader.next();
                                    continue;
                                }
                                                                
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                                    reader.getName().equals(tagArtifactDefinition)) {
                                                
                                    String artifactDefinitionId = reader.getAttributeValue(null, Attribute.ID);

                                    if (!artifactDefinitions.containsKey(artifactDefinitionId)) {
                                        int type = Integer.valueOf(reader.getAttributeValue(null, Attribute.TYPE));

                                        HashMap<String, String> artifactParameters = new HashMap();

                                        reader.nextTag();
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT &&
                                                reader.getName().equals(tagParameters)) {
                                                                                        
                                            while (true) {
                                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                                    if (reader.getName().equals(tagParameter)) {
                                                        
                                                        String attributeName = reader.getAttributeValue(null, Attribute.NAME);
                                                        String attributeValue = reader.getElementText();

                                                        artifactParameters.put(attributeName, attributeValue);
                                                    }
                                                }
                                                if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                                                    
                                                    if (reader.getName().equals(tagParameters))
                                                        break;
                                                }
                                                reader.next();
                                            }
                                        }
                                        byte[] definition = null;
                                        if (type == ArtifactDefinition.TYPE_ATTACHMENT || type == ArtifactDefinition.TYPE_CONDITIONAL) {
                                            definition = artifactParameters.get("definition").getBytes(); //NOI18N
                                        } else if (type == ArtifactDefinition.TYPE_FORM) {
                                            definition = getFormArtifactDefinition(processDefinitionId, artifactParameters.get("definition")); //NOI18N
                                        }
                                        
                                        ArtifactDefinition artifactDefinition = new ArtifactDefinition(
                                            artifactDefinitionId,
                                            artifactParameters.get("name"), //NOI18N
                                            artifactParameters.get("description"), //NOI18N
                                            artifactParameters.get("version"), //NOI18N
                                            type, 
                                            definition, 
                                            Boolean.valueOf(artifactParameters.get("printable")), //NOI18N
                                            artifactParameters.get("printableTemplate"), //NOI18N
                                            artifactParameters.get("externalScripts") //NOI18N
                                        );
                                        
                                        artifactDefinition.setPreconditionsScript(artifactParameters.containsKey("preconditionsScript") ? artifactParameters.get("preconditionsScript").getBytes() : null);
                                        artifactDefinition.setPostconditionsScript(artifactParameters.containsKey("postconditionsScript") ? artifactParameters.get("postconditionsScript").getBytes() : null);

                                        artifactDefinitions.put(artifactDefinition.getId(), artifactDefinition);
                                        
                                        if (!activityartifact.containsKey(activityDefinitionId))
                                            activityartifact.put(activityDefinitionId, new ArrayList());
                                        
                                        activityartifact.get(activityDefinitionId).add(artifactDefinition.getId());
                                    }
                                }
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                                    reader.getName().equals(tagKpis)) {
                                    
                                    if (!activityKpis.containsKey(activityDefinitionId))
                                        activityKpis.put(activityDefinitionId, new ArrayList());
                                    
                                    if (!activityKpiActions.containsKey(activityDefinitionId))
                                        activityKpiActions.put(activityDefinitionId, new ArrayList());
                                    
                                    while (true) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT &&
                                            reader.getName().equals(tagKpi)) {

                                            String kpiName = reader.getAttributeValue(null, Attribute.NAME);
                                            String kpiDescription = reader.getAttributeValue(null, Attribute.DESCRIPTION);
                                            String kpiAction = reader.getAttributeValue(null, Attribute.ACTION);
                                            Properties kpiThresholds = new Properties();

                                            reader.nextTag();

                                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                                                reader.getName().equals(tagThreshold)) {

                                                while (true) {
                                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                                        if (reader.getName().equals(tagThreshold)) {

                                                            String thresholdName = reader.getAttributeValue(null, Attribute.NAME);
                                                            String thresholdValue = reader.getAttributeValue(null, Attribute.VALUE);

                                                            if (thresholdName != null && thresholdValue != null) {
                                                                kpiThresholds.setProperty(thresholdName, thresholdValue);
                                                            } else {
                                                                //ToDo: Malformed xml exception
                                                            }
                                                        }
                                                    }
                                                    if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                                                        if (reader.getName().equals(tagKpi))
                                                            break;
                                                    }
                                                    reader.next();
                                                }
                                            }
                                            Kpi kpi = new Kpi(kpiName, kpiDescription, kpiAction, kpiThresholds);
                                            activityKpis.get(activityDefinitionId).add(kpi);
                                        }
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT &&
                                            reader.getName().equals(tagActions)) {

                                            while (true) {
                                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                                                    reader.getName().equals(tagAction)) {

                                                    int actionType = Integer.valueOf(reader.getAttributeValue(null, Attribute.TYPE));
                                                    String actionName = reader.getAttributeValue(null, Attribute.NAME);
                                                    String actionDescription = reader.getAttributeValue(null, Attribute.DESCRIPTION);
                                                    String actionScript = reader.getElementText();

                                                    KpiAction kpiAction = new KpiAction(actionType, actionName, actionDescription, actionScript);
                                                    activityKpiActions.get(activityDefinitionId).add(kpiAction);
                                                }
                                                if (reader.getEventType() == XMLStreamConstants.END_ELEMENT && 
                                                    reader.getName().equals(tagActions))
                                                    break;

                                                reader.next();
                                            }
                                        }
                                        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT &&
                                            reader.getName().equals(tagKpis))
                                            break;

                                        reader.next();
                                    }
                                }
                                if (reader.getEventType() == XMLStreamConstants.END_ELEMENT && 
                                    reader.getName().equals(tagActivityDefinition)) {
                                    break;
                                }
                                reader.next();
                            }
                        } else
                            throw new XMLProcessDefinitionException(String.format(ts.getTranslatedString("module.processman.process-def-loader.structure-malformed.activity-definition-id-is-in-use"), activityDefinitionId));
                        }
                        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT && 
                            reader.getName().equals(tagActivityDefinitions)) {
                            
                            break;
                        }
                        reader.next();
                    }
                }
                if (reader.getName().equals(tagKpis)) {
                    while (true) {
                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT &&
                            reader.getName().equals(tagKpi)) {
                            
                            String kpiName = reader.getAttributeValue(null, Attribute.NAME);
                            String kpiDescription = reader.getAttributeValue(null, Attribute.DESCRIPTION);
                            String kpiAction = reader.getAttributeValue(null, Attribute.ACTION);
                            Properties kpiThresholds = new Properties();

                            reader.nextTag();

                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                                reader.getName().equals(tagThreshold)) {

                                while (true) {
                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                        if (reader.getName().equals(tagThreshold)) {

                                            String thresholdName = reader.getAttributeValue(null, Attribute.NAME);
                                            String thresholdValue = reader.getAttributeValue(null, Attribute.VALUE);

                                            if (thresholdName != null && thresholdValue != null) {
                                                kpiThresholds.setProperty(thresholdName, thresholdValue);
                                            } else {
                                                //ToDo: Malformed xml exception
                                            }
                                        }
                                    }
                                    if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                                        if (reader.getName().equals(tagKpi))
                                            break;
                                    }
                                    reader.next();
                                }
                            }
                            Kpi kpi = new Kpi(kpiName, kpiDescription, kpiAction, kpiThresholds);
                            processKpis.add(kpi);
                        }
                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT &&
                            reader.getName().equals(tagActions)) {
                            
                            while (true) {
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                                    reader.getName().equals(tagAction)) {
                                    
                                    int actionType = Integer.valueOf(reader.getAttributeValue(null, Attribute.TYPE));
                                    String actionName = reader.getAttributeValue(null, Attribute.NAME);
                                    String actionDescription = reader.getAttributeValue(null, Attribute.DESCRIPTION);
                                    String actionScript = reader.getElementText();
                                    
                                    KpiAction kpiAction = new KpiAction(actionType, actionName, actionDescription, actionScript);
                                    processKpiActions.add(kpiAction);
                                }
                                if (reader.getEventType() == XMLStreamConstants.END_ELEMENT && 
                                    reader.getName().equals(tagActions))
                                    break;
                                
                                reader.next();
                            }
                        }
                        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT &&
                            reader.getName().equals(tagKpis))
                            break;
                        
                        reader.next();
                    }
                }
            }
        }
        if (processDefinition != null && activityDefinitions.containsKey(startActivityId)) {
            
            processDefinition.setStartActivity(activityDefinitions.get(startActivityId));
            processDefinition.setKpis(processKpis);
            processDefinition.setKpiActions(processKpiActions);
            
            for (String activityDefinitionId : activityDefinitions.keySet()) {
                
                ActivityDefinition activityDefinition = activityDefinitions.get(activityDefinitionId);
                
                if (activityartifact.containsKey(activityDefinitionId) && !activityartifact.get(activityDefinitionId).isEmpty()) {
                    String artifactId = activityartifact.get(activityDefinitionId).get(0);
                    
                    if (artifactDefinitions.containsKey(artifactId))
                        activityDefinition.setArfifact(artifactDefinitions.get(artifactId));
                    
                    if (activityDefinition instanceof ConditionalActivityDefinition) {
                        if (activityartifact.get(activityDefinitionId).size() >= 2) {
                            
                            String informationArfifactId = activityartifact.get(activityDefinitionId).get(1);
                            
                            if (artifactDefinitions.containsKey(informationArfifactId))
                                ((ConditionalActivityDefinition) activityDefinition).setInformationArfifact(artifactDefinitions.get(informationArfifactId));
                        }
                    }
                } else {
                    //TODO: exception activity no has artifact
                }
                
                if (activityactor.containsKey(activityDefinitionId)) {
                    String actorId = activityactor.get(activityDefinitionId);
                    
                    if (actors.containsKey(actorId))
                        activityDefinition.setActor(actors.get(actorId));
                    
                } else {
                    //TODO: exception activity no has actor
                }
                
                if (activityKpis.containsKey(activityDefinitionId)) {
                    activityDefinition.setKpis(activityKpis.get(activityDefinitionId));
                }
                
                if (activityKpiActions.containsKey(activityDefinitionId)) {
                    activityDefinition.setKpiActions(activityKpiActions.get(activityDefinitionId));
                }
                
                if (activityDefinition.getType() == ActivityDefinition.TYPE_CONDITIONAL) {
                    if (paths.containsKey(activityDefinitionId)) {
                        List<String> activityPaths = paths.get(activityDefinitionId);
                        if (activityPaths.size() == 2) {
                            String nextActivityDefinitionId = activityPaths.get(0);

                            if (activityDefinitions.containsKey(nextActivityDefinitionId)) {
                                ((ConditionalActivityDefinition) activityDefinition).setNextActivityIfTrue(activityDefinitions.get(nextActivityDefinitionId));
                            } else {
                                //TODO: exception id next activity no found
                            }
                            
                            nextActivityDefinitionId = activityPaths.get(1);
                            
                            if (activityDefinitions.containsKey(nextActivityDefinitionId)) {
                                ((ConditionalActivityDefinition) activityDefinition).setNextActivityIfFalse(activityDefinitions.get(nextActivityDefinitionId));
                            } else {
                                //TODO: exception id next activity no found
                            }
                        }
                    }

                } 
                else if (activityDefinition.getType() == ActivityDefinition.TYPE_PARALLEL) {
                    if (paths.containsKey(activityDefinitionId) && paths.get(activityDefinitionId) != null) {
                        List<String> activityPaths = paths.get(activityDefinitionId);
                        for (String pathId : activityPaths) {
                            if (activityDefinitions.containsKey(pathId)) {
                                
                                ParallelActivityDefinition parallelActivityDef = (ParallelActivityDefinition) activityDefinition;
                                
                                if (parallelActivityDef.getPaths() == null)
                                    parallelActivityDef.setPaths(new ArrayList());
                                
                                parallelActivityDef.getPaths().add(activityDefinitions.get(pathId));
                            }
                        }
                    }
                }
                else {
                    if (paths.containsKey(activityDefinitionId)) {
                        List<String> activityPaths = paths.get(activityDefinitionId);
                        if (activityPaths.size() == 1) {
                            String nextActivityDefinitionId = activityPaths.get(0);

                            if (activityDefinitions.containsKey(nextActivityDefinitionId)) {
                                activityDefinition.setNextActivity(activityDefinitions.get(nextActivityDefinitionId));
                            } else {
                                //TODO: exception id next activity no found
                            }
                        }
                    }
                }
                
            }
        } else {
            throw new XMLProcessDefinitionException(ts.getTranslatedString("module.processman.process-def-loader.structure-malformed.start-activity-cannot-be-found"));
        }
        processDefinition.setActivityDefinitions(activityDefinitions);
        processDefinition.setActors(actors);
        return processDefinition;
    }
    
    private byte[] getFormArtifactDefinition(String processDefinitionId, String artifactDefinitionId) {
        File file = new File(String.format(
            "%s/form/definitions/%s/%s", 
            processEnginePath, processDefinitionId, artifactDefinitionId
        ));
        return getFileAsByteArray(file);
    }
    
    public static byte[] getFileAsByteArray(File file) {
        try {
            Scanner in = new Scanner(file);

            String line = "";

            while (in.hasNext())
                line += in.nextLine() + "\n";

            byte [] structure = line.getBytes();

            in.close();

            return structure;

        } catch (FileNotFoundException ex) {

            return null;
        }
    }
    
}
