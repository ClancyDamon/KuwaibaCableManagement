/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>
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
package org.neotropic.kuwaiba.modules.optional.reports.javascript;

import java.util.List;

/**
 * Wrapper to a JavaScrip Function.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class JavaScriptFunction {
    /**
     * The JavaScript function name.
     */
    private String functionName;
    /**
     * JavaScript function parameters.
     */
    private List<String> jsFunctionParameters;
    
    public JavaScriptFunction(String functionName) {
        this.functionName = functionName;
        this.jsFunctionParameters = null;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public List<String> getJavaScriptFunctionParameters() {
        return jsFunctionParameters;
    }

    public void setJavaScriptFunctionParameters(List<String> parameters) {
        this.jsFunctionParameters = parameters;
    }
    
    /**
     * Mapping the statements and parameters of the JavaScript Function
     * @return code block inside curly brackets {...}
     * @throws Exception Throw to notify to some error when process the code block of the function, example: data malformed.
     */
    public abstract String getCodeBlock() throws Exception;
    
    @Override
    public String toString() {
        StringBuilder jsFunction = new StringBuilder().append("function ").append(functionName).append("(");
        try {
            String codeBlock = getCodeBlock();
            
            if (jsFunctionParameters != null && !jsFunctionParameters.isEmpty()) {
                int nparameters = jsFunctionParameters.size();
                for (int i = 0; i < nparameters - 1; i += 1)
                    jsFunction.append(jsFunctionParameters.get(i)).append(",");
                jsFunction.append(jsFunctionParameters.get(nparameters - 1));
            }
            jsFunction.append(") {").append(codeBlock);
            
        } catch (Exception ex) {
            jsFunction.append(") {").append(String.format("document.write(\"<p style=\"color:red\">%s</p>\");", ex.getMessage()));
        }
        jsFunction.append("}");
        return jsFunction.toString();
    }
}
