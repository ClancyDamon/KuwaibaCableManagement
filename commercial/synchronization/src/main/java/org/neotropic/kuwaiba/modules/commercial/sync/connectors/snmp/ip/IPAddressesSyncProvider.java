/*
 * Copyright 2023-2023. Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.ip;

import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ConnectionException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.SnmpManager;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.reference.SnmpifXTableResocurceDefinition;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractDataEntity;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.model.PollResult;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncAction;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncFinding;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncResult;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncUtil;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
import org.neotropic.kuwaiba.modules.commercial.sync.model.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This provider finds IP address and relates them to physical interfaces, organizing them in subnets
 *
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
@Component
public class IPAddressesSyncProvider extends AbstractSyncProvider {
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
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;

    @Override
    public String getDisplayName() {
        //IP Addresses, subnets and Interfaces
        return "IP Addresses";
    }

    @Override
    public String getId() {
        return IPAddressesSyncProvider.class.getName();
    }

    @Override
    public boolean isAutomated() {
        return true;
    }

    @Override
    public List<AbstractDataEntity> unmappedPoll(SynchronizationGroup syncGroup) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PollResult mappedPoll(SynchronizationGroup syncGroup) {
        PollResult pollResult = new PollResult();

        for (SyncDataSourceConfiguration agent : syncGroup.getSyncDataSourceConfigurations()) {

            if (!agent.getParameters().containsKey("deviceId")) { //NOI18N
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_deviceId_no_defined")
                                , syncGroup.getName(), syncGroup.getId())));
                continue;
            }
            if (!agent.getParameters().containsKey("deviceClass")) { //NOI18N
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_deviceClass_no_defined")
                                , syncGroup.getName(), syncGroup.getId())));
                continue;
            }
            if (!agent.getParameters().containsKey("ipAddress")) { //NOI18N
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_ipAddress_no_defined")
                                , syncGroup.getName(), syncGroup.getId())));
                continue;
            }

            if (!agent.getParameters().containsKey("port")) { //NOI18N
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_port_no_defined")
                                , syncGroup.getName(), syncGroup.getId())));
                continue;
            }

            if (!agent.getParameters().containsKey(Constants.PROPERTY_SNMP_VERSION)) {
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_snmp_version_no_defined")
                                , syncGroup.getName(), syncGroup.getId())));
                continue;
            }

            String snmpVersion = agent.getParameters().get(Constants.PROPERTY_SNMP_VERSION);

            if (SnmpManager.VERSION_2C.equals(snmpVersion)) {
                if (!agent.getParameters().containsKey(Constants.PROPERTY_COMMUNITY)) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_community_no_defined")
                                    , syncGroup.getName(), syncGroup.getId())));
                    continue;
                }
            }

            if (SnmpManager.VERSION_3.equals(snmpVersion)) {
                if (!agent.getParameters().containsKey(Constants.PROPERTY_AUTH_PROTOCOL)) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_auth_protocol_no_defined")
                                    , syncGroup.getName(), syncGroup.getId())));
                    continue;
                }

                if (!agent.getParameters().containsKey(Constants.PROPERTY_SECURITY_NAME)) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_security_name_no_defined")
                                    , syncGroup.getName(), syncGroup.getId())));
                    continue;
                }
            }

            try {
                BusinessObjectLight mappedObjLight = bem.getObjectLight(agent.getParameters().get("deviceClass")
                        , agent.getParameters().get("deviceId"));
                SnmpManager snmpManager = SnmpManager.getInstance();

                snmpManager.setAddress(String.format("udp:%s/%s", agent.getParameters().get("ipAddress")
                        , agent.getParameters().get("port"))); //NOI18N
                snmpManager.setVersion(snmpVersion);

                if (SnmpManager.VERSION_2C.equals(snmpVersion))
                    snmpManager.setCommunity(agent.getParameters().get(Constants.PROPERTY_COMMUNITY));

                if (SnmpManager.VERSION_3.equals(snmpVersion)) {
                    snmpManager.setAuthProtocol(agent.getParameters().get(Constants.PROPERTY_AUTH_PROTOCOL));
                    snmpManager.setAuthPass(agent.getParameters().get(Constants.PROPERTY_AUTH_PASS));
                    snmpManager.setSecurityLevel(agent.getParameters().get(Constants.PROPERTY_SECURITY_LEVEL));
                    snmpManager.setContextName(agent.getParameters().get(Constants.PROPERTY_CONTEXT_NAME));
                    snmpManager.setSecurityName(agent.getParameters().get(Constants.PROPERTY_SECURITY_NAME));
                    snmpManager.setPrivacyProtocol(agent.getParameters().get(Constants.PROPERTY_PRIVACY_PROTOCOL));
                    snmpManager.setPrivacyPass(agent.getParameters().get(Constants.PROPERTY_PRIVACY_PASS));
                }
                //ipAddrTable table
                SnmpIPResourceDefinition ipAddrTable = new SnmpIPResourceDefinition();
                List<List<String>> tableAsString = snmpManager.getTableAsString(ipAddrTable.values().toArray(new org.snmp4j.smi.OID[0]));

                if (tableAsString == null) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                            new ConnectionException(String.format(ts.getTranslatedString("snmp_agent_connection_exception")
                                    , mappedObjLight.toString())));
                    return pollResult;
                }

                pollResult.getResult().put(agent, new ArrayList<>());
                pollResult.getResult().get(agent).add(
                        new TableData("ipAddrTable", SyncUtil.parseMibTable("instance", ipAddrTable
                                , tableAsString))); //NOI18N
                //
                SnmpifXTableResocurceDefinition ifMibTable = new SnmpifXTableResocurceDefinition();
                List<List<String>> ifMibTableAsString = snmpManager.getTableAsString(ifMibTable.values().toArray(new org.snmp4j.smi.OID[0]));

                if (ifMibTableAsString == null) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                            new ConnectionException(String.format(ts.getTranslatedString("snmp_agent_connection_exception")
                                    , mappedObjLight.toString())));
                    return pollResult;
                }

                pollResult.getResult().get(agent).add(
                        new TableData("ifMibTable", SyncUtil.parseMibTable("instance", ifMibTable, ifMibTableAsString))); //NOI18N
            } catch (InventoryException ex) {
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("snmp_sync_object_not_found"), ex.getMessage())));
            }
        }
        return pollResult;
    }

    @Override
    public PollResult fetchData(SyncDataSourceConfiguration dataSourceConfiguration) {
        PollResult pollResult = new PollResult();
        if (!dataSourceConfiguration.getParameters().containsKey("deviceId")) { //NOI18N
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_deviceId_no_defined")
                            , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));
        }
        if (!dataSourceConfiguration.getParameters().containsKey("deviceClass")) { //NOI18N
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_deviceClass_no_defined")
                            , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));
        }
        if (!dataSourceConfiguration.getParameters().containsKey("ipAddress")) { //NOI18N
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_ipAddress_no_defined")
                            , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));
        }

        if (!dataSourceConfiguration.getParameters().containsKey("port")) { //NOI18N
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_port_no_defined")
                            , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));
        }

        if (!dataSourceConfiguration.getParameters().containsKey(Constants.PROPERTY_SNMP_VERSION)) {
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_snmp_version_no_defined")
                            , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));
        }

        String snmpVersion = dataSourceConfiguration.getParameters().get(Constants.PROPERTY_SNMP_VERSION);

        if (SnmpManager.VERSION_2C.equals(snmpVersion)) {
            if (!dataSourceConfiguration.getParameters().containsKey(Constants.PROPERTY_COMMUNITY)) {
                pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_community_no_defined")
                                , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));
            }
        }

        if (SnmpManager.VERSION_3.equals(snmpVersion)) {
            if (!dataSourceConfiguration.getParameters().containsKey(Constants.PROPERTY_AUTH_PROTOCOL)) {
                pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_auth_protocol_no_defined")
                                , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));
            }

            if (!dataSourceConfiguration.getParameters().containsKey(Constants.PROPERTY_SECURITY_NAME)) {
                pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_security_name_no_defined")
                                , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));
            }
        }

        try {
            BusinessObjectLight mappedObjLight = bem.getObjectLight(dataSourceConfiguration.getParameters().get("deviceClass")
                    , dataSourceConfiguration.getParameters().get("deviceId"));
            SnmpManager snmpManager = SnmpManager.getInstance();

            snmpManager.setAddress(String.format("udp:%s/%s", dataSourceConfiguration.getParameters().get("ipAddress")
                    , dataSourceConfiguration.getParameters().get("port"))); //NOI18N
            snmpManager.setVersion(snmpVersion);

            if (SnmpManager.VERSION_2C.equals(snmpVersion))
                snmpManager.setCommunity(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_COMMUNITY));

            if (SnmpManager.VERSION_3.equals(snmpVersion)) {
                snmpManager.setAuthProtocol(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_AUTH_PROTOCOL));
                snmpManager.setAuthPass(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_AUTH_PASS));
                snmpManager.setSecurityLevel(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_SECURITY_LEVEL));
                snmpManager.setContextName(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_CONTEXT_NAME));
                snmpManager.setSecurityName(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_SECURITY_NAME));
                snmpManager.setPrivacyProtocol(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_PRIVACY_PROTOCOL));
                snmpManager.setPrivacyPass(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_PRIVACY_PASS));
            }
            //ipAddrTable table
            SnmpIPResourceDefinition ipAddrTable = new SnmpIPResourceDefinition();
            List<List<String>> tableAsString = snmpManager.getTableAsString(ipAddrTable.values().toArray(new org.snmp4j.smi.OID[0]));

            if (tableAsString == null) {
                pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new ConnectionException(String.format(ts.getTranslatedString("snmp_agent_connection_exception")
                                , mappedObjLight.toString())));
                return pollResult;
            }

            pollResult.getResult().put(dataSourceConfiguration, new ArrayList<>());
            pollResult.getResult().get(dataSourceConfiguration).add(
                    new TableData("ipAddrTable", SyncUtil.parseMibTable("instance", ipAddrTable
                            , tableAsString))); //NOI18N
            //
            SnmpifXTableResocurceDefinition ifMibTable = new SnmpifXTableResocurceDefinition();
            List<List<String>> ifMibTableAsString = snmpManager.getTableAsString(ifMibTable.values().toArray(new org.snmp4j.smi.OID[0]));

            if (ifMibTableAsString == null) {
                pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new ConnectionException(String.format(ts.getTranslatedString("snmp_agent_connection_exception")
                                , mappedObjLight.toString())));
                return pollResult;
            }

            pollResult.getResult().get(dataSourceConfiguration).add(
                    new TableData("ifMibTable", SyncUtil.parseMibTable("instance", ifMibTable, ifMibTableAsString))); //NOI18N
        } catch (InventoryException ex) {
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("snmp_sync_object_not_found"), ex.getMessage())));
        }

        return pollResult;
    }

    @Override
    public List<SyncFinding> supervisedSync(PollResult pollResult) {
        throw new UnsupportedOperationException("This provider does not support supervised sync for unmapped pollings");
    }

    @Override
    public List<SyncFinding> supervisedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SyncResult> automatedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support automated sync");
    }

    @Override
    public List<SyncResult> automatedSync(PollResult pollResult) {

        List<SyncResult> res = new ArrayList<>();
        // Adding to findings list the not blocking execution exception found during the mapped poll
        for (SyncDataSourceConfiguration agent : pollResult.getExceptions().keySet()) {
            for (Exception ex : pollResult.getExceptions().get(agent))
                res.add(new SyncResult(agent.getId(), SyncFinding.EVENT_ERROR
                        , String.format("Severe error while processing data source configuration %s", agent.getName())
                        , ex.getLocalizedMessage()));
        }

        HashMap<SyncDataSourceConfiguration, List<AbstractDataEntity>> originalData = pollResult.getResult();
        for (Map.Entry<SyncDataSourceConfiguration, List<AbstractDataEntity>> entrySet : originalData.entrySet()) {
            List<TableData> mibTables = new ArrayList<>();
            entrySet.getValue().forEach(value -> {
                mibTables.add((TableData) value);
            });
            IPSynchronizer ipSync = new IPSynchronizer(entrySet.getKey().getId(),
                    new BusinessObjectLight(entrySet.getKey().getParameters().get("deviceClass"),
                            entrySet.getKey().getParameters().get("deviceId"), ""), mibTables, bem, aem);
            res.addAll(ipSync.execute());
        }
        return res;
    }

    @Override
    public List<SyncResult> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
