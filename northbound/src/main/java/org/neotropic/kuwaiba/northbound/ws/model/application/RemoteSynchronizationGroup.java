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
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;

/**
 * Wrapper of SynchronizationGroup
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class RemoteSynchronizationGroup implements Serializable {
     /**
     * Group id
     */
    private long id;
    /**
     * Group name
     */
    private String name;
    /**
     * Group provider
     */
    private List<RemoteSynchronizationProvider> lastSelectedProviders;
    
    public RemoteSynchronizationGroup() { }
    
    public RemoteSynchronizationGroup(SynchronizationGroup syncGroup) {
        this.id = syncGroup.getId();
        this.name = syncGroup.getName();
        this.lastSelectedProviders = new ArrayList<>();

        for(AbstractSyncProvider provider : syncGroup.getLastSelectedProviders()){
            this.lastSelectedProviders.add(new RemoteSynchronizationProvider(provider.getId(), 
               provider.getDisplayName(), provider.isAutomated()));
        }
    }

    public RemoteSynchronizationGroup(long id, String name, 
            List<RemoteSynchronizationProvider> lastSelectedProviders) {
        this.id = id;
        this.name = name;
        this.lastSelectedProviders = lastSelectedProviders;
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

    public List<RemoteSynchronizationProvider> getLastSelectedProviders() {
        return lastSelectedProviders;
    }

    public void setLastSelectedProviders(List<RemoteSynchronizationProvider> lastSelectedProviders) {
        this.lastSelectedProviders = lastSelectedProviders;
    }
    
    public static List<RemoteSynchronizationGroup> toArray(List<SynchronizationGroup> syncGroups) {
        List<RemoteSynchronizationGroup> res = new ArrayList<>();
        List<RemoteSynchronizationProvider> lastSelectedRemoteProviders = new ArrayList<>();
        for (SynchronizationGroup syncGroup : syncGroups){
            if(syncGroup.getLastSelectedProviders() != null){
                for(AbstractSyncProvider provider : syncGroup.getLastSelectedProviders())
                    lastSelectedRemoteProviders.add(new RemoteSynchronizationProvider(provider.getId(), 
                            provider.getDisplayName(), provider.isAutomated()));
            }
            res.add(new RemoteSynchronizationGroup(syncGroup.getId(),
                    syncGroup.getName(), lastSelectedRemoteProviders));
        }
        return res;
    }
}
