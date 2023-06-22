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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to get an ascending numeric sequence, given the start and end of the sequence
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NumericSequence extends DynamicSectionFunction {
    public static final String FUNCTION_PATTERN = "sequence\\([0-9]+,[0-9]+\\)";
    private int parameter1;
    private int parameter2;
    
    protected NumericSequence(String dynamicSectionPattern, String dynamicSectionFunction) throws InvalidArgumentException {
        super(dynamicSectionPattern, dynamicSectionFunction);
    }
    
    public NumericSequence(String dynamicSectionFunction) throws InvalidArgumentException {
        this(FUNCTION_PATTERN, dynamicSectionFunction);
        
        Pattern pattern = Pattern.compile("[0-9]+,[0-9]+");
        Matcher matcher = pattern.matcher(dynamicSectionFunction);
        if (matcher.find()) {
            parameter1 = Integer.parseInt(matcher.group().split(",")[0]);
            parameter2 = Integer.parseInt(matcher.group().split(",")[1]);
            
            if (parameter1 >= parameter2)
                throw new InvalidArgumentException("Function definition malformed: In \"" + dynamicSectionFunction + "\", the parameter " + parameter1 + " is greater than or equal to " + parameter2);
        }
    }
    
    @Override
    public List<String> getPossibleValues() {
        List<String> dynamicSections = new ArrayList();
        for (int i = parameter1; i <= parameter2; i++)
            dynamicSections.add(dynamicSectionFormat(parameter2, i));
        return dynamicSections;
    }
    
    private String dynamicSectionFormat(int maxValue, int value) {
        int zeros = 1; // Used to define the number of left zeros
        int ones = 1; // start with: ones

        while (true) {
          if (maxValue/ones == 1 || maxValue/(ones * 10) < 1) {
            break;
          }
          ones *= 10; // follow with tens, hundreds ...
          zeros += 1;
        }
        if (zeros < 3) {
            zeros = 3; // The number of left zeros must be minimum three
        }
        return String.format("%0" + zeros + "d", value);
    }
}
