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

import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FunctionI18NRunner implements Runner {
    private final ElementI18N i18n;
    
    public FunctionI18NRunner(ElementI18N i18n) {
        this.i18n = i18n;
    }

    @Override
    public Object run(List parameters) {
        if (i18n != null && parameters != null && !parameters.isEmpty())
            return i18n.getMessage((String) parameters.get(0), i18n.getLang());
        return null;
    }

    @Override
    public ScriptQueryExecutor getScriptQueryExecutor() {
        return null;
    }

    @Override
    public void setScriptQueryExecutor(ScriptQueryExecutor scriptQueryExecutor) {
    }
        
}
