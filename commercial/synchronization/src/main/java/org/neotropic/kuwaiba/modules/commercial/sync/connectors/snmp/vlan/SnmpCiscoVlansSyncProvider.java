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

package org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.vlan;

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
import org.snmp4j.smi.OID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
@Component
public class SnmpCiscoVlansSyncProvider extends AbstractSyncProvider {
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
        //Cisco - vlanTrunkPortTable SNMP Synchronization Provider
        return "VLANs";
    }

    @Override
    public String getId() {
        return SnmpCiscoVlansSyncProvider.class.getName();
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
        PollResult pollResult = new PollResult();

        for (SyncDataSourceConfiguration dsConfig : syncGroup.getSyncDataSourceConfigurations()) {
            String id = null;
            String className = null;
            String address = null;
            String port = null;

            if (dsConfig.getParameters().containsKey("deviceId")) //NOI18N
                id = dsConfig.getParameters().get("deviceId"); //NOI18N
            else
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_deviceId_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (dsConfig.getParameters().containsKey("deviceClass")) //NOI18N
                className = dsConfig.getParameters().get("deviceClass"); //NOI18N
            else
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_deviceClass_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (dsConfig.getParameters().containsKey("ipAddress")) //NOI18N
                address = dsConfig.getParameters().get("ipAddress"); //NOI18N
            else
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_ipAddress_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (dsConfig.getParameters().containsKey("port")) //NOI18N
                port = dsConfig.getParameters().get("port"); //NOI18N
            else
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_port_no_defined"), syncGroup.getName(), syncGroup.getId())));

            String version = SnmpManager.VERSION_2C;
            if (dsConfig.getParameters().containsKey(Constants.PROPERTY_SNMP_VERSION))
                version = dsConfig.getParameters().get(Constants.PROPERTY_SNMP_VERSION);

            if (SnmpManager.VERSION_2C.equals(version)) {
                if (!dsConfig.getParameters().containsKey(Constants.PROPERTY_COMMUNITY))
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_community_no_defined"), syncGroup.getName(), syncGroup.getId())));
            }
            if (SnmpManager.VERSION_3.equals(version)) {
                if (!dsConfig.getParameters().containsKey(Constants.PROPERTY_AUTH_PROTOCOL))
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_auth_protocol_no_defined"), syncGroup.getName(), syncGroup.getId())));

                if (!dsConfig.getParameters().containsKey(Constants.PROPERTY_SECURITY_NAME))
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_security_name_no_defined"), syncGroup.getName(), syncGroup.getId())));
            }

            if (pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).isEmpty()) {
                BusinessObjectLight mappedObjLight = null;
                try {
                    mappedObjLight = bem.getObjectLight(className, id);
                } catch (InventoryException ex) {
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("snmp_sync_object_not_found"), ex.getMessage())));
                }
                if (mappedObjLight != null) {
                    SnmpManager snmpManager = SnmpManager.getInstance();

                    snmpManager.setAddress(String.format("udp:%s/%s", address, port)); //NOI18N
                    snmpManager.setVersion(version);

                    if (SnmpManager.VERSION_2C.equals(version))
                        snmpManager.setCommunity(dsConfig.getParameters().get(Constants.PROPERTY_COMMUNITY));

                    if (SnmpManager.VERSION_3.equals(version)) {
                        snmpManager.setAuthProtocol(dsConfig.getParameters().get(Constants.PROPERTY_AUTH_PROTOCOL));
                        snmpManager.setAuthPass(dsConfig.getParameters().get(Constants.PROPERTY_AUTH_PASS));
                        snmpManager.setSecurityLevel(dsConfig.getParameters().get(Constants.PROPERTY_SECURITY_LEVEL));
                        snmpManager.setContextName(dsConfig.getParameters().get(Constants.PROPERTY_CONTEXT_NAME));
                        snmpManager.setSecurityName(dsConfig.getParameters().get(Constants.PROPERTY_SECURITY_NAME));
                        snmpManager.setPrivacyProtocol(dsConfig.getParameters().get(Constants.PROPERTY_PRIVACY_PROTOCOL));
                        snmpManager.setPrivacyPass(dsConfig.getParameters().get(Constants.PROPERTY_PRIVACY_PASS));
                    }
                    //VlanTrunkPortsTable
                    SnmpVlanTrunkPortsTableResourceDefinition VlanTrunkPortsTable = new SnmpVlanTrunkPortsTableResourceDefinition();
                    List<List<String>> vlansMibTableAsString = snmpManager.getTableAsString(VlanTrunkPortsTable.values().toArray(new OID[0]));

                    if (vlansMibTableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                                new ConnectionException(String.format(ts.getTranslatedString("snmp_agent_connection_exception"), mappedObjLight)));
                        return pollResult;
                    }
                    pollResult.getResult().put(dsConfig, new ArrayList<>());
                    pollResult.getResult().get(dsConfig).add(
                            new TableData("vlansMibTable", SyncUtil.parseMibTable("instance", VlanTrunkPortsTable, vlansMibTableAsString))); //NOI18N
                    //ifXTable
                    SnmpifXTableResocurceDefinition ifXTable = new SnmpifXTableResocurceDefinition();
                    List<List<String>> ifXTableAsString = snmpManager.getTableAsString(ifXTable.values().toArray(new OID[0]));

                    if (ifXTableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                                new ConnectionException(String.format(ts.getTranslatedString("snmp_agent_connection_exception"), mappedObjLight)));
                        return pollResult;
                    }

                    pollResult.getResult().get(dsConfig).add(
                            new TableData("ifXTable", SyncUtil.parseMibTable("instance", ifXTable, ifXTableAsString))); //NOI18N
                    //VlanInfo
                    SnmpVtpVlanTableResourceDefinition vlanInfo = new SnmpVtpVlanTableResourceDefinition();
                    List<List<String>> vlanInfoAsString = snmpManager.getTableAsString(vlanInfo.values().toArray(new OID[0]));

                    if (vlanInfoAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                                new ConnectionException(String.format(ts.getTranslatedString("snmp_agent_connection_exception"), mappedObjLight)));
                        return pollResult;
                    }

                    pollResult.getResult().get(dsConfig).add(
                            new TableData("vlanInfo", SyncUtil.parseMibTable("instance", vlanInfo, vlanInfoAsString))); //NOI18N

                    //vmMemberShipTable
                    SnmpvmMembershipTableResourceDefinition vmMembershipTable = new SnmpvmMembershipTableResourceDefinition();
                    List<List<String>> vmMembershipTableAsString = snmpManager.getTableAsString(vmMembershipTable.values().toArray(new OID[0]));

                    if (vmMembershipTableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                                new ConnectionException(String.format(ts.getTranslatedString("snmp_agent_connection_exception"), mappedObjLight)));
                        return pollResult;
                    }

                    pollResult.getResult().get(dsConfig).add(
                            new TableData("vmMembershipTable", SyncUtil.parseMibTable("instance", vmMembershipTable, vmMembershipTableAsString))); //NOI18N
                }
            }
        }
        return pollResult;
    }

    @Override
    public List<SyncFinding> supervisedSync(PollResult pollResult) {
        throw new UnsupportedOperationException("This provider does not support automated sync");
    }

    @Override
    public List<SyncFinding> supervisedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling");
    }

    @Override
    public List<SyncResult> automatedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support supervised sync for unmapped pollings");
    }

    @Override
    public List<SyncResult> automatedSync(PollResult pollResult) {
        List<SyncResult> res = new ArrayList<>();
        HashMap<SyncDataSourceConfiguration, List<AbstractDataEntity>> originalData = pollResult.getResult();
        // Adding to result list the not blocking execution exception found during the mapped poll
        for (SyncDataSourceConfiguration dsConfig : pollResult.getExceptions().keySet()) {
            for (Exception ex : pollResult.getExceptions().get(dsConfig))
                res.add(new SyncResult(dsConfig.getId(), SyncFinding.EVENT_ERROR, String.format("Severe error while processing data source configuration %s", dsConfig.getName()), ex.getLocalizedMessage()));
        }
        for (Map.Entry<SyncDataSourceConfiguration, List<AbstractDataEntity>> entrySet : originalData.entrySet()) {
            List<TableData> mibTables = new ArrayList<>();
            entrySet.getValue().forEach((value) -> mibTables.add((TableData) value));

            CiscoVlansSynchronizer ciscoSync = new CiscoVlansSynchronizer(entrySet.getKey().getId(),
                    new BusinessObjectLight(entrySet.getKey().getParameters().get("deviceClass"),
                            entrySet.getKey().getParameters().get("deviceId"), ""),
                    mibTables, bem, aem);
            res.addAll(ciscoSync.execute());
        }
        return res;
    }

    @Override
    public PollResult fetchData(SyncDataSourceConfiguration dataSourceConfiguration) {
        PollResult pollResult = new PollResult();

        String id = null;
        String className = null;
        String address = null;
        String port = null;

        if (dataSourceConfiguration.getParameters().containsKey("deviceId")) //NOI18N
            id = dataSourceConfiguration.getParameters().get("deviceId"); //NOI18N
        else
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_deviceId_no_defined")
                            , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));

        if (dataSourceConfiguration.getParameters().containsKey("deviceClass")) //NOI18N
            className = dataSourceConfiguration.getParameters().get("deviceClass"); //NOI18N
        else
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_deviceClass_no_defined")
                            , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));

        if (dataSourceConfiguration.getParameters().containsKey("ipAddress")) //NOI18N
            address = dataSourceConfiguration.getParameters().get("ipAddress"); //NOI18N
        else
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_ipAddress_no_defined")
                            , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));

        if (dataSourceConfiguration.getParameters().containsKey("port")) //NOI18N
            port = dataSourceConfiguration.getParameters().get("port"); //NOI18N
        else
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_port_no_defined")
                            , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));

        String version = SnmpManager.VERSION_2C;
        if (dataSourceConfiguration.getParameters().containsKey(Constants.PROPERTY_SNMP_VERSION))
            version = dataSourceConfiguration.getParameters().get(Constants.PROPERTY_SNMP_VERSION);

        if (SnmpManager.VERSION_2C.equals(version)) {
            if (!dataSourceConfiguration.getParameters().containsKey(Constants.PROPERTY_COMMUNITY))
                pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_community_no_defined")
                                , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));
        }
        if (SnmpManager.VERSION_3.equals(version)) {
            if (!dataSourceConfiguration.getParameters().containsKey(Constants.PROPERTY_AUTH_PROTOCOL))
                pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_auth_protocol_no_defined")
                                , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));

            if (!dataSourceConfiguration.getParameters().containsKey(Constants.PROPERTY_SECURITY_NAME))
                pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_security_name_no_defined")
                                , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));
        }

        if (pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).isEmpty()) {
            BusinessObjectLight mappedObjLight = null;
            try {
                mappedObjLight = bem.getObjectLight(className, id);
            } catch (InventoryException ex) {
                pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("snmp_sync_object_not_found"), ex.getMessage())));
            }
            if (mappedObjLight != null) {
                SnmpManager snmpManager = SnmpManager.getInstance();

                snmpManager.setAddress(String.format("udp:%s/%s", address, port)); //NOI18N
                snmpManager.setVersion(version);

                if (SnmpManager.VERSION_2C.equals(version))
                    snmpManager.setCommunity(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_COMMUNITY));

                if (SnmpManager.VERSION_3.equals(version)) {
                    snmpManager.setAuthProtocol(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_AUTH_PROTOCOL));
                    snmpManager.setAuthPass(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_AUTH_PASS));
                    snmpManager.setSecurityLevel(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_SECURITY_LEVEL));
                    snmpManager.setContextName(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_CONTEXT_NAME));
                    snmpManager.setSecurityName(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_SECURITY_NAME));
                    snmpManager.setPrivacyProtocol(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_PRIVACY_PROTOCOL));
                    snmpManager.setPrivacyPass(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_PRIVACY_PASS));
                }
                //VlanTrunkPortsTable
                SnmpVlanTrunkPortsTableResourceDefinition VlanTrunkPortsTable = new SnmpVlanTrunkPortsTableResourceDefinition();
                List<List<String>> vlansMibTableAsString = snmpManager.getTableAsString(VlanTrunkPortsTable.values().toArray(new OID[0]));

                if (vlansMibTableAsString == null) {
                    pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new ConnectionException(String.format(ts.getTranslatedString("snmp_agent_connection_exception"), mappedObjLight)));
                    return pollResult;
                }
                pollResult.getResult().put(dataSourceConfiguration, new ArrayList<>());
                pollResult.getResult().get(dataSourceConfiguration).add(
                        new TableData("vlansMibTable", SyncUtil.parseMibTable("instance", VlanTrunkPortsTable, vlansMibTableAsString))); //NOI18N
                //ifXTable
                SnmpifXTableResocurceDefinition ifXTable = new SnmpifXTableResocurceDefinition();
                List<List<String>> ifXTableAsString = snmpManager.getTableAsString(ifXTable.values().toArray(new OID[0]));

                if (ifXTableAsString == null) {
                    pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new ConnectionException(String.format(ts.getTranslatedString("snmp_agent_connection_exception"), mappedObjLight)));
                    return pollResult;
                }

                pollResult.getResult().get(dataSourceConfiguration).add(
                        new TableData("ifXTable", SyncUtil.parseMibTable("instance", ifXTable, ifXTableAsString))); //NOI18N
                //VlanInfo
                SnmpVtpVlanTableResourceDefinition vlanInfo = new SnmpVtpVlanTableResourceDefinition();
                List<List<String>> vlanInfoAsString = snmpManager.getTableAsString(vlanInfo.values().toArray(new OID[0]));

                if (vlanInfoAsString == null) {
                    pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new ConnectionException(String.format(ts.getTranslatedString("snmp_agent_connection_exception"), mappedObjLight)));
                    return pollResult;
                }

                pollResult.getResult().get(dataSourceConfiguration).add(
                        new TableData("vlanInfo", SyncUtil.parseMibTable("instance", vlanInfo, vlanInfoAsString))); //NOI18N

                //vmMemberShipTable
                SnmpvmMembershipTableResourceDefinition vmMembershipTable = new SnmpvmMembershipTableResourceDefinition();
                List<List<String>> vmMembershipTableAsString = snmpManager.getTableAsString(vmMembershipTable.values().toArray(new OID[0]));

                if (vmMembershipTableAsString == null) {
                    pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new ConnectionException(String.format(ts.getTranslatedString("snmp_agent_connection_exception"), mappedObjLight)));
                    return pollResult;
                }

                pollResult.getResult().get(dataSourceConfiguration).add(
                        new TableData("vmMembershipTable", SyncUtil.parseMibTable("instance", vmMembershipTable, vmMembershipTableAsString))); //NOI18N
            }
        }

        return pollResult;
    }

    @Override
    public List<SyncResult> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
