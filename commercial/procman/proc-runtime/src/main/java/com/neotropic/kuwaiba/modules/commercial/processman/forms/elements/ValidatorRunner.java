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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ValidatorRunner implements Runner {
    private String validatorName;
    private List<String> parameterNames = new ArrayList();
    private String script;
    private String message;
    
    private ScriptQueryExecutor scriptQueryExecutor;
    
    public ValidatorRunner(String validatorName, String paramNames, String script, String message) {
        this.validatorName = validatorName;
        if (paramNames != null) {
            
            String[] parameterNamesArray = paramNames.split(" ");
            
            if (parameterNamesArray != null) {
                parameterNames = new ArrayList();
                parameterNames.addAll(Arrays.asList(parameterNamesArray));
            }
        }
        this.script = script;
        this.message = message; 
    }
    
    public String getValidatorName() {
        return validatorName;
    }
    
    public void setValidatorName(String validatorName) {
        this.validatorName = validatorName;
    }
    
    public List<String> getParameterNames() {
        return parameterNames;                
    }
    
    public String getScript() {
        return script;
    }
    
    public void setScript(String script) {
        this.script = script;
    }
    
    public void setParameterNames(List<String> parameterNames) {
        this.parameterNames = parameterNames;        
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
            
    @Override
    public Object run(List parameters) {
        GroovyShell shell = null;
        
        Binding binding = new Binding();        
        
        binding.setVariable("scriptQueryExecutor", scriptQueryExecutor);
                
        if (parameterNames != null && parameters != null && parameterNames.size() == parameters.size()) {
            
            for (int i = 0; i < parameters.size(); i++)
                binding.setVariable(parameterNames.get(i), parameters.get(i));
            
            shell = new GroovyShell(FunctionRunner.class.getClassLoader(), binding);
        } else
            shell = new GroovyShell(FunctionRunner.class.getClassLoader());
                
        return shell.evaluate(script);
    }

    @Override
    public ScriptQueryExecutor getScriptQueryExecutor() {
        return scriptQueryExecutor;
    }

    @Override
    public void setScriptQueryExecutor(ScriptQueryExecutor scriptQueryExecutor) {
        this.scriptQueryExecutor = scriptQueryExecutor;
    }
    
}
