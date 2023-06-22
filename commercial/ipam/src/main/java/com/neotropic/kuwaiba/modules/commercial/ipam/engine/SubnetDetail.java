/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ipam.engine;

import java.util.List;

/**
 * Represents an IPv4 or IPv6 subnet it also used to holds the subnet details 
 * after subneting calculation with the ipam engine, details like range, 
 * number of hosts, etc
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SubnetDetail {
    /**
     * Subnet id
     */
    private String subnetId;
    /**
     * IP address in cidr format ip address / mask bits
     */
    private String cidr;
    /**
     * the ip address of the whished subnet
     */
    private String ipAddress;
    /**
     * The mask bits of the subnet
     */
    private int maskBits;
    /**
     * Mask of the subnet in binary
     */
    private List<List<String>> binaryMask;
    /**
     * Mask of the subnet
     */
    private List<String> mask;
    /**
     * the possible number of hosts
     */
    private int numberOfHosts;
    /**
     * The network ip address of the subnet
     */
    private String networkIpAddr;
    /**
     * the boradcast ip address of the subnet
     */
    private String broadCastIpAddr;
    /**
     * The ip address version 4 or 6
     */
    private int ipAddrV;

    public SubnetDetail(String cidr) {
        this.cidr = cidr;
    }
    
    public SubnetDetail(String subnetId, String cidr) {
        this.subnetId = subnetId;
        this.cidr = cidr;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getMaskBits() {
        return maskBits;
    }

    public void setMaskBits(int maskBits) {
        this.maskBits = maskBits;
    }

    public int getNumberOfHosts() {
        return numberOfHosts;
    }
   
    public void setNumberOfHosts(int numberOfHosts) {
        this.numberOfHosts = numberOfHosts;
    }

    public String getNetworkIpAddr() {
        return networkIpAddr;
    }

    public void setNetworkIpAddr(String networkIpAddr) {
        this.networkIpAddr = networkIpAddr;
    }

    public String getBroadCastIpAddr() {
        return broadCastIpAddr;
    }

    public void setBroadCastIpAddr(String broadCastIpAddr) {
        this.broadCastIpAddr = broadCastIpAddr;
    }

    public String getRange() {
        return String.format("%s - %s", getNetworkIpAddr(), getBroadCastIpAddr());
    }

    public int getIpAddrV() {
        return ipAddrV;
    }

    public void setIpAddrV(int ipAddrV) {
        this.ipAddrV = ipAddrV;
    }

    public List<List<String>> getBinaryMask() {
        return binaryMask;
    }

    public void setBinaryMask(List<List<String>> binaryMask) {
        this.binaryMask = binaryMask;
    }

    public String getMask() {
        if(ipAddrV == 4)
            return String.join(". ", mask);
        return "";
    }

    public void setMask(List<String> mask) {
        this.mask = mask;
    }
}
