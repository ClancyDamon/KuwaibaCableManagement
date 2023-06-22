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

package org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.PDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A SNMP Manager is a client of an SNMP agent which consume the information given
 * by the agent, and transform the data to be manage like java objects
 *
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
public class SnmpManager {
    public static final String NONE = "None";
    public static final String VERSION_2C = "2c";
    public static final String VERSION_3 = "3";
    public static final String AUTH_MD5 = "MD5";
    public static final String AUTH_SHA = "SHA";
    public static final String PRIV_DES = "DES";
    public static final String PRIV_AES = "AES";
    public static final String NO_AUTH_NO_PRIV = "noAuthNoPriv";
    public static final String AUTH_NO_PRIV = "authNoPriv";
    public static final String AUTH_PRIV = "authPriv";

    private static final int RETRIES = 2;
    private static final int TIMEOUT = 5000;
    /**
     * Singleton instance
     */
    private static SnmpManager instance;
    /**
     * e.g. udp:127.0.0.1/1024
     */
    private String address;
    /**
     * SNMP client
     */
    private Snmp snmp;
    /**
     * SNMP version. Possible values: 2c, 3. Default value 2c
     */
    private String version = "2c";
    /**
     * SNMP Version 2c attribute community. Default value public
     */
    private String community = "public";
    /**
     * SNMP version 3 attribute authentication protocol. Possible values: MD5, SHA
     */
    private String authProtocol;
    /**
     * SNMP version 3 attribute  authentication protocol pass phrase
     */
    private String authPass;
    /*
    private String securityEngineID; // no supported yet
    private String contextEngineID; // no supported yet
    */
    /**
     * SNMP version 3 attribute security level. Possible values: noAuthNoPriv, authNoPriv, authPriv
     */
    private String securityLevel;
    /**
     * SNMP version 3 attribute context name
     */
    private String contextName;
    /**
     * SNMP version 3 attribute security name
     */
    private String securityName;
    /**
     * SNMP version 3 attribute privacy protocol. Possible values: DES, AES
     */
    private String privacyProtocol;
    /**
     * SNMP version 3 attribute privacy protocol pass phrase
     */
    private String privacyPass;

    /*
    private String destinationEngine_Boots_Time; // no supported yet
    */
    private SnmpManager() {
    }

    public static SnmpManager getInstance() {
        return instance == null ? instance = new SnmpManager() : instance;
    }

    public String getAddress() {
        return address;
    }

    /**
     * @param address e.g. udp:127.0.0.1/1024
     */
    public void setAddress(String address) {
        this.address = address;
    }

    public String getVersion() {
        return version;
    }

    /**
     * @param version SNMP version. Possible values: 2c, 3. Default value 2c
     */
    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommunity() {
        return community;
    }

    /**
     * @param community SNMP version 2 attribute community default value public
     */
    public void setCommunity(String community) {
        this.community = community;
    }

    public String getAuthProtocol() {
        return authProtocol;
    }

    /**
     * @param authProtocol SNMP version 3 attribute authentication protocol. Possible values: MD5, SHA
     */
    public void setAuthProtocol(String authProtocol) {
        this.authProtocol = authProtocol;
    }

    public String getAuthPass() {
        return authPass;
    }

    /**
     * @param authPass SNMP version 3 attribute  authentication protocol pass phrase.
     *                 If the value of authProtocol is not null authPass must be not null
     */
    public void setAuthPass(String authPass) {
        this.authPass = authPass;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    /**
     * @param securityLevel SNMP version 3 attribute security level. Possible values: noAuthNoPriv, authNoPriv, authPriv
     */
    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }

    public String getContextName() {
        return contextName;
    }

    /**
     * @param contextName SNMP version 3 attribute context name
     */
    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public String getSecurityName() {
        return securityName;
    }

    /**
     * @param securityName SNMP version 3 attribute security name
     */
    public void setSecurityName(String securityName) {
        this.securityName = securityName;
    }

    public String getPrivacyProtocol() {
        return privacyProtocol;
    }

    /**
     * @param privacyProtocol SNMP version 3 attribute privacy protocol. Possible values: DES, AES
     */
    public void setPrivacyProtocol(String privacyProtocol) {
        this.privacyProtocol = privacyProtocol;
    }

    public String getPrivacyPass() {
        return privacyPass;
    }

    /**
     * @param privacyPass SNMP version 3 attribute privacy protocol pass phrase
     */
    public void setPrivacyPass(String privacyPass) {
        this.privacyPass = privacyPass;
    }

    /**
     * Gets a list of items for the given oids. Only if the parameters for a version of SNMP are assigned
     *
     * @param oids list of oids
     * @return A list of items where the columns are assigned to the oids and the rows for each item
     */
    public List<List<String>> getTableAsString(OID[] oids) {
        if (oids == null)
            return null;

        if (oids.length < 1)
            return null;

        switch (version) {
            case VERSION_2C:
                return getTableAsStringSnmpVersion2c(oids);
            case VERSION_3:
                return getTableAsStringSnmpVersion3(oids);
            default:
                return null;
        }
    }

    private Target getTarget() {
        Address targetAddress = GenericAddress.parse(address);
        if (targetAddress == null)
            return null;

        switch (version) {
            case VERSION_2C:
                return getCommunityTarget(targetAddress);
            case VERSION_3:
                return getUserTarget(targetAddress);
            default:
                return null;
        }
    }

    private CommunityTarget getCommunityTarget(Address targetAddress) {
        try {
            TransportMapping<UdpAddress> transportMapping = new DefaultUdpTransportMapping();

            snmp = new Snmp(transportMapping);

            snmp.listen();

            CommunityTarget communityTarget = new CommunityTarget();

            communityTarget.setCommunity(new OctetString(community == null ? "public" : community)); //NOI18N
            communityTarget.setVersion(SnmpConstants.version2c);

            communityTarget.setAddress(targetAddress);
            communityTarget.setRetries(RETRIES);
            communityTarget.setTimeout(TIMEOUT);

            return communityTarget;

        } catch (IOException ex) {
            return null;
        }
    }

    private UserTarget getUserTarget(Address targetAddress) {
        try {
            TransportMapping<UdpAddress> transportMapping = new DefaultUdpTransportMapping();

            snmp = new Snmp(transportMapping);

            USM usm = new USM(SecurityProtocols.getInstance(),
                    new OctetString(MPv3.createLocalEngineID()), 0);

            SecurityModels.getInstance().addSecurityModel(usm);

            snmp.listen();

            OID authProtocolOID = null;

            if (!NONE.equals(authProtocol)) {
                if (AUTH_MD5.equals(authProtocol))
                    authProtocolOID = AuthMD5.ID;
                /*
                if (AUTH_SHA.equals(authProtocol)) {
                    //TODO: Manage SHA
                }
                */
            }
            OID privacyProtocolOID = null;

            if (!NONE.equals(privacyProtocol)) {

                if (PRIV_DES.equals(privacyProtocol))
                    privacyProtocolOID = PrivDES.ID;
                /*
                if (PRIV_AES.equals(privacyProtocol)) {
                    //TODO: Manage AES
                }
                */
            }
            snmp.getUSM().addUser(!securityName.isEmpty() ? new OctetString(securityName) : null,
                    new UsmUser(!securityName.isEmpty() ? new OctetString(securityName) : null,
                            authProtocolOID,
                            authProtocolOID == null || !authPass.isEmpty() ? new OctetString(authPass) : null,
                            privacyProtocolOID,
                            privacyProtocolOID == null || !privacyPass.isEmpty() ? new OctetString(privacyPass) : null));

            UserTarget userTarget = new UserTarget();
            userTarget.setAddress(targetAddress);
            userTarget.setRetries(RETRIES);
            userTarget.setTimeout(TIMEOUT);
            userTarget.setVersion(SnmpConstants.version3);

            switch (securityLevel) {
                case AUTH_NO_PRIV:
                    userTarget.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
                    break;
                case AUTH_PRIV:
                    userTarget.setSecurityLevel(SecurityLevel.AUTH_PRIV);
                    break;
                case NO_AUTH_NO_PRIV:
                default:
                    userTarget.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
                    break;
            }
            userTarget.setSecurityName(!securityName.isEmpty() ? new OctetString(securityName) : new OctetString());

            return userTarget;

        } catch (IOException ex) {
            return null;
        }
    }

    private List<List<String>> getTableAsStringSnmpVersion2c(OID[] oids) {
        Target target = getTarget();

        if (target == null)
            return null;

        TableUtils tableUtils = new TableUtils(snmp, new DefaultPDUFactory());

        return getTableAsString(target, tableUtils, oids);
    }

    private List<List<String>> getTableAsStringSnmpVersion3(OID[] oids) {
        Target target = getTarget();

        if (target == null)
            return null;

        TableUtils tableUtils = new TableUtils(snmp, new PDUFactory() {

            @Override
            public PDU createPDU(Target target) {
                ScopedPDU pdu = new ScopedPDU();
                pdu.setType(PDU.GETBULK);
                pdu.setContextName(!contextName.isEmpty() ? new OctetString(contextName) : new OctetString());

                return pdu;
            }

            @Override
            public PDU createPDU(MessageProcessingModel messageProcessingModel) {
                return null;
            }
        });

        return getTableAsString(target, tableUtils, oids);
    }

    private List<List<String>> getTableAsString(Target target, TableUtils tableUtils, OID[] oids) {

        List<TableEvent> events = tableUtils.getTable(target, oids, null, null);

        List<List<String>> list = new ArrayList<>();

        for (TableEvent event : events) {
            if (event.isError())
                return null;

            List<String> strList = new ArrayList<>();
            list.add(strList);

            for (VariableBinding vb : event.getColumns())
                strList.add(vb != null ? vb.getVariable().toString() : "");

            String strIndex = event.getIndex().toString();
            strList.add(strIndex);
        }
        try {
            snmp.close();
        } catch (IOException ex) {
            return null;
        }
        return list;
    }
}
