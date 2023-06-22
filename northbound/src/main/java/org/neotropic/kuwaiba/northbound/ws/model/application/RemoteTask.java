/**
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
package org.neotropic.kuwaiba.northbound.ws.model.application;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;

/**
 * Remote representation of a task
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */

@XmlAccessorType(XmlAccessType.FIELD)
public final class RemoteTask implements Serializable {
    /**
     * Task id
     */
    private long id;
    /**
     * Task name
     */
    private String name;
    /**
     * Task description
     */
    private String description;
    /**
     * Id this task enabled?
     */
    private boolean enabled;
    /**
     * Should this task commit the changes (if any) after its execution. Handle with care!
     */
    private boolean commitOnExecute;
    /**
     * Task script
     */
    private String script;
    /**
     * List of parameters as a set of parameter name/value pairs
     */
    private List<StringPair> parameters;
    /**
     * When the task should be executed
     */
    private RemoteTaskScheduleDescriptor schedule;
    /**
     * How the results of the task should be notified to the subscribed users
     */
    private RemoteTaskNotificationDescriptor notificationType;
    /**
     * Users subscribed to this task
     */
    private List<RemoteUserInfoLight> users;

    //No-arg constructor required
    public RemoteTask() {   }

    public RemoteTask(long id, String name, String description, boolean enabled, boolean commitOnExecute,
            String script, List<StringPair> parameters, RemoteTaskScheduleDescriptor schedule, RemoteTaskNotificationDescriptor notificationType, List<RemoteUserInfoLight> users) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.commitOnExecute = commitOnExecute;
        this.script = script;
        this.parameters = parameters;
        this.schedule = schedule;
        this.notificationType = notificationType;
        this.users = users;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<StringPair> getParameters() {
        return parameters;
    }

    public void setParameters(List<StringPair> parameters) {
        this.parameters = parameters;
    }

    public RemoteTaskScheduleDescriptor getSchedule() {
        return schedule;
    }

    public void setSchedule(RemoteTaskScheduleDescriptor schedule) {
        this.schedule = schedule;
    }

    public RemoteTaskNotificationDescriptor getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(RemoteTaskNotificationDescriptor notificationType) {
        this.notificationType = notificationType;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<RemoteUserInfoLight> getUsers() {
        return users;
    }

    public void setUsers(List<RemoteUserInfoLight> users) {
        this.users = users;
    }

    public boolean isCommitOnExecute() {
        return commitOnExecute;
    }

    public void setCommitOnExecute(boolean commitOnExecute) {
        this.commitOnExecute = commitOnExecute;
    }
}    
    