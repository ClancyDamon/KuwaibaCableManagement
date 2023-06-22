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

package org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.reference;

import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ArraySizeMismatchException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ConnectionException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.SnmpManager;
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
import org.neotropic.kuwaiba.modules.commercial.sync.model.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.json.Json;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Synchronization provider to SNMP agents
 * This class implement the logic to connect with a group of SNMP agents to
 * retrieve the data and compare the differences with the management objects
 *
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
@Component
public class ReferenceSnmpSyncProvider extends AbstractSyncProvider {
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
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;

    @Override
    public String getDisplayName() {
        //Reference SNMP Synchronization Provider
        return "Physical / Virtual Interfaces";
    }

    @Override
    public String getId() {
        return ReferenceSnmpSyncProvider.class.getName();
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
            //String community = null;

            if (dsConfig.getParameters().containsKey("deviceId")) //NOI18N
                id = dsConfig.getParameters().get("deviceId"); //NOI18N
            else
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_deviceId_no_defined")
                                , syncGroup.getName(), syncGroup.getId())));

            if (dsConfig.getParameters().containsKey("deviceClass")) //NOI18N
                className = dsConfig.getParameters().get("deviceClass"); //NOI18N
            else
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_deviceClass_no_defined")
                                , syncGroup.getName(), syncGroup.getId())));

            if (dsConfig.getParameters().containsKey("ipAddress")) //NOI18N
                address = dsConfig.getParameters().get("ipAddress"); //NOI18N
            else
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_ipAddress_no_defined")
                                , syncGroup.getName(), syncGroup.getId())));

            if (dsConfig.getParameters().containsKey("port")) //NOI18N
                port = dsConfig.getParameters().get("port"); //NOI18N
            else
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_port_no_defined")
                                , syncGroup.getName(), syncGroup.getId())));

            String version = SnmpManager.VERSION_2C;
            if (dsConfig.getParameters().containsKey(Constants.PROPERTY_SNMP_VERSION))
                version = dsConfig.getParameters().get(Constants.PROPERTY_SNMP_VERSION);

            if (SnmpManager.VERSION_2C.equals(version)) {
                if (!dsConfig.getParameters().containsKey(Constants.PROPERTY_COMMUNITY))
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_community_no_defined")
                                    , syncGroup.getName(), syncGroup.getId())));
            }
            if (SnmpManager.VERSION_3.equals(version)) {
                if (!dsConfig.getParameters().containsKey(Constants.PROPERTY_AUTH_PROTOCOL))
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_auth_protocol_no_defined")
                                    , syncGroup.getName(), syncGroup.getId())));

                if (!dsConfig.getParameters().containsKey(Constants.PROPERTY_SECURITY_NAME))
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_security_name_no_defined")
                                    , syncGroup.getName(), syncGroup.getId())));
            }

            if (pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).isEmpty()) {

                BusinessObjectLight mappedObjLight = null;

                try {
                    mappedObjLight = bem.getObjectLight(className, id);
                } catch (InventoryException ex) {
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                            new InvalidArgumentException(String.format("Can not connect to the synchronization data source due to: %s", ex.getMessage())));
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
                    boolean firstSnmpConnectionAttemptFail = false;
                    //ENTITY-MIB table
                    ReferenceSnmpEntPhysicalTableResourceDefinition entPhysicalTable = new ReferenceSnmpEntPhysicalTableResourceDefinition();
                    List<List<String>> tableAsString = snmpManager.getTableAsString(entPhysicalTable.values().toArray(new org.snmp4j.smi.OID[0]));

                    if (tableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                                new ConnectionException(String.format("Can not connect to the synchronization data source in %s, due to: %s"
                                        , address, mappedObjLight.toString())));
                        firstSnmpConnectionAttemptFail = true;
                    } else {
                        pollResult.getResult().put(dsConfig, new ArrayList<>());
                        pollResult.getResult().get(dsConfig).add(
                                new TableData("entPhysicalTable", SyncUtil.parseMibTable("instance"
                                        , entPhysicalTable, tableAsString))); //NOI18N
                    }
                    //IF_MIB
                    SnmpifXTableResocurceDefinition ifMibTable = new SnmpifXTableResocurceDefinition();
                    List<List<String>> ifMibTableAsString;
                    //if the first attempt fails it will not be possible to make the sync, so we avoid the reading of others MIB tables
                    if (!firstSnmpConnectionAttemptFail) {
                        ifMibTableAsString = snmpManager.getTableAsString(ifMibTable.values().toArray(new org.snmp4j.smi.OID[0]));
                        //if something goes wrong with reading the second MIB table.
                        if (ifMibTableAsString == null) {
                            pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                                    new ConnectionException(String.format("Can not connect to the synchronization data source in %s, due to: %s"
                                            , address, mappedObjLight)));
                        } else {
                            pollResult.getResult().get(dsConfig).add(
                                    new TableData("ifMibTable", SyncUtil.parseMibTable("instance", ifMibTable, ifMibTableAsString))); //NOI18N
                        }
                    }
                }
            }
        }
        return pollResult;
    }

    @Override
    public PollResult fetchData(SyncDataSourceConfiguration dataSourceConfiguration) {
        PollResult pollResult = new PollResult();

        String id = null;
        String className = null;
        String address = null;
        String port = null;
        //String community = null;
        if (dataSourceConfiguration.getBusinessObjectLight() != null) //NOI18N
            id = dataSourceConfiguration.getBusinessObjectLight().getId();
        else
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_deviceId_no_defined")
                            , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));

        if (dataSourceConfiguration.getBusinessObjectLight() != null) //NOI18N
            className = dataSourceConfiguration.getBusinessObjectLight().getClassName();
        else
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_deviceClass_no_defined")
                            , dataSourceConfiguration.getName(), dataSourceConfiguration.getId())));

        if (dataSourceConfiguration.getCommonParameters().getParameters().containsKey(ESyncParameters.SNMP_ADDRESS.getValue())) //NOI18N
            address = dataSourceConfiguration.getCommonParameters().getParameters().get(ESyncParameters.SNMP_ADDRESS.getValue()); //NOI18N
        else
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_ipAddress_no_defined")
                            , ESyncParameters.SNMP_ADDRESS.getValue(), dataSourceConfiguration.getId())));

        if (dataSourceConfiguration.getCommonParameters().getParameters().containsKey(ESyncParameters.SNMP_PORT.getValue())) //NOI18N
            port = dataSourceConfiguration.getCommonParameters().getParameters().get(ESyncParameters.SNMP_PORT.getValue()); //NOI18N
        else
            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_port_no_defined")
                            , ESyncParameters.SNMP_PORT.getValue(), null)));
        if (className != null && id != null && port != null) {
            String version = SnmpManager.VERSION_2C;
            if (dataSourceConfiguration.getCommonParameters().getParameters().containsKey(Constants.PROPERTY_SNMP_VERSION))
                version = dataSourceConfiguration.getCommonParameters().getParameters().get(Constants.PROPERTY_SNMP_VERSION);

            if (SnmpManager.VERSION_2C.equals(version)) {
                if (!dataSourceConfiguration.getCommonParameters().getParameters().containsKey(Constants.PROPERTY_COMMUNITY))
                    pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_community_no_defined")
                                    , Constants.PROPERTY_COMMUNITY, null )));
            }
            if (SnmpManager.VERSION_3.equals(version)) {
                if (!dataSourceConfiguration.getParameters().containsKey(Constants.PROPERTY_AUTH_PROTOCOL))
                    pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_auth_protocol_no_defined")
                            )));

                if (!dataSourceConfiguration.getParameters().containsKey(Constants.PROPERTY_SECURITY_NAME))
                    pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new InvalidArgumentException(String.format(ts.getTranslatedString("parameter_security_name_no_defined")
                            )));
            }

            if (pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).isEmpty()) {

                BusinessObjectLight mappedObjLight = null;

                try {
                    mappedObjLight = bem.getObjectLight(className, id);
                } catch (InventoryException ex) {
                    pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                            new InvalidArgumentException(String.format("Can not connect to the synchronization data source due to: %s", ex.getMessage())));
                }
                if (mappedObjLight != null) {
                    SnmpManager snmpManager = SnmpManager.getInstance();

                    snmpManager.setAddress(String.format("udp:%s/%s", address, port)); //NOI18N
                    snmpManager.setVersion(version);

                    if (SnmpManager.VERSION_2C.equals(version))
                        snmpManager.setCommunity(dataSourceConfiguration.getCommonParameters().getParameters().get(Constants.PROPERTY_COMMUNITY));

                    if (SnmpManager.VERSION_3.equals(version)) {
                        snmpManager.setAuthProtocol(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_AUTH_PROTOCOL));
                        snmpManager.setAuthPass(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_AUTH_PASS));
                        snmpManager.setSecurityLevel(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_SECURITY_LEVEL));
                        snmpManager.setContextName(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_CONTEXT_NAME));
                        snmpManager.setSecurityName(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_SECURITY_NAME));
                        snmpManager.setPrivacyProtocol(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_PRIVACY_PROTOCOL));
                        snmpManager.setPrivacyPass(dataSourceConfiguration.getParameters().get(Constants.PROPERTY_PRIVACY_PASS));
                    }
                    boolean firstSnmpConnectionAttemptFail = false;
                    //ENTITY-MIB table
                    ReferenceSnmpEntPhysicalTableResourceDefinition entPhysicalTable = new ReferenceSnmpEntPhysicalTableResourceDefinition();
                    List<List<String>> tableAsString = snmpManager.getTableAsString(entPhysicalTable.values().toArray(new org.snmp4j.smi.OID[0]));

                    if (tableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                                new ConnectionException(String.format("Can not connect to the synchronization data source in %s, due to: %s"
                                        , address, mappedObjLight)));
                        firstSnmpConnectionAttemptFail = true;
                    } else {
                        pollResult.getResult().put(dataSourceConfiguration, new ArrayList<>());
                        pollResult.getResult().get(dataSourceConfiguration).add(
                                new TableData("entPhysicalTable", SyncUtil.parseMibTable("instance"
                                        , entPhysicalTable, tableAsString))); //NOI18N
                    }
                    //IF_MIB
                    SnmpifXTableResocurceDefinition ifMibTable = new SnmpifXTableResocurceDefinition();
                    List<List<String>> ifMibTableAsString = null;
                    //if the first attempt fails it will not be possible to make the sync, so we avoid the reading of others MIB tables
                    if (!firstSnmpConnectionAttemptFail) {
                        ifMibTableAsString = snmpManager.getTableAsString(ifMibTable.values().toArray(new org.snmp4j.smi.OID[0]));
                        //if something goes wrong with reading the second MIB table.
                        if (ifMibTableAsString == null) {
                            pollResult.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                                    new ConnectionException(String.format("Can not connect to the synchronization data source in %s, due to: %s"
                                            , address, mappedObjLight)));
                        } else {
                            pollResult.getResult().get(dataSourceConfiguration).add(
                                    new TableData("ifMibTable", SyncUtil.parseMibTable("instance", ifMibTable, ifMibTableAsString))); //NOI18N
                        }
                    }
                }
            }
        }
        return pollResult;
    }

    @Override
    public List<SyncFinding> supervisedSync(PollResult pollResult) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling");
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
        HashMap<SyncDataSourceConfiguration, List<AbstractDataEntity>> originalData = pollResult.getResult();
        List<SyncResult> results = new ArrayList<>();
        // Adding to findings list the not blocking execution exception found during the mapped poll
        for (SyncDataSourceConfiguration agent : pollResult.getExceptions().keySet()) {
            for (Exception exception : pollResult.getExceptions().get(agent))
                results.add(new SyncResult(agent.getId(), SyncFinding.EVENT_ERROR,
                        exception.getMessage(),
                        Json.createObjectBuilder().add("type", "ex").build().toString()));
        }
        for (Map.Entry<SyncDataSourceConfiguration, List<AbstractDataEntity>> entrySet : originalData.entrySet()) {
            List<TableData> mibTables = new ArrayList<>();
            entrySet.getValue().forEach((value) -> mibTables.add((TableData) value));

            EntPhysicalSynchronizer x = new EntPhysicalSynchronizer(
                    entrySet.getKey().getId()
                    , new BusinessObjectLight(entrySet.getKey().getBusinessObjectLight().getClassName()
                    , entrySet.getKey().getBusinessObjectLight().getId(), "")
                    , mibTables, bem, aem, mem, ts);

            try {
                results.addAll(x.sync());
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException
                     | OperationNotPermittedException | ApplicationObjectNotFoundException | ArraySizeMismatchException
                     | NotAuthorizedException ex) {
                Logger.getLogger(ReferenceSnmpSyncProvider.class.getName()).log(Level.SEVERE, ex.getMessage());
            }

        }
        return results;
    }

    @Override
    public List<SyncResult> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}