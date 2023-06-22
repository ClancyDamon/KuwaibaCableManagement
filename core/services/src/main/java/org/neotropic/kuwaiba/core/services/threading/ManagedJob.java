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

package org.neotropic.kuwaiba.core.services.threading;

import java.util.function.Supplier;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfileLight;

/**
 * Defines methods to update the progress and state of a Kuwaiba job, 
 * so it can be managed by the Threading Service.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <T> The type of the result of the job.
 */
public abstract class ManagedJob<T> implements Supplier {
    protected ManagedJobDescriptor descriptor;
    
    /**
     * Default constructor.
     * @param user The user the job will be run by.
     */
    public ManagedJob(UserProfileLight user) {
        this.descriptor = new ManagedJobDescriptor(user);
    }
    
    public ManagedJobDescriptor getDescriptor() {
        return this.descriptor;
    }
    
    @Override
    public abstract T get();
}
