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

package org.neotropic.kuwaiba.northbound.ws.model.application;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;

/**
 * Wrapper for entity class Privilege.
 * @author Adrian Fernando Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class PrivilegeInfo implements Serializable{
    /**
     * The unique id of the feature (for example, a web service method or a simple string with the name of the module)
     */
    private String featureToken;
    /**
     * Access level. See ACCESS_LEVEL* for possible values 
     */
    private int accessLevel;

    //No-arg constructor required
    public PrivilegeInfo() { }
    
    public PrivilegeInfo(Privilege privilege) {
        this.featureToken = privilege.getFeatureToken();
        this.accessLevel = privilege.getAccessLevel();
    }

    public String getFeatureToken() {
        return featureToken;
    }

    public void setFeatureToken(String featureToken) {
        this.featureToken = featureToken;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    
}
