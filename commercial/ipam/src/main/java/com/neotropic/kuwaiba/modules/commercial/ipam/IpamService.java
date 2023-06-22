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

import com.neotropic.kuwaiba.modules.commercial.ipam.engine.IpamEngine;
import static com.neotropic.kuwaiba.modules.commercial.ipam.IpamModule.RELATIONSHIP_IPAMBELONGSTOVLAN;
import static com.neotropic.kuwaiba.modules.commercial.ipam.IpamModule.RELATIONSHIP_IPAMBELONGSTOVRFINSTACE;
import static com.neotropic.kuwaiba.modules.commercial.ipam.IpamModule.RELATIONSHIP_IPAMHASADDRESS;
import static com.neotropic.kuwaiba.modules.commercial.ipam.IpamModule.RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE;
import com.neotropic.kuwaiba.modules.commercial.ipam.engine.SubnetDetail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ArraySizeMismatchException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The logic behind the IPAM module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class IpamService {
    /**
     * default mask for individual IP addresses created in folders
     */
    public static final String DEFAULT_MASK = "255.255.255.255";
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * default ipv4 root
     */
    private InventoryObjectPool ipv4Root;
    /**
     *  default ipv6
     */
    private InventoryObjectPool ipv6Root;
    
    /**
     * Get the default pool nodes for IPv4 and IPv6 subnets
     * @return default pool for IPv4 and IPv6
     * @throws NotAuthorizedException 
     */
    private List<InventoryObjectPool> getDefaultIPAMRootNodes() throws NotAuthorizedException, MetadataObjectNotFoundException, InvalidArgumentException{
        List<InventoryObjectPool> ipv4RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        List<InventoryObjectPool> ipv6RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV6, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        
        List<InventoryObjectPool> rootSubnetPools = new ArrayList<>();
        if(ipv4RootPools.isEmpty() || ipv6RootPools.isEmpty()){
            createRootNodes();
            ipv4RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
            ipv6RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV6, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        }
        
        if(!ipv4RootPools.isEmpty()){
            ipv4Root = ipv4RootPools.get(0);
            //for display purposes, we wrap the name to romeve the word root and make it friendly
            ipv4Root.setName("IPv4"); 
        }
        if(!ipv6RootPools.isEmpty()){
            ipv6Root = ipv6RootPools.get(0);
            //for display purposes, we wrap the name to romeve the word root and make it friendly
            ipv6Root.setName("IPv6");
        }
   
        rootSubnetPools.add(ipv4Root);
        rootSubnetPools.add(ipv6Root);
        
        return rootSubnetPools;
    }

    public InventoryObjectPool getIpv4Root() {
        return ipv4Root;
    }

    public InventoryObjectPool getIpv6Root() {
        return ipv6Root;
    }
    
    /**
     * Create the IPv4 and IPv6 default nodes if they don't exist.
     * @throws MetadataObjectNotFoundException If the class IPAddress don't exist
     * @throws NotAuthorizedException If the user is not authorized to create pool nodes
     */
    private void createRootNodes() throws MetadataObjectNotFoundException, NotAuthorizedException{
        aem.createRootPool(Constants.NODE_IPV6ROOT, Constants.NODE_IPV6ROOT, 
                Constants.CLASS_SUBNET_IPV6, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
        aem.createRootPool(Constants.NODE_IPV4ROOT, Constants.NODE_IPV4ROOT, 
                Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
    }
    
    /**
     * Creates a pool(folder) of subnets if the parentId is -1 the pool will be created 
     * in the default root for pools of subnets
     * @param parentId the given parent id if the id is -1 it means the parent 
     * could be one of the default root nodes.
     * @param subnetPoolName subnet pool name
     * @param subnetPoolDescription subnet pool description
     * @param className if is a IPv4 subnet or if is a IPv6 subnet
     * @return the id of the created new subnet pool
     * @throws InvalidArgumentException if something goes wrong and can't reach the backend
     * @throws MetadataObjectNotFoundException If the class IPv4 o IPv6 doesn't exists
     * @throws NotAuthorizedException If the user is not authorized to create pool nodes
     * @throws ApplicationObjectNotFoundException if the IPAM root nodes doesn't exists
     */
    public String createFolder(String parentId, String subnetPoolName, 
            String subnetPoolDescription, String className) throws InvalidArgumentException, 
            MetadataObjectNotFoundException, NotAuthorizedException, ApplicationObjectNotFoundException 
    {
        if (aem == null)
           throw new InvalidArgumentException("Can't reach the backend. Contact your administrator");
        return aem.createPoolInPool(parentId, subnetPoolName, subnetPoolDescription, Constants.CLASS_GENERICADDRESS, 3);
    }
    
    /**
     * Retrieves a subnet by its className and id
     * @param className if the subnet is IPv4 or IPv6
     * @param oid  the subnet id
     * @return a business object that represents the subnet, note: the name of the subnet is in CIDR format networkAddress/mask bits
     * @throws MetadataObjectNotFoundException If the class IPv4 o IPv6 can't be find
     * @throws BusinessObjectNotFoundException if the requested object(subnet) can't be found
     * @throws InvalidArgumentException if the requested object(subnet) can't be found
     * @throws NotAuthorizedException If the user is not authorized to get subnets
     */
    public BusinessObject getSubnet(String className, String oid) throws MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, InvalidArgumentException, NotAuthorizedException
    {
        return bem.getObject(className, oid);
        
    }
    
    /**
     * Retrieves a subnet pool by its id
     * @param oid the subnet id
     * @return a subnet pool
     * @throws ApplicationObjectNotFoundException if the subnet pool can't be found
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException It the subnet pool does not have uuid
     */
    public InventoryObjectPool getFolder(String oid) throws NotAuthorizedException, InvalidArgumentException, 
            ApplicationObjectNotFoundException
    {
        return bem.getPool(oid);
    }
    
    /**
     * Returns the folders
     * Retrieves a pool of subnets pools from a pool of subnets or from the root
     * @param parentId parent id
     * @param className if is a IPv4 subnet or if is a IPv6 subnet
     * @param page number of results to skip
     * @param limit the number of results per page
     * @return a list of subnet pools
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws ApplicationObjectNotFoundException if can't get the pools of a subnet pool
     * @throws MetadataObjectNotFoundException if there are not IPAM root nodes
     * @throws InvalidArgumentException If the parent does not have uuid
     * public List<Pool> getFoldersInFolder(String parentId, String className, int page, int limit) 
     */
    public List<InventoryObjectPool> getFoldersInFolder(String parentId, String className, int page, int limit)  
            throws NotAuthorizedException, ApplicationObjectNotFoundException, InvalidArgumentException, 
            MetadataObjectNotFoundException {
        if("-1".equals(parentId) && className == null)
            return getDefaultIPAMRootNodes();
        
        return bem.getPoolsInPool(parentId, Constants.CLASS_GENERICADDRESS);
    }
    
     /**
     * Returns the count of subnets folders
     * Retrieves a pool of subnets pools from a pool of subnets or from the root
     * @param parentId parent id
     * @param className if is a IPv4 subnet or if is a IPv6 subnet
     * @return a list of subnet pools
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws ApplicationObjectNotFoundException if can't get the pools of a subnet pool
     * @throws InvalidArgumentException If the parent does not have uuid
     */
    public long getFolderInFolderCount(String parentId, String className) 
            throws NotAuthorizedException, ApplicationObjectNotFoundException, InvalidArgumentException {
        if("-1".equals(parentId) && className == null)
            return 2;
            
        return bem.getPoolsInPoolCount(parentId, Constants.CLASS_GENERICADDRESS);
    }
    
    /**
     * Get a set of subnets from a pool folder
     * @param subnetPoolId subnet pool id
     * @param classNameToFilter the className of the wished folder objects, subents or ip address 
     * @param page the page or the number of elements to skip, to no pagination -1.
     * @param limit the limit of elements by page, no limit -1
     * @return a list of subnets
     * @throws ApplicationObjectNotFoundException if the given subnet pool id is not valid
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the subnet pool does not have uuid
     * public List<BusinessObjectLight> getFolderItems(String subnetPoolId, HashMap<String,String> filters, int page, int limit) 
     */
    public List<BusinessObjectLight> getFolderItems(String subnetPoolId, String classNameToFilter, int page, int limit)
            throws ApplicationObjectNotFoundException, NotAuthorizedException, InvalidArgumentException
    {
        return bem.getPoolItemsByClassName(subnetPoolId, classNameToFilter, page, limit);
    }
   
    /**
     * Retrieve the count of subnets from a pool of subnets
     * @param subnetPoolId subnet pool id
     * @param className the classname of the objects in pool
     * @return a list of subnets
     * @throws ApplicationObjectNotFoundException if the given subnet pool id is not valid
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the subnet pool does not have uuid
     */
    public long getFolderItemsCount(String subnetPoolId, String className) throws ApplicationObjectNotFoundException, 
            NotAuthorizedException, InvalidArgumentException
    {
        return bem.getPoolItemsCount(subnetPoolId, className);
    }
        
    /**
     * Retrieves all the Subnets created inside a subnet
     * @param id a given subnet id
     * @param className If the subnet has IPv4 or an IPv6 addresses
    * @param page the results page or number of elements skipped
    * @param limit limit of elements per page
     * @return A list of subnets for the given subnet id
     * @throws MetadataObjectNotFoundException If can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException If the object(subnet) could not be found.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the subnet does not have uuid
     */ 
    public List<BusinessObjectLight> getSubnetsInSubnet(String id, String className, int page, int limit) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException,
            NotAuthorizedException, InvalidArgumentException
    {
        List<String> classNamesToFilter = new ArrayList<>();
        classNamesToFilter.add(className);
                        
        List<BusinessObjectLight> children = bem.getObjectSpecialChildrenWithFilters(className, id, classNamesToFilter, page, limit);
        List<BusinessObjectLight> subnets = new ArrayList<>();
        for (BusinessObjectLight child : children) {
            if(child.getClassName().equals(Constants.CLASS_SUBNET_IPV4) || 
                    child.getClassName().equals(Constants.CLASS_SUBNET_IPV6))
            subnets.add(child);
        }
        subnets.sort(new SubnetComparator());
        return subnets;
    }
    
    /**
     * Retrieves all the Subnets created inside a subnet
     * @param id a given subnet id
     * @param className If the subnet has IPv4 or an IPv6 addresses
     * @return A list of subnets for the given subnet id
     * @throws MetadataObjectNotFoundException If can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException If the object(subnet) could not be found.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the subnet does not have uuid
     */
    public long getSubnetsInSubnetCount(String id, String className) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException,
            NotAuthorizedException, InvalidArgumentException
    {
        return bem.getObjectSpecialChildrenCount(className, id, className);
    }
    
    /**
     * create a subnet
     * @param parentId subnet pool id
     * @param className subnet class name
     * @param attributes subnet attributes, networkIP, broadcastIP, hosts
     * @return new subnet id
     * @throws ApplicationObjectNotFoundException can't find the parent(a subnet pool) to create the subnet
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws MetadataObjectNotFoundException If can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException  If the update can't be performed due to a format issue
     */
    public String createSubnet(String parentId, String className, HashMap<String, String> attributes) throws InvalidArgumentException, 
            ArraySizeMismatchException, NotAuthorizedException,  
            MetadataObjectNotFoundException, OperationNotPermittedException, 
            BusinessObjectNotFoundException, ApplicationObjectNotFoundException
    {
        try { // we creeate a subnet insde a folder
            getOverlapedSubnets(attributes.get(Constants.PROPERTY_NAME)
                , className
                , attributes.get(Constants.PROPERTY_NETWORK_IP)
                , attributes.get(Constants.PROPERTY_BROADCAST_IP));
                
            return bem.createPoolItem(parentId, className, attributes, null);
        } catch (ApplicationObjectNotFoundException ex) {
            return bem.createSpecialObject(className, className, parentId, attributes, null);
        }
    }
    
    /**
     * Creates several subnets in a subnet
     * @param parentId subnet pool id
     * @param className subnet class name
     * @param subnetsAttributes all the attributes of the subnets to be created: networkIP, broadcastIP, hosts, etc
     * @return a list of the new subnet ids
     * @throws ApplicationObjectNotFoundException can't find the parent(a subnet pool) to create the subnet
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws MetadataObjectNotFoundException If can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException  If the update can't be performed due to a format issue
     */
    public List<String> createSubnets(String parentId, String className, List<HashMap<String, String>> subnetsAttributes) throws InvalidArgumentException, 
            ArraySizeMismatchException, NotAuthorizedException,  
            MetadataObjectNotFoundException, OperationNotPermittedException, 
            BusinessObjectNotFoundException, ApplicationObjectNotFoundException
    {
        for (HashMap<String, String> attributes : subnetsAttributes){
            getOverlapedSubnets(attributes.get(Constants.PROPERTY_NAME)
                    , className
                    , attributes.get(Constants.PROPERTY_NETWORK_IP)
                    , attributes.get(Constants.PROPERTY_BROADCAST_IP));
        }
        
        List<String> createdIds = new ArrayList<>();
        for (HashMap<String, String> subnetAttributes : subnetsAttributes)
            createdIds.add(bem.createSpecialObject(className, className, parentId, subnetAttributes, null));
        
        return createdIds;
    }
    
   
    
    /**
     * Checks if a a list of subnet or IP addresses names exist in the whole inventory
     * it will return a list of all the existing subnet found
     * @param ipAddresses subnets name should be in cidr format, or ip addresses
     * @param className class of subnets ipv4 or ipv 6
     * @return a list of found subnets
     * @throws InvalidArgumentException 
     */
    public List<BusinessObjectLight> existInInventory(List<String> ipAddresses, String className)
            throws InvalidArgumentException
    {
        return bem.getObjectsByNameAndClassName(ipAddresses, -1, -1, className);
    }
   
    /**
     * Deletes a subnet
     * @param subnetIds subnet ids
     * @param className subnets class name
     * @param releaseRelationships release any relationship 
     * @throws BusinessObjectNotFoundException If the requested subnet can't be found
     * @throws MetadataObjectNotFoundException If the requested object class can't be found (problems with IPv4 or IPV6 classes)
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked or it has relationships and releaseRelationships is false
     * @throws InvalidArgumentException If it was not possible to release the possible unique attributes
     */
    public void deleteSubnets(String className, List<String> subnetIds, boolean releaseRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, 
            OperationNotPermittedException, InvalidArgumentException {
        HashMap<String, List<String>> objectsToBeDeleted = new HashMap<>();
        objectsToBeDeleted.put(className, subnetIds);
        bem.deleteObjects(objectsToBeDeleted, releaseRelationships);
    }
    
    /**
     * deletes a subnet Pool
     * @param subnetPoolsId subnet ids 
     * @throws OperationNotPermittedException If any of the objects in the pool can not be deleted because it's not a business related instance (it's more a security restriction)
     * @throws ApplicationObjectNotFoundException  If the subnet pool can't be found
     */
    public void deleteSubnetPools(String[] subnetPoolsId) 
            throws OperationNotPermittedException, 
            ApplicationObjectNotFoundException
    {
        aem.deletePools(subnetPoolsId);
    }

    /**
     * creates an IP address inside a subnet
     * @param parentSubnetId subnet Id
     * @param parentSubnetClassName if is a IPv4 or an IPv6 subnet
     * @param attributes ip Address attributes, name description
     * @return IP address id
     * @throws ApplicationObjectNotFoundException Can't find the parent(a subnet) to create the IP address
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws MetadataObjectNotFoundException if can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException Thrown if the parent(the subnet) id is not found
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     */
    public String addIPAddressToSubnet(String parentSubnetId, String parentSubnetClassName, HashMap<String, String> attributes) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, 
            ArraySizeMismatchException, NotAuthorizedException, MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, OperationNotPermittedException
    {
            List<BusinessObjectLight> exists = existInInventory(
                new ArrayList<>(Arrays.asList(attributes.get(Constants.PROPERTY_NAME)))
                 , Constants.CLASS_IP_ADDRESS);
            if(!exists.isEmpty())
                throw new InvalidArgumentException(ts.getTranslatedString("module.ipam.actions.add-ip-addr.error.already-exists"));

            //TODO look for a proper way to set this property or any new attribute added to the ip address if is a listype
            String reservedId = null;
            if(attributes.containsKey(Constants.PROPERTY_STATE)){
                List<BusinessObjectLight> listTypeItems = aem.getListTypeItems("OperationalState");
                for (BusinessObjectLight item : listTypeItems) {
                    if(item.getName().toLowerCase().equals("reserved"))
                        reservedId = item.getId();
                }
            }
            if(reservedId == null)
                attributes.remove(Constants.PROPERTY_STATE);
            else
                attributes.put(Constants.PROPERTY_STATE, reservedId);

        return bem.createSpecialObject(Constants.CLASS_IP_ADDRESS, parentSubnetClassName, parentSubnetId, attributes, null);
        }
    
    /**
     * Creates multiple an IP addresses inside a subnet
     * @param parentSubnetId subnet Id
     * @param parentSubnetClassName if is a IPv4 or an IPv6 subnet
     * @param ipAddresesAttributes ip Address attributes, name description
     * @return IP Address id
     * @throws ApplicationObjectNotFoundException Can't find the parent(a subnet) to create the IP address
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws MetadataObjectNotFoundException if can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException Thrown if the parent(the subnet) id is not found
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     */
    public String addIPAddressesToSubnet(String parentSubnetId, String parentSubnetClassName, List<HashMap<String, String>> ipAddresesAttributes) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, 
            ArraySizeMismatchException, NotAuthorizedException, MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, OperationNotPermittedException
    {
        List<String> createdIpAddresIds = new ArrayList<>();
        for (HashMap<String, String> attributes : ipAddresesAttributes) {
            List<BusinessObjectLight> exists = existInInventory(
                new ArrayList<>(Arrays.asList(attributes.get(Constants.PROPERTY_NAME)))
                 , Constants.CLASS_IP_ADDRESS);
            if(!exists.isEmpty())
                throw new InvalidArgumentException(ts.getTranslatedString("module.ipam.actions.add-ip-addr.error.already-exists"));

            //TODO look for a proper way to set this property or any new attribute added to the ip address if is a listype
            String reservedId = null;
            if(attributes.containsKey(Constants.PROPERTY_STATE)){
                List<BusinessObjectLight> listTypeItems = aem.getListTypeItems("OperationalState");
                for (BusinessObjectLight item : listTypeItems) {
                    if(item.getName().toLowerCase().equals("reserved"))
                        reservedId = item.getId();
                }
            }
            if(reservedId == null)
                attributes.remove(Constants.PROPERTY_STATE);
            else
                attributes.put(Constants.PROPERTY_STATE, reservedId);

            createdIpAddresIds.add(bem.createSpecialObject(Constants.CLASS_IP_ADDRESS, parentSubnetClassName, parentSubnetId, attributes, null));
        }
        return String.join(";", createdIpAddresIds);
    }
     /**
     * Updates some attributes of the ip address, description, state are the only 
     * editable attributes of an ip address
     * @param id ip address id
     * @param attributes attribuetes to update description or state.
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws InvalidArgumentException 
     */
    public void updateIpAdress(String id, HashMap<String, String> attributes) 
            throws MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, 
            OperationNotPermittedException, InvalidArgumentException
    {
        //TODO this should be removed and look for a proper way to set this property
        String reservedId = null;
        if(attributes.containsKey(Constants.PROPERTY_STATE)){
            List<BusinessObjectLight> listTypeItems = aem.getListTypeItems("OperationalState");
            for (BusinessObjectLight item : listTypeItems) {
                if(item.getName().toLowerCase().equals("reserved"))
                    reservedId = item.getId();
            }
        }
        if(reservedId == null)
            attributes.remove(Constants.PROPERTY_STATE);
        else
            attributes.put(Constants.PROPERTY_STATE, reservedId);
        
         
        bem.updateObject(Constants.CLASS_IP_ADDRESS, id, attributes);
    }

    /**
     * Creates an IP address that has no subnet parent as direct child of a folder
     * @param parentFolderId the folder (pool) Id
     * @param attributes IP Address attributes, name description
     * @return IP address id
     * @throws ApplicationObjectNotFoundException Can't find the parent(a subnet) to create the IP address
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws MetadataObjectNotFoundException if can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException Thrown if the parent(the subnet) id is not found
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     */
    public String addIPAddressToFolder(String parentFolderId, HashMap<String, String> attributes) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, 
            ArraySizeMismatchException, NotAuthorizedException, MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, OperationNotPermittedException
    {
            List<BusinessObjectLight> exists = existInInventory(
                    new ArrayList<>(Arrays.asList(attributes.get(Constants.PROPERTY_NAME)))
                     , Constants.CLASS_IP_ADDRESS);
                if(!exists.isEmpty())
                    throw new InvalidArgumentException(ts.getTranslatedString("module.ipam.actions.add-ip-addr.error.already-exists"));

        return bem.createPoolItem(parentFolderId, Constants.CLASS_IP_ADDRESS, attributes, null);
    }

    
    /**
     * Creates multiple IP addresses that has no subnet parent as direct child of a folder
     * @param parentFolderId the folder (pool) Id
     * @param ipAddressesAttributes a list of the attribues(name, description) all IP Addreses that il be created
     * @return IP address id
     * @throws ApplicationObjectNotFoundException Can't find the parent(a subnet) to create the IP address
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws MetadataObjectNotFoundException if can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException Thrown if the parent(the subnet) id is not found
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     */
    public String addIPAddressesToFolder(String parentFolderId, List<HashMap<String, String>> ipAddressesAttributes) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, 
            ArraySizeMismatchException, NotAuthorizedException, MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, OperationNotPermittedException
    {
        List<String> createdIpAddresIds = new ArrayList<>();
        for(HashMap<String, String> attributes : ipAddressesAttributes){
            List<BusinessObjectLight> exists = existInInventory(
                    new ArrayList<>(Arrays.asList(attributes.get(Constants.PROPERTY_NAME)))
                     , Constants.CLASS_IP_ADDRESS);
                if(!exists.isEmpty())
                    throw new InvalidArgumentException(ts.getTranslatedString("module.ipam.actions.add-ip-addr.error.already-exists"));

            createdIpAddresIds.add(bem.createPoolItem(parentFolderId, Constants.CLASS_IP_ADDRESS, attributes, null));
        }
        return String.join(";", createdIpAddresIds);
    }
    
    /**
     * Removes an IP address from a subnet or folder
     * @param ipAddressesIds
     * @param releaseRelationships release existing relationships
     * @throws BusinessObjectNotFoundException If the requested IP address can't be found
     * @throws MetadataObjectNotFoundException If can't find IPv4 or IPV6 classes
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked or it has relationships and releaseRelationships is false
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the ip ids do no have uuid
     */
    public void deleteIpAddress(List<String> ipAddressesIds, boolean releaseRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, 
            OperationNotPermittedException, NotAuthorizedException, InvalidArgumentException
    {
        if(ipAddressesIds != null){
            HashMap<String, List<String>> toDelete = new HashMap<>();
            toDelete.put(Constants.CLASS_IP_ADDRESS, ipAddressesIds);
            
            bem.deleteObjects(toDelete, releaseRelationships);
        }
    }
    
    /**
     * Relates an IP address with a generic communication port
     * @param ipAddrId subnet id
     * @param portClassName Generic communications element
     * @param portId generic communications id
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found
     * @throws InvalidArgumentException If the subnet/port do not have uuid
     */
    public void relateIpAddressToNetworkInterface(String ipAddrId, String portClassName, String portId) throws BusinessObjectNotFoundException,
            OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        List<BusinessObjectLight> ipAddrs = bem.getSpecialAttribute(portClassName, portId, RELATIONSHIP_IPAMHASADDRESS);
        for(BusinessObjectLight ipAddr: ipAddrs){
            if(ipAddr.getId().equals(ipAddrId))    
                throw new InvalidArgumentException(ts.getTranslatedString("module.ipam.exception.ipaddr-alreadyrelated"));
        }
        bem.createSpecialRelationship(portClassName, portId, Constants.CLASS_IP_ADDRESS, ipAddrId, RELATIONSHIP_IPAMHASADDRESS, false);
    }
    
    /**
     * Relate a Subnet with a VLAN, this method also allow to relate VLANs to 
     * BDIs, VFRIs.
     * @param subnetId subnet id
     * @param className if the subnet has IPv4 or IPv6 addresses
     * @param vlanId VLAN id
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found
     * @throws InvalidArgumentException If the subnet/vlan do not have uuid
     */
    public void relateSubnetToVLAN(String subnetId, String className, String vlanId)
        throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException
    {
        bem.createSpecialRelationship(Constants.CLASS_VLAN, vlanId, className, subnetId, RELATIONSHIP_IPAMBELONGSTOVLAN, true);
    }
    
    /**
     * Release a relationship between a subnet and a VLAN, this method also 
     * allow to relate VLANs to BDIs, VFRIs.
     * @param vlanId the vlan Id
     * @param subnetId the subnet id
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found
     * @throws InvalidArgumentException If the subnet/vlan do not have uuid
     */
    public void releaseSubnetFromVLAN(String subnetId, String vlanId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException
    {
        bem.releaseSpecialRelationship(Constants.CLASS_VLAN, vlanId, subnetId, RELATIONSHIP_IPAMBELONGSTOVLAN);
    }
    
    /**
     * Relate a Subnet with a VRF
     * @param subnetId subnet id
     * @param className if the subnet has IPv4 or IPv6 addresses
     * @param vrfId VLAN id
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found
     * @throws InvalidArgumentException If the subnet/vrf do not have uuid
     */
    public void relateSubnetToVRF(String subnetId, String className, String vrfId)
        throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException
    {
        bem.createSpecialRelationship(Constants.CLASS_VRF_INSTANCE, vrfId, className, subnetId, RELATIONSHIP_IPAMBELONGSTOVRFINSTACE, true);
    }
    
    /**
     * Release the relationship between a GenericPort and an 
     * IP Address.
     * @param portClass GenericCommunications Element
     * @param portId GenericCommunications id
     * @param id IP address id 
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException If the port class can not be found
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the port does not have uuid
     */
    public void releaseIpAddrFromNetworkInterface(String portClass, String portId, String id)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException,
            NotAuthorizedException
    {
        bem.releaseSpecialRelationship(portClass, portId, id, RELATIONSHIP_IPAMHASADDRESS);
        
    }
    
    /**
     * Release a relationship between a subnet and a VRF
     * @param subnetId the subnet id
     * @param vrfId the VRF Id
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException If the port class can not be found
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the subnet/vrf do not have uuid
     */
    public void releaseSubnetFromVRF(String subnetId, String vrfId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException,
            NotAuthorizedException, InvalidArgumentException
    {
        bem.releaseSpecialRelationship(Constants.CLASS_VRF_INSTANCE, vrfId, subnetId, RELATIONSHIP_IPAMBELONGSTOVRFINSTACE);
    }

    /**
     * Retrieves all the IP address created in a subnet
     * @param id a given subnet id
     * @param className If the subnet has IPv4 or an IPv6 addresses
     * @return a list of IP addresses that are related to an interface
     * @throws MetadataObjectNotFoundException If can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException If the object(subnet) could not be found.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the subnet does not have uuid
     */
    public List<BusinessObjectLight> getSubnetIpAddrCreated(String id, String className, int page, int limit) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException,  
            NotAuthorizedException, InvalidArgumentException
    {
        List<String> classNamesToFilter = new ArrayList<>();
        classNamesToFilter.add(Constants.CLASS_IP_ADDRESS);
        
        List<BusinessObjectLight> usedIps = bem.getObjectSpecialChildrenWithFilters(className, id, classNamesToFilter, page, limit);
        usedIps.sort(new IPAddressComparator());
        
        return usedIps;
    }
    
    /**
     * Relates an interface with a GenericCommunicationPort
     * @param portId port id
     * @param portClassName the class name of the configuration you want to relate with
     * @param interfaceClassName the interface class name
     * @param interfaceId interface id
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the port/interface do not have uuid
     */
    public void relatePortToInterface(String portId, String portClassName, 
            String interfaceClassName, String interfaceId) throws BusinessObjectNotFoundException,
            OperationNotPermittedException, MetadataObjectNotFoundException, NotAuthorizedException, InvalidArgumentException {
        bem.createSpecialRelationship(interfaceClassName, interfaceId, portClassName, portId, RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE, true);
    }
    
    /**
     * Release the relationship between a GenericCommunicationPort and an interface
     * @param interfaceClassName interface's class
     * @param interfaceId interface id
     * @param portId port id 
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException If the port class can not be found
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the interface/port do not have uuid
     */
    public void releasePortFromInterface(String interfaceClassName, String interfaceId, String portId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException,
            NotAuthorizedException, InvalidArgumentException
    {
        bem.releaseSpecialRelationship(interfaceClassName, interfaceId, portId, RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE);
    }
   
    /**
     * Checks if the new subnet exists or overlaps with in other created subnets
     * @param cidr subnet's cidr format
     * @param className if is subnet ip v4 or ip v6
     * @param newNetworkIpAddress
     * @param newBroadcastIpAddress
     * @throws InvalidArgumentException if a subnet with the exact cidr already 
     * exists or if it overlaps with others created subnets
     */
    public void getOverlapedSubnets(String cidr, String className
            , String newNetworkIpAddress
            , String newBroadcastIpAddress) throws InvalidArgumentException
    {
        String address = IpamEngine.getPartialSubnetIpAddr(cidr
                , className.equals(Constants.CLASS_SUBNET_IPV4) ? 4 : (className.equals(Constants.CLASS_SUBNET_IPV6) ? 6 : 0));
        List<String> overlapedSubnets = new ArrayList<>();
        
        if(address != null){
            List<BusinessObjectLight> currentSubnets = 
                    bem.getSuggestedObjectsWithFilter(address
                        , -1, -1, className);

            String spliter = "\\.";
            if(className.equals(Constants.CLASS_SUBNET_IPV4)){
                long newNetworkIpAddr = Long.valueOf(newNetworkIpAddress.replaceAll(spliter, ""));
                long newBroadcastIpAddr = Long.valueOf(newBroadcastIpAddress.replaceAll(spliter, ""));

                for (BusinessObjectLight subnet : currentSubnets) {
                    if(subnet.getName().equals(cidr))
                        throw new InvalidArgumentException(ts.getTranslatedString("module.ipam.actions.add-subnet.error-subnet-exists"));

                    SubnetDetail subnetDetail = new SubnetDetail(subnet.getName());
                    IpamEngine.ipv4SubnetCalculation(subnetDetail);

                    long networkIpAddr = Long.valueOf(subnetDetail.getNetworkIpAddr().replaceAll(spliter, ""));
                    long broadcastIpAddr = Long.valueOf(subnetDetail.getBroadCastIpAddr().replaceAll(spliter, ""));

                    if(Integer.valueOf(cidr.split("/")[1]) <= subnetDetail.getMaskBits() 
                            && (newNetworkIpAddr >= networkIpAddr || newBroadcastIpAddr <= broadcastIpAddr))
                        overlapedSubnets.add(subnet.getName());
                }
            }
            else if(className.equals(Constants.CLASS_SUBNET_IPV6)){
                                
                for (BusinessObjectLight subnet : currentSubnets) {
                    if(subnet.getName().equals(cidr))
                        throw new InvalidArgumentException(ts.getTranslatedString("module.ipam.actions.add-subnet.error-subnet-exists"));

                    SubnetDetail subnetDetail = new SubnetDetail(subnet.getName());
                    IpamEngine.ipv4SubnetCalculation(subnetDetail);
                    
                    if(IpamEngine.ipv6SubnetsOvelaps(cidr, subnetDetail.getNetworkIpAddr(), subnetDetail.getBroadCastIpAddr()))
                        overlapedSubnets.add(subnet.getName());
                }
            }
        }
        if(!overlapedSubnets.isEmpty())
            throw new InvalidArgumentException(
                    String.format(ts.getTranslatedString("module.ipam.actions.add-subnet.error-subnet-overlaps")
                    , cidr, String.join(" - ", overlapedSubnets)));
        
    }
    
    /**
     * Custom comparator to sort lists of IP addresses
     */
    public class IPAddressComparator implements Comparator<BusinessObjectLight>{
        @Override
        public int compare(BusinessObjectLight ipAddrA, BusinessObjectLight ipAddrB) {
            String ipAddr1 = ipAddrA.getName();
            String ipAddr2 = ipAddrB.getName();

            if(IpamEngine.isIpv4Address(ipAddr1) && IpamEngine.isIpv4Address(ipAddr2)){
                ipAddr1 = IpamEngine.getIpv4Completed(ipAddr1).replaceAll("\\.", "");
                ipAddr2 = IpamEngine.getIpv4Completed(ipAddr2).replaceAll("\\.", "");
                return Double.valueOf(ipAddr1).compareTo(Double.valueOf(ipAddr2));
            }
            else if(IpamEngine.isIpv6Address(ipAddr1) && IpamEngine.isIpv6Address(ipAddr2)){
                String[] aIp = IpamEngine.completeIPv6(ipAddr1);
                String[] bIp = IpamEngine.completeIPv6(ipAddr2);
                if(Arrays.equals(aIp, bIp))
                    return 0;

                for(int k=0; k < 8; k++){
                    int x = Integer.parseInt(aIp[k],16);
                    int y = Integer.parseInt(bIp[k],16);
                    int compare = Integer.compare(x, y);
                    if(compare != 0)
                        return compare;
                }
            }
            return 0;    
        }
    }
    
    /**
     * Custom subnet comparator to sort subnets lists
     */
    public class SubnetComparator implements Comparator<BusinessObjectLight>{
        @Override//both subnets came in CIDR format
        public int compare(BusinessObjectLight subnetA, BusinessObjectLight subnetB) {
            String cidrSubnet1 = subnetA.getName(); //retrieves 
            String cidrSubnet2 = subnetB.getName();

            if(IpamEngine.isIpv4Address(IpamEngine.getSubnetIpAddr(cidrSubnet1)) 
                    && IpamEngine.isIpv4Address(IpamEngine.getSubnetIpAddr(cidrSubnet2)))
            {
               String subnet1 = IpamEngine.getSubnetIpAddr(cidrSubnet1).replaceAll("\\.", "");
               String subnet2 = IpamEngine.getSubnetIpAddr(cidrSubnet2).replaceAll("\\.", "");

               return Long.compare(Long.valueOf(subnet1), Long.valueOf(subnet2));
            }
            else if(IpamEngine.isIpv6Address(IpamEngine.getSubnetIpAddr(cidrSubnet1)) 
                    && IpamEngine.isIpv6Address(IpamEngine.getSubnetIpAddr(cidrSubnet2)))
            {
                String[] aSubnetSplit = IpamEngine.completeIPv6(IpamEngine.getSubnetIpAddr(cidrSubnet1));
                String[] bSubnetSplit = IpamEngine.completeIPv6(IpamEngine.getSubnetIpAddr(cidrSubnet2));
                if(Arrays.equals(aSubnetSplit, bSubnetSplit))
                    return 0;

                for(int k=0; k<8; k++){
                    int x = Integer.parseInt(aSubnetSplit[k],16);
                    int y = Integer.parseInt(bSubnetSplit[k],16);
                    int compare = Integer.compare(x, y);
                    if(compare != 0)
                        return compare;
                }
            }
            return 0;
        }
    }
    
    /**
     * Gets the number of IP addresses instantiated (created) in the subnet 
     * that has a relationship with an inventory object e.g. port
     * @param subnetId the subnet id
     * @param subnetClassName subnet class name
     * @return the number of IP addresses with relationships
     * @throws MetadataObjectNotFoundException class name of the subnet not found
     * @throws BusinessObjectNotFoundException the IP address not found
     * @throws InvalidArgumentException if son parameter is missing in the methods used
     */
    public List<BusinessObjectLight> getSubnetIpAddrsInUse(String subnetId, String subnetClassName) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException
    {
        List<BusinessObjectLight> usdedIpAddrs = new ArrayList<>();        
        List<String> classNamesToFilter = new ArrayList<>();
        classNamesToFilter.add( Constants.CLASS_IP_ADDRESS);
                
        List<BusinessObjectLight> ipAddrs = bem.getObjectSpecialChildrenWithFilters(subnetClassName, subnetId, classNamesToFilter,  -1, -1);
        for (BusinessObjectLight ip : ipAddrs) {
            HashMap<String, List<BusinessObjectLight>> rels = bem.getSpecialAttributes(Constants.CLASS_IP_ADDRESS, ip.getId(), IpamModule.RELATIONSHIP_IPAMHASADDRESS);
            if(!rels.isEmpty())
                usdedIpAddrs.add(ip);
        }
        return usdedIpAddrs;
    }
    
    /* Gets the number of IP addresses reserved
     * @param subnetId the subnet id
     * @param subnetClassName subnet class name
     * @return the number of IP addresses with relationships
     * @throws MetadataObjectNotFoundException class name of the subnet not found
     * @throws BusinessObjectNotFoundException the IP address not found
     * @throws InvalidArgumentException if son parameter is missing in the methods used
     */
    public List<BusinessObjectLight> getSubnetIpAddrsReserved(String subnetId, String subnetClassName) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException
            , InvalidArgumentException, ApplicationObjectNotFoundException
    {
        List<BusinessObjectLight> reservedIpAddrs = new ArrayList<>();
        List<String> classNamesToFilter = new ArrayList<>();
        classNamesToFilter.add( Constants.CLASS_IP_ADDRESS);

        List<BusinessObjectLight> ipAddrs = bem.getObjectSpecialChildrenWithFilters(subnetClassName, subnetId, classNamesToFilter,  -1, -1);

        for (BusinessObjectLight ip : ipAddrs) {
            String state = bem.getAttributeValueAsString(Constants.CLASS_IP_ADDRESS, ip.getId(), Constants.PROPERTY_STATE); //this is
            if(state != null && state.toLowerCase().equals("reserved")) //TODO move this hard string reserved
                reservedIpAddrs.add(ip);
        }
        return reservedIpAddrs;
    }

    public void createAllIpAddressInSubnet(){
    }

}
