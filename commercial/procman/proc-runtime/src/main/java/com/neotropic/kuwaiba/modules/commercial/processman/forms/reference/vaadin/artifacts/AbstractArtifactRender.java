/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.artifacts;

import com.vaadin.flow.component.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;

/**
 * Renderer a process activity artifact.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class AbstractArtifactRender {
    /**
     * TODO: document
     */
    private final List<StringPair> sharedInformation = new ArrayList();
    /**
     * TODO: document
     */
    private final HashMap<String, String> sharedMap = new HashMap();
    
    /**
     * TODO: document
     * @return 
     */
    public abstract Component render();
    
    public abstract byte[] getContent();
    /**
     * TODO: document
     * @return 
     */
    public List<StringPair> getSharedInformation() {
        sharedInformation.clear();
        sharedMap.forEach((key, value) -> sharedInformation.add(new StringPair(key, value)));
        return sharedInformation;
    }
    /**
     * TODO: document
     * @return 
     */
    public HashMap<String, String> getSharedMap() {
        return sharedMap;
    }
}
