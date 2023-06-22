/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.neotropic.kuwaiba.modules.commercial.sync;

import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractCommercialModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.NewSyncDataSourceConfigurationVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.bgp.BgpSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.ip.IPAddressesSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.reference.ReferenceSnmpSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.vlan.SnmpCiscoVlansSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.BridgeDomainSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.DumbSyncProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Keep you inventory up-to-date by fetching information from live sources like devices and NMS,
 * finding differences and dealing with them.
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class SynchronizationModule extends AbstractCommercialModule {
    /**
     * Module id.
     */
    public static final String MODULE_ID = "sync";
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * The index of all actions provided by this module that are not of general purpose.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * Reference to the module registry.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;
    /**
     * Reference to the providers registry.
     */
    @Autowired
    private ProviderRegistry providerRegistry;
    /**
     * Reference of the module visual action to configure sync data source.
     */
    @Autowired
    private NewSyncDataSourceConfigurationVisualAction newSyncDataSourceConfigurationVisualAction;
    /**
     * Reference of the dumb provider.
     */
    @Autowired
    private BgpSyncProvider bgpSyncProvider;
    /**
     * Reference of the dumb provider.
     */
    @Autowired
    private IPAddressesSyncProvider ipAddressesSyncProvider;
    /**
     * Reference of the dumb provider.
     */
    @Autowired
    private ReferenceSnmpSyncProvider snmpSyncProvider;
    /**
     * Reference of the dumb provider.
     */
    @Autowired
    private SnmpCiscoVlansSyncProvider snmpCiscoVlansSyncProvider;
    /**
     * Reference of the dumb provider.
     */
    @Autowired
    private DumbSyncProvider dumbSyncProvider;
    /**
     * Reference of the bridge provider.
     */
    @Autowired
    private BridgeDomainSyncProvider bridgeDomainSyncProvider;

    @PostConstruct
    public void init() {
        // Register all actions provided by this module
        this.advancedActionsRegistry.registerAction(this.getId(), newSyncDataSourceConfigurationVisualAction);
        // The providers exposed by this module
        this.providerRegistry.registerProvider(dumbSyncProvider);
        this.providerRegistry.registerProvider(bridgeDomainSyncProvider);
        this.providerRegistry.registerProvider(bgpSyncProvider);
        this.providerRegistry.registerProvider(ipAddressesSyncProvider);
        this.providerRegistry.registerProvider(snmpSyncProvider);
        this.providerRegistry.registerProvider(snmpCiscoVlansSyncProvider);
        // Now register the module itself
        this.moduleRegistry.registerModule(this);
        //this.setEnabled(false);// Still under development.
    }

    @Override
    public int getCategory() {
        return CATEGORY_OTHER;
    }

    @Override
    public void validate() throws OperationNotPermittedException {
    }

    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public String getName() {
        return ts.getTranslatedString("module.sync.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.sync.description");
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
        return ModuleType.TYPE_TEMPORARY_LICENSE;
    }
}