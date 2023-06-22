/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.util.visual.icons;

import org.neotropic.util.visual.resources.AbstractResourceFactory;
import com.vaadin.flow.server.AbstractStreamResource;

/**
 * Abstract class for generated icons for an item
 * @param <T> item type for which the icon is generated 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class IconGenerator<T> {
     
    protected AbstractResourceFactory resourceFactory;

    public IconGenerator(AbstractResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }  

    public IconGenerator() {
    }
    
    /**
     * Gets a stream resource for the item
     * @param item the item for which the icon is generated
     * @return the generated stream resource
     */
    public abstract AbstractStreamResource apply(T item);
}
