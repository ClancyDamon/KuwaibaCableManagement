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
package org.neotropic.kuwaiba.core.persistence.reference.extras.processman;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ArtifactDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ConditionalActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ParallelActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.PersistenceService;
import org.springframework.stereotype.Service;

/**
 * Service to manage the process definitions, process instances, activity definitions, and artifacts
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Service
public class ProcessManagerService {
    private PersistenceService persistenceService;
    private TranslationService ts;
    
    private final HashMap<String, List<ActivityDefinition>> processActivityDefinitions = new HashMap();
    private final List<ProcessDefinition> processDefinitions = new ArrayList();
    private final HashMap<String, ProcessInstance> processInstances = new HashMap();
    private final HashMap<ProcessDefinition, List<ProcessInstance>> relatedProcessInstances = new HashMap();
    private final HashMap<ProcessInstance, HashMap<ArtifactDefinition, Artifact>> processInstanceArtifacts = new HashMap();
    
    public void init(PersistenceService persistenceService, TranslationService ts) {
        this.persistenceService = persistenceService;
        this.ts = ts;
        Logger.getLogger(ProcessManagerService.class.getName()).log(Level.INFO, 
            ts.getTranslatedString("module.processman.service.init-process-definition-cache")
        );
        updateProcessDefinitions();
    }
    
    public void cacheProcessDefinition(ProcessDefinition processDefinition) {
        if (processDefinition != null) {
            for (ProcessDefinition processDef : processDefinitions) {
                if (processDef.getId() != null && processDef.getId().equals(processDefinition.getId())) {
                    processDefinitions.remove(processDef);
                    break;
                }
            }
            processDefinitions.add(processDefinition);
                        
            relatedProcessInstances.put(processDefinition, new ArrayList());
            
            ActivityDefinition startActivity = processDefinition.getStartActivity();
            
            if (startActivity != null) {
                processActivityDefinitions.put(processDefinition.getId(), new ArrayList());
                
                initActivitiesCache(processDefinition, startActivity);
            }
        }
    }
    
    private void initActivitiesCache(ProcessDefinition processDefinition, ActivityDefinition activity) {
        if (activity != null) {
            
            for (ActivityDefinition activityDef : processActivityDefinitions.get(processDefinition.getId())) {
                if (activityDef.getId() != null && activityDef.getId().equals(activity.getId()))
                    return;
            }
            processActivityDefinitions.get(processDefinition.getId()).add(activity);

            if (activity instanceof ConditionalActivityDefinition) {
                initActivitiesCache(processDefinition, ((ConditionalActivityDefinition) activity).getNextActivityIfTrue());
                initActivitiesCache(processDefinition, ((ConditionalActivityDefinition) activity).getNextActivityIfFalse());
            } 
            else if (activity instanceof ParallelActivityDefinition && 
                    ((ParallelActivityDefinition) activity).getPaths() != null) {
                
                for (ActivityDefinition activityDef : ((ParallelActivityDefinition) activity).getPaths())
                    initActivitiesCache(processDefinition, activityDef);                    
            }
            else {
                initActivitiesCache(processDefinition, activity.getNextActivity());
            }
        }
    }
        
    public void updateProcessDefinitions() {     
        String processEnginePath = String.valueOf(persistenceService.getApplicationProperties().get("processEnginePath")); //NOI18N
        File processDefDir = new File(processEnginePath + "/process/definitions"); //NOI18N
        
        if (processDefDir.exists()) {
            for (File processDefFile : processDefDir.listFiles()) {
                if (processDefFile.isFile()) {
                    String processDefId = processDefFile.getName().replace(".xml", "");
                    if (processDefId.contains("_"))
                        processDefId = processDefFile.getName().substring(0, 1);
                    ProcessDefinition processDef = getProcessDefinition(processDefId, processDefFile);
                    cacheProcessDefinition(processDef);
                }
            }
        } else {
            Logger.getLogger(ProcessManagerService.class.getName()).log(Level.INFO, String.format(
                ts.getTranslatedString("module.processman.service.process-definitions-not-found"), 
                processDefDir.getAbsolutePath()
            ));
        }
    }
    
    public ProcessDefinition getProcessDefinition(String processDefId, File processDefFile) {
        String processEnginePath = String.valueOf(persistenceService.getApplicationProperties().get("processEnginePath")); //NOI18N
        ProcessDefinitionLoader processDefinitionLoader = new ProcessDefinitionLoader(processEnginePath, processDefId, ts);
        byte[] processDefinitionStructure = ProcessDefinitionLoader.getFileAsByteArray(processDefFile);
        try {
            ProcessDefinition processDefinition = processDefinitionLoader.loadProcessDefinition(processDefinitionStructure);
            return processDefinition;
        } catch (XMLStreamException | NumberFormatException | ProcessDefinitionLoader.XMLProcessDefinitionException ex) {
            return null;
        }
    }
    
    /**
     * Retrieves a process definition either from the database or a process definition repository (like a file or an external provider)
     * @param id The id of the process definition
     * @return The process definition mapped as a Java object
     * @throws ApplicationObjectNotFoundException When the process definition could not be found
     */
    public ProcessDefinition getProcessDefinition(String id) throws ApplicationObjectNotFoundException {
        for (ProcessDefinition processDefinition : processDefinitions) {
            if (processDefinition.getId() != null && processDefinition.getId().equals(id))            
                return processDefinition;                
        }
        throw new ApplicationObjectNotFoundException(ts.getTranslatedString("module.processman.service.process-definition-cannot-found"));
    }
    
    public String createProcessInstance(String processInstanceId, String processDefId, String name, String description) throws InventoryException {
        ProcessDefinition processDef = getProcessDefinition(processDefId);
        
        String currentActivity = processDef.getStartActivity().getId();
        
        ProcessInstance processInstance = new ProcessInstance(processInstanceId, name, description, currentActivity, processDefId);
        
        if (!relatedProcessInstances.containsKey(processDef))
            relatedProcessInstances.put(processDef, new ArrayList());
                
        relatedProcessInstances.get(processDef).add(processInstance);
        
        processInstances.put(processInstance.getId(), processInstance);
        processInstanceArtifacts.put(processInstance, new HashMap());
        
        return processInstance.getId();
    }
    
    public void setProcessInstance(ProcessInstance processInstance) throws InventoryException {
        ProcessDefinition processDef = getProcessDefinition(processInstance.getProcessDefinitionId());
        if (relatedProcessInstances.containsKey(processDef)) {
            for (ProcessInstance processIns : relatedProcessInstances.get(processDef)) {
                if (processIns.getId() != null && processIns.getId().equals(processInstance.getId())) {
                    relatedProcessInstances.get(processDef).remove(processIns);
                    break;
                }
            }
        } else
            relatedProcessInstances.put(processDef, new ArrayList());            
        
        relatedProcessInstances.get(processDef).add(processInstance);
        
        processInstances.put(processInstance.getId(), processInstance);
        
        if (!processInstanceArtifacts.containsKey(processInstance))
            processInstanceArtifacts.put(processInstance, new HashMap());
                
        renderProcessInstance(processInstance);
    }
    
    public ProcessInstance getProcessInstance(String processInstanceId) throws ApplicationObjectNotFoundException {
        ProcessInstance processInstance = processInstances.get(processInstanceId);
        if (processInstance != null)        
            return processInstance;
        throw new ApplicationObjectNotFoundException(ts.getTranslatedString("module.processman.service.process-instance-cannot-be-found"));
    }
    
    public ActivityDefinition getActivityDefinition(String processDefinitionId, String activityDefinitionId) throws InventoryException {
        if (processActivityDefinitions.containsKey(processDefinitionId)) {
            
            if (processActivityDefinitions.get(processDefinitionId) != null) {
                
                for (ActivityDefinition activityDef : processActivityDefinitions.get(processDefinitionId))
                    
                    if (activityDef.getId() != null && activityDef.getId().equals(activityDefinitionId))
                        return activityDef;
            }
        }
        throw new InventoryException(ts.getTranslatedString("module.processman.service.activity-definition-cannot-be-found")) {};
    }
    
    public Artifact getArtifactForActivity(String processInstanceId, String activityId) throws ApplicationObjectNotFoundException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        ProcessDefinition processDef = getProcessDefinition(processInstance.getProcessDefinitionId());
        
        ActivityDefinition activity;
        try {
            activity = getActivityDefinition(processDef.getId(), activityId);
        } catch (InventoryException ex) {
            throw new ApplicationObjectNotFoundException(ex.getMessage());
        }
        
        if (activity != null) {
            
            ArtifactDefinition artifactDef = activity.getArfifact();
            
            if (processInstanceArtifacts.containsKey(processInstance)) {
                
                HashMap<ArtifactDefinition, Artifact> artifactInstances = processInstanceArtifacts.get(processInstance);
                
                if (artifactInstances.containsKey(artifactDef))
                    return artifactInstances.get(artifactDef);
            }
        }
        throw new ApplicationObjectNotFoundException(ts.getTranslatedString("module.processman.service.process-instances-artifact-cannot-be-found"));
    }
    
    public ArtifactDefinition getArtifactDefinitionForActivity(String processDefinitionId, String activityDefinitionId) throws InventoryException {
        
        if (processActivityDefinitions.containsKey(processDefinitionId)) {
            
            if (processActivityDefinitions.get(processDefinitionId) != null) {
                
                for (ActivityDefinition activityDef : processActivityDefinitions.get(processDefinitionId))
                    if (activityDef.getId() != null && activityDef.getId().equals(activityDefinitionId))
                        return activityDef.getArfifact();
                        
            }
        }
        
        throw new InventoryException(ts.getTranslatedString("module.processman.service.artifact-definition-cannot-be-found")) {};
    }
    
    private boolean getConditionalArtifactContent(Artifact artifact) throws ApplicationObjectNotFoundException {
        if (artifact == null)
            throw new ApplicationObjectNotFoundException(ts.getTranslatedString("module.processman.service.conditional-artifact-cannot-be-found"));
        
        try {
            byte[] content = artifact.getContent();            
            XMLInputFactory xif = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(content);
            XMLStreamReader reader = xif.createXMLStreamReader(bais);
            QName tagValue = new QName("value"); //NOI18N
            
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagValue))
                        return Boolean.valueOf(reader.getElementText());
                }
            }
            
            reader.close();
            
        } catch (Exception ex) {
            throw new ApplicationObjectNotFoundException(ts.getTranslatedString("module.processman.service.conditional-artifact-malformed"));
        }
        return false;
    }
        
    public List<ActivityDefinition> getProcessInstanceActivitiesPath(String processInstanceId) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinitionId());
        ActivityDefinition activity = processDefinition.getStartActivity();
        
        List<ActivityDefinition> result = new ArrayList();              
        result.addAll(getPath(processInstanceId, processDefinition.getId(), processInstance.getCurrentActivityId(), activity));
        result.add(getActivityDefinition(processDefinition.getId(), processInstance.getCurrentActivityId()));
        return result;
    }
    
    private List<ActivityDefinition> getPath(String processInstanceId, String processDefinitionId, String currentActivityId, ActivityDefinition activityDef) throws InventoryException {
        List<ActivityDefinition> result = new ArrayList();
        
        if (activityDef == null)
            return result;
                        
        if (activityDef.getId() != null && activityDef.getId().equals(currentActivityId))
            return result;
        
        if (activityDef instanceof ParallelActivityDefinition &&
            ((ParallelActivityDefinition) activityDef).getSequenceFlow() ==  ParallelActivityDefinition.JOIN) {
            
            return result;
        }
        
        result.add(activityDef);
        
        if (activityDef instanceof ParallelActivityDefinition) {
            
            ParallelActivityDefinition parallelActivityDef = (ParallelActivityDefinition) activityDef;
            
            if (parallelActivityDef.getSequenceFlow() == ParallelActivityDefinition.FORK && 
                parallelActivityDef.getPaths() != null && 
                parallelActivityDef.getIncomingSequenceFlowId() != null) {
                
                for (ActivityDefinition path : parallelActivityDef.getPaths())
                    result.addAll(getPath(processInstanceId, processDefinitionId, currentActivityId, path));
                
                ActivityDefinition joinActivityDef = getActivityDefinition(processDefinitionId, parallelActivityDef.getIncomingSequenceFlowId());
                
                if (joinActivityDef.getId() != null && joinActivityDef.getId().equals(currentActivityId))
                    return result;
                
                result.add(joinActivityDef);
                
                if (joinActivityDef instanceof ParallelActivityDefinition) {
                    parallelActivityDef = (ParallelActivityDefinition) joinActivityDef;

                    if(parallelActivityDef.getSequenceFlow() ==  ParallelActivityDefinition.JOIN && 
                       parallelActivityDef.getPaths() != null) {

                        for (ActivityDefinition path : parallelActivityDef.getPaths())
                            result.addAll(getPath(processInstanceId, processDefinitionId, currentActivityId, path));
                    }
                }
            }
        }
        else if (activityDef instanceof ConditionalActivityDefinition) {
            
            ConditionalActivityDefinition conditionalActivityDef = (ConditionalActivityDefinition) activityDef;
            
            boolean isTrue = false;
            try {
                Artifact artifact = getArtifactForActivity(processInstanceId, conditionalActivityDef.getId());
                isTrue = getConditionalArtifactContent(artifact);
            } catch(Exception ex) {
            }
            if (isTrue)
                result.addAll(getPath(processInstanceId, processDefinitionId, currentActivityId, conditionalActivityDef.getNextActivityIfTrue()));
            else
                result.addAll(getPath(processInstanceId, processDefinitionId, currentActivityId, conditionalActivityDef.getNextActivityIfFalse()));
        }
        else
            result.addAll(getPath(processInstanceId, processDefinitionId, currentActivityId, activityDef.getNextActivity()));
                
        return result;
    }
    
    public ActivityDefinition getNextActivityToParallelActivity(String processDefinitionId, String activityDefinitionId) throws ApplicationObjectNotFoundException {
        ActivityDefinition activityDefinition;    
        
        try {
            activityDefinition = getActivityDefinition(processDefinitionId, activityDefinitionId);
        } catch (InventoryException ex) {
            throw new ApplicationObjectNotFoundException(ex.getMessage());
        }
        
        if (activityDefinition instanceof ParallelActivityDefinition) {
            ParallelActivityDefinition parallelActivityDef = (ParallelActivityDefinition) activityDefinition;
            
            if (parallelActivityDef.getSequenceFlow() == ParallelActivityDefinition.FORK) {
                    try {
                        String incomingSeqFlowId = ((ParallelActivityDefinition) activityDefinition).getIncomingSequenceFlowId();
                        return getActivityDefinition(processDefinitionId, incomingSeqFlowId);
                        
                    } catch (InventoryException ex) {
                        throw new ApplicationObjectNotFoundException(ex.getMessage());
                    }
            }
        }
        return activityDefinition;
    }
    
    public ActivityDefinition getNextActivityForProcessInstance(String processInstanceId, String currentActivityId) throws ApplicationObjectNotFoundException {
        
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinitionId());
        
        ActivityDefinition activityDefinition;    

        try {
            activityDefinition = getActivityDefinition(processDefinition.getId(), currentActivityId);
        } catch (InventoryException ex) {
            throw new ApplicationObjectNotFoundException(ex.getMessage());
        }
                
        if (activityDefinition instanceof ConditionalActivityDefinition) {
            Artifact artifact = null;

            try {
                artifact = getArtifactForActivity(processInstanceId, activityDefinition.getId());
            } catch(Exception ex) {
            }
            boolean isTrue = false;

            if (artifact != null)
                isTrue = getConditionalArtifactContent(artifact);

            if (isTrue)
                return getNextActivityToParallelActivity(processDefinition.getId(), ((ConditionalActivityDefinition) activityDefinition).getNextActivityIfTrue().getId());
            else
                return getNextActivityToParallelActivity(processDefinition.getId(), ((ConditionalActivityDefinition) activityDefinition).getNextActivityIfFalse().getId());
        }
        if (activityDefinition instanceof ParallelActivityDefinition) {

            ParallelActivityDefinition parallelActivityDef = (ParallelActivityDefinition) activityDefinition;

            if (parallelActivityDef.getSequenceFlow() == ParallelActivityDefinition.JOIN && 
                parallelActivityDef.getPaths() != null && 
                !parallelActivityDef.getPaths().isEmpty()) {

                return parallelActivityDef.getPaths().get(0);
            }
            else {
                throw new ApplicationObjectNotFoundException(ts.getTranslatedString("module.processman.service.next-activity-cannot-be-found"));
            }
        }
        if (activityDefinition.getType() == ActivityDefinition.TYPE_END)
            return activityDefinition;
        return getNextActivityToParallelActivity(processDefinition.getId(), activityDefinition.getNextActivity().getId());
    }
    
    public ActivityDefinition getNextActivityForProcessInstance(String processInstanceId) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        return getNextActivityForProcessInstance(processInstanceId, processInstance.getCurrentActivityId());
    }
        
    public void updateActivity(String processInstanceId, String activityDefinitionId, Artifact artifact) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinitionId());
                
        ActivityDefinition activity = getActivityDefinition(processDefinition.getId(), activityDefinitionId);
        
        if (activity != null && artifact != null) {
            if (processInstanceArtifacts.containsKey(processInstance)) {
                                
                if (activity instanceof ConditionalActivityDefinition) {
                    Artifact oldArtifact = null;
                    
                    try {
                        oldArtifact = getArtifactForActivity(processInstanceId, activity.getId());
                    } catch(Exception ex) {
                    }
                    
                    if (oldArtifact != null) {
                        boolean oldValue = getConditionalArtifactContent(oldArtifact);
                        boolean newValue = getConditionalArtifactContent(artifact);
                        
                        if (oldValue != newValue)
                            processInstance.setCurrentActivity(activity.getId());
                    }
                }
                
                HashMap<ArtifactDefinition, Artifact> artifactInstance = processInstanceArtifacts.get(processInstance);
                
                artifactInstance.put(activity.getArfifact(), artifact);
                
            } else
                throw new InventoryException(ts.getTranslatedString("module.processman.service.process-instance-cannot-be-found")) {};
            
            processInstance.setArtifactsContent(processInstanceAsXML(processInstanceId));
        }
    }
    
    public void commitActivity(String processInstanceId, String activityDefinitionId, Artifact artifact) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinitionId());
                
        ActivityDefinition activity = getActivityDefinition(processDefinition.getId(), activityDefinitionId);
        
        if (activity != null) {
            if (processInstanceArtifacts.containsKey(processInstance)) {
                HashMap<ArtifactDefinition, Artifact> artifactInstance = processInstanceArtifacts.get(processInstance);
                
                if (processInstance.getCurrentActivityId() != null && processInstance.getCurrentActivityId().equals(activityDefinitionId))
                    artifactInstance.put(activity.getArfifact(), artifact);
                else
                    throw new InventoryException(ts.getTranslatedString("module.processman.service.artifact-cannot-be-commited")) {};
            }
                        
            processInstance.setArtifactsContent(processInstanceAsXML(processInstanceId));
            
            ActivityDefinition nextActivity = getNextActivityForProcessInstance(processInstanceId);
            if (nextActivity != null)
                processInstance.setCurrentActivity(nextActivity.getId());
        }
    }
    
    public void setProcessInstances(String processDefinitionId, List<ProcessInstance> lstProcessInstances) throws InventoryException {
        ProcessDefinition processDefinition = getProcessDefinition(processDefinitionId);
        
        if (relatedProcessInstances.containsKey(processDefinition))
            relatedProcessInstances.replace(processDefinition, lstProcessInstances);
        else
            relatedProcessInstances.put(processDefinition, lstProcessInstances);
    }
    
    public List<ProcessInstance> getProcessInstances(String processDefinitionId) throws InventoryException {
        ProcessDefinition processDefinition = getProcessDefinition(processDefinitionId);
        
        if (relatedProcessInstances.containsKey(processDefinition))
            return relatedProcessInstances.get(processDefinition);
        
        throw new InventoryException(ts.getTranslatedString("module.processman.service.process-instance-cannot-be-found")) {};
    }
    
    public List<ProcessDefinition> getProcessDefinitions() {
        return processDefinitions;
    }
    
    public Artifact getArtifact(String processInstanceId, String activityDefinitionId) throws InventoryException {
        if (processInstances.containsKey(processInstanceId)) {
            ProcessInstance processInstance = getProcessInstance(processInstanceId);
                        
            ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinitionId());
            
            if (processActivityDefinitions.containsKey(processDefinition.getId())) {
                List<ActivityDefinition> activityDefs = processActivityDefinitions.get(processDefinition.getId());
                
                for (ActivityDefinition activityDef : activityDefs) {
                    
                    if (activityDef.getId() != null && activityDef.getId().equals(activityDefinitionId)) {
                        if (processInstanceArtifacts.containsKey(processInstance) && 
                            processInstanceArtifacts.get(processInstance).containsKey(activityDef.getArfifact())) 
                                return processInstanceArtifacts.get(processInstance).get(activityDef.getArfifact());
                    }
                }
            }
        }
        return null;
    }
    
    private byte[] processInstanceAsXML(String processInstanceId) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
                
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            final String TAG_PROCESS_INSTANCE = "processInstance";
            final String TAG_PROCESS_ARTIFACTS = "artifacts";
            final String TAG_PROCESS_ARTIFACT = "artifact";
            final String TAG_PROCESS_CONTENT = "content";
            final String TAG_PROCESS_SHARES = "shares";
            final String TAG_PROCESS_SHARE = "share";
            
            final String ATTR_ID = "id";
            final String ATTR_NAME = "name";
            final String ATTR_DESCRIPTION = "description";
            final String ATTR_CURRENT_ACTIVITY_ID = "currentActivityId";
            final String ATTR_PROCESS_DEFINITION_ID = "processDefinitionId";
            final String ATTR_CONTENT_TYPE = "contentType";
            final String ATTR_ARTIFACT_DEFINTION_ID = "artifactDefinitionId";
            final String ATTR_KEY = "key";
            final String ATTR_VALUE = "value";
            final String ATTR_CREATION_DATE = "creationDate";
            final String ATTR_COMMIT_DATE = "commitDate";
            
            QName tagProcessInstance = new QName(TAG_PROCESS_INSTANCE);
            QName tagArtifacts = new QName(TAG_PROCESS_ARTIFACTS);
            QName tagArtifact = new QName(TAG_PROCESS_ARTIFACT);
            QName tagContent = new QName(TAG_PROCESS_CONTENT);
            QName tagShares = new QName(TAG_PROCESS_SHARES);
            QName tagShare = new QName(TAG_PROCESS_SHARE);
            
            xmlew.add(xmlef.createStartElement(tagProcessInstance, null, null));
            xmlew.add(xmlef.createAttribute(new QName(ATTR_ID), processInstance.getId())); //process instance id to remove
            xmlew.add(xmlef.createAttribute(new QName(ATTR_NAME), processInstance.getName()));
            xmlew.add(xmlef.createAttribute(new QName(ATTR_DESCRIPTION), processInstance.getDescription()));
            xmlew.add(xmlef.createAttribute(new QName(ATTR_CURRENT_ACTIVITY_ID), processInstance.getCurrentActivityId()));
            xmlew.add(xmlef.createAttribute(new QName(ATTR_PROCESS_DEFINITION_ID), processInstance.getProcessDefinitionId()));
            
            xmlew.add(xmlef.createStartElement(tagArtifacts, null, null));
            
            List<ActivityDefinition> activityDefs = new ArrayList();

            if (processActivityDefinitions.containsKey(processInstance.getProcessDefinitionId()))
                activityDefs = processActivityDefinitions.get(processInstance.getProcessDefinitionId());
            
            List<ActivityDefinition> path = getProcessInstanceActivitiesPath(processInstanceId);
            if (path != null) {
                for (ActivityDefinition activityDefinition : activityDefs) {
                    
                    Artifact artifact = null;                            
                    try {                    
                        artifact = getArtifactForActivity(processInstanceId, activityDefinition.getId());
                    } catch(InventoryException ex) {
                    }
                    
                    if (artifact != null) {
                        
                        boolean contain = false;
                        for (ActivityDefinition a : path) {
                            if (activityDefinition.getId() != null && activityDefinition.getId().equals(a.getId())) {
                                contain = true;
                                break;
                            }
                        }
                        if (!contain) {
                            if (artifact.getSharedInformation() != null)
                                artifact.getSharedInformation().add(new StringPair("__interrupted__", "true"));
                        }
                        
                        xmlew.add(xmlef.createStartElement(tagArtifact, null, null));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_ID), artifact.getId()));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_NAME), artifact.getName() != null ? artifact.getName() : ""));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_CONTENT_TYPE), artifact.getContentType() != null ? artifact.getContentType() : ""));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_ARTIFACT_DEFINTION_ID), activityDefinition.getArfifact().getId()));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_CREATION_DATE), Long.toString(artifact.getCreationDate())));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_COMMIT_DATE), Long.toString(artifact.getCommitDate())));
                        
                        xmlew.add(xmlef.createStartElement(tagContent, null, null));
                        xmlew.add(xmlef.createCData(new String(artifact.getContent())));
                        xmlew.add(xmlef.createEndElement(tagContent, null));
                        
                        if (artifact.getSharedInformation() != null && !artifact.getSharedInformation().isEmpty()) {
                            xmlew.add(xmlef.createStartElement(tagShares, null, null));
                            for (StringPair share : artifact.getSharedInformation()) {
                                xmlew.add(xmlef.createStartElement(tagShare, null, null));                                                                
                                xmlew.add(xmlef.createAttribute(new QName(ATTR_KEY), share.getKey() != null ? share.getKey() : ""));
                                xmlew.add(xmlef.createAttribute(new QName(ATTR_VALUE), share.getValue() != null ? share.getValue() : ""));
                                xmlew.add(xmlef.createEndElement(tagShare, null));
                            }
                            xmlew.add(xmlef.createEndElement(tagShares, null));
                        }

                        xmlew.add(xmlef.createEndElement(tagArtifact, null));
                    }
                }
            }
            xmlew.add(xmlef.createEndElement(tagArtifacts, null));
                        
            xmlew.add(xmlef.createEndElement(tagProcessInstance, null));
            
            xmlew.close();
            return baos.toByteArray();
            
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
        return null;
    }
        
    private void renderProcessInstance(ProcessInstance processInstance) throws InventoryException {
        if (processInstance.getArtifactsContent() == null)
            return;
        
        List<ActivityDefinition> activityDefs = new ArrayList();
        
        if (processActivityDefinitions.containsKey(processInstance.getProcessDefinitionId()))
            activityDefs = processActivityDefinitions.get(processInstance.getProcessDefinitionId());
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(processInstance.getArtifactsContent());
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            XMLStreamReader reader = xmlif.createXMLStreamReader(bais, "utf-8");
            
            final String TAG_PROCESS_ARTIFACT = "artifact";
            final String TAG_PROCESS_CONTENT = "content";
            final String TAG_PROCESS_SHARES = "shares";
            final String TAG_PROCESS_SHARE = "share";
            
            final String ATTR_ID = "id";
            final String ATTR_NAME = "name";
            final String ATTR_CONTENT_TYPE = "contentType";
            final String ATTR_ARTIFACT_DEFINTION_ID = "artifactDefinitionId";
            final String ATTR_KEY = "key";
            final String ATTR_VALUE = "value";
            final String ATTR_CREATION_DATE = "creationDate";
            final String ATTR_COMMIT_DATE = "commitDate";
            
            QName tagArtifact = new QName(TAG_PROCESS_ARTIFACT);
            QName tagContent = new QName(TAG_PROCESS_CONTENT);
            QName tagShares = new QName(TAG_PROCESS_SHARES);
            QName tagShare = new QName(TAG_PROCESS_SHARE);
            
            while (reader.hasNext()) {
                reader.next();
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagArtifact)) {
                        
                        reader.getAttributeValue(null, ATTR_ID);
                        String name = reader.getAttributeValue(null, ATTR_NAME);
                        String contentType = reader.getAttributeValue(null, ATTR_CONTENT_TYPE);
                        String artifactDefId = reader.getAttributeValue(null, ATTR_ARTIFACT_DEFINTION_ID);
                        long creationDate = Long.valueOf(reader.getAttributeValue(null, ATTR_CREATION_DATE) != null ? reader.getAttributeValue(null, ATTR_CREATION_DATE) : "0");
                        long commitDate = Long.valueOf(reader.getAttributeValue(null, ATTR_COMMIT_DATE) != null ? reader.getAttributeValue(null, ATTR_COMMIT_DATE) : "0");
                        
                        byte[] content = null;
                        List<StringPair> shares = new ArrayList();
                                                
                        while (true) {
                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                if (reader.getName().equals(tagShares)) {
                                    while (true) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                            
                                            if (reader.getName().equals(tagShare)) {
                                                String key = reader.getAttributeValue(null, ATTR_KEY);
                                                String value = reader.getAttributeValue(null, ATTR_VALUE);
                                                shares.add(new StringPair(key, value));
                                            }
                                        }
                                        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {

                                            if (reader.getName().equals(tagShares))
                                                break;
                                        }
                                        reader.next();                                        
                                    }
                                }
                            }
                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                if (reader.getName().equals(tagContent)) {
                                    content = reader.getElementText().getBytes();
                                }
                            }
                            if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                                
                                if (reader.getName().equals(tagArtifact))
                                    break;
                            }
                            reader.next();
                        }
                        Artifact artifact = new Artifact(UUID.randomUUID().toString(), name, contentType, content, shares, creationDate, commitDate);
                                                
                        for (ActivityDefinition activityDef : activityDefs) {
                            
                            if (artifactDefId != null && artifactDefId.equals(activityDef.getArfifact().getId())) {
                                
                                ArtifactDefinition artifactDef = activityDef.getArfifact();
                                
                                processInstanceArtifacts.get(processInstance).put(artifactDef, artifact);
                                
                                break;
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
    }
}
