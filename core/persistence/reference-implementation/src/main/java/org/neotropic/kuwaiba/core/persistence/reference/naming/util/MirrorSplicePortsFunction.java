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
 * Implements the <code>mirror-splice(a, b)</code> function, that generates
 * pairs of ports numbered from <code>a</code> to <code>b</code>. <code>a</code>
 * and <code>b</code> must be integers greater or equal to 0, static prefix an
 * suffix IN and OUT
 *
 * @author Hardy Ryan Chingal {@literal <ryan.chingal@kuwaiba.org>}
 */
public class MirrorSplicePortsFunction extends DynamicSectionFunction {

    public static final String FUNCTION_PATTERN = "mirror-splice\\([0-9]+,[0-9]+\\)";
    protected int parameter1;
    protected int parameter2;

    protected MirrorSplicePortsFunction(String functionPattern, String dynamicSection) throws InvalidArgumentException {
        super(functionPattern, dynamicSection);
    }

    public MirrorSplicePortsFunction(String dynamicSectionFunction) throws InvalidArgumentException {
        this(FUNCTION_PATTERN, dynamicSectionFunction);

        Pattern pattern = Pattern.compile("[0-9]+,[0-9]+");
        Matcher matcher = pattern.matcher(dynamicSectionFunction);
        if (matcher.find()) {
            parameter1 = Integer.parseInt(matcher.group().split(",")[0]);
            parameter2 = Integer.parseInt(matcher.group().split(",")[1]);

            if (parameter1 >= parameter2) {
                throw new InvalidArgumentException("Dynamic section function malformed \"" + dynamicSectionFunction + "\" the parameter " + parameter1 + " greater than or equal to " + parameter2);
            }
        }
        
    }

    @Override
    public List<String> getPossibleValues() {
        List<String> dynamicSections = new ArrayList();

        for (int c = parameter1; c <= parameter2; c++) {
            dynamicSections.add(c + "-IN");
            dynamicSections.add(c + "-OUT");
        }
        return dynamicSections;
    }
}
