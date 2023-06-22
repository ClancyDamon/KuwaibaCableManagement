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
  * A filter definition holds the metadata of a filter, a name, a description, if is enabled or not and the class of objects to which it will be applied
  * finally it includes the filter itself. 
  * The filter allows to have a custom query(with a Groovy Script), that could be executed by an inventory object
  * in order to obtain more complex results than the API results that are limited to get the direct children or get children of a class recursively
  * e.g. 
  * - a filter for the class Router, that gets all the Optical Ports in a Router
  * - a filter for the class Router, that gets all the Physical and Virtual Ports in a Router nut not the ServiceInstace
  * - a filter for class City, that gets all routers and switches with vendor "cisco" in a City
  * 
  * A filter is created for a class metadata and applies to its instances
  * 
  * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class FilterDefinition implements Comparable<FilterDefinition> {
     /**
     * The id of the filter definition.
     */
    private long id;
    /**
     * The name of the filter definition.
     */
    private String name;
    /**
     * filter definition description.
     */
    private String description;
    /**
     * The filter check conditions for instances of certain classes. Here you specify which one. It supports abstract superclasses
     */
    private String classToBeApplied;
    /**
     * A Groovy script that receiving the object id and class name as parameters, performs the logic that does the filter
     */
    private String script;
    /**
     * Is the filter be enabled for execution?
     */
    private boolean enabled;
    /**
     * A filter definition is only the metadata to some executable code. This code is contained by this filter instance.
     */
    private Filter filter;
    
    public FilterDefinition(long id, String name, boolean enabled) {
        this.id = id;
        this.name = name;
        this.enabled = enabled;
    }
    
    public FilterDefinition(long id, String name, String description, String classToBeApplied, String script, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.classToBeApplied = classToBeApplied;
        this.script = script;
        this.enabled = enabled;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClassToBeApplied() {
        return classToBeApplied;
    }

    public void setClassToBeApplied(String classToBeApplied) {
        this.classToBeApplied = classToBeApplied;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public int compareTo(FilterDefinition o) {
        return name.compareTo(o.getName());
    }
    
    @Override
    public boolean equals(Object obj){
       if(obj == null)
           return false;
       if (!(obj instanceof FilterDefinition))
           return false;
       return this.getId() == ((FilterDefinition)obj).getId();
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.id);
        return hash;
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
