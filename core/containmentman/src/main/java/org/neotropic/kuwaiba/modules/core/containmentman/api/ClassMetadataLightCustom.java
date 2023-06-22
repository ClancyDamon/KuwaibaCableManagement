/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.modules.core.containmentman.api;

import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;

/**
 * Decoration for ClassMetadataLight, allow avoid restriction of render same element in a grid or tree
 * 
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class ClassMetadataLightCustom {
    private boolean root;
    private ClassMetadataLight classMetadataLight; 

    public ClassMetadataLightCustom(ClassMetadataLight classMetadataLight) {
        this.classMetadataLight = classMetadataLight;
        this.root = true;
    }
    
    public ClassMetadataLightCustom(ClassMetadataLight classMetadataLight, boolean root) {
        this.classMetadataLight = classMetadataLight;
        this.root = root;
    }
    
    
    public String toString() {
        return (this.classMetadataLight.getDisplayName() == null || this.classMetadataLight.getDisplayName().isEmpty())
                ? this.classMetadataLight.getName() : this.classMetadataLight.getDisplayName().trim();
    }

    /**
     * ClassMetada's Name
     */
    public String getName() {
        return classMetadataLight.getName();
    }
    
    /**
     * ClassMetada 
     */
    public ClassMetadataLight getClassMetadataLight() {
        return classMetadataLight;
    }
    
    /**
     * @return the root
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(boolean root) {
        this.root = root;
    }
    
}
