/*
 * Copyright 2023-2023. Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.modules.commercial.sync;

import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractSyncProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * All providers must be registered here at startup. The registry
 * will later be used to create menus and dialog.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
@Service
public class ProviderRegistry {
    /**
     * A map with the id of the provider as key and the provider itself as value.
     */
    private final HashMap<String, AbstractSyncProvider> registeredProviders;

    public ProviderRegistry() {
        this.registeredProviders = new LinkedHashMap<>();
    }

    public void registerProvider(AbstractSyncProvider provider) {
        this.registeredProviders.put(provider.getId(), provider);
    }

    public HashMap<String, AbstractSyncProvider> getProviders() {
        return registeredProviders;
    }

}
