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
package org.neotropic.kuwaiba.modules.core.navigation.filters;

import com.vaadin.flow.component.Component;

/**
 * Functional interface intended to be used to create filters that will retrieve
 * data to be placed in the page after a search result or a static filter.
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 * @param <T>
 */
public abstract class Filter<T> {
    
    /**
     * The filter's name
     */
    protected String name;
    
    /**
     * Returns the class whose instances the view applies to.
     * @return Abstract super classes (such as ViewableObject) 
     * are also supported.
     */
    public abstract String[] appliesTo();
   
    /**
     * used to retrieves the data that will used to create the content in the 
     * page when the filter is used
     * @return a Vaadin component
     */
    public abstract Component callBack();
}
