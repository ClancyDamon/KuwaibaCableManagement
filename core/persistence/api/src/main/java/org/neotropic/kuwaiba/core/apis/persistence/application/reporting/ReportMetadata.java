/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.core.apis.persistence.application.reporting;

import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import java.util.List;

/**
 * An application report. A report might apply only to all instances of the same class or superclass 
 * (inventory objects in a particular location, capacity of certain equipment, etc)  
 * or a broad scope report, involving information from different inventory objects not necessarily related
 * (for example a report about all the elements in the database, or capacity reports across different domains).
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ReportMetadata extends ReportMetadataLight {
    /**
     * Script text.
     */
    private String script;
    /**
     * Parameters list.
     */
    private List<StringPair> parameters;

    public ReportMetadata(long id, String name, String description, boolean enabled,
            int type, String script, List<StringPair> parameters) {
        super(id, name, description, enabled, type);
        this.script = script;
        this.parameters = parameters;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public List<StringPair> getParameters() {
        return parameters;
    }

    public void setParameters(List<StringPair> parameters) {
        this.parameters = parameters;
    }
}
