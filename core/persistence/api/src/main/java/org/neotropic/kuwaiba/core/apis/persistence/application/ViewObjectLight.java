/**
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

package org.neotropic.kuwaiba.core.apis.persistence.application;

import java.io.Serializable;

/**
 * The light version of a ViewObject
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ViewObjectLight implements Serializable {
    /**
     * View id
     */
    private Long id;
    /**
     * View name
     */
    private String name;
    /**
     * View description
     */
    private String description;
    /**
     * ViewObject class name (Supported types are documented at the business domain level)
     */
    private String viewClassName;

    public ViewObjectLight(long id, String name, String description, String viewClassName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.viewClassName = viewClassName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getViewClassName() {
        return viewClassName;
    }

    public void setViewType(String viewClassName) {
        this.viewClassName = viewClassName;
    }
}
