/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.core.persistence.reference.naming.util;

import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An Abstract class used to define the method to get the possible values of the current function
 * A dynamic section are a word between a left square bracket "[" and a right square bracket "]"
 * the content in the brackets are a function, use to define a Dynamic Name see <code>DynamicName</code>
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class DynamicSectionFunction {
    /**
     * @param functionPattern Regular expression used to verify if the entry dynamic section has  the syntax correct for the current function.
     * @param dynamicSection A dynamic section are a word between a left square bracket "[" and a right square bracket "]".
     * @throws InvalidArgumentException If the given dynamic section do not match with the given pattern
     */        
    public DynamicSectionFunction(String functionPattern, String dynamicSection) throws InvalidArgumentException {
        Pattern pattern = Pattern.compile(functionPattern);
        Matcher matcher = pattern.matcher(dynamicSection);
        
        if (!matcher.find())
            throw new InvalidArgumentException("Function in dynamic section \"" + dynamicSection + "\" does not match with the pattern " + functionPattern);
    }
        
    /**
     * Gets possible values generated with the defined function inside the Dynamic section
     * @return A list of possible values generated using the current function
     */                   
    public abstract List<String> getPossibleValues();
}
