/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.ipAddr
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ipam.visual;

import com.neotropic.kuwaiba.modules.commercial.ipam.engine.IpamEngine;
import com.neotropic.kuwaiba.modules.commercial.ipam.engine.SubnetDetail;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.util.visual.general.FormatedBusinessObjectDiv;

/**
 * A graphical square used to contains numbers used in IPAM to show al the 
 * numbers of the last segment, design to be used also with evlans/cvlans
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class DivIpAddr extends FormatedBusinessObjectDiv{
    /**
     * The free style name for the ip address
     */
    public static final String STATE_FREE = "free";
    /**
     * The busy style name for the ip address
     */
    public static final String STATE_BUSY = "busy";
    /**
     * The busy style name for the ip address
     */
    public static final String STATE_RESERVED = "reserved";
    /**
     * The busy style name for the ip address
     */
    public static final String IS_MANGEMENT = "mngmnt";
    /**
     * Possible IP address in a subnet but not yet created in kuwaiba
     */
    public static final String STATE_NOT_CREATED = "not-created";
    /**
     * The parent subnet id of the IP address
     */
    private String subnetId;
    /**
     * The IP address id if it was created in kuwaiba
     */
    private String ipAddrId;
    /**
     * Common subnet segment
     */
    private String subnetSegment;
    /**
     * Subnet mask
     */
    private final String mask;
    /**
     * Subnet's network IP address
     */
    private String networkIpAddr;
    /**
     * The last segment of the subnet (when the subnet has only 254 hosts)
     */
    private String ipAddr;
    /**
     * The last added state to the div ip
     */
    private String oldState;
    /**
     * The divIp style
     */
    private String state;
    /**
     * Subnet parent class name
     */
    private String parentClassName;
    
    public DivIpAddr(BusinessObjectLight object, SubnetDetail subnetDetail, String ipAddr, String state) {
        super(object);
        this.subnetId = subnetDetail.getSubnetId();
        this.networkIpAddr = subnetDetail.getNetworkIpAddr();
        this.ipAddrId = object != null ? object.getId() : null;
        this.ipAddr = ipAddr;
        this.state = state;
        this.oldState = state;
        this.mask = subnetDetail.getMask();
        this.parentClassName = subnetDetail.getIpAddrV() == 4 ? Constants.CLASS_SUBNET_IPV4 : Constants.CLASS_SUBNET_IPV6;
        this.addClassName(state);
        setText("." + ipAddr); //we must add the dot
        
        //first segment
        if(subnetDetail.getMaskBits() <= 8)
            subnetSegment = String.format("%s."
                    , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 0)); 

        else if(subnetDetail.getMaskBits() > 8 && subnetDetail.getMaskBits() <= 16)
            subnetSegment = String.format("%s.%s."
                    , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 0) 
                    , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 1)); 

        else if(subnetDetail.getMaskBits() > 16 && subnetDetail.getMaskBits() <= 24)
            subnetSegment = String.format("%s.%s.%s."
                    , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 0) 
                    , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 1)
                    , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 2)); 

        else if(subnetDetail.getMaskBits() > 24 && subnetDetail.getMaskBits() <= 32)
            subnetSegment = String.format("%s.%s.%s."
                    , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 0) 
                    , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 1)
                    , IpamEngine.getIpv4Segment(subnetDetail.getNetworkIpAddr(), 2)); 
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getNetworkIpAddr() {
        return networkIpAddr;
    }

    public void setNetworkIpAddr(String networkIpAddr) {
        this.networkIpAddr = networkIpAddr;
    }

    public String getIpAddrId() {
        return ipAddrId;
    }

    public void setIpAddrId(String ipAddrId) {
        this.ipAddrId = ipAddrId;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getState() {
        return state;
    }

    public String getOldState() {
        return oldState;
    }

    public void setOldState(String oldState) {
        this.oldState = oldState;
    }

    public String getSubnetSegment() {
        return subnetSegment;
    }

    public String getMask() {
        return mask;
    }

    public String getParentClassName() {
        return parentClassName;
    }

    public void setParentClassName(String parentClassName) {
        this.parentClassName = parentClassName;
    }

    /**
     * Removes the old state from the div amd sets the new one
     * @param state the style for the div
     */
    public void setState(String state) {
        this.removeClassName(oldState);
        this.addClassName(state);
        this.state = state;
        this.oldState = state;
    }
}
