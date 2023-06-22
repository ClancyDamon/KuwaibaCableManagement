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

package org.neotropic.kuwaiba.core.apis.persistence.business;

import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.util.StringUtils;

/**
 * Contains a business object basic information
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class BusinessObjectLight implements Serializable, Comparable<BusinessObjectLight> {
    /**
     * Object's id.
     */
    private String id;
    /**
     * Object's name.
     */
    private String name;
    /**
     * Class this object is instance of.
     */
    private String className;
    /**
     * The display name of the class associated to the instance. This was added in the Persistence API 2.1 
     * to avoid dealing with the class metadata cache every time the display name of the class was needed. 
     * This field will likely be null unless the display name is different from the real name.
     */
    private String classDisplayName;
    /**
     * The result of evaluating all the validators associated to the instance
     */
    private List<Validator> validators;

    public BusinessObjectLight(String className, String id, String name) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.validators = new ArrayList<>();
    }

    /**
     * Constructor 
     * @param className The real name of the class the object is instance of.
     * @param id 
     * @param name
     * @param classDisplayName 
     */
    public BusinessObjectLight(String className, String id, String name, String classDisplayName) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.classDisplayName = classDisplayName;
        this.validators = new ArrayList<>();
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassDisplayName() {
        return classDisplayName;
    }

    public void setClassDisplayName(String classDisplayName) {
        this.classDisplayName = classDisplayName;
    }

    public List<Validator> getValidators() {
        return validators;
    }

    public void setValidators(List<Validator> validators) {
        this.validators = validators;
    }

    /**
     * Returns the name 
     * @return 
     */
    @Override
    public String toString() {
        return this.name + " [" + (StringUtils.isEmpty(classDisplayName) ? className : classDisplayName) + "]";
    }

    @Override
    public int compareTo(BusinessObjectLight o) {
        return this.name.compareTo(o.getName());
    }
    
    @Override
    public boolean equals(Object obj){
       if(obj == null)
           return false;
       if (!(obj instanceof BusinessObjectLight))
           return false;
       return (this.getId() == null ? ((BusinessObjectLight)obj).getId() == null : this.getId().equals(((BusinessObjectLight)obj).getId()));
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.id);
        return hash;
    }
}