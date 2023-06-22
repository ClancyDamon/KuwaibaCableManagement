/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.service;

import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;

/**
 * Represents the functions of an artifact.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class ArtifactDefinitionFunction {
    /**
     * Function type
     */
    private String type;
    /**
     * Function name
     */
    private String name;
    /**
     * Function parameter names
     */
    private List<StringPair> parameters;
    /**
     * Function return
     */
    private String returns;
    /**
     * Function script value
     */
    private String value;
    
    public ArtifactDefinitionFunction() { }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StringPair> getParameters() {
        return parameters;
    }

    public void setParameters(List<StringPair> parameters) {
        this.parameters = parameters;
    }

    public String getReturns() {
        return returns;
    }

    public void setReturns(String returns) {
        this.returns = returns;
    }   

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return getName();
    }
}