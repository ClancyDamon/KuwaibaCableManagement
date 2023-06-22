/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.core.persistence.reference.extras.sync;

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.snmp4j.smi.OID;

/**
 *
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SyncUtil {
    /**
     * Version of the XML format used to store this view (see getAsXML method)
     */
    private final static String VIEW_FORMAT_VERSION = "1.0";
    
    /**
     * To Filter which if names could be synchronized
     * @param ifName the port or interface name
     * @return boolean if should be synchronized false if not
     */
    public static boolean isSynchronizable(String ifName){
        //avoids eo, cpp, span, dwdm
        return ifName.matches("\\d+") ||
               ifName.toLowerCase().equals("gi0") || 
               ifName.toLowerCase().startsWith("lo") ||
               ifName.startsWith("Po") ||
               (ifName.toLowerCase().contains("po") && ifName.contains("/")) || 
                ifName.toLowerCase().startsWith("se") || 
                ifName.toLowerCase().startsWith("tu") ||
                ifName.toLowerCase().startsWith("vl") ||
                ifName.toLowerCase().startsWith("br") ||
                ifName.toLowerCase().startsWith("bd") ||
                ifName.toLowerCase().contains("vlan") ||
                ifName.contains("/");
    }
   
    /**
     * Checks if the object is a port an wraps the name into a 
     * standardized name
     * @param obj a given object
     * @return the object with the port name wrapped
     */
    public static BusinessObject wrapPortName(BusinessObject obj){
        if(SyncUtil.isSynchronizable(obj.getName()) && 
                obj.getClassName().toLowerCase().contains("port") && 
                !obj.getName().contains("Power") && 
                !obj.getClassName().contains("Power"))
            obj.setName(normalizePortName(obj.getName()));
       
        return obj;
    }
    
    /**
     * Checks if the object is a port an wraps the name into a 
     * standardized name
     * @param obj a given object
     * @return the object with the port name wrapped
     */
    public static BusinessObjectLight wrapPortName(BusinessObjectLight obj){
        if(SyncUtil.isSynchronizable(obj.getName()) && 
                obj.getClassName().toLowerCase().contains("port") && 
                !obj.getName().contains("Power") && 
                !obj.getClassName().contains("Power"))
            obj.setName(normalizePortName(obj.getName()));
       
        return obj;
    }
        
    /**
     * Cleans de VFI names removes de (VFI)
     * @param rawVfi name raw vfi name
     * @return normalized vfi name 
     */
    public static String normalizeVfiName(String rawVfi){
        return rawVfi.contains("(") ? rawVfi.substring(0, rawVfi.indexOf("(")) : rawVfi;
    }
    
    /**
     * Wraps the port name into a standardized port name gi, te, fa, pos
     * @param interfaceName raw port name
     * @return normalized port name 
     */
    public static String normalizePortName(String interfaceName){
        


        //the port channel always have a capital P and lower o
        if(!interfaceName.startsWith("Po")){
            interfaceName = interfaceName.toLowerCase();
            interfaceName = interfaceName.replace("_", "/"); //could happend in some mibs
            //0/RP0-GigabitEthernet0/0/0/0 we remove the uneccesary part of the por name
            if(interfaceName.startsWith("0/rp0-"))
                interfaceName = interfaceName.substring(interfaceName.indexOf("-")+1);
            //Pseudowires
            if(interfaceName.contains("pw"))
                return interfaceName.replace("\\s", "");
            //mpls tunnel
            if(interfaceName.contains("tunnel-te"))
                return interfaceName.replace("tunnel-te", "tu");

            if(interfaceName.contains(".si"))  //is a service instance
                interfaceName = interfaceName.split("\\.")[2];
            else if (interfaceName.toLowerCase().contains(".") && interfaceName.split("\\.").length == 2) //is a virtualPort        
                interfaceName = interfaceName.split("\\.")[1];

            if(interfaceName.toLowerCase().startsWith("lo") && interfaceName.length() < 6) //is a loopback
                return interfaceName.replace("lo", "loopback");

            //Fastethernet
            if(interfaceName.contains("fastethernet"))
                return interfaceName.replace("fastethernet", "fa");
            //Te
            if(interfaceName.contains("tengigabitethernet"))
                return interfaceName.replace("tengigabitethernet", "te");  
            if(interfaceName.contains("tengige"))
                return interfaceName.replace("tengige", "te");
            if(interfaceName.contains("tentigt"))
                return interfaceName.replace("tentigt", "te");
            if(interfaceName.contains("tengig"))
                return interfaceName.replace("tengig", "te");
            if(interfaceName.contains("tengi"))
                return interfaceName.replace("tengi", "te");   

            //POS and PO
            if(interfaceName.contains("pos"))
                return interfaceName;
            if(interfaceName.contains("po"))
                return interfaceName.replace("po", "pos");
            //Gi Ge Gigabitethernet
            if(interfaceName.contains("gigabitethernet"))
                return interfaceName.replace("gigabitethernet", "gi");
            if(interfaceName.contains("gi"))
                return interfaceName.replace("gi", "gi");
            if(interfaceName.startsWith("ge "))
                return interfaceName.toLowerCase().replace("ge ", "gi");
            if(interfaceName.startsWith("ge"))
                return interfaceName.toLowerCase().replace("ge", "gi");
            if(interfaceName.startsWith("g"))
                return interfaceName.toLowerCase().replace("g", "gi");
            //Serial Port
            if(interfaceName.contains("se"))
                return interfaceName;
        }
        
        return interfaceName;
        
    }
    
    /**
     * Compare the old port names with the new name, the first load of the SNMP
     * sync depends of the name of the ports because this names are the only
     * common point to start the search and update/creation of the device.
     * Supported cases:  Ge, gi1/1/13, Gi8/18, GigabitEthernet0/9, 
     * Tengi0/0/0, POS0/1/0
     * @param oldName the old port name
     * @param oldClassName the old port class
     * @param newName the new port name
     * @param newClassName the new port class
     * @return boolean if the name match
     */
    public static boolean compareLegacyPortNames(String oldName, String oldClassName, String newName, String newClassName) {
        if (oldClassName.equals(newClassName)) {
            oldName = oldName.toLowerCase().trim();
            newName = newName.toLowerCase().trim();
            if (!oldName.equals(newName)) {
                
                String[] splitOldName;
                if(!oldName.contains("/") && !newName.contains("/"))
                    return newName.trim().contains(oldName.toLowerCase().replace("port", "").trim());
                
                splitOldName = oldName.toLowerCase().split("/");
                String[] splitNewName = newName.toLowerCase().split("/");

                boolean allPartsAreEquals = true;
                if (splitNewName.length == splitOldName.length) {
                    for (int i = 0; i < splitOldName.length; i++) {
                        if (!splitOldName[i].equals(splitNewName[i]))
                            allPartsAreEquals = false;
                    }
                    if (allPartsAreEquals) 
                        return true;

                    //first part
                    boolean firstPart= false;
                    String oldPart1 = splitOldName[0];
                    oldPart1 = oldPart1.replaceAll("[-._:,]", "");
                    
                    String newPart1 = splitNewName[0];
                    
                    newPart1 = newPart1.replaceAll("[-._:,]", "");
                    if (oldPart1.equals(newPart1))
                        firstPart = true;
                    
                    else if(newPart1.contains("tentigt")) //TenGigE
                        newPart1 = newPart1.replace("tentigt", "tt");
                    else if(newPart1.contains("tengige")) //TenGigE
                        newPart1 = newPart1.replace("tengige", "te");
                    else if(newPart1.contains("teabitethernet")) //TenGigE
                        newPart1 = newPart1.replace("teabitethernet", "te");
                    else if(newPart1.contains("gigabitethernet"))
                        newPart1 = newPart1.replace("gigabitethernet", "ge");
                    else if(newPart1.contains("mgmteth"))
                        newPart1 = newPart1.replace("mgmteth", "mg");
                    else if(newPart1.contains("gi") && newPart1.length() < 4)
                        newPart1 = newPart1.replace("gi", "ge");
                    else if(newPart1.contains("tengi"))
                        newPart1 = newPart1.replace("tengi", "te");
                    
                    if (oldPart1.replaceAll("\\s+","").equals(newPart1.replaceAll("\\s+","")) && !firstPart)
                            firstPart = true;

                    //the other parts
                    boolean lastPartAreEquals = true;
                    if (splitOldName.length > 1 && splitNewName.length > 1) {
                        for (int i = 1; i < splitOldName.length; i++) {
                            if (!splitOldName[i].equals(splitNewName[i])) 
                                lastPartAreEquals = false;
                        }
                    }
                    return (firstPart && lastPartAreEquals) ;
                   
                } else 
                    return false;
            }//end kind of port optical
            else 
                return true;
        }
        return false;
    }
    
    /**
     * Compares the old and the new list of attributes an return the changes
     * @param oldObjectAttributes the old list of attributes
     * @param newObjectAttributes the list read it from SNMP
     * @return the a map with the attributes changed
     */
    public static HashMap<String, String> compareAttributes(HashMap<String, String> oldObjectAttributes, HashMap<String, String> newObjectAttributes){
        HashMap<String, String> updatedAttributes = new HashMap<>();
        for (String attributeName : newObjectAttributes.keySet()) {
            String newAttributeValue = newObjectAttributes.get(attributeName);
            if (oldObjectAttributes.containsKey(attributeName)) {
                String oldAttributeValues = oldObjectAttributes.get(attributeName);
                if (oldAttributeValues != null && newAttributeValue != null) {
                    if (!oldAttributeValues.equals(newAttributeValue)) 
                        updatedAttributes.put(attributeName, newAttributeValue);
                }
            } else
                updatedAttributes.put(attributeName, newAttributeValue);//an added attribute
        }
        return updatedAttributes;
    }
    
    /**
     * Compares the old and the new list of attributes an return the changes
     * @param oldObjectAttributes the old list of attributes
     * @param newObjectAttributes the list read it from SNMP
     * @return the a map with the attributes changed
     */
    public static HashMap<String, String> compareAttributes(HashMap<String, String> oldObjectAttributes,  JsonObject newObjectAttributes){
        HashMap<String, String> updatedAttributes = new HashMap<>();
        for (String newAttributeName : newObjectAttributes.keySet()) {
            String newAttributeValue = newObjectAttributes.getString(newAttributeName);
            if (oldObjectAttributes.containsKey(newAttributeName)) {
                String oldAttributeValues = oldObjectAttributes.get(newAttributeName);
                if (oldAttributeValues != null && newAttributeValue != null) {
                    if (!oldAttributeValues.equals(newAttributeValue)) 
                        updatedAttributes.put(newAttributeName, newAttributeValue);
                }
            } else
                updatedAttributes.put(newAttributeName, newAttributeValue);//an added attribute
        }
        return updatedAttributes;
    }
    
    /**
     * Parse a hash map with the attributes of the objects to a JSON format
     * @param attributes the attributes to create the JSON
     * @return a JSON object with the attributes
     */
    public static JsonObject parseAttributesToJson(HashMap<String, String> attributes) {
        JsonObject jsonObj = Json.createObjectBuilder().build();
        for (String key : attributes.keySet()) 
            jsonObj = SyncUtil.joBuilder(jsonObj).add(key, attributes.get(key).trim()).build();

        return jsonObj;
    }
    
    /**
     * Parse a hash map with the attributes of the objects to a JSON format
     * @param attributes the attributes to create the JSON
     * @return a JSON object with the attributes
     */
    public static JsonObject parseOldAttributesToJson(HashMap<String, String> attributes) {
        JsonObject jsonObj = Json.createObjectBuilder().build();
        for (String key : attributes.keySet()) 
            jsonObj = SyncUtil.joBuilder(jsonObj).add(key, attributes.get(key).trim()).build();

        return jsonObj;
    }
    
    //JSON helpers
    /**
     * Utility that allow to edit a created jSON
     * @param jo created json object
     * @return the edited JSON with new fields
     */
    public static JsonObjectBuilder joBuilder(JsonObject jo) {
        JsonObjectBuilder job = Json.createObjectBuilder();

        for (Map.Entry<String, JsonValue> entry : jo.entrySet()) 
            job.add(entry.getKey(), entry.getValue());

        return job;
    }

    /**
     * Utility that allow to edit a created jSON Array
     * @param ja created json array
     * @return the edited JSON with new fields
     */
    public static JsonArrayBuilder jArrayBuilder(JsonArray ja) {
        JsonArrayBuilder jao = Json.createArrayBuilder();
        for (JsonValue v : ja) 
            jao.add(v);
        
        return jao;
    }
    
    //Helper for providers
    /**
     * In providers, maps the data table got from SNMP into a Map to 
     * @param mibTableName
     * @param mibTable
     * @param tableAsString
     * @return 
     */
    public static HashMap<String, List<String>> parseMibTable(String mibTableName, 
            HashMap<String, OID> mibTable, List<List<String>> tableAsString){
        HashMap<String, List<String>> value = new HashMap();
        int i = 0;
        
        for (String mibTreeNodeName : mibTable.keySet()) {                        
            List<String> currentColumn = new ArrayList();

            for (List<String> cell : tableAsString)
                currentColumn.add(cell.get(i));

            value.put(mibTreeNodeName, currentColumn);
            i++;                            
        }

        int size = mibTable.keySet().size();
        List<String> instances = new ArrayList();
        for (List<String> cell : tableAsString)
            instances.add(cell.get(size));
        
        value.put(mibTableName, instances);
        
        return value;
    }
    
    /**
     * Updates the MPLS view after sync
     * @param structure MPLS view structure
     * @param device device under sync
     * @param side which side is been sync
     * @return updated vMPLS view structure
     */
    public static byte[] updateView(byte[] structure, BusinessObjectLight device, int side){
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        QName aside = new QName("aside"); //NOI18N
        QName bside = new QName("bside"); //NOI18N
        QName qEdge = new QName("edge"); //NOI18N
        QName qClass = new QName("class"); //NOI18N
        QName qNode = new QName("node"); //NOI18N
        List<Attribute> newAttributes;
        Iterator attributes;
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLEventReader reader = inputFactory.createXMLEventReader(bais);
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            XMLEventWriter writer = xof.createXMLEventWriter(baos);
            XMLEventFactory ef = XMLEventFactory.newInstance();
            StartElement router = null;
            while (reader.hasNext()) {
                XMLEvent event = (XMLEvent) reader.next();
                if (event.isCharacters() && router != null) {
                    String data = event.asCharacters().getData();
                    if (data.contains("-1"))
                        event = ef.createCharacters(device.getId());
                    writer.add(event);
                }else if (event.isStartElement()) {
                    StartElement s = event.asStartElement();
                    s.getAttributes();
                    String tagName = s.getName().getLocalPart();
                    switch (tagName) {
                        case "node":
                            router = s;
                            newAttributes = new ArrayList();
                            attributes = s.getAttributes();
                            boolean mustEdit = false;
                            while(attributes.hasNext()){
                                Attribute next = (Attribute) attributes.next();
                                if(next.getName().equals(qClass) && next.getValue().contains("sync")){
                                    newAttributes.add(ef.createAttribute(qClass, device.getClassName()));
                                    mustEdit = true;
                                }
                                else
                                    newAttributes.add(next);
                            }
                            if(mustEdit)
                                event = ef.createStartElement(qNode, newAttributes.iterator(), s.getNamespaces());
                            writer.add(event);
                            break;
                        case "edge":
                            newAttributes = new ArrayList();
                            attributes = s.getAttributes();
                            while(attributes.hasNext()){
                                Attribute next = (Attribute) attributes.next();
                                
                                if(side == 1 && next.getName().equals(aside))
                                    newAttributes.add(ef.createAttribute(aside, device.getId()));
                                else if(side == 2 && next.getName().equals(bside))
                                    newAttributes.add(ef.createAttribute(bside, device.getId()));
                                else
                                    newAttributes.add(next);
                            }   
                            event = ef.createStartElement(qEdge, newAttributes.iterator(), s.getNamespaces());
                            writer.add(event);
                            break;
                        default:
                            writer.add(event);
                            break;
                    }
                } else {
                    writer.add(event);
                }
            }
            reader.close();
            writer.flush();
            
        } catch (NumberFormatException | XMLStreamException ex) {
            ex.printStackTrace();
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Creates a MPLS view if doesn't exist 
     * @param devices the device under sync
     * @param link the new MPLS link
     * @return a new MPLS view structure
     */
    public static byte[] createMplsView(List<BusinessObjectLight> devices, 
             BusinessObjectLight link) 
    {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameView = new QName("view");
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), VIEW_FORMAT_VERSION)); // NOI18N
            
            QName qnameClass = new QName("class");
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters("MPLSLink")); //NOI18N
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            //Device
            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            int x = 20, y = 200;
            for(BusinessObjectLight device : devices){
                QName qnameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(x)));
                xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(y)));

                xmlew.add(xmlef.createAttribute(new QName("class"), device.getClassName()));
                xmlew.add(xmlef.createCharacters(device.getId()));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
                x += 194; y +=18;
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            //Link
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            QName qnameEdge = new QName("edge");
            xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
            xmlew.add(xmlef.createAttribute(new QName("id"), link.getId()));
            xmlew.add(xmlef.createAttribute(new QName("class"), link.getClassName()));

            xmlew.add(xmlef.createAttribute(new QName("aside"), devices.get(0).getId()));
            xmlew.add(xmlef.createAttribute(new QName("bside"), devices.get(1).getId()));
            x=114; y=260; 
            for (int i=0; i<2; i++) {
                QName qnameControlpoint = new QName("controlpoint");
                xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(x)));
                xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(y)));
                xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                x += 439;
            }
            xmlew.add(xmlef.createEndElement(qnameEdge, null));
            
            xmlew.add(xmlef.createEndElement(qnameEdges, null));
            
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
