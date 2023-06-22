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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to get an ascending alphabetic uppercase sequence, given the start and end of the sequence
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class AlphabeticUppercaseSequence extends AlphabeticLowercaseSequence {
    public static final String FUNCTION_PATTERN = "sequence\\([A-Z],[A-Z]\\)";
            
    public AlphabeticUppercaseSequence(String dynamicSectionFunction) throws InvalidArgumentException {
        super(FUNCTION_PATTERN, dynamicSectionFunction);
        
        Pattern pattern = Pattern.compile("[A-Z],[A-Z]");
        Matcher matcher = pattern.matcher(dynamicSectionFunction);
        if (matcher.find()) {
            parameter1 = matcher.group().split(",")[0].charAt(0);
            parameter2 = matcher.group().split(",")[1].charAt(0);
                
            if (parameter1 >= parameter2)
                throw new InvalidArgumentException("Function definition malformed: In \"" + dynamicSectionFunction + "\", the parameter " + parameter1 + " greater than or equal to " + parameter2);
        }
    }
}
