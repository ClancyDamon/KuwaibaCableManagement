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
package org.neotropic.kuwaiba.core.persistence.reference.extras.processman;

import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfileLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Actor;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActorAuthorizationManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {@link ActorAuthorizationManager Actor Authorization Manager} reference implementation
 * to Kuwaiba.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Service
public class ActorAuthorizationManagerImpl implements ActorAuthorizationManager {
    @Autowired
    private ApplicationEntityManager aem;
    
    @Override
    public boolean existGroup(UserProfile user, Actor actor) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(actor);
        try {
            List<GroupProfileLight> groups = aem.getGroupsForUser(user.getId());
            for (GroupProfileLight group : groups) {
                if (actor.getName() != null && actor.getName().equals(group.getName()))
                    return true;
            }
            return false;
        } catch (ApplicationObjectNotFoundException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
}
