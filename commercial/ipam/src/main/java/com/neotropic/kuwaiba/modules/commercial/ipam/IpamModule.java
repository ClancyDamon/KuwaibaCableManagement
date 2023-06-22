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
package com.neotropic.kuwaiba.modules.commercial.ipam;


import com.neotropic.kuwaiba.modules.commercial.ipam.actions.RelateIpToNetworkInterfaceVisualAction;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractCommercialModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * IP address manager module definition. This module allows managing addressing space and subnetting for 
 * IPv4 and IPv6 networks.
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class IpamModule extends AbstractCommercialModule {
    /**
     * The index of all actions provided by this module that are not of general purpose.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * Reference to relate to network interface visual action.
     */
    @Autowired
    private RelateIpToNetworkInterfaceVisualAction relateIpToNetworkInterfaceVisualAction;
    /**
     * The module id.
     */
    public static final String MODULE_ID = "ipam"; 
    /**
     * This relationship is used to connect a GenericPort with an IP address 
     */
    public static final String RELATIONSHIP_IPAMHASADDRESS = "ipamHasIpAddress";
    /**
     * This relationship is used to connect a VLAN with a Subnet
     */
    public static final String RELATIONSHIP_IPAMBELONGSTOVLAN = "ipamBelongsToVlan";
    /**
     * This relationship is used to relate a VRF with a Subnet
     */
    public static final String RELATIONSHIP_IPAMBELONGSTOVRFINSTACE = "ipamBelongsToVrfInstance";
    /**
     * TODO: place this relationships in other place
     * This relationship is used to relate a network element with extra logical configuration
     */
    public static final String RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE = "ipamPortrelatedtointerface";
    /*
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the module registry.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;
    
    @PostConstruct
    public void init() {
        // Register all actions
        
        // Now the module itself
        this.moduleRegistry.registerModule(this);
        this.advancedActionsRegistry.registerAction(MODULE_ID, relateIpToNetworkInterfaceVisualAction);
    }
    @Override
    public String getName() {
        return ts.getTranslatedString("module.ipam.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.ipam.description");
    }

    @Override
    public String getVersion() {
        return "2.1";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_PERPETUAL_LICENSE;
    }

    @Override
    public void configureModule(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem) {
        this.aem = aem;
        this.mem = mem;
        this.bem = bem;
        //Registers the display names
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_IPAMBELONGSTOVLAN, ts.getTranslatedString("module.ipam.rels.belogns-to-vlan"));
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_IPAMHASADDRESS, ts.getTranslatedString("module.ipam.rels.element-has-ip-addr"));
    }

    @Override
    public void validate() throws OperationNotPermittedException { }

    @Override
    public String getId() {
        return MODULE_ID;
    }
    
    @Override
    public int getCategory() {
        return CATEGORY_LOGICAL;
    }
}
