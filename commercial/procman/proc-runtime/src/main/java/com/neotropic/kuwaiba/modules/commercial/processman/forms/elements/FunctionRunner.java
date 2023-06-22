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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FunctionRunner implements Runner {
    /**
     * Function name precondition
     */
    public static final String FUNC_NAME_PRECONDITIONS = "preconditions";
    /**
     * Function name postconditions
     */
    public static final String FUNC_NAME_POSTCONDITIONS = "postconditions";
    /**
     * Parameter name processInstanceId
     */
    public static final String PARAM_NAME_PROCESS_INSTANCE_ID = "processInstanceId";
    /**
     * Parameter name activityDefinitionId
     */
    public static final String PARAM_NAME_ACTIVITY_DEFINITION_ID = "activityDefinitionId";
    /**
     * Parameter name nextActivityDefinitionId
     */
    public static final String PARAM_NAME_NEXT_ACTIVITY_DEFINITION_ID = "nextActivityDefinitionId";
    /**
     * Parameter name printableTemplateInstance
     */
    public static final String PARAM_NAME_PRINTABLE_TEMPLATE_INSTANCE = "printableTemplateInstance";
    
    private String functionName;
    private List<String> parameterNames = new ArrayList();;
    private String script;
    
    private final ElementScript elementScript;         
    private ScriptQueryExecutor scriptQueryExecutor;
    private final Consumer<FunctionRunnerException> consumerFuncRunnerEx;
    private final HashMap<String, Object> funcRunnerParams;
    
    public FunctionRunner(String functionName, String paramNames, String script, ElementScript elementScript, 
        Consumer<FunctionRunnerException> consumerFuncRunnerEx, HashMap<String, Object> funcRunnerParams) {
        this.functionName = functionName;
        
        if (paramNames != null) {
            
            String[] parameterNamesArray = paramNames.split(" ");
            
            if (parameterNamesArray != null) {
                parameterNames = new ArrayList();
                parameterNames.addAll(Arrays.asList(parameterNamesArray));
            }
        }
        this.script = script;
        this.elementScript = elementScript;
        this.consumerFuncRunnerEx = consumerFuncRunnerEx;
        this.funcRunnerParams = funcRunnerParams;
    }
    
    public String getFunctionName() {
        return functionName;
    }
    
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
    
    public List<String> getParametersNames() {
        return parameterNames;        
    }
    
    public void setParametersNames(List<String> parametersNames) {
        this.parameterNames = parametersNames;
    }
    
    public String getScript() {
        return script;        
    }
    
    public void setScript(String script) {
        this.script = script;        
    }
    
    @Override
    public Object run(List parameters) {
        
        Binding binding = new Binding();
        
        binding.setVariable("scriptQueryExecutor", scriptQueryExecutor);
        binding.setVariable("elementScript", elementScript);
        if (funcRunnerParams != null) {
            funcRunnerParams.forEach((key, value) -> {
                if (key != null && value != null)
                    binding.setVariable(key, value);
            });
        }
        if (parameterNames != null && parameters != null && parameterNames.size() == parameters.size()) {
            
            for (int i = 0; i < parameters.size(); i++)
                binding.setVariable(parameterNames.get(i), parameters.get(i));
        }
        GroovyShell shell = new GroovyShell(FunctionRunner.class.getClassLoader(), binding);
        
        try {
            return shell.evaluate(script);
        } catch(Exception ex) {
            if (consumerFuncRunnerEx != null)
                consumerFuncRunnerEx.accept(new FunctionRunnerException(functionName, ex));
            
            System.out.println(String.format(
                "[KUWAIBA] [%s] [PROCESS ENGINE] Executed Function %s", 
                Calendar.getInstance().getTime(), getFunctionName()
            ));
            throw ex;
        }
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
