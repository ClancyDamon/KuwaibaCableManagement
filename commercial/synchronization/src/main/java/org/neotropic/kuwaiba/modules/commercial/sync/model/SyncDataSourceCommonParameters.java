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
public class SyncDataSourceCommonParameters implements Serializable {
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
     * Data source type
     */
    @Getter
    @Setter
    private String dataSourcetype;
    /**
     * The parameters stored in this configuration entry
     */
    @Getter
    @Setter
    private HashMap<String, String> parameters;

    public SyncDataSourceCommonParameters(long id, String dataSourcetype, HashMap<String, String> parameters) {
        this.id = id;
        this.dataSourcetype = dataSourcetype;
        this.parameters = parameters;
    }

    public SyncDataSourceCommonParameters(String dataSourcetype) {
        this.dataSourcetype = dataSourcetype;
    }

    public List<ParameterItemDataSource> getParameterToItem() {
        if (parameters != null) {
            parameters.entrySet().stream()
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
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SyncDataSourceCommonParameters other = (SyncDataSourceCommonParameters) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.dataSourcetype, other.dataSourcetype)) {
            return false;
        }
        if (!Objects.equals(this.parameters, other.parameters)) {
            return false;
        }
        return true;
    }

}