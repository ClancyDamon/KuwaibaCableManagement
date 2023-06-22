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
package org.neotropic.kuwaiba.core.apis.persistence.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The result of a task execution
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class TaskResult implements Serializable {
    /**
     * The list of messages showing the results of the task
     */
    private List<ResultMessage> messages;
    

    public TaskResult() {
        this.messages = new ArrayList<>();
    }

    public TaskResult(List<ResultMessage> messages) {
        this.messages = messages;
    }

    public List<ResultMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ResultMessage> messages) {
        this.messages = messages;
    }

    public static TaskResult createErrorResult(String message) {
        ArrayList<ResultMessage> errorMessage = new ArrayList<>();
        errorMessage.add(new ResultMessage(ResultMessage.STATUS_ERROR, message));
        return new TaskResult(errorMessage);
    }
    
    public static TaskResult createInformationResult(String message) {
        ArrayList<ResultMessage> infoMessage = new ArrayList<>();
        infoMessage.add(new ResultMessage(ResultMessage.STATUS_SUCCESS, message));
        return new TaskResult(infoMessage);
    }
    
    public static ResultMessage createErrorMessage(String message) {
        return new ResultMessage(ResultMessage.STATUS_ERROR, message);
    }
    
    public static ResultMessage createInformationMessage(String message) {
        return new ResultMessage(ResultMessage.STATUS_SUCCESS, message);
    }
    
    public static ResultMessage createWarningMessage(String message) {
        return new ResultMessage(ResultMessage.STATUS_WARNING, message);
    }
}    
    
