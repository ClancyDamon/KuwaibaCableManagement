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
 * An activity is an step in a process. Conditionals are a particular type of activities from the point of view of this API. This class
 * is a representation of a definition of an activity, which is basically a description of what it does (like presenting a form for the user 
 * to fill it in). The activity definition has at least one artifact definition, which contains (in our example) the actual form. This is a simplified
 * version of a step
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ActivityDefinitionLight {
    /**
     * An activity that represents a step in the process. There's always an action associated to it
     */
    public static final int TYPE_NORMAL = 1;
    /**
     * A start event. When a process instance is created, the "pointer" to the current activity is set there
     */
    public static final int TYPE_START = 2;
    /**
     * An end event. When a process instance doesn't have any more normal actions to be executed, the "pointer" to the current activity is set there. This means that the process instance ended.
     */
    public static final int TYPE_END = 3;
    /**
     * A conditional (a logical branch)
     */
    public static final int TYPE_CONDITIONAL = 4;
    /**
     * Activity with parallel paths
     */
    public static final int TYPE_PARALLEL = 5;
    /**
     * Activity definition id
     */
    private String id;
    /**
     * Activity definition name
     */
    private String name;
    /**
     * Activity definition description
     */
    private String description;
    /**
     * Activity type. See ActivityDefinitionLight.TYPE_* for valid values
     */
    private int type;
    /**
     * Define if an Activity can be mark as idle activity
     */    
    private boolean idling;
    /**
     * Define if is necessary Confirm before commit the Activity
     */
    private boolean confirm;
    /**
     * Define the activity color
     */
    private String color;
    /**
     * Artifact associated to the activity definition
     */
    private ArtifactDefinition arfifact;
    
    public ActivityDefinitionLight(String id, String name, String description, 
            int type, ArtifactDefinition arfifact, boolean idling, boolean confirm, String color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.arfifact = arfifact;
        this.idling = idling;
        this.confirm = confirm;
        this.color = color;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArtifactDefinition getArfifact() {
        return arfifact;
    }

    public void setArfifact(ArtifactDefinition arfifact) {
        this.arfifact = arfifact;
    }
    
    public boolean isIdling() {
        return idling;
    }
        
    public void setIdling(boolean idling) {
        this.idling = idling;                
    }
    
    public boolean confirm() {
        return confirm;
    }
        
    public void setConfirm(boolean confirm) {
        this.confirm = confirm;        
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
            
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.id);
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + this.type;
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ActivityDefinitionLight ? ((ActivityDefinitionLight) obj).getId() == id : false;
    }
}
