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
import java.util.HashMap;
import java.util.List;

/**
 * Class to get a set of dynamic names given a expression to build a name
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DynamicNameGenerator {
    /**
     * A map to storage the function assigned to a dynamic section
     */
    private HashMap<String, DynamicSectionFunction> functions;
    /**
     * List of static sections. A static section are the section which no change
     * in the name
     */
    private List<String> staticSections;
    /**
     * List of dynamic sections. A dynamic section are the section which is the
     * variable part of the name, defined by a given function
     */
    private List<String> dynamicSections;
    /**
     * List of names, generated from the given expression
     */
    private List<String> dynamicNames;

    public DynamicNameGenerator(String expressionForTheName) throws InvalidArgumentException {
        staticSections = new ArrayList();
        dynamicSections = new ArrayList();
        functions = new HashMap();

        String section = "";
        boolean squareBracketOpen = false;
        expressionForTheName = expressionForTheName.replace(" ", "");
        for (int i = 0; i < expressionForTheName.length(); i += 1) {
            if (expressionForTheName.charAt(i) == '[' && squareBracketOpen) 
                throw new InvalidArgumentException("A left square bracket \"[\" was open but never closed");

            if (expressionForTheName.charAt(i) == ']' && !squareBracketOpen)
                throw new InvalidArgumentException("A right square bracket \"]\" was closed but never open");

            switch (expressionForTheName.charAt(i)) {
                case '[':
                    staticSections.add(section);
                    section = "";
                    squareBracketOpen = true;
                    section += expressionForTheName.charAt(i);
                    break;
                case ']':
                    squareBracketOpen = false;
                    section += expressionForTheName.charAt(i);
                    dynamicSections.add(section);
                    section = "";
                    break;
                default:
                    section += expressionForTheName.charAt(i);
                    break;
            }
        }
        if (squareBracketOpen)
            throw new InvalidArgumentException("A left square bracket \"[\" was open but never was close");
        
        if (!section.equals(""))
            staticSections.add(section);
        
        setFunctions();
    }

    /**
     * if is true it means that the created sequence are mirror ports and they
     * shall be connected
     */
    public boolean isMirrorPortsSequence() {
        return dynamicSections.size() > 0 && dynamicSections.get(0).contains("mirror");
    }

    public void recursiveName(String values, int idxNextDynamicSection) {
        if (idxNextDynamicSection == -1) {
            String[] arrayOfValues = values.split(",");
            dynamicNames.add(getDynamicName(arrayOfValues));
            return;
        }
        for (String value : functions.get(dynamicSections.get(idxNextDynamicSection)).getPossibleValues())
            recursiveName(values + value + ",", idxNextDynamicSection + 1 < dynamicSections.size() ? idxNextDynamicSection + 1 : -1);
    }

    public List<String> getDynamicNames() {
        if (dynamicNames == null) {
            dynamicNames = new ArrayList();
            if (dynamicSections.size() > 0) {
                for (String value : functions.get(dynamicSections.get(0)).getPossibleValues()) 
                    recursiveName(value + ",", 1 < dynamicSections.size() ? 1 : -1);
            } else
                recursiveName(",", -1);
        }

        return dynamicNames;
    }

    public int getNumberOfDynamicNames() {
        if (dynamicNames == null)
            getDynamicNames();
        
        return dynamicNames.size();
    }

    private String getDynamicName(String[] valuesOfDynamicSections) {
        if (valuesOfDynamicSections.length != dynamicSections.size())
            return null;

        String dynamicName = "";
        for (int i = 0; i < staticSections.size(); i += 1) {
            dynamicName += staticSections.get(i);
            if (i < valuesOfDynamicSections.length) 
                dynamicName += valuesOfDynamicSections[i];
        }
        return dynamicName;
    }

    private void setFunctions() throws InvalidArgumentException {

        for (String dynamicSection : dynamicSections) {
            DynamicSectionFunction function;

            if ((function = DynamicSectionFunctionFactory.getAlphabeticLowercaseSequence(dynamicSection)) != null) 
                functions.put(dynamicSection, function);
            else if ((function = DynamicSectionFunctionFactory.getAlphabeticUppercaseSequence(dynamicSection)) != null)
                functions.put(dynamicSection, function);
            else if ((function = DynamicSectionFunctionFactory.getNumericSequence(dynamicSection)) != null)
                functions.put(dynamicSection, function);
            else if ((function = DynamicSectionFunctionFactory.getFunctionValue(dynamicSection)) != null)
                functions.put(dynamicSection, function);
            else if ((function = DynamicSectionFunctionFactory.getMirrorPortsPairing(dynamicSection)) != null)
                functions.put(dynamicSection, function);               
            else if ((function = DynamicSectionFunctionFactory.getMirrorSplicePortsPairing(dynamicSection)) != null)
               functions.put(dynamicSection, function);               
            else 
                throw new InvalidArgumentException(String.format("Function %s not defined", dynamicSection));
        }
    }
}
