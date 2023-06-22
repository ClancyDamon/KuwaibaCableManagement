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
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;

/**
 * This is a wrapper class for the entity class ViewObjectLight (see Persistence Abstraction Layer API docs for details). It's the object returned
 * when a view is requested
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteViewObjectLight implements Serializable {
    /**
     * View id
     */
    private long id;
    /**
     * View class
     */
    private String className;
    /**
     * View name
     */
    private String name;
    /**
     * View description
     */
    private String description;

    //No-arg constructor required
    public RemoteViewObjectLight(){}

    public RemoteViewObjectLight(ViewObjectLight myView) {
        this.id = myView.getId();
        this.className = myView.getViewClassName();
        this.name = myView.getName();
        this.description = myView.getDescription();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getViewClassName() {
        return className;
    }

    public void setType(String className) {
        this.className = className;
    }
    
    @Override
    public String toString() {
        return name;
    }
}