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
package com.neotropic.kuwaiba.modules.commercial.sdh;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractCommercialModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class implements the functionality corresponding to the SDH module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class SdhModule extends AbstractCommercialModule {
    /**
     * Module id.
     */
    public static final String MODULE_ID = "sdh";
    /**
    * translation service
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
     * Root class of all high order tributary links (VC4)
     */
    public static String CLASS_GENERICSDHHIGHORDERTRIBUTARYLINK = "GenericSDHHighOrderTributaryLink";
    /**
     * Root class of all low order tributary links (VC12/VC3)
     */
    public static String CLASS_GENERICSDHLOWORDERTRIBUTARYLINK = "GenericSDHLowOrderTributaryLink";
    /**
     * A side in a transport link
     */
    public static String RELATIONSHIP_SDHTLENDPOINTA = "sdhTLEndpointA";
    /**
     * B side in a transport link
     */
    public static String RELATIONSHIP_SDHTLENDPOINTB = "sdhTLEndpointB";
    /**
     * The relationship used to connect two GenericCommunicationsEquipment to represent that ports within the equipment are connected with Transport Links. This is used to ease the way to find routes between elements
     */
    public static String RELATIONSHIP_SDHTRANSPORTLINK = "sdhTransportLink";
    /**
     * The relationship used to connect two GenericCommunicationsEquipment to represent that ports within the equipment are connected with Container Links. This is used to ease the way to find routes between elements
     */
    public static String RELATIONSHIP_SDHCONTAINERLINK = "sdhContainerLink";
    /**
     * A side in a tributary link
     */
    public static String RELATIONSHIP_SDHTTLENDPOINTA = "sdhTTLEndpointA";
    /**
     * B side in a tributary link
     */
    public static String RELATIONSHIP_SDHTTLENDPOINTB = "sdhTTLEndpointB";
    /**
     * This relationship describes how a Transport Link carries a Container Link
     */
    public static String RELATIONSHIP_SDHTRANSPORTS = "sdhTransports";
    /**
     * This relationship describes how a Container Link carries another Container link of a lower order
     */
    public static String RELATIONSHIP_SDHCONTAINS = "sdhContains";
    /**
     * This relationship describes how a Container Link carries a Tributary Link
     */
    public static String RELATIONSHIP_SDHDELIVERS = "sdhDelivers";
    /**
     * The timeslot used by a container in a transport link or in another container
     */
    public static String PROPERTY_SDHPOSITION = "sdhPosition";
    
    @Override
    public String getName() {
        return ts.getTranslatedString("module.sdh.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.sdh.description");
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

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_OPEN_SOURCE;
    }
    
    @PostConstruct
    public void init() {
        
        // Now register the module itself
        this.moduleRegistry.registerModule(this);
    }

    @Override
    public void validate() throws OperationNotPermittedException { }

    @Override
    public void configureModule(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem) {
        super.configureModule(mem, aem, bem);
        
        //Registers the display names
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHTLENDPOINTA, "SDH Transport Link A Side");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHTLENDPOINTB, "SDH Transport Link B Side");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHTRANSPORTLINK, "SDH Transport Link Connecting To");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHCONTAINERLINK, "SDH Container Link Connecting To");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHTTLENDPOINTA, "SDH Tributary Link A Side");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHTTLENDPOINTB, "SDH Tributary Link B Side");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHTRANSPORTS, "Transported SDH Container Links");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHCONTAINS, "Contained SDH Container Links");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHDELIVERS, "Delivered SDH Tributary Links");
    }
    
    @Override
    public String getId() {
        return MODULE_ID;
    }
}
