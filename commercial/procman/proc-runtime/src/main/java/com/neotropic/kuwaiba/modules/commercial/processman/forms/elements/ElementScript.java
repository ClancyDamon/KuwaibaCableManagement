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

import java.util.HashMap;
import java.util.function.Consumer;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * POJO wrapper of a <b>script</b> element in a Form Artifact Definition and 
 * external scripts.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ElementScript implements Tag {
    private final HashMap<String, Runner> functions;
    private final Consumer<FunctionRunnerException> consumerFuncRunnerEx;
    private final HashMap<String, Object> funcRunnerParams;
        
    public ElementScript(Consumer<FunctionRunnerException> consumerFuncRunnerEx, HashMap<String, Object> funcRunnerParams) {
        functions = new HashMap();
        this.consumerFuncRunnerEx = consumerFuncRunnerEx;
        this.funcRunnerParams = funcRunnerParams;
    }
    
    public HashMap<String, Runner> getFunctions() {
        return functions;
    }
        
    public Runner getFunctionByName(String name) {
        return functions != null ? functions.get(name) : null;
    }
    
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        QName tagScript = new QName(Constants.Tag.SCRIPT);
        QName tagFunction = new QName(Constants.Tag.FUNCTION);
                
        while (true) {
            reader.nextTag();
            
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                
                if (reader.getName().equals(tagFunction)) {
                    
                    String functionName = reader.getAttributeValue(null, Constants.Attribute.NAME);
                    String functionType = reader.getAttributeValue(null, Constants.Attribute.TYPE);
                    
                    if (functionName != null && functionType != null) {
                        
                        String parameterNames = reader.getAttributeValue(null, Constants.Attribute.PARAMETER_NAMES);
                        String queryName = reader.getAttributeValue(null, Constants.Attribute.QUERY_NAME);
                        String message = reader.getAttributeValue(null, Constants.Attribute.MESSAGE);
                        String blockOfCode = reader.getElementText();

                        if (Constants.Function.Type.FUNCTION.equals(functionType)) {
                            if (blockOfCode != null)
                                getFunctions().put(functionName, new FunctionRunner(functionName, parameterNames, blockOfCode, ElementScript.this, consumerFuncRunnerEx, funcRunnerParams));
                        }
                        if (Constants.Function.Type.QUERY.equals(functionType)) {
                            if (queryName != null)
                                getFunctions().put(functionName, new ScriptQueryRunner(functionName, queryName, parameterNames));
                        }
                        if (Constants.Function.Type.VALIDATOR.equals(functionType)) {
                            if (message != null)
                                getFunctions().put(functionName, new ValidatorRunner(functionName, parameterNames, blockOfCode, message));
                        }
                    }
                }
            }
            if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                
                if (reader.getName().equals(tagScript))
                    return;
            }
        }
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.SCRIPT;
    }
    
}