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

package org.neotropic.kuwaiba.core.apis.persistence.application;

import java.util.Objects;


/**
 * A privilege is composed by a string token (unique id of the module or method 
 * the privilege refers to, for example "nav-tree" or "create-object") and a number 
 * that specifies the access level (see ACCESS_LEVEL* for possible values)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Privilege {

    /**
     * The user can access the feature in a read-only mode
     */
    public static final int ACCESS_LEVEL_READ = 1;
    /**
     * The user can access the feature in a read and write mode
     */
    public static final int ACCESS_LEVEL_READ_WRITE = 2;
    /**
     * Feature token property name to be used in the database
     */
    public static final String PROPERTY_FEATURE_TOKEN = "featureToken";
    /**
     * Access level property name to be used in the data base
     */
    public static final String PROPERTY_ACCESS_LEVEL = "accessLevel";
    /**
     * The unique id of the feature (for example, a web service method or a simple string with the name of the module)
     */
    private String featureToken;
    /**
     * Access level. See ACCESS_LEVEL* for possible values.
     */
    private int accessLevel;
    

    public Privilege(String featureToken, int accessLevel) {
        this.featureToken = featureToken;
        this.accessLevel = accessLevel;
    }

    public String getFeatureToken() {
        return featureToken;
    }

    public void setFeatureToken(String featureToken) {
        this.featureToken = featureToken;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Privilege) 
            return featureToken.equals(((Privilege)obj).getFeatureToken()); //Only the feature token is enough
         else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.featureToken);
        hash = 13 * hash + this.accessLevel;
        return hash;
    }
}
