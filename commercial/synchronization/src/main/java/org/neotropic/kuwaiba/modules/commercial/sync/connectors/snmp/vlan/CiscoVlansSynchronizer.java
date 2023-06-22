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
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncResult;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncUtil;
import org.neotropic.kuwaiba.modules.commercial.sync.model.TableData;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Synchronize the VLANs data, creates the VLANs as special children of the
 * device that is being sync, for every created VLAN it also created
 * relationships with its interfaces(GeneriPorts)
 * The OID required, read it from SNMP is vmMembershipTable(1.3.6.1.4.1.9.9.68.1.2.2.1.2)
 *
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
public class CiscoVlansSynchronizer {
    /**
     * Relationship to associate a port with vlans
     */
    public static final String RELATIONSHIP_PORT_BELONGS_TO_VLAN = "portBelongsToVlan";
    /**
     * The class name of the object
     */
    private final String className;
    /**
     * Device id
     */
    private final String id;
    /**
     * Device Data Source Configuration id
     */
    private final long dsConfigId;
    /**
     * The vlanInfo table loaded into the memory
     */
    private final HashMap<String, List<String>> vlanInfo;
    /**
     * The vlanTrunkPortTable table loaded into the memory
     */
    private final HashMap<String, List<String>> vlanTrunkPortsTable;
    /**
     * The ifXTable loaded into the memory
     */
    private final HashMap<String, List<String>> ifXTable;
    /**
     * The vmMembershipTable loaded into the memory
     */
    private final HashMap<String, List<String>> vmMembershipTable;
    /**
     * The current ports of the device
     */
    private final List<BusinessObjectLight> currentPorts;
    /**
     * Current virtual ports of the device
     */
    private final List<BusinessObjectLight> currentVlans;
    /**
     * Reference to {@link ApplicationEntityManager} aem
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to {@link BusinessEntityManager} bem
     */
    private final BusinessEntityManager bem;
    /**
     * The result finding list
     */
    private final List<SyncResult> results = new ArrayList<>();

    public CiscoVlansSynchronizer(long dsConfigId, BusinessObjectLight obj, List<TableData> data
            , BusinessEntityManager bem, ApplicationEntityManager aem) {
        this.bem = bem;
        this.aem = aem;
        this.className = obj.getClassName();
        this.id = obj.getId();
        this.dsConfigId = dsConfigId;
        vlanTrunkPortsTable = (HashMap<String, List<String>>) data.get(0).getValue();
        ifXTable = (HashMap<String, List<String>>) data.get(1).getValue();
        vlanInfo = (HashMap<String, List<String>>) data.get(2).getValue();
        currentPorts = new ArrayList<>();
        currentVlans = new ArrayList<>();
        vmMembershipTable = (HashMap<String, List<String>>) data.get(3).getValue();
    }

    /**
     * Calculate the VLAN ids counting the if a number 1 is found in
     * the octet
     *
     * @param bit         the current index bit
     * @param firstOctet  a given first four binaries
     * @param secondOctet a given second four binaries
     * @return a list with the vlans ids
     */
    private static List<Long> getVlanIds(int bit, int vlanTrunk, String firstOctet, String secondOctet) {
        int bitIndex = (bit * 8) + vlanTrunk;
        List<Long> vlans = new ArrayList<>();
        char[] fisrtOctetArray = firstOctet.toCharArray();
        char[] secondOctetArray = secondOctet.toCharArray();
        if (fisrtOctetArray.length == secondOctetArray.length) {
            for (int i = 0; i < fisrtOctetArray.length; i++) {
                if (fisrtOctetArray[i] == '1')
                    vlans.add((long) (bitIndex + i));
                if (secondOctetArray[i] == '1')
                    vlans.add((long) (bitIndex + i + 4));
            }
        }
        return vlans;
    }

    /**
     * Adds zeros to the given binary, if it has less than 4 bits (10 -> 0010).
     *
     * @param bin a given binary
     * @return a four positions binary
     */
    private static String addZeros(String bin) {
        StringBuilder s = new StringBuilder(bin);
        if (bin.length() < 4) {
            int numberOfZerosToAdd = 4 - bin.length();
            for (int f = 0; f < numberOfZerosToAdd; f++)
                s.insert(0, "0");
        }
        return s.toString();
    }

    /**
     * Parse a hex to binary
     *
     * @param s a given hex
     * @return a bin
     */
    private static String hexToBin(String s) {
        return new BigInteger(s, 16).toString(2);
    }

    public List<SyncResult> execute() {
        try {
            readCurrentStructure(bem.getObjectChildren(className, id, -1), 1);
            readCurrentStructure(bem.getObjectSpecialChildren(className, id), 2);
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Unexpected error reading current structure", ex.getLocalizedMessage()));
        }
        syncVlans();
        relateTrunkPorts();
        relateAccessPorts();
        return results;
    }

    private void syncVlans() {
        List<String> vlanInstances = vlanInfo.get("instance");
        List<String> vlanNames = vlanInfo.get("vtpVlanName");
        List<BusinessObjectLight> vlansToRemove = new ArrayList<>();
        //first we must to romve the vlans that doens't match with the ones that we are synchronizing
        for (BusinessObjectLight vlan : currentVlans) {
            boolean vlanExists = false;
            for (String vlanInstance : vlanInstances) {
                if ((vlanInstance.split("\\.")[1]).equals(vlan.getName())) {
                    vlanExists = true;
                    break;
                }
            }
            if (!vlanExists) {
                try {
                    bem.deleteObject(vlan.getClassName(), vlan.getId(), true);
                } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException |
                         OperationNotPermittedException | InvalidArgumentException ex) {
                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                            String.format("%s can not be deleted", vlan), ex.getLocalizedMessage()));
                }
                vlansToRemove.add(vlan);
                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "",
                        String.format("%s was deleted", vlan)));
            }
        }
        //we must also remove the deleted vlans from the current data
        vlansToRemove.forEach(currentVlans::remove);
        //Now we create the vlans
        for (int i = 0; i < vlanInstances.size(); i++) {
            //Instances have 1.XXX, so we must split and add the "vlan" we will not support the "vl".
            BusinessObjectLight currentVlan = searchInCurrentStructure(vlanInstances.get(i).split("\\.")[1]);
            if (currentVlan == null) {
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(Constants.PROPERTY_DESCRIPTION, vlanNames.get(i));
                attributes.put(Constants.PROPERTY_NAME, vlanInstances.get(i).split("\\.")[1]);

                String newVlanId;
                try {
                    newVlanId = bem.createSpecialObject(Constants.CLASS_VLAN, className, id, attributes, null);
                    BusinessObject newVlan = bem.getObject(Constants.CLASS_VLAN, newVlanId);
                    currentVlans.add(newVlan);
                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "",
                            String.format("%s was created", newVlan)));
                    //aem.createGeneralActivityLogEntry(aem.getUserInSession(className)
                    // ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.valueOf(newObjectId));
                } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException
                         | MetadataObjectNotFoundException ex) {
                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                            String.format("%s can not be created", vlanInstances.get(i).split("\\.")[1]),
                            ex.getMessage()));
                }
            }
        }
    }

    /**
     * For every port (in trunk mode) instances it processes the vlans data obtained
     * from the vlanTrunkPortTable and compares it with ports and
     * vlans created in Kuwaiba in order to create relationships.
     * <p>
     * e.g.
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  -  15
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  -  31
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  -  47
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  -  63
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  -  79
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  -  95
     * 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00  - 111
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  - 127
     * <p>
     * The data has 128 positions (staring with 0), every position of has
     * two hex values (00) that represents a binary octet (0000 0000).
     * <p>
     * In order to process the data, we iterate over the data until we find
     * some value different, in this case (02) in position (103)
     * <p>
     * We must calculate the bit position, so 8 * 103 = 824
     * <p>
     * Note important!
     * Depending on which vlanTrunkPortVlansEnable we are reading
     * we must add 1024 for 2k, 2048 for 3k, 3072 for 4k
     * <p>
     * e.g: lets supposed that we are reading an oid vlanTrunkPortVlansEnabled4k,
     * so we must add 3072 to the current bit position, 824 + 3072 = 3896
     * we convert 02 from hex to bin, getting: 0000 0010, (zeros were added to the left)
     * <p>
     * The first, second, third, fourth, fifth, sixth are set to cero  that means
     * that there is nothing to do
     * <p>
     * 0     0     0     0  -  0     0     1     0
     * |     |     |     |     |     |     |     |
     * 3896  3897  3898   3899  3900  3901  3902  3903
     * <p>
     * The seventh bit is set on 1 so the VLAN is the instance 3092, in the ifXtable
     * <p>
     * We avoid the patterns with ff:ff:ff:ff or Xf:ff:ff:ff or ff:ff:ff:fX
     */
    public void relateTrunkPorts() {
        List<String> vlanInstances = vlanInfo.get("instance");
        List<String> ifInstances = ifXTable.get("instance");
        List<String> instancesNames = ifXTable.get("ifName");
        List<String> vlanPortIds = vlanTrunkPortsTable.get("instance");
        List<String> vlans1k = vlanTrunkPortsTable.get("vlanTrunkPortVlansEnabled");   //0000 - 1023
        List<String> vlans2k = vlanTrunkPortsTable.get("vlanTrunkPortVlansEnabled2k"); //1024 - 2047
        List<String> vlans3k = vlanTrunkPortsTable.get("vlanTrunkPortVlansEnabled3k"); //2048 - 3071
        List<String> vlans4k = vlanTrunkPortsTable.get("vlanTrunkPortVlansEnabled4k"); //3072 - 4095
        HashMap<String, List<Long>> vlansPortMap = new HashMap<>();
        //create a map with ports and vlans, but we avoid the paterns with ff:ff:ff:ff or [X]f:ff:ff:ff or ff:ff:ff:f[X]
        Pattern p = Pattern.compile("(.f(:ff)*)|((ff:)f.*)");
        for (int i = 0; i < vlanPortIds.size(); i++) {
            List<Long> vlans = new ArrayList<>();
            String portId = vlanPortIds.get(i);
            if (!p.matcher(vlans1k.get(i).toLowerCase()).matches())
                vlans.addAll(readOctets(vlans1k.get(i), 0));
            if (!p.matcher(vlans2k.get(i).toLowerCase()).matches())
                vlans.addAll(readOctets(vlans2k.get(i), 1024));
            if (!p.matcher(vlans3k.get(i).toLowerCase()).matches())
                vlans.addAll(readOctets(vlans3k.get(i), 2048));
            if (!p.matcher(vlans4k.get(i).toLowerCase()).matches())
                vlans.addAll(readOctets(vlans4k.get(i), 3072));

            vlansPortMap.put(portId, new ArrayList<>(vlans));
        }
        //We procces port by port and its list of vlnas to which it belongs
        for (String portId : vlansPortMap.keySet()) {
            //first we search for the port in the ifXTable
            int portIndex = ifInstances.indexOf(portId);
            if (portIndex > -1) {
                String portName = instancesNames.get(portIndex);
                //We get the list of vlans for every port, and we filter that list
                List<Long> allVlans = vlansPortMap.get(portId);
                if (!allVlans.isEmpty()) {
                    //Then we search for the port in the current kuwaiba's structure
                    BusinessObjectLight currentPort = searchInCurrentStructure(SyncUtil.normalizePortName(portName));
                    if (currentPort == null)
                        results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                                "Search in the current structure",
                                String.format("%s not found", SyncUtil.normalizePortName(portName))));
                    else {
                        //We filter to get only the vlans that exits in the ifXTable
                        List<Long> candidateVlans = new ArrayList<>();
                        allVlans.forEach(vlanId ->
                                vlanInstances.forEach(vlanInstance -> {
                                    if (Objects.equals(Long.valueOf(vlanInstance.split("\\.")[1]), vlanId))
                                        candidateVlans.add(vlanId);
                                })
                        );
                        List<BusinessObjectLight> assosiatedVlans = new ArrayList<>();
                        try {
                            assosiatedVlans = bem.getSpecialAttribute(currentPort.getClassName(), currentPort.getId(), RELATIONSHIP_PORT_BELONGS_TO_VLAN);
                        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException |
                                 InvalidArgumentException ex) {
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, ex.getLocalizedMessage(),
                                    String.format("Can not get Vlans associated to %s", currentPort)));
                        }
                        checkPortVlansRelationships(currentPort, candidateVlans, assosiatedVlans);
                        //The filtterd vlans should be created in Kuwaiba, but we check if they are already created in kuwaiba
                        for (long vlanName : candidateVlans) {
                            BusinessObjectLight currentVlan = searchInCurrentStructure(Long.toString(vlanName));
                            if (currentVlan == null) {
                                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                                        "Searching in the current structure",
                                        String.format("%s not found", vlanName)));
                            } else {//we must check if a relationship between the port and vland is already set
                                boolean isAlreadyAssociated = false;
                                for (BusinessObjectLight assosiatedVlan : assosiatedVlans) {
                                    if (assosiatedVlan.getId() != null && currentVlan.getId() != null
                                            && assosiatedVlan.getId().equals(currentVlan.getId())) { //The port and the vlan have a relation
                                        isAlreadyAssociated = true;
                                        results.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION, "",
                                                String.format("%s and %s were related", currentPort, assosiatedVlan)));
                                        break;
                                    }
                                }
                                if (!isAlreadyAssociated) {
                                    try {
                                        bem.createSpecialRelationship(currentPort.getClassName(), currentPort.getId(),
                                                currentVlan.getClassName(), currentVlan.getId(), RELATIONSHIP_PORT_BELONGS_TO_VLAN, false);
                                    } catch (BusinessObjectNotFoundException | OperationNotPermittedException |
                                             MetadataObjectNotFoundException | InvalidArgumentException ex) {
                                        results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                                                String.format("%s and %s were not related", currentPort, vlanName),
                                                ex.getLocalizedMessage()));
                                    }
                                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "",
                                            String.format("%s and %s was related successfully ", currentPort, vlanName)));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void relateAccessPorts() {
        List<String> ifInstances = ifXTable.get("instance"); //port ids
        List<String> instancesNames = ifXTable.get("ifName"); // port names
        List<String> vmVlan = vmMembershipTable.get("vmVlan"); //vlan names
        List<String> vmVlanInstances = vmMembershipTable.get("instance"); //the port ids

        for (int i = 0; i < vmVlan.size(); i++) {
            BusinessObjectLight currentVlan = searchInCurrentStructure(vmVlan.get(i));
            if (currentVlan == null)
                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                        "Searching in the current structure",
                        String.format("%s not found", vmVlan.get(i))));
            else {
                int indexOf = ifInstances.indexOf(vmVlanInstances.get(i));
                if (indexOf > -1) {
                    BusinessObjectLight currentPort = searchInCurrentStructure(instancesNames.get(indexOf));
                    if (currentPort == null)
                        results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                                "Search in the current structure",
                                String.format("%s not found", SyncUtil.normalizePortName(instancesNames.get(indexOf)))));
                    else {
                        List<BusinessObjectLight> assosiatedVlans = new ArrayList<>();
                        try {
                            assosiatedVlans = bem.getSpecialAttribute(currentPort.getClassName(), currentPort.getId(), RELATIONSHIP_PORT_BELONGS_TO_VLAN);
                        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException |
                                 InvalidArgumentException ex) {
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, ex.getLocalizedMessage(),
                                    String.format("Can not get Vlans associated to %s", currentPort)));
                        }
                        boolean isAlreadyAssociated = false;
                        for (BusinessObjectLight assosiatedVlan : assosiatedVlans) {
                            if (assosiatedVlan.getId() != null && currentVlan.getId() != null
                                    && assosiatedVlan.getId().equals(currentVlan.getId())) { //The port and the vlan have a relation
                                isAlreadyAssociated = true;
                                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION, "",
                                        String.format("%s and %s were related", currentPort, assosiatedVlan)));
                                break;
                            }
                        }
                        if (!isAlreadyAssociated) {
                            try {
                                bem.createSpecialRelationship(currentPort.getClassName(), currentPort.getId(),
                                        currentVlan.getClassName(), currentVlan.getId(), RELATIONSHIP_PORT_BELONGS_TO_VLAN, false);
                            } catch (BusinessObjectNotFoundException | OperationNotPermittedException |
                                     MetadataObjectNotFoundException | InvalidArgumentException ex) {
                                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                                        String.format("%s and %s were not related", currentPort, vmVlan.get(i)),
                                        ex.getLocalizedMessage()));
                            }
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "",
                                    String.format("%s and %s was related successfully ", currentPort, vmVlan.get(i))));
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if there are changes and release the relationships between
     * ports and vlans
     *
     * @param port             a given port
     * @param vlanstoAssosiate a list of vlans to associate with the port
     */
    private void checkPortVlansRelationships(BusinessObjectLight port,
                                             List<Long> vlanstoAssosiate, List<BusinessObjectLight> assosiatedVlans) {
        for (BusinessObjectLight assosiatedVlan : assosiatedVlans) {
            boolean isAssociatedVlanInVlansToAssosiate = false;
            for (long vlanToAssosiateName : vlanstoAssosiate) {
                if (assosiatedVlan.getName().equals(vlanToAssosiateName))
                    isAssociatedVlanInVlansToAssosiate = true;
            }
            //if the vlan is not in the list of the vlans to assosiate after sync it must be release
            if (!isAssociatedVlanInVlansToAssosiate) {
                try {
                    bem.releaseSpecialRelationship(port.getClassName(), port.getId(), assosiatedVlan.getId(), RELATIONSHIP_PORT_BELONGS_TO_VLAN);
                } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException |
                         InvalidArgumentException ex) {
                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                            ex.getLocalizedMessage(),
                            String.format("Relation between %s and %s was not released", port, assosiatedVlan)));
                }
                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                        "Updating ports related with VLANs",
                        String.format("Relationship between %s and %s was release", port, assosiatedVlan)));
            }
        }
    }

    /**
     * Reads a whole string o a given octets to calculate the vlans ids
     *
     * @param bitString bits the string of octets
     * @param vlanTrunk the number of vlanTrunkPortVlansEnabled
     */
    private List<Long> readOctets(String bitString, int vlanTrunk) {
        String[] bits = bitString.split(":");
        List<Long> vlans = new ArrayList<>();
        if (bits.length <= 128 && bits.length % 2 == 0) {
            for (int bit = 0; bit < bits.length; bit++) {
                String hexPair = bits[bit];
                String firstHex = hexPair.substring(0, 1);
                String secondHex = hexPair.substring(1, 2);
                String firstOctet = hexToBin(firstHex);
                String secondOctet = hexToBin(secondHex);
                firstOctet = addZeros(firstOctet);
                secondOctet = addZeros(secondOctet);
                vlans.addAll(getVlanIds(bit, vlanTrunk, firstOctet, secondOctet));
            }
        }
        return vlans;
    }

    /**
     * Reads the device's current structure (ports, and logical ports)
     *
     * @param children     a given set of children
     * @param childrenType 1 child, 2 special children
     * @throws MetadataObjectNotFoundException Can not find the class name of
     *                                         the device related with the data source configuration.
     * @throws BusinessObjectNotFoundException if the requested object(electrical port, optical port, virtual port) can't be found
     */
    private void readCurrentStructure(List<BusinessObjectLight> children, int childrenType)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        for (BusinessObjectLight child : children) {
            if (child.getClassName().equals(Constants.CLASS_ELECTRICALPORT) ||
                    child.getClassName().contains(Constants.CLASS_OPTICALPORT) ||
                    child.getClassName().equals(Constants.CLASS_VIRTUALPORT))
                currentPorts.add(child);
            else if (child.getClassName().equals(Constants.CLASS_VLAN))
                currentVlans.add(child);

            if (childrenType == 1)
                readCurrentStructure(bem.getObjectChildren(child.getClassName(), child.getId(), -1), 1);
            else if (childrenType == 2)
                readCurrentStructure(bem.getObjectSpecialChildren(child.getClassName(), child.getId()), 2);
        }
    }

    /**
     * Checks if a given port exists in the current structure
     *
     * @param instance a given name for port, virtual port or vlan
     * @return the object, null doesn't exist in the current structure
     */
    private BusinessObjectLight searchInCurrentStructure(String instance) {
        for (BusinessObjectLight currentPort : currentPorts) {
            if (SyncUtil.normalizePortName(currentPort.getName()).equals(SyncUtil.normalizePortName(instance)))
                return currentPort;
        }
        for (BusinessObjectLight currentVlan : currentVlans) {
            if (currentVlan.getName().toLowerCase().equals(instance))
                return currentVlan;
        }
        return null;
    }
}
