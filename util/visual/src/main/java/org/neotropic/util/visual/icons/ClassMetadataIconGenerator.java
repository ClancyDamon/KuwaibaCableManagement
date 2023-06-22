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


import com.vaadin.flow.server.AbstractStreamResource;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.util.visual.resources.AbstractResourceFactory;

/**
 * Implementation that provides the ability to generate small icons for class metadata
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */

public class ClassMetadataIconGenerator extends IconGenerator<ClassMetadataLight> {
        
    public ClassMetadataIconGenerator(AbstractResourceFactory resourceFactory) {
        super(resourceFactory);
    }  

    @Override
    public AbstractStreamResource apply(ClassMetadataLight cls) {
        return resourceFactory.getClassSmallIcon(cls.getName()); 
    }
    
}
