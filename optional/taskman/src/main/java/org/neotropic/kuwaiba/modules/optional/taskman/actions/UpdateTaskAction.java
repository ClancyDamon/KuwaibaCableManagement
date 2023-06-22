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
package org.neotropic.kuwaiba.modules.optional.taskman.actions;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskNotificationDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskScheduleDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Updates a task.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class UpdateTaskAction extends AbstractAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;    
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Parameters
     */
    private static final String PARAM_TASK = "task";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_DESCRIPTION = "description";
    private static final String PARAM_ENABLE = "enabled";
    private static final String PARAM_COMMIT_ON_EXECUTE = "commitOnExecute";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_NOTIFICATIONTYPE = "notificationType";
    private static final String PARAM_START_TIME = "startTime";
    private static final String PARAM_EVERY_MINUTES = "everyxMinutes";
    private static final String PARAM_EXECUTIONTYPE = "executionType";
    private static final String PARAM_EXCEPTION = "exception";

    @PostConstruct
    protected void init() {
        this.id = "taskman.update-task";
        this.displayName = ts.getTranslatedString("module.taskman.task.actions.update-task.name");
        this.description = ts.getTranslatedString("module.taskman.task.actions.update-task.description");
        this.order = 1000;

        setCallback((parameters) -> {
            Task task = (Task) parameters.get(PARAM_TASK);
            ActionResponse actionResponse = new ActionResponse();
            try {
                if (parameters.containsKey(PARAM_NAME)) {
                    aem.updateTaskProperties(task.getId(), Constants.PROPERTY_NAME, (String) parameters.get(PARAM_NAME));
                    task.setName((String) parameters.get(PARAM_NAME));
                }
                if (parameters.containsKey(PARAM_DESCRIPTION)) {
                    aem.updateTaskProperties(task.getId(), Constants.PROPERTY_DESCRIPTION, (String) parameters.get(PARAM_DESCRIPTION));
                    task.setDescription((String) parameters.get(PARAM_DESCRIPTION));
                }
                if (parameters.containsKey(PARAM_ENABLE)) {
                    aem.updateTaskProperties(task.getId(), Constants.PROPERTY_ENABLED, (String) Boolean.toString((Boolean) parameters.get(PARAM_ENABLE)));
                    task.setEnabled((Boolean) parameters.get(PARAM_ENABLE));
                }
                if (parameters.containsKey(PARAM_COMMIT_ON_EXECUTE)) {
                    aem.updateTaskProperties(task.getId(), Constants.PROPERTY_COMMIT_ON_EXECUTE, (String) Boolean.toString((Boolean) parameters.get(PARAM_COMMIT_ON_EXECUTE)));
                    task.setCommitOnExecute((Boolean) parameters.get(PARAM_COMMIT_ON_EXECUTE));
                }

                // Update Task Notification Type
                TaskNotificationDescriptor notification;
                if (parameters.containsKey(PARAM_EMAIL) && parameters.containsKey(PARAM_NOTIFICATIONTYPE)) {
                    notification = new TaskNotificationDescriptor((String) parameters.get(PARAM_EMAIL), (int) parameters.get(PARAM_NOTIFICATIONTYPE));
                    aem.updateTaskNotificationType(task.getId(), notification);
                    
                    task.getNotificationType().setEmail((String) parameters.get(PARAM_EMAIL));
                    task.getNotificationType().setNotificationType((int) parameters.get(PARAM_NOTIFICATIONTYPE));
                } else if (parameters.containsKey(PARAM_EMAIL)) {
                    notification = new TaskNotificationDescriptor((String) parameters.get(PARAM_EMAIL), task.getNotificationType().getNotificationType());
                    aem.updateTaskNotificationType(task.getId(), notification);
                    task.getNotificationType().setEmail((String) parameters.get(PARAM_EMAIL));
                } else if (parameters.containsKey(PARAM_NOTIFICATIONTYPE)) {
                    notification = new TaskNotificationDescriptor(task.getNotificationType().getEmail(), (int) parameters.get(PARAM_NOTIFICATIONTYPE));
                    aem.updateTaskNotificationType(task.getId(), notification);
                    task.getNotificationType().setNotificationType((int) parameters.get(PARAM_NOTIFICATIONTYPE));
                }

                // Update Task Schedule
                TaskScheduleDescriptor schedule;
                if (parameters.containsKey(PARAM_START_TIME) && parameters.containsKey(PARAM_EVERY_MINUTES) && parameters.containsKey(PARAM_EVERY_MINUTES)) {
                    schedule = new TaskScheduleDescriptor((long) parameters.get(PARAM_START_TIME), (int) parameters.get(PARAM_EVERY_MINUTES), (int) parameters.get(PARAM_EXECUTIONTYPE));
                    aem.updateTaskSchedule(task.getId(), schedule);
                                        
                    task.getSchedule().setStartTime((long) parameters.get(PARAM_START_TIME));
                    task.getSchedule().setEveryXMinutes((int) parameters.get(PARAM_EVERY_MINUTES));
                    task.getSchedule().setExecutionType((int) parameters.get(PARAM_EXECUTIONTYPE));
                } else if (parameters.containsKey(PARAM_START_TIME)) {
                    schedule = new TaskScheduleDescriptor((long) parameters.get(PARAM_START_TIME), task.getSchedule().getEveryXMinutes(), task.getSchedule().getExecutionType());
                    aem.updateTaskSchedule(task.getId(), schedule);
                    task.getSchedule().setStartTime((long) parameters.get(PARAM_START_TIME));
                } else if (parameters.containsKey(PARAM_EVERY_MINUTES)) {
                    schedule = new TaskScheduleDescriptor(task.getSchedule().getStartTime(), (int) parameters.get(PARAM_EVERY_MINUTES), task.getSchedule().getExecutionType());
                    aem.updateTaskSchedule(task.getId(), schedule);
                    task.getSchedule().setEveryXMinutes((int) parameters.get(PARAM_EVERY_MINUTES));
                } else if (parameters.containsKey(PARAM_EXECUTIONTYPE)) {
                    schedule = new TaskScheduleDescriptor(task.getSchedule().getStartTime(), task.getSchedule().getEveryXMinutes(), (int) parameters.get(PARAM_EXECUTIONTYPE));
                    aem.updateTaskSchedule(task.getId(), schedule);
                    task.getSchedule().setExecutionType((int) parameters.get(PARAM_EXECUTIONTYPE));
                }                
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                actionResponse.put(PARAM_EXCEPTION, ex);
            }
            return actionResponse;
        });
    }

    @Override
    public int getRequiredAccessLevel() {
        return Privilege.ACCESS_LEVEL_READ_WRITE;
    }

    @Override
    public boolean requiresConfirmation() {
        return false;
    }
}