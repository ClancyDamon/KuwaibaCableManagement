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
package com.neotropic.kuwaiba.modules.commercial.processman.scripts;

import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.Constants;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FileInformation;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ScriptQueryExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Implementation of a set of functions executed in the process definition.
 * Like shared to get the value of shared fields in process instance,
 * activityConditionalValue to get the value of an activity of conditional type
 * in a process instance.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ScriptQueryExecutorImpl implements ScriptQueryExecutor {
    private final String SCRIPT_QUERY_SHARED = "shared"; //NOI18N
    private final String SCRIPT_ACTIVITY_CONDITIONAL_VALUE = "activityConditionalValue"; //NOI18N
    private final String SCRIPT_QUERY_ACTIVITY_HAS_ARTIFACT = "activityHasArtifact"; //NOI18N
    private final String PARAM_VAL_PROCESS_INSTANCE_ID = "__processInstanceId__"; //NOI18N
    private final String PARAM_VAL_USERNAME = "__userName__"; //NOI18N
    private final String ROWS_COUNT = "rowscount"; //NOI18N
    private final String COLUMNS_COUNT = "columnscount"; //NOI18N
    
    private final Session session;
    private final ProcessInstance processInstance;
    private final BusinessEntityManager bem;
    private final ApplicationEntityManager aem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    
    public ScriptQueryExecutorImpl(ProcessInstance processInstance, Session session, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        Objects.requireNonNull(processInstance);
        Objects.requireNonNull(session);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        this.session = session;
        this.processInstance = processInstance;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts; 
    }

    @Override
    public Object execute(String scriptQueryName, List<String> parameterNames, List<String> parameterValues) {
        if (SCRIPT_ACTIVITY_CONDITIONAL_VALUE.equals(scriptQueryName) && parameterValues != null && parameterValues.size() == 1) {
            try {
                String paramValue0 = parameterValues.get(0);
                Artifact artifact = aem.getArtifactForActivity(processInstance.getId(), paramValue0);
                String content = new String(artifact.getContent());
                return content.contains("true"); //NOI18N
            } catch (ApplicationObjectNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        } else if (SCRIPT_QUERY_ACTIVITY_HAS_ARTIFACT.equals(scriptQueryName) && parameterValues != null && parameterValues.size() == 1) {
            try {
                String paramVal0 = parameterValues.get(0);
                Artifact artifact = aem.getArtifactForActivity(processInstance.getId(), paramVal0);
                return artifact != null;
            } catch (ApplicationObjectNotFoundException ex) {
                return false;
            }
        } else if (SCRIPT_QUERY_SHARED.equals(scriptQueryName) && parameterValues != null && parameterValues.size() >= 1) {
            String paramVal0 = parameterValues.get(0);
            if (PARAM_VAL_PROCESS_INSTANCE_ID.equals(paramVal0))
                return String.valueOf(processInstance.getId());
            if (PARAM_VAL_USERNAME.equals(paramVal0))
                return session.getUser().getUserName();
            if (parameterValues.size() == 2 || parameterValues.size() == 3) {
                String activityId = paramVal0;
                String sharedId = parameterValues.get(1);
                String processInstanceId = processInstance.getId();
                if (parameterValues.size() == 3)
                    processInstanceId = parameterValues.get(3);
                
                Artifact artifact = null;
                try {
                    artifact = aem.getArtifactForActivity(processInstanceId, activityId);
                } catch (InventoryException ex) {
                    return null;
                }
                if (artifact != null) {
                    List<StringPair> sharedInformation = artifact.getSharedInformation();
                    if (sharedInformation != null) {
                        Properties sharedInfo = new Properties();
                        sharedInformation.forEach(item -> sharedInfo.setProperty(item.getKey(), item.getValue()));
                        if (isGrid(sharedId, sharedInfo))
                            return getRows(sharedId, sharedInfo);
                        else if (sharedInfo.containsKey(sharedId))
                            return sharedInfo.getProperty(sharedId);
                        else if (sharedInfo.containsKey(sharedId + Constants.Attribute.DATA_TYPE)) {
                            if (Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH.equals(sharedInfo.getProperty(sharedId + Constants.Attribute.DATA_TYPE))) {
                                if (sharedInfo.containsKey(sharedId + Constants.Attribute.CLASS_NAME) &&
                                    sharedInfo.containsKey(sharedId + Constants.Attribute.OBJECT_ID) &&
                                    sharedInfo.containsKey(sharedId + Constants.Attribute.OBJECT_NAME)) {
                                    
                                    String objClassName = sharedInfo.getProperty(sharedId + Constants.Attribute.CLASS_NAME);
                                    String objId = sharedInfo.getProperty(sharedId + Constants.Attribute.OBJECT_ID);
                                    
                                    try {
                                        return bem.getObjectLight(objClassName, objId);
                                    } catch (InventoryException ex) {
                                        new SimpleNotification(
                                            ts.getTranslatedString("module.general.messages.error"), 
                                            ex.getLocalizedMessage(), 
                                            AbstractNotification.NotificationType.ERROR, 
                                            ts
                                        ).open();
                                        return sharedInfo.getProperty(sharedId + Constants.Attribute.OBJECT_NAME);
                                    }
                                }
                            } else if (Constants.Attribute.DataType.CLASS_INFO_LIGTH.equals(sharedInfo.getProperty(sharedId + Constants.Attribute.DATA_TYPE))) {
                                if (sharedInfo.containsKey(sharedId + Constants.Attribute.NAME)) {
                                    String className = sharedInfo.getProperty(sharedId + Constants.Attribute.NAME);
                                    try {
                                        return mem.getClass(className);
                                    } catch (MetadataObjectNotFoundException ex) {
                                        new SimpleNotification(
                                            ts.getTranslatedString("module.general.messages.error"), 
                                            ex.getLocalizedMessage(), 
                                            AbstractNotification.NotificationType.ERROR, 
                                            ts
                                        ).open();
                                        return sharedInfo.getProperty(sharedId + Constants.Attribute.NAME);
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }
        } else if ("notifications".equals(scriptQueryName) && //NOI18N
            parameterNames != null && parameterNames.size() >= 1 &&
            parameterValues != null && parameterValues.size() >= 1 &&
            parameterNames.size() == parameterValues.size()) {
            
            for (int i = 0; i < parameterNames.size(); i++) {
                String parameterName = parameterNames.get(i);
                
                if (null != parameterName) {
                    switch (parameterName) {
                        case "error": //NOI18N
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                parameterValues.get(i), 
                                AbstractNotification.NotificationType.ERROR, 
                                ts
                            ).open();
                            break;
                        case "info": //NOI18N
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.information"), 
                                parameterValues.get(i), 
                                AbstractNotification.NotificationType.INFO, 
                                ts
                            ).open();
                            break;
                        case "warning": //NOI18N
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.warning"), 
                                parameterValues.get(i), 
                                AbstractNotification.NotificationType.WARNING, 
                                ts
                            ).open();
                            break;
                        default:
                            break;
                    }
                }
            }
            return null;
        }
        throw new UnsupportedOperationException(String.format("Script query %s not supported yet.", scriptQueryName));
    }
    
    private boolean isGrid(String sharedId, Properties sharedInfo) {
        return sharedId != null && sharedInfo != null && 
            sharedInfo.containsKey(sharedId + ROWS_COUNT) && 
            sharedInfo.containsKey(sharedId + COLUMNS_COUNT);
    }
    
    private List<List<Object>> getRows(String gridId, Properties sharedInfo) {
        try {
            int rowCount = Integer.valueOf(sharedInfo.getProperty(gridId + ROWS_COUNT));
            int columnCount = Integer.valueOf(sharedInfo.getProperty(gridId + COLUMNS_COUNT));
            
            if (rowCount > 0 && columnCount > 0) {
                List<List<Object>> rows = new ArrayList();
                
                for (int i = 0; i < rowCount; i += 1) {
                    List<Object> row = new ArrayList();
                    
                    for (int j = 0; j < columnCount; j += 1) {
                        String sharedId = gridId + i + j;
                        
                        if (Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH.equals(
                            sharedInfo.getProperty(sharedId + Constants.Attribute.DATA_TYPE))) {
                                                        
                            String objectName = sharedInfo.getProperty(sharedId + Constants.Attribute.OBJECT_NAME);
                            String objectId = sharedInfo.getProperty(sharedId + Constants.Attribute.OBJECT_ID);
                            String className = sharedInfo.getProperty(sharedId + Constants.Attribute.CLASS_NAME);
                            
                            try {
                                BusinessObjectLight businessObject = bem.getObjectLight(className, objectId);
                                
                                row.add(businessObject != null ? businessObject : (objectName != null ? objectName : ""));
                            } catch (Exception exception) {
                                row.add(sharedInfo.get(sharedId) != null ? sharedInfo.get(sharedId) : "");
                            }
                        } else if (Constants.Attribute.DataType.STRING.equals(
                            sharedInfo.getProperty(sharedId + Constants.Attribute.DATA_TYPE))) {
                            
                            row.add(sharedInfo.get(sharedId) != null ? sharedInfo.get(sharedId) : "");
                        } else if (Constants.Attribute.DataType.ATTACHMENT.equals(
                            sharedInfo.getProperty(sharedId + Constants.Attribute.DATA_TYPE))) {
                            
                            String name = sharedInfo.getProperty(sharedId + Constants.Attribute.NAME);
                            String path = sharedInfo.getProperty(sharedId + Constants.Attribute.PATH);
                            
                            if (name != null && path != null) {
                                FileInformation fileInformation = new FileInformation(name, path);
                                row.add(fileInformation);
                            }
                            else {
                                row.add(sharedInfo.get(sharedId) != null ? sharedInfo.get(sharedId) : "");
                            }
                        } else if (Constants.Attribute.DataType.CLASS_INFO_LIGTH.equals(
                            sharedInfo.getProperty(sharedId + Constants.Attribute.DATA_TYPE))) {
                            String className = sharedInfo.getProperty(sharedId + Constants.Attribute.DATA_TYPE);
                            if (className != null) {
                                ClassMetadataLight sharedClass = mem.getClass(className);
                                row.add(sharedClass);
                            }
                            else
                                row.add(sharedInfo.get(sharedId) != null ? sharedInfo.get(sharedId) : "");
                        }
                    }
                    rows.add(row);
                }
                return rows;
            }            
        } catch(Exception ex) {
            // Expected NumberFormatException or another parse error of an data type no supported in grids                        
        }
        return null;
    }
    
    @Override
    public void setDebug(boolean debug) {
    }
    
    @Override
    public void openMessage(String message) {
        if (message != null) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.information"), 
                message, 
                AbstractNotification.NotificationType.INFO, 
                ts
            ).open();
        }
    }
}
