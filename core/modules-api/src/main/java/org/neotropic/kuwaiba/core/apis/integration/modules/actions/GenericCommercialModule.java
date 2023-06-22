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
package org.neotropic.kuwaiba.core.apis.integration.modules.actions;

import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;

/**
 * All third-party commercial modules should implement this interface
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public interface GenericCommercialModule {
    /**
     * Gets the module's name. Must be unique, otherwise, the system will only take last one loaded at application's startup
     * @return The module's name
     */
    public String getName();
    /**
     * Gets the module description
     * @return he module's description
     */
    public String getDescription();
    /**
     * Gets the module's version
     * @return The module's version
     */
    public abstract String getVersion();
    /**
     * Gets the module's vendor
     * @return The module's vendor
     */
    public abstract String getVendor();
    /**
     * Gets the module's category
     * @return The module's category
     */
    public abstract String getCategory();
    /**
     * Gets the module's type. For valid values #ModuleTypes
     * @return The module's types
     */
    public abstract ModuleType getModuleType();
    
    /**
     * Says if the module can be used or not (for example, if the license has expired or not)
     * @return 
     */
    public abstract boolean isValid();
    
    /**
     * This method initializes the module. Must be called before anything else
     * @param aem The ApplicationEntityManager instance. Might be null if not needed by the module
     * @param mem The MetadataEntityManager instance. Might be null if not needed by the module
     * @param bem The BusinessEntityManager instance. Might be null if not needed by the module
     */
    public abstract void configureModule (ApplicationEntityManager aem, MetadataEntityManager mem, BusinessEntityManager bem);
    
    public enum ModuleType {
        TYPE_FREEWARE,
        TYPE_TRIAL,
        TYPE_PERPETUAL_LICENSE,
        TYPE_TEMPORARY_LICENSE,
        TYPE_OTHER
    }
}
