/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.forms.elements;

import java.util.List;

/**
 * Implements and allows to execute default functions for the process engine 
 * within the elements of type script in the Form Artifact Definition
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface ScriptQueryExecutor {
    Object execute(String scriptQueryName, List<String> parameterNames, List<String> parameterValues);
    /**
     * Sets debug equal true to show the error messages
     * @param debug true to enable error messages
     */
    void setDebug(boolean debug);
    
    void openMessage(String message);
}
