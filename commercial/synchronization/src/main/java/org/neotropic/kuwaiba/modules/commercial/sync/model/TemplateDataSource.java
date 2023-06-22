/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.neotropic.kuwaiba.modules.commercial.sync.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neotropic.kuwaiba.modules.commercial.sync.components.ParameterItemDataSource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Basically a hash-map that stores a set of configuration parameters
 * that will be used by the sync provider
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@NoArgsConstructor
public class TemplateDataSource implements Serializable {
    /**
     * Temporal parameters stored in this configuration entry
     */
    @Getter
    private final List<ParameterItemDataSource> listOfParameters = new ArrayList<>();
    /**
     * Configuration id
     */
    @Getter
    @Setter
    private long id;
    /**
     * Configuration name
     */
    @Getter
    @Setter
    private String name;
    /**
     * The parameters stored in this configuration entry
     */
    @Getter
    @Setter
    private HashMap<String, String> parameters;
    /**
     * Description about data source
     */
    @Getter
    @Setter
    private String description;

    public TemplateDataSource(long id, String name, String description, HashMap<String, String> parameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    public List<ParameterItemDataSource> getParameterToItem() {
        if (parameters != null) {
            parameters.entrySet().stream()
                    .filter(entry ->
                        entry.getKey() != null && !(entry.getKey()).trim().isEmpty()
                    )
                    .filter(entry -> listOfParameters.stream().noneMatch(item -> item.getPropertyName().equalsIgnoreCase(entry.getKey())))
                    .map(entry -> new ParameterItemDataSource(entry.getKey(), entry.getValue()))
                    .forEachOrdered(this::addParameterItem);
        }
        return listOfParameters;
    }

    public void addParameterItem(ParameterItemDataSource item) {
        if (!listOfParameters.contains(item))
            listOfParameters.add(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemplateDataSource that = (TemplateDataSource) o;

        if (id != that.id) return false;
        if (listOfParameters != null ? !listOfParameters.equals(that.listOfParameters) : that.listOfParameters != null)
            return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(parameters, that.parameters)) return false;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        int result = listOfParameters != null ? listOfParameters.hashCode() : 0;
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

}