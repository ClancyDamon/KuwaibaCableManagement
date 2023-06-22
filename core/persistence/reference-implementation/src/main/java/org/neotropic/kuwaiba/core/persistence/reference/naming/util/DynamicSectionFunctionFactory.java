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
 * Factory to dynamic name functions
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DynamicSectionFunctionFactory {
    
    private static boolean isDynamicSectionFunction(String functionPattern, String dynamicSection) {
        Pattern pattern = Pattern.compile(functionPattern);
        Matcher matcher = pattern.matcher(dynamicSection);
        
        return matcher.find();
    }
        
    public static AlphabeticUppercaseSequence getAlphabeticUppercaseSequence(String dynamicSection) 
        throws InvalidArgumentException {
        
        if (isDynamicSectionFunction(AlphabeticUppercaseSequence.FUNCTION_PATTERN, dynamicSection))
            return new AlphabeticUppercaseSequence(dynamicSection);
        return null;
    }
    
    public static AlphabeticLowercaseSequence getAlphabeticLowercaseSequence(String dynamicSection) 
        throws InvalidArgumentException {
        
        if (isDynamicSectionFunction(AlphabeticLowercaseSequence.FUNCTION_PATTERN, dynamicSection))
            return new AlphabeticLowercaseSequence(dynamicSection);
        return null;
    }
    
    public static NumericSequence getNumericSequence(String dynamicSection) 
        throws InvalidArgumentException {
        
        if (isDynamicSectionFunction(NumericSequence.FUNCTION_PATTERN, dynamicSection))
            return new NumericSequence(dynamicSection);
        return null;
    }
    
    public static FunctionValue getFunctionValue(String dynamicSection) 
        throws InvalidArgumentException {
        
        if (isDynamicSectionFunction(FunctionValue.FUNCTION_PATTERN, dynamicSection))
            return new FunctionValue(dynamicSection);
        return null;
    }
    
    public static MirrorPortsFunction getMirrorPortsPairing(String dynamicSection)
            throws InvalidArgumentException {
        
        if (isDynamicSectionFunction(MirrorPortsFunction.FUNCTION_PATTERN, dynamicSection))
            return new MirrorPortsFunction(dynamicSection);
        return null;
    }
    
    public static MirrorSplicePortsFunction getMirrorSplicePortsPairing(String dynamicSection)
            throws InvalidArgumentException {
        
        if (isDynamicSectionFunction(MirrorSplicePortsFunction.FUNCTION_PATTERN, dynamicSection))
            return new MirrorSplicePortsFunction(dynamicSection);
        return null;
    }
}
