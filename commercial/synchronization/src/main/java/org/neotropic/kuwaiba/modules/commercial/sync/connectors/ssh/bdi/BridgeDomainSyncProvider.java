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

package org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.entities.BridgeDomain;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.entities.NetworkInterface;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.parsers.BridgeDomainsASR1002Parser;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.parsers.BridgeDomainsASR1006Parser;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.parsers.BridgeDomainsASR9001Parser;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.parsers.BridgeDomainsASR920Parser;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.parsers.BridgeDomainsME3600Parser;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractDataEntity;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.model.ESyncParameters;
import org.neotropic.kuwaiba.modules.commercial.sync.model.PollResult;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncAction;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncFinding;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncResult;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncUtil;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * This provider connects to Cisco routers via SSH, retrieves the bridge domain configuration, and creates/updates the relationships between
 * the bridge domains and the logical/physical
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class BridgeDomainSyncProvider extends AbstractSyncProvider {
    /**
     * Reference to the Translation Service.s
     */
    @Autowired
    private TranslationService ts;

    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * The Application Entity Manager instance.
     */
    @Autowired
    private ApplicationEntityManager aem;

    @Override
    public String getDisplayName() {
        //Bridge Domains and Bridge Domain Interfaces Sync Provider
        return "Bridge Domains";
    }

    @Override
    public String getId() {
        return BridgeDomainSyncProvider.class.getName();
    }

    @Override
    public boolean isAutomated() {
        return true;
    }

    @Override
    public List<AbstractDataEntity> unmappedPoll(SynchronizationGroup syncGroup) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling");
    }

    @Override
    public PollResult mappedPoll(SynchronizationGroup syncGroup) {
        List<SyncDataSourceConfiguration> syncDataSourceConfigurations = syncGroup.getSyncDataSourceConfigurations();

        PollResult res = new PollResult();
        /**
         * uncomment, fix and replace
         * BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
         */
        //BusinessEntityManager bem = null;

        JSch sshShell = new JSch();
        Session session = null;
        ChannelExec channel = null;

        for (SyncDataSourceConfiguration dataSourceConfiguration : syncDataSourceConfigurations) {
            try {
                String deviceId;
                int port;
                String className, host, user, password;

                if (dataSourceConfiguration.getParameters().containsKey("deviceId")) //NOI18N
                    deviceId = dataSourceConfiguration.getParameters().get("deviceId"); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_not_defined"),//NOI18N ts
                                    "deviceId", syncGroup.getName())));
                    continue;
                }

                if (dataSourceConfiguration.getParameters().containsKey("deviceClass")) //NOI18N
                    className = dataSourceConfiguration.getParameters().get("deviceClass"); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_not_defined"),//NOI18N ts
                                    "deviceClass", syncGroup.getName()))); //NOI18N
                    continue;
                }

                if (dataSourceConfiguration.getParameters().containsKey("ipAddress")) //NOI18N
                    host = dataSourceConfiguration.getParameters().get("ipAddress"); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_not_defined"),//NOI18N ts
                                    "ipAddress", syncGroup.getName()))); //NOI18N
                    continue;
                }

                if (dataSourceConfiguration.getParameters().containsKey("sshPort")) //NOI18N
                    port = Integer.valueOf(dataSourceConfiguration.getParameters().get("sshPort")); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_not_defined"),//NOI18N ts
                                    "sshPort", syncGroup.getName())));
                    continue;
                }

                if (dataSourceConfiguration.getParameters().containsKey("sshUser")) //NOI18N
                    user = dataSourceConfiguration.getParameters().get("sshUser"); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_not_defined"), "sshUser", //NOI18N ts
                                    syncGroup.getName()))); //NOI18N
                    continue;
                }

                if (dataSourceConfiguration.getParameters().containsKey("sshPassword")) //NOI18N
                    password = dataSourceConfiguration.getParameters().get("sshPassword"); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_not_defined"), //NOI18N ts
                                    "sshPassword", syncGroup.getName()))); //NOI18N
                    continue;
                }

                BusinessObjectLight currentObject = bem.getObjectLight(className, deviceId);

                session = sshShell.getSession(user, host, port);

                session.setPassword(password);
                //Enable to -not recommended
                //- disable host key checking
                session.setConfig("StrictHostKeyChecking", "no");
//                String knownHostsFileLocation = (String) PersistenceService.getInstance().getApplicationEntityManager().
//                getConfigurationVariableValue("sync.bdi.knownHostsFile");
//                sshShell.setKnownHosts(knownHostsFileLocation);
                session.connect(10000); //Connection timeout
                channel = (ChannelExec) session.openChannel("exec");


                String modelString = currentObject.getName().split("-")[0];

                switch (modelString) { //The model of the device is identified from its name. Alternatively, this could be taken from its actual "model" attribute, but this implementation takes the quickest approach.
                    case "ASR920": {
                        channel.setCommand("sh bridge-domain"); //NOI18N
                        channel.connect();

                        BridgeDomainsASR920Parser parser = new BridgeDomainsASR920Parser();
                        res.getResult().put(dataSourceConfiguration,
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }
                    case "ASR1002": {
                        channel.setCommand("sh bridge-domain"); //NOI18N
                        channel.connect();

                        BridgeDomainsASR1002Parser parser = new BridgeDomainsASR1002Parser();

                        res.getResult().put(dataSourceConfiguration,
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }
                    case "ASR1006": {
                        channel.connect();
                        channel.setCommand("sh bridge-domain"); //NOI18N

                        BridgeDomainsASR1006Parser parser = new BridgeDomainsASR1006Parser();

                        res.getResult().put(dataSourceConfiguration,
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }
                    case "ASR9001": {
                        channel.setCommand("sh l2vpnxconnect"); //NOI18N
                        channel.connect();

                        BridgeDomainsASR9001Parser parser = new BridgeDomainsASR9001Parser();

                        res.getResult().put(dataSourceConfiguration,
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }
                    case "ME3600": {
                        channel.setCommand("sh bridge-domain"); //NOI18N
                        channel.connect();
                        BridgeDomainsME3600Parser parser = new BridgeDomainsME3600Parser();

                        res.getResult().put(dataSourceConfiguration,
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }

                    default:
                        res.getExceptions().put(dataSourceConfiguration, Arrays.asList(new InvalidArgumentException(String.format("Model %s is not supported. Check your naming conventions [ASR920-XXX, ASR1002-XXX, ASR9001-XXX, ME3600-XXX]", modelString))));

                }
            } catch (Exception ex) {
                res.getExceptions().put(dataSourceConfiguration, Arrays.asList(ex));
            } finally {
                if (session != null)
                    session.disconnect();
                if (channel != null)
                    channel.disconnect();
            }
        }
        return res;
    }

    @Override
    public List<SyncResult> automatedSync(PollResult pollResult) {
        List<SyncResult> res = new ArrayList<>();
        //First, we inject the unexpected errors
        for (SyncDataSourceConfiguration dsConfig : pollResult.getExceptions().keySet()) {
            for (Exception ex : pollResult.getExceptions().get(dsConfig))
                res.add(new SyncResult(dsConfig.getId(),
                        SyncResult.TYPE_ERROR, String.format("Severe error while processing data source configuration %s",
                        dsConfig.getName()), ex.getLocalizedMessage()));
        }

        for (SyncDataSourceConfiguration dataSourceConfiguration : pollResult.getResult().keySet()) {
            try {
                BusinessObjectLight relatedOject = new BusinessObjectLight(
                        dataSourceConfiguration.getBusinessObjectLight().getClassName(),
                        dataSourceConfiguration.getBusinessObjectLight().getId(),
                        "");
                //The bridge domains in Kuwaiba in the synchronized element
                List<BusinessObjectLight> existingBridgeDomains = bem.getSpecialChildrenOfClassLight(relatedOject.getId(),
                        relatedOject.getClassName(), "BridgeDomain", -1);
                //The bridge domains found in the real device
                List<AbstractDataEntity> bridgeDomainsInDevice = pollResult.getResult().get(dataSourceConfiguration);
                //The VFIs found the device
                List<BusinessObjectLight> currentVFIs = bem.getChildrenOfClassLight(relatedOject.getId(), relatedOject.getClassName(), "VFI", 0);

                for (AbstractDataEntity bridgeDomainInDevice : bridgeDomainsInDevice) { //First we check if the bridge domains exists within the device. If they do not, they will be created, if they do, we will check the interfaces
                    BusinessObjectLight matchingBridgeDomain = null;
                    List<BusinessObjectLight> bridgeDomainInterfaces = null; //These objects are retrieved lazily
                    List<BusinessObjectLight> physicalInterfaces = null; //These objects are retrieved lazily

                    for (BusinessObjectLight existingBridgeDomain : existingBridgeDomains) {
                        if (existingBridgeDomain.getName().equals(((BridgeDomain) bridgeDomainInDevice).getName())) {
                            res.add(new SyncResult(dataSourceConfiguration.getId()
                                    , SyncResult.TYPE_INFORMATION
                                    , String.format("Check if Bridge Domain %s exists within %s"
                                    , existingBridgeDomain, relatedOject)
                                    ,"The Bridge Domain already exists and was not modified"));
                            matchingBridgeDomain = existingBridgeDomain;
                            break;
                        }
                    }

                    if (matchingBridgeDomain == null) {
                        HashMap<String, String> defaultAttributes = new HashMap<>();
                        defaultAttributes.put(Constants.PROPERTY_NAME, bridgeDomainInDevice.getName());
                        String newBridgeDomain = bem.createSpecialObject("BridgeDomain", relatedOject.getClassName(),
                                relatedOject.getId(), defaultAttributes, null);
                        aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [BridgeDomain] (%s)", bridgeDomainInDevice.getName(), newBridgeDomain));
                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Check if Bridge Domain %s exists within %s", bridgeDomainInDevice.getName(), relatedOject),
                                "The Bridge Domain did not exist and was created successfully"));

                        matchingBridgeDomain = new BusinessObjectLight("BridgeDomain", newBridgeDomain, bridgeDomainInDevice.getName());
                        existingBridgeDomains.add(matchingBridgeDomain);
                        bridgeDomainInterfaces = new ArrayList<>();
                    }

                    //Now we check if the network interfaces exist and relate them if necessary
                    for (NetworkInterface networkInterface : ((BridgeDomain) bridgeDomainInDevice).getNetworkInterfaces()) {
                        if (networkInterface.getNetworkInterfaceType() == NetworkInterface.TYPE_VFI) { //The VFI are not created automatically in this provider
                            BusinessObjectLight matchingVFI = null;
                            if (networkInterface.getName() != null) {
                                for (BusinessObjectLight vfi : currentVFIs) {
                                    if (vfi.getName().equals(networkInterface.getName())) {
                                        matchingVFI = vfi;
                                        break;
                                    }
                                }
                                //The VFI doesn't exist so it must be created
                                if (matchingVFI == null) {
                                    HashMap<String, String> defaultAttributes = new HashMap<>();
                                    defaultAttributes.put(Constants.PROPERTY_NAME, networkInterface.getName());
                                    String newVfiId = bem.createSpecialObject("VFI", relatedOject.getClassName(), relatedOject.getId(), defaultAttributes, null);
                                    matchingVFI = new BusinessObjectLight("VFI", newVfiId, networkInterface.getName());
                                    currentVFIs.add(matchingVFI);
                                    aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [VFI] (%s)", networkInterface.getName(), matchingVFI.getId()));

                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS,
                                            String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                            String.format("The VFI %s did not exist and was created.", networkInterface.getName())));
                                }
                                //maybe it was creadted it MPLS sync, now with must create the relationship if doesn't exists.
                                List<BusinessObjectLight> relatedBDs = bem.getSpecialAttribute(matchingVFI.getClassName(), matchingVFI.getId(), "networkHasBridgeInterface");

                                if (!relatedBDs.contains(matchingBridgeDomain)) {
                                    bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), "VFI", matchingVFI.getId(), "networkHasBridgeInterface", false);

                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                            String.format("The VFI %s was related it with the Bridge Domain.", networkInterface.getName())));
                                    aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT,
                                            String.format("%s [BridgeDomainInterface] (%s)", networkInterface.getName(), matchingVFI.getId()));
                                }
                            }
                            continue;
                        }

                        if (networkInterface.getNetworkInterfaceType() == NetworkInterface.TYPE_BDI) {
                            if (bridgeDomainInterfaces == null) {
                                // We assume that there are not multiple BDIs with the same name
                                bridgeDomainInterfaces = bem.getObjectsOfClassLight("BridgeDomainInterface", new HashMap<String, String>(), 15, -1);

                            }

                            BusinessObjectLight matchingBridgeDomainInterface = null;
                            for (BusinessObjectLight bridgeDomainInterface : bridgeDomainInterfaces) {
                                if (bridgeDomainInterface.getName().equals(networkInterface.getName())) {
                                    matchingBridgeDomainInterface = bridgeDomainInterface;
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                            String.format("BDI %s already exists. No changes were made", networkInterface.getName())));
                                    break;
                                }
                            }

                            if (matchingBridgeDomainInterface == null) {
                                HashMap<String, String> defaultAttributes = new HashMap<>();
                                defaultAttributes.put(Constants.PROPERTY_NAME, networkInterface.getName());
                                String newBridgeDomainInterfaceId = bem.createHeadlessObject("BridgeDomainInterface", defaultAttributes, null);
                                bem.createSpecialRelationship(relatedOject.getClassName(), relatedOject.getId(), "BridgeDomain", matchingBridgeDomain.getId(), "networkHasBridgeInterface", false);

                                bridgeDomainInterfaces.add(new BusinessObjectLight("BridgeDomainInterface", newBridgeDomainInterfaceId, networkInterface.getName()));

                                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                        String.format("The BDI %s did not exist and was created.", networkInterface.getName())));
                                aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT,
                                        String.format("%s [BridgeDomainInterface] (%s)", networkInterface.getName(), newBridgeDomainInterfaceId));
                            }
                            continue;
                        }

                        if (networkInterface.getNetworkInterfaceType() == NetworkInterface.TYPE_SERVICE_INSTANCE) {
                            String[] interfaceNameTokens = networkInterface.getName().replace(" (split-horizon)", "").split(" "); //The interface name would look like this: GigabitEthernet0/0/2 service instance 10
                            //Some entries have an extra " (split-horizon)" at the end that can be discarded
                            if (physicalInterfaces == null)
                                physicalInterfaces = bem.getChildrenOfClassLight(relatedOject.getId(), relatedOject.getClassName(), "GenericCommunicationsPort", -1);


                            BusinessObjectLight matchingPhysicalInterface = null;
                            String standardName = SyncUtil.normalizePortName(interfaceNameTokens[0]);
                            for (BusinessObjectLight physicalInterface : physicalInterfaces) {
                                if (physicalInterface.getName().equals(standardName)) { //Checks for the extended and the condensed interface name formats (GigabitEthernetXXX vs GiXXXX)
                                    matchingPhysicalInterface = physicalInterface;
                                    break;
                                }
                            }

                            if (matchingPhysicalInterface == null)
                                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_ERROR, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                        String.format("The physical interface %s was not found. The service instance %s will not be created nor related to the bridge domain", standardName, networkInterface.getName())));
                            else {
                                List<BusinessObjectLight> serviceInstances = bem.getChildrenOfClassLight(matchingPhysicalInterface.getId(),
                                        matchingPhysicalInterface.getClassName(), Constants.CLASS_SERVICE_INSTANCE, -1);

                                BusinessObjectLight matchingServiceInstance = null;
                                for (BusinessObjectLight serviceInstace : serviceInstances) {
                                    if (serviceInstace.getName().equals(interfaceNameTokens[interfaceNameTokens.length - 1])) {
                                        matchingServiceInstance = serviceInstace;
                                        break;
                                    }
                                }

                                if (matchingServiceInstance == null) {
                                    HashMap<String, String> defaultAttributes = new HashMap<>();
                                    defaultAttributes.put(Constants.PROPERTY_NAME, interfaceNameTokens[interfaceNameTokens.length - 1]);
                                    String newServiceInstance = bem.createObject(Constants.CLASS_SERVICE_INSTANCE, matchingPhysicalInterface.getClassName(), matchingPhysicalInterface.getId(),
                                            defaultAttributes, null);

                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                            String.format("Service Instance %s did not exist and was created.", networkInterface.getName())));

                                    matchingServiceInstance = new BusinessObjectLight(Constants.CLASS_SERVICE_INSTANCE, newServiceInstance, interfaceNameTokens[interfaceNameTokens.length - 1]);
                                } else
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                            String.format("Service Instance %s already exists. No changes were made.", matchingServiceInstance.getName())));

                                List<BusinessObjectLight> relatedBridgeDomain = bem.getSpecialAttribute(matchingServiceInstance.getClassName(), matchingServiceInstance.getId(), "networkHasBridgeInterface");
                                if (relatedBridgeDomain.isEmpty()) {
                                    bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), Constants.CLASS_SERVICE_INSTANCE, matchingServiceInstance.getId(), "networkHasBridgeInterface", true);
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                            String.format("Service instace %s was successfully related to the bridge domain %s", matchingServiceInstance.getName(), matchingBridgeDomain.getName())));
                                } else {
                                    if (relatedBridgeDomain.get(0).getId() != null &&
                                            matchingBridgeDomain.getId() != null &&
                                            relatedBridgeDomain.get(0).getId().equals(matchingBridgeDomain.getId()))
                                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                                String.format("Service instace %s is already related to bridge domain %s. No changes were made.", matchingServiceInstance.getName(), matchingBridgeDomain.getName())));
                                    else {
                                        bem.releaseRelationships(matchingServiceInstance.getClassName(), matchingServiceInstance.getId(), Arrays.asList("networkHasBridgeInterface"));
                                        bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), Constants.CLASS_SERVICE_INSTANCE, matchingServiceInstance.getId(), "networkHasBridgeInterface", true);

                                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s", bridgeDomainInDevice.getName()),
                                                String.format("Service instace %s was related to bridge domain %s, but the relationship was changed to bridge domain %s", matchingServiceInstance.getName(),
                                                        relatedBridgeDomain.get(0).getName(), matchingBridgeDomain.getName())));
                                    }
                                }
                            }
                            continue;
                        }

                        if (networkInterface.getNetworkInterfaceType() == NetworkInterface.TYPE_GENERIC_SUBINTERFACE) {
                            String[] interfaceNameTokens = networkInterface.getName().replace(" (split-horizon)", "").split(" "); //The interface name would look like this: GigabitEthernet0/0/2 10
                            //Some entries have an extra " (split-horizon)" at the end that can be discarded
                            if (physicalInterfaces == null) {
                                physicalInterfaces = bem.getChildrenOfClassLight(relatedOject.getId(), relatedOject.getClassName(), "GenericCommunicationsPort", -1);
                            }

                            BusinessObjectLight matchingPhysicalInterface = null;
                            String standardName = SyncUtil.normalizePortName(interfaceNameTokens[0]);
                            for (BusinessObjectLight physicalInterface : physicalInterfaces) {
                                if (physicalInterface.getName().equals(standardName)) { //Checks for the extended and the condensed interface name formats (GigabitEthernetXXX vs GiXXXX)
                                    matchingPhysicalInterface = physicalInterface;
                                    break;
                                }
                            }

                            if (matchingPhysicalInterface == null)
                                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_ERROR, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                        String.format("The physical interface %s was not found. The subinterface %s will not be created nor related to the bridge domain", standardName, networkInterface.getName())));
                            else {
                                List<BusinessObjectLight> virtualPorts = bem.getChildrenOfClassLight(matchingPhysicalInterface.getId(),
                                        matchingPhysicalInterface.getClassName(), Constants.CLASS_VIRTUALPORT, -1);

                                BusinessObjectLight matchingVirtualPort = null;
                                for (BusinessObjectLight virtualPort : virtualPorts) {
                                    if (virtualPort.getName().equals(interfaceNameTokens[interfaceNameTokens.length - 1])) {
                                        matchingVirtualPort = virtualPort;
                                        break;
                                    }
                                }

                                if (matchingVirtualPort == null) {
                                    HashMap<String, String> defaultAttributes = new HashMap<>();
                                    defaultAttributes.put(Constants.PROPERTY_NAME, interfaceNameTokens[interfaceNameTokens.length - 1]);
                                    String newVirtualPort = bem.createObject(Constants.CLASS_VIRTUALPORT, matchingPhysicalInterface.getClassName(), matchingPhysicalInterface.getId(),
                                            defaultAttributes, null);

                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                            String.format("Subinterface %s did not exist and was created.", networkInterface)));

                                    matchingVirtualPort = new BusinessObjectLight(Constants.CLASS_VIRTUALPORT, newVirtualPort, interfaceNameTokens[interfaceNameTokens.length - 1]);
                                } else
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                            String.format("Subinterface %s already exists. No changes were made.", matchingVirtualPort)));

                                List<BusinessObjectLight> relatedBridgeDomain = bem.getSpecialAttribute(matchingVirtualPort.getClassName(), matchingVirtualPort.getId(), "networkHasBridgeInterface");
                                if (relatedBridgeDomain.isEmpty()) {
                                    bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), Constants.CLASS_VIRTUALPORT, matchingVirtualPort.getId(), "networkHasBridgeInterface", true);
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                            String.format("Subinterface %s was successfully related to the bridge domain %s", matchingVirtualPort, matchingBridgeDomain.getName())));
                                } else {
                                    if (relatedBridgeDomain.get(0).getId() != null &&
                                            matchingBridgeDomain.getId() != null &&
                                            relatedBridgeDomain.get(0).getId().equals(matchingBridgeDomain.getId()))
                                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject),
                                                String.format("Subinterface %s is already related to bridge domain %s. No changes were made.", matchingVirtualPort, matchingBridgeDomain.getName())));
                                    else {
                                        bem.releaseRelationships(matchingVirtualPort.getClassName(), matchingVirtualPort.getId(), Collections.singletonList("networkHasBridgeInterface"));
                                        bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), Constants.CLASS_VIRTUALPORT, matchingVirtualPort.getId(), "networkHasBridgeInterface", true);

                                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s", bridgeDomainInDevice.getName()),
                                                String.format("Subinterface %s was related to bridge domain %s, but the relationship was changed to bridge domain %s", matchingVirtualPort,
                                                        relatedBridgeDomain.get(0).getName(), matchingBridgeDomain.getName())));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (InventoryException ex) {
                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_ERROR, "Bridge Domain Information Processing", ex.getLocalizedMessage()));
            }
        }
        return res;
    }

    @Override
    public List<SyncResult> automatedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support automated sync for unmapped pollings");
    }

    @Override
    public List<SyncFinding> supervisedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support supervised sync");
    }

    @Override
    public List<SyncFinding> supervisedSync(PollResult pollResult) {
        throw new UnsupportedOperationException("This provider does not support supervised sync");
    }

    @Override
    public List<SyncResult> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("This provider does not support this operation"); //Not used for now
    }

    /**
     * Reads the channel's input stream into a string.
     *
     * @param channel The session's channel.
     * @return The string with the result of the command execution.
     * @throws InvalidArgumentException if there was an error executing the command or reading its result.
     */
    private String readCommandExecutionResult(ChannelExec channel) throws InvalidArgumentException {
        StringBuilder result = new StringBuilder();
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        try {
            InputStream in = channel.getInputStream();
            InputStream err = channel.getExtInputStream();

            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    outputBuffer.write(tmp, 0, i);
                }
                while (err.available() > 0) {
                    int i = err.read(tmp, 0, 1024);
                    if (i < 0) break;
                    errorBuffer.write(tmp, 0, i);
                }
                if (channel.isClosed()) {
                    if ((in.available() > 0) || (err.available() > 0)) continue;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }

            result.append(outputBuffer.toString("UTF-8"));
            result.append(errorBuffer.toString("UTF-8"));
            channel.disconnect();
        } catch (IOException | JSchException ex) {
            throw new InvalidArgumentException(String.format("Error reading the command execution result: %s", ex.getLocalizedMessage()));
        }
        return result.toString();
    }

    @Override
    public PollResult fetchData(SyncDataSourceConfiguration dataSourceConfiguration) {

        PollResult res = new PollResult();
        JSch sshShell = new JSch();
        Session session = null;
        ChannelExec channel = null;

        try {
            int port = 0;
            String deviceId = null;
            String className = null;
            String host = null;
            String user = null;
            String password = null;

            if (dataSourceConfiguration.getBusinessObjectLight() != null) //NOI18N
                deviceId = dataSourceConfiguration.getBusinessObjectLight().getId();
            else {
                res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_not_defined")
                                , "id", null)));
            }

            if (dataSourceConfiguration.getBusinessObjectLight() != null) //NOI18N
                className = dataSourceConfiguration.getBusinessObjectLight().getClassName(); //NOI18N
            else {
                res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_not_defined")
                                , "className", null))); //NOI18N
            }

            if (dataSourceConfiguration.getCommonParameters().getParameters().containsKey(ESyncParameters.SSH_HOST.getValue())) //NOI18N
                host = dataSourceConfiguration.getCommonParameters().getParameters().get(ESyncParameters.SSH_HOST.getValue()); //NOI18N
            else {
                res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_not_defined")
                                , ESyncParameters.SSH_HOST.getValue(), null))); //NOI18N
            }

            if (dataSourceConfiguration.getCommonParameters().getParameters().containsKey(ESyncParameters.SSH_PORT.getValue())) //NOI18N
                port = Integer.parseInt(dataSourceConfiguration.getCommonParameters().getParameters().get(ESyncParameters.SSH_PORT.getValue())); //NOI18N
            else {
                res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_not_defined")
                                , ESyncParameters.SSH_PORT.getValue(), null)));
            }

            if (dataSourceConfiguration.getCommonParameters().getParameters().containsKey(ESyncParameters.SSH_USER.getValue())) //NOI18N
                user = dataSourceConfiguration.getCommonParameters().getParameters().get(ESyncParameters.SSH_USER.getValue()); //NOI18N
            else {
                res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_not_defined")
                                , ESyncParameters.SSH_USER.getValue(), null))); //NOI18N
            }

            if (dataSourceConfiguration.getCommonParameters().getParameters().containsKey(ESyncParameters.SSH_PASSWORD.getValue())) //NOI18N
                password = dataSourceConfiguration.getCommonParameters().getParameters().get(ESyncParameters.SSH_PASSWORD.getValue()); //NOI18N
            else {
                res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_not_defined")
                                , ESyncParameters.SSH_PASSWORD.getValue(), null))); //NOI18N
            }

            if (className != null && deviceId != null && port > 0) {
                BusinessObjectLight currentObject = bem.getObjectLight(className, deviceId);
                String modelString = currentObject.getName().split("-")[0];

                session = sshShell.getSession(user, host, port);
                session.setPassword(password);
                //Enable to -not recommended
                //- disable host key checking
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect(10000); //Connection timeout
                channel = (ChannelExec) session.openChannel("exec");

                switch (modelString) { //The model of the device is identified from its name. Alternatively, this could be taken from its actual "model" attribute, but this implementation takes the quickest approach.
                    case "ASR920": {
                        channel.setCommand("sh bridge-domain"); //NOI18N
                        channel.connect();

                        BridgeDomainsASR920Parser parser = new BridgeDomainsASR920Parser();
                        res.getResult().put(dataSourceConfiguration,
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }
                    case "ASR1002": {
                        channel.setCommand("sh bridge-domain"); //NOI18N

                        BridgeDomainsASR1002Parser parser = new BridgeDomainsASR1002Parser();

                        res.getResult().put(dataSourceConfiguration,
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }
                    case "ASR1006": {
                        channel.setCommand("sh bridge-domain"); //NOI18N
                        channel.connect();

                        BridgeDomainsASR1006Parser parser = new BridgeDomainsASR1006Parser();

                        res.getResult().put(dataSourceConfiguration,
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }
                    case "ASR9001": {
                        channel.setCommand("sh l2vpnxconnect"); //NOI18N
                        channel.connect();

                        BridgeDomainsASR9001Parser parser = new BridgeDomainsASR9001Parser();

                        res.getResult().put(dataSourceConfiguration,
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }
                    case "ME3600": {
                        channel.setCommand("sh bridge-domain"); //NOI18N
                        channel.connect();
                        BridgeDomainsME3600Parser parser = new BridgeDomainsME3600Parser();

                        res.getResult().put(dataSourceConfiguration,
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }

                    default:
                        res.getExceptions().put(dataSourceConfiguration, Collections.singletonList(
                                new InvalidArgumentException(
                                        String.format("Model %s is not supported. Check your naming conventions [ASR920-XXX, ASR1002-XXX, ASR9001-XXX, ME3600-XXX]"
                                                , modelString))));

                }
            }
        } catch (Exception ex) {
            res.getExceptions().put(dataSourceConfiguration, Collections.singletonList(ex));
        } finally {
            if (session != null)
                session.disconnect();
            if (channel != null)
                channel.disconnect();
        }

        return res;
    }
}