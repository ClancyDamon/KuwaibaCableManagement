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
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Synchronizer for the ipAddrTable data
 *
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
public class IPSynchronizer {
    /**
     * This relationship is used to connect a GenericCommunicationElement with
     * a subnet's IP address
     */
    public static final String RELATIONSHIP_IPAMHASADDRESS = "ipamHasIpAddress";
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
     * To load the structure of the actual device
     */
    private final List<BusinessObjectLight> currentVirtualPorts;
    /**
     * The current map of subnets and sub-subnets
     */
    private final HashMap<BusinessObjectLight, List<BusinessObjectLight>> subnets;
    /**
     * The current subnets with its ips
     */
    private final HashMap<BusinessObjectLight, List<BusinessObjectLight>> ips;
    /**
     * The current ports in the device
     */
    private final List<BusinessObjectLight> currentPorts;
    /**
     * The ipAddrTable table loaded into the memory
     */
    private final HashMap<String, List<String>> ipAddrTable;
    /**
     * The ifXTable table loaded into the memory
     */
    private final HashMap<String, List<String>> ifXTable;
    /**
     * reference to the bem
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the aem
     */
    private final ApplicationEntityManager aem;
    /**
     * List of results after sync process
     */
    private final List<SyncResult> res;
    /**
     * Reference to the root node of the IPv4
     */
    private InventoryObjectPool ipv4Root;
    /**
     * Reference to the root node of the IPv6
     */
    private InventoryObjectPool ipv6Root;

    public IPSynchronizer(long dsConfigId, BusinessObjectLight obj, List<TableData> data, BusinessEntityManager bem
            , ApplicationEntityManager aem) {

        this.bem = bem;
        this.aem = aem;

        res = new ArrayList<>();
        this.className = obj.getClassName();
        this.id = obj.getId();
        this.dsConfigId = dsConfigId;
        ipAddrTable = (HashMap<String, List<String>>) data.get(0).getValue();
        ifXTable = (HashMap<String, List<String>>) data.get(1).getValue();
        currentPorts = new ArrayList<>();
        subnets = new HashMap<>();
        ips = new HashMap<>();
        currentVirtualPorts = new ArrayList<>();
    }

    /**
     * Executes the synchronization to associate the interfaces get it
     * from the ifmib table with the Ip addresses get it from the ipAddrTable
     *
     * @return list of findings
     */
    public List<SyncResult> execute() {
        try {
            readCurrentStructure(bem.getObjectChildren(className, id, -1), 1);
            readCurrentStructure(bem.getObjectSpecialChildren(className, id), 2);
            //we get the root nodes for ipv4, ipv6
            List<InventoryObjectPool> ipv4RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV4
                    , ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
            List<InventoryObjectPool> ipv6RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV6
                    , ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
            ipv4Root = ipv4RootPools.get(0);
            ipv6Root = ipv6RootPools.get(0);
            readcurrentFolder(ipv4RootPools);
            readcurrentFolder(ipv6RootPools);

            readMibData();
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | ApplicationObjectNotFoundException
                 | InvalidArgumentException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                    "Unexpected error reading current structure",
                    ex.getLocalizedMessage()));
        }
        return res;
    }

    /**
     * Creates an IP address in a given subnet
     *
     * @param subnet   a given subnet
     * @param ipAddr   a new ip address to be created
     * @param syncMask a mask for the given ip address
     * @return the new created ip address
     */
    private BusinessObject createIp(BusinessObjectLight subnet, String ipAddr, String syncMask) {
        BusinessObject createdIp = null;
        HashMap<String, String> ipAttributes = new HashMap<>();
        ipAttributes.put(Constants.PROPERTY_NAME, ipAddr);
        ipAttributes.put(Constants.PROPERTY_DESCRIPTION, "Created by the IP Sync Provider");
        ipAttributes.put(Constants.PROPERTY_MASK, syncMask); //TODO set the list types attributes
        try {
            String newIpId = bem.createSpecialObject(Constants.CLASS_IP_ADDRESS, subnet.getClassName(), subnet.getId(), ipAttributes, null);
            createdIp = bem.getObject(Constants.CLASS_IP_ADDRESS, newIpId);
            ips.get(subnet).add(createdIp);
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "Add IP to Subnet"
                    , String.format("%s was successfully added to %s", ipAddr, subnet)));
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException
                 | ApplicationObjectNotFoundException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                    String.format("%s was not added to %s", ipAddr, subnet),
                    ex.getLocalizedMessage()));
        }
        return createdIp;
    }

    /**
     * Creates a new subnet
     *
     * @param newSubnet a given subnet name
     * @return the created subnet
     */
    private BusinessObjectLight createSubnet(String newSubnet) {
        BusinessObjectLight currentSubnet = null;
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("name", newSubnet + ".0/24");
        attributes.put("description", "created with sync");
        attributes.put("networkIp", newSubnet + ".0");
        attributes.put("broadcastIp", newSubnet + ".255");
        attributes.put("hosts", "254");

        try {
            currentSubnet = bem.getObject(Constants.CLASS_SUBNET_IPV4, bem.createPoolItem(ipv4Root.getId(),
                    ipv4Root.getClassName(), attributes, null));
        } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException
                 | MetadataObjectNotFoundException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                    String.format("%s [Subnet] can't be created", newSubnet + ".0/24"),
                    ex.getLocalizedMessage()));
        }//we must add the new subnet into the current subnets and ips
        subnets.put(currentSubnet, new ArrayList<>());
        ips.put(currentSubnet, new ArrayList<>());
        return currentSubnet;
    }

    /**
     * Search for a given IP address got it from the ipAddrTableMIB data if it doesn't exist it will be created
     *
     * @param ipAddr   the ip address
     * @param syncMask the ip address mask from sync
     * @return an IP address created in kuwaiba
     */
    private BusinessObjectLight updateSubentsIps(String ipAddr, String syncMask) {
        //We will consider only a /24 subnet
        String[] ipAddrSegments = ipAddr.split("\\.");
        String newSubnet = ipAddrSegments[0] + "." + ipAddrSegments[1] + "." + ipAddrSegments[2];
        BusinessObjectLight currentSubnet = null;
        //we look for the subnet
        for (BusinessObjectLight subnet : subnets.keySet()) {
            if (subnet.getName().equals(newSubnet + ".0/24")) {
                currentSubnet = subnet;
                break;
            }
        }//we create the subnet if it doesn't exist
        if (currentSubnet == null)
            currentSubnet = createSubnet(newSubnet);

        //with the subnet found we must search if the Ip address exists
        List<BusinessObjectLight> currentIps = ips.get(currentSubnet);
        //we found the subnet but has no IPs, so we create the ip
        if (currentIps != null && !currentIps.isEmpty()) {
            for (BusinessObjectLight currentIpLight : currentIps) {
                if (currentIpLight.getName().equals(ipAddr)) {
                    try {//we must check the mask if the IP already exists and if its attributes are updated
                        BusinessObject currentIp = bem.getObject(Constants.CLASS_IP_ADDRESS, currentIpLight.getId());
                        String oldMask = (String) currentIp.getAttributes().get(Constants.PROPERTY_MASK);
                        if (oldMask == null || !oldMask.equals(syncMask)) {
                            currentIp.getAttributes().put(Constants.PROPERTY_MASK, syncMask);
                            HashMap<String, String> ipAttributes = (HashMap<String, String>) currentIp.getAttributes()
                                    .entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
                            bem.updateObject(currentIp.getClassName(), currentIp.getId(), ipAttributes);
                            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                    String.format("Updating the netmask for %s", currentIp),
                                    String.format("From %s to %s", oldMask, syncMask)));
                        }
                        return currentIpLight;
                    } catch (InvalidArgumentException | BusinessObjectNotFoundException |
                             MetadataObjectNotFoundException
                             | OperationNotPermittedException ex) {
                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR
                                , String.format("updating the mask of %s", currentIpLight), ex.getLocalizedMessage()));
                    }
                }
            }
        }//we create the ip address if it doesn't exist in the current subnet
        return createIp(currentSubnet, ipAddr, syncMask);
    }

    /**
     * Reads the MIB data an associate IP addresses with ports
     */
    private void readMibData() {
        List<String> ipAddresses = ipAddrTable.get("ipAdEntAddr");
        List<String> addrPortsIds = ipAddrTable.get("ipAdEntIfIndex");
        List<String> masks = ipAddrTable.get("ipAdEntNetMask");
        List<String> ifportIds = ifXTable.get("instance");
        List<String> portNames = ifXTable.get("ifName");
        List<String> servicesNames = ifXTable.get("ifAlias");
        //read every port from the mib
        for (int i = 0; i < addrPortsIds.size(); i++) {
            String portId = addrPortsIds.get(i);
            String ipAddress = ipAddresses.get(i); //get the ipAddr related in the mib data
            String mask = masks.get(i);
            //We search for the ip address
            BusinessObjectLight currentIpAddress = updateSubentsIps(ipAddress, mask);
            if (currentIpAddress != null) {
                for (int j = 0; j < ifportIds.size(); j++) { //we read the list of interfaces from ifXmib
                    if (ifportIds.get(j).equals(portId)) {
                        String portName = portNames.get(j);
                        String serviceName = servicesNames.get(j);

                        BusinessObjectLight currentPort = searchInCurrentStructure(portName);
                        if (currentPort != null) {
                            List<BusinessObjectLight> currentRelatedIPAddresses;
                            try {
                                currentRelatedIPAddresses = bem.getSpecialAttribute(
                                        currentPort.getClassName(),
                                        currentPort.getId(), RELATIONSHIP_IPAMHASADDRESS);
                                //We check if the interface is already related with the ip
                                boolean alreadyRelated = false;
                                //We also relate the ipAddr with the service
                                if (!serviceName.isEmpty())
                                    checkServices(serviceName, currentIpAddress.getId(), currentIpAddress.getName());
                                for (BusinessObjectLight currentRelatedIPAddress : currentRelatedIPAddresses) {
                                    if (currentRelatedIPAddress.getName().equals(currentIpAddress.getName())) {
                                        alreadyRelated = true;
                                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION, "Relating interface with IP address",
                                                String.format("%s and %s already related", currentRelatedIPAddress, currentPort)));
                                        break;
                                    }
                                }//If not related, we related interface with the ip
                                if (!alreadyRelated) {
                                    bem.createSpecialRelationship(currentPort.getClassName(), currentPort.getId(),
                                            currentIpAddress.getClassName(), currentIpAddress.getId(), RELATIONSHIP_IPAMHASADDRESS
                                            , true);

                                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "Relating interface with IP address",
                                            String.format("%s and %s were related successfully ", currentIpAddress, currentPort)));
                                }

                            } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException |
                                     OperationNotPermittedException | InvalidArgumentException ex) {
                                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                                        String.format("trying to relate %s with %s", currentIpAddress, currentPort),
                                        ex.getLocalizedMessage()));
                            }
                        } else
                            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                                    "Search in the current structure",
                                    String.format("%s not found ", portName)));
                    }
                }
            }
        }
    }

    /**
     * Reads the device's current structure (ports, and logical ports)
     *
     * @param children     a given set of children
     * @param childrenType 1 child, 2 special children
     * @throws MetadataObjectNotFoundException can not find the class name of the device related with the data source configuration.
     * @throws BusinessObjectNotFoundException the requested object(electrical, sftp, optical, virtual port or mpls tunnel) can't be found
     */
    private void readCurrentStructure(List<BusinessObjectLight> children, int childrenType)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        for (BusinessObjectLight child : children) {
            if (child.getClassName().equals(Constants.CLASS_ELECTRICALPORT) || child.getClassName().equals(Constants.CLASS_SFPPORT) || child.getClassName().contains(Constants.CLASS_OPTICALPORT))
                currentPorts.add(child);
            else if (child.getClassName().equals(Constants.CLASS_VIRTUALPORT) || child.getClassName().equals(Constants.CLASS_MPLSTUNNEL) ||
                    child.getClassName().equals(Constants.CLASS_BRIDGEDOMAININTERFACE) || child.getClassName().equals(Constants.CLASS_SERVICE_INSTANCE))
                currentVirtualPorts.add(child);

            if (childrenType == 1)
                readCurrentStructure(bem.getObjectChildren(child.getClassName(), child.getId(), -1), 1);
            else if (childrenType == 2)
                readCurrentStructure(bem.getObjectSpecialChildren(child.getClassName(), child.getId()), 2);
        }
    }

    /**
     * Reads the current folders in the IPAM
     *
     * @param folders a given name for port, virtual port or MPLS Tunnel
     */
    private void readcurrentFolder(List<InventoryObjectPool> folders)
            throws ApplicationObjectNotFoundException,
            MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        for (InventoryObjectPool folder : folders) {
            if (!folders.isEmpty())
                readcurrentFolder(bem.getPoolsInPool(folder.getId(), folder.getClassName()));
            readCurrentSubnets(folder);
        }
    }

    /**
     * Gets the subnets of a given folder from the IPAM module
     *
     * @param folder a given folder from the IPAM
     * @throws ApplicationObjectNotFoundException can not find the class name of the device related with the data source configuration.
     * @throws MetadataObjectNotFoundException    can not find the class name of the device related with the data source configuration.
     * @throws BusinessObjectNotFoundException    the requested object(subnet, ips, folder) can't be found
     */
    private void readCurrentSubnets(Pool folder)
            throws ApplicationObjectNotFoundException,
            MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        //we read the subnets of the folder
        List<BusinessObjectLight> subnetsInFolder = bem.getPoolItems(folder.getId(), -1);
        for (BusinessObjectLight subnet : subnetsInFolder) {
            //we save the subnet
            if (!subnets.containsKey(subnet))
                subnets.put(subnet, new ArrayList<>());
            if (!ips.containsKey(subnet))
                ips.put(subnet, new ArrayList<>());
            if (!subnetsInFolder.isEmpty())//we get the subnets inside folders
                readCurrentSubnetChildren(subnet);
        }
    }

    /**
     * Reads recursively the subnets its sub-subnets and its IPs addresses
     *
     * @param subnet a given subnet
     * @throws MetadataObjectNotFoundException can not find the class name of the device related with the data source configuration.
     * @throws BusinessObjectNotFoundException the requested object(subnet, ips, folder) can't be found
     * @throws InvalidArgumentException        if some element of the list of syncDataSourceConfiguration has more paramNames than paramValues
     */
    private void readCurrentSubnetChildren(BusinessObjectLight subnet)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        //we get the ips and the subnets inside subents
        List<BusinessObjectLight> subnetChildren = bem.getObjectSpecialChildren(subnet.getClassName(), subnet.getId());
        for (BusinessObjectLight subnetChild : subnetChildren) {
            if (subnetChild.getClassName().equals(Constants.CLASS_SUBNET_IPV4) ||
                    subnetChild.getClassName().equals(Constants.CLASS_SUBNET_IPV6))
                subnets.get(subnet).add(subnetChild);
            else
                ips.get(subnet).add(subnetChild);

            if (!subnetChildren.isEmpty())
                readCurrentSubnetChildren(subnetChild);
        }
    }

    /**
     * Checks if a given port exists in the current structure
     *
     * @param ifName a given name for port, virtual port or MPLS Tunnel
     * @return the object, null doesn't exist in the current structure
     */
    private BusinessObjectLight searchInCurrentStructure(String ifName) {
        for (BusinessObjectLight currentPort : currentPorts) {
            if (currentPort.getName().toLowerCase().equals(SyncUtil.normalizePortName(ifName)))
                return currentPort;
        }

        for (BusinessObjectLight currentVirtualPort : currentVirtualPorts) {
            String portName;
            if (ifName.toLowerCase().contains(".si"))
                portName = ifName.split("\\.")[2];
            else if (ifName.toLowerCase().contains(".") && ifName.split("\\.").length == 2)
                portName = ifName.split("\\.")[1];
            else
                portName = SyncUtil.normalizePortName(ifName);

            if (currentVirtualPort.getName().toLowerCase().equals(portName))
                return currentVirtualPort;
        }
        return null;
    }

    /**
     * Checks if a given service name exists in kuwaiba in order to
     * associate the resource read it form the if-mib
     *
     * @param serviceName the service read it forms the  if-mib
     * @param ipAddrId    the ip address id of the resource created
     */
    private void checkServices(String serviceName, String ipAddrId, String ipAddr) {
        try {
            List<BusinessObjectLight> servicesCreatedInKuwaiba = new ArrayList<>();
            //We get the services created in kuwaiba
            List<InventoryObjectPool> serviceRoot = bem.getRootPools(Constants.CLASS_GENERICCUSTOMER, 2, false);
            for (Pool customerPool : serviceRoot) {
                //TelecoOperators
                List<BusinessObjectLight> poolItems = bem.getPoolItems(customerPool.getId(), -1);
                for (BusinessObjectLight telecoOperator : poolItems) {
                    List<InventoryObjectPool> poolsInObject = bem.getPoolsInObject(telecoOperator.getClassName()
                            , telecoOperator.getId(), "GenericService");
                    //Service Pool
                    for (InventoryObjectPool servicePool : poolsInObject) {
                        List<BusinessObjectLight> actualServices = bem.getPoolItems(servicePool.getId(), -1);
                        servicesCreatedInKuwaiba.addAll(actualServices);
                    }
                }
            }
            boolean related = false;
            //Now we check the resources with the given serviceName or ifAlias
            for (BusinessObjectLight currentService : servicesCreatedInKuwaiba) {
                //The service is already created in kuwaiba
                if (!currentService.getName().isEmpty() &&
                        (serviceName.equals(currentService.getName()) || serviceName.toLowerCase().contains(currentService.getName().toLowerCase()))) {
                    List<BusinessObjectLight> serviceResources = bem.getSpecialAttribute(currentService.getClassName()
                            , currentService.getId(), "uses");
                    for (BusinessObjectLight resource : serviceResources) {
                        if (resource.getId() != null && resource.getId().equals(ipAddrId)) { //The port is already a resource of the service
                            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION,
                                    "Searching service",
                                    String.format("The service: %s is related with the ip: %s ", serviceName, ipAddr)));
                            related = true;
                            break;
                        }
                    }//end for search for ip in the resources
                    if (!related) {
                        bem.createSpecialRelationship(currentService.getClassName(), currentService.getId()
                                , Constants.CLASS_IP_ADDRESS, ipAddrId, "uses", true);
                        related = true;
                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                "Searching service",
                                String.format("The service: %s was related with the ip: %s ", serviceName, ipAddr)));
                    }
                }
            }//end for
            if (!related)
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_WARNING,
                        "Searching service", String.format("The service: %s Not found, the ip: %s will not be related"
                        , serviceName, ipAddr)));

        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException
                 | ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                    String.format("Serching service %s, related with ip: %s ", serviceName, ipAddr),
                    String.format("due to: %s ", ex.getLocalizedMessage())));
        }
    }
}

