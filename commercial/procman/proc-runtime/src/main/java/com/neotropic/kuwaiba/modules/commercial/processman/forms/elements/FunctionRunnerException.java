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
package com.neotropic.kuwaiba.modules.commercial.processman.forms.elements;

/**
 * Exception that is thrown when a {@link FunctionRunner function is executed}
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FunctionRunnerException extends Exception {
    private final Exception exception;
    private final String functionName;
    
    public FunctionRunnerException(String functionName, Exception exception) {
        this.functionName = functionName;
        this.exception = exception;
    }

    public String getFunctionName() {
        return functionName;
    }
    
    public Exception getException() {
        return exception;
    }
}
