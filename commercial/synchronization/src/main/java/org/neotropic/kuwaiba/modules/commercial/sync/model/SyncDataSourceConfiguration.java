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
import lombok.Setter;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
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
public class SyncDataSourceConfiguration implements Serializable {
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
     * Common parameters used  in this data source by example ip and type
     */
    @Getter
    @Setter
    private SyncDataSourceCommonParameters commonParameters;
    /**
     * Description about data source
     */
    @Getter
    @Setter
    private String description;
    /**
     * Parent business object
     */
    @Getter
    @Setter
    private BusinessObjectLight businessObjectLight;
    /**
     * Template data source used
     */
    @Getter
    @Setter
    private TemplateDataSource templateDataSource;

    public SyncDataSourceConfiguration() {
        this.commonParameters = new SyncDataSourceCommonParameters();
    }

    public SyncDataSourceConfiguration(long id, String name, HashMap<String, String> parameters) {
        this.id = id;
        this.name = name;
        this.parameters = parameters;
    }

    public SyncDataSourceConfiguration(long id, String name, String description, HashMap<String, String> parameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    public SyncDataSourceConfiguration(long id, String name, HashMap<String, String> parameters
            , SyncDataSourceCommonParameters commonParameters) {
        this.id = id;
        this.name = name;
        this.parameters = parameters;
        this.commonParameters = commonParameters;
    }

    public SyncDataSourceConfiguration(long id, String name, String description, HashMap<String, String> parameters
            , SyncDataSourceCommonParameters commonParameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.commonParameters = commonParameters;
    }

    public SyncDataSourceConfiguration(long id, String name, String description, HashMap<String, String> parameters
            , SyncDataSourceCommonParameters commonParameters, BusinessObjectLight businessObjectLight) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.commonParameters = commonParameters;
        this.commonParameters = commonParameters;
        this.businessObjectLight = businessObjectLight;
    }

    public List<ParameterItemDataSource> getParameterToItem() {
        if (parameters != null) {
            parameters.entrySet().stream()
                    .filter(entry -> {
                        return entry.getKey() != null && !((String) entry.getKey()).trim().isEmpty()
                                && entry.getValue() != null && !entry.getValue().trim().isEmpty();
                    })
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
        hash = 53 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.commonParameters);
        hash = 53 * hash + Objects.hashCode(this.description);
        hash = 53 * hash + Objects.hashCode(this.businessObjectLight);
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
        final SyncDataSourceConfiguration other = (SyncDataSourceConfiguration) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.commonParameters, other.commonParameters)) {
            return false;
        }
        if (!Objects.equals(this.businessObjectLight, other.businessObjectLight)) {
            return false;
        }
        return true;
    }


}