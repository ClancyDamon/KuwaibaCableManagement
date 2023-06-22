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
package org.neotropic.kuwaiba.core.apis.persistence.application.processman;

import java.util.Objects;

/**
 * An actor is someone (a person, organizational unit or external agent, like a customer) who is responsible to cary on with an activity.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Actor {
    /**
     * The actor is a local user
     */
    public static int TYPE_USER = 1;
    /**
     * The actor is a local group
     */
    public static int TYPE_GROUP = 2;
    /**
     * The actor is an external agent (a customer, provider, etc)
     */
    public static int TYPE_EXTERNAL = 3;
    /**
     * The id of the actor. If it is an external agent, this value will be -1
     */
    private String id;
    /**
     * Alias used to identify the actor. Most of the times, it will be the name of the user/group
     */
    private String name;
    /**
     * Actor type. See TYPE_* for valid values
     */
    private int type;

    public Actor(String id, String name, int type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass().isInstance(Actor.class) ? this.id.equals(((Actor)obj).getId()) : false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.id);
        return hash;
    }
  
}
