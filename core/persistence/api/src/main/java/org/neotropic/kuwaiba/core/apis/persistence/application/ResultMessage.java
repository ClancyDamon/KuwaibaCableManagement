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

/**
 * The result of a task execution
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */

public class ResultMessage implements Serializable {
    /**
     * The task resulted in error. The consumer should check the errorMessage for details
     */
    public static final int STATUS_ERROR = 1;
    /**
     * The execution was successful
     */
    public static final int STATUS_SUCCESS = 2;
    /**
     * The execution had non-blocking errors. There will be messages, but also an error message
     */
    public static final int STATUS_WARNING = 3;
    /**
     * The list of messages showing the results of the task
     */
    private String message;
    private int messageType;

    public ResultMessage(int messagetType, String message) {
        this.messageType = messagetType;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
}    
    