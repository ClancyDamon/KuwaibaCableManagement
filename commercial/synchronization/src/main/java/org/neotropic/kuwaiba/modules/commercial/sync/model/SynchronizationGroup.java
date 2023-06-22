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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Creates a Synchronization Group. A Sync Group is a set of Synchronization
 * Configurations that will be processed by the same Synchronization Provider
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SynchronizationGroup implements Serializable {
    /**
     * Group id
     */
    @Getter
    @Setter
    private long id;
    /**
     * Group name
     */
    @Getter
    @Setter
    private String name;
    /**
     * Running provider
     */
    @Getter
    @Setter
    private AbstractSyncProvider currentProvider;
    /**
     * Description about data group
     */
    @Getter
    @Setter
    private String description;
    /**
     * Group provider
     */
    @Getter
    @Setter
    private List<AbstractSyncProvider> lastSelectedProviders;
    /**
     * The configurations to be processed
     */
    @Getter
    @Setter
    private List<SyncDataSourceConfiguration> syncDataSourceConfigurations;

    public SynchronizationGroup() {
    }

    public SynchronizationGroup(long id, String name,
                                List<SyncDataSourceConfiguration> syncDataSourceConfigurations) {
        this.id = id;
        this.name = name;
        this.syncDataSourceConfigurations = syncDataSourceConfigurations;
    }

    public SynchronizationGroup(long id, String name, String description,
                                List<SyncDataSourceConfiguration> syncDataSourceConfigurations) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.syncDataSourceConfigurations = syncDataSourceConfigurations;
    }

    public void addSyncDataSource(SyncDataSourceConfiguration dataSource) {
        if (syncDataSourceConfigurations == null)
            syncDataSourceConfigurations = new ArrayList<>();

        if (!syncDataSourceConfigurations.contains(dataSource))
            syncDataSourceConfigurations.add(dataSource);
    }

    public void removeSyncDataSource(SyncDataSourceConfiguration dataSource) {
        if (syncDataSourceConfigurations != null)
            syncDataSourceConfigurations.remove(dataSource);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.currentProvider);
        hash = 79 * hash + Objects.hashCode(this.description);
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
        final SynchronizationGroup other = (SynchronizationGroup) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.currentProvider, other.currentProvider)) {
            return false;
        }
        return true;
    }


}