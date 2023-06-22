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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;

/**
 * Make all the validations an calculate the possible subnets for IPv4 and IPv6 Addresses
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class IpamEngine {

    public static void ipv4SubnetCalculation(SubnetDetail subnetDetail) throws InvalidArgumentException{
        if(!isCIDRFormat(subnetDetail.getCidr()))
            throw new InvalidArgumentException("Invalid format");
        
        String[] ipCIDRsplited = subnetDetail.getCidr().split("/");
        subnetDetail.setIpAddress(ipCIDRsplited[0]);
        if(Integer.parseInt(ipCIDRsplited[1]) < 0 || Integer.parseInt(ipCIDRsplited[1]) > 32)
            throw new InvalidArgumentException("Mask bits should be between 1 to 32");

        subnetDetail.setIpAddrV(4);
        subnetDetail.setMaskBits(Integer.parseInt(ipCIDRsplited[1]));
        subnetDetail.setNumberOfHosts(ipv4NumberOfHosts(subnetDetail.getMaskBits()));
        subnetDetail.setBinaryMask(ipv4CreateBinaryMask(subnetDetail.getMaskBits()));
        subnetDetail.setMask(ipv4CreateMask(subnetDetail.getBinaryMask()));
        
        List<List<String>> binaryIPSegments = ipv4ParseToBinary(subnetDetail.getIpAddress());

        int segementPos = 0;
     
        List<String> decimalSegmentsNetwork = new ArrayList<>();
        List<String> decimalSegmentsBroadcast = new ArrayList<>();
        
        for (List<String> segment : binaryIPSegments) {
            
            String subnetSegmentNetwork = "";
            String subnetSegmentBroadcast = "";
            
            for (String bit : segment) {
                if(segementPos >= subnetDetail.getMaskBits()){
                    subnetSegmentNetwork += 0;
                    subnetSegmentBroadcast += 1;
                }
                else
                    subnetSegmentBroadcast = subnetSegmentNetwork += bit;
                
                segementPos++;
            }
            decimalSegmentsNetwork.add(Integer.toString(Integer.parseInt(subnetSegmentNetwork, 2)));
            decimalSegmentsBroadcast.add(Integer.toString(Integer.parseInt(subnetSegmentBroadcast, 2)));
        }
        
        subnetDetail.setNetworkIpAddr(String.join(".", decimalSegmentsNetwork));
        subnetDetail.setBroadCastIpAddr(String.join(".", decimalSegmentsBroadcast));
    }
    
    public static List<SubnetDetail> ipv4Split(String networkAddress
            , int currentMaskBits, String broadcastIpAddr, int bitsToSplit) 
            throws InvalidArgumentException
    {
        List<SubnetDetail> splitedSubnets = new ArrayList<>();
        
        int currentNumberOfHosts = ipv4NumberOfHosts(currentMaskBits);
        int splitedNumberOfHosts = ipv4NumberOfHosts(bitsToSplit);
        int totalNumberOfHosts = 0;
        
        String address = networkAddress;
        while(totalNumberOfHosts + (splitedSubnets.size() * 2) != currentNumberOfHosts + 2){
            
            List<List<String>> binaryIPSegments = ipv4ParseToBinary(address);
            SubnetDetail subnetDetail = new SubnetDetail(address + "/" + Integer.toString(bitsToSplit));
            
            int segementPos = 0;
        
            List<String> decimalSegmentsNetwork = new ArrayList<>();
            List<String> decimalSegmentsBroadcast = new ArrayList<>();
        
            //We iterate over the segments of the given binary ip address
            for (List<String> segment : binaryIPSegments) {
                String subnetSegmentNetwork = "";
                String subnetSegmentBroadcast = "";

                for (String bit : segment) {
                    if(segementPos >= bitsToSplit){
                        subnetSegmentNetwork += 0;
                        subnetSegmentBroadcast += 1;
                    }
                    else
                        subnetSegmentBroadcast = subnetSegmentNetwork += bit;

                    segementPos++;
                }
                decimalSegmentsNetwork.add(Integer.toString(Integer.parseInt(subnetSegmentNetwork, 2)));
                decimalSegmentsBroadcast.add(Integer.toString(Integer.parseInt(subnetSegmentBroadcast, 2)));
            }//end for
            
            subnetDetail.setCidr(address + "/" + bitsToSplit);
            subnetDetail.setIpAddress(address);
            subnetDetail.setMaskBits(bitsToSplit);
            subnetDetail.setNetworkIpAddr(String.join(".", decimalSegmentsNetwork));
            subnetDetail.setBroadCastIpAddr(String.join(".", decimalSegmentsBroadcast));
            subnetDetail.setIpAddrV(4);
            subnetDetail.setBinaryMask(ipv4CreateBinaryMask(subnetDetail.getMaskBits()));
            subnetDetail.setMask(ipv4CreateMask(subnetDetail.getBinaryMask()));
            subnetDetail.setNumberOfHosts(splitedNumberOfHosts);
            
            totalNumberOfHosts += splitedNumberOfHosts;
            address = ipv4nextAddr(subnetDetail.getBroadCastIpAddr(), broadcastIpAddr, subnetDetail.getBroadCastIpAddr(), currentMaskBits);
            splitedSubnets.add(subnetDetail);
        }
        
        return splitedSubnets;
    } 

    public static List<SubnetDetail> ipv6Split(String networkAddress
            , int currentMaskBits, String broadcastIpAddr, int bitsToSplit) 
            throws InvalidArgumentException
    {
        List<SubnetDetail> splitedSubnets = new ArrayList<>();
        
        int currentNumberOfHosts = ipv6NumberOfHosts(currentMaskBits);
        int splitedNumberOfHosts = ipv6NumberOfHosts(bitsToSplit);
        int totalNumberOfHosts = 0;
        
        String address = networkAddress;
        while(totalNumberOfHosts + (splitedSubnets.size() * 2) != currentNumberOfHosts + 2){
            
            List<List<String>> binaryIPSegments = ipv6ParseToBinary(completeIPv6(networkAddress));
            SubnetDetail subnetDetail = new SubnetDetail(address + "/" + Integer.toString(bitsToSplit));
            
            int segementPos = 0;
        
            List<String> decimalSegmentsNetwork = new ArrayList<>();
            List<String> decimalSegmentsBroadcast = new ArrayList<>();
        
            //We iterate over the segments of the given binary ip address
            for (List<String> segment : binaryIPSegments) {
                String subnetSegmentNetwork = "";
                String subnetSegmentBroadcast = "";

                for (String bit : segment) {
                    if(segementPos >= bitsToSplit){
                        subnetSegmentNetwork += 0;
                        subnetSegmentBroadcast += 1;
                    }
                    else
                        subnetSegmentBroadcast = subnetSegmentNetwork += bit;

                    segementPos++;
                }
                decimalSegmentsNetwork.add(Integer.toString(Integer.parseInt(subnetSegmentNetwork, 2), 16));
                decimalSegmentsBroadcast.add(Integer.toString(Integer.parseInt(subnetSegmentBroadcast, 2), 16));
            }//end for
            
            subnetDetail.setCidr(address + "/" + bitsToSplit);
            subnetDetail.setIpAddress(address);
            subnetDetail.setMaskBits(bitsToSplit);
            subnetDetail.setNetworkIpAddr(compressIpv6(String.join(":", decimalSegmentsNetwork)));
            subnetDetail.setBroadCastIpAddr(compressIpv6(String.join(":", decimalSegmentsBroadcast)));
            subnetDetail.setIpAddrV(6);
            subnetDetail.setNumberOfHosts(splitedNumberOfHosts);
            
            totalNumberOfHosts += splitedNumberOfHosts;
            address = ipv6NextAddr(subnetDetail.getBroadCastIpAddr(), broadcastIpAddr, subnetDetail.getBroadCastIpAddr(), currentMaskBits);
            splitedSubnets.add(subnetDetail);
        }
        
        return splitedSubnets;
    } 

    
    private static List<String> segmentCalculation(String subnetSegment){
        List<String> segments = new ArrayList<>();
        int bits = subnetSegment.length();
        int x = Integer.parseInt(subnetSegment, 2);
        while(true){
            String segment = "";
            int diference = 0;
            if(Integer.toString(x, 2).length() > bits)
                break;
            if(Integer.toString(x, 2).length() < bits)
                diference = bits - Integer.toString(x, 2).length();
            for (int i = 0; i < diference; i++) 
                segment += "0";
           segment += Integer.toString(x, 2);
           segments.add(segment);
           x++;
        }
        return segments;
    }
    
    /**
     * For example in the IP 123.35.140.0/22 the 0 in third segment(140) is the 
     * first bit for subnetig so after calculate the possible subnet values 
     * we got in binary 00, 01, 10, 11 the complement means 
     * 14 + 00(0 in decimal) = 140 
     * 14 + 01(1 in decimal) = 141
     * 14 + 10(2 in decimal) = 142 
     * 14 + 11(3 in decimal) = 143
     * @param segment in this case it is 14
     * @param complement all the possible values for the subnet.
     * @return a list of all the possible subnet combinations for every segment
     */
    private static List<String> complement(String segment, List<String> complement){
        List<String> complements = new ArrayList<>();
        String first = segment;
        for (String bits : complement) 
            complements.add(first+bits);
        return complements;
    }
    
    /**
     * Parse to binary a given ip v4 address
     * @param ipAddress the given ip v4 address
     * @return 
     */
    public static List<List<String>> ipv4ParseToBinary(String ipAddress){
        List<String> binaryAddress = new ArrayList<>();
        List<String> singleSegment = new ArrayList<>();
        List<List<String>> binarySegments = new ArrayList<>();
        
        String[] splitedIpAddress = ipAddress.split("\\.");
        for (String segment : splitedIpAddress) {
            String segmen = Integer.toString(Integer.parseInt(segment), 2);
            //We add zeros to the left to complete the 8 positions
            if(segmen.length() < 8){
                for (int j = 0; j < 8-segmen.length(); j++) 
                    binaryAddress.add("0");
            }
            //we add the value after the zeros
            for (int j = 0; j < segmen.length(); j++) 
                binaryAddress.add("" + segmen.charAt(j));
        }
        //We split the binary ip address in a list of segments
        for(int i=0; i < binaryAddress.size(); i++){
            if(i % 8 == 0 && i != 0){
                binarySegments.add(singleSegment);
                singleSegment = new ArrayList<>();
            }
            singleSegment.add(binaryAddress.get(i));
        }
        binarySegments.add(singleSegment); //we add the last segment
        
        return binarySegments;
    }
    
    /**
     * Creates an IPv4 binary mask with the number of bits for the mask
     * @param maskBits number of bits
     * @return a list fo list with the IP mask
     */
    public static List<List<String>> ipv4CreateBinaryMask(int maskBits){
        //Create de mask in binary with de CIDR format!
        List<List<String>> binaryMask = new ArrayList<>();
        List<String> segment = new ArrayList<>();
        String bit = "1";
        for (int i = 1; i <= 32; i++) {
            if(i > maskBits)
                bit = "0";
            segment.add(bit); 
            if(i % 8 == 0){
                binaryMask.add(segment);
                segment = new ArrayList<>();
            }
        }
        return binaryMask;
    }
    
    /**
     * Creates an IPv4 binary mask with the number of bits for the mask
     * @param binaryMask
     * @return a list fo list with the IP mask
     */
    public static List<String> ipv4CreateMask(List<List<String>> binaryMask){
        List<String> decMask = new ArrayList<>();
        for (List<String> binSegment : binaryMask) {
            String conSegment = "";
            for (String bit : binSegment)
                conSegment += bit;
            decMask.add(Integer.toString(Integer.parseInt(conSegment, 2)));
        }
        return decMask;
    }
    
    /**
     * Creates a IPv6 mask with a given mask bits
     * @param maskBits number of bit for the mask
     * @return the mask
     */
    public static List<List<String>> createIpv6Mask(int maskBits){
        List<List<String>> binaryMask = new ArrayList<>();
        List<String> segment = new ArrayList<>();
        String bit = "1";
        int t = 0;
        String s = "";
        for (int i = 1; i <= 128; i++) {
            if(i > maskBits)
                bit = "0";
            s += bit;
            t++;
            if(t == 4){
                t = 0;
                segment.add(s); 
                s = "";
            }
            if(i % 16 == 0){
                binaryMask.add(segment);
                segment = new ArrayList<>();
            }
        }
        return binaryMask;
    }
    
    /**
     * Uncompress an IPv6
     * @param ip a compressed IP v6 Address
     * @return a complete IPv6
     */
    public static String[] completeIPv6(String ip){
        String[] shortIPAddress = ip.split(":");
        String[] ipAddress = {"0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000"};
        boolean flag = false;
        for (int g = 0; g<shortIPAddress.length;g++){ 
            if(shortIPAddress[g].isEmpty()){
                flag = true;
                break;
            }
            while(shortIPAddress[g].length()<4)
                shortIPAddress[g] = "0" + shortIPAddress[g];
            ipAddress[g] = shortIPAddress[g];
        }
        
        if(flag){
            int l = 7;
            for (int g = shortIPAddress.length - 1; g > 0 ;g--){ 
                if(shortIPAddress[g].isEmpty())
                    break;
                while(shortIPAddress[g].length()<4)
                    shortIPAddress[g] = "0" + shortIPAddress[g];
                ipAddress[l] = shortIPAddress[g];
                l--;
            }
        }
        return ipAddress;
    }

    /**
     * Parse an IPv6 to binary format
     * @param ip the IPv6 address
     * @return List for every segment of the IP address
     */
    private static List<List<String>> ipv6ParseToBinary(String[] ip){
        List<List<String>> binaryIPAddress = new ArrayList<>();
        for (String segment : ip) {
            List<String> segments = new ArrayList<>();
            for(int i=1; i<=segment.length();i++){
                if(segment.length() == 3)
                    segment = "0" + segment;
                if (segment.length() == 2) 
                    segment = "00" +segment;
                if (segment.length() == 1) 
                    segment = "000" + segment;
                String h = Integer.toString(Integer.parseInt(segment.substring(i-1, i), 16), 2);
                while(h.length() < 4)
                    h = "0" + h;
                segments.add(h);
            }
            binaryIPAddress.add(segments);
        }
        return binaryIPAddress;
    }
    
    public static void ipv6SubnetCalculation(SubnetDetail subnetDetail){
        String[] splitedCIDR = subnetDetail.getCidr().split("/");
        subnetDetail.setIpAddress(splitedCIDR[0]);
        subnetDetail.setMaskBits(Integer.parseInt(splitedCIDR[1]));
        subnetDetail.setNumberOfHosts(ipv6NumberOfHosts(subnetDetail.getMaskBits()));
        subnetDetail.setIpAddrV(6);
        
        List<List<String>> ip = ipv6ParseToBinary(completeIPv6(subnetDetail.getIpAddress()));
        List<String> segmentos;
        List<List<String>> temSubnets = new ArrayList<>();
        int i = 0;
        boolean flag = false;
        String netPart = "";
        String maskPart ="";
        for (List<String> segments : ip) {
            segmentos = new ArrayList<>();
            for (String segment : segments) {
                maskPart = "";
                for (int k =0; k < segment.length(); k++) {
                    if(i == subnetDetail.getMaskBits()){
                        maskPart = segment.substring(k);
                        netPart = segment.substring(0, k);
                        flag = true;
                        break;
                    }
                    i++;
                }        
                if(flag)
                    break;
                segmentos.add(segment);
            } 
            temSubnets.add(segmentos);
            if(flag)
                break;
        }
        List<String> calculation = segmentCalculation(maskPart);
        List<String> complement = complement(netPart, calculation);
        createIPv6(temSubnets, complement, subnetDetail);
    }
    
    private static String ipv6AsString(String[] ip){
        String ipv6 = "";
        for (String segment : ip) 
            ipv6 += segment + ":";
        return ipv6.substring(0, ipv6.length()-1);
    }
    
    /**
     * Creates a segment of a given ip address
     * @param ipAddr
     * @return a list of integer with all the possible values of the segment 
     * that it is been calculated
     */
    private static List<Integer> segmentAnIP(String ipAddr) {
        String segments[] = null;
        List<Integer>  theSegments= new ArrayList<>();
        if(ipAddr.contains("."))
            segments = ipAddr.split("\\.");
        else if(ipAddr.contains(":")){
            ipAddr = ipv6AsString(completeIPv6(ipAddr));
            segments = ipAddr.split(":");
            for (String segment : segments) 
                theSegments.add(Integer.parseInt(segment,16));
            return theSegments;
        }
        for (String segment : segments) 
           theSegments.add(Integer.parseInt(segment));
        return theSegments;
    }
    
    /**
     * calculate if a given networkIp for a subnet is inside of another subnet 
     * taking as parameters the broadcastIp an de networkIp
     * @param netwrokIp the network IP
     * @param broadcastIp the broadcast IP
     * @param ipAddr the possible Ip
     * @return true if it contained, false if not
     */
    public static boolean itContains(String netwrokIp, String broadcastIp, String ipAddr){
        List<Integer> binaryNetworkIp = segmentAnIP(netwrokIp);
        List<Integer> binarybroadcastIp = segmentAnIP(broadcastIp);
        List<Integer> binaryIp = segmentAnIP(ipAddr);
        boolean contains = false;
        for (int i = 0; i < binaryIp.size(); i++) {
            if((binaryIp.get(i) >= binaryNetworkIp.get(i)) && (binaryIp.get(i) <= binarybroadcastIp.get(i)))
                    contains = true;
            else{ 
                contains = false;
                break;
            }
        }
        return contains;
    }
    
    private static void createIPv6(List<List<String>> segments, List<String> complements, SubnetDetail subnetDetail){
        String ip = "";
        String[] nipAddress = {"0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000"};
        String[] bipAddress = {"ffff", "ffff", "ffff", "ffff", "ffff", "ffff", "ffff", "ffff"};
        boolean flag = true;
        List<String> partialSubnets =  new ArrayList<>();

        for (List<String> segment : segments) {
            if(segment.size()>0){
                for (String bits : segment) {
                    ip += Integer.toString(Integer.parseInt(bits, 2), 16);
                    if(segment.size()<4)
                        flag = false;
                }
                if(flag)
                    ip += ":";
            }
        }
        if(ip.length() > 0){
            if((ip.substring(ip.length() - 2, ip.length()-1)).equals(":"))
                ip = ip.substring(0, ip.length()-1);
        }
        
        for (String string : complements)
            partialSubnets.add(ip + Integer.toString(Integer.parseInt(string, 2),16));               
         
        String networkip = partialSubnets.get(0);
        String broadcastip = partialSubnets.get(partialSubnets.size()-1);
        String[] partialNetworkSplited = networkip.split(":");
        String[] partialBroadcastSplited = broadcastip.split(":");
        String n = "";
        String b = "";
        for(int i = 0; i < partialNetworkSplited.length; i++){
            n = partialNetworkSplited[i];
            b = partialBroadcastSplited[i];
            while(n.length() < 4){
                n += "0";
                b += "f";
            }
            nipAddress[i] = n;
            bipAddress[i] = b;
        }
        String subnet = "";
        for(int i = 0; i < nipAddress.length; i++)
            subnet += nipAddress[i]+":";
        subnetDetail.setNetworkIpAddr(subnet.substring(0, subnet.length()-1));
        subnet = "";
        for(int i = 0; i<bipAddress.length; i++)
            subnet += bipAddress[i]+":";
        subnetDetail.setBroadCastIpAddr(subnet.substring(0, subnet.length()-1));
    }

    /**
     * Checks if a given string is an ipv4 or an ipv6 valid ip address 
     * @param ipAddress a String with the possible ip address
     * @return true if the string is an ip address
     */
    public static boolean isIpAddress(String ipAddress){
        String ipv4Regex = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
        String ipv6Regex = "^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*";
        Pattern ipv4Pattern = Pattern.compile(ipv4Regex);
        Pattern ipv6Pattern = Pattern.compile(ipv6Regex);
        Matcher ipv4 = ipv4Pattern.matcher(ipAddress);
        Matcher ipv6 = ipv6Pattern.matcher(ipAddress);
        return ipv4.matches() || ipv6.matches();
    }
    
    
    /**
     * Checks if a given string is an ip address 
     * @param ipAddress a possible ip address
     * @return true if the string is an ip address
     */
    public static boolean isIpv4Address(String ipAddress){
        String ipv4Regex = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
        Pattern ipv4Pattern = Pattern.compile(ipv4Regex);
        Matcher ipv4 = ipv4Pattern.matcher(ipAddress);
        return ipv4.matches();
    }
    
    /**
     * Checks if a given string is an ip address 
     * @param ipAddress a possible ip address
     * @return true if the string is an ip address
     */
    public static boolean isIpv6Address(String ipAddress){
        String ipv6Regex = "^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*";
        Pattern ipv6Pattern = Pattern.compile(ipv6Regex);
        Matcher ipv6 = ipv6Pattern.matcher(ipAddress);
        return ipv6.matches();
    }
    
    /**
     * Checks if a given string complies with CIDR format
     * @param ipAddress a possible string with CIDR format: ipAddres / mask bits
     * @return true if the string has de CIDR format
     */
    public static boolean isCIDRFormat(String ipAddress){
        String ipv4Regex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\/([0-9]|[1-2][0-9]|3[0-2]))$";
        String ipv6Regex = "^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*(\\/([0-9]|[1-9][0-9]|1[0-1][0-9]|12[0-8]))?$";
        Pattern ipv4Pattern = Pattern.compile(ipv4Regex);
        Pattern ipv6Pattern = Pattern.compile(ipv6Regex);
        Matcher ipv4 = ipv4Pattern.matcher(ipAddress);
        Matcher ipv6 = ipv6Pattern.matcher(ipAddress);
        return ipv4.matches() || ipv6.matches();
    }
    
    public static boolean isHostname(String hostname){
        String hostnameRegex = "^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9-]*[a-zA-Z0-9]).)*([A-Za-z]|[A-Za-z][A-Za-z0-9-]*[A-Za-z0-9])$";
        Pattern hostnamePattern = Pattern.compile(hostnameRegex);
        Matcher mhostname = hostnamePattern.matcher(hostname);
        return mhostname.matches();
    }
    
    /**
     * Compress a complete IPv6 Address e.g.
     * compress  2abf:aaaa:00ff:0000:0000:0000:0:0 into 2abf:aaaa:ff::
     * @param ipAddr the IP v6 Address 
     * @return a compressed IPv6 address
     */
    public static String compressIpv6(String ipAddr){
        List<String> ipSegments = new ArrayList<>();
        String regex = "[0]*";
        Pattern pCeros = Pattern.compile(regex);
                
        String ceros = "";
        for (String segment : ipAddr.split(":")) {
            segment = Integer.toString(Integer.parseInt(segment, 16) , 16);
            if(segment.equals("0"))
                ceros += segment;
            else{
                if(!ceros.isEmpty()){
                    ipSegments.add(ceros);
                    ceros="";
                }
                ipSegments.add(segment);
            }
        }
        if(!ceros.isEmpty())
            ipSegments.add(ceros.substring(0, ceros.length()-1));
        
        int amountOfCeros = 0;
        boolean flag = true;
        int pos = 0;
        
        for (int i = 0; i < ipSegments.size(); i++) {
            if (pCeros.matcher(ipSegments.get(i)).matches()) {
                if(flag){
                    amountOfCeros = ipSegments.get(i).length();
                    flag =false;
                    pos = i;
                }
                else{
                    if(ipSegments.get(i).length() > amountOfCeros)
                        pos = i;
                }
            }
        }
        ipSegments.set(pos, "");
        String compressedIp = "";
        for (String segement : ipSegments)
            compressedIp += segement + ":";

        //if all the last segements are in cero we need double ::
        if(ipSegments.size() - 1 == pos && ipSegments.get(pos).isEmpty())
            return compressedIp;
        
        return compressedIp.substring(0, compressedIp.length() - 1);
    }
        
    /**
     * Calculate the next available IP Address
     * @param networkIp the subnet's network IP Address
     * @param ipAddr the IP Address
     * @param broadCastIp the subnet's broadcast IP Address
     * @param maskBits the subnet mask bits
     * @return the next available IP Address
     */
    public static String ipv4nextAddr(String networkIp, String broadCastIp, String ipAddr, int maskBits){
        String[] splitedIp = ipAddr.split("\\.");

        for(int i = splitedIp.length-1; i > 0; i--) {
            if(ipv4addrBelongsToSubnet(networkIp, ipAddr, maskBits) ||  !broadCastIp.equals(ipAddr)){
                int bit = Integer.parseInt(splitedIp[i]);
                if(bit == 255)
                    splitedIp[i] = "0";
                else{
                    bit++;
                    splitedIp[i] = Integer.toString(bit);
                    break;
                }
            }
            else
                return null;
        }
        ipAddr="";
        
        for (String segment : splitedIp) 
            ipAddr += segment + ".";
        
        return ipAddr.substring(0, ipAddr.length() - 1);
    }
   
    /**
     * Calculate the next available IP Address
     * @param networkIp the subnet's network IP Address
     * @param ipAddr the IP Address
     * @param broadCastIp the subnet's broadcast IP Address
     * @param maskBit the subnet mask bits
     * @return the next available IP Address
     */
    public static String ipv6NextAddr(String networkIp, String broadCastIp, String ipAddr, int maskBit){
        String[] splitedIp = completeIPv6(ipAddr);
        
        String nextIp = "";
        for(int i = splitedIp.length-1; i>=0; i--) {
            if(ipv6AddrBelongsToSubnet(networkIp, ipAddr, maskBit)){
                int bit = Integer.parseInt(splitedIp[i],16);
                if(bit == 0xffff)
                    splitedIp[i] = "0000";
                else{
                    bit++;
                    splitedIp[i] = Integer.toString(bit,16);
                    for (String segment : splitedIp) 
                        nextIp += Integer.toString(Integer.parseInt(segment,16),16)+":";
                    nextIp = nextIp.substring(0, nextIp.length()-1);
                    break;
                }
            }
            else
                return null;
        }
        return compressIpv6(nextIp);
    }
    
    /**
     * calculate if a given networkIp for a subnet is inside of another subnet 
     * taking as parameters the networkIp
     * @param networkIp the subnet's network ip address
     * @param maskBits the subnet's numbers of bits
     * @param ipAddr the possible ip address
     * @return true if it contained, false if not
     */
    public static boolean ipv4addrBelongsToSubnet(String networkIp, String ipAddr, int maskBits){
        List<List<String>> binaryIp = ipv4ParseToBinary(ipAddr);
        List<List<String>> binaryNetworkIp = ipv4ParseToBinary(networkIp);
        List<List<String>> binaryMask = ipv4CreateBinaryMask(maskBits);
        int bit = 0;
        boolean flag = false;
        for (int i = 0; i < binaryIp.size(); i++) {
            List<String> ipSegment = binaryIp.get(i);
            List<String> networkIpSegment = binaryNetworkIp.get(i);
            List<String> maskIpSegment = binaryMask.get(i);
            for(int j = 0; j < ipSegment.size();j++){
                if(bit == maskBits){
                    flag=true;
                    break;
                }
                int x = Integer.parseInt(maskIpSegment.get(j),2) - Integer.parseInt(ipSegment.get(j),2);
                int y = Integer.parseInt(maskIpSegment.get(j),2) - Integer.parseInt(networkIpSegment.get(j),2);
                if (x != y)
                    return false;
                bit++;
            }
            if(flag)
                break;
        }
        return true;
    }
    
    /**
     * Calculate if a given IP for a subnet is inside of another subnet 
     * taking as parameters the broadcastIp an de networkIp
     * @param networkIp the network IP
     * @param ipAddr the possible Ip
     * @param maskBits
     * @return true if it contained, false if not
     */
    public static boolean ipv6AddrBelongsToSubnet(String networkIp, String ipAddr, int maskBits){
        List<List<String>> binaryIp = ipv6ParseToBinary(completeIPv6(ipAddr));
        List<List<String>> binaryNetworkIp = ipv6ParseToBinary(completeIPv6(networkIp));
        List<List<String>> binaryMask = createIpv6Mask(maskBits);
        int bit = 0;
        boolean flag = false;
        for (int i = 0; i<binaryIp.size(); i++) {
            List<String> ipSegment = binaryIp.get(i);
            List<String> networkIpSegment = binaryNetworkIp.get(i);
            List<String> maskIpSegment = binaryMask.get(i);
            for(int j = 0; j<ipSegment.size();j++){
                if(bit == maskBits){
                    flag=true;
                    break;
                }
                for (int k = 1; k <= maskIpSegment.get(j).length(); k++) {
                    int x = Integer.parseInt(maskIpSegment.get(j).substring(k-1,k),2) - Integer.parseInt(ipSegment.get(j).substring(k-1,k),2);
                    int y = Integer.parseInt(maskIpSegment.get(j).substring(k-1,k),2) - Integer.parseInt(networkIpSegment.get(j).substring(k-1,k),2);
                    if(bit == maskBits){
                        flag=true;
                        break;
                    }
                    if (x!= y)
                        return false;
                    bit++;
                }
            }
            if(flag)
                break;
        }
        return true;
    }
    
    
    private static int ipv4NumberOfHosts(int maskBits){
        int n = 32 - maskBits;
        return (int)(Math.pow(2, n)-2);
    }
    
    private static int ipv6NumberOfHosts(int maskBits){
        int n = 128 - maskBits;
        return (int)(Math.pow(2, n));
    }
    
    /**
     * Splits a given ip address
     * @param ipAddr the given ip address
     * @param segment the wished segment 0 or 1 or 2 or 3
     * @return the segment of the ip address
     */
    public static String getIpv4Segment(String ipAddr, int segment){
        if(isIpv4Address(ipAddr)){
            String[] segments = ipAddr.split("\\.");
            if(segments.length == 4 && segment >= 0 && segment <= 3)
                return segments[segment];
        }
        return "";
    }
    
    /**
     * Splits a given ip address
     * @param ipAddr the given ip address
     * @return the segment of the ip address
     */
    public static String getIpv4Completed(String ipAddr){
        List<String> completedIpAddr = new ArrayList<>();        
        if(isIpv4Address(ipAddr)){
            String[] segments = ipAddr.split("\\.");
            if(segments.length == 4){
                for (String segment : segments) {
                    if(segment.length() == 2)
                        segment = "0" + segment;
                    else if(segment.length() == 1)
                        segment = "00" + segment;
                    completedIpAddr.add(segment);
                }
            }
        }
        return String.join(".", completedIpAddr);
    }

    /**
     * Checks if the broadcast ip and the network ip of a given new subnet 
     * is inside of other subnet
     * @param newNetworkIpAddr
     * @param newBoradcstIpAddr
     * @param subnetContainer the alleged subnet that could contains the new subnet
     * @return true if contains completely the new subnet
     * @throws org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException
     */
    public boolean ipv4SubnetOverlaps(String newNetworkIpAddr
            , String newBoradcstIpAddr, SubnetDetail subnetContainer) throws InvalidArgumentException{
        
//        String[] splitNewSubnet = cidr.split("/");
//        String[] splitSubnetFather = subnetContainer.getCidr().split("/");
//        
//        if(Integer.valueOf(splitNewSubnet[1]) < Integer.valueOf(splitSubnetFather[1]))
//            return false;
//        else{
//            ipv4SubnetCalculation(cidr);
//            String newSubnetBroadcastIp = subnets.get(subnets.size()-1);
//            String newSubnetNetworkIp = subnets.get(0);
//            
//            ipv4SubnetCalculation(subnetContainer);
//            String subnetFatherNetworkIp = subnets.get(0);
//            
//            if(ipv4addrBelongsToSubnet(subnetFatherNetworkIp, newSubnetNetworkIp, Integer.valueOf(splitSubnetFather[1])))
//                return ipv4addrBelongsToSubnet(subnetFatherNetworkIp, newSubnetBroadcastIp, Integer.valueOf(splitSubnetFather[1]));
//            else
                return false;
//        }
    }
    
    /**
     * Checks if the broadcast ip and the network ip of a given new subnet 
     * is inside of other subnet
     * @param newSubnet a given new subnet in CIDR format
     * @param allegedSubnetContainer the alleged subnet that could contains the new subnet
     * @return true if contains completely the new subnet
     */
    public static boolean ipv6SubnetsOvelaps(String possibleSubnetContainerCidr
            , String possibleContainedSubnetNetworkAddress
            , String possibleContainedSubnetBroadcastAddress)
    {
        if(isCIDRFormat(possibleSubnetContainerCidr)){
            String possibleSubnetContainerNetworkAddress = possibleSubnetContainerCidr.split("/")[0];
            int possibleContainerSubnetMaskBits = Integer.valueOf(possibleSubnetContainerCidr.split("/")[1]);
            
            if(ipv6AddrBelongsToSubnet(possibleSubnetContainerNetworkAddress, possibleContainedSubnetNetworkAddress, possibleContainerSubnetMaskBits))
                return ipv6AddrBelongsToSubnet(possibleSubnetContainerNetworkAddress, possibleContainedSubnetBroadcastAddress, possibleContainerSubnetMaskBits);
        }
            return false;
    }
    
    /**
     * Splits the subnet from its CDIR format into the single ip address
     * @param cidrSubnet an String that contains the subnet in CDIR format networkAddress/Mask bits
     * @return the subnet without mask
     */
    public static String getSubnetIpAddr(String cidrSubnet){
        String[] cidrSplit = cidrSubnet.split("/");
        if(cidrSplit.length == 2)
            return cidrSplit[0];
        else 
            return null; //it should not happen
    }
    
    /**
     * Returns a part of the ip address to search if ther is any match with 
     * this part
     * @param cidrSubnet the subnet address in cidr format
     * @return a part of the subnet address
     */
    public static String getPartialSubnetIpAddr(String cidrSubnet, int version){
        List<String> address = new ArrayList<>();
        if(isCIDRFormat(cidrSubnet)){
            int segementPos = 0;
            String[] split =cidrSubnet.split("/");
            List<List<String>> binaryIPSegments = new ArrayList<>();
            if(version == 4)
                binaryIPSegments = IpamEngine.ipv4ParseToBinary(split[0]);
            else if(version == 6)
                binaryIPSegments = ipv6ParseToBinary(completeIPv6(split[0]));
            
            for (List<String> segment : binaryIPSegments) {
                String subnetSegmentNetwork = "";

                for (String bit : segment) {
                    if(segementPos == Integer.valueOf(split[1]))
                        break;
                    else
                        subnetSegmentNetwork += bit;

                    segementPos++;
                }
                if(!subnetSegmentNetwork.isEmpty()){
                    if(version == 4)
                        address.add(Integer.toString(Integer.parseInt(subnetSegmentNetwork, 2)));
                    if(version == 6)
                        address.add(Integer.toString(Integer.parseInt(subnetSegmentNetwork, 2), 16));
                }
                else 
                    break;
            }
        }
        if (version == 4)
            return String.join(".", address);
        else if (version == 6)
            return String.join(":", address);
        else
            return null;
    }
}
