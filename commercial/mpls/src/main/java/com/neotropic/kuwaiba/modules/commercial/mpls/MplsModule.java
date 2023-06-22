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
package com.neotropic.kuwaiba.modules.commercial.mpls;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractCommercialModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * This class implements the functionality corresponding to the MPLS module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class MplsModule extends AbstractCommercialModule {
    /**
     * Module id.
     */
    public static final String MODULE_ID = "mpls";
    /*
     * Translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the module registry.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;

    //Constants
        /**
     * Class to identify all views made using the MPLS module
     */
    public static String CLASS_VIEW = "MPLSModuleView";
    /**
     * A side in a tributary link
     */
    public static String RELATIONSHIP_MPLSENDPOINTA = "mplsEndpointA";
    /**
     * B side in a tributary link
     */
    public static String RELATIONSHIP_MPLSENDPOINTB = "mplsEndpointB";
    /**
     * Relates a pseudowire and its output interface, the output interface is the endpoint of a MPLS link if is a port
     */
    public static String RELATIONSHIP_MPLS_PW_ISRELATEDWITH_VFI = "mplsPseudowireIsRelatedWithVFI";
    /**
     * Relates two pseudowires that are logical linked inside a MPLS device
     */
    public static String RELATIONSHIP_MPLS_PW_ISRELATEDWITH_PW = "mplsPseudowireIsRelatedWithPseudowire";
    /**
     * Relates the MPLS link directly with the GenericNetworkElements parents of 
     * the end points of the MPLS link, it is used to explore the MPLS links in a 
     * MPLS device or the routing between devices
     */
    public static String RELATIONSHIP_MPLSLINK = "mplsLink";           
    
     @Override
    public String getName() {
        return ts.getTranslatedString("module.mpls.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.mpls.description");
    }
    
    @Override
    public String getVersion() {
        return "2.1";
    }
    
    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>"; //NOI18N
    }

    @Override
    public int getCategory() {
        return CATEGORY_LOGICAL;
    }
    
    @PostConstruct
    public void init() {
        
        // Now register the module itself
        this.moduleRegistry.registerModule(this);
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_PERPETUAL_LICENSE;
    }

    @Override
    public void validate() throws OperationNotPermittedException { }

    @Override
    public String getId() {
        return MODULE_ID;
    }
}